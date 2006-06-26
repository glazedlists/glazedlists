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
 * Adapt a {@link Tree1} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree1AsList<V> extends AbstractList<V> {

    private final Tree1<V> tree;
    /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ private final byte colors; /* 
/* END SINGLE ALTERNATE */

    /** the color of inserted or added elements */
    /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ private final byte color; /* 
/* END SINGLE ALTERNATE */

    /**
     * Create a new {@link Tree1AsList} adapting the specified tree.
     */
    // 
/* BEGIN SINGLE SKIPPED CODE 
    public Tree1AsList
            (Tree1<V> tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }
    // END SINGLE SKIPPED CODE */

    /**
     * Create a new {@link Tree1AsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public Tree1AsList/**/(Tree1<V> tree /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ , byte colors, byte color /* 
/* END SINGLE ALTERNATE */) {
        this.tree = tree;
        // 
/* BEGIN SINGLE SKIPPED CODE 
        this.colors = colors;
        this.color = color;
        // END SINGLE SKIPPED CODE */
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */, colors /* 
/* END SINGLE ALTERNATE */).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index, /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ colors, color, /* 
/* END SINGLE ALTERNATE */ element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index, /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ colors, color, /* 
/* END SINGLE ALTERNATE */ element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index, /* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ colors, /* 
/* END SINGLE ALTERNATE */ 1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(/* USE SINGLE ALTERNATE */ 
// IGNORE DEFAULT: */ colors /* 
/* END SINGLE ALTERNATE */);
    }
}