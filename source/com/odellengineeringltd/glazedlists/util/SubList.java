/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;

/**
 * A SubList is a view of a sub-range of an EventList.
 *
 * <p>Although the <code>SubList</code>'s size is initially fixed, the 
 * <code>SubList</code> can change size as a consequence of changes to
 * the source list that occur within the range covered by
 *  the <code>SubList</code>.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class SubList extends WritableMutationList implements ListChangeListener, EventList {

    /** the start index of this list, inclusive */
    private int startIndex;
    
    /** the end index of this list, exclusive */
    private int endIndex;

    /**
     * Creates a new SubList that covers the specified range of indicies
     * in the source list.
     *
     * @param startIndex the start index of the source list, inclusive
     * @param endIndex the end index of the source list, exclusive
     * @param source the source list to view
     * @param automaticallyRemove true if this SubList should deregister itself
     *      from the ListChangeListener list of the source list once it is
     *      otherwise out of scope.
     *
     * @see com.odellengineeringltd.glazedlists.event.ListChangeListenerWeakReferenceProxy
     */
    public SubList(EventList source, int startIndex, int endIndex, boolean automaticallyRemove) {
        super(source);
        synchronized(getRootList()) {
            // do consistency checking
            if(startIndex < 0 || endIndex <= startIndex || endIndex > source.size()) {
                throw new IllegalArgumentException("The range " + startIndex + "-" + endIndex + " is not valid over a list of size " + source.size());
            }
            
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        
        // listen directly or via a proxy that will do garbage collection
        if(automaticallyRemove) {
            source.addListChangeListener(new ListChangeListenerWeakReferenceProxy(source, this));
        } else {
            source.addListChangeListener(this);
        }
    }
    
    /**
     * Returns the number of elements in this list. 
     */
    public int size() {
        synchronized(getRootList()) {
            return endIndex - startIndex;
        }
    }
    
    /**
     * Returns the element at the specified position in this list.
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            return source.get(index - startIndex);
        }
    }
    
    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex - startIndex;
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected boolean isWritable() {
        return true;
    }

    /**
     * When the list is changed, the SubList only forwards changes that occur
     * within the bounds of the SubList.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        synchronized(getRootList()) {
            updates.beginAtomicChange();
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
                
                // if it is a change before
                if(changeIndex < startIndex) {
                    if(changeType == ListChangeBlock.INSERT) {
                        startIndex++;
                        endIndex++;
                    } else if(changeType == ListChangeBlock.DELETE) {
                        startIndex--;
                        endIndex--;
                    }
                // if it is a change within
                } else if(changeIndex < endIndex) {
                    if(changeType == ListChangeBlock.INSERT) {
                        endIndex++;
                        updates.appendChange(changeIndex - startIndex, ListChangeBlock.INSERT);
                    } else if(changeType == ListChangeBlock.UPDATE) {
                        updates.appendChange(changeIndex - startIndex, ListChangeBlock.INSERT);
                    } else if(changeType == ListChangeBlock.DELETE) {
                        endIndex--;
                        updates.appendChange(changeIndex - startIndex, ListChangeBlock.DELETE);
                    }
                // if it is a change after
                } else {
                    // do nothing
                }
            }
            updates.commitAtomicChange();
        }
    }
}
