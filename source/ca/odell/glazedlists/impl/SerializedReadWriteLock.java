/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A ReadWriteLock dummy implementation that's only used for Java object
 * serialization. The regular lock implementations for Java 1.4 and 1.5 are
 * representated by this class on the serialization stream. Upon
 * deserialization on the target JVM, an appropriate lock implementation is
 * reconstructed according to the capabilities of the target platform.
 *  
 * @author Holger Brands
 */
public final class SerializedReadWriteLock implements ReadWriteLock, Serializable {

    /** For versioning as a {@link Serializable} */
    private static final long serialVersionUID = -8627867501684280198L;

    /** {@inheritDoc} */
    public Lock readLock() {
        throw new UnsupportedOperationException("SerializedReadWriteLock is only used for serialization");
    }

    /** {@inheritDoc} */
    public Lock writeLock() {
        throw new UnsupportedOperationException("SerializedReadWriteLock is only used for serialization");
    }

    /** Recreate an appropriate lock implementation when deserialized on the target JVM. */
    private Object readResolve() throws ObjectStreamException {
        return LockFactory.DEFAULT.createReadWriteLock();
    }
}