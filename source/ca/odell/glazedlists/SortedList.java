/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.ListEvent;

import java.util.*;

/**
 * An {@link EventList} that shows its source {@link EventList} in sorted order.
 *
 * <p>The sorting strategy is specified with a {@link Comparator}. If no
 * {@link Comparator} is specified, all of the elements of the source {@link EventList}
 * must implement {@link Comparable}.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), change comparator O(N log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>72 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SortedList<E> extends TransformedList<E,E> {

    private final SortingStrategy<E> sortingStrategy;

    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}.
     * Because this constructor takes no {@link Comparator} argument, all
     * elements in the specified {@link EventList} must implement {@link Comparable}
     * or a {@link ClassCastException} will be thrown.
     */
    public SortedList(EventList<E> source) {
        this(source, (Comparator<E>)GlazedLists.comparableComparator());
    }

    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}
     * using the specified {@link Comparator} to determine sort order. If the
     * specified {@link Comparator} is <tt>null</tt>, then this list will be
     * unsorted.
     */
    public SortedList(EventList<E> source, Comparator<E> comparator) {
        super(source);

        this.sortingStrategy = new ActiveSorting<E>(updates, source);
        setComparator(comparator);

        source.addListEventListener(this);
    }

    /**
     * Creates a {@link SortedList} that sorts passively, only on
     * explicit calls to {@link #setComparator(Comparator)}
     */
    public SortedList(EventList<E> source, boolean passive) {
        super(source);

        this.sortingStrategy = new PassiveSorting<E>(updates, source);

        source.addListEventListener(this);
    }


    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        sortingStrategy.listChanged(listChanges);
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return sortingStrategy.getSourceIndex(mutationIndex);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /**
     * Gets the {@link Comparator} that is being used to sort this list.
     *
     * @return the {@link Comparator} in use, or <tt>null</tt> if this list is
     *      currently unsorted. If this is an {@link ca.odell.glazedlists.EventList} of {@link Comparable}
     *      elements in natural order, then a {@link ca.odell.glazedlists.impl.sort.ComparableComparator} will
     *      be returned.
     */
    public Comparator<E> getComparator() {
        return sortingStrategy.getComparator();
    }

    /**
     * Set the {@link Comparator} in use in this {@link ca.odell.glazedlists.EventList}. This will
     * sort the {@link ca.odell.glazedlists.EventList} into a new order.
     *
     * <p>Performance Note: sorting will take <code>O(N * Log N)</code> time.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link ca.odell.glazedlists.EventList} for an example
     * of thread safe code.
     *
     * @param comparator the {@link Comparator} to specify how to sort the list. If
     *      the source {@link ca.odell.glazedlists.EventList} elements implement {@link Comparable},
     *      you may use a {@link ca.odell.glazedlists.impl.sort.ComparableComparator} to sort them in their
     *      natural order. You may also specify <code>null</code> to put this
     *      {@link SortedList} in unsorted order.
     */
    public void setComparator(Comparator<E> comparator) {
        sortingStrategy.setComparator(comparator);
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        if(sortingStrategy instanceof AdvancedSortingStrategy) {
            return ((AdvancedSortingStrategy)sortingStrategy).indexOf(object);
        }
        return super.indexOf(object);
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        if(sortingStrategy instanceof AdvancedSortingStrategy) {
            return ((AdvancedSortingStrategy)sortingStrategy).lastIndexOf(object);
        }
        return super.lastIndexOf(object);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or the index where that element would be in the list if it were
     * inserted.
     *
     * @return the index in this list of the first occurrence of the specified
     *      element, or the index where that element would be in the list if it
     *      were inserted. This will return a value in <tt>[0, size()]</tt>,
     *      inclusive.
     */
    public int indexOfSimulated(Object object) {
        if(sortingStrategy instanceof AdvancedSortingStrategy) {
            return ((AdvancedSortingStrategy)sortingStrategy).indexOfSimulated(object);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /** {@inheritDoc} */
    public Iterator<E> iterator() {
        if(sortingStrategy instanceof AdvancedSortingStrategy) {
            return ((AdvancedSortingStrategy)sortingStrategy).iterator();
        }
        return super.iterator();
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        return indexOf(object) != -1;
    }

    /**
     * We outsource sorting to another class which provides the actual
     * index mapping and event firing service. This allows us to use
     * two completely different implementations with only one external
     * interface.
     */
    interface SortingStrategy<E> {
        void listChanged(ListEvent<E> listChanges);
        void setComparator(Comparator<E> comparator);
        Comparator<E> getComparator();
        int getSourceIndex(int mutationIndex);
    }

    /**
     * SortedLists that exploit the sorted order can provide additional
     * methods.
     */
    interface AdvancedSortingStrategy<E> extends SortingStrategy<E> {
        public int indexOf(Object object);
        public int lastIndexOf(Object object);
        public int indexOfSimulated(Object object);
        public Iterator<E> iterator();
   }
}