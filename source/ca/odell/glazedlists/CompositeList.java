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
 * <p><strong><font color="#FF0000">Warning:</font></strong> the CompositeList's
 * concurrency lock recursively acquires the lock for all source lists. This can
 * lead to deadlocks in situations where multiple CompositeLists share the same
 * source lists. To avoid risk of deadlock, no source list (or its parent lists)
 * should be a member to more than one CompositeList. 
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=25">Bug 25</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class CompositeList extends AbstractEventList {
    
    /** the lists that we are following events on */
    private List memberLists = new ArrayList();
    
    /** keep a list of lists with unprocessed changes */
    private LinkedList globalChangeQueue = new LinkedList();

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
        // lock all member lists and the new list
        list.getReadWriteLock().readLock().lock();
        getReadWriteLock().writeLock().lock();
        try {
            // insert the actual list
            MemberList memberList = new MemberList(list);
            memberLists.add(memberList);
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // pass on a change for the insert of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addInsert(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        } finally {
            // release all member lists which now include the new list
            getReadWriteLock().writeLock().unlock();
        }
    }
    
    /**
     * Removes the specified list from the lists that compose this list.
     */
    public void removeMemberList(EventList list) {
        // lock all member lists
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
            
            // stop listening for events
            memberList.getSourceList().removeListEventListener(memberList);
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // remove the member list
            memberLists.remove(memberList);

            // pass on a change for the remove of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addDelete(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        } finally {
            // release all member lists and the removed list
            list.getReadWriteLock().readLock().unlock();
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
        throw new RuntimeException("Unable to find offset of member list " + memberList);
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
        
        /** a list of changes to process */
        private LinkedList changeQueue = new LinkedList();
        
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
         *
         * Changes are propogated in the following sequence:
         * Add the change event to this member list's queue of changes to process.
         * Add this member list to the global list of lists with changes
         * do while the global list of changes is not empty
         *     Attempt to obtain the writer lock to this list
         *     If the writer lock is obtained, process all events
         *     If the writer lock is not obtained, break
         * loop
         */
        public void listChanged(ListEvent listChanges) {
            synchronized(CompositeList.this) {
                changeQueue.addLast(listChanges);
                globalChangeQueue.addLast(MemberList.this);
            }
            // attempting at lock guarantees changes are propagated
            boolean locked = getReadWriteLock().writeLock().tryLock();
            if(locked) getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Propagates all changes from the global change queue.
     *
     * <p>Callers to this method must have already obtained this list's
     * lock. 
     */
    private void propagateChanges() {
        // propogate all the changes
        while(!globalChangeQueue.isEmpty()) {
            updates.beginEvent();
            // get the list and its changes
            MemberList listWithChanges = (MemberList)globalChangeQueue.removeFirst();
            ListEvent listChanges = (ListEvent)listWithChanges.changeQueue.removeFirst();

            // get the offset for the elements of this member
            int offset = getListOffset(listWithChanges);

            // pass on the changes
            while(listChanges.next()) {
                // propagate the change
                int index = listChanges.getIndex();
                int type = listChanges.getType();
                updates.addChange(type, offset + index);
                
                // update the size
                if(type == ListEvent.DELETE) {
                    listWithChanges.size--;
                } else if(type == ListEvent.INSERT) {
                    listWithChanges.size++;
                }
                assert(listWithChanges.size >= 0);
            }
            updates.commitEvent();
        }
    }

    /**
     * The CompositeReadWriteLock uses a single CompositeLock for both
     * reading and writing.
     */
    class CompositeReadWriteLock implements ReadWriteLock {
        
        /** the reading and writing lock */
        Lock lock = new CompositeLock();
        
        /**
         * Return the lock used for reading.
         */
        public Lock readLock() {
            return lock;
        }

        /**
         * Return the lock used for writing.
         */
        public Lock writeLock() {
            return lock;
        }
    }
    
    /**
     * The composite lock is a very special lock designed to solve some
     * concurrency problems faced by the CompositeList. This lock attempts
     * to forward events in the lock stage after acquiring all read locks.
     * This allows writer threads to give up their writer lock on their
     * source list by allowing the thread holding the composite lock to do
     * the forwarding.
     */
    class CompositeLock implements Lock {
    
        /** use a delegate lock to guarantee mutual exclusion */
        private Lock raceLock = new J2SE12ReadWriteLock().writeLock();
    
        /**
         * Obtaining this lock includes obtaining a read lock on all
         * member lists. This is a complex process and may require
         * the forwarding of change events in order to gain acquisition.
         *
         * First, a race lock is acquired. This guarantees that only
         * a single thread can hold the CompositeLock at one time.
         * Secondly, read locks for all member lists are aquired in
         * sequence. In obtaining these read locks, it is possible that
         * the holders of the corresponding write locks will attempt
         * to forward changes to this list. Such threads will write their
         * changes to the global change queue and then give up when they
         * cannot obtain this lock. Therefore there may be changes to be
         * forwarded, which are forwarded as the final stage of acquiring
         * this lock.
         */
        public void lock() {
            // 1 Obtain a lock to guarantee mutex
            raceLock.lock();
            // 2 Obtain all read locks
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().lock();
            }
            // 3 Forward events
            propagateChanges();
        }

        /**
         * Trying for this lock follows the same process as obtaining
         * the lock naturally. If the raceLock cannot be acquired, this
         * fails fast. If it can be acquired, this obtains all read locks
         * using the lock() method and not the immediate tryLock() method.
         */
        public boolean tryLock() {
            // 1 Obtain a lock to guarantee mutex
            boolean success = raceLock.tryLock();
            if(!success) return false;
            // 2 Obtain all read locks
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().lock();
            }
            // 3 Forward events
            propagateChanges();
            // 4 Return success
            return true;
        }

        /**
         * Releasing this lock releases the raceLock and then all member
         * list's locks in sequence.
         */
        public void unlock() {
            // 1 Release the race lock guaranteeing mutex
            raceLock.unlock();
            // 2 Release all read locks
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().unlock();
            }
        }
    }
}
