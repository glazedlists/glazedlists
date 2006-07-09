/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.AbstractList;

/*
 # some M4 Macros that make it easy to use m4 with Java










  M4 Macros

STANDARD M4 LOOP ---------------------------------------------------------------



MACRO CODE WITH A JAVA ALTERNATIVE ---------------------------------------------





NODE SPECIFIC VARIABLES & FUNCTIONS--- -----------------------------------------









USE ALTERNATE CODE WHEN WE ONLY HAVE ONE COLOR ---------------------------------



SKIP SECTIONS OF CODE WHEN WE ONLY HAVE ONE COLOR ------------------------------







*/
/*[ BEGIN_M4_JAVA ]*/   

/**
 * Adapt a {@link SimpleTree} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SimpleTreeAsList<V> extends AbstractList<V> {

    private final SimpleTree<V> tree;

      

      

    /**
     * Create a new {@link SimpleTreeAsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public SimpleTreeAsList/**/(SimpleTree<V> tree   ) {
        this.tree = tree;
          
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index   ).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index,    element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index,    element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index,    1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(  );
    }
}
  /*[ END_M4_JAVA ]*/
