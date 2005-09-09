/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

// Primary GlazedList package
import ca.odell.glazedlists.*;
// To track clicks
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;
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

    /** this regular expression for parsing the string representation of a column */
    private static final Pattern FROM_STRING_PATTERN = Pattern.compile("^\\s*(\\d+)(\\s+comparator\\s+(\\d+))?(\\s+(reversed))?\\s*$", Pattern.CASE_INSENSITIVE);

    /** the sorted list to choose the comparators for */
    protected SortedList<E> sortedList;

    /** the columns to sort over */
    private TableFormat<E> tableFormat;

    /** the potentially foreign comparator associated with the sorted list */
    protected Comparator<E> sortedListComparator = null;

    /** the columns and their click counts in indexed order */
    protected List<ColumnClickTracker> columnClickTrackers;

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
        // build the column click trackers
        final int columnCount = tableFormat.getColumnCount();

        columnClickTrackers = new ArrayList<ColumnClickTracker>(columnCount);
        for(int i = 0; i < columnCount; i++) {
            columnClickTrackers.add(new ColumnClickTracker(tableFormat, i));
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
        return columnClickTrackers.get(column).getComparators();
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
        return columnClickTrackers.get(column).getComparatorIndex();
    }

    /**
     * Gets whether the comparator in use for the specified column is reverse.
     */
    public boolean isColumnReverse(int column) {
        return columnClickTrackers.get(column).isReverse();
    }

    /**
     * Append the comparator specified by the column, comparator index and reverse
     * parameters to the end of the sequence of comparators this
     * {@link AbstractTableComparatorChooser} is sorting the {@link SortedList}
     * by.
     *
     * <p><i>Append</i> implies that if this {@link AbstractTableComparatorChooser}
     * is already sorting that list by another column, this comparator will only
     * be used to break ties from that {@link Comparator}. If the table is already
     * sorting by the specified column, it will be silently discarded.
     *
     * <p>Suppose we're currently not sorting the table, this method will cause
     * the table to be sorted by the column specified. If we are sorting the table
     * by some column c, this will sort by that column first and the column
     * specified here second.
     *
     * <p>If this {@link AbstractTableComparatorChooser} doesn't support multiple
     * column sort, this will replace the current {@link Comparator} rather than
     * appending to it.
     *
     * @param column the column to sort by
     * @param comparatorIndex the comparator to use, specify <code>0</code> for the
     *      default comparator.
     * @param reverse whether to reverse the specified comparator.
     */
    public void appendComparator(int column, int comparatorIndex, boolean reverse) {
        appendComparator(column, comparatorIndex, reverse, true);
    }
    private void appendComparator(int column, int comparatorIndex, boolean reverse, boolean rebuildComparator) {
        if(column > columnClickTrackers.size()) throw new IllegalArgumentException("invalid column " + column + ", must be in range 0, " + columnClickTrackers.size());
        if(comparatorIndex >= getComparatorsForColumn(column).size()) throw new IllegalArgumentException("invalid comparator index " + comparatorIndex + ", must be in range 0, " + getComparatorsForColumn(column).size());
        if(recentlyClickedColumns.contains(columnClickTrackers.get(column))) return;

        // clear the previous comparator if sorting only allows a single column
        if(!multipleColumnSort) {
            clearComparator(false);
        }

        // add clicks to the specified column
        columnClickTrackers.get(column).setComparatorIndex(comparatorIndex);
        columnClickTrackers.get(column).setReverse(reverse);

        // rebuild the clicked column list
        if(primaryColumn == -1) primaryColumn = column;
        recentlyClickedColumns.add(columnClickTrackers.get(column));
        if(rebuildComparator) rebuildComparator();
    }

    /**
     * Clear all sorting state and set the {@link SortedList} to use its
     * natural order.
     */
    public void clearComparator() {
        clearComparator(true);
    }
    private void clearComparator(boolean rebuildComparator) {
        // clear the click counts
        for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
            ColumnClickTracker columnClickTracker = i.next();
            columnClickTracker.resetClickCount();
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();
        if(rebuildComparator) rebuildComparator();
    }

    /**
     * Handle a column being clicked by sorting that column.
     */
    protected void columnClicked(int column, int clicks) {
        ColumnClickTracker currentTracker = columnClickTrackers.get(column);
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
        final Comparator<E> rebuiltComparator;

        // build a new comparator
        if(recentlyClickedColumns.isEmpty()) {
            rebuiltComparator = null;
        } else {
            List<Comparator<E>> comparators = new ArrayList<Comparator<E>>(recentlyClickedColumns.size());
            for(Iterator<ColumnClickTracker> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                ColumnClickTracker columnClickTracker = i.next();
                Comparator<E> comparator = columnClickTracker.getComparator();
                comparators.add(comparator);
            }

            rebuiltComparator = (Comparator<E>)GlazedLists.chainComparators(comparators);
        }

        // select the new comparator
        sortedList.getReadWriteLock().writeLock().lock();
        try {
            sortedListComparator = rebuiltComparator;
            sortedList.setComparator(rebuiltComparator);
        } finally {
            sortedList.getReadWriteLock().writeLock().unlock();
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
        for(int c = 0; c < columnClickTrackers.size(); c++) {
            columnClickTrackers.get(c).resetClickCount();
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
            for(int c = 0; c < columnClickTrackers.size(); c++) {
                if(recentlyClickedColumns.contains(columnClickTrackers.get(c))) {
                    continue;
                }
                int comparatorIndex = columnClickTrackers.get(c).getComparators().indexOf(comparator);
                if(comparatorIndex != -1) {
                    final ColumnClickTracker columnClickTracker = columnClickTrackers.get(c);
                    columnClickTracker.setComparatorIndex(comparatorIndex);
                    columnClickTracker.setReverse(reverse);
                    if(recentlyClickedColumns.isEmpty()) primaryColumn = c;
                    recentlyClickedColumns.add(columnClickTracker);
                    if(!multipleColumnSort) break walkThroughComparators;
                }
            }
        }
    }

    /**
     * Gets the sorting style currently applied to the specified column.
     */
    protected int getSortingStyle(int column) {
        return columnClickTrackers.get(column).getSortingStyle();
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

    /**
     * Encode the current sorting state as a {@link String}. This specially formatted
     * {@link String} is ideal for persistence using any preferences API. The
     * state of this {@link AbstractTableComparatorChooser} can be restored
     * by passing the return value of this method to {@link #fromString(String)}.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        List sortingColumns = getSortingColumns();
        for(int c = 0; c < sortingColumns.size(); c++) {
            int columnIndex = ((Integer)sortingColumns.get(c)).intValue();

            // add a comma for every column but the first
            if(c != 0) result.append(", ");

            // write the column index
            result.append(columnIndex);

            // write the comparator index
            int comparatorIndex = getColumnComparatorIndex(columnIndex);
            if(comparatorIndex != 0) {
                result.append(" comparator ");
                result.append(comparatorIndex);
            }

            // write reversed
            if(isColumnReverse(columnIndex)) {
                result.append(" reversed");
            }
        }
        return result.toString();
    }

    /**
     * <p>This class is capable of representing its own state with a String, to
     * persist sorting state externally. The format uses zero or more column specifications,
     * separated by commas. Here are some valid examples:
     *
     * <table border><tr><th>String Representation</th><th>Description</th></tr>
     * <tr><td><code>"3"</code></td><td>Sort using the column at index 3, using that column's first comparator, in forward order</td></tr>
     * <tr><td><code>"3 reversed"</code></td><td>Sort using the column at index 3, using that column's first comparator, in reverse order</td></tr>
     * <tr><td><code>"3, 1"</code></td><td>Sort using the column at index 3, using that column's first comparator, in forward order<br>
     *                                     <i>then by</i><br> the column at index 1, using that column's first comparator, in forward order.</td></tr>
     * <tr><td><code>"3 comparator 2"</code></td><td>Sort using the column at index 3, using that column's comparator at index 2, in forward order</td></tr>
     * <tr><td><code>"3 comparator 2 reversed"</code></td><td>Sort using the column at index 3, using that column's comparator at index 2, in reverse order</td></tr>
     * <tr><td><code>"3 reversed, 1 comparator 2, 5 comparator 1 reversed, 0"</code></td><td>Sort using the column at index 3, using that column's first comparator, in reverse order<br>
     *                                     <i>then by</i><br> the column at index 1, using that column's comparator at index 2, in forward order<br>
     *                                     <i>then by</i><br> the column at index 5, using that column's comparator at index 1, in reverse order<br>
     *                                     <i>then by</i><br> the column at index 0, using that column's first comparator, in forward order.</td></tr>
     * </table>
     *
     * <p>More formally, the grammar for this String representation is as follows:
     * <br><code>&lt;COLUMN&gt; = &lt;COLUMN INDEX&gt; (reversed)? (comparator &lt;COMPARATOR INDEX&gt;)?</code>
     * <br><code>&lt;COMPARATOR SPEC&gt; = ( &lt;COLUMN&gt; (, &lt;COLUMN&gt;)* )?</code>
     */
    public void fromString(String stringEncoded) {
        clearComparator(false);

        // parse each column part in sequence using regex groups
        String[] parts = stringEncoded.split(",");
        for(int p = 0; p < parts.length; p++) {
            // skip empty strings
            if(parts[p].trim().length() == 0) continue;

            Matcher matcher = FROM_STRING_PATTERN.matcher(parts[p]);

            if(!matcher.find())
                throw new IllegalArgumentException("Failed to parse column spec, \"" + parts[p] + "\"");

            int columnIndex = Integer.parseInt(matcher.group(1));
            int comparatorIndex = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
            boolean reversedComparator = matcher.group(5) != null;

            // bail on invalid data
            if(columnIndex >= tableFormat.getColumnCount()) continue;
            if(comparatorIndex >= getComparatorsForColumn(columnIndex).size()) continue;

            // add this comparator in sequence
            appendComparator(columnIndex, comparatorIndex, reversedComparator, false);
        }

        rebuildComparator();
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
    private final class ColumnClickTracker {

        /** the column for this comparator */
        private int column = 0;
        /** the number of repeated clicks on this column header */
        private int clickCount = 0;
        /** the sequence of comparators for this column */
        private List<Comparator<E>> comparators = new ArrayList<Comparator<E>>();

        /**
         * Creates a new ColumnClickTracker for the specified column.
         */
        public ColumnClickTracker(TableFormat<E> tableFormat, int column) {
            this.column = column;

            // add the preferred comparator for AdvancedTableFormat
            if(tableFormat instanceof AdvancedTableFormat) {
                AdvancedTableFormat advancedTableFormat = (AdvancedTableFormat)tableFormat;
                Comparator columnComparator = advancedTableFormat.getColumnComparator(column);
                if(columnComparator != null) comparators.add(new TableColumnComparator<E>(tableFormat, column, columnComparator));
            // otherwise just add the default comparator
            } else {
                comparators.add(new TableColumnComparator<E>(tableFormat, column));
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
        public Comparator<E> getComparator() {
            Comparator<E> comparator = comparators.get(getComparatorIndex());
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