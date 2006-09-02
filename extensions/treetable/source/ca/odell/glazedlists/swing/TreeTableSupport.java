/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

    private final KeyListener expandAndCollapseKeyListener = new ExpandAndCollapseKeyListener();

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

        this.table.addKeyListener(expandAndCollapseKeyListener);
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

        table.removeKeyListener(expandAndCollapseKeyListener);

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

    private class ExpandAndCollapseKeyListener extends KeyAdapter {
        private boolean eventSelectionModelEnabled;
        private boolean restoreAutoStartsEdit;
        private Boolean autoStartsEdit;

        public void keyPressed(KeyEvent e) {
            // if the table doesn't own the focus, break early
            if (!table.isFocusOwner())
                return;

            // if the key pressed is not the space bar, break early
            if (e.getKeyChar() != KeyEvent.VK_SPACE)
                return;

            treeList.getReadWriteLock().writeLock().lock();
            try {
                final int row = table.getSelectionModel().getLeadSelectionIndex();
                final int column = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();

                // ensure some valid cell has focus
                if (row == -1 || column == -1)
                    return;

                // ensure the column is the hierarchical column
                final int focusedColumnModelIndex = table.convertColumnIndexToModel(column);
                if (focusedColumnModelIndex != modelColumnIndex)
                    return;

                // if the row is expandable, toggle its expanded state
                if (treeList.isExpandable(row)) {
                    // turn off the client property that begins a cell edit -
                    // we don't want the spacebar to invoke two different behaviours
                    autoStartsEdit = (Boolean) table.getClientProperty("JTable.autoStartsEdit");
                    table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
                    restoreAutoStartsEdit = true;

                    TreeTableUtilities.toggleExpansionWithoutAdjustingSelection(table, treeList, row);
                }
            } finally {
                treeList.getReadWriteLock().writeLock().unlock();
            }
        }

        public void keyReleased(KeyEvent e) {
            // restore the original value of the client property that normally begins a cell edit on a keystroke
            if (restoreAutoStartsEdit) {
                table.putClientProperty("JTable.autoStartsEdit", autoStartsEdit);
                restoreAutoStartsEdit = false;
            }
        }
    }
}