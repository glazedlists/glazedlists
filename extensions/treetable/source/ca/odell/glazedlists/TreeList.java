/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.FourColorTree;
import ca.odell.glazedlists.impl.adt.barcode2.ListToByteCoder;

import java.util.*;

/**
 * A hierarchial EventList that infers its structure from a flat list.
 *
 * <p><strong>Developer Preview</strong> this class is still under heavy development
 * and subject to API changes. It's also really slow at the moment and won't scale
 * to lists of size larger than a hundred or so efficiently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeList<E> extends TransformedList<TreeList.Node<E>,E> {

    private static final FunctionList.Function NO_OP_FUNCTION = new NoOpFunction();
    private static final NodeComparator COMPARABLE_NODE_COMPARATOR = comparatorToNodeComparator((Comparator)GlazedLists.comparableComparator());

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

    /** compare nodes */
    private final NodeComparator<E> nodeComparator;

    /** an {@link EventList} of {@link Node}s with the structure of the tree. */
    private EventList<Node<E>> nodesList = null;

    /**
     * All the tree data is stored in this tree.
     *
     * <p>Children of collapsed nodes are {@link #VISIBLE_NODES}, everything
     * else is {@link #HIDDEN_NODES}.
     */
    private FourColorTree<Node<E>> data = new FourColorTree<Node<E>>(BYTE_CODER);

    /**
     * The format is used to obtain path information from list elements.
     */
    private Format<E> format;

    /**
     * Create a new TreeList that adds hierarchy to the specified source list.
     * This constructor sorts the elements automatically, which is particularly
     * convenient if you're data is not already in a natural tree ordering,
     * ie. siblings are not already adjacent.
     */
    public TreeList(EventList<E> source, Format<E> format, Comparator<E> comparator) {
        this(source, format, comparatorToNodeComparator(comparator), null);
    }
    /** hack extra Comparator so we can build the nodeComparator only once. */
    private TreeList(EventList<E> source, Format<E> format, NodeComparator<E> nodeComparator, Void unusedParameterForSortFirst) {
        this(new SortedList<Node<E>>(new FunctionList<E, Node<E>>(source, new ElementToTreeNodeFunction<E>(format), NO_OP_FUNCTION), nodeComparator), format, nodeComparator);
    }

    /**
     * Create a new TreeList that adds hierarchy to the specified source list.
     * This constructor does not sort the elements.
     */
    public TreeList(EventList<E> source, Format<E> format) {
        this(new FunctionList<E, Node<E>>(source, new ElementToTreeNodeFunction<E>(format), NO_OP_FUNCTION), format, COMPARABLE_NODE_COMPARATOR);
    }
    /** master Constructor that takes a FunctionList or a SortedList(FunctionList) */
    private TreeList(EventList<Node<E>> source, Format<E> format, NodeComparator<E> nodeComparator) {
        super(source);
        this.format = format;
        this.nodeComparator = nodeComparator;

        // insert the new elements like they were adds
        for(int i = 0; i < super.source.size(); i++) {
            Node<E> node = super.source.get(i);
            Element<Node<E>> element = data.add(i, ALL_NODES, VISIBLE_REAL, node, 1);
            node.element = element;
        }

        // populate parent links
        for(int i = 0; i < super.source.size(); i++) {
            Node<E> node = super.source.get(i);
            attachParent(node, false);
        }

        // prepare sibling links
        rebuildAllSiblingLinks();

        assert(isValid());

        super.source.addListEventListener(this);
    }

    /**
     * @return an {@link EventList} of {@link Node}s which can be used
     *      to access this tree more structurally.
     */
    public EventList<Node<E>> getNodesList() {
        if(nodesList == null) {
            nodesList = new NodesList();
        }
        return nodesList;
    }

    /**
     * Iterate through all nodes in the tree, updating their sibling links.
     * This should only be used by the constructor since it takes a long time
     * to execute.
     */
    private void rebuildAllSiblingLinks() {
        for(int i = 0; i < data.size(ALL_NODES); i++) {
            Node<E> node = data.get(i, ALL_NODES).get();

            // populate sibling relations
            Node<E> siblingBefore = findSiblingBeforeByValue(node);
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
     * @param fireEvents false to suppress event firing, for use only in the
     *      constructor of {@link TreeList}.
     * @return true if the attached node is visible, false otherwise
     */
    private boolean attachParent(Node<E> node, boolean fireEvents) {
        Node<E> parent = findParentByValue(node, fireEvents);
        node.parent = parent;

        // toggle the visibility of the attached node
        if(parent != null) {
            boolean visible = parent.expanded && parent.isVisible();
            setVisible(node, visible);
            return visible;
        }

        // parentless nodes are always visible
        return true;
    }

    /**
     * Search the tree for the parent of the specified node.
     */
    private Node<E> findParentByValue(Node<E> node, boolean fireEvents) {
        // no parents for root nodes
        if(node.pathLength() == 1) return null;

        // search for our parent, starting at our predecessor
        Node<E> parentPrototype = node.describeParent();
        Element<Node<E>> previousElement = node.element.previous();
        Node<E> predecessor = previousElement != null ? previousElement.get() : null;

        // we don't have a predecessor, we need to fab a parent
        if(predecessor == null) {
            // create a parent below

        // our predecessor is too shallow to be our parent
        } else if(predecessor.pathLength() < parentPrototype.pathLength()) {
            // create a parent below

        // our predecessor is at our parent's height, it could be our parent
        } else if(predecessor.pathLength() == parentPrototype.pathLength()) {
            if(compareNodes(predecessor, parentPrototype) == 0) {
                return predecessor;
            }

        // our predecessor is deeper than our parent, it could be a descendent of our parent
        } else {
            if(isAncestorByValue(predecessor, parentPrototype)) {
                Node<E> predecessorAncestor = predecessor;
                while(predecessorAncestor.pathLength() > parentPrototype.pathLength()) {
                    predecessorAncestor = predecessorAncestor.parent;
                }
                return predecessorAncestor;
            }
        }

        // create a parent
        int index = data.indexOfNode(node.element, ALL_NODES);
        Element<Node<E>> parentElement = data.add(index, ALL_NODES, VISIBLE_VIRTUAL, parentPrototype, 1);
        parentPrototype.element = parentElement;
        boolean visible = attachParent(parentPrototype, fireEvents);
        if(fireEvents && visible) {
            int visibleIndex = data.indexOfNode(parentPrototype.element, VISIBLE_NODES);
            updates.addInsert(visibleIndex);
        }
        return parentPrototype;
    }

    /**
     * Find the latest node with the same parent that's before this node.
     */
    private Node<E> findSiblingBeforeByValue(Node<E> node) {

        // this is crappy, we should figure out something more robust!
        // Currently we do a linear scan backwards looking for a node with the
        // same path length
        int nodeIndex = data.indexOfNode(node.element, ALL_NODES);
        for(int i = nodeIndex - 1; i >= 0; i--) {
            Node<E> beforeNode = data.get(i, ALL_NODES).get();

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

    /**
     * @return the number of ancestors of the node at the specified index.
     *      Root nodes have depth 0, other nodes depth is one
     *      greater than the depth of their parent node.
     */
    public int depth(int visibleIndex) {
        return getTreeNode(visibleIndex).path.size() - 1;
    }

    /**
     * The number of nodes including the node itself in its subtree.
     */
    public int subtreeSize(int visibleIndex, boolean includeCollapsed) {
        byte colorsOut = includeCollapsed ? ALL_NODES : VISIBLE_NODES;

        // get the subtree size by finding the next node not in the subtree.
        // We could also calculate this by looking at the first child, then
        // traversing across all children until we get to the end of the
        // children.
        Node<E> node = data.get(visibleIndex, colorsOut).get();

        // find the next node that's not a child to find the delta
        Node<E> nextNodeNotInSubtree = nextNodeThatsNotAChildOf(node);

        // if we don't have a sibling after us, we've hit the end of the tree
        if(nextNodeNotInSubtree == null) {
            return data.size(colorsOut) - visibleIndex;
        }

        return data.indexOfNode(nextNodeNotInSubtree.element, colorsOut) - visibleIndex;
    }

    /**
     * Find the first node after the specified node that's not its child. This
     * is necessary to calculate the size of the node's subtree.
     */
    private Node<E> nextNodeThatsNotAChildOf(Node<E> node) {
        for(; node != null; node = node.parent) {
            Node<E> followerNode = node.siblingAfter;
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
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        return data.convertIndexColor(mutationIndex, VISIBLE_NODES, REAL_NODES);
    }

    /** {@inheritDoc} */
    public E get(int visibleIndex) {
        return getTreeNode(visibleIndex).getElement();
    }

    /** {@inheritDoc} */
    public Node<E> getTreeNode(int visibleIndex) {
        return data.get(visibleIndex, VISIBLE_NODES).get();
    }

    /**
     * @return <code>true</code> if the node at the specified index has
     *      children, regardless of whether such children are visible.
     */
    public boolean hasChildren(int visibleIndex) {
        return subtreeSize(visibleIndex, true) > 1;
    }

    /**
     * Whether the value at the specified index can have child nodes or not.
     *
     * @see Format#allowsChildren
     * @return <code>true<code> if child nodes can be added to the
     *      specified node.
     */
    public boolean getAllowsChildren(int visibleIndex) {
        return format.allowsChildren(get(visibleIndex));
    }

    /**
     * @return <code>true</code> if the children of the node at the specified
     *      index are visible. Nodes with no children may be expanded or not,
     *      this is used to determine whether to show children should any be
     *      added.
     */
    public boolean isExpanded(int visibleIndex) {
        return data.get(visibleIndex, VISIBLE_NODES).get().expanded;
    }
    /**
     * Control whether the child elements of the specified node are visible.
     *
     * @param expanded <code>true</code> to expand the node, <code>false</code>
     *      to collapse it.
     */
    public void setExpanded(int visibleIndex, boolean expanded) {
        Node<E> toExpand = data.get(visibleIndex, VISIBLE_NODES).get();

        // if we're already in the desired state, give up!
        if(toExpand.expanded == expanded) return;

        // first toggle the active node. Note that it's visibility does not
        // change, only that of its children
        toExpand.expanded = expanded;

        updates.beginEvent();

        Node<E> toExpandNextSibling = nextNodeThatsNotAChildOf(toExpand);

        // walk through the subtree, looking for all the descendents we need
        // to change. As we encounter them, change them and fire events
        for(Element<Node<E>> descendentElement = toExpand.element.next(); descendentElement != null; descendentElement = descendentElement.next()) {
            Node<E> descendent = descendentElement.get();
            if(descendent == toExpandNextSibling) break;

            // figure out if this node should be visible by walking up the ancestors
            // to the node being expanded, searching for a parent that's not
            // expanded
            boolean shouldBeVisible = expanded;
            for(Node<E> ancestor = descendent.parent; shouldBeVisible && ancestor != toExpand; ancestor = ancestor.parent) {
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
     * A convenience method to expand the row if it is currently collapsed or
     * vice versa.
     */
    public void toggleExpanded(int visibleIndex) {
        setExpanded(visibleIndex, !isExpanded(visibleIndex));
    }

    /**
     * Set the visibility of the specified node without firing any events.
     */
    private void setVisible(Node<E> node, boolean visible) {
        byte newColor;
        if(visible) {
            newColor = node.virtual ? VISIBLE_VIRTUAL : VISIBLE_REAL;
        } else {
            newColor = node.virtual ? HIDDEN_VIRTUAL : HIDDEN_REAL;
        }
        data.setColor(node.element, newColor);
    }

    /**
     * Set the virtualness of the specified node without firing events.
     */
    private void setVirtual(Node<E> node, boolean virtual) {
        byte newColor;
        if(virtual) {
            newColor = node.isVisible() ? VISIBLE_VIRTUAL : HIDDEN_VIRTUAL;
        } else {
            newColor = node.virtual ? VISIBLE_REAL : HIDDEN_REAL;
        }
        data.setColor(node.element, newColor);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<Node<E>> listChanges) {
        updates.beginEvent(true);

        // add the new data, remove the old data, and mark the updated data
        List<Node<E>> parentsToVerify = new ArrayList<Node<E>>();
        List<Node<E>> parentsToRestore = new ArrayList<Node<E>>();
        while(listChanges.next()) {
            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                handleInsert(sourceIndex);
                // todo: repair siblings

            } else if(type == ListEvent.UPDATE) {
                Node<E> node = data.get(sourceIndex, REAL_NODES).get();

                // shift as necessary, so if the parents are immediately after, they're used
                if(hasLogicallyShifted(node)) {
                    handleDelete(sourceIndex, parentsToVerify, parentsToRestore);
                    handleInsert(sourceIndex);
                } else {
                    if(node.isVisible()) {
                        int viewIndex = data.indexOfNode(node.element, VISIBLE_NODES);
                        updates.addUpdate(viewIndex);
                    }
                }

                // todo: repair siblings

            } else if(type == ListEvent.DELETE) {
                handleDelete(sourceIndex, parentsToVerify, parentsToRestore);

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
     * Remove the node at the specified index, firing all the required
     * notifications.
     */
    private void handleDelete(int sourceIndex, List<Node<E>> parentsToVerify, List<Node<E>> parentsToRestore) {
        Node<E> node = data.get(sourceIndex, REAL_NODES).get();

        // if it has children, make it virtual and schedule if for verification or deletion later
        if(!node.isLeaf()) {
            // todo: make setVirtual() actually do node.virtual = virtual
            setVirtual(node, true);
            node.virtual = true;
            parentsToVerify.add(node);
            return;
        }

        // otherwise delete it
        boolean visible = node.isVisible();
        if(visible) {
            int viewIndex = data.indexOfNode(node.element, VISIBLE_NODES);
            updates.addDelete(viewIndex);
        }
        data.remove(node.element);
        node.element = null; // null out the element

        // remove the parent if necessary in the next iteration
        parentsToVerify.add(node.parent);
        // restore this as a parent for its children in the next iteration
        parentsToRestore.add(node);
    }

    /**
     * Ensure all of the specified parents are still required. If they're not,
     * they'll be removed and the appropriate events fired.
     */
    private void deleteObsoleteParents(List<Node<E>> parentsToVerify) {
        deleteObsoleteParents:
        for(Iterator<Node<E>> i = parentsToVerify.iterator(); i.hasNext(); ) {
            Node<E> parent = i.next();

            // walk up the tree, deleting nodes
            while(parent != null) {
                // we've reached a real parent, don't delete it!
                if(!parent.virtual) break;
                // we've already deleted this parent, we're done
                if(parent.element == null) break;

                // if this is a legit parent, then we'll still have a child node.
                // if it turns out that that child is also unnecessary, we'll clean
                // everything up in the next iteration
                boolean stillRequired;
                int index = data.indexOfNode(parent.element, ALL_NODES);
                if(index + 1 < data.size(ALL_NODES)) {
                    Node<E> possibleChild = data.get(index + 1, ALL_NODES).get();
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
     * Decide whether an node needs to be rebuilt due to a change in its
     * structural meaning. For example, if it is no longer a parent by value for
     * its children, or if its no longer a child by value of its parent.
     *
     * @return <code>true</code> if the specified node has structurally
     *      shifted in the tree.
     */
    private boolean hasLogicallyShifted(Node<E> node) {
        // todo: handle shifts in children?

        // is the predecessor a parent or sibling?
        while(true) {
            Element<Node<E>> previousElement = node.element.previous();
            if(previousElement == null) break;
            Node<E> previousNode = previousElement.get();

            // the predecessor is a sibling
            if(previousNode.pathLength() == node.pathLength()) {
                // make sure the sibling is on the right side
                if(previousNode.virtual && compareNodes(previousNode, node) > 0) {
                    return true;
                // and that we agree what our parent looks like
                } else if(!isAncestorByValue(node, previousNode.parent)) {
                    return true;
                }

            // the predecessor is a parent
            } else if(previousNode.pathLength() == node.pathLength() - 1) {
                // make sure that parent it is an ancestor by value
                if(!isAncestorByValue(node, previousNode)) {
                    return true;
                }

            } else {
                return true;
            }

            break;
        }

        // should it be swapped with the follower?
        while(true) {
            Element<Node<E>> nextElement = node.element.next();
            if(nextElement == null) break;
            Node<E> nextNode = nextElement.get();
            // the follower is not virtual
            if(!nextNode.virtual) break;
            // no swap is necessary, the node is supposed to be after us
            if(compareNodes(nextNode, node) >= 0) break;

            // a swap is necessary!
            return true;
        }

        return false;
    }

    /**
     * Figure out where to insert the specified value in the data list,
     * accounting for virtual parents that may already exist and require
     * skipping.
     */
    private void handleInsert(int sourceIndex) {
        Node<E> node = super.source.get(sourceIndex);

        // figure out where the source node immediately before us lies. All
        // nodes between it and us must me our virtual parents
        int predecessorIndex = sourceIndex > 0 ? data.convertIndexColor(sourceIndex - 1, REAL_NODES, ALL_NODES) : -1;
        int followerIndex = data.size(REAL_NODES) > sourceIndex ? data.convertIndexColor(sourceIndex, REAL_NODES, ALL_NODES) : data.size(ALL_NODES);

        // walk through all our parent nodes already in the tree
        int insertIndex = predecessorIndex + 1;
        while(insertIndex < followerIndex) {
            Node<E> possibleAncestor = data.get(insertIndex, ALL_NODES).get();

            // this ancestor is a virtual copy of the node to be inserted,
            // in which case we want to update over it. For example, if we
            // simulated a node and then that value gets inserted 'for real',
            // we keep the state of that node
            int relativePosition = compareNodes(node, possibleAncestor);
            if(possibleAncestor.virtual && relativePosition == 0) {

                // make the real node copy the state of the virtual
                node.updateFrom(possibleAncestor);

                // replace the virtual with the real in the tree structure
                possibleAncestor.element.set(node);
                setVirtual(possibleAncestor, false);
                for(Node<E> child = possibleAncestor.firstChild(); child != null; child = child.siblingAfter) {
                    child.parent = node;
                }

                // fire an 'update' event so selection is not destroyed
                if(possibleAncestor.isVisible()) {
                    int visibleIndex = data.indexOfNode(node.element, VISIBLE_NODES);
                    updates.addUpdate(visibleIndex);
                }
                return;
            }

            // if this node follows us by value, we've gone too far
            if(relativePosition < 0) {
                break;
            }

            insertIndex++;
        }

        // insert a new node
        Element<Node<E>> element = data.add(insertIndex, ALL_NODES, VISIBLE_REAL, node, 1);
        node.element = element;

        // link to the parent to determine visibility
        boolean visible = attachParent(node, true);

        // set visibility and fire an event
        if(visible) {
            int visibleIndex = data.indexOfNode(node.element, VISIBLE_NODES);
            updates.addInsert(visibleIndex);
        }
    }

    private int compareNodes(Node<E> a, Node<E> b) {
        return nodeComparator.compare(a, b);
    }

    /**
     * Change how the structure of the tree is derived.
     *
     * @param treeFormat
     */
    public void setTreeFormat(Format<E> treeFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a {@link List} containing all {@link Node}s with no parents in the
     * tree.
     */
    public List<Node<E>> getRoots() {
        // todo: make this make sense
        List<Node<E>> result = new ArrayList<Node<E>>();
        for(int i = 0; i < size(); i++) {
            Node<E> possibleRoot = getTreeNode(i);
            if(possibleRoot.pathLength() == 1) {
                result.add(possibleRoot);
            }
        }
        return result;
    }

    /**
     * @return true if the path of possibleAncestor is a proper prefix of
     *      the path of this node.
     */
    public boolean isAncestorByValue(Node<E> child, Node<E> possibleAncestor) {
        List<E> possibleAncestorPath = possibleAncestor.path;

        // this is too long a bath to be an ancestor's
        if(possibleAncestorPath.size() >= child.path.size()) return false;

        // make sure the whole trail of the ancestor is common with our trail
        for(int d = possibleAncestorPath.size() - 1; d >= 0; d--) {
            E possibleAncestorPathElement = possibleAncestorPath.get(d);
            E pathElement = child.path.get(d);
            if(nodeComparator.comparator.compare(possibleAncestorPathElement, pathElement) != 0) return false;
        }
        return true;
    }

    /**
     * Define the tree structure of an node by expressing the path from the
     * element itself to the tree's root.
     */
    public interface Format<E> {

        /**
         * Populate path with a list describing the path from a root node to
         * this element. Upon returning, the list must have size >= 1, where
         * the provided element identical to the list's last element.
         *
         * @param path a list that the implementor shall add their path
         *      elements to via <code>path.add()</code>. This may be a non-empty
         *      List and it is an error to call any method other than add().
         */
        public void getPath(List<E> path, E element);

        /**
         * Whether an element can have children.
         *
         * @return <code>true</code> if this element can have child elements,
         *      or <code>false</code> if it is always a leaf node.
         */
        public boolean allowsChildren(E element);
    }

    /**
     * Convert the specified {@link Comparator<E>} into a {@link Comparator} for
     * nodes of type E.
     */
    private static <E> NodeComparator<E> comparatorToNodeComparator(Comparator<E> comparator) {
        return new NodeComparator<E>(comparator);
    }

    private static class NodeComparator<E> implements Comparator<Node<E>> {
        private final Comparator<E> comparator;

        public NodeComparator(Comparator<E> comparator) {
            if(comparator == null) throw new IllegalArgumentException();
            this.comparator = comparator;
        }

        public int compare(Node<E> a, Node<E> b) {
            int myPathLength = a.path.size();
            int otherPathLength = b.path.size();

            // compare by value first
            for(int i = 0; i < myPathLength && i < otherPathLength; i++) {
                int result = comparator.compare(a.path.get(i), b.path.get(i));
                if(result != 0) return result;
            }

            // and path length second
            return myPathLength - otherPathLength;
        }

    }

    /**
     * A private function used to convert list elements into tree paths.
     */
    private static class ElementToTreeNodeFunction<E> implements FunctionList.AdvancedFunction<E, Node<E>> {
        private final Format<E> format;
        private final List<E> workingPath = new ArrayList<E>();
        public ElementToTreeNodeFunction(Format<E> format) {
            this.format = format;
        }
        public Node<E> evaluate(E sourceValue) {
            Node<E> result = new Node<E>();
            result.virtual = false;

            // populate the path using the working path as a temporary variable
            format.getPath(workingPath, sourceValue);
            result.path = new ArrayList<E>(workingPath);
            workingPath.clear();

            return result;
        }

        public Node<E> reevaluate(E sourceValue, Node<E> transformedValue) {
            format.getPath(workingPath, sourceValue);
            transformedValue.path = new ArrayList<E>(workingPath);
            workingPath.clear();

            return transformedValue;
        }

        public void dispose(E sourceValue, Node<E> transformedValue) {
            // do nothing
        }
    }
    private static class NoOpFunction implements FunctionList.Function {
        public Object evaluate(Object sourceValue) {
            return sourceValue;
        }
    }

    /**
     * A node in the display tree.
     */
    public static class Node<E> {

        private List<E> path;

        /** true if this node isn't in the source list */
        private boolean virtual;

        /** true if this node's children should be visible */
        private boolean expanded = true;

        /** the element object points back at this, for the tree's structure cache */
        private Element<Node<E>> element;

        /** the relationship of this node to others */
        private Node<E> siblingAfter;
        private Node<E> siblingBefore;
        private Node<E> parent;

        /**
         * The length of the path to this node, in nodes.
         *
         * @return 1 for the root node, 2 for its children, etc.
         */
        private int pathLength() {
            return path.size();
        }

        /**
         * Get the List element at the end of this path.
         */
        public E getElement() {
            return path.get(path.size() - 1);
        }

        /**
         * @return the path elements for this element, it is an error to modify.
         */
        public List<E> path() {
            return path;
        }

        /**
         * Create a {@link Node} that resembles the parent of this.
         */
        private Node<E> describeParent() {
            Node<E> result = new Node<E>();
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
         * Copy the values from that object into this object. This is useful
         * when a virtual node is replaced by a real one, and we want the real
         * one to have the attributes of its predecessor.
         */
        private void updateFrom(Node<E> other) {
            element = other.element;
            virtual = false;
            expanded = other.expanded;
            siblingAfter = other.siblingAfter;
            siblingBefore = other.siblingBefore;
            parent = other.parent;
        }

        /**
         * @return <code>true</code> if this node has at least one child node,
         *      according to our structure cache.
         */
        public boolean isLeaf() {
            Element<Node<E>> next = element.next();
            if(next == null) return true;

            return next.get().parent != this;
        }

        /**
         * Get the first child of this node, or <code>null</code> if no
         * such child exists. This is <strong>not</strong> by value, but by the
         * current tree structure.
         */
        private Node<E> firstChild() {
            // the first child is always the node immediately after
            Element<Node<E>> possibleChildElement = element.next();
            if(possibleChildElement == null) return null;
            Node<E> possibleChild = possibleChildElement.get();
            if(possibleChild.parent != this) return null;
            return possibleChild;
        }

        /**
         * List all children of this node.
         */
        public List<Node<E>> getChildren() {
            List<Node<E>> result = new ArrayList<Node<E>>();
            for(Node<E> child = firstChild(); child != null; child = child.siblingAfter) {
                result.add(child);
            }
            return result;
        }
    }

    /**
     * Convert this {@link TreeList<E>} or {@link Node<E>}s, and expose
     * it as the raw {@link E elements}s.
     */
    private class NodesList extends TransformedList<E,Node<E>> {

        public NodesList() {
            super(TreeList.this);
            TreeList.this.addListEventListener(this);
        }

        protected boolean isWritable() {
            return true;
        }

        public Node<E> get(int index) {
            return getTreeNode(index);
        }

        public void listChanged(ListEvent<E> listChanges) {
            updates.forwardEvent(listChanges);
        }
    }

    /** {@inheritDoc} */
    public String toTreeString() {
        final StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < size(); i++) {
            final int depth = depth(i);
            final Node<E> node = getTreeNode(i);

            for(int j = 0; j < depth; j++) {
                buffer.append("\t");
            }

            buffer.append(node).append("\n");
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
            Node<E> node = data.get(i, ALL_NODES).get();

            // path lengths should only grow by one from one child to the next
            assert(node.pathLength() <= lastPathLengthSeen + 1);
            lastPathLengthSeen = node.pathLength();

            // the virtual flag should be consistent with the node color
            if(node.virtual) {
                assert(node.element.getColor() == HIDDEN_VIRTUAL|| node.element.getColor() == VISIBLE_VIRTUAL);
            } else {
                assert(source.get(data.convertIndexColor(i, ALL_NODES, REAL_NODES)) == node);
                assert(node.element.getColor() == HIDDEN_REAL || node.element.getColor() == VISIBLE_REAL);
            }

            // only validate the roots, they'll validate the rest recursively
            if(node.pathLength() == 1) {
                validateSubtree(node);
            }
        }

        return true;
    }
    private void validateSubtree(Node<E> node) {
        int index = data.indexOfNode(node.element, ALL_NODES);
        int size = subtreeSize(index, true);

        // validate all children in the subtree
        Node<E> lastChildSeen = null;
        for(int i = 1; i < size; i++) {
            Node<E> descendent = data.get(index + i, ALL_NODES).get();

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
