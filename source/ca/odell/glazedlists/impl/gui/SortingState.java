/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.sort.ComparatorChain;
import ca.odell.glazedlists.impl.sort.ReverseComparator;
import ca.odell.glazedlists.impl.sort.TableColumnComparator;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Keep track of which columns are sorted and how. This is
 * largely independent of how that state is applied to a
 * <code>SortedList</code>, which is managed independently by
 * <code>TableComparatorChooser</code>.
 *
 * <p>Users must explicity call {@link #fireSortingChanged()} in order
 * to prepare a new Comparator for the target table.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SortingState {

    /** this regular expression for parsing the string representation of a column */
    private static final Pattern FROM_STRING_PATTERN = Pattern.compile("^\\s*column\\s+(\\d+)(\\s+comparator\\s+(\\d+))?(\\s+(reversed))?\\s*$", Pattern.CASE_INSENSITIVE);

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

    /** the columns and their click counts in indexed order */
    protected List<SortingColumn> sortingColumns;

    /** a list that contains all ColumnClickTrackers with non-zero click counts in their visitation order */
    protected List<SortingColumn> recentlyClickedColumns = new ArrayList<SortingColumn>(2);

    private final AbstractTableComparatorChooser tableComparatorChooser;

    /** whom to notify when the sorting state is chaged */
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public SortingState(AbstractTableComparatorChooser tableComparatorChooser) {
        this.tableComparatorChooser = tableComparatorChooser;
    }

    public AbstractTableComparatorChooser getTableComparatorChooser() {
        return tableComparatorChooser;
    }

    public void fireSortingChanged() {
        changeSupport.firePropertyChange("comparator", null, null);
    }
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public Comparator buildComparator() {
        // build a new comparator
        if(recentlyClickedColumns.isEmpty()) {
            return null;
        } else {
            List<Comparator<Object>> comparators = new ArrayList<Comparator<Object>>(recentlyClickedColumns.size());
            for(Iterator<SortingColumn> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                SortingColumn sortingColumn = i.next();
                Comparator comparator = sortingColumn.getComparator();
                if(comparator == null) throw new IllegalStateException();
                comparators.add(comparator);
            }

            return GlazedLists.chainComparators(comparators);
        }
    }

    /**
     * @return the indices of the columns currently being sorted.
     */
    public List<Integer> getSortingColumnIndexes() {
        final List<Integer> sortingColumns = new ArrayList<Integer>();
        final List<SortingState.SortingColumn> recentlyClickedColumns = getRecentlyClickedColumns();
        for(int c = 0; c < recentlyClickedColumns.size(); c++) {
            SortingState.SortingColumn clickedColumn = recentlyClickedColumns.get(c);
            sortingColumns.add(new Integer(clickedColumn.getColumn()));
        }
        return sortingColumns;
    }

    public void appendComparator(int column, int comparatorIndex, boolean reverse) {
        if(column > getColumns().size()) throw new IllegalArgumentException("invalid column " + column + ", must be in range 0, " + sortingColumns.size());
        if(comparatorIndex >= sortingColumns.get(column).getComparators().size()) throw new IllegalArgumentException("invalid comparator index " + comparatorIndex + ", must be in range 0, " + sortingColumns.get(column).getComparators().size());
        if(recentlyClickedColumns.contains(getColumns().get(column))) return;

        // add clicks to the specified column
        SortingColumn sortingColumn = sortingColumns.get(column);
        sortingColumn.setComparatorIndex(comparatorIndex);
        sortingColumn.setReverse(reverse);

        // rebuild the clicked column list
        recentlyClickedColumns.add(sortingColumn);
    }

    public void detectStateFromComparator(Comparator foreignComparator) {
        // Clear the current click counts
        clearComparators();

        // Populate a list of Comparators
        final List<Comparator> comparatorsList;
        if(foreignComparator == null) {
            comparatorsList = Collections.EMPTY_LIST;
        } else if(foreignComparator instanceof ComparatorChain) {
            ComparatorChain chain = (ComparatorChain)foreignComparator;
            comparatorsList = Arrays.asList(chain.getComparators());
        } else {
            comparatorsList = Collections.singletonList(foreignComparator);
        }

        // walk through the list of Comparators and assign click counts
        for(Iterator<Comparator> i = comparatorsList.iterator(); i.hasNext(); ) {
            // get the current comparator
            Comparator comparator = i.next();
            boolean reverse = false;
            if(comparator instanceof ReverseComparator) {
                reverse = true;
                comparator = ((ReverseComparator)comparator).getSourceComparator();
            }

            // discover where to add clicks for this comparator
            for(int c = 0; c < sortingColumns.size(); c++) {
                if(recentlyClickedColumns.contains(sortingColumns.get(c))) {
                    continue;
                }
                int comparatorIndex = sortingColumns.get(c).getComparators().indexOf(comparator);
                if(comparatorIndex != -1) {
                    final SortingColumn columnClickTracker = sortingColumns.get(c);
                    columnClickTracker.setComparatorIndex(comparatorIndex);
                    columnClickTracker.setReverse(reverse);
                    recentlyClickedColumns.add(columnClickTracker);
                }
            }
        }
    }

    public void clearComparators() {
        // clear the click counts
        for(Iterator<SortingColumn> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
            SortingColumn sortingColumn = i.next();
            sortingColumn.clear();
        }
        recentlyClickedColumns.clear();
    }

    /**
     * When the column model is changed, this resets the column clicks and
     * comparator list for each column.
     */
    public void rebuildColumns(TableFormat tableFormat) {
        // build the column click trackers
        final int columnCount = tableFormat.getColumnCount();

        sortingColumns = new ArrayList<SortingColumn>(columnCount);
        for(int i = 0; i < columnCount; i++) {
            sortingColumns.add(createSortingColumn(tableFormat, i));
        }

        recentlyClickedColumns.clear();
    }

    protected SortingColumn createSortingColumn(TableFormat tableFormat, int columnIndex) {
        return new SortingColumn(tableFormat, columnIndex);
    }

    public List<SortingColumn> getColumns() {
        return sortingColumns;
    }

    public List<SortingColumn> getRecentlyClickedColumns() {
        return recentlyClickedColumns;
    }

    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer();
        for(Iterator<Integer> i = getSortingColumnIndexes().iterator(); i.hasNext();) {
            final int columnIndex = i.next().intValue();
            final SortingState.SortingColumn sortingColumn = getColumns().get(columnIndex);

            // write the column index
            result.append("column ");
            result.append(columnIndex);

            // write the comparator index
            final int comparatorIndex = sortingColumn.getComparatorIndex();
            if(comparatorIndex != 0) {
                result.append(" comparator ");
                result.append(comparatorIndex);
            }

            // write reversed
            if(sortingColumn.isReverse()) {
                result.append(" reversed");
            }

            // add a comma if more columns exist
            if (i.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public void fromString(String stringEncoded) {
        clearComparators();

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
            if(columnIndex >= sortingColumns.size()) continue;
            if(comparatorIndex >= sortingColumns.get(columnIndex).getComparators().size()) continue;

            // add this comparator in sequence
            appendComparator(columnIndex, comparatorIndex, reversedComparator);
        }

    }

    public class SortingColumn {
        /** the column whose sorting state is being managed */
        private final int column;
        /** the sequence of comparators for this column */
        private final List<Comparator> comparators = new ArrayList<Comparator>(1);
        /** whether this column is sorted in reverse order */
        private boolean reverse = false;
        /** the comparator in the comparator list to sort by */
        private int comparatorIndex = -1;


        public SortingColumn(TableFormat tableFormat, int column) {
            this.column = column;

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

        public void clear() {
            this.reverse = false;
            this.comparatorIndex = -1;
        }

        public int getColumn() {
            return column;
        }

        /**
         * Gets the index of the comparator to use for this column.
         */
        public void setComparatorIndex(int comparatorIndex) {
            assert(comparatorIndex < comparators.size());
            this.comparatorIndex = comparatorIndex;
        }
        public int getComparatorIndex() {
            return comparatorIndex;
        }

        /**
         * Gets the list of comparators for this column.
         */
        public List<Comparator> getComparators() {
            return comparators;
        }

        /**
         * Gets the current best comparator to sort this column.
         */
        public Comparator getComparator() {
            if(comparatorIndex == -1) return null;
            Comparator comparator = comparators.get(getComparatorIndex());
            if(isReverse()) comparator = GlazedLists.reverseComparator(comparator);
            return comparator;
        }

        /**
         * Get whether this column is in reverse order.
         */
        public boolean isReverse() {
            return reverse;
        }
        public void setReverse(boolean reverse) {
            this.reverse = reverse;
        }

        /**
         * Gets the sorting style for this column.
         */
        public int getSortingStyle() {
            if(comparatorIndex == -1) return COLUMN_UNSORTED;
            boolean primaryColumn = !recentlyClickedColumns.isEmpty() && recentlyClickedColumns.get(0) == this;
            boolean primaryComparator = getComparatorIndex() == 0;

            if(primaryColumn) {
                if(!isReverse()) {
                    if(primaryComparator) return COLUMN_PRIMARY_SORTED;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE;
                } else {
                    if(primaryComparator) return COLUMN_PRIMARY_SORTED_REVERSE;
                    else return COLUMN_PRIMARY_SORTED_ALTERNATE_REVERSE;
                }
            } else {
                if(!isReverse()) {
                    if(primaryComparator) return COLUMN_SECONDARY_SORTED;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE;
                } else {
                    if(primaryComparator) return COLUMN_SECONDARY_SORTED_REVERSE;
                    else return COLUMN_SECONDARY_SORTED_ALTERNATE_REVERSE;
                }
            }
        }
    }
}