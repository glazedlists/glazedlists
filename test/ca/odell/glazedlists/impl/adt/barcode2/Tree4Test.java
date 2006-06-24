// Copyright 2006 Google Inc. All Rights Reserved.

package ca.odell.glazedlists.impl.adt.barcode2;

import junit.framework.TestCase;

import java.util.*;

import ca.odell.glazedlists.GlazedListsTests;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree4Test extends TestCase {

    /** test values */
    private static List<String> colors = GlazedListsTests.stringToList("ABC");
    private static ListToByteCoder<String> coder = new ListToByteCoder<String>(Tree4Test.colors);
    private static byte allColors = Tree4Test.coder.colorsToByte(GlazedListsTests.stringToList("ABC"));
    private static byte a = Tree4Test.coder.colorToByte("A");
    private static byte b = Tree4Test.coder.colorToByte("B");
    private static byte c = Tree4Test.coder.colorToByte("C");
    private static byte aOrB = (byte) (Tree4Test.a | Tree4Test.b);
    private static byte bOrC = (byte) (Tree4Test.b | Tree4Test.c);
    private static byte aOrC = (byte) (Tree4Test.a | Tree4Test.c);
    private static final String january = "January";
    private static final String february = "February";
    private static final String march = "March";
    private static final String april = "April";
    private static final String may = "May";

    public void testThreeColorInserts() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);

        Element<String> nodeB1 = tree.add(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 5);
        Element<String> nodeA1 = tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.march, 5);
        Element<String> nodeC1 = tree.add(10, Tree4Test.allColors, Tree4Test.c, Tree4Test.april, 5);
        Element<String> nodeB2 = tree.add(12, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 3);

        assertEquals(5, tree.size(Tree4Test.a));
        assertEquals(8, tree.size(Tree4Test.b));
        assertEquals(5, tree.size(Tree4Test.c));
        assertEquals(18, tree.size(Tree4Test.allColors));
        assertEquals(13, tree.size(Tree4Test.aOrB));
        assertEquals(13, tree.size(Tree4Test.bOrC));
        assertEquals(10, tree.size(Tree4Test.aOrC));

        assertEquals(0, tree.indexOfNode(nodeA1, Tree4Test.allColors));
        assertEquals(5, tree.indexOfNode(nodeB1, Tree4Test.allColors));
        assertEquals(10, tree.indexOfNode(nodeC1, Tree4Test.allColors));
        assertEquals(12, tree.indexOfNode(nodeB2, Tree4Test.allColors));

        assertEquals(5, tree.indexOfNode(nodeC1, Tree4Test.b));
        assertEquals(2, tree.indexOfNode(nodeB2, Tree4Test.c));
        assertEquals(10, tree.indexOfNode(nodeB2, Tree4Test.aOrB));
        assertEquals(7, tree.indexOfNode(nodeB2, Tree4Test.aOrC));
        assertEquals(7, tree.indexOfNode(nodeB2, Tree4Test.bOrC));

        Element<String> nodeC3 = tree.add(12, Tree4Test.allColors, Tree4Test.c, Tree4Test.april, 3);
        assertSame(nodeC1, nodeC3);
        Element<String> nodeA2 = tree.add(5, Tree4Test.allColors, Tree4Test.a, Tree4Test.march, 1);
        assertSame(nodeA1, nodeA2);
        Element<String> nodeA3 = tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.march, 4);
        assertSame(nodeA1, nodeA3);

        Element<String> nodeB3 = tree.add(12, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 5);
        assertNotSame(nodeB1, nodeB3);
        assertEquals("AAAAAAAAAABBBBBBBBBBCCCCCBBBCCC", tree.asSequenceOfColors());

        Element<String> nodeB4 = tree.add(4, Tree4Test.allColors, Tree4Test.b, Tree4Test.may, 2);
        Element<String> nodeB5 = tree.add(7, Tree4Test.allColors, Tree4Test.b, Tree4Test.may, 2);
        Element<String> nodeB6 = tree.add(10, Tree4Test.allColors, Tree4Test.b, Tree4Test.may, 2);
        assertEquals("AAAABBABBABBAAAABBBBBBBBBBCCCCCBBBCCC", tree.asSequenceOfColors());

        //   A INDICES 0123  4  5  6789
        //   B INDICES     01 23 45    6789012345     678
        //   C INDICES                           01234   567
        // ALL INDICES 0123456789012345678901234567890123456
        //      VALUES AAAABBABBABBAAAABBBBBBBBBBCCCCCBBBCCC
        assertEquals(4, tree.convertIndexColor(0, Tree4Test.b, Tree4Test.allColors));
        assertEquals(0, tree.convertIndexColor(4, Tree4Test.allColors, Tree4Test.b));
        assertEquals(11, tree.convertIndexColor(5, Tree4Test.b, Tree4Test.allColors));
        assertEquals(5, tree.convertIndexColor(11, Tree4Test.allColors, Tree4Test.b));
        assertEquals(31, tree.convertIndexColor(16, Tree4Test.b, Tree4Test.allColors));
        assertEquals(16, tree.convertIndexColor(31, Tree4Test.allColors, Tree4Test.b));
        assertEquals(4, tree.convertIndexColor(2, Tree4Test.b, Tree4Test.a));
        assertEquals(4, tree.convertIndexColor(3, Tree4Test.b, Tree4Test.a));
        assertEquals(18, tree.convertIndexColor(5, Tree4Test.c, Tree4Test.b));
        assertEquals(33, tree.convertIndexColor(18, Tree4Test.b, Tree4Test.allColors));
        assertEquals(11, tree.convertIndexColor(11, Tree4Test.allColors, Tree4Test.aOrB));
        assertEquals(11, tree.convertIndexColor(11, Tree4Test.aOrB, Tree4Test.allColors));
        assertEquals(9, tree.convertIndexColor(19, Tree4Test.bOrC, Tree4Test.a));
        assertEquals(36, tree.convertIndexColor(7, Tree4Test.c, Tree4Test.allColors));
        assertEquals(10, tree.size(Tree4Test.a));
        assertEquals(19, tree.size(Tree4Test.b));
        assertEquals(8, tree.size(Tree4Test.c));
        assertEquals(29, tree.size(Tree4Test.aOrB));
        assertEquals(18, tree.size(Tree4Test.aOrC));
        assertEquals(37, tree.size(Tree4Test.allColors));
    }

    public void testRemoves() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);

        tree.add(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 5);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.march, 5);
        assertEquals("AAAAABBBBB", tree.asSequenceOfColors());

        tree.remove(0, Tree4Test.allColors, 3);
        assertEquals("AABBBBB", tree.asSequenceOfColors());
        assertEquals(2, tree.size(Tree4Test.a));
        assertEquals(5, tree.size(Tree4Test.b));

        tree.remove(0, Tree4Test.allColors, 2);
        assertEquals("BBBBB", tree.asSequenceOfColors());

        tree.remove(3, Tree4Test.allColors, 2);
        assertEquals("BBB", tree.asSequenceOfColors());

        tree.add(3, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 6);
        assertEquals("BBBBBBBBB", tree.asSequenceOfColors());

        tree.remove(0, Tree4Test.allColors, 3);
        tree.remove(3, Tree4Test.allColors, 3);
        tree.remove(1, Tree4Test.allColors, 1);
        assertEquals("BB", tree.asSequenceOfColors());

        tree.add(1, Tree4Test.allColors, Tree4Test.a, Tree4Test.february, 4);
        tree.add(5, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 3);
        tree.add(1, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 3);
        assertEquals("BBBBAAAABBBB", tree.asSequenceOfColors());

        tree.remove(4, Tree4Test.b, 4);
        assertEquals("BBBBAAAA", tree.asSequenceOfColors());

        tree.remove(2, Tree4Test.aOrB, 4);
        assertEquals("BBAA", tree.asSequenceOfColors());

        tree.remove(0, Tree4Test.aOrB, 4);
        assertEquals("", tree.asSequenceOfColors());
    }

    public void testRemovesFromCenter() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);

        tree.add(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 28);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.january, 31);
        tree.add(59, Tree4Test.allColors, Tree4Test.c, Tree4Test.march, 31);

        tree.remove(30, Tree4Test.allColors, 30);
        assertEquals(30, tree.size(Tree4Test.a));
        assertEquals(0, tree.size(Tree4Test.b));
        assertEquals(30, tree.size(Tree4Test.c));
    }

    public void testRemoveInBulk() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);

        // remove all nodes from the tree
        for(int i = 0; i < 10; i++) {
            byte color = Tree4Test.coder.colorToByte(Tree4Test.colors.get(i % Tree4Test.colors.size()));
            tree.add(tree.size(Tree4Test.allColors), Tree4Test.allColors, color, null, 5);
        }
        tree.remove(0, Tree4Test.allColors, tree.size(Tree4Test.allColors));
        assertEquals("", tree.asSequenceOfColors());

        // remove all nodes from the tree
        for(int i = 0; i < 100; i++) {
            byte color = Tree4Test.coder.colorToByte(Tree4Test.colors.get(i % Tree4Test.colors.size()));
            tree.add(tree.size(Tree4Test.allColors), Tree4Test.allColors, color, null, 5);
        }
        tree.remove(0, Tree4Test.allColors, tree.size(Tree4Test.allColors));
        assertEquals("", tree.asSequenceOfColors());

        // remove the nodes from the middle of the tree
        for(int i = 0; i < 10; i++) {
            byte color = Tree4Test.coder.colorToByte(Tree4Test.colors.get(i % Tree4Test.colors.size()));
            tree.add(tree.size(Tree4Test.allColors), Tree4Test.allColors, color, null, 2);
        }
        tree.remove(5, Tree4Test.allColors, 10);
        assertEquals("AABBCBCCAA", tree.asSequenceOfColors());
        tree.remove(0, Tree4Test.b, 3);
        assertEquals("AACCCAA", tree.asSequenceOfColors());
        tree.remove(1, Tree4Test.a, 2);
        assertEquals("ACCCA", tree.asSequenceOfColors());
        tree.remove(0, Tree4Test.aOrC, 5);
        assertEquals("", tree.asSequenceOfColors());
    }

    /**
     * This tests a scenario where the trees used to become unbalanced
     * when the height didn't change when it became unbalanced.
     */
    public void testBalance() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.b, null, 1);
        tree.add(0, Tree4Test.allColors, Tree4Test.c, null, 1);
        tree.add(2, Tree4Test.allColors, Tree4Test.c, null, 1);
        tree.add(0, Tree4Test.allColors, Tree4Test.b, null, 1);
        tree.add(2, Tree4Test.allColors, Tree4Test.a, null, 1);
        tree.add(5, Tree4Test.allColors, Tree4Test.a, null, 1);
        assertEquals("BCABCA", tree.asSequenceOfColors());
        tree.remove(0, Tree4Test.allColors, 3);
        assertEquals("BCA", tree.asSequenceOfColors());
    }

    /**
     * We need to make sure that when we delete, we rebalance
     * in the right place. This tree rebalances on the opposite
     * side as is deleted.
     */
    public void testDeleteRebalance() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, null, 1);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, null, 1);
        tree.add(2, Tree4Test.allColors, Tree4Test.b, null, 1);
        tree.add(0, Tree4Test.allColors, Tree4Test.c, null, 1);
        tree.add(2, Tree4Test.allColors, Tree4Test.c, null, 1);
        tree.add(5, Tree4Test.allColors, Tree4Test.c, null, 1);
        tree.add(3, Tree4Test.allColors, Tree4Test.c, null, 1);
        assertEquals("CACCABC", tree.asSequenceOfColors());
        tree.remove(6, Tree4Test.allColors, 1);
        assertEquals("CACCAB", tree.asSequenceOfColors());
    }

    /**
     * Insert a bunch of stuff randomly into the tree, and hope it works.
     */
    public void testRandomOperations() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        List<String> expectedSequenceOfColors = new ArrayList<String>();

        Random dice = new Random(0);
        for(int i = 0; i < 1000; i++) {
            int operation = dice.nextInt(3);
            if(expectedSequenceOfColors.isEmpty() || operation <= 1) {
                int index = dice.nextInt(expectedSequenceOfColors.size() + 1);
                String color = Tree4Test.coder.getColors().get(dice.nextInt(Tree4Test.coder.getColors().size()));
                tree.add(index, Tree4Test.allColors, Tree4Test.coder.colorToByte(color), null, 1);
                expectedSequenceOfColors.add(index, color);

            } else if(operation == 2) {
                int index = dice.nextInt(expectedSequenceOfColors.size());
                tree.remove(index, Tree4Test.allColors, 1);
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
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 4);
        tree.add(4, Tree4Test.allColors, Tree4Test.c, Tree4Test.march, 4);
        assertEquals("BBBBCCCC", tree.asSequenceOfColors());

        tree.remove(2, Tree4Test.bOrC, 6);
        assertEquals("BB", tree.asSequenceOfColors());
    }

    public void testGet() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 4);
        tree.add(4, Tree4Test.allColors, Tree4Test.c, Tree4Test.march, 4);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.january, 4);
        assertEquals("AAAABBBBCCCC", tree.asSequenceOfColors());
        assertEquals(Tree4Test.january, tree.get(0, Tree4Test.allColors).get());
        assertEquals(Tree4Test.january, tree.get(3, Tree4Test.allColors).get());
        assertEquals(Tree4Test.february, tree.get(4, Tree4Test.allColors).get());
        assertEquals(Tree4Test.february, tree.get(7, Tree4Test.allColors).get());
        assertEquals(Tree4Test.march, tree.get(8, Tree4Test.allColors).get());
        assertEquals(Tree4Test.march, tree.get(11, Tree4Test.allColors).get());
        assertEquals(Tree4Test.march, tree.get(2, Tree4Test.c).get());
        assertEquals(Tree4Test.february, tree.get(2, Tree4Test.b).get());
        assertEquals(Tree4Test.march, tree.get(4, Tree4Test.bOrC).get());
        tree.add(4, Tree4Test.b, Tree4Test.a, Tree4Test.april, 4);
        assertEquals("AAAABBBBAAAACCCC", tree.asSequenceOfColors());
        tree.add(8, Tree4Test.a, Tree4Test.b, Tree4Test.february, 4);
        assertEquals("AAAABBBBAAAABBBBCCCC", tree.asSequenceOfColors());
        assertEquals(Tree4Test.february, tree.get(12, Tree4Test.allColors).get());
        assertEquals(Tree4Test.april, tree.get(4, Tree4Test.a).get());
    }

