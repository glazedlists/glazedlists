/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import java.util.List;

/**
 * @see ca.odell.glazedlists.gui.AbstractTableComparatorChooser#MULTIPLE_COLUMN_KEYBOARD
 * 
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MouseKeyboardSortingStrategy implements SortingStrategy {

    /** a column is sorted in forward, reverse or not at all */
    private static final int NONE = 0;
    private static final int FORWARD = 1;
    private static final int REVERSE = 2;

    /**
     * Adjust the sorting state based on receiving the specified click event.
     */
    public void columnClicked(SortingState sortingState, int column, int clicks, boolean shift, boolean control) {
        SortingState.SortingColumn sortingColumn = sortingState.getColumns().get(column);
        if(sortingColumn.getComparators().isEmpty()) return;
        List<SortingState.SortingColumn> recentlyClickedColumns = sortingState.getRecentlyClickedColumns();

        // figure out which comparator and reverse state we were on before
        int comparatorIndexBefore = sortingColumn.getComparatorIndex();
        final int forwardReverseNoneBefore;
        if(comparatorIndexBefore == -1) forwardReverseNoneBefore = NONE;
        else forwardReverseNoneBefore = sortingColumn.isReverse() ? REVERSE : FORWARD;

        // figure out which comparator and reverse state we shall go to
        int forwardReverseNoneAfter;
        int comparatorIndexAfter;
        boolean moreComparators = comparatorIndexBefore + 1 < sortingColumn.getComparators().size();
        boolean lastDirective = shift ? forwardReverseNoneBefore == FORWARD : forwardReverseNoneBefore == REVERSE;

        // if we're on the last mode of this comparator, go to the next comparator
        if(moreComparators && lastDirective) {
            comparatorIndexAfter = (comparatorIndexBefore + 1) % sortingColumn.getComparators().size();
            forwardReverseNoneAfter = forwardReverseNoneBefore == FORWARD ? REVERSE : FORWARD;

        // otherwise merely toggle forward/reverse/none
        } else {
            comparatorIndexAfter = comparatorIndexBefore != -1 ? comparatorIndexBefore : 0;
            forwardReverseNoneAfter = (shift ? forwardReverseNoneBefore + 2 : forwardReverseNoneBefore + 1) % 3;
        }

        // clean up if necessary
        if(!control) {
            sortingState.clearComparators();
        }

        // prepare the latest column
        if(forwardReverseNoneAfter == NONE) {
            sortingColumn.clear();
            recentlyClickedColumns.remove(sortingColumn);
        } else {
            sortingColumn.setComparatorIndex(comparatorIndexAfter);
            sortingColumn.setReverse(forwardReverseNoneAfter == REVERSE);
            if(!recentlyClickedColumns.contains(sortingColumn)) {
                recentlyClickedColumns.add(sortingColumn);
            }
        }

        // rebuild the sorting state
        sortingState.fireSortingChanged();
    }
}