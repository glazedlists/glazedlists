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
 * Atomic sets of changes may involve many lines of changes and many blocks
 * of changes. They are committed to the queue in one action. No other threads
 * should be creating a change on the same list change queue when an atomic
 * change is being created.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListChangeSequence {
    
    /** the set of non-contiguous changes */
    //private ArrayList changeBlocks = new ArrayList();
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
     *
     * @todo clean up blocks before beginning an atomic change
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
     * are appended to this list change set.
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
}
