/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.impl.Grouper;
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
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=27">27</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=34">34</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=35">35</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=45">45</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=46">46</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=55">55</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=58">58</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=114">114</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class UniqueList<E> extends TransformedList<E, E> {

    /** the grouping service manages collapsing out duplicates */
    private final Grouper<E> grouper;

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
        this(new SortedList<E>(source, comparator), comparator, null);
    }

    /**
     * A private constructor which allows us to use the {@link SortedList} as
     * the main decorated {@link EventList}.
     *
     * <p>The current implementation of {@link UniqueList} uses the {@link Grouper}
     * service to manage collapsing duplicates, which is more efficient than the
     * previous implementation that required a longer pipeline of {@link GroupingList}s.
     *
     * <p>UniqueList exposes a few extra querying methods like {@link #getCount(int)}
     * and {@link #getAll(int)} which require us to query the {@link Grouper}s
     * barcode, which retains state on groups.
     *
     * @param source a private {@link SortedList} whose {@link Comparator} never
     *      changes, this is used to keep track of uniqueness.
     * @param comparator the {@link Comparator} used to determine equality.
     * @param dummyParameter dummy parameter to differentiate between the different
     *      {@link GroupingList} constructors.
     */
    private UniqueList(SortedList<E> source, Comparator<E> comparator, Void dummyParameter) {
        super(source);

        // the grouper handles changes to the SortedList
        GrouperClient grouperClient = new GrouperClient();
        this.grouper = new Grouper<E>(source, grouperClient);

        source.addListEventListener(this);
    }

    /**
     * Handle changes to the grouper's groups.
     */
    private class GrouperClient implements Grouper.Client {
        public void groupChanged(int index, int groupIndex, int groupChangeType, boolean primary, int elementChangeType) {
            if(groupChangeType == ListEvent.INSERT) {
                updates.addInsert(groupIndex);
            } else if(groupChangeType == ListEvent.DELETE) {
                updates.addDelete(groupIndex);
            } else if(groupChangeType == ListEvent.UPDATE) {
                updates.addUpdate(groupIndex);
            } else {
                throw new IllegalStateException();
            }
        }
    }


    /** {@inheritDoc} */
    public int size() {
        return grouper.getBarcode().colourSize(Grouper.UNIQUE);
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int index) {
        if(index == size()) return source.size();
        return grouper.getBarcode().getIndex(index, Grouper.UNIQUE);
    }

    /**
     * Get the first element that's not a duplicate of the element at the specified
     * index. This is useful for things like {@link #getCount(int)} because we can
     * find the full range of a value quickly.
     */
    private int getEndIndex(int index) {
        if(index == (size() - 1)) return source.size();
        else return getSourceIndex(index + 1);
    }

    /** {@inheritDoc} */
    public E remove(int index) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot remove at " + index + " on list of size " + size());

        updates.beginEvent(true);

        // remember the first duplicate
        E result = get(index);

        // remove all duplicates at this index
        int startIndex = getSourceIndex(index);
        int endIndex = getEndIndex(index);
        ((SortedList)source).subList(startIndex, endIndex).clear();

        updates.commitEvent();

        return result;
    }

    /** {@inheritDoc} */
    public E set(int index, E value) {
        if(index < 0 || index >= size()) throw new IndexOutOfBoundsException("Cannot set at " + index + " on list of size " + size());

        updates.beginEvent(true);

        // remove all duplicates of this value first
        E result = remove(index);
        add(index, value);

        updates.commitEvent();

        return result;
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
        final int index = Collections.binarySearch(this, (E) element, ((SortedList)source).getComparator());

        // if the element is not found (index is negative) then return -1 to indicate the list does not contain it
        return index < 0 ? -1 : index;
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent(true);
        grouper.listChanged(listChanges);
        updates.commitEvent();
    }

    /**
     * Returns the number of duplicates of the value found at the specified index.
     */
    public int getCount(int index) {
        int startIndex = getSourceIndex(index);
        int endIndex = getEndIndex(index);
        return endIndex - startIndex;
    }

    /**
     * Returns the number of duplicates of the specified value.
     */
    public int getCount(E value) {
        final int index = this.indexOf(value);
        if(index == -1) return 0;
        else return getCount(index);
    }

    /**
     * Returns a List of all original elements represented by the value at the
     * given <code>index</code> within this {@link UniqueList}.
     */
    public List<E> getAll(int index) {
        int startIndex = getSourceIndex(index);
        int endIndex = getEndIndex(index);
        return new ArrayList<E>(source.subList(startIndex, endIndex));
    }

    /**
     * Returns a List of all original elements represented by the given
     * <code>value</code> within this {@link UniqueList}.
     */
    public List<E> getAll(E value) {
        final int index = this.indexOf(value);
        return index == -1 ? (List<E>) Collections.EMPTY_LIST : this.getAll(index);
    }

    /** {@inheritDoc} */
    public void dispose() {
        ((SortedList)source).dispose();
        super.dispose();
    }
}