/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * This renderer removes some of the burden of producing an appropriate looking
 * component for the hierarchy column of a tree table. Specifically, it takes
 * care of adding components to a panel that render a node location within the
 * tree, but leaves the rendering of the <strong>data</strong> of the node to
 * a delegate {@link TableCellRenderer} that is supplied in the constructor.
 *
 * <p>For example, in the following tree representation, the spacing and +/-
 * icons would be added to each tree node by this renderer, while the data text
 * would be added by the component returned from the delegate
 * {@link TableCellRenderer}.
 *
 * <pre>
 * - Cars
 *   + BMW
 *   - Ford
 *       Taurus
 *       Focus
 *   - Lexus
 *       ES 300
 *       LS 600h L
 * </pre>
 *
 * @author James Lemieux
 */
public class TreeTableCellRenderer implements TableCellRenderer {

    /** The user-supplied renderer that produces the look of the tree node's data. */
    private TableCellRenderer delegate;

    /** The data structure that answers questions about the tree node. */
    private TreeList treeList;

    /** The panel capable of laying out a spacer component, expander button, and data Component to produce the entire tree node display. */
    private final TreeTableCellPanel component = new TreeTableCellPanel();

    /**
     * Decorate the component returned from the <code>delegate</code> with
     * extra components that display the tree nodes location within the tree.
     * If <code>delegate</code> is <tt>null</tt> then a
     * {@link DefaultTableCellRenderer} will be used as the delegate.
     *
     * @param delegate the renderer that produces the data for the tree node
     * @param treeList the data structure that answers questions about the tree
     *      node and the tree that contains it
     */
    public TreeTableCellRenderer(TableCellRenderer delegate, TreeList treeList) {
        this.delegate = delegate == null ? createDelegateRenderer() : delegate;
        this.treeList = treeList;
    }

    /**
     * Build the delegate TableCellEditor that handles rendering the data of
     * each tree node.
     */
    protected TableCellRenderer createDelegateRenderer() {
        return new DefaultTableCellRenderer();
    }

    /**
     * Return decorated form of the component returned by the data
     * {@link TableCellRenderer}.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        final int depth;
        final boolean isExpanded;
        final boolean isExpandable;

        treeList.getReadWriteLock().readLock().lock();
        try {
            // read information about the tree node from the TreeList
            depth = treeList.depth(row);
            isExpanded = treeList.isExpanded(row);
            isExpandable = treeList.isExpandable(row);
        } finally {
            treeList.getReadWriteLock().readLock().unlock();
        }

        // ask our special component to configure itself for this tree node
        component.configure(depth, isExpandable, isExpanded, c);
        return component;
    }

    /**
     * Returns the delegate TableCellRenderer being decorated.
     */
    public TableCellRenderer getDelegate() {
        return delegate;
    }

    /**
     * Cleanup the data within this renderer as it is no longer being used and
     * must be prepared for garbage collection.
     */
    public void dispose() {
        this.delegate = null;
        this.treeList = null;
    }
}