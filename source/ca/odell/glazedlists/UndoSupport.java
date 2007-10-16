package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import javax.swing.event.EventListenerList;
import java.util.*;

/**
 * UndoSupport, as the name suggests, will provide generic support for undoing
 * and redoing groups of changes to an {@link EventList}. The granularity of
 * each undoable edit is determined by the ListEvent that describes the
 * arbitrary changes made to the EventList.
 *
 * <p>Not every change described in a ListEvent results in an undoable edit.
 * Specifically, a <strong>mutation</strong> of a list element IN PLACE does
 * not produce an undoable edit. For example, an {@link ObservableElementList}
 * which observes a change of an element, or a call to {@link List#set} with
 * the same object at that index produce a ListEvent that does not have a
 * corresponding {@link Edit} object. These ListEvent are ignored because they
 * lack sufficient information to undo or redo the change.
 *
 * <p>In general UndoSupport only makes sense for use with a
 * {@link BasicEventList} or a trivial wrapper around a BasicEventList which
 * does not affect the order or type of the elements, such as an
 * {@link ObservableElementList}. Advanced transformations, such as
 * {@link SortedList} or {@link FilterList} will not work as expected with this
 * UndoSupport class since their contents are controlled by information outside
 * of themselves ({@link Comparator}s and {@link ca.odell.glazedlists.matchers.Matcher}s).
 *
 * <p>This class is agnostic to any particular GUI toolkit. As such it may be
 * used in a headless environment or can also be bound to a specific toolkit.
 *
 * @author James Lemieux
 */
public final class UndoSupport<E> {

    /** A wrapper around the true source EventList provides control over the granularity of ListEvents it produces. */
    private NestableEventsList<E> nestableSource;

    /** A ListEventListener that watches the {@link #nestableSource} and in turn broadcasts an {@link Edit} object to all {@link Listener}s */
    private ListEventListener<E> nestableSourceListener = new NestableSourceListener();

    /** A data structure storing all registered {@link Listener}s. */
    private final EventListenerList listenerList = new EventListenerList();

    /** A flag which indicates a ListEvent should be ignored because it was caused by an undo or redo. */
    private boolean ignoreListEvent = false;

    /**
     * A list of the elements in the <code>source</code> prior to the most
     * recent change. It is necessary to track the prior elements since
     * list removals broadcast ListEvents which do not include the removed
     * element as part of the ListEvent. We use this list to locate
     * removed elements when constructing objects that can undo the change.
     * todo remove this list when ListEvent can reliably furnish us with a deleted value
     */
    private List<E> priorElements;

    private UndoSupport(EventList<E> source) {
        this.nestableSource = new NestableEventsList<E>(source, true);
        this.nestableSource.addListEventListener(nestableSourceListener);

        this.priorElements = new ArrayList<E>(source);
    }

    /**
     * Add a {@link Listener} which will receive a callback when an undoable
     * edit occurs on the given source {@link EventList}.
     */
    public void addUndoSupportListener(Listener l) {
	    listenerList.add(Listener.class, l);
    }

    /**
     * Remove a {@link Listener} from receiving a callback when an undoable
     * edit occurs on the given source {@link EventList}.
     */
    public void removeUndoSupportListener(Listener l) {
	    listenerList.remove(Listener.class, l);
    }

    /**
     * Notifies all registered {@link Listener}s of the given <code>edit</code>.
     */
    private void fireUndoableEditHappened(Edit edit) {
        Object[] listeners = listenerList.getListenerList();

        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]== Listener.class)
                ((Listener) listeners[i+1]).undoableEditHappened(edit);
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
     * @return an instance of UndoSupport through which the undo/redo behaviour
     *      can be customized
     */
    public static <E> UndoSupport install(EventList<E> source) {
        return new UndoSupport<E>(source);
    }

    /**
     * This method removes undo/redo support from the {@link EventList} it was
     * installed on. This method is useful when the {@link EventList} must
     * outlive the UndoSupport object itself. The UndoSupport object will become
     * available for garbage collection independently of the {@link EventList}
     * it decorated with behaviour.
     */
    public void uninstall() {
        nestableSource.dispose();
        nestableSource.removeListEventListener(nestableSourceListener);

        nestableSource = null;
        priorElements = null;
    }

    /**
     * This Listener watches the NestableEventsList for changes and responds by
     * created an {@link Edit} and broadcasting that object to all registered
     * {@link Listener}s.
     */
    private class NestableSourceListener implements ListEventListener<E> {
        public void listChanged(ListEvent<E> listChanges) {
            // if an undo or redo caused this ListEvent, it is not an undoable edit
            if (ignoreListEvent)
                return;

            // build a CompositeEdit that describes the ListEvent and provides methods for undoing and redoing it
            final CompositeEdit edit = new CompositeEdit();

            while (listChanges.next()) {
                final int changeIndex = listChanges.getIndex();
                final int changeType = listChanges.getType();

                // provide an AddEdit to the CompositeEdit
                if (changeType == ListEvent.INSERT) {
                    final E inserted = nestableSource.get(changeIndex);
                    priorElements.add(changeIndex, inserted);
                    edit.add(new AddEdit<E>(nestableSource, changeIndex, inserted));

                // provide a RemoveEdit to the CompositeEdit
                } else if (changeType == ListEvent.DELETE) {
                    // try to get the previous value through the ListEvent
                    E deleted = listChanges.getOldValue();
                    E deletedElementFromPrivateCopy = priorElements.remove(changeIndex);

                    // if the ListEvent could not give us the previous value, use the value from priorElements
                    if (deleted == ListEvent.UNKNOWN_VALUE)
                        deleted = deletedElementFromPrivateCopy;

                    edit.add(new RemoveEdit<E>(nestableSource, changeIndex, deleted));

                // provide an UpdateEdit to the CompositeEdit
                } else if (changeType == ListEvent.UPDATE) {
                    E previousValue = listChanges.getOldValue();

                    // if the ListEvent could not give us the previous value, use the value from priorElements
                    if (previousValue == ListEvent.UNKNOWN_VALUE)
                        previousValue = priorElements.get(changeIndex);

                    final E newValue = nestableSource.get(changeIndex);

                    // if a different object is present at the index
                    if (newValue != previousValue) {
                        priorElements.set(changeIndex, newValue);
                        edit.add(new UpdateEdit<E>(nestableSource, changeIndex, newValue, previousValue));
                    }
                }
            }

            // if the edit has real contents, broadcast it
            if (!edit.isEmpty())
                fireUndoableEditHappened(edit);
        }
    }

    /**
     * Implementations of this Listener interface should be registered with an
     * UndoSupport object via {@link UndoSupport#addUndoSupportListener}. They
     * will be notified of each undoable edit that occurs to the given EventList.
     */
    public interface Listener extends EventListener {
        /**
         * Notified of each undoable edit applied to the given EventList.
         */
        public void undoableEditHappened(Edit edit);
    }

    /**
     * Provides an easy interface to undo/redo a ListEvent in its entirety.
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

    /**
     * An Edit which acts as a container for finer-grained Edit objects.
     */
    private final class CompositeEdit implements Edit {

        /** The edits in the order they were made. */
        private final List<Edit> edits = new ArrayList<Edit>();

        /** Initially the Edit can be undone but not redone. */
        private boolean canUndo = true;

        /** Adds a single Edit to this container of Edits. */
        private void add(Edit edit) { edits.add(edit); }

        /** Returns <tt>true</tt> if this container of Edits is empty; <tt>false</tt> otherwise. */
        private boolean isEmpty() { return edits.isEmpty(); }

        public void undo() {
            if (!canUndo())
                throw new IllegalStateException("The Edit is in an incorrect state for undoing");

            ignoreListEvent = true;
            nestableSource.beginEvent(true);
            try {
                // undo the Edits in reverse order they were applied
                for (ListIterator<Edit> i = edits.listIterator(edits.size()); i.hasPrevious();)
                    i.previous().undo();
            } finally {
                nestableSource.commitEvent();
                ignoreListEvent = false;
            }

            canUndo = false;
        }

        public void redo() {
            if (!canRedo())
                throw new IllegalStateException("The Edit is in an incorrect state for redoing");

            ignoreListEvent = true;
            nestableSource.beginEvent(true);
            try {
                for (Iterator<Edit> i = edits.iterator(); i.hasNext();)
                    i.next().redo();
            } finally {
                nestableSource.commitEvent();
                ignoreListEvent = false;
            }

            canUndo = true;
        }

        public final boolean canUndo() { return canUndo; }
        public final boolean canRedo() { return !canUndo; }
    }

    /**
     * A base class implementing common logic and storage for the specific kind
     * of Edits which can occur to a single index in the EventList.
     */
    private static abstract class AbstractEdit<E> implements Edit {

        protected final EventList<E> source;
        protected final int index;
        protected final E value;

        protected AbstractEdit(EventList<E> source, int index, E value) {
            this.source = source;
            this.index = index;
            this.value = value;
        }

        public final boolean canUndo() { return true; }
        public final boolean canRedo() { return true; }
    }

    /**
     * A class describing an undoable Add to an EventList.
     */
    private static final class AddEdit<E> extends AbstractEdit<E> {
        public AddEdit(EventList<E> source, int index, E value) {
            super(source, index, value);
        }

        public void undo() { source.remove(index); }
        public void redo() { source.add(index, value); }
    }

    /**
     * A class describing an undoable Remove to an EventList.
     */
    private static final class RemoveEdit<E> extends AbstractEdit<E> {
        public RemoveEdit(EventList<E> source, int index, E value) {
            super(source, index, value);
        }

        public void undo() { source.add(index, value); }
        public void redo() { source.remove(index); }
    }

    /**
     * A class describing an undoable Update to an EventList.
     */
    private static final class UpdateEdit<E> extends AbstractEdit<E> {
        private final E oldValue;

        public UpdateEdit(EventList<E> source, int index, E value, E oldValue) {
            super(source, index, value);
            this.oldValue = oldValue;
        }

        public void undo() { source.set(index, oldValue); }
        public void redo() { source.set(index, value); }
    }
}