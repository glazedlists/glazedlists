/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.event.*;
import com.odellengineeringltd.glazedlists.util.*;
// Java collections are used for underlying data storage
import java.util.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;


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
public final class SortedList extends WritableMutationList implements ListChangeListener, EventList {

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
        // trees are instansiated when a comparator is set
        setComparator(comparator);
        source.addListChangeListener(this);
    }

    
    /**
     * For implementing the ListChangeListener interface. When the underlying list
     * changes, this notification allows the object to repaint itself or update
     * itself as necessary.
     *
     * <p>This is implemented in two phases. The first phase updates the unsorted
     * indexes of all nodes. The second phase updates the sorted indexes, removing
     * and deleting nodes as necessary. Two phases are necessary because the sorted
     * nodes cannot be inserted until all of the final offsets are known. This
     * means that all offsets must be updated before the first sorted value is
     * updated.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        synchronized(getRootList()) {
            
            // all of these changes to this list happen "atomically"
            updates.beginAtomicChange();
            
            // first update the offset tree for all changes, and keep the changed nodes in a list
            ArrayList unsortedNodes = new ArrayList();

            // copy the list change sequence in order to iterate twice
            ListChangeEvent clonedChanges = new ListChangeEvent(listChanges);
            while(clonedChanges.next()) {
                
                // get the current change info
                int unsortedIndex = clonedChanges.getIndex();
                int changeType = clonedChanges.getType();

                // insert the offset node
                if(changeType == ListChangeBlock.INSERT) {
                    IndexedTreeNode unsortedNode = unsorted.addByNode(unsortedIndex, this);
                    unsortedNodes.add(unsortedNode);

                // delete the offset node
                } else if(changeType == ListChangeBlock.DELETE) {
                    IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                    unsortedNode.removeFromTree();
                    unsortedNodes.add(unsortedNode);
        
                // fetch the offset node
                } else if(changeType == ListChangeBlock.UPDATE) {
                    IndexedTreeNode unsortedNode = unsorted.getNode(unsortedIndex);
                    unsortedNodes.add(unsortedNode);
                }
            }

            // for all changes now calculate the sorted index with the updated offset data
            Iterator unsortedNodesIterator = unsortedNodes.iterator();
            while(listChanges.next()) {
                
                // get the current change info
                int unsortedIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
                IndexedTreeNode unsortedNode = (IndexedTreeNode)unsortedNodesIterator.next();

                // insert into the sorted list
                if(changeType == ListChangeBlock.INSERT) {
                    int sortedIndex = insertByUnsortedNode(unsortedNode);
                    updates.appendChange(sortedIndex, ListChangeBlock.INSERT);

                // delete from the sorted list
                } else if(changeType == ListChangeBlock.DELETE) {
                    int sortedIndex = deleteByUnsortedNode(unsortedNode);
                    updates.appendChange(sortedIndex, ListChangeBlock.DELETE);
        
                // update the sorted list, potentially by adding and then deleting
                } else if(changeType == ListChangeBlock.UPDATE) {
                    int deleteSortedIndex = deleteByUnsortedNode(unsortedNode);
                    int insertSortedIndex = insertByUnsortedNode(unsortedNode);
                    
                    if(deleteSortedIndex == insertSortedIndex) {
                        updates.appendChange(insertSortedIndex, ListChangeBlock.UPDATE);
                    } else {
                        updates.appendChange(deleteSortedIndex, ListChangeBlock.DELETE);
                        updates.appendChange(insertSortedIndex, ListChangeBlock.INSERT);
                    }
                }
            }
            // commit the changes and notify listeners
            updates.commitAtomicChange();
        }
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
        synchronized(getRootList()) {
            // save this comparator
            this.comparator = comparator;
            // create the sorted list with a simple comparator
            Comparator treeComparator = null;
            if(comparator != null) treeComparator = new IndexedTreeNodeComparator(comparator);
            else treeComparator = new IndexedTreeNodeRawOrderComparator();
            sorted = new IndexedTree(treeComparator);
            // create a list which knows the offsets of the indexes
            unsorted = new IndexedTree();
            
            // we're done if there's no elements in the source list
            if(source.size() == 0) return;
            
            // add all elements in the source list, in order
            for(int i = 0; i < source.size(); i++) {
                IndexedTreeNode unsortedNode = unsorted.addByNode(i, this);
                insertByUnsortedNode(unsortedNode);
            }

            // notification about the big change
            updates.beginAtomicChange();
            updates.appendChange(0, size() - 1, ListChangeBlock.DELETE);
            updates.appendChange(0, size() - 1, ListChangeBlock.INSERT);
            updates.commitAtomicChange();
        }
    }


    /**
     * Gets the specified element from the list. Note that calls to get(i) will
     * not always return the same object. This is because the underlying source
     * list may change or the sorting algorithm may change. When such changes
     * occur, the element at get(i) may be shifted to a different location, but
     * still exist in the list.
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            IndexedTreeNode sortedNode = sorted.getNode(index);
            IndexedTreeNode unsortedNode = (IndexedTreeNode)sortedNode.getValue();
            int unsortedIndex = unsortedNode.getIndex();
            return source.get(unsortedIndex);
        }
    }

    /**
     * Gets the total number of elements in the list.
     */
    public int size() {
        synchronized(getRootList()) {
            return sorted.size();
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
