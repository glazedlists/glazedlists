/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.gui.TreeIconFactory;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Prototype interface...
 *
 * @author jessewilson
 */
public final class TreeTableSupport {

    /** the sort icons to use */
    private static Icon[] icons = TreeIconFactory.loadIcons();

    private final JTable table;
    private final TreeList treeList;
    private final int modelColumnIndex;
    private final TreeTableCellRenderer treeTableCellRenderer;

    private TreeTableSupport(JTable table, TreeList treeList, int modelColumnIndex) {
        this.table = table;
        this.treeList = treeList;
        this.modelColumnIndex = modelColumnIndex;

        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);
        final TableCellRenderer renderer = viewColumn.getCellRenderer();

        this.treeTableCellRenderer = new TreeTableCellRenderer(renderer, treeList);
        viewColumn.setCellRenderer(treeTableCellRenderer);
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
     * // todo doc this better when we actually use pngs (we currently use Icons defined in code)
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
        icons = TreeIconFactory.loadIcons(path);
    }

    /**
     * Returns the icon representing an expanded tree node for all TreeTables.
     */
    public static Icon getExpandedIcon() {
        return icons[0];
    }

    /**
     * Returns the icon representing a collapsed tree node for all TreeTables.
     */
    public static Icon getCollapsedIcon() {
        return icons[1];
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