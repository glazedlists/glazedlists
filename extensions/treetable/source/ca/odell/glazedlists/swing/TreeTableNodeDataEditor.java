/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.table.TableCellEditor;

/**
 * This interface extends the notion of a normal TableCellEditor to one that is
 * more appropriate for editing the <strong>data</strong> portion of a
 * {@link TreeTableCellEditor}. This is an interface that should only be
 * implemented if editing the data portion of a tree cell requires knowledge
 * about the tree node's hierarchical information.
 */
public interface TreeTableNodeDataEditor extends TableCellEditor {

    /**
     * This method will be called before {@link #getTableCellEditorComponent}
     * in order to provide hierarchy information about the tree node being
     * edited. Implementations should store a reference to the given
     * <code>treeNodeData</code> and use it when producing an editor component.
     *
     * @param treeNodeData an object describing hierarchical information about
     *      the tree node being edited
     */
    public void setTreeNodeData(TreeNodeData treeNodeData);
}