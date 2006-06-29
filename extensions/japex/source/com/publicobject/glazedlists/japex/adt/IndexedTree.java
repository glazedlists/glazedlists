/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex.adt;

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
 * @deprecated, replaced with {@link ca.odell.glazedlists.impl.adt.barcode2.TreeN BC2}
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class IndexedTree<V> {

    /** a decision maker for ordering elements */
    private Comparator comparator;

    /** the root node of the tree, this may be replaced by a delete */
    IndexedTreeNode<V> root = null;

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
    public V get(int index) {
        if(index > size()) throw new IndexOutOfBoundsException("cannot get from tree of size " + size() + " at " + index);
        IndexedTreeNode<V> treeNode = root.getNodeWithIndex(index);
        return treeNode.getValue();
    }
    /**
     * Gets the tree node with the specified index.
     */
    public IndexedTreeNode<V> getNode(int index) {
        if(index > size()) throw new IndexOutOfBoundsException("cannot get from tree of size " + size() + " at " + index);
        return root.getNodeWithIndex(index);
    }

    /**
     * Gets the tree node with the specified value.
     *
     * @return the tree node containing the specified value, or <code>null</code>
     *      if no such node is found.
     */
    public IndexedTreeNode<V> getNode(Object value) {
        if(root == null) return null;
        return root.getShallowestNodeWithValue(comparator, value, false);
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
    public IndexedTreeIterator<V> iterator(int index) {
        return new IndexedTreeIterator<V>(this, index);
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
    public IndexedTreeNode<V> removeByIndex(int index) {
        if(index > size()) throw new IndexOutOfBoundsException("cannot get from tree of size " + size() + " at " + index);
        //IndexedTreeNode treeNode = root.getNodeWithIndex(index);
        //treeNode.removeFromTree(this);
        //return treeNode;
        return root.removeNode(this, index);
    }

    /**
     * Inserts the specified object into the tree.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode<V> addByNode(V value) {
        try {
            if(root == null) {
                root = new IndexedTreeNode<V>(null, value);
                return root;
            } else {
                return root.insert(this, value);
            }
        } finally {
//            validate(); // DEBUG!
        }
    }

    /**
     * Inserts the specified object into the tree with the specified index.
     *
     * @return the node object that was added. This object has
     *      useful methods such as getIndex() to get the dynamic
     *      index of the node.
     */
    public IndexedTreeNode<V> addByNode(int index, V value) {
        try {
            if(index > size()) throw new IndexOutOfBoundsException("cannot insert into tree of size " + size() + " at " + index);
            if(value == null) throw new NullPointerException("cannot insert a value that is null");

            if(root == null) {
                root = new IndexedTreeNode<V>(null, value);
                return root;
            } else {
                return root.insert(this, index, value);
            }
        } finally {
//            validate(); // DEBUG!
        }
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int indexOf(Object object) {
        if(root == null) return -1;

        // find any node with this value
        IndexedTreeNode<V> shallowestNodeWithValue = root.getShallowestNodeWithValue(comparator, object, false);
        if(shallowestNodeWithValue == null) return -1;

        // iterate to the first node with this value
        IndexedTreeIterator<V> iterator = new IndexedTreeIterator<V>(this, shallowestNodeWithValue);
        while(iterator.hasPrevious()) {
            IndexedTreeNode<V> node = iterator.previous();
            if(comparator.compare(node.getValue(), object) == 0) continue;
            else return iterator.nextIndex() + 1;
        }

        // we ran out of values
        return 0;
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or the index where that element would be in the list if it were
     * in the list.
     */
    public int indexOfSimulated(Object object) {
        if(root == null) return 0;

        // find any node with this value
        IndexedTreeNode<V> shallowestNodeWithValue = root.getShallowestNodeWithValue(comparator, object, true);
        if(shallowestNodeWithValue == null) throw new IllegalStateException();
        int compareResult = comparator.compare(shallowestNodeWithValue.getValue(), object);

        // the result is before our target, increment until that result changes
        if(compareResult < 0) {
            IndexedTreeIterator<V> iterator = new IndexedTreeIterator<V>(this, shallowestNodeWithValue);
            while(iterator.hasNext()) {
                IndexedTreeNode<V> node = iterator.next();
                if(comparator.compare(node.getValue(), object) < 0) continue;
                else return iterator.previousIndex();
            }
            // we ran out of values
            return size();

        // the result is equal to or after our value, decrement until that result changes
        } else {
            IndexedTreeIterator<V> iterator = new IndexedTreeIterator<V>(this, shallowestNodeWithValue);
            while(iterator.hasPrevious()) {
                IndexedTreeNode<V> node = iterator.previous();
                if(comparator.compare(node.getValue(), object) >= 0) continue;
                else return iterator.nextIndex() + 1;
            }
            // we ran out of values
            return 0;
        }
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     */
    public int lastIndexOf(Object object) {
        if(root == null) return -1;

        // find any node with this value
        IndexedTreeNode<V> shallowestNodeWithValue = root.getShallowestNodeWithValue(comparator, object, false);
        if(shallowestNodeWithValue == null) return -1;

        // iterate to the last node with this value
        IndexedTreeIterator<V> iterator = new IndexedTreeIterator<V>(this, shallowestNodeWithValue);
        while(iterator.hasNext()) {
            IndexedTreeNode<V> node = iterator.next();
            if(comparator.compare(node.getValue(), object) == 0) continue;
            else return iterator.previousIndex() - 1;
        }

        // we ran out of values
        return size() - 1;
    }

    /**
     * Validates the entire tree by iterating over its nodes and validating
     * them one at a time.
     */
    void validate() {
        for(Iterator<IndexedTreeNode<V>> i = (Iterator<IndexedTreeNode<V>>)iterator(0); i.hasNext();) {
            IndexedTreeNode<V> node = i.next();
            node.validate(this);
        }
    }

    /**
     * Print the tree by its contents
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for(IndexedTreeIterator i = iterator(0); i.hasNext(); ) {
            if(result.length() > 0) result.append(", ");
            result.append(i.next());
        }
        return result.toString();
    }

    /**
     * Sets the root of this tree to be the specified node. This
     * method should not be called by client classes as it is an
     * implementation artifact.
     */
    void setRootNode(IndexedTreeNode<V> root) {
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
            IndexedTree<Integer> tree = new IndexedTree<Integer>(GlazedLists.comparableComparator());
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
