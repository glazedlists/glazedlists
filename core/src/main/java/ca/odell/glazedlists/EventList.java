/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An observable {@link List}. {@link ListEventListener}s can register to be
 * notified when this list changes. A {@link ListEvent} represents these changes
 * to an {@link EventList}.
 *
 * <p>{@link EventList}s may be writable or read-only. Consult the Javadoc for
 * your {@link EventList} if you are unsure.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> {@link EventList}s
 * are thread ready but not thread safe. If you are sharing an {@link EventList}
 * between multiple threads, you can add thread safety by using the built-in
 * locks:
 * <pre>
 * EventList myList = ...
 * myList.getReadWriteLock().writeLock().lock();
 * try {
 *    // access myList here
 *    if(myList.size() > 3) {
 *       System.out.println(myList.get(3));
 *       myList.remove(3);
 *    }
 * } finally {
 *    myList.getReadWriteLock().writeLock().unlock();
 * }
 * </pre>
 *
 * Note that you are also required to acquire and hold the lock during the
 * construction of an EventList if concurrent modifications are possible in
 * your environment, like so:
 *
 * <pre>
 * EventList source = ...
 * SortedList sorted;
 * source.getReadWriteLock().readLock().lock();
 * try {
 *    sorted = new SortedList(source);
 * } finally {
 *    source.getReadWriteLock().readLock().unlock();
 * }
 * </pre>
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> {@link EventList}s
 * may break the contract required by {@link java.util.List}. For example, when
 * you {@link #add(int,Object) add()} on a {@link SortedList}, it will ignore the specified
 * index so that the element will be inserted in sorted order.
 *
 * @see GlazedLists#eventListOf(Object[])
 * @see GlazedLists#eventList(Collection)
 * @see GlazedLists#readOnlyList(EventList)
 * @see GlazedLists#threadSafeList(EventList)
 * @see GlazedLists#weakReferenceProxy(EventList, ListEventListener)
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface EventList<E> extends List<E> {

    /**
     * Registers the specified listener to receive change updates for this list.
     *
     * @param listChangeListener event listener != null
     * @throws NullPointerException if the specified listener is null
     */
    public void addListEventListener(ListEventListener<? super E> listChangeListener);

    /**
     * Removes the specified listener from receiving change updates for this list.
     *
     * @param listChangeListener event listener != null
     * @throws NullPointerException if the specified listener is null
     * @throws IllegalArgumentException if the specified listener wasn't added before
     */
    public void removeListEventListener(ListEventListener<? super E> listChangeListener);

    /**
     * Gets the lock required to share this list between multiple threads. It's always defined.
     *
     * @return a re-entrant {@link ReadWriteLock} that guarantees thread safe
     *      access to this list.
     */
    public ReadWriteLock getReadWriteLock();

    /**
     * Get the publisher used to distribute {@link ListEvent}s. It's always defined.
     */
    public ListEventPublisher getPublisher();

    /**
     * Disposing an EventList will make it eligible for garbage collection.
     * Some EventLists install themselves as listeners to related objects so
     * disposing them is necessary.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link EventList} after it has been disposed.
     */
    public void dispose();
    
    /**
     * Executes the block of code represented by the given consumer while holding the read lock of
     * this EventList.
     * 
     * @param consumer the consumer != null
     * @deprecated this is a <strong>developer preview</strong> API that is experimental and not yet
     *             finalized
     */
    @Deprecated
    default void acceptWithReadLock(Consumer<EventList<E>> consumer) {
        getReadWriteLock().readLock().lock();
        try {
            consumer.accept(this);
        } finally {
            getReadWriteLock().readLock().unlock(); 
        }
    }
    
    /**
     * Executes the block of code represented by the given consumer while holding the write lock of
     * this EventList.
     *
     * @param consumer the consumer != null
     * @deprecated this is a <strong>developer preview</strong> API that is experimental and not yet
     *             finalized
     */
    @Deprecated
    default void acceptWithWriteLock(Consumer<EventList<E>> consumer) {
        getReadWriteLock().writeLock().lock();
        try {
            consumer.accept(this);
        } finally {
            getReadWriteLock().writeLock().unlock();    
        }
    }

    /**
     * Applies the given function while holding the read lock of this EventList.
     * 
     * @param function the function != null
     * @param <R> the result type of the function
     * @return the result of the function
     * @deprecated this is a <strong>developer preview</strong> API that is experimental and not yet
     *             finalized
     */
    @Deprecated
    default <R> R applyWithReadLock(Function<EventList<E>, R> function) {
        getReadWriteLock().readLock().lock();
        try {
            return function.apply(this);
        } finally {
            getReadWriteLock().readLock().unlock(); 
        }
    }
    
    /**
     * Applies the given function while holding the write lock of this EventList.
     * 
     * @param function the function != null
     * @param <R> the result type of the function
     * @return the result of the function
     * @deprecated this is a <strong>developer preview</strong> API that is experimental and not yet
     *             finalized
     */
    @Deprecated
    default <R> R applyWithWriteLock(Function<EventList<E>, R> function) {
        getReadWriteLock().writeLock().lock();
        try {
            return function.apply(this);
        } finally {
            getReadWriteLock().writeLock().unlock();    
        }
    }
}