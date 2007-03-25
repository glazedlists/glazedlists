/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

/**
 * Instances of this class should only be constructed by Glazed Lists.
 * It describes information to related to the tree node that is about to be
 * rendered or edited. Special renderers and editors,
 * {@link TreeTableNodeDataEditor} and {@link TreeTableNodeDataRenderer}, will
 * receive instances of this class. Renderers and editors that require
 * hierarchical information about tree nodes to produce the correct component
 * should implement one of those editors in order to receive one of these
 * objects detailing the node's information.
 */
public final class TreeNodeData {

    private int depth;
    private boolean isExpanded;
    private boolean hasChildren;
    private boolean allowsChildren;

    /**
     * Returns the depth of the tree node in the hierarchy.
     */
    public int getDepth() { return depth; }
    void setDepth(int depth) { this.depth = depth; }

    /**
     * Returns <tt>true</tt> if the node is expanded and its children are thus
     * visible; <tt>false</tt> if it is collapsed and its children are thus
     * hidden. This argument only has meaning when {@link #hasChildren()}
     * returns <tt>true</tt>; otherwise it should be ignored.
     */
    public boolean isExpanded() { return isExpanded; }
    void setExpanded(boolean expanded) { isExpanded = expanded; }

    /**
     * Returns <tt>true</tt> if the tree node has child nodes; <tt>false</tt>
     * otherwise.
     */
    public boolean hasChildren() { return hasChildren; }
    void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }

    /**
     * Returns <tt>true</tt> if the node is of the type that can have child
     * elements; <tt>false</tt> otherwise.
     */
    public boolean allowsChildren() { return allowsChildren; }
    void setAllowsChildren(boolean allowsChildren) { this.allowsChildren = allowsChildren; }
}