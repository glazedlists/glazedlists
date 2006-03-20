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
 * <p>Note that the <code>counts</code> summary member is created lazily when
 * this node is given children. This causes the code to be less easy to read,
 * but it means we can put off about a huge number of object allocations since
 * 50% of the nodes in an arbitrary tree are leaf nodes, and these leaf nodes
 * now don't have counts.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class Node<V> implements Element<V> {

    /** the number of elements of each color in this subtree */
    int[] counts;

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
     * @param color a bitmask value such as 1, 2, 4, 8 or 16.
     * @param size the size of the node
     * @param value the value of the node
     * @param parent the parent node in the tree, or <code>null</code> for the
     *      root node.
     */
    public Node(byte color, int size, V value, Node<V> parent) {
        assert(Tree.colorAsIndex(color) >= 0 && Tree.colorAsIndex(color) < 7);
        this.color = color;
        this.size = size;
        this.value = value;
        this.height = 1;
        this.parent = parent;
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
        if(counts == null) {
            return (colors & color) != 0 ? size : 0;
        } else {
            // total the values of the specified array for the specified colors.
            int result = 0;

            if((colors & 1) != 0) result += counts[0];
            if(counts.length == 1) return result;

            if((colors & 2) != 0) result += counts[1];
            if(counts.length == 2) return result;

            if((colors & 4) != 0) result += counts[2];
            if(counts.length == 3) return result;

            if((colors & 8) != 0) result += counts[3];
            if(counts.length == 4) return result;

            if((colors & 16) != 0) result += counts[4];
            if(counts.length == 5) return result;

            if((colors & 32) != 0) result += counts[5];
            if(counts.length == 6) return result;

            if((colors & 64) != 0) result += counts[6];
            if(counts.length == 7) return result;

            throw new IllegalStateException();
        }
    }

    /**
     * The size of the node for the specified colors.
     */
    final int nodeSize(byte colors) {
        return (colors & color) > 0 ? size : 0;
    }

    /**
     * Update the counts member variable by examining the counts of
     * the child nodes and the size member variable.
     *
     * @param colorCount the number of colors in the tree
     */
    final void refreshCounts(int colorCount) {
        // if we have a child node, we need a valid counts array
        if(left != null || right != null) {
            if(counts == null) counts = new int[colorCount];
            for(int colorIndex = 0; colorIndex < counts.length; colorIndex++) {
                int colorTotal = 0;
                if(left != null && left.counts != null) colorTotal += left.counts[colorIndex];
                if(right != null && right.counts != null) colorTotal += right.counts[colorIndex];
                counts[colorIndex] = colorTotal;
            }
            counts[Tree.colorAsIndex(color)] += size;
            if(left != null && left.counts == null) counts[Tree.colorAsIndex(left.color)] += left.size;
            if(right != null && right.counts == null) counts[Tree.colorAsIndex(right.color)] += right.size;

        // we don't have a child node yet, the counts array may be null
        } else {
            if(counts != null) {
                for(int c = 0; c < colorCount; c++) {
                    counts[c] = 0;
                }
                counts[Tree.colorAsIndex(color)] = size;
            }
        }
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
        out.append(colors.get(Tree.colorAsIndex(color)));
        out.append(" [").append(size).append("]");
        if(value != null) out.append(": ").append(value);
        out.append("\n");

        // write the right subtree
        if(right != null) right.asTree(indentation + 1, out, colors);
    }
}