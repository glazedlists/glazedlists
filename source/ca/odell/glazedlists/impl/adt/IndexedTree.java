/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

// for comparators and iterators
import java.util.*;
// for access to the ComparableComparator
import ca.odell.glazedlists.GlazedLists;

/**
 * Models a tree which keeps its elements either in sorted order
 * or by index.
 *
 * <p>When an <code>IndexedTree</code> is being used in sorted order, it
 * <strong>must</strong> be accessed using sorted order exclusively.
 * This means that {@link IndexedTreeNode#setValue(Object)} shall not be used,
 * and neither should {@link #addByNode(int,Object)}.
 * Similarly, when an <code>IndexedTree</code> is being used in random order,
 * it does not support {@link #addByNode(Object)} because that
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
        return root.getNodeByValue(comparator, value);
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
     * This iterator returns {@link IndexedTreeNode}s, so to get the
     * values use the node's {@link IndexedTreeNode#getValue()} method.
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
    public IndexedTreeNode removeByIndex(int index) {
        IndexedTreeNode treeNode = root.getNodeWithIndex(index);
        treeNode.removeFromTree(this);
        return treeNode;
    }

    /**
     * Inserts the specified object into the tree.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode addByNode(Object value) {
        if(root == null) root = new IndexedTreeNode(null);
        return root.insert(this, value);
    }
    /**
     * Inserts the specified object into the tree with the specified index.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode addByNode(int index, Object value) {
        if(index > size()) throw new IndexOutOfBoundsException("cannot insert into tree of size " + size() + " at " + index);
        else if(value == null) throw new NullPointerException("cannot insert a value that is null");
        else if(root == null && index == 0) root = new IndexedTreeNode(null);
        return root.insert(this, index, value);
    }

    /**
     * Returns true if this list contains the specified element.
     */
    public boolean contains(Object object) {
        if(root == null) return false;
        return root.contains(comparator, object);
    }

    /**
     * Returns true if this list contains all of the elements of the specified collection.
     */
    public boolean containsAll(Collection collection) {
        // look for something that is missing
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            Object a = i.next();
            if(!contains(a)) return false;
        }
        // contained everything we looked for
        return true;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object object) {
        if(root == null) return -1;
        return root.indexOf(comparator, object, false);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object object) {
        if(root == null) return -1;
        return root.lastIndexOf(comparator, object);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or the index where that element would be in the list if it were
     * in the list.
     */
    public int indexOfSimulated(Object object) {
        if(root == null) return 0;
        return root.indexOf(comparator, object, true);
    }

    /**
     * Validates the entire tree by iterating over its nodes and validating
     * them one at a time.
     */
    void validate() {
        for(Iterator i = iterator(); i.hasNext();) {
            IndexedTreeNode node = (IndexedTreeNode)i.next();
            node.validate(this);
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
            IndexedTree tree = new IndexedTree(GlazedLists.comparableComparator());
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
