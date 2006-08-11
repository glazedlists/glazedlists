/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.FourColorTree;
import ca.odell.glazedlists.impl.adt.barcode2.ListToByteCoder;
import ca.odell.glazedlists.event.ListEvent;

import java.util.*;

/**
 * An experimental attempt at building a TreeList. Not for real-world use!
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeList<E> extends TransformedList<TreeList.TreeElement<E>,TreeList.TreeElement<E>> {

    /** node colors define where it is in the source and where it is here */
    private static final ListToByteCoder BYTE_CODER = new ListToByteCoder(Arrays.asList(new String[] { "R", "V", "r", "v" }));
    private static final byte VISIBLE_REAL = BYTE_CODER.colorToByte("R");
    private static final byte VISIBLE_VIRTUAL = BYTE_CODER.colorToByte("V");
    private static final byte HIDDEN_REAL = BYTE_CODER.colorToByte("r");
    private static final byte HIDDEN_VIRTUAL = BYTE_CODER.colorToByte("v");

    /** node classes let us search through nodes more efficiently */
    private static final byte ALL_NODES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "R", "V", "r", "v" }));
    private static final byte VISIBLE_NODES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "R", "V" }));
    private static final byte HIDDEN_NODES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "r", "v" }));
    private static final byte REAL_NODES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "R", "r" }));

    /**
     * All the tree data is stored in this tree.
     *
     * <p>Children of collapsed nodes are {@link #VISIBLE_NODES}, everything
     * else is {@link #HIDDEN_NODES}.
     */
    private FourColorTree<TreeElement<E>> data = new FourColorTree<TreeElement<E>>(BYTE_CODER);

    public TreeList(EventList<E> source, Format<E> format) {
        super(new SortedList<TreeElement<E>>(new FunctionList<E, TreeElement<E>>(source, new TreeElementFunction<E>(format))));

        // insert the new elements like they were adds
        for(int i = 0; i < super.source.size(); i++) {
            TreeElement<E> treeElement = super.source.get(i);
            Element<TreeElement<E>> element = data.add(i, ALL_NODES, VISIBLE_REAL, treeElement, 1);
            treeElement.element = element;
        }

        // populate the lead sibling and parent links, including virtual elements
        for(int i = 0; i < super.source.size(); i++) {
            TreeElement<E> node = super.source.get(i);

            // populate parent relations
            node.parent = (TreeElement<E>)findParentByValue(node, true, false);

            // populate sibling relations
            TreeElement<E> siblingBefore = findSiblingBeforeByValue(node);
            if(siblingBefore != null) {
                node.siblingBefore = siblingBefore;
                siblingBefore.siblingAfter = node;
            }
        }

        assert(isValid());

        super.source.addListEventListener(this);
    }

    /**
     * @param createIfNecessary true to recursively create as many parents
     *      as necessary.
     * @param fireEvents false to suppress event firing, for use only in the
     *      constructor of {@link TreeList}.
     */
    private TreeElement<E> findParentByValue(TreeElement<E> node, boolean createIfNecessary, boolean fireEvents) {
        if(fireEvents) throw new UnsupportedOperationException();

        // no parents for root nodes
        if(node.pathLength() == 1) return null;

        // figure out what our parent would look like and where it would be
        TreeElement<E> parentPrototype = node.describeParent();
        int expectedParentIndex = data.indexOfValue(parentPrototype, true, true, ALL_NODES);
        assert(expectedParentIndex <= data.indexOfNode(node.element, ALL_NODES));
        TreeElement<E> possibleParent = data.get(expectedParentIndex, ALL_NODES).get();

        // we already have a parent!
        if(parentPrototype.compareTo(possibleParent) == 0) {
            return possibleParent;

        // we need to create a parent
        } else if(createIfNecessary) {
            Element<TreeElement<E>> element = data.add(expectedParentIndex, ALL_NODES, VISIBLE_VIRTUAL, parentPrototype, 1);
            parentPrototype.element = element;
            parentPrototype.parent = findParentByValue(parentPrototype, true, fireEvents);
            return parentPrototype;

        // no parent exists, and we're not going to make one
        } else {
            return null;
        }
    }

    /**
     * Find the latest node with the same parent that's before this node.
     */
    private TreeElement<E> findSiblingBeforeByValue(TreeElement<E> node) {

        // this is crappy, we should figure out something more robust!
        // Currently we do a linear scan backwards looking for a node with the
        // same path length
        int nodeIndex = data.indexOfNode(node.element, ALL_NODES);
        for(int i = nodeIndex - 1; i >= 0; i++) {
            TreeElement<E> beforeNode = data.get(i, ALL_NODES).get();

            // our sibling will have the same path length
            if(beforeNode.pathLength() == node.pathLength()) {
                return beforeNode;

            // we've stumbled on our parent, don't bother going further
            } else if(beforeNode.pathLength() < node.pathLength()) {
                return null;
            }
        }

        // we've run out of data, this must be the first root
        assert(node.pathLength() == 1);
        assert(nodeIndex == 0);
        return null;
    }

    public int depth(int index) {
        return getTreeElement(index).path.size();
    }


    /**
     * The number of elements including the node itself in its subtree.
     */
    public int subtreeSize(int index, boolean includeCollapsed) {
        // get the subtree size by finding the next element not in the subtree.
        // We could also calculate this by looking at the first child, then
        // traversing across all children until we get to the end of the
        // children.

        TreeElement<E> treeElement = getTreeElement(index);

        // find the next node that's not a child to find the delta
        TreeElement<E> nextNodeNotInSubtree = nextNodeThatsNotAChildOf(treeElement);

        // if we don't have a sibling after us, we have no children
        if(nextNodeNotInSubtree == null) {
            return 1;
        }

        byte colorsOut = includeCollapsed ? ALL_NODES : VISIBLE_NODES;
        return data.indexOfNode(nextNodeNotInSubtree.element, colorsOut) - index + 1;
    }
    private TreeElement<E> getTreeElement(int visibleIndex) {
        return data.get(visibleIndex, VISIBLE_NODES).get();
    }

    /**
     * Find the first node after the specified node that's not its child. This
     * is necessary to calculate the size of the node's subtree.
     */
    private TreeElement<E> nextNodeThatsNotAChildOf(TreeElement<E> element) {

        for(; element != null; element = element.parent) {
            TreeElement<E> followerNode = element.siblingAfter;
            if(followerNode != null) {
                return followerNode;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public int size() {
        return data.size(VISIBLE_NODES);
    }

    /** {@inheritDoc} */
    public TreeElement<E> get(int index) {
        return data.get(index, VISIBLE_NODES).get();
    }

    /**
     * @param expanded true to expand the node, false to collapse it.
     */
    public int setExpanded(int index, boolean expanded) {
        throw new UnsupportedOperationException();
    }

    public void listChanged(ListEvent<TreeElement<E>> listChanges) {
        throw new UnsupportedOperationException();
    }

    /**
     * Define the tree structure of an element by expressing the path from the
     * element itself to the tree's root.
     */
    public interface Format<E> {
        public List<E> getPath(E element);
    }

    /**
     * A private function used to convert list elements into tree paths.
     */
    private static class TreeElementFunction<E> implements FunctionList.AdvancedFunction<E, TreeElement<E>> {
        private final Format<E> format;
        public TreeElementFunction(Format<E> format) {
            this.format = format;
        }
        public TreeElement<E> evaluate(E sourceValue) {
            TreeElement<E> result = new TreeElement<E>();
            result.path = format.getPath(sourceValue);
            result.virtual = false;
            return result;
        }

        public TreeElement<E> reevaluate(E sourceValue, TreeElement<E> transformedValue) {
            transformedValue.path = format.getPath(sourceValue);
            return transformedValue;
        }

        public void dispose(E sourceValue, TreeElement<E> transformedValue) {
            // do nothing
        }
    }

    /**
     * A node in the display tree.
     */
    public static class TreeElement<E> implements Comparable<TreeElement<E>> {

        private static final Comparator comparableComparator = GlazedLists.comparableComparator();
        private List<E> path;

        /** true if this element isn't in the source list */
        private boolean virtual;

        /** the element object points back at this */
        private Element<TreeElement<E>> element;

        /** the relationship of this element to others */
        private TreeElement<E> siblingAfter;
        private TreeElement<E> siblingBefore;
        private TreeElement<E> parent;

        /**
         * The length of the path to this element, in nodes.
         *
         * @return 1 for the root node, 2 for its children, etc.
         */
        private int pathLength() {
            return path.size();
        }

        /**
         * Compare paths by parts from root deeper.
         */
        public int compareTo(TreeElement<E> other) {
            int myPathLength = path.size();
            int otherPathLength = other.path.size();

            // compare by value first
            for(int i = 0; i < myPathLength && i < otherPathLength; i++) {
                int result = comparableComparator.compare(this.path.get(i), other.path.get(i));
                if(result != 0) return result;
            }

            // and path lenght second
            return myPathLength - otherPathLength;
        }

        /**
         * Create a {@link TreeElement} that resembles the parent of this.
         */
        private TreeElement<E> describeParent() {
            TreeElement<E> result = new TreeElement<E>();
            result.path = path.subList(0, path.size() - 1);
            result.virtual = true;
            return result;
        }

        /** {@inheritDoc} */
        public String toString() {
            return path.toString();
        }
    }



    /**
     * Sanity check the entire datastructure.
     */
    private boolean isValid() {
        int lastPathLengthSeen = 0;
        for(int i = 0; i < data.size(ALL_NODES); i++) {
            TreeElement<E> element = data.get(i, ALL_NODES).get();

            // path lengths should only grow by one from one child to the next
            assert(element.pathLength() <= lastPathLengthSeen + 1);
            lastPathLengthSeen = element.pathLength();

            // only validate the roots, they'll validate the rest recursively
            if(element.pathLength() == 1) {
                validateSubtree(element);
            }
        }

        return true;
    }
    private void validateSubtree(TreeElement<E> node) {
        int index = data.indexOfNode(node.element, ALL_NODES);
        int size = subtreeSize(index, true);

        // validate all children in the subtree
        TreeElement<E> lastChildSeen = null;
        for(int i = 1; i < size; i++) {
            TreeElement<E> descendent = data.get(index + i, ALL_NODES).get();

            // if this is a direct child, validate it
            if(descendent.pathLength() == node.pathLength() + 1) {
                assert(descendent.parent == node);
                assert(lastChildSeen == descendent.siblingBefore);
                if(lastChildSeen != null) assert(lastChildSeen.siblingAfter == descendent);
                lastChildSeen = descendent;
                validateSubtree(descendent);

            // skip grandchildren and deeper
            } else if(descendent.pathLength() > node.pathLength() + 1) {
                // do nothing

            // we've found a parent in our subtree?
            } else {
                throw new IllegalStateException();
            }
        }

        // make sure we don't have a trailing sibling
        if(lastChildSeen != null) assert(lastChildSeen.siblingAfter == null);
    }
}
