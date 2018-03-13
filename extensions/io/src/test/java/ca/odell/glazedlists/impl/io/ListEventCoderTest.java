/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.io;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.io.ByteCoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the ListEventCoder.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListEventCoderTest {

    /** encodes java.lang.Integer */
    private ByteCoder intCoder = new IntegerCoder();

    /**
     * Tests that a list event can be encoded and decoded.
     */
    @Test
    public void testEncodeDecode() throws IOException {
        // prepare the encoding list
        EventList<Integer> toEncode = new BasicEventList<>();
        EventEncoderListener encoder = new EventEncoderListener(intCoder);
        toEncode.addListEventListener(encoder);

        // prepare the decoding list
        EventList<Integer> toDecode = new BasicEventList<>();

        // change, encode, decode
        toEncode.add(new Integer(8));
        Bufferlo add8Encoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventToBytes.toListEvent(add8Encoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);

        // multichange, encode, decode
        List<Integer> addAll = Arrays.asList(6, 7, 5, 3, 0, 9);
        toEncode.addAll(addAll);
        Bufferlo addAllEncoding = (Bufferlo)encoder.getEncodings().remove(0);
        ListEventToBytes.toListEvent(addAllEncoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
    }

    /**
     * Tests that a snapshot can be decoded.
     */
    @Test
    public void testSnapshotDecode() throws IOException {
        // prepare the encoding list
        EventList<Integer> toEncode = new BasicEventList<>();

        // prepare the decoding list
        EventList<Integer> toDecode = new BasicEventList<>();
        toDecode.add(new Integer(1));
        toDecode.add(new Integer(2));
        toDecode.add(new Integer(4));

        // change, encode, decode
        List<Integer> entireList = Arrays.asList(8, 6, 7, 5, 3, 0, 9);
        toEncode.addAll(entireList);
        Bufferlo entireListEncoding = ListEventToBytes.toBytes(toEncode, intCoder);
        ListEventToBytes.toListEvent(entireListEncoding, toDecode, intCoder);
        assertEquals(toEncode, toDecode);
    }
}
