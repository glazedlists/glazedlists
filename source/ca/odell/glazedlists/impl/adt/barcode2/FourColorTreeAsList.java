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
 * Adapt a {@link FourColorTree} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FourColorTreeAsList <  T0>   extends AbstractList<T0> {

    private final FourColorTree <  T0>   tree;

     
    private final byte colors;

    /** the color of inserted or added elements */
    private final byte color;
      

     
    /**
     * Create a new {@link FourColorTreeAsList} adapting the specified tree.
     */
    public FourColorTreeAsList/**/(FourColorTree tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }
      

    /**
     * Create a new {@link FourColorTreeAsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public FourColorTreeAsList/**/(FourColorTree <  T0>   tree   , byte colors, byte color   ) {
        this.tree = tree;
         
        this.colors = colors;
        this.color = color;
          
    }

    /** {@inheritDoc} */
    public T0 get(int index) {
        return tree.get(index  , colors   ).get();
    }

    /** {@inheritDoc} */
    public void add(int index, T0 element) {
        tree.add(index,   colors, color,    element, 1);
    }

    /** {@inheritDoc} */
    public T0 set(int index, T0 element) {
        T0 replaced = get(index);
        tree.set(index,   colors, color,    element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public T0 remove(int index) {
        T0 removed = get(index);
        tree.remove(index,   colors,    1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(  colors   );
    }
}
  /*[ END_M4_JAVA ]*/
