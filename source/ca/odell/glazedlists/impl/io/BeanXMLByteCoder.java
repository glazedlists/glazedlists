/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.io.ByteCoder;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link ByteCoder} that uses the {@link java.beans.XMLEncoder XMLEncoder} and
 * {@link java.beans.XMLDecoder XMLDecoder} classes from java.beans. Encoded Objects
 * must be JavaBeans.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanXMLByteCoder implements ByteCoder {

    /** {@inheritDoc} */
    @Override
    public void encode(Object source, OutputStream target) throws IOException {
        XMLEncoder xmlOut = new XMLEncoder(target);
        xmlOut.writeObject(source);
        xmlOut.close();
    }

    /** {@inheritDoc} */
    @Override
    public Object decode(InputStream source) throws IOException {
        XMLDecoder xmlIn = new XMLDecoder(source);
        return xmlIn.readObject();
    }
}