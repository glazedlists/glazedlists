/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

// Primary GlazedList package
import ca.odell.glazedlists.*;
// To track clicks
import java.util.*;
// For Comparators
import ca.odell.glazedlists.impl.sort.*;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a table
 * widget by clicking on the table's headers. It requires that the table has a
 * SortedList as a source as the sorting on that list is used.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public abstract class AbstractTableComparatorChooser<E> {

    /** the sorted list to choose the comparators for */
    protected SortedList<E> sortedList;
    
    /** the columns to sort over */
    private TableFormat<E> tableFormat;

    /** the potentially foreign comparator associated with the sorted list */
    protected Comparator<E> sortedListComparator = null;

    /** the columns and their click counts in indexed order */
    protected ColumnClickTracker[] columnClickTrackers;

    /** the first comparator in the comparator chain */
    protected int primaryColumn = -1;

    /** a list that contains all ColumnClickTrackers with non-zero click counts in their visitation order */
    protected List<ColumnClickTracker> recentlyClickedColumns = new ArrayList<ColumnClickTracker>();

    /** whether to support sorting on single or multiple columns */
    protected boolean multipleColumnSort;

    /** the sorting style on a column is used for icon choosing */
    protected static final int COLUMN_UNSORTED = 0;
    protected static final int COLUMN_PRIMARY_SORTED = 1;
    protected static final int COLUMN_PRIMARY_SORTED_REVERSE = 2;
    protected static final int COLUMN_PRIMARY_SORTED_ALTERNATE = 3;
    protected static final int COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE = 4;
    protected static final int COLUMN_SECONDARY_SORTED = 5;
    protected static final int COLUMN_SECONDARY_SORTED_REVERSE = 6;
    protected static final int COLUMN_SECONDARY_SORTED_ALTERNATE = 7;
    protected static final int COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE = 8;

    /**
     * Create a {@link AbstractTableComparatorChooser} that sorts the specified
     * {@link SortedList} over the specified columns.
     */
    protected AbstractTableComparatorChooser(SortedList<E> sortedList, TableFormat<E> tableFormat, boolean multipleColumnSort) {
        this.sortedList = sortedList;
        this.multipleColumnSort = multipleColumnSort;
        this.setTableFormat(tableFormat);
    }

    /**
     * Adjusts the TableFormat this comparator chooser uses when selecting
     * comparators. Calling this method will clear any active sorting.
     */
    protected void setTableFormat(TableFormat<E> tableFormat) {
        this.tableFormat = tableFormat;
        // reinit the column click trackers
        rebuildColumns();
    }

    /**
     * When the column model is changed, this resets the column clicks and
     * comparator list for each column.
     */
    protected void rebuildColumns() {
//        final List<ColumnClickTracker> columnClickTrackerList = new ArrayList<ColumnClickTracker>(tableFormat.getColumnCount());
//        for(int i = 0; i < columnClickTrackerList.size(); i++) {
//            columnClickTrackerList.add(new ColumnClickTracker(tableFormat, i));
//        }
//
//        columnClickTrackers = columnClickTrackerList.toArray(new ColumnClickTracker[columnClickTrackerList.size()]);

        // build the column click trackers
        columnClickTrackers = new ColumnClickTracker[tableFormat.getColumnCount()];
        for(int i = 0; i < columnClickTrackers.length; i++) {
            columnClickTrackers[i] = new ColumnClickTracker(tableFormat, i, primaryColumn);
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

    /**
     * Get the columns that the TableComparatorChooser is sorting by.
     *
     * @return a List of Integers. The first Integer is the primary sorting column,
     *      the second is the secondary, etc. This list may be empty but never null.
     */
    public List<Integer> getSortingColumns() {
        final List<Integer> sortingColumns = new ArrayList<Integer>();
        for(int c = 0; c < recentlyClickedColumns.size(); c++) {
            ColumnClickTracker clickedColumn = recentlyClickedColumns.get(c);
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
        for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
            ColumnClickTracker columnClickTracker = i.next();
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

    /**
     * Handle a column being clicked by sorting that column.
     */
    protected void columnClicked(int column, int clicks) {
        ColumnClickTracker currentTracker = columnClickTrackers[column];
        if(currentTracker.getComparators().isEmpty()) return;

        // on a double click, clear the click counts
        if(clicks == 2) {
            for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = i.next();
                columnClickTracker.resetClickCount();
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        // if we're only sorting one column at a time, clear other columns
        } else if(!multipleColumnSort) {
            for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = i.next();
                if(columnClickTracker != currentTracker) {
                    columnClickTracker.resetClickCount();
                }
            }
            primaryColumn = -1;
            recentlyClickedColumns.clear();
        }

        // add a click to the newly clicked column if it has any comparators
        currentTracker.addClick();
        if(recentlyClickedColumns.isEmpty()) {
            recentlyClickedColumns.add(currentTracker);
            primaryColumn = column;
        } else if(!recentlyClickedColumns.contains(currentTracker)) {
            recentlyClickedColumns.add(currentTracker);
        }

        // apply our comparator changes to the sorted list
        rebuildComparator();
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    protected void rebuildComparator() {
        // build a new comparator
        if(!recentlyClickedColumns.isEmpty()) {
            List<Comparator<E>> comparators = new ArrayList<Comparator<E>>();
            for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = i.next();
                Comparator<E> comparator = columnClickTracker.getComparator();
                comparators.add(comparator);
            }
            ComparatorChain<E> comparatorChain = (ComparatorChain<E>)GlazedLists.chainComparators(comparators);

            // select the new comparator
            sortedList.getReadWriteLock().writeLock().lock();
            try {
                sortedListComparator = comparatorChain;
                sortedList.setComparator(comparatorChain);
            } finally {
                sortedList.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    /**
     * Examines the current {@link Comparator} of the SortedList and
     * adds icons to the table header renderers in response.
     *
     * <p>To do this, clicks are injected into each of the
     * corresponding <code>ColumnClickTracker</code>s.
     */
    protected void redetectComparator(Comparator<E> currentComparator) {
        sortedListComparator = currentComparator;

        // Clear the current click counts
        for(int c = 0; c < columnClickTrackers.length; c++) {
            columnClickTrackers[c].resetClickCount();
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();

        // Populate a list of Comparators
        List<Comparator<E>> comparatorsList = new ArrayList<Comparator<E>>();
        if(sortedListComparator == null) {
            // Do Nothing
        } else if(sortedListComparator instanceof ComparatorChain) {
            ComparatorChain<E> chain = (ComparatorChain<E>)sortedListComparator;
            comparatorsList.addAll(chain.getComparators());
        } else {
            comparatorsList.add(sortedListComparator);
        }

        // walk through the list of Comparators and assign click counts
        walkThroughComparators:
        for(Iterator<Comparator<E>> i = comparatorsList.iterator(); i.hasNext(); ) {
            // get the current comparator
            Comparator<E> comparator = i.next();
            boolean reverse = false;
            if(comparator instanceof ReverseComparator) {
                reverse = true;
                comparator = ((ReverseComparator<E>)comparator).getSourceComparator();
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
    }
    
    /**
     * Gets the sorting style currently applied to the specified column.
     */
    protected int getSortingStyle(int column) {
        return columnClickTrackers[column].getSortingStyle();
    }

    /**
     * Creates a {@link Comparator} that can compare list elements
     * given a {@link Comparator} that can compare column values for the specified
     * column. This returns a {@link Comparator} that extracts the table values for
     * the specified column and then delegates the actual comparison to the specified
     * comparator.
     */
    public Comparator createComparatorForElement(Comparator<E> comparatorForColumn, int column) {
        return new TableColumnComparator<E>(tableFormat, column, comparatorForColumn);
    }

    public void dispose() {
        // null out references to potentially long lived objects
        this.sortedList = null;
        this.tableFormat = null;
        this.sortedListComparator = null;
    }

    /**
     * A ColumnClickTracker monitors the clicks on a specified column
     * and provides access to the most appropriate comparator for that
     * column.
     */
    private static final class ColumnClickTracker {

        /** the column for this comparator */
        private final int primaryColumn;
        /** the column for this comparator */
        private int column = 0;
        /** the number of repeated clicks on this column header */
        private int clickCount = 0;
        /** the sequence of comparators for this column */
        private List<Comparator> comparators = new ArrayList<Comparator>();

        /**
         * Creates a new ColumnClickTracker for the specified column.
         */
        public ColumnClickTracker(TableFormat tableFormat, int column, int primaryColumn) {
            this.column = column;
            this.primaryColumn = primaryColumn;

            // add the preferred comparator for AdvancedTableFormat
            if(tableFormat instanceof AdvancedTableFormat) {
                AdvancedTableFormat advancedTableFormat = (AdvancedTableFormat)tableFormat;
                Comparator columnComparator = advancedTableFormat.getColumnComparator(column);
                if(columnComparator != null) comparators.add(new TableColumnComparator(tableFormat, column, columnComparator));
            // otherwise just add the default comparator
            } else {
                comparators.add(new TableColumnComparator(tableFormat, column));
            }
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
        public void setComparatorIndex(int comparatorIndex) {
            assert(comparatorIndex < comparators.size());
            boolean wasReverse = isReverse();
            clickCount = (comparatorIndex * 2) + 1;
            if(!wasReverse) clickCount = clickCount + 1;
        }

        /**
         * Gets the index of the comparator to use for this column.
         */
        public int getComparatorIndex() {
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
            Comparator comparator = comparators.get(getComparatorIndex());
            if(isReverse()) comparator = GlazedLists.reverseComparator(comparator);
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