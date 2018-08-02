/* Glazed Lists                                                 (c) 2003-2018 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A helper class for guarding executable code (in the form of {@link Consumer} or {@link Function}
 * objects) with read or write locks on an {@link EventList}.
 * <p>
 * These methods complement the corresponding default methods in the {@link EventList} interface.
 * Whereas those methods only expose EventList methods to the Consumer or Function objects, these
 * methods here expose the EventList implementation class and therefore offer more methods to use.
 * <p>
 * For example:
 * 
 * <pre>
 * {@code
 * EventList<String> source = ...
 * TransactionList<String> transactionList = new TransactionList<>(source);
 * Guard.acceptWithWriteLock(transactionList, txList -> {
 *      txList.beginEvent(true);
 *      txList.add("A new element");
 *      txList.set(0, "A changed element");
 *      txList.remove(6);
 *      txList.commitEvent();
 * });
 * }
 * </pre>
 */
public final class Guard {
    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private Guard() {
        throw new UnsupportedOperationException();
    }

    /**
     * Executes the block of code represented by the given consumer while holding the read lock of
     * the supplied {@link EventList}.
     *
     * @param list EventList
     * @param consumer the consumer != null
     * @param <E> the element type of the list
     * @param <L> the concrete list type
     * @see EventList#acceptWithReadLock(Consumer)
     */
    public static <E, L extends EventList<E>> void acceptWithReadLock(L eventList, Consumer<L> consumer) {
        if (eventList == null) {
            return;
        }

        eventList.getReadWriteLock().readLock().lock();
        try {
            consumer.accept(eventList);
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Executes the block of code represented by the given consumer while holding the write lock of
     * the supplied {@link EventList}.
     *
     * @param list EventList
     * @param consumer the consumer != null
     * @param <E> the element type of the list
     * @param <L> the concrete list type
     * @see EventList#acceptWithWriteLock(Consumer)
     */
    public static <E, L extends EventList<E>> void acceptWithWriteLock(L eventList, Consumer<L> consumer) {
        if (eventList == null) {
            return;
        }

        eventList.getReadWriteLock().writeLock().lock();
        try {
            consumer.accept(eventList);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Applies the given function to the supplied {@link EventList} while holding the read lock of
     * this EventList.
     * 
     * @param list EventList != null
     * @param function the function != null
     * @param <E> the element type of the list
     * @param <L> the concrete list type
     * @param <R> the result type of the function
     * @return the result of the function
     * @see EventList#applyWithReadLock(Function)
     */
    public static <E, L extends EventList<E>, R> R applyWithReadLock(L list, Function<L, R> function) {
        list.getReadWriteLock().readLock().lock();
        try {
            return function.apply(list);
        } finally {
            list.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Applies the given function to the supplied {@link EventList} while holding the write lock of
     * this EventList.
     * 
     * @param list EventList != null
     * @param function the function != null
     * @param <E> the element type of the list
     * @param <L> the concrete list type
     * @param <R> the result type of the function
     * @return the result of the function
     * @see EventList#applyWithWriteLock(Function)
     */
    public static <E, L extends EventList<E>, R> R applyWithWriteLock(L list, Function<L, R> function) {
        list.getReadWriteLock().writeLock().lock();
        try {
            return function.apply(list);
        } finally {
            list.getReadWriteLock().writeLock().unlock();
        }
    }
}
