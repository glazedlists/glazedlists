/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import java.beans.*;
import java.io.*;
// Glazed Lists' pluggable object to bytes interface
import ca.odell.glazedlists.io.ByteCoder;

/**
 * A {@link ByteCoder} that uses the {@link java.beans.XMLEncoder XMLEncoder} and
 * {@link java.beans.XMLDecoder XMLDecoder} classes from java.beans. Encoded Objects
 * must be JavaBeans.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanXMLByteCoder implements ByteCoder {

    /** {@inheritDoc} */
    public void encode(Object source, OutputStream target) throws IOException {
        XMLEncoder xmlOut = new XMLEncoder(target);
        xmlOut.writeObject(source);
        xmlOut.close();
    }
    
    /** {@inheritDoc} */
    public Object decode(InputStream source) throws IOException {
        XMLDecoder xmlIn = new XMLDecoder(source);
        return xmlIn.readObject();
    }
}