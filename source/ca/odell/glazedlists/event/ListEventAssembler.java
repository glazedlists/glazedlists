/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
    
    /** the current working copy of the atomic change */
    private List atomicChangeBlocks = null;
    /** the most recent list change; this is the only change we can append to */
    private ListEventBlock atomicLatestBlock = null;
    /** the current reordering array if this change is a reorder */
    private int[] reorderMap = null;
    
    /** the sequences that provide a view on this queue */
    private List listeners = new ArrayList();
    private List listenerEvents = new ArrayList();
    
    /** the pipeline manages the distribution of events */
    private ListEventPublisher publisher = null;
    
    /** the event level is the number of nested events */
    private int eventLevel = 0;
    /** whether to allow nested events */
    private boolean allowNestedEvents = true;
    /** whether to allow contradicting events */
    private boolean allowContradictingEvents = false;

    /**
     * Creates a new ListEventAssembler that tracks changes for the specified list.
     */
    public ListEventAssembler(EventList sourceList, ListEventPublisher publisher) {
        this.sourceList = sourceList;
        this.publisher = publisher;
    }
    
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
        if(allowNestedEvents) allowContradictingEvents = true;
        
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
    }
        
    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * <p>One or more calls to this method must be prefixed by a call to
     * beginEvent() and followed by a call to commitEvent().
     */
    public void addChange(int type, int startIndex, int endIndex) {
        // attempt to merge this into the most recent block
        if(atomicLatestBlock != null) {
            boolean appendSuccess = atomicLatestBlock.append(startIndex, endIndex, type);
            if(appendSuccess) return;
        }
        
        // create a new block for the change
        atomicLatestBlock = new ListEventBlock(startIndex, endIndex, type);
        atomicChangeBlocks.add(atomicLatestBlock);
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
     * <p>Note that this method should be preferred to manually forwarding events
     * because it is heavily optimized.
     *
     * <p>Note that currently this implementation does a best effort to preserve
     * reorderings. This means that a reordering is lost if it is combined with
     * any other ListEvent.
     */
    public void forwardEvent(ListEvent listChanges) {
        // if we're not nested, we can fire the event directly
        if(eventLevel == 0) {
            atomicChangeBlocks = listChanges.getBlocks();
            reorderMap = listChanges.isReordering() ? listChanges.getReorderMap() : null;
            fireEvent();
            
        // if we're nested, we have to copy this event's parts to our queue
        } else {
            beginEvent();
            this.reorderMap = null;
            if(atomicChangeBlocks.isEmpty() && listChanges.isReordering()) {
                reorder(listChanges.getReorderMap());
            } else {
                while(listChanges.nextBlock()) {
                    addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                }
            }
            commitEvent();
        }
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
        
        // if this is the last stage, sort and fire
        if(eventLevel == 0) {
            ListEventBlock.sortListEventBlocks(atomicChangeBlocks, allowContradictingEvents);
            fireEvent();
        }
    }

    /**
     * Returns <tt>true</tt> if the current atomic change to this list change
     * queue is empty; <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if the current atomic change to this list change
     *      queue is empty; <tt>false</tt> otherwise
     */
    public boolean isEventEmpty() {
        return this.atomicChangeBlocks == null || this.atomicChangeBlocks.isEmpty();
    }

    /**
     * Fires the current event. This needs to be called for each fired
     * event exactly once, even if that event includes nested events.
     */
    private void fireEvent() {
        try {
            // bail on empty changes
            if(atomicChangeBlocks.isEmpty()) return;

            // Protect against the listener set changing via a duplicate list.
            // Some listeners (ie. WeakReferenceProxy) remove themselves as listeners
            // from within their listChanged() method. If we don't make protective
            // copies, these lists will change while we're operating on them.
            List listenersToNotify = new ArrayList(listeners);
            List listenerEventsToNotify = new ArrayList(listenerEvents);
            // perform the notification on the duplicate list
            publisher.fireEvent(sourceList, listenersToNotify, listenerEventsToNotify);

        // clear the change for the next caller
        } finally {
            atomicChangeBlocks = null;
            atomicLatestBlock = null;
            reorderMap = null;
            allowContradictingEvents = false;
        }
    }

    /**
     * Gets the list of blocks for the specified atomic change.
     *
     * @return a List containing the sequence of {@link ListEventBlock}s modelling
     *      the specified change. It is an error to modify this list or its contents.
     */
    List getBlocks() {
        return atomicChangeBlocks;
    }
    
    /**
     * Gets the reorder map for the specified atomic change or null if that
     * change is not a reordering.
     */
    int[] getReorderMap() {
        return reorderMap;
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
        publisher.addDependency(sourceList, listChangeListener);
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
        
        // remove the publisher's dependency
        publisher.removeDependency(sourceList, listChangeListener);
    }
}