/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.SortedSet;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;


/**
 * A filter list is a pseudo-list that owns another list. It only displays
 * a subset of the source list. That subset may be the complete set, an empty
 * set, or a select group of elements from a list. 
 *
 * The filter may be static or dynamic. In effect, the subset may change
 * in size by modifying the source set, or the filter itself. When the filter
 * has changed, the user should call the <code>handleFilterChanged()</code>
 * method.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part3/index.html">Glazed
 * Lists Tutorial Part 3 - Custom Filtering</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class AbstractFilterList extends MutationList implements ListChangeListener, EventList {

    /** the arraylist that maps to the elements that are displayed */
    private int[] map = null;
    private int mapSize = 0;
    private ArrayList filterSet = new ArrayList();
    
    /** the listeners for list change events */
    private ArrayList listChangeListeners = new ArrayList();
    
    /**
     * Creates a new filter list that filters elements out of the
     * specified source list. After an extending class is done instantiation,
     * it should <strong>always</strong> call handleFilterChanged().
     */
    public AbstractFilterList(EventList source) {
        super(source);
        source.addListChangeListener(this);
    }
    
    /**
     * For implementing the ListChangeListener interface. When the underlying list
     * changes, this notification allows the object to repaint itself or update
     * itself as necessary.
     *
     * When a list item is updated, it may become visible or become filtered.
     * When a list item is inserted, it may be visible or filtered.
     * When a list item is deleted, it must be filtered if it is not visible.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        synchronized(getRootList()) {
            
            // all of these changes to this list happen "atomically"
            updates.beginAtomicChange();
            
            // for all changes, one index at a time
            while(listChanges.next()) {
                
                // get the current change info
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                // handle delete events first because we have no item to examine
                if(changeType == ListChangeBlock.DELETE) {
                    // where we keep this item
                    int filterIndex = getFilterIndex(changeIndex);
                    filterSet.remove(changeIndex);
                    rebuildMap();
                    if(filterIndex != -1) {
                        updates.appendChange(filterIndex, ListChangeBlock.DELETE);
                    }
        
                // handle insert and update items where we have an item to examine
                } else {
                    // whether we should keep this item
                    Object sourceItem = source.get(changeIndex);
                    boolean updateMatches = filterMatches(sourceItem);

                    // update events will either add, remove or keep
                    if(changeType == ListChangeBlock.UPDATE) {
                        // where we keep this item
                        int filterIndex = getFilterIndex(changeIndex);
                        filterSet.set(changeIndex, Boolean.valueOf(updateMatches));
                        rebuildMap();
                        // if it not yet in the list
                        if(filterIndex == -1) {
                            if(updateMatches) {
                                updates.appendChange(getFilterIndex(changeIndex), ListChangeBlock.INSERT);
                            } else {
                                // keep it out of the list
                            }
                        // if it is already in the list
                        } else {
                            if(!updateMatches) {
                                updates.appendChange(filterIndex, ListChangeBlock.DELETE);
                            } else {
                                updates.appendChange(filterIndex, ListChangeBlock.UPDATE);
                            }
                        }
        
                    // insert events will either propagate or they won't 
                    } else if(changeType == ListChangeBlock.INSERT) {
                        filterSet.add(changeIndex, Boolean.valueOf(updateMatches));
                        rebuildMap();
                        if(updateMatches) {
                            updates.appendChange(getFilterIndex(changeIndex), ListChangeBlock.INSERT);
                        }
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
     * This iterates through all of the source items. Each source item can
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

            // set up the updated filters
            ArrayList updatedFilterSet = new ArrayList();
            int[] updatedMap = new int[source.size()]; 
            int currentFilteredIndex = 0;
            int updatedMapSize = 0;

            // for all source items, see what the change is
            for(int i = 0; i < source.size(); i++) {
                Object currentSource = source.get(i);
                // if this source item is the current item being displayed
                if(currentFilteredIndex < mapSize && map[currentFilteredIndex] == i) {
                    currentFilteredIndex++;
                    // the object still matches
                    if(filterMatches(currentSource)) {
                        //System.out.println("  Still matches:   " + i + " -> " + updatedMapSize);
                        updatedFilterSet.add(Boolean.TRUE);
                        updatedMap[updatedMapSize] = i;
                        updatedMapSize++;
                    // the object no longer matches
                    } else {
                        //System.out.println("- No longer match: " + i + " -> " + updatedMapSize);
                        updates.appendChange(updatedMapSize, ListChangeBlock.DELETE);
                        updatedFilterSet.add(Boolean.FALSE);
                    }
                // this source item is not being displayed
                } else {
                    // the object now matches
                    if(filterMatches(currentSource)) {
                        //System.out.println("+ New Match:       " + i + " -> " + updatedMapSize);
                        updates.appendChange(updatedMapSize, ListChangeBlock.INSERT);
                        updatedFilterSet.add(Boolean.TRUE);
                        updatedMap[updatedMapSize] = i;
                        updatedMapSize++;
                    // the object still doesn't match
                    } else {
                        //System.out.println("  Still no match   " + i + " -> " + updatedMapSize);
                        // do nothing
                        updatedFilterSet.add(Boolean.FALSE);
                    }
                }
            }
            // now that the change has occurred, store the updates
            filterSet = updatedFilterSet;
            map = updatedMap;
            mapSize = updatedMapSize;

            // commit the changes and notify listeners
            updates.commitAtomicChange();

            // return the number of updates that occurred
            //return updates.size();
        }
    }
    
    /**
     * Rebuilds the mapping from elements in the displayed filter to elements
     * in the source list.
     */
    private void rebuildMap() {
        synchronized(getRootList()) {
            map = new int[filterSet.size()];
            int current = 0;
            for(int i = 0; i < filterSet.size(); i++) {
                if(filterSet.get(i).equals(Boolean.TRUE)) {
                    map[current] = i;
                    current++;
                }
            }
            mapSize = current;
        }
    }
    private int getFilterIndex(int sourceIndex) {
        if(map == null) rebuildMap();
        if(filterSet.get(sourceIndex).equals(Boolean.FALSE)) return -1;
        for(int i = 0; i < map.length; i++) {
            if(map[i] == sourceIndex) return i;
        }
        return -1;
    }
    
    /**
     * Gets the filter list as a String for debugging.
     */
    private String getFilterListAsString() {
        StringBuffer filterListAsString = new StringBuffer();
        for(int i = 0; i < filterSet.size(); i++) {
            if(i % 10 == 0) filterListAsString.append(" ");
            if(Boolean.TRUE.equals(filterSet.get(i))) filterListAsString.append("T ");
            if(Boolean.FALSE.equals(filterSet.get(i))) filterListAsString.append("" + i%10 + " ");
        }
        return filterListAsString.toString();
    }
    /**
     * Gets the map as a String for debugging.
     */
    private String getMapAsString() {
        StringBuffer mapAsString = new StringBuffer();
        for(int i = 0; i < mapSize; i++) {
            mapAsString.append(map[i]);
            mapAsString.append(" ");
        }
        return mapAsString.toString();
    }
    
    
    /**
     * Tests if the specified item matches the current filter. The implementing
     * class must implement only this method.
     */
    public abstract boolean filterMatches(Object element);

    /**
     * Gets the specified element from the list.
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            if(map == null) rebuildMap();
            try {
                return source.get(map[index]);
            } catch(ArrayIndexOutOfBoundsException e) {
                System.out.println("abstract filter list error!");
                System.out.println("    map size:      " + map.length);
                System.out.println("    display size:  " + mapSize);
                System.out.println("    source size:   " + source.size());
                throw e;
            }
        }
    }

    /**
     * Gets the total number of elements in the list.
     */
    public int size() {
        synchronized(getRootList()) {
            if(map == null) rebuildMap();
            return mapSize;
        }
    }
}
