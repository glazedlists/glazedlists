/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for specifying a sorting order
import java.util.Comparator;
// for iterating the nodes
import java.util.Iterator;
import java.util.Collections;
import java.util.NoSuchElementException;

/**
 * This node is a helper class that does all the real work for
 * SparseList.
 * 
 * It is a tree node that can be accessed either by its real index
 * or by a fake index! The fake index includes is the number of
 * virtual nodes between a node and its parent node. 
 *
 * For example, suppose we have a list:
 *    3  0  6  4  7  1  8  9  5  2
 *
 * and of that list, the following values are selected:
 *    3 (0) 6  4 (7) 1 (8) 9 (5)(2)
 *
 * and that list is layed out in a tree:
 *
 *                (7)
 *               /   \
 *            (0)     (5)
 *                   /   \
 *                (8)     (2)
 *
 * Then in this case, there is a virtual node "3" before the node (0)
 * because "3" is not included in the tree. Therefore the tree index
 * of (0) is 0 and the virtual index of (0) is 1. In effect, the tree
 * index is the index in the tree of selected values, wheras the virtual
 * index is the index in the original list.
 *
 * Another example: There are virtual nodes "6" and "4" between nodes (0)
 * and (7). The virtual index of (7) is 4 because its left subtrees virtual
 * size is 2 plus the virtual nodes between it and its left subtree of 2. 
 *
 * Each node contains a virtual left subtree, this is a count of the nodes
 * with a value less than the node but still greater than all of the nodes
 * left-side ancestors. The virtual index of a node is this virutal size
 * plus the left size plus the size of left-side ancestors.
 * 
 * This tree-node uses AVL-Trees to ensure that access is always
 * logarithmic in terms of the size of the tree. AVL Trees use
 * rotations (single and double) when the height of a pair of
 * subtrees do not match in order to guarantee a bound on the
 * difference in their height. This bound can be shown to provide
 * an overall bound on the access time on the tree. 
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class SparseListNode {
    
    /** the parent node, used to delete from leaf up */
    private SparseListNode parent;
    
    /** the sublist that this node is a member of */
    private SparseList host;
    
    /** the left and right child nodes */
    private SparseListNode left = null;
    private SparseListNode right = null;
    
    /** the size of the left and right subtrees */
    private int treeLeftSize = 0;
    private int treeRightSize = 0;
    private int treeRootSize = 0;
    
    /** the size of the virtual left subtree and right subtrees */
    private int totalRightSize = 0;
    private int totalLeftSize = 0;
    private int virtualRootSize = 0;
    
    /** the height of this subtree */
    private int height = 0;
    
    /** the value at this node */
    private Object value = null;
    
    /**
     * Creates a new SparseListNode with the specified parent node.
     */
    public SparseListNode(SparseList host, SparseListNode parent) {
        this.host = host;
        this.parent = parent;
    }
    
    /**
     * Gets the object with the specified index in the tree.
     */
    public SparseListNode getNodeByCompressedIndex(int index) {
        // ensure the index value is valid
        if(index >= treeLeftSize + treeRightSize + treeRootSize) {
            throw new IndexOutOfBoundsException("cannot get compressed index from a tree of size " + treeSize() + " at " + index);
        // recurse to the left
        } else if(index < treeLeftSize) {
            return left.getNodeByCompressedIndex(index);
        // return this node's root
        } else if(index < treeLeftSize + treeRootSize) {
            return this;
        // recurse on the right side
        } else {
            return right.getNodeByCompressedIndex(index - (treeLeftSize + treeRootSize));
        }
    }

    /**
     * Gets the object with the specified virtual index in the tree, or
     * null if that node is virtual.
     */
    public SparseListNode getNodeByIndex(int index) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;

        // recurse to the left
        if(index < totalLeftSize) {
            return left.getNodeByIndex(index);
        // if this node is real
        } else if(index < totalLeftSize + virtualRootSize) {
            return null;
        // return this node's root
        } else if(index < totalLeftSize + allRootSize) {
            return this;
        // recurse on the right side
        } else if(index < allSubtreeSize) {
            return right.getNodeByIndex(index - (totalLeftSize + allRootSize));
        // when the index value is invalid
        } else {
            throw new IndexOutOfBoundsException("cannot get from tree of size " + allSubtreeSize + " at " + index);
        }
    }
    
    /**
     * Gets the compressed index of the specified index into the tree. This
     * is the index of the node that the specified index will be stored in.
     *
     * @param lead true for compressed-out nodes to return the index of the
     *      not-compressed-out node on the left. False for such
     *      nodes to return the not-compressed-out node on the right.
     */
    public int getCompressedIndex(int index, boolean lead) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;

        // recurse to the left
        if(index < totalLeftSize) {
            return left.getCompressedIndex(index, lead);

        // this is a compressed-out node
        } else if(index < totalLeftSize + virtualRootSize) {
            if(lead) return treeLeftSize - 1;
            else return treeLeftSize;

        // this is a kept node
        } else if(index == totalLeftSize + virtualRootSize) {
            return treeLeftSize;
            
        // recurse on the right side
        } else if(index < allSubtreeSize) {
            return right.getCompressedIndex(index - (totalLeftSize + allRootSize), lead) + treeLeftSize + treeRootSize;

        // when the index value is invalid
        } else {
            throw new IndexOutOfBoundsException("cannot get from tree of size " + allSubtreeSize + " at " + index);
        }
    }

    /**
     * Removes the specified virtual or non-virtual node by the specified
     * virtual index.
     */
    public void remove(int index) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;

        // ensure the index value is valid
        if(index >= allSubtreeSize) {
            throw new IndexOutOfBoundsException("cannot get from tree of size " + allSubtreeSize + " at " + index);

        // remove on the left side
        } else if(index < totalLeftSize) {
            left.remove(index);

        // remove a virtual part of this node
        } else if(index < totalLeftSize + virtualRootSize) {
            virtualRootSize--;
            fireChildSizeChanged(true, -1);

        // remove the real part of this node
        } else if(index < totalLeftSize + allRootSize) {
            removeFromTree();
            
        // remove on the right side
        } else {
            right.remove(index - (totalLeftSize + allRootSize));
        }
    }
    
    /**
     * Gets the value of this node.
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Retrieves the number of nodes in this subtree.
     */
    public int treeSize() {
        return treeLeftSize + treeRootSize + treeRightSize;
    }
    
    /**
     * Gets the total size of this subtree.
     */
    public int size() {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;
        
        return allSubtreeSize;
    }

    /**
     * Gets the number of leading nulls on this node. This is the virtual
     * size of this node.
     */
    public int getNodeVirtualSize() {
        return virtualRootSize;
    }

    /**
     * Retrieves the subtree node with the largest value.
     */
    public SparseListNode getLargestChildNode() {
        if(treeRightSize > 0) return right.getLargestChildNode();
        else return this;
    }
    /**
     * Retrieves the subtree node with the smallest value.
     */
    public SparseListNode getSmallestChildNode() {
        if(treeLeftSize > 0) return left.getSmallestChildNode();
        else return this;
    }
    
    /**
     * Gets the index of the current node, based on a recurrsive
     * path up the tree.
     */
    public int getCompressedIndex() {
        return getCompressedIndex(null);
    }
    private int getCompressedIndex(SparseListNode child) {
        // if the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getCompressedIndex(this);
            return 0;

        // if there is no child, get the index of the current node
        } else if(child == null) {
            if(parent != null) return parent.getCompressedIndex(this) + treeLeftSize;
            return treeLeftSize;

        // if the child is on the right, return the index recursively
        } else if(child == right) {
            if(parent != null) return parent.getCompressedIndex(this) + treeLeftSize + treeRootSize;
            return treeLeftSize + treeRootSize;

        // if no child is found, we have a problem
        } else {
            throw new IllegalArgumentException(this + " cannot get the index of a subtree that does not exist on this node!");
        }
    }
    
    /**
     * Gets the virtual index of the current node, based on a recursive
     * path up the tree. This is the index of the value in this node and
     * not necessarily the value of the first null in this node. To get
     * that value, use <code>getIndex() - virtualRootSize</code>.
     */
    public int getIndex() {
        return getIndex(null);
    }
    private int getIndex(SparseListNode child) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;

        // if there is no child, get the index of the current node
        if(child == null) {
            if(parent != null) return parent.getIndex(this) + totalLeftSize + virtualRootSize;
            return totalLeftSize + virtualRootSize;

        // if the child is on the left, return the index recursively
        } else if(child == left) {
            if(parent != null) return parent.getIndex(this);
            return 0;

        // if the child is on the right, return the index recursively
        } else if(child == right) {
            if(parent != null) return parent.getIndex(this) + totalLeftSize + allRootSize;
            return totalLeftSize + allRootSize;

        // if no child is found, we have a problem
        } else {
            throw new IllegalArgumentException(this + " cannot get the index of a subtree that does not exist on this node!");
        }
    }

    /**
     * Inserts the specified object.
     */
    public void insert(int index, Object value) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;
        
        // if we're actually inserting space, do that
        if(value == null) {
            insertSpace(index, 1);

        // if the virtual index is less than 0, complain
        } else if(index < 0) {
            throw new IndexOutOfBoundsException("cannot insert into a tree of virtual size " + allSubtreeSize + " at " + index);

        // if we can insert on the left, insert there
        } else if(index < totalLeftSize) {
            if(left == null) left = new SparseListNode(host, this);
            left.insert(index, value);
            doRotationsForThisLevel();

        // if the index is in the middle of our virtual space
        } else if(index < (totalLeftSize + allRootSize)) {
            if(left == null) left = new SparseListNode(host, this);

            // firts calculate where the inserts go
            int leftOverSpace = index - totalLeftSize;
            int insertLocation = totalLeftSize;

            // insert on the left
            left.insert(insertLocation, value);

            // move my virtual space to the left, right before the new node
            virtualRootSize = virtualRootSize - leftOverSpace;
            fireChildSizeChanged(true, -1 * leftOverSpace);
            left.insertSpace(insertLocation, leftOverSpace);

            // rebalance post-insert
            doRotationsForThisLevel();

        // if we're inserting into the root of this node
        } else if(index == (totalLeftSize + allRootSize) && treeRootSize == 0) {
            this.value = value;
            treeRootSize = treeRootSize + 1;
            fireChildSizeChanged(false, 1);

        // if the index is not on the left side, insert on the right
        } else if(index <= allSubtreeSize) {
            if(right == null) right = new SparseListNode(host, this);
            right.insert(index - totalLeftSize - allRootSize, value);
            doRotationsForThisLevel();

        // if the virtual index is bigger than the tree, complain
        } else {
            throw new IndexOutOfBoundsException("cannot insert into a tree of virtual size " + allSubtreeSize + " at " + index);
        }
    }
    
    /**
     * Inserts space into the sparse list at the specified index with
     * the specified length.
     */
    public void insertSpace(int index, int length) {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;
        
        // if we can insert on the left, insert there
        if(length < 0) {
            throw new IndexOutOfBoundsException("cannot insert space of length " + length);

        // do nothing to insert no space
        } else if(length == 0) {
            // do nothing
            
        // if the index is on the left side, insert there
        } else if(index < totalLeftSize) {
            if(left == null) left = new SparseListNode(host, this);
            left.insertSpace(index, length);

        // if the index is in the root, insert there
        } else if(index <= (totalLeftSize + virtualRootSize)) {
            virtualRootSize = virtualRootSize + length;
            fireChildSizeChanged(true, length);

        // if the index is not on the left side, insert on the right
        } else if(index <= allSubtreeSize) {
            if(right == null) right = new SparseListNode(host, this);
            right.insertSpace(index - totalLeftSize - allRootSize, length);

        // if the virtual index is bigger than the tree, complain
        } else {
            throw new IndexOutOfBoundsException("cannot insert into a tree of virtual size " + allSubtreeSize + " at " + index);
        }
    }


    /**
     * Unlinks this node from the sorted tree. This may cause the tree to
     * rotate nodes using AVL rotations.
     *
     * The remove procedure has some special cases to accomodate for the possible
     * presense of virtual nodes. If the node to be removed has leading virtual nodes,
     * they are first removed from the tree. The node itself is then removed. Finally
     * the trailing virtual nodes are restored.
     */
    private void removeFromTree() {
        // if this node has no mass, we have a problem!
        if(treeRootSize == 0) throw new IllegalStateException("cannot delete a node of size 0");
        
        // temporarily remove the leading nulls if they exist
        int spaceToRestore = 0;
        int restoreIndex = 0;
        if(virtualRootSize > 0) {
            spaceToRestore = virtualRootSize;
            restoreIndex = getIndex() - virtualRootSize;
            virtualRootSize = 0;
            fireChildSizeChanged(true, -1 * spaceToRestore);
        }
        
        // if this is a leaf, we can delete it outright
        if(treeLeftSize == 0 && treeRightSize == 0) {
            // update the parent
            fireChildSizeChanged(false, -1);
            if(parent != null) {
                parent.replaceChildNode(this, null);
            } else {
                host.setRootNode(null);
            }
            // clear the parent
            parent = null;
        // if this node has only a left child, we can replace this with that child
        } else if(treeLeftSize > 0 && treeRightSize == 0) {
            // update the left child
            left.parent = parent;
            // update the parent
            fireChildSizeChanged(false, -1);
            if(parent != null) {
                parent.replaceChildNode(this, left);
                parent.doRotationsUpTheTree();
            } else {
                host.setRootNode(left);
            }
        // if this node has only a right child, we can replace this with that child
        } else if(treeLeftSize == 0 && treeRightSize > 0) {
            // update the right child
            right.parent = parent;
            // update the parent
            fireChildSizeChanged(false, -1);
            if(parent != null) {
                parent.replaceChildNode(this, right);
                parent.doRotationsUpTheTree();
            } else {
                host.setRootNode(right);
            }
        // if this node has two children, replace this node with the best of the biggest
        } else {
            SparseListNode middle = null;
            // if the left side is larger, use a left side node
            if(treeLeftSize > treeRightSize) {
                middle = left.getLargestChildNode();
            // otherwise use a right side node
            } else {
                middle = right.getSmallestChildNode();
            }

            // move the virtual root of the middle node first
            int middleVirtualRootSize = middle.virtualRootSize;
            middle.virtualRootSize = 0;
            middle.fireChildSizeChanged(true, -1 * middleVirtualRootSize);
            virtualRootSize = virtualRootSize + middleVirtualRootSize;
            fireChildSizeChanged(true, middleVirtualRootSize);
            
            // unlink the middle node from the tree
            middle.removeFromTree();
            if(middle.treeLeftSize > 0 || middle.treeRightSize > 0) throw new IllegalStateException("cannot have a new middle with leaves");
            // update the left child
            middle.left = left;
            middle.treeLeftSize = treeLeftSize;
            middle.totalLeftSize = totalLeftSize;
            if(left != null) left.parent = middle;
            // update the right child
            middle.right = right;
            middle.treeRightSize = treeRightSize;
            middle.totalRightSize = totalRightSize;
            if(right != null) right.parent = middle;
            // update the centre space
            middle.virtualRootSize = virtualRootSize;
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
        treeLeftSize = 0;
        totalLeftSize = 0;
        // clear the right child
        right = null;
        treeRightSize = 0;
        totalRightSize = 0;
        // clear the centre space
        virtualRootSize = 0;

        // finally restore the leading nulls
        if(spaceToRestore > 0) {
            host.insertNulls(restoreIndex, spaceToRestore);
        }
    }
    
    /**
     * Sends notification to the parent node that nodes have been
     * removed. If there is no such parent node then only the host
     * list is notified.
     *
     * @param difference the amount of nodes that the subtree has changed
     *      by. This is positive for adds and negative for removes.
     */
    private void fireChildSizeChanged(boolean virtual, int difference) {
        if(difference == 0) return;
        
        // fire notification at the next level up the tree
        if(parent != null) parent.childSizeChanged(this, virtual, difference);
        else host.childSizeChanged(difference);
        // fore notification of height changes
        if(!virtual) recalculateHeight();
    }
    
    /**
     * Notifies that a node has been removed from the specified subtree.
     * This simply decrements the count on that subtree.
     *
     * @param subtree the child subtree that the nodes were removed from - this
     *      must be this nodes left or right subtree.
     * @param difference the number of nodes that have been removed. This should
     *      usually only be greater than one for virtual nodes.
     * @param difference the amount of nodes that the subtree has changed
     *      by. This is positive for adds and negative for removes.
     */
    private void childSizeChanged(SparseListNode subtree, boolean virtual, int difference) {
        // a child on the left has been removed
        if(subtree == left) {
            if(!virtual) treeLeftSize = treeLeftSize + difference;
            totalLeftSize = totalLeftSize + difference;
        // a child on the right has been removed
        } else if(subtree == right) {
            if(!virtual) treeRightSize = treeRightSize + difference;
            totalRightSize = totalRightSize + difference;
        // an unknown child has been removed
        } else {
            throw new IllegalArgumentException(this + " cannot remove a subtree that does not exist on this node!");
        }
        
        // fire notification at the next level up the tree
        if(parent != null) parent.childSizeChanged(this, virtual, difference);
        else host.childSizeChanged(difference);
    }
    /**
     * Replaces the specified child with a new child.
     */
    private void replaceChildNode(SparseListNode original, SparseListNode replacement) {
        if(original == left) left = replacement;
        else if(original == right) right = replacement;
        else throw new IllegalArgumentException(this + " cannot replace a non-existant child");
        
        // the height may change as a consequence
        recalculateHeight();
    }
    
    /**
     * Recalculates the cached height of this node after a child node has been
     * removed or added.
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
     * Gets the cached value of this nodes height.
     */
    private int height() {
        return height;
    }
    
    /**
     * A primitive way to validate that nodes have a consistent state. Called
     * on a subtree this validates the root of that subtree and then all child
     * subtrees in depth-first order.
     *
     * @throws IllegalStateException if the state of the node is inconsistent.
     */
    public void validate() {
        // calculate some convenience sizes
        int allRootSize = treeRootSize + virtualRootSize;
        int allSubtreeSize = totalLeftSize + allRootSize + totalRightSize;
        
        if(left != null) left.validate();
        if(right != null) right.validate();
        
        if(value == null) {
            throw new IllegalStateException("Node value is null");
        }
        if(treeRootSize > 1 || treeRootSize < 0) {
            throw new IllegalStateException(value + " Root size " + treeRootSize);
        }
        if(virtualRootSize < 0) {
            throw new IllegalStateException(value + " Virtual root size " + virtualRootSize);
        }
        if(treeLeftSize < 0) {
            throw new IllegalStateException(value + " Tree Left size " + treeLeftSize);
        }
        if(totalLeftSize < 0) {
            throw new IllegalStateException(value + " Left size " + totalLeftSize);
        }
        if(treeRightSize < 0) {
            throw new IllegalStateException(value + " Tree Right size " + treeRightSize);
        }
        if(totalRightSize < 0) {
            throw new IllegalStateException(value + " Right size " + totalRightSize);
        }
        if((left == null && treeLeftSize != 0) || (left != null && treeLeftSize != left.treeSize())) {
            throw new IllegalStateException(value + " Cached leftSize " + treeLeftSize + " != reported left.treeSize() " + left.treeSize());
        }
        if((right == null && treeRightSize != 0) || (right != null && treeRightSize != right.treeSize())) {
            throw new IllegalStateException(value + " Cached rightSize " + treeRightSize + " != reported right.treeSize() " + right.treeSize());
        }
        if((right == null && totalRightSize != 0) || (right != null && totalRightSize != right.size())) {
            throw new IllegalStateException(value + " Cached rightSize " + totalRightSize + " != reported right.size() " + (right!=null?right.size():0));
        }
        if((left == null && totalLeftSize != 0) || (left != null && totalLeftSize != left.size())) {
            throw new IllegalStateException(value + " Cached leftSize " + totalLeftSize + " != reported left.size() " + (left!=null?left.size():0));
        }
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
        SparseListNode replacement = left;
        // take the right child of the replacement as my left child
        left = replacement.right;
        treeLeftSize = replacement.treeRightSize;
        totalLeftSize = replacement.totalRightSize;
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
        replacement.treeRightSize = treeSize();
        replacement.totalRightSize = size();
        // recalculate heights
        recalculateHeight();
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
        SparseListNode replacement = right;
        // take the left child of the replacement as my right child
        right = replacement.left;
        treeRightSize = replacement.treeLeftSize;
        totalRightSize = replacement.totalLeftSize;
        if(replacement.left != null) replacement.left.parent = this;
        // set the replacement's parent to my parent and mine to the replacement 
        if(parent != null) {
            parent.replaceChildNode(this, replacement);
        } else {
            host.setRootNode(replacement);
        }
        replacement.parent = parent;
        parent = replacement;
        // set the left child of the replacement to this
        replacement.left = this;
        replacement.treeLeftSize = treeSize();
        replacement.totalLeftSize = size();
        // recalculate heights
        recalculateHeight();
    }

    
    /**
     * Prints the tree by its contents.
     */
    public String toString() {
        String valueString = value.toString();
        for(int i = 0; i < virtualRootSize; i++) valueString = "." + valueString;

        if(left != null && right != null) {
            return "(" + left.toString() + " " + valueString + " " + right.toString() + ")";
        } else if(left != null) {
            return "(" + left.toString() + " " + valueString + " .)";
        } else if(right != null) {
            return "(. " + valueString + " " + right.toString() + ")";
        } else if(treeRootSize == 0) {
            return ".";
        } else { 
            return valueString;
        }
    }
}
