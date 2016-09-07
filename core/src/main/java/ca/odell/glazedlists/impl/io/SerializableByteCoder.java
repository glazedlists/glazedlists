/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.io.ByteCoder;

import java.io.*;

/**
 * A {@link ByteCoder} that uses {@link Serializable}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SerializableByteCoder implements ByteCoder {

    /** {@inheritDoc} */
    @Override
    public void encode(Object source, OutputStream target) throws IOException {
        ObjectOutputStream objectOut = new ObjectOutputStream(target);
        objectOut.writeObject(source);
        objectOut.close();
    }

    /** {@inheritDoc} */
    @Override
    public Object decode(InputStream source) throws IOException {
        try {
            ObjectInputStream objectIn = new ObjectInputStream(source);
            return objectIn.readObject();
        } catch(ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
}