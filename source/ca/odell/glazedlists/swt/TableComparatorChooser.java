/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// the Glazed Lists util and volatile packages for default comparators
import ca.odell.glazedlists.util.*;
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.sort.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// for keeping lists of comparators
import java.util.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.events.*;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a ListTable by clicking
 * on the table's headers. It requires that the ListTable has a SortedList as
 * a source as the sorting on that list is used.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @see <a href="http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet2.java?rev=HEAD">Snippet 2</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class TableComparatorChooser extends AbstractTableComparatorChooser {

    /** the table being sorted */
    private Table table;
    private EventTableViewer eventTableViewer;

    /** listeners to sort change events */
    private List sortListeners = new ArrayList();

    /**
     * Creates a new TableComparatorChooser that responds to clicks
     * on the specified table and uses them to sort the specified list.
     *
     * @param eventTableViewer the table viewer for the table to be sorted
     * @param sortedList the sorted list to update.
     * @param multipleColumnSort <code>true</code> to sort by multiple columns
     *      at a time, or <code>false</code> to sort by a single column. Although
     *      sorting by multiple columns is more powerful, the user interface is
     *      not as simple and this strategy should only be used where necessary.
     */
    public TableComparatorChooser(EventTableViewer eventTableViewer, SortedList sortedList, boolean multipleColumnSort) {
        this.eventTableViewer = eventTableViewer;
        this.table = eventTableViewer.getTable();
        this.sortedList = sortedList;
        this.multipleColumnSort = multipleColumnSort;

        // set up the column click listeners
        rebuildColumns();

        // listen for events on the specified table
        for(int c = 0; c < table.getColumnCount(); c++) {
            table.getColumn(c).addSelectionListener(new ColumnListener(c));
        }
    }

    /**
     * When the column model is changed, this resets the column clicks and
     * comparator list for each column.
     */
    private void rebuildColumns() {
        // build the column click managers
        columnClickTrackers = new ColumnClickTracker[table.getColumnCount()];
        for(int i = 0; i < columnClickTrackers.length; i++) {
            columnClickTrackers[i] = new ColumnClickTracker(eventTableViewer.getTableFormat(), i);
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();
    }

    /**
     * Registers the specified {@link Listener} to receive notification whenever
     * the {@link Table} is sorted by this {@link TableComparatorChooser}.
     */
    public void addSortListener(final Listener sortListener) {
        sortListeners.add(sortListener);
    }
    /**
     * Deregisters the specified {@link Listener} to no longer receive events.
     */
    public void removeSortActionListener(final Listener sortListener) {
        for(Iterator i = sortListeners.iterator(); i.hasNext(); ) {
            if(sortListener == i.next()) {
                i.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Cannot remove nonexistant listener " + sortListener);
    }

    class ColumnListener implements SelectionListener {
        private int column;
        public ColumnListener(int column) {
            this.column = column;
        }
        public void widgetSelected(SelectionEvent e) {
            columnClicked(column, 1);
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            // Do Nothing
        }
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    protected final void rebuildComparator() {
        // build a new comparator
        if(!recentlyClickedColumns.isEmpty()) {
            List comparators = new ArrayList();
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                Comparator comparator = columnClickTracker.getComparator();
                comparators.add(comparator);
            }
            ComparatorChain comparatorChain = (ComparatorChain)ComparatorFactory.chain(comparators);

            // select the new comparator
            sortedList.getReadWriteLock().writeLock().lock();
            try {
                sortedListComparator = comparatorChain;
                sortedList.setComparator(comparatorChain);
            } finally {
                sortedList.getReadWriteLock().writeLock().unlock();
            }
        }

        // notify interested listeners that the sorting has changed
        Event sortEvent = new Event();
        sortEvent.widget = table;
        for(Iterator i = sortListeners.iterator(); i.hasNext(); ) {
            Listener listener = (Listener)i.next();
            listener.handleEvent(sortEvent);
        }
    }

    /**
     * Gets the sorting style currently applied to the specified column.
     */
    protected final int getSortingStyle(int column) {
        return columnClickTrackers[column].getSortingStyle();
    }

    /**
     * Creates a {@link Comparator} that can compare list elements
     * given a {@link Comparator} that can compare column values for the specified
     * column. This returns a {@link Comparator} that extracts the table values for
     * the specified column and then delegates the actual comparison to the specified
     * comparator.
     */
    public Comparator createComparatorForElement(Comparator comparatorForColumn, int column) {
        return new TableColumnComparator(eventTableViewer.getTableFormat(), column, comparatorForColumn);
    }
}
