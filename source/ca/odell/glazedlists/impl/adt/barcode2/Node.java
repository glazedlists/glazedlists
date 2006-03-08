/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.Arrays;

/**
 * A node in a tree which supports both a value and compressed nodes that
 * contain a size, useful for index offsetting.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class Node<V> implements Element<V> {

    /** the number of elements of each color in this subtree */
    final int[] counts;

    /** the node's color */
    final byte color;

    /** the node's value */
    V value;

    /** the size of this node */
    int size;

    /** values for managing the node within the tree */
    byte height;
    Node<V> left, right, parent;

    /**
     * Create a new node.
     *
     * @param colorCount the number of colors in the tree
     * @param color a bitmask value usch as 1, 2, 4, 8 or 16.
     * @param colorAsIndex an index value such as 0, 1, 2, 3 or 4.
     * @param size the size of the node
     * @param value the value of the node
     * @param parent the parent node in the tree, or <code>null</code> for the
     *      root node.
     */
    public Node(int colorCount, byte color, int colorAsIndex, int size, V value, Node<V> parent) {
        this.color = color;
        this.size = size;
        this.value = value;
        this.height = 1;
        this.parent = parent;

        counts = new int[colorCount];
        this.counts[colorAsIndex] = size;
    }

    /**
     * Get the value of this element.
     */
    public V get() {
        return value;
    }

    /**
     * Set the value of this element.
     */
    public void set(V value) {
        this.value = value;
    }

    /**
     * Get the color of this element.
     */
    public byte getColor() {
        return color;
    }

    /**
     * The size of this entire node, including the left child, this node
     * and the right child.
     */
    final int size(byte colors) {
        int result = 0;
        for(int i = 0; i < counts.length; i++) {
            if((colors & (1 << i)) > 0) result += counts[i];
        }
        return result;
    }

    /**
     * The size of the left subtree and this node.
     */
    final int nodeSize(byte colors) {
        return (colors & color) > 0 ? size : 0;
    }

    /**
     * Update the counts member variable by examining the counts of
     * the child nodes and the size member variable.
     */
    final void setCountsFromChildNodesAndSize() {
        for(int colorIndex = 0; colorIndex < counts.length; colorIndex++) {
            int colorTotal = 0;
            if(left != null) colorTotal += left.counts[colorIndex];
            if(right != null) colorTotal += right.counts[colorIndex];
            if((color >> colorIndex) == 1) colorTotal += size;
            counts[colorIndex] = colorTotal;
        }
    }

    /**
     * Convert the specified color value (such as 1, 2, 4, 8, 16 etc.) into an
     * index value (such as 0, 1, 2, 3, 4 etc. ).
     */
    static final int colorAsIndex(byte color) {
        int colorAsIndex = 0;
        for(; (color >> colorAsIndex) != 1; colorAsIndex++) {
            if((color >> colorAsIndex) == 0) throw new IllegalStateException();
        }
        return colorAsIndex;
    }

    /**
     * Find the next node in the tree, working from left to right.
     */
    Node<V> next() {
        // if this node has a right subtree, it's the leftmost node in that subtree
        if(right != null) {
            Node<V> child = right;
            while(child.left != null) {
                child = child.left;
            }
            return child;

        // otherwise its the nearest ancestor where I'm in the left subtree
        } else {
            Node<V> ancestor = this;
            while(ancestor.parent != null && ancestor.parent.right == ancestor) {
                ancestor = ancestor.parent;
            }
            return ancestor.parent;
        }
    }

    /**
     * Find the leftmost child in this subtree.
     */
    Node<V> leftmostChild() {
        Node<V> result = this;
        while(result.left != null) {
            result = result.left;
        }
        return result;
    }

    /** {@inheritDoc} */
    public String toString() {
        return toString(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G", "H"}));
    }

    /**
     * Write this node out as a String, using the specified colors to write
     * each of the node values.
     */
    String toString(List colors) {
        StringBuffer result = new StringBuffer();
        asTree(0, result, colors);
        return result.toString();
    }
    /**
     * Dump this node as a String for diagnostic and debugging purposes.
     */
    void asTree(int indentation, StringBuffer out, List colors) {
        // write the left subtree
        if(left != null) left.asTree(indentation + 1, out, colors);

        // write this node
        for(int i = 0; i < indentation; i++) {
            out.append("   ");
        }
        out.append(colors.get(colorAsIndex(color)));
        if(size > 1) out.append(" [").append(size).append("]");
        if(value != null) out.append(": ").append(value);
        out.append("\n");

        // write the right subtree
        if(right != null) right.asTree(indentation + 1, out, colors);
    }
}