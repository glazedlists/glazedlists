/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

import java.util.*;

/**
 * A BooleanList is an ADT to replace the more general CompressableList
 * ADT.  CompressableList provides list compression capabilites that allow
 * a list to be accessed by both the real index and a compressed index.
 * The compressed index corresponds to the index of the current value as
 * though no nulls existed in the list.
 *
 * <p>This provides a huge performance boost over ArrayList on partially empty
 * lists.  However, the CompressableList is one of the volatile implementation
 * classes for internal development and isn't the best structure for the current
 * usage.  The GlazedLists use CompressableList to store only three values:
 * Boolean.TRUE, Boolean.FALSE, and null.  As such, it is slower and more
 * memory intensive than it could be due to its general purpose design.
 *
 * The BooleanList is designed such that a tree of n elements of the same
 * value will contain at most one node.  This will improve the performance
 * and scalability of the GlazedLists which make use of CompressableList.
 * BooleanList is already up for replacement by another ADT codenamed "barcode",
 * which takes the concepts from BooleanList and adds some powerful new features.
 * BooleanList does not support all three of the values used by GlazedLists at
 * this time.  Until UniqueList is refactored to make use of only two values,
 * BooleanList cannot completely replace CompressableList and will exist in parallel.
 *
 * <p>In an effort to maximize performance this ADT does NOT validate that arguments
 * passed to methods are valid in any way.  While this adds inherent risk to
 * the use of this code, this is a volatile implementation class.  As such, it
 * should only be used for internal GlazedList development.  It is up to the
 * calling code to do any argument validation which may be necessary.  If you
 * are still concerned, consider the benefits.  Being in a tree structure means the
 * methods on this ADT are often recursive.  Recursively validating arguments
 * makes no sense, and has a real-world impact on performance, while not a
 * Big-Oh impact.
 *
 * <p>Every effort has been made to squeeze the highest performance and smallest
 * footprint out of this data structure.  These benefits hopefully don't come at
 * the cost of code clarity or maintainability.  The memory usage of this ADT
 * is bound to the number of groupings of like values that are non-null.  Null
 * elements have no additional memory impact on the data structure.
 *
 * <p>The intent of this high-performance, low-cost data structure is for
 * improving the scalability of some of the GlazedLists.  It is technically
 * possible to scale this ADT above the Integer.MAX_SIZE barrier imposed by
 * integer-based indexing.  However, doing so requires particular care in the
 * structuring of the list and should be avoided if possible.  It is advised
 * that users do their best to operate within the bounds of the Integer.MAX_SIZE
 * size limit.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 *
 */
public final class BooleanList extends AbstractList {

    /** the root of the tree */
    private BooleanListNode root = null;

    /** a view of this list that contains no nulls */
    private CompressedList compressedList = new CompressedList();

    /** the total size of this data structure */
    private int size = 0;

    /** the size of tree */
    private int treeSize = 0;

    /**
     * Gets the size of this tree
     */
    public int size() {
        return size;
    }

    /**
     * Inserts a value into this tree at the given index
     */
    public void add(int index, Object value) {
        // Let nulls be inserted by the method created for that purpose
        if(value == null) addNulls(index, 1);

        // Let values be inserted by the method created for that purpose
        else addValues(index, 1);
    }

    /**
     * Inserts a sequence of nulls into the tree
     */
    public void addNulls(int index, int length) {
        // Adding to the trailing nulls
        if(root == null || index >= treeSize) {
            size += length;

        // Adding nulls to the actual tree
        } else {
            root.insertEmptySpace(index, length);
            treeSizeChanged();

        }
    }

    /**
     * Inserts a sequence of values into the tree
     */
    public void addValues(int index, int length) {
        // Make a new root
        if(root == null) {
            root = new BooleanListNode(this, null, length, index);
            size += length;
            treeSize = index + length;

        // Add in the trailing nulls
        } else if(index >= treeSize) {
            int movingNulls = index - treeSize;
            size -= movingNulls;
            root.insertValuesAtEnd(length, movingNulls);
            treeSizeChanged();

        // Add values to the actual tree
        } else {
            root.insertValues(index, length);
            treeSizeChanged();

        }
    }

    /**
     * Gets the value in this tree at the given index
     */
    public Object get(int index) {
        BooleanListNode node = getNode(index);
        // The value at that index is a null
        if(node == null) return null;
        return node.getValue();
    }

    /**
     * Gets the node for an element in this tree at the given index
     * or null if the value at that index is null
     */
    private BooleanListNode getNode(int index) {
        if(root != null && index < treeSize) return root.getNode(index);
        return null;
    }

    /**
     * Sets the value of the node at the given index
     */
    public Object set(int index, Object value) {
        // The set occurs in the actual tree
        if(root != null && index < treeSize) {
            Object returnValue = root.set(index, value);
            // This is not intutive as the tree size should 'never' change on a
            // set call.  However, since sets can result in inserts and deletes
            // this is currently necessary.  But I'm working on it.
            if(root != null) treeSizeChanged();
            return returnValue;

        // The set occurs in the trailing nulls
        } else {
            // no-op to set a null to a null
            if(value == null) {
                return null;

            // this is equivalent to adding at this index
            } else {
                size--;
                add(index, value);
                return null;
            }
        }
    }

    /**
     * Removes the nodex at the given index
     */
    public Object remove(int index) {
        // The remove occurs in the actual tree
        if(root != null && index < treeSize) {
            Object returnValue = root.remove(index);
            if(root != null) treeSizeChanged();
            return returnValue;

        // The remove occurs in the trailing nulls
        } else {
            size--;
            return null;
        }
    }

    /**
     * Clears the tree
     */
    public void clear() {
        size = 0;
        root = null;
    }

    /**
     * Sets the root for this tree.  This method is exposed for the
     * BooleanListNode in the event that the tree's root is involved in
     * an AVL rotation.
     */
    void setRootNode(BooleanListNode root) {
        this.root = root;
        if(root == null) {
            size -= treeSize;
            treeSize = 0;
        }
    }

    /**
     * Notifies the tree that the underlying tree size has changed. This method
     * is exposed for BooleanListNode to make size adjustments.
     */
    void treeSizeChanged() {
        size -= treeSize;
        treeSize = root.size();
        size += treeSize;
    }

    /**
     * Gets the compressed view of this list.
     */
    public List getCompressedList() {
        return compressedList;
    }

    /**
     * A CompressedList is a read-only view of the list where all the nulls
     * have been removed.
     */
    class CompressedList extends AbstractList {

        /**
         * Gets the value at the specified index.
         */
        public Object get(int index) {
            if(root == null) throw new IndexOutOfBoundsException("cannot get from tree of size 0 at " + index);
            BooleanListNode node = root.getNodeByCompressedIndex(index);
            return node.getValue();
        }

        /**
         * Gets the size of the compressed list. This is a shortcut to
         * the number of non-null elements are in the main list.
         */
        public int size() {
            if(root == null) return 0;
            return root.treeSize();
        }
    }

    /**
     * Gets the index of the node with the specified compressedIndex.
     * Compressed index is defined as the index of that element in
     * the compressed list. This is the index of the node if all nulls
     * were to be removed from the list.
     */
    public int getIndex(int compressedIndex) {
        if(root == null) throw new IndexOutOfBoundsException("cannot get from a tree of size 0 at " + compressedIndex);
        return root.getIndexByCompressedIndex(compressedIndex);
    }

    /**
     * Gets the compressed index of the node with the specified natural
     * index. The parameter index specifies the index in the natural tree
     * and this returns the index in the compressed tree. This method will
     * throw an IllegalArgumentException if the value with the specified
     * index is null.
     */
    public int getCompressedIndex(int index) {
        // get from the main tree
        if(root != null && index < root.size()) {
            int compressedIndex = root.getCompressedIndex(index);
            if(compressedIndex == -1) throw new IllegalArgumentException("Cannot get compressed index of " + index + ", that value is compressed out");
            return compressedIndex;

        // This was an error
        } else {
            throw new IllegalArgumentException("Cannot get compressed index of " + index + ", that value is compressed out");
        }
    }

    /**
     * Gets the compressed index of the specified index into the tree. This
     * is the index of the node that the specified index will be stored in.
     *
     * @param left true for compressed-out nodes to return the index of the
     *      not-compressed-out node on the left. Such values will range from
     *      <code>-1</code> through <code>size()-1</code>. False for such
     *      nodes to return the not-compressed-out node on the right. Such values
     *      will range from <code>0</code> through <code>size()</code>.
     */
    public int getCompressedIndex(int index, boolean left) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + index);

        // there is no main tree
        if(root == null) {
            if(left) return -1;
            else return 0;
        }

        // if it is beyond the main tree
        if(index >= root.size()) {
            if(left) return root.treeSize() - 1;
            return root.treeSize();
        }

        // get from the main tree
        return root.getCompressedIndex(index, left);
    }

    /**
     * Gets the number of leading nulls on this index. This is the number of
     * nulls between the value at this index and the next lowest index that has
     * a non-null value.
     *
     * @param compressedIndex the compressed index into the tree
     */
    public int getLeadingNulls(int compressedIndex) {
        if(root == null) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + compressedIndex);
        BooleanListNode node = root.getNodeByCompressedIndex(compressedIndex);
        if(node == null) throw new IllegalArgumentException("Cannot get compressed index of " + compressedIndex + ", that value is compressed out");
        return node.getEmptySpace();
    }

    /**
     * Gets the number of trailing nulls on this index. This is the number
     * of nulls between the value at this index and the next highest index that has
     * a non-null value.
     */
    public int getTrailingNulls(int compressedIndex) {
        if(root == null && compressedIndex != 0) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + compressedIndex);
        else if(root == null) return size;
        else if(compressedIndex == root.treeSize() - 1) return size - root.size();
        return getLeadingNulls(compressedIndex + 1);
    }
}