/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

/*
 M4 Macros

STANDARD M4 LOOP ---------------------------------------------------------------



MACRO CODE WITH A JAVA ALTERNATIVE ---------------------------------------------





NODE SPECIFIC VARIABLES & FUNCTIONS--- -----------------------------------------









USE ALTERNATE CODE WHEN WE ONLY HAVE ONE COLOR ---------------------------------



SKIP SECTIONS OF CODE WHEN WE ONLY HAVE ONE COLOR ------------------------------



*/

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
class Node4<V> implements Element<V> {

    /** the number of elements of each color in this subtree */
    /*  BEGIN M4 MACRO GENERATED CODE */
    int count1;
    int count2;
    int count4;
    int count8;
    
    /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
    int count1;
    int count2;
    int count4;
    // END ALTERNATE CODE */

    /** the node's color */
    /* USE DEFAULT */ final byte color; /* END DEFAULT */


    /** the node's value */
    V value;

    /** the size of this node */
    int size;

    /** values for managing the node within the tree */
    byte height;
    Node4/**/<V> left, right, parent;

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
    public Node4/**/(/* USE DEFAULT */ byte color, /* END DEFAULT */ int size, V value, Node4/**/<V> parent) {
        // 
        assert(Tree4.colorAsIndex(color) >= 0 && Tree4.colorAsIndex(color) < 7);
        this.color = color;
        // 
        this.size = size;
        this.value = value;
        this.height = 1;
        this.parent = parent;

        /*  BEGIN M4 MACRO GENERATED CODE */
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        if(color == 8) count8 += size;
        
        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        // END ALTERNATE CODE */
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
        return /* USE DEFAULT */ color /* END DEFAULT */;
    }

    /**
     * The size of this entire node, including the left child, this node
     * and the right child.
     */
    final int size(byte colors) {
        // total the values of the specified array for the specified colors.
        int result = 0;

        /*  BEGIN M4 MACRO GENERATED CODE */
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        if((colors & 8) != 0) result += count8;
        
        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        // END ALTERNATE CODE */
        return result;
    }

    /**
     * The size of the node for the specified colors.
     */
    // 
    final int nodeSize(byte colors) {
        return (colors & color) > 0 ? size : 0;
    }
    // 

    /**
     * Update the counts member variable by examining the counts of
     * the child nodes and the size member variable.
     */
    final void refreshCounts() {

        /*  BEGIN M4 MACRO GENERATED CODE */
        count1 = 0;
        count2 = 0;
        count4 = 0;
        count8 = 0;
        
        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
        count1 = 0;
        count2 = 0;
        count4 = 0;
        // END ALTERNATE CODE */

        // left child
        if(left != null) {
            /*  BEGIN M4 MACRO GENERATED CODE */
            count1 += left.count1;
            count2 += left.count2;
            count4 += left.count4;
            count8 += left.count8;
            
            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
            count1 += left.count1;
            count2 += left.count2;
            count4 += left.count4;
            // END ALTERNATE CODE */
        }

        // right child
        if(right != null) {
            /*  BEGIN M4 MACRO GENERATED CODE */
            count1 += right.count1;
            count2 += right.count2;
            count4 += right.count4;
            count8 += right.count8;
            
            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
            count1 += right.count1;
            count2 += right.count2;
            count4 += right.count4;
            // END ALTERNATE CODE */
        }

        // this node
        /*  BEGIN M4 MACRO GENERATED CODE */
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        if(color == 8) count8 += size;
        
        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        // END ALTERNATE CODE */

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
        /* USE DEFAULT */ out.append(colors.get(Tree4.colorAsIndex(color))); /* END DEFAULT */
        out.append(" [").append(size).append("]");
        if(value != null) {
            out.append(": ");
            if(value instanceof Node4) {
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
        return Tree4.next(this);
    }

    /** {@inheritDoc} */
    public Element<V> previous() {
        return Tree4.previous(this);
    }
}