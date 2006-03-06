/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import junit.framework.TestCase;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

import ca.odell.glazedlists.GlazedListsTests;

/**
 * Make sure we can encode to bytes and back consistently.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListToByteCoderTest extends TestCase {

    public void testSymmetry() {
        List values = GlazedListsTests.stringToList("ABCDEFGH");
        ListToByteCoder coder = new ListToByteCoder(values);

        // color to byte
        byte a = coder.colorToByte("A");
        byte b = coder.colorToByte("B");
        byte c = coder.colorToByte("C");
        byte d = coder.colorToByte("D");
        byte e = coder.colorToByte("E");
        byte f = coder.colorToByte("F");
        byte g = coder.colorToByte("G");
        byte h = coder.colorToByte("H");

        // make sure all values are distinct
        Set<Byte> distinctValues = new HashSet<Byte>();
        assertTrue(distinctValues.add(new Byte(a)));
        assertTrue(distinctValues.add(new Byte(b)));
        assertTrue(distinctValues.add(new Byte(c)));
        assertTrue(distinctValues.add(new Byte(d)));
        assertTrue(distinctValues.add(new Byte(e)));
        assertTrue(distinctValues.add(new Byte(f)));
        assertTrue(distinctValues.add(new Byte(g)));
        assertTrue(distinctValues.add(new Byte(h)));
        assertEquals(8, distinctValues.size());

        // byte to color
        assertEquals("A", coder.byteToColor(a));
        assertEquals("B", coder.byteToColor(b));
        assertEquals("C", coder.byteToColor(c));
        assertEquals("D", coder.byteToColor(d));
        assertEquals("E", coder.byteToColor(e));
        assertEquals("F", coder.byteToColor(f));
        assertEquals("G", coder.byteToColor(g));
        assertEquals("H", coder.byteToColor(h));

        // colors to bytes
        byte abd = coder.colorsToByte(GlazedListsTests.stringToList("ABD"));
        byte abe = coder.colorsToByte(GlazedListsTests.stringToList("ABE"));
        byte abcde = coder.colorsToByte(GlazedListsTests.stringToList("ABCDE"));
        byte ae = coder.colorsToByte(GlazedListsTests.stringToList("AE"));
        byte gh = coder.colorsToByte(GlazedListsTests.stringToList("GH"));

        // make sure all values are still distinct
        assertTrue(distinctValues.add(new Byte(abd)));
        assertTrue(distinctValues.add(new Byte(abe)));
        assertTrue(distinctValues.add(new Byte(abcde)));
        assertTrue(distinctValues.add(new Byte(ae)));
        assertTrue(distinctValues.add(new Byte(gh)));

        // bytes to colors
        assertEquals(GlazedListsTests.stringToList("ABD"), coder.byteToColors(abd));
        assertEquals(GlazedListsTests.stringToList("ABE"), coder.byteToColors(abe));
        assertEquals(GlazedListsTests.stringToList("ABCDE"), coder.byteToColors(abcde));
        assertEquals(GlazedListsTests.stringToList("AE"), coder.byteToColors(ae));

    }
}