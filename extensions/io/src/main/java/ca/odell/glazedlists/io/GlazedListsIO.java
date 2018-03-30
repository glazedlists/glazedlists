/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import ca.odell.glazedlists.impl.io.BeanXMLByteCoder;
import ca.odell.glazedlists.impl.io.SerializableByteCoder;

/**
 * A factory for creating all sorts of objects to be used with Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 *
 * @deprecated The io extension and its types are deprecated.
 *             This extension becomes unsupported and will be removed
 *             from the official distribution with the next major release.
 */
@Deprecated
public final class GlazedListsIO {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsIO() {
        throw new UnsupportedOperationException();
    }


    // ByteCoders // // // // // // // // // // // // // // // // // // // // //

    /** Provide Singleton access for all ByteCoders with no internal state */
    private static ByteCoder serializableByteCoder = new SerializableByteCoder();
    private static ByteCoder beanXMLByteCoder = new BeanXMLByteCoder();

    /**
     * Creates a {@link ByteCoder} that encodes {@link java.io.Serializable Serializable}
     * Objects using an {@link java.io.ObjectOutputStream}.
     */
    public static ByteCoder serializableByteCoder() {
        if(serializableByteCoder == null) serializableByteCoder = new SerializableByteCoder();
        return serializableByteCoder;
    }

    /**
     * Creates a {@link ByteCoder} that uses {@link java.beans.XMLEncoder XMLEncoder} and
     * {@link java.beans.XMLDecoder XMLDecoder} classes from java.beans. Encoded
     * Objects must be JavaBeans.
     */
    public static ByteCoder beanXMLByteCoder() {
        if(beanXMLByteCoder == null) beanXMLByteCoder = new BeanXMLByteCoder();
        return beanXMLByteCoder;
    }
}