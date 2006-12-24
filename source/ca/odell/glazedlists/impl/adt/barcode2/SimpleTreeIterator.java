/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.NoSuchElementException;

/*
 # some M4 Macros that make it easy to use m4 with Java










  M4 Macros














# define a function NODE_WIDTH(boolean) to get the node's size for this color

    


# define a function NODE_SIZE(node, colors) to no node.nodeSize()

   


# define a function to refresh counts

   



*/
/*[ BEGIN_M4_JAVA ]*/   

/**
 * Iterate through a {@link SimpleTree}, one element at a time.
 *
 * <p>We should consider adding the following enhancements to this class:
 * <li>writing methods, such as <code>set()</code> and <code>remove()</code>.
 * <li>a default color, specified at construction time, that shall always be
 *     used as the implicit parameter to overloaded versions of {@link #hasNext}
 *     and {@link #next}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SimpleTreeIterator<V> {

     
    int count1;
    
    
     

    private SimpleTree<V> tree;
    private SimpleNode<V> node;
    private int index;

    public SimpleTreeIterator/**/(SimpleTree<V> tree) {
        this(tree, 0, (byte)0);
    }

    /**
     * Create an iterator starting at the specified index.
     *
     * @param tree the tree to iterate
     * @param nextIndex the index to be returned after calling {@link #next next()}.
     * @param nextIndexColors the colors to interpret nextIndex in terms of
     */
    public SimpleTreeIterator/**/(SimpleTree<V> tree, int nextIndex, byte nextIndexColors) {
        this.tree = tree;

        // if the start is, we need to find the node in the tree
        if(nextIndex != 0) {
            int currentIndex = nextIndex - 1;
            this.node = (SimpleNode<V>)tree.get(currentIndex   );

            // find the counts
             
            count1 = currentIndex;
            
            
             

            // find out the index in the node
             
            this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            
            
             

        // just start before the beginning of the tree
        } else {
            this.node = null;
            this.index = 0;
        }
    }

    /**
     * Create a {@link SimpleTreeIterator} exactly the same as this one.
     * The iterators will be backed by the same tree but maintain
     * separate cursors into the tree.
     */
    public SimpleTreeIterator<V> copy() {
        SimpleTreeIterator<V> result = new SimpleTreeIterator<V>(tree);

         
        result.count1 = this.count1;
        
        
         

        result.node = node;
        result.index = index;
        return result;
    }

    /**
     * @return <code>true</code> if there's an element of the specified color in
     *     this tree following the current element.
     */
    public boolean hasNext(  ) {
        if(node == null) {
            return tree.size(  ) > 0;
        } else if( true ) {
            return index(  ) < tree.size(  ) - 1;
        } else {
            return index(  ) < tree.size(  );
        }
    }

    /**
     * @return <code>true</code> if there's a node of the specified color in this
     *      tree following the current node.
     */
    public boolean hasNextNode(  ) {
        if(node == null) {
            return tree.size(  ) > 0;
        } else {
            return nodeEndIndex(  ) < tree.size(  );
        }
    }

    /**
     * Step to the next element.
     */
    public void next(  ) {
        if(!hasNext(  )) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
               return;

        // increment within the current node
        } else if(   index <  1  - 1) {
             
            count1++;
            
            
             
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
             
            count1 += 1 - index;
            
            
             
            node = SimpleTree.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
               break;
        }
    }

    /**
     * Step to the next node.
     */
    public void nextNode(  ) {
        if(!hasNextNode(  )) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
               return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
             
            count1 += 1 - index;
            
            
             
            node = SimpleTree.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
               break;
        }
    }


    /**
     * Get the size of the current node, or 0 if it's color doesn't match those
     * specified.
     */
    public int nodeSize(  ) {
        if( true ) {
            return  1 ;
        } else {
            return 0;
        }
    }

      

    /**
     * Expected values for index should be in the range  ( 0, size() - 1 )
     */
    public int index(  ) {
        if(node == null) throw new NoSuchElementException();

        // total the values of the specified array for the specified colors.
        int result = 0;

         
        result += count1;
        
        
         
        return result;
    }
    /**
     * Get the index of the current node's start.
     */
    public int nodeStartIndex(  ) {
        if(node == null) throw new NoSuchElementException();

        // the count of all nodes prior to this one
        int result = 0;

        // this should merely be the sum of each count
         
        result += count1;
        
        
         

        // subtract the count of anything in the current node which we may
        // have included inadvertently
        if( true ) {
            result -= index;
        }

        return result;
    }
    /**
     * Get the index of the node immediately following the current. Expected
     * values are in the range ( 1, size() )
     */
    public int nodeEndIndex(  ) {
        if(node == null) throw new NoSuchElementException();

        // the count of all nodes previous
        return nodeStartIndex(  )
                + nodeSize(  );
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
  /*[ END_M4_JAVA ]*/
