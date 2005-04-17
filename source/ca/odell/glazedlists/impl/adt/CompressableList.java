/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for implementing Java Collections List
import java.util.*;

/**
 * A compressable list is a list that is optimized for holding several
 * values that are null.
 *
 * <p>A compressable list can has a compressed view - this is a second list
 * that contains no null values. It is an error to modify the compressed
 * view.
 *
 * <p>The compressable list also has methods to get the compressed index from
 * the natural index, and the natural index from the compressed
 * index.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class CompressableList extends AbstractList {

    /** the root node of the tree, this may be replaced by a delete */
    private CompressableListNode root = null;

    /** a view of this list with no nulls */
    private CompressedList compressedList = new CompressedList();

    /** the total size can include trailing nulls beyond the tree */
    private int size = 0;

    /**
     * Gets the size of this tree.
     */
    public int size() {
        return size;
    }

    /**
     * Clears the compressable list.
     */
    public void clear() {
        size = 0;
        root = null;
    }

    /**
     * Sets the value at the specified location.
     *
     * This lazy implementation simply performs a remove and then
     * an add. This should be sufficient since the tree does not
     * punish size-changing operations too severely. This also
     * avoids the ugly cases of set() where a null is being
     * replaced by an Object and an Object being replaced by a null.
     *
     * @return the previous value at that location.
     */
    public Object set(int index, Object value) {
        Object previous = remove(index);
        add(index, value);
        return previous;
    }

    /**
     * Deletes the node with the specified sort-order from the tree.
     */
    public Object remove(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot remove from tree of size " + size + " at " + index);
        Object value = get(index);

        // remove from the main tree
        if(root != null && index < root.size()) {
            root.remove(index);

        // remove from the trailing nulls
        } else {
            size--;
        }

        return value;
    }

    /**
     * Inserts the specified value at the specified location.
     */
    public void add(int index, Object value) {
        if(index > size) throw new IndexOutOfBoundsException("cannot insert into a tree of size " + size + " at " + index);

        // insert nulls as space
        if(value == null) {
            insertNulls(index, 1);
            return;
        }
        // ensure the node exists
        if(root == null) {
            root = new CompressableListNode(this, null);
        }

        // when the insert is within the trailing nulls, remove them and re-insert them
        if(index > root.size()) {

            // the base size is the size of the tree
            int baseSize = root.size();

            // the interned nulls is the count of nulls moving into the tree
            int internedNulls = index - baseSize;
            childSizeChanged(-1 * internedNulls);
            root.insert(baseSize, value);

            // replace the removed nulls
            insertNulls(baseSize, internedNulls);

        // when the insert is within the main tree
        } else {
            root.insert(index, value);
        }
    }

    /**
     * Inserts a sequence of nulls into the list.
     */
    public void insertNulls(int index, int length) {
        if(index > size) throw new IndexOutOfBoundsException("cannot insert into a tree of size " + size + " at " + index);

        // insert the space after all else
        if(root == null || index >= root.size()) {
            size = size + length;
            return;
        }
        // ensure the node exists
        if(root == null) {
            root = new CompressableListNode(this, null);
        }

        // insert in the main tree
        root.insertSpace(index, length);
    }

    /**
     * Gets the value at the specified index.
     */
    public Object get(int index) {
        if(index >= size) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + index);

        // get from the main tree
        if(root != null && index < root.size()) {
            CompressableListNode node = root.getNodeByIndex(index);
            if(node == null) return null;
            return node.getValue();

        // get from the trailing nulls
        } else {
            return null;
        }
    }

    /**
     * Sets the root of this tree to be the specified node. This is
     * used by the owned CompressableListNodes when the root changes due to a
     * rotation.
     */
    void setRootNode(CompressableListNode root) {
        this.root = root;
    }

    /**
     * Changes the size of the tree. This is used by the owned
     * CompressableListNodes when the tree changes due to a removal.
     *
     * @param difference the amount of nodes that the tree has changed
     *      by. This is positive for adds and negative for removes.
     */
    void childSizeChanged(int difference) {
        size = size + difference;
    }

    /**
     * Verifies that the tree has a consistent state.
     */
    void validate() {
        if(root != null) root.validate();
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
            CompressableListNode node = root.getNodeByCompressedIndex(index);
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
        //CompressableListNode node = root.getNodeByCompressedIndex(compressedIndex);
        //return node.getIndex();
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
        if(index >= size) throw new IndexOutOfBoundsException("cannot get from a tree of size " + size + " at " + index);

        // get from the main tree
        if(root != null && index < root.size()) {
            CompressableListNode node = root.getNodeByIndex(index);
            if(node == null) throw new IllegalArgumentException("Cannot get compressed index of " + index + ", that value is compressed out");
            return node.getCompressedIndex();

        // get from the trailing nulls
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
        CompressableListNode node = root.getNodeByCompressedIndex(compressedIndex);
        if(node == null) throw new IllegalArgumentException("Cannot get compressed index of " + compressedIndex + ", that value is compressed out");
        return node.getNodeVirtualSize();
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
