/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.io;

import java.util.*;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.util.impl.*;
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
 * Tests the ListEventCoder.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventCoderTest extends TestCase {
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }
    
    /**
     * Tests that a list event can be encoded and decoded.
     */
    public void testEncodeDecode() throws IOException {
        ByteCoder intCoder = new IntCoder();
        
        // prepare the encoding list
        EventList toEncode = new BasicEventList();
        EventEncoderListener encoder = new EventEncoderListener(intCoder);
        toEncode.addListEventListener(encoder);
        
        // prepare the decoding list
        EventList toDecode = new BasicEventList();
        
        // change, encode, decode
        toEncode.add(new Integer(8));
        Bufferlo add8Encoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventCoder.bytesToListEvent(add8Encoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
        
        // multichange, encode, decode
        List addAll = Arrays.asList(new Object[] { new Integer(6), new Integer(7), new Integer(5), new Integer(3), new Integer(0), new Integer(9) });
        toEncode.addAll(addAll);
        Bufferlo addAllEncoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventCoder.bytesToListEvent(addAllEncoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
    }

    
    /**
     * Encodes List events as they arrive.
     */
    class EventEncoderListener implements ListEventListener {
        public List encodings = new ArrayList();
        public ByteCoder byteCoder = null;
        public EventEncoderListener(ByteCoder byteCoder) {
            this.byteCoder = byteCoder;
        }
        public void listChanged(ListEvent listChanges) {
            try {
                Bufferlo encoding = ListEventCoder.listEventToBytes(listChanges, byteCoder);
                encodings.add(encoding);
            } catch(IOException e) {
                fail(e.getMessage());
            }
        }
        public List getEncodings() {
            return encodings;
        }
    }
    
    /**
     * Encodes integers and decodes them.
     */
    class IntCoder implements ByteCoder {
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
}
