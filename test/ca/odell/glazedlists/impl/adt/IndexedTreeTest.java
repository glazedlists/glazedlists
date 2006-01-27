/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the IndexedTree works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IndexedTreeTest extends TestCase {

    /** for randomly choosing list indices */
    private Random random = new Random();

    /**
     * Tests to verify that the IndexedTree is consistent after a long
     * series of list operations.
     */
    public void testListOperations() {
        IndexedTree indexedTree = new IndexedTree();
        List controlList = new ArrayList();

        // apply various operations to both the list and the tree
        for(int i = 0; i < 30; i++) {
            int operation = random.nextInt(4);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());
            Object value = new Integer(random.nextInt(10));

            if(operation <= 1 || controlList.isEmpty()) {
                indexedTree.addByNode(index, value);
                controlList.add(index, value);
            } else if(operation == 2) {
                indexedTree.removeByIndex(index);
                controlList.remove(index);
            }
        }

        // create a list from the elements of the IndexedTree
        List indexedTreeList = new ArrayList();
        for(IndexedTreeIterator i = indexedTree.iterator(0); i.hasNext(); ) {
            IndexedTreeNode node = i.next();
            indexedTreeList.add(node.getValue());
        }

        // verify the lists are equal
        assertEquals(controlList, indexedTreeList);
    }


    /**
     * Tests to verify that the IndexedTree is consistent with multiple
     * entries that have the same value.
     */
    public void testEqualValues() {
        IndexedTree indexedTree = new IndexedTree(GlazedLists.comparableComparator());

        int ACount = 0;
        int BCount = 0;
        int CCount = 0;
        int DCount = 0;
        int ECount = 0;

        // populate the list with 100 B's and 100 D's
        while(BCount < 100 || DCount < 100) {
            indexedTree.addByNode("B");
            BCount++;
            indexedTree.addByNode("D");
            DCount++;
        }

        // add 100 A's, 100 C's and 100 E's in random order
        while(ACount < 100 || CCount < 100 || ECount < 100) {
            int letter = random.nextInt(3);

            if(letter == 0 && ACount < 100) {
                indexedTree.addByNode("A");
                ACount++;
            } else if(letter == 1 && CCount < 100) {
                indexedTree.addByNode("C");
                CCount++;
            } else if(letter == 2 && ECount < 100) {
                indexedTree.addByNode("E");
                ECount++;
            }
        }

        // remove the A's, C's and E's in random order
        while(ACount > 0 || CCount > 0 || ECount > 0) {
            int letter = random.nextInt(3);

            if(letter == 0 && ACount > 0) {
                indexedTree.getNode("A").removeFromTree(indexedTree);
                ACount--;
            } else if(letter == 1 && CCount > 0) {
                indexedTree.getNode("C").removeFromTree(indexedTree);
                CCount--;
            } else if(letter == 2 && ECount > 0) {
                indexedTree.getNode("E").removeFromTree(indexedTree);
                ECount--;
            }
        }

        // verify the list contains only the original 100 Bs and 100 Ds
        for(IndexedTreeIterator i = indexedTree.iterator(0); i.hasNext(); ) {
            IndexedTreeNode node = i.next();
            if(node.getValue().equals("B")) BCount--;
            else if(node.getValue().equals("D")) DCount--;
            else fail();
        }
        assertEquals(BCount, 0);
        assertEquals(DCount, 0);
    }

    public void testIterators() {
        IndexedTree<String> tree = new IndexedTree<String>();
        tree.addByNode(0, "A");
        tree.addByNode(1, "B");
        tree.addByNode(2, "C");

        IndexedTreeIterator<String> iterator = new IndexedTreeIterator<String>(tree, 0);

        assertEquals(true, iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertEquals("A", iterator.next().getValue());
        assertEquals(true, iterator.hasNext());
        assertEquals(1, iterator.nextIndex());
        assertEquals("B", iterator.next().getValue());
        assertEquals(true, iterator.hasNext());
        assertEquals(2, iterator.nextIndex());
        assertEquals("C", iterator.next().getValue());
        assertEquals(false, iterator.hasNext());

        assertEquals(true, iterator.hasPrevious());
        assertEquals(2, iterator.previousIndex());
        assertEquals("C", iterator.previous().getValue());
        assertEquals(true, iterator.hasPrevious());
        assertEquals(1, iterator.previousIndex());
        assertEquals("B", iterator.previous().getValue());
        assertEquals(true, iterator.hasPrevious());
        assertEquals(0, iterator.previousIndex());
        assertEquals("A", iterator.previous().getValue());
        assertEquals(false, iterator.hasPrevious());

        assertEquals(true, iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertEquals("A", iterator.next().getValue());
        iterator.remove();
        assertEquals(true, iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertEquals("B", iterator.next().getValue());
        iterator.remove();
        assertEquals(true, iterator.hasNext());
        assertEquals(0, iterator.nextIndex());
        assertEquals("C", iterator.next().getValue());
        iterator.remove();
        assertEquals(0, tree.size());
        assertEquals(false, iterator.hasNext());
        assertEquals(false, iterator.hasPrevious());
    }

    public void testIndexOfEtc() {
        IndexedTree<String> tree = new IndexedTree<String>(GlazedLists.comparableComparator());
        tree.addByNode("B");
        tree.addByNode("B");
        tree.addByNode("B");
        tree.addByNode("D");
        tree.addByNode("D");
        tree.addByNode("E");

        assertEquals(0, tree.indexOf("B"));
        assertEquals(2, tree.lastIndexOf("B"));
        assertEquals(0, tree.indexOfSimulated("B"));

        assertEquals(3, tree.indexOf("D"));
        assertEquals(4, tree.lastIndexOf("D"));
        assertEquals(3, tree.indexOfSimulated("D"));

        assertEquals(5, tree.indexOf("E"));
        assertEquals(5, tree.lastIndexOf("E"));
        assertEquals(5, tree.indexOfSimulated("E"));

        assertEquals(-1, tree.indexOf("A"));
        assertEquals(-1, tree.lastIndexOf("A"));
        assertEquals(0, tree.indexOfSimulated("A"));

        assertEquals(-1, tree.indexOf("C"));
        assertEquals(-1, tree.lastIndexOf("C"));
        assertEquals(3, tree.indexOfSimulated("C"));

        assertEquals(-1, tree.indexOf("F"));
        assertEquals(-1, tree.lastIndexOf("F"));
        assertEquals(6, tree.indexOfSimulated("F"));
    }
}