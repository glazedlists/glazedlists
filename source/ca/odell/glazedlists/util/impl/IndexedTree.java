/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

// for specifying a sorting order
import java.util.*;
// for event list utilities, iterators and comparators
import ca.odell.glazedlists.util.*;

/**
 * Models a tree which keeps its elements either in sorted order
 * or by index.
 *
 * <p>When an <code>IndexedTree</code> is being used in sorted order, it
 * <strong>must</strong> be accessed using sorted order exclusively.
 * This means that the <code>setValue()</code> method of
 * <code>IndexedTreeNode</code> shall not be used, and neither should
 * the method <code>addByNode(int,Object)</code> of <code>IndexedTree</code>.
 * Similarly, when an IndexedTree is being used in random order,
 * it does not support <code>addByNode(Object)</code> because that
 * method specifies no index.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class IndexedTree {
    
    /** a decision maker for ordering elements */
    private Comparator comparator;
    
    /** the root node of the tree, this may be replaced by a delete */
    private IndexedTreeNode root = null;
    
    /**
     * Creates a new empty tree that uses the specified comparator to sort values.
     */
    public IndexedTree(Comparator comparator) {
        this.comparator = comparator;
    }
    
    /**
     * Creates a new empty sorted tree that requires that objects in the
     * tree implement the Comparable interface.
     */
    public IndexedTree() {
        comparator = null;
    }

    /**
     * Gets the value of the sorted tree node with the specified index.
     */
    public Object get(int index) {
        IndexedTreeNode treeNode = root.getNodeWithIndex(index);
        return treeNode.getValue();
    }
    /**
     * Gets the tree node with the specified index.
     */
    public IndexedTreeNode getNode(int index) {
        return root.getNodeWithIndex(index);
    }

    /**
     * Gets the tree node with the specified value.
     *
     * @return the tree node containing the specified value, or null
     *      if no such node is found.
     */
    public IndexedTreeNode getNode(Object value) {
        if(root == null) return null;
        return root.getNodeByValue(value);
    }
    
    /**
     * Gets the size of this tree.
     */
    public int size() {
        if(root == null) return 0;
        return root.size();
    }
    
    /**
     * Gets an iterator for this tree. The iterator moves in sorted order
     * for sorted trees and order of increasing index for indexed trees.
     * This iterator returns <code>IndexedTreeNode</code>s, so to get the
     * values use the node's <code>getValue()</code> method.
     */
    public Iterator iterator() {
        if(root != null) return root.getSmallestChildNode().iterator();
        else return Collections.EMPTY_LIST.iterator();
    }
    
    /** 
     * Gets the comparator used to sort the nodes in this tree.
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * Deletes the node with the specified sort-order from the tree.
     */
    public void removeByIndex(int index) {
        IndexedTreeNode treeNode = root.getNodeWithIndex(index);
        treeNode.removeFromTree();
    }
    
    /**
     * Inserts the specified object into the tree.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode addByNode(Object value) {
        if(root == null) root = new IndexedTreeNode(this, null);
        return root.insert(value);
    }
    /**
     * Inserts the specified object into the tree with the specified index.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode addByNode(int index, Object value) {
        if(root == null) root = new IndexedTreeNode(this, null);
        return root.insert(index, value);
    }
    
    /**
     * Validates the entire tree by iterating over its nodes and validating
     * them one at a time.
     */
    void validate() {
        for(Iterator i = iterator(); i.hasNext();) {
            IndexedTreeNode node = (IndexedTreeNode)i.next();
            node.validate();
        }
    }
    
    /**
     * Print the tree by its contents
     */
    public String toString() {
        if(root == null) return ".";
        return root.toString();
    }
    
    /**
     * Sets the root of this tree to be the specified node. This
     * method should not be called by client classes as it is an
     * implementation artifact.
     */
    void setRootNode(IndexedTreeNode root) {
        this.root = root;
    }
    
    
    /**
     * Test method for the indexed tree compares it in sort time to the
     * TreeSet.
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("Usage: IndexedTree <operations> <repetitions>");
            return;
        }
        
        int operations = Integer.parseInt(args[0]);
        int repetitions = Integer.parseInt(args[1]);
        
        java.util.Random random = new java.util.Random();
        
        System.out.print("Indexed Tree ");
        long start = System.currentTimeMillis();

        for(int r = 0; r < repetitions; r++) {
            IndexedTree tree = new IndexedTree(new ComparableComparator());
            for(int i = 0; i < operations; i++) {
                int operation = (int)(random.nextDouble() * 3.0);
                
                if(operation <= 1 || tree.size() == 0) {
                    Integer value = new Integer((int)(random.nextDouble() * (double)Integer.MAX_VALUE));
                    tree.addByNode(value);
                } else {
                    int index = (int)(random.nextDouble() * (double)tree.size());
                    tree.removeByIndex(index);
                }
                
                //tree.validate();
            }
        }

        long total = System.currentTimeMillis() - start;
        System.out.println("time: " + total);
    }
}
