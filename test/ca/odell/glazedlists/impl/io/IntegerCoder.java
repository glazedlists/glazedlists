/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.io.ByteCoder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encodes integers and decodes them.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IntegerCoder implements ByteCoder {
    public void encode(Object source, OutputStream target) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(target);
        Integer sourceInt = (Integer)source;
        dataOut.writeInt(sourceInt.intValue());
        dataOut.flush();
    }
    public Object decode(InputStream source) throws IOException {
        DataInputStream dataIn = new DataInputStream(source);
        int value = dataIn.readInt();
        return new Integer(value);
    }
}
