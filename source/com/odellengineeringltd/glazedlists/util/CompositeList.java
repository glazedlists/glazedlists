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
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A CompositeList is a list that is composed of one or more lists.
 *
 * <p><strong><font color="#FF0000">Warning</font></strong> this class has
 * major concurrency problems that are not yet resolved. Do not use this class
 * in a multi-threaded environment! Use at your own risk!
 *
 * <p>One difficult problem posed by the implementation of CompositeList is
 * locking. Glazed Lists uses <code>getRootList()</code> in order to provide a
 * single lock for a single source list and all lists that depend upon it. As
 * CompositeList has multiple sources, this solution is not possible.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeList extends AbstractList implements EventList {
    
    /** the lists that we are following events on */
    public List memberLists = new ArrayList();
    
    /** the change event and notification system */
    protected ListChangeSequence updates = new ListChangeSequence();

    /**
     * Creates a new CompositeList.
     */
    public CompositeList() {
    }
    
    /**
     * Returns the element at the specified position in this list. Most
     * mutation lists will override the get method to use a mapping.
     */
    public Object get(int index) {
        synchronized(getRootList()) {
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                if(index < current.size()) return current.getSourceList().get(index);
                else index = index - current.size();
            }
        }
        return null;
    }
    
    /**
     * Adds the specified list to the lists that compose this list.
     */
    public void addMemberList(EventList list) {
        synchronized(getRootList()) {
            // insert the actual list
            MemberList memberList = new MemberList(list);
            memberLists.add(memberList);
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // pass on a change for the insert of all this list's elements
            updates.beginAtomicChange();
            updates.appendChange(offset, offset + memberList.size() - 1, ListChangeBlock.INSERT);
            updates.commitAtomicChange();
        }
    }
    
    /**
     * Removes the specified list from the lists that compose this list.
     */
    public void removeMemberList(EventList list) {
        synchronized(getRootList()) {
            // find the member list
            MemberList memberList = null;
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                if(current.getSourceList() == list) {
                    memberList = current;
                    break;
                }
            }
            if(memberList == null) throw new IllegalArgumentException("Cannot remove list " + list + " which is not in this CompositeList");
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // remove the member list
            memberLists.remove(memberList);
        
            // pass on a change for the remove of all this list's elements
            updates.beginAtomicChange();
            updates.appendChange(offset, offset + memberList.size() - 1, ListChangeBlock.DELETE);
            updates.commitAtomicChange();
        }
    }
    
    /**
     * Returns the number of elements in this list.
     */
    public int size() {
        synchronized(getRootList()) {
            int size = 0;
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                size = size + current.size();
            }
            return size;
        }
    }
    
    /**
     * Gets the offset of the specified member list.
     */
    private int getListOffset(MemberList memberList) {
        int listOffset = 0;
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            if(current == memberList) return listOffset;
            else listOffset = listOffset + current.size();
        }
        throw new RuntimeException();
    }

    
    /**
     * Registers the specified listener to receive notification of changes
     * to this list.
     */
    public final void addListChangeListener(ListChangeListener listChangeListener) {
        updates.addListChangeListener(listChangeListener);
    }

    /**
     * Removes the specified listener from receiving change updates for this list.
     */
    public void removeListChangeListener(ListChangeListener listChangeListener) {
        updates.removeListChangeListener(listChangeListener);
    }

    /**
     * For implementing the EventList interface. This returns this list, which does
     * not depend on another list.
     */
    public EventList getRootList() {
        return this;
    }

    
    /**
     * The member list listener listens to a single list for changes and
     * forwards the changes from that list to all listeners for composed list.
     */
    class MemberList implements ListChangeListener {
        
        /** the source list for this member */
        private EventList sourceList;
        
        /** the last reported size of the source list */
        private int size;
        
        /**
         * Creates a new member list that listens to changes in the specified
         * source list.
         */
        public MemberList(EventList sourceList) {
            this.sourceList = sourceList;
            this.size = sourceList.size();
            
            sourceList.addListChangeListener(this);
        }
        
        /**
         * Gets the last known size of this member list. This may be different
         * from the current size of the sourceList because size change events
         * may be enroute to this listener. For consistency it is necessary to
         * keep an independent count of the size.
         */
        public int size() {
            return size;
        }
        
        /**
         * Gets the source of this member list.
         */
        public EventList getSourceList() {
            return sourceList;
        }
        
        /**
         * When the source list is changed, this changes the composite list
         * also.
         */
        public void notifyListChanges(ListChangeEvent listChanges) {
            synchronized(getRootList()) {
                // get the offset for the elements of this member
                int offset = getListOffset(this);

                // pass on the changes
                updates.beginAtomicChange();
                while(listChanges.next()) {
                    // propagate the change
                    int index = listChanges.getIndex();
                    int type = listChanges.getType();
                    updates.appendChange(offset + index, type);
                    
                    // update the size
                    if(type == ListChangeBlock.DELETE) {
                        size--;
                    } else if(type == ListChangeBlock.INSERT) {
                        size++;
                    }
                    assert(size >= 0);
                }
                updates.commitAtomicChange();
            }
        }
    }
}
