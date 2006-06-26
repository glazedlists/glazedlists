/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.NoSuchElementException;

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

/**
 * Iterate through a {@link TreeN}, one element at a time.
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
        this(tree, 0, (byte)0);
    }

    /**
     * Create an iterator starting at the specified index.
     *
     * @param tree the tree to iterate
     * @param nextIndex the index to be returned after calling {@link #next next()}.
     * @param nextIndexColors the colors to interpret nextIndex in terms of
     */
    public TreeNIterator/**/(TreeN<V> tree, int nextIndex, byte nextIndexColors) {
        this.tree = tree;

        // if the start is, we need to find the node in the tree
        if(nextIndex != 0) {
            int currentIndex = nextIndex - 1;
            this.node = (NodeN<V>)tree.get(currentIndex /* SINGLE_ALTERNATE */, nextIndexColors /* END_SINGLE_ALTERNATE */);

            // find the counts
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 = currentIndex;
            ', counti(i)` = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)'indexToBit(i)`) + (node.color == 'indexToBit(i)` ? 0 : 1);
            ')')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            count1 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)1) + (node.color == 1 ? 0 : 1);
            count2 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)2) + (node.color == 2 ? 0 : 1);
            count4 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)4) + (node.color == 4 ? 0 : 1);
            // END_M4_ALTERNATE

            // find out the index in the node
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            ',`if(node.color == 'indexToBit(i)`) this.index = 'counti(i)` - tree.indexOfNode(this.node, '(byte)indexToBit(i)`);
            ')')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            if(node.color == 1) this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            if(node.color == 2) this.index = count2 - tree.indexOfNode(this.node, (byte)2);
            if(node.color == 4) this.index = count4 - tree.indexOfNode(this.node, (byte)4);
            // END_M4_ALTERNATE

        // just start before the beginning of the tree
        } else {
            this.node = null;
            this.index = 0;
        }
    }

    /**
     * Create a {@link TreeNIterator} exactly the same as this one.
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

    /**
     * @return <code>true</code> if there's an element of the specified color in
     *     this tree.
     */
    public boolean hasNext(/* SINGLE_ALTERNATE */ byte colors /* END_SINGLE_ALTERNATE */) {
        if(node == null) {
            return tree.size(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */) > 0;
        } else if(/* SINGLE_ALTERNATE(`true') */ (colors & node.color) != 0 /* END_SINGLE_ALTERNATE */) {
            return index(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */) < tree.size(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */) - 1;
        } else {
            return index(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */) < tree.size(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */);
        }
    }

    public void next(/* SINGLE_ALTERNATE */ byte colors /* END_SINGLE_ALTERNATE */) {
        if(!hasNext(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */)) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
            /* SINGLE_ALTERNATE */if((node.color & colors) != 0) /* END_SINGLE_ALTERNATE */ return;

        // increment within the current node
        } else if(/* SINGLE_ALTERNATE */ (node.color & colors) != 0 && /* END_SINGLE_ALTERNATE */ index < node.size - 1) {
            /* BEGIN_M4_MACRO
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1++;
            ', `if(node.color == indexToBit(i)) counti(i)++;
            ')')
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
            forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`count1 += node.size - index;
            ', `if(node.color == indexToBit(i)) counti(i) += node.size - index;
            ')')
            END_M4_MACRO */ // BEGIN_M4_ALTERNATE
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            // END_M4_ALTERNATE
            node = TreeN.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
            /* SINGLE_ALTERNATE */ if((node.color & colors) != 0) /* END_SINGLE_ALTERNATE */ break;
        }
    }

    /**
     * The color of the current element.
     */
    // BEGIN_SINGLE_SKIP
    public byte color() {
        if(node == null) throw new IllegalStateException();
        return node.color;
    }
    // END_SINGLE_SKIP

    /**
     * Expected values for index should be 0, 1, 2, 3...
     */
    public int index(/* SINGLE_ALTERNATE */ byte colors /* END_SINGLE_ALTERNATE */) {
        if(node == null) throw new NoSuchElementException();

        // total the values of the specified array for the specified colors.
        int result = 0;

        /* BEGIN_M4_MACRO
        forloop(`i', 0, VAR_LAST_COLOR_INDEX, )

        forloop(`i', 0, VAR_LAST_COLOR_INDEX, `m4_ifelse(VAR_COLOUR_COUNT,`1',`result += count1;
        ', `if((colors & indexToBit(i)) != 0) result += counti(i);
        ')')

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
    public Element<V> node() {
        if(node == null) throw new IllegalStateException();
        return node;
    }
}