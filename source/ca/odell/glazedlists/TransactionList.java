/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.event.Tree4Deltas;

/**
 * A TransactionList provides functionality to an EventList pipeline that is
 * very similar to traditional database transactions. A batch of changes can be
 * proposed against the EventList pipeline and either {@link #commit committed}
 * or {@link #rollback rolled back} at some point in the future. For example,
 * an application could present the user with a dialog that displays a
 * TransactionList sitting on top of their normal EventList pipeline. The user
 * is allowed to make arbitrary modifications to the TransactionList within the
 * dialog and commit those changes to the true EventList pipeline by closing
 * the dialog with the "Ok" button, or discard the changes by closing the
 * dialog with the "Cancel" button.
 *
 * <p>Transactions are started using {@link #begin()} and a maximum of one
 * transaction can exist in this TransactionList at any time. Consequently an
 * {@link IllegalStateException} is thrown if multiple calls to {@link #begin()}
 * occur before {@link #commit()} or {@link #rollback()} is called.
 *
 * <p>After beginning a transaction, changes are accumulated by calling the
 * {@link #remove} and {@link #set}. During this time TransactionList will
 * begin to diverge from its source EventList. TransactionList will broadcast
 * ListEvents describing each mutation during the transaction so the EventLists
 * stacked on top of TransactionList will be see the changes as they are made
 * while the EventLists below the TransactionList will see none of the changes
 * yet.
 *
 * <p>After all changes have been made, the last thing to do is end the
 * transaction by calling either {@link #commit()} if the changes should be
 * integrated with the source EventList or {@link #rollback()} if the changes
 * should be discarded.
 *
 * <p>The only major departure from database transactions is the concept that
 * changes to the source EventList can occur *during* a live transaction. Those
 * changes are then arbitrated and merged into the TransactionList using a
 * {@link Policy} object which contains the logic for dealing with those
 * situations. To illustrate this feature, imagine that a long-running
 * transaction begins and updates the first element of the List to be "frog."
 * Now imagine that another Thread changes the first element of the source
 * List to be "mongoose." We have a dilemma. Which value should be honoured,
 * "frog" or "mongoose"? The answer to that question comes from the
 * {@link Policy} object, and specifically from
 * {@link Policy#sourceUpdatedTargetUpdated}.
 *
 * <p>By providing an implementation of the Policy interface to the constructor
 * of TransactionList you can control the behaviour of conflicts that arise
 * during long running transactions. Two convenient implementations exist for
 * the common cases: {@link #PREFER_TARGET_CHANGES} and
 * {@link #PREFER_SOURCE_CHANGES}.
 *
 * <p>Note that if you want to avoid dealing with conflicts altogether you
 * simply want to acquire the write lock to the TransactionList and hold it for
 * the duration of the transaction.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TransactionList<S> extends TransformedList<S,S> {

    public static final Policy PREFER_TARGET_CHANGES = new PreferTargetChangesPolicy();
    public static final Policy PREFER_SOURCE_CHANGES = new PreferSourceChangesPolicy();

    private final Tree4Deltas<S> deltas = new Tree4Deltas<S>();

    private boolean txStarted = false;

    private final Policy<S> policy;

    public TransactionList(EventList<S> source) {
        this(source, PREFER_TARGET_CHANGES);
    }

    public TransactionList(EventList<S> source, Policy<S> policy) {
        super(source);

        this.policy = policy;
        deltas.horribleHackPreferMostRecentValue = true;
        
        deltas.reset(source.size());
        source.addListEventListener(this);
    }

    public void begin() {
        if (txStarted)
            throw new IllegalStateException("Cannot begin() another transaction before committing or rolling back the previous transaction");

        deltas.reset(source.size());
        txStarted = true;
    }

    public void commit() {
        if (!txStarted)
            throw new IllegalStateException("Cannot commit() a transaction that does not exist");

        new EventListTransactionHack().runAsTransaction(new Runnable() {
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
        }, source);
    }

    /**
     * The mother of all hacks.
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

    public void rollback() {
        if (!txStarted)
            throw new IllegalStateException("Cannot rollback() a transaction that does not exist");

        txStarted = false;
        deltas.reset(source.size());
    }

    protected boolean isWritable() {
        return true;
    }

    public void listChanged(ListEvent<S> listChanges) {
        updates.beginEvent(true);

        while (listChanges.next()) {
            int sourceType = listChanges.getType();
            int sourceIndex = listChanges.getIndex();

            if (sourceType == ListEvent.INSERT) {
                deltas.sourceInsert(sourceIndex);
                updates.addInsert(deltas.sourceToTarget(sourceIndex));
                
            } else if (sourceType == ListEvent.UPDATE) {
                final byte targetType = deltas.getChangeType(sourceIndex);

                if (targetType == Tree4Deltas.INSERT) {
                    throw new IllegalStateException("Unexpected target type: insert over top of update");

                } else if (targetType == Tree4Deltas.UPDATE) {
                    final int targetIndex = deltas.sourceToTarget(sourceIndex);
                    final S targetValue = deltas.getTargetValue(targetIndex);
                    final S sourceValue = source.get(sourceIndex);

                    final S resolvedValue = policy.sourceUpdatedTargetUpdated(sourceValue, targetValue);
                    if (resolvedValue == sourceValue) {
                        deltas.sourceRevert(sourceIndex);
                        updates.elementUpdated(targetIndex, targetValue);
                    } else if (resolvedValue == targetValue) {
                        // no-op the target change has already been broadcasted
                    } else {
                        deltas.targetUpdate(targetIndex, targetIndex+1, resolvedValue);
                        updates.elementUpdated(targetIndex, targetValue);
                    }


                } else if (targetType == Tree4Deltas.DELETE) {
                    final S deletedFromTarget = deltas.getSourceValue(sourceIndex);// deltas.getTargetValue(targetIndex);
                    final S updatedFromSource = source.get(sourceIndex);
                    final Policy.Result policyResult = policy.sourceUpdatedTargetDeleted(updatedFromSource, deletedFromTarget);

                    if (policyResult == Policy.KEEP_SOURCE) {
                        deltas.sourceRevert(sourceIndex);
                        updates.addInsert(deltas.sourceToTarget(sourceIndex));

                    } else if (policyResult == Policy.KEEP_TARGET) {
                        // change the value we recorded as the deleted value
                        deltas.sourceRevert(sourceIndex);
                        final int targetIndex = deltas.sourceToTarget(sourceIndex);
                        deltas.targetDelete(targetIndex, targetIndex+1, updatedFromSource);

                    } else {
                        throw new IllegalStateException("Unexpected policy result: " + policyResult);
                    }

                } else if (targetType == Tree4Deltas.NO_CHANGE) {
                    updates.elementUpdated(deltas.sourceToTarget(sourceIndex), listChanges.getPreviousValue());
                }

            } else if (sourceType == ListEvent.DELETE) {
                final byte targetType = deltas.getChangeType(sourceIndex);

                if (targetType == Tree4Deltas.INSERT) {
                    throw new IllegalStateException("Unexpected target type: insert over top of delete");

                } else if (targetType == Tree4Deltas.UPDATE) {
                    final int targetIndex = deltas.sourceToTarget(sourceIndex);
                    final S targetValue = deltas.getTargetValue(targetIndex);
                    final S sourceValue = listChanges.getPreviousValue();
                    final Policy.Result policyResult = policy.sourceDeletedTargetUpdated(sourceValue, targetValue);

                    if (policyResult == Policy.KEEP_SOURCE) {
                        deltas.sourceDelete(sourceIndex);
                        updates.elementDeleted(targetIndex, targetValue);

                    } else if (policyResult == Policy.KEEP_TARGET) {
                        deltas.sourceDelete(sourceIndex);
                        deltas.targetInsert(targetIndex, targetIndex+1, targetValue);

                    } else {
                        throw new IllegalStateException("Unexpected policy result: " + policyResult);
                    }

                } else if (targetType == Tree4Deltas.DELETE) {
                    deltas.sourceDelete(sourceIndex);

                } else if (targetType == Tree4Deltas.NO_CHANGE) {
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

    public S get(int index) {
        final Object targetValue = deltas.getTargetValue(index);
        if (targetValue == ListEvent.UNKNOWN_VALUE) {
            return source.get(deltas.targetToSource(index));
        } else {
            return (S) targetValue;
        }
    }

    public int size() {
        return deltas.targetSize();
    }

    public interface Policy<S> {

        public static final Result KEEP_SOURCE = new Result();
        public static final Result KEEP_TARGET = new Result();

        /**
         *
         * @param deletedFromSource the value removed from the source of the TransactionList
         * @param updatedFromTarget the value updated in the TransactionList
         * @return {@link #KEEP_SOURCE} if the delete be honoured and the update discarded;
         *      {@link #KEEP_TARGET} if the update is honoured and the delete discarded
         */
        public Result sourceDeletedTargetUpdated(S deletedFromSource, S updatedFromTarget);
        public Result sourceUpdatedTargetDeleted(S updatedFromSource, S deletedFromTarget);
        public S sourceUpdatedTargetUpdated(S updatedFromSource, S updatedFromTarget);

        public static class Result {
            private Result() {}
        }
    }

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