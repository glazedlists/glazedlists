/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.AbstractList;

/*
 M4 Macros








 Barcode2 Macros










*/

/**
 * Adapt a {@link Tree1} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree1AsList<V> extends AbstractList<V> {

    private final Tree1<V> tree;
    private final byte colors;

    /** the color of inserted or added elements */
    private final byte color;

    /**
     * Create a new {@link Tree1AsList} adapting the specified tree.
     */
    public Tree1AsList/**/(Tree1<V> tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }

    /**
     * Create a new {@link Tree1AsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public Tree1AsList/**/(Tree1<V> tree, byte colors, byte color) {
        this.tree = tree;
        this.colors = colors;
        this.color = color;
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index, colors).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index, colors, color, element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index, colors, color, element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index, colors, 1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(colors);
    }
}