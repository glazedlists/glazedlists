/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

/**
 * This interface is used to report all of the data that is known about a given
 * tree node which is being rendered or edited. If a renderer or editor needs
 * information about where a node is located in the hierarchy or other
 * attributes about that node, they can implement the
 * {@link ca.odell.glazedlists.swing.TreeTableNodeDataRenderer} or
 * {@link ca.odell.glazedlists.swing.TreeTableNodeDataEditor} interfaces and
 * receive an instance of this interface which answers hierarchy questions
 * about the tree node they will be rendering or editing.
 */
public interface TreeNodeData {

    /**
     * Returns the depth of the tree node in the hierarchy.
     */
    public int getDepth();

    /**
     * Returns <tt>true</tt> if the tree node has child nodes; <tt>false</tt>
     * otherwise.
     */
    public boolean hasChildren();

    /**
     * Returns <tt>true</tt> if the node is of the type that can have child
     * elements; <tt>false</tt> otherwise.
     */
    public boolean allowsChildren();

    /**
     * Returns <tt>true</tt> if the node is expanded and its children are thus
     * visible; <tt>false</tt> if it is collapsed and its children are thus
     * hidden. This argument only has meaning when {@link #hasChildren()}
     * returns <tt>true</tt>; otherwise it should be ignored.
     */
    public boolean isExpanded();
}