/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// for keeping a list of changes
import java.util.*;

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
public final class ListEventAssembler {
    
    /** the list that this tracks changes for */
    private EventList sourceList;
    
    /** the list of lists of change blocks */
    private ArrayList atomicChanges = new ArrayList();
    private ArrayList reorderMaps = new ArrayList();
    private int oldestChange = 0;
    
    /** the current working copy of the atomic change */
    private ArrayList atomicChangeBlocks = null;
    /** the most recent list change; this is the only change we can append to */
    private ListEventBlock atomicLatestBlock = null;
    /** the current reordering array if this change is a reorder */
    private int[] reorderMap = null;
    
    /** the pool of list change objects */
    private ArrayList changePool = new ArrayList();
    
    /** the sequences that provide a view on this queue */
    private ArrayList listeners = new ArrayList();
    private ArrayList listenerEvents = new ArrayList();
    
    /** the pipeline manages the distribution of events */
    ListEventPipeline pipeline = new ListEventPipeline();

    /**
     * Creates a new ListEventAssembler that tracks changes for the
     * specified list.
     */
    public ListEventAssembler(EventList sourceList, ListEventPipeline pipeline) {
        this.sourceList = sourceList;
        this.pipeline = pipeline;
    }
    
    /** the event level is the number of nested events */
    private int eventLevel = 0;
    
    /** whether to allow nested events */
    private boolean allowNestedEvents = true;
    
    /**
     * Starts a new atomic change to this list change queue. 
     *
     * <p>This simple change event does not support change events nested within.
     * To allow other methods to nest change events within a change event, use
     * beginEvent(true).
     */
    public void beginEvent() {
        beginEvent(false);
    }
        
    /**
     * Starts a new atomic change to this list change queue. This signature
     * allows you to specify allowing nested changes. This simply means that
     * you can call other methods that contain a beginEvent(), commitEvent()
     * block and their changes will be recorded but not fired. This allows
     * the creation of list modification methods to call simpler list modification
     * methods while still firing a single ListEvent to listeners.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=52">Bug 52</a>
     *
     * @param allowNestedEvents false to throw an exception
     *      if another call to beginEvent() is made before
     *      the next call to commitEvent(). Nested events allow
     *      multiple method's events to be composed into a single
     *      event.
     */
    public synchronized void beginEvent(boolean allowNestedEvents) {
        // complain if we cannot nest any further
        if(!this.allowNestedEvents) {
            throw new ConcurrentModificationException("Cannot begin a new event while another event is in progress");
        }
        this.allowNestedEvents = allowNestedEvents;
        
        // prepare for a new event if we haven't already
        if(eventLevel == 0) {
            prepareEvent();
        }
        
        // track how deeply nested we are
        eventLevel++;
    }
        
    /**
     * Prepares to receive event parts. This needs to be called for
     * each fired event exactly once, even if that event includes some
     * nested events.
     *
     * <p>To prevent two simultaneous atomic changes, the atomicChangeBlocks
     * arraylist is used as a flag. If it is null, there is no change taking
     * place. Otherwise there is a conflicting change and a
     * ConcurrentModificationException is thrown.
     */
    private void prepareEvent() {
        atomicChangeBlocks = new ArrayList();
        atomicLatestBlock = null;
        reorderMap = null;
        
        // calculate the oldest change still needed
        int oldestRequiredChange = atomicChanges.size(); 
        for(int e = 0; e < listenerEvents.size(); e++) {
            ListEvent listChangeEvent = (ListEvent)listenerEvents.get(e);
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
        oldestChange = oldestRequiredChange;
    }
        
    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * <p>One or more calls to this method must be prefixed by a call to
     * beginEvent() and followed by a call to commitEvent().
     */
    public void addChange(int type, int startIndex, int endIndex) {
        // create a new change for the first change
        if(atomicLatestBlock == null) {
            atomicLatestBlock = createListEventBlock(startIndex, endIndex, type);
            atomicChangeBlocks.add(atomicLatestBlock);
            return;
        }
        
        // append the change if possible
        boolean appended = atomicLatestBlock.append(startIndex, endIndex, type);
        // if appended is null the changes couldn't be merged
        if(!appended) {
            ListEventBlock block = createListEventBlock(startIndex, endIndex, type);
            atomicChangeBlocks.add(block);
            atomicLatestBlock = block;
        }
    }
    /**
     * Convenience method for appending a single change of the specified type.
     */
    public void addChange(int type, int index) {
        addChange(type, index, index);
    }
    /**
     * Convenience method for appending a single insert.
     */
    public void addInsert(int index) {
        addChange(ListEvent.INSERT, index);
    }
    /**
     * Convenience method for appending a single delete.
     */
    public void addDelete(int index) {
        addChange(ListEvent.DELETE, index);
    }
    /**
     * Convenience method for appending a single update.
     */
    public void addUpdate(int index) {
        addChange(ListEvent.UPDATE, index);
    }
    /**
     * Convenience method for appending a range of inserts.
     */
    public void addInsert(int startIndex, int endIndex) {
        addChange(ListEvent.INSERT, startIndex, endIndex);
    }
    /**
     * Convenience method for appending a range of deletes.
     */
    public void addDelete(int startIndex, int endIndex) {
        addChange(ListEvent.DELETE, startIndex, endIndex);
    }
    /**
     * Convenience method for appending a range of updates.
     */
    public void addUpdate(int startIndex, int endIndex) { 
        addChange(ListEvent.UPDATE, startIndex, endIndex);
    }
    /**
     * Sets the current event as a reordering. Reordering events cannot be
     * combined with other events.
     */
    public void reorder(int[] reorderMap) {
        if(atomicChangeBlocks.size() > 0) throw new IllegalStateException("Cannot combine reorder with other change events");
        // can't reorder an empty list, see bug 91
        if(reorderMap.length == 0) return;
        addChange(ListEvent.DELETE, 0, reorderMap.length - 1);
        addChange(ListEvent.INSERT, 0, reorderMap.length - 1);
        this.reorderMap = reorderMap;
    }
    /**
     * Forwards the event. This is a convenience method that does the following:
     * <br>1. beginEvent()
     * <br>2. For all changes in sourceEvent, apply those changes to this
     * <br>3. commitEvent()
     *
     * <p>This method should be preferred to manually forwarding events because
     * it may be optimized. Note that this implementation is currently not optimized,
     * but it should be real soon!
     */
    public void forwardEvent(ListEvent listChanges) {
        beginEvent();
        if(listChanges.isReordering()) {
            int[] reorderMap = listChanges.getReorderMap();
            reorder(reorderMap);
        } else {
            while(listChanges.next()) {
                addChange(listChanges.getType(), listChanges.getIndex());
            }
        }
        commitEvent();
    }
    /**
     * Commits the current atomic change to this list change queue. This will
     * notify all listeners about the change.
     *
     * <p>If the current event is nested within a greater event, this will simply
     * change the nesting level so that further changes are applied directly to the
     * parent change.
     */
    public synchronized void commitEvent() {
        // complain if we have no event to commit
        if(eventLevel == 0) throw new IllegalStateException("Cannot commit without an event in progress");
        
        // we are one event less nested
        eventLevel--;
        allowNestedEvents = true;
        
        // fire the event if we are no longer nested
        if(eventLevel == 0) {
            fireEvent();
        }
    }
    
    /**
     * Fires the current event. This needs to be called for each fired
     * event exactly once, even if that event includes nested events.
     */
    private void fireEvent() {
        // bail early on empty changes
        if(atomicChangeBlocks.size() == 0) {
            atomicChangeBlocks = null;
            atomicLatestBlock = null;
            reorderMap = null;
            return;
        }

        // sort and simplify this block
        ListEventBlock.sortListEventBlocks(atomicChangeBlocks);
        // add this to the complete set
        atomicChanges.add(atomicChangeBlocks);
        // add the reorder map to the complete set only if it is the only change
        if(reorderMap != null && atomicChangeBlocks.size() == 2) {
            reorderMaps.add(reorderMap);
        } else {
            reorderMaps.add(null);
        }
        
        // notify listeners
        try {
            // protect against the listener set changing via a duplicate list
            List listenersToNotify = new ArrayList();
            List listenerEventsToNotify = new ArrayList();
            listenersToNotify.addAll(listeners);
            listenerEventsToNotify.addAll(listenerEvents);
            // perform the notification on the duplicate list
            pipeline.fireEvent(sourceList, listenersToNotify, listenerEventsToNotify);
        // clear the change for the next caller
        } finally {
            atomicChangeBlocks = null;
            atomicLatestBlock = null;
            reorderMap = null;
        }
    }


    /**
     * For pooling list change objects. This gets a new list change
     * object or recycles one if that exists.
     */
    private ListEventBlock createListEventBlock(int startIndex, int endIndex, int type) {
        if(changePool.isEmpty()) return new ListEventBlock(startIndex, endIndex, type);
        ListEventBlock listChange = (ListEventBlock)changePool.remove(changePool.size() - 1);
        listChange.setData(startIndex, endIndex, type);
        return listChange;
    }
    
    /**
     * Gets the total number of blocks in the specified atomic change.
     */
    int getBlockCount(int atomicCount) {
        List atomicChange = (List)atomicChanges.get(atomicCount);
        return atomicChange.size();
    }

    /**
     * Gets the total number of atomic changes so far.
     */
    int getAtomicCount() {
        return atomicChanges.size();
    }
    
    /**
     * Gets the specified block from the current set of change blocks. Note
     * that not blocks may be cleaned up after they are viewed by all
     * monitoring sequences. Such blocks may not be available.
     */
    ListEventBlock getBlock(int atomicCount, int block) {
        List atomicChange = (List)atomicChanges.get(atomicCount);
        return (ListEventBlock)atomicChange.get(block);
    }
    
    /**
     * Gets the reorder map for the specified atomic change or null if that
     * change is not a reordering.
     */
    int[] getReorderMap(int atomicCount) {
        return (int[])reorderMaps.get(atomicCount);
    }
    
    /**
     * Registers the specified listener to be notified whenever new changes
     * are appended to this list change sequence.
     *
     * For each listener, a ListEvent is created, which provides
     * a read-only view to the list changes in the list. The same
     * ListChangeView object is used for all notifications to the specified
     * listener, so if a listener does not process a set of changes, those
     * changes will persist in the next notification.
     */
    public synchronized void addListEventListener(ListEventListener listChangeListener) {
        listeners.add(listChangeListener);
        listenerEvents.add(new ListEvent(this, sourceList));
        pipeline.addDependency(listChangeListener, sourceList);
    }
    /**
     * Removes the specified listener from receiving notification when new
     * changes are appended to this list change sequence.
     *
     * This uses the <code>==</code> identity comparison to find the listener
     * instead of <code>equals()</code>. This is because multiple Lists may be
     * listening and therefore <code>equals()</code> may be ambiguous.
     */
    public synchronized void removeListEventListener(ListEventListener listChangeListener) {
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
}