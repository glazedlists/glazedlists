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
 * <strong><font color="#FF0000">Warning!</font> The CompositeList is thread-unsafe
 * and may cause deadlock in multi-threaded applications. Its use is for single-threaded
 * environments only! This issue is a high priority issue in the Glazed Lists bug tracker
 * and will hopefully be resolved soon. (Jesse Wilson, June 14, 2004)</font>
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
    /** allow a single thread to be the forwarding thread in collisions */
    private Thread forwardingThread = null;
    
    ///** the lists we are following events on, sorted by lock order */
    //private SortedSet memberListsSorted = new TreeSet(new LockOrderComparator());
    
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
        //getReadWriteLock().writeLock().lock();
        //try {
            // insert the actual list
            MemberList memberList = new MemberList(list);
            memberLists.add(memberList);
            //memberListsSorted.add(memberList);
            
            // get the offset for the elements of this member
            int offset = getListOffset(memberList);

            // pass on a change for the insert of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addInsert(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        //} finally {
        //    getReadWriteLock().writeLock().unlock();
        //}
    }
    
    /**
     * Removes the specified list from the lists that compose this list.
     */
    public void removeMemberList(EventList list) {
        //getReadWriteLock().writeLock().lock();
        //try {
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
            //memberListsSorted.remove(memberList);

            // pass on a change for the remove of all this list's elements
            if(memberList.size() > 0) {
                updates.beginEvent();
                updates.addDelete(offset, offset + memberList.size() - 1);
                updates.commitEvent();
            }
        //} finally {
        //    getReadWriteLock().writeLock().unlock();
        //}
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
            tryPropagateChanges();
        }
    }

    /**
     * Attempts to propagate all changes from the global change queue. If the
     * write lock cannot be obtained, this method returns immediately. Otherwise
     * it propogates changes while the change queue is available and the 
     */
    private void tryPropagateChanges() {
        while(true) {
            // quit if there are no more changes to process
            synchronized(CompositeList.this) {
                // verify that there is work to be done
                if(globalChangeQueue.isEmpty()) {
                    forwardingThread = null;
                    return;
                }
            }
            // give up if we cannot get the lock, somebody else will forward for us
            if(!((CompositeReadWriteLock)getReadWriteLock()).eventForwardLock().tryLock()) {
                return;
            }
            
            // we have the lock
            try {
                // process all of the changes available right now
                List changesToProcess = new ArrayList();
                synchronized(CompositeList.this) {
                    changesToProcess.addAll(globalChangeQueue);
                    globalChangeQueue.clear();
                }

                // propogate all those changes
                updates.beginEvent();
                for(Iterator i = changesToProcess.iterator(); i.hasNext(); ) {
                    // get the list and its changes
                    MemberList listWithChanges = (MemberList)i.next();
                    ListEvent listChanges = null;
                    synchronized(CompositeList.this) {
                        listChanges = (ListEvent)listWithChanges.changeQueue.removeFirst();
                    }

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
                }
                updates.commitEvent();
            } finally {
                ((CompositeReadWriteLock)getReadWriteLock()).eventForwardLock().unlock();
            }
        }
    }

    static int count = 0;
    /**
     * The CompositeReadWriteLock is a lock that is composed of a list of
     * source ReadWriteLocks.
     */
    class CompositeReadWriteLock implements ReadWriteLock {

        /** the composite read lock */
        private Lock compositeReadLock = new CompositeReadLock();
        /** the forward lock is composed of one mutex and the read lock */
        private EventForwardLock eventForwardLock = new EventForwardLock(compositeReadLock);
        
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
            throw new IllegalArgumentException("list is not writable");
        }
        
        /**
         * Return the lock used for forwarding events.
         */
        public Lock eventForwardLock() {
            return eventForwardLock;
        }
    }
    
    /**
     * The EventForwardLock is a very special lock that is composed
     * of a single mutex that is obtained first, and then the CompositeReadLock,
     * which is in itself composed of all read locks from the source list.
     */
    class EventForwardLock implements Lock {
        
        /** the forward and read locks delegated to */
        //private Lock forwardLock = new LoudLock("PROPLOCK", new ReentrantWriterPreferenceReadWriteLock().writeLock());
        private Lock forwardLock = new J2SE12ReadWriteLock().writeLock();
        private Lock readLock;

        /**
         * Creates a new EventForwardLock that uses the specified read lock
         * to protect against writes while forwarding events.
         */
        public EventForwardLock(Lock readLock) {
            this.readLock = readLock;
        }
        
        /**
         * The event forward lock does not support lock() directly, use tryLock()
         * instead. This is to protect against deadlock.
         */
        public void lock() {
            throw new UnsupportedOperationException("Cannot lock() the event forward lock, use tryLock()");
        }

        /**
         * Trying the event forwad lock tries a single forward lock first, then
         * locks all read locks via the composite read lock.
         */
        public boolean tryLock() {
            boolean gotForwardLock = forwardLock.tryLock();
            if(!gotForwardLock) return false;
            readLock.lock();
            return true;
        }

        /**
         * Unlocks the event forward lock.
         */
        public void unlock() {
            readLock.unlock();
            forwardLock.unlock();
        }
    }
    
    /**
     * The CompositeReadLock is a lock that is composed of a list of source
     * ReadLocks.
     */
    class CompositeReadLock implements Lock {

        /**
         * Gets the lock for a specified list. This gets the read lock.
         */
        private Lock lockForList(MemberList list) {
            return list.getSourceList().getReadWriteLock().readLock();
        }

        /**
         * Acquires the lock.
         */
        public void lock() {
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList current = (MemberList)i.next();
                lockForList(current).lock();
            }
        }
        
        /**
         * Acquires the lock only if it is free at the time of invocation.
         */
        public boolean tryLock() {
            List locksAquired = new ArrayList();
            // try to acquire locks in order
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList current = (MemberList)i.next();
                boolean success = lockForList(current).tryLock();
    
                // add this lock to the locked list
                if(success) {
                    locksAquired.add(current);
                // release all success locks on a single fail
                } else {
                    for(int j = 0; j < locksAquired.size(); j++) {
                        MemberList listToRelease = (MemberList)locksAquired.get(j);
                        lockForList(listToRelease).unlock();
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
            for(Iterator i = memberLists.iterator(); i.hasNext(); ) {
                MemberList current = (MemberList)i.next();
                lockForList(current).unlock();
            }
        }
    }
    
    /*
     * List locks are stored in a set sorted by the order they are to be
     * locked. This order is currently specified by the System.identityHashCode()
     * value for their reader/writer lock.
     *
     * This may have limitations, in the case of hash code collisions or in
     * theory, objects whose identityHashCode value changes over time. For now
     * it seems like the simplest way to provide a global ordering for lock
     * order.
     */
    /*class LockOrderComparator implements Comparator {
        public int compare(Object alpha, Object beta) {
            MemberList listAlpha = (MemberList)alpha;
            ReadWriteLock lockAlpha = listAlpha.getSourceList().getReadWriteLock();
            MemberList listBeta = (MemberList)beta;
            ReadWriteLock lockBeta = listBeta.getSourceList().getReadWriteLock();
            
            int hashCodeA = System.identityHashCode(lockAlpha);
            int hashCodeB = System.identityHashCode(lockBeta);
            return hashCodeA - hashCodeB;
        }
    }*/
}
