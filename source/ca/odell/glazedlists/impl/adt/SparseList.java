/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

// For Lists and Iterators
import java.util.*;

/**
 * A SparseList is an ADT to complement the CompressableList and IndexedTree
 * ADTs.  IndexedTree provides accessible nodes that can recalculate their index
 * on the fly so users can avoid dealing with index offsetting.  CompressableList
 * provides list compression capabilites that allow a list to be accessed
 * by both the real index and a compressed index.  The compressed index
 * corresponds to the index of the current value as though no nulls exist
 * in the list.  While this is a powerful feature, the larger benefits of the
 * compression of nulls are a significant performance boost and smaller footprint
 * for lists that tend towards containing a significant number of nulls.
 *
 * <p>The SparseList was created to provide the indexed accessible nodes
 * as found in IndexedTree while reaping the performance and memory enhancements
 * of CompressableList.  These optimizations have been taken several steps further
 * to gain significantly better performance over the current implementation of
 * CompressableList.
 *
 * <p>In an effort to maximize performance, this ADT does NOT validate that
 * arguments passed to methods are valid in any way.  While this adds inherent
 * risk to the use of this code, this is a volatile implementation class.  As
 * such, it should only be used for internal GlazedList development.  It is up
 * to the calling code to do any argument validation which may be necessary.  If
 * you are still concerned, consider the benefits.  Being in a tree structure
 * means the methods on this ADT are often recursive.  Recursively validating
 * arguments makes no sense, and has a real-world impact on performance, while
 * not a Big-Oh impact.
 *
 * <p>Every effort has been made to squeeze the highest performance and smallest
 * footprint out of this data structure.  These benefits hopefully don't come at
 * the cost of code clarity or maintainability.  The memory usage of this ADT
 * is bound to the number of non-null elements.  Null elements have no additional
 * memory impact on the data structure.
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
public final class SparseList extends AbstractList {

    /** the root of the tree */
    private SparseListNode root = null;

    /** the total size of this data structure */
    private int size = 0;

    /** the size of tree */
    private int treeSize = 0;

    /**
     * Gets the size of this {@link List}.
     */
    public int size() {
        return size;
    }

    /**
     * Inserts a value into this tree at the given index
     */
    public void add(int index, Object value) {
        // Let nulls be inserted by the method created for that purpose
        if(value == null) {
            addNulls(index, 1);

        // The tree already has a root
        } else if(root != null) {
            // Insert a real node into the trailing nulls
            if(index >= treeSize) {
                int movingNulls = index - treeSize;
                size -= movingNulls;

                // Insert at the end of the tree
                root.insertAtEnd(value, movingNulls);
                treeSizeChanged();

            // Insert into the tree
            } else {
                root.insert(index, value);
                treeSizeChanged();
            }

        // Create a root for this tree
        } else {
            root = new SparseListNode(this, null, value, index);
            treeSize = index + 1;
            size++;
        }
    }

    /**
     * Inserts a sequence of nulls into the tree
     */
    public void addNulls(int index, int length) {
        // Increase the total tree size
        size += length;

        // The nulls are to be added to the actual tree
        if(root != null && index < treeSize) {
            root.insertEmptySpace(index, length);
            treeSize += length;
        }
    }

    /**
     * Gets the value in this tree at the given index
     */
    public Object get(int index) {
        SparseListNode node = getNode(index);
        // The value at that index is a null
        if(node == null) return null;
        return node.getValue();
    }

    /**
     * Gets the node for an element in this tree at the given index
     * or null if the value at that index is null
     */
    public SparseListNode getNode(int index) {
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
            treeSizeChanged();
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
            treeSizeChanged();
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
     * SparseListNode in the event that the tree's root is involved in
     * an AVL rotation.
     */
    void setRootNode(SparseListNode root) {
        this.root = root;
        if(root == null) {
            size -= treeSize;
            treeSize = 0;
        }
    }

    /**
     * Notifies the tree that the underlying tree size has changed. This method
     * is exposed for SparseListNode to make size adjustments.
     */
    void treeSizeChanged() {
        size -= treeSize;
        treeSize = root != null ? root.size() : 0;
        size += treeSize;
    }

    /**
     * Obtains an {@link Iterator} for this {@link List}.
     */
    public Iterator iterator() {
        if(size == 0) return Collections.EMPTY_LIST.iterator();
        return new SparseListNode.SparseListIterator(this, root);
    }

    /**
     * Prints out the structure of the tree for debug purposes.
     */
    public void printDebug() {
        System.out.println(root.toString());
    }
}