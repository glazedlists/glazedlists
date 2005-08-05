/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the core Glazed Lists package
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link EventList} composed of multiple source {@link EventList}s. This list
 * shows the contents of its source lists.
 *
 * <p>Note that all contained {@link EventList}s must use the same {@link ReadWriteLock}
 * if this {@link EventList} is to be used my multiple threads concurrently. To
 * construct an {@link EventList} that shares the {@link ReadWriteLock} with this
 * {@link CompositeList}, use {@link #createMemberList()} or
 * {@link BasicEventList#BasicEventList(ReadWriteLock) new BasicEventList(CompositeList.getReadWriteLock())}
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompositeList extends CollectionList {

    public CompositeList() {
        super(new BasicEventList(), GlazedLists.listCollectionListModel());
    }
    
    /**
     * Adds the specified {@link EventList} as a source to this {@link CompositeList}.
     */
    public void addMemberList(EventList list) {
        source.add(list);
    }
    
    /**
     * Creates a new {@link EventList} that shares its {@link ReadWriteLock} with
     * this {@link CompositeList}. This is necessary when this {@link CompositeList}
     * will be used by multiple threads.
     *
     * <p>Note that the created {@link EventList} must be explicitly added as a member
     * to this {@link CompositeList} using {@link #addMemberList(EventList)}.
     */
    public EventList createMemberList() {
        return new BasicEventList(getReadWriteLock());
    }
    
    /**
     * Removes the specified {@link EventList} as a source {@link EventList}
     * to this {@link CompositeList}.
     */
    public void removeMemberList(EventList list) {
        for(Iterator i = source.iterator(); i.hasNext(); ) {
            if(i.next() == list) {
                i.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Cannot remove list " + list + " which is not in this CompositeList");
    }
}


/**
 * An {@link EventList} composed of multiple source {@link EventList}s.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This
 * {@link EventList}'s {@link ReadWriteLock} recursively acquires the locks
 * for all source {@link EventList}s. This can cause deadlock if multiple
 * {@link CompositeList}s share the same source {@link EventList}s. Therefore
 * {@link CompositeList}s must not share source {@link EventList}s.
 *
 * <p><font size="5"><strong><font color="#FF0000">Warning:</font></strong> This
 * class is going to be merged with {@link CollectionList} in a future release.
 * Therefore the API is subject to change.</font>
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="tableheadingcolor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Writable:</b></td><td>only {@link #set(int,Object) set()} and {@link #remove(int) remove()}</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Performance:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Memory:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="tablesubheadingcolor"><b>Issues:</b></td><td>N/A</td></tr>
 * </table>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
final class OldCompositeList extends AbstractEventList {
//public final class CompositeList extends AbstractEventList {
    
    /** the lists that we are following events on */
    private List memberLists = new ArrayList();
    
    /** keep a list of lists with unprocessed changes */
    private LinkedList globalChangeQueue = new LinkedList();

    /**
     * Creates a {@link CompositeList} that is initially composed of zero source
     * {@link EventList}s.
     */
    public OldCompositeList() {
        super(null);
        readWriteLock = new CompositeReadWriteLock();
    }
    
    /**
     * Adds the specified {@link EventList} as a source to this {@link CompositeList}.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void addMemberList(EventList list) {
        // insert the actual list
        MemberList memberList = new MemberList(list);
        memberLists.add(memberList);
        
        // lock the new list
        ((CompositeReadWriteLock)getReadWriteLock()).lockNewMember(memberList);
        
        // get the offset for the elements of this member
        int offset = getListOffset(memberList);

        // pass on a change for the insert of all this list's elements
        if(memberList.size() > 0) {
            updates.beginEvent();
            updates.addInsert(offset, offset + memberList.size() - 1);
            updates.commitEvent();
        }
    }
    
    /**
     * Removes the specified {@link EventList} as a source {@link EventList}
     * to this {@link CompositeList}.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> This method is
     * thread ready but not thread safe. See {@link EventList} for an example
     * of thread safe code.
     */
    public void removeMemberList(EventList list) {
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
    }
    
    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public Object set(int index, Object value) {
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            if(index < current.size()) {
                return current.getSourceList().set(index, value);
            } else {
                index = index - current.size();
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    public Object remove(int index) {
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            if(index < current.size()) {
                return current.getSourceList().remove(index);
            } else {
                index = index - current.size();
            }
        }
        return null;
    }
    
    /** {@inheritDoc} */
    public int size() {
        int size = 0;
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            size = size + current.size();
        }
        return size;
    }
    
    /** {@inheritDoc} */
    private int getListOffset(MemberList memberList) {
        int listOffset = 0;
        for(int i = 0; i < memberLists.size(); i++) {
            MemberList current = (MemberList)memberLists.get(i);
            if(current == memberList) return listOffset;
            else listOffset = listOffset + current.size();
        }
        throw new IllegalStateException("Unable to find offset of member list " + memberList);
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
     * The member list listener listens to a single list for changes and
     * forwards the changes from that list to all listeners for composed list.
     */
    private class MemberList implements ListEventListener {
        
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
            synchronized(OldCompositeList.this) {
                changeQueue.addLast(listChanges);
                globalChangeQueue.addLast(MemberList.this);
            }
            // attempting at lock guarantees changes are propagated
            boolean locked = getReadWriteLock().writeLock().tryLock();
            if(locked) getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * The CompositeReadWriteLock uses a single CompositeLock for both
     * reading and writing.
     */
    private class CompositeReadWriteLock implements ReadWriteLock {
        
        /** the reading and writing lock */
        CompositeLock lock = new CompositeLock();
        
        /**
         * Return the lock used for reading.
         */
        public Lock readLock() {
            return lock;
        }
        
        /**
         * Locks the specified {@link MemberList} in addition to the already locked
         * member lists.
         */
        public void lockNewMember(MemberList newMember) {
            lock.lockNewMember(newMember);
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
    private class CompositeLock implements Lock {
    
        /** use a delegate lock to guarantee mutual exclusion */
        private Lock raceLock = LockFactory.DEFAULT.createLock();
    
        /** list of MemberLists whose locks are currently acquired */
        private List lockedMemberLists = new ArrayList();

        /** the locking thread may need to know if it has made a lock */
        private boolean locked = false;
        
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
            locked = true;
            // 2 Obtain all read locks
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().lock();
                lockedMemberLists.add(memberList);
            }
            // 3 Forward events
            propagateChanges();
        }
        
        /**
         * Locks the specified {@link MemberList} which has become a {@link MemberList}
         * since this lock was originally acquired. This requires that this lock
         * has been acquired.
         */
        public void lockNewMember(MemberList newMember) {
            // only lock if it will eventually be released
            if(locked) {
                newMember.getSourceList().getReadWriteLock().readLock().lock();
                lockedMemberLists.add(newMember);
            }
        }
        
        /**
         * Trying for this lock follows the same process as obtaining
         * the lock naturally. If the raceLock cannot be acquired, this
         * fails fast. If it can be acquired, this obtains all read locks
         * using the lock() method and not the immediate tryLock() method.
         */
        public boolean tryLock() {
            // 1 Obtain a lock to guarantee mutex
            locked = false;
            boolean success = raceLock.tryLock();
            if(!success) return false;
            // 2 Obtain all read locks
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().lock();
                lockedMemberLists.add(memberList);
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
            // 1 List the locks to release
            List locksToRelease = new ArrayList();
            locksToRelease.addAll(lockedMemberLists);
            lockedMemberLists.clear();
            // 2 Release the race lock guaranteeing mutex
            raceLock.unlock();
            // 3 Release all read locks
            for(Iterator i = locksToRelease.iterator(); i.hasNext(); ) {
                MemberList memberList = (MemberList)i.next();
                memberList.getSourceList().getReadWriteLock().readLock().unlock();
            }
            lockedMemberLists.clear();
        }
    }
}
