/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.concurrent;

/**
 * A ReadWriteLock maintains a pair of associated locks, one for read-only operations
 * and one for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive.
 *
 * <p>This interface is a back-port of the {@link java.util.concurrent.Locks.ReadWriteLock}
 * class that first appeared in J2SE 1.5. Due to a requirement for sophisticated
 * concurrency, this interface has been back-ported for use in J2SE 1.4 (and greater).
 * It shares similar method signatures to be consistent with the J2SE 1.5 API.
 *
 * @see java.util.concurrent.locks.ReadWriteLock
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/locks/ReadWriteLock.html">ReadWriteLock</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface ReadWriteLock {

    /**
     * Return the lock used for reading.
     */
    public Lock readLock();

    /**
     * Return the lock used for writing.
     */
    public Lock writeLock();
}

