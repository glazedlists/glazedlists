/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import java.util.Arrays;

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
 * for up to eight different colors. As well, this supports values in the node.
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
public class Tree<V> {

    /** the number of colors in this tree */
    private final int colorCount;
    /** the colors in the tree, used for printing purposes only */
    private final ListToByteCoder coder;

    /** the tree's root, or <code>null</code> for an empty tree */
    private Node<V> root = null;

    /**
     * Create a tree using the specified color codings for the nodes.
     */
    public Tree(ListToByteCoder coder) {
        this.coder = coder;
        this.colorCount = coder.getColors().size();
    }

    /**
     * Get the tree element at the specified index relative to the specified index
     * colors.
     */
    public Element<V> get(int index, byte indexColors) {
        if(root == null) throw new IndexOutOfBoundsException();

        // go deep, looking for our node of interest
        Node<V> node = root;
        while(true) {
            assert(node != null);
            assert(index >= 0);

            // recurse on the left
            int leftSize = node.left != null ? node.left.size(indexColors) : 0;
            if(index < leftSize) {
                node = node.left;
                continue;
            } else {
                index -= leftSize;
            }

            // the result is in the centre
            int size = node.nodeSize(indexColors);
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
    public Element<V> add(int index, byte indexColors, byte color, V value, int size) {
        assert(index >= 0);
        assert(index <= size(indexColors));
        assert(size >= 0);

        int colorAsIndex = Node.colorAsIndex(color);

        if(this.root == null) {
            if(index != 0) throw new IndexOutOfBoundsException();

            this.root = new Node<V>(colorCount, color, colorAsIndex, size, value, null);
            assert(valid());
            return this.root;
        } else {
            Node<V> inserted = insertIntoSubtree(root, index, indexColors, color, colorAsIndex, value, size);
            assert(valid());
            return inserted;
        }
    }

    /**
     *
     * @param parent the subtree to insert into, must not be null.
     * @param index the color index to insert at
     * @param indexColors a bitmask of all colors that the index is defined in
     *      terms of. For example, if this is determined in terms of colors 4, 8
     *      and 32, then the value here should be 44 (32 + 8 + 4).
     * @param color a bitmask value such as 1, 2, 4, 8, 16, 32, 64 or 128.
     * @param colorAsIndex an index value such as 0, 1, 2, 3, 4, 5, 6 or 7.
     * @param value the object to hold in the inserted node.
     * @param size the size of the inserted node, with respect to indices.
     * @return the inserted node, or the modified node if this insert simply
     *      increased the size of an existing node.
     */
    private Node<V> insertIntoSubtree(Node<V> parent, int index, byte indexColors, byte color, int colorAsIndex, V value, int size) {
        while(true) {
            assert(parent != null);
            assert(index >= 0);

            // figure out the layout of this node
            int parentLeftSize = parent.left != null ? parent.left.size(indexColors) : 0;
            int parentRightStartIndex = parentLeftSize + parent.nodeSize(indexColors);

            // the first thing we want to try is to merge this value into the
            // current node, since that's the cheapest thing to do:
            if(value == parent.value && value != null && color == parent.color) {
                if(index >= parentLeftSize && index <= parentRightStartIndex) {
                    parent.size += size;
                    fixCountsThruRoot(parent, colorAsIndex, size);
                    return parent;
                }
            }

            // we can insert on the left
            if(index <= parentLeftSize) {
                // as a new left child
                if(parent.left == null) {
                    Node<V> inserted = new Node<V>(colorCount, color, colorAsIndex, size, value, parent);
                    parent.left = inserted;
                    fixCountsThruRoot(parent, colorAsIndex, size);
                    fixHeightPostChange(parent);
                    return inserted;

                // recurse on the left
                } else {
                    parent = parent.left;
                    continue;
                }
            }

            // we need to insert in the centre. This works by splitting in the
            // centre, and inserting the value
            if(index < parentRightStartIndex) {
                int parentLeftHalfSize = index - parentLeftSize;
                int parentRightHalfSize = parentRightStartIndex - index;
                parent.size -= parentRightHalfSize;
                int parentColorAsIndex = Node.colorAsIndex(parent.color);
                fixCountsThruRoot(parent, parentColorAsIndex, -parentRightHalfSize);
                // insert as null first to make sure this doesn't get merged back
                Element<V> inserted = insertIntoSubtree(parent, parentLeftHalfSize, parent.color, parent.color, parentColorAsIndex, null, parentRightHalfSize);
                inserted.set(parent.value);

                // recalculate parentRightStartIndex, since that should have
                // changed by now. this will then go on to insert on the right
                parentRightStartIndex = parentLeftSize + parent.nodeSize(indexColors);
            }

            // on the right
            right: {
                int parentSize = parent.size(indexColors);
                if(index > parentSize) throw new IndexOutOfBoundsException();

                // as a right child
                if(parent.right == null) {
                    Node<V> inserted = new Node<V>(colorCount, color, colorAsIndex, size, value, parent);
                    parent.right = inserted;
                    fixCountsThruRoot(parent, colorAsIndex, size);
                    fixHeightPostChange(parent);
                    return inserted;

                // recurse on the right
                } else {
                    parent = parent.right;
                    index -= parentRightStartIndex;
                }
            }
        }
    }

    /**
     * Adjust counts for all nodes (including the specified node) up the tree
     * to the root. The counts of the specified color are adjusted by delta
     * (which may be positive or negative).
     */
    private final void fixCountsThruRoot(Node<V> node, int colorIndex, int delta) {
        for( ; node != null; node = node.parent) {
            node.counts[colorIndex] += delta;
        }
    }

    /**
     * Fix the height of the specified ancestor after inserting a child node.
     * This method short circuits when it finds the first node where the size
     * has not changed.
     *
     * @param node the root of a changed subtree. This shouldn't be called
     *      on inserted nodes, but rather their parent nodes, since only
     *      the parent nodes sizes will be changing.
     */
    private final void fixHeightPostChange(Node<V> node) {
        int LEFT = -1;
        int EQUAL = 0;
        int RIGHT = 1;
        int largerChild = EQUAL;
        int largerGrandChild = EQUAL;

        // update the height
        for(; node != null; node = node.parent) {
            byte leftHeight = node.left != null ? node.left.height : 0;
            byte rightHeight = node.right != null ? node.right.height : 0;
            byte newHeight = (byte)(Math.max(leftHeight, rightHeight) + 1);
            if(node.height == newHeight) break;
            node.height = newHeight;

            // figure out rotations
            largerGrandChild = largerChild;
            int delta = 0;
            if(leftHeight > rightHeight) {
                largerChild = LEFT;
                delta = leftHeight - rightHeight;
            } else if(rightHeight > leftHeight) {
                largerChild = RIGHT;
                delta = rightHeight - leftHeight;
            }
            assert(delta >= 0 && delta <= 2);

            // do rotations as necessary
            if(delta == 2) {
                if(largerChild == LEFT) {
                    // single rotate left
                    if(largerGrandChild != RIGHT) {
                        node = rotateLeft(node);
                    // double rotate left
                    } else {
                        rotateRight(node.left);
                        node = rotateLeft(node);
                    }
                } else if(largerChild == RIGHT) {
                    // single rotate right
                    if(largerGrandChild != LEFT) {
                        node = rotateRight(node);
                    // double rotate right
                    } else {
                        rotateLeft(node.right);
                        node = rotateRight(node);
                    }
                }
            }
            assert(Math.abs((node.left != null ? node.left.height : 0)
                    - (node.right != null ? node.right.height : 0)) <= 1);
        }
    }

    /**
     * Perform an AVL rotation of the tree.
     *
     * @return the new root of the subtree
     *
     *     D               B
     *    / \   ROTATE    / \
     *   B   E  LEFT AT  A   D
     *  / \     NODE D      / \
     * A   C               C   E
     */
    private final Node<V> rotateLeft(Node<V> subtreeRoot) {
        assert(subtreeRoot.left != null);
        // subtreeRoot is D
        // newSubtreeRoot is B
        Node<V> newSubtreeRoot = subtreeRoot.left;

        // modify the counts
        // originally: B = A + C, D = A + B + C + E
        // remove C counts from B and (so B = A)
        // then remove B counts from D (which includes A) (so  D = C + E)
        // then add D counts to B (so B = A + C + D + E)
        //for(int i = 0; i < colorCount; i++) {
        //    if(newSubtreeRoot.right != null) {
        //        newSubtreeRoot.counts[i] -= newSubtreeRoot.right.counts[i]; // remove C from B
        //    }
        //    subtreeRoot.counts[i] -= newSubtreeRoot.counts[i]; // remove B from D
        //    newSubtreeRoot.counts[i] += subtreeRoot.counts[i]; // add D to B
        //}

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
    private final Node<V> rotateRight(Node<V> subtreeRoot) {
        assert(subtreeRoot.right != null);
        // subtreeRoot is D
        // newSubtreeRoot is B
        Node<V> newSubtreeRoot = subtreeRoot.right;

        // modify the counts
        // originally: B = A + C, D = A + B + C + E
        // remove C counts from B and (so B = A)
        // then remove B counts from D (which includes A) (so  D = C + E)
        // then add D counts to B (so B = A + C + D + E)
        //for(int i = 0; i < colorCount; i++) {
        //    if(newSubtreeRoot.left != null) {
        //        newSubtreeRoot.counts[i] -= newSubtreeRoot.left.counts[i]; // remove C from B
        //    }
        //    subtreeRoot.counts[i] -= newSubtreeRoot.counts[i]; // remove B from D
        //    newSubtreeRoot.counts[i] += subtreeRoot.counts[i]; // add D to B
        //}

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
     * Remove size values at the specified index. Only values of the type
     * specified in indexColors will be removed.
     *
     * <p>Note that if the two nodes on either side of the removed node could
     * be merged, they probably will not be merged by this implementation. This
     * is to simplify the implementation, but it means that when iterating a
     * tree, sometimes multiple nodes of the same color and value will be
     * encountered in sequence.
     */
    public void remove(int index, byte indexColors, int size) {
        if(size == 0) return;
        assert(index >= 0);
        assert(index + size <= size(indexColors));
        assert(root != null);

        removeFromSubtree(root, index, indexColors, size);
        valid();
    }

    /**
     * Remove at the specified index in the specified subtree.
     */
    private void removeFromSubtree(Node<V> node, int index, byte indexColors, int size) {
        while(size > 0) {
            assert(node != null);
            assert(index >= 0);

            // figure out the layout of this node
            int leftSize = node.left != null ? node.left.size(indexColors) : 0;

            // delete on the left first
            if(index < leftSize) {
                // we can only remove part of our requirement on the left, so do
                // that part recursively
                if(index + size > leftSize) {
                    int toRemove = leftSize - index;
                    removeFromSubtree(node.left, index, indexColors, toRemove);
                    leftSize -= toRemove;
                    size -= toRemove;
                // we can do our full delete on the left side
                } else {
                    node = node.left;
                    continue;
                }
            }
            assert(index >= leftSize);

            // delete in the centre
            int rightStartIndex = leftSize + node.nodeSize(indexColors);
            if(index < rightStartIndex) {
                int toRemove = Math.min(rightStartIndex - index, size);
                // decrement the appropriate counts all the way up
                int colorIndex = Node.colorAsIndex(node.color);
                node.size -= toRemove;
                fixCountsThruRoot(node, colorIndex, -toRemove);
                // replace this node with another if empty
                if(node.size == 0) {
                    if(node.right == null) {
                        replaceChild(node, node.left);
                    } else if(node.left == null) {
                        replaceChild(node, node.right);
                    } else {
                        node = replaceEmptyNodeWithChild(node);
                    }
                }
                size -= toRemove;
                if(size == 0) return;
                leftSize -= toRemove;
                rightStartIndex -= toRemove;
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
    private void replaceChild(Node<V> node, Node<V> replacement) {
        // replace the root
        if(node.parent == null) {
            assert(node == root);
            root = replacement;

        // replace on the left
        } else if(node.parent.left == node) {
            node.parent.left = replacement;

        // replace on the right
        } else if(node.parent.right == node) {
            node.parent.right = replacement;
        }

        // update the replacement's parent
        if(replacement != null) {
            replacement.parent = node.parent;
        }

        // the height has changed, update that up the tree
        fixHeightPostChange(node.parent);
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
    private Node<V> replaceEmptyNodeWithChild(Node<V> toReplace) {
        assert(toReplace.size == 0);
        assert(toReplace.left != null);
        assert(toReplace.right != null);

        // find the rightmost child on the leftside
        Node<V> replacement = toReplace.left;
        while(replacement.right != null) {
            replacement = replacement.right;
        }
        assert(replacement.right == null);

        // remove that node from the tree
        int replacementColorIndex = Node.colorAsIndex(replacement.color);
        fixCountsThruRoot(replacement, replacementColorIndex, -replacement.size);
        replaceChild(replacement, replacement.left);

        // update the tree structure to point to the replacement
        replacement.left = toReplace.left;
        if(replacement.left != null) replacement.left.parent = replacement;
        replacement.right = toReplace.right;
        if(replacement.right != null) replacement.right.parent = replacement;
        replacement.height = toReplace.height;
        replacement.refreshCounts();
        replaceChild(toReplace, replacement);
        fixCountsThruRoot(replacement.parent, replacementColorIndex, replacement.size);

        return replacement;
    }

    /**
     * @return true if this tree is structurally valid
     */
    private boolean valid() {
        if(root == null) return true;

        // walk through all nodes in the tree, looking for something invalid
        for(Node node = root.leftmostChild(); node != null; node = node.next()) {
            // sizes (counts) are valid
            int[] currentCounts = node.counts.clone();
            node.refreshCounts();
            assert(Arrays.equals(currentCounts, node.counts)) : "Incorrect counts on node: \n" + node  + "\n Expected " + Arrays.toString(currentCounts) + " but was " + Arrays.toString(node.counts);

            // heights are valid
            int leftHeight = node.left != null ? node.left.height : 0;
            int rightHeight = node.right != null ? node.right.height : 0;
            assert(Math.max(leftHeight, rightHeight) + 1 == node.height);

            // left child's parent is this
            assert(node.left == null || node.left.parent == node);

            // right child's parent is this
            assert(node.right == null || node.right.parent == node);
        }

        // we're valid
        return true;
    }

    /**
     * Replace all values at the specified index with the specified new value.
     *
     * <p>Currently this uses a naive implementation of remove then add. If
     * it proves desirable, it may be worthwhile to optimize this implementation
     * with one that performs the remove and insert simultaneously, to save
     * on tree navigation.
     */
    public void set(int index, byte indexColors, byte color, V value, int size) {
        remove(index, indexColors, size);
        add(index, indexColors, color, value, size);
    }

    /**
     * Get the index of the specified element, counting only the colors
     * specified.
     */
    public int indexOf(Element<V> element, byte colorsOut) {
        Node<V> node = (Node<V>)element;

        // count all elements left of this node
        int index = node.left != null ? node.left.size(colorsOut) : 0;

        // add all elements on the left, all the way to the root
        while(node.parent != null) {
            if(node.parent.right == node) {
                index += node.parent.left != null ? node.parent.left.size(colorsOut) : 0;
                index += node.parent.nodeSize(colorsOut);
            }
            node = node.parent;
        }

        return index;
    }

    /**
     * Convert one index into another.
     */
    public int indexOf(int index, byte indexColors, byte colorsOut) {
        if(root == null) {
            if(index == 0) return 0;
            else throw new IndexOutOfBoundsException();
        }

        int result = 0;

        // go deep, looking for our node of interest
        Node<V> node = root;
        while(true) {
            assert(node != null);
            assert(index >= 0);

            // figure out the layout of this node
            int leftSize = node.left != null ? node.left.size(indexColors) : 0;

            // recurse on the left
            if(index < leftSize) {
                node = node.left;
                continue;
            // increment by the count on the left
            } else {
                if(node.left != null) result += node.left.size(colorsOut);
                index -= leftSize;
            }

            // the result is in the centre
            int size = node.nodeSize(indexColors);
            if(index < size) {
                // we're on a node of the same color, return the adjusted index
                if((colorsOut & node.color) > 0) {
                    result += index;
                // we're on a node of a different color, return the previous node of the requested color
                } else {
                    result -= 1;
                }
                return result;

            // increment by the count in the centre
            } else {
                result += node.nodeSize(colorsOut);
                index -= size;
            }

            // the result is on the right
            node = node.right;
        }
    }

    /**
     * The size of the tree for the specified colors.
     */
    public int size(byte colors) {
        if(root == null) return 0;
        else return root.size(colors);
    }

    /**
     * Print this tree as a list of values.
     */
    public String toString() {
        if(root == null) return "";
        return root.toString(coder.getColors());
    }

    /**
     * Print this tree as a list of colors, removing all hierarchy.
     */
    public String asSequenceOfColors() {
        if(root == null) return "";

        // print it flattened, like a list of colors
        StringBuffer result = new StringBuffer();
        for(Node n = root.leftmostChild(); n != null; n = n.next()) {
            Object color = coder.getColors().get(Node.colorAsIndex(n.color));
            for(int i = 0; i < n.size; i++) {
                result.append(color);
            }
        }
        return result.toString();
    }
}