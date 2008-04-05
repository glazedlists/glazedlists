/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.List;
import java.util.ArrayList;

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
 * <p>Transactions may be nested arbitrarily deep using code that resembles:
 * <pre>
 *   txList.beginEvent();
 *     txList.add("A");
 *
 *     txList.beginEvent();
 *       txList.set(0, "B");
 *     txList.commitEvent();
 *
 *     txList.beginEvent();
 *       txList.add("C");
 *     txList.commitEvent();
 *   txList.commitEvent();
 * </pre>
 *
 * @author James Lemieux
 */
public class TransactionList<E> extends TransformedList<E, E> {

    /** produces {@link UndoRedoSupport.Edit}s which are collected during a transaction to support rollback */
    private UndoRedoSupport rollbackSupport;

    /** A stack of transactions contexts; one for each layer of nested transaction */
    private final List<Context> txContextStack = new ArrayList<Context>();

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
     * @param rollbackSupport <tt>true</tt> indicates this TransactionList must
     *      support the rollback ability; <tt>false</tt> indicates it is not
     *      necessary
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
        // start a nestable ListEvent if we're supposed to buffer them
        if (buffered)
            updates.beginEvent(true);

        // push a new context onto the stack describing this new transaction
        txContextStack.add(new Context(buffered));
    }

    /**
     * Demarks the successful completion of a transaction. If changes were
     * buffered during the transaction by calling {@link #beginEvent(boolean) beginEvent(true)}
     * then a single ListEvent will be fired from this TransactionList
     * describing the changes accumulated during the transaction.
     */
    public void commitEvent() {
        // verify that there is a transaction to roll back
        if (rollbackSupport != null && txContextStack.isEmpty())
            throw new IllegalStateException("No ListEvent exists to commit");

        // pop the last context off the stack and ask it to commit
        txContextStack.remove(txContextStack.size()-1).commit();
    }

    /**
     * Demarks the unsuccessful completion of a transaction. If changes were
     * NOT buffered during the transaction by calling {@link #beginEvent(boolean) beginEvent(false)}
     * then a single ListEvent will be fired from this TransactionList
     * describing the rollback of the changes accumulated during the transaction.
     */
    public void rollbackEvent() {
        // check if this TransactionList was created with rollback abilities
        if (rollbackSupport == null)
            throw new IllegalStateException("This TransactionList does not support rollback");

        // check if a transaction exists to rollback
        if (txContextStack.isEmpty())
            throw new IllegalStateException("No ListEvent exists to roll back");

        // pop the last context off the stack and ask it to rollback
        txContextStack.remove(txContextStack.size()-1).rollback();
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
        txContextStack.clear();

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
            // if a tx context exists we are in the middle of a transaction
            if (!txContextStack.isEmpty())
                txContextStack.get(txContextStack.size()-1).add(edit);
        }
    }

    /**
     * A small object describing the details about the transaction that was
     * started so that it can be properly committed or rolled back at a later
     * time. Specifically it tracks:
     *
     * <ul>
     *   <li>a CompositeEdit which can be used to undo the transaction's changes
     *       in the case of a rollback</li>
     *   <li>a flag indicating wether a ListEvent was started when the
     *       transaction began (and thus must be committed or discarded later)
     * </ul>
     */
    private final class Context {
        /** collects the smaller intermediate Edits that occur during a transaction; <code>null</code> if no transaction exists */
        private UndoRedoSupport.CompositeEdit rollbackEdit = rollbackSupport == null ? null : rollbackSupport.new CompositeEdit();

        /**
         * <tt>true</tt> indicates a ListEvent was started when this Context
         * was created and must be committed or rolled back later.
         */
        private boolean eventStarted = false;

        public Context(boolean eventStarted) {
            this.eventStarted = eventStarted;
        }

        /**
         * Add the given edit into this Context to support its possible rollback.
         */
        public void add(UndoRedoSupport.Edit edit) {
            if (rollbackEdit != null)
                rollbackEdit.add(edit);
        }

        /**
         * Commit the changes associated with this transaction.
         */
        public void commit() {
            rollbackEdit = null;

            if (eventStarted)
                updates.commitEvent();
        }

        /**
         * Rollback the changes associated with this transaction.
         */
        public void rollback() {
            if (rollbackEdit != null) {
                // rollback all changes from the transaction as a single ListEvent
                updates.beginEvent(true);
                try {
                    rollbackEdit.undo();
                } finally {
                    updates.commitEvent();
                }

                rollbackEdit = null;
            }

            // throw away the ListEvent if we started one
            if (eventStarted)
                updates.discardEvent();
        }
    }
}