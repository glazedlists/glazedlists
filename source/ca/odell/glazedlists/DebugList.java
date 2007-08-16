/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.*;

/**
 * DebugList is meant to be used as a drop-in replacement for
 * {@link BasicEventList} at the root of pipelines of {@link EventList}s during
 * development. It provides methods for turning on various types of assertions
 * which throw {@link RuntimeException}s when they are violated. The goal is to
 * detect and fail fast on error conditions in much the same way Iterators
 * commonly throw {@link java.util.ConcurrentModificationException}s.
 *
 * <p>Some of the assertions that are controlled by this DebugList include:
 *
 * <ul>
 *   <li>{@link #setLockCheckingEnabled(boolean)} toggles whether this DebugList
 *       asserts that all read operations are guarded by read locks and all
 *       write operations are guarded by write locks.
 *
 *   <li>{@link #getSanctionedReaderThreads()} is the Set of Threads which are
 *       allowed to read from the DebugList. If the Set is empty then
 *       <strong>ALL</strong> Threads are assumed to be sanctioned readers.
 *
 *   <li>{@link #getSanctionedWriterThreads()} is the Set of Threads which are
 *       allowed to write to the DebugList. If the Set is empty then
 *       <strong>ALL</strong> Threads are assumed to be sanctioned writers.
 * </ul>
 *
 * This class is left non-final to allow subclassing but since it is a
 * debugging tool, we make no guarantees about backward compatibility between
 * releases. It is meant to evolve as users discover new assertions to be added.
 *
 * @author James Lemieux
 */
public class DebugList<E> extends AbstractEventList<E> {

    /** A flag controlling whether we check locks before performing reads and writes. */
    private boolean lockCheckingEnabled;

    /** The Set of Threads that are allowed to read from this DebugList; empty Set indicates all Threads may read. */
    private final Set<Thread> sanctionedReaderThreads = new HashSet<Thread>();
    /** The Set of Threads that are allowed to write to this DebugList; empty Set indicates all Threads may write. */
    private final Set<Thread> sanctionedWriterThreads = new HashSet<Thread>();

    /** A delegate EventList that implements the actual List operations. */
    private final EventList<E> delegate;

    /** A special ReadWriteLock that reports the Threads that own the read lock and write lock at any given time. */
    private final DebugReadWriteLock debugReadWriteLock;

    /**
     * Constructs a DebugList which, by default, performs no debugging. It must
     * be customized after construction to turn on the assertions which make
     * sense for the list pipeline.
     */
    public DebugList() {
        this(null, new DebugReadWriteLock());
    }

    /**
     * Creates a {@link DebugList} using the specified
     * {@link ListEventPublisher} and {@link ReadWriteLock}. This is
     * particularly useful when multiple {@link DebugList}s are used within a
     * {@link CompositeList} and must share the same lock and publisher.
     */
    private DebugList(ListEventPublisher publisher, DebugReadWriteLock debugReadWriteLock) {
        this.debugReadWriteLock = debugReadWriteLock;

        // use a normal BasicEventList as the delegate implementation
        this.delegate = new BasicEventList<E>(publisher, this.debugReadWriteLock);
        this.delegate.addListEventListener(new ListEventForwarder());
    }

    /**
     * This private ListEventListener simply forwards updates to the delegate
     * BasicEventList since we're only decorating the BasicEventList.
     */
    private class ListEventForwarder implements ListEventListener<E> {
        public void listChanged(ListEvent<E> listChanges) {
            updates.forwardEvent(listChanges);
        }
    }

    /**
     * Returns <tt>true</tt> if DebugList is currently checking the calling
     * Thread for lock ownership before each read and write operation.
     */
    public boolean isLockCheckingEnabled() {
        return lockCheckingEnabled;
    }
    /**
     * If <code>lockCheckingEnabled</code> is <tt>true</tt> this DebugList will
     * check the calling Thread for lock ownership before each read and write
     * operation; <tt>false</tt> indicates this check shouldn't be performed.
     */
    public void setLockCheckingEnabled(boolean lockCheckingEnabled) {
        this.lockCheckingEnabled = lockCheckingEnabled;
    }

