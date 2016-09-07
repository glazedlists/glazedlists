/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.Preconditions;
import ca.odell.glazedlists.impl.WeakReferenceProxy;
import ca.odell.glazedlists.impl.event.BlockSequence;
import ca.odell.glazedlists.impl.event.Tree4Deltas;

import java.util.ConcurrentModificationException;
import java.util.List;

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
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ListEventAssembler<E> {

    /** the list that this tracks changes for */
    protected EventList<E> sourceList;

    /** non-null if an event is currently pending */
    private Thread eventThread;
    /** the event level is the number of nested events */
    protected int eventLevel = 0;
    /** whether to allow nested events */
    protected boolean allowNestedEvents = true;

    /** the current reordering array if this change is a reorder */
    protected int[] reorderMap = null;
    /** prefer to use the linear blocks, which are more performant but handle only a subset of all cases */
    private BlockSequence<E> blockSequence = new BlockSequence<E>();
    private boolean useListBlocksLinear = false;
    /** fall back to list tree4deltas, which are capable of all list changes */
    private Tree4Deltas<E> listDeltas = new Tree4Deltas<E>();

    private final SequenceDependenciesEventPublisher publisher;
    private final ListEvent<E> listEvent;
    private final ListEventFormat eventFormat = new ListEventFormat();
    /** true if we're waiting on the publisher to distribute our event */
    private boolean eventIsBeingPublished = false;

    /**
     * Create a new {@link ListEventPublisher} for an {@link EventList} not attached
     * to any other {@link EventList}s.
     */
    public static ListEventPublisher createListEventPublisher() {
        return new SequenceDependenciesEventPublisher();
    }

    /**
     * Creates a new ListEventAssembler that tracks changes for the specified list.
     */
    public ListEventAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
        this.sourceList = sourceList;
        this.publisher = (SequenceDependenciesEventPublisher) publisher;
        this.listEvent = new Tree4DeltasListEvent<E>(this, sourceList);
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
            throw new ConcurrentModificationException("Cannot begin a new event while another event is in progress by thread, "  + eventThread.getName());
        }
        this.allowNestedEvents = allowNestedEvents;
        if(allowNestedEvents || (eventLevel == 0 && eventThread != null)) {
            listDeltas.setAllowContradictingEvents(true);
        }

        // prepare for a new event if we haven't already
        if(eventThread == null) {
            this.eventThread = Thread.currentThread();
            useListBlocksLinear = true;
        }

        // track how deeply nested we are
        eventLevel++;
    }

    /**
     * Add to the current ListEvent the insert of the element at
     * the specified index, with the specified previous value.
     */
    public void elementInserted(int index, E newValue) {
        addChange(ListEvent.INSERT, index, index, ListEvent.<E>unknownValue(), newValue);
    }
    /**
     * Add to the current ListEvent the update of the element at the specified
     * index, with the specified previous value.
     */
    public void elementUpdated(int index, E oldValue, E newValue) {
        addChange(ListEvent.UPDATE, index, index, oldValue, newValue);
    }
    /**
     * Add to the current ListEvent the removal of the element at the specified
     * index, with the specified previous value.
     */
    public void elementDeleted(int index, E oldValue) {
        addChange(ListEvent.DELETE, index, index, oldValue, ListEvent.<E>unknownValue());
    }

    /**
     * @deprecated replaced with {@link #elementUpdated(int, Object, Object)}.
     */
    @Deprecated
    public void elementUpdated(int index, E oldValue) {
        elementUpdated(index, oldValue, ListEvent.<E>unknownValue());
    }

    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * <p>One or more calls to this method must be prefixed by a call to
     * beginEvent() and followed by a call to commitEvent().
     *
     * @deprecated replaced with {@link #elementInserted}, {@link #elementUpdated}
     *     and {@link #elementDeleted}.
     */
    @Deprecated
    public void addChange(int type, int startIndex, int endIndex) {
        addChange(type, startIndex, endIndex, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
    }
    /**
     * Convenience method for appending a single change of the specified type.
     *
     * @deprecated replaced with {@link #elementInserted}, {@link #elementUpdated}
     *     and {@link #elementDeleted}.
     */
    @Deprecated
    public void addChange(int type, int index) {
        addChange(type, index, index);
    }
    /**
     * Convenience method for appending a single insert.
     *
     * @deprecated replaced with {@link #elementInserted}.
     */
    @Deprecated
    public void addInsert(int index) {
        addChange(ListEvent.INSERT, index);
    }
    /**
     * Convenience method for appending a single delete.
     *
     * @deprecated replaced with {@link #elementDeleted}.
     */
    @Deprecated
    public void addDelete(int index) {
        addChange(ListEvent.DELETE, index);
    }
    /**
     * Convenience method for appending a single update.
     *
     * @deprecated replaced with {@link #elementUpdated}.
     */
    @Deprecated
    public void addUpdate(int index) {
        addChange(ListEvent.UPDATE, index);
    }
    /**
     * Convenience method for appending a range of inserts.
     *
     * @deprecated replaced with {@link #elementInserted}.
     */
    @Deprecated
    public void addInsert(int startIndex, int endIndex) {
        addChange(ListEvent.INSERT, startIndex, endIndex);
    }
    /**
     * Convenience method for appending a range of deletes.
     *
     * @deprecated replaced with {@link #elementDeleted}.
     */
    @Deprecated
    public void addDelete(int startIndex, int endIndex) {
        addChange(ListEvent.DELETE, startIndex, endIndex);
    }
    /**
     * Convenience method for appending a range of updates.
     *
     * @deprecated replaced with {@link #elementUpdated}.
     */
    @Deprecated
    public void addUpdate(int startIndex, int endIndex) {
        addChange(ListEvent.UPDATE, startIndex, endIndex);
    }
    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * @param endIndex the inclusive end index
     */
    private void addChange(int type, int startIndex, int endIndex, E oldValue, E newValue) {
        // try the linear holder first
        if(useListBlocksLinear) {
            final boolean success = blockSequence.addChange(type, startIndex, endIndex + 1, oldValue, newValue);
            if (success)
                return;

            // convert from linear to tree4deltas
            listDeltas.addAll(blockSequence);
            useListBlocksLinear = false;
        }

        // try the good old reliable tree4deltas
        switch (type) {
            case ListEvent.INSERT: listDeltas.targetInsert(startIndex, endIndex + 1, newValue); break;
            case ListEvent.UPDATE: listDeltas.targetUpdate(startIndex, endIndex + 1, oldValue, newValue); break;
            case ListEvent.DELETE: listDeltas.targetDelete(startIndex, endIndex + 1, oldValue); break;
        }
    }

    /**
     * Sets the current event as a reordering. Reordering events cannot be
     * combined with other events.
     */
    public void reorder(int[] reorderMap) {
        if(!isEventEmpty()) throw new IllegalStateException("Cannot combine reorder with other change events");
        // can't reorder an empty list, see bug 91
        if(reorderMap.length == 0) return;
        addChange(ListEvent.DELETE, 0, reorderMap.length - 1, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
        addChange(ListEvent.INSERT, 0, reorderMap.length - 1, ListEvent.<E>unknownValue(), ListEvent.<E>unknownValue());
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
    public void forwardEvent(ListEvent<?> listChanges) {
        beginEvent(false);
        this.reorderMap = null;
        if(isEventEmpty() && listChanges.isReordering()) {
            reorder(listChanges.getReorderMap());
        } else {
            while(listChanges.next()) {
                int type = listChanges.getType();
                int index = listChanges.getIndex();
                E oldValue = (E) listChanges.getOldValue();
                E newValue = (E) listChanges.getNewValue();
                addChange(type, index, index, oldValue, newValue);
            }
            listChanges.reset();
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

        // if this is the last stage, sort and fire
        if (eventLevel != 0) {
            return;
        }

        if (isEventEmpty()) {
            cleanup();
            return;
        }

        // we've already fired this event, we're just adding to it
        if(eventIsBeingPublished) {
            return;
        }

        eventIsBeingPublished = true;
        publisher.fireEvent(sourceList, listEvent, eventFormat);
    }

    /**
     * Discards the current atomic change to this list change queue. This does
     * not notify any listeners about any changes.
     *
     * <p>The caller of this method is responsible for returning the EventList
     * to its state before the event began. If they fail to do so, the EventList
     * pipeline may be in an inconsistent state.
     *
     * <p>If the current event is nested within a greater event, this will
     * discard changes at the current nesting level and that further changes
     * are still applied directly to the parent change.
     */
    public synchronized void discardEvent() {
        // complain if we have no event to commit
        if(eventLevel == 0) throw new IllegalStateException("Cannot discard without an event in progress");

        // we are one event less nested
        eventLevel--;
        allowNestedEvents = true;

        // if this is the last stage, clean it up
        if(eventLevel == 0) {
            cleanup();
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
        return useListBlocksLinear ? blockSequence.isEmpty() : listDeltas.isEmpty();
    }

    /**
     * Registers the specified listener to be notified whenever new changes
     * are appended to this list change sequence.
     *
     * <p>For each listener, a ListEvent is created, which provides
     * a read-only view to the list changes in the list. The same
     * ListChangeView object is used for all notifications to the specified
     * listener, so if a listener does not process a set of changes, those
     * changes will persist in the next notification.
     *
     * @param listChangeListener event listener != null
     * @throws NullPointerException if the specified listener is null
     */
    public synchronized void addListEventListener(ListEventListener<? super E> listChangeListener) {
    	Preconditions.checkNotNull(listChangeListener, "ListEventListener is undefined");
        publisher.addListener(sourceList, listChangeListener, eventFormat);
    }

    /**
     * Removes the specified listener from receiving notification when new
     * changes are appended to this list change sequence.
     *
     * <p>This uses the <code>==</code> identity comparison to find the listener
     * instead of <code>equals()</code>. This is because multiple Lists may be
     * listening and therefore <code>equals()</code> may be ambiguous.
     *
     * @param listChangeListener event listener != null
     * @throws NullPointerException if the specified listener is null
     * @throws IllegalArgumentException if the specified listener wasn't added before
     */
    public synchronized void removeListEventListener(ListEventListener<? super E> listChangeListener) {
    	Preconditions.checkNotNull(listChangeListener, "ListEventListener is undefined");
        publisher.removeListener(sourceList, listChangeListener);
    }

    /**
     * Get all {@link ListEventListener}s observing the {@link EventList}.
     */
    public List<ListEventListener<E>> getListEventListeners() {
        return publisher.getListeners(sourceList);
    }

    // these method sare used by the ListEvent
    boolean getUseListBlocksLinear() { return useListBlocksLinear; }
    Tree4Deltas getListDeltas() { return listDeltas; }
    BlockSequence getListBlocksLinear() { return blockSequence; }
    int[] getReorderMap() { return reorderMap; }

    /**
     * Cleanup all temporary variables necessary while events are being fired.
     */
    private void cleanup() {
        eventThread = null;
        blockSequence.reset();
        listDeltas.reset(sourceList.size());
        reorderMap = null;
        listDeltas.setAllowContradictingEvents(false);
        // force cleanup of iterator which still could reference old data
        listEvent.reset();
    }

    /**
     * Adapt {@link SequenceDependenciesEventPublisher.EventFormat} for use with {@link ListEvent}s.
     */
    private class ListEventFormat implements SequenceDependenciesEventPublisher.EventFormat<EventList<E>,ListEventListener<? super E>,ListEvent<E>> {
        @Override
        public void fire(EventList<E> subject, ListEvent<E> event, ListEventListener<? super E> listener) {
            event.reset();
            listener.listChanged((ListEvent) event);
        }
        @Override
        public void postEvent(EventList<E> subject) {
            cleanup();
            eventIsBeingPublished = false;
        }
        @Override
        public boolean isStale(EventList<E> subject, ListEventListener<? super E> listener) {
            if(listener instanceof WeakReferenceProxy && ((WeakReferenceProxy)listener).getReferent() == null) {
                ((WeakReferenceProxy)listener).dispose();
                return true;
            }
            return false;
        }
    }
}