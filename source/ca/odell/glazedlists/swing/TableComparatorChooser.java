/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// the Glazed Lists util and impl packages include default comparators
import ca.odell.glazedlists.util.*;
import ca.odell.glazedlists.impl.*;
import ca.odell.glazedlists.impl.sort.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import javax.swing.table.*;
// for responding to user actions
import java.awt.event.*;
import java.awt.AWTEventMulticaster;
import javax.swing.event.*;
// for keeping lists of comparators
import java.util.*;


/**
 * A TableComparatorChooser is a tool that allows the user to sort a ListTable by clicking
 * on the table's headers. It requires that the ListTable has a SortedList as
 * a source as the sorting on that list is used.
 *
 * <p>The TableComparatorChooser includes custom arrow icons that indicate the sort
 * order. The icons used are chosen based on the current Swing look and feel.
 * Icons are available for the following look and feels: Mac OS X, Metal, Windows.
 *
 * <p>The TableComparatorChooser supports multiple sort strategies for each
 * column, specified by having muliple comparators for each column. This may
 * be useful when you want to sort a single column in either of two ways. For
 * example, when sorting movie names, "The Phantom Menace" may be sorted under
 * "T" for "The", or "P" for "Phantom".
 *
 * <p>The TableComparatorChooser supports sorting multiple columns simultaneously.
 * In this mode, the user clicks a first column to sort by, and then the user
 * clicks subsequent columns. The list is sorted by the first column and ties
 * are broken by the second column.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=4">Bug 4</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=31">Bug 31</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TableComparatorChooser extends AbstractTableComparatorChooser {

    /** listen for table and mouse events */
    private Listener listener = new Listener();

    /** the table being sorted */
    private JTable table = null;
    private EventTableModel eventTableModel = null;

    /** listeners to sort change events */
    private ActionListener sortListener = null;

    /** the sort icons to use */
    private static Icon[] icons = SortIconFactory.getIcons();

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
    public TableComparatorChooser(JTable table, SortedList sortedList, boolean multipleColumnSort) {
        this.table = table;
        this.sortedList = sortedList;
        this.multipleColumnSort = multipleColumnSort;

        // get the table model from the table
        try {
            eventTableModel = (EventTableModel)table.getModel();
        } catch(ClassCastException e) {
            throw new IllegalArgumentException("Can not apply TableComparatorChooser to a table whose table model is not an EventTableModel");
        }

        // set up the column click listeners
        rebuildColumns();

        // set the table header
        table.getTableHeader().setDefaultRenderer(new SortArrowHeaderRenderer());

        // listen for events on the specified table
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().addMouseListener(listener);
        table.getModel().addTableModelListener(listener);
    }

    /**
     * When the column model is changed, this resets the column clicks and
     * comparator list for each column.
     */
    private void rebuildColumns() {
        // build the column click managers
        columnClickTrackers = new ColumnClickTracker[eventTableModel.getColumnCount()];
        for(int i = 0; i < columnClickTrackers.length; i++) {
            columnClickTrackers[i] = new ColumnClickTracker(eventTableModel.getTableFormat(), i);
        }
        primaryColumn = -1;
        recentlyClickedColumns.clear();
    }

    /**
     * Registers the specified {@link ActionListener} to receive notification whenever
     * the {@link JTable} is sorted by this {@link TableComparatorChooser}.
     */
    public void addSortActionListener(final ActionListener sortActionListener) {
        sortListener = AWTEventMulticaster.add(sortListener, sortActionListener);
    }
    /**
     * Deregisters the specified {@link ActionListener} to no longer receive
     * action events.
     */
    public void removeSortActionListener(final ActionListener sortActionListener) {
        sortListener = AWTEventMulticaster.remove(sortListener, sortActionListener);
    }

    /**
     * Examines the current {@link Comparator} of the SortedList and
     * adds icons to the table header renderers in response.
     *
     * <p>To do this, clicks are injected into each of the
     * corresponding <code>ColumnClickTracker</code>s.
     */
    private void redetectComparator(Comparator currentComparator) {
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

        // force the table header to redraw itself
        table.getTableHeader().revalidate();
        table.getTableHeader().repaint();

        // notify interested listeners that the sorting has changed
        if(sortListener != null) sortListener.actionPerformed(new ActionEvent(this, 0, "sort"));
    }

    /**
     * Gets the sorting style currently applied to the specified column.
     */
    protected final int getSortingStyle(int column) {
        int modelColumn = table.convertColumnIndexToModel(column);
        return columnClickTrackers[modelColumn].getSortingStyle();
    }

    /**
     * Creates a {@link Comparator} that can compare list elements
     * given a {@link Comparator} that can compare column values for the specified
     * column. This returns a {@link Comparator} that extracts the table values for
     * the specified column and then delegates the actual comparison to the specified
     * comparator.
     */
    public Comparator createComparatorForElement(Comparator comparatorForColumn, int column) {
        return new TableColumnComparator(eventTableModel.getTableFormat(), column, comparatorForColumn);
    }

    /**
     * Determines if the specified mouse event shall be handled by this
     * {@link TableComparatorChooser}. The default implementation handles only clicks
     * with the left mouse button. Extending classes can customize which mouse
     * events the table comparator chooser responds to by overriding this method.
     */
    protected boolean isSortingMouseEvent(MouseEvent e) {
        return (e.getButton() == MouseEvent.BUTTON1);
    }

    /**
     * Nested Listener class handles table events and mouse events.
     */
    private class Listener extends MouseAdapter implements TableModelListener {

        /**
         * When the mouse is clicked, this selects the next comparator in
         * sequence for the specified table. This will re-sort the table
         * by a new criterea.
         *
         * This code is based on the Java Tutorial's TableSorter
         * @see <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/table.html#sorting">The Java Tutorial</a>
         */
        public void mouseClicked(MouseEvent e) {
            if(!isSortingMouseEvent(e)) return;

            TableColumnModel columnModel = table.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = table.convertColumnIndexToModel(viewColumn);
            int clicks = e.getClickCount();
            if(clicks >= 1 && column != -1) {
                columnClicked(column, clicks);
            }
        }

        /**
         * When the number of columns changes in the table, we need to
         * clear the comparators and columns.
         */
        public void tableChanged(TableModelEvent event) {
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
        }
    }

    /**
     * The SortArrowHeaderRenderer simply delegates most of the rendering
     * to the previous renderer, and adds an icon to indicate sorting
     * direction. This eliminates the hassle of setting the border and
     * background colours.
     *
     * <p>This class fails to add indicator arrows on tables where the
     * renderer does not extend DefaultTableCellRenderer.
     */
    class SortArrowHeaderRenderer implements TableCellRenderer {

        /** the renderer to delegate */
        private TableCellRenderer delegateRenderer;

        /** whether we can inject icons into this renderer */
        private boolean iconInjection = false;

        /**
         * Creates a new SortArrowHeaderRenderer that delegates most drawing
         * to the tables current header renderer.
         */
        public SortArrowHeaderRenderer() {
            // find the delegate
            this.delegateRenderer = table.getTableHeader().getDefaultRenderer();

            // determine if we can inject icons into the delegate
            iconInjection = (delegateRenderer instanceof DefaultTableCellRenderer);
        }

        /**
         * Renders the header in the default way but with the addition of an icon.
         */
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
            if(iconInjection) {
                DefaultTableCellRenderer jLabelRenderer = (DefaultTableCellRenderer)delegateRenderer;
                Icon iconToUse = icons[getSortingStyle(column)];
                jLabelRenderer.setIcon(iconToUse);
                jLabelRenderer.setHorizontalTextPosition(jLabelRenderer.LEADING);
                return delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }
}
