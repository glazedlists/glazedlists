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

    
    public boolean debug = false;
    
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
        if(debug) System.out.println("");
        if(debug) System.out.println("handling change event: " + listChanges);

        // all of these changes to this list happen "atomically"
        updates.beginEvent();
        
        // first update the offset tree for all changes, and keep the changed nodes in a list
        LinkedList insertNodes = new LinkedList();
        LinkedList updateNodes = new LinkedList();
        LinkedList deleteIndices = new LinkedList();

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

        // perform the updates on the indexed tree
        ListEvent secondPass = listChanges;
        while(secondPass.next()) {
            
            // get the current change info
            int unsortedIndex = secondPass.getIndex();
            int changeType = secondPass.getType();

            // on insert, insert the index node
            if(changeType == ListEvent.UPDATE) {
                IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                updateNodes.addLast(unsortedNode);
                deleteIndices.addLast(new Integer(deleteSortedIndex));
            }
        }
        
        if(debug) System.out.println(deleteIndices);
        
        
        // fire all the update events
        while(!updateNodes.isEmpty()) {
            IndexedTreeNode updateNode = (IndexedTreeNode)updateNodes.removeFirst();
            int deletedIndex = ((Integer)deleteIndices.removeFirst()).intValue();
            int insertedIndex = insertByUnsortedNode(updateNode);
            // adjust the out of order insert with respect to the delete list
            if(debug) System.out.println("HANDLE RM " + deletedIndex + ", ADD " + insertedIndex + "... " + deleteIndices); 
            
            for(ListIterator i = deleteIndices.listIterator(deleteIndices.size()); i.hasPrevious(); ) {
                int currentDeletedIndex = ((Integer)i.previous()).intValue();
                if(currentDeletedIndex < insertedIndex ||
                    (currentDeletedIndex == insertedIndex && insertedIndex < deletedIndex)) {
                //if(currentDeletedIndex <= insertedIndex) {
                    insertedIndex++;
                } else {
                    currentDeletedIndex++;
                    i.set(new Integer(currentDeletedIndex));
                }
            }
            // fire the events
            if(deletedIndex == insertedIndex) {
                if(debug == true) System.out.println("UPDATE! UPDATE " + insertedIndex);
                updates.addUpdate(insertedIndex);
            } else {
                if(debug == true) System.out.println("UPDATE! RM " + deletedIndex + ", ADD " + insertedIndex);
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
        if(debug) System.out.println("");
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
     * Returns true if this list contains the specified element.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public boolean contains(Object object) {
        return (binarySearch(object) != -1);
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
        return binarySearchForFirst(object);
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if this list does not contain this element.  Since
     * uniqueness is guaranteed for this list, the value returned by this
     * method will always be indentical to calling <code>indexOf()</code>.
     *
     * <p>Like all read-only methods, this method <strong>does not</strong> manage
     * its own thread safety. Callers can obtain thread safe access to this method
     * via <code>getReadWriteLock().readLock()</code>.
     */
    public int lastIndexOf(Object object) {
        return binarySearchForLast(object);
    }

    /**
     * Returns the index of where the value is found or -1 if that value doesn't exist.
     */
    private int binarySearch(Object object) {
        int start = 0;
        int end = size() - 1;

        while(start <= end) {
            int current = (start + end) / 2;
            int comparisonResult = comparator.compare(object, get(current));
            // The object is larger than current so focus on right half of list
            if(comparisonResult > 0) {
                start = current + 1;
            // The object is smaller than current so focus on left half of list
            } else if (comparisonResult < 0) {
                end = current - 1;
            // The object equals the object at current, so return
            } else {
                return current;
            }
        }
        return -1;
    }

    /**
     * Returns the first index of the value if found or -1 if that value doesn't exist.
     */
    private int binarySearchForFirst(Object object) {
        int start = 0;
        int end = size() - 1;
        int current = 0;

        while(start <= end) {
            current = (start + end) / 2;
            int comparisonResult = comparator.compare(object, get(current));
            // The object is larger than current so focus on right half of list
            if(comparisonResult > 0) {
                start = current + 1;
            // The object is smaller than current so focus on left half of list
            } else if (comparisonResult < 0) {
                end = current - 1;
            // The object equals the object at current, so return
            } else {
                // if it's the first or the one to the left isn't the same, return
                if(current == 0 || 0 != comparator.compare(get(current-1), get(current))) {
                    return current;
                }
                else {
                    end = current - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Returns the last index of the value if found or -1 if that value doesn't exist.
     */
    private int binarySearchForLast(Object object) {
        int start = 0;
        int end = size() - 1;
        int current = 0;

        while(start <= end) {
            current = (start + end) / 2;
            int comparisonResult = comparator.compare(object, get(current));
            // The object is larger than current so focus on right half of list
            if(comparisonResult > 0) {
                start = current + 1;
            // The object is smaller than current so focus on left half of list
            } else if (comparisonResult < 0) {
                end = current - 1;
            // The object equals the object at current, so return
            } else {
                // if it's the last or the one to the left isn't the same, return
                if(current == size() - 1 || 0 != comparator.compare(get(current), get(current+1))) {
                    return current;
                }
                else {
                    start = current + 1;
                }
            }
        }
        return -1;
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
