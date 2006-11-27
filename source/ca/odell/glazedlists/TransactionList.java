/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.event.Tree4Deltas;

/**
 * A TransactionList provides functionality to another EventList that is very
 * similar to traditional database transactions. A batch of changes can be
 * proposed against the source EventList and either {@link #commit committed}
 * or {@link #rollback rolled back} at some point in the future. For example,
 * an application could present the user with a dialog that displays a
 * TransactionList sitting on top of their source EventList. The user is
 * allowed to make arbitrary modifications to the TransactionList within the
 * dialog and commit those changes to the source EventList by closing the
 * dialog with the "Ok" button, or discard the changes by closing the dialog
 * with the "Cancel" button.
 *
 * <p>Transactions are started using {@link #begin()} and a maximum of one
 * transaction can exist in this TransactionList at any time. Consequently an
 * {@link IllegalStateException} is thrown if multiple calls to {@link #begin()}
 * occur before {@link #commit()} or {@link #rollback()} is called.
 *
 * <p>After beginning a transaction, changes are accumulated by calling the
 * normal mutation methods on the TransactionList such as {@link #add},
 * {@link #remove} and {@link #set}. During this time TransactionList will
 * begin to diverge from the source EventList. TransactionList will broadcast
 * ListEvents describing each intermediate state during the transaction so any
 * EventLists stacked on top of TransactionList will be see the changes as they
 * are made but the EventLists below the TransactionList will see none of the
 * changes yet.
 *
 * <p>After all changes have been made, the end of the transaction must be
 * signalled by calling either {@link #commit()} if the transaction's batch of
 * changes should be integrated with the source EventList or
 * {@link #rollback()} if they should be discarded.
 *
 * <p>The only major difference between TransactionList's behaviour and
 * database transactions is the concept that changes to the source EventList
 * can occur *during* a live transaction. Those changes to the source EventList
 * may conflict with changes proposed within the transaction and thus must be
 * arbitrated and merged into the TransactionList using a {@link Policy} object
 * which contains the logic for dealing with those situations. To illustrate
 * this feature, imagine that a long-running transaction begins and updates the
 * first element of the List to be "frog." Now imagine that another Thread
 * changes the first element of the source List to be "mongoose." We have a
 * dilemma. Which value should be honoured, "frog" or "mongoose"? The answer to
 * that question comes from the {@link Policy} object, and specifically from
 * {@link Policy#sourceUpdatedTargetUpdated}. The {@link Policy} interface
 * defines a total of three methods that arbitrate the 3 different conflicts
 * that can occur in practice.
 *
 * <p>By providing an implementation of the Policy interface to the constructor
 * of TransactionList you can control the handling of conflicts that arise
 * during long running transactions. Two convenient implementations exist for
 * the common cases: {@link #PREFER_TARGET_CHANGES} and
 * {@link #PREFER_SOURCE_CHANGES}.
 *
 * <p>Note that if you want to avoid dealing with conflicts altogether you can
 * simply acquire the write lock to the TransactionList and hold it for the
 * duration of the transaction like so:
 *
 * <pre>
 * EventList source = ...
 * TransactionList txList = new TransactionList(source);
 *
 * txList.getReadWriteLock().writeLock().lock();
 * try {
 *    txList.begin();
 *
 *    // mutate the txList arbitrarily
 *
 *    txList.commit();
 * } finally {
 *    txList.getReadWriteLock().writeLock().unlock();
 * }
 * </pre>
 *
 * If the transaction is capable of lasting for long periods of time, then it
 * may be undesirable to block other Threads from using the EventList pipeline
 * by acquiring the write lock. The decision must be made by the API user.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TransactionList<S> extends TransformedList<S,S> {

    /** A {@link Policy} implementation that always prefers to use the changes from the TransactionList. */
    public static final Policy PREFER_TARGET_CHANGES = new PreferTargetChangesPolicy();
    /** A {@link Policy} implementation that always prefers to use the changes from the source EventList. */
    public static final Policy PREFER_SOURCE_CHANGES = new PreferSourceChangesPolicy();

    /** The data structure that maps indices between this TransactionList and the source EventList despite arbitrary changes. */
    private final Tree4Deltas<S> deltas = new Tree4Deltas<S>();

    /** A flag to denote whether a transaction is currently active. */
    private boolean txStarted = false;

    /** An object encapsulating the logic for arbitrating conflicting changes between the TransactionList and the underlying source EventList. */
    private final Policy<S> policy;

    /**
     * Constructs a TransactionList over top of the given <code>source</code>.
     * If conflicts occur, the changes proposed by the TransactionList are
     * always preferred over those made against the <code>source</code>.
     *
     * @param source the {@link EventList} to transform
     */
    public TransactionList(EventList<S> source) {
        this(source, PREFER_TARGET_CHANGES);
    }

    /**
     * Constructs a TransactionList over top of the given <code>source</code>.
     * If conflicts occur the given <code>policy</code> is consulted to
     * arbitrate the result.
     * 
     * @param source the {@link EventList} to transform
     * @param policy the logic consulted to arbitrate conflicting changes
     */
    public TransactionList(EventList<S> source, Policy<S> policy) {
        super(source);

        this.policy = policy;

        deltas.horribleHackPreferMostRecentValue = true;
        deltas.reset(source.size());

        source.addListEventListener(this);
    }

    /**
     * Demarks the beginning of a transaction. The transaction must eventually
     * be ended using either {@link #commit()} or {@link #rollback()} at which
     * point all changes will be honoured or discarded.
     *
     * @throws IllegalStateException if a transaction is already in progress
     */
    public void begin() {
        if (txStarted)
            throw new IllegalStateException("Cannot begin() another transaction before committing or rolling back the current transaction");

        deltas.reset(source.size());
        txStarted = true;
    }

    /**
     * Demarks the end of a transaction and the desire for all changes
     * to the TransactionList during the transaction to be flushed to the
     * source EventList.
     *
     * @throws IllegalStateException if a transaction has not been started
     */
    public void commit() {
        if (!txStarted)
            throw new IllegalStateException("Cannot commit() a transaction that does not exist. Please call begin() to start a transaction first.");

        new EventListTransactionHack().runAsTransaction(new HackRunnable(), source);
    }

    /**
     * Demarks the end of a transaction and the desire for all changes
     * to the TransactionList during the transaction to be discarded and for
     * the TransactionList to be reset to the contents of the source EventList.
     *
     * @throws IllegalStateException if a transaction has not been started
     */
    public void rollback() {
        if (!txStarted)
            throw new IllegalStateException("Cannot rollback() a transaction that does not exist. Please call begin() to start a transaction first.");

        txStarted = false;
        deltas.reset(source.size());
    }

    /**
     * The mother of all hacks.
     * todo remove this hack
     */
    private final class HackRunnable implements Runnable {
        public void run() {
            // 1. undo the transaction's changes on self
            updates.beginEvent(true);
            for(Tree4Deltas.Iterator<S> i = deltas.iterator(); i.hasNext(); ) {
                i.next();
                int index = i.getIndex();
                int type = i.getType();
                if(type == ListEvent.INSERT) {
                    updates.elementDeleted(index, deltas.getTargetValue(index));
                } else if(type == ListEvent.UPDATE) {
                    updates.elementUpdated(index, deltas.getTargetValue(index));
                } else if(type == ListEvent.DELETE) {
                    updates.addInsert(index);
                }
            }
            updates.commitEvent();

            final int sourceSizeBeforeChanges = source.size();

            // 2. redo the ListEvent's changes on source
            for(Tree4Deltas.Iterator<S> i = deltas.iterator(); i.hasNext(); ) {
                i.next();
                int index = i.getIndex();
                int type = i.getType();
                if(type == ListEvent.INSERT) {
                    source.add(index, i.getPreviousValue());
                } else if(type == ListEvent.UPDATE) {
                    source.set(index, i.getPreviousValue());
                } else if(type == ListEvent.DELETE) {
                    source.remove(index);
                }
            }

            txStarted = false;
            deltas.reset(sourceSizeBeforeChanges);
        }
    }

    /**
     * The mother of all hacks.
     * todo remove this hack
     */
    private static final class EventListTransactionHack {
        public void runAsTransaction(final Runnable task, EventList eventList) {
            BasicEventList list = new BasicEventList(eventList.getPublisher(), eventList.getReadWriteLock());
            ListEventListener listener = new ListEventListener() {
                public void listChanged(ListEvent listChanges) {
                    task.run();
                }
            };
            list.addListEventListener(listener);
            list.add("A");
            list.removeListEventListener(listener);
        }
    }

    /** @inheritDoc */
    protected boolean isWritable() {
        return true;
    }

    /**
     * This method is called when the source EventList is updated. If a
     * transaction is currently in progress then any conflicts (changes in
     * the source EventList to elements which have already been modified
     * within the transaction) are resolved by consulting the {@link Policy}
     * object this TransactionList was constructed with.
     *
     * @param listChanges the changes which have occurred in the source
     */
    public void listChanged(ListEvent<S> listChanges) {
        updates.beginEvent(true);

        while (listChanges.next()) {
            final int sourceChangeType = listChanges.getType();
            final int sourceIndex = listChanges.getIndex();

            if (sourceChangeType == ListEvent.INSERT) {
                // inserts in the source can never be conflicts
                deltas.sourceInsert(sourceIndex);
                updates.addInsert(deltas.sourceToTarget(sourceIndex));
                
            } else if (sourceChangeType == ListEvent.UPDATE) {
                // updates in the source may conflict with updates or deletes in the TransactionList
                final byte targetChangeType = deltas.getChangeType(sourceIndex);

                if (targetChangeType == Tree4Deltas.INSERT) {
                    // this is an impossible case which we document with an exception
                    throw new IllegalStateException("Unexpected target type: insert over top of update");

                } else if (targetChangeType == Tree4Deltas.UPDATE) {
                    // fetch the transaction's value and the source's value for the index in question
                    final int targetIndex = deltas.sourceToTarget(sourceIndex);
                    final S targetValue = deltas.getTargetValue(targetIndex);
                    final S sourceValue = source.get(sourceIndex);

                    // ask the policy to arbitrate the conflicting updates
                    final S resolvedValue = policy.sourceUpdatedTargetUpdated(sourceValue, targetValue);

                    // if the policy says to keep the source value, remove the modification from the transaction
                    if (resolvedValue == sourceValue) {
                        deltas.sourceRevert(sourceIndex);
                        updates.elementUpdated(targetIndex, targetValue);

                    // if the policy says to keep the transaction's value, no work exists
                    } else if (resolvedValue == targetValue) {
                        // no-op the target change has already been broadcasted

                    // otherwise an entirely different value has been returned and we must record it as the new transaction value
                    } else {
                        deltas.targetUpdate(targetIndex, targetIndex+1, resolvedValue);
                        updates.elementUpdated(targetIndex, targetValue);
                    }

                } else if (targetChangeType == Tree4Deltas.DELETE) {
                    // fetch the transaction's value and the source's value for the index in question
                    final S deletedFromTarget = deltas.getSourceValue(sourceIndex);
                    final S updatedFromSource = source.get(sourceIndex);

                    // ask the policy to arbitrate the conflicting update and delete
                    final Policy.Result policyResult = policy.sourceUpdatedTargetDeleted(updatedFromSource, deletedFromTarget);

                    // if the policy says to keep the source value, then add the updated source value back into the transaction
                    if (policyResult == Policy.KEEP_SOURCE) {
                        deltas.sourceRevert(sourceIndex);
                        updates.addInsert(deltas.sourceToTarget(sourceIndex));

                    // if the policy says to keep the transaction value, then update the record we have of the value that was deleted
                    } else if (policyResult == Policy.KEEP_TARGET) {
                        // change the value we recorded as the deleted value
                        deltas.sourceRevert(sourceIndex);
                        final int targetIndex = deltas.sourceToTarget(sourceIndex);
                        deltas.targetDelete(targetIndex, targetIndex+1, updatedFromSource);
                    }

                } else if (targetChangeType == Tree4Deltas.NO_CHANGE) {
                    // update the record we have of the value at that index
                    updates.elementUpdated(deltas.sourceToTarget(sourceIndex), listChanges.getPreviousValue());
                }

            } else if (sourceChangeType == ListEvent.DELETE) {
                final byte targetChangeType = deltas.getChangeType(sourceIndex);

                if (targetChangeType == Tree4Deltas.INSERT) {
                    // this is an impossible case which we document with an exception
                    throw new IllegalStateException("Unexpected target type: insert over top of delete");

                } else if (targetChangeType == Tree4Deltas.UPDATE) {
                    // fetch the transaction's value and the source's value for the index in question
                    final int targetIndex = deltas.sourceToTarget(sourceIndex);
                    final S targetValue = deltas.getTargetValue(targetIndex);
                    final S sourceValue = listChanges.getPreviousValue();

                    // ask the policy to arbitrate the conflicting delete and update
                    final Policy.Result policyResult = policy.sourceDeletedTargetUpdated(sourceValue, targetValue);

                    // if the policy says to keep the source value, then remove the value from the transaction
                    if (policyResult == Policy.KEEP_SOURCE) {
                        deltas.sourceDelete(sourceIndex);
                        updates.elementDeleted(targetIndex, targetValue);

                    // if the policy says to keep the transaction value, then change the update to look like an insert
                    } else if (policyResult == Policy.KEEP_TARGET) {
                        deltas.sourceDelete(sourceIndex);
                        deltas.targetInsert(targetIndex, targetIndex+1, targetValue);
                    }

                } else if (targetChangeType == Tree4Deltas.DELETE) {
                    // two deletes simply result in no change (the delete in the transaction has already been broadcasted to listeners)
                    deltas.sourceDelete(sourceIndex);

                } else if (targetChangeType == Tree4Deltas.NO_CHANGE) {
                    // delete the value from the transaction and broadcast the change
                    updates.elementDeleted(deltas.sourceToTarget(sourceIndex), listChanges.getPreviousValue());
                    deltas.sourceDelete(sourceIndex);
                }
            }
        }

        updates.commitEvent();
    }

    //
    // List methods which defer to either {@link #txSnapshot} or the source
    // list depending on whether the calling Thread is currently involved in a
    // transaction at the moment. That is, views of transactions that are only
    // partially complete will always appear to be local to the Thread that
    // started them.
    //

    /** @inheritDoc */
    public void add(int index, S element) {
        if (txStarted) {
            updates.beginEvent();
            updates.addInsert(index);
            deltas.targetInsert(index, index+1, element);
            updates.commitEvent();
        } else {
            super.add(index, element);
        }
    }

    /** @inheritDoc */
    public S remove(int index) {
        if (txStarted) {
            updates.beginEvent();
            final S value = get(index);
            deltas.targetDelete(index, index+1, value);
            updates.elementDeleted(index, value);
            updates.commitEvent();
            return value;
        } else {
            return super.remove(index);
        }
    }

    /** @inheritDoc */
    public S set(int index, S element) {
        if (txStarted) {
            updates.beginEvent();
            final S value = get(index);
            deltas.targetUpdate(index, index+1, element);
            updates.elementUpdated(index, value);
            updates.commitEvent();
            return value;
        } else {
            return super.set(index, element);
        }
    }

    /** @inheritDoc */
    public S get(int index) {
        final Object targetValue = deltas.getTargetValue(index);
        if (targetValue == ListEvent.UNKNOWN_VALUE) {
            return source.get(deltas.targetToSource(index));
        } else {
            return (S) targetValue;
        }
    }

    /** @inheritDoc */
    public int size() {
        return deltas.targetSize();
    }

    /**
     * A Policy object is used by TransactionList to arbitrate conflicting
     * changes that may occur during a transaction. Conflicts occur when a
     * transaction begins within a TransactionList and then both the
     * TransactionList and its source EventList attempt to change the same
     * element either by updating it or deleting it. When these conficts occur,
     * TransactionList must decide which version it will honour if the
     * transaction is committed. The strategy for making that decision is
     * defined by this interface.
     */
    public interface Policy<S> {

        /** An object that indicates the value from the source EventList should be honoured. */
        public static final Result KEEP_SOURCE = new Result();

        /** An object that indicates the value from the TransactionList should be honoured. */
        public static final Result KEEP_TARGET = new Result();

        /**
         * The same value has been updated by the TransactionList and then
         * deleted from the source EventList. The {@link Result} returned by
         * this method determines whether the delete or the update is honoured
         * by the transaction.
         *
         * @param deletedFromSource the value removed in the source of the TransactionList
         * @param updatedFromTarget the value updated in the TransactionList
         * @return {@link #KEEP_SOURCE} if the delete is honoured and the update discarded;
         *      {@link #KEEP_TARGET} if the update is honoured and the delete discarded
         */
        public Result sourceDeletedTargetUpdated(S deletedFromSource, S updatedFromTarget);

        /**
         * The same value has been deleted by the TransactionList and then
         * updated from the source EventList. The {@link Result} returned by
         * this method determines whether the delete or the update is honoured
         * by the transaction.
         *
         * @param updatedFromSource the value updated in the source of the TransactionList
         * @param deletedFromTarget the value deleted in the TransactionList
         * @return {@link #KEEP_SOURCE} if the delete is honoured and the update discarded;
         *      {@link #KEEP_TARGET} if the update is honoured and the delete discarded
         */
        public Result sourceUpdatedTargetDeleted(S updatedFromSource, S deletedFromTarget);

        /**
         * The same value has been updated by the TransactionList and then
         * updated from the source EventList. The Object returned by this value
         * will be the new value used within the TransactionList. Valid
         * implementations of this method could return <code>updatedFromSource</code>,
         *      <code>updatedFromTarget</code>, and even a new Object created
         *      by merging the two changes.
         *
         * @param updatedFromSource the value updated in the source of the TransactionList
         * @param updatedFromTarget the value updated in the TransactionList
         * @return an object that may be <code>updatedFromSource</code>,
         *      <code>updatedFromTarget</code>, and even a new Object created
         *      by merging the two changes
         */
        public S sourceUpdatedTargetUpdated(S updatedFromSource, S updatedFromTarget);

        /**
         * A typesafe object representing the intention returned from some
         * methods of the Policy object.
         */
        public static class Result {
            private Result() {}
        }
    }

    /**
     * A convenient implementation of {@link Policy} that always prefers to
     * keep changes from the source EventList when conflicts occur.
     */
    private static class PreferSourceChangesPolicy implements Policy {

        public Result sourceDeletedTargetUpdated(Object deletedFromSource, Object updatedFromTarget) {
            return KEEP_SOURCE;
        }

        public Result sourceUpdatedTargetDeleted(Object updatedFromSource, Object deletedFromTarget) {
            return KEEP_SOURCE;
        }

        public Object sourceUpdatedTargetUpdated(Object updatedFromSource, Object updatedFromTarget) {
            return updatedFromSource;
        }
    }

    /**
     * A convenient implementation of {@link Policy} that always prefers to
     * keep changes from the TransactionList when conflicts occur.
     */
    private static class PreferTargetChangesPolicy implements Policy {

        public Result sourceDeletedTargetUpdated(Object deletedFromSource, Object updatedFromTarget) {
            return KEEP_TARGET;
        }

        public Result sourceUpdatedTargetDeleted(Object updatedFromSource, Object deletedFromTarget) {
            return KEEP_TARGET;
        }

        public Object sourceUpdatedTargetUpdated(Object updatedFromSource, Object updatedFromTarget) {
            return updatedFromTarget;
        }
    }
}