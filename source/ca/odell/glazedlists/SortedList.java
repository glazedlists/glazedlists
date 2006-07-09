/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTreeIterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.event.ListEvent;

import java.util.*;

/**
 * An {@link EventList} that shows its source {@link EventList} in sorted order.
 *
 * <p>The sorting strategy is specified with a {@link Comparator}. If no
 * {@link Comparator} is specified, all of the elements of the source {@link EventList}
 * must implement {@link Comparable}.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), change comparator O(N log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>72 bytes per element</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=39">39</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=40">40</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=58">58</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=60">60</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=62">62</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=66">66</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=161">161</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=170">170</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=206">206</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=239">239</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=255">255</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=261">261</a>
 * </td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class SortedList<E> extends TransformedList<E,E> {

    private static final byte ALL_COLORS = 1;
    private static final Element EMPTY_ELEMENT = null;

    /**
     * Sorting mode where elements are always in sorted order, even if this
     * requires that elements be moved from one index to another when their
     * value is changed.
     */
    public static final int STRICT_SORT_ORDER = 0;

    /**
     * Sorting mode where elements aren't moved when their value is changed,
     * even if this means they are no longer in perfect sorted order. This mode
     * is useful in editable lists and tables because it is annoying
     * for the current element to move if its value changes.
     */
    public static final int AVOID_MOVING_ELEMENTS = 1;

    /** a map from the unsorted index to the sorted index */
    private SimpleTree<Element> unsorted = null;
    /** a map from the sorted index to the unsorted index */
    private SimpleTree<Element> sorted = null;

    /** the comparator that this list uses for sorting */
    private Comparator<? super E> comparator = null;

    /** one of {@link #STRICT_SORT_ORDER} or {@link #AVOID_MOVING_ELEMENTS}. */
    private int mode = STRICT_SORT_ORDER;


    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}.
     * Because this constructor takes no {@link Comparator} argument, all
     * elements in the specified {@link EventList} must implement {@link Comparable}
     * or a {@link ClassCastException} will be thrown.
     */
    public SortedList(EventList<E> source) {
        this(source, (Comparator<E>)GlazedLists.comparableComparator());
    }

    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}
     * using the specified {@link Comparator} to determine sort order. If the
     * specified {@link Comparator} is <code>null</code>, then this {@link List}
     * will be unsorted.
     */
    public SortedList(EventList<E> source, Comparator<? super E> comparator) {
        super(source);

        setComparator(comparator);

        source.addListEventListener(this);
    }

    /**
     * Modify the behaviour of this {@link SortedList} to one of the predefined modes.
     *
     * @param mode either {@link #STRICT_SORT_ORDER} or {@link #AVOID_MOVING_ELEMENTS}.
     */
    public void setMode(int mode) {
        if(mode != STRICT_SORT_ORDER && mode != AVOID_MOVING_ELEMENTS) throw new IllegalArgumentException("Mode must be either SortedList.STRICT_SORT_ORDER or SortedList.AVOID_MOVING_ELEMENTS");
        if(mode == this.mode) return;

        // apply the new mode
        this.mode = mode;

        // we need to re-sort the table on the off-chance that an element
        // was out of order before
        if(this.mode == STRICT_SORT_ORDER) {
            setComparator(getComparator());
        }
    }
    /**
     * Get the behaviour mode for this {@link SortedList}.
     *
     * @return one of {@link #STRICT_SORT_ORDER} (default) or
     *     {@link #AVOID_MOVING_ELEMENTS}.
     */
    public int getMode() {
        return this.mode;
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        // handle reordering events
        if(listChanges.isReordering()) {
            int[] sourceReorder = listChanges.getReorderMap();

            // remember what the mapping was before
            int[] previousIndexToSortedIndex = new int[sorted.size()];
            int index = 0;
            for(SimpleTreeIterator<Element> i = new SimpleTreeIterator<Element>(sorted); i.hasNext(); index++) {
                i.next();
                Element<Element> unsortedNode = i.value();
                int unsortedIndex = unsorted.indexOfNode(unsortedNode, ALL_COLORS);
                previousIndexToSortedIndex[unsortedIndex] = index;
            }
            // adjust the from index for the source reorder
            int[] newIndexToSortedIndex = new int[sorted.size()];
            for(int i = 0; i < previousIndexToSortedIndex.length; i++) {
                newIndexToSortedIndex[i] = previousIndexToSortedIndex[sourceReorder[i]];
            }

            // reorder the unsorted nodes to get the new sorted order
            Element<Element>[] unsortedNodes = new Element[unsorted.size()];
            index = 0;
            for(SimpleTreeIterator<Element> i = new SimpleTreeIterator<Element>(unsorted); i.hasNext(); index++) {
                i.next();
                Element<Element> unsortedNode = i.node();
                unsortedNodes[index] = unsortedNode;
            }
            Arrays.sort(unsortedNodes, sorted.getComparator());

            // create a new reorder map to send the changes forward
            int[] reorderMap = new int[sorted.size()];
            boolean indexChanged = false;
            index = 0;
            for(SimpleTreeIterator<Element> i = new SimpleTreeIterator<Element>(sorted); i.hasNext(); index++) {
                i.next();
                Element<Element> sortedNode = i.node();
                Element<Element> unsortedNode = unsortedNodes[index];
                sortedNode.set(unsortedNode);
                unsortedNode.set(sortedNode);
                int unsortedIndex = unsorted.indexOfNode(unsortedNode, ALL_COLORS);
                reorderMap[index] = newIndexToSortedIndex[unsortedIndex];
                indexChanged = indexChanged || (index != reorderMap[index]);
            }

            // notify the world of the reordering
            if(indexChanged) {
                updates.beginEvent();
                updates.reorder(reorderMap);
                updates.commitEvent();
            }

            return;
        }

        // This is implemented in three phases. These phases are:
        // 1. Update the unsorted tree for all event types. Update the sorted tree
        //    for delete events by deleting nodes. Fire delete events. Queue unsorted
        //    nodes for inserts and deletes in a list.
        // 2. Fire update events by going through the updated nodes and testing
        //    whether they're still in sort order or if they need to be moved
        // 3. Process queue of unsorted nodes for inserts. Fire insert events.

        // This cycle is rather complex but necessarily so. The reason is that for
        // the two-tree SortedList to function properly, there is a very strict order
        // for how trees can be modified. The unsorted tree must be brought completely
        // up-to-date before any access is made to the sorted tree. This ensures that
        // the unsorted nodes can discover their indices properly. The sorted tree must
        // have all deleted notes removed and updated nodes marked as unsorted
        // before any nodes are inserted. This is because a deleted node may
        // have a changed value that violates the sorted order in the tree. An
        // insert in this case may compare against a violating node and result
        // in inconsistency, even if the other node is eventually deleted.
        // Therefore the order of operations above is essentially update
        // the unsorted tree, delete from the sorted tree and finally insert into the
        // sorted tree.

        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // first update the offset tree for all changes, and keep the changed nodes in a list
        LinkedList<Element> insertNodes = new LinkedList<Element>();
        List<Element<Element>> updateNodes = new ArrayList<Element<Element>>();

        // Update the indexed tree so it matches the source.
        // Save the nodes to be inserted and updated as well
        while(listChanges.next()) {

            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.INSERT) {
                Element<Element> unsortedNode = unsorted.add(unsortedIndex, EMPTY_ELEMENT, 1);
                insertNodes.addLast(unsortedNode);

            // on update, mark the updated node as unsorted and save it so it can be moved
            } else if(changeType == ListEvent.UPDATE) {
                Element<Element> unsortedNode = unsorted.get(unsortedIndex);
                Element sortedNode = unsortedNode.get();
                sortedNode.setSorted(false);
                updateNodes.add(sortedNode);

            // on delete, delete the index and sorted node
            } else if(changeType == ListEvent.DELETE) {
                Element<Element> unsortedNode = unsorted.get(unsortedIndex);
                unsorted.remove(unsortedNode);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updates.addDelete(deleteSortedIndex);

            }
        }

        // fire update events
        for(Iterator<Element<Element>> i = updateNodes.iterator(); i.hasNext(); ) {
            Element<Element> sortedNode = i.next();
            int originalIndex = sorted.indexOfNode(sortedNode, ALL_COLORS);

            // the element is still in sorted order, forward the update event
            if(isNodeInSortedOrder(sortedNode)) {
                sortedNode.setSorted(true);
                updates.addUpdate(originalIndex);

            // sort order is not enforced so we lose perfect sorting order
            // but we don't need to move elements around
            } else if(mode == AVOID_MOVING_ELEMENTS) {
                updates.addUpdate(originalIndex);

            // sort order is enforced so move the element to its new location
            } else {
                sorted.remove(sortedNode);
                updates.addDelete(originalIndex);
                int insertedIndex = insertByUnsortedNode(sortedNode.get());
                updates.addInsert(insertedIndex);
            }
        }

        // fire insert events
        while(!insertNodes.isEmpty()) {
            Element insertNode = insertNodes.removeFirst();
            int insertedIndex = insertByUnsortedNode(insertNode);
            updates.addInsert(insertedIndex);
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Whether this node is greater or equal to its neighbour on the left and
     * less than or equal to its neighbour on the right.
     *
     * <p>This method skips unsorted nodes, whose value should not be compared
     * against when determining tree ordering.
     */
    private boolean isNodeInSortedOrder(Element sortedNode) {
        Comparator comparator = sorted.getComparator();

        // first ensure this node is greater than its predecessors
        for(Element leftNeighbour = sortedNode.previous(); leftNeighbour != null; leftNeighbour = leftNeighbour.previous()) {
            if(!leftNeighbour.isSorted()) continue;
            if(comparator.compare(leftNeighbour.get(), sortedNode.get()) > 0) return false;
            break;
        }

        // then ensure this node is less than its followers
        for(Element rightNeighbour = sortedNode.next(); rightNeighbour != null; rightNeighbour = rightNeighbour.next()) {
            if(!rightNeighbour.isSorted()) continue;
            if(comparator.compare(sortedNode.get(), rightNeighbour.get()) > 0) return false;
            break;
        }

        return true;
    }

    /**
     * Inserts the specified unsorted node as the value in the sorted tree
     * and returns the sorted order.
     *
     * @return the sortIndex of the inserted object.
     */
    private int insertByUnsortedNode(Element unsortedNode) {
        // add the object to the sorted set
        Element<Element> sortedNode = sorted.addInSortedOrder(ALL_COLORS, unsortedNode, 1);
        // assign the unsorted node the value of the sorted node
        unsortedNode.set(sortedNode);
        // return the sorted index
        return sorted.indexOfNode(sortedNode, ALL_COLORS);
    }
    /**
     * Deletes the node in the sorted tree based on the value of the specified
     * unsorted tree node.
     *
     * @return the sortIndex of the deleted object.
     */
    private int deleteByUnsortedNode(Element unsortedNode) {
        // get the sorted node
        Element sortedNode = (Element)unsortedNode.get();
        // look up the sorted index before removing the nodes
        int sortedIndex = sorted.indexOfNode(sortedNode, ALL_COLORS);
        // delete the sorted node from its tree
        sorted.remove(sortedIndex, 1);
        // return the sorted index
        return sortedIndex;
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        Element sortedNode = sorted.get(mutationIndex);
        Element unsortedNode = (Element)sortedNode.get();
        return unsorted.indexOfNode(unsortedNode, ALL_COLORS);
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /**
     * Gets the {@link Comparator} that is being used to sort this list.
     *
     * @return the {@link Comparator} in use, or <tt>null</tt> if this list is
     *      currently unsorted. If this is an {@link EventList} of {@link Comparable}
     *      elements in natural order, then a ComparableComparator} will
     *      be returned.
     */
    public Comparator<? super E> getComparator() {
        return comparator;
    }

    /**
     * Set the {@link Comparator} in use in this {@link EventList}. This will
     * sort the {@link EventList} into a new order.
     *
     * <p>Performance Note: sorting will take <code>O(N * Log N)</code> time.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     *
     * @param comparator the {@link Comparator} to specify how to sort the list. If
     *      the source {@link EventList} elements implement {@link Comparable},
     *      you may use a {@link GlazedLists#comparableComparator()} to sort them
     *      in their natural order. You may also specify <code>null</code> to put
     *      this {@link SortedList} in unsorted order.
     */
    public void setComparator(Comparator<? super E> comparator) {
        // save this comparator
        this.comparator = comparator;
        // keep the old trees to construct the reordering
        SimpleTree previousSorted = sorted;
        // create the sorted list with a simple comparator
        Comparator treeComparator = null;
        if(comparator != null) treeComparator = new ElementComparator(comparator);
        else treeComparator = new ElementRawOrderComparator();
        sorted = new SimpleTree<Element>(treeComparator);

        // create a list which knows the offsets of the indexes to initialize this list
        if(previousSorted == null && unsorted == null) {
            unsorted = new SimpleTree<Element>();
            // add all elements in the source list, in order
            for(int i = 0, n = source.size(); i < n; i++) {
                Element unsortedNode = unsorted.add(i, EMPTY_ELEMENT, 1);
                insertByUnsortedNode(unsortedNode);
            }
            // this is the first sort so we're done
            return;
        }

        // if the lists are empty, we're done
        if(source.size() == 0) return;

        // rebuild the sorted tree to reflect the new Comparator
        for(SimpleTreeIterator<Element> i = new SimpleTreeIterator<Element>(unsorted); i.hasNext(); ) {
            i.next();
            Element unsortedNode = i.node();
            insertByUnsortedNode(unsortedNode);
        }

        // construct the reorder map
        int[] reorderMap = new int[size()];
        int oldSortedIndex = 0;
        for(SimpleTreeIterator<Element> i = new SimpleTreeIterator<Element>(previousSorted); i.hasNext(); oldSortedIndex++) {
            i.next();
            Element oldSortedNode = i.node();
            Element unsortedNode = (Element)oldSortedNode.get();
            Element newSortedNode = (Element)unsortedNode.get();
            int newSortedIndex = sorted.indexOfNode(newSortedNode, ALL_COLORS);
            reorderMap[newSortedIndex] = oldSortedIndex;
        }

        // notification about the big change
        updates.beginEvent();
        updates.reorder(reorderMap);
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        if(mode != STRICT_SORT_ORDER || comparator == null) return source.indexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = ((SimpleTree)sorted).indexOfValue(object, true, false, ALL_COLORS);

        // if we couldn't use the comparator to find the index, return -1
        if (index == -1) return -1;

        // otherwise, we must now begin a linear search for the index of an element
        // that .equals() the given object
        for (; index < size(); index++) {
            E objectAtIndex = get(index);

            // if the objectAtIndex no longer compares equally with the given object, stop the linear search
            if (comparator.compare((E)object, objectAtIndex) != 0) return -1;

            // if the objectAtIndex and object are equal, return the index
            if (GlazedListsImpl.equal(object, objectAtIndex))
                return index;
        }

        // if we fall out of the loop we could not locate the object
        return -1;
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        if(mode != STRICT_SORT_ORDER || comparator == null) return source.lastIndexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = ((SimpleTree)sorted).indexOfValue(object, false, false, ALL_COLORS);

        // if we couldn't use the comparator to find the index, return -1
        if (index == -1) return -1;

        // otherwise, we must now begin a linear search for the index of an element
        // that .equals() the given object
        for(; index > -1; index--) {
            E objectAtIndex = get(index);

            // if the objectAtIndex no longer compares equally with the given object, stop the linear search
            if(comparator.compare((E)object, objectAtIndex) != 0) return -1;

            // if the objectAtIndex and object are equal, return the index
            if(GlazedListsImpl.equal(object, objectAtIndex))
                return index;
        }

        // if we fall out of the loop we could not locate the object
        return -1;
    }

    /**
     * Returns the first index of the <code>object</code>'s sort location or
     * the first index at which the <code>object</code> could be positioned if
     * inserted.
     *
     * <p>Unlike {@link #indexOf} this method does not guarantee the given
     * <code>object</code> {@link Object#equals(Object) equals} the element at
     * the returned index. Instead, they are indistinguishable according to the
     * sorting {@link Comparator}.
     *
     * @return a value in <tt>[0, size()]</tt> inclusive
     */
    public int sortIndex(Object object) {
        if (comparator == null)
            throw new IllegalStateException("No Comparator exists to perform this operation");

        return ((SimpleTree)sorted).indexOfValue(object, true, true, ALL_COLORS);
    }

    /**
     * Returns the last index of the <code>object</code>'s sort location or
     * the last index at which the <code>object</code> could be positioned if
     * inserted.
     *
     * <p>Unlike {@link #lastIndexOf} this method does not guarantee the given
     * <code>object</code> {@link Object#equals(Object) equals} the element at
     * the returned index. Instead, they are indistinguishable according to the
     * sorting {@link Comparator}.
     *
     * @return a value in <tt>[0, size()]</tt> inclusive
     */
    public int lastSortIndex(Object object) {
        if (comparator == null)
            throw new IllegalStateException("No Comparator exists to perform this operation");

        return ((SimpleTree)sorted).indexOfValue(object, false, true, ALL_COLORS);
    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or the index where that element would be in the list if it were
     * inserted.
     *
     * @return the index in this list of the first occurrence of the specified
     *      element, or the index where that element would be in the list if it
     *      were inserted. This will return a value in <tt>[0, size()]</tt>,
     *      inclusive.
     *
     * @deprecated Deprecated as of 12/11/2005. Replaced with {@link #sortIndex(Object)}
     *      which has cleaner semantics.
     */
    public int indexOfSimulated(Object object) {
        return comparator != null ? ((SimpleTree)sorted).indexOfValue(object, true, true, ALL_COLORS) : size();
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        return indexOf(object) != -1;
    }


    /**
     * A comparator that takes an indexed node, and compares the value
     * of an object in a list that has the index of that node.
     *
     * <p>If one of the objects passed to {@link #compare} is not an
     * {@link Element}, it will compare the object directly to the object
     * in the source {@link EventList} referenced by the {@link Element}.
     * This functionality is necessary to allow use of the underlying
     * {@link Comparator} within {@link SimpleTree} to support {@link List#indexOf},
     * {@link List#lastIndexOf}, and {@link List#contains}.
     */
    private class ElementComparator implements Comparator {

        /** the actual comparator used on the values found */
        private Comparator comparator;

        /**
         * Creates an {@link ElementComparator} that compares the
         * objects in the source list based on the indexes of the tree
         * nodes being compared.
         */
        public ElementComparator(Comparator comparator) {
            this.comparator = comparator;
        }

        /**
         * Compares object alpha to object beta by using the source comparator.
         */
        public int compare(Object alpha, Object beta) {
            Object alphaObject = alpha;
            Object betaObject = beta;
            int alphaIndex = -1;
            int betaIndex = -1;
            if(alpha instanceof Element) {
                Element alphaTreeNode = (Element)alpha;
                alphaIndex = unsorted.indexOfNode(alphaTreeNode, ALL_COLORS);
                alphaObject = source.get(alphaIndex);
            }
            if(beta instanceof Element) {
                Element betaTreeNode = (Element)beta;
                betaIndex = unsorted.indexOfNode(betaTreeNode, ALL_COLORS);
                betaObject = source.get(betaIndex);
            }
            int result = comparator.compare(alphaObject, betaObject);
            if(result != 0) return result;
            if(alphaIndex != -1 && betaIndex != -1) return alphaIndex - betaIndex;
            return 0;
        }
    }

    /**
     * A comparator that takes an indexed node, and compares the index of that node.
     */
    private class ElementRawOrderComparator implements Comparator {
        /**
         * Compares the alpha object to the beta object by their indices.
         */
        public int compare(Object alpha, Object beta) {
            try {
                Element alphaTreeNode = (Element)alpha;
                Element betaTreeNode = (Element)beta;
                int alphaIndex = unsorted.indexOfNode(alphaTreeNode, ALL_COLORS);
                int betaIndex = unsorted.indexOfNode(betaTreeNode, ALL_COLORS);
                return alphaIndex - betaIndex;
            } catch(ClassCastException e) {
                System.out.println(alpha.getClass());
                System.out.println(beta.getClass());
                throw e;
            }
        }
    }

    /** {@inheritDoc} */
    public Iterator<E> iterator() {
        return new SortedListIterator();
    }

    /**
     * The fast iterator for SortedList
     */
    private class SortedListIterator implements Iterator<E> {

        /** the SimpleTreeIterator to use to move across the tree */
        private SimpleTreeIterator<Element> treeIterator = new SimpleTreeIterator<Element>(sorted);

        /**
         * Returns true iff there are more value to iterate on by caling next()
         */
        public boolean hasNext() {
            return treeIterator.hasNext();
        }

        /**
         * Returns the next value in the iteration.
         */
        public E next() {
            treeIterator.next();
            Element unsortedNode = treeIterator.value();
            return source.get(unsorted.indexOfNode(unsortedNode, ALL_COLORS));
        }

        /**
         * Removes the last value returned by this iterator.
         */
        public void remove() {
            int indexToRemove = treeIterator.index();
            SortedList.this.source.remove(getSourceIndex(indexToRemove));
            treeIterator = new SimpleTreeIterator(sorted, indexToRemove, ALL_COLORS);
        }
    }
}
