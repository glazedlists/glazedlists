/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

/*
 M4 Macros

m4_divert(-1)
# forloop(i, from, to, stmt)

m4_define(`forloop', `m4_pushdef(`$1', `$2')_forloop(`$1', `$2', `$3', `$4')m4_popdef(`$1')')
m4_define(`_forloop',
       `$4`'m4_ifelse($1, `$3', ,
             `m4_define(`$1', m4_incr($1))_forloop(`$1', `$2', `$3', `$4')')')
m4_divert

m4_define(`BEGIN_M4_MACRO', ` BEGIN M4 MACRO GENERATED CODE *'`/')
m4_define(`END_M4_MACRO', `/'`* END M4 MACRO GENERATED CODE ')
m4_define(`BEGIN_M4_ALTERNATE', `BEGIN M4 ALTERNATE CODE
/'`* ')
m4_define(`END_M4_ALTERNATE', `END ALTERNATE CODE *'`/')

 Barcode2 Macros

m4_define(`VAR_LAST_COLOR_INDEX', `m4_eval(VAR_COLOUR_COUNT-1)')
m4_define(`originalCounti', ``originalCount'indexToBit($1)')
m4_define(`indexToBit', `m4_eval(`2 ** $1')')
m4_define(`NodeN', ``Node'VAR_COLOUR_COUNT')
m4_define(`TreeN', ``Tree'VAR_COLOUR_COUNT')
m4_define(`TreeNAsList', ``Tree'VAR_COLOUR_COUNT`AsList'')
m4_define(`TreeNIterator', ``Tree'VAR_COLOUR_COUNT`Iterator'')
m4_define(`counti', ``count'indexToBit($1)')

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
public class TreeNIterator<V> {

    /* BEGIN_M4_MACRO
    forloop(`i', 0, VAR_LAST_COLOR_INDEX, `int counti(i);
    ')
    END_M4_MACRO */ // BEGIN_M4_ALTERNATE
    int count1;
    int count2;
    int count4;
    // END_M4_ALTERNATE

    private TreeN<V> tree;
    private NodeN<V> node;
    private int index;

    public TreeNIterator/**/(TreeN<V> tree) {
        this.tree = tree;
        this.node = null;
        this.index = 0;
    }

    /**
     * Create a {@link TreeIterator} exactly the same as this one.
     * The iterators will be backed by the same tree but maintain
     * separate cursors into the tree.
     */
    public TreeNIterator<V> copy() {
        TreeNIterator<V> result = new TreeNIterator<V>(tree);

        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `result.counti(i) = this.counti(i);
        ')
        END_M4_MACRO */ // BEGIN_M4_ALTERNATE
        result.count1 = this.count1;
        result.count2 = this.count2;
        result.count4 = this.count4;
        // END_M4_ALTERNATE

        result.node = node;
        result.index = index;
        return result;
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
            index = 0;
            if((node.color & colors) != 0) return;

        // increment within the current node
        } else if((node.color & colors) != 0 && index < node.size - 1) {
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `if(node.color == indexToBit(i)) counti(i)++;
            ')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            if(node.color == 1) count1++;
            if(node.color == 2) count2++;
            if(node.color == 4) count4++;
            // END_M4_ALTERNATE
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `if(node.color == indexToBit(i)) counti(i) += node.size - index;
            ')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            // END_M4_ALTERNATE
            node = TreeN.next(node);
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
    public V value() {
        if(node == null) throw new IllegalStateException();
        return node.get();
    }
}