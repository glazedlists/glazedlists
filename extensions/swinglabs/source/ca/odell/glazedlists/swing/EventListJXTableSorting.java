/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.sort.TableColumnComparator;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.SortController;
import org.jdesktop.swingx.decorator.SortKey;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.table.TableColumnExt;

import java.util.*;

/**
 * Sort a {@link JXTable} using {@link ca.odell.glazedlists.SortedList}.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan="2"><font size="+2"><b>Extension: SwingX</b></font></td></tr>
 * <tr><td  colspan="2">This Glazed Lists <i>extension</i> requires the third party library <b>SwingX</b>.</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Tested Version:</b></td><td>CVS Head, September 19, 2006</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Home page:</b></td><td><a href="https://swingx.dev.java.net/">https://swingx.dev.java.net/</a></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>License:</b></td><td><a href="http://www.gnu.org/copyleft/lesser.html">LGPL</a></td></tr>
 * </td></tr>
 * </table>
 *
 * <p>To prepare a {@link JXTable} to be sorted using a {@link SortedList}:
 * <li>Create a {@link SortedList} and {@link EventTableModel} that depends
 * on that {@link SortedList}.
 * <li>Create a {@link JXTable} using the {@link EventTableModel} as its model.
 * <li>Run the {@link EventListJXTableSorting#install} method to bind the
 * {@link JXTable}'s headers to the {@link SortedList}'s {@link Comparator}.
 *
 * <p>Note that your {@link FilterPipeline} will be changed as a consequence of
 * using {@link EventListJXTableSorting}. This is due to API limitations in
 * SwingX which will hopefully be eventually resolved. For this reason it is
 * recommended that you not use {@link FilterPipeline} with this class. Instead,
 * consider using {@link FilterList} for filtering your {@link JXTable}.
 *
 * <p>Since this class works with the <a href="https://swingx.dev.java.net/">SwingX</a>
 * project, it is likely to change as SwingX continues to evolve and mature.
 * We will attempt to maintain source and binary compatibility as SwingX changes,
 * but it is highly recommended that you test your table sorting each time you
 * update your SwingX jar file.
 *
 * <p>The behaviour of multiple column sorting in JXTable is not particularly
 * flexible. For different configuration options, consider using a
 * {@link TableComparatorChooser} instead of {@link EventListJXTableSorting}.
 * To do this, you'll first need to disable the JXTable's built-in sorting
 * and restore the standard header renderer:
 * <pre><code>JXTable table = ...
 * table.setSortable(false);
 * table.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());</code></pre>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListJXTableSorting {

    /** the sorted list behind the table being sorted */
    private final SortedList sortedList;
    private final JXTable table;

    /** adapters between SortedList and JXTable */
    private final SortController sortController;
    private final EventListFilterPipeline filterPipeline;

    /** the original filter pipeline, used in {@link #uninstall} only */
    private final FilterPipeline originalFilterPipeline;

    /** whether to sort multiple columns at a time */
    private boolean multipleColumnSort = false;

    /**
     * Usually, constructors shouldn't supposed to have side-effects, but this one
     * changes the table's filter pipeline. Therefore we use this private
     * constructor and call through it from the {@link #install} method.
     */
    private EventListJXTableSorting(JXTable table, SortedList sortedList) {
        this.table = table;
        this.sortedList = sortedList;
        this.originalFilterPipeline = table.getFilters();

        this.sortController = new EventListSortController();
        this.filterPipeline = new EventListFilterPipeline();
        table.setFilters(filterPipeline);
    }

    /**
     * Install this {@link EventListJXTableSorting} to provide the sorting
     * behaviour for the specified {@link JXTable}.
     */
    public static EventListJXTableSorting install(JXTable table, SortedList sortedList) {
        return new EventListJXTableSorting(table, sortedList);
    }

    /**
     * Toggle whether clicked columns will be sorted after other sorted columns
     * (ie. break ties), or whether they will be come the exclusive sorted
     * column.
     *
     * <p>The default behaviour of JXTable multiple column sorting is that each
     * click appends a column to the sorting columns, reversing that column
     * if it's already sorting. Clearing the sorting columns is done by holding
     * <code>SHIFT</code> and clicking on a column header.
     *
     * @see org.jdesktop.swingx.JXTableHeader.SortGestureRecognizer to change
     * the clear/click behaviour
     * @see TableComparatorChooser for a more flexible approach to table sorting
     */
    public void setMultipleColumnSort(boolean multipleColumnSort) {
        this.multipleColumnSort = multipleColumnSort;
    }

    /**
     * Remove this {@link EventListJXTableSorting} from the {@link JXTable}.
     */
    public void uninstall() {
        table.setFilters(originalFilterPipeline);
    }

    /**
     * Unfortunately, the only way to provide a {@link SortController} for a
     * {@link JXTable} is to extend {@link FilterPipeline}. This is a significant
     * weakness in the SwingX API, and unfortunately they do not have the
     * resources to improve it for us. Perhaps sometime in the future we can
     * provide a patch to SwingX that makes installing a {@link SortController}
     * more elegant.
     */
    private class EventListFilterPipeline extends FilterPipeline {
        public SortController getSortController() {
            return sortController;
        }
    }

    /**
     * Implement {@link SortController} to provide sorting for {@link JXTable}.
     */
    private class EventListSortController implements SortController {

        /** the active sort columns */
        private final List<SortKey> sortKeys = new ArrayList<SortKey>(1);
        private final List<SortKey> sortKeysReadOnly = Collections.unmodifiableList(sortKeys);

        /** {@inheritDoc} */
        public void toggleSortOrder(int columnIndex) {
            toggleSortOrder(columnIndex, GlazedLists.comparableComparator());
        }

        /** {@inheritDoc} */
        public void toggleSortOrder(int columnIndex, Comparator comparator) {
            List<SortKey> newSortKeys = new ArrayList<SortKey>(getSortKeys());

            // see if we're already sorting with this column
            SortOrder columnSortOrder = SortOrder.ASCENDING;
            for(Iterator<? extends SortKey> s = newSortKeys.iterator(); s.hasNext(); ) {
                SortKey sortKey = (SortKey)s.next();
                if(sortKey.getSortOrder() == SortOrder.UNSORTED) continue;
                if(sortKey.getColumn() == columnIndex) {
                    columnSortOrder = (sortKey.getSortOrder() == SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
                    s.remove();
                    break;
                }
            }

            // prepare the new sort order
            if(!multipleColumnSort) newSortKeys.clear();
            newSortKeys.add(new SortKey(columnSortOrder, columnIndex));

            // kick off the sort
            setSortKeys(newSortKeys);
        }

        /** {@inheritDoc} */
        public void setSortKeys(List<? extends SortKey> sortKeys) {
            if(sortKeys == sortKeysReadOnly) return;
            if(sortKeys == null) sortKeys = Collections.emptyList();

            this.sortKeys.clear();
            this.sortKeys.addAll(sortKeys);

            // rebuild the SortedList's comparator
            List<Comparator> comparators = new ArrayList<Comparator>(this.sortKeys.size());
            for(int k = 0; k < this.sortKeys.size(); k++) {
                SortKey sortKey = (SortKey)this.sortKeys.get(k);
                if(sortKey.getSortOrder() == SortOrder.UNSORTED) continue;

                Comparator comparator = getComparator(sortKey.getColumn());
                if(sortKey.getSortOrder() == SortOrder.DESCENDING) comparator = GlazedLists.reverseComparator(comparator);

                comparators.add(comparator);
            }

            // figure out the final comparator
            final Comparator comparator;
            if(comparators.isEmpty()) {
                comparator = null;
            } else if(comparators.size() == 1) {
                comparator = comparators.get(0);
            } else {
                comparator = GlazedLists.chainComparators((List)comparators);
            }

            // apply this comparator to the sortedlist
            sortedList.getReadWriteLock().writeLock().lock();
            try {
                sortedList.setComparator(comparator);
            } finally {
                sortedList.getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * We need to fix this implementation so it looks into the {@link JXTable}'s
         * {@link TableColumnExt} to find the appropriate column comparator. Failing
         * that, it could look for an {@link AdvancedTableFormat} and the column's
         * Comparator.
         */
        private Comparator getComparator(int modelIndex) {
            EventTableModel tableModel = (EventTableModel)table.getModel();
            TableFormat tableFormat = tableModel.getTableFormat();
            return new TableColumnComparator(tableFormat, modelIndex);
        }

        /** {@inheritDoc} */
        public List<? extends SortKey> getSortKeys() {
            return sortKeysReadOnly;
        }

        /** {@inheritDoc} */
        public SortOrder getSortOrder(int columnIndex) {
            for(SortKey s : sortKeys) {
                if(s.getColumn() == columnIndex) return s.getSortOrder();
            }
            return SortOrder.UNSORTED;
        }
    }
}