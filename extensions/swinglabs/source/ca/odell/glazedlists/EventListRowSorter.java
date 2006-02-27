/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import org.jdesktop.swingx.RowSorter;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import java.util.*;

import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.impl.sort.TableColumnComparator;

/**
 * Sort {@link org.jdesktop.swingx.JXTable} using {@link SortedList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListRowSorter implements RowSorter {

    /** the table being sorted, used to find comparators */
    private final JXTable table;

    /** the sorted list behind the table being sorted */
    private final SortedList sortedList;

    /** the active sort columns */
    private final List<SortKey> sortKeys = new ArrayList<SortKey>(1);
    private final List<SortKey> sortKeysReadOnly = Collections.unmodifiableList(sortKeys);

    /** whom to notify when the sort order is changed */
    private List<RowSorterListener> listeners = new ArrayList(1);

    /**
     * Private constructor, instantiate instances via the static {@link #install}
     * method.
     */
    private EventListRowSorter(JXTable table, SortedList sortedList) {
        this.table = table;
        this.sortedList = sortedList;
    }

    /**
     * Install this {@link RowSorter} to provide the sorting behaviour
     * for a {@link JXTable}.
     */
    public static EventListRowSorter install(JXTable table, SortedList sortedList) {
        EventListRowSorter rowSorter = new EventListRowSorter(table, sortedList);
        table.setRowSorter(rowSorter);
        return rowSorter;
    }

    /** {@inheritDoc} */
    public void toggleSortOrder(int columnIndex) {
        List<SortKey> sortKeys = getSortKeys();

        // see if we're already sorting with this column
        SortKey columnSortKey = null;
        for(Iterator<SortKey> s = sortKeys.iterator(); s.hasNext(); ) {
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
    public void setSortKeys(List<? extends SortKey> list) {
        if(list == sortKeysReadOnly) return;

        this.sortKeys.clear();
        this.sortKeys.addAll(list);

        // rebuild the SortedList's comparator
        List<Comparator> comparators = new ArrayList<Comparator>(sortKeys.size());
        for(int k = 0; k < sortKeys.size(); k++) {
            SortKey sortKey = (SortKey)sortKeys.get(k);
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

        // notify the world that the sort order has changed
        EventObject sortEvent = new EventObject(table);
        for(RowSorterListener r : listeners) {
            r.sorterChanged(sortEvent);
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
    public List<SortKey> getSortKeys() {
        return sortKeysReadOnly;
    }

    /** {@inheritDoc} */
    public SortOrder getSortOrder(int columnIndex) {
        for(SortKey s : sortKeys) {
            if(s.getColumn() == columnIndex) return s.getSortOrder();
        }
        return SortOrder.UNSORTED;
    }

    /** {@inheritDoc} */
    public void addRowSorterListener(RowSorterListener rowSorterListener) {
        listeners.add(rowSorterListener);
    }
    /** {@inheritDoc} */
    public void removeRowSorterListener(RowSorterListener rowSorterListener) {
        listeners.remove(rowSorterListener);
    }
}