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
 * A filter list is a mutation-list that views a subset of the source list.
 * That subset may be the complete set, an empty set, or a select group of
 * elements from a list. 
 *
 * <p>The filter may be static or dynamic. In effect, the subset may change
 * in size by modifying the source set, or the filter itself. When the filter
 * has changed, the user should call the <code>handleFilterChanged()</code>
 * method.
 *
 * <p>As of April 8, 2004, the array-based filter data structure has been replaced with
 * a high-performance tree-based data structure. This data structure has potentially
 * slower access for get() but with significantly better performance for updates.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part3/index.html">Glazed
 * Lists Tutorial Part 3 - Custom Filtering</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class AbstractFilterList extends WritableMutationList implements ListChangeListener, EventList {

    /** the flag list contains Boolean.TRUE for selected items and null or others */
    private SparseList flagList = new SparseList();
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list. After an extending class is done instantiation,
     * it should <strong>always</strong> call handleFilterChanged().
     */
    protected AbstractFilterList(EventList source) {
        super(source);
        
        prepareFlagList();
        
        source.addListChangeListener(this);
    }
    
    
    /**
     * When the FlagList is prepared, it populates it with information from
     * the source list and the initial selection model.
     *
     * <p>This is copied without shame from SelectionList.
     */
    private void prepareFlagList() {
        for(int i = 0; i < source.size(); i++) {
            flagList.add(null);
        }
    }

    /**
     * For implementing the ListChangeListener interface. When the underlying list
     * changes, this notification allows the object to repaint itself or update
     * itself as necessary.
     *
     * <li>When a list item is updated, it may become visible or become filtered.
     * <li>When a list item is inserted, it may be visible or filtered.
     * <li>When a list item is deleted, it must be filtered if it is not visible.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        synchronized(getRootList()) {
            
            // all of these changes to this list happen "atomically"
            updates.beginAtomicChange();
            
            // for all changes, one index at a time
            while(listChanges.next()) {
                
                // get the current change info
                int sourceIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                // handle delete events
                if(changeType == ListChangeBlock.DELETE) {
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;
                    
                    // if this value was not filtered out, it is now so add a change
                    if(wasIncluded) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.appendChange(filteredIndex, ListChangeBlock.DELETE);
                    }

                    // remove this entry from the flag list
                    flagList.remove(sourceIndex);
                    
                // handle insert events
                } else if(changeType == ListChangeBlock.INSERT) {
                    
                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));
                    
                    // if this value should be included, add a change and add the item
                    if(include) {
                        flagList.add(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.appendChange(filteredIndex, ListChangeBlock.INSERT);

                    // if this value should not be included, just add the item
                    } else {
                        flagList.add(sourceIndex, null);
                    }

                // handle update events
                } else if(changeType == ListChangeBlock.UPDATE) {
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;
                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));

                    // if this element is being removed as a result of the change
                    if(wasIncluded && !include) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        flagList.set(sourceIndex, null);
                        updates.appendChange(filteredIndex, ListChangeBlock.DELETE);

                    // if this element is being added as a result of the change
                    } else if(!wasIncluded && include) {
                        flagList.set(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.appendChange(filteredIndex, ListChangeBlock.INSERT);
                    }
                }
            }
            // commit the changes and notify listeners
            updates.commitAtomicChange();
        }
    }


    /**
     * When the filter changes, this goes through all of the list elements
     * and retains only the ones who match the current filter.
     *
     * <p>This iterates through all of the source items. Each source item can
     * have changed in varous ways:
     *    <li>It could be added because it now matches and didn't before
     *    <li>It could be removed because it doesn't matches and used to
     *    <li>It could stay on because it always matches
     *    <li>It coudl stay off because it never matches
     */
    protected void handleFilterChanged() {
        synchronized(getRootList()) {
            // all of these changes to this list happen "atomically"
            updates.beginAtomicChange();

            // for all source items, see what the change is
            for(int i = 0; i < source.size(); i++) {
                
                // test if this value was already not filtered out
                boolean wasIncluded = flagList.get(i) != null;
                // whether we should add this item
                boolean include = filterMatches(source.get(i));

                // if this element is being removed as a result of the change
                if(wasIncluded && !include) {
                    int filteredIndex = flagList.getCompressedIndex(i);
                    flagList.set(i, null);
                    updates.appendChange(filteredIndex, ListChangeBlock.DELETE);

                // if this element is being added as a result of the change
                } else if(!wasIncluded && include) {
                    flagList.set(i, Boolean.TRUE);
                    int filteredIndex = flagList.getCompressedIndex(i);
                    updates.appendChange(filteredIndex, ListChangeBlock.INSERT);
                }
            }

            // commit the changes and notify listeners
            updates.commitAtomicChange();
        }
    }
    
    /**
     * Tests if the specified item matches the current filter. The implementing
     * class must implement only this method.
     */
    public abstract boolean filterMatches(Object element);

    /**
     * Gets the specified element from the list.
     *
     * This is copied without shame from SelectionList.
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            int sourceIndex = flagList.getIndex(index);
    
            // ensure that this value still exists before retrieval
            if(sourceIndex < source.size()) {
                return source.get(sourceIndex);
            } else {
                //new Exception("Returning null for removed selection " + row).printStackTrace();
                return null;
            }
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * This is the number of elements currently selected.
     *
     * This is copied without shame from SelectionList.
     */
    public int size() {
        synchronized(getRootList()) {
            return flagList.getCompressedList().size();
        }
    }

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected final int getSourceIndex(int mutationIndex) {
        return flagList.getIndex(mutationIndex);
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected final boolean isWritable() {
        return true;
    }
}
