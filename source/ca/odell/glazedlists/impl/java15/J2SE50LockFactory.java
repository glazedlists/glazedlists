/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.java15;

import ca.odell.glazedlists.impl.SerializedReadWriteLock;
import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An implementation of {@link LockFactory} that has been derived from
 * {@link java.util.concurrent.locks.ReadWriteLock JDK 1.5 Locks}.
 *
 * @author James Lemieux
 */
public class J2SE50LockFactory implements LockFactory {
    @Override
    public ReadWriteLock createReadWriteLock() {
        return new J2SE50ReadWriteLock();
    }

    @Override
    public Lock createLock() {
        return new LockAdapter(new java.util.concurrent.locks.ReentrantLock());
    }
}

/**
 * A ReadWriteLock implementation that is compatable with J2SE 5.0 and better. This
 * implementation is a facade over {@link java.util.concurrent.locks.ReadWriteLock}.
 *
 * @author James Lemieux
 */
final class J2SE50ReadWriteLock implements ReadWriteLock, Serializable {

    /** For versioning as a {@link Serializable} */
    private static final long serialVersionUID = 188277016505951193L;

    private transient final Lock readLock;
    private transient final Lock writeLock;

    J2SE50ReadWriteLock() {
        final java.util.concurrent.locks.ReadWriteLock delegate = new ReentrantReadWriteLock();
        this.readLock = new LockAdapter(delegate.readLock());
        this.writeLock = new LockAdapter(delegate.writeLock());
    }

    /** Use a {@link SerializedReadWriteLock} as a placeholder in the serialization stream. */
    private Object writeReplace() throws ObjectStreamException {
        return new SerializedReadWriteLock();
    }

    /**
     * Return the lock used for reading.
     */
    @Override
    public Lock readLock() {
        return this.readLock;
    }

    /**
     * Return the lock used for writing.
     */
    @Override
    public Lock writeLock() {
        return this.writeLock;
    }
}

/**
 * This adapts a J2SE 5.0 compatible Lock to the Glazed Lists Lock interface.
 *
 * @author James Lemieux
 */
final class LockAdapter implements Lock {

    private final java.util.concurrent.locks.Lock delegateLock;

    LockAdapter(java.util.concurrent.locks.Lock delegateLock) {
        this.delegateLock = delegateLock;
    }

    @Override
    public void lock() {
        delegateLock.lock();
    }

    @Override
    public boolean tryLock() {
        return delegateLock.tryLock();
    }

    @Override
    public void unlock() {
        delegateLock.unlock();
    }
}