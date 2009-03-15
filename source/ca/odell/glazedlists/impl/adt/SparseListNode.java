/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for iterators
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A SparseListNode models a node in an SparseList.  This class
 * does the bulk of the heavy lifting for SparseList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 *
 */
public final class SparseListNode {

    /** the parent node */
    private SparseListNode parent;

    /** the tree that this node is a member of */
    private SparseList host;

    /** the left and right child nodes */
    private SparseListNode left = null;
    private SparseListNode right = null;

    /** the size of the left subtree and right subtrees including empty space */
    private int totalRightSize = 0;
    private int totalLeftSize = 0;

    /** the amount of empty space that preceeds this node */
    private int emptySpace = 0;

    /** the height of this subtree */
    private int height = 1;

    /** the value at this node */
    private Object value = null;

    /**
     * Creates a new SparseListNode with the specified parent node, host tree and value.
     */
    SparseListNode(SparseList host, SparseListNode parent, Object value) {
        this.host = host;
        this.parent = parent;
        this.value = value;
    }

    /**
     * This is a convienience constructor for creating a new SparseListNode
     * with a given value and amount of preceeding empty space.
     */
    SparseListNode(SparseList host, SparseListNode parent, Object value, int emptySpace) {
        this(host, parent, value);
        this.emptySpace = emptySpace;
    }

    /**
     * Returns the size of the subtree rooted at this node
     */
    int size() {
        return totalLeftSize + emptySpace + totalRightSize + 1;
    }

