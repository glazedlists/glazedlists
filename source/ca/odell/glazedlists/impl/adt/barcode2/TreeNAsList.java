/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.AbstractList;

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
 * Adapt a {@link TreeN} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeNAsList<V> extends AbstractList<V> {

    private final TreeN<V> tree;
    /* SINGLE_ALTERNATE */ private final byte colors; /* END_SINGLE_ALTERNATE */

    /** the color of inserted or added elements */
    /* SINGLE_ALTERNATE */ private final byte color; /* END_SINGLE_ALTERNATE */

    /**
     * Create a new {@link TreeNAsList} adapting the specified tree.
     */
    // BEGIN_SINGLE_SKIP
    public TreeNAsList
            (TreeN<V> tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }
    // END_SINGLE_SKIP

    /**
     * Create a new {@link TreeNAsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public TreeNAsList/**/(TreeN<V> tree /* SINGLE_ALTERNATE */ , byte colors, byte color /* END_SINGLE_ALTERNATE */) {
        this.tree = tree;
        // BEGIN_SINGLE_SKIP
        this.colors = colors;
        this.color = color;
        // END_SINGLE_SKIP
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index /* SINGLE_ALTERNATE */, colors /* END_SINGLE_ALTERNATE */).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index, /* SINGLE_ALTERNATE */ colors, color, /* END_SINGLE_ALTERNATE */ element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index, /* SINGLE_ALTERNATE */ colors, color, /* END_SINGLE_ALTERNATE */ element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index, /* SINGLE_ALTERNATE */ colors, /* END_SINGLE_ALTERNATE */ 1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(/* SINGLE_ALTERNATE */ colors /* END_SINGLE_ALTERNATE */);
    }
}