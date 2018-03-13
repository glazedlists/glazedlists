/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex.adt;

// for specifying a sorting order
import java.util.*;

/**
 * A tree node that can be accessed either in sorted order or by
 * index.
 *
 * <p>This tree-node uses AVL-Trees to ensure that access is always
 * logarithmic in terms of the size of the tree. AVL Trees use
 * rotations (single and double) when the height of a pair of
 * subtrees do not match in order to guarantee a bound on the
 * difference in their height. This bound can be shown to provide
 * an overall bound on the access time on the tree.
 *
 * <p>As of October 9, 2005, this class can be used to create
 * <i>partially sorted</i> trees. This means that some subset of the nodes
 * are not considered when sorting. The purpose of this is to provide a
 * reasonable data store when new elements should be added in sorted order,
 * but old elements are not to be moved once added. To mark a node as unsorted,
 * use the
 *
 * @deprecated, replaced with {@link ca.odell.glazedlists.impl.adt.barcode2.BciiTree BC2}
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class IndexedTreeNode<V> {

    /** a token node */
    public static final IndexedTreeNode EMPTY_NODE = new IndexedTreeNode(null, "EMPTY_NODE");

    /** the parent node, used to delete from leaf up */
    IndexedTreeNode<V> parent;

    /** the left and right child nodes */
    IndexedTreeNode<V> left = null;
    IndexedTreeNode<V> right = null;

    /** the size of the left and right subtrees */
    int leftSize = 0;
    int rightSize = 0;

    /** the height of this subtree */
    private int height = 0;

    /**
     * The value of this node, which should be greater than all values in
     * the left subtree but less than all values in the right subtree.
     */
    private V value;

    /**
     * Whether to use the node's value when doing comparisons. Otherwise
     * the node value is a placeholder for a value and should <strong>not</strong>
     * be used when doing comparisons on operations like
     * {@link #getShallowestNodeWithValue} or {@link #insert}.
     */
    private boolean sorted = true;

    /**
     * Creates a new IndexedTreeNode with the specified parent node.
     */
    IndexedTreeNode(IndexedTreeNode<V> parent, V value) {
        this.parent = parent;
        this.value = value;
    }

    /**
     * Gets the value of this tree node.
     */
    public V getValue() {
        return value;
    }

    /**
     * Sets the value of this tree node. <strong>Warning:</strong> changing
     * the value of a node in a sorted tree may cause sorting to break
     * miserably.
     */
    public void setValue(V value) {
        this.value = value;
    }


    /**
     * Set this node to be used or ignored when sorting other values in the
     * tree. Unsorted nodes are useful when it is necessary to keep elements
     * in place even after their sort-order may have changed.
     */
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
    }

    /**
     * Whether this node is sorted.
     */
    public boolean isSorted() {
        return sorted;
    }

    /**
     * Gets the object with the specified index in the tree.
     */
    IndexedTreeNode<V> getNodeWithIndex(int index) {
        // recurse to the left
        if(index < leftSize) {
            return left.getNodeWithIndex(index);

        // recurse on the right side
        } else if(index > leftSize) {
            return right.getNodeWithIndex(index - (leftSize + 1));

        // return this node's root
        } else {
            return this;
        }
    }

    /**
     * @return <code>true</code> if this is on the left side of its parent.
     */
    private boolean isRightChild() {
        return parent != null && parent.right == this;
    }

    /**
     * @return <code>true</code> if this is on the right side of its parent.
     */
    private boolean isLeftChild() {
        return parent != null && parent.left == this;
    }

    /**
     * Whether the search value sorts on the left, right or center.
     *
     * @return 0 if this element equals the search value, a negative number if
     *      the search value belongs on the left and a positive number if the
     *      search value belongs on the right.
     */
    private int getSortSide(Comparator comparator, Object searchValue) {
        IndexedTreeNode currentFollower = this;
        while(true) {
            // we've hit the end of the list, assume the element is on the left hand side
            if(currentFollower == null) {
                return -1;
            // we've found a comparable element, use this!
            } else if(currentFollower.sorted) {
                return comparator.compare(searchValue, currentFollower.value);
            }

            // we failed to find a comparable element, try the next element after
            currentFollower = currentFollower.next();
        }
    }

    /**
     * @return the node whose index is one greater than this node, or <code>null</code>
     *      if this is the last node in the tree.
     */
    public IndexedTreeNode<V> next() {
        IndexedTreeNode<V> result;

        //  go to the leftmost child in the right subtree
        if(right != null) {
            result = right;
            while(result.left != null) {
                result = result.left;
            }

        // we're a root node with no right child, we have no follower
        } else if(parent == null) {
            result = null;

        // we're the top child on the left hand side, parent is next
        } else if(isLeftChild()) {
            result = parent;

        // we're in a right subtree, go all the way up to the parent of a left child
        } else if(isRightChild()) {
            IndexedTreeNode<V> parentOfRightChild = this.parent;
            while(parentOfRightChild.isRightChild()) {
                parentOfRightChild = parentOfRightChild.parent;
            }
            result = parentOfRightChild.parent;

        // we should have handled all cases already
        } else {
            throw new IllegalStateException();
        }

        return result;
    }

    /**
     * @return the node whose index is one less than this node, or <code>null</code>
     *      if this is the first node in the tree.
     */
    public IndexedTreeNode<V> previous() {
        IndexedTreeNode<V> result;

        //  go into the rightmost child in the left subtree
        if(left != null) {
            result = left;
            while(result.right != null) {
                result = result.right;
            }

        // we're a root node with no left child, we have no predecessor
        } else if(parent == null) {
            result = null;

        // we're the top child on the right hand side, parent is next
        } else if(isRightChild()) {
            result = parent;

        // we're in a left subtree, go all the way up to the parent of a right child
        } else if(isLeftChild()) {
            IndexedTreeNode<V> parentOfLeftChild = this.parent;
            while(parentOfLeftChild.isLeftChild()) {
                parentOfLeftChild = parentOfLeftChild.parent;
            }
            result = parentOfLeftChild.parent;

        // we should have handled all cases already
        } else {
            throw new IllegalStateException();
        }

        return result;
    }

    /**
     * Return the first node found such that {@link Comparator#compare} ranks
     * the specified object equal to that node's value. It is up to the caller
     * to narrow in on a more specific node to be used by methods like
     * {@link List#indexOf} etc.
     */
    IndexedTreeNode<V> getShallowestNodeWithValue(Comparator comparator, Object object, boolean approximate) {
        int sortSide = getSortSide(comparator, object);

        // we're found a matching node
        if(sortSide == 0) {
            return this;
        }

        // recurse on the left or the right, depending on the sort
        IndexedTreeNode<V> recurseNode = (sortSide < 0) ? left : right;
        if(recurseNode != null) {
            return recurseNode.getShallowestNodeWithValue(comparator, object, approximate);
        }

        // no result was found, but return 'this' as an approximation if requested
        return approximate ? this : null;
    }

    /**
     * Retrieves the size of this subtree.
     */
    int size() {
        return leftSize + 1 + rightSize;
    }

    /**
     * Retrieves the height of this subtree.
     */
    int height() {
        return height;
    }

    /**
     * Retrieves the subtree node with the largest value.
     */
    IndexedTreeNode<V> getLargestChildNode() {
        if(rightSize > 0) return right.getLargestChildNode();
        else return this;
    }
    /**
     * Retrieves the subtree node with the smallest value.
     */
    IndexedTreeNode<V> getSmallestChildNode() {
        if(leftSize > 0) return left.getSmallestChildNode();
        else return this;
    }


    /**
     * Gets the index of the current node, based on a recurrsive
     * path up the tree.
     */
    public int getIndex() {
        return getIndex(null);
    }
    private int getIndex(IndexedTreeNode<V> child) {
        // if the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;

        // if there is no child, get the index of the current node
        } else if(child == null) {
            if(parent != null) return parent.getIndex(this) + leftSize;
            return leftSize;

        // if the child is on the right, return the index recursively
        } else if(child == right) {
            if(parent != null) return parent.getIndex(this) + leftSize + 1;
            return leftSize + 1;
        }
        // if no child is found, we have a problem
        throw new IndexOutOfBoundsException(this + " cannot get the index of a subtree that does not exist on this node!");
    }


    /**
     * Inserts the specified object into the tree in sorted order.
     *
     * @return the IndexedTreeNode node where the object was inserted. This
     *      node can be used to call the deleteUp() method, which will
     *      delete the node from it's parent tree. It is also possible to
     *      use the getIndex() method on the node to discover what the sorted
     *      index of the value is.
     */
    IndexedTreeNode<V> insert(IndexedTree<V> host, V value) {
        boolean insertOnLeft = getSortSide(host.getComparator(), value) < 0;
        IndexedTreeNode<V> subtree = insertOnLeft ? left : right;

        final IndexedTreeNode<V> inserted;

        // update the size of the appropriate side
        if(insertOnLeft) this.leftSize++;
        else this.rightSize++;

        // if we need to create a new subtree, do that
        if(subtree == null) {
            inserted = new IndexedTreeNode<>(this, value);

            // assign a member value to the subtree
            if(insertOnLeft) this.left = inserted;
            else this.right = inserted;

            // do the necessary rotations
            inserted.ensureAVL(host);

        // recurse on our subtree
        } else {
            inserted = subtree.insert(host, value);
        }

        return inserted;
    }
    /**
     * Inserts the specified object into the tree with the specified index.
     *
     * @return the IndexedTreeNode node where the object was inserted. This
     *      node can be used to call the deleteUp() method, which will
     *      delete the node from it's parent tree. It is also possible to call
     *      the getIndex() method on the node. As new nodes are inserted, the
     *      index will shift. The getIndex() method can be used to get the
     *      current index of the node at any time.
     */
    IndexedTreeNode<V> insert(IndexedTree<V> host, int index, V value) {
        boolean insertOnLeft = index <= leftSize;
        IndexedTreeNode<V> subtree = insertOnLeft ? left : right;

        final IndexedTreeNode<V> inserted;

        // update the size of the appropriate side
        if(insertOnLeft) this.leftSize++;
        else this.rightSize++;

        // we need to create a new subtree, do that
        if(subtree == null) {
            inserted = new IndexedTreeNode<>(this, value);

            // assign a member value to the subtree
            if(insertOnLeft) this.left = inserted;
            else this.right = inserted;

            // do the necessary rotations
            inserted.ensureAVL(host);

        // recurse on our selected subtree
        } else {
            int recurseIndex = insertOnLeft ? index : index - leftSize - 1;
            inserted = subtree.insert(host, recurseIndex, value);
        }

        return inserted;
    }

    /**
     * Recurses down from the root and removes the node at the given index.
     */
    IndexedTreeNode<V> removeNode(IndexedTree<V> host, int index) {
        // recurse to the left
        if(index < leftSize) {
            leftSize--;
            return left.removeNode(host, index);

        // recurse on the right side
        } else if(index > leftSize) {
            rightSize--;
            return right.removeNode(host, index - (leftSize + 1));
        }

        // if this is a leaf, we can delete it outright
        if(leftSize == 0 && rightSize == 0) {
            // update the parent
            if(parent != null) {
                parent.replaceChildNode(this, null);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(null);
            }
        // if this node has only a left child, we can replace this with that child
        } else if(leftSize > 0 && rightSize == 0) {
            // update the left child
            left.parent = parent;
            // update the parent
            if(parent != null) {
                parent.replaceChildNode(this, left);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(left);
            }
        // if this node has only a right child, we can replace this with that child
        } else if(leftSize == 0 && rightSize > 0) {
            // update the right child
            right.parent = parent;
            // update the parent
            if(parent != null) {
                parent.replaceChildNode(this, right);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(right);
            }
        // if this node has two children, replace this node with the best of the biggest
        } else {
            IndexedTreeNode<V> replacement = null;
            // if the left side is larger, use a left side node
            if(leftSize > rightSize) {
                leftSize--;
                replacement = left.pruneLargestChild(host);

            // otherwise use a right side node
            } else {
                rightSize--;
                replacement = right.pruneSmallestChild(host);
            }

            // update the left child
            replacement.left = left;
            replacement.leftSize = leftSize;
            if(left != null) left.parent = replacement;

            // update the right child
            replacement.right = right;
            replacement.rightSize = rightSize;
            if(right != null) right.parent = replacement;
            // update the height
            replacement.height = height;
            // update the parent
            replacement.parent = parent;
            if(parent != null) {
                parent.replaceChildNode(this, replacement);
            } else {
                host.setRootNode(replacement);
            }
        }
        // clear all the linking and size values
        clearNodeValues();
        return this;
    }

    /**
     * Removes the smallest child of the subtree rooted at this and returns it.
     */
    IndexedTreeNode<V> pruneSmallestChild(IndexedTree<V> host) {
        // recurse down the tree
        if(leftSize > 0) {
            leftSize--;
            return left.pruneSmallestChild(host);
        }

        IndexedTreeNode<V> replacement = null;
        // this node has a right child
        if(rightSize != 0) {
            replacement = right;
            right.parent = parent;
        }

        // update the parent
        if(parent != null) {
            parent.replaceChildNode(this, replacement);
            parent.ensureAVL(host);
        } else {
            host.setRootNode(replacement);
        }

        // clear all the linking and size values
        clearNodeValues();
        return this;
    }


    /**
     * Removes the largest child of the subtree rooted at this and returns it.
     */
    IndexedTreeNode<V> pruneLargestChild(IndexedTree<V> host) {
        // recurse down the tree
        if(rightSize > 0) {
            rightSize--;
            return right.pruneLargestChild(host);
        }

        IndexedTreeNode<V> replacement = null;
        // this node has a left child
        if(leftSize != 0) {
            replacement = left;
            left.parent = parent;
        }

        // update the parent
        if(parent != null) {
            parent.replaceChildNode(this, replacement);
            parent.ensureAVL(host);
        } else {
            host.setRootNode(replacement);
        }

        // clear all the linking and size values
        clearNodeValues();
        return this;
    }


    /**
     * Unlinks this node from the sorted tree. This may cause the tree to
     * rotate nodes using AVL rotations.
     */
    public void removeFromTree(IndexedTree<V> host) {
        // if this node has no value, we have a problem!
        assert(value != null);
        // if this is a leaf, we can delete it outright
        if(leftSize == 0 && rightSize == 0) {
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, null);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(null);
            }
        // if this node has only a left child, we can replace this with that child
        } else if(leftSize > 0 && rightSize == 0) {
            // update the left child
            left.parent = parent;
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, left);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(left);
            }
        // if this node has only a right child, we can replace this with that child
        } else if(leftSize == 0 && rightSize > 0) {
            // update the right child
            right.parent = parent;
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, right);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(right);
            }
        // if this node has two children, replace this node with the best of the biggest
        } else {
            IndexedTreeNode<V> middle = null;
            // if the left side is larger, use a left side node
            if(leftSize > rightSize) {
                middle = left.getLargestChildNode();
            // otherwise use a right side node
            } else {
                middle = right.getSmallestChildNode();
            }
            middle.removeFromTree(host);
            // cannot have new middle with leaves
            assert(middle.leftSize == 0 && middle.rightSize == 0);
            // update the left child
            middle.left = left;
            middle.leftSize = leftSize;
            if(left != null) left.parent = middle;
            // update the right child
            middle.right = right;
            middle.rightSize = rightSize;
            if(right != null) right.parent = middle;
            // update the height
            middle.height = height;
            // update the parent
            middle.parent = parent;
            if(parent != null) {
                parent.replaceChildNode(this, middle);
                parent.ensureAVL(host);
            } else {
                host.setRootNode(middle);
            }
        }
        // clear all the linking and size values
        clearNodeValues();
    }

    /**
     * Clears all values related to how this node is linked in the tree, but
     * does NOT clear the value of the node available via setValue().
     */
    private void clearNodeValues() {
        // clear the parent
        parent = null;
        // clear the left child
        left = null;
        leftSize = 0;
        // clear the right child
        right = null;
        rightSize = 0;
    }

    /**
     * Notifies that a node has been removed from the specified subtree.
     * This simply decrements the count on that subtree.
     */
    private void notifyChildNodeRemoved(IndexedTreeNode<V> subtree) {
        if(subtree == left) leftSize--;
        else if(subtree == right) rightSize--;
        else throw new IllegalArgumentException(this + " cannot remove a subtree that does not exist on this node!");
        if(parent != null) parent.notifyChildNodeRemoved(this);
    }
    /**
     * Replaces the specified child with a new child.
     */
    private void replaceChildNode(IndexedTreeNode<V> original, IndexedTreeNode<V> replacement) {
        if(original == left) left = replacement;
        else if(original == right) right = replacement;
        else throw new IllegalArgumentException(this + " cannot replace a non-existant child");
    }

    /**
     * A primitive way to validate that nodes are stored in sorted
     * order and that their sizes are consistent. This throws a
     * IllegalStateException if any infraction is found.
     */
    void validate(IndexedTree<V> host) {
        // first validate the children
        if(left != null) left.validate(host);
        if(right != null) right.validate(host);

        // validate sort order
        if(host.getComparator() != null && sorted) {
            if(leftSize > 0 && left.sorted && host.getComparator().compare(left.value, value) > 0) {
                throw new IllegalStateException("" + this + "left node, \"" + left + "\" larger than middle node, \"" + this + "\"");
            }
            if(rightSize > 0 && right.sorted && host.getComparator().compare(value, right.value) > 0) {
                throw new IllegalStateException("" + this + " middle node, \"" + this + "\" larger than right node, \"" + right + "\"");
            }
        }
        // validate left size
        if((left == null && leftSize != 0) || (left != null && leftSize != left.size())) {
            throw new IllegalStateException("Cached leftSize " + leftSize + " != reported left.size() " + left.size());
        }
        // validate right size
        if((right == null && rightSize != 0) || (right != null && rightSize != right.size())) {
            throw new IllegalStateException("Cached rightSize " + rightSize + " != reported right.size() " + right.size());
        }
    }

    /**
     * Ensures that the tree satisfies the AVL property.  It is sufficient to
     * recurse up the tree only as long as height recalculations are needed.
     * As such, this method is intended to be called only on a node whose height
     * may be out of sync due to an insertion or deletion.
     */
    private void ensureAVL(IndexedTree<V> host) {
        int oldHeight = height;
        recalculateHeight();
        avlRotate(host);

        // If adjustments were made, recurse up the tree
        if(height != oldHeight && parent != null) parent.ensureAVL(host);
    }

    /**
     * Recalculates the cached height at this level.
     */
    private void recalculateHeight() {
        int leftHeight = left == null ? 0 : left.height;
        int rightHeight = right == null ? 0 : right.height;
        height = 1 + Math.max(leftHeight, rightHeight);
    }

    /**
     * Determines if AVL rotations are required and performs them if they are.
     */
    private void avlRotate(IndexedTree<V> host) {
        // look up the left and right heights
        int leftHeight = (left != null ? left.height : 0);
        int rightHeight = (right != null ? right.height : 0);

        // rotations will be on the left
        if(leftHeight - rightHeight >= 2) {
            // determine if a double rotation is necessary
            int leftLeftHeight = (left.left != null ? left.left.height : 0);
            int leftRightHeight = (left.right != null ? left.right.height : 0);

            // Perform first half of double rotation if necessary
            if(leftRightHeight > leftLeftHeight) left.rotateRight(host);

            // Do the rotation for this node
            rotateLeft(host);

        // rotations will be on the right
        } else if(rightHeight - leftHeight >= 2) {
            // determine if a double rotation is necessary
            int rightLeftHeight = (right.left != null ? right.left.height : 0);
            int rightRightHeight = (right.right != null ? right.right.height : 0);

            // Perform first half of double rotation if necessary
            if(rightLeftHeight > rightRightHeight) right.rotateLeft(host);

            // Do the rotation for this node
            rotateRight(host);
        }
    }

    /**
     * AVL-Rotates this subtree with its left child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateLeft(IndexedTree<V> host) {
        // The replacement node is on the left
        IndexedTreeNode<V> replacement = left;

        // take the right child of the replacement as my left child
        left = replacement.right;
        leftSize = replacement.rightSize;
        if(replacement.right != null) replacement.right.parent = this;

        // set the right child of the replacement to this
        replacement.right = this;
        replacement.rightSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replaceChildNode(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        // fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    /**
     * AVL-Rotates this subtree with its right child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateRight(IndexedTree<V> host) {
        // The replacement node is on the right
        IndexedTreeNode<V> replacement = right;

        // take the right child of the replacement as my left child
        right = replacement.left;
        rightSize = replacement.leftSize;
        if(replacement.left != null) replacement.left.parent = this;

        // set the right child of the replacement to this
        replacement.left = this;
        replacement.leftSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replaceChildNode(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        //fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    /**
     * Prints the tree by its contents.
     */
    @Override
    public String toString() {

//        String leftString = left == null ? "." : left.toString();
        String valueString = value instanceof IndexedTreeNode ? "NODE " + ((IndexedTreeNode)value).getIndex() : value.toString();
//        String rightString = right == null ? "." : right.toString();
//        return valueString;
//        return "(" + leftString + " " + valueString + " " + rightString + ")";
        int index = getIndex();
        return "[" + index + "] " + valueString;
    }
}
