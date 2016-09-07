package ca.odell.glazedlists.impl.gui;

import java.util.List;

/**
 * @see ca.odell.glazedlists.gui.AbstractTableComparatorChooser#MULTIPLE_COLUMN_MOUSE_WITH_UNDO
 *
 * @author James Lemieux
 */
public class MouseOnlySortingStrategyWithUndo implements SortingStrategy {

    /**
     * The normal MouseOnlySortingStrategy that is decorated with the ability
     * to clear the sort from the table.
     */
    private final SortingStrategy decorated = new MouseOnlySortingStrategy(true);

    @Override
    public void columnClicked(SortingState sortingState, int column, int clicks, boolean shift, boolean control) {
        final SortingState.SortingColumn clickedColumn = sortingState.getColumns().get(column);

        // if the column defines no Comparators, we don't need to alter the table's Comparator
        if (clickedColumn.getComparators().isEmpty())
            return;

        final List<SortingState.SortingColumn> recentlyClickedColumns = sortingState.getRecentlyClickedColumns();

        final boolean wasPrimarySortColumnClicked = !recentlyClickedColumns.isEmpty() && clickedColumn == recentlyClickedColumns.get(0);
        final boolean isPrimarySortColumnReversed = clickedColumn.isReverse();
        final boolean isLastComparatorForPrimarySortColumn = clickedColumn.getComparatorIndex() == clickedColumn.getComparators().size() - 1;

        // if the following conditions exist on this click:
        // a) the column that was clicked was the column providing the primary sort
        // b) the primary sort column is currently reversed
        // c) the primary sort column is currently sorting using its last Comparator
        //
        // then deviate from the normal behaviour of MouseOnlySortingStrategy by
        // removing all sorting from the table
        if (wasPrimarySortColumnClicked && isPrimarySortColumnReversed && isLastComparatorForPrimarySortColumn) {
            sortingState.clearComparators();
            sortingState.fireSortingChanged();
            return;
        }

        // otherwise, proceed with the normal MouseOnlySortingStrategy
        decorated.columnClicked(sortingState, column, clicks, shift, control);
    }
}