/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

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
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class IndexedTreeNode {

    /** the parent node, used to delete from leaf up */
    private IndexedTreeNode parent;

    /** the indexed tree that this node is a member of */
    private IndexedTree host;

    /** the left and right child nodes */
    private IndexedTreeNode left = null;
    private IndexedTreeNode right = null;

    /** the size of the left and right subtrees */
    private int leftSize = 0;
    private int rightSize = 0;
    private int rootSize = 0;

    /** the height of this subtree */
    private int height = 0;

    /** the value of this node, assuming it is a leaf */
    private Object value;

    /**
     * Creates a new IndexedTreeNode with the specified parent node.
     */
    IndexedTreeNode(IndexedTree host, IndexedTreeNode parent) {
        this.host = host;
        this.parent = parent;
    }

    /**
     * Gets the value of this tree node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this tree node. <strong>Warning:</strong> changing
     * the value of a node in a sorted tree may cause sorting to break
     * miserably.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the object with the specified index in the tree.
     */
    IndexedTreeNode getNodeWithIndex(int index) {
        // ensure the index value is valid
        if(index >= leftSize + rightSize + rootSize) throw new IndexOutOfBoundsException("cannot get from tree of size " + size() + " at " + index);
        // recurse to the left
        if(index < leftSize) {
            return left.getNodeWithIndex(index);
        // return this node's root
        } else if(index < leftSize + rootSize) {
            return this;
        // recurse on the right side
        } else {
            return right.getNodeWithIndex(index - (leftSize + rootSize));
        }
    }

    /**
     * Gets the object with the specified value in the tree.
     */
    IndexedTreeNode getNodeByValue(Object searchValue) {
        int sortSide = host.getComparator().compare(searchValue, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return null;
            return left.getNodeByValue(searchValue);
        // if it equals this node, return this
        } else if(sortSide == 0) {
            return this;
        // if it sorts on the right side, search there
        } else {
            if(right == null) return null;
            return right.getNodeByValue(searchValue);
        }
    }

    /**
     * Retrieves the size of this subtree.
     */
    int size() {
        return leftSize + rootSize + rightSize;
    }

    /**
     * Creates an iterator starting at this node and continuing from here
     * through the tree.
     */
    Iterator iterator() {
        return new IndexedTreeIterator(this);
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
    IndexedTreeNode getLargestChildNode() {
        if(rightSize > 0) return right.getLargestChildNode();
        else return this;
    }
    /**
     * Retrieves the subtree node with the smallest value.
     */
    IndexedTreeNode getSmallestChildNode() {
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
    private int getIndex(IndexedTreeNode child) {
        // if the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;
        }
        // if there is no child, get the index of the current node
        if(child == null) {
            if(parent != null) return parent.getIndex(this) + leftSize;
            return leftSize;
        }
        // if the child is on the right, return the index recursively
        if(child == right) {
            if(parent != null) return parent.getIndex(this) + leftSize + rootSize;
            return leftSize + rootSize;
        }
        // if no child is found, we have a problem
        throw new IllegalArgumentException(this + " cannot get the index of a subtree that does not exist on this node!");
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
    IndexedTreeNode insert(Object inserted) {
        // if this is a newborn leaf, the value can be null as long as there are no children
        if(value == null) {
            if(leftSize > 0 || rightSize > 0) throw new IllegalStateException("insert into non-leaf node with null value");
            rootSize++;
            value = inserted;
            recalculateHeight();
            return this;
        }
        // if it sorts on the left side, insert there
        if(host.getComparator().compare(inserted, value) < 0) {
            if(left == null) left = new IndexedTreeNode(host, this);
            leftSize++;
            IndexedTreeNode result = left.insert(inserted);
            // perform any necessary AVL-Rotations to keep the tree balanced
            doRotationsForThisLevel();
            return result;
        // if it doesn't sort on the left side, insert on the right
        } else {
            if(right == null) right = new IndexedTreeNode(host, this);
            rightSize++;
            IndexedTreeNode result = right.insert(inserted);
            // perform any necessary AVL-Rotations to keep the tree balanced
            doRotationsForThisLevel();
            return result;
        }
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
    IndexedTreeNode insert(int index, Object inserted) {
        if(index > leftSize + rootSize + rightSize) throw new IndexOutOfBoundsException("cannot insert into tree of size " + (leftSize + rootSize + rightSize) + " at " + index);
        if(inserted == null) throw new NullPointerException("cannot insert a value that is null");
        // if this node has no value, insert as a leaf
        if(index == 0 && value == null) {
            if(leftSize > 0 || rightSize > 0) throw new IllegalStateException("insert into non-leaf node with null value");
            rootSize++;
            value = inserted;
            recalculateHeight();
            return this;
        // if the index is on the left side, insert there
        } else if(index <= leftSize) {
            if(left == null) left = new IndexedTreeNode(host, this);
            leftSize++;
            IndexedTreeNode result = left.insert(index, inserted);
            // perform any necessary AVL-Rotations to keep the tree balanced
            doRotationsForThisLevel();
            return result;
        // if the index is not on the left side, insert on the right
        } else {
            if(right == null) right = new IndexedTreeNode(host, this);
            rightSize++;
            IndexedTreeNode result = right.insert(index - leftSize - rootSize, inserted);
            // perform any necessary AVL-Rotations to keep the tree balanced
            doRotationsForThisLevel();
            return result;
        }
    }


    /**
     * Unlinks this node from the sorted tree. This may cause the tree to
     * rotate nodes using AVL rotations.
     */
    public void removeFromTree() {
        // if this node has no value, we have a problem!
        if(value == null) throw new IllegalStateException("cannot delete a node with no value");
        // if this is a leaf, we can delete it outright
        if(leftSize == 0 && rightSize == 0) {
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, null);
                parent.recalculateHeight();
            } else {
                host.setRootNode(null);
            }
            // clear the parent
            parent = null;
        // if this node has only a left child, we can replace this with that child
        } else if(leftSize > 0 && rightSize == 0) {
            // update the left child
            left.parent = parent;
            // update the parent
            if(parent != null) {
                parent.notifyChildNodeRemoved(this);
                parent.replaceChildNode(this, left);
                parent.recalculateHeight();
                parent.doRotationsUpTheTree();
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
                parent.recalculateHeight();
                parent.doRotationsUpTheTree();
            } else {
                host.setRootNode(right);
            }
        // if this node has two children, replace this node with the best of the biggest
        } else {
            IndexedTreeNode middle = null;
            // if the left side is larger, use a left side node
            if(leftSize > rightSize) {
                middle = left.getLargestChildNode();
            // otherwise use a right side node
            } else {
                middle = right.getSmallestChildNode();
            }
            middle.removeFromTree();
            if(middle.leftSize > 0 || middle.rightSize > 0) throw new IllegalStateException("cannot have a new middle with leaves");
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
                parent.doRotationsUpTheTree();
            } else {
                host.setRootNode(middle);
            }
        }
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
    private void notifyChildNodeRemoved(IndexedTreeNode subtree) {
        if(subtree == left) leftSize--;
        else if(subtree == right) rightSize--;
        else throw new IllegalArgumentException(this + " cannot remove a subtree that does not exist on this node!");
        if(parent != null) parent.notifyChildNodeRemoved(this);
    }
    /**
     * Replaces the specified child with a new child.
     */
    private void replaceChildNode(IndexedTreeNode original, IndexedTreeNode replacement) {
        if(original == left) left = replacement;
        else if(original == right) right = replacement;
        else throw new IllegalArgumentException(this + " cannot replace a non-existant child");
    }

    /**
     * A primitive way to validate that nodes are stored in sorted
     * order and that their sizes are consistent. This throws a
     * IllegalStateException if any infraction is found.
     */
    void validate() {
        // first validate the children
        if(left != null) left.validate();
        if(right != null) right.validate();

        // validate sort order
        if(host.getComparator() != null) {
            if(leftSize > 0 && rootSize > 0 && host.getComparator().compare(left.value, value) > 0) {
                throw new IllegalStateException("" + this + "left larger than middle");
            }
            if(rightSize > 0 && rootSize > 0) if(host.getComparator().compare(value, right.value) > 0) {
                throw new IllegalStateException("" + this + " middle larger than right");
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
     * When the height of a subtree changes, propogate that change up the
     * tree.
     */
    private void recalculateHeight() {
        // save the old height to test for a difference
        int oldHeight = height;

        // calculate the new height
        if(left == null && right == null) height = 1;
        else if(right == null) height = 1 + left.height();
        else if(left == null) height = 1 + right.height();
        else height = 1 + Math.max(left.height(), right.height());

        // propagate changes upstream if the height changed
        if(height != oldHeight && parent != null) parent.recalculateHeight();
    }

    /**
     * Checks the heights of the left and right child nodes, and does rotations
     * if necessary. This only does rotations at the current node. It is necessary
     * to use another method to send more recursive rotations up or down the
     * tree.
     */
    private void doRotationsForThisLevel() {
        // look up the left and right heights
        int leftHeight = (left != null ? left.height() : 0);
        int rightHeight = (right != null ? right.height() : 0);
        // rotations will be on the left
        if(leftHeight - rightHeight >= 2) {
            // do the first rotation in a double-rotation if necessary
            int leftLeftHeight = (left.left != null ? left.left.height() : 0);
            int leftRightHeight = (left.right != null ? left.right.height() : 0);
            if(leftRightHeight > leftLeftHeight) {
                left.rotateRight();
            }
            // rotate on this node
            rotateLeft();
        // rotations will be on the right
        } else if(rightHeight - leftHeight >= 2) {
            // do the first rotation in a double-rotation if necessary
            int rightLeftHeight = (right.left != null ? right.left.height() : 0);
            int rightRightHeight = (right.right != null ? right.right.height() : 0);
            if(rightLeftHeight > rightRightHeight) {
                right.rotateLeft();
            }
            // rotate on this node
            rotateRight();
        }
    }
    /**
     * Performs clean-up rotations from this node all the way up the tree. When
     * this completes, every node from this up to the root should be balanced.
     */
    private void doRotationsUpTheTree() {
        doRotationsForThisLevel();
        // at the leaf level, just have the parent do rotations
        if(parent != null) parent.doRotationsUpTheTree();
    }
    /**
     * AVL-Rotates this subtree with its left child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateLeft() {
        if(left == null) throw new IllegalArgumentException("Cannot rotate with a null child");
        IndexedTreeNode replacement = left;
        // take the right child of the replacement as my left child
        left = replacement.right;
        leftSize = replacement.rightSize;
        if(replacement.right != null) replacement.right.parent = this;
        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) {
            parent.replaceChildNode(this, replacement);
        } else {
            host.setRootNode(replacement);
        }
        replacement.parent = parent;
        parent = replacement;
        // set the right child of the replacement to this
        replacement.right = this;
        replacement.rightSize = size();
        // recalculate heights
        recalculateHeight();
        if(replacement.parent != null) replacement.parent.recalculateHeight();
    }
    /**
     * AVL-Rotates this subtree with its right child.
     *
     * For every link (left, right, parent), there are up to three
     * updates to be made. We need to set the new value on the
     * replacement, the new value on this, and the new value on the
     * other node.
     */
    private void rotateRight() {
        if(right == null) throw new IllegalArgumentException("Cannot rotate with a null child");
        IndexedTreeNode replacement = right;
        // take the right child of the replacement as my left child
        right = replacement.left;
        rightSize = replacement.leftSize;
        if(replacement.left != null) replacement.left.parent = this;
        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) {
            parent.replaceChildNode(this, replacement);
        } else {
            host.setRootNode(replacement);
        }
        replacement.parent = parent;
        parent = replacement;
        // set the right child of the replacement to this
        replacement.left = this;
        replacement.leftSize = size();
        // recalculate heights
        recalculateHeight();
        if(replacement.parent != null) replacement.parent.recalculateHeight();
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    boolean contains(Object object) {
        int sortSide = host.getComparator().compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return false;
            return left.contains(object);

        // if it equals this node, return this
        } else if(sortSide == 0) {
            return true;

        // if it sorts on the right side, search there
        } else {
            if(right == null) return false;
            return right.contains(object);
        }
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    int indexOf(Object object) {
        int sortSide = host.getComparator().compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return -1;
            return left.indexOf(object);

        // if it equals this node, search to the left for equal values
        } else if(sortSide == 0) {
            return findLastNode(this, object, true);

        // if it sorts on the right side, search there
        } else {
            if(right == null) return -1;
            return right.indexOf(object);
        }
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>This method has package-level protection as if it is
     * called on a node directly it will have non-deterministic
     * results.
     */
    int lastIndexOf(Object object) {
        int sortSide = host.getComparator().compare(object, value);

        // if it sorts on the left side, search there
        if(sortSide < 0) {
            if(left == null) return -1;
            return left.lastIndexOf(object);

        // if it equals this node, search to the right for equal values
        } else if(sortSide == 0) {
            return findLastNode(this, object, false);

        // if it sorts on the right side, search there
        } else {
            if(right == null) return -1;
            return right.lastIndexOf(object);
        }
    }

    /**
     * Helper method to the indexOf and lastIndexOf methods
     */
    private int findLastNode(IndexedTreeNode parentNode, Object object, boolean goLeft) {
        IndexedTreeNode child = null;
        if(goLeft) child = parentNode.left;
        else child = parentNode.right;

        // Child doesn't exist
        if(child == null) {
            return parentNode.getIndex();

        // Child is different than the parent node
        } else if(host.getComparator().compare(object, child.value) != 0) {
            int result = secondaryLastNodeSearch(child, object, !goLeft);
            if(result != -1) {
                return result;
            }
            return parentNode.getIndex();

        // Child is the same so recurse on the child
        } else {
            return findLastNode(child, object, goLeft);
        }
    }

    /**
     * Helper method to the indexOf and lastIndexOf methods
     */
    private int secondaryLastNodeSearch(IndexedTreeNode parentNode, Object object, boolean goLeft) {
        IndexedTreeNode child = null;
        if(goLeft) child = parentNode.left;
        else child = parentNode.right;

        // Child doesn't exist
        if(child == null) {
            return -1;

        // Child is the same as the searched for node
        } else if(host.getComparator().compare(object, child.value) == 0) {
            return findLastNode(child, object, !goLeft);

        // Child is different so recurse on the child
        } else {
            return secondaryLastNodeSearch(child, object, goLeft);
        }
    }

    /**
     * Prints the tree by its contents.
     */
    public String toString() {
        //String valueString = "" + height();
        String valueString = value.toString();
        if(left != null && right != null) {
            return "(" + left.toString() + " " + valueString + " " + right.toString() + ")";
        } else if(left != null) {
            return "(" + left.toString() + " " + valueString + " .)";
        } else if(right != null) {
            return "(. " + valueString + " " + right.toString() + ")";
        } else if(value == null) {
            return ".";
        } else {
            return valueString;
        }
    }

    /**
     * A simple read-only iterator of the indexed tree.
     *
     * @todo implement write capability, and backward-forward
     *      motion to complete the ListIterator interface.
     */
    static class IndexedTreeIterator implements Iterator {

        /** the last tree node returned by this iterator */
        private IndexedTreeNode next;

        /**
         * Creates an iterator that iterates the tree starting at the specified
         * node.
         */
        public IndexedTreeIterator(IndexedTreeNode first) {
            this.next = first;
        }

        /**
         * Returns true if the iteration has more elements.
         */
        public boolean hasNext() {
            return (next != null);
         }

        /**
         * Returns the next element in the iteration.
         */
        public Object next() {
            // there are no more nodes right of this one
            if (next == null) throw new NoSuchElementException();
            // before returning the result, calculate the result to follow
            IndexedTreeNode result = next;
            // if there's a right child, return that child's leftmost child
            if(next.rightSize != 0) {
                next = next.right.getSmallestChildNode();
            // if there's no right child, return the first right parent
            } else {
                IndexedTreeNode currentParent = next;
                next = null;
                // set the next value if this node has a parent on the right somewhere
                while(currentParent.parent != null) {
                    if(currentParent.parent.left == currentParent) {
                        next = currentParent.parent;
                        break;
                    } else {
                        currentParent = currentParent.parent;
                    }
                }
            }
            return result;
        }

        /**
         * This operation is <strong>not implemented</strong>. Removes from
         * the underlying collection the last element returned by the iterator.
         */
        public void remove() {
            throw new UnsupportedOperationException("The method is not implemented.");
        }
    }
}

