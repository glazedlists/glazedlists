/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

/**
 * A convenient editor that eases the implementation of
 * TreeTableNodeDataRenderer by providing convenient access methods to the data
 * within the given {@link ca.odell.glazedlists.swing.TreeNodeData}.
 */
public abstract class AbstractTreeTableNodeDataEditor implements TreeTableNodeDataEditor {

    private TreeNodeData treeNodeData;

    /** @inheritDoc */
    public final void setTreeNodeData(TreeNodeData treeNodeData) {
        this.treeNodeData = treeNodeData;
    }

    /**
     * Returns the depth of the tree node in the hierarchy.
     */
    protected int getDepth() { return treeNodeData.getDepth(); }

    /**
     * Returns <tt>true</tt> if the tree node has child nodes; <tt>false</tt>
     * otherwise.
     */
    protected boolean hasChildren() { return treeNodeData.hasChildren(); }

    /**
     * Returns <tt>true</tt> if the node is of the type that can have child
     * elements; <tt>false</tt> otherwise.
     */
    protected boolean allowsChildren() { return treeNodeData.allowsChildren(); }

    /**
     * Returns <tt>true</tt> if the node is expanded and its children are thus
     * visible; <tt>false</tt> if it is collapsed and its children are thus
     * hidden. This argument only has meaning when {@link #hasChildren()}
     * returns <tt>true</tt>; otherwise it should be ignored.
     */
    protected boolean isExpanded() { return treeNodeData.isExpanded(); }
}