/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import ca.odell.glazedlists.GlazedListsTests;

import java.util.List;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeTest extends TestCase {

    public void testThreeColorInserts() {
        List<String> colors = GlazedListsTests.stringToList("ABC");
        ListToByteCoder<String> coder = new ListToByteCoder<String>(colors);
        String january = "January";
        String february = "February";
        String march = "March";
        String april = "April";
        String may = "May";

        Tree<String> tree = new Tree<String>(coder);

        byte allColors = coder.colorsToByte(GlazedListsTests.stringToList("ABC"));
        byte a = coder.colorToByte("A");
        byte b = coder.colorToByte("B");
        byte c = coder.colorToByte("C");
        byte aOrB = (byte) (a | b);
        byte bOrC = (byte) (b | c);
        byte aOrC = (byte) (a | c);

        Element<String> nodeB1 = tree.add(0, allColors, b, january, 5);
        Element<String> nodeA1 = tree.add(0, allColors, a, march, 5);
        Element<String> nodeC1 = tree.add(10, allColors, c, april, 5);
        Element<String> nodeB2 = tree.add(12, allColors, b, february, 3);

        assertEquals(5, tree.size(a));
        assertEquals(8, tree.size(b));
        assertEquals(5, tree.size(c));
        assertEquals(18, tree.size(allColors));
        assertEquals(13, tree.size(aOrB));
        assertEquals(13, tree.size(bOrC));
        assertEquals(10, tree.size(aOrC));

        assertEquals(0, tree.indexOf(nodeA1, allColors));
        assertEquals(5, tree.indexOf(nodeB1, allColors));
        assertEquals(10, tree.indexOf(nodeC1, allColors));
        assertEquals(12, tree.indexOf(nodeB2, allColors));

        assertEquals(5, tree.indexOf(nodeC1, b));
        assertEquals(2, tree.indexOf(nodeB2, c));
        assertEquals(10, tree.indexOf(nodeB2, aOrB));
        assertEquals(7, tree.indexOf(nodeB2, aOrC));
        assertEquals(7, tree.indexOf(nodeB2, bOrC));

        Element<String> nodeC3 = tree.add(12, allColors, c, april, 3);
        assertSame(nodeC1, nodeC3);
        Element<String> nodeA2 = tree.add(5, allColors, a, march, 1);
        assertSame(nodeA1, nodeA2);
        Element<String> nodeA3 = tree.add(0, allColors, a, march, 4);
        assertSame(nodeA1, nodeA3);

        Element<String> nodeB3 = tree.add(12, allColors, b, february, 5);
        assertNotSame(nodeB1, nodeB3);
        assertEquals("AAAAAAAAAABBBBBBBBBBCCCCCBBBCCC", tree.asSequenceOfColors());

        Element<String> nodeB4 = tree.add(4, allColors, b, may, 2);
        Element<String> nodeB5 = tree.add(7, allColors, b, may, 2);
        Element<String> nodeB6 = tree.add(10, allColors, b, may, 2);
        assertEquals("AAAABBABBABBAAAABBBBBBBBBBCCCCCBBBCCC", tree.asSequenceOfColors());

        //   A INDICES 0123  4  5  6789
        //   B INDICES     01 23 45    6789012345     678
        //   C INDICES                           01234   567
        // ALL INDICES 0123456789012345678901234567890123456
        //      VALUES AAAABBABBABBAAAABBBBBBBBBBCCCCCBBBCCC
        assertEquals(4, tree.indexOf(0, b, allColors));
        assertEquals(0, tree.indexOf(4, allColors, b));
        assertEquals(11, tree.indexOf(5, b, allColors));
        assertEquals(5, tree.indexOf(11, allColors, b));
        assertEquals(31, tree.indexOf(16, b, allColors));
        assertEquals(16, tree.indexOf(31, allColors, b));
        assertEquals(4, tree.indexOf(2, b, a));
        assertEquals(4, tree.indexOf(3, b, a));
        assertEquals(18, tree.indexOf(5, c, b));
        assertEquals(33, tree.indexOf(18, b, allColors));
        assertEquals(11, tree.indexOf(11, allColors, aOrB));
        assertEquals(11, tree.indexOf(11, aOrB, allColors));
        assertEquals(9, tree.indexOf(19, bOrC, a));
        assertEquals(36, tree.indexOf(7, c, allColors));
        assertEquals(10, tree.size(a));
        assertEquals(19, tree.size(b));
        assertEquals(8, tree.size(c));
        assertEquals(29, tree.size(aOrB));
        assertEquals(18, tree.size(aOrC));
        assertEquals(37, tree.size(allColors));
    }
}