/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import com.odellengineeringltd.glazedlists.util.concurrent.*;

/**
 * An EventList is a list that can send events to listeners. There are two
 * main types of EventLists:
 * <li><strong><i>Root lists:</i></strong> EventLists that provide data
 * <li><strong><i>Mutation lists:</i></strong> EventLists that mutate the
 * data from a different EventList
 *
 * <p>Usually EventLists are deployed with a chain of mutation lists that
 * view a root list. For example, a BasicEventList may be sorted via a
 * SortedList and then filtered with one or more AbstractFilterLists.
 * The final list in this chain can then be displayed using a widget such
 * as ListTable or EventJList.
 *
 * <p>EventLists are thread-safe except where otherwise noted. When implementing
 * <code>EventList</code>, use the read/write lock, <code>getReadWriteLock()</code>
 * to ensure mutual exclusion. For the simplest implementation, lists should
 * return the <code>ReadWriteLock</code> of their source lists.
 *
 * <p>EventLists can be writable but there is no requirement for doing so.
 * When a <i>root list</i> is changed, the change event is propagated to all
 * ListEventListeners that are interested in the change. The set of
 * ListEventListeners will include any <i>mutation lists</i> providing a
 * mutated view of the root list.
 *
 * <p>When a <i>mutation list</i> is modified via <code>set()</code>,
 * <code>add()</code>, <code>remove()</code>, etc. the modification is
 * propagated to the root list which can then make the change or throw an
 * Exception. If the change is made it will be propagated back to the
 * mutation list.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> changes to
 * <i>mutation lists</i> may break the contract normally required by all
 * implementations of <code>java.util.List</code>. This is because the
 * change made may be mutated itself. For example, calling the method
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
     *     in MutationList for an example.
     */
    public void removeListEventListener(ListEventListener listChangeListener);

    /**
     * Gets the source list that this list depends on. This may return the same
     * object, or another object. This is useful for synchronization of chained
     * lists, so that dependent lists can be synchronized on the root list to
     * prevent deadlocks.
     *
     * @deprecated As of 2004-05-21, this method has been replaced with
     *      <code>getReadWriteLock()</code>.
     */
    public EventList getRootList();
    
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