    /**
     * Inserts a value into the host tree.
     */
    void insert(int index, Object value) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left adjusting sizes as you go
        if(localizedIndex < 0) {
            totalLeftSize++;
            left.insert(index, value);

        // Recurse to the Right adjusting sizes as you go
        } else if(localizedIndex > emptySpace) {
            totalRightSize++;
            right.insert(localizedIndex - emptySpace - 1, value);

        // Insert in the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            emptySpace -= localizedIndex;
            totalLeftSize += localizedIndex + 1;
            if(left == null) {
                left = new SparseListNode(host, this, value, localizedIndex);
                ensureAVL();
            } else {
                left.insertAtEnd(value, localizedIndex);
            }

        // Insert at the same index as this node
        } else {
            insertAtThisNode(value);
        }
    }

    /**
     * Inserts a value into the host tree at an index where a value already
     * exists.  This will offset the current node's value by 1.
     */
    private void insertAtThisNode(Object value) {
        SparseListNode replacement = new SparseListNode(host, parent, value, emptySpace);
        emptySpace = 0;
        replacement.height = height;
        height = 1;
        replacement.totalRightSize = totalRightSize + 1;
        // Since the left side will be unaffected by this insert, just 'move' it onto the replacement
        replacement.left = left;
        if(left != null) {
            replacement.left.parent = replacement;
            replacement.totalLeftSize = totalLeftSize;
            totalLeftSize = 0;
            left = null;
        }

        // Notify the host tree that the root has changed
        if(parent == null) host.setRootNode(replacement);

        // Replace this with the new child in the parent
        else parent.replace(this, replacement);

        // Move this to the right child of the replacement
        if(right == null) {
            parent = replacement;
            replacement.right = this;
            replacement.ensureAVL();

        // Move this to be the smallest node in the right subtree
        } else {
            replacement.right = right;
            replacement.right.parent = replacement;
            totalRightSize = 0;
            right = null;
            replacement.right.moveToSmallest(this);
        }
    }

    /**
     * Inserts a value at the end of the tree rooted at this.
     */
    void insertAtEnd(Object value, int leadingNulls) {
        // Adjust sizes during recursion
        totalRightSize += leadingNulls + 1;

        // Recurse to the right
        if(right != null) right.insertAtEnd(value, leadingNulls);

        // Insert on the right
        else {
            right = new SparseListNode(host, this, value, leadingNulls);
            ensureAVL();
        }
    }

    /**
     * Inserts multiple null values as empty space in the host tree.
     */
    void insertEmptySpace(int index, int length) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) {
            totalLeftSize += length;
            left.insertEmptySpace(index, length);

        // Recurse to the Right
        } else if(localizedIndex > emptySpace) {
            totalRightSize += length;
            right.insertEmptySpace(localizedIndex - emptySpace - 1, length);

        // Insert at this node
        } else {
            emptySpace += length;
        }
    }

    /**
     * Moves a given node to be the smallest node in the subtree rooted at
     * this.
     */
    private void moveToSmallest(SparseListNode movingNode) {
        // Adjust sizes during recursion
        totalLeftSize += movingNode.emptySpace + 1;

        // Recurse to the left
        if(left != null) {
            left.moveToSmallest(movingNode);

        // Add the node as a left child of this
        } else {
            // Add the moving node on the left
            movingNode.parent = this;
            left = movingNode;

            // Adjust heights and rotate if necessary
            ensureAVL();
        }
    }

    /**
     * Gets the index of the value in this node.  This is NOT the index of the
     * first null indexed by this node.
     */
    public int getIndex() {
        if(parent != null) return parent.getIndex(this) + totalLeftSize + emptySpace;
        return totalLeftSize + emptySpace;
    }
    private int getIndex(SparseListNode child) {
        // the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;

        // the child is on the right, return the index recursively
        } else {
            if(parent != null) return parent.getIndex(this) + totalLeftSize + emptySpace + 1;
            return totalLeftSize + emptySpace + 1;
        }
    }

    /**
     * Gets the node with the given index, or null if that index is empty.
     */
    SparseListNode getNode(int index) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getNode(index);

        // Recurse to the Right
        else if(localizedIndex > emptySpace) return right.getNode(localizedIndex - emptySpace - 1);

        // Get a null from the middle of the empty space
        else if(localizedIndex < emptySpace) return null;

        // Get this node
        else return this;
    }

    /**
     * Gets the value of this node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this node and returns the replaced value.
     * If the value is set to null, this node  will be removed from
     * the tree and clear() will be called.
     */
    public Object setValue(Object value) {
        // Just a simple set operation
        if(value != null) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;

        // This node must be removed and replaced with empty space
        } else {
            emptySpace++;
            return unlink();
        }
    }

    /**
     * Sets the value of the node at a given index.
     */
    Object set(int index, Object value) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) {
            return left.set(index, value);

        // Recurse to the Right
        } else if(localizedIndex > emptySpace) {
            return right.set(localizedIndex - emptySpace - 1, value);

        // Set a value in the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            if(value == null) return null;
            emptySpace--;
            insert(index, value);
            return null;

        // Set the value in this node
        } else {
            return setValue(value);
        }
    }

    /**
     * Removes and returns the value at the given index.
     */
    Object remove(int index) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) {
            totalLeftSize--;
            return left.remove(index);

        // Recurse to the Right
        } else if(localizedIndex > emptySpace) {
            totalRightSize--;
            return right.remove(localizedIndex - emptySpace - 1);

        // Remove from the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            emptySpace--;
            return null;

        // Remove from the value in this node
        } else {
            return unlink();
        }
    }

    /**
     * Unlinks this node from the tree and clears it.
     */
    private Object unlink() {
        int index = -1;
        SparseListNode replacement = null;
        boolean isLeftChild = false;

        // Two children exist
        if(right != null && left != null) {
            return unlinkFromTwoChildren();

        // Only a right child exists
        } else if(right != null) {
            replacement = right;
            replacement.parent = parent;
            replacement.emptySpace += emptySpace;

        // A left child or no child exists, which are handled almost the same way
        } else {

            // Only a left child exists
            if(left != null) {
                replacement = left;
                replacement.parent = parent;

            // No children exist
            } else replacement = null;

            // Parent is null so empty space moves to the trailing nulls iff it is significant
            if(parent == null) index = emptySpace == 0 ? -1 : host.size();

            // This is a left child so empty space goes to the parent
            else if(parent.left == this) {
                isLeftChild = true;
                parent.emptySpace += emptySpace;
                parent.totalLeftSize -= emptySpace;

            // Find the index of the empty space to insert it later iff it is significant
            } else if(emptySpace != 0) index = getIndex() - emptySpace;
        }

        // This wasn't the root of the tree
        if(parent != null) {
            parent.replace(this, replacement);
            parent.ensureAVL();

        // This was the root so replace the reference in the host
        } else {
            host.setRootNode(replacement);
        }

        // Empty space needs to be reinserted elsewhere
        if(index != -1) {
            if(parent != null) parent.prepareForReinsert(isLeftChild, emptySpace);
            host.addNulls(index, emptySpace);
        }
        return clear();
    }

    /**
     * Unlinks this node in the special case where this node has both
     * a left and right child.
     */
    private Object unlinkFromTwoChildren() {
        // Get the replacement from the right subtree
        SparseListNode replacement = right.pruneSmallestChild();
        SparseListNode repParent = replacement.parent;
        replacement.emptySpace += emptySpace;
        replacement.height = height;

        // left subtree is unaffected so move it and cache sizes
        replacement.left = left;
        replacement.left.parent = replacement;
        replacement.totalLeftSize = totalLeftSize;

        // adjust replacement's parent link to this.parent
        replacement.parent = parent;

        // Notify the host tree that the root has changed
        if(parent == null) host.setRootNode(replacement);

        // Replace this with the new child in the parent
        else parent.replace(this, replacement);

        // The smallest node is the right child of this
        if(repParent == this) replacement.ensureAVL();

        //  The smallest node is a left child in the right subtree
        else {
            // linking on the right subtree needs updating
            repParent.left = replacement.right;
            if(repParent.left != null) repParent.left.parent = repParent;
            repParent.totalLeftSize = replacement.totalRightSize;
            replacement.right = right;
            replacement.right.parent = replacement;
            replacement.totalRightSize = replacement.right.size();
            repParent.ensureAVL();
        }
        return clear();
    }

    /**
     * Prunes and returns the smallest child of the subtree rooted at this.
     * Tree references are maintained out of necessity of the calling method,
     * but sizes in the subtree are corrected accordingly.
     */
    private SparseListNode pruneSmallestChild() {
        // Recurse to the left
        if(left != null) {
            SparseListNode prunedNode = left.pruneSmallestChild();
            totalLeftSize -= prunedNode.emptySpace + 1;
            return prunedNode;

        // return this node
        } else return this;
    }

    /**
     * Prepares this tree to have length nulls reinserted.  This method
     * recurses up the tree altering sizes so that the tree is in a
     * consistent state for addNulls() to be called on the host tree.
     */
    private void prepareForReinsert(boolean leftChild, int length) {
        // left subtree is smaller
        if(leftChild) totalLeftSize -= length;

        // right subtree is smaller
        else totalRightSize -= length;

        // recurse up the tree to the root
        if(parent != null) parent.prepareForReinsert(parent.left == this, length);

        // Notify the tree size has changed
        else host.treeSizeChanged();
    }

    /**
     * Clears this node and returns the value it had.
     */
    private Object clear() {
        // clear the children
        left = null;
        totalLeftSize = 0;
        right = null;
        totalRightSize = 0;

        // clear this node and return value
        host = null;
        parent = null;
        emptySpace = 0;
        height = -1;
        Object thisValue = value;
        value = null;
        return thisValue;
    }

    /**
     * Ensures that the tree satisfies the AVL property.  It is sufficient to
     * recurse up the tree only as long as height recalculations are needed.
     * As such, this method is intended to be called only on a node whose height
     * may be out of sync due to an insertion or deletion.  For example, calling
     * this method on a leaf node will not guarantee that this tree satisfies the
     * AVL property as it will not recurse.
     */
    private void ensureAVL() {
        int oldHeight = height;
        recalculateHeight();
        avlRotate();

        // If adjustments were made, recurse up the tree
        if(height != oldHeight && parent != null) parent.ensureAVL();
    }

    /**
     * Replaces a given child with the replacement node
     */
    private void replace(SparseListNode child, SparseListNode replacement) {
        // replacing the left child
        if(child == left) left = replacement;

        // Replacing the right child
        else right = replacement;
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
    private void avlRotate() {
        // look up the left and right heights
        int leftHeight = (left != null ? left.height : 0);
        int rightHeight = (right != null ? right.height : 0);

        // rotations will be on the left
        if(leftHeight - rightHeight >= 2) {
            // determine if a double rotation is necessary
            int leftLeftHeight = (left.left != null ? left.left.height : 0);
            int leftRightHeight = (left.right != null ? left.right.height : 0);

            // Perform first half of double rotation if necessary
            if(leftRightHeight > leftLeftHeight) left.rotateRight();

            // Do the rotation for this node
            rotateLeft();

        // rotations will be on the right
        } else if(rightHeight - leftHeight >= 2) {
            // determine if a double rotation is necessary
            int rightLeftHeight = (right.left != null ? right.left.height : 0);
            int rightRightHeight = (right.right != null ? right.right.height : 0);

            // Perform first half of double rotation if necessary
            if(rightLeftHeight > rightRightHeight) right.rotateLeft();

            // Do the rotation for this node
            rotateRight();
        }
    }

    /**
     * AVL-Rotates this subtree with its left child.
     */
    private void rotateLeft() {
        // The replacement node is on the left
        SparseListNode replacement = left;

        // take the right child of the replacement as my left child
        left = replacement.right;
        totalLeftSize = replacement.totalRightSize;
        if(replacement.right != null) replacement.right.parent = this;

        // set the right child of the replacement to this
        replacement.right = this;
        replacement.totalRightSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replace(this, replacement);

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
     */
    private void rotateRight() {
        // The replacement node is on the right
        SparseListNode replacement = right;

        // take the left child of the replacement as my right child
        right = replacement.left;
        totalRightSize = replacement.totalLeftSize;
        if(replacement.left != null) replacement.left.parent = this;

        // set the left child of the replacement to this
        replacement.left = this;
        replacement.totalLeftSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replace(this, replacement);

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
     * For debugging purposes.
     */
    @Override
    public String toString() {
        return "[ " + left + " <"+emptySpace+"> " + value +" <"+height+"> "
            + right + " ]";
    }

    /**
     * Corrects all the cached sizes up the tree by the given offsets starting
     * from this so an Iterator can perform a fast remove.
     */
    private void correctSizes(int sizeChange) {
        if(parent != null)  {
            // left subtree has changed in size
            if(parent.left == this) totalLeftSize += sizeChange;

            // right subtree has changed in size
            else totalRightSize += sizeChange;

            // recurse up the tree to the root
            parent.correctSizes(sizeChange);

        // Notify the host tree that the size has changed
        } else host.treeSizeChanged();
    }

    /**
     * A specialized Iterator that will significantly outperform the default
     * one provided by AbstractList when acting on this ADT.
     */
    final static class SparseListIterator implements Iterator {

        /** the current SparseListNode being inspected */
        private SparseListNode currentNode = null;

        /** the number of times the current node has been requested */
        private int timesRequested = -1;

        /** a reference to the SparseList for removal of trailing nulls */
        private SparseList sparseList = null;

        /** the size of the actual tree within the SparseList*/
        private int treeSize = 0;

        /** the size of the list */
        private int size = 0;

        /** the current index being inspected */
        private int index = -1;

        /**
         * Creates a new Iterator that is optimized for SparseLists.
         */
        SparseListIterator(SparseList sparseList, SparseListNode root) {
            // move the Iterator to the start position.
            if(root != null) {
                this.treeSize = root.size();
                currentNode = root;
                while(currentNode.left != null) {
                    currentNode = currentNode.left;
                }
            }
            this.sparseList = sparseList;
            this.size = sparseList.size();
        }

        /**
         * Returns whether or not there are more values in the SparseList to
         * iterate over.
         */
        public boolean hasNext() {
            if(index >= treeSize - 1 && index == size - 1) {
                return false;
            }
            return true;
        }

        /**
         * Gets the next value in this SparseList.
         */
        public Object next() {
            // iterate on this node
            timesRequested++;
            index++;

            // handle the empty tree case
            if(currentNode == null) {
                // beyond the tree in the trailing nulls
                if(index < size) {
                    return null;

                // at the end of the list
                } else {
                    throw new NoSuchElementException();
                }

            // at the edge of the current node
            } else if(timesRequested > currentNode.emptySpace) {
                // move to the next node
                if(index < treeSize) {
                    findNextNode();
                    timesRequested = 0;

                // act on the trailing nulls
                } else {
                    // beyond the tree in the trailing nulls
                    if(index < size) {
                        return null;

                    // at the end of the list
                    } else {
                        throw new NoSuchElementException();
                    }
                }
            }

            // next() was a null value
            if(timesRequested < currentNode.emptySpace) {
                return null;

            // next() was the value of this node
            } else if(timesRequested == currentNode.emptySpace) {
                return currentNode.value;

            // the iterator is out of state
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Removes the current value at the Iterator from the SparseList.
         *
         * @throws UnsupportedOperationException This feature is not yet implemented.
         *
         */
        public void remove() {
            // handle the uninitialized iterator case
            if(timesRequested == -1) {
                throw new IllegalStateException("Cannot remove() without a prior call to next()");

            // remove from the trailing nulls
            } else if(currentNode == null || index >= treeSize) {
                sparseList.remove(index);

            // remove a null
            } else if(timesRequested < currentNode.emptySpace) {
                currentNode.correctSizes(-1);
                currentNode.emptySpace--;

            // remove a value
            } else if(timesRequested == currentNode.emptySpace) {
                currentNode.correctSizes(-1);
                SparseListNode nodeToRemove = currentNode;
                findNextNode();
                timesRequested = -1;
                nodeToRemove.unlink();

            // the iterator is out of state
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Finds the next node in the tree.
         */
        private void findNextNode() {
            //  go into the right subtree for the next node
            if(currentNode.right != null) {
                currentNode = currentNode.right;
                while(currentNode.left != null) {
                    currentNode = currentNode.left;
                }

            // go to the parent for the next node
            } else if(currentNode.parent.left == currentNode) {
                currentNode = currentNode.parent;

            // get out of the right subtree
            } else if(currentNode.parent.right == currentNode) {
                // move to the top of the current subtree
                while(currentNode.parent.right == currentNode) {
                    currentNode = currentNode.parent;
                }
                // Move up one more node to leave the subtree
                currentNode = currentNode.parent;

            // the iterator is out of state
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Finds the previous node in the tree.
         */
        private void findPreviousNode() {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        @Override
        public String toString() {
            return "Accessing " + currentNode + " for the " + timesRequested + " time.";
        }
    }
}