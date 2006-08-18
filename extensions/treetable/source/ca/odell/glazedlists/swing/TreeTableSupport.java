/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Prototype interface...
 *
 * @author jessewilson
 */
public final class TreeTableSupport {

    private final JTable table;
    private final TreeList treeList;
    private final int modelColumnIndex;
    private final TreeTableCellRenderer treeTableCellRenderer;
    private final TreeTableCellEditor treeTableCellEditor;

    private TreeTableSupport(JTable table, TreeList treeList, int modelColumnIndex) {
        this.table = table;
        this.treeList = treeList;
        this.modelColumnIndex = modelColumnIndex;

        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);

        this.treeTableCellRenderer = new TreeTableCellRenderer(viewColumn.getCellRenderer(), treeList);
        viewColumn.setCellRenderer(treeTableCellRenderer);

        this.treeTableCellEditor = new TreeTableCellEditor(viewColumn.getCellEditor(), treeList);
        viewColumn.setCellEditor(treeTableCellEditor);
    }

    /**
     * Some things this could do:
     *
     * 1. Install custom renderer that wraps the renderer in place
     * 2. Install custom editor that wraps the editor in place
     * 3. Install keyboard listeners for tree navigation via keyboard
     */
    public static TreeTableSupport install(JTable table, TreeList treeList, int modelColumnIndex) {
        checkAccessThread();

        return new TreeTableSupport(table, treeList, modelColumnIndex);
    }

    public void uninstall() {
        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);
        final TableCellRenderer renderer = viewColumn.getCellRenderer();

        // if the tree table cell renderer is still installed, reinstall its delegate
        if (renderer == treeTableCellRenderer)
            viewColumn.setCellRenderer(treeTableCellRenderer.getDelegate());

        this.treeTableCellRenderer.dispose();
    }

    /**
     * A convenience method to ensure {@link AutoCompleteSupport} is being
     * accessed from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("AutoCompleteSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
    }
}