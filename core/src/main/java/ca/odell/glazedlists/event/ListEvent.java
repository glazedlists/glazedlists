/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

import java.util.EventObject;
import java.util.List;

/**
 * A ListEvent models a sequence of changes to an {@link EventList}. A
 * {@link ListEventListener} can be registered on an {@link EventList} to
 * observe and handle these ListEvents.
 *
 * <p>
 * A <em>simple change</em> is characterized by its type and the corresponding
 * list index. The type indicates the {@link #INSERT}, {@link #UPDATE} or
 * {@link #DELETE} operation that took place at the specified index.
 *
 * <p>
 * Here are some examples:
 * <ul>
 * <li>{@code eventList.add(0, value)} produces an {@link #INSERT} at index 0
 * ("I0")</li>
 * <li>{@code eventList.add(value)} on a list with size 5 produces an
 * {@link #INSERT} at index 5 ("I5")</li>
 * <li>{@code eventList.remove(3)} on a list with appropriate size produces a
 * {@link #DELETE} at index 3 ("D3")</li>
 * <li>{@code eventList.remove(value)} on a list which contains the value at
 * index 2 produces a {@link #DELETE} at index 2 ("D2")</li>
 * <li>{@code eventList.clear()} on a list with size 1 produces a
 * {@link #DELETE} at index 0 ("D0")</li>
 * <li>{@code eventList.set(1, value)} on a list with appropriate size produces
 * an {@link #UPDATE} at index 1 ("U1")</li>
 * </ul>
 *
 * <p>
 * A ListEvent is capable of representing a sequence of such changes. Consider
 * this example:
 *
 * <pre>
 * EventList&lt;String&gt; list = GlazedLists.eventListOf(&quot;A&quot;, &quot;D&quot;, &quot;E&quot;);
 * list.addAll(1, GlazedLists.eventListOf(&quot;B&quot;, &quot;C&quot;));
 * </pre>
 *
 * This operation inserts element "B" at index 1 ("I1") and element "C" at index
 * 2 ("I2"). These two changes are not represented as two separate ListEvents
 * but as a sequence of changes ("I1", "I2") within one ListEvent. Because of
 * this, the ListEvent is accessed like an iterator with the user iterating over
 * the contained sequence of changes, see below.
 *
 * Here is another example:
 *
 * <pre>
 * EventList&lt;String&gt; list = GlazedLists.eventListOf(&quot;A&quot;, &quot;B&quot;, &quot;C&quot;, &quot;B&quot;);
 * list.removeAll(GlazedLists.eventListOf(&quot;A&quot;, &quot;C&quot;));
 * </pre>
 *
 * This will remove element "A" at index 0 and element "C" at original index 2.
 * But the corresponding ListEvent looks like ("D0","D1"). The list index of the
 * second deletion is automatically adjusted, taking the first deletion into
 * account.
 *
 * <p>
 * Note, that the sequence of changes is ordered by ascending list indexes.
 *
 * <p>
 * The typical pattern to iterate over a ListEvent is:
 *
 * <pre>
 * ListEvent listChanges = ...
 * while (listChanges.next()) {
 *     final int type = listChanges.getType());
 *     final int index = listChanges.getIndex();
 *     // handle insert, update or delete at index
 *     ...
 * }
 * </pre>
 *
 * If you need to iterate over a ListEvent again, that's of course possible,
 * just {@link #reset() reset} the ListEvent and iterate again.
 *
 * <p>
 * In addition to simple changes, ListEvent supports the view on list changes as
 * blocks. This basically means, that simple changes of one particular type that
 * build a continuous range of indexes are grouped together. Consider this
 * example:
 *
 * <pre>
 * EventList&lt;String&gt; list = GlazedLists.eventListOf(&quot;A&quot;, &quot;A&quot;, &quot;B&quot;, &quot;B&quot;, &quot;C&quot;, &quot;C&quot;);
 * list.removeAll(GlazedLists.eventListOf(&quot;A&quot;, &quot;C&quot;));
 * </pre>
 *
 * This deletes all occurences of elements "A" and "C" from the list. So there
 * is a deletion from index 0 to index 1 (inclusive) and another deletion from
 * index 2 to 3. So, instead of modeling each change one by one like ("D0",
 * "D0", "D2", "D2"), you can view the changes in blocks "D0-1" and "D2-3". This
 * view is exactly what ListEvent offers by iterating the changes in blocks:
 *
 * <pre>
 * ListEvent listChanges = ...
 * while (listChanges.nextBlock()) {
 *     final int type = listChanges.getType());
 *     final int startIndex = listChanges.getBlockStartIndex();
 *     final int endIndex = listChanges.getBlockEndIndex();
 *     // handle insert, update or delete from startIndex to endIndex
 *     ...
 * }
 * </pre>
 *
 * In the above example you would have two blocks of changes of type
 * {@link #DELETE} instead of four simple changes.
 *
 * It is up to you, which iteration style you choose. Handling blocks of changes
 * might yield a better performance.
 *
 * <p>
 * While it is possible to change the style during one iteration, it is not
 * recommended, because you have to be careful to not miss some changes.
 * Refering to the last example above, the following code would skip the change
 * "D1":
 *
 * <pre>
 * ListEvent listChanges = ...// ("D0", "D0", "D2", "D2") vs. ("D0-1", "D2-3")
 * if (listChanges.next()) {
 *     final int type = listChanges.getType();
 *     final int index = listChanges.getIndex();
 *     // handle delete at index 0
 *     // ...
 *     while (listChanges.nextBlock()) {// move to next block starting at index 2, skipping change at index 1, which is part of the first block !
 *         final int type2 = listChanges.getType();
 *         final int startIndex = listChanges.getBlockStartIndex();
 *         final int endIndex = listChanges.getBlockEndIndex();
 *         // handle deletion from startIndex 2 to endIndex 3
 *         ...
 *     }
 * }
 * </pre>
 *
 * This kind of code is unusual, error-prone and should be avoided.
 *
 * <p>
 * There is a special kind of change, a {@link #isReordering() reordering}
 * ListEvent. It indicates a reordering of the list elements, for example
 * triggered by setting a new comparator on a {@link SortedList}.
 *
 * <p>
 * A reorder event cannot contain other regular changes in the current implementation.
 * Instead it provides a {@link #getReorderMap() reorder map}, which maps the
 * new indexes of the list elements to the old indexes. For details of the
 * mapping see the javadoc of method {@link #getReorderMap()}.
 *
 * <p>
 * {@link ListEventListener}s, that don't explicitly check for a reorder event,
 * will observe a deletion of all list elements with a subsequent re-insertion
 * instead.
 *
 * <p>
 * In the future, ListEvent will provide even more information about the list
 * changes to be more self-contained:
 * <ul>
 * <li>for deletes, it will provide the deleted element with
 * {@link #getOldValue()}
 * <li>for inserts, it will provide the inserted element with
 * {@link #getNewValue()}
 * <li>for updates, it will provide the old and new element with
 * {@link #getOldValue()} and {@link #getNewValue()}
 * </ul>
 *
 * The methods are currently marked as deprecated and should not be used yet,
 * because the implementation is a work in progress.
 *
 * <p>
 * Note, that providing the old and new elements has an impact on the
 * granularity of blocks of changes. For example, consider the clear operation
 * on a list:
 *
 * <pre>
 * EventList&lt;String&gt; list = GlazedLists.eventListOf(&quot;A&quot;, &quot;B&quot;, &quot;C&quot;);
 * list.clear();
 * </pre>
 *
 * Without considering the old and new elements, the ListEvent would consist of
 * one block: a deletion from index 0 to 2 ("D0-2"). With the feature of
 * providing the deleted elements, the ListEvent cannot consist of one block
 * anymore, because of the additional requirement, that the old element must
 * have the same value in one block:
 *
 * <pre>
 * ListEvent listChanges = ...
 * while (listChanges.nextBlock()) {
 *     final int type = listChanges.getType());
 *     final int startIndex = listChanges.getBlockStartIndex();
 *     final int endIndex = listChanges.getBlockEndIndex();
 *     final Object oldValue = listChanges.getOldValue();
 *     final Object newValue = listChanges.getNewValue();
 *     // handle insert, update or delete from startIndex to endIndex
 *     ...
 * }
 * </pre>
 *
 * So, a sequence of simple changes can only be grouped as block, when the type, as well
 * as the old and new value are equal.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public abstract class ListEvent<E> extends EventObject {

    /** different types of changes */
    public static final int DELETE = 0;
    public static final int UPDATE = 1;
    public static final int INSERT = 2;

    /** indicates a removed element whose value is unknown */
    public static final Object UNKNOWN_VALUE = new String("UNKNOWN VALUE");

    /** Returns a value indicating a removed element whose value is unknown. */
    @SuppressWarnings("unchecked")
    public static final <E> E unknownValue() {
        return (E) UNKNOWN_VALUE;
    }

    /** the list that has changed */
    protected EventList<E> sourceList;

    /**
     * Create a new list change sequence that uses the source master list
     * for the source of changes.
     */
    ListEvent(EventList<E> sourceList) {
        super(sourceList);

        // keep track of the origin sequence and list
        this.sourceList = sourceList;
    }

    /**
     * Create a bitwise copy of this {@link ListEvent}.
     */
    public abstract ListEvent<E> copy();

    /**
     * Resets this event's position to the previously-marked position. This should
     * be used for {@link TransformedList}s that require multiple-passes of the
     * {@link ListEvent} in order to process it.
     */
    public abstract void reset();

    /**
     * Increments the change sequence to view the next change. This will
     * return true if such a change exists and false when there is no
     * change to view.
     */
    public abstract boolean next();

    /**
     * Without incrementing the implicit iterator, this tests if there is another
     * change to view. The user will still need to call next() to view
     * such a change.
     */
    public abstract boolean hasNext();

    /**
     * Increments the change sequence to view the next change block.
     */
    public abstract boolean nextBlock();

    /**
     * Tests if this change is a complete reordering of the list.
     * <p>If it's a reordering, you can determine the changed element positions
     * with the help of the reorder map.
     *
     * @see #getReorderMap()
     */
    public abstract boolean isReordering();

    /**
     * Gets the reorder map of this list. Before calling this method,
     * you should check that {@link #isReordering()} returns <code>true</code>.
     *
     * <p>The size of the returned array is equal to the list size.
     * The array value at index i is the previous index (before the reordering)
     * of the list element at index i (after the reordering).
     * <p>
     * list before the reordering: "A", "B", "C"
     * <p>
     * list after the reordering: "C", "B", "A"
     * <p>
     * The reorder map of the corresponding ListEvent would look like:
     * map[0]=2;
     * map[1]=1;
     * map[2]=0
     *
     * @return an array of integers where the previous index of a value is
     *      stored at the current index of that value.
     * @throws IllegalStateException if this is not a reordering event
     *
     * @see #isReordering()
     */
    public abstract int[] getReorderMap();

    /**
     * Gets the current row index. If the block type is delete, this
     * will always return the startIndex of the current list change.
     */
    public abstract int getIndex();

    /**
     * Gets the first row of the current block of changes. Inclusive.
     */
    public abstract int getBlockStartIndex();

    /**
     * Gets the last row of the current block of changes. Inclusive.
     */
    public abstract int getBlockEndIndex();

    /**
     * Gets the type of the current change, which should be one of
     * ListEvent.INSERT, UPDATE, or DELETE.
     */
    public abstract int getType();

    /**
     * Gets the previous value for a deleted or updated element. If that data is
     * not available, this will return {@link ListEvent#UNKNOWN_VALUE}.
     */
    public abstract E getOldValue();

    /**
     * Gets the current value for an inserted or updated element. If that data is
     * not available, this will return {@link ListEvent#UNKNOWN_VALUE}.
     */
    public abstract E getNewValue();

    public abstract List<ObjectChange<E>> getBlockChanges();

    /**
     * Gets the number of blocks currently remaining in this atomic change.
     *
     * @deprecated this method depends on a particular implementation of
     *      how list events are stored internally, and this implementation has
     *      since changed.
     */
    @Deprecated
    public abstract int getBlocksRemaining();

    /**
     * Gets the List where this event originally occured.
     */
    public EventList<E> getSourceList() {
        return sourceList;
    }

    /**
     * Gets this event as a String. This simply iterates through all blocks
     * and concatenates them.
     */
    @Override
    public abstract String toString();
}