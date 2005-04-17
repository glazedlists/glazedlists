/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.io;

import java.util.*;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.io.*;
import ca.odell.glazedlists.event.*;
// NIO
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
// for being a JUnit test case
import junit.framework.*;
import ca.odell.glazedlists.*;
// regular expressions
import java.util.regex.*;
import java.text.ParseException;
// logging
import java.util.logging.*;
import java.text.ParseException;

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
