/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;

/**
 * An EventList is a list that can send events to listeners. There are two
 * main types of EventLists:
 * <li><strong><i>Root lists:</i></strong> EventLists that provide data
 * <li><strong><i>Transformed lists:</i></strong> EventLists that transform the
 * data from a different EventList
 *
 * <p>Usually EventLists are deployed with a chain of transformed lists that
 * view a root list. For example, a BasicEventList may be sorted via a
 * SortedList and then filtered with one or more AbstractFilterLists.
 * The final list in this chain can then be displayed using a widget such
 * as ListTable or EventJList.
 *
 * <p>EventLists are not thread-safe. If you are sharing an EventList with another
 * thread, you can make access thread-safe by using a <code>ThreadSafeList</code>:<br>
 * <code><pre>EventList myList = ...
 * ThreadSafeList myThreadSafeList = new ThreadSafeList(myList);
 * // access myThreadSafeList here</code></pre>
 * 
 * <p>Alternatively, you can manually acquire the read lock before access and
 * release it afterwards:<br>
 * <code><pre>myList.getReadWriteLock().readLock().lock();
 * try {
 *     // access myList here
 * } finally {
 *     myList.getReadWriteLock().readLock().unlock();
 * }</pre></code>
 *
 * <p>EventLists can be writable but there is no requirement for doing so.
 * When a <i>root list</i> is changed, the change event is propagated to all
 * ListEventListeners that are interested in the change. The set of
 * ListEventListeners will include any <i>transformed lists</i> providing a
 * transformed view of the root list.
 *
 * <p>When a <i>transformed list</i> is modified via <code>set()</code>,
 * <code>add()</code>, <code>remove()</code>, etc. the modification is
 * propagated to the root list which can then make the change or throw an
 * Exception. If the change is made it will be propagated back to the
 * transformed list.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> changes to
 * <i>transformed lists</i> may break the contract normally required by all
 * implementations of <code>java.util.List</code>. This is because the
 * change made may be transformed itself. For example, calling the method
 * <code>add()</code> allows the user to specify at what index the value
 * is inserted. But a <code>SortedList</code> will <strong>ignore</strong>
 * this index and insert the value in sorted order. Similarly, a value
 * added to a <code>AbstractFilterList</code> may appear not to be added
 * at all. This is because the filter may have filtered-out the added value.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface EventList extends List {

    /**
     * Registers the specified listener to receive change updates for this list.
     */
    public void addListEventListener(ListEventListener listChangeListener);

    /**
     * Removes the specified listener from receiving change updates for this
     * list.
     *
     * @since 2004-01-30. This has been added to support the new FreezableList.
     *     If you need to implement this method, please review the implementation
     *     in TransformedList for an example.
     */
    public void removeListEventListener(ListEventListener listChangeListener);

    /**
     * Gets the lock object in order to access this list in a thread-safe manner.
     * This will return a <strong>re-entrant</strong> implementation of
     * ReadWriteLock which can be used to guarantee mutual exclusion on access.
     *
     * @since 2004-05-21 This replaces <code>getRootList()</code> which was used
     *     for synchronizing access to lists. That technique was simple but it
     *     suffers from the problem of preventing concurrent reads. This problem
     *     becomes magnified when multiple lists have a common source, and such
     *     lists perform read-intensive operations such as sorting.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=26">Bug 26</a>
     */
    public ReadWriteLock getReadWriteLock();
}
