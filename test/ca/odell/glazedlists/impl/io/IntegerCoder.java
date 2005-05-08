/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.io.*;
import java.io.*;

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
