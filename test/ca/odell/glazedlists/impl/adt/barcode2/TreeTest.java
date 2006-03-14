/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import ca.odell.glazedlists.GlazedListsTests;
import ca.odell.glazedlists.GlazedLists;

import java.util.*;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TreeTest extends TestCase {

    /** test values */
    private static List<String> colors = GlazedListsTests.stringToList("ABC");
    private static ListToByteCoder<String> coder = new ListToByteCoder<String>(colors);
    private static byte allColors = coder.colorsToByte(GlazedListsTests.stringToList("ABC"));
    private static byte a = coder.colorToByte("A");
    private static byte b = coder.colorToByte("B");
    private static byte c = coder.colorToByte("C");
    private static byte aOrB = (byte) (a | b);
    private static byte bOrC = (byte) (b | c);
    private static byte aOrC = (byte) (a | c);
    private static final String january = "January";
    private static final String february = "February";
    private static final String march = "March";
    private static final String april = "April";
    private static final String may = "May";

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
        for(int i = 0; i < 10; i++) {
            byte color = coder.colorToByte(colors.get(i % colors.size()));
            tree.add(tree.size(allColors), allColors, color, null, 5);
        }
        tree.remove(0, allColors, tree.size(allColors));
        assertEquals("", tree.asSequenceOfColors());

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
     * This tests a scenario where the trees used to become unbalanced
     * when the height didn't change when it became unbalanced.
     */
    public void testBalance() {
        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, b, null, 1);
        tree.add(0, allColors, c, null, 1);
        tree.add(2, allColors, c, null, 1);
        tree.add(0, allColors, b, null, 1);
        tree.add(2, allColors, a, null, 1);
        tree.add(5, allColors, a, null, 1);
        assertEquals("BCABCA", tree.asSequenceOfColors());
        tree.remove(0, allColors, 3);
        assertEquals("BCA", tree.asSequenceOfColors());
    }

    /**
     * We need to make sure that when we delete, we rebalance
     * in the right place. This tree rebalances on the opposite
     * side as is deleted.
     */
    public void testDeleteRebalance() {
        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, a, null, 1);
        tree.add(0, allColors, a, null, 1);
        tree.add(2, allColors, b, null, 1);
        tree.add(0, allColors, c, null, 1);
        tree.add(2, allColors, c, null, 1);
        tree.add(5, allColors, c, null, 1);
        tree.add(3, allColors, c, null, 1);
        assertEquals("CACCABC", tree.asSequenceOfColors());
        tree.remove(6, allColors, 1);
        assertEquals("CACCAB", tree.asSequenceOfColors());
    }

    /**
     * Insert a bunch of stuff randomly into the tree, and hope it works.
     */
    public void testRandomOperations() {
        Tree<String> tree = new Tree<String>(coder);
        List<String> expectedSequenceOfColors = new ArrayList<String>();

        Random dice = new Random(0);
        for(int i = 0; i < 1000; i++) {
            int operation = dice.nextInt(3);
            if(expectedSequenceOfColors.isEmpty() || operation <= 1) {
                int index = dice.nextInt(expectedSequenceOfColors.size() + 1);
                String color = coder.getColors().get(dice.nextInt(coder.getColors().size()));
                tree.add(index, allColors, coder.colorToByte(color), null, 1);
                expectedSequenceOfColors.add(index, color);

            } else if(operation == 2) {
                int index = dice.nextInt(expectedSequenceOfColors.size());
                tree.remove(index, allColors, 1);
                expectedSequenceOfColors.remove(index);

            }

            assertEquals(listToString(expectedSequenceOfColors, ""), tree.asSequenceOfColors());
        }
    }

    /**
     * Convert the list elements to a larger String, using the specified
     * delimeter between elements.
     */
    private String listToString(List<String> list, String delimiter) {
        StringBuffer result = new StringBuffer();
        for(Iterator<String> i = list.iterator(); i.hasNext(); ) {
            result.append(i.next());
            if(i.hasNext()) result.append(delimiter);
        }
        return result.toString();
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

    public void testSevenColors() {
        colors = GlazedListsTests.stringToList("ABCDEFG");
        coder = new ListToByteCoder<String>(colors);
        allColors = coder.colorsToByte(GlazedListsTests.stringToList("ABCDEFG"));
        final byte d = coder.colorToByte("D");
        final byte e = coder.colorToByte("E");
        final byte f = coder.colorToByte("F");
        final byte g = coder.colorToByte("G");

        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, a, null, 4);
        tree.add(4, allColors, b, null, 4);
        tree.add(8, allColors, c, null, 4);
        tree.add(12, allColors, d, null, 4);
        tree.add(16, allColors, e, null, 4);
        tree.add(20, allColors, f, null, 4);
        tree.add(24, allColors, g, null, 4);
        assertEquals("AAAABBBBCCCCDDDDEEEEFFFFGGGG", tree.asSequenceOfColors());
        assertEquals(4, tree.size(a));
        assertEquals(4, tree.size(b));
        assertEquals(4, tree.size(c));
        assertEquals(4, tree.size(d));
        assertEquals(4, tree.size(e));
        assertEquals(4, tree.size(f));
        assertEquals(4, tree.size(g));
    }

    public void testTreeAsList() {
        Tree<String> tree = new Tree<String>(coder);
        TreeAsList<String> treeAsList = new TreeAsList<String>(tree);
        List<String> expected = new ArrayList<String>();

        treeAsList.add(0, "A");
        expected.add(0, "A");
        treeAsList.add(1, "B");
        expected.add(1, "B");
        treeAsList.add(2, "C");
        expected.add(2, "C");
        treeAsList.add(3, "D");
        expected.add(3, "D");
        treeAsList.add(4, "E");
        expected.add(4, "E");
        assertEquals(treeAsList, expected);

        treeAsList.removeAll(GlazedListsTests.stringToList("AE"));
        expected.removeAll(GlazedListsTests.stringToList("AE"));

        assertEquals(treeAsList, expected);

        treeAsList.addAll(2, GlazedListsTests.stringToList("FGHIJKLMNOPQRSTU"));
        expected.addAll(2, GlazedListsTests.stringToList("FGHIJKLMNOPQRSTU"));
        assertEquals(treeAsList, expected);

        treeAsList.removeAll(GlazedListsTests.stringToList("DGJLOPU"));
        expected.removeAll(GlazedListsTests.stringToList("DGJLOPU"));
        assertEquals(treeAsList, expected);
    }

    /**
     * Make sure the iterator works for simple operations.
     */
    public void testTreeIterator() {
        Tree<String> tree = new Tree<String>(coder);
        tree.add(0, allColors, a, january, 3);
        tree.add(3, allColors, b, february, 4);
        tree.add(7, allColors, c, march, 3);
        tree.add(10, allColors, a, april, 2);
        tree.add(12, allColors, a, may, 2);
        tree.add(14, allColors, b, january, 1);

        TreeIterator<String> iterator = new TreeIterator<String>(tree);
        assertTrue(iterator.hasNext(allColors));
        assertTrue(iterator.hasNext(a));
        assertTrue(iterator.hasNext(aOrB));

        iterator.next(allColors);
        assertEquals(a, iterator.color());
        assertEquals(january, iterator.value());
        assertEquals(0, iterator.index(allColors));

        iterator.next(allColors);
        iterator.next(allColors);
        iterator.next(allColors);
        assertEquals(b, iterator.color());
        assertEquals(february, iterator.value());
        assertEquals(3, iterator.index(allColors));

        iterator.next(allColors);
        assertEquals(4, iterator.index(allColors));

        iterator.next(allColors);
        iterator.next(allColors);
        iterator.next(allColors);
        assertEquals(c, iterator.color());
        assertEquals(march, iterator.value());
        assertEquals(7, iterator.index(allColors));
        assertEquals(4, iterator.index(b));
        assertEquals(3, iterator.index(a));

        iterator.next(a);
        assertEquals(a, iterator.color());
        assertEquals(10, iterator.index(allColors));
        assertTrue(iterator.hasNext(b));
        assertTrue(iterator.hasNext(a));

        iterator.next(b);
        assertEquals(b, iterator.color());
        assertEquals(14, iterator.index(allColors));
        assertEquals(january, iterator.value());
        assertFalse(iterator.hasNext(allColors));
        assertFalse(iterator.hasNext(a));
        assertFalse(iterator.hasNext(b));
    }
}