/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

import ca.odell.glazedlists.impl.gui.SortingStrategy;
import ca.odell.glazedlists.impl.gui.SortingState;

import java.util.List;

/**
 * Emulate the sorting behaviour of TableSorter, by Philip Milne et. al.
 *
 * <p>This is not a direct adaptation since we choose to support potentially
 * many Comparators per column, wheras TableSorter is limited to one.
 *
 * <p>For reverence, this is TableSorter's behaviour, copied shamelessly
 * from that project's source file:
 *
 * <li>Mouse-click: Clears the sorting gui of all other columns and advances
 * the sorting gui of that column through three values:
 * {NOT_SORTED, ASCENDING, DESCENDING} (then back to NOT_SORTED again).
 *
 * <li>SHIFT-mouse-click: Clears the sorting gui of all other columns and
 * cycles the sorting gui of the column through the same three values,
 * in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 *
 * <li>CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except that the
 * changes to the column do not cancel the statuses of columns that are
 * already sorting - giving a way to initiate a compound sort.
 *
 * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html">Table tutorial</a>
 */
public class KeyboardModifiersSortingStrategy implements SortingStrategy {

    /** a column is sorted in forward, reverse or not at all */
    private static final int NONE = 0;
    private static final int FORWARD = 1;
    private static final int REVERSE = 2;

    /** the sorted state manages the actual comparators and columns */
    private SortingState sortingState;

    /** {@inheritDoc} */
    public void setSortingState(SortingState sortingState) {
        this.sortingState = sortingState;
    }

    /**
     * Adjust the sorting state based on receiving the specified click event.
     */
    public void columnClicked(int column, boolean shift, boolean control) {
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

    /** {@inheritDoc} */
    public void dispose() {
        // do nothing
    }
}