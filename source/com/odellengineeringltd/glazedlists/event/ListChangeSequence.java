/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.event;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// for keeping a list of changes
import java.util.List;
import java.util.ArrayList;
// to prevent two changes at once
import java.util.ConcurrentModificationException;

/**
 * Models a continuous stream of changes on a list. Changes of the same type
 * that occur on a continuous set of rows are grouped into blocks
 * automatically for performance benefits.
 *
 * <p>Atomic sets of changes may involve many lines of changes and many blocks
 * of changes. They are committed to the queue in one action. No other threads
 * should be creating a change on the same list change queue when an atomic
 * change is being created.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ListChangeSequence {
    
    /** the list of lists of change blocks */
    private ArrayList atomicChanges = new ArrayList();
    private int oldestChange = 0;
    /** the total count of simple changes */
    private int changeCount = 0;
    /** the total number of change blocks */
    private int blockCount = 0;
    
    /** the current working copy of the atomic change */
    private ArrayList atomicChangeBlocks = null;
    /** the atomic change's count of change blocks */
    private int atomicChangeCount = 0;
    /** the most recent list change; this is the only change we can append to */
    private ListChangeBlock atomicLatestBlock = null;
    
    /** the pool of list change objects */
    private ArrayList changePool = new ArrayList();
    
    /** the sequences that provide a view on this queue */
    private ArrayList listeners = new ArrayList();
    private ArrayList listenerEvents = new ArrayList();
    
    
    /**
     * Starts a new atomic change to this list change queue.
     *
     * To prevent two simultaneous atomic changes, the atomicChangeBlocks
     * arraylist is used as a flag. If it is null, there is no change taking
     * place. Otherwise there is a conflicting change and a
     * ConcurrentModificationException is thrown.
     */
    public synchronized void beginAtomicChange() {
        if(atomicChangeBlocks != null) {
            throw new java.util.ConcurrentModificationException("Cannot change this list while another change is taking place");
        }
        atomicChangeBlocks = new ArrayList();
        atomicChangeCount = 0;
        atomicLatestBlock = null;
        
        // attempt to reclaim some changes for the change pool
        int oldestRequiredChange = atomicChanges.size(); 
        for(int e = 0; e < listenerEvents.size(); e++) {
            ListChangeEvent listChangeEvent = (ListChangeEvent)listenerEvents.get(e);
            int eventOldestChange = listChangeEvent.getAtomicChangeCount();
            if(eventOldestChange < oldestRequiredChange) {
                oldestRequiredChange = eventOldestChange;
            }
        }
        // recycle every change that is no longer used
        for(int i = oldestChange; i < oldestRequiredChange; i++) {
            List recycledChanges = (List)atomicChanges.get(i);
            changePool.addAll(recycledChanges);
            atomicChanges.set(i, null);
        }
        // now we have reclaimed all these change objects
        oldestChange = oldestRequiredChange;
    }
        
    /**
     * Adds the specified change to the set of list changes. The change
     * will be merged with the most recent change if possible, otherwise a
     * new change will be created and added.
     *
     * One or more calls to this method must be prefixed by a call to
     * beginAtomicChange() and followed by a call to commitAtomicChange().
     */
    public void appendChange(int index, int type) {
        appendChange(index, index, type);
    }

    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * One or more calls to this method must be prefixed by a call to
     * beginAtomicChange() and followed by a call to commitAtomicChange().
     */
    public void appendChange(int startIndex, int endIndex, int type) {
        // increment the size
        atomicChangeCount = atomicChangeCount + (endIndex - startIndex + 1);
        
        // create a new change for the first change
        if(atomicLatestBlock == null) {
            atomicLatestBlock = createListChangeBlock(startIndex, endIndex, type);
            atomicChangeBlocks.add(atomicLatestBlock);
            return;
        }
        
        // append the change if possible
        ListChangeBlock appended = atomicLatestBlock.append(startIndex, endIndex, type);
        // if appended is null the changes couldn't be merged
        if(appended == null) {
            appended = createListChangeBlock(startIndex, endIndex, type);
            atomicChangeBlocks.add(appended);
            atomicLatestBlock = appended;
        }
    }
    
    /**
     * Commits the current atomic change to this list change queue. This will
     * notify all listeners about the change.
     */
    public synchronized void commitAtomicChange() {
        // do a 'real' commit only on non-empty changes
        if(atomicChangeBlocks.size() > 0) {
            // sort and simplify this block
            sortChangeBlocks(atomicChangeBlocks);
            // add this to the complete set
            atomicChanges.add(atomicChangeBlocks);
            changeCount = changeCount + atomicChangeCount;
            blockCount = blockCount + atomicChangeBlocks.size();
            
            // notify listeners
            for(int i = 0; i < listeners.size(); i++) {
                ListChangeListener listener = (ListChangeListener)listeners.get(i);
                ListChangeEvent event = (ListChangeEvent)listenerEvents.get(i);
                listener.notifyListChanges(event);
            }
        }

        // clear the change for the next caller
        atomicChangeBlocks = null;
        atomicChangeCount = 0;
        atomicLatestBlock = null;
    }


    /**
     * For pooling list change objects. This gets a new list change
     * object or recycles one if that exists.
     */
    private ListChangeBlock createListChangeBlock(int startIndex, int endIndex, int type) {
        if(changePool.isEmpty()) return new ListChangeBlock(startIndex, endIndex, type);
        ListChangeBlock listChange = (ListChangeBlock)changePool.remove(changePool.size() - 1);
        listChange.setData(startIndex, endIndex, type);
        return listChange;
    }
    
    /**
     * Gets the total number of blocks in the specified atomic change.
     */
    public int getBlockCount(int atomicCount) {
        List atomicChange = (List)atomicChanges.get(atomicCount);
        return atomicChange.size();
    }

    /**
     * Gets the total number of atomic changes so far.
     */
    public int getAtomicCount() {
        return atomicChanges.size();
    }

    /**
     * Gets the total number of simple changes seen so far.
     */
    public int getChangeCount() {
        return changeCount;
    }
    
    /**
     * Gets the specified block from the current set of change blocks. Note
     * that not blocks may be cleaned up after they are viewed by all
     * monitoring sequences. Such blocks may not be available.
     */
    public ListChangeBlock getBlock(int atomicCount, int block) {
        List atomicChange = (List)atomicChanges.get(atomicCount);
        return (ListChangeBlock)atomicChange.get(block);
    }
    
    /**
     * Registers the specified listener to be notified whenever new changes
     * are appended to this list change sequence.
     *
     * For each listener, a ListChangeEvent is created, which provides
     * a read-only view to the list changes in the list. The same
     * ListChangeView object is used for all notifications to the specified
     * listener, so if a listener does not process a set of changes, those
     * changes will persist in the next notification.
     */
    public synchronized void addListChangeListener(ListChangeListener listChangeListener) {
        listeners.add(listChangeListener);
        listenerEvents.add(new ListChangeEvent(this));
    }
    /**
     * Removes the specified listener from receiving notification when new
     * changes are appended to this list change sequence.
     *
     * This uses the <code>==</code> identity comparison to find the listener
     * instead of <code>equals()</code>. This is because multiple Lists may be
     * listening and therefore <code>equals()</code> may be ambiguous.
     */
    public synchronized void removeListChangeListener(ListChangeListener listChangeListener) {
        // find the listener
        int index = -1;
        for(int i = 0; i < listeners.size(); i++) {
            if(listeners.get(i) == listChangeListener) {
                index = i;
                break;
            }
        }

        // remove the listener
        if(index != -1) {
            listenerEvents.remove(index);
            listeners.remove(index);
        } else {
            throw new IllegalArgumentException("Cannot remove nonexistent listener " + listChangeListener);
        }
    }
    
    /**
     * Attempts to combine the specified list change blocks. The following types
     * of blocks can be combined:
     * <li>adjacent and overlapping inserts
     * <li>adjacent and overlapping removes
     * <li>adjacent and overlapping updates
     * <li>an insert and remove that overlap
     *
     * <p>The two blocks in the specified list will be combined if at all possible.
     *
     * @param changes the list where the two blocks are located
     * @param index the index of the first block to change. The second block will
     *      be at <code>index+1</code>.
     * @return the number of blocks remaining from the attempt to combine the two
     *      source blocks. This will be 2 if no combine was made.
     */
    private int combineBlocks(List changes, int index) {
        ListChangeBlock first = (ListChangeBlock)changes.get(index);
        ListChangeBlock second = (ListChangeBlock)changes.get(index + 1);

        // the variables for the first block
        int firstType = first.getType();
        int firstStartIndex = first.getStartIndex();
        int firstEndIndex = first.getEndIndex();
        int firstLength = firstEndIndex - firstStartIndex + 1;
        
        // the variables for the second block
        int secondType = second.getType();
        int secondStartIndex = second.getStartIndex();
        int secondEndIndex = second.getEndIndex();
        int secondLength = secondEndIndex - secondStartIndex + 1;
        
        // if the blocks are the same type, attempt to combine them
        if(firstType == secondType) {
            if(firstType == ListChangeBlock.INSERT) {
                if(secondStartIndex >= firstStartIndex && secondStartIndex <= firstEndIndex - 1) {
                    firstEndIndex = firstEndIndex + secondLength;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    changes.remove(index + 1);
                    changePool.add(second);
                    return 1;
                } else {
                    return 2;
                }
            } else if(firstType == ListChangeBlock.UPDATE) {
                if(firstEndIndex >= secondStartIndex - 1 && firstStartIndex <= secondEndIndex + 1) {
                    firstStartIndex = Math.min(firstStartIndex, secondStartIndex);
                    firstEndIndex = Math.max(firstEndIndex, secondEndIndex);
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    changes.remove(index + 1);
                    changePool.add(second);
                    return 1;
                } else {
                    return 2;
                }
            } else if(firstType == ListChangeBlock.DELETE) {
                if(secondStartIndex <= firstStartIndex && secondEndIndex >= firstStartIndex - 1) {
                    int deleteLength = firstLength + secondLength;
                    firstStartIndex = Math.min(firstStartIndex, secondStartIndex);
                    firstEndIndex = firstStartIndex + deleteLength - 1;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    changes.remove(index + 1);
                    changePool.add(second);
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        
        // if it is an INSERT and then a DELETE
        if(firstType == ListChangeBlock.INSERT && secondType == ListChangeBlock.DELETE) {
            // ensure there is an intersection
            if(firstEndIndex >= secondStartIndex && firstStartIndex <= secondEndIndex) {

                // calculate the size of the intersection
                int intersectionStartIndex = Math.max(firstStartIndex, secondStartIndex);
                int intersectionEndIndex = Math.min(firstEndIndex, secondEndIndex);
                int intersectionLength = intersectionEndIndex - intersectionStartIndex + 1;
                assert(intersectionLength > 0);
                
                // keep track of the number of blocks removed
                int removedBlocksCount = 0;
                
                // update the insert, removing it completely if it is empty
                firstEndIndex = firstEndIndex - intersectionLength;
                firstLength = firstEndIndex - firstStartIndex + 1;
                if(firstLength > 0) {
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                } else {
                    changes.remove(index);
                    changePool.add(first);
                    removedBlocksCount++;
                }
                
                // update the remove, removing it completely if it is empty
                secondEndIndex = secondEndIndex - intersectionLength;
                secondLength = secondEndIndex - secondStartIndex + 1;
                if(secondLength > 0) {
                    second.setData(secondStartIndex, secondEndIndex, secondType);
                } else {
                    changes.remove(index + 1 - removedBlocksCount);
                    changePool.add(second);
                    removedBlocksCount++;
                }
                
                // return the number of blocks remaining
                return (2 - removedBlocksCount);
            
            }
        }
        
        // all other cases we cannot combine
        return 2;
    }
    
    /**
     * Attempts to swap the specified list change blocks. Blocks will be swapped
     * whenever the second change occurs before the first change in the final
     * list.
     *
     * <p>The specified blocks may <strong>not</strong> be <i>combinable</i>, that
     * is, they may not be able to be replaced by a single block.
     *
     * @param changes the list where the two blocks are located
     * @param index the index of the first block to change. The second block will
     *      be at <code>index+1</code>.
     * @return true if the blocks were swapped.
     */
    private boolean swapBlocks(List changes, int index) {
        ListChangeBlock first = (ListChangeBlock)changes.get(index);
        ListChangeBlock second = (ListChangeBlock)changes.get(index + 1);

        // get rid of this assertion later, it is just for sanity
        assert(combineBlocks(changes, index) != 2);
        
        // the variables for the first block
        int firstType = first.getType();
        int firstStartIndex = first.getStartIndex();
        int firstEndIndex = first.getEndIndex();
        int firstLength = firstEndIndex - firstStartIndex + 1;
        
        // the variables for the second block
        int secondType = second.getType();
        int secondStartIndex = second.getStartIndex();
        int secondEndIndex = second.getEndIndex();
        int secondLength = secondEndIndex - secondStartIndex + 1;
        
        if(secondType == ListChangeBlock.INSERT) {
            if(secondStartIndex <= firstStartIndex) {
                // swap
                changes.set(index, second);
                changes.set(index + 1, first);
                
                // shift first
                firstStartIndex = firstStartIndex + secondLength;
                firstEndIndex = firstEndIndex + secondLength;
                first.setData(firstStartIndex, firstEndIndex, firstType);

                return true;
            } else {
                return false;
            }
        } else if(secondType == ListChangeBlock.DELETE) {
            if(secondStartIndex <= firstStartIndex) {
                // swap
                changes.set(index, second);
                changes.set(index + 1, first);
                
                // shift first
                firstStartIndex = firstStartIndex - secondLength;
                firstEndIndex = firstEndIndex - secondLength;
                first.setData(firstStartIndex, firstEndIndex, firstType);

                return true;
            } else {
                return false;
            }
        } else if(secondType == ListChangeBlock.UPDATE) {
            if(secondStartIndex < firstStartIndex) {
                // swap
                changes.set(index, second);
                changes.set(index + 1, first);
                
                return true;
            } else {
                return false;
            }
        }
        
        throw new RuntimeException();
    }
    
    /**
     * Sorts the blocks of the specified list of changes. This ensures that
     * the user iterating the list of change blocks can view changes in
     * increasing order. This ensures that indicies will not be shifted.
     *
     * <p>This performs a bubble sort, swapping adjacent change blocks if
     * they should be swapped. The bubble sort is used instead of a more
     * efficient sort because it is necessary to adjust offsets when swapping
     * and therefore it is preferred to only swap adjacent elements. Bubble
     * sort is a sort that only swaps adjacent elements.
     */
    private void sortChangeBlocks(List changes) {
        // repeat bubbling an element down the list
        for(int repetition = 0; repetition < changes.size(); repetition++) {
            // count the number of swaps made on this repetition
            int repetitionSwaps = 0;
            // keep track of the curent block to compress
            int currentBlock = 0;
            
            // iterate through through all blocks, combining adjacent ones
            while(currentBlock < changes.size() - 1 - repetition) {

                // combine the adjacent blocks
                int combineResult = combineBlocks(changes, currentBlock);
    
                // advance to the next block when there was no change
                if(combineResult == 2) {
                    boolean swapped = swapBlocks(changes, currentBlock);
                    if(swapped) repetitionSwaps++;
        
                    currentBlock++;
                }
            }
        }
    }
}
