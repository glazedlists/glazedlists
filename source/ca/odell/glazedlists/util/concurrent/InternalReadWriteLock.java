/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.concurrent;

/**
 * An internal read-write lock manages a pair of read-write locks. This allows
 * modification at two levels: at the source level and at the local/internal
 * level. By providing a new internal level, internal changes can be made without
 * requiring the full locking of the source level. This is done by locking the
 * source level with only a read-lock, while locking the local/internal level with
 * a write lock.
 *
 * <p>This is useful for classes such as {@link ca.odell.glazedlists.SortedList},
 * where changes to the internal structure occur without modifying the source
 * structure.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class InternalReadWriteLock implements ReadWriteLock {

    /** the locks to delegate to */
    private Lock readLock;
    private Lock writeLock;
    private Lock internalLock;

    /**
     * Creates a new InternalReadWriteLock that uses the specified locks for
     * the source and internal.
     */
    public InternalReadWriteLock(ReadWriteLock source, ReadWriteLock internal) {
        readLock = new LockPair(source.readLock(), internal.readLock());
        writeLock = new LockPair(source.writeLock(), internal.writeLock());
        internalLock = new LockPair(source.readLock(), internal.writeLock());
    }

    /**
     * Return the lock used for reading.
     */
    public Lock readLock() {
        return readLock;
    }

    /**
     * Return the lock used for writing.
     */
    public Lock writeLock() {
        return writeLock;
    }

    /**
     * Return the lock used for reading.
     */
    public Lock internalLock() {
        return internalLock;
    }
}


/**
 * A LockPair is a set of two locks that are locked and unlocked in
 * seqeunce. To prevent the locks from causing a deadlock, they are locked
 * in the order: ( first, second ), and unlocked in the order ( second, first ).
 */
class LockPair implements Lock {

    /** the locks to delegate to */
    private Lock first;
    private Lock second;

    /**
     * Creates a lock pair that uses the specified locks in sequence.
     */
    public LockPair(Lock first, Lock second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Acquires the lock.
     */
    public void lock() {
        first.lock();
        second.lock();
    }

    /**
     * Acquires the lock only if it is free at the time of invocation.
     */
    public boolean tryLock() {
        boolean firstSuccess = first.tryLock();
        if(!firstSuccess) return false;

        boolean secondSuccess = second.tryLock();
        if(!secondSuccess) {
            first.unlock();
            return false;
        }

        return true;
    }

    /**
     * Releases the lock.
     */
    public void unlock() {
        second.unlock();
        first.unlock();
    }
}
