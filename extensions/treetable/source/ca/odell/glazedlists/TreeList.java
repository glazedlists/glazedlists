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

    private static final Comparator comparableComparator = GlazedLists.comparableComparator();

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

        // populate parent links
        for(int i = 0; i < super.source.size(); i++) {
            TreeElement<E> node = super.source.get(i);
            node.parent = findParentByValue(node, true, false);
        }

        // prepare sibling links
        rebuildAllSiblingLinks();

        assert(isValid());

        super.source.addListEventListener(this);
    }

    /**
     * Iterate through all elements in the tree, updating their sibling links.
     * This should only be used by the constructor since it takes a long time
     * to execute.
     */
    private void rebuildAllSiblingLinks() {
        for(int i = 0; i < data.size(ALL_NODES); i++) {
            TreeElement<E> node = data.get(i, ALL_NODES).get();

            // populate sibling relations
            TreeElement<E> siblingBefore = findSiblingBeforeByValue(node);
            if(siblingBefore != null) {
                node.siblingBefore = siblingBefore;
                siblingBefore.siblingAfter = node;
            } else {
                node.siblingBefore = null;
            }

            // sibling after may be set in a future iteration of this loop
            node.siblingAfter = null;
        }
    }

    /**
     * @param createIfNecessary true to recursively create as many parents
     *      as necessary.
     * @param fireEvents false to suppress event firing, for use only in the
     *      constructor of {@link TreeList}.
     */
    private TreeElement<E> findParentByValue(TreeElement<E> node, boolean createIfNecessary, boolean fireEvents) {

        // no parents for root nodes
        if(node.pathLength() == 1) return null;

        // figure out what our parent would look like and where it would be
        TreeElement<E> parentPrototype = node.describeParent();
        int expectedParentIndex = data.indexOfValue(parentPrototype, true, true, ALL_NODES);
        assert(node.element == null || expectedParentIndex <= data.indexOfNode(node.element, ALL_NODES));
        TreeElement<E> possibleParent = data.get(expectedParentIndex, ALL_NODES).get();

        // we already have a parent!
        if(parentPrototype.compareTo(possibleParent) == 0) {
            return possibleParent;

        // we need to create a parent
        } else if(createIfNecessary) {
            Element<TreeElement<E>> element = data.add(expectedParentIndex, ALL_NODES, VISIBLE_VIRTUAL, parentPrototype, 1);
            parentPrototype.element = element;
            parentPrototype.parent = findParentByValue(parentPrototype, true, fireEvents);
            if(fireEvents) updates.addInsert(expectedParentIndex);
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
        for(int i = nodeIndex - 1; i >= 0; i--) {
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
        return getTreeElement(index).path.size()-1;
    }


    /**
     * The number of elements including the node itself in its subtree.
     */
    public int subtreeSize(int index, boolean includeCollapsed) {
        byte colorsOut = includeCollapsed ? ALL_NODES : VISIBLE_NODES;

        // get the subtree size by finding the next element not in the subtree.
        // We could also calculate this by looking at the first child, then
        // traversing across all children until we get to the end of the
        // children.

        TreeElement<E> treeElement = getTreeElement(index);

        // find the next node that's not a child to find the delta
        TreeElement<E> nextNodeNotInSubtree = nextNodeThatsNotAChildOf(treeElement);

        // if we don't have a sibling after us, we've hit the end of the tree
        if(nextNodeNotInSubtree == null) {
            return data.size(colorsOut) - index;
        }

        return data.indexOfNode(nextNodeNotInSubtree.element, colorsOut) - index;
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

        updates.beginEvent(true);

        // add the new data, remove the old data, and mark the updated data
        List<TreeElement<E>> parentsToVerify = new ArrayList<TreeElement<E>>();
        while(listChanges.next()) {
            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                int insertIndex = findInsertIndex(sourceIndex);
                TreeElement<E> treeElement = source.get(sourceIndex);
                Element<TreeElement<E>> element = data.add(insertIndex, ALL_NODES, VISIBLE_REAL, treeElement, 1);
                treeElement.element = element;
                updates.addInsert(insertIndex);

                // populate parent relations
                treeElement.parent = findParentByValue(treeElement, true, true);

                // todo: repair siblings
                // todo: handle case where an identical virtual element already exists

            } else if(type == ListEvent.UPDATE) {
                Element<TreeElement<E>> element = data.get(sourceIndex, REAL_NODES);
                int viewIndex = data.indexOfNode(element, VISIBLE_NODES);
                updates.addUpdate(viewIndex);
                // todo: repair parents, repair siblings

            } else if(type == ListEvent.DELETE) {
                Element<TreeElement<E>> element = data.get(sourceIndex, REAL_NODES);
                int viewIndex = data.indexOfNode(element, VISIBLE_NODES);
                data.remove(sourceIndex, REAL_NODES, 1);
                updates.addDelete(viewIndex);

                // remove the parent if necessary in the next iteration
                parentsToVerify.add(element.get().parent);

                // todo: repair siblings
            }
        }

        // blow away obsolete parent nodes, and their parents recursively
        deleteObsoleteParents(parentsToVerify);

        // we're currently too lazy to rebuild the sibling links properly, so
        // just brute-force through all of them!
        rebuildAllSiblingLinks();

        assert(isValid());

        updates.commitEvent();
    }

    /**
     * Ensure all of the specified parents are still required. If they're not,
     * they'll be removed and the appropriate events fired.
     */
    private void deleteObsoleteParents(List<TreeElement<E>> parentsToVerify) {
        deleteObsoleteParents:
        for(Iterator<TreeElement<E>> i = parentsToVerify.iterator(); i.hasNext(); ) {
            TreeElement<E> parent = i.next();

            // walk up the tree, deleting nodes
            while(parent != null) {
                // we've reached a real parent, don't delete it!
                if(!parent.virtual) break;

                // if this is a legit parent, then we'll still have a child element.
                // if it turns out that that child is also unnecessary, we'll clean
                // everything up in the next iteration
                boolean stillRequired;
                int index = data.indexOfNode(parent.element, ALL_NODES);
                if(index + 1< data.size(ALL_NODES)) {
                    TreeElement<E> possibleChild = data.get(index + 1, ALL_NODES).get();
                    stillRequired = possibleChild.parent == parent;
                } else {
                    stillRequired = false;
                }

                // this is a legit parent, nothing to see here
                if(stillRequired) {
                    continue deleteObsoleteParents;
                }

                // we need to destroy this parent
                int viewIndex = data.indexOfNode(parent.element, VISIBLE_NODES);
                data.remove(index, ALL_NODES, 1);
                updates.addDelete(viewIndex);

                // now we might need to delete the parent's parent
                parent = parent.parent;
            }
        }
    }

    /**
     * Figure out where to insert the specified value in the data list,
     * accounting for virtual parents that may already exist and require
     * skipping.
     */
    private int findInsertIndex(int sourceIndex) {
        TreeElement<E> treeElement = super.source.get(sourceIndex);

        // figure out where the source element immediately before us lies. All
        // elements between it and us must me our virtual parents
        int predecessorIndex = sourceIndex > 0 ? data.convertIndexColor(sourceIndex - 1, REAL_NODES, ALL_NODES) : -1;
        int followerIndex = data.size(REAL_NODES) > sourceIndex ? data.convertIndexColor(sourceIndex, REAL_NODES, ALL_NODES) : data.size(ALL_NODES);

        // walk through all our parent nodes already in the tree
        int insertIndex = predecessorIndex + 1;
        skipAllAncestors:
        while(insertIndex < followerIndex) {
            TreeElement<E> possibleAncestor = data.get(insertIndex, ALL_NODES).get();

            // this element is definitely not our parent, it's path is too long
            if(possibleAncestor.pathLength() > treeElement.pathLength() - 1) return insertIndex;

            // make sure the data is consistent with our parent's data
            List<E> possibleAncestorPath = possibleAncestor.path;
            for(int d = possibleAncestorPath.size() - 1; d >= 0; d--) {
                E possibleAncestorPathElement = possibleAncestorPath.get(d);
                E pathElement = treeElement.path.get(d);
                if(comparableComparator.compare(possibleAncestorPathElement, pathElement) != 0) return insertIndex;
            }

            // we found a parent, skip past it when inserting
            insertIndex++;
        }
        return insertIndex;
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
         * @return the path elements for this element, it is an error to modify.
         */
        public List<E> path() {
            return path;
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

            // and path length second
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

    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            final int depth = depth(i);
            final TreeElement<E> treeElement = get(i);

            for (int j = 0; j < depth; j++)
                buffer.append("\t");

            buffer.append(treeElement).append("\n");
        }

        return buffer.toString();
    }


    /**
     * Sanity check the entire datastructure.
     */
    private boolean isValid() {
        // we should have the correct number of real nodes
        assert(source.size() == data.size(REAL_NODES));

        // walk through the tree, validating structure and each subtree
        int lastPathLengthSeen = 0;
        for(int i = 0; i < data.size(ALL_NODES); i++) {
            TreeElement<E> element = data.get(i, ALL_NODES).get();

            // path lengths should only grow by one from one child to the next
            assert(element.pathLength() <= lastPathLengthSeen + 1);
            lastPathLengthSeen = element.pathLength();

            // the virtual flag should be consistent with the node color
            if(element.virtual) {
                assert(element.element.getColor() == HIDDEN_VIRTUAL|| element.element.getColor() == VISIBLE_VIRTUAL);
            } else {
                assert(source.get(data.convertIndexColor(i, ALL_NODES, REAL_NODES)) == element);
                assert(element.element.getColor() == HIDDEN_REAL || element.element.getColor() == VISIBLE_REAL);
            }

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
        assert(lastChildSeen == null || lastChildSeen.siblingAfter == null);
    }
}