    /**
     * Returns the {@link Set} of Threads that are allowed to perform reads on
     * this DebugList. If the {@link Set} is empty, all Threads are allowed to
     * read from this DebugList. Users are expected to add and remove Threads
     * directly on this {@link Set}.
     */
    public Set<Thread> getSanctionedReaderThreads() {
        return sanctionedReaderThreads;
    }

    /**
     * Returns the {@link Set} of Threads that are allowed to perform writes on
     * this DebugList. If the {@link Set} is empty, all Threads are allowed to
     * write to this DebugList. Users are expected to add and remove Threads
     * directly on this {@link Set}.
     */
    public Set<Thread> getSanctionedWriterThreads() {
        return sanctionedWriterThreads;
    }

    /**
     * Returns a new empty {@link DebugList} which shares the same
     * {@link ListEventListener} and {@link ReadWriteLock} with this DebugList.
     * This method is particularly useful when debugging a {@link CompositeList}
     * where some member lists are DebugLists and thus must share an identical
     * publisher and locks in order to participate in the CompositeList.
     */
    public DebugList<E> createNewDebugList() {
        return new DebugList<E>(getPublisher(), debugReadWriteLock);
    }

    /**
     * This generic method is called immediately before any read operation is
     * invoked. All generic read assertions should take place here.
     */
    protected void beforeReadOperation() {
        // if a Set of reader Threads have been given, ensure the current Thread is one of them
        if (!sanctionedReaderThreads.isEmpty() && !sanctionedReaderThreads.contains(Thread.currentThread()))
            throw new IllegalStateException("DebugList detected an unexpected Thread (" + Thread.currentThread() + ") attempting to perform a read operation");

        // if lock checking is enabled, ensure the current Thread holds a read or write lock before continuing
        if (isLockCheckingEnabled() && !debugReadWriteLock.isThreadHoldingReadOrWriteLock())
            throw new IllegalStateException("DebugList detected a failure to acquire the readLock prior to a read operation");
    }

    /**
     * This method is currently a no-op and exists for parity. Subclasses may
     * choose to insert extract assertion logic here.
     */
    protected void afterReadOperation() { }

    /**
     * This generic method is called immediately before any write operation is
     * invoked. All generic write assertions should take place here.
     */
    protected void beforeWriteOperation() {
        // if a Set of writer Threads have been given, ensure the current Thread is one of them
        if (!sanctionedWriterThreads.isEmpty() && !sanctionedWriterThreads.contains(Thread.currentThread()))
            throw new IllegalStateException("DebugList detected an unexpected Thread (" + Thread.currentThread() + ") attempting to perform a write operation");

        // if lock checking is enabled, ensure the current Thread holds a write lock before continuing
        if (isLockCheckingEnabled() && !debugReadWriteLock.isThreadHoldingWriteLock())
            throw new IllegalStateException("DebugList detected a failure to acquire the writeLock prior to a write operation");
    }

    /**
     * This method is currently a no-op and exists for parity. Subclasses may
     * choose to insert extract assertion logic here.
     */
    protected void afterWriteOperation() { }

    /** {@inheritDoc} */
    public ReadWriteLock getReadWriteLock() {
        return delegate.getReadWriteLock();
    }

    /** {@inheritDoc} */
    public ListEventPublisher getPublisher() {
        return delegate.getPublisher();
    }

