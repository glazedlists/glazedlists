/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.util.*;
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
}