//    public void testSevenColors() {
//        Tree4Test.colors = GlazedListsTests.stringToList("ABCDEFG");
//        Tree4Test.coder = new ListToByteCoder<String>(Tree4Test.colors);
//        Tree4Test.allColors = Tree4Test.coder.colorsToByte(GlazedListsTests.stringToList("ABCDEFG"));
//        final byte d = Tree4Test.coder.colorToByte("D");
//        final byte e = Tree4Test.coder.colorToByte("E");
//        final byte f = Tree4Test.coder.colorToByte("F");
//        final byte g = Tree4Test.coder.colorToByte("G");
//
//        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
//        tree.add(0, Tree4Test.allColors, Tree4Test.a, null, 4);
//        tree.add(4, Tree4Test.allColors, Tree4Test.b, null, 4);
//        tree.add(8, Tree4Test.allColors, Tree4Test.c, null, 4);
//        tree.add(12, Tree4Test.allColors, d, null, 4);
//        tree.add(16, Tree4Test.allColors, e, null, 4);
//        tree.add(20, Tree4Test.allColors, f, null, 4);
//        tree.add(24, Tree4Test.allColors, g, null, 4);
//        assertEquals("AAAABBBBCCCCDDDDEEEEFFFFGGGG", tree.asSequenceOfColors());
//        assertEquals(4, tree.size(Tree4Test.a));
//        assertEquals(4, tree.size(Tree4Test.b));
//        assertEquals(4, tree.size(Tree4Test.c));
//        assertEquals(4, tree.size(d));
//        assertEquals(4, tree.size(e));
//        assertEquals(4, tree.size(f));
//        assertEquals(4, tree.size(g));
//    }

    public void testTreeAsList() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        Tree4AsList<String> treeAsList = new Tree4AsList<String>(tree);
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

    public void testSetIndexIsCorrect() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.january, 13);
        tree.set(0, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 1);
        tree.set(2, Tree4Test.a, Tree4Test.b, Tree4Test.february, 1);
        tree.set(9, Tree4Test.a, Tree4Test.b, Tree4Test.february, 1);
        tree.set(7, Tree4Test.a, Tree4Test.b, Tree4Test.february, 1);
        assertEquals("BAABAAAAABABA", tree.asSequenceOfColors());
    }

    /**
     * Make sure the iterator works for simple operations.
     */
    public void testTreeIterator() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.add(0, Tree4Test.allColors, Tree4Test.a, Tree4Test.january, 3);
        tree.add(3, Tree4Test.allColors, Tree4Test.b, Tree4Test.february, 4);
        tree.add(7, Tree4Test.allColors, Tree4Test.c, Tree4Test.march, 3);
        tree.add(10, Tree4Test.allColors, Tree4Test.a, Tree4Test.april, 2);
        tree.add(12, Tree4Test.allColors, Tree4Test.a, Tree4Test.may, 2);
        tree.add(14, Tree4Test.allColors, Tree4Test.b, Tree4Test.january, 1);

        Tree4Iterator<String> iterator = new Tree4Iterator<String>(tree);
        assertTrue(iterator.hasNext(Tree4Test.allColors));
        assertTrue(iterator.hasNext(Tree4Test.a));
        assertTrue(iterator.hasNext(Tree4Test.aOrB));

        iterator.next(Tree4Test.allColors);
        assertEquals(Tree4Test.a, iterator.color());
        assertEquals(Tree4Test.january, iterator.value());
        assertEquals(0, iterator.index(Tree4Test.allColors));

        iterator.next(Tree4Test.allColors);
        iterator.next(Tree4Test.allColors);
        iterator.next(Tree4Test.allColors);
        assertEquals(Tree4Test.b, iterator.color());
        assertEquals(Tree4Test.february, iterator.value());
        assertEquals(3, iterator.index(Tree4Test.allColors));

        iterator.next(Tree4Test.allColors);
        assertEquals(4, iterator.index(Tree4Test.allColors));

        iterator.next(Tree4Test.allColors);
        iterator.next(Tree4Test.allColors);
        iterator.next(Tree4Test.allColors);
        assertEquals(Tree4Test.c, iterator.color());
        assertEquals(Tree4Test.march, iterator.value());
        assertEquals(7, iterator.index(Tree4Test.allColors));
        assertEquals(4, iterator.index(Tree4Test.b));
        assertEquals(3, iterator.index(Tree4Test.a));

        iterator.next(Tree4Test.a);
        assertEquals(Tree4Test.a, iterator.color());
        assertEquals(10, iterator.index(Tree4Test.allColors));
        assertTrue(iterator.hasNext(Tree4Test.b));
        assertTrue(iterator.hasNext(Tree4Test.a));

        iterator.next(Tree4Test.b);
        assertEquals(Tree4Test.b, iterator.color());
        assertEquals(14, iterator.index(Tree4Test.allColors));
        assertEquals(Tree4Test.january, iterator.value());
        assertFalse(iterator.hasNext(Tree4Test.allColors));
        assertFalse(iterator.hasNext(Tree4Test.a));
        assertFalse(iterator.hasNext(Tree4Test.b));
    }

    public void testSortedTree() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.addInSortedOrder(Tree4Test.a, Tree4Test.january, 1);
        tree.addInSortedOrder(Tree4Test.a, Tree4Test.february, 1);
        tree.addInSortedOrder(Tree4Test.a, Tree4Test.march, 1);
        tree.addInSortedOrder(Tree4Test.a, Tree4Test.april, 1);
        tree.addInSortedOrder(Tree4Test.a, Tree4Test.may, 1);

        assertEquals(Arrays.asList(new String[] { Tree4Test.april, Tree4Test.february, Tree4Test.january, Tree4Test.march, Tree4Test.may }), new Tree4AsList<String>(tree));
    }

    public void testSortedTreeIndexOf() {
        Tree4<String> tree = new Tree4<String>(Tree4Test.coder);
        tree.addInSortedOrder(Tree4Test.a, "B", 1);
        tree.addInSortedOrder(Tree4Test.a, "B", 1);
        tree.addInSortedOrder(Tree4Test.a, "C", 1);
        tree.addInSortedOrder(Tree4Test.a, "E", 1);
        tree.addInSortedOrder(Tree4Test.a, "F", 1);
        tree.addInSortedOrder(Tree4Test.a, "F", 1);
        tree.addInSortedOrder(Tree4Test.a, "G", 1);
        tree.addInSortedOrder(Tree4Test.a, "G", 1);
        tree.addInSortedOrder(Tree4Test.a, "G", 1);

        assertExpectedIndices(tree, "A", -1, -1, 0, 0);
        assertExpectedIndices(tree, "B", 0, 1, 0, 1);
        assertExpectedIndices(tree, "C", 2, 2, 2, 2);
        assertExpectedIndices(tree, "D", -1, -1, 3, 3);
        assertExpectedIndices(tree, "E", 3, 3, 3, 3);
        assertExpectedIndices(tree, "F", 4, 5, 4, 5);
        assertExpectedIndices(tree, "G", 6, 8, 6, 8);
    }

    private static <V> void assertExpectedIndices(Tree4<V> tree, V value, int first, int last, int firstSimulated, int lastSimulated) {
        assertEquals("" + value, first, tree.indexOfValue(value, true, false, allColors));
        assertEquals("" + value, last, tree.indexOfValue(value, false, false, allColors));
        assertEquals("" + value, firstSimulated, tree.indexOfValue(value, true, true, allColors));
        assertEquals("" + value, lastSimulated, tree.indexOfValue(value, false, true, allColors));
    }
}
