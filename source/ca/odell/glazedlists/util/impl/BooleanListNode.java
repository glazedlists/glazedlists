/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

/**
 * A BooleanListNode models a node in an BooleanList.  This class
 * does the bulk of the heavy lifting for BooleanList.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 *
 */
final class BooleanListNode {

    /** the parent node */
    private BooleanListNode parent;

    /** the tree that this node is a member of */
    private BooleanList host;

    /** the left and right child nodes */
    private BooleanListNode left = null;
    private BooleanListNode right = null;

    /** the size of the left subtree and right subtrees including empty space */
    private int totalRightSize = 0;
    private int totalLeftSize = 0;

    /** the size of the left subtree and right subtrees excluding empty space */
    private int treeRightSize = 0;
    private int treeLeftSize = 0;

    /** the amount of empty space that preceeds this node */
    private int emptySpace = 0;

    /** the number of values represented by this node */
    private int rootSize = 1;

    /** the height of this subtree */
    private int height = 1;

    /**
     * Creates a new BooleanListNode with the specified parent node and host tree.
     */
    BooleanListNode(BooleanList host, BooleanListNode parent) {
        this.host = host;
        this.parent = parent;
    }

    /**
     * This is a convienience constructor for creating a new BooleanListNode
     * with a given number of values and amount of preceeding empty space.
     */
    BooleanListNode(BooleanList host, BooleanListNode parent, int values, int emptySpace) {
        this(host, parent);
        this.emptySpace = emptySpace;
        this.rootSize = values;
    }

    /**
     * Returns the size of the subtree rooted at this node
     */
    int size() {
        return totalLeftSize + emptySpace + rootSize + totalRightSize;
    }

    /**
     * Returns the compressed size of the subtree rooted at this node
     */
    int treeSize() {
        return treeLeftSize + rootSize + treeRightSize;
    }

    /**
     * Returns the amount of empty space preceeding this node
     */
    int getEmptySpace() {
        return emptySpace;
    }

    /**
     * Inserts multiple values into the host tree
     */
    void insertValues(int index, int length) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left adjusting sizes as you go
        if(localizedIndex < 0) {
            totalLeftSize += length;
            treeLeftSize += length;
            left.insertValues(index, length);

        // Recurse to the Right adjusting sizes as you go
        } else if(localizedIndex > emptySpace + rootSize) {
            totalRightSize += length;
            treeRightSize += length;
            right.insertValues(localizedIndex - emptySpace - rootSize, length);

        // The new values should be compressed into this node
        } else if(localizedIndex == emptySpace + rootSize) {
            rootSize += length;

        // Insert in the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            emptySpace -= localizedIndex;
            totalLeftSize += localizedIndex + length;
            treeLeftSize += length;
            if(left == null) {
                left = new BooleanListNode(host, this, length, localizedIndex);
                ensureAVL();
            } else {
                left.insertValuesAtEnd(length, localizedIndex);
            }

        // Insert within this node
        } else {
            rootSize += length;
        }
    }

    /**
     * Inserts a value at the end of the tree rooted at this.
     */
    void insertValuesAtEnd(int values, int leadingNulls) {
        // Adjust sizes during recursion
        totalRightSize += leadingNulls + values;
        treeRightSize += values;

        // Recurse to the right
        if(right != null) right.insertValuesAtEnd(values, leadingNulls);

        // Insert on the right
        else {
            if(leadingNulls == 0) {
               rootSize += values;
               totalRightSize -= values;
               treeRightSize -= values;
            } else {
                right = new BooleanListNode(host, this, values, leadingNulls);
                ensureAVL();
            }
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
        } else if(localizedIndex > emptySpace + rootSize - 1) {
            totalRightSize += length;
            right.insertEmptySpace(localizedIndex - emptySpace - rootSize, length);

        // Insert before this node
        } else if(localizedIndex <= emptySpace) {
            emptySpace += length;

        // Insert within this node
        } else {
            localizedIndex -= emptySpace;
            int movingRoot = rootSize - localizedIndex;
            rootSize  = localizedIndex;
            treeRightSize += movingRoot;
            totalRightSize += length + movingRoot;

            if(right == null) {
                right = new BooleanListNode(host, this, movingRoot, length);
                ensureAVL();
            } else {
                BooleanListNode node = new BooleanListNode(host, null, movingRoot, length);
                right.moveToSmallest(node);
            }
        }
    }

    /**
     * Moves a given node to be the smallest node in the subtree rooted at
     * this.
     */
    private void moveToSmallest(BooleanListNode movingNode) {
        // Adjust sizes during recursion
        totalLeftSize += movingNode.emptySpace + movingNode.rootSize;
        treeLeftSize += movingNode.rootSize;

        // Recurse to the left
        if(left != null) {
            left.moveToSmallest(movingNode);

        // Add the node as a left child of this
        } else {
            // This node will be compressed now
            if(emptySpace == 0) {
                rootSize += movingNode.rootSize;
                treeLeftSize -= movingNode.rootSize;
                emptySpace += movingNode.emptySpace;
                totalLeftSize -= movingNode.emptySpace + movingNode.rootSize;
                movingNode.clear();

            // Add the moving node on the left
            } else {
                movingNode.parent = this;
                left = movingNode;
                ensureAVL();
            }
        }
    }

    /**
     * Gets the index of the first value in this node.  This is NOT the index of
     * the first null indexed by this node.
     */
    public int getIndex() {
        if(parent != null) return parent.getIndex(this) + totalLeftSize + emptySpace;
        return totalLeftSize + emptySpace;
    }
    private int getIndex(BooleanListNode child) {
        // the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;

        // the child is on the right, return the index recursively
        } else {
            if(parent != null) return parent.getIndex(this) + totalLeftSize + emptySpace + rootSize;
            return totalLeftSize + emptySpace + rootSize;
        }
    }

    /**
     * Gets the compressed index from the given index of returns -1 if that
     * index is compressed out.
     */
    public int getCompressedIndex(int index) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getCompressedIndex(index);

        // Recurse to the Right
        else if(localizedIndex > emptySpace + rootSize - 1) {
            return right.getCompressedIndex(localizedIndex - emptySpace - rootSize) + treeLeftSize + rootSize;

        // Get the compressed index of a real node since this is in the empty space
        } else if(localizedIndex < emptySpace) {
            return -1;

        // Get the compressed index at this node
        } else return treeLeftSize + localizedIndex - emptySpace;
    }

    /**
     * Gets the compressed index of the specified index into the tree.  This
     * is the index of the node that the specified index will be stored in.
     *
     * @param lead true for compressed-out nodes to return the index of the
     *      last value in the real node on the left.  False for compressed
     *      nodes to return the index of the first value in the real node
     *      on the right
     */
    public int getCompressedIndex(int index, boolean lead) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getCompressedIndex(index, lead);

        // Recurse to the Right
        else if(localizedIndex > emptySpace + rootSize - 1) {
            return right.getCompressedIndex(localizedIndex - emptySpace - rootSize, lead) + treeLeftSize + rootSize;

        // Get the compressed index of a real node since this is in the empty space
        } else if(localizedIndex < emptySpace) {
            if(lead) return treeLeftSize - 1;
            return treeLeftSize;

        // Get the compressed index at this node
        } else return treeLeftSize + localizedIndex - emptySpace;
    }

    /**
     * Gets the actual index from a given compressed index.
     */
    public int getIndexByCompressedIndex(int compressedIndex) {
        int localizedIndex = compressedIndex - treeLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getIndexByCompressedIndex(compressedIndex);

        // Recurse to the Right
        else if(localizedIndex >= rootSize) {
            return right.getIndexByCompressedIndex(localizedIndex - rootSize)
                + totalLeftSize + emptySpace + rootSize;

        // Get the index of the first value in this node
        } else return totalLeftSize + emptySpace + localizedIndex;
    }

    /**
     * Gets the node with the given index, or null if that index is empty.
     */
    BooleanListNode getNode(int index) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getNode(index);

        // Recurse to the Right
        else if(localizedIndex > emptySpace + rootSize - 1) return right.getNode(localizedIndex - emptySpace - rootSize);

        // Get a null from the middle of the empty space
        else if(localizedIndex < emptySpace) return null;

        // Get this node
        else return this;
    }

    /**
     * Gets the node with the given compressed index.
     */
    BooleanListNode getNodeByCompressedIndex(int index) {
        int localizedIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) return left.getNodeByCompressedIndex(index);

        // Recurse to the Right
        else if(localizedIndex >= rootSize) return right.getNodeByCompressedIndex(localizedIndex - rootSize);

        // Get this node
        else return this;
    }

    /**
     * Gets the value of this node.
     */
    public Object getValue() {
        return Boolean.TRUE;
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
        } else if(localizedIndex > emptySpace + rootSize) {
            return right.set(localizedIndex - emptySpace - rootSize, value);

        // Edge case where leading null moves to this
        } else if(localizedIndex == emptySpace + rootSize) {
            // Add the new value to this root
            if(value != null) {
                rootSize++;
                totalRightSize--;
                if(parent != null) parent.correctSizes(parent.left == this, 1, 0);
                right.setFirstNullToTrue(localizedIndex - emptySpace - rootSize + 1);
            }
            return null;

        // Set a value in the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            if(value == null) return null;
            emptySpace--;
            if(parent != null) parent.correctSizes(parent.left == this, 1, 0);
            insertValues(index, 1);
            compressNode();
            return null;

        // Set a value at the leading edge of this node
        } else if(localizedIndex == emptySpace) {
            if(value == null) {
                emptySpace++;
                rootSize--;
                if(rootSize == 0) {
                    rootSize = 1;
                    unlink();
                } else if(parent != null) parent.correctSizes(parent.left == this, -1, 0);
            }
            return Boolean.TRUE;

        // Set a value at the trailing edge of this node
        } else if(localizedIndex == emptySpace + rootSize - 1) {
            if(value == null) {
                rootSize--;
                if(right != null) {
                    totalRightSize++;
                    right.insertEmptySpace(localizedIndex - emptySpace - rootSize, 1);
                    if(parent != null) {
                        parent.correctSizes(parent.left == this, -1, 0);
                    }
                    return Boolean.TRUE;
                } else if(parent != null && parent.left == this) {
                    parent.emptySpace++;
                    parent.totalLeftSize--;
                    parent.correctSizes(true, -1, 0);
                    return Boolean.TRUE;
                }

                if(parent != null) {
                    parent.correctSizes(parent.left == this, -1, -1);
                } else {
                    host.treeSizeChanged();
                }
                int affectedIndex = getIndex() + localizedIndex - emptySpace;
                host.addNulls(affectedIndex, 1);
            }
            return Boolean.TRUE;

        // Set the value in this node
        } else {
            if(value == null) {
                rootSize--;
                if(parent != null ) parent.correctSizes(parent.left == this, -1, 0);
                insertEmptySpace(index, 1);
            }
            return Boolean.TRUE;
        }
    }

    /**
     * A helper method for the edge condition where the first null on a node
     * is set to a value.  This value is moved to the node that it is compressed
     * into before this method is called.  This method may result in further
     * compression.
     */
    private void setFirstNullToTrue(int index) {
        int localizedIndex = index - totalLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) {
            totalLeftSize--;
            left.setFirstNullToTrue(index);

        // Recurse to the Right
        } else if(localizedIndex > emptySpace + rootSize - 1) {
            totalRightSize--;
            right.setFirstNullToTrue(localizedIndex - emptySpace - rootSize);

        // Affect this node
        } else {
            emptySpace--;
            compressNode();
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
        } else if(localizedIndex > emptySpace + rootSize - 1) {
            totalRightSize--;
            return right.remove(localizedIndex - emptySpace - rootSize);

        // Remove from the middle of the empty space
        } else if(localizedIndex < emptySpace) {
            emptySpace--;
            compressNode();
            return null;

        // Remove from the value in this node
        } else {
            rootSize--;
            if(rootSize == 0) {
                rootSize = 1;
                unlink();
            } else if(parent != null) parent.correctSizes(parent.left == this, -1, 0);
            return Boolean.TRUE;
        }
    }

    /**
     * Unlinks this node from the tree and clears it.
     */
    private void unlink() {

        // Two children exist
        if(right != null && left != null) {
            if(parent != null && rootSize != 0) parent.correctSizes(parent.left == this, -rootSize, 0);
            unlinkFromTwoChildren();

        // Only a right child exists
        } else if(right != null) {
            unlinkWithRightChild();

        // A left child or no child exists, which are handled almost the same way
        } else {
            BooleanListNode replacement = null;

            // Only a left child exists
            if(left != null) {
                replacement = left;
                replacement.parent = parent;

            // No children exist
            } else replacement = null;

            // Parent is null so significant empty space moves to the trailing nulls
            if(parent == null) {
                host.setRootNode(replacement);
                if(emptySpace != 0) host.addNulls(host.size() + 1, emptySpace);

            // This is a left child so empty space goes to the parent
            } else if(parent.left == this) {
                parent.emptySpace += emptySpace;
                parent.totalLeftSize -= emptySpace;
                parent.left = replacement;
                parent.ensureAVL();
                if(rootSize != 0) parent.correctSizes(true, -rootSize, 0);
                clear();

            // This is a right child so significant empty space must be reinserted
            } else {
                parent.right = replacement;
                parent.ensureAVL();
                if(emptySpace != 0) {
                    parent.correctSizes(false, -1, -emptySpace);
                    host.addNulls(getIndex() - emptySpace, emptySpace);
                } else if(rootSize != 0) {
                    parent.correctSizes(false, -rootSize, 0);
                }
                clear();
            }
        }
    }

    /**
     * Unlinks this node in the special case where this node has both
     * a left and right child.
     */
    private void unlinkFromTwoChildren() {
        // Get the replacement from the right subtree
        BooleanListNode replacement = right.pruneSmallestChild();
        BooleanListNode repParent = replacement.parent;
        replacement.emptySpace += emptySpace;
        replacement.height = height;

        // left subtree is unaffected so move it and cache sizes
        replacement.left = left;
        replacement.left.parent = replacement;
        replacement.totalLeftSize = totalLeftSize;
        replacement.treeLeftSize = treeLeftSize;

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
            replacement.treeRightSize = replacement.right.treeSize();
            repParent.ensureAVL();
        }
        clear();
    }

    /**
     * Unlinks a node that has only a right child
     */
    private void unlinkWithRightChild() {
        emptySpace += right.emptySpace;
        int oldSize = rootSize;
        rootSize = right.rootSize;
        right.clear();
        right = null;
        totalRightSize = 0;
        treeRightSize = 0;
        height = 1;
        if(parent != null) {
            if(oldSize != 0) parent.correctSizes(parent.left == this, -oldSize, 0);
            parent.ensureAVL();
        }
    }

    /**
     * Prunes and returns the smallest child of the subtree rooted at this.
     * Tree references are maintained out of necessity of the calling method,
     * but sizes in the subtree are corrected accordingly.
     */
    private BooleanListNode pruneSmallestChild() {
        // Recurse to the left
        if(left != null) {
            BooleanListNode prunedNode = left.pruneSmallestChild();
            totalLeftSize -= prunedNode.emptySpace + prunedNode.rootSize;
            treeLeftSize -= prunedNode.rootSize;
            return prunedNode;

        // return this node
        } else return this;
    }

    /**
     * Corrects all of the cached sizes up the tree by the given offsets.
     */
    private void correctSizes(boolean leftChild, int valueOffset, int nullOffset) {
        // left subtree is smaller
        if(leftChild) {
            totalLeftSize += nullOffset;
            treeLeftSize += valueOffset;

        // right subtree is smaller
        } else {
            totalRightSize += nullOffset;
            treeRightSize += valueOffset;
        }

        // recurse up the tree to the root
        if(parent != null) parent.correctSizes(parent.left == this, valueOffset, nullOffset);

        // Notify the tree size has changed
        else host.treeSizeChanged();
    }

    /**
     * Clears this node and returns the value it had.
     */
    private void clear() {
        // clear the children
        left = null;
        totalLeftSize = 0;
        treeLeftSize = 0;
        right = null;
        totalRightSize = 0;
        treeRightSize = 0;

        // clear this node
        host = null;
        parent = null;
        emptySpace = 0;
        rootSize = 0;
        height = -1;
    }

    /**
     * Replaces a given child with the replacement node
     */
    private void replace(BooleanListNode child, BooleanListNode replacement) {
        // replacing the left child
        if(child == left) left = replacement;

        // Replacing the right child
        else right = replacement;
    }

    /**
     * Attempts to compress the current node out of the tree if possible
     */
    private void compressNode() {
        // Fast fail if this node cannot be compressed
        if(emptySpace != 0) return;

        // This is the root
        if(parent == null) {
            // Compress to the left
            if(left != null) {
                // special case that's really fast
                if(right == null) {
                    left.rootSize += rootSize;
                    left.parent = null;
                    host.setRootNode(left);
                    clear();
                } else {
                    left.compressRight(rootSize);
                    totalLeftSize += rootSize;
                    treeLeftSize += rootSize;
                    rootSize = 0;
                    unlink();
                }

            // The node is as compressed as possible
            } else {
                // Do Nothing
            }

        // This is a left child
        } else if(parent.left == this) {
            // Compress to the left
            if(left != null) {
                left.compressRight(rootSize);
                totalLeftSize += rootSize;
                treeLeftSize += rootSize;
                rootSize = 0;
                unlink();

            // Painful readdition case
            } else {
                int index = getIndex();
                // This is the first value, can't compress it
                if(index == 0) return;

                // move the right child onto the parent
                parent.left = right;
                if(right != null) parent.left.parent = parent;

                // fix tree state and readd these values
                parent.correctSizes(true, -rootSize, -rootSize);
                parent.ensureAVL();
                host.addValues(index - 1, rootSize);
                clear();
            }

        // This is a right child
        } else {
            // Compress to the parent
            if(left == null) {
                parent.treeRightSize -= rootSize;
                parent.totalRightSize -= rootSize;
                parent.rootSize += rootSize;
                rootSize = 0;
                unlink();

            // Compress to the left
            } else {
                left.compressRight(rootSize);
                totalLeftSize += rootSize;
                treeLeftSize += rootSize;
                rootSize = 0;
                unlink();
            }
        }

    }
    /**
     * Compresses the given values into the largest node in this subtree.
     */
    private void compressRight(int values) {
        if(right != null) {
            totalRightSize += values;
            treeRightSize += values;
            right.compressRight(values);
        } else {
            rootSize += values;
        }
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
        BooleanListNode replacement = left;

        // take the right child of the replacement as my left child
        left = replacement.right;
        totalLeftSize = replacement.totalRightSize;
        treeLeftSize = replacement.treeRightSize;
        if(replacement.right != null) replacement.right.parent = this;

        // set the right child of the replacement to this
        replacement.right = this;
        replacement.totalRightSize = size();
        replacement.treeRightSize = treeSize();

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
        BooleanListNode replacement = right;

        // take the left child of the replacement as my right child
        right = replacement.left;
        totalRightSize = replacement.totalLeftSize;
        treeRightSize = replacement.treeLeftSize;
        if(replacement.left != null) replacement.left.parent = this;

        // set the left child of the replacement to this
        replacement.left = this;
        replacement.totalLeftSize = size();
        replacement.treeLeftSize = treeSize();

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
}