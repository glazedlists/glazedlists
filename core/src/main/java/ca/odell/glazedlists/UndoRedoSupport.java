/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * UndoRedoSupport, as the name suggests, will provide generic support for
 * undoing and redoing groups of changes to an {@link EventList}. The
 * granularity of each undoable edit is determined by the ListEvent from which
 * it was generated.
 *
 * <p>Not every change described in a ListEvent results in an undoable edit.
 * Specifically, a <strong>mutation</strong> of a list element IN PLACE does
 * not produce an undoable edit. For example, an {@link ObservableElementList}
 * which observes a change of an element, or a call to {@link List#set} with
 * the same object at that index produce a ListEvent that does not have a
 * corresponding {@link Edit} object. These ListEvents are ignored because they
 * lack sufficient information to undo or redo the change.
 *
 * <p>In general UndoRedoSupport only makes sense for use with a
 * {@link BasicEventList} or a trivial wrapper around a BasicEventList which
 * does not affect the order or type of the elements, such as an
 * {@link ObservableElementList}. Advanced transformations, such as
 * {@link SortedList} or {@link FilterList} will not work as expected with this
 * UndoRedoSupport class since their contents are controlled by information
 * outside of themselves ({@link Comparator}s and
 * {@link ca.odell.glazedlists.matchers.Matcher}s).
 *
 * <p>This class is agnostic to any particular GUI toolkit. As such it may be
 * used in a headless environment or can also be bound to a specific toolkit.
 *
 * @author James Lemieux
 */
public final class UndoRedoSupport<E> {

    /** A wrapper around the true source EventList provides control over the granularity of ListEvents it produces. */
    private TransactionList<E> txSource;

    /** A ListEventListener that watches the {@link #txSource} and in turn broadcasts an {@link Edit} object to all {@link Listener}s */
    private ListEventListener<E> txSourceListener = new TXSourceListener();

    /** A data structure storing all registered {@link Listener}s. */
    private final CopyOnWriteArrayList<Listener> listenerList = new CopyOnWriteArrayList<Listener>();

    /**
     * A count which, when greater than 0, indicates a ListEvent must be
     * ignored by this UndoRedoSupport because it was caused by an undo or
     * redo. An int is used rather than a boolean flag so that nested
     * undos/redos are properly handled.
     */
    private int ignoreListEvent = 0;

    /**
     * A list of the elements in the <code>source</code> prior to the most
     * recent change. It is necessary to track the prior elements since
     * list removals broadcast ListEvents which do not include the removed
     * element as part of the ListEvent. We use this list to locate
     * removed elements when constructing objects that can undo the change.
     * todo remove this list when ListEvent can reliably furnish us with a deleted value
     */
    private List<E> priorElements;

    private UndoRedoSupport(EventList<E> source) {
        // build a TransactionList that does NOT support rollback - we don't
        // need it and it relies on UndoRedoSupport, so we would have
        this.txSource = new TransactionList<E>(source, false);
        this.txSource.addListEventListener(txSourceListener);

        this.priorElements = new ArrayList<E>(source);
    }

    /**
     * Add a {@link Listener} which will receive a callback when an undoable
     * edit occurs on the given source {@link EventList}.
     */
    public void addUndoSupportListener(Listener l) {
    	if (l != null) {
    		listenerList.add(l);
    	}
    }

    /**
     * Remove a {@link Listener} from receiving a callback when an undoable
     * edit occurs on the given source {@link EventList}.
     */
    public void removeUndoSupportListener(Listener l) {
    	if (l != null) {
    		listenerList.remove(l);
    	}
    }

    /**
     * Notifies all registered {@link Listener}s of the given <code>edit</code>.
     */
    private void fireUndoableEditHappened(Edit edit) {
		// NOTE: We are intentionally dispatching in LIFO order with an iterator
		// we need to clone before reverse iteration to be thread-safe, see http://stackoverflow.com/a/42046731/336169
		List<Listener> listenerListCopy = (List<Listener>) listenerList.clone();
	    ListIterator<Listener> li = listenerListCopy.listIterator(listenerListCopy.size());
		while (li.hasPrevious()) {
			li.previous().undoableEditHappened(edit);
		}
    }

    /**
     * Installs support for undoing and redoing changes to the given
     * <code>source</code>. To be notified of undoable changes, a
     * {@link Listener} must be registered on the object that is returned by
     * this method. That Listener object will typically add the {@link Edit}
     * it is given over to whatever data structure is managing all undo/redo
     * functions for the entire application.
     *
     * @param source the EventList on which to provide undo/redo capabilities
     * @return an instance of UndoRedoSupport through which the undo/redo behaviour
     *      can be customized
     */
    public static <E> UndoRedoSupport install(EventList<E> source) {
        return new UndoRedoSupport<E>(source);
    }

    /**
     * This method removes undo/redo support from the {@link EventList} it was
     * installed on. This method is useful when the {@link EventList} must
     * outlive the UndoRedoSupport object itself. The UndoRedoSupport object will become
     * available for garbage collection independently of the {@link EventList}
     * it decorated with behaviour.
     */
    public void uninstall() {
        txSource.dispose();
        txSource.removeListEventListener(txSourceListener);

        txSource = null;
        priorElements = null;
    }

    /**
     * This Listener watches the TransactionList for changes and responds by
     * created an {@link Edit} and broadcasting that object to all registered
     * {@link Listener}s.
     */
    private class TXSourceListener implements ListEventListener<E> {
        @Override
        public void listChanged(ListEvent<E> listChanges) {
            // if an undo or redo caused this ListEvent, it is not an undoable edit
            if (ignoreListEvent > 0)
                return;

            // build a CompositeEdit that describes the ListEvent and provides methods for undoing and redoing it
            final CompositeEdit edit = new CompositeEdit();

            while (listChanges.next()) {
                final int changeIndex = listChanges.getIndex();
                final int changeType = listChanges.getType();

                // provide an AddEdit to the CompositeEdit
                if (changeType == ListEvent.INSERT) {
                    final E inserted = txSource.get(changeIndex);
                    priorElements.add(changeIndex, inserted);
                    edit.add(new AddEdit<E>(txSource, changeIndex, inserted));

                // provide a RemoveEdit to the CompositeEdit
                } else if (changeType == ListEvent.DELETE) {
                    // try to get the previous value through the ListEvent
                    E deleted = listChanges.getOldValue();
                    E deletedElementFromPrivateCopy = priorElements.remove(changeIndex);

                    // if the ListEvent could not give us the previous value, use the value from priorElements
                    if (deleted == ListEvent.UNKNOWN_VALUE)
                        deleted = deletedElementFromPrivateCopy;

                    edit.add(new RemoveEdit<E>(txSource, changeIndex, deleted));

                // provide an UpdateEdit to the CompositeEdit
                } else if (changeType == ListEvent.UPDATE) {
                    E previousValue = listChanges.getOldValue();

                    // if the ListEvent could not give us the previous value, use the value from priorElements
                    if (previousValue == ListEvent.UNKNOWN_VALUE)
                        previousValue = priorElements.get(changeIndex);

                    final E newValue = txSource.get(changeIndex);

                    // if a different object is present at the index
                    if (newValue != previousValue) {
                        priorElements.set(changeIndex, newValue);
                        edit.add(new UpdateEdit<E>(txSource, changeIndex, newValue, previousValue));
                    }
                }
            }

            // if the edit has real contents, broadcast it
            if (!edit.isEmpty())
                fireUndoableEditHappened(edit.getSimplestEdit());
        }
    }

    /**
     * Implementations of this Listener interface should be registered with an
     * UndoRedoSupport object via {@link UndoRedoSupport#addUndoSupportListener}. They
     * will be notified of each undoable edit that occurs to the given EventList.
     */
    @FunctionalInterface
    public interface Listener extends EventListener {
        /**
         * Notified of each undoable edit applied to the given EventList.
         */
        public void undoableEditHappened(Edit edit);
    }

    /**
     * Provides an easy interface to undo/redo a ListEvent in its entirety.
     * At any point in time it is only possible to do one, and only one, of
     * {@link #undo} and {@link #redo}. To determine which one is allowed, use
     * {@link #canUndo()} and {@link #canRedo()}.
     */
    public interface Edit {
        /** Undo the edit. */
        public void undo();

        /** Returns true if this edit may be undone. */
        public boolean canUndo();

        /** Re-applies the edit. */
        public void redo();

        /** Returns true if this edit may be redone. */
        public boolean canRedo();
    }

    private abstract class AbstractEdit implements Edit {
        /** Initially the Edit can be undone but not redone. */
        protected boolean canUndo = true;

        @Override
        public void undo() {
            // validate that we can proceed with the undo
            if (!canUndo())
                throw new IllegalStateException("The Edit is in an incorrect state for undoing");

            ignoreListEvent++;
            try {
                undoImpl();
            } finally {
                ignoreListEvent--;
            }

            canUndo = false;
        }

        @Override
        public void redo() {
            // validate that we can proceed with the redo
            if (!canRedo())
                throw new IllegalStateException("The Edit is in an incorrect state for redoing");

            ignoreListEvent++;
            try {
                redoImpl();
            } finally {
                ignoreListEvent--;
            }

            canUndo = true;
        }

        protected abstract void undoImpl();
        protected abstract void redoImpl();

        @Override
        public final boolean canUndo() { return canUndo; }
        @Override
        public final boolean canRedo() { return !canUndo; }
    }

    /**
     * An Edit which acts as a container for finer-grained Edit objects.
     */
    final class CompositeEdit extends AbstractEdit {

        /** The edits in the order they were made. */
        private final List<Edit> edits = new ArrayList<Edit>();

        /** Adds a single Edit to this container of Edits. */
        void add(Edit edit) { edits.add(edit); }

        /** Returns <tt>true</tt> if this container of Edits is empty; <tt>false</tt> otherwise. */
        private boolean isEmpty() { return edits.isEmpty(); }

        /** Returns the single Edit contained within this composite, if only one exists, otherwise it returns this entire CompositeEdit. */
        private Edit getSimplestEdit() { return edits.size() == 1 ? edits.get(0) : this; }

        @Override
        public void undoImpl() {
            txSource.beginEvent();
            try {
                // undo the Edits in reverse order they were applied
                for (ListIterator<Edit> i = edits.listIterator(edits.size()); i.hasPrevious();)
                    i.previous().undo();
            } finally {
                txSource.commitEvent();
            }
        }

        @Override
        public void redoImpl() {
            txSource.beginEvent();
            try {
                // re-apply each edit in their original order
                for (Edit edit : edits)
                    edit.redo();
            } finally {
                txSource.commitEvent();
            }
        }
    }

    /**
     * A base class implementing common logic and storage for the specific kind
     * of Edits which can occur to a single index in the EventList.
     */
    private abstract class AbstractSimpleEdit<E> extends AbstractEdit {

        protected final EventList<E> source;
        protected final int index;
        protected final E value;

        protected AbstractSimpleEdit(EventList<E> source, int index, E value) {
            this.source = source;
            this.index = index;
            this.value = value;
        }
    }

    /**
     * A class describing an undoable Add to an EventList.
     */
    private final class AddEdit<E> extends AbstractSimpleEdit<E> {
        public AddEdit(EventList<E> source, int index, E value) {
            super(source, index, value);
        }

        @Override
        public void undoImpl() { source.remove(index); }
        @Override
        public void redoImpl() { source.add(index, value); }
    }

    /**
     * A class describing an undoable Remove to an EventList.
     */
    private final class RemoveEdit<E> extends AbstractSimpleEdit<E> {
        public RemoveEdit(EventList<E> source, int index, E value) {
            super(source, index, value);
        }

        @Override
        public void undoImpl() { source.add(index, value); }
        @Override
        public void redoImpl() { source.remove(index); }
    }

    /**
     * A class describing an undoable Update to an EventList.
     */
    private final class UpdateEdit<E> extends AbstractSimpleEdit<E> {
        private final E oldValue;

        public UpdateEdit(EventList<E> source, int index, E value, E oldValue) {
            super(source, index, value);
            this.oldValue = oldValue;
        }

        @Override
        public void undoImpl() { source.set(index, oldValue); }
        @Override
        public void redoImpl() { source.set(index, value); }
    }
}