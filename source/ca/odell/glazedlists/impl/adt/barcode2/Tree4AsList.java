/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.AbstractList;

/*
 M4 Macros

STANDARD M4 LOOP ---------------------------------------------------------------



MACRO CODE WITH A JAVA ALTERNATIVE ---------------------------------------------





NODE SPECIFIC VARIABLES & FUNCTIONS--- -----------------------------------------









USE ALTERNATE CODE WHEN WE ONLY HAVE ONE COLOR ---------------------------------



SKIP SECTIONS OF CODE WHEN WE ONLY HAVE ONE COLOR ------------------------------



*/

/**
 * Adapt a {@link Tree4} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree4AsList<V> extends AbstractList<V> {

    private final Tree4<V> tree;
    /* USE DEFAULT */ private final byte colors; /* END DEFAULT */

    /** the color of inserted or added elements */
    /* USE DEFAULT */ private final byte color; /* END DEFAULT */

    /**
     * Create a new {@link Tree4AsList} adapting the specified tree.
     */
    // 
    public Tree4AsList
            (Tree4<V> tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }
    // 

    /**
     * Create a new {@link Tree4AsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public Tree4AsList/**/(Tree4<V> tree /* USE DEFAULT */ , byte colors, byte color /* END DEFAULT */) {
        this.tree = tree;
        // 
        this.colors = colors;
        this.color = color;
        // 
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index /* USE DEFAULT */, colors /* END DEFAULT */).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index, /* USE DEFAULT */ colors, color, /* END DEFAULT */ element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index, /* USE DEFAULT */ colors, color, /* END DEFAULT */ element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index, /* USE DEFAULT */ colors, /* END DEFAULT */ 1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(/* USE DEFAULT */ colors /* END DEFAULT */);
    }
}