/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.table.TableCellRenderer;

/**
 * This interface extends the notion of a normal TableCellRenderer to one that
 * is more appropriate for rendering the <strong>data</strong> portion of a
 * {@link TreeTableCellRenderer}. This is an interface that should only be
 * implemented if rendering the data portion of a tree cell requires knowledge
 * about the tree node's hierarchical information.
 */
public interface TreeTableNodeDataRenderer extends TableCellRenderer {

    /**
     * This method will be called before {@link #getTableCellRendererComponent}
     * in order to provide hierarchy information about the tree node being
     * rendered. Implementations should store a reference to the given
     * <code>treeNodeData</code> and use it when producing an renderer component.
     *
     * @param treeNodeData an object describing hierarchical information about
     *      the tree node being rendered
     */
    public void setTreeNodeData(TreeNodeData treeNodeData);
}