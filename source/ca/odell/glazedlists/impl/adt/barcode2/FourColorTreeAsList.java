/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.List;
import java.util.AbstractList;

/*
 # some M4 Macros that make it easy to use m4 with Java










  M4 Macros














# define a function NODE_WIDTH(boolean) to get the node's size for this color

    


# define a function NODE_SIZE(node, colors) to no node.nodeSize()

    
       
    


# define a function to refresh counts

   



*/
/*[ BEGIN_M4_JAVA ]*/   

/**
 * Adapt a {@link FourColorTree} for use as a {@link List}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FourColorTreeAsList<V> extends AbstractList<V> {

    private final FourColorTree<V> tree;

     
    private final byte colors;

    /** the color of inserted or added elements */
    private final byte color;
      

     
    /**
     * Create a new {@link FourColorTreeAsList} adapting the specified tree.
     */
    public FourColorTreeAsList/**/(FourColorTree<V> tree) {
        this(tree, tree.getCoder().colorsToByte(tree.getCoder().getColors()), (byte)1);
    }
      

    /**
     * Create a new {@link FourColorTreeAsList}, adapting the specified colors subset
     * of the specified tree. Inserted elements via {@link #add} will be of the
     * specified color.
     */
    public FourColorTreeAsList/**/(FourColorTree<V> tree   , byte colors, byte color   ) {
        this.tree = tree;
         
        this.colors = colors;
        this.color = color;
          
    }

    /** {@inheritDoc} */
    public V get(int index) {
        return tree.get(index  , colors   ).get();
    }

    /** {@inheritDoc} */
    public void add(int index, V element) {
        tree.add(index,   colors, color,    element, 1);
    }

    /** {@inheritDoc} */
    public V set(int index, V element) {
        V replaced = get(index);
        tree.set(index,   colors, color,    element, 1);
        return replaced;
    }

    /** {@inheritDoc} */
    public V remove(int index) {
        V removed = get(index);
        tree.remove(index,   colors,    1);
        return removed;
    }

    /** {@inheritDoc} */
    public int size() {
        return tree.size(  colors   );
    }
}
  /*[ END_M4_JAVA ]*/
