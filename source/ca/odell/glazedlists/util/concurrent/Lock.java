/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.concurrent;

/**
 * A lock is a tool for controlling access to a shared resource by multiple threads.
 *
 * <p>This interface is a back-port of the {@link java.util.concurrent.Locks.Lock}
 * class that first appeared in J2SE 1.5. Due to a requirement for sophisticated
 * concurrency, this interface has been back-ported for use in J2SE 1.4 (and greater).
 * It shares similar method signatures to be consistent with the J2SE 1.5 API.
 *
 * @see java.util.concurrent.locks.Lock
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/concurrent/locks/Lock.html">Lock</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Lock {

    /**
     * Acquires the lock.
     */
    public void lock();
    
    /**
     * Acquires the lock only if it is free at the time of invocation.
     */
    public boolean tryLock();
    
    /**
     * Releases the lock.
     */
    public void unlock();
}
