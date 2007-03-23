/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

/**
 * A package-protected implementation of TreeNodeData. Instances of this class
 * should only be constructed by Glazed Lists and users of the class will only
 * access it through its immutable interface, thus it is not necessary to
 * expose this class outside of this package.
 */
class MutableTreeNodeData implements TreeNodeData {

    private int depth;
    private boolean isExpanded;
    private boolean hasChildren;
    private boolean allowsChildren;

    /** @inheritDoc */
    public int getDepth() { return depth; }
    void setDepth(int depth) { this.depth = depth; }

    /** @inheritDoc */
    public boolean isExpanded() { return isExpanded; }
    void setExpanded(boolean expanded) { isExpanded = expanded; }

    /** @inheritDoc */
    public boolean hasChildren() { return hasChildren; }
    void setHasChildren(boolean hasChildren) { this.hasChildren = hasChildren; }

    /** @inheritDoc */
    public boolean allowsChildren() { return allowsChildren; }
    void setAllowsChildren(boolean allowsChildren) { this.allowsChildren = allowsChildren; }
}