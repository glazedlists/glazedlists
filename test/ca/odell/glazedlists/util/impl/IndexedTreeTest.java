/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.util.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the IndexedTree works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IndexedTreeTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random();

    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

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
        for(Iterator i = indexedTree.iterator(); i.hasNext(); ) {
            IndexedTreeNode node = (IndexedTreeNode)i.next();
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
        IndexedTree indexedTree = new IndexedTree(ComparatorFactory.comparable());

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
                indexedTree.getNode("A").removeFromTree();
                ACount--;
            } else if(letter == 1 && CCount > 0) {
                indexedTree.getNode("C").removeFromTree();
                CCount--;
            } else if(letter == 2 && ECount > 0) {
                indexedTree.getNode("E").removeFromTree();
                ECount--;
            }
        }

        // verify the list contains only the original 100 Bs and 100 Ds
        List indexedTreeList = new ArrayList();
        for(Iterator i = indexedTree.iterator(); i.hasNext(); ) {
            IndexedTreeNode node = (IndexedTreeNode)i.next();
            if(node.getValue().equals("B")) BCount--;
            else if(node.getValue().equals("D")) DCount--;
            else fail();
        }
        assertEquals(BCount, 0);
        assertEquals(DCount, 0);
    }
}
