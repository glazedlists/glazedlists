/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the CompressableList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CompressableListTest extends TestCase {

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
        CompressableList sparseList = new CompressableList();
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

    /**
     * Verifies that the compressed index works.
     */
    public void testCompressedIndex() {
        CompressableList list = new CompressableList();
        list.add(null);
        list.add(null);
        list.add("A");
        list.add(null);
        list.add(null);
        list.add(null);
        list.add("B");
        list.add(null);
        list.add(null);
        list.add(null);
        list.add(null);

        // verify the lead works
        assertEquals(0, list.getCompressedIndex(1, false));
        assertEquals(-1, list.getCompressedIndex(1, true));

        // verify the found value works
        assertEquals(0, list.getCompressedIndex(2, false));
        assertEquals(0, list.getCompressedIndex(2, true));

        // verify a middle value works
        assertEquals(1, list.getCompressedIndex(4, false));
        assertEquals(0, list.getCompressedIndex(4, true));

        // verify a trailing value works
        assertEquals(2, list.getCompressedIndex(8, false));
        assertEquals(1, list.getCompressedIndex(8, true));

        // verify leading nulls works
        assertEquals(2, list.getLeadingNulls(0));
        assertEquals(3, list.getLeadingNulls(1));

        // verify trailing nulls works
        assertEquals(3, list.getTrailingNulls(0));
        assertEquals(4, list.getTrailingNulls(1));
    }
}
