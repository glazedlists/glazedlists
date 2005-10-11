/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.adt.IndexedTreeNode;
import ca.odell.glazedlists.impl.adt.IndexedTree;
import ca.odell.glazedlists.impl.adt.IndexedTreeIterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;

import java.util.*;

/**
 * This sorting implementation keeps elements in sorted order while
 * new elements are added or removed.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ActiveSorting<E> implements SortedList.AdvancedSortingStrategy<E>{

    /** a map from the unsorted index to the sorted index */
    private IndexedTree<IndexedTreeNode> unsorted = null;
    /** a map from the sorted index to the unsorted index */
    private IndexedTree<IndexedTreeNode> sorted = null;

    /** the comparator that this list uses for sorting */
    private Comparator<E> comparator = null;

    private final ListEventAssembler updates;
    private final EventList<E> source;

    /** whether this list enforces sort order, even if it causes update events to be replaced by moves */
    private final boolean sortOrderEnforced;

    public ActiveSorting(ListEventAssembler updates, EventList<E> source, boolean sortOrderEnforced) {
        this.updates = updates;
        this.source = source;
        this.sortOrderEnforced = sortOrderEnforced;
    }


    /** {@inheritDoc} */
    public void listChanged(ListEvent<E> listChanges) {
        // handle reordering events
        if(listChanges.isReordering()) {
            // the reorder map tells us what moved where
            int[] reorderMap = listChanges.getReorderMap();

            // create an array with the sorted nodes
            IndexedTreeNode[] sortedNodes = new IndexedTreeNode[sorted.size()];
            int index = 0;
            for(IndexedTreeIterator<IndexedTreeNode> i = unsorted.iterator(0); i.hasNext(); index++) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = i.next();
                sortedNodes[index] = unsortedNode.getValue();
            }

            // set the unsorted nodes to point to the new set of sorted nodes
            index = 0;
            for(IndexedTreeIterator<IndexedTreeNode> i = unsorted.iterator(0); i.hasNext(); index++) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = i.next();
                unsortedNode.setValue(sortedNodes[reorderMap[index]]);
                sortedNodes[reorderMap[index]].setValue(unsortedNode);
            }

            // we have handled the reordering!
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
        LinkedList<IndexedTreeNode> insertNodes = new LinkedList<IndexedTreeNode>();
        List<IndexedTreeNode<IndexedTreeNode>> updateNodes = new ArrayList<IndexedTreeNode<IndexedTreeNode>>();

        // Update the indexed tree so it matches the source.
        // Save the nodes to be inserted and updated as well
        while(listChanges.next()) {

            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.INSERT) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.addByNode(unsortedIndex, IndexedTreeNode.EMPTY_NODE);
                insertNodes.addLast(unsortedNode);

            // on update, mark the updated node as unsorted and save it so it can be moved
            } else if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.getNode(unsortedIndex);
                IndexedTreeNode sortedNode = unsortedNode.getValue();
                sortedNode.setSorted(false);
                updateNodes.add(sortedNode);

            // on delete, delete the index and sorted node
            } else if(changeType == ListEvent.DELETE) {
                IndexedTreeNode<IndexedTreeNode> unsortedNode = unsorted.getNode(unsortedIndex);
                unsortedNode.removeFromTree(unsorted);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updates.addDelete(deleteSortedIndex);

            }
        }

        // fire update events
        for(Iterator<IndexedTreeNode<IndexedTreeNode>> i = updateNodes.iterator(); i.hasNext(); ) {
            IndexedTreeNode<IndexedTreeNode> sortedNode = i.next();
            int originalIndex = sortedNode.getIndex();

            // the element is still in sorted order, forward the update event
            if(isNodeInSortedOrder(sortedNode)) {
                sortedNode.setSorted(true);
                updates.addUpdate(originalIndex);

            // sort order is not enforced so we lose perfect sorting order
            // but we don't need to move elements around
            } else if(!sortOrderEnforced) {
                updates.addUpdate(originalIndex);

            // sort order is enforced so move the element to its new location
            } else {
                sortedNode.removeFromTree(sorted);
                updates.addDelete(originalIndex);
                int insertedIndex = insertByUnsortedNode(sortedNode.getValue());
                updates.addInsert(insertedIndex);
            }
        }

        // fire insert events
        while(!insertNodes.isEmpty()) {
            IndexedTreeNode insertNode = insertNodes.removeFirst();
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
    private boolean isNodeInSortedOrder(IndexedTreeNode sortedNode) {
        // first ensure this node is greater than its predecessors
        for(IndexedTreeNode leftNeighbour = sortedNode.previous(); leftNeighbour != null; leftNeighbour = leftNeighbour.previous()) {
            if(!leftNeighbour.isSorted()) continue;
            if(sorted.getComparator().compare(leftNeighbour.getValue(), sortedNode.getValue()) > 0) return false;
            break;
        }

        // then ensure this node is less than its followers
        for(IndexedTreeNode rightNeighbour = sortedNode.next(); rightNeighbour != null; rightNeighbour = rightNeighbour.next()) {
            if(!rightNeighbour.isSorted()) continue;
            if(sorted.getComparator().compare(sortedNode.getValue(), rightNeighbour.getValue()) > 0) return false;
            break;
        }

        return true;
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

    /** {@inheritDoc} */
    public Comparator<E> getComparator() {
        return comparator;
    }

    public E get(int index) {
        return source.get(getSourceIndex(index));
    }
    public int size() {
        return source.size();
    }

    /** {@inheritDoc} */
    public void setComparator(Comparator<E> comparator) {
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
        for(IndexedTreeIterator i = unsorted.iterator(0); i.hasNext(); ) {
            IndexedTreeNode unsortedNode = i.next();
            insertByUnsortedNode(unsortedNode);
        }

        // construct the reorder map
        int[] reorderMap = new int[size()];
        int oldSortedIndex = 0;
        for(IndexedTreeIterator i = previousSorted.iterator(0); i.hasNext(); oldSortedIndex++) {
            IndexedTreeNode oldSortedNode = i.next();
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
    public int getSourceIndex(int mutationIndex) {
        IndexedTreeNode sortedNode = sorted.getNode(mutationIndex);
        IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
        return unsortedNode.getIndex();
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        if(!sortOrderEnforced || comparator == null) return source.indexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = sorted.indexOf(object);

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
        if(!sortOrderEnforced || comparator == null) return source.lastIndexOf(object);

        // use the fact that we have sorted data to quickly locate a position
        // at which we can begin a linear search for an object that .equals(object)
        int index = sorted.lastIndexOf(object);

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

    /** {@inheritDoc} */
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
     * in the source {@link ca.odell.glazedlists.EventList} referenced by the {@link IndexedTreeNode}.
     * This functionality is necessary to allow use of the underlying
     * {@link Comparator} within {@link IndexedTree} to support {@link java.util.List#indexOf},
     * {@link java.util.List#lastIndexOf}, and {@link java.util.List#contains}.
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

    /** {@inheritDoc} */
    public Iterator<E> iterator() {
        return new SortedListIterator();
    }

    /**
     * The fast iterator for SortedList
     */
    private class SortedListIterator implements Iterator<E> {

        /** the IndexedTreeIterator to use to move across the tree */
        private IndexedTreeIterator<IndexedTreeNode> treeIterator = sorted.iterator(0);

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
            return source.get(unsortedNode.getIndex());
        }

        /**
         * Removes the last value returned by this iterator.
         */
        public void remove() {
            int indexToRemove = treeIterator.previousIndex();
            ActiveSorting.this.source.remove(getSourceIndex(indexToRemove));
            treeIterator = sorted.iterator(indexToRemove);
        }
    }
}