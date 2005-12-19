/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import java.util.List;
import java.util.Iterator;

/**
 * Single clicks cycle through comparators, double clicks clear them.
 *
 * <p>This is the original sorting strategy provided by Glazed Lists, with a
 * limitation that it is impossible to clear a sort order that is  already in
 * place. It's designed to be used with multiple columns and multiple comparators
 * per column.
 *
 * <p>The overall behaviour is as follows:
 *
 * <li>Click: sort this column. If it's already sorted, reverse the sort order.
 * If its already reversed, sort using the column's next comparator in forward
 * order. If there are no more comparators, go to the first comparator. If there
 * are multiple columns, sort this column after those columns.
 *
 * <li>Double click: like a single click, but clear all sorting columns first.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ClickDoubleClickSortingStrategy implements SortingStrategy {

    /** if false, other sorting columns will be cleared before a click takes effect */
    private final boolean multipleColumnSort;

    /** the sorted state manages the actual comparators and columns */
    private SortingState sortingState;

    /**
     * Create a new {@link ClickDoubleClickSortingStrategy}, sorting multiple
     * columns or not as specified.
     */
    public ClickDoubleClickSortingStrategy(boolean multipleColumnSort) {
        this.multipleColumnSort = multipleColumnSort;
    }

    /** {@inheritDoc} */
    public void setSortingState(SortingState sortingState) {
        this.sortingState = sortingState;
    }

    /**
     * Adjust the sorting state based on receiving the specified clicks.
     */
    public void columnClicked(int column, int clicks) {
        SortingState.SortingColumn clickedColumn = sortingState.getColumns().get(column);
        if(clickedColumn.getComparators().isEmpty()) return;

        List<SortingState.SortingColumn> recentlyClickedColumns = sortingState.getRecentlyClickedColumns();

        // on a double click, clear all click counts
        if(clicks == 2) {
            for(Iterator<SortingState.SortingColumn> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                SortingState.SortingColumn sortingColumn = i.next();
                sortingColumn.clear();
            }
            recentlyClickedColumns.clear();

        // if we're only sorting one column at a time, clear other columns
        } else if(!multipleColumnSort) {
            for(Iterator<SortingState.SortingColumn> i = recentlyClickedColumns.iterator(); i.hasNext(); ) {
                SortingState.SortingColumn sortingColumn = i.next();
                if(sortingColumn != clickedColumn) {
                    sortingColumn.clear();
                }
            }
            recentlyClickedColumns.clear();
        }

        // add a click to the newly clicked column if it has any comparators
        int netClicks = 1 + clickedColumn.getComparatorIndex() * 2 + (clickedColumn.isReverse() ? 1 : 0);
        clickedColumn.setComparatorIndex((netClicks / 2) % clickedColumn.getComparators().size());
        clickedColumn.setReverse(netClicks % 2 == 1);
        if(!recentlyClickedColumns.contains(clickedColumn)) {
            recentlyClickedColumns.add(clickedColumn);
        }

        // rebuild the sorting state
        sortingState.fireSortingChanged();
    }

    /** {@inheritDoc} */
    public void dispose() {
        // do nothing
    }
}