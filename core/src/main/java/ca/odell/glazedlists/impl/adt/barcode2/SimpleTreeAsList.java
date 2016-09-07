/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.AbstractList;
import java.util.List;

/*
 # some M4 Macros that make it easy to use m4 with Java










  M4 Macros














# define a function NODE_WIDTH(boolean) to get the node's size for this color




# define a function NODE_SIZE(node, colors) to no node.nodeSize()




# define a function to refresh counts




# multiple values









*/
/*[ BEGIN_M4_JAVA ]*/

/**
 * Adapt a {@link SimpleTree} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SimpleTreeAsList <  T0>   extends AbstractList<T0> {

    private final SimpleTree <  T0>   tree;





    /**
     * Create a new {@link SimpleTreeAsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public SimpleTreeAsList/**/(SimpleTree <  T0>   tree   ) {
        this.tree = tree;

    }

    /** {@inheritDoc} */
    @Override
    public T0 get(int index) {
        return tree.get(index   ).get();
    }

    /** {@inheritDoc} */
    @Override
    public void add(int index, T0 element) {
        tree.add(index,    element, 1);
    }

    /** {@inheritDoc} */
    @Override
    public T0 set(int index, T0 element) {
        T0 replaced = get(index);
        tree.set(index,    element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    @Override
    public T0 remove(int index) {
        T0 removed = get(index);
        tree.remove(index,    1);
        return removed;
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return tree.size(  );
    }
}
  /*[ END_M4_JAVA ]*/
