/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.awt.*;

/**
 * This class {@link #install}s support for a single hierarchical column within
 * a standard {@link JTable} that is backed by an {@link EventTableModel}. The
 * {@link EventTableModel}, in turn <strong>must</strong> be backed by the same
 * {@link TreeList} which is given as a parameter to {@link #install}.
 *
 * <p>Typical usage of {@link TreeTableSupport} resembles this:
 * <br><br>
 * <pre>
 * // create an EventList of data
 * EventList myEventList = ...
 *
 * // create a TreeList which uses a TreeFormat object to infer a hierarchy for each element in myEventList
 * TreeList treeList = new TreeList(myEventList, new MyTreeFormat());
 *
 * // create a JTable that displays the contents of treeList in a tabular format
 * EventTableModel myTableModel = new EventTableModel(treeList, new MyTableFormat());
 * JTable myTable = new JTable(myTableModel);
 *
 * // make the 3rd table column a hierarchical column to create a TreeTable
 * TreeTableSupport.install(myTable, treeList, 2);
 * </pre>
 *
 * <p>In order to achieve all of the treetable behaviours, the following occurs
 * when {@link #install} is called:
 *
 * <ul>
 *   <li>a {@link TreeTableCellRenderer} will be installed on the hierarchical
 *       {@link TableColumn}. It wraps any {@link TableCellRenderer} previously
 *       installed on the {@link TableColumn}, or if none was present, a
 *       DefaultTableCellRenderer.
 *   <li>a {@link TreeTableCellEditor} will be installed on the hierarchical
 *       {@link TableColumn}. It wraps any {@link TableCellEditor} previously
 *       installed on the {@link TableColumn}, or if none was present, a
 *       DefaultCellEditor.
 * </ul>
 *
 * @author James Lemieux
 */
public final class TreeTableSupport {

    /** The table to decorate with tree table behaviour */
    private final JTable table;

    /** The TreeList that stores the state of the tree */
    private final TreeList treeList;

    /** The model index of the table column that is the hierarchical column */
    private final int modelColumnIndex;

    /** Expands and collapses the tree structure based on key strokes (the space bar). */
    private final KeyListener expandAndCollapseKeyListener = new ExpandAndCollapseKeyListener();

    /** Expands and collapses the tree structure based on left and right arrow key strokes. */
    private final KeyListener arrowKeyListener = new ArrowKeyListener();

    /** The renderer installed on the hierarchical TableColumn */
    private final TreeTableCellRenderer treeTableCellRenderer;

    /** The editor installed on the hierarchical TableColumn */
    private final TreeTableCellEditor treeTableCellEditor;

    /** The orignal TableCellRenderer, if any, to be replaced when TreeTableSupport is {@link #uninstall() uninstalled} */
    private final TableCellRenderer originalRenderer;

    /** The orignal TableCellEditor, if any, to be replaced when TreeTableSupport is {@link #uninstall() uninstalled} */
    private final TableCellEditor originalEditor;

    /**
     * This private constructor creates a TreeTableSupport object which adds
     * tree table behaviours to a single column that is nominated as the
     * "hierarchical column." Specifically, the rendering and editing of values
     * in the hierarchical column will be augmented to display position within
     * a hierarchy by indenting the "normal renderer and editor" according to
     * the height within a tree and adding a collapse/expand button.
     *
     * @param table the table to convert into a tree table
     * @param treeList The TreeList capable of answering hierarchical questions
     *        for the hierachical TableColumn's renderer and editor
     * @param modelColumnIndex the model index of the hierarchical column
     */
    private TreeTableSupport(JTable table, TreeList treeList, int modelColumnIndex) {
        this.table = table;
        this.treeList = treeList;
        this.modelColumnIndex = modelColumnIndex;

        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);

        // ensure we can find the view column index of the hierarchical column
        if (viewColumnIndex == -1)
            throw new IllegalArgumentException("Unable to locate a view index for the given model index: " + modelColumnIndex);

        // if we have data, check that the hierarchical column is editable
        if (!treeList.isEmpty() && !table.isCellEditable(0, viewColumnIndex))
            throw new IllegalStateException("The hierarchy view column at index " + viewColumnIndex + " must be editable to support expanding and collapsing tree nodes");

        // look up the hierarchical TableColumn
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);

        // wrap the existing TableCellRenderer with a TreeTableCellRenderer
        this.originalRenderer = viewColumn.getCellRenderer();
        this.treeTableCellRenderer = new TreeTableCellRenderer(originalRenderer, treeList);
        viewColumn.setCellRenderer(treeTableCellRenderer);

        // wrap the existing TableCellEditor with a TreeTableCellEditor
        this.originalEditor = viewColumn.getCellEditor();
        this.treeTableCellEditor = new TreeTableCellEditor(originalEditor, treeList);
        viewColumn.setCellEditor(treeTableCellEditor);

        this.table.addKeyListener(arrowKeyListener);
        this.table.addKeyListener(expandAndCollapseKeyListener);
        decorateUIDelegateMouseListener(this.table);
    }

    /**
     * This method is a complete hack, but is necessary to achieve the
     * desired behaviour when adding collapse/expand capabilities to a
     * treetable.
     *
     * The problem is that when a mouse button is pressed, the MouseListener
     * installed by the UI Delegate is executed <strong>first</strong> and
     * changes the selected row to the row that was just clicked. This is
     * undesirable from the standpoint of treetables. One should be able to
     * expand or collapse tree nodes without adjusting the selection.
     *
     * To solve the problem, we rip out the MouseListener installed by
     * the BasicTableUI and decorate it before reinstalling it. The decorated
     * version of the MouseListener detects mouse clicks that actually toggle
     * the collapsed/expanded state and ensures that we do not change the
     * selected row in that case.
     */
    private void decorateUIDelegateMouseListener(Component c) {
        // replace the first MouseListener that appears to be installed by the UI Delegate
        final MouseListener[] mouseListeners = c.getMouseListeners();
        for (int i = mouseListeners.length-1; i >= 0; i--) {
            if (mouseListeners[i].getClass().getName().indexOf("TableUI") != -1) {
                c.removeMouseListener(mouseListeners[i]);
                c.addMouseListener(new ExpandAndCollapseMouseListener(mouseListeners[i]));
                break;
            }
        }
    }

    /**
     * This method will undo the work performed by {@link #decorateUIDelegateMouseListener(Component)}.
     */
    private void undecorateUIDelegateMouseListener(Component c) {
        // replace all decorated MouseListeners with original UI Delegate MouseListener
        final MouseListener[] mouseListeners = c.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; i++) {
            if (mouseListeners[i] instanceof ExpandAndCollapseMouseListener) {
                c.removeMouseListener(mouseListeners[i]);
                c.addMouseListener(((ExpandAndCollapseMouseListener) mouseListeners[i]).getDelegate());
            }
        }
    }

    /**
     * Installs support for a hierarchical table column into the
     * <code>table</code> on the column with the given
     * <code>modelColumnIndex</code>. The data returned by the
     * <code>TableModel</code> for the hierarchical column will remain
     * unchanged. But, the renderer and editor will be wrapped with instances
     * of {@link TreeTableCellRenderer} and {@link TreeTableCellEditor}
     * which take care of displaying the hierarchical information (namely the
     * whitespace indenting and expand/collapse button).
     *
     * <p>The following must be true in order to successfully install
     * TreeTableSupport on a {@link JTable}:
     *
     * <ul>
     *   <li>the <code>modelColumnIndex</code> must correspond to a valid
     *       view column index
     *   <li>the table column at the given <code>modelColumnIndex</code> must
     *       be editable so that tree node collapsing and expanding is possible
     *       (collapsing/expanding is accomplished via the TreeTableCellEditor)
     * </ul>
     *
     * @param table the table to convert into a tree table
     * @param treeList The TreeList capable of answering hierarchical questions
     *        for the hierachical TableColumn's renderer and editor
     * @param modelColumnIndex the model index of the hierarchical column
     * @return an instance of the support class providing treetable behaviour
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static TreeTableSupport install(JTable table, TreeList treeList, int modelColumnIndex) {
        checkAccessThread();

        return new TreeTableSupport(table, treeList, modelColumnIndex);
    }

    /**
     * This method removes treetable support from the {@link JTable} it was
     * installed on. This method is useful when the {@link TreeList} of items
     * that backs the JTable must outlive the JTable itself. Calling this method
     * will return the hierarchical column to its original state before
     * treetable support was installed, and it will be available for garbage
     * collection independently of the {@link TreeList} of items.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void uninstall() {
        checkAccessThread();

        // stop trying to toggle tree nodes due to keystrokes and mouse clicks
        table.removeKeyListener(arrowKeyListener);
        table.removeKeyListener(expandAndCollapseKeyListener);
        undecorateUIDelegateMouseListener(table);

        // fetch information about the hierarchical column
        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);
        final TableCellRenderer renderer = viewColumn.getCellRenderer();
        final TableCellEditor editor = viewColumn.getCellEditor();

        // if the TreeTableCellRenderer is still installed, reinstall the original TableCellRenderer
        if (renderer == treeTableCellRenderer)
            viewColumn.setCellRenderer(originalRenderer);

        // if the TreeTableCellEditor is still installed, reinstall the original TableCellEditor
        if (editor == treeTableCellEditor)
            viewColumn.setCellEditor(originalEditor);

        treeTableCellRenderer.dispose();
        treeTableCellEditor.dispose();
    }

    /**
     * A convenience method to ensure {@link TreeTableSupport} is being
     * accessed from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("TreeTableSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
    }

    /**
     * This listener watches for mouse clicks overtop of the expander button
     * within a renderer in a hierarchical column and toggles the expansion
     * of the tree node, if possible.
     */
    private class ExpandAndCollapseMouseListener extends MouseAdapter {

        /** An optional MouseListener to which we delegate after performing our own processing. */
        private final MouseListener delegate;

        /**
         * Decorate the given <code>delegate</code> with extra functionality.
         */
        public ExpandAndCollapseMouseListener(MouseListener delegate) {
            this.delegate = delegate;
        }

        /**
         * Return the {@link MouseListener} being decorated.
         */
        public MouseListener getDelegate() {
            return delegate;
        }

        public void mousePressed(MouseEvent me) {
            // if the table isn't enabled, break early
            if (!table.isEnabled())
                return;

            // we're going to check if the single click was overtop of the
            // expander button and toggle the expansion of the row if it was

            final TreeTableCellPanel renderedPanel = TreeTableUtilities.prepareRenderer(me);

            // translate the click to be relative to the cellRect (and thus its rendered component)
            // extract information about the location of the click
            final JTable table = (JTable) me.getSource();
            final Point clickPoint = me.getPoint();
            final int row = table.rowAtPoint(clickPoint);
            final int column = table.columnAtPoint(clickPoint);

            // ensure a valid cell has clicked
            if (row == -1 || column == -1)
                return;

            // translate the clickPoint to be relative to the rendered component
            final Rectangle cellRect = table.getCellRect(row, column, true);
            clickPoint.translate(-cellRect.x, -cellRect.y);

            // if a left-click occurred over the expand/collapse button
            if (SwingUtilities.isLeftMouseButton(me) && renderedPanel != null && renderedPanel.isPointOverExpanderButton(clickPoint)) {
                treeList.getReadWriteLock().writeLock().lock();
                try {
                    // expand/collapse the rowObject if possible
                    if (treeList.getAllowsChildren(row))
                        TreeTableUtilities.toggleExpansion(table, treeList, row);
                } finally {
                    treeList.getReadWriteLock().writeLock().unlock();
                }
            }

            if (delegate != null)
                delegate.mousePressed(me);
        }
    }

    /**
     * This listener watches for space bar presses when cell focus is within
     * a hierarchical column and toggles the expansion of the tree node, if
     * possible.
     */
    private class ExpandAndCollapseKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            // if the table isn't enabled, break early
            if (!table.isEnabled())
                return;

            // if the table doesn't own the focus, break early
            if (!table.isFocusOwner())
                return;

            // if the key pressed is not the space bar, break early
            if (e.getKeyCode() != KeyEvent.VK_SPACE || e.getModifiers() != 0)
                return;

            final int row = table.getSelectionModel().getLeadSelectionIndex();
            final int column = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();

            // ensure a valid cell has focus
            if (row == -1 || column == -1)
                return;

            // ensure the column is the hierarchical column
            final int focusedColumnModelIndex = table.convertColumnIndexToModel(column);
            if (focusedColumnModelIndex != modelColumnIndex)
                return;

            treeList.getReadWriteLock().writeLock().lock();
            try {
                // if the row is expandable, toggle its expanded state
                if (treeList.getAllowsChildren(row))
                    TreeTableUtilities.toggleExpansion(table, treeList, row);
            } finally {
                treeList.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    /**
     * This listener watches for ENTER key presses when cell focus is within
     * a hierarchical column and toggles the expansion of the tree node, if
     * possible.
     */
    private class ArrowKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            // if the table isn't enabled, break early
            if (!table.isEnabled())
                return;

            // if column selection is allowed, we want normal table handling of
            // the arrow key, not this special tree node toggling logic
            if (table.getColumnSelectionAllowed())
                return;

            // if the key pressed is not the space bar, break early
            final int c = e.getKeyCode();
            final boolean isLeftArrowKey = c == KeyEvent.VK_LEFT;
            final boolean isRightArrowKey = c == KeyEvent.VK_RIGHT;
            if (!(isLeftArrowKey || isRightArrowKey) || e.getModifiers() != 0)
                return;

            final int row = table.getSelectionModel().getLeadSelectionIndex();

            // ensure a valid cell has focus
            if (row == -1)
                return;

            treeList.getReadWriteLock().writeLock().lock();
            try {
                // if the row is expandable, toggle its expanded state
                if (treeList.getAllowsChildren(row)) {
                    final boolean expanded = treeList.isExpanded(row);
                    if ((expanded && isLeftArrowKey) || (!expanded && isRightArrowKey))
                        TreeTableUtilities.toggleExpansion(table, treeList, row);
                }
            } finally {
                treeList.getReadWriteLock().writeLock().unlock();
            }
        }
    }
}