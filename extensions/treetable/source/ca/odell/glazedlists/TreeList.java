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
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 * 
 * <p><strong>Developer Preview</strong> this class is still under heavy development
 * and subject to API changes. It's also really slow at the moment and won't scale
 * to lists of size larger than a hundred or so efficiently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeList<E> extends TransformedList<TreeList.Node<E>,E> {

    private static final FunctionList.Function NO_OP_FUNCTION = new NoOpFunction();

    /** determines the layout of new nodes as they are created */
    private static final NewNodeStateProvider DEFAULT_NEW_NODE_STATE_PROVIDER = new DefaultNewNodeStateProvider();

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
     * reuse the CloneStateNewNodeStateProvider to save expensive <code>new</code> calls.
     * Note that this cannot be static because it is stateful.
     */
    private CloneStateNewNodeStateProvider<E> cloneStateNewNodeStateProvider = new CloneStateNewNodeStateProvider<E>();

    /**
     * Create a new TreeList that adds hierarchy to the specified source list.
     * This constructor sorts the elements automatically, which is particularly
     * convenient if you're data is not already in a natural tree ordering,
     * ie. siblings are not already adjacent.
     */
    public TreeList(EventList<E> source, Format<E> format, Comparator<E> comparator) {
        this(source, format, comparatorToNodeComparator(comparator, format), null);
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
        this(new FunctionList<E, Node<E>>(source, new ElementToTreeNodeFunction<E>(format), NO_OP_FUNCTION), format, comparatorToNodeComparator((Comparator)GlazedLists.comparableComparator(), format));
    }
    /** master Constructor that takes a FunctionList or a SortedList(FunctionList) */
    private TreeList(EventList<Node<E>> source, Format<E> format, NodeComparator<E> nodeComparator) {
        super(source);
        this.format = format;
        this.nodeComparator = nodeComparator;

        // insert the new elements like they were adds
        for(int i = 0; i < super.source.size(); i++) {
            Node<E> node = super.source.get(i);
            node.element = data.add(i, REAL_NODES, VISIBLE_REAL, node, 1);
            attachParentsAndSiblings(node, false);
        }

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
        Node<E> nextNodeNotInSubtree = nextNodeThatsNotAChildOfByStructure(node);

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
    private Node<E> nextNodeThatsNotAChildOfByStructure(Node<E> node) {
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
     * Change how the structure of the tree is derived.
     *
     * @param treeFormat
     */
    public void setTreeFormat(Format<E> treeFormat) {
        // todo implement this
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

        updates.beginEvent();

        // first toggle the active node. Note that it's visibility does not
        // change, only that of its children
        toExpand.expanded = expanded;
        updates.addUpdate(visibleIndex);

        Node<E> toExpandNextSibling = nextNodeThatsNotAChildOfByStructure(toExpand);

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
                updates.elementDeleted(deleteIndex, descendent.getElement());
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

        // first pass: apply changes to the trees structure, marking all new
        // nodes as hidden. In the next pass we'll figure out parents, siblings
        // and fire events for nodes that shouldn't be hidden
        List<Node<E>> parentsToVerify = new ArrayList<Node<E>>();
        List<Node<E>> changeNodes = new ArrayList<Node<E>>();
        while(listChanges.next()) {

            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                changeNodes.add(findOrInsertNode(sourceIndex));

            } else if(type == ListEvent.UPDATE) {
                Node<E> node = data.get(sourceIndex, REAL_NODES).get();

                // shift as necessary, so if the parents are immediately after, they're used
                if(hasStructurallyChanged(node)) {
                    deleteNode(sourceIndex, parentsToVerify);
                    changeNodes.add(findOrInsertNode(sourceIndex));

                } else {
                    changeNodes.add(node);
                }

            } else if(type == ListEvent.DELETE) {
                deleteNode(sourceIndex, parentsToVerify);
            }
        }

        // now walk through all the changed nodes and attach parents and siblings
        for(Iterator<Node<E>> i = changeNodes.iterator(); i.hasNext(); ) {
            Node<E> changed = i.next();

            attachParentsAndSiblings(changed, true);
            Node<E> next = changed.next();
            if(next != null) {
                attachParentsAndSiblings(next, true);
            }
        }

        // blow away obsolete parent nodes, and their parents recursively
        deleteObsoleteVirtualAncestry(parentsToVerify);

        // still todo:
        // delete nodes that have been collapsed

        assert(isValid());

        updates.commitEvent();
    }

    /**
     * Walk up the tree, fixing parents and siblings of the specified changed
     * node. Performing this fix may require:
     * <li>attaching parents
     * <li>attaching siblings
     * <li>firing an 'insert' event for such parents and siblings
     *
     *
     * @param changed
     */
    private void attachParentsAndSiblings(Node<E> changed, boolean fireEvents) {
        int index = data.indexOfNode(changed.element, ALL_NODES);
        List<Node<E>> pathToRoot = new ArrayList<Node<E>>(changed.pathLength());

        // create a path from the changed node to the root, attaching parents as we go
        {
            Node<E> current = changed;
            Node<E> predecessor = changed.previous(); // at height of current.parent
            Node<E> predecessorAtOurHeight = null; // at height of current
            boolean preexistingParentFound = false;
            while(current != null) {
                int currentPathLength = current.pathLength();
                int predecessorPathLength = predecessor == null ? 0 : predecessor.pathLength();

                // we've already found a connection, keep accumulating the path to root
                if(preexistingParentFound) {
                    pathToRoot.add(current);
                    current = current.parent;

                // the predecessor is too short to be our parent, so create a new parent
                // and hope that the predecessor is our grandparent
                } else if(currentPathLength > predecessorPathLength + 1) {
                    pathToRoot.add(current);
                    Node<E> parent = current.describeParent();
                    if(parent != null) {
                        parent.element = data.add(index, ALL_NODES, HIDDEN_VIRTUAL, parent, 1);
                        attachParent(current, parent, null);
                    }
                    current = parent;

                // the predecessor is too tall to be our parent, maybe its parent is our parent?
                } else if(predecessorPathLength >= currentPathLength) {
                    // the predecessor loses siblings since we split them apart
                    if(predecessor.siblingAfter != null) {
                        predecessor.siblingAfter.siblingBefore = null;
                        predecessor.siblingAfter = null;
                    }
                    predecessorAtOurHeight = predecessor;
                    predecessor = predecessor.parent;

                // sweet! the predecessor node is our parent!
                } else if(isAncestorByValue(current, predecessor)) {
                    pathToRoot.add(current);
                    assert(currentPathLength == predecessorPathLength + 1);
                    attachParent(current, predecessor, predecessorAtOurHeight);
                    // we're mostly done, just fill in the path to root
                    current = current.parent;
                    preexistingParentFound = true;

                // the predecessor node is not our parent, so create a new parent
                // and hope we have common grandparents
                } else {
                    pathToRoot.add(current);
                    assert(currentPathLength == predecessorPathLength + 1);
                    assert(predecessor != null);
                    Node<E> parent = current.describeParent();
                    parent.element = data.add(index, ALL_NODES, HIDDEN_VIRTUAL, parent, 1);
                    attachParent(current, parent, null);
                    current = current.parent;
                    predecessorAtOurHeight = predecessor;
                    predecessor = predecessor.parent;

                }
            }
        }

        // phase two: fix visibility and fire events, going from root down
        boolean visible = true;
        for(int i = pathToRoot.size() - 1; i >= 0; i--) {
            Node<E> current = pathToRoot.get(i);

            // only fire events for visible nodes
            if(visible) {
                // an inserted node
                if(!current.isVisible()) {
                    setVisible(current, true);
                    int visibleIndex = data.indexOfNode(current.element, VISIBLE_NODES);
                    if(fireEvents) {
                        updates.addInsert(visibleIndex);
                    }

                // an updated node
                } else {
                    int visibleIndex = data.indexOfNode(current.element, VISIBLE_NODES);
                    if(fireEvents) {
                        updates.addUpdate(visibleIndex);
                    }
                }
            }

            // collapsed state restricts visibility on child elements
            visible = visible && current.expanded;
        }
    }

    /**
     * Attach the specified parent to the specified node.
     *
     * @param node the node to be attached as parent
     * @param parent the parent node, may be <code>null</code>
     * @param siblingBeforeNode the node immediately before the node of interest
     *      who is a child of the same parent. This will be linked in as the
     *      new node's sibling
     */
    private void attachParent(Node<E> node, Node<E> parent, Node<E> siblingBeforeNode) {
        assert(node != null);
        assert((node.pathLength() == 1 && parent == null) || (node.pathLength() == parent.pathLength() + 1));
        Node<E> originalParent = node.parent;
        node.parent = parent;

        // the nearest child of our parent will become our sibling
        if(siblingBeforeNode != null) {
            assert(siblingBeforeNode.pathLength() == node.pathLength());
            assert(siblingBeforeNode.parent == parent);

            // attach the sibling before
            node.siblingBefore = siblingBeforeNode;
            siblingBeforeNode.siblingAfter = node;
        }

        // todo: resolve the EXPANDED state of these nodes, which may now become
        // visible!

        // attach all siblings after to this new parent
        for(Node<E> siblingAfter = node.siblingAfter; siblingAfter != null; siblingAfter = siblingAfter.siblingAfter) {
            assert(siblingAfter.parent == originalParent);
            siblingAfter.parent = parent;
        }
    }

    /**
     * Handle a source insert at the specified index by adding the corresponding
     * real node, or converting a virtual node to a real node. The real node
     * is inserted, marked as real and hidden, and returned.
     *
     * @param sourceIndex the index of the element in the source list that has
     *      been inserted
     * @return the new node, prior to any events fired
     */
    private Node<E> findOrInsertNode(int sourceIndex) {
        Node<E> inserted = source.get(sourceIndex);

        //  bound the range of indices where this node can be inserted. This is
        // all the virtual nodes between our predecessor and follower in the
        // source list
        int predecessorIndex = sourceIndex > 0 ? data.convertIndexColor(sourceIndex - 1, REAL_NODES, ALL_NODES) : -1;
        int followerIndex = data.size(REAL_NODES) > sourceIndex ? data.convertIndexColor(sourceIndex, REAL_NODES, ALL_NODES) : data.size(ALL_NODES);

        // prepare to search through the virtual nodes, looking for the node with
        // the longest common path with the inserted node
        int indexOfNearestAncestorByValue = predecessorIndex;
        int lengthOfLongestAncestorCommonPath;
        if(predecessorIndex >= 0) {
            Node<E> predecessor = data.get(predecessorIndex, ALL_NODES).get();
            lengthOfLongestAncestorCommonPath = commonPathLength(inserted, predecessor);
        } else {
            lengthOfLongestAncestorCommonPath = 0;
        }

        // search through the virtual nodes, looking for the node with
        // the longest common path with the inserted node
        for(int i = predecessorIndex + 1; i < followerIndex; i++) {
            Node<E> possibleAncestor = data.get(i, ALL_NODES).get();
            assert(possibleAncestor.virtual);

            // what's the longest ancestor that we share
            int commonPathLength = commonPathLength(inserted, possibleAncestor);

            // if the common path is the complete path, then we have a virtual
            // node that has become real.
            if(commonPathLength == inserted.pathLength()) {
                // make the real node copy the state of the virtual
                inserted.updateFrom(possibleAncestor);

                // replace the virtual with the real in the tree structure
                possibleAncestor.element.set(inserted);
                setVirtual(possibleAncestor, false);
                for(Node<E> child = possibleAncestor.firstChild(); child != null; child = child.siblingAfter) {
                    child.parent = inserted;
                }

                return inserted;

            // have we found a new longest common path?
            } else if(commonPathLength > lengthOfLongestAncestorCommonPath) {
                lengthOfLongestAncestorCommonPath = commonPathLength;
                indexOfNearestAncestorByValue = i;
            }
        }

        // insert the node as hidden by default - if we need to show this node,
        // we'll change its state later and fire an 'insert' event then
        Element<Node<E>> element = data.add(indexOfNearestAncestorByValue + 1, ALL_NODES, HIDDEN_REAL, inserted, 1);
        inserted.element = element;
        return inserted;
    }

    /**
     * Remove the node at the specified index, firing all the required
     * notifications.
     */
    private void deleteNode(int sourceIndex, List<Node<E>> parentsToVerify) {
        Node<E> node = data.get(sourceIndex, REAL_NODES).get();

        // if it has children, make it virtual and schedule if for verification or deletion later
        if(!node.isLeaf()) {
            // todo: make setVirtual() actually do node.virtual = virtual
            setVirtual(node, true);
            node.virtual = true;
            parentsToVerify.add(node);

        // otherwise delete it directly
        } else {
            // remove links to this node from siblings
            node.detachSiblings();

            boolean visible = node.isVisible();
            if(visible) {
                int viewIndex = data.indexOfNode(node.element, VISIBLE_NODES);
                updates.elementDeleted(viewIndex, node.getElement());
            }
            data.remove(node.element);
            node.element = null; // null out the element

            // remove the parent if necessary in the next iteration
            parentsToVerify.add(node.parent);
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
    private boolean hasStructurallyChanged(Node<E> node) {
        // our parent should have a length one less than our length
        int parentPathLength = node.parent == null ? 0 : node.parent.pathLength();
        if(parentPathLength != node.pathLength() - 1) {
            return true;
        }

        // our previous parent is still our parent, so nothing has changed structurally
        if(node.parent == null || isAncestorByValue(node, node.parent)) {
            return false;
        }

        // something about our parentage has changed
        return true;
    }

    /**
     * Ensure all of the specified parents are still required. If they're not,
     * they'll be removed and the appropriate events fired.
     */
    private void deleteObsoleteVirtualAncestry(List<Node<E>> parentsToVerify) {
        deleteObsoleteParents:
        for(Iterator<Node<E>> i = parentsToVerify.iterator(); i.hasNext(); ) {
            Node<E> parent = i.next();

            // walk up the tree, deleting nodes
            while(parent != null) {
                // we've reached a real parent, don't delete it!
                if(!parent.virtual) continue deleteObsoleteParents;
                // we've already deleted this parent, we're done
                if(parent.element == null) continue deleteObsoleteParents;

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
                Node<E> predecessor = parent.previous();
                parent.detachSiblings();
                int viewIndex = data.indexOfNode(parent.element, VISIBLE_NODES);
                data.remove(parent.element);
                parent.element = null; // null out the element
                updates.elementDeleted(viewIndex, parent.getElement());

                // handle the merging of the nodes surrounding the deleted node
                deleteRedundantVirtualAncestryAfter(predecessor);

                // now we might need to delete the parent's parent
                parent = parent.parent;
            }
        }
    }

    /**
     * Clean up parents of deleted followers. When two siblings with independent
     * ancestry are joined, we want to delete the redundant virtual parent nodes.
     * For example, consider the initial tree where lowercase values are virtual:
     * <code>
     * /a
     * /a/b
     * /a/b/C
     * /j
     * /j/K
     * /a
     * /a/b
     * /a/b/D
     * </code>
     *
     * <p>If the node <code>/j/K</code> is deleted, then we would otherwise
     * be left with this tree:
     * <code>
     * /a
     * /a/b
     * /a/b/C
     * /a
     * /a/b
     * /a/b/D
     * </code>
     *
     * <p>In this case, there are redundant virtual parents left around. This
     * method removes such parents, leaving the tree in the correct state:
     * <code>
     * /a
     * /a/b
     * /a/b/C
     * /a/b/D
     * </code>
     *
     * <p>This process is only necessary for unsorted trees, but it has no
     * negative side-effects for sorted trees.
     *
     * <p>When merging redundant nodes, sometimes we'll have a conflict
     * between whether the nodes were collapsed or expaneded. The current
     * policy is to have the result expanded if either of the two nodes are
     * expanded.
     *
     * @param beforePossiblyRedundant the node to validate that ancestry after it is
     *     necessary. This is the node immediately before the recently deleted
     *     element.
     */
    private void deleteRedundantVirtualAncestryAfter(Node<E> beforePossiblyRedundant) {
        if(beforePossiblyRedundant == null) return;

        // delete all the redundant nodes after the changed node
        while(true) {
            Node<E> possiblyRedundant = beforePossiblyRedundant.next();

            // if we can't clean this node up, we're done
            if(possiblyRedundant == null
                || !possiblyRedundant.virtual
                || !isAncestorByValue(beforePossiblyRedundant, possiblyRedundant)) {
                return;
            }

            // find the node to merge into
            Node<E> mergeInto = beforePossiblyRedundant.ancestorWithPathLength(possiblyRedundant.pathLength());

            // find the last child of the node to merge into
            Node<E> lastChildBeforeMerge;
            if(beforePossiblyRedundant.pathLength() >= possiblyRedundant.pathLength() + 1) {
                lastChildBeforeMerge = beforePossiblyRedundant.ancestorWithPathLength(possiblyRedundant.pathLength() + 1);
            } else {
                lastChildBeforeMerge = null;
            }

            // merge nodes: expand/collapse
            if(mergeInto.expanded != possiblyRedundant.expanded) {
                // we don't yet know how to merge expanded with unexpanded nodes, but we'll figure out how to soon
                throw new UnsupportedOperationException("TODO!");
            }

            // merge nodes: fix siblings
            mergeInto.siblingAfter = possiblyRedundant.siblingAfter;
            if(possiblyRedundant.siblingAfter != null) {
                possiblyRedundant.siblingAfter.siblingBefore = mergeInto;
            }

            // merge nodes: attach children as siblings
            Node<E> firstChildAfterMerge = possiblyRedundant.firstChild();
            if(firstChildAfterMerge != null) firstChildAfterMerge.siblingBefore = lastChildBeforeMerge;
            if(lastChildBeforeMerge != null) lastChildBeforeMerge.siblingAfter = firstChildAfterMerge;

            // merge nodes: set node.parent on all children
            for(Node<E> child = firstChildAfterMerge; child != null; child = child.siblingAfter) {
                child.parent = mergeInto;
            }

            // merge nodes: delete the redundant node
            if(possiblyRedundant.isVisible()) {
                int deleteIndex = data.indexOfNode(possiblyRedundant.element, VISIBLE_NODES);
                updates.elementDeleted(deleteIndex, possiblyRedundant.getElement());
            }
            data.remove(possiblyRedundant.element);
            possiblyRedundant.element = null; // null out the element
        }
    }

    /**
     * @return the length of the common prefix path between the specified nodes
     */
    private int commonPathLength(Node<E> a, Node<E> b) {
        List<E> aPath = a.path;
        List<E> bPath = b.path;
        int maxCommonPathLength = Math.min(aPath.size(), bPath.size());

        // walk through the paths, looking for the first difference
        for(int i = 0; i < maxCommonPathLength; i++) {
            if(!valuesEqual(i, aPath.get(i), bPath.get(i))) {
                return i;
            }
        }
        return maxCommonPathLength;
    }

    /**
     * @return true if the path of possibleAncestor is a proper prefix of
     *      the path of this node.
     */
    public boolean isAncestorByValue(Node<E> child, Node<E> possibleAncestor) {
        if(possibleAncestor == null) return true;
        List<E> possibleAncestorPath = possibleAncestor.path;

        // this is too long a path to be an ancestor's
        if(possibleAncestorPath.size() >= child.path.size()) return false;

        // make sure the whole trail of the ancestor is common with our trail
        for(int d = possibleAncestorPath.size() - 1; d >= 0; d--) {
            E possibleAncestorPathElement = possibleAncestorPath.get(d);
            E pathElement = child.path.get(d);
            if(!valuesEqual(d, possibleAncestorPathElement, pathElement)) return false;
        }
        return true;
    }

    /**
     * Compare two path elements for equality. The path elements will always
     * have the same depth.
     *
     * @param depth the index of the value in the element's path. For example
     *     if the path is <code>/Users/jessewilson/Desktop/yarbo.mp4</code>,
     *     then depth 3 corresponds to the element <code>yarmo.mp4</code>.
     */
    private boolean valuesEqual(int depth, E a, E b) {
        return nodeComparator.comparator.compare(a, b) == 0;
    }

    private boolean nodesEqualByValue(Node<E> a, Node<E> b) {
        return nodeComparator.compare(a, b) == 0;
    }

    /**
     * Prepare the state of node as it is created.
     */
    private interface NewNodeStateProvider<E> {
        /**
         * Initialize the node. The only legal state to set is whether the
         * node is expanded.
         */
        void initialize(Node<E> value);
    }

    /**
     * The default state provider, that starts all nodes off as expanded.
     */
    private static class DefaultNewNodeStateProvider<E> implements NewNodeStateProvider<E> {
        /** {@inheritDoc} */
        public void initialize(Node<E> value) {
            value.expanded = true;
        }
    }

    /**
     * A node state provider that clones the state from another subtree. This
     * is useful when a node tree gets split.
     */
    private static class CloneStateNewNodeStateProvider<E> implements NewNodeStateProvider<E> {
        private boolean[] expandedStateByDepth;
        /**
         * Save the expanded state of nodes. This is necessary because the state of
         * {@code nodeToPrototype} is subject to change.
         */
        public void setNodeToPrototype(Node<E> nodeToPrototype) {
            expandedStateByDepth = new boolean[nodeToPrototype.pathLength()];

            // walk up the tree to the root, saving ancestor's expanded state
            Node<E> nodeWithDepthD = nodeToPrototype;
            for(int d = expandedStateByDepth.length - 1; d >= 0; d--) {
                expandedStateByDepth[d] = nodeWithDepthD.expanded;
                nodeWithDepthD = nodeWithDepthD.parent;
            }
        }
        /** {@inheritDoc} */
        public void initialize(Node<E> value) {
            value.expanded = expandedStateByDepth[value.pathLength() - 1];
        }
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
    private static <E> NodeComparator<E> comparatorToNodeComparator(Comparator<E> comparator, Format<E> format) {
        return new NodeComparator<E>(comparator, format);
    }

    private static class NodeComparator<E> implements Comparator<Node<E>> {
        private final Comparator<E> comparator;
        private final Format<E> format;

        public NodeComparator(Comparator<E> comparator, Format<E> format) {
            if(comparator == null) throw new IllegalArgumentException();
            this.comparator = comparator;
            this.format = format;
        }

        public int compare(Node<E> a, Node<E> b) {
            int aPathLength = a.path.size();
            int bPathLength = b.path.size();

            // get the effective length, everything but the leaf nodes in the path
            boolean aAllowsChildren = a.virtual || format.allowsChildren(a.path.get(aPathLength - 1));
            boolean bAllowsChildren = b.virtual || format.allowsChildren(b.path.get(bPathLength - 1));
            int aEffectiveLength = aPathLength + (aAllowsChildren ? 0 : -1);
            int bEffectiveLength = bPathLength + (bAllowsChildren ? 0 : -1);

            // compare by value first
            for(int i = 0; i < aEffectiveLength && i < bEffectiveLength; i++) {
                int result = comparator.compare(a.path.get(i), b.path.get(i));
                if(result != 0) return result;
            }

            // and path length second
            return aEffectiveLength - bEffectiveLength;
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
            int pathLength = pathLength();
            // this is a root node, it has no parent
            if(pathLength == 1) return null;
            // return a node describing the parent path
            Node<E> result = new Node<E>();
            result.path = path.subList(0, pathLength - 1);
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

            // weave in the siblings
            siblingBefore = other.siblingBefore;
            if(siblingBefore != null) {
                siblingBefore.siblingAfter = this;
            }
            siblingAfter = other.siblingAfter;
            if(siblingAfter != null) {
                siblingAfter.siblingBefore = this;
            }

            parent = other.parent;
        }

        /**
         * @return <code>true</code> if this node has at least one child node,
         *      according to our structure cache.
         */
        public boolean isLeaf() {
            Node<E> next = next();
            if(next == null) return true;
            return next.parent != this;
        }

        /**
         * @return the node that follows this one, or <code>null</code> if there
         * is no such node.
         */
        public Node<E> next() {
            Element<Node<E>> next = element.next();
            return (next == null) ? null : next.get();
        }

        /**
         * @return the node that precedes this one, or <code>null</code> if there
         * is no such node.
         */
        public Node<E> previous() {
            Element<Node<E>> previous = element.previous();
            return (previous == null) ? null : previous.get();
        }

        /**
         * Get the first child of this node, or <code>null</code> if no
         * such child exists. This is <strong>not</strong> by value, but by the
         * current tree structure.
         */
        private Node<E> firstChild() {
            // the first child is always the node immediately after
            Node<E> possibleChild = next();
            if(possibleChild == null) return null;
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

        /**
         * Lookup the ancestor of this node whose path length is the length
         * specified. For example, the ancestor of path length 2 of the node
         * <code>/Users/jessewilson/Desktop/yarbo.mp4</code> is the node
         * <code>/Users/jessewilson</code>.
         *
         * <p>If the ancestor path length is the same as this node's path length,
         * then this node will be returned.
         */
        public Node<E> ancestorWithPathLength(int ancestorPathLength) {
            assert(pathLength() >= ancestorPathLength);
            Node<E> ancestor = this;
            while(ancestor.pathLength() > ancestorPathLength) {
                ancestor = ancestor.parent;
            }
            return ancestor;
        }

        /**
         * Detach the siblings of the specified node so those siblings remain
         * well-formed in absence of this node.
         */
        private void detachSiblings() {
            // remove myself, linked list style
            if(siblingBefore != null) {
                siblingBefore.siblingAfter = siblingAfter;
            }
            if(siblingAfter != null) {
                siblingAfter.siblingBefore = siblingBefore;
            }
        }

        /** {@inheritDoc} */
        public boolean equals(Object o) {
            if(this == o) return true;
            final Node node = (Node) o;
            return path.equals(node.path);
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return path.hashCode();
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
                if(lastChildSeen != descendent.siblingBefore) {
                    throw new IllegalStateException();
                }
                if(lastChildSeen != null) {
                    if(lastChildSeen.siblingAfter != descendent) {
                        throw new IllegalStateException();
                    }
                }
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
