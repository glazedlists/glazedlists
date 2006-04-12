/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.WeakReferenceProxy;
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
public final class ListEventAssembler<E> {

    /** the delegate contains logic specific for the current event storage strategy */
    private final AssemblerHelper<E> delegate;
    
    /**
     * Creates a new ListEventAssembler that tracks changes for the specified list.
     */
    public ListEventAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
//        delegate = new BlockDeltasAssembler<E>(sourceList, publisher);
//        delegate = new TreeDeltasAssembler<E>(sourceList, publisher);

        String driver = System.getProperty("GlazedLists.ListEventAssemblerDelegate");
        if(driver == null || driver.equals("blockdeltas")) {
            delegate = new BlockDeltasAssembler<E>(sourceList, publisher);
        } else if(driver.equals("barcodedeltas")) {
            delegate = new BarcodeDeltasAssembler<E>(sourceList, publisher);
        } else if(driver.equals("treedeltas")) {
            delegate = new TreeDeltasAssembler<E>(sourceList, publisher);
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Starts a new atomic change to this list change queue. 
     *
     * <p>This simple change event does not support change events nested within.
     * To allow other methods to nest change events within a change event, use
     * beginEvent(true).
     */
    public void beginEvent() {
        delegate.beginEvent(false);
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
        delegate.beginEvent(allowNestedEvents);
    }

    /**
     * Adds a block of changes to the set of list changes. The change block
     * allows a range of changes to be grouped together for efficiency.
     *
     * <p>One or more calls to this method must be prefixed by a call to
     * beginEvent() and followed by a call to commitEvent().
     */
    public void addChange(int type, int startIndex, int endIndex) {
        delegate.addChange(type, startIndex, endIndex);
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
        delegate.reorder(reorderMap);
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
        delegate.forwardEvent(listChanges);
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
        delegate.commitEvent();
    }

    /**
     * Returns <tt>true</tt> if the current atomic change to this list change
     * queue is empty; <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if the current atomic change to this list change
     *      queue is empty; <tt>false</tt> otherwise
     */
    public boolean isEventEmpty() {
        return delegate.isEventEmpty();
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
     */
    public synchronized void addListEventListener(ListEventListener<E> listChangeListener) {
        delegate.addListEventListener(listChangeListener);
    }
    /**
     * Removes the specified listener from receiving notification when new
     * changes are appended to this list change sequence.
     *
     * <p>This uses the <code>==</code> identity comparison to find the listener
     * instead of <code>equals()</code>. This is because multiple Lists may be
     * listening and therefore <code>equals()</code> may be ambiguous.
     */
    public synchronized void removeListEventListener(ListEventListener<E> listChangeListener) {
        delegate.removeListEventListener(listChangeListener);
    }

    /**
     * Get all {@link ListEventListener}s observing the {@link EventList}.
     */
    public List<ListEventListener<E>> getListEventListeners() {
        return delegate.getListEventListeners();
    }

    /**
     * Base class for implementing ListEventAssembler logic, regardless of how
     * the list events are stored.
     */
    static abstract class AssemblerHelper<E> {

        /** the list that this tracks changes for */
        protected EventList<E> sourceList;
        /** the current reordering array if this change is a reorder */
        protected int[] reorderMap = null;

        /** the sequences that provide a view on this queue */
        protected List<ListEventListener<E>> listeners = new ArrayList<ListEventListener<E>>();
        protected List<ListEvent<E>> listenerEvents = new ArrayList<ListEvent<E>>();

        /** the pipeline manages the distribution of events */
        protected ListEventPublisher publisher = null;

        /** the event level is the number of nested events */
        protected int eventLevel = 0;
        /** whether to allow nested events */
        protected boolean allowNestedEvents = true;
        /** whether to allow contradicting events */
        protected boolean allowContradictingEvents = false;


        /**
         * Starts a new atomic change to this list change queue.
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

        protected abstract void prepareEvent();

        /**
         * Sets the current event as a reordering. Reordering events cannot be
         * combined with other events.
         */
        public void reorder(int[] reorderMap) {
            if(!isEventEmpty()) throw new IllegalStateException("Cannot combine reorder with other change events");
            // can't reorder an empty list, see bug 91
            if(reorderMap.length == 0) return;
            addChange(ListEvent.DELETE, 0, reorderMap.length - 1);
            addChange(ListEvent.INSERT, 0, reorderMap.length - 1);
            this.reorderMap = reorderMap;
        }

        /**
         * Adds a block of changes to the set of list changes. The change block
         * allows a range of changes to be grouped together for efficiency.
         */
        public abstract void addChange(int type, int startIndex, int endIndex);


        /**
         * @return <tt>true</tt> if the current atomic change to this list change
         *      queue is empty; <tt>false</tt> otherwise
         */
        public abstract boolean isEventEmpty();

        /**
         * Commits the current atomic change to this list change queue.
         */
        public synchronized void commitEvent() {
            // complain if we have no event to commit
            if(eventLevel == 0) throw new IllegalStateException("Cannot commit without an event in progress");

            // we are one event less nested
            eventLevel--;
            allowNestedEvents = true;

            // if this is the last stage, sort and fire
            if(eventLevel == 0) {
                fireEvent();
            }
        }

        /**
         * Fires the current event. This needs to be called for each fired
         * event exactly once, even if that event includes nested events.
         */
        protected abstract void fireEvent();

        /**
         * Forward the specified event to listeners.
         */
        public abstract void forwardEvent(ListEvent<?> listChanges);

        /**
         * Adds the specified listener.
         */
        public synchronized void addListEventListener(ListEventListener<E> listChangeListener) {
            updateListEventListeners(listChangeListener, null);
            publisher.addDependency(sourceList, listChangeListener);
        }

        /**
         * Removes the specified listener.
         */
        public synchronized void removeListEventListener(ListEventListener<E> listChangeListener) {
            updateListEventListeners(null, listChangeListener);
            publisher.removeDependency(sourceList, listChangeListener);
        }

        /**
         * This method does three things:
         *
         * <ol>
         *   <li> adds the listener <code>toAdd</code> if it is non-null
         *   <li> removes the listener <code>toRemove</code> if it is non-null
         *   <li> tests each WeakReferenceProxy to see if its referent
         *        ListEventListener has been garbage collected, and thus the
         *        WeakReferenceProxy is able to be unregistered
         * </ol>
         *
         * @param toAdd a ListEventListener to be added, or <code>null</code>
         * @param toRemove a ListEventListener to be removed, or <code>null</code>
         */
        private void updateListEventListeners(ListEventListener<E> toAdd, ListEventListener<E> toRemove) {
            // only work on copies of the Lists and swap them in place at the end
            final List<ListEventListener<E>> listenersCopy = new ArrayList<ListEventListener<E>>(listeners);
            final List<ListEvent<E>> listenerEventsCopy = new ArrayList<ListEvent<E>>(listenerEvents);

            // a flag to determine whether we found the ListEventListener toRemove
            boolean toRemoveFound = (toRemove == null);

            for (int i = listenersCopy.size()-1; i >= 0; i--) {
                final ListEventListener<E> listener = listenersCopy.get(i);

                // if we're supposed to remove this ListEventListener, do so
                if (listener == toRemove) {
                    toRemoveFound = true;

                // if the ListEventListener is a WeakReferenceProxy with a null
                // (i.e. garbage collected) referent, also remove it
                } else if (listener instanceof WeakReferenceProxy) {
                    final WeakReferenceProxy weakReferenceProxy = (WeakReferenceProxy) listener;
                    if (weakReferenceProxy.getReferent() != null) continue;
                    weakReferenceProxy.dispose();

                // otherwise we should not remove this listener
                } else {
                    continue;
                }

                // remove the listener
                listenersCopy.remove(i);
                listenerEventsCopy.remove(i);
            }

            // sanity check to ensure we found the listener we were asked to remove, if any
            if (!toRemoveFound)
                throw new IllegalArgumentException("Cannot remove nonexistent listener " + toRemove);

            // add the listener we were asked to add, if any
            if (toAdd != null) {
                listenersCopy.add(toAdd);
                listenerEventsCopy.add(createListEvent());
            }

            // swap the copies overtop of the real thing
            listeners = listenersCopy;
            listenerEvents = listenerEventsCopy;
        }

        /**
         * Gets the reorder map for the specified atomic change or null if that
         * change is not a reordering.
         */
        int[] getReorderMap() {
            return reorderMap;
        }

        protected abstract ListEvent<E> createListEvent();

        /**
         * Get all {@link ListEventListener}s observing the {@link EventList}.
         */
        public List<ListEventListener<E>> getListEventListeners() {
            return Collections.unmodifiableList(listeners);
        }
    }

    /**
     * ListEventAssembler using {@link BarcodeListDeltas} to store list changes.
     */
    static class BarcodeDeltasAssembler<E> extends AssemblerHelper<E> {

        private BarcodeListDeltas listDeltas = new BarcodeListDeltas();

        /**
         * Creates a new ListEventAssembler that tracks changes for the specified list.
         */
        public BarcodeDeltasAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
            this.sourceList = sourceList;
            this.publisher = publisher;
        }

        protected void prepareEvent() {
            listDeltas.reset(sourceList.size());
        }

        public void addChange(int type, int startIndex, int endIndex) {
            for(int i = startIndex; i <= endIndex; i++) {
                if(type == ListEvent.INSERT) {
                    listDeltas.add(i);
                } else if(type == ListEvent.UPDATE) {
                    listDeltas.update(i);
                } else if(type == ListEvent.DELETE) {
                    listDeltas.remove(startIndex);
                }
            }
        }

        public BarcodeListDeltas getListDeltas() {
            return listDeltas;
        }

        public boolean isEventEmpty() {
            return listDeltas.isEmpty();
        }

        protected void fireEvent() {
            try {
                // bail on empty changes
                if(isEventEmpty()) return;

                final List<ListEventListener<E>> listenersToNotify;
                final List<ListEvent<E>> listenerEventsToNotify;

                // grab a consistent snapshot of the parallel lists
                synchronized (this) {
                    listenersToNotify = listeners;
                    listenerEventsToNotify = listenerEvents;
                }

                // reset the events before firing them
                for(Iterator<ListEvent<E>> e = listenerEventsToNotify.iterator(); e.hasNext();) {
                    e.next().reset();
                }

                // perform the notification on the duplicate list
                publisher.fireEvent(sourceList, listenersToNotify, listenerEventsToNotify);

            // clear the change for the next caller
            } finally {
                listDeltas.reset(0);
                reorderMap = null;
                allowContradictingEvents = false;
            }
        }

        public void forwardEvent(ListEvent<?> listChanges) {
            // if we're not nested, we can fire the event directly
            if(eventLevel == 0) {
                // todo: optimize by reusing the existing listDeltas
                beginEvent(false);
                while(listChanges.nextBlock()) {
                    addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                }
                commitEvent();

            // if we're nested, we have to copy this event's parts to our queue
            } else {
                beginEvent(false);
                this.reorderMap = null;
                if(isEventEmpty() && listChanges.isReordering()) {
                    reorder(listChanges.getReorderMap());
                } else {
                    while(listChanges.nextBlock()) {
                        addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                    }
                }
                commitEvent();
            }
        }

        protected ListEvent<E> createListEvent() {
            return new BarcodeListDeltasListEvent<E>(this, sourceList);
        }
    }



    /**
     * ListEventAssembler using {@link TreeDeltas} to store list changes.
     */
    static class TreeDeltasAssembler<E> extends AssemblerHelper<E> {

        /** prefer to use the linear blocks, which are more performant but handle only a subset of all cases */
        private BlockSequence blockSequence = new BlockSequence();
        private boolean useListBlocksLinear = false;

        /** fall back to list deltas 2, which are capable of all list changes */
        private TreeDeltas listDeltas = new TreeDeltas();

        /**
         * Creates a new ListEventAssembler that tracks changes for the specified list.
         */
        public TreeDeltasAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
            this.sourceList = sourceList;
            this.publisher = publisher;
        }

        /** {@inheritDoc} */
        protected void prepareEvent() {
            //listDeltas.reset(sourceSize);
            listDeltas.setAllowContradictingEvents(this.allowContradictingEvents);
            useListBlocksLinear = true;
        }

        /** {@inheritDoc} */
        public void addChange(int type, int startIndex, int endIndex) {
            // try the linear holder first
            if(useListBlocksLinear) {
                boolean success = blockSequence.addChange(type, startIndex, endIndex + 1);
                if(success) {
                    return;
                } else {
                    listDeltas.addAll(blockSequence);
                    useListBlocksLinear = false;
                }
            }

            // try the good old reliable deltas 2
            if(type == ListEvent.INSERT) {
                listDeltas.insert(startIndex, endIndex + 1);
            } else if(type == ListEvent.UPDATE) {
                listDeltas.update(startIndex, endIndex + 1);
            } else if(type == ListEvent.DELETE) {
                listDeltas.delete(startIndex, endIndex + 1);
            }
        }

        public boolean getUseListBlocksLinear() {
            return useListBlocksLinear;
        }

        public TreeDeltas getListDeltas() {
            return listDeltas;
        }

        public BlockSequence getListBlocksLinear() {
            return blockSequence;
        }

        /** {@inheritDoc} */
        public boolean isEventEmpty() {
            if(useListBlocksLinear) return blockSequence.isEmpty();
            else return listDeltas.isEmpty();
        }

        /** {@inheritDoc} */
        protected void fireEvent() {
            try {
                // bail on empty changes
                if(isEventEmpty()) return;

                final List<ListEventListener<E>> listenersToNotify;
                final List<ListEvent<E>> listenerEventsToNotify;

                // grab a consistent snapshot of the parallel lists
                synchronized (this) {
                    listenersToNotify = listeners;
                    listenerEventsToNotify = listenerEvents;
                }

                // reset the events before firing them
                for(Iterator<ListEvent<E>> e = listenerEventsToNotify.iterator(); e.hasNext();) {
                    e.next().reset();
                }

                // perform the notification on the duplicate list
                publisher.fireEvent(sourceList, listenersToNotify, listenerEventsToNotify);

            // clear the change for the next caller
            } finally {
                blockSequence.reset();
                listDeltas.reset(sourceList.size());
                reorderMap = null;
                allowContradictingEvents = false;
            }
        }

        public void forwardEvent(ListEvent<?> listChanges) {
            // if we're not nested, we can fire the event directly
            if(eventLevel == 0) {
                // todo: optimize by reusing the existing listDeltas
                beginEvent(false);
                while(listChanges.nextBlock()) {
                    addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                }
                commitEvent();

            // if we're nested, we have to copy this event's parts to our queue
            } else {
                beginEvent(false);
                this.reorderMap = null;
                if(isEventEmpty() && listChanges.isReordering()) {
                    reorder(listChanges.getReorderMap());
                } else {
                    while(listChanges.nextBlock()) {
                        addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                    }
                }
                commitEvent();
            }
        }

        protected ListEvent<E> createListEvent() {
            return new TreeDeltasListEvent<E>(this, sourceList);
        }

        public String toString() {
            return listDeltas.toString();
        }
    }


    /**
     * ListEventAssembler using {@link Block}s to store list changes.
     */
    static class BlockDeltasAssembler<E> extends AssemblerHelper<E> {

        /** the current working copy of the atomic change */
        private List<Block> atomicChangeBlocks = null;
        /** the most recent list change; this is the only change we can append to */
        private Block atomicLatestBlock = null;

        /**
         * Creates a new ListEventAssembler that tracks changes for the specified list.
         */
        public BlockDeltasAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
            this.sourceList = sourceList;
            this.publisher = publisher;
        }

        /** {@inheritDoc} */
        protected ListEvent<E> createListEvent() {
            return new BlockDeltasListEvent<E>(this, sourceList);
        }

        /** {@inheritDoc} */
        protected void prepareEvent() {
            atomicChangeBlocks = new ArrayList<Block>();
            atomicLatestBlock = null;
            reorderMap = null;
        }

        /** {@inheritDoc} */
        public void addChange(int type, int startIndex, int endIndex) {
            // attempt to merge this into the most recent block
            if(atomicLatestBlock != null) {
                boolean appendSuccess = atomicLatestBlock.append(startIndex, endIndex, type);
                if(appendSuccess) return;
            }

            // create a new block for the change
            atomicLatestBlock = new Block(startIndex, endIndex, type);
            atomicChangeBlocks.add(atomicLatestBlock);
        }

        /** {@inheritDoc} */
        public void forwardEvent(ListEvent<?> listChanges) {
            // if we're not nested, we can fire the event directly
            if(eventLevel == 0) {
                atomicChangeBlocks = listChanges.getBlocks();
                reorderMap = listChanges.isReordering() ? listChanges.getReorderMap() : null;
                fireEvent();

            // if we're nested, we have to copy this event's parts to our queue
            } else {
                beginEvent(false);
                this.reorderMap = null;
                if(isEventEmpty() && listChanges.isReordering()) {
                    reorder(listChanges.getReorderMap());
                } else {
                    while(listChanges.nextBlock()) {
                        addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                    }
                }
                commitEvent();
            }
        }

        /** {@inheritDoc} */
        public boolean isEventEmpty() {
            return this.atomicChangeBlocks == null || this.atomicChangeBlocks.isEmpty();
        }

        /** {@inheritDoc} */
        protected void fireEvent() {
            Block.sortListEventBlocks(atomicChangeBlocks, allowContradictingEvents);

            try {
                // bail on empty changes
                if(isEventEmpty()) return;

                final List<ListEventListener<E>> listenersToNotify;
                final List<ListEvent<E>> listenerEventsToNotify;

                // grab a consistent snapshot of the parallel lists
                synchronized (this) {
                    listenersToNotify = listeners;
                    listenerEventsToNotify = listenerEvents;
                }

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
         * @return a List containing the sequence of {@link Block}s modelling
         *      the specified change. It is an error to modify this list or its contents.
         */
        List<Block> getBlocks() {
            return atomicChangeBlocks;
        }
    }
}