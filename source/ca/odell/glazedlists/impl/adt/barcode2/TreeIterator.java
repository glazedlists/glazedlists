/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

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
public class TreeIterator<V> {

    private final int[] counts;

    private Tree<V> tree;
    private Node<V> node;
    private int index;
    private int nodeColorAsIndex;

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
        assert(hasNext(colors));

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            nodeColorAsIndex = Tree.colorAsIndex(node.color);
            index = 0;
            if((node.color & colors) != 0) return;

        // increment within the current node
        } else if((node.color & colors) != 0 && index < node.size - 1) {
            counts[nodeColorAsIndex]++;
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
            counts[nodeColorAsIndex] += node.size - index;
            node = Tree.next(node);
            nodeColorAsIndex = Tree.colorAsIndex(node.color);
            index = 0;

            // we've found a node that meet our requirements, so return
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
     * Expected values for index should be 0, 1, 2, 3...
     */
    public int index(byte colors) {
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
    public V value() {
        return node.get();
    }
}