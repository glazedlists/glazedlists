/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// the Glazed Lists util and impl packages include default comparators
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.impl.SortIconFactory;
import ca.odell.glazedlists.impl.gui.SortingStrategy;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import javax.swing.table.*;
// for responding to user actions
import java.awt.event.*;
import java.awt.*;
import javax.swing.event.*;
// for keeping lists of comparators
import java.util.*;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a ListTable by clicking
 * on the table's headers. It requires that the {@link JTable}s model is an
 * {@link EventTableModel} with a {@link SortedList} as a source.
 *
 * <p>This class includes custom arrow icons that indicate the sort
 * order. The icons used are chosen based on the current Swing look and feel.
 * Icons are available for the following look and feels: Mac OS X, Metal, Windows.
 *
 * <p>This class supports multiple sort strategies for each
 * column, specified by having muliple comparators for each column. This may
 * be useful when you want to sort a single column in either of two ways. For
 * example, when sorting movie names, "The Phantom Menace" may be sorted under
 * "T" for "The", or "P" for "Phantom".
 *
 * <p>This class supports sorting multiple columns simultaneously.
 * In this mode, the user clicks a first column to sort by, and then the user
 * clicks subsequent columns. The list is sorted by the first column and ties
 * are broken by the second column.
 *
 * <p>If the {@link EventTableModel} uses a {@link AdvancedTableFormat}, its
 * {@link AdvancedTableFormat#getColumnComparator} method will be used to
 * populate the initial column {@link Comparator}s.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=4">Bug 4</a>
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=31">Bug 31</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TableComparatorChooser<E> extends AbstractTableComparatorChooser<E> {

    /**
     * the header renderer which decorates an underlying renderer
     * (the table header's default renderer) with a sort arrow icon.
     */
    private final SortArrowHeaderRenderer sortArrowHeaderRenderer;

    /** listen for table and mouse events */
    private final TableModelHandler tableModelHandler = new TableModelHandler();

    /** the table being sorted */
    private JTable table = null;

    /** listeners to sort change events */
    private ActionListener sortListener = null;

    /** the sort icons to use */
    private static Icon[] icons = SortIconFactory.loadIcons();

    /** when somebody clicks on the header, update the sorting state */
    private final HeaderClickHandler headerClickHandler;

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
    public TableComparatorChooser(JTable table, SortedList<E> sortedList, boolean multipleColumnSort) {
        this(table, sortedList, multipleColumnSort ? MULTIPLE_COLUMN_MOUSE : SINGLE_COLUMN);
    }


    public TableComparatorChooser(JTable table, SortedList<E> sortedList, Object strategy) {
        super(sortedList, ((EventTableModel)table.getModel()).getTableFormat());
        if(!(strategy instanceof SortingStrategy)) {
            throw new IllegalArgumentException("Unrecognized sorting strategy, \"" + strategy + "\", use one of AbstractTableComparatorChooser.SINGLE_COLUMN, AbstractTableComparatorChooser.MULTIPLE_COLUMN_MOUSE, or AbstractTableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD");
        }

        // save the Swing-specific state
        this.table = table;

        // build and set the table header renderer which decorates the existing renderer with sort arrows
        sortArrowHeaderRenderer = new SortArrowHeaderRenderer(table.getTableHeader().getDefaultRenderer());
        table.getTableHeader().setDefaultRenderer(sortArrowHeaderRenderer);

        // listen for events on the specified table
        table.setColumnSelectionAllowed(false);
        table.getModel().addTableModelListener(tableModelHandler);

        // install the sorting strategy to interpret clicks
        this.headerClickHandler = new HeaderClickHandler(table, (SortingStrategy)strategy);
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
    protected void redetectComparator(Comparator<E> currentComparator) {
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
     *
     * <p>As of 2005/12/20, that this method is no longer called when the
     * corresponding mouse press event was a popup trigger. In effect, if this
     * is a right-click on Windows or a 'control-click' on the Mac.
     */
    protected boolean isSortingMouseEvent(MouseEvent e) {
        // skip the sort if it's not button 1
        if(e.getButton() != MouseEvent.BUTTON1) return false;

        // we have no reason to dislike this mouse event!
        return true;
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
        super.dispose();
        headerClickHandler.dispose();

        // if the default renderer within the table header is our sort arrow renderer,
        // uninstall it by restoring the table header's original default renderer
        if (table.getTableHeader().getDefaultRenderer() == sortArrowHeaderRenderer)
            table.getTableHeader().setDefaultRenderer(sortArrowHeaderRenderer.getDelegateRenderer());

        // remove our listeners from the table's header and model
        table.getModel().removeTableModelListener(tableModelHandler);

        // null out our table reference for safety's sake
        table = null;
    }

    /**
     * Nested Listener class handles table events and mouse events.
     */
    private class TableModelHandler implements TableModelListener {

        /**
         * When the number of columns changes in the table, we need to
         * clear the comparators and columns.
         */
        public void tableChanged(TableModelEvent event) {
            if(event.getFirstRow() == TableModelEvent.HEADER_ROW &&
               event.getColumn() == TableModelEvent.ALL_COLUMNS) {
                // the table structure may have changed due to a change in the
                // table format so we conservatively reset the TableFormat
                setTableFormat(((EventTableModel)table.getModel()).getTableFormat());
            }

            // if the comparator has changed
            Comparator currentComparator = sortedList.getComparator();
            if(currentComparator != sortedListComparator) {
                redetectComparator(currentComparator);
            }
        }
    }

    /**
     * The SortArrowHeaderRenderer simply delegates most of the rendering
     * to a given delegate renderer, and adds an icon to indicate sorting
     * direction. This allows TableComparatorChooser to work equally well
     * with any custom TableCellRenderers that are used as the default
     * table header renderer.
     *
     * <p>This class fails to add indicator arrows on table headers where the
     * default table header render is not a DefaultTableCellRenderer or does
     * not implement {@link SortableRenderer}.
     */
    class SortArrowHeaderRenderer implements TableCellRenderer {

        /** the renderer to which we delegate */
        private TableCellRenderer delegateRenderer;

        /**
         * Creates a new SortArrowHeaderRenderer that attempts to decorate the
         * given <code>delegateRenderer</code> which a sorting icon.
         */
        public SortArrowHeaderRenderer(TableCellRenderer delegateRenderer) {
            this.delegateRenderer = delegateRenderer;
        }

        /**
         * Returns the delegate renderer that is decorated with sort arrows.
         */
        public TableCellRenderer getDelegateRenderer() {
            return this.delegateRenderer;
        }

        /**
         * Renders the header in the default way but with the addition of an icon.
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            final Icon sortIcon = icons[getSortingStyle(column)];
            final Component rendered;

            // 1. look for our custom SortableRenderer interface
            if (delegateRenderer instanceof SortableRenderer) {
                ((SortableRenderer) delegateRenderer).setSortIcon(sortIcon);
                rendered = delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 2. if it's a DefaultTableCellRenderer that returned itself then customize it directly with
            //    the sorting icon after the fact (this is the case of the default header renderer)
            } else if (delegateRenderer instanceof DefaultTableCellRenderer) {
                rendered = delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (rendered == delegateRenderer) {
                    final DefaultTableCellRenderer label = (DefaultTableCellRenderer) rendered;
                    label.setIcon(sortIcon);
                    label.setHorizontalTextPosition(SwingConstants.LEADING);
                }

            // 3. We are unable to inject an icon into the rendered component
            } else {
                rendered = delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }

            return rendered;
        }
    }

    /**
     * Handle clicks to the table's header by adjusting the sorrting state.
     */
    private class HeaderClickHandler extends MouseAdapter {
        private final JTable table;
        private final SortingStrategy delegate;
        private boolean mouseEventIsPerformingPopupTrigger = false;

        public HeaderClickHandler(JTable table, SortingStrategy delegate) {
            this.table = table;
            this.delegate = delegate;
            table.getTableHeader().addMouseListener(this);
        }

        public void mouseClicked(MouseEvent e) {
            if(mouseEventIsPerformingPopupTrigger) return;
            if(!isSortingMouseEvent(e)) return;

            boolean shift = e.isShiftDown();
            boolean control = e.isControlDown() || e.isMetaDown();

            TableColumnModel columnModel = table.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = table.convertColumnIndexToModel(viewColumn);
            int clicks = e.getClickCount();
            if(clicks >= 1 && column != -1) {
                delegate.columnClicked(sortingState, column, clicks, shift, control);
            }
        }

        /**
         * Keep track of whether the mouse is triggering a popup, so we
         * can avoid sorting the table when the poor user just wants to show
         * a context menu.
         */
        public void mousePressed(MouseEvent mouseEvent) {
            this.mouseEventIsPerformingPopupTrigger = mouseEvent.isPopupTrigger();
        }

        public void dispose() {
            table.getTableHeader().removeMouseListener(this);
        }
    }
}