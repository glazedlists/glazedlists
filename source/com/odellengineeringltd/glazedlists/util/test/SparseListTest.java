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
 * This test verifies that the SparseList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SparseListTest extends TestCase {
    
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
     * Tests to verify that the sparse list is consistent after a long
     * series of list operations.
     */
    public void testListOperations() {
        SparseList sparseList = new SparseList();
        List controlList = new ArrayList();
        
        // apply various operations to both lists
        for(int i = 0; i < 5000; i++) {
            int operation = random.nextInt(5);
            int index = controlList.isEmpty() ? 0 : random.nextInt(controlList.size());
            Object value = new Integer(random.nextInt());
            
            if(operation == 0 || controlList.isEmpty()) {
                sparseList.add(index, value);
                controlList.add(index, value);
            } else if(operation == 1) {
                sparseList.add(index, null);
                controlList.add(index, null);
            } else if(operation == 2) {
                sparseList.remove(index);
                controlList.remove(index);
            } else if(operation == 3) {
                sparseList.set(index, value);
                controlList.set(index, value);
            } else if(operation == 4) {
                sparseList.set(index, null);
                controlList.set(index, null);
            }
        }
        
        // verify the lists are equal
        assertEquals(controlList, sparseList);
        
        // obtain the compressed list
        List compressedControlList = new ArrayList();
        for(Iterator i = controlList.iterator(); i.hasNext(); ) {
            Object value = i.next();
            if(value != null) compressedControlList.add(value);
        }

        // verify the compressed lists are equal
        assertEquals(compressedControlList, sparseList.getCompressedList());
    }
}
