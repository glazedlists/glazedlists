/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * An {@link EventList} that shows the unique elements from its source {@link EventList}
 * ordered by the frequency of their appearance.
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
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>196 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class PopularityList extends TransformedList {

    /** the list of distinct elements */
    private UniqueList uniqueList;

    /**
     * Creates a new {@link PopularityList} that provides frequency-ranking for the
     * specified {@link EventList}.
     *
     * @param uniqueComparator The {@link Comparator} used to determine equality.
     */
    public PopularityList(EventList source, Comparator uniqueComparator) {
        this(source, new UniqueList(source, uniqueComparator));
    }

    /**
     * Creates a new {@link PopularityList} that provides frequency-ranking for the
     * specified {@link EventList}.
     */
    public PopularityList(EventList source) {
        this(source, new UniqueList(source));
    }

    /**
     * Private constructor is used as a Java-language hack to allow us to save
     * a reference to the specified {@link UniqueList}.
     */
    private PopularityList(EventList source, UniqueList uniqueList) {
        super(new SortedList(uniqueList, new PopularityComparator(uniqueList)));
        this.uniqueList = uniqueList;
        uniqueList.setFireCountChangeEvents(true);

        // listen for changes to the source list
        ((SortedList)super.source).addListEventListener(this);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent listChanges) {
        updates.forwardEvent(listChanges);
    }

    /** {@inheritDoc} */
    public void dispose() {
        SortedList sortedSource = (SortedList)source;
        super.dispose();
        sortedSource.dispose();
        uniqueList.dispose();
    }

    /**
     * Compares objects by their popularity.
     */
    private static class PopularityComparator implements Comparator {
        private UniqueList target;
        public PopularityComparator(UniqueList target) {
            this.target = target;
        }
        public int compare(Object a, Object b) {
            int aCount = target.getCount(a);
            int bCount = target.getCount(b);
            return bCount - aCount;
        }
    }
}