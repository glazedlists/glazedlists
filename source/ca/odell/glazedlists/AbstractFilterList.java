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
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// Java collections are used for underlying data storage
import java.util.*;


/**
 * A filter list is a mutation-list that views a subset of the source list.
 * That subset may be the complete set, an empty set, or a select group of
 * elements from a list. 
 *
 * <p>The filter may be static or dynamic. In effect, the subset may change
 * in size by modifying the source set, or the filter itself. When the filter
 * has changed, the user should call the {@link #handleFilterChanged()}
 * method.
 *
 * <p>As of April 8, 2004, the array-based filter data structure has been replaced with
 * a high-performance tree-based data structure. This data structure has potentially
 * slower access for get() but with significantly better performance for updates.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial-0.9.1/">Glazed
 * Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class AbstractFilterList extends TransformedList implements ListEventListener {

    /** the flag list contains Boolean.TRUE for selected items and null or others */
    private SparseList flagList = new SparseList();
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list. After an extending class is done instantiation,
     * it should <strong>always</strong> call handleFilterChanged().
     */
    protected AbstractFilterList(EventList source) {
        super(source);
        
        // use an Internal Lock to avoid locking the source list during a sort
        readWriteLock = new InternalReadWriteLock(source.getReadWriteLock(), new J2SE12ReadWriteLock());
        
        // load the initial data
        getReadWriteLock().readLock().lock();
        try {
            // build a list of what is filtered and what's not
            prepareFlagList();
            // listen for changes to the source list
            source.addListEventListener(this);
        } finally {
            getReadWriteLock().readLock().unlock();
        }
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
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this notification allows the object to repaint itself or update
     * itself as necessary.
     *
     * <li>When a list item is updated, it may become visible or become filtered.
     * <li>When a list item is inserted, it may be visible or filtered.
     * <li>When a list item is deleted, it must be filtered if it is not visible.
     */
    public final void listChanged(ListEvent listChanges) {
        // all of these changes to this list happen "atomically"
        updates.beginEvent();
        
        // handle reordering events
        if(listChanges.isReordering()) {
            int[] sourceReorderMap = listChanges.getReorderMap();
            int[] filterReorderMap = new int[flagList.getCompressedList().size()];
            
            // adjust the flaglist & construct a reorder map to propagate
            SparseList previousFlagList = flagList;
            flagList = new SparseList();
            for(int i = 0; i < sourceReorderMap.length; i++) {
                Object flag = previousFlagList.get(sourceReorderMap[i]); 
                flagList.add(flag);
                if(flag != null) filterReorderMap[flagList.getCompressedIndex(i)] = previousFlagList.getCompressedIndex(sourceReorderMap[i]);
            }
            
            // fire the reorder
            updates.reorder(filterReorderMap);
            
        // handle non-reordering events
        } else {
        
            // for all changes, one index at a time
            while(listChanges.next()) {
                
                // get the current change info
                int sourceIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
    
                // handle delete events
                if(changeType == ListEvent.DELETE) {
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;
                    
                    // if this value was not filtered out, it is now so add a change
                    if(wasIncluded) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addDelete(filteredIndex);
                    }
    
                    // remove this entry from the flag list
                    flagList.remove(sourceIndex);
                    
                // handle insert events
                } else if(changeType == ListEvent.INSERT) {
                    
                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));
                    
                    // if this value should be included, add a change and add the item
                    if(include) {
                        flagList.add(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addInsert(filteredIndex);
    
                    // if this value should not be included, just add the item
                    } else {
                        flagList.add(sourceIndex, null);
                    }
    
                // handle update events
                } else if(changeType == ListEvent.UPDATE) {
                    // test if this value was already not filtered out
                    boolean wasIncluded = flagList.get(sourceIndex) != null;
                    // whether we should add this item
                    boolean include = filterMatches(source.get(sourceIndex));
    
                    // if this element is being removed as a result of the change
                    if(wasIncluded && !include) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        flagList.set(sourceIndex, null);
                        updates.addDelete(filteredIndex);
    
                    // if this element is being added as a result of the change
                    } else if(!wasIncluded && include) {
                        flagList.set(sourceIndex, Boolean.TRUE);
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addInsert(filteredIndex);

                    // this element is still here
                    } else if(wasIncluded && include) {
                        int filteredIndex = flagList.getCompressedIndex(sourceIndex);
                        updates.addUpdate(filteredIndex);

                    }
                }
            }
        }

        // commit the changes and notify listeners
        updates.commitEvent();
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
    protected final void handleFilterChanged() {
        ((InternalReadWriteLock)getReadWriteLock()).internalLock().lock();
        try {
            // all of these changes to this list happen "atomically"
            updates.beginEvent();

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
                    updates.addDelete(filteredIndex);

                // if this element is being added as a result of the change
                } else if(!wasIncluded && include) {
                    flagList.set(i, Boolean.TRUE);
                    int filteredIndex = flagList.getCompressedIndex(i);
                    updates.addInsert(filteredIndex);
                }
            }

            // commit the changes and notify listeners
            updates.commitEvent();
        } finally {
            ((InternalReadWriteLock)getReadWriteLock()).internalLock().unlock();
        }
    }
    
    /**
     * Tests if the specified item matches the current filter. The implementing
     * class must implement only this method.
     */
    public abstract boolean filterMatches(Object element);

    /**
     * Returns the number of elements in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public final int size() {
        return flagList.getCompressedList().size();
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
    protected boolean isWritable() {
        return true;
    }
}
