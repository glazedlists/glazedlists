/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// the Glazed Lists util and volatile packages for default comparators
import ca.odell.glazedlists.util.*;
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.sort.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// for keeping lists of comparators
import java.util.*;
// for looking up icon files in jars
import java.net.URL;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;

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
public final class TableComparatorChooser {

    /** the table being sorted */
    private Table table;
    private EventTableViewer eventTableViewer;

    /** the sorted list to choose the comparators for */
    private SortedList sortedList;

    /** the potentially foreign comparator associated with the sorted list */
    private Comparator sortedListComparator = null;

    /** the columns and their click counts */
    private ColumnClickTracker[] columnClickTrackers;
    /** the first comparator in the comparator chain */
    private int primaryColumn = -1;
    /** an array that contains all columns with non-zero click counts */
    private ArrayList recentlyClickedColumns = new ArrayList();

    /** the sorting style on a column is used for icon choosing */
    private static final int COLUMN_UNSORTED = 0;
    private static final int COLUMN_PRIMARY_SORTED = 1;
    private static final int COLUMN_PRIMARY_SORTED_REVERSE = 2;
    private static final int COLUMN_PRIMARY_SORTED_ALTERNATE = 3;
    private static final int COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE = 4;
    private static final int COLUMN_SECONDARY_SORTED = 5;
    private static final int COLUMN_SECONDARY_SORTED_REVERSE = 6;
    private static final int COLUMN_SECONDARY_SORTED_ALTERNATE = 7;
    private static final int COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE = 8;

    /** whether to support sorting on single or multiple columns */
    private boolean multipleColumnSort;

    /** listeners to sort change events */
    private List sortListeners = new ArrayList();

    /**
     * Creates a new TableComparatorChooser that responds to clicks
     * on the specified table and uses them to sort the specified list.
     *
     * @param table the table with headers that can be clicked on.
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

        // get the table model from the table
        /*try {
            eventTableModel = (EventTableModel)table.getModel();
        } catch(ClassCastException e) {
            throw new IllegalArgumentException("Can not apply TableComparatorChooser to a table whose table model is not an EventTableModel");
        }*/

        // set up the column click listeners
        rebuildColumns();

        // set the table header
        //table.getTableHeader().setDefaultRenderer(new SortArrowHeaderRenderer());

