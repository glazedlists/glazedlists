/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

// the core Glazed Lists package
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.WeakReferenceProxy;

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
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ListEventAssembler<E> {

    /** the delegate contains logic specific for the current event storage strategy */
    private final AssemblerHelper<E> delegate;

    /**
     * Our strategy will be for the list event assembler.
     * We support 4 possible strategies:
     * <li>blockdeltas: originally the only strategy, this uses a bubblesort
     *     which causes certain sortedlist events to be very slow
     * <li>barcodedeltas: a proof of concept based on the barcode class, this
     *     strategy is particularly performant, although not very elegant!
     * <li>treedeltas: uses our specially designed general tree ADT
     * <li>tree4deltas: a fork of our treedeltas strategy that tweaks the
     *     code in the general tree to be particularly performant for tracking
     *     list changes
     *
     * <p>Ideally we will support fewer strategies in the future. In particular,
     * we plan on keeping only treedeltas and removing the other strategies.
     */
    private static final String assemblerName;

    /**
     * Our strategy for managing dependencies, which we call "publishing".
     * We support 2 possible strategies:
     * <li>graphdependencies: for each change, a graph is crawled of listeners
     *     who require notification and who can be notified. The order of
     *     notification is done by dynamically analyzing this graph at change
     *     time.
     * <li>sequencedependencies: a list is precomputed of the order of notification,
     *     and listeners are always notified in sequence corresponding to order
     *     in this list.
     */
    private static final String publisherName;

    // look up strategies using System properties
    static {
        String assemblerProperty = null;
        String publisherProperty = null;
        try {
            assemblerProperty = System.getProperty("GlazedLists.ListEventAssemblerDelegate");
            publisherProperty = System.getProperty("GlazedLists.ListEventPublisher");
        } catch(SecurityException e) {
            // do nothing
        }
        if(assemblerProperty != null) assemblerName = assemblerProperty;
        else assemblerName = "treedeltas";
        if(publisherProperty != null) publisherName = publisherProperty;
        else publisherName = "sequencedependencies";
    }

    /**
     * Create a delegate using the current assembler.
     */
    private static <E> AssemblerHelper<E> createAssemblerDelegate(EventList<E> sourceList, ListEventPublisher publisher) {
        if(assemblerName.equals("treedeltas")) {
            return new Tree4DeltasAssembler<E>(sourceList, publisher);
        } else if(assemblerName.equals("blockdeltas")) {
            return new BlockDeltasAssembler<E>(sourceList, publisher);
        } else if(assemblerName.equals("barcodedeltas")) {
            return new BarcodeDeltasAssembler<E>(sourceList, publisher);
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Create a new {@link ListEventPublisher} for an {@link EventList} not attached
     * to any other {@link EventList}s.
     */
    public static ListEventPublisher createListEventPublisher() {
        if(publisherName.equals("graphdependencies")) {
            return new GraphDependenciesListEventPublisher();
        } else if(publisherName.equals("sequencedependencies")) {
            return new SequenceDependenciesEventPublisher();
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Creates a new ListEventAssembler that tracks changes for the specified list.
     */
    public ListEventAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
        delegate = createAssemblerDelegate(sourceList, publisher);
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
     * Add to the current ListEvent the removal of the element at the specified
     * index, with the specified previous value.
     */
    public void elementRemoved(int index, E removedValue) {
        delegate.elementRemoved(index, removedValue);
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
        delegate.publisherAdapter.addListEventListener(listChangeListener, delegate.createListEvent());
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
        delegate.publisherAdapter.removeListEventListener(listChangeListener);
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

        private Thread eventThread;

        /** the list that this tracks changes for */
        protected EventList<E> sourceList;
        /** the current reordering array if this change is a reorder */
        protected int[] reorderMap = null;

        /** the active implementation of {@link ListEventPublisher} */
        protected PublisherAdapter<E> publisherAdapter;

        /** the event level is the number of nested events */
        protected int eventLevel = 0;
        /** whether an event has been prepared but not yet fired */
        protected boolean eventPending = false;
        /** whether to allow nested events */
        protected boolean allowNestedEvents = true;
        /** whether to allow contradicting events */
        private boolean allowContradictingEvents = false;

        protected AssemblerHelper(EventList<E> sourceList, ListEventPublisher publisher) {
            this.sourceList = sourceList;
            this.publisherAdapter = createPublisherDelegate(publisher);
        }

        /**
         * Create a {@link PublisherAdapter} using the current strategy.
         */
        private PublisherAdapter<E> createPublisherDelegate(ListEventPublisher publisher) {
            if(publisherName.equals("graphdependencies")) {
                return new GraphSequencePublisherAdapter<E>(this, publisher);
            } else if(publisherName.equals("sequencedependencies")) {
                return new ListSequencePublisherAdapter<E>(this, publisher);
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Starts a new atomic change to this list change queue.
         */
        public final synchronized void beginEvent(boolean allowNestedEvents) {
            // complain if we cannot nest any further
            if(!this.allowNestedEvents) {
                throw new ConcurrentModificationException("Cannot begin a new event while another event is in progress by thread, "  + eventThread.getName());
            }
            this.allowNestedEvents = allowNestedEvents;
            if(allowNestedEvents || (eventLevel == 0 && eventPending)) {
                setAllowContradictingEvents(true);
            }

            // prepare for a new event if we haven't already
            if(!eventPending) {
                this.eventPending = true;
                this.eventThread = Thread.currentThread();
                prepareEvent();
            }

            // track how deeply nested we are
            eventLevel++;
        }

        protected abstract void prepareEvent();

        protected void setAllowContradictingEvents(boolean allowContradictingEvents) {
            this.allowContradictingEvents = allowContradictingEvents;
        }
        protected boolean getAllowContradictingEvents() {
            return allowContradictingEvents;
        }

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
         * Adds a block describing a deleted element.
         */
        public abstract void elementRemoved(int index, E removedValue);

        /**
         * @return <tt>true</tt> if the current atomic change to this list change
         *      queue is empty; <tt>false</tt> otherwise
         */
        public abstract boolean isEventEmpty();

        /**
         * Commits the current atomic change to this list change queue.
         */
        public final synchronized void commitEvent() {
            // complain if we have no event to commit
            if(eventLevel == 0) throw new IllegalStateException("Cannot commit without an event in progress");

            // we are one event less nested
            eventLevel--;
            allowNestedEvents = true;

            // if this is the last stage, sort and fire
            if(eventLevel == 0) {
                beforeFireEvent();
                if(!isEventEmpty()) {
                    publisherAdapter.fireEvent();
                } else {
                    eventPending = false;
                    cleanup();
                }
            }
        }

        /**
         * Hook method to prepare for a fire event. This method may be called
         * multiple times.
         */
        protected void beforeFireEvent() {
            // do nothing
        }

        /**
         * Forward the specified event to listeners.
         */
        public final synchronized void forwardEvent(ListEvent<?> listChanges) {
            beginEvent(false);
            this.reorderMap = null;
            if(isEventEmpty() && listChanges.isReordering()) {
                reorder(listChanges.getReorderMap());
            } else {
                while(listChanges.nextBlock()) {
                    addChange(listChanges.getType(), listChanges.getBlockStartIndex(), listChanges.getBlockEndIndex());
                }
                listChanges.reset();
            }
            commitEvent();
        }

        /**
         * Cleanup all temporary variables necessary while events are being fired.
         */
        public abstract void cleanup();

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
            return publisherAdapter.getListEventListeners();
        }
    }

    /**
     * ListEventAssembler using {@link BarcodeListDeltas} to store list changes.
     *
     * @deprecated replaced with {@link Tree4DeltasAssembler}
     */
    static class BarcodeDeltasAssembler<E> extends AssemblerHelper<E> {

        private BarcodeListDeltas listDeltas = new BarcodeListDeltas();

        /**
         * Creates a new ListEventAssembler that tracks changes for the specified list.
         */
        public BarcodeDeltasAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
            super(sourceList, publisher);
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

        /** {@inheritDoc} */
        public void elementRemoved(int index, Object removedValue) {
            addChange(ListEvent.DELETE, index, index);
        }

        public BarcodeListDeltas getListDeltas() {
            return listDeltas;
        }

        public boolean isEventEmpty() {
            return listDeltas.isEmpty();
        }

        /** {@inheritDoc} */
        public void cleanup() {
            listDeltas.reset(0);
            reorderMap = null;
            setAllowContradictingEvents(false);
        }

        protected ListEvent<E> createListEvent() {
            return new BarcodeListDeltasListEvent<E>(this, sourceList);
        }
    }

    /**
     * ListEventAssembler using {@link Tree4Deltas} to store list changes.
     */
    static class Tree4DeltasAssembler<E> extends AssemblerHelper<E> {

        /** prefer to use the linear blocks, which are more performant but handle only a subset of all cases */
        private BlockSequence<E> blockSequence = new BlockSequence<E>();
        private boolean useListBlocksLinear = false;

        /** fall back to list deltas 2, which are capable of all list changes */
        private Tree4Deltas<E> listDeltas = new Tree4Deltas<E>();

        /**
         * Creates a new ListEventAssembler that tracks changes for the specified list.
         */
        public Tree4DeltasAssembler(EventList<E> sourceList, ListEventPublisher publisher) {
            super(sourceList, publisher);
        }

        /** {@inheritDoc} */
        protected void prepareEvent() {
            useListBlocksLinear = true;
        }

        protected void setAllowContradictingEvents(boolean allowContradictingEvents) {
            super.setAllowContradictingEvents(allowContradictingEvents);
            listDeltas.setAllowContradictingEvents(allowContradictingEvents);
        }

        /** {@inheritDoc} */
        public void addChange(int type, int startIndex, int endIndex) {
            // try the linear holder first
            if(useListBlocksLinear) {
                boolean success = blockSequence.addChange(type, startIndex, endIndex + 1, (E)ListEvent.UNKNOWN_VALUE);
                if(success) {
                    return;

                // convert from linear to tree4deltas
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
                listDeltas.delete(startIndex, endIndex + 1, (E)ListEvent.UNKNOWN_VALUE);
            }
        }

        /** {@inheritDoc} */
        public void elementRemoved(int index, E removedValue) {
            // try the linear holder first
            if(useListBlocksLinear) {
                boolean success = blockSequence.addChange(ListEvent.DELETE, index, index + 1, removedValue);
                if(success) {
                    return;

                // convert from linear to tree4deltas
                } else {
                    listDeltas.addAll(blockSequence);
                    useListBlocksLinear = false;
                }
            }

            // try the good old reliable deltas 2
            listDeltas.delete(index, index + 1, removedValue);
        }

        public boolean getUseListBlocksLinear() {
            return useListBlocksLinear;
        }

        public Tree4Deltas getListDeltas() {
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
        public void cleanup() {
            blockSequence.reset();
            listDeltas.reset(sourceList.size());
            reorderMap = null;
            setAllowContradictingEvents(false);
        }

        protected ListEvent<E> createListEvent() {
            return new Tree4DeltasListEvent<E>(this, sourceList);
        }

        public String toString() {
            return listDeltas.toString();
        }
    }



    /**
     * ListEventAssembler using {@link Block}s to store list changes.
     *
     * @deprecated replaced with {@link Tree4DeltasAssembler}
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
            super(sourceList, publisher);
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
        public void elementRemoved(int index, Object removedValue) {
            addChange(ListEvent.DELETE, index, index);
        }

        /** {@inheritDoc} */
        public boolean isEventEmpty() {
            return this.atomicChangeBlocks == null || this.atomicChangeBlocks.isEmpty();
        }

        /** {@inheritDoc} */
        protected void beforeFireEvent() {
            Block.sortListEventBlocks(atomicChangeBlocks, getAllowContradictingEvents());
        }

        /** {@inheritDoc} */
        public void cleanup() {
            atomicChangeBlocks = null;
            atomicLatestBlock = null;
            reorderMap = null;
            setAllowContradictingEvents(false);
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

    /**
     * Manage listeners and firing events in a safe order in which dependencies
     * are always satisfied before listeners are notified.
     */
    private interface PublisherAdapter<E> {

        /**
         * Adds the specified listener.
         */
        void addListEventListener(ListEventListener<E> listChangeListener, ListEvent<E> listEvent);

        /**
         * Removes the specified listener.
         */
        void removeListEventListener(ListEventListener<E> listener);

        /**
         * Get all list event listeners.
         */
        List<ListEventListener<E>> getListEventListeners();

        /**
         * Notify the listeners of the list's changes.
         */
        void fireEvent();
    }

    /**
     * Delegate to the classic {@link ListEventPublisher}.
     */
    private static class GraphSequencePublisherAdapter<E> implements PublisherAdapter<E> {

        /** the list event assembler being acted upon */
        private AssemblerHelper<E> assemblerHelper;
        /** the list that this tracks changes for */
        private final EventList<E> sourceList;

        /** the sequences that provide a view on this queue */
        private List<ListEventListener<E>> listeners = new ArrayList<ListEventListener<E>>();
        private List<ListEvent<E>> listenerEvents = new ArrayList<ListEvent<E>>();

        private final GraphDependenciesListEventPublisher publisher;

        public GraphSequencePublisherAdapter(AssemblerHelper<E> assemblerDelegate, ListEventPublisher publisher) {
            this.assemblerHelper = assemblerDelegate;
            this.sourceList = assemblerDelegate.sourceList;
            this.publisher = (GraphDependenciesListEventPublisher)publisher;
        }

        /** {@inheritDoc} */
        public synchronized void addListEventListener(ListEventListener<E> listChangeListener, ListEvent<E> listEvent) {
            updateListEventListeners(listChangeListener, null, listEvent);
            publisher.addDependency(sourceList, listChangeListener);
        }

        /** {@inheritDoc} */
        public synchronized void removeListEventListener(ListEventListener<E> listChangeListener) {
            updateListEventListeners(null, listChangeListener, null);
            publisher.removeDependency(sourceList, listChangeListener);
        }

        /** {@inheritDoc} */
        public synchronized List<ListEventListener<E>> getListEventListeners() {
            return Collections.unmodifiableList(listeners);
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
        private void updateListEventListeners(ListEventListener<E> toAdd, ListEventListener<E> toRemove, ListEvent<E> listEvent) {
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
                listenerEventsCopy.add(listEvent);
            }

            // swap the copies overtop of the real thing
            listeners = listenersCopy;
            listenerEvents = listenerEventsCopy;
        }

        /** {@inheritDoc} */
        public void fireEvent() {
            final List<ListEventListener<E>> listenersToNotify;
            final List<ListEvent<E>> listenerEventsToNotify;

            // grab a consistent snapshot of the parallel lists
            synchronized(this) {
                listenersToNotify = listeners;
                listenerEventsToNotify = listenerEvents;
            }

            // reset the events before firing them
            for(Iterator<ListEvent<E>> e = listenerEventsToNotify.iterator(); e.hasNext();) {
                e.next().reset();
            }

            // perform the notification on the duplicate list
            try {
                publisher.fireEvent(sourceList, listenersToNotify, listenerEventsToNotify);
            } finally {
                assemblerHelper.eventPending = false;
                assemblerHelper.cleanup();
            }
        }
    }

    /**
     * Delegate to the improved {@link SequenceDependenciesEventPublisher}.
     */
    private static class ListSequencePublisherAdapter<E> implements PublisherAdapter<E> {

        private final EventList<E> sourceList;
        private final SequenceDependenciesEventPublisher publisherSequenceDependencies;
        private ListEvent<E> listEvent = null;
        private final ListEventFormat<E> eventFormat;
        private boolean eventEnqueued = false;

        public ListSequencePublisherAdapter(AssemblerHelper<E> assemblerDelegate, ListEventPublisher publisherSequenceDependencies) {
            this.sourceList = assemblerDelegate.sourceList;
            this.eventFormat = new ListEventFormat<E>(assemblerDelegate);
            this.publisherSequenceDependencies = (SequenceDependenciesEventPublisher)publisherSequenceDependencies;
        }

        /** {@inheritDoc} */
        public void addListEventListener(ListEventListener<E> listChangeListener, ListEvent<E> listEvent) {
            publisherSequenceDependencies.addListener(sourceList, listChangeListener, eventFormat);
            if(this.listEvent == null) this.listEvent = listEvent;
        }

        /** {@inheritDoc} */
        public void removeListEventListener(ListEventListener<E> listener) {
            publisherSequenceDependencies.removeListener(sourceList, listener);
        }

        /** {@inheritDoc} */
        public List<ListEventListener<E>> getListEventListeners() {
            return publisherSequenceDependencies.getListeners(sourceList);
        }

        /** {@inheritDoc} */
        public void fireEvent() {
            // we've already fired this event, we're just adding to it
            if(eventEnqueued) {
                return;
            }

            eventEnqueued = true;
            publisherSequenceDependencies.fireEvent(sourceList, listEvent, eventFormat);
        }

        /**
         * Adapt {@link SequenceDependenciesEventPublisher.EventFormat} for use with {@link ListEvent}s.
         */
        private static class ListEventFormat<E> implements SequenceDependenciesEventPublisher.EventFormat<EventList<E>,ListEventListener<E>,ListEvent<E>> {
            private AssemblerHelper<E> assemblerDelegate;

            public ListEventFormat(AssemblerHelper<E> assemblerDelegate) {
                this.assemblerDelegate = assemblerDelegate;
            }
            public void fire(EventList<E> subject, ListEvent<E> event, ListEventListener<E> listener) {
                event.reset();
                listener.listChanged(event);
            }
            public void postEvent(EventList<E> subject) {
                assemblerDelegate.eventPending = false;
                assemblerDelegate.cleanup();
                ((ListSequencePublisherAdapter)assemblerDelegate.publisherAdapter).eventEnqueued = false;
            }
            public boolean isStale(EventList<E> subject, ListEventListener<E> listener) {
                if(listener instanceof WeakReferenceProxy && ((WeakReferenceProxy)listener).getReferent() == null) {
                    ((WeakReferenceProxy)listener).dispose();
                    return true;
                }
                return false;
            }
        }
    }
}