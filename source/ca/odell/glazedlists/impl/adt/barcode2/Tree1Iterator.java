/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.NoSuchElementException;

/*
 M4 Macros








 Barcode2 Macros










*/

/**
 * Iterate through a {@link Tree}, one element at a time.
 *
 * <p>We should consider adding the following enhancements to this class:
 * <li>writing methods, such as <code>set()</code> and <code>remove()</code>.
 * <li>a default color, specified at construction time, that shall always be
 *     used as the implicit parameter to overloaded versions of {@link #hasNext}
 *     and {@link #next}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree1Iterator<V> {

    /*  BEGIN M4 MACRO GENERATED CODE */
    int count1;

    /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
    int count1;
    int count2;
    int count4;
    // END ALTERNATE CODE */

    private Tree1<V> tree;
    private Node1<V> node;
    private int index;

    public Tree1Iterator/**/(Tree1<V> tree) {
        this(tree, 0, (byte)0);
    }

    /**
     * Create an iterator starting at the specified index.
     *
     * @param tree the tree to iterate
     * @param nextIndex the index to be returned after calling {@link #next next()}.
     * @param nextIndexColors the colors to interpret nextIndex in terms of
     */
    public Tree1Iterator/**/(Tree1<V> tree, int nextIndex, byte nextIndexColors) {
        this.tree = tree;

        // if the start is, we need to find the node in the tree
        if(nextIndex != 0) {
            int currentIndex = nextIndex - 1;
            this.node = (Node1<V>)tree.get(currentIndex, nextIndexColors);

            // find the counts
            /*  BEGIN M4 MACRO GENERATED CODE */
            count1 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)1) + (node.color == 1 ? 0 : 1);

            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
            count1 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)1) + (node.color == 1 ? 0 : 1);
            count2 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)2) + (node.color == 2 ? 0 : 1);
            count4 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)4) + (node.color == 4 ? 0 : 1);
            // END ALTERNATE CODE */

            // find out the index in the node
            /*  BEGIN M4 MACRO GENERATED CODE */
            if(node.color == 1) this.index = count1 - tree.indexOfNode(this.node, (byte)1);

            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
            if(node.color == 1) this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            if(node.color == 2) this.index = count2 - tree.indexOfNode(this.node, (byte)2);
            if(node.color == 4) this.index = count4 - tree.indexOfNode(this.node, (byte)4);
            // END ALTERNATE CODE */

        // just start before the beginning of the tree
        } else {
            this.node = null;
            this.index = 0;
        }
    }

    /**
     * Create a {@link TreeIterator} exactly the same as this one.
     * The iterators will be backed by the same tree but maintain
     * separate cursors into the tree.
     */
    public Tree1Iterator<V> copy() {
        Tree1Iterator<V> result = new Tree1Iterator<V>(tree);

        /*  BEGIN M4 MACRO GENERATED CODE */
        result.count1 = this.count1;

        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
        result.count1 = this.count1;
        result.count2 = this.count2;
        result.count4 = this.count4;
        // END ALTERNATE CODE */

        result.node = node;
        result.index = index;
        return result;
    }

    public boolean hasNext(byte colors) {
        if(node == null) {
            return tree.size(colors) > 0;
        } else if((colors & node.color) != 0) {
            return index(colors) < tree.size(colors) - 1;
        } else {
            return index(colors) < tree.size(colors);
        }
    }

    public void next(byte colors) {
        // first make sure there's an element to return
        if(!hasNext(colors)) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
            if((node.color & colors) != 0) return;

        // increment within the current node
        } else if((node.color & colors) != 0 && index < node.size - 1) {
            /*  BEGIN M4 MACRO GENERATED CODE */
            if(node.color == 1) count1++;

            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
            if(node.color == 1) count1++;
            if(node.color == 2) count2++;
            if(node.color == 4) count4++;
            // END ALTERNATE CODE */
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
            /*  BEGIN M4 MACRO GENERATED CODE */
            if(node.color == 1) count1 += node.size - index;

            /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/*
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            // END ALTERNATE CODE */
            node = Tree1.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
            if((node.color & colors) != 0) break;
        }
    }

    /**
     * The color of the current element.
     */
    public byte color() {
        if(node == null) throw new IllegalStateException();
        return node.color;
    }

    /**
     * Expected values for index should be 0, 1, 2, 3...
     */
    public int index(byte colors) {
        if(node == null) throw new NoSuchElementException();

        // total the values of the specified array for the specified colors.
        int result = 0;

        /*  BEGIN M4 MACRO GENERATED CODE */
        if((colors & 1) != 0) result += count1;
        
        /* END M4 MACRO GENERATED CODE  */ // BEGIN M4 ALTERNATE CODE
/* 
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        // END ALTERNATE CODE */
        return result;
    }
    public V value() {
        if(node == null) throw new IllegalStateException();
        return node.get();
    }
    public Element<V> node() {
        if(node == null) throw new IllegalStateException();
        return node;
    }
}