/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;

/**
 * A CompositeList is a list that is composed of one or more lists.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=25">Bug 25</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class CompositeList extends AbstractEventList {
    
    /** the lists that we are following events on */
    public List memberLists = new ArrayList();
    
    /** the change event and notification system */
    protected ListEventAssembler updates = new ListEventAssembler(this);

    /**
     * Creates a new CompositeList.
     */
    public CompositeList() {
        readWriteLock = new CompositeReadWriteLock();
    }
    
    /**
     * For implementing the ListEventListener interface. Extending classes should
     * adjust in response to this change and forward notifications about that
     * adjustment to downstream listeners.
     *
     * Because the CompositeList uses inner classes, this method throws an
     * IllegalStateException.
     */
    public void listChanged(ListEvent listChanges) {
        throw new IllegalStateException();
    }

    /**
     * Adds the specified list to the lists that compose this list.
     */
    public void addMemberList(EventList list) {
        getReadWriteLock().writeLock().lock();
        try {
            // insert the actual list
            MemberList memberList = new MemberList(list);
            memberLists.add(memberList);
            
            // lock this list for consistency with the composite lock
            list.getReadWriteLock().writeLock().lock();
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // pass on a change for the insert of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addInsert(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Removes the specified list from the lists that compose this list.
     */
    public void removeMemberList(EventList list) {
        getReadWriteLock().writeLock().lock();
        try {
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
        
            // unlock this list for consistency with the composite lock
            list.getReadWriteLock().writeLock().unlock();

            // pass on a change for the remove of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addDelete(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Returns the element at the specified position in this list. Most
     * mutation lists will override the get method to use a mapping.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public Object get(int index) {
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            if(index < current.size()) {
                return current.getSourceList().get(index);
            } else {
                index = index - current.size();
            }
        }
        return null;
    }
    
    /**
     * Returns the number of elements in this list.
     *
     * <p>This method is not thread-safe and callers should ensure they have thread-
     * safe access via <code>getReadWriteLock().readLock()</code>.
     */
    public int size() {
        int size = 0;
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            size = size + current.size();
        }
        return size;
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
     * The member list listener listens to a single list for changes and
     * forwards the changes from that list to all listeners for composed list.
     */
    class MemberList implements ListEventListener {
        
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
            
            sourceList.addListEventListener(this);
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
        public void listChanged(ListEvent listChanges) {
            getReadWriteLock().writeLock().lock();
            try {
                // get the offset for the elements of this member
                int offset = getListOffset(this);

                // pass on the changes
                updates.beginEvent();
                while(listChanges.next()) {
                    // propagate the change
                    int index = listChanges.getIndex();
                    int type = listChanges.getType();
                    updates.addChange(type, offset + index);
                    
                    // update the size
                    if(type == ListEvent.DELETE) {
                        size--;
                    } else if(type == ListEvent.INSERT) {
                        size++;
                    }
                    assert(size >= 0);
                }
                updates.commitEvent();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
    }
    
    /**
     * The CompositeReadWriteLock is a lock that is composed of a list of
     * source ReadWriteLocks.
     */
    class CompositeReadWriteLock implements ReadWriteLock {
        /** the composite read lock */
        private Lock compositeReadLock = new CompositeReadLock();
        /** the composite write lock */
        private Lock compositeWriteLock = new CompositeWriteLock();
        
        /**
         * Return the lock used for reading.
         */
        public Lock readLock() {
            return compositeReadLock;
        }
        /**
         * Return the lock used for writing.
         */
        public Lock writeLock() {
            return compositeWriteLock;
        }
    }
    
    /**
     * The CompositeReadLock is a lock that is composed of a list of source
     * ReadLocks.
     */
    class CompositeReadLock implements Lock {
        /**
         * Acquires the lock.
         */
        public void lock() {
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                current.getSourceList().getReadWriteLock().readLock().lock();
            }
        }
        
        /**
         * Acquires the lock only if it is free at the time of invocation.
         */
        public boolean tryLock() {
            List locksAquired = new ArrayList();
            // try to acquire locks in order
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                boolean success = current.getSourceList().getReadWriteLock().readLock().tryLock();

                // add this lock to the locked list
                if(success) {
                    locksAquired.add(current);
                // release all success locks on a single fail
                } else {
                    for(int j = 0; j < locksAquired.size(); j++) {
                        MemberList listToRelease = (MemberList)locksAquired.get(j);
                        Lock lockToRelease = listToRelease.getSourceList().getReadWriteLock().readLock();
                        lockToRelease.unlock();
                    }
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Releases the lock.
         */
        public void unlock() {
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                current.getSourceList().getReadWriteLock().readLock().unlock();
            }
        }
    }

    /**
     * The CompositeWriteLock is a lock that is composed of a list of source
     * WriteLocks.
     */
    class CompositeWriteLock implements Lock {
        /**
         * Acquires the lock.
         */
        public void lock() {
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                current.getSourceList().getReadWriteLock().writeLock().lock();
            }
        }
        
        /**
         * Acquires the lock only if it is free at the time of invocation.
         */
        public boolean tryLock() {
            List locksAquired = new ArrayList();
            // try to acquire locks in order
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                boolean success = current.getSourceList().getReadWriteLock().writeLock().tryLock();

                // add this lock to the locked list
                if(success) {
                    locksAquired.add(current);
                // release all success locks on a single fail
                } else {
                    for(int j = 0; j < locksAquired.size(); j++) {
                        MemberList listToRelease = (MemberList)locksAquired.get(j);
                        Lock lockToRelease = listToRelease.getSourceList().getReadWriteLock().writeLock();
                        lockToRelease.unlock();
                    }
                    return false;
                }
            }
            return true;
        }
        
        /**
         * Releases the lock.
         */
        public void unlock() {
            for(int i = 0; i < memberLists.size(); i++) {
                MemberList current = (MemberList)memberLists.get(i);
                current.getSourceList().getReadWriteLock().writeLock().unlock();
            }
        }
    }
}
