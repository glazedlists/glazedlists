/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// To extend the List interface from Java Collections
import java.util.*;

/**
 * An observable {@link List}. {@link ListEventListener}s can register to be
 * notified when this list changes.
 *
 * <p>{@link EventList}s may be writable or read-only. Consult the Javadoc for
 * your {@link EventList} if you are unsure.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> {@link EventList}s
 * are thread ready but not thread safe. If you are sharing an {@link EventList}
 * between multiple threads, you can add thread safety by using the built-in
 * locks:
 * <pre> EventList myList = ...
 * myList.getReadWriteLock().writeLock().lock();
 * try {
 *    // access myList here
 *    if(myList.size() > 3) {
 *       System.out.println(myList.get(3));
 *       myList.remove(3);
 *    }
 * } finally {
 *    myList.getReadWriteLock().writeLock().unlock();
 * }</pre>
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> {@link EventList}s
 * may break the contract required by {@link java.util.List}. For example, when
 * you {@link #add(int,Object) add()} on a {@link SortedList}, it will ignore the specified
 * index so that the element will be inserted in sorted order.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface EventList extends List {

    /**
     * Registers the specified listener to receive change updates for this list.
     */
    public void addListEventListener(ListEventListener listChangeListener);

    /**
     * Removes the specified listener from receiving change updates for this list.
     */
    public void removeListEventListener(ListEventListener listChangeListener);

    /**
     * Gets the lock required to share this list between multiple threads.
     *
     * @return a re-entrant {@link ReadWriteLock} that guarantees thread safe
     *      access to this list.
     */
    public ReadWriteLock getReadWriteLock();

    /**
     * Get the publisher used to distribute {@link ListEvent}s.
     */
    public ListEventPublisher getPublisher();

}
