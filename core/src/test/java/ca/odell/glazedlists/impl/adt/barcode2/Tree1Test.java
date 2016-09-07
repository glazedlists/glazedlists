/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree1Test {

    /** test values */
    private static byte allColors = 1;

    /** for randomly choosing list indices */
    private Random random = new Random();

    /**
     * Make sure we can have a few unsorted elements in an otherwise ordered tree.
     */
    @Test
    public void testUnsortedElementInSortedTree() {
        SimpleTree<String> tree = new SimpleTree<String>();
        Element<String> e = tree.addInSortedOrder(Tree1Test.allColors, "E", 1);
        Element<String> g = tree.addInSortedOrder(Tree1Test.allColors, "G", 1);
        Element<String> i = tree.addInSortedOrder(Tree1Test.allColors, "I", 1);
        Element<String> k = tree.addInSortedOrder(Tree1Test.allColors, "K", 1);
        Element<String> m = tree.addInSortedOrder(Tree1Test.allColors, "M", 1);
        Element<String> o = tree.addInSortedOrder(Tree1Test.allColors, "O", 1);

        k.setSorted(Element.UNSORTED);
        k.set("A");

        Element<String> h = tree.addInSortedOrder(Tree1Test.allColors, "H", 1);
        Element<String> n = tree.addInSortedOrder(Tree1Test.allColors, "N", 1);

        List<String> asList = new SimpleTreeAsList<String>(tree);

        assertEquals(GlazedListsTests.stringToList("EGHIAMNO"), asList);
    }


    /**
     * Tests to verify that the SimpleTree is consistent after a long
     * series of list operations.
     */
    @Test
    public void testListOperations() {
        SimpleTree indexedTree = new SimpleTree();
        List controlList = new ArrayList();

        // apply various operations to both the list and the tree
        for(int i = 0; i < 30; i++) {
            int operation = random.nextInt(4);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());
            Object value = new Integer(random.nextInt(10));

            if(operation <= 1 || controlList.isEmpty()) {
                indexedTree.add(index, value, 1);
                controlList.add(index, value);
            } else if(operation == 2) {
                indexedTree.remove(index, 1);
                controlList.remove(index);
            }
        }

        // create a list from the elements of the SimpleTree
        List indexedTreeList = new ArrayList();
        for(SimpleTreeIterator i = new SimpleTreeIterator(indexedTree); i.hasNext(); ) {
            i.next();
            Element node = i.node();
            indexedTreeList.add(node.get());
        }

        // verify the lists are equal
        assertEquals(controlList, indexedTreeList);
    }


    /**
     * Tests to verify that the SimpleTree is consistent with multiple
     * entries that have the same value.
     */
    @Test
    public void testEqualValues() {
        SimpleTree indexedTree = new SimpleTree(GlazedLists.comparableComparator());

        int ACount = 0;
        int BCount = 0;
        int CCount = 0;
        int DCount = 0;
        int ECount = 0;

        // populate the list with 100 B's and 100 D's
        while(BCount < 100 || DCount < 100) {
            indexedTree.addInSortedOrder((byte)1, "B", 1);
            BCount++;
            indexedTree.addInSortedOrder((byte)1, "D", 1);
            DCount++;
        }

        // add 100 A's, 100 C's and 100 E's in random order
        while(ACount < 100 || CCount < 100 || ECount < 100) {
            int letter = random.nextInt(3);

            if(letter == 0 && ACount < 100) {
                indexedTree.addInSortedOrder((byte)1, "A", 1);
                ACount++;
            } else if(letter == 1 && CCount < 100) {
                indexedTree.addInSortedOrder((byte)1, "C", 1);
                CCount++;
            } else if(letter == 2 && ECount < 100) {
                indexedTree.addInSortedOrder((byte)1, "E", 1);
                ECount++;
            }
        }

        // remove the A's, C's and E's in random order
        while(ACount > 0 || CCount > 0 || ECount > 0) {
            int letter = random.nextInt(3);

            if(letter == 0 && ACount > 0) {
                int index = indexedTree.indexOfValue("A", true, false, (byte)1);
                indexedTree.remove(index, 1);
                ACount--;
            } else if(letter == 1 && CCount > 0) {
                int index = indexedTree.indexOfValue("C", true, false, (byte)1);
                indexedTree.remove(index, 1);
                CCount--;
            } else if(letter == 2 && ECount > 0) {
                int index = indexedTree.indexOfValue("E", true, false, (byte)1);
                indexedTree.remove(index, 1);
                ECount--;
            }
        }

        // verify the list contains only the original 100 Bs and 100 Ds
        for(SimpleTreeIterator i = new SimpleTreeIterator(indexedTree); i.hasNext(); ) {
            i.next();
            Element node = i.node();
            if(node.get().equals("B")) BCount--;
            else if(node.get().equals("D")) DCount--;
            else fail();
        }
        assertEquals(BCount, 0);
        assertEquals(DCount, 0);
    }

    @Test
    public void testIterators() {
        SimpleTree<String> tree = new SimpleTree<String>();
        tree.add(0, "A", 1);
        tree.add(1, "B", 1);
        tree.add(2, "C", 1);

        SimpleTreeIterator<String> iterator = new SimpleTreeIterator<String>(tree, 0, (byte)1);

        assertEquals(true, iterator.hasNext());
        iterator.next();
        assertEquals(0, iterator.index());
        assertEquals("A", iterator.value());
        assertEquals(true, iterator.hasNext());
        iterator.next();
        assertEquals(1, iterator.index());
        assertEquals("B", iterator.value());
        assertEquals(true, iterator.hasNext());
        iterator.next();
        assertEquals(2, iterator.index());
        assertEquals("C", iterator.value());
        assertEquals(false, iterator.hasNext());

//        assertEquals(true, iterator.hasPrevious());
//        assertEquals(2, iterator.previousIndex());
//        assertEquals("C", iterator.previous().getValue());
//        assertEquals(true, iterator.hasPrevious());
//        assertEquals(1, iterator.previousIndex());
//        assertEquals("B", iterator.previous().getValue());
//        assertEquals(true, iterator.hasPrevious());
//        assertEquals(0, iterator.previousIndex());
//        assertEquals("A", iterator.previous().getValue());
//        assertEquals(false, iterator.hasPrevious());

//        assertEquals(true, iterator.hasNext());
//        assertEquals(0, iterator.nextIndex());
//        assertEquals("A", iterator.next().getValue());
//        iterator.remove();
//        assertEquals(true, iterator.hasNext());
//        assertEquals(0, iterator.nextIndex());
//        assertEquals("B", iterator.next().getValue());
//        iterator.remove();
//        assertEquals(true, iterator.hasNext());
//        assertEquals(0, iterator.nextIndex());
//        assertEquals("C", iterator.next().getValue());
//        iterator.remove();
//        assertEquals(0, tree.size());
//        assertEquals(false, iterator.hasNext());
//        assertEquals(false, iterator.hasPrevious());
    }

    @Test
    public void testIndexOfEtc() {
        SimpleTree<String> tree = new SimpleTree<String>(GlazedLists.comparableComparator());
        tree.addInSortedOrder((byte)1, "B", 1);
        tree.addInSortedOrder((byte)1, "B", 1);
        tree.addInSortedOrder((byte)1, "B", 1);
        tree.addInSortedOrder((byte)1, "D", 1);
        tree.addInSortedOrder((byte)1, "D", 1);
        tree.addInSortedOrder((byte)1, "E", 1);

        assertEquals(0, tree.indexOfValue("B", true, false, (byte)1));
        assertEquals(2, tree.indexOfValue("B", false, false, (byte)1));
        assertEquals(0, tree.indexOfValue("B", true, true, (byte)1));

        assertEquals(3, tree.indexOfValue("D", true, false, (byte)1));
        assertEquals(4, tree.indexOfValue("D", false, false, (byte)1));
        assertEquals(3, tree.indexOfValue("D", true, true, (byte)1));

        assertEquals(5, tree.indexOfValue("E", true, false, (byte)1));
        assertEquals(5, tree.indexOfValue("E", false, false, (byte)1));
        assertEquals(5, tree.indexOfValue("E", true, true, (byte)1));

        assertEquals(-1, tree.indexOfValue("A", true, false, (byte)1));
        assertEquals(-1, tree.indexOfValue("A", false, false, (byte)1));
        assertEquals(0, tree.indexOfValue("A", true, true, (byte)1));

        assertEquals(-1, tree.indexOfValue("C", true, false, (byte)1));
        assertEquals(-1, tree.indexOfValue("C", false, false, (byte)1));
        assertEquals(3, tree.indexOfValue("C", true, true, (byte)1));

        assertEquals(-1, tree.indexOfValue("F", true, false, (byte)1));
        assertEquals(-1, tree.indexOfValue("F", false, false, (byte)1));
        assertEquals(6, tree.indexOfValue("F", true, true, (byte)1));
    }
}
