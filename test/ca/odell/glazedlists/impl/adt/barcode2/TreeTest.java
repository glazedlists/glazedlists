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

    /** test values */
    private static List<String> colors = GlazedListsTests.stringToList("ABC");
    private static ListToByteCoder<String> coder = new ListToByteCoder<String>(colors);
    private static final String january = "January";
    private static final String february = "February";
    private static final String march = "March";
    private static final String april = "April";
    private static final String may = "May";
    private static final byte allColors = coder.colorsToByte(GlazedListsTests.stringToList("ABC"));
    private static final byte a = coder.colorToByte("A");
    private static final byte b = coder.colorToByte("B");
    private static final byte c = coder.colorToByte("C");
    private static final byte aOrB = (byte) (a | b);
    private static final byte bOrC = (byte) (b | c);
    private static final byte aOrC = (byte) (a | c);

    public void testThreeColorInserts() {
        Tree<String> tree = new Tree<String>(coder);

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

    public void testRemoves() {
        Tree<String> tree = new Tree<String>(coder);

        tree.add(0, allColors, b, january, 5);
        tree.add(0, allColors, a, march, 5);
        assertEquals("AAAAABBBBB", tree.asSequenceOfColors());

        tree.remove(0, allColors, 3);
        assertEquals("AABBBBB", tree.asSequenceOfColors());
        assertEquals(2, tree.size(a));
        assertEquals(5, tree.size(b));

        tree.remove(0, allColors, 2);
        assertEquals("BBBBB", tree.asSequenceOfColors());

        tree.remove(3, allColors, 2);
        assertEquals("BBB", tree.asSequenceOfColors());

        tree.add(3, allColors, b, january, 6);
        assertEquals("BBBBBBBBB", tree.asSequenceOfColors());

        tree.remove(0, allColors, 3);
        tree.remove(3, allColors, 3);
        tree.remove(1, allColors, 1);
        assertEquals("BB", tree.asSequenceOfColors());

        tree.add(1, allColors, a, february, 4);
        tree.add(5, allColors, b, january, 3);
        tree.add(1, allColors, b, january, 3);
        assertEquals("BBBBAAAABBBB", tree.asSequenceOfColors());

        tree.remove(4, b, 4);
        assertEquals("BBBBAAAA", tree.asSequenceOfColors());

        tree.remove(2, aOrB, 4);
        assertEquals("BBAA", tree.asSequenceOfColors());

        tree.remove(0, aOrB, 4);
        assertEquals("", tree.asSequenceOfColors());
    }

    public void testRemovesFromCenter() {
        Tree<String> tree = new Tree<String>(coder);

        tree.add(0, allColors, b, february, 28);
        tree.add(0, allColors, a, january, 31);
        tree.add(59, allColors, c, march, 31);

        tree.remove(30, allColors, 30);
        assertEquals(30, tree.size(a));
        assertEquals(0, tree.size(b));
        assertEquals(30, tree.size(c));
    }

    public void testRemoveInBulk() {
        Tree<String> tree = new Tree<String>(coder);

        // remove all nodes from the tree
        for(int i = 0; i < 100; i++) {
            byte color = coder.colorToByte(colors.get(i % colors.size()));
            tree.add(tree.size(allColors), allColors, color, null, 5);
        }
        tree.remove(0, allColors, tree.size(allColors));
        assertEquals("", tree.asSequenceOfColors());

        // remove the nodes from the middle of the tree
        for(int i = 0; i < 10; i++) {
            byte color = coder.colorToByte(colors.get(i % colors.size()));
            tree.add(tree.size(allColors), allColors, color, null, 2);
        }
        tree.remove(5, allColors, 10);
        assertEquals("AABBCBCCAA", tree.asSequenceOfColors());
        tree.remove(0, b, 3);
        assertEquals("AACCCAA", tree.asSequenceOfColors());
        tree.remove(1, a, 2);
        assertEquals("ACCCA", tree.asSequenceOfColors());
        tree.remove(0, aOrC, 5);
        assertEquals("", tree.asSequenceOfColors());
    }

    /**
     * Remove such that the centre node is completely removed, and the
     * right node shifts in to take its place, but we want to continue to
     * remove on the right hand side.
     */
    public void testRemoveCentreShiftRight() {
        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, b, february, 4);
        tree.add(4, allColors, c, march, 4);
        assertEquals("BBBBCCCC", tree.asSequenceOfColors());

        tree.remove(2, bOrC, 6);
        assertEquals("BB", tree.asSequenceOfColors());
    }

    public void testGet() {
        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, b, february, 4);
        tree.add(4, allColors, c, march, 4);
        tree.add(0, allColors, a, january, 4);
        assertEquals("AAAABBBBCCCC", tree.asSequenceOfColors());
        assertEquals(january, tree.get(0, allColors).get());
        assertEquals(january, tree.get(3, allColors).get());
        assertEquals(february, tree.get(4, allColors).get());
        assertEquals(february, tree.get(7, allColors).get());
        assertEquals(march, tree.get(8, allColors).get());
        assertEquals(march, tree.get(11, allColors).get());
        assertEquals(march, tree.get(2, c).get());
        assertEquals(february, tree.get(2, b).get());
        assertEquals(march, tree.get(4, bOrC).get());
        tree.add(4, b, a, april, 4);
        assertEquals("AAAABBBBAAAACCCC", tree.asSequenceOfColors());
        tree.add(8, a, b, february, 4);
        assertEquals("AAAABBBBAAAABBBBCCCC", tree.asSequenceOfColors());
        assertEquals(february, tree.get(12, allColors).get());
        assertEquals(april, tree.get(4, a).get());
    }
}