/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

/**
 * A list transformation that presents traditional transaction semantics.
 * Typical usage resembles one of two methods:
 *
 * <pre>
 *   EventList source = ...
 *   TransactionList txList = new TransactionList(source);
 *
 *   // begin a transaction in which all ListEvents are collected by txList
 *   // into a single "super ListEvent", which is fired on commit
 *   txList.beginEvent(true);
 *
 *   // fill in the details of the transaction
 *   // (these operations actually physically change ONLY txList and its source)
 *   txList.add("A new element");
 *   txList.set(0, "A changed element");
 *   txList.remove(6);
 *
 *   // commit the transaction, which will broadcast a single ListEvent from
 *   // txList describing the aggregate of all changes made during the transaction
 *   // (this returns the entire list pipeline to a consistent state)
 *   txList.commitEvent();
 * </pre>
 *
 * In this usage, all ListEventListeners "downstream" of TransactionList remain
 * clueless about changes made during a transaction. As a result, the
 * "list pipeline" is allowed to temporarily fall into an inconsistent state
 * because only a portion of the pipeline (TransactionList and lower) has seen
 * changes made during the transaction. Users must ensure that they do not
 * read or write through any "downstream" EventList that depends on the
 * TransactionList during a transaction. Typically this is done using the
 * built-in {@link #getReadWriteLock() locks}.
 *
 * <p>If the transaction was rolled back instead of committed, the txList would
 * not produce a ListEvent, since none of its listeners would be aware of any
 * changes made during the transaction.
 * 
 * The second popular usage resembles this:
 *
 * <pre>
 *   EventList source = ...
 *   TransactionList txList = new TransactionList(source);
 *
 *   // begin a transaction in which we change the ListEvent
 *   txList.beginEvent(); // this is the same as txList.beginEvent(false);
 *
 *   // fill in the details of the transaction
 *   // (these operations actually physically change the ENTIRE PIPELINE)
 *   txList.add("A new element");
 *   txList.set(0, "A changed element");
 *   txList.remove(6);
 *
 *   // commit the transaction, which will NOT broadcast a ListEvent from
 *   // txList because all of its listeners are already aware of the changes
 *   // made during the transaction
 *   txList.commitEvent();
 * </pre>
 *
 * In this case, the "list pipeline" always remains consistent and reads/writes
 * may occur through any part EventList in the pipeline without error.
 *
 * <p>If the transaction is rolled back instead of committed, the txList
 * produces a ListEvent describing the rollback, since its listeners are fully
 * aware of the changes made during the transaction and must also be given a
 * chance to undo their changes.
 *
 * @author James Lemieux
 */
public class TransactionList<E> extends TransformedList<E, E> {

    /** produces {@link UndoRedoSupport.Edit}s which are collected during a transaction to support rollback */
    private UndoRedoSupport rollbackSupport;

    /** collects the smaller intermediate Edits that occur during a transaction; <code>null</code> if no transaction exists */
    private UndoRedoSupport.CompositeEdit rollbackEdit;

    /**
     * <tt>true</tt> indicates a ListEvent was started when {@link #beginEvent(boolean)}
     * was called and must be committed on either {@link #commitEvent()} or {@link #rollbackEvent()}.
     */
    private boolean eventStarted = false;

    /**
     * Constructs a <code>TransactionList</code> that provides traditional
     * transaction semantics over the given <code>source</code>.
     *
     * @param source the EventList over which to provide a transactional view
     */
    public TransactionList(EventList<E> source) {
        this(source, true);
    }

    /**
     * Constructs a <code>TransactionList</code> that provides traditional
     * transaction semantics over the given <code>source</code>.
     *
     * <p>If <code>rollbackSupport</code> is <tt>true</tt> then this
     * TransactionList supports calling {@link #rollbackEvent()} during a
     * transaction. This constructor exists solely to break the constructor
     * cycle between UndoRedoSupport and TransactionList and should only be
     * used internally by Glazed Lists.
     *
     * @param source the EventList over which to provide a transactional view
     */
    TransactionList(EventList<E> source, boolean rollbackSupport) {
        super(source);

        // if rollback support is requested, build the necessary infrastructure
        if (rollbackSupport) {
            this.rollbackSupport = UndoRedoSupport.install(source);
            this.rollbackSupport.addUndoSupportListener(new RollbackSupportListener());
        }

        source.addListEventListener(this);
    }

    /**
     * Demarks the beginning of a transaction which accumulates all ListEvents
     * received during the transaction and fires a single aggregate ListEvent
     * on {@link #commitEvent()}.
     */
    public void beginEvent() {
        beginEvent(true);
    }

    /**
     * Demarks the beginning of a transaction. If <code>buffered</code> is
     * <tt>true</tt> then all ListEvents received during the transaction are
     * accumulated and fired as a single aggregate ListEvent on
     * {@link #commitEvent()}. If <code>buffered</code> is <tt>false</tt> then
     * all ListEvents received during the transaction are forwarded immediately
     * and {@link #commitEvent()} produces no ListEvent of its own.
     *
     * @param buffered <tt>true</tt> indicates ListEvents should be buffered and
     *      sent on {@link #commitEvent()}; <tt>false</tt> indicates they should
     *      be sent on immediately
     */
    public void beginEvent(boolean buffered) {
        // we don't allow nested transactions, so check if one is in progress
        if (rollbackSupport != null && rollbackEdit != null)
            throw new IllegalStateException("Unable to begin a new transaction before committing or rolling back the previous transaction");

        // start a nestable ListEvent if we're supposed to buffer them
        if (buffered)
            updates.beginEvent(true);

        // record whether a ListEvent was started
        this.eventStarted = buffered;

        // build a new CompositeEdit to accumulate the smaller Edits which
        // occur during the transaction (this allows us to support rollback)
        rollbackEdit = rollbackSupport == null ? null : rollbackSupport.new CompositeEdit();
    }

    /**
     * Demarks the successful completion of a transaction. If changes were
     * buffered during the transaction by calling {@link #beginEvent(true)}
     * then a single ListEvent will be fired from this TransactionList
     * describing the changes accumulated during the transaction.
     */
    public void commitEvent() {
        // verify that there is a transaction to roll back
        if (rollbackSupport != null && rollbackEdit == null)
            throw new IllegalStateException("No ListEvent exists to commit");

        rollbackEdit = null;

        // fire the summary ListEvent if necessary
        if (eventStarted)
            updates.commitEvent();
    }

    /**
     * Demarks the unsuccessful completion of a transaction. If changes were
     * NOT buffered during the transaction by calling {@link #beginEvent(false)}
     * then a single ListEvent will be fired from this TransactionList
     * describing the rollback of the changes accumulated during the transaction.
     */
    public void rollbackEvent() {
        // check if this TransactionList was created with rollback abilities
        if (rollbackSupport == null)
            throw new IllegalStateException("This TransactionList does not support rollback");

        // check if a transaction exists to rollback
        if (rollbackEdit == null)
            throw new IllegalStateException("No ListEvent exists to roll back");

        // undo all of the transaction's changes as a single ListEvent
        updates.beginEvent(true);
        try {
            rollbackEdit.undo();
        } finally {
            updates.commitEvent();
        }
        rollbackEdit = null;

        // throw away the ListEvent if we started one in beginEvent()
        if (eventStarted)
            updates.discardEvent();
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** @inheritDoc */
    public void dispose() {
        if (rollbackSupport != null)
            rollbackSupport.uninstall();
        rollbackSupport = null;
        rollbackEdit = null;

        super.dispose();
    }

    /**
     * Simply forwards all of the <code>listChanges</code> since TransactionList
     * doesn't transform the source data in any way.
     */
    public void listChanged(ListEvent<E> listChanges) {
        updates.forwardEvent(listChanges);
    }

    /**
     * Accumulates all of the small Edits that occur during a transaction
     * within a CompositeEdit that can be undone to support rollback, if
     * necessary.
     */
    private class RollbackSupportListener implements UndoRedoSupport.Listener {
        public void undoableEditHappened(UndoRedoSupport.Edit edit) {
            // if rollbackEdit is not null we are in the middle of a transaction
            if (rollbackEdit != null)
                rollbackEdit.add(edit);
        }
    }
}