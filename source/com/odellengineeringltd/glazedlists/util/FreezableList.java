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
// an arraylist holds the frozen data
import java.util.ArrayList;

/**
 * A FreezableList is a list that can be <i>frozen</i> to prevent it from being
 * modified while it is being accessed. The list can later resume receiving
 * updates and to complete the methaphor this is called being <i>thawed</i>.
 *
 * When a FreezableList is frozen, it copies the elements of the source list
 * into an internal array. It notifies its source list that it is no longer
 * interested in receiving updates. At this point the list is temporarily 
 * immutable.
 *
 * When the FreezableList is thawed it discards its copied array and resumes
 * receiving updates from the source list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class FreezableList extends MutationList {

    /** the state of the freezable list */
    private boolean frozen = false;
    
    /** the frozen objects */
    private ArrayList frozenData = new ArrayList();
    
    /**
     * Creates a new FreezableList that can freeze the specified source list.
     */
    public FreezableList(EventList source) {
        super(source);
        source.addListChangeListener(this);
    }
    
    /**
     * Returns the element at the specified position in this list. Most
     * mutation lists will override the get method to use a mapping.
     */
    public Object get(int index) {
        if(frozen) {
            return frozenData.get(index);
        } else {
            return source.get(index);
        }
    }
    
    /**
     * Returns the number of elements in this list.
     */
    public int size() {
        if(frozen) {
            return frozenData.size();
        } else {
            return source.size();
        }
    }
    
    /**
     * Gets whether this list is blocked from receiving changes.
     */
    public boolean isFrozen() {
        return frozen;
    }
    
    /**
     * Locks the contents of the FreezableList from receiving any changes.
     */
    public void freeze() {
        synchronized(getRootList()) {
            if(frozen) throw new IllegalStateException("Cannot freeze a list that is already frozen");
            
            // we are no longer interested in update events
            source.removeListChangeListener(this);
            
            // copy the source array into the frozen list
            frozenData.addAll(source);
            
            // mark this list as frozen
            frozen = true;
        }
    }
    
    /**
     * Sets the FreezableList to be synchronized with the source list. This
     * will allow the FreezableList to resume receiving updates from its
     * source.
     *
     * When thawed the FreezableList will notify listening lists that the
     * data has changed.
     */
    public void thaw() {
        synchronized(getRootList()) {
            if(!frozen) throw new IllegalStateException("Cannot thaw a list that is not frozen");
            
            // mark this list as thawed
            frozen = false;
            int frozenDataSize = frozenData.size();
            frozenData.clear();
            
            // fire events to listeners of the thaw
            updates.beginAtomicChange();
            if(frozenData.size() > 0) updates.appendChange(0, frozenDataSize - 1, ListChangeBlock.DELETE);
            if(source.size() > 0) updates.appendChange(0, source.size() - 1, ListChangeBlock.INSERT);
            updates.commitAtomicChange();

            // being listening to update events
            source.addListChangeListener(this);
        }
    }
    
    /**
     * When the list is changed the cache is cleared. This could be replaced
     * by a more efficient implementation which iterated through the cache
     * and updated elements whose indexes changed.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        if(frozen) {
            // when a list change event arrives and this list is frozen,
            // it is possible that the event was queued before this list
            // was frozen. for this reason we do not throw any exceptions
            // but instead silently ignore the event
            
        } else {
            super.notifyListChanges(listChanges);
        }
    }
}
