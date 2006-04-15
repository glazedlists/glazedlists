/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import org.jdesktop.swingx.decorator.SortController;
import org.jdesktop.swingx.decorator.SortKey;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import java.util.*;

import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.impl.sort.TableColumnComparator;

/**
 * Sort a {@link JXTable} using {@link SortedList}.
 *
 * <p>To prepare a {@link JXTable} to be sorted using a {@link SortedList}:
 *     <li>Create a {@link SortedList} and {@link EventTableModel} that depends
 *         on that {@link SortedList}.
 *     <li>Create a {@link JXTable} using the {@link EventTableModel} as its model.
 *     <li>Run the {@link EventListJXTableSorting#install} method to bind the
 *         {@link JXTable}'s headers to the {@link SortedList}'s {@link Comparator}.
 *
 * <p>Note that your {@link FilterPipeline} will be changed as a consequence of
 * using {@link EventListJXTableSorting}. This is due to API limitations in
 * SwingX which will hopefully be eventually resolved. For this reason it is
 * recommended that you not use {@link FilterPipeline} with this class. Instead,
 * consider using {@link FilterList} for filtering your {@link JXTable}.
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
            List<? extends SortKey> sortKeys = getSortKeys();

            // see if we're already sorting with this column
            SortKey columnSortKey = null;
            for(Iterator<? extends SortKey> s = sortKeys.iterator(); s.hasNext(); ) {
                SortKey sortKey = (SortKey)s.next();
                if(sortKey.getSortOrder() == SortOrder.UNSORTED) continue;
                if(sortKey.getColumn() == columnIndex) {
                    columnSortKey = sortKey;
                    break;
                }
            }

            // create the new sort key
            if(columnSortKey == null) {
                columnSortKey = new SortKey(SortOrder.ASCENDING, columnIndex);
            } else {
                SortOrder sortOrder = columnSortKey.getSortOrder() == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
                columnSortKey = new SortKey(sortOrder, columnIndex);
            }

            // prepare the new sort order
            setSortKeys(Collections.singletonList(columnSortKey));
        }

        /** {@inheritDoc} */
        public void setSortKeys(List<? extends SortKey> sortKeys) {
            if(sortKeys == sortKeysReadOnly) return;

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