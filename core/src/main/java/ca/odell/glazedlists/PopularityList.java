/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.Comparator;

/**
 * An {@link EventList} that shows the unique elements from its source
 * {@link EventList} ordered by the frequency of their appearance.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See
 * {@link EventList} for an example.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>196 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=104">104</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class PopularityList<E> extends TransformedList<E, E> {

    /** the list of distinct elements */
    private UniqueList<E> uniqueList;

    /**
     * Creates a new {@link PopularityList} that provides frequency-ranking
     * for the specified {@link EventList}. All elements of the source {@link EventList}
     * must implement {@link Comparable}.
     */
    public static <E extends Comparable<? super E>> PopularityList<E> create(EventList<E> source) {
        return new PopularityList<>(UniqueList.create(source));
    }

    /**
     * Creates a new {@link PopularityList} that provides frequency-ranking
     * for the specified {@link EventList}. All elements of the source {@link EventList}
     * must implement {@link Comparable}.
     * <p>Usage of factory method {@link #create(EventList)} is preferable.
     */
    public PopularityList(EventList<E> source) {
        this(new UniqueList<>(source));
    }

    /**
     * Creates a new {@link PopularityList} that provides frequency-ranking
     * for the specified {@link EventList}.
     *
     * @param uniqueComparator {@link Comparator} used to determine equality
     */
    public PopularityList(EventList<E> source, Comparator<E> uniqueComparator) {
        this(new UniqueList<>(source, uniqueComparator));
    }

    /**
     * Private constructor is used as a Java-language hack to allow us to save
     * a reference to the specified {@link UniqueList}.
     */
    private PopularityList(UniqueList<E> uniqueList) {
        super(new SortedList<>(uniqueList, new PopularityComparator<>(uniqueList)));
        this.uniqueList = uniqueList;

        // listen for changes to the source list
        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        SortedList sortedSource = (SortedList)source;
        super.dispose();
        sortedSource.dispose();
        uniqueList.dispose();
    }

    /**
     * Compares objects by their popularity.
     */
    private static class PopularityComparator<E> implements Comparator<E> {
        private UniqueList<E> target;
        public PopularityComparator(UniqueList<E> target) {
            this.target = target;
        }
        @Override
        public int compare(E a, E b) {
            int aCount = target.getCount(a);
            int bCount = target.getCount(b);
            return bCount - aCount;
        }
    }
}