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
            attachParent(node, true, false);
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
     * Find the parent for the specified node, creating it if necessary. When
     * a parent is found, this node is attached to that parent and its
     * visibility is inherited from its parent's 'visible' and 'expanded'
     * flags.
     *
     * @param createIfNecessary true to recursively create as many parents
     *      as necessary.
     * @param fireEvents false to suppress event firing, for use only in the
     *      constructor of {@link TreeList}.
     * @return true if the attached element is visible, false otherwise
     */
    private boolean attachParent(TreeElement<E> node, boolean createIfNecessary, boolean fireEvents) {
        TreeElement<E> parent = findParentByValue(node, createIfNecessary, fireEvents);
        node.parent = parent;

        // toggle the visibility of the attached node
        if(parent != null) {
            boolean visible = parent.expanded && parent.isVisible();
            setVisible(node, visible);
            return visible;
        }

        // parentless elements are always visible
        return true;
    }

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

        // create a parent
        } else if(createIfNecessary) {
            Element<TreeElement<E>> element = data.add(expectedParentIndex, ALL_NODES, VISIBLE_VIRTUAL, parentPrototype, 1);
            parentPrototype.element = element;
            boolean visible = attachParent(parentPrototype, true, fireEvents);
            if(fireEvents && visible) {
                int visibleIndex = data.indexOfNode(parentPrototype.element, VISIBLE_NODES);
                updates.addInsert(visibleIndex);
            }
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
        TreeElement<E> treeElement = data.get(index, colorsOut).get();

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

    public boolean isExpandable(int index) {
        return true;
//        throw new UnsupportedOperationException();
    }

    public boolean isExpanded(int index) {
        return data.get(index, VISIBLE_NODES).get().expanded;
    }
    /**
     * @param expanded true to expand the node, false to collapse it.
     */
    public void setExpanded(int index, boolean expanded) {
        TreeElement<E> toExpand = data.get(index, VISIBLE_NODES).get();

        // if we're already in the desired state, give up!
        if(toExpand.expanded == expanded) return;

        // first toggle the active node. Note that it's visibility does not
        // change, only that of its children
        toExpand.expanded = expanded;

        updates.beginEvent();

        TreeElement<E> toExpandNextSibling = nextNodeThatsNotAChildOf(toExpand);

        // walk through the subtree, looking for all the descendents we need
        // to change. As we encounter them, change them and fire events
        for(Element<TreeElement<E>> descendentElement = toExpand.element.next(); descendentElement != null; descendentElement = descendentElement.next()) {
            TreeElement<E> descendent = descendentElement.get();
            if(descendent == toExpandNextSibling) break;

            // figure out if this node should be visible by walking up the ancestors
            // to the node being expanded, searching for a parent that's not
            // expanded
            boolean shouldBeVisible = expanded;
            for(TreeElement<E> ancestor = descendent.parent; shouldBeVisible && ancestor != toExpand; ancestor = ancestor.parent) {
                if(!ancestor.expanded) {
                    shouldBeVisible = false;
                }
            }
            if(shouldBeVisible == descendent.isVisible()) continue;

            // show a non-visible node
            if(shouldBeVisible) {
                setVisible(descendent, true);
                int insertIndex = data.indexOfNode(descendent.element, VISIBLE_NODES);
                updates.addInsert(insertIndex);

            // hide a visible node
            } else {
                int deleteIndex = data.indexOfNode(descendent.element, VISIBLE_NODES);
                updates.addDelete(deleteIndex);
                setVisible(descendent, false);
            }
        }
        assert(isValid());
        updates.commitEvent();
    }

    /**
     * Set the visibility of the specified element without firing any events.
     */
    private void setVisible(TreeElement<E> node, boolean visible) {
        byte newColor;
        if(visible) {
            newColor = node.virtual ? VISIBLE_VIRTUAL : VISIBLE_REAL;
        } else {
            newColor = node.virtual ? HIDDEN_VIRTUAL : HIDDEN_REAL;
        }
        data.setColor(node.element, newColor);
    }

    /**
     * Set the virtualness of the specified element without firing events.
     */
    private void setVirtual(TreeElement<E> node, boolean virtual) {
        byte newColor;
        if(virtual) {
            newColor = node.isVisible() ? VISIBLE_VIRTUAL : HIDDEN_VIRTUAL;
        } else {
            newColor = node.virtual ? VISIBLE_REAL : HIDDEN_REAL;
        }
        data.setColor(node.element, newColor);
    }

    public void listChanged(ListEvent<TreeElement<E>> listChanges) {

        updates.beginEvent(true);

        // add the new data, remove the old data, and mark the updated data
        List<TreeElement<E>> parentsToVerify = new ArrayList<TreeElement<E>>();
        while(listChanges.next()) {
            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                handleInsert(sourceIndex);
                // todo: repair siblings

            } else if(type == ListEvent.UPDATE) {
                TreeElement<E> treeElement = data.get(sourceIndex, REAL_NODES).get();
                int viewIndex = data.indexOfNode(treeElement.element, VISIBLE_NODES);
                updates.addUpdate(viewIndex);

                // shift as necessary, so if the parents are immediately after, they're used
                shiftParentsAsNecessary(treeElement);

                // add new parents, validate the old ones are still necessary
                parentsToVerify.add(treeElement.parent);
                attachParent(treeElement, true, true);
                // todo: repair siblings

                // todo: handle case where it went from visible to invisible
                // due to moving from one subtree to another

            } else if(type == ListEvent.DELETE) {
                TreeElement<E> treeElement = data.get(sourceIndex, REAL_NODES).get();
                boolean visible = treeElement.isVisible();
                if(visible) {
                    int viewIndex = data.indexOfNode(treeElement.element, VISIBLE_NODES);
                    updates.addDelete(viewIndex);
                }
                data.remove(treeElement.element);
                treeElement.element = null; // null out the element

                // remove the parent if necessary in the next iteration
                parentsToVerify.add(treeElement.parent);

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
                // we've already deleted this parent, we're done
                if(parent.element == null) break;

                // if this is a legit parent, then we'll still have a child element.
                // if it turns out that that child is also unnecessary, we'll clean
                // everything up in the next iteration
                boolean stillRequired;
                int index = data.indexOfNode(parent.element, ALL_NODES);
                if(index + 1 < data.size(ALL_NODES)) {
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
                data.remove(parent.element);
                updates.addDelete(viewIndex);
                parent.element = null; // null out the element

                // now we might need to delete the parent's parent
                parent = parent.parent;
            }
        }
    }

    /**
     * Adjust the location of the updated tree element so if it now fits under
     * it's neighbours parents, those parents are in the correct place.
     *
     * <p>This method could benefit highly from the addition of a Glazed Lists
     * 'move' event.
     */
    private void shiftParentsAsNecessary(TreeElement<E> treeElement) {
        int index = data.indexOfNode(treeElement.element, ALL_NODES);

        // swap forward through misplaced followers until we're in the right place
        boolean shiftedForward = false;
        while(index - 1 > 0) {
            TreeElement<E> predecessor = data.get(index - 1, ALL_NODES).get();
            // the follower's not virtual
            if(!predecessor.virtual) break;
            // no swap is necessary, the element is supposed to be before us
            if(predecessor.compareTo(treeElement) <= 0) break;

            // swap this with its precessor
            data.remove(predecessor.element);
            // todo - fire events in the 'visible' index range
            updates.addDelete(index - 1);
            Element<TreeElement<E>> element = data.add(index, ALL_NODES, predecessor.element.getColor(), predecessor, 1);
            predecessor.element = element;
            updates.addInsert(index);
            index--;
            shiftedForward = true;
        }

        // don't shift backwards if we shifted forwards
        if(shiftedForward) return;

        // shift backwards through virtual followers until we're in the right place
        while(index + 1 < data.size(ALL_NODES)) {
            TreeElement<E> possiblePredecessor = data.get(index + 1, ALL_NODES).get();
            // the predecessor's not virtual
            if(!possiblePredecessor.virtual) break;
            // no swap is necessary, the element is supposed to be after us
            if(possiblePredecessor.compareTo(treeElement) >= 0) break;

            // swap this with its follower
            data.remove(possiblePredecessor.element);
            updates.addDelete(index + 1);
            Element<TreeElement<E>> element = data.add(index, ALL_NODES, possiblePredecessor.element.getColor(), possiblePredecessor, 1);
            possiblePredecessor.element = element;
            updates.addInsert(index);
            index++;
        }
    }

    /**
     * Figure out where to insert the specified value in the data list,
     * accounting for virtual parents that may already exist and require
     * skipping.
     */
    private void handleInsert(int sourceIndex) {
        TreeElement<E> treeElement = super.source.get(sourceIndex);

        // figure out where the source element immediately before us lies. All
        // elements between it and us must me our virtual parents
        int predecessorIndex = sourceIndex > 0 ? data.convertIndexColor(sourceIndex - 1, REAL_NODES, ALL_NODES) : -1;
        int followerIndex = data.size(REAL_NODES) > sourceIndex ? data.convertIndexColor(sourceIndex, REAL_NODES, ALL_NODES) : data.size(ALL_NODES);

        // walk through all our parent nodes already in the tree
        int insertIndex = predecessorIndex + 1;
        while(insertIndex < followerIndex) {
            TreeElement<E> possibleAncestor = data.get(insertIndex, ALL_NODES).get();

            // this ancestor is a virtual copy of the node to be inserted,
            // in which case we want to update over it. For example, if we
            // simulated a node and then that value gets inserted 'for real',
            // we keep the state of that node
            if(possibleAncestor.virtual && possibleAncestor.compareTo(treeElement) == 0) {

                // make the real element copy the state of the virtual
                treeElement.updateFrom(possibleAncestor);

                // replace the virtual with the real in the tree structure
                possibleAncestor.element.set(treeElement);
                setVirtual(possibleAncestor, false);
                for(TreeElement<E> child = firstChildOf(possibleAncestor); child != null; child = child.siblingAfter) {
                    child.parent = treeElement;
                }

                // fire an 'update' event so selection is not destroyed
                if(possibleAncestor.isVisible()) {
                    int visibleIndex = data.indexOfNode(treeElement.element, VISIBLE_NODES);
                    updates.addUpdate(visibleIndex);
                }
                return;
            }

            // this element is definitely not our parent, it's path is too long
            if(possibleAncestor.pathLength() > treeElement.pathLength() - 1) break;

            // make sure the data is consistent with our parent's data
            if(!treeElement.isAncestorByValue(possibleAncestor)) break;

            // we found an ancestor, skip past it when inserting
            insertIndex++;
        }

        // insert a new element
        Element<TreeElement<E>> element = data.add(insertIndex, ALL_NODES, VISIBLE_REAL, treeElement, 1);
        treeElement.element = element;

        // link to the parent to determine visibility
        boolean visible = attachParent(treeElement, true, true);

        // set visibility and fire an event
        if(visible) {
            int visibleIndex = data.indexOfNode(treeElement.element, VISIBLE_NODES);
            updates.addInsert(visibleIndex);
        }
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
     * Get the first child of the specified node, or <code>null</code> if no
     * such child exists. This is <strong>not</strong> by value, but by the
     * current tree structure.
     */
    public TreeElement<E> firstChildOf(TreeElement<E> node) {
        // the first child is always the node immediately after
        Element<TreeElement<E>> possibleChildElement = node.element.next();
        if(possibleChildElement == null) return null;
        TreeElement<E> possibleChild = possibleChildElement.get();
        if(possibleChild.parent != node) return null;
        return possibleChild;
    }

    /**
     * A node in the display tree.
     */
    public static class TreeElement<E> implements Comparable<TreeElement<E>> {

        private List<E> path;

        /** true if this element isn't in the source list */
        private boolean virtual;

        /** true if this element's children should be visible */
        private boolean expanded = true;

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

        /**
         * @return <code>true</code> if this node shows up in the output
         *      EventList, or <code>false</code> if it's a descendent of a
         *      collapsed node.
         */
        public boolean isVisible() {
            return (element.getColor() & VISIBLE_NODES) > 0;
        }

        /**
         * @return true if the path of possibleAncestor is a proper prefix of
         *      the path of this element.
         */
        public boolean isAncestorByValue(TreeElement<E> possibleAncestor) {
            List<E> possibleAncestorPath = possibleAncestor.path;

            // this is too long a bath to be an ancestor's
            if(possibleAncestorPath.size() >= path.size()) return false;

            // make sure the whole trail of the ancestor is common with our trail
            for(int d = possibleAncestorPath.size() - 1; d >= 0; d--) {
                E possibleAncestorPathElement = possibleAncestorPath.get(d);
                E pathElement = path.get(d);
                if(comparableComparator.compare(possibleAncestorPathElement, pathElement) != 0) return false;
            }
            return true;
        }

        /**
         * Copy the values from that object into this object. This is useful
         * when a virtual node is replaced by a real one, and we want the real
         * one to have the attributes of its predecessor.
         */
        private void updateFrom(TreeElement<E> other) {
            element = other.element;
            virtual = false;
            expanded = other.expanded;
            siblingAfter = other.siblingAfter;
            siblingBefore = other.siblingBefore;
            parent = other.parent;
        }
    }

    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            final int depth = depth(i);
            final TreeElement<E> treeElement = (TreeElement<E>) get(i);

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
                if(descendent.parent != node) {
                    throw new IllegalStateException();
                }
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
