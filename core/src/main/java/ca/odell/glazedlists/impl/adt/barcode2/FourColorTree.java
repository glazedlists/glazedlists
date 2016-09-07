/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import ca.odell.glazedlists.GlazedLists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
 # some M4 Macros that make it easy to use m4 with Java










  M4 Macros














# define a function NODE_WIDTH(boolean) to get the node's size for this color




# define a function NODE_SIZE(node, colors) to no node.nodeSize()






# define a function to refresh counts




# multiple values









*/
/*[ BEGIN_M4_JAVA ]*/

/**
 * Our second generation tree class.
 *
 * <p>Currently the API for this class is fairly low-level, particularly the
 * use of <code>byte</code>s as color values. This is an implementation detail,
 * exposed to maximize performance. Wherever necessary, consider creating a
 * facade around this <code>Tree</code> class that provides methods more appropriate
 * for your particular application.
 *
 * <p>This is a prototype replacement for the <code>Barcode</code> class that adds support
 * for up to seven different colors. As well, this supports values in the node.
 * It will hopefully also replace our <code>IndexedTree</code> class. This class
 * is designed after those two classes and hopefully improves upon them in
 * a few interesting ways:
 * <li>Avoid recursion wherever possible to increase performance
 * <li>Be generic to simplify handling of black/white values. These can be
 *     handled in one case rather than one case for each.
 * <li>Make the node class a dataholder only and put most of the logic in
 *     the tree class. This allows us to share different Node classes with
 *     different memory requirements, while using the same logic class
 *
 * <p>This class came into being so we could use a tree to replace
 * <code>ListEventBlock</code>s, which has only mediocre performance, particularly
 * due to having to sort elements. As well, we might be able to keep a moved
 * value in the tree, to support moved elements in <code>ListEvent</code>s.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class FourColorTree <  T0>   {


    /** the colors in the tree, used for printing purposes only */
    private final ListToByteCoder coder;


    /** the tree's root, or <code>null</code> for an empty tree */
    private  FourColorNode <  T0>   root = null;

    /**
     * a list to add all nodes to that must be removed from
     * the tree. The nodes are removed only after the tree has been modified,
     * which allows us a chance to do rotations without losing our position
     * in the tree.
     */
    private final List< FourColorNode <  T0>  > zeroQueue = new ArrayList< FourColorNode <  T0>  >();

    /**
     * The comparator to use when performing ordering operations on the tree.
     * Sometimes this tree will not be sorted, so in such situations this
     * comparator will not be used.
     */
    private final Comparator<? super T0> comparator;

    /**
     * @param coder specifies the node colors
     * @param comparator the comparator to use when ordering values within the
     *      tree. If this tree is unsorted, use the one-argument constructor.
     */
    public FourColorTree/**/(  ListToByteCoder coder,    Comparator<? super T0> comparator) {
           if(coder == null) throw new NullPointerException("Coder cannot be null.");
        if(comparator == null) throw new NullPointerException("Comparator cannot be null.");

          this.coder = coder;
        this.comparator = comparator;
    }

    /**
     * @param coder specifies the node colors
     */
    public FourColorTree/**/(  ListToByteCoder coder   ) {
        this(  coder,    (Comparator)GlazedLists.comparableComparator());
    }


    public ListToByteCoder getCoder() {
        return coder;
    }


    public Comparator<? super T0> getComparator() {
        return comparator;
    }

    /**
     * Get the tree element at the specified index relative to the specified index
     * colors.
     *
     * <p>This method is an hotspot, so its crucial that it run as efficiently
     * as possible.
     */
    public Element<T0> get(int index  , byte indexColors   ) {
        if(root == null) throw new IndexOutOfBoundsException();

        // go deep, looking for our node of interest
         FourColorNode <  T0>   node = root;
        while(true) {
            assert(node != null);
            assert(index >= 0);

            // recurse on the left
             FourColorNode <  T0>   nodeLeft = node.left;
            int leftSize = nodeLeft != null ? nodeLeft.  size(indexColors)    : 0;
            if(index < leftSize) {
                node = nodeLeft;
                continue;
            } else {
                index -= leftSize;
            }

            // the result is in the centre
            int size =  node.nodeSize(indexColors)  ;
            if(index < size) {
                return node;
            } else {
                index -= size;
            }

            // the result is on the right
            node = node.right;
        }
    }

    /**
     * Add a tree node at the specified index relative to the specified index
     * colors. The inserted nodes' color, value and size are specified.
     *
     * <p><strong>Note that nodes with <code>null</code> values will never be
     * merged together to allow those nodes to be assigned other values later.
     *
     * @param size the size of the node to insert.
     * @param index the location into this tree to insert at
     * @param indexColors the colors that index is relative to. This should be
     *      all colors in the tree ORed together for the entire tree.
     * @param value the node value. If non-<code>null</code>, the node may be
     *      combined with other nodes of the same color and value. <code>null</code>
     *      valued nodes will never be combined with each other.
     * @return the element the specified value was inserted into. This is non-null
     *      unless the size parameter is 0, in which case the result is always
     *      <code>null</code>.
     */
    public Element<T0> add(int index,   byte indexColors, byte color,    T0 value, int size) {
        assert(index >= 0);
        assert(index <= size(  indexColors   ));
        assert(size >= 0);

        if(this.root == null) {
            if(index != 0) throw new IndexOutOfBoundsException();

            this.root = new  FourColorNode <  T0>  (  color,    size, value, null);
            assert(valid());
            return this.root;
        } else {
             FourColorNode <  T0>   inserted = insertIntoSubtree(root, index,   indexColors, color,    value, size);
            assert(valid());
            return inserted;
        }
    }

    /**
     * @param parent the subtree to insert into, must not be null.
     * @param index the color index to insert at
     * @param indexColors a bitmask of all colors that the index is defined in
     *      terms of. For example, if this is determined in terms of colors 4, 8
     *      and 32, then the value here should be 44 (32 + 8 + 4).
     * @param color a bitmask value such as 1, 2, 4, 8, 16, 32, 64 or 128.
     * @param value the object to hold in the inserted node.
     * @param size the size of the inserted node, with respect to indices.
     * @return the inserted node, or the modified node if this insert simply
     *      increased the size of an existing node.
     */
    private  FourColorNode <  T0>   insertIntoSubtree( FourColorNode <  T0>   parent, int index,   byte indexColors, byte color,    T0 value, int size) {
        while(true) {
            assert(parent != null);
            assert(index >= 0);

            // figure out the layout of this node
             FourColorNode <  T0>   parentLeft = parent.left;
            int parentLeftSize = parentLeft != null ? parentLeft.  size(indexColors)    : 0;
            int parentRightStartIndex = parentLeftSize +  parent.nodeSize(indexColors)  ;

            // the first thing we want to try is to merge this value into the
            // current node, since that's the cheapest thing to do:
            if(     color == parent.color &&     value == parent.t0 && value != null) {
                if(index >= parentLeftSize && index <= parentRightStartIndex) {
                      parent.size += size;
                    fixCountsThruRoot(parent,   color,    size);
                    return parent;
                }
            }

            // we can insert on the left
            if(index <= parentLeftSize) {
                // as a new left child
                if(parentLeft == null) {
                     FourColorNode <  T0>   inserted = new  FourColorNode <  T0>  (  color,    size, value, parent);
                    parent.left = inserted;
                    fixCountsThruRoot(parent,   color,    size);
                    fixHeightPostChange(parent, false);
                    return inserted;

                // recurse on the left
                } else {
                    parent = parentLeft;
                    continue;
                }
            }

            // we need to insert in the centre. This works by splitting in the
            // centre, and inserting the value
            if(index < parentRightStartIndex) {
                int parentRightHalfSize = parentRightStartIndex - index;
                  parent.size -= parentRightHalfSize;
                fixCountsThruRoot(parent,   parent.color,    -parentRightHalfSize);
                // insert as null first to make sure this doesn't get merged back
                Element<T0> inserted = insertIntoSubtree(parent, index,   indexColors, parent.color,    null, parentRightHalfSize);
                inserted.set(parent.t0);

                // recalculate parentRightStartIndex, since that should have
                // changed by now. this will then go on to insert on the right
                parentRightStartIndex = parentLeftSize +  parent.nodeSize(indexColors)  ;
            }

            // on the right
            right: {
                int parentSize = parent.  size(indexColors)   ;
                assert(index <= parentSize);
                 FourColorNode <  T0>   parentRight = parent.right;

                // as a right child
                if(parentRight == null) {
                     FourColorNode <  T0>   inserted = new  FourColorNode <  T0>  (  color,    size, value, parent);
                    parent.right = inserted;
                    fixCountsThruRoot(parent,   color,    size);
                    fixHeightPostChange(parent, false);
                    return inserted;

                // recurse on the right
                } else {
                    parent = parentRight;
                    index -= parentRightStartIndex;
                }
            }
        }
    }

    /**
     * Add a tree node in sorted order.
     *
     * @param size the size of the node to insert.
     * @param value the node value. If non-<code>null</code>, the node may be
     *      combined with other nodes of the same color and value. <code>null</code>
     *      valued nodes will never be combined with each other.
     * @return the element the specified value was inserted into. This is non-null
     *      unless the size parameter is 0, in which case the result is always
     *      <code>null</code>.
     */
    public Element<T0> addInSortedOrder(byte color, T0 value, int size) {
        assert(size >= 0);

        if(this.root == null) {
            this.root = new  FourColorNode <  T0>  (  color,    size, value, null);
            assert(valid());
            return this.root;
        } else {
             FourColorNode <  T0>   inserted = insertIntoSubtreeInSortedOrder(root,   color,    value, size);
            assert(valid());
            return inserted;
        }
    }

    /**
     * @param parent the subtree to insert into, must not be null.
     * @param color a bitmask value such as 1, 2, 4, 8, 16, 32, 64 or 128.
     * @param value the object to hold in the inserted node.
     * @param size the size of the inserted node, with respect to indices.
     * @return the inserted node, or the modified node if this insert simply
     *      increased the size of an existing node.
     */
    private  FourColorNode <  T0>   insertIntoSubtreeInSortedOrder( FourColorNode <  T0>   parent,   byte color,    T0 value, int size) {
        while(true) {
            assert(parent != null);

            // calculating the sort side is a little tricky since we can have
            // unsorted nodes in the tree. we just look for a neighbour (ie next)
            // that is sorted, and compare with that
            int sortSide;
            for( FourColorNode <  T0>   currentFollower = parent; true; currentFollower = next(currentFollower)) {
                // we've hit the end of the list, assume the element is on the left side
                if(currentFollower == null) {
                    sortSide = -1;
                    break;
                // we've found a comparable node, use it
                } else if(currentFollower.sorted == Element.SORTED) {
                    sortSide = comparator.compare(value, currentFollower.t0);
                    break;
                }
            }

            // the first thing we want to try is to merge this value into the
            // current node, since that's the cheapest thing to do:
            if(   sortSide == 0 &&   color == parent.color &&    value == parent.t0 && value != null) {
                  parent.size += size;
                fixCountsThruRoot(parent,   color,    size);
                return parent;
            }

            // insert on the left...
            boolean insertOnLeft = false;
            insertOnLeft = insertOnLeft || sortSide < 0;
            insertOnLeft = insertOnLeft || sortSide == 0 && parent.left == null;
            insertOnLeft = insertOnLeft || sortSide == 0 && parent.right != null && parent.left.height < parent.right.height;
            if(insertOnLeft) {
                 FourColorNode <  T0>   parentLeft = parent.left;

                // as a new left child
                if(parentLeft == null) {
                     FourColorNode <  T0>   inserted = new  FourColorNode <  T0>  (  color,    size, value, parent);
                    parent.left = inserted;
                    fixCountsThruRoot(parent,   color,    size);
                    fixHeightPostChange(parent, false);
                    return inserted;

                // recurse on the left
                } else {
                    parent = parentLeft;
                    continue;
                }

                // ...or on the right
            } else {
                 FourColorNode <  T0>   parentRight = parent.right;

                // as a right child
                if(parentRight == null) {
                     FourColorNode <  T0>   inserted = new  FourColorNode <  T0>  (  color,    size, value, parent);
                    parent.right = inserted;
                    fixCountsThruRoot(parent,   color,    size);
                    fixHeightPostChange(parent, false);
                    return inserted;

                // recurse on the right
                } else {
                    parent = parentRight;
                }
            }
        }
    }

    /**
     * Adjust counts for all nodes (including the specified node) up the tree
     * to the root. The counts of the specified color are adjusted by delta
     * (which may be positive or negative).
     */
    private final void fixCountsThruRoot( FourColorNode <  T0>   node,   byte color,    int delta) {

        if(color == 1) {
            for( ; node != null; node = node.parent) node.count1 += delta;
        }
        if(color == 2) {
            for( ; node != null; node = node.parent) node.count2 += delta;
        }
        if(color == 4) {
            for( ; node != null; node = node.parent) node.count4 += delta;
        }
        if(color == 8) {
            for( ; node != null; node = node.parent) node.count8 += delta;
        }



    }


    /**
     * Change the color of the specified element.
     */
    public final void setColor(Element<T0> element, byte color) {
        FourColorNode node = (FourColorNode)element;
        byte oldColor  = node.getColor();
        if(oldColor == color) return;

        fixCountsThruRoot(node, oldColor, -node.size);
        node.color = color;
        fixCountsThruRoot(node, color, node.size);
    }


    /**
     * Fix the height of the specified ancestor after inserting a child node.
     * This method short circuits when it finds the first node where the size
     * has not changed.
     *
     * @param node the root of a changed subtree. This shouldn't be called
     *      on inserted nodes, but rather their parent nodes, since only
     *      the parent nodes sizes will be changing.
     * @param allTheWayToRoot <code>true</code> to walk up the tree all the way
     *      to the tree's root, or <code>false</code> to walk up until the height
     *      is unchanged. We go to the root on a delete, since the rotate is on
     *      the opposite side of the tree, whereas on an insert we only delete
     *      as far as necessary.
     */
    private final void fixHeightPostChange( FourColorNode <  T0>   node, boolean allTheWayToRoot) {

        // update the height
        for(; node != null; node = node.parent) {
            byte leftHeight = node.left != null ? node.left.height : 0;
            byte rightHeight = node.right != null ? node.right.height : 0;

            // rotate left?
            if(leftHeight > rightHeight && (leftHeight - rightHeight == 2)) {
                // do we need to rotate the left child first?
                int leftLeftHeight = node.left.left != null ? node.left.left.height : 0;
                int leftRightHeight = node.left.right != null ? node.left.right.height : 0;
                if(leftRightHeight > leftLeftHeight) {
                    rotateRight(node.left);
                }

                // finally rotate right
                node = rotateLeft(node);
            // rotate right?
            } else if(rightHeight > leftHeight && (rightHeight - leftHeight == 2)) {
                // do we need to rotate the right child first?
                int rightLeftHeight = node.right.left != null ? node.right.left.height : 0;
                int rightRightHeight = node.right.right != null ? node.right.right.height : 0;
                if(rightLeftHeight > rightRightHeight) {
                    rotateLeft(node.right);
                }

                // finally rotate left
                node = rotateRight(node);
            }

            // update the node height
            leftHeight = node.left != null ? node.left.height : 0;
            rightHeight = node.right != null ? node.right.height : 0;
            byte newNodeHeight = (byte) (Math.max(leftHeight, rightHeight) + 1);
            // if the height doesn't need updating, we might just be done!
            if(!allTheWayToRoot && node.height == newNodeHeight) return;
            // otherwise change the height and keep rotating
            node.height = newNodeHeight;
        }
    }

    /**
     * Perform an AVL rotation of the tree.
     *
     *     D               B
     *    / \   ROTATE    / \
     *   B   E  LEFT AT  A   D
     *  / \     NODE D      / \
     * A   C               C   E
     *
     * @return the new root of the subtree
     */
    private final  FourColorNode <  T0>   rotateLeft( FourColorNode <  T0>   subtreeRoot) {
        assert(subtreeRoot.left != null);
        // subtreeRoot is D
        // newSubtreeRoot is B
         FourColorNode <  T0>   newSubtreeRoot = subtreeRoot.left;

        // modify the links between nodes
        // attach C as a child of to D
        subtreeRoot.left = newSubtreeRoot.right;
        if(newSubtreeRoot.right != null) newSubtreeRoot.right.parent = subtreeRoot;
        // link b as the new root node for this subtree
        newSubtreeRoot.parent = subtreeRoot.parent;
        if(newSubtreeRoot.parent != null) {
            if(newSubtreeRoot.parent.left == subtreeRoot) newSubtreeRoot.parent.left = newSubtreeRoot;
            else if(newSubtreeRoot.parent.right == subtreeRoot) newSubtreeRoot.parent.right = newSubtreeRoot;
            else throw new IllegalStateException();
        } else {
            root = newSubtreeRoot;
        }
        // attach D as a child of B
        newSubtreeRoot.right = subtreeRoot;
        subtreeRoot.parent = newSubtreeRoot;

        // update height and counts of the old subtree root
        byte subtreeRootLeftHeight = subtreeRoot.left != null ? subtreeRoot.left.height : 0;
        byte subtreeRootRightHeight = subtreeRoot.right != null ? subtreeRoot.right.height : 0;
        subtreeRoot.height = (byte)(Math.max(subtreeRootLeftHeight, subtreeRootRightHeight) + 1);
         subtreeRoot.refreshCounts();

        // update height and counts of the new subtree root
        byte newSubtreeRootLeftHeight = newSubtreeRoot.left != null ? newSubtreeRoot.left.height : 0;
        byte newSubtreeRootRightHeight = newSubtreeRoot.right != null ? newSubtreeRoot.right.height : 0;
        newSubtreeRoot.height = (byte)(Math.max(newSubtreeRootLeftHeight, newSubtreeRootRightHeight) + 1);
         newSubtreeRoot.refreshCounts();

        return newSubtreeRoot;
    }
    private final  FourColorNode <  T0>   rotateRight( FourColorNode <  T0>   subtreeRoot) {
        assert(subtreeRoot.right != null);
        // subtreeRoot is D
        // newSubtreeRoot is B
         FourColorNode <  T0>   newSubtreeRoot = subtreeRoot.right;

        // modify the links between nodes
        // attach C as a child of to D
        subtreeRoot.right = newSubtreeRoot.left;
        if(newSubtreeRoot.left != null) newSubtreeRoot.left.parent = subtreeRoot;
        // link b as the new root node for this subtree
        newSubtreeRoot.parent = subtreeRoot.parent;
        if(newSubtreeRoot.parent != null) {
            if(newSubtreeRoot.parent.left == subtreeRoot) newSubtreeRoot.parent.left = newSubtreeRoot;
            else if(newSubtreeRoot.parent.right == subtreeRoot) newSubtreeRoot.parent.right = newSubtreeRoot;
            else throw new IllegalStateException();
        } else {
            root = newSubtreeRoot;
        }
        // attach D as a child of B
        newSubtreeRoot.left = subtreeRoot;
        subtreeRoot.parent = newSubtreeRoot;

        // update height and counts of the old subtree root
        byte subtreeRootLeftHeight = subtreeRoot.left != null ? subtreeRoot.left.height : 0;
        byte subtreeRootRightHeight = subtreeRoot.right != null ? subtreeRoot.right.height : 0;
        subtreeRoot.height = (byte)(Math.max(subtreeRootLeftHeight, subtreeRootRightHeight) + 1);
         subtreeRoot.refreshCounts();

        // update height and counts of the new subtree root
        byte newSubtreeRootLeftHeight = newSubtreeRoot.left != null ? newSubtreeRoot.left.height : 0;
        byte newSubtreeRootRightHeight = newSubtreeRoot.right != null ? newSubtreeRoot.right.height : 0;
        newSubtreeRoot.height = (byte)(Math.max(newSubtreeRootLeftHeight, newSubtreeRootRightHeight) + 1);
         newSubtreeRoot.refreshCounts();

        return newSubtreeRoot;
    }

    /**
     * Remove the specified element from the tree outright.
     */
    public void remove(Element<T0> element) {
         FourColorNode <  T0>   node = ( FourColorNode <  T0>  )element;
          assert(node.size > 0);
        assert(root != null);

        // delete the node by adding to the zero queue
        fixCountsThruRoot(node,   node.color,      -node.size   );
          node.size = 0;
        zeroQueue.add(node);
        drainZeroQueue();

        assert(valid());
    }

    /**
     * Remove size values at the specified index. Only values of the type
     * specified in indexColors will be removed.
     *
     * <p>Note that if the two nodes on either side of the removed node could
     * be merged, they probably will not be merged by this implementation. This
     * is to simplify the implementation, but it means that when iterating a
     * tree, sometimes multiple nodes of the same color and value will be
     * encountered in sequence.
     */
    public void remove(int index,   byte indexColors,    int size) {
        if(size == 0) return;
        assert(index >= 0);
        assert(index + size <= size(  indexColors   ));
        assert(root != null);

        // remove values from the tree
        removeFromSubtree(root, index,   indexColors,    size);

        // clean up any nodes that got deleted
        drainZeroQueue();

        assert(valid());
    }

    /**
     * Prune all nodes scheduled for deletion.
     */
    private void drainZeroQueue() {
        for(int i = 0, size = zeroQueue.size(); i < size; i++) {
             FourColorNode <  T0>   node = zeroQueue.get(i);
              assert(node.size == 0);

            if(node.right == null) {
                replaceChild(node, node.left);
            } else if(node.left == null) {
                replaceChild(node, node.right);
            } else {
                node = replaceEmptyNodeWithChild(node);
            }
        }
        zeroQueue.clear();
    }

    /**
     * Remove at the specified index in the specified subtree. This doesn't ever
     * remove any nodes of size zero, that's up to the caller to do after by
     * removing all nodes in the zeroQueue from the tree.
     */
    private void removeFromSubtree( FourColorNode <  T0>   node, int index,   byte indexColors,    int size) {
        while(size > 0) {
            assert(node != null);
            assert(index >= 0);

            // figure out the layout of this node
             FourColorNode <  T0>   nodeLeft = node.left;
            int leftSize = nodeLeft != null ? nodeLeft.  size(indexColors)    : 0;

            // delete on the left first
            if(index < leftSize) {
                // we can only remove part of our requirement on the left, so do
                // that part recursively
                if(index + size > leftSize) {
                    int toRemove = leftSize - index;
                    removeFromSubtree(nodeLeft, index,   indexColors,    toRemove);
                    size -= toRemove;
                    leftSize -= toRemove;
                    // we can do our full delete on the left side
                } else {
                    node = nodeLeft;
                    continue;
                }
            }
            assert(index >= leftSize);

            // delete in the centre
            int rightStartIndex = leftSize +  node.nodeSize(indexColors)  ;
            if(index < rightStartIndex) {
                int toRemove = Math.min(rightStartIndex - index, size);
                // decrement the appropriate counts all the way up
                  node.size -= toRemove;
                size -= toRemove;
                rightStartIndex -= toRemove;
                fixCountsThruRoot(node,   node.color,    -toRemove);
                if(  node.size == 0   ) {
                    zeroQueue.add(node);
                }
                if(size == 0) return;
            }
            assert(index >= rightStartIndex);

            // delete on the right last
            index -= rightStartIndex;
            node = node.right;
        }
    }
    /**
     * Replace the specified node with the specified replacement. This does the
     * replacement, then walks up the tree to ensure heights are correct, so
     * the replacement node should have its height set first before this method
     * is called.
     */
    private void replaceChild( FourColorNode <  T0>   node,  FourColorNode <  T0>   replacement) {
         FourColorNode <  T0>   nodeParent = node.parent;

        // replace the root
        if(nodeParent == null) {
            assert(node == root);
            root = replacement;

            // replace on the left
        } else if(nodeParent.left == node) {
            nodeParent.left = replacement;

            // replace on the right
        } else if(nodeParent.right == node) {
            nodeParent.right = replacement;
        }

        // update the replacement's parent
        if(replacement != null) {
            replacement.parent = nodeParent;
        }

        // the height has changed, update that up the tree
        fixHeightPostChange(nodeParent, true);
    }
    /**
     * Replace the specified node with another node deeper in the tree. This
     * is necessary to maintain treeness through deletes.
     *
     * <p>This implementation finds the largest node in the left subtree,
     * removes it, and puts it in the specified node's place.
     *
     * @return the replacement node
     */
    private  FourColorNode <  T0>   replaceEmptyNodeWithChild( FourColorNode <  T0>   toReplace) {
          assert(toReplace.size == 0);
        assert(toReplace.left != null);
        assert(toReplace.right != null);

        // find the rightmost child on the leftside
         FourColorNode <  T0>   replacement = toReplace.left;
        while(replacement.right != null) {
            replacement = replacement.right;
        }
        assert(replacement.right == null);

        // remove that node from the tree
        fixCountsThruRoot(replacement,   replacement.color,      -replacement.size   );
        replaceChild(replacement, replacement.left);

        // update the tree structure to point to the replacement
        replacement.left = toReplace.left;
        if(replacement.left != null) replacement.left.parent = replacement;
        replacement.right = toReplace.right;
        if(replacement.right != null) replacement.right.parent = replacement;
        replacement.height = toReplace.height;
         replacement.refreshCounts();
        replaceChild(toReplace, replacement);
        fixCountsThruRoot(replacement.parent,   replacement.color,      replacement.size   );

        return replacement;
    }


    /**
     * Replace all values at the specified index with the specified new value.
     *
     * <p>Currently this uses a naive implementation of remove then add. If
     * it proves desirable, it may be worthwhile to optimize this implementation
     * with one that performs the remove and insert simultaneously, to save
     * on tree navigation.
     *
     * @return the element that was updated. This is non-null unless the size
     *      parameter is 0, in which case the result is always <code>null</code>.
     */
    public Element<T0> set(int index,   byte indexColors, byte color,    T0 value, int size) {
        remove(index,   indexColors,    size);
        return add(index,   indexColors, color,    value, size);
    }


    /**
     * Remove all nodes from the tree. Note that this is much faster than calling
     * remove on all elements, since the structure can be discarded instead of
     * managed during the removal.
     */
    public void clear() {
        root = null;
    }

    /**
     * Get the index of the specified element, counting only the colors
     * specified.
     *
     * <p>This method is an hotspot, so its crucial that it run as efficiently
     * as possible.
     */
    public int indexOfNode(Element<T0> element, byte colorsOut) {
         FourColorNode <  T0>   node = ( FourColorNode <  T0>  )element;

        // count all elements left of this node
        int index = node.left != null ? node.left.  size(colorsOut)    : 0;

        // add all elements on the left, all the way to the root
        for( ; node.parent != null; node = node.parent) {
            if(node.parent.right == node) {
                index += node.parent.left != null ? node.parent.left.  size(colorsOut)    : 0;
                index +=  node.parent.nodeSize(colorsOut)  ;
            }
        }

        return index;
    }


    /**
     * Find the index of the specified element
     *
     * @param firstIndex true to return the index of the first occurrence of the
     *     specified element,  or false for the last index.
     * @param simulated true to return an index value even if the element is not
     *     found. Otherwise -1 is returned.
     * @return an index, or -1 if simulated is false and there exists no
     *     element x in this tree such that
     *     <code>FourColorTree.getComparator().compare(x, element) == 0</code>.
     */
    public int indexOfValue(T0 element, boolean firstIndex, boolean simulated, byte colorsOut) {
        int result = 0;
        boolean found = false;

        // go deep, looking for our node of interest
         FourColorNode <  T0>   node = root;
        while(true) {
            if(node == null) {
                if(found && !firstIndex) result--;
                if(found || simulated) return result;
                else return -1;
            }

            // figure out if the value is left, center or right
            int comparison = comparator.compare(element, node.get());

            // recurse on the left
            if(comparison < 0) {
                node = node.left;
                continue;
            }
             FourColorNode <  T0>   nodeLeft = node.left;

            // the result is in the centre
            if(comparison == 0) {
                found = true;

                // recurse deeper on the left, looking for the first left match
                if(firstIndex) {
                    node = nodeLeft;
                    continue;
                }
            }

            // recurse on the right, increment result by left size and center size
            result += nodeLeft != null ? nodeLeft.  size(colorsOut)    : 0;
            result +=  node.nodeSize(colorsOut)  ;
            node = node.right;
        }
    }

    /**
     * Convert one index into another.
     */
    public int convertIndexColor(int index, byte indexColors, byte colorsOut) {
        if(root == null) {
            if(index == 0) return 0;
            else throw new IndexOutOfBoundsException();
        }

        int result = 0;

        // go deep, looking for our node of interest
         FourColorNode <  T0>   node = root;
        while(true) {
            assert(node != null);
            assert(index >= 0);

            // figure out the layout of this node
             FourColorNode <  T0>   nodeLeft = node.left;
            int leftSize = nodeLeft != null ? nodeLeft.  size(indexColors)    : 0;

            // recurse on the left
            if(index < leftSize) {
                node = nodeLeft;
                continue;
                // increment by the count on the left
            } else {
                if(nodeLeft != null) result += nodeLeft.  size(colorsOut)   ;
                index -= leftSize;
            }

            // the result is in the centre
            int size =  node.nodeSize(indexColors)  ;
            if(index < size) {
                // we're on a node of the same color, return the adjusted index

                if(  (colorsOut & node.color) > 0   ) {
                    result += index;
                    // we're on a node of a different color, return the previous node of the requested color
                } else {
                    result -= 1;
                }
                return result;

                // increment by the count in the centre
            } else {
                result +=  node.nodeSize(colorsOut)  ;
                index -= size;
            }

            // the result is on the right
            node = node.right;
        }
    }

    /**
     * The size of the tree for the specified colors.
     */
    public int size(  byte colors   ) {
        if(root == null) return 0;
        else return root.  size(colors)   ;
    }

    /**
     * Print this tree as a list of values.
     */
    @Override
    public String toString() {
        if(root == null) return "";
        return root.toString(  coder.getColors()   );
    }

    /**
     * Print this tree as a list of colors, removing all hierarchy.
     */

    public String asSequenceOfColors() {
        if(root == null) return "";

        // print it flattened, like a list of colors
        StringBuffer result = new StringBuffer();
        for(FourColorNode n = firstNode(); n != null; n = next(n)) {
            Object color = coder.getColors().get(colorAsIndex(n.color));
            for(  int i = 0; i < n.size; i++  ) {
                result.append(color);
            }
        }
        return result.toString();
    }



    /**
     * Find the next node in the tree, working from left to right.
     */
    public static  <  T0>    FourColorNode <  T0>   next( FourColorNode <  T0>   node) {
        // if this node has a right subtree, it's the leftmost node in that subtree
        if(node.right != null) {
             FourColorNode <  T0>   child = node.right;
            while(child.left != null) {
                child = child.left;
            }
            return child;

            // otherwise its the nearest ancestor where I'm in the left subtree
        } else {
             FourColorNode <  T0>   ancestor = node;
            while(ancestor.parent != null && ancestor.parent.right == ancestor) {
                ancestor = ancestor.parent;
            }
            return ancestor.parent;
        }
    }


    /**
     * Find the previous node in the tree, working from right to left.
     */
    public static  <  T0>    FourColorNode <  T0>   previous( FourColorNode <  T0>   node) {
        // if this node has a left subtree, it's the rightmost node in that subtree
        if(node.left != null) {
             FourColorNode <  T0>   child = node.left;
            while(child.right != null) {
                child = child.right;
            }
            return child;

            // otherwise its the nearest ancestor where I'm in the right subtree
        } else {
             FourColorNode <  T0>   ancestor = node;
            while(ancestor.parent != null && ancestor.parent.left == ancestor) {
                ancestor = ancestor.parent;
            }
            return ancestor.parent;
        }
    }

    /**
     * Find the leftmost child in this subtree.
     */
     FourColorNode <  T0>   firstNode() {
        if(root == null) return null;

         FourColorNode <  T0>   result = root;
        while(result.left != null) {
            result = result.left;
        }
        return result;
    }

    /**
     * @return true if this tree is structurally valid
     */
    private boolean valid() {
        // walk through all nodes in the tree, looking for something invalid
        for( FourColorNode <  T0>   node = firstNode(); node != null; node = next(node)) {
            // sizes (counts) are valid


            int originalCount1 = node.count1;
            int originalCount2 = node.count2;
            int originalCount4 = node.count4;
            int originalCount8 = node.count8;



             node.refreshCounts();

            assert(originalCount1 == node.count1) : "Incorrect count 0 on node: \n" + node  + "\n Expected " + node.count1 + " but was " + originalCount1;
            assert(originalCount2 == node.count2) : "Incorrect count 1 on node: \n" + node  + "\n Expected " + node.count2 + " but was " + originalCount2;
            assert(originalCount4 == node.count4) : "Incorrect count 2 on node: \n" + node  + "\n Expected " + node.count4 + " but was " + originalCount4;
            assert(originalCount8 == node.count8) : "Incorrect count 3 on node: \n" + node  + "\n Expected " + node.count8 + " but was " + originalCount8;




            // heights are valid
            int leftHeight = node.left != null ? node.left.height : 0;
            int rightHeight = node.right != null ? node.right.height : 0;
            assert(Math.max(leftHeight, rightHeight) + 1 == node.height);

            // left child's parent is this
            assert(node.left == null || node.left.parent == node);

            // right child's parent is this
            assert(node.right == null || node.right.parent == node);

            // tree is AVL
            assert(Math.abs(leftHeight - rightHeight) < 2) : "Subtree is not AVL: \n" + node;
        }

        // we're valid
        return true;
    }

    /**
     * Convert the specified color value (such as 1, 2, 4, 8, 16 etc.) into an
     * index value (such as 0, 1, 2, 3, 4 etc. ).
     */
    static final int colorAsIndex(byte color) {
        switch(color) {
            case 1: return 0;
            case 2: return 1;
            case 4: return 2;
            case 8: return 3;
            case 16: return 4;
            case 32: return 5;
            case 64: return 6;
        }
        throw new IllegalArgumentException();
    }
}
  /*[ END_M4_JAVA ]*/
