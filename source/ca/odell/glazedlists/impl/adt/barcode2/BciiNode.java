/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.Arrays;

/*
 m4_include(source/ca/odell/glazedlists/impl/adt/barcode2/JavaMacros.m4)
 m4_include(source/ca/odell/glazedlists/impl/adt/barcode2/TreeMacros.m4)
*/
/*[ BEGIN_M4_JAVA ]*/

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
class BciiNode<V> implements Element<V> {

    /** the number of elements of each color in this subtree */
    /*[ GENERATED_CODE_START
    forloop(`i', 0, VAR_LAST_COLOR_INDEX, `int counti(i);
    ')
    GENERATED_CODE_END
    EXAMPLE_START ]*/
    int count1;
    int count2;
    int count4;
    /*[ EXAMPLE_END ]*/

    /*[ COLORED_START ]*/
    /** the node's color */
    final byte color;
    /*[ COLORED_END ]*/


    /** the node's value */
    V value;

    /*[ WIDE_NODES_START ]*/
    /** the size of this node */
    int size;
    /*[ WIDE_NODES_END ]*/

    /** values for managing the node within the tree */
    byte height;
    BciiNode<V> left, right, parent;

    /** whether this node is consistent in the sorting order */
    boolean sorted = true;

    /**
     * Create a new node.
     *
     * @param color a bitmask value such as 1, 2, 4, 8 or 16.
     * @param size the size of the node
     * @param value the value of the node
     * @param parent the parent node in the tree, or <code>null</code> for the
     *      root node.
     */
    public BciiNode/**/(/*[ COLORED_START ]*/ byte color, /*[ COLORED_END ]*/ int size, V value, BciiNode/**/<V> parent) {
        /*[ COLORED_START ]*/
        assert(BciiTree.colorAsIndex(color) >= 0 && BciiTree.colorAsIndex(color) < 7);
        this.color = color;
        /*[ COLORED_END ]*/
        /*[ WIDE_NODES_START(assert(size == 1);) ]*/
        this.size = size;
        /*[ WIDE_NODES_END ]*/
        this.value = value;
        this.height = 1;
        this.parent = parent;

        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 += size;
        ', `if(color == indexToBit(i)) counti(i) += size;
        ')')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        /*[ EXAMPLE_END ]*/
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
        return /*[ COLORED_START(1) ]*/ color /*[ COLORED_END ]*/;
    }

    /**
     * The size of this entire node, including the left child, this node
     * and the right child.
     */
    final int size(byte colors) {
        // total the values of the specified array for the specified colors.
        int result = 0;

        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `if((colors & indexToBit(i)) != 0) result += counti(i);
        ')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        /*[ EXAMPLE_END ]*/
        return result;
    }

    /*[ COLORED_START ]*/
    /**
     * The size of the node for the specified colors.
     */
    final int nodeSize(byte colors) {
        return (colors & color) > 0 ? size : 0;
    }
    /*[ COLORED_END ]*/

    /**
     * Update the counts member variable by examining the counts of
     * the child nodes and the size member variable.
     */
    final void refreshCounts(/*[ WIDE_NODES_START(boolean countSelf) WIDE_NODES_END ]*/) {

        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) = 0;
        ')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        count1 = 0;
        count2 = 0;
        count4 = 0;
        /*[ EXAMPLE_END ]*/

        // left child
        if(left != null) {
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) += left.counti(i);
            ')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            count1 += left.count1;
            count2 += left.count2;
            count4 += left.count4;
            /*[ EXAMPLE_END ]*/
        }

        // right child
        if(right != null) {
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) += right.counti(i);
            ')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            count1 += right.count1;
            count2 += right.count2;
            count4 += right.count4;
            /*[ EXAMPLE_END ]*/
        }

        // this node
        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',counti(i)` += 'NODE_WIDTH(countSelf)`;
        ', `if(color == 'indexToBit(i)`) 'counti(i)` += 'NODE_WIDTH(countSelf)`;
        ')')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        /*[ EXAMPLE_END ]*/
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
        /*[ COLORED_START ]*/ out.append(colors.get(BciiTree.colorAsIndex(color))); /*[ COLORED_END ]*/
        /*[ WIDE_NODES_START ]*/ out.append(" [").append(size).append("]"); /*[ WIDE_NODES_END ]*/
        if(value != null) {
            out.append(": ");
            if(value instanceof BciiNode) {
                out.append("<Node>");
            } else {
                out.append(value);
            }
        }
        out.append("\n");

        // write the right subtree
        if(right != null) right.asTree(indentation + 1, out, colors);
    }

    /**
     * Toggle whether this node is sorted.
     */
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    /**
     * Get whether the value of this node is greater than the previous node
     * and less than the next node. This is useful to have occasional unsorted
     * elements in an otherwise sorted collection, such as what happens when the
     * user expects order to be both sorted and stable during edits which would
     * otherwise change the sorting order.
     */
    public boolean isSorted() {
        return sorted;
    }

    /** {@inheritDoc} */
    public Element<V> next() {
        return BciiTree.next(this);
    }

    /** {@inheritDoc} */
    public Element<V> previous() {
        return BciiTree.previous(this);
    }
}
/*[ END_M4_JAVA ]*/
