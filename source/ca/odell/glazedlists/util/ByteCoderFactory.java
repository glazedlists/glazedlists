/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util;

// Glazed Lists in bytes
import ca.odell.glazedlists.io.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.io.*;

/**
 * A factory for creating some commonly used types of {@link ByteCoder}s.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ByteCoderFactory {

    /** Provide Singleton access for all ByteCoders with no internal state */
    private static ByteCoder serializableByteCoder = null;
    private static ByteCoder beanXMLByteCoder = null;

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private ByteCoderFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link ByteCoder} that encodes {@link java.io.Serializable Serializable}
     * Objects using an {@link java.io.ObjectOutputStream}.
     */
    public static ByteCoder serializable() {
        if(serializableByteCoder == null) serializableByteCoder = new SerializableByteCoder(); 
        return serializableByteCoder;
    }

    /**
     * Creates a {@link ByteCoder} that uses {@link java.beans.XMLEncoder XMLEncoder} and
     * {@link java.beans.XMLDecoder XMLDecoder} classes from java.beans. Encoded
     * Objects must be JavaBeans.
     */
    public static ByteCoder beanXML() {
        if(beanXMLByteCoder == null) beanXMLByteCoder = new BeanXMLByteCoder(); 
        return beanXMLByteCoder;
    }
}