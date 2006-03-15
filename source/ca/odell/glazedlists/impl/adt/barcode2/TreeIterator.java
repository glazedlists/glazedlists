/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

/**
 * Iterate through a {@link Tree}, one element at a time.
 *
 * <p>We should consider enhancing this to iterate one node at a time.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeIterator<V> {

    private final int[] counts;

    private Tree<V> tree;
    private Node<V> node;
    private int index;

    public TreeIterator(Tree<V> tree) {
        this.counts = new int[tree.getCoder().getColors().size()];
        this.tree = tree;
        this.node = null;
        this.index = 0;
    }

    public boolean hasNext(byte colors) {
        if(node != null && (colors & node.color) != 0) {
            return index(colors) < tree.size(colors) - 1;
        } else {
            return index(colors) < tree.size(colors);
        }
    }

    public void next(byte colors) {
        // we could optimize this loop by going through one node
        // at a time rather than one index at a time
        while(true) {
            assert(hasNext(colors));

            // handle the first node in the tree
            if(node == null) {
                node = tree.firstNode();
                index = 0;

            // count the previous quantity, then increment
            } else {
                counts[Tree.colorAsIndex(node.color)]++;
                index++;
            }

            // if we've gone past the current node, prepare for next node
            if(index == node.size) {
                index = 0;
                node = Tree.next(node);
                assert(node.size > 0);
            }

            // we've incremented to a node that meets our requirements
            // so we can quit incrementing
            if((node.color & colors) != 0) break;
        }
    }

    /**
     * The color of the current element.
     */
    public byte color() {
        return node.color;
    }

    /**
     * Expected values for index should be 0, 1, 2, 3..
     */
    public int index(byte colors) {
        int result = 0;
        for(int i = 0; i < counts.length; i++) {
            if((colors & (1 << i)) > 0) result += counts[i];
        }
        return result;
    }

    public V value() {
        return node.get();
    }
}