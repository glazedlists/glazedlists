/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists packages
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.util.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// Java collections are used for underlying data storage
import java.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;


/**
 * A list that provides a sorted view on its elements.
 *
 * <p>The sorting algorithm may be dynamic. In effect, user may specify the
 * criteria that is used to choose a sorting order.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part4/index.html">Glazed
 * Lists Tutorial Part 4 - Sorting</a>
 * @see java.util.Comparator
 * @see java.lang.Comparable
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SortedList extends TransformedList {

    /** a map from the unsorted index to the sorted index */
    private IndexedTree unsorted = null;
    /** a map from the sorted index to the unsorted index */
    private IndexedTree sorted = null;

    /** the comparator that this list uses for sorting */
    private Comparator comparator = null;

    /**
     * Creates a new filter list that provides a sorted view on the source data.
     * By not specifying a <code>Comparator</code> to sort by, the list elements
     * must implement the <code>Comparable</code> interface.
     */
    public SortedList(EventList source) {
        this(source, new ComparableComparator());
    }

    /**
     * Creates a new filter list that provides a sorted view on the source data.
     *
     * @param comparator the comparator to specify how to sort the list. You
     *      may also specify <code>null</code>, which will leave the list in
     *      the same order as the source list until a different <code>Comparator</code>
     *      is applied via the <code>setComparator()</code> method.
     */
    public SortedList(EventList source, Comparator comparator) {
        super(source);

        // use an Internal Lock to avoid locking the source list during a sort
        readWriteLock = new InternalReadWriteLock(source.getReadWriteLock(), new J2SE12ReadWriteLock());

        // load the initial data
        getReadWriteLock().readLock().lock();
        try {
            // trees are instansiated when a comparator is set
            setComparator(comparator);
            source.addListEventListener(this);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this notification allows the object to repaint itself or update
     * itself as necessary.
     *
     * <p>This is implemented in four phases. These phases are:
     * <br>1. Update the unsorted tree for all event types. Update the sorted tree
     *     for delete events by deleting nodes. Fire delete events. Queue unsorted
     *     nodes for inserts in a list.
     * <br>2. Update the sorted tree for update events by deleting nodes. Queue the
     *     unsorted nodes for updates in a list.
     * <br>3. Update the sorted tree for updates by inserting nodes. Fire insert
     *     events.
     * <br>4. Process queue of unsorted nodes for inserts. Fire insert events.
     *
     * <p>This cycle is rather complex but necessarily so. The reason is that for
     * the two-tree SortedList to function properly, there is a very strict order
     * for how trees can be modified. The unsorted tree must be brought completely
     * up-to-date before any access is made to the sorted tree. This ensures that
     * the unsorted nodes can discover their indices properly. The sorted tree must
     * have all deleted and updated nodes removed before any nodes are inserted.
     * This is because a deleted node may have a changed value that violates the
     * sorted order in the tree. An insert in this case may compare against a violating
     * node and result in inconsistency, even if the other node is eventually
     * deleted. Therefore the order of operations above is essentially update
     * the unsorted tree, delete from the sorted tree and finally insert into the
     * sorted tree. The last two insert steps are split to simplify finding updates
     * where the index does not change.
     */
    public void listChanged(ListEvent listChanges) {

        // handle reordering events
        if(listChanges.isReordering()) {
            // the reorder map tells us what moved where
            int[] reorderMap = listChanges.getReorderMap();

            // create an array with the sorted nodes
            IndexedTreeNode[] sortedNodes = new IndexedTreeNode[sorted.size()];
            int index = 0;
            for(Iterator i = unsorted.iterator(); i.hasNext(); index++) {
                IndexedTreeNode unsortedNode = (IndexedTreeNode)i.next();
                IndexedTreeNode sortedNode = (IndexedTreeNode)unsortedNode.getValue();
                sortedNodes[index] = sortedNode;
            }

            // set the unsorted nodes to point to the new set of sorted nodes
            index = 0;
            for(Iterator i = unsorted.iterator(); i.hasNext(); index++) {
                IndexedTreeNode unsortedNode = (IndexedTreeNode)i.next();
                unsortedNode.setValue(sortedNodes[reorderMap[index]]);
                sortedNodes[reorderMap[index]].setValue(unsortedNode);
            }

            // we have handled the reordering!
            return;
        }

        // all of these changes to this list happen "atomically"
        updates.beginEvent();

        // first update the offset tree for all changes, and keep the changed nodes in a list
        LinkedList insertNodes = new LinkedList();

        // perform the updates on the indexed tree
        ListEvent firstPass = new ListEvent(listChanges);
        while(firstPass.next()) {

            // get the current change info
            int unsortedIndex = firstPass.getIndex();
            int changeType = firstPass.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.INSERT) {
                IndexedTreeNode unsortedNode = unsorted.addByNode(unsortedIndex, this);
                insertNodes.addLast(unsortedNode);

            // on delete, delete the index and sorted node
            } else if(changeType == ListEvent.DELETE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                unsortedNode.removeFromTree();
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updates.addDelete(deleteSortedIndex);

            } else if(changeType == ListEvent.UPDATE) {
                // do not handle these till the next pass
            }
        }

        // update indices which are all deleted and then inserted
        IndicesPendingDeletion indicesPendingDeletion = new IndicesPendingDeletion();

        // record the original indices of the nodes for update
        ListEvent secondPass = new ListEvent(listChanges);
        while(secondPass.next()) {

            // get the current change info
            int unsortedIndex = secondPass.getIndex();
            int changeType = secondPass.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                IndexedTreeNode sortedNode = (IndexedTreeNode)unsortedNode.getValue();
                int originalIndex = sortedNode.getIndex();
                indicesPendingDeletion.addPair(new IndexNodePair(originalIndex, unsortedNode));
            }
        }

        // delete the indices of the updated nodes
        ListEvent thirdPass = listChanges;
        while(thirdPass.next()) {

            // get the current change info
            int unsortedIndex = thirdPass.getIndex();
            int changeType = thirdPass.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
            }
        }

        // fire all the update events
        while(indicesPendingDeletion.hasPair()) {
            IndexNodePair indexNodePair = indicesPendingDeletion.removePair();
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
            IndexedTreeNode insertNode = (IndexedTreeNode)insertNodes.removeFirst();
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
        sortedNode.removeFromTree();
        // return the sorted index
        return sortedIndex;
    }

    /**
     * Gets the comparator used to sort this list.
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * Set the comparator used to sort this list. This will resort the list
     * and will take <code>O(N * Log N)</code> time. For large lists, it may
     * be worthwhile to create multiple SortedList views, each of which uses
     * a different Comparator to specify the ordering.
     *
     * @param comparator the comparator to specify how to sort the list. If
     *      the list elements implement <code>Comparable</code>, you may use
     *      a <code>ComparableComparator</code> instance to sort them in their
     *      natural order. You may also specify <code>null</code>, which will
     *      leave the list in the same order as the source list.
     */
    public void setComparator(Comparator comparator) {
        ((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();
        try {
            // save this comparator
            this.comparator = comparator;
            // keep the old trees to construct the reordering
            IndexedTree previousSorted = sorted;
            IndexedTree previousUnsorted = unsorted;
            // create the sorted list with a simple comparator
            Comparator treeComparator = null;
            if(comparator != null) treeComparator = new IndexedTreeNodeComparator(comparator);
            else treeComparator = new IndexedTreeNodeRawOrderComparator();
            sorted = new IndexedTree(treeComparator);
            // create a list which knows the offsets of the indexes
            unsorted = new IndexedTree();

            // if the lists are empty, we're done
            if(source.size() == 0) return;

            // add all elements in the source list, in order
            for(int i = 0; i < source.size(); i++) {
                IndexedTreeNode unsortedNode = unsorted.addByNode(i, this);
                insertByUnsortedNode(unsortedNode);
            }

            // if this is the first sort, we're done
            if(previousSorted == null && previousUnsorted == null) return;

            // construct the reorder map
            int[] reorderMap = new int[size()];
            for(int i = 0; i < reorderMap.length; i++) {
                // first get unsorted index at i
                IndexedTreeNode sortedNode = sorted.getNode(i);
                IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
                int unsortedIndex = unsortedNode.getIndex();
                // now find original index for that unsorted index
                IndexedTreeNode previousUnsortedNode = previousUnsorted.getNode(unsortedIndex);
                IndexedTreeNode previousSortedNode = (IndexedTreeNode)previousUnsortedNode.getValue();
                int previousIndex = previousSortedNode.getIndex();
                // save the values in the reorder map
                reorderMap[i] = previousIndex;
            }

            // notification about the big change
            updates.beginEvent();
            updates.reorder(reorderMap);
            updates.commitEvent();

        } finally {
            ((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
        }
    }

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int mutationIndex) {
        IndexedTreeNode sortedNode = sorted.getNode(mutationIndex);
        IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
        int unsortedIndex = unsortedNode.getIndex();
        return unsortedIndex;
    }

    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected boolean isWritable() {
        return true;
    }

    /**
     * Returns true if this list contains the specified element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean contains(Object object) {
        return sorted.contains(object);

    }

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int indexOf(Object object) {
        return sorted.indexOf(object);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        return sorted.lastIndexOf(object);
    }

    /**
     * A comparator that takes an indexed node, and compares the index
     * of that node.
     */
    static class IndexedTreeNodeRawOrderComparator implements Comparator {
        /**
         * Compares the alpha object to the beta object by their indicies.
         */
        public int compare(Object alpha, Object beta) {
            IndexedTreeNode alphaTreeNode = (IndexedTreeNode)alpha;
            IndexedTreeNode betaTreeNode = (IndexedTreeNode)beta;
            int alphaIndex = alphaTreeNode.getIndex();
            int betaIndex = betaTreeNode.getIndex();
            return alphaIndex - betaIndex;
        }
    }

    /**
     * A comparator that takes an indexed node, and compares the value
     * of an object in a list that has the index of that node.
     */
    class IndexedTreeNodeComparator implements Comparator {

        /** the actual comparator used on the values found */
        private Comparator comparator;

        /**
         * Creates a new IndexedTreeNodeComparator that compares the
         * objects in the source list based on the indexes of the tree
         * nodes being compared.
         *
         * <p>If one of the objects passed to compare() is not a tree node,
         * it will compare the object directly to the object in the source
         * list referenced by the tree node.  This functionality was
         * necessary to allow use of the underlying comparator within
         * IndexedTree to support indexOf(), lastIndexOf, and contains().
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
            if(alpha instanceof IndexedTreeNode) {
                IndexedTreeNode alphaTreeNode = (IndexedTreeNode)alpha;
                int alphaIndex = alphaTreeNode.getIndex();
                alphaObject = source.get(alphaIndex);
            }
            if(beta instanceof IndexedTreeNode) {
                IndexedTreeNode betaTreeNode = (IndexedTreeNode)beta;
                int betaIndex = betaTreeNode.getIndex();
                betaObject = source.get(betaIndex);
            }
            return comparator.compare(alphaObject, betaObject);
        }
    }

    /**
     * A class for managing a list of pending deletes. This class
     * presents deletes in order sorted by the index where they will be
     * reinserted.
     */
    static class IndicesPendingDeletion {

        /** the underlying data storage */
        SortedSet indexNodePairs = new TreeSet();

        /**
         * Adds the specified index to the list of indices pending deletion.
         */
        public void addPair(IndexNodePair nodePair) {
            indexNodePairs.add(nodePair);
        }

        /**
         * Adjusts the indices for a delete and an insert at the specified indices. This is
         * necessary if an event is forwarded be fore this list of delete events is forwarded.
         *
         * <p>This method can use some optimization.
         */
        public int adjustDeleteAndInsert(int deletedIndex, int insertedIndex) {
            for(Iterator i = indexNodePairs.iterator(); i.hasNext(); ) {
                IndexNodePair indexNodePair = (IndexNodePair)i.next();
                int originalIndex = indexNodePair.index;
                if(deletedIndex < indexNodePair.index) {
                    indexNodePair.index--;
                }
                if(insertedIndex <= indexNodePair.index) {
                    indexNodePair.index++;
                } else {
                    insertedIndex++;
                }
            }
            return insertedIndex;
        }

        /**
         * Gets the next index/node pair to insert.
         */
        public IndexNodePair removePair() {
            IndexNodePair first = (IndexNodePair)indexNodePairs.first();
            indexNodePairs.remove(first);
            return first;
        }

        /**
         * Tests if there is a pair to remove.
         */
        public boolean hasPair() {
            return !(indexNodePairs.isEmpty());
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
    class IndexNodePair implements Comparable {
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
            Object myObject = source.get(node.getIndex());
            Object otherObject = source.get(otherIndexNodePair.node.getIndex());
            int compareResult = comparator.compare(myObject, otherObject);
            if(compareResult != 0) return compareResult;
            return index - otherIndexNodePair.index;
        }
    }
}
