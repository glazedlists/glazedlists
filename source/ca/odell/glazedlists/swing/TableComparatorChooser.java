/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// the Glazed Lists util and impl packages include default comparators
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.impl.SortIconFactory;
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

    /** listeners to sort change events */
    private ActionListener sortListener = null;

    /** the sort icons to use */
    private static Icon[] icons = SortIconFactory.loadIcons();

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
        super(sortedList, ((EventTableModel)table.getModel()).getTableFormat(), multipleColumnSort);
        
        // save the Swing-specific state
        this.table = table;

        // set the table header
        table.getTableHeader().setDefaultRenderer(new SortArrowHeaderRenderer());

        // listen for events on the specified table
        table.setColumnSelectionAllowed(false);
        table.getTableHeader().addMouseListener(listener);
        table.getModel().addTableModelListener(listener);
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
    protected void redetectComparator(Comparator currentComparator) {
        super.redetectComparator(currentComparator);

        // force the table header to redraw itself
        table.getTableHeader().revalidate();
        table.getTableHeader().repaint();
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    protected final void rebuildComparator() {
        super.rebuildComparator();

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
        return super.getSortingStyle(table.convertColumnIndexToModel(column));
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
            Comparator currentComparator = sortedList.getComparator();
            if(currentComparator != sortedListComparator) {
                redetectComparator(currentComparator);
            }
        }
    }

    /**
     * Releases the resources consumed by this {@link TableComparatorChooser} so that it
     * may eventually be garbage collected.
     *
     * <p>A {@link TableComparatorChooser} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link TableComparatorChooser}
     * to be garbage collected before its source {@link EventList}. This is 
     * necessary for situations where an {@link TableComparatorChooser} is short-lived but
     * its source {@link EventList} is long-lived.
     * 
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link TableComparatorChooser} after it has been disposed.
     */
    public void dispose() {
        table.getTableHeader().removeMouseListener(listener);
        table.getModel().removeTableModelListener(listener);
    }
    
    /**
     * Set all {@link TableComparatorChooser}s to use the icons from the directory
     * specified. The folder should contain the following eight icon files:
     * <li>primary_sorted.png                      <li>secondary_sorted.png
     * <li>primary_sorted_alternate.png            <li>secondary_sorted_alternate.png
     * <li>primary_sorted_alternate_reverse.png    <li>secondary_sorted_alternate_reverse.png
     * <li>primary_sorted_reverse.png              <li>secondary_sorted_reverse.png
     *
     * <p>Note that this path must be on the system classpath. It may be within a
     * jar file.
     */
    public static void setIconPath(String path) {
        icons = SortIconFactory.loadIcons(path);
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
                jLabelRenderer.setHorizontalTextPosition(SwingConstants.LEADING);
                return delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                return delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
    }
}