    /** {@inheritDoc} */
    public E get(int index) {
        beforeReadOperation();
        try {
            return delegate.get(index);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public int size() {
        beforeReadOperation();
        try {
            return delegate.size();
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean contains(Object object) {
        beforeReadOperation();
        try {
            return delegate.contains(object);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean containsAll(Collection<?> collection) {
        beforeReadOperation();
        try {
            return delegate.containsAll(collection);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean equals(Object object) {
        beforeReadOperation();
        try {
            return delegate.equals(object);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public int hashCode() {
        beforeReadOperation();
        try {
            return delegate.hashCode();
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public int indexOf(Object object) {
        beforeReadOperation();
        try {
            return delegate.indexOf(object);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public int lastIndexOf(Object object) {
        beforeReadOperation();
        try {
            return delegate.lastIndexOf(object);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        beforeReadOperation();
        try {
            return delegate.isEmpty();
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public Object[] toArray() {
        beforeReadOperation();
        try {
            return delegate.toArray();
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public <T> T[] toArray(T[] array) {
        beforeReadOperation();
        try {
            return delegate.toArray(array);
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public String toString() {
        beforeReadOperation();
        try {
            return delegate.toString();
        } finally {
            afterReadOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean add(E value) {
        beforeWriteOperation();
        try {
            return delegate.add(value);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean remove(Object toRemove) {
        beforeWriteOperation();
        try {
            return delegate.remove(toRemove);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean addAll(Collection<? extends E> values) {
        beforeWriteOperation();
        try {
            return delegate.addAll(values);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean addAll(int index, Collection<? extends E> values) {
        beforeWriteOperation();
        try {
            return delegate.addAll(index, values);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean removeAll(Collection<?> values) {
        beforeWriteOperation();
        try {
            return delegate.removeAll(values);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public boolean retainAll(Collection<?> values) {
        beforeWriteOperation();
        try {
            return delegate.retainAll(values);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public void clear() {
        beforeWriteOperation();
        try {
            delegate.clear();
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public E set(int index, E value) {
        beforeWriteOperation();
        try {
            return delegate.set(index, value);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public void add(int index, E value) {
        beforeWriteOperation();
        try {
            delegate.add(index, value);
        } finally {
            afterWriteOperation();
        }
    }

    /** {@inheritDoc} */
    public E remove(int index) {
        beforeWriteOperation();
        try {
            return delegate.remove(index);
        } finally {
            afterWriteOperation();
        }
    }

    /**
     * This special ReadWriteLock can answer the question of whether the current
     * Thread holds the read or write lock.
     */
    private static class DebugReadWriteLock implements ReadWriteLock {
        private final DebugLock readLock;
        private final DebugLock writeLock;

        public DebugReadWriteLock() {
            // decorate normaly read/write locks with Thread recording
            final ReadWriteLock decorated = LockFactory.DEFAULT.createReadWriteLock();
            this.readLock = new DebugLock(decorated.readLock());
            this.writeLock = new DebugLock(decorated.writeLock());
        }

        public Lock readLock() { return readLock; }
        public Lock writeLock() { return writeLock; }

        /**
         * Returns <tt>true</tt> if and only if the current Thread holds the
         * write lock.
         */
        public boolean isThreadHoldingWriteLock() {
            return writeLock.getThreadsHoldingLock().contains(Thread.currentThread());
        }

        /**
         * Returns <tt>true</tt> if the current Thread holds the read lock or
         * write lock.
         */
        public boolean isThreadHoldingReadOrWriteLock() {
            return readLock.getThreadsHoldingLock().contains(Thread.currentThread()) ||
                   writeLock.getThreadsHoldingLock().contains(Thread.currentThread());
        }

        /**
         * A special wrapper around a conventional Lock which tracks the
         * Threads that current hold the lock.
         */
        private static class DebugLock implements Lock {
            private final Lock delegate;
            private final List<Thread> threadsHoldingLock = new ArrayList<Thread>();

            public DebugLock(Lock delegate) {
                this.delegate = delegate;
            }

            public void lock() {
                delegate.lock();

                // record the current Thread as a lock holder
                threadsHoldingLock.add(Thread.currentThread());
            }

            public boolean tryLock() {
                final boolean success = delegate.tryLock();

                // if the lock was successfully acquired, record the current Thread as a lock holder
                if (success) threadsHoldingLock.add(Thread.currentThread());

                return success;
            }

            public void unlock() {
                delegate.unlock();

                // remove the current Thread as a lock holder
                threadsHoldingLock.remove(Thread.currentThread());
            }

            /**
             * Returns the List of Threads holding the lock.
             */
            public List<Thread> getThreadsHoldingLock() {
                return threadsHoldingLock;
            }
        }
    }
}