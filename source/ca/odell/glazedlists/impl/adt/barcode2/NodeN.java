/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

/*
 M4 Macros

STANDARD M4 LOOP ---------------------------------------------------------------

m4_divert(-1)
# forloop(i, from, to, stmt)

m4_define(`forloop', `m4_pushdef(`$1', `$2')_forloop(`$1', `$2', `$3', `$4')m4_popdef(`$1')')
m4_define(`_forloop',
       `$4`'m4_ifelse($1, `$3', ,
             `m4_define(`$1', m4_incr($1))_forloop(`$1', `$2', `$3', `$4')')')
m4_divert

MACRO CODE WITH A JAVA ALTERNATIVE ---------------------------------------------
m4_define(`BEGIN_M4_MACRO', ` BEGIN M4 MACRO GENERATED CODE *'`/')
m4_define(`END_M4_MACRO', `/'`* END M4 MACRO GENERATED CODE ')
m4_define(`BEGIN_M4_ALTERNATE', `BEGIN M4 ALTERNATE CODE
/'`* ')
m4_define(`END_M4_ALTERNATE', `END ALTERNATE CODE *'`/')

NODE SPECIFIC VARIABLES & FUNCTIONS--- -----------------------------------------
m4_define(`VAR_LAST_COLOR_INDEX', `m4_eval(VAR_COLOUR_COUNT-1)')
m4_define(`originalCounti', ``originalCount'indexToBit($1)')
m4_define(`indexToBit', `m4_eval(`2 ** $1')')
m4_define(`NodeN', ``Node'VAR_COLOUR_COUNT')
m4_define(`TreeN', ``Tree'VAR_COLOUR_COUNT')
m4_define(`TreeNAsList', ``Tree'VAR_COLOUR_COUNT`AsList'')
m4_define(`TreeNIterator', ``Tree'VAR_COLOUR_COUNT`Iterator'')
m4_define(`counti', ``count'indexToBit($1)')

USE ALTERNATE CODE WHEN WE ONLY HAVE ONE COLOR ---------------------------------
m4_define(`SINGLE_ALTERNATE', m4_ifelse(VAR_COLOUR_COUNT,`1',`USE SINGLE ALTERNATE *'`/ '$1`
// IGNORE DEFAULT:',`USE DEFAULT'))
m4_define(`END_SINGLE_ALTERNATE', m4_ifelse(VAR_COLOUR_COUNT,`1',`
/'`* END SINGLE ALTERNATE',`END DEFAULT'))

SKIP SECTIONS OF CODE WHEN WE ONLY HAVE ONE COLOR ------------------------------
m4_define(`BEGIN_SINGLE_SKIP', m4_ifelse(VAR_COLOUR_COUNT,`1',`
/'`* BEGIN SINGLE SKIPPED CODE '))
m4_define(`END_SINGLE_SKIP', m4_ifelse(VAR_COLOUR_COUNT,`1',`END SINGLE SKIPPED CODE *'`/'))

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
class NodeN<V> implements Element<V> {

    /** the number of elements of each color in this subtree */
    /* BEGIN_M4_MACRO
    forloop(`i', 0, VAR_LAST_COLOR_INDEX, `int counti(i);
    ')
    END_M4_MACRO */ // BEGIN_M4_ALTERNATE
    int count1;
    int count2;
    int count4;
    // END_M4_ALTERNATE

    /** the node's color */
    /* SINGLE_ALTERNATE */ final byte color; /* END_SINGLE_ALTERNATE */


    /** the node's value */
    V value;

    /** the size of this node */
    int size;

    /** values for managing the node within the tree */
    byte height;
    NodeN/**/<V> left, right, parent;

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
    public NodeN/**/(/* SINGLE_ALTERNATE */ byte color, /* END_SINGLE_ALTERNATE */ int size, V value, NodeN/**/<V> parent) {
        // BEGIN_SINGLE_SKIP
        assert(TreeN.colorAsIndex(color) >= 0 && TreeN.colorAsIndex(color) < 7);
        this.color = color;
        // END_SINGLE_SKIP
        this.size = size;
        this.value = value;
        this.height = 1;
        this.parent = parent;

        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 += size;
        ', `if(color == indexToBit(i)) counti(i) += size;
        ')')
        END_M4_MACRO */ // BEGIN_M4_ALTERNATE
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        // END_M4_ALTERNATE
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
        return /* SINGLE_ALTERNATE(`1') */ color /* END_SINGLE_ALTERNATE */;
    }

    /**
     * The size of this entire node, including the left child, this node
     * and the right child.
     */
    final int size(byte colors) {
        // total the values of the specified array for the specified colors.
        int result = 0;

        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `if((colors & indexToBit(i)) != 0) result += counti(i);
        ')
        END_M4_MACRO */ // BEGIN_M4_ALTERNATE
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        // END_M4_ALTERNATE
        return result;
    }

    /**
     * The size of the node for the specified colors.
     */
    // BEGIN_SINGLE_SKIP
    final int nodeSize(byte colors) {
        return (colors & color) > 0 ? size : 0;
    }
    // END_SINGLE_SKIP

    /**
     * Update the counts member variable by examining the counts of
     * the child nodes and the size member variable.
     */
    final void refreshCounts() {

        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) = 0;
        ')
        END_M4_MACRO */ // BEGIN_M4_ALTERNATE
        count1 = 0;
        count2 = 0;
        count4 = 0;
        // END_M4_ALTERNATE

        // left child
        if(left != null) {
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) += left.counti(i);
            ')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            count1 += left.count1;
            count2 += left.count2;
            count4 += left.count4;
            // END_M4_ALTERNATE
        }

        // right child
        if(right != null) {
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `counti(i) += right.counti(i);
            ')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            count1 += right.count1;
            count2 += right.count2;
            count4 += right.count4;
            // END_M4_ALTERNATE
        }

        // this node
        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',counti(i)` += size;
        ', `if(color == 'indexToBit(i)`) 'counti(i)` += size;
        ')')
        END_M4_MACRO */ // BEGIN_M4_ALTERNATE
        if(color == 1) count1 += size;
        if(color == 2) count2 += size;
        if(color == 4) count4 += size;
        // END_M4_ALTERNATE

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
        /* SINGLE_ALTERNATE */ out.append(colors.get(TreeN.colorAsIndex(color))); /* END_SINGLE_ALTERNATE */
        out.append(" [").append(size).append("]");
        if(value != null) {
            out.append(": ");
            if(value instanceof NodeN) {
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
        return TreeN.next(this);
    }

    /** {@inheritDoc} */
    public Element<V> previous() {
        return TreeN.previous(this);
    }
}