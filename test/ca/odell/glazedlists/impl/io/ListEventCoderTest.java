/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.io.ByteCoder;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Tests the ListEventCoder.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventCoderTest extends TestCase {
    
    /** encodes java.lang.Integer */
    private ByteCoder intCoder = new IntegerCoder();

    /**
     * Tests that a list event can be encoded and decoded.
     */
    public void testEncodeDecode() throws IOException {
        // prepare the encoding list
        EventList toEncode = new BasicEventList();
        EventEncoderListener encoder = new EventEncoderListener(intCoder);
        toEncode.addListEventListener(encoder);
        
        // prepare the decoding list
        EventList toDecode = new BasicEventList();
        
        // change, encode, decode
        toEncode.add(new Integer(8));
        Bufferlo add8Encoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventToBytes.toListEvent(add8Encoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
        
        // multichange, encode, decode
        List addAll = Arrays.asList(new Object[] { new Integer(6), new Integer(7), new Integer(5), new Integer(3), new Integer(0), new Integer(9) });
        toEncode.addAll(addAll);
        Bufferlo addAllEncoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventToBytes.toListEvent(addAllEncoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
    }

    /**
     * Tests that a snapshot can be decoded.
     */
    public void testSnapshotDecode() throws IOException {
        // prepare the encoding list
        EventList toEncode = new BasicEventList();
        
        // prepare the decoding list
        EventList toDecode = new BasicEventList();
        toDecode.add(new Integer(1));
        toDecode.add(new Integer(2));
        toDecode.add(new Integer(4));
        
        // change, encode, decode
        List entireList = Arrays.asList(new Object[] { new Integer(8), new Integer(6), new Integer(7), new Integer(5), new Integer(3), new Integer(0), new Integer(9) });
        toEncode.addAll(entireList);
        Bufferlo entireListEncoding = ListEventToBytes.toBytes(toEncode, intCoder);
        ListEventToBytes.toListEvent(entireListEncoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
    }
}