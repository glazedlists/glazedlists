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
public class ListChangeSequence {
    
    /** the list of lists of change blocks */
    private ArrayList atomicChanges = new ArrayList();
    private int oldestChange = 0;
    /** the total count of simple changes */
    public int changeCount = 0;
    /** the total number of change blocks */
    public int blockCount = 0;
    
    /** the current working copy of the atomic change */
    private ArrayList atomicChangeBlocks = null;
    /** the atomic change's count of simple changes */
    private int atomicBlockCount = 0;
    /** the atomic change's count of change blocks */
    private int atomicChangeCount = 0;
    /** the most recent list change; this is the only change we can append to */
    private ListChangeBlock atomicLatestBlock = null;
    
    /** the pool of list change objects */
    public ArrayList changePool = new ArrayList();
    
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
        atomicBlockCount = 0;
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
            atomicBlockCount++;
            atomicChangeBlocks.add(atomicLatestBlock);
            return;
        }
        
        // append the change if possible
        ListChangeBlock appended = atomicLatestBlock.append(startIndex, endIndex, type);
        // if appended is not null, the changes couldn't be merged
        if(appended == null) {
            appended = createListChangeBlock(startIndex, endIndex, type);
            atomicBlockCount++;
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
            //sortChanges(atomicChangeBlocks);
            // add this to the complete set
            atomicChanges.add(atomicChangeBlocks);
            changeCount = changeCount + atomicChangeCount;
            blockCount = blockCount + atomicBlockCount;
            
            // notify listeners
            for(int i = 0; i < listeners.size(); i++) {
                ListChangeListener listener = (ListChangeListener)listeners.get(i);
                ListChangeEvent event = (ListChangeEvent)listenerEvents.get(i);
                listener.notifyListChanges(event);
            }
        }

        // clear the change for the next caller
        atomicChangeBlocks = null;
        atomicBlockCount = 0;
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
     *      source blocks.
     */
    private int combineBlocks(List changes, int index) {
        ListChangeBlock first = (ListChangeBlock)changes.get(index);
        ListChangeBlock second = (ListChangeBlock)changes.get(index + 1);
        
        //System.out.println("EXAMINING " + first +  " THEN " + second);

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
                    //System.out.println("COMBINING " + first +  " WITH " + second);
                    firstEndIndex = firstEndIndex + secondLength;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    changes.remove(index + 1);
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
                    return 1;
                } else {
                    return 2;
                }
            } else if(firstType == ListChangeBlock.DELETE) {
                if(secondStartIndex <= firstStartIndex && secondEndIndex >= firstStartIndex - 1) {
                    firstEndIndex = firstEndIndex + secondLength;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    changes.remove(index + 1);
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        
        // if it is an INSERT and a DELETE
        if((firstType == ListChangeBlock.INSERT && secondType == ListChangeBlock.DELETE)
        || (firstType == ListChangeBlock.DELETE && secondType == ListChangeBlock.INSERT)) {
            // ensure there is an intersection
            if(firstEndIndex >= secondStartIndex && firstStartIndex <= secondEndIndex) {

                // calculate the size of the intersection
                int intersectionStartIndex = Math.max(firstStartIndex, secondStartIndex);
                int intersectionEndIndex = Math.min(firstEndIndex, secondEndIndex);
                int intersectionLength = intersectionEndIndex - intersectionStartIndex + 1;
                assert(intersectionLength > 0);
                
                // add an UPDATE for DELETE then INSERT
                int addedBlocksCount = 0;
                if(firstType == ListChangeBlock.DELETE && secondType == ListChangeBlock.INSERT) {
                    ListChangeBlock update = createListChangeBlock(intersectionStartIndex, intersectionEndIndex, ListChangeBlock.UPDATE);
                    changes.add(index + 2, update);
                    addedBlocksCount++;
                }

                // keep track of the number of blocks removed
                int removedBlocksCount = 0;
                
                // update the insert, removing it completely if it is empty
                firstEndIndex = firstEndIndex - intersectionLength;
                firstLength = firstEndIndex - firstStartIndex + 1;
                if(firstLength > 0) {
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                } else {
                    changes.remove(index);
                    removedBlocksCount++;
                }
                
                // update the remove, removing it completely if it is empty
                secondEndIndex = secondEndIndex - intersectionLength;
                secondLength = secondEndIndex - secondStartIndex + 1;
                if(secondLength > 0) {
                    second.setData(secondStartIndex, secondEndIndex, secondType);
                } else {
                    changes.remove(index + 1 - removedBlocksCount);
                    removedBlocksCount++;
                }
                
                // return the number of blocks remaining
                return (2 - removedBlocksCount + addedBlocksCount);
            
            } else {
                return 2;
            }
        }
        
        // if it is an UPDATE and an INSERT or DELETE
        if(firstType == ListChangeBlock.UPDATE || secondType == ListChangeBlock.UPDATE) {
            // handle this case
            return 2;
        }
        
        throw new RuntimeException();
    }
    
    /**
     * Sorts the blocks of the specified list of changes.
     */
    private void sortChanges(List changes) {
        //System.out.println("SORTING BLOCKS! " + changes.size());
        for(int i = 0; i < changes.size() - 1; i++) {
            // prepare to debug the change
            String firstBefore = "" + changes.get(i);
            String secondBefore = "" + changes.get(i + 1);

            // combine the adjacent blocks
            int blocksRemaining = combineBlocks(changes, i);

            // debug the change
            /*if(blocksRemaining == 0) {
                System.out.println(firstBefore + " AND " + secondBefore + " cancelled each other out!");
            } else if(blocksRemaining == 1) {
                String firstAfter = "" + changes.get(i);
                System.out.println(firstBefore + " AND " + secondBefore + " became " + firstAfter);
            } else if(blocksRemaining == 2) {
                String firstAfter = "" + changes.get(i);
                String secondAfter = "" + changes.get(i + 1);
                if(!firstBefore.equals(firstAfter) || !secondBefore.equals(secondAfter)) {
                    System.out.println(firstBefore + " AND " + secondBefore + " became " + firstAfter + " AND " + secondAfter);
                }
            }*/
        }
    }
    
    
    /**
     * Ensures that two list change blocks are in proper sequence and discrete.
     * This will change the blocks so that the following are true:
     * <li>the action described by the blocks does not change
     * <li>the blocks are not redundant
     * <li>the blocks are in increasing order
     *
     * <p>This applies to a adjacent blocks within a List. This method can be used
     * within a bubble-sort to ensure the entire list holds the required properties.
     *
     * @param changes the list where the two blocks are located
     * @param index the index of the first block to change. The second block will
     *      be at <code>index+1</code>.
     */
    /*private int simplifyAndOrderBlocks(List changes, int index) {
        ListChangeBlock first = changes.get(index);
        ListChangeBlock second = changes.get(index + 1);
        
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

        // handle cases starting with an INSERT
        if(firstType == ListChangeBlock.INSERT) {
            
            // handle INSERT followed by INSERT
            if(secondType == ListChangeBlock.INSERT) {
                // if the second precedes the first, swap and shift
                if(secondStartIndex < firstStartIndex) {
                    // swap
                    changes.set(index, second);
                    changes.set(index + 1, first);

                    // shift
                    firstStartIndex = firstStartIndex + secondLength;
                    firstEndIndex = firstEndIndex + secondLength;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    
                    // we have processed this pair
                    return 1;
                
                // if the inserts overlap, merge them
                } else if(secondStartIndex <= firstEndIndex) {
                    // merge
                    firstEndIndex = firstEndIndex + secondLength;
                    
                    // remove the second
                    changes.remove(index + 1);
                    
                    // we must process the new replacement for second
                    return 0;
                // if the second change happens after the first, do nothing
                } else {
                    // we have processed this pair
                    return 1;
                }
                assert(false);

            // handle INSERT followed by DELETE
            } else if(secondType == ListChangeBlock.DELETE) {
                // if the second precedes the first, swap and shift
                if(secondEndIndex < firstStartIndex) {
                    // swap
                    changes.set(index, second);
                    changes.set(index + 1, first);
                    
                    // shift
                    firstStartIndex = firstStartIndex - secondLength;
                    firstEndIndex = firstEndIndex - secondLength;
                    first.setData(firstStartIndex, firstEndIndex, firstType);
                    
                    // we have processed this pair
                    return 1;
                
                // if the second intersects the first and it does not trail, contract both
                } else if(secondEndIndex <= firstEndIndex) {
                    // if the remove leads
                    if(secondHeadIndex < firstHeadIndex) {
                        // swap
                        changes.set(index, second);
                        changes.set(index + 1, first);
                        
                        // contract the remove
                        secondEndIndex = firstHeadIndex - 1;
                        second.setData(secondStartIndex, secondEndIndex, secondType);
                        
                        // contract the insert and remove it if necessary
                        int contractedSecondLength = secondEndIndex - secondStartIndex + 1;
                        int deltaSecondLength = secondLength - contractedSecondLength;
                        firstEndIndex = firstEndIndex - deltaSecondLength;
                        firstLength = firstEndIndex - firstStartIndex + 1;
                        
                        // the insert is now unnecessary
                        if(firstLength == 0) {
                            changes.remove(index + 1);
                            return 0;
                        // we have processed this pair
                        } else {
                            first.setData(firstStartIndex, firstEndIndex, firstType);
                            return 1;
                        }
                        
                    // if the remove is contained by the insert, the remove is unnecessary
                    } else if(secondHeadIndex >= firstHeadIndex) {
                        // the remove is now unnecessary
                        changes.remove(index + 1);
                        
                        // adjust the first length
                        firstEndIndex = firstEndIndex - secondLength;
                        firstLength = firstEndIndex - firstStartIndex + 1;
                        
                        // if the first length is now zero, the insert is also unnecessary
                        if(firstLength == 0) {
                            changes.remove(index);
                            return -1;
                        // we must process the new replacement for second
                        } else {
                            return 0;
                        }
                    }
                    assert(false);

                // if the second trails, and may intersect with the first
                } else if(secondEndIndex > firstEndIndex) {
                    // if the remove contains the insert, the insert is unnecessary
                    if(secondStartIndex <= firstStartIndex) {
                        // the insert is now unnecessary
                        changes.remove(index);
                        
                        // adjust the remove length
                        secondEndIndex = secondEndIndex - firstLength;
                        secondLength = secondEndIndex - secondStartIndex + 1;
                        
                        assert(secondLength > 0);
                        
                        // we must find a new replacement for the first
                        return 0;

                    // if they intersect, contract them both
                    } else if(secondStartIndex <= firstEndIndex) {
                        // contract the insert
                        firstEndIndex = secondStartIndex - 1;
                        first.setData(firstStartIndex, firstEndIndex, firstType);
                        
                        // contract the remove
                        int contractedFirstLength = firstEndIndex - firstStartIndex + 1;
                        int deltaFirstLength = firstLength - contractedFirstLength;
                        secondEndIndex = secondEndIndex - deltaFirstLength;
                        secondLength = secondEndIndex - secondStartIndex + 1;
                        second.setData(secondStartIndex, secondEndIndex, secondType);
                        
                        assert(secondLength >= 0);
                        
                        // we have processed this pair
                        return 1;

                    // if the second change happens after the first, do nothing
                    } else {
                        // we have processed this pair
                        return 1;
                    }
                    assert(false);
                }
                
            // handle INSERT followed by UPDATE
            } else if (secondType == ListChangeBlock.UPDATE) {
                // if the update precedes the insert, swap
                if(secondStartIndex <= firstStartIndex) {
                    // swap
                    changes.set(index, second);
                    changes.set(index + 1, first);
                    
                    // we have processed this pair
                    return 1;

                // if the second change happens after the first, do nothing
                } else if(secondStartIndex >= firstStartIndex) {
                    // we have processed this pair
                    return 1;
                }
                assert(false);
            }
        }
    }*/
}
