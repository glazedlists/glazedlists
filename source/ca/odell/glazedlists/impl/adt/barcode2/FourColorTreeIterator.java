/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.NoSuchElementException;

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
 * Iterate through a {@link FourColorTree}, one element at a time.
 *
 * <p>We should consider adding the following enhancements to this class:
 * <li>writing methods, such as <code>set()</code> and <code>remove()</code>.
 * <li>a default color, specified at construction time, that shall always be
 *     used as the implicit parameter to overloaded versions of {@link #hasNext}
 *     and {@link #next}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FourColorTreeIterator<V> {

     
    int count1;
    int count2;
    int count4;
    int count8;
    
    
     

    private FourColorTree<V> tree;
    private FourColorNode<V> node;
    private int index;

    public FourColorTreeIterator/**/(FourColorTree<V> tree) {
        this(tree, 0, (byte)0);
    }

    /**
     * Create an iterator starting at the specified index.
     *
     * @param tree the tree to iterate
     * @param nextIndex the index to be returned after calling {@link #next next()}.
     * @param nextIndexColors the colors to interpret nextIndex in terms of
     */
    public FourColorTreeIterator/**/(FourColorTree<V> tree, int nextIndex, byte nextIndexColors) {
        this.tree = tree;

        // if the start is, we need to find the node in the tree
        if(nextIndex != 0) {
            int currentIndex = nextIndex - 1;
            this.node = (FourColorNode<V>)tree.get(currentIndex  , nextIndexColors   );

            // find the counts
             
            count1 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)1) + (node.color == 1 ? 0 : 1);
            count2 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)2) + (node.color == 2 ? 0 : 1);
            count4 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)4) + (node.color == 4 ? 0 : 1);
            count8 = tree.convertIndexColor(currentIndex, nextIndexColors, (byte)8) + (node.color == 8 ? 0 : 1);
            
            
             

            // find out the index in the node
             
            if(node.color == 1) this.index = count1 - tree.indexOfNode(this.node, (byte)1);
            if(node.color == 2) this.index = count2 - tree.indexOfNode(this.node, (byte)2);
            if(node.color == 4) this.index = count4 - tree.indexOfNode(this.node, (byte)4);
            if(node.color == 8) this.index = count8 - tree.indexOfNode(this.node, (byte)8);
            
            
             

        // just start before the beginning of the tree
        } else {
            this.node = null;
            this.index = 0;
        }
    }

    /**
     * Create a {@link FourColorTreeIterator} exactly the same as this one.
     * The iterators will be backed by the same tree but maintain
     * separate cursors into the tree.
     */
    public FourColorTreeIterator<V> copy() {
        FourColorTreeIterator<V> result = new FourColorTreeIterator<V>(tree);

         
        result.count1 = this.count1;
        result.count2 = this.count2;
        result.count4 = this.count4;
        result.count8 = this.count8;
        
        
         

        result.node = node;
        result.index = index;
        return result;
    }

    /**
     * @return <code>true</code> if there's an element of the specified color in
     *     this tree.
     */
    public boolean hasNext(  byte colors   ) {
        if(node == null) {
            return tree.size(  colors   ) > 0;
        } else if(  (colors & node.color) != 0   ) {
            return index(  colors   ) < tree.size(  colors   ) - 1;
        } else {
            return index(  colors   ) < tree.size(  colors   );
        }
    }

    public void next(  byte colors   ) {
        if(!hasNext(  colors   )) {
            throw new NoSuchElementException();
        }

        // start at the first node in the tree
        if(node == null) {
            node = tree.firstNode();
            index = 0;
             if((node.color & colors) != 0)    return;

        // increment within the current node
        } else if(  (node.color & colors) != 0 &&    index < node.size - 1) {
             
            if(node.color == 1) count1++;
            if(node.color == 2) count2++;
            if(node.color == 4) count4++;
            if(node.color == 8) count8++;
            
            
             
            index++;
            return;
        }

        // scan through the nodes, looking for the first one of the right color
        while(true) {
             
            if(node.color == 1) count1 += node.size - index;
            if(node.color == 2) count2 += node.size - index;
            if(node.color == 4) count4 += node.size - index;
            if(node.color == 8) count8 += node.size - index;
            
            
             
            node = FourColorTree.next(node);
            index = 0;

            // we've found a node that meet our requirements, so return
              if((node.color & colors) != 0)    break;
        }
    }

     
    /**
     * The color of the current element.
     */
    public byte color() {
        if(node == null) throw new IllegalStateException();
        return node.color;
    }
      

    /**
     * Expected values for index should be 0, 1, 2, 3...
     */
    public int index(  byte colors   ) {
        if(node == null) throw new NoSuchElementException();

        // total the values of the specified array for the specified colors.
        int result = 0;

        // forloop(i, 0, VAR_LAST_COLOR_INDEX, )
         
        if((colors & 1) != 0) result += count1;
        if((colors & 2) != 0) result += count2;
        if((colors & 4) != 0) result += count4;
        if((colors & 8) != 0) result += count8;
        
        
         
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
  /*[ END_M4_JAVA ]*/
