/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.NoSuchElementException;

/*
 m4_include(source/ca/odell/glazedlists/impl/adt/barcode2/JavaMacros.m4)
 m4_include(source/ca/odell/glazedlists/impl/adt/barcode2/TreeMacros.m4)
*/
/*[ BEGIN_M4_JAVA ]*/

/**
 * Iterate through a {@link BciiTree}, one element at a time.
 *
 * <p>We should consider adding the following enhancements to this class:
 * <li>writing methods, such as <code>set()</code> and <code>remove()</code>.
 * <li>a default color, specified at construction time, that shall always be
 *     used as the implicit parameter to overloaded versions of {@link #hasNext}
 *     and {@link #next}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BciiTreeIterator/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ {

    /*[ GENERATED_CODE_START
    forloop(`i', 0, VAR_LAST_COLOR_INDEX, `int counti(i);
    ')
    GENERATED_CODE_END
    EXAMPLE_START ]*/
    int count1;
    int count2;
    int count4;
    /*[ EXAMPLE_END ]*/

    private BciiTree/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ tree;
    private BciiNode/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ node;
    private int index;

    public BciiTreeIterator/**/(BciiTree/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ tree) {
        this(tree, 0, (byte)0);
    }

    /**
     * Create an iterator starting at the specified index.
     *
     * @param tree the tree to iterate
     * @param nextIndex the index to be returned after calling {@link #next next()}.
     * @param nextIndexColors the colors to interpret nextIndex in terms of
     */
    public BciiTreeIterator/**/(BciiTree/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ tree, int nextIndex, byte nextIndexColors) {
        this.tree = tree;

        // if the start is, we need to find the node in the tree
        if(nextIndex != 0) {
            int currentIndex = nextIndex - 1;
            this.node = (/*[ NODENAME_START ]*/ BciiNode<T0,T1> /*[ NODENAME_END ]*/)tree.get(currentIndex /*[ COLORED_START ]*/, nextIndexColors /*[ COLORED_END ]*/);

            // find the counts
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 = currentIndex;
            ', counti(i)` = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)'indexToBit(i)`) + (node.color == 'indexToBit(i)` ? 0 : 1);
            ')')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            count1 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)1) + (node.color == 1 ? 0 : 1);
            count2 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)2) + (node.color == 2 ? 0 : 1);
            count4 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)4) + (node.color == 4 ? 0 : 1);
            /*[ EXAMPLE_END ]*/

            // find out the index in the node
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            ',`if(node.color == 'indexToBit(i)`) this.index = 'counti(i)` - tree.indexOfNode(this.node, '(byte)indexToBit(i)`);
            ')')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            if(node.color == 1) this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            if(node.color == 2) this.index = count2 - tree.indexOfNode(this.node, (byte)2);
            if(node.color == 4) this.index = count4 - tree.indexOfNode(this.node, (byte)4);
            /*[ EXAMPLE_END ]*/

        // just start before the beginning of the tree
        } else {
            this.node = null;
            this.index = 0;
        }
    }

    /**
     * Create a {@link BciiTreeIterator} exactly the same as this one.
     * The iterators will be backed by the same tree but maintain
     * separate cursors into the tree.
     */
    public BciiTreeIterator/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ copy() {
        BciiTreeIterator/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/ result = new BciiTreeIterator/*[ TYPELIST_START ]*/ <T0,T1> /*[ TYPELIST_END ]*/(tree);

        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `result.counti(i) = this.counti(i);
        ')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        result.count1 = this.count1;
        result.count2 = this.count2;
        result.count4 = this.count4;
        /*[ EXAMPLE_END ]*/

        result.node = node;
        result.index = index;
        return result;
    }

    /**
     * @return <code>true</code> if there's an element of the specified color in
     *     this tree following the current element.
     */
    public boolean hasNext(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(node == null) {
            return tree.size(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) > 0;
        } else if(/*[ COLORED_START(true) ]*/ (colors & node.color) != 0 /*[ COLORED_END ]*/) {
            return index(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) < tree.size(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) - 1;
        } else {
            return index(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) < tree.size(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/);
        }
    }

    /**
     * @return <code>true</code> if there's a node of the specified color in this
     *      tree following the current node.
     */
    public boolean hasNextNode(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(node == null) {
            return tree.size(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) > 0;
        } else {
            return nodeEndIndex(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/) < tree.size(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/);
        }
    }

    /**
     * Step to the next element.
     */
    public void next(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(!hasNext(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/)) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
            /*[ COLORED_START ]*/if((node.color & colors) != 0) /*[ COLORED_END ]*/ return;

        // increment within the current node
        } else if(/*[ COLORED_START ]*/ (node.color & colors) != 0 && /*[ COLORED_END ]*/ index < /*[ WIDE_NODES_START(1) ]*/ node.size /*[ WIDE_NODES_END ]*/ - 1) {
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1++;
            ', `if(node.color == indexToBit(i)) counti(i)++;
            ')')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            if(node.color == 1) count1++;
            if(node.color == 2) count2++;
            if(node.color == 4) count4++;
            /*[ EXAMPLE_END ]*/
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 += NODE_SIZE(node, colors) - index;
            ', `if(node.color == indexToBit(i)) counti(i) += node.size - index;
            ')')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            /*[ EXAMPLE_END ]*/
            node = BciiTree.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
            /*[ COLORED_START ]*/ if((node.color & colors) != 0) /*[ COLORED_END ]*/ break;
        }
    }

    /**
     * Step to the next node.
     */
    public void nextNode(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(!hasNextNode(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/)) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
            /*[ COLORED_START ]*/if((node.color & colors) != 0) /*[ COLORED_END ]*/ return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
            /*[ GENERATED_CODE_START
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 += NODE_SIZE(node, colors) - index;
            ', `if(node.color == indexToBit(i)) counti(i) += node.size - index;
            ')')
            GENERATED_CODE_END
            EXAMPLE_START ]*/
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            /*[ EXAMPLE_END ]*/
            node = BciiTree.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
            /*[ COLORED_START ]*/ if((node.color & colors) != 0) /*[ COLORED_END ]*/ break;
        }
    }


    /**
     * Get the size of the current node, or 0 if it's color doesn't match those
     * specified.
     */
    public int nodeSize(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(/*[ COLORED_START(true) ]*/ (node.color & colors) != 0 /*[ COLORED_END ]*/) {
            return /*[ WIDE_NODES_START(1) ]*/ node.size /*[ WIDE_NODES_END ]*/;
        } else {
            return 0;
        }
    }

    /*[ COLORED_START ]*/
    /**
     * The color of the current element.
     */
    public byte color() {
        if(node == null) throw new IllegalStateException();
        return node.color;
    }
    /*[ COLORED_END ]*/

    /**
     * Expected values for index should be in the range  ( 0, size() - 1 )
     */
    public int index(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(node == null) throw new NoSuchElementException();

        // total the values of the specified array for the specified colors.
        int result = 0;

        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`result += count1;
        ', `if((colors & indexToBit(i)) != 0) result += counti(i);
        ')')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        /*[ EXAMPLE_END ]*/
        return result;
    }
    /**
     * Get the index of the current node's start.
     */
    public int nodeStartIndex(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(node == null) throw new NoSuchElementException();

        // the count of all nodes prior to this one
        int result = 0;

        // this should merely be the sum of each count
        /*[ GENERATED_CODE_START
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`result += count1;
        ', `if((colors & indexToBit(i)) != 0) result += counti(i);
        ')')
        GENERATED_CODE_END
        EXAMPLE_START ]*/
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        /*[ EXAMPLE_END ]*/

        // subtract the count of anything in the current node which we may
        // have included inadvertently
        if(/*[ COLORED_START(true) ]*/ (node.color & colors) != 0 /*[ COLORED_END ]*/) {
            result -= index;
        }

        return result;
    }
    /**
     * Get the index of the node immediately following the current. Expected
     * values are in the range ( 1, size() )
     */
    public int nodeEndIndex(/*[ COLORED_START ]*/ byte colors /*[ COLORED_END ]*/) {
        if(node == null) throw new NoSuchElementException();

        // the count of all nodes previous
        return nodeStartIndex(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/)
                + nodeSize(/*[ COLORED_START ]*/ colors /*[ COLORED_END ]*/);
    }
    public T0 value() {
        if(node == null) throw new IllegalStateException();
        return node.get();
    }
    public Element<T0> node() {
        if(node == null) throw new IllegalStateException();
        return node;
    }
}
/*[ END_M4_JAVA ]*/
