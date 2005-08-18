/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.IndexedTree;
import ca.odell.glazedlists.impl.adt.IndexedTreeNode;
import ca.odell.glazedlists.impl.adt.IndexedTreeIterator;
import ca.odell.glazedlists.impl.sort.ComparableComparator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.util.concurrent.InternalReadWriteLock;
import ca.odell.glazedlists.util.concurrent.LockFactory;

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
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N), change comparator O(N log N)</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>72 bytes per element</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SortedList<E> extends TransformedList<E,E> {

    /** a map from the unsorted index to the sorted index */
    private IndexedTree<IndexedTreeNode> unsorted = null;
    /** a map from the sorted index to the unsorted index */
    private IndexedTree<IndexedTreeNode> sorted = null;

    /** the comparator that this list uses for sorting */
    private Comparator comparator = null;

    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}.
     * Because this constructor takes no {@link Comparator} argument, all
     * elements in the specified {@link EventList} must implement {@link Comparable}
     * or a {@link ClassCastException} will be thrown.
     */
    public SortedList(EventList<E> source) {
        this(source, GlazedLists.comparableComparator());
    }

    /**
     * Creates a {@link SortedList} that sorts the specified {@link EventList}
     * using the specified {@link Comparator} to determine sort order. If the
     * specified {@link Comparator} is <tt>null</tt>, then this list will be
     * unsorted.
     */
    public SortedList(EventList<E> source, Comparator comparator) {
        super(source);

        // use an Internal Lock to avoid locking the source list during a sort
        readWriteLock = new InternalReadWriteLock(source.getReadWriteLock(),
			LockFactory.DEFAULT.createReadWriteLock());

        // trees are instansiated when a comparator is set
        setComparator(comparator);

        source.addListEventListener(this);
    }

    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        // This is implemented in four phases. These phases are:
        // 1. Update the unsorted tree for all event types. Update the sorted tree
        //    for delete events by deleting nodes. Fire delete events. Queue unsorted
        //    nodes for inserts in a list.
        // 2. Update the sorted tree for update events by deleting nodes. Queue the
        //    unsorted nodes for updates in a list.
        // 3. Update the sorted tree for updates by inserting nodes. Fire insert
        //    events.
        // 4. Process queue of unsorted nodes for inserts. Fire insert events.

        // This cycle is rather complex but necessarily so. The reason is that for
        // the two-tree SortedList to function properly, there is a very strict order
        // for how trees can be modified. The unsorted tree must be brought completely
        // up-to-date before any access is made to the sorted tree. This ensures that
        // the unsorted nodes can discover their indices properly. The sorted tree must
        // have all deleted and updated nodes removed before any nodes are inserted.
        // This is because a deleted node may have a changed value that violates the
        // sorted order in the tree. An insert in this case may compare against a violating
        // node and result in inconsistency, even if the other node is eventually
        // deleted. Therefore the order of operations above is essentially update
        // the unsorted tree, delete from the sorted tree and finally insert into the
        // sorted tree. The last two insert steps are split to simplify finding updates
        // where the index does not change.

        // handle reordering events
        if(listChanges.isReordering()) {
            // the reorder map tells us what moved where
            int[] reorderMap = listChanges.getReorderMap();

            // create an array with the sorted nodes
            IndexedTreeNode[] sortedNodes = new IndexedTreeNode[sorted.size()];
            int index = 0;
            for(Iterator<IndexedTreeNode<IndexedTreeNode>> i = unsorted.iterator(); i.hasNext(); index++) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = i.next();
                sortedNodes[index] = unsortedNode.getValue();
            }

            // set the unsorted nodes to point to the new set of sorted nodes
            index = 0;
            for(Iterator<IndexedTreeNode<IndexedTreeNode>> i = unsorted.iterator(); i.hasNext(); index++) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = i.next();
                unsortedNode.setValue(sortedNodes[reorderMap[index]]);
                sortedNodes[reorderMap[index]].setValue(unsortedNode);
            }

            // we have handled the reordering!
            return;
        }

        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // first update the offset tree for all changes, and keep the changed nodes in a list
        LinkedList<IndexedTreeNode> insertNodes = new LinkedList<IndexedTreeNode>();

        // perform the inserts and deletes on the indexed tree
        while(listChanges.next()) {

            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.INSERT) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.addByNode(unsortedIndex, IndexedTreeNode.EMPTY_NODE);
                insertNodes.addLast(unsortedNode);

            // on delete, delete the index and sorted node
            } else if(changeType == ListEvent.DELETE) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.getNode(unsortedIndex);
                unsortedNode.removeFromTree(unsorted);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updates.addDelete(deleteSortedIndex);

            } else if(changeType == ListEvent.UPDATE) {
                // do not handle these till the next pass
            }
        }

        // update indices which are all deleted and then inserted
        IndicesPendingDeletion indicesPendingDeletion = new IndicesPendingDeletion();

        // record the original indices of the nodes for update
        listChanges.reset();
        while(listChanges.next()) {

            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.getNode(unsortedIndex);
                IndexedTreeNode sortedNode = unsortedNode.getValue();
                int originalIndex = sortedNode.getIndex();
                indicesPendingDeletion.addPair(new IndexNodePair(originalIndex, unsortedNode));
            }
        }

        // delete the indices of the updated nodes
        listChanges.reset();
        while(listChanges.next()) {

            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                deleteByUnsortedNode(unsortedNode);
            }
        }

        // fire all the update events
        for(Iterator i = indicesPendingDeletion.iterator(); i.hasNext(); ) {
            IndexNodePair indexNodePair = (IndexNodePair)i.next();
            i.remove();

            int insertedIndex = insertByUnsortedNode(indexNodePair.node);
            int deletedIndex = indexNodePair.index;
            // adjust the out of order insert with respect to the delete list
            insertedIndex = indicesPendingDeletion.adjustDeleteAndInsert(deletedIndex, insertedIndex);

            // fire the events
            if(deletedIndex == insertedIndex) {
                updates.addUpdate(insertedIndex);
            } else {
                updates.addDelete(deletedIndex);
                updates.addInsert(insertedIndex);
            }
        }

        // fire all the insert events
        while(!insertNodes.isEmpty()) {
            IndexedTreeNode insertNode = insertNodes.removeFirst();
            int insertedIndex = insertByUnsortedNode(insertNode);
            updates.addInsert(insertedIndex);
        }

        // commit the changes and notify listeners
        updates.commitEvent();
    }

    /**
     * Inserts the specified unsorted node as the value in the sorted tree
     * and returns the sorted order.
     *
     * @return the sortedIndex of the inserted object.
     */
    private int insertByUnsortedNode(IndexedTreeNode unsortedNode) {
        // add the object to the sorted set
        IndexedTreeNode sortedNode = sorted.addByNode(unsortedNode);
        // assign the unsorted node the value of the sorted node
        unsortedNode.setValue(sortedNode);
        // return the sorted index
        return sortedNode.getIndex();
    }
    /**
     * Deletes the node in the sorted tree based on the value of the specified
     * unsorted tree node.
     *
     * @return the sortedIndex of the deleted object.
     */
    private int deleteByUnsortedNode(IndexedTreeNode unsortedNode) {
        // get the sorted node
        IndexedTreeNode sortedNode = (IndexedTreeNode)unsortedNode.getValue();
        // look up the sorted index before removing the nodes
        int sortedIndex = sortedNode.getIndex();
        // delete the sorted node from its tree
        sortedNode.removeFromTree(sorted);
        // return the sorted index
        return sortedIndex;
    }

    /**
     * Gets the {@link Comparator} that is being used to sort this list.
     *
     * @return the {@link Comparator} in use, or <tt>null</tt> if this list is
     *      currently unsorted. If this is an {@link EventList} of {@link Comparable}
     *      elements in natural order, then a {@link ComparableComparator} will
     *      be returned.
     */
    public Comparator getComparator() {
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
     *      you may use a {@link ComparableComparator} to sort them in their
     *      natural order. You may also specify <code>null</code> to put this
     *      {@link SortedList} in unsorted order.
     */
    public void setComparator(Comparator comparator) {
        // save this comparator
        this.comparator = comparator;
        // keep the old trees to construct the reordering
        IndexedTree previousSorted = sorted;
        // create the sorted list with a simple comparator
        Comparator treeComparator = null;
        if(comparator != null) treeComparator = new IndexedTreeNodeComparator(comparator);
        else treeComparator = new IndexedTreeNodeRawOrderComparator();
        sorted = new IndexedTree<IndexedTreeNode>(treeComparator);

        // create a list which knows the offsets of the indexes to initialize this list
        if(previousSorted == null && unsorted == null) {
            unsorted = new IndexedTree<IndexedTreeNode>();
            // add all elements in the source list, in order
            for(int i = 0; i < source.size(); i++) {
                IndexedTreeNode unsortedNode = unsorted.addByNode(i, IndexedTreeNode.EMPTY_NODE);
                insertByUnsortedNode(unsortedNode);
            }
            // this is the first sort so we're done
            return;
        }

        // if the lists are empty, we're done
        if(source.size() == 0) return;

        // rebuild the sorted tree to reflect the new Comparator
        for(Iterator i = unsorted.iterator();i.hasNext(); ) {
            IndexedTreeNode unsortedNode = (IndexedTreeNode)i.next();
            insertByUnsortedNode(unsortedNode);
        }

        // construct the reorder map
        int[] reorderMap = new int[size()];
        int oldSortedIndex = 0;
        for(Iterator i = previousSorted.iterator();i.hasNext();oldSortedIndex++) {
            IndexedTreeNode oldSortedNode = (IndexedTreeNode)i.next();
            IndexedTreeNode unsortedNode = (IndexedTreeNode)oldSortedNode.getValue();
            IndexedTreeNode newSortedNode = (IndexedTreeNode)unsortedNode.getValue();
            int newSortedIndex = newSortedNode.getIndex();
            reorderMap[newSortedIndex] = oldSortedIndex;
        }

        // notification about the big change
        updates.beginEvent();
        updates.reorder(reorderMap);
        updates.commitEvent();
    }

    /** {@inheritDoc} */
    protected int getSourceIndex(int mutationIndex) {
        IndexedTreeNode sortedNode = sorted.getNode(mutationIndex);
        IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
        return unsortedNode.getIndex();
    }

    /** {@inheritDoc} */
    protected boolean isWritable() {
        return true;
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        return indexOf(object) != -1;
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        if(comparator == null) return source.indexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = sorted.indexOf(object);

        // if we couldn't use the comparator to find the index, return -1
        if (index == -1) return -1;

        // otherwise, we must now begin a linear search for the index of an element
        // that .equals() the given object
        for (; index < size(); index++) {
            Object objectAtIndex = get(index);

            // if the objectAtIndex no longer compares equally with the given object, stop the linear search
            if (comparator.compare(object, objectAtIndex) != 0) return -1;

            // if the objectAtIndex and object are equal, return the index
            if (GlazedListsImpl.equal(object, objectAtIndex))
                return index;
        }

        // if we fall out of the loop we could not locate the object
        return -1;
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        if(comparator == null) return source.lastIndexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = sorted.lastIndexOf(object);

        // if we couldn't use the comparator to find the index, return -1
        if (index == -1) return -1;

        // otherwise, we must now begin a linear search for the index of an element
        // that .equals() the given object
        for (; index > -1; index--) {
            Object objectAtIndex = get(index);

            // if the objectAtIndex no longer compares equally with the given object, stop the linear search
            if (comparator.compare(object, objectAtIndex) != 0) return -1;

            // if the objectAtIndex and object are equal, return the index
            if (GlazedListsImpl.equal(object, objectAtIndex))
                return index;
        }

        // if we fall out of the loop we could not locate the object
        return -1;
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
     */
    public int indexOfSimulated(Object object) {
        return comparator != null ? sorted.indexOfSimulated(object) : size();
    }

    /**
     * A comparator that takes an indexed node, and compares the index of that node.
     */
    private static class IndexedTreeNodeRawOrderComparator implements Comparator {
        /**
         * Compares the alpha object to the beta object by their indicies.
         */
        public int compare(Object alpha, Object beta) {
            try {
                IndexedTreeNode alphaTreeNode = (IndexedTreeNode)alpha;
                IndexedTreeNode betaTreeNode = (IndexedTreeNode)beta;
                int alphaIndex = alphaTreeNode.getIndex();
                int betaIndex = betaTreeNode.getIndex();
                return alphaIndex - betaIndex;
            } catch(ClassCastException e) {
                System.out.println(alpha.getClass());
                System.out.println(beta.getClass());
                throw e;
            }
        }
    }

    /**
     * A comparator that takes an indexed node, and compares the value
     * of an object in a list that has the index of that node.
     *
     * <p>If one of the objects passed to {@link #compare} is not an
     * {@link IndexedTreeNode}, it will compare the object directly to the object
     * in the source {@link EventList} referenced by the {@link IndexedTreeNode}.
     * This functionality is necessary to allow use of the underlying
     * {@link Comparator} within {@link IndexedTree} to support {@link List#indexOf},
     * {@link List#lastIndexOf}, and {@link List#contains}.
     */
    private class IndexedTreeNodeComparator implements Comparator {

        /** the actual comparator used on the values found */
        private Comparator comparator;

        /**
         * Creates an {@link IndexedTreeNodeComparator} that compares the
         * objects in the source list based on the indexes of the tree
         * nodes being compared.
         */
        public IndexedTreeNodeComparator(Comparator comparator) {
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
            if(alpha instanceof IndexedTreeNode) {
                IndexedTreeNode alphaTreeNode = (IndexedTreeNode)alpha;
                alphaIndex = alphaTreeNode.getIndex();
                alphaObject = source.get(alphaIndex);
            }
            if(beta instanceof IndexedTreeNode) {
                IndexedTreeNode betaTreeNode = (IndexedTreeNode)beta;
                betaIndex = betaTreeNode.getIndex();
                betaObject = source.get(betaIndex);
            }
            int result = comparator.compare(alphaObject, betaObject);
            if(result != 0) return result;
            if(alphaIndex != -1 && betaIndex != -1) return alphaIndex - betaIndex;
            return 0;
        }
    }

    /**
     * A class for managing a list of pending deletes. This class presents deletes
     * in order sorted by the index where they will be reinserted.
     */
    private class IndicesPendingDeletion {

        /** the underlying data storage */
        SortedSet<IndexNodePair> indexNodePairs = new TreeSet<IndexNodePair>();

        /**
         * Adds the specified index to the list of indices pending deletion.
         */
        public void addPair(IndexNodePair nodePair) {
            indexNodePairs.add(nodePair);
        }

        /**
         * Adjusts the indices for a delete and an insert at the specified indices. This is
         * necessary if an event is forwarded before this list of delete events is forwarded.
         *
         * <p>This method can use some optimization.
         */
        public int adjustDeleteAndInsert(int deletedIndex, int insertedIndex) {
            for(Iterator<IndexNodePair> i = indexNodePairs.iterator(); i.hasNext(); ) {
                IndexNodePair indexNodePair = i.next();
                // adjust due to the delete
                if(deletedIndex < indexNodePair.index) {
                    indexNodePair.index--;
                }
                // adjust due to the insert
                if(insertedIndex <= indexNodePair.index) {
                    indexNodePair.index++;
                } else {
                    insertedIndex++;
                }
            }
            return insertedIndex;
        }

        /**
         * Gets the Iterator for this set of indices.
         */
        public Iterator<IndexNodePair> iterator() {
            return indexNodePairs.iterator();
        }

        /**
         * Gets this as a String for debugging.
         */
        public String toString() {
            return "" + indexNodePairs;
        }
    }

    /**
     * An IndexNodePair is simply a node and an index. This is useful for
     * keeping track of pending deletes.
     */
    private class IndexNodePair implements Comparable {
        private int index;
        private IndexedTreeNode node;

        public IndexNodePair(int index, IndexedTreeNode node) {
            this.index = index;
            this.node = node;
        }

        public String toString() {
            return "" + index + "(" + source.get(node.getIndex()) + ")";
        }
        /**
         * Index node pairs are compared first by their Object's values, and then
         * by their indices.
         */
        public int compareTo(Object other) {
            IndexNodePair otherIndexNodePair = (IndexNodePair)other;
            if(comparator != null) {
                Object myObject = source.get(node.getIndex());
                Object otherObject = source.get(otherIndexNodePair.node.getIndex());
                int compareResult = comparator.compare(myObject, otherObject);
                if(compareResult != 0) return compareResult;
            }
            return index - otherIndexNodePair.index;
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

        /** the IndexedTreeIterator to use to move across the tree */
        private IndexedTreeIterator<IndexedTreeNode> treeIterator = sorted.iterator();

        /** the last unsorted index to be returned by this iterator */
        private int lastUnsortedIndex = -1;

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
            IndexedTreeNode sortedNode = (IndexedTreeNode)treeIterator.next();
            IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
            lastUnsortedIndex = unsortedNode.getIndex();
            return source.get(lastUnsortedIndex);
        }

        /**
         * Removes the last value returned by this iterator.
         */
        public void remove() {
            // fast fail if next hasn't been called
            if(lastUnsortedIndex == -1) throw new NoSuchElementException("Cannot remove before next is called");

            // this isn't the first value so just step the iterator back one and remove
            if(treeIterator.hasPrevious()) {
                treeIterator.previous();
                source.remove(lastUnsortedIndex);

            // this is the first value so just remove and reset the tree iterator
            } else {
                source.remove(lastUnsortedIndex);
                treeIterator = sorted.iterator();
            }
            lastUnsortedIndex = -1;
        }
    }
}