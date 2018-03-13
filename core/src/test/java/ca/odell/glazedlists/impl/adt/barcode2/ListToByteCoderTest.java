/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Make sure we can encode to bytes and back consistently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListToByteCoderTest {

    @Test
    public void testSymmetry() {
        List<String> values = GlazedListsTests.stringToList("ABCDEFG");
        ListToByteCoder<String> coder = new ListToByteCoder<>(values);

        // color to byte
        byte a = coder.colorToByte("A");
        byte b = coder.colorToByte("B");
        byte c = coder.colorToByte("C");
        byte d = coder.colorToByte("D");
        byte e = coder.colorToByte("E");
        byte f = coder.colorToByte("F");
        byte g = coder.colorToByte("G");

        // make sure all values are distinct
        Set<Byte> distinctValues = new HashSet<>();
        assertTrue(distinctValues.add(new Byte(a)));
        assertTrue(distinctValues.add(new Byte(b)));
        assertTrue(distinctValues.add(new Byte(c)));
        assertTrue(distinctValues.add(new Byte(d)));
        assertTrue(distinctValues.add(new Byte(e)));
        assertTrue(distinctValues.add(new Byte(f)));
        assertTrue(distinctValues.add(new Byte(g)));
        assertEquals(7, distinctValues.size());

        // byte to color
        assertEquals("A", coder.byteToColor(a));
        assertEquals("B", coder.byteToColor(b));
        assertEquals("C", coder.byteToColor(c));
        assertEquals("D", coder.byteToColor(d));
        assertEquals("E", coder.byteToColor(e));
        assertEquals("F", coder.byteToColor(f));
        assertEquals("G", coder.byteToColor(g));

        // colors to bytes
        byte abd = coder.colorsToByte(GlazedListsTests.stringToList("ABD"));
        byte abe = coder.colorsToByte(GlazedListsTests.stringToList("ABE"));
        byte abcde = coder.colorsToByte(GlazedListsTests.stringToList("ABCDE"));
        byte ae = coder.colorsToByte(GlazedListsTests.stringToList("AE"));
        byte fg = coder.colorsToByte(GlazedListsTests.stringToList("FG"));

        // make sure all values are still distinct
        assertTrue(distinctValues.add(new Byte(abd)));
        assertTrue(distinctValues.add(new Byte(abe)));
        assertTrue(distinctValues.add(new Byte(abcde)));
        assertTrue(distinctValues.add(new Byte(ae)));
        assertTrue(distinctValues.add(new Byte(fg)));

        // bytes to colors
        assertEquals(GlazedListsTests.stringToList("ABD"), coder.byteToColors(abd));
        assertEquals(GlazedListsTests.stringToList("ABE"), coder.byteToColors(abe));
        assertEquals(GlazedListsTests.stringToList("ABCDE"), coder.byteToColors(abcde));
        assertEquals(GlazedListsTests.stringToList("AE"), coder.byteToColors(ae));

    }
}