        // listen for events on the specified table
        for(int c = 0; c < table.getColumnCount(); c++) {
            table.getColumn(c).addListener(SWT.Selection, new ColumnListener(c));
            table.getColumn(c).addSelectionListener(new ColumnListener(c));
            table.getColumn(c).addControlListener(new ColumnListener(c));
        }
        //table.setColumnSelectionAllowed(false);
        //table.getTableHeader().addMouseListener(this);
        //table.getModel().addTableModelListener(this);
    }

    /**
     * When the column model is changed, this resets the column clicks and
     * comparator list for each column.
     */
    private void rebuildColumns() {
        // build the column click managers
        columnClickTrackers = new ColumnClickTracker[table.getColumnCount()];
        for(int i = 0; i < columnClickTrackers.length; i++) {
            columnClickTrackers[i] = new ColumnClickTracker(i);
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();
    }


    /**
     * Gets the list of comparators for the specified column. The user is
     * free to add comparators to this list or clear the list if the specified
     * column cannot be sorted.
     */
    public List getComparatorsForColumn(int column) {
        return columnClickTrackers[column].getComparators();
    }

    /*
     * When the mouse is clicked, this selects the next comparator in
     * sequence for the specified table. This will re-sort the table
     * by a new criterea.
     *
     * This code is based on the Java Tutorial's TableSorter
     * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#sorting">The Java Tutorial</a>
     */
    /*public void mouseClicked(MouseEvent e) {
        TableColumnModel columnModel = table.getColumnModel();
        int viewColumn = columnModel.getColumnIndexAtX(e.getX());
        int column = table.convertColumnIndexToModel(viewColumn);
        int clicks = e.getClickCount();
        if(clicks >= 1 && column != -1) {
            columnClicked(column, clicks);
        }
    }*/

    /**
     * Get the columns that the TableComparatorChooser is sorting by.
     *
     * @return a List of Integers. The first Integer is the primary sorting column,
     *      the second is the secondary, etc. This list may be empty but never null.
     */
    public List getSortingColumns() {
        List sortingColumns = new ArrayList();
        for(int c = 0; c < recentlyClickedColumns.size(); c++) {
            ColumnClickTracker clickedColumn = (ColumnClickTracker)recentlyClickedColumns.get(c);
            sortingColumns.add(new Integer(clickedColumn.getColumn()));
        }
        return sortingColumns;
    }

    /**
     * Gets the index comparator in use for the specified column. This comparator
     * may be retrieved using {@link #getComparatorsForColumn(int)}.
     *
     * @return the comparator index for the specified column, or -1 if that column
     *      is not being used to sort.
     */
    public int getColumnComparatorIndex(int column) {
        return columnClickTrackers[column].getComparatorIndex();
    }

    /**
     * Gets whether the comparator in use for the specified column is reverse.
     */
    public boolean isColumnReverse(int column) {
        return columnClickTrackers[column].isReverse();
    }

    /*
     * When the number of columns changes in the table, we need to
     * clear the comparators and columns.
     */
    /*public void tableChanged(TableModelEvent event) {
        if(event.getFirstRow() == TableModelEvent.HEADER_ROW
        && event.getColumn() == TableModelEvent.ALL_COLUMNS) {
            rebuildColumns();
        }

        // if the comparator has changed
        ((InternalReadWriteLock)sortedList.getReadWriteLock()).internalLock().lock();
        try {
            Comparator currentComparator = sortedList.getComparator();
            if(currentComparator != sortedListComparator) {
                redetectComparator(currentComparator);
            }
        } finally {
            ((InternalReadWriteLock)sortedList.getReadWriteLock()).internalLock().unlock();
        }
    }*/

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

    /*
     * Examines the current {@link Comparator} of the SortedList and
     * adds icons to the table header renderers in response.
     *
     * <p>To do this, clicks are injected into each of the
     * corresponding <code>ColumnClickTracker</code>s.
     */
    /*private void redetectComparator(Comparator currentComparator) {
        sortedListComparator = currentComparator;

        // Clear the current click counts
        for(int c = 0; c < columnClickTrackers.length; c++) {
            columnClickTrackers[c].resetClickCount();
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();

        // Populate a list of Comparators
        List comparatorsList = new ArrayList();
        if(sortedListComparator == null) {
            // Do Nothing
        } else if(sortedListComparator instanceof ComparatorChain) {
            ComparatorChain chain = (ComparatorChain)sortedListComparator;
            comparatorsList.addAll(chain.getComparators());
        } else {
            comparatorsList.add(sortedListComparator);
        }

        // walk through the list of Comparators and assign click counts
        walkThroughComparators:
        for(Iterator i = comparatorsList.iterator(); i.hasNext(); ) {
            // get the current comparator
            Comparator comparator = (Comparator)i.next();
            boolean reverse = false;
            if(comparator instanceof ReverseComparator) {
                reverse = true;
                comparator = ((ReverseComparator)comparator).getSourceComparator();
            }

            // discover where to add clicks for this comparator
            for(int c = 0; c < columnClickTrackers.length; c++) {
                if(recentlyClickedColumns.contains(columnClickTrackers[c])) {
                    continue;
                }
                int comparatorIndex = columnClickTrackers[c].getComparators().indexOf(comparator);
                if(comparatorIndex != -1) {
                    columnClickTrackers[c].setComparatorIndex(comparatorIndex);
                    columnClickTrackers[c].setReverse(reverse);
                    if(recentlyClickedColumns.isEmpty()) primaryColumn = c;
                    recentlyClickedColumns.add(columnClickTrackers[c]);
                    if(!multipleColumnSort) break walkThroughComparators;
                }
            }
        }

        // force the table header to redraw itself
        table.getTableHeader().revalidate();
        table.getTableHeader().repaint();
    }*/

    /**
     * Set the table to use the specified comparator.
     *
     * @param column the column to sort by
     * @param comparatorIndex the comparator to use, specify <code>0</code> for the
     *      default comparator.
     * @param reverse whether to reverse the specified comparator.
     */
    public void chooseComparator(int column, int comparatorIndex, boolean reverse) {
        if(column > columnClickTrackers.length) throw new IllegalArgumentException("invalid column " + column + ", must be in range 0, " + columnClickTrackers.length);
        if(comparatorIndex > getComparatorsForColumn(column).size()) throw new IllegalArgumentException("invalid comparator index " + comparatorIndex + ", must be in range 0, " + getComparatorsForColumn(column).size());

        // clear the click counts
        for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
            ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
            columnClickTracker.resetClickCount();
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();

        // add clicks to the specified column
        ColumnClickTracker currentTracker = columnClickTrackers[column];
        currentTracker.setComparatorIndex(comparatorIndex);
        currentTracker.setReverse(reverse);

        // rebuild the clicked column list
        primaryColumn = column;
        recentlyClickedColumns.add(currentTracker);
        rebuildComparator();
    }

    class ColumnListener implements SelectionListener, Listener, ControlListener {
        private int column;
        public ColumnListener(int column) {
            this.column = column;
        }
        public void handleEvent(Event e) {
            System.out.println("column clicked, " + e);
        }
        public void widgetSelected(SelectionEvent e) {
            System.out.println("column clicked, " + e);
            columnClicked(column, 1);
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            System.out.println("default selected, " + e);
        }
        public void controlMoved(ControlEvent e) {
            System.out.println("control moved, " + e);
        }
        public void controlResized(ControlEvent e) {
            System.out.println("control resized, " + e);
        }
    }

    /**
     * Handle a column being clicked by sorting that column.
     */
    private void columnClicked(int column, int clicks) {
        ColumnClickTracker currentTracker = columnClickTrackers[column];

        // on a double click, clear the click counts
        if(clicks == 2) {
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                columnClickTracker.resetClickCount();
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        // if we're only sorting one column at a time, clear other columns
        } else if(!multipleColumnSort) {
            for(Iterator i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = (ColumnClickTracker)i.next();
                if(columnClickTracker != currentTracker) {
                    columnClickTracker.resetClickCount();
                }
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        }

        // add a click to the newly clicked column if it has any comparators
        if(!currentTracker.getComparators().isEmpty()) {
            currentTracker.addClick();
            if(recentlyClickedColumns.isEmpty()) {
                recentlyClickedColumns.add(currentTracker);
                primaryColumn = column;
            } else if(!recentlyClickedColumns.contains(currentTracker)) {
                recentlyClickedColumns.add(currentTracker);
            }
        }

        // apply our comparator changes to the sorted list
        rebuildComparator();
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    private void rebuildComparator() {
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

        // force the table header to redraw itself
        //table.getTableHeader().revalidate();
        //table.getTableHeader().repaint();

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
    private int getSortingStyle(int column) {
        //int modelColumn = table.convertColumnIndexToModel(column);
        return columnClickTrackers[column].getSortingStyle();
    }

    /**
     * A ColumnClickTracker monitors the clicks on a specified column
     * and provides access to the most appropriate comparator for that
     * column.
     */
    private class ColumnClickTracker {

        /** the column for this comparator */
        private int column = 0;
        /** the number of repeated clicks on this column header */
        private int clickCount = 0;
        /** the sequence of comparators for this column */
        private List comparators = new ArrayList();

        /**
         * Creates a new ColumnClickTracker for the specified column.
         */
        public ColumnClickTracker(int column) {
            this.column = column;
            // add a default comparator
            comparators.add(new TableColumnComparator(eventTableViewer.getTableFormat(), column));
        }

        /**
         * Adds a single click to this column.
         */
        public void addClick() {
            clickCount++;
        }

        /**
         * Resets the count of clicks on this column.
         */
        public void resetClickCount() {
            clickCount = 0;
        }

        /**
         * Gets the column for this ColumnComparator.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Sets the sort order to be reverse or not.
         */
        public void setReverse(boolean reverse) {
            if(isReverse() != reverse) {
                if(reverse) {
                    clickCount++;
                } else {
                    clickCount--;
                }
            }
        }

        /**
         * Returns true if this column is sorted in reverse order.
         */
        public boolean isReverse() {
            return (clickCount % 2 == 0);
        }

        /**
         * Gets the index of the comparator to use for this column.
         */
        private void setComparatorIndex(int comparatorIndex) {
            assert(comparatorIndex < comparators.size());
            boolean wasReverse = isReverse();
            clickCount = (comparatorIndex * 2) + 1;
            if(!wasReverse) clickCount = clickCount + 1;
        }

        /**
         * Gets the index of the comparator to use for this column.
         */
        private int getComparatorIndex() {
            if(comparators.size() == 0 || clickCount == 0) return -1;
            return ((clickCount-1) / 2) % comparators.size();
        }

        /**
         * Gets the list of comparators for this column.
         */
        public List getComparators() {
            return comparators;
        }

        /**
         * Gets the current best comparator to sort this column.
         */
        public Comparator getComparator() {
            Comparator comparator = (Comparator)comparators.get(getComparatorIndex());
            if(isReverse()) comparator = ComparatorFactory.reverse(comparator);
            return comparator;
        }

        /**
         * Gets the sorting style for this column.
         */
        public int getSortingStyle() {
            if(clickCount == 0) return COLUMN_UNSORTED;

            if(column == primaryColumn) {
                if(!isReverse()) {
                    if(getComparatorIndex() == 0) return COLUMN_PRIMARY_SORTED;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE;
                } else {
                    if(getComparatorIndex() == 0) return COLUMN_PRIMARY_SORTED_REVERSE;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE;
                }
            } else {
                if(!isReverse()) {
                    if(getComparatorIndex() == 0) return COLUMN_SECONDARY_SORTED;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE;
                } else {
                    if(getComparatorIndex() == 0) return COLUMN_SECONDARY_SORTED_REVERSE;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE;
                }
            }
        }
    }

}

/**
 * A comparator that sorts a table by the column that was clicked.
 */
class TableColumnComparator implements Comparator {

    /** the table format knows to map objects to their fields */
    private TableFormat tableFormat;

    /** the field of interest */
    private int column;

    /** comparison is delegated to a ComparableComparator */
    private static Comparator comparableComparator = ComparatorFactory.comparable();

    /**
     * Creates a new TableColumnComparator that sorts objects by the specified
     * column using the specified table format.
     */
    public TableColumnComparator(TableFormat tableFormat, int column) {
        this.column = column;
        this.tableFormat = tableFormat;
    }

    /**
     * Compares the two objects, returning a result based on how they compare.
     */
    public int compare(Object alpha, Object beta) {
        Object alphaField = tableFormat.getColumnValue(alpha, column);
        Object betaField = tableFormat.getColumnValue(beta, column);
        try {
            return comparableComparator.compare(alphaField, betaField);
        // throw a 'nicer' exception if the class does not implement Comparable
        } catch(ClassCastException e) {
            IllegalStateException illegalStateException = new IllegalStateException("TableComparatorChooser can not sort objects \"" + alphaField + "\", \"" + betaField + "\" that do not implement Comparable");
            illegalStateException.initCause(e);
            throw illegalStateException;
        }
    }

    /**
     * Test if this TableColumnComparator is equal to the other specified
     * TableColumnComparator.
     */
    public boolean equals(Object other) {
        if(!(other instanceof TableColumnComparator)) return false;

        TableColumnComparator otherTableColumnComparator = (TableColumnComparator)other;
        if(!otherTableColumnComparator.tableFormat.equals(tableFormat)) return false;
        if(otherTableColumnComparator.column != column) return false;

        return true;
    }
}
