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
     * <p>This is implemented in two phases. First, the unsorted index tree is updated
     * so that it maches the source list. This is necessary so that when sorted operations
     * are performed, the comparisons are done with respect to the correct unsorted
     * index. In the second phase, the sorted list is updated with the changes.
     */
    public void listChanged(ListEvent listChanges) {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();
        
        // first update the offset tree for all changes, and keep the changed nodes in a list
        ArrayList unsortedNodes = new ArrayList();
        ArrayList deletedIndices = new ArrayList();

        // copy the list change sequence in order to iterate twice
        ListEvent clonedChanges = new ListEvent(listChanges);
        
        // keep track of the difference between the sorted set size and its expected size
        int sortedOffset = 0;
        
        // perform the updates on the indexed tree
        while(clonedChanges.next()) {
            
            // get the current change info
            int unsortedIndex = clonedChanges.getIndex();
            int changeType = clonedChanges.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.INSERT) {
                IndexedTreeNode unsortedNode = unsorted.addByNode(unsortedIndex, this);
                unsortedNodes.add(unsortedNode);
                sortedOffset++;

            // on delete, delete the index and sorted node
            } else if(changeType == ListEvent.DELETE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                unsortedNode.removeFromTree();
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updates.addDelete(deleteSortedIndex);

            // on update, delete the sorted node
            } else if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode) + sortedOffset;
                unsortedNodes.add(unsortedNode);
                deletedIndices.add(new Integer(deleteSortedIndex));
                sortedOffset++;
            }
        }
        
        // verify the index tree matches the source
        if(unsorted.size() != source.size()) throw new IllegalStateException();
        if(sorted.size() + sortedOffset != source.size()) throw new IllegalStateException();

        // for all changes now calculate the sorted index with the updated offset data
        Iterator unsortedNodesIterator = unsortedNodes.iterator();
        Iterator deletedIndicesIterator = deletedIndices.iterator();
        while(listChanges.next()) {
            
            // get the current change info
            int unsortedIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // on insert, insert into the sorted list and fire an event
            if(changeType == ListEvent.INSERT) {
                IndexedTreeNode unsortedNode = (IndexedTreeNode)unsortedNodesIterator.next();
                int sortedIndex = insertByUnsortedNode(unsortedNode);
                updates.addInsert(sortedIndex);

            // on delete, we've already fired the event
            } else if(changeType == ListEvent.DELETE) {
                // do nothing
    
            // on update, re-insert and fire an event
            } else if(changeType == ListEvent.UPDATE) {
                int deleteSortedIndex = ((Integer)deletedIndicesIterator.next()).intValue();
                IndexedTreeNode unsortedNode = (IndexedTreeNode)unsortedNodesIterator.next();
                int insertSortedIndex = insertByUnsortedNode(unsortedNode);
                
                if(deleteSortedIndex == insertSortedIndex) {
                    updates.addUpdate(insertSortedIndex);
                } else {
                    updates.addDelete(deleteSortedIndex);
                    updates.addInsert(insertSortedIndex);
                }
            }
        }
        
        if(deletedIndicesIterator.hasNext()) throw new IllegalStateException();
        if(unsortedNodesIterator.hasNext()) throw new IllegalStateException();
        
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
     * A comparator that takes an indexed node, and compares the index
     * of that node.
     */
    class IndexedTreeNodeRawOrderComparator implements Comparator {
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
         * Create a new IndexedTreeNodeComparator that compares the
         * objects in the array based on the indexes of the tree
         * nodes being compared.
         */
        public IndexedTreeNodeComparator(Comparator comparator) {
            this.comparator = comparator;
        }
        
        /**
         * Compares object alpha to object beta by using the source comparator.
         */
        public int compare(Object alpha, Object beta) {
            IndexedTreeNode alphaTreeNode = (IndexedTreeNode)alpha;
            IndexedTreeNode betaTreeNode = (IndexedTreeNode)beta;
            int alphaIndex = alphaTreeNode.getIndex();
            int betaIndex = betaTreeNode.getIndex();
            return comparator.compare(source.get(alphaIndex), source.get(betaIndex));
        }
    }
}
