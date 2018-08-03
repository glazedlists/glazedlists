package ca.odell.glazedlists;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;

public class TransactionEventAssembler<E> {
  /**
   * produces {@link UndoRedoSupport.Edit}s which are collected during a transaction to support rollback
   */
  private UndoRedoSupport<E> rollbackSupport;

  /**
   * The current tree of transactions contexts; for each layer of nested transaction
   */
  private IContext contextLevel;

  /**
   * Current level
   **/
  private int nestedLevel = -1;

  private final ListEventAssembler<E> bufferedUpdates;

  public TransactionEventAssembler(boolean rollbackSupport) {
    // if rollback support is requested, build the necessary infrastructure
    if (rollbackSupport) {
      this.rollbackSupport = UndoRedoSupport.install(this);
      this.rollbackSupport.addUndoSupportListener(new RollbackSupportListener());
    }
    this.bufferedUpdates = new ListEventAssembler<>(null, ListEventAssembler.createListEventPublisher());
  }

  public ListEventAssembler<E> getBufferedUpdates() {
    return bufferedUpdates;
  }

  /**
   * Demarks the beginning of a transaction which accumulates all ListEvents received during the transaction and fires a single aggregate ListEvent on
   * {@link #commitEvent()}.
   */
  public void beginEvent() {
    beginEvent(true);
  }

  /**
   * Demarks the beginning of a transaction. If <code>buffered</code> is <tt>true</tt> then all ListEvents received during the transaction are accumulated and
   * fired as a single aggregate ListEvent on {@link #commitEvent()}. If <code>buffered</code> is <tt>false</tt> then all ListEvents received during the
   * transaction are forwarded immediately and {@link #commitEvent()} produces no ListEvent of its own.
   *
   * @param buffered
   *          <tt>true</tt> indicates ListEvents should be buffered and sent on {@link #commitEvent()}; <tt>false</tt> indicates they should be sent on
   *          immediately
   */
  public void beginEvent(boolean buffered) {
    // start a nestable ListEvent if we're supposed to buffer them
    if (buffered)
      bufferedUpdates.beginEvent(true);

    // push a new context onto the stack describing this new transaction
    contextLevel = new Context(buffered, contextLevel);
    nestedLevel++;
  }

  /**
   * Demarks the successful completion of a transaction. If changes were buffered during the transaction by calling {@link #beginEvent(boolean)
   * beginEvent(true)} then a single ListEvent will be fired from this TransactionList describing the changes accumulated during the transaction.
   */
  public void commitEvent() {
    // verify that there is a transaction to commit
    if (contextLevel == null)
      throw new IllegalStateException("No ListEvent exists to commit");

    // get the last context off the stack and ask it to commit
    if (this.contextLevel.isEventStarted()) {
      bufferedUpdates.commitEvent();
    }

    this.contextLevel = this.contextLevel.getParent();
    nestedLevel--;
  }

  /**
   * Demarks the unsuccessful completion of a transaction. If changes were NOT buffered during the transaction by calling {@link #beginEvent(boolean)
   * beginEvent(false)} then a single ListEvent will be fired from this TransactionList describing the rollback of the changes accumulated during the
   * transaction.
   */
  public void rollbackEvent() {
    // check if this TransactionList was created with rollback abilities
    if (rollbackSupport == null)
      throw new IllegalStateException("This TransactionList does not support rollback");

    // check if a transaction exists to rollback
    if (contextLevel == null)
      throw new IllegalStateException("No ListEvent exists to roll back");

    // rollback all changes from the transaction as a single ListEvent
    bufferedUpdates.beginEvent(true);
    try {
      this.contextLevel.undo();
    } finally {
      bufferedUpdates.commitEvent();
    }
    if (this.contextLevel.isEventStarted())
      bufferedUpdates.discardEvent();

    this.contextLevel = this.contextLevel.getParent();
    nestedLevel--;
  }

  public void dispose() {
    if (rollbackSupport != null) {
      rollbackSupport.uninstall();
      rollbackSupport = null;
    }
    if (contextLevel != null) {
      contextLevel.clear();
    }
  }

  public void addListEventListener(ListEventListener<? super E> listChangeListener) {
    this.bufferedUpdates.addListEventListener(listChangeListener);
  }

  public void removeListEventListener(ListEventListener<? super E> listChangeListener) {
    this.bufferedUpdates.removeListEventListener(listChangeListener);
  }

  /**
   * Accumulates all of the small Edits that occur during a transaction within a CompositeEdit that can be undone to support rollback, if necessary.
   */
  private class RollbackSupportListener implements UndoRedoSupport.Listener {
    public void undoableEditHappened(UndoRedoSupport.Edit edit) {
      boolean noContext = nestedLevel < 0;
      if (noContext) {
        bufferedUpdates.beginEvent();
      }
      // if a tx context exists we are in the middle of a transaction
      contextLevel.add(edit);
      if (noContext) {
        bufferedUpdates.commitEvent();
      }
    }
  }

  private static interface IContext extends UndoRedoSupport.Edit {
    public boolean isEventStarted();

    public void add(UndoRedoSupport.Edit edit);

    public void clear();

    public IContext getParent();
  }

  /**
   * A small object describing the details about the transaction that was started so that it can be properly committed or rolled back at a later time.
   * Specifically it tracks:
   *
   * <ul>
   * <li>a CompositeEdit which can be used to undo the transaction's changes in the case of a rollback</li>
   * <li>a flag indicating wether a ListEvent was started when the transaction began (and thus must be committed or discarded later)
   * </ul>
   */
  private final class Context implements IContext {
    /**
     * collects the smaller intermediate Edits that occur during a transaction; <code>null</code> if no transaction exists
     */
    private UndoRedoSupport<E>.CompositeEdit rollbackEdit = rollbackSupport == null ? null : rollbackSupport.new CompositeEdit();
    private IContext parent;

    /**
     * <tt>true</tt> indicates a ListEvent was started when this Context was created and must be committed or rolled back later.
     */
    private boolean eventStarted = false;

    public Context(boolean eventStarted, IContext parent) {
      this.eventStarted = eventStarted;
      this.parent = parent;
      if (this.parent != null) {
        this.parent.add(this);
      }
    }

    /**
     * Add the given edit into this Context to support its possible rollback.
     */
    public void add(UndoRedoSupport.Edit edit) {
      if (rollbackEdit != null) {
        rollbackEdit.add(edit);
      }
    }

    @Override
    public boolean isEventStarted() {
      return this.eventStarted;
    }

    @Override
    public void undo() {
      if (rollbackEdit != null) {
        rollbackEdit.undo();
      }
    }

    @Override
    public boolean canUndo() {
      if (rollbackEdit != null) {
        return rollbackEdit.canUndo();
      }
      return false;
    }

    @Override
    public void redo() {
      if (rollbackEdit != null) {
        rollbackEdit.redo();
      }
    }

    @Override
    public boolean canRedo() {
      if (rollbackEdit != null) {
        return rollbackEdit.canRedo();
      }
      return false;
    }

    @Override
    public void clear() {
      rollbackEdit = rollbackSupport == null ? null : rollbackSupport.new CompositeEdit();
    }

    @Override
    public IContext getParent() {
      return parent;
    }

    @Override
    public String toString() {
      return rollbackEdit == null ? "null" : rollbackEdit.toString();
    }
  }

  /**
   * UndoRedoSupport, as the name suggests, will provide generic support for undoing and redoing groups of changes to an {@link EventList}. The granularity of
   * each undoable edit is determined by the ListEvent from which it was generated.
   */
  public static final class UndoRedoSupport<E> {

    /** A wrapper around the true source EventList provides control over the granularity of ListEvents it produces. */
    private TransactionEventAssembler<E> txSource;

    /** A ListEventListener that watches the {@link #txSource} and in turn broadcasts an {@link Edit} object to all {@link Listener}s */
    private ListEventListener<E> txSourceListener = new TXSourceListener();

    /** A data structure storing all registered {@link Listener}s. */
    private final CopyOnWriteArrayList<Listener> listenerList = new CopyOnWriteArrayList<>();

    /**
     * A count which, when greater than 0, indicates a ListEvent must be ignored by this UndoRedoSupport because it was caused by an undo or redo. An int is
     * used rather than a boolean flag so that nested undos/redos are properly handled.
     */
    private int ignoreListEvent = 0;

    private UndoRedoSupport(TransactionEventAssembler<E> source) {
      // build a TransactionList that does NOT support rollback - we don't
      // need it and it relies on UndoRedoSupport, so we would have
      this.txSource = Objects.requireNonNull(source);
      this.txSource.addListEventListener(txSourceListener);
    }

    /**
     * Add a {@link Listener} which will receive a callback when an undoable edit occurs on the given source {@link EventList}.
     */
    public void addUndoSupportListener(Listener l) {
      if (l != null) {
        listenerList.add(l);
      }
    }

    /**
     * Remove a {@link Listener} from receiving a callback when an undoable edit occurs on the given source {@link EventList}.
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
      @SuppressWarnings("unchecked")
      List<Listener> listenerListCopy = (List<Listener>) listenerList.clone();
      ListIterator<Listener> li = listenerListCopy.listIterator(listenerListCopy.size());
      while (li.hasPrevious()) {
        li.previous().undoableEditHappened(edit);
      }
    }

    /**
     * Installs support for undoing and redoing changes to the given <code>source</code>. To be notified of undoable changes, a {@link Listener} must be
     * registered on the object that is returned by this method. That Listener object will typically add the {@link Edit} it is given over to whatever data
     * structure is managing all undo/redo functions for the entire application.
     *
     * @param source
     *          the EventList on which to provide undo/redo capabilities
     * @return an instance of UndoRedoSupport through which the undo/redo behaviour can be customized
     */
    public static <E> UndoRedoSupport<E> install(TransactionEventAssembler<E> source) {
      return new UndoRedoSupport<>(source);
    }

    /**
     * This method removes undo/redo support from the {@link EventList} it was installed on. This method is useful when the {@link EventList} must outlive the
     * UndoRedoSupport object itself. The UndoRedoSupport object will become available for garbage collection independently of the {@link EventList} it
     * decorated with behaviour.
     */
    public void uninstall() {
      txSource.removeListEventListener(txSourceListener);
      txSource = null;
    }

    /**
     * This Listener watches the TransactionList for changes and responds by created an {@link Edit} and broadcasting that object to all registered
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
            final E inserted = listChanges.getNewValue();
            edit.add(new AddEdit(txSource.getBufferedUpdates(), changeIndex, inserted));

            // provide a RemoveEdit to the CompositeEdit
          } else if (changeType == ListEvent.DELETE) {
            // try to get the previous value through the ListEvent
            E deleted = listChanges.getOldValue();
            edit.add(new RemoveEdit(txSource.getBufferedUpdates(), changeIndex, deleted));
            // provide an UpdateEdit to the CompositeEdit
          } else if (changeType == ListEvent.UPDATE) {
            final E previousValue = listChanges.getOldValue();
            final E newValue = listChanges.getNewValue();

            // if a different object is present at the index
            if (newValue != previousValue) {
              edit.add(new UpdateEdit(txSource.getBufferedUpdates(), changeIndex, newValue, previousValue));
            }
          }
        }

        // if the edit has real contents, broadcast it
        if (!edit.isEmpty())
          fireUndoableEditHappened(edit.getSimplestEdit());
      }
    }

    /**
     * Implementations of this Listener interface should be registered with an UndoRedoSupport object via {@link UndoRedoSupport#addUndoSupportListener}. They
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
     * Provides an easy interface to undo/redo a ListEvent in its entirety. At any point in time it is only possible to do one, and only one, of {@link #undo}
     * and {@link #redo}. To determine which one is allowed, use {@link #canUndo()} and {@link #canRedo()}.
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
      public final boolean canUndo() {
        return canUndo;
      }

      @Override
      public final boolean canRedo() {
        return !canUndo;
      }
    }

    /**
     * An Edit which acts as a container for finer-grained Edit objects.
     */
    final class CompositeEdit extends AbstractEdit {

      /** The edits in the order they were made. */
      private final List<Edit> edits = new ArrayList<>();

      /** Adds a single Edit to this container of Edits. */
      void add(Edit edit) {
        edits.add(edit);
      }

      /** Returns <tt>true</tt> if this container of Edits is empty; <tt>false</tt> otherwise. */
      private boolean isEmpty() {
        return edits.isEmpty();
      }

      /** Returns the single Edit contained within this composite, if only one exists, otherwise it returns this entire CompositeEdit. */
      private Edit getSimplestEdit() {
        return edits.size() == 1 ? edits.get(0) : this;
      }

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
     * A base class implementing common logic and storage for the specific kind of Edits which can occur to a single index in the EventList.
     */
    private abstract class AbstractSimpleEdit extends AbstractEdit {

      protected final ListEventAssembler<E> source;
      protected final int index;
      protected final E value;

      protected AbstractSimpleEdit(ListEventAssembler<E> source, int index, E value) {
        this.source = source;
        this.index = index;
        this.value = value;
      }
    }

    /**
     * A class describing an undoable Add to an EventList.
     */
    private final class AddEdit extends AbstractSimpleEdit {
      public AddEdit(ListEventAssembler<E> source, int index, E value) {
        super(source, index, value);
      }

      @Override
      public void undoImpl() {
        this.source.elementDeleted(this.index, this.value);
      }

      @Override
      public void redoImpl() {
        this.source.elementInserted(this.index, this.value);
      }
    }

    /**
     * A class describing an undoable Remove to an EventList.
     */
    private final class RemoveEdit extends AbstractSimpleEdit {
      public RemoveEdit(ListEventAssembler<E> source, int index, E value) {
        super(source, index, value);
      }

      @Override
      public void undoImpl() {
        this.source.elementInserted(this.index, this.value);
      }

      @Override
      public void redoImpl() {
        this.source.elementDeleted(this.index, this.value);
      }
    }

    /**
     * A class describing an undoable Update to an EventList.
     */
    private final class UpdateEdit extends AbstractSimpleEdit {
      private final E oldValue;

      public UpdateEdit(ListEventAssembler<E> source, int index, E value, E oldValue) {
        super(source, index, value);
        this.oldValue = oldValue;
      }

      @Override
      public void undoImpl() {
        this.source.elementUpdated(this.index, this.value, this.oldValue);
      }

      @Override
      public void redoImpl() {
        this.source.elementUpdated(this.index, this.oldValue, this.value);
      }
    }
  }
}
