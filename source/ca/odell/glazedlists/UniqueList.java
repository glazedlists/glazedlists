/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that shows the unique elements from its source
 * {@link EventList}. For example, the source list {A, A, B, C, C, C, D} would
 * be simplified to {A, B, C, D} by this UniqueList.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class breaks
 * the contract required by {@link List}. See {@link EventList} for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author James Lemieux
 */
public final class UniqueList<E> extends TransformedList<E,E> {

    private final GroupingList<E> groupingList;

    private final Comparator<E> comparator;

    /**
     * Creates a {@link UniqueList} that determines uniqueness via the
     * {@link Comparable} interface. All elements of the source {@link EventList}
     * must implement {@link Comparable}.
     *
     * @param source the {@link EventList} containing duplicates to remove
     */
    public UniqueList(EventList<E> source) {
        this(source, (Comparator<E>) GlazedLists.comparableComparator());
    }

    /**
     * Creates a {@link UniqueList} that determines uniqueness using the
     * specified {@link Comparator}.
     *
     * @param source the {@link EventList} containing duplicates to remove
     * @param comparator the {@link Comparator} used to determine equality
     */
    public UniqueList(EventList<E> source, Comparator<E> comparator) {
        this(new GroupingList<E>(source, comparator), comparator);
    }

    /**
     * A private constructor which allows us a chance to store a reference to
     * the {@link GroupingList} we install. UniqueList is largely implemented
     * through the use of GroupingList and FunctionList. After this constructor
     * completes, the EventList pipeline looks like this:
     *
     * UniqueList -> FunctionList -> GroupingList -> source EventList...
     *
     * where the GroupingList groups the values into GroupLists and the
     * FunctionList simply chooses the first element of each GroupList as the
     * new representation of that GroupList.
     *
     * UniqueList exposes a few extra querying methods like {@link #getCount(int)}
     * and {@link #getAll(int)} which require us to keep a reference to the
     * GroupingList. This private constructor allows us to retain a handle to
     * the GroupingList after using it in a call to the super constructor.
     *
     * @param groupingList the GroupingList that is installed around the
     *      original source EventList
     */
    private UniqueList(GroupingList<E> groupingList, Comparator<E> comparator) {
        super(new FunctionList<E, List<E>>(groupingList, new ListElementZeroFunction<E>(), new WrapInListFunction<E>()));

        this.groupingList = groupingList;
        this.comparator = comparator;

        source.addListEventListener(this);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * <code>element</code>, or -1 if this list does not contain this
     * <code>element</code>. More formally, returns the lowest index <tt>i</tt>
     * such that <tt>uniqueListComparator.compare(get(i), element) == 0</tt>,
     * or -1 if there is no such index.
     *
     * <p>Note: This is a departure from the contract for {@link List#indexOf}
     * since it does not guarantee that <tt>element.equals(get(i))</tt> where i
     * is a positive index returned from this method.
     *
     * @param element the element to search for.
     * @return the index in this list of the first occurrence of the specified
     *         element, or -1 if this list does not contain this element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list
     */
    public int indexOf(Object element) {
        final int index = Collections.binarySearch(source, (E) element, this.comparator);

        // if the element is not found (index is negative) then return -1 to indicate the list does not contain it
        return index < 0 ? -1 : index;
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.forwardEvent(listChanges);
    }

    /**
     * Returns the number of duplicates of the value found at the specified index.
     */
    public int getCount(int index) {
        return this.getAll(index).size();
    }

    /**
     * Returns the number of duplicates of the specified value.
     */
    public int getCount(E value) {
        final int index = this.indexOf(value);
        return index == -1 ? 0 : this.getCount(index);
    }

    /**
     * Returns a List of all original elements represented by the value at the
     * given <code>index</code> within this {@link UniqueList}.
     */
    public List<E> getAll(int index) {
        return (List<E>)this.groupingList.get(index);
    }

    /**
     * Returns a List of all original elements represented by the given
     * <code>value</code> within this {@link UniqueList}.
     */
    public List<E> getAll(E value) {
        final int index = this.indexOf(value);
        return index == -1 ? (List<E>) Collections.EMPTY_LIST : this.getAll(index);
    }

    /**
     * This Function maps each List produced by the source GroupingList to its
     * first element.
     */
    private static class ListElementZeroFunction<A> implements FunctionList.Function<List<A>, A> {
        public A evaluate(List<A> value) {
            return value.get(0);
        }
    }

    /**
     * This Function creates a single element List from the given value.
     */
    private static class WrapInListFunction<B> implements FunctionList.Function<B, List<B>> {
        public List<B> evaluate(B value) {
            return Collections.singletonList(value);
        }
    }
}