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
 * <p><strong>Developer Preview</strong> this class is still under development
 * and subject to API changes.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeList<E> extends TransformedList<TreeList.Node<E>,E> {

    /** An {@link ExpansionProvider} with a simple policy: all nodes start expanded. */
    public static final ExpansionProvider NODES_START_EXPANDED = new DefaultExpansionProvider(true);
    /** An {@link ExpansionProvider} with a simple policy: all nodes start collapsed. */
    public static final ExpansionProvider NODES_START_COLLAPSED = new DefaultExpansionProvider(false);


    private static final FunctionList.Function NO_OP_FUNCTION = new NoOpFunction();

    /** determines the layout of new nodes as they are created */
    private ExpansionProvider<E> expansionProvider;


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

    /** compare nodes by value */
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
    public TreeList(EventList<E> source, Format<E> format, Comparator<E> comparator, ExpansionProvider<E> expansionProvider) {
        this(source, format, comparatorToNodeComparator(comparator, format), null, expansionProvider);
    }
    /** hack extra Comparator so we can build the nodeComparator only once. */
    private TreeList(EventList<E> source, Format<E> format, NodeComparator<E> nodeComparator, Void unusedParameterForSortFirst, ExpansionProvider<E> expansionProvider) {
        // TODO: store a reference to FunctionList, so we can dispose() it later!
        this(new SortedList<Node<E>>(new FunctionList<E, Node<E>>(source, new ElementToTreeNodeFunction<E>(format), NO_OP_FUNCTION), nodeComparator), format, nodeComparator, expansionProvider);
    }

    /** @deprecated */
    public TreeList(EventList<E> source, Format<E> format) {
        this(source, format, NODES_START_EXPANDED);
    }
    /** @deprecated */
    public TreeList(EventList<E> source, Format<E> format, Comparator<E> comparator) {
        this(source, format, comparator, NODES_START_EXPANDED);
    }


    /**
     * Create a new TreeList that adds hierarchy to the specified source list.
     * This constructor does not sort the elements.
     */
    public TreeList(EventList<E> source, Format<E> format, ExpansionProvider<E> expansionProvider) {
        this(new FunctionList<E, Node<E>>(source, new ElementToTreeNodeFunction<E>(format), NO_OP_FUNCTION), format, comparatorToNodeComparator((Comparator)GlazedLists.comparableComparator(), format), expansionProvider);
    }
    /** master Constructor that takes a FunctionList or a SortedList(FunctionList) */
    private TreeList(EventList<Node<E>> source, Format<E> format, NodeComparator<E> nodeComparator, ExpansionProvider<E> expansionProvider) {
        super(source);
        this.format = format;
        this.nodeComparator = nodeComparator;
        this.expansionProvider = expansionProvider;

        // insert the new elements like they were adds
        NodeAttacher nodeAttacher = new NodeAttacher(false);
        for(int i = 0; i < super.source.size(); i++) {
            Node<E> node = super.source.get(i);
            addNode(node, HIDDEN_REAL, expansionProvider, i);
            nodeAttacher.nodesToAttach.appendNewlyInserted(node);
        }
        // attach siblings and parent nodes
        nodeAttacher.attachAll();

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
     * Get the size of the subtree at the specified index, counting nodes
     * of the specified types only.
     *
     * @param index either a visible or invisible index of the node in the tree
     *      whose subtree size is to be measured
     * @param indexIsVisibleIndex <code>true</code> if index is a visible index,
     *      false if it's an overall index
     * @param includeCollapsedNodes <code>true</code> if the result should include
     *      nodes not visible in the fully-expanded tree, <code>false</code> if the
     *      result should be restricted to only those nodes that are visible.
     */
    private int subtreeSize(int index, boolean indexIsVisibleIndex, boolean includeCollapsedNodes) {
        byte coloursIn = indexIsVisibleIndex ? VISIBLE_NODES : ALL_NODES;
        byte coloursOut = includeCollapsedNodes ? ALL_NODES : VISIBLE_NODES;

        // the node whose subtree is being measured
        Node<E> node = data.get(index, coloursIn).get();

        // get the index in terms of collapsed nodes
        int indexOut;
        if(coloursIn == coloursOut) {
            indexOut = index;
        } else {
            // we can't get the collapsed subtree size of a collapsed node
            assert((node.element.getColor() & coloursOut) != 0);
            indexOut = data.convertIndexColor(index, coloursIn, coloursOut);
        }

        // find the next node that's not a child to find the delta
        Node<E> nextNodeNotInSubtree = nextNodeThatsNotAChildOfByStructure(node);

        // if we don't have a sibling after us, we've hit the end of the tree
        if(nextNodeNotInSubtree == null) {
            return data.size(coloursOut) - indexOut;
        }

        return data.indexOfNode(nextNodeNotInSubtree.element, coloursOut) - indexOut;
    }

    /**
     * The number of nodes including the node itself in its subtree.
     */
    public int subtreeSize(int visibleIndex, boolean includeCollapsed) {
        return subtreeSize(visibleIndex, true, includeCollapsed);
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
        boolean hasChildren = subtreeSize(visibleIndex, true) > 1;
        boolean isLeaf = getTreeNode(visibleIndex).isLeaf();
        if(isLeaf == hasChildren) {
            subtreeSize(visibleIndex, true, true);
        }
        return hasChildren;
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
        // todo: make this fast
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
        expansionProvider.setExpanded(toExpand.getElement(), toExpand.path, expanded);
        setExpanded(toExpand, expanded);
    }

    /**
     * Internally control the expanded/collapsed state of a node without
     * reporting the change to the external {@llink ExpansionProvider}. This
     * is useful when expand/collapsed state changes as nodes are split and
     * merged due to tree structure changes.
     */
    private void setExpanded(Node<E> toExpand, boolean expanded) {

        // if we're already in the desired state, give up!
        if(toExpand.expanded == expanded) return;

        // toggle the active node.
        toExpand.expanded = expanded;

        // whether the entire subtree, including the specified node, is visible
        boolean subtreeIsVisible = (toExpand.element.getColor() & VISIBLE_NODES) != 0;

        // only fire events and change deeper elements if the subtree is showing
        if(subtreeIsVisible) {
            updates.beginEvent();

            // This node's visibility does not change, only that of its children
            if(toExpand.isVisible()) {
                int visibleIndex = data.indexOfNode(toExpand.element, VISIBLE_NODES);
                updates.addUpdate(visibleIndex);
            }

            Node<E> toExpandNextSibling = nextNodeThatsNotAChildOfByStructure(toExpand);

            // walk through the subtree, looking for all the descendents we need
            // to change. As we encounter them, change them and fire events
            for(Node<E> descendent = toExpand.next(); descendent != null && descendent != toExpandNextSibling; descendent = descendent.next()) {
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
            updates.commitEvent();
        }

        assert(isValid());
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
            newColor = node.isVisible() ? VISIBLE_REAL : HIDDEN_REAL;
        }
        data.setColor(node.element, newColor);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<Node<E>> listChanges) {
        updates.beginEvent(true);

        // first pass: apply changes to the trees structure, marking all new
        // nodes as hidden. In the next pass we'll figure out parents, siblings
        // and fire events for nodes that shouldn't be hidden
        List<Node<E>> nodesToVerify = new ArrayList<Node<E>>();
        NodeAttacher nodeAttacher = new NodeAttacher(true);
        while(listChanges.next()) {
            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                Node<E> inserted = findOrInsertNode(sourceIndex);
                nodeAttacher.nodesToAttach.appendNewlyInserted(inserted);

            } else if(type == ListEvent.UPDATE) {
                Node<E> node = data.get(sourceIndex, REAL_NODES).get();

                // shift as necessary, so if the parents are immediately after, they're used
                if(hasStructurallyChanged(node)) {
                    deleteAndDetachNode(sourceIndex, nodesToVerify);
                    Node<E> updated = findOrInsertNode(sourceIndex);
                    nodeAttacher.nodesToAttach.appendNewlyInserted(updated);

                } else {
                    nodeAttacher.nodesToAttach.appendNewlyInserted(node);
                }

            } else if(type == ListEvent.DELETE) {
                deleteAndDetachNode(sourceIndex, nodesToVerify);
            }
        }

        // blow away obsolete virtual leaf nodes now that we can know for sure
        // if they're obsolete this doesn't depend on the siblings & parents to be attached
        deleteObsoleteVirtualLeaves(nodesToVerify);

        // second pass: walk through all the changed nodes and attach parents
        // and siblings, plus fire events for all the inserted or updated nodes.
        nodeAttacher.attachAll();

        // blow away obsolete parent nodes now that we can know for sure if they're obsolete
        deleteObsoleteVirtualParents(nodesToVerify);

        assert(isValid());

        updates.commitEvent();
    }

    /**
     * A transient helper object to attach nodes to sibling and parent nodes.
     *
     * <p>Because this class is stateful, it shouldn't be referenced across changes
     * as that risks a memory leak.
     */
    private class NodeAttacher {

        private final boolean fireEvents;

        /** the queue of nodes needing parents and siblings attached */
        private final NodesToAttach nodesToAttach = new NodesToAttach();

        /** the node having its parents and siblings attached */
        private Node<E> current;

        /** the active node before current which we're hoping is current's parent */
        private Node<E> predecessor;

        /** the active node before current which we're hoping is current's sibling */
        private Node<E> predecessorAtOurHeight;

        /** the path from the changed node to the root, used to fire events in the second phase */
        private List<Node<E>> pathToRoot = new ArrayList<Node<E>>();

        /** the index of the changed node, which is where parent nodes shall be inserted */
        private int index;

        /** provide expand/collapsed state for nodes that are inserted or split */
        private ExpansionProvider<E> expansionProvider;

        public NodeAttacher(boolean fireEvents) {
            this.fireEvents = fireEvents;
        }

        public void attachAll() {
            while(!nodesToAttach.isEmpty()) {
                Node<E> changed = nodesToAttach.removeFirst();
                boolean newlyInserted = nodesToAttach.isNewlyInserted(changed);
                attach(changed, newlyInserted);
            }
        }

        /**
         * Walk up the tree, fixing parents and siblings of the specified changed
         * node. Performing this fix may require:
         * <li>attaching parents
         * <li>attaching siblings
         * <li>firing an 'insert' event for such parents and siblings
         *
         * @param changed the node to attach parents and siblings to
         * @param newlyInserted <code>true</code> if this node is a brand-new
         *      node in the tree, or <code>false</code> if we're reattaching an
         *      existing node. This has significant consequences for the logic that
         *      manages expanded state and repairing siblings.
         * @return <code>true</code> if changes were made to the tree
         */
        private void attach(Node<E> changed, boolean newlyInserted) {
            current = changed;

            // prepare the expand/collapsed state of created nodes
            if(newlyInserted) {
                expansionProvider = TreeList.this.expansionProvider;
            } else {
                expansionProvider = new CloneStateNewNodeStateProvider<E>(current);
            }

            // prepare state for attaching the node
            index = data.indexOfNode(current.element, ALL_NODES);
            predecessor = current.previous();
            predecessorAtOurHeight = null;

            // make sure the following node is repaired, as it might have become
            // separated from its siblings or parent by the insertion of this node
            if(newlyInserted) {
                Node<E> follower = current.next();
                if(follower != null) {
                    nodesToAttach.prependPotentiallySplit(follower);
                }
            }

            attachParentsAndSiblings();
            fixVisibilityAndFireEvents();

            // cleanup
            pathToRoot.clear();
        }

        private void attachParentsAndSiblings() {
            boolean preexistingParentFound = false;
            while(current != null) {
                int currentPathLength = current.pathLength();
                int predecessorPathLength = predecessor == null ? 0 : predecessor.pathLength();

                // we've already found a connection, keep accumulating the path to root
                if(preexistingParentFound) {
                    incrementCurrent();

                // the predecessor is too short to be our parent, so create a new parent
                // and hope that the predecessor is our grandparent
                } else if(currentPathLength > predecessorPathLength + 1) {
                    createAndAttachParent();

                // the predecessor is too tall to be our parent, maybe its parent is our parent?
                } else if(predecessorPathLength >= currentPathLength) {
                    // make sure our predecessor's sibling has parents reattached if necessary
                    incrementPredecessor();

                // sweet! the predecessor node is our parent!
                } else if(isAncestorByValue(current, predecessor)) {
                    assert(currentPathLength == predecessorPathLength + 1);
                    attachParent(predecessor, predecessorAtOurHeight);
                    // we're mostly done, just fill in the path to root
                    preexistingParentFound = true;

                // the predecessor node is not our parent, so create a new parent
                // and hope we have common grandparents
                } else {
                    assert(currentPathLength == predecessorPathLength + 1);
                    assert(predecessor != null);
                    createAndAttachParent();
                    // make sure our predecessor's sibling has parents reattached if necessary
                    incrementPredecessor();

                }
            }
        }

        private void incrementCurrent() {
            pathToRoot.add(current);
            current = current.parent;
        }

        private void incrementPredecessor() {
            if(predecessor.siblingAfter != null && predecessor.siblingAfter != current) {
                nodesToAttach.addInOrder(predecessor.siblingAfter);
                predecessor.siblingAfter.siblingBefore = null;
                predecessor.siblingAfter = null;
            }
            predecessorAtOurHeight = predecessor;
            predecessor = predecessor.parent;
        }


        /**
         * Attach the default parent for the specified node, using the node's
         * ability to describe a prototype for its parent object.
         */
        private void createAndAttachParent() {
            Node<E> parent = current.describeParent();
            if(parent != null) {
                addNode(parent, HIDDEN_VIRTUAL, expansionProvider, index);
            }
            attachParent(parent, null);
        }

        /**
         * Attach the specified parent to the specified node.
         *
         * @param parent the parent node, may be <code>null</code>
         * @param siblingBeforeNode the node immediately before the node of interest
         *      who is a child of the same parent. This will be linked in as the
         *      new node's sibling. If <code>null</code>, no linking/unlinking will
         *      be performed.
         */
        private void attachParent(Node<E> parent, Node<E> siblingBeforeNode) {
            assert(current != null);
            assert((current.pathLength() == 1 && parent == null) || (current.pathLength() == parent.pathLength() + 1));

            // attach the siblings, the nearest child of our parent will become our sibling
            // if it isn't already
            if(siblingBeforeNode != null && siblingBeforeNode.siblingAfter != current) {
                if(siblingBeforeNode.pathLength() != current.pathLength()) {
                    throw new IllegalStateException();
                }
                assert(siblingBeforeNode.parent == parent);

                if(siblingBeforeNode.siblingAfter != null) {
                    assert(current.siblingAfter == null);
                    current.siblingAfter = siblingBeforeNode.siblingAfter;
                    siblingBeforeNode.siblingAfter.siblingBefore = current;
                }
                current.siblingBefore = siblingBeforeNode;
                siblingBeforeNode.siblingAfter = current;

                assert(current.siblingBefore != current);
                assert(current.siblingAfter != current);
            }

            // attach the new parent to this and siblings after. This is necessary when
            // siblings have been split from their previous parent
            for(Node<E> currentSibling = current; currentSibling != null; currentSibling = currentSibling.siblingAfter) {
                currentSibling.parent = parent;
            }

            // now the current node has shifted up to the parent node
            incrementCurrent();
        }

        /**
         * Fire events for the recently changed node, going from root down
         */
        private void fixVisibilityAndFireEvents() {
            boolean visible = true;
            for(int i = pathToRoot.size() - 1; i >= 0; i--) {
                current = pathToRoot.get(i);

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
    }

    /**
     * A list of nodes to be attached to their parent nodes and siblings, provided
     * in increasing order by index.
     *
     * <p>This queue is necessary because our algorithm encounters nodes in random
     * order but must process them in increasing order.
     */
    private final class NodesToAttach {
        private final List<Node<E>> nodes = new ArrayList<Node<E>>();
        private final NodeIndexComparator nodeIndexComparator = new NodeIndexComparator();
        private final Map<Node<E>,Boolean> newlyInsertedNodes = new IdentityHashMap<Node<E>,Boolean>();

        private void addInOrder(Node<E> node) {
            int position = Collections.binarySearch(nodes, node, nodeIndexComparator);
            if(position >= 0) return;
            nodes.add(-position - 1, node);
            assert(isValid());
        }

        private void prependPotentiallySplit(Node<E> node) {
            if(!nodes.isEmpty()) {
                if(nodes.get(0) == node) {
                    return;
                }
                assert(nodeIndexComparator.compare(nodes.get(0), node) >= 0);
            }
            nodes.add(0, node);
            assert(isValid());
        }

        private void appendNewlyInserted(Node<E> node) {
            assert(nodes.isEmpty() || nodeIndexComparator.compare(nodes.get(nodes.size() - 1), node) < 0);
            nodes.add(node);
            newlyInsertedNodes.put(node, Boolean.TRUE);
            assert(isValid());
        }

        private boolean isEmpty() {
            return nodes.isEmpty();
        }

        private Node<E> removeFirst() {
            return nodes.remove(0);
        }

        private boolean isNewlyInserted(Node<E> node) {
            return newlyInsertedNodes.containsKey(node);
        }

        private boolean isValid() {
            for(int i = 0; i < nodes.size() - 1; i++) {
                Node<E> a = nodes.get(i);
                Node<E> b = nodes.get(i + 1);
                assert(nodeIndexComparator.compare(a, b) <= 0);
            }
            return true;
        }

        /**
         * Compare two nodes by their position in the tree.
         */
        private final class NodeIndexComparator implements Comparator<Node<E>> {
            public int compare(Node<E> a, Node<E> b) {
                if(a.element == null || b.element == null) {
                    throw new IllegalStateException();
                }

                return data.indexOfNode(a.element, ALL_NODES) - data.indexOfNode(b.element, ALL_NODES);
            }
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
        inserted.reset();

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
            // if the virtual node is deeper than the inserted node, it isn't be an ancestor
            if(possibleAncestor.pathLength() > inserted.pathLength()) continue;

            // what's the longest ancestor that we share
            int commonPathLength = commonPathLength(inserted, possibleAncestor);

            // if the common path is the complete path, then we have a virtual
            // node that has become real.
            if(commonPathLength == inserted.pathLength()) {
                replaceNode(possibleAncestor, inserted, false);
                return inserted;

            // have we found a new longest common path?
            } else if(commonPathLength > lengthOfLongestAncestorCommonPath) {
                lengthOfLongestAncestorCommonPath = commonPathLength;
                indexOfNearestAncestorByValue = i;
            }
        }

        // insert the node as hidden by default - if we need to show this node,
        // we'll change its state later and fire an 'insert' event then
        // TODO(jessewilson): do we need to worry about parent nodes being initialized incorrectly?
        addNode(inserted, HIDDEN_REAL, expansionProvider, indexOfNearestAncestorByValue + 1);
        return inserted;
    }


    /**
     * Remove the node at the specified index, firing all the required
     * notifications.
     */
    private void deleteAndDetachNode(int sourceIndex, List<Node<E>> nodesToVerify) {
        Node<E> node = data.get(sourceIndex, REAL_NODES).get();

        // if it has children, replace it with a virtual copy and schedule that for verification
        if(!node.isLeaf()) {
            Node<E> replacement = new Node<E>(node.virtual, new ArrayList<E>(node.path()));
            replaceNode(node, replacement, true);

            nodesToVerify.add(replacement);

        // otherwise delete it directly
        } else {
            Node<E> follower = node.next();

            deleteNode(node);

            // remove the parent if necessary in the next iteration
            nodesToVerify.add(node.parent);

            // also remove the follower - it may have become redundant as well
            if(follower != null && follower.virtual) nodesToVerify.add(follower);
        }
    }

    /**
     * Replace the before node with the replacement node, updating the tree
     * structure completely. This is used to replace a virtual node with a real
     * one or vice versa.
     */
    private void replaceNode(Node<E> before, Node<E> after, boolean virtual) {
        assert(before.pathLength() == after.pathLength());
        after.expanded = before.expanded;
        // change parent and children
        after.parent = before.parent;
        for(Node<E> child = before.firstChild(); child != null; child = child.siblingAfter) {
            assert(child.parent == before);
            child.parent = after;
        }
        // change siblings
        if(before.siblingAfter != null) {
            after.siblingAfter = before.siblingAfter;
            after.siblingAfter.siblingBefore = after;
        }
        if(before.siblingBefore != null) {
            after.siblingBefore = before.siblingBefore;
            after.siblingBefore.siblingAfter = after;
        }
        // change element
        after.element = before.element;
        after.element.set(after);
        // change virtual
        setVirtual(after, virtual);
        after.virtual = virtual;
        // mark the original as obsolete
        before.element = null;
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
     * Delete all virtual parents that no longer have child nodes attached.
     * This method does not depend upon child nodes being properly configured.
     */
    private void deleteObsoleteVirtualLeaves(List<Node<E>> nodesToVerify) {
        deleteObsoleteLeaves:
        for(Iterator<Node<E>> i = nodesToVerify.iterator(); i.hasNext(); ) {
            Node<E> node = i.next();

            // walk up the tree, deleting nodes
            while(node != null) {
                // we've reached a real parent, don't delete it!
                if(!node.virtual) continue deleteObsoleteLeaves;
                // we've already deleted this parent, we're done
                if(node.element == null) continue deleteObsoleteLeaves;
                // this node now has children, don't delete it
                // todo: create a test case where isLeaf fails, instead we want hasChildByValue() or something
                // todo: that might still pass, because the parent will be recreated. In that case we should
                // todo: cleverly make sure the parent node's expand/collapse state is unchanged
                if(!node.isLeaf()) continue deleteObsoleteLeaves;

                // if this virtual node has no children, then it's obsolete and
                // we can delete it right away. Afterwards, we might need to
                // delete that node's parent
                deleteNode(node);
                node = node.parent;
            }
        }
    }

    /**
     * Remove obsolete virtual parents where there's another equal parent
     * earlier in the tree. This can happen when sibling nodes are united by
     * the deletion of an intervening node. For example, consider the following
     * tree, where lowercase values are virtual:
     * <code>
     * /a
     * /a/b
     * /a/b/C
     * /Z
     * /a
     * /a/b
     * /a/b/D
     * </code>
     *
     * <p>If the node <code>/Z</code> is deleted, then the second set of
     * <code>/a</code> and <code>/a/b</code> nodes are now redundant. In this
     * case, we must delete those as well.
     *
     * <p>Because it depends upon ancestry being properly configured, this method may
     * only be executed after the tree's parent and sibling nodes have been attached.
     */
    private void deleteObsoleteVirtualParents(List<Node<E>> nodesToVerify) {
        deleteObsoleteParents:
        for(Iterator<Node<E>> i = nodesToVerify.iterator(); i.hasNext(); ) {
            Node<E> node = i.next();

            // walk up the tree, deleting nodes
            while(node != null) {
                // we've reached a real parent, don't delete it!
                if(!node.virtual) continue deleteObsoleteParents;
                // we've already deleted this parent, we're done
                if(node.element == null) continue deleteObsoleteParents;

                // todo: come up with a test case where previous pathlength == parent pathlength,
                // which will cause this to fail (slightly) because the expanded state will be destroyed
                Node<E> previous = node.previous();
                if(previous == null) continue deleteObsoleteParents;
                if(!isAncestorByValue(previous, node)) continue deleteObsoleteParents;

                // if this virtual parent is redundant, then we can delete it
                node = deleteVirtualAncestryRootDown(previous, node);
            }
        }
    }
    private Node<E> deleteVirtualAncestryRootDown(Node<E> previous, Node<E> parent) {
        Node<E> replacementLastSibling = previous.ancestorWithPathLength(parent.pathLength() + 1);
        assert(replacementLastSibling.siblingAfter == null);
        Node<E> replacement = replacementLastSibling.parent;

        // merge expand/collapse state first
        if(replacement.expanded && !parent.expanded) {
            setExpanded(parent, true);
        } else if(parent.expanded && !replacement.expanded) {
            setExpanded(replacement, true);
        }

        // link the children of the two parents as siblings
        Node<E> parentFirstChild = parent.firstChild();
        assert(parentFirstChild == null || parentFirstChild.siblingBefore == null);
        replacementLastSibling.siblingAfter = parentFirstChild;
        if(parentFirstChild != null) parentFirstChild.siblingBefore = replacementLastSibling;

        // point all children at the new parent
        for(Node<E> child = parentFirstChild; child != null; child = child.siblingAfter) {
            child.parent = replacement;
        }

        // remove the parent itself
        deleteNode(parent);

        // next up for potential deletion is the child of this parent
        return parentFirstChild;
    }

    /**
     * Delete the actual node, without unlinking children or unlinking siblings,
     * which must be handled externally.
     */
    private void deleteNode(Node<E> node) {
        // remove links to this node from siblings
        node.detachSiblings();

        boolean visible = node.isVisible();
        if(visible) {
            int viewIndex = data.indexOfNode(node.element, VISIBLE_NODES);
            updates.elementDeleted(viewIndex, node.getElement());
        }
        data.remove(node.element);
        node.element = null; // null out the element
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

    /**
     * Provide the expand/collapse state of nodes.
     *
     * <p>This interface will be consulted when nodes are inserted. Whenever
     * the expanded/collapsed state of an element is changed, this provider
     * is notified. It is not strictly necessary for implementors to record the
     * expand/collapsed state of all nodes, since {@link TreeList} caches
     * node state internally.
     */
    public interface ExpansionProvider<E> {
        /**
         * Determine the specified element's initial expand/collapse state.
         *
         * @param element the newly inserted  (or unfiltered etc.) value
         * @param path the tree path of the element, from root to the value.
         * @return <code>true</code> if the specified node's children shall be
         *      visible, or <code>false</code> if they should be hidden.
         */
        boolean isExpanded(E element, List<E> path);

        /**
         * Notifies this handler that the specified element's expand/collapse
         * state has changed.
         */
        void setExpanded(E element, List<E> path, boolean expanded);
    }

    /**
     * The default state provider, that starts all nodes off as expanded.
     */
    private static class DefaultExpansionProvider<E> implements ExpansionProvider<E> {
        private boolean expanded;
        public DefaultExpansionProvider(boolean expanded) {
            this.expanded = expanded;
        }
        public boolean isExpanded(E element, List<E> path) {
            return expanded;
        }
        public void setExpanded(E element, List<E> path, boolean expanded) {
            // do nothing
        }
    }

    /**
     * Prepare the state of the node and insert it into the datastore. It will
     * still be necessary to attach parent and sibling nodes.
     */
    private void addNode(Node<E> node, byte nodeColor, ExpansionProvider<E> expansionProvider, int realIndex) {
        node.expanded = expansionProvider.isExpanded(node.getElement(), node.path);
        node.element = data.add(realIndex, ALL_NODES, nodeColor, node, 1);
    }

    /**
     * A node state provider that clones the state from another subtree. This
     * is useful when a node tree gets split.
     */
    private static class CloneStateNewNodeStateProvider<E> implements ExpansionProvider<E> {
        private boolean[] expandedStateByDepth;
        /**
         * Save the expanded state of nodes. This is necessary because the state of
         * {@code nodeToPrototype} is subject to change.
         */
        public CloneStateNewNodeStateProvider(Node<E> nodeToPrototype) {
            expandedStateByDepth = new boolean[nodeToPrototype.pathLength()];

            // walk up the tree to the root, saving ancestor's expanded state
            Node<E> nodeWithDepthD = nodeToPrototype;
            for(int d = expandedStateByDepth.length - 1; d >= 0; d--) {
                if(nodeWithDepthD != null) {
                    expandedStateByDepth[d] = nodeWithDepthD.expanded;
                    nodeWithDepthD = nodeWithDepthD.parent;
                } else {
                    expandedStateByDepth[d] = true;
                }
            }
        }
        public boolean isExpanded(E element, List<E> path) {
            return expandedStateByDepth[path.size() - 1];
        }
        public void setExpanded(E element, List<E> path, boolean expanded) {
            // do nothing
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
        public ElementToTreeNodeFunction(Format<E> format) {
            this.format = format;
        }
        public Node<E> evaluate(E sourceValue) {
            // populate the path using the working path as a temporary variable
            List<E> path = new ArrayList<E>();
            format.getPath(path, sourceValue);
            return new Node<E>(false, path);
        }

        public Node<E> reevaluate(E sourceValue, Node<E> transformedValue) {
            transformedValue.path.clear();
            format.getPath(transformedValue.path, sourceValue);
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
    public static final class Node<E> {

        private final List<E> path;

        /** true if this node isn't in the source list */
        private boolean virtual;

        /** true if this node's children should be visible */
        private boolean expanded;

        /** the element object points back at this, for the tree's structure cache */
        private Element<Node<E>> element;

        /** the relationship of this node to others */
        private Node<E> siblingAfter;
        private Node<E> siblingBefore;
        private Node<E> parent;

        /**
         * Construct a new node.
         *
         * @param virtual <code>true</code> if this node is initially virtual
         * @param path the tree path from root to value that this node represents. It
         *      is an error to mutate this path once it has been provided to a node.
         */
        private Node(boolean virtual, List<E> path) {
            reset();
            this.virtual = virtual;
            this.path = path;
        }

        /**
         * Clean up this node for insertion into the tree. It might have some
         * residual state due to a previous location in the tree, in the event
         * that the node was subject to a reordering event.
         */
        private void reset() {
            virtual = false;
            expanded = true;
            element = null;
            siblingAfter = null;
            siblingBefore = null;
            parent = null;
        }

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
            return new Node<E>(true, new ArrayList<E>(path.subList(0, pathLength - 1)));
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
        private Node<E> next() {
            Element<Node<E>> next = element.next();
            return (next == null) ? null : next.get();
        }

        /**
         * @return the node that precedes this one, or <code>null</code> if there
         * is no such node.
         */
        private Node<E> previous() {
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
        private Node<E> ancestorWithPathLength(int ancestorPathLength) {
            assert(pathLength() >= ancestorPathLength);
            Node<E> ancestor = this;
            while(ancestor.pathLength() > ancestorPathLength) {
                ancestor = ancestor.parent;
                if(ancestor == null) {
                    throw new IllegalStateException();
                }
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
            super((EventList) TreeList.this);
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

    /**
     * @return true if the visibility of this node is what it's parents prescribe.
     */
    private boolean isVisibilityValid(Node<E> node) {
        boolean expectedVisible = true;
        for(Node<E> ancestor = node.parent; ancestor != null; ancestor = ancestor.parent) {
            if(!ancestor.expanded) {
                expectedVisible = false;
                break;
            }
        }

        return node.isVisible() == expectedVisible;
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

            // the node's visibility should be consistent with its parent nodes expanded state
            assert(isVisibilityValid(node));

            // the virtual flag should be consistent with the node color
            if(node.virtual) {
                assert(node.element.getColor() == HIDDEN_VIRTUAL|| node.element.getColor() == VISIBLE_VIRTUAL);
            } else {
                if(source.get(data.convertIndexColor(i, ALL_NODES, REAL_NODES)) != node) {
                    throw new IllegalStateException();
                }
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
        int size = subtreeSize(index, false, true);

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
                    if(lastChildSeen.pathLength() != descendent.pathLength()) {
                        throw new IllegalStateException();
                    }
                } else {
                    if(descendent.siblingBefore != null) {
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
