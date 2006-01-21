/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A ReadOnlyListTest tests the functionality of the ReadOnlyList
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ReadOnlyListTest extends TestCase {

    /** attempt to modify this list */
    private EventList readOnlyData = null;

    /** attempt to modify this list */
    private List readOnly = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        // create a list of data
        readOnlyData = new BasicEventList();
        readOnlyData.add("A");
        readOnlyData.add("B");
        readOnlyData.add("C");
        
        // our list is that data, but read only
        readOnly = GlazedLists.readOnlyList(readOnlyData);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        readOnlyData = null;
        readOnly = null;
    }

    /**
     * Verifies that the sublist is also read only.
     */
    public void testSubList() {
        try {
            readOnly.subList(0, 3).clear();
            fail();
        } catch(UnsupportedOperationException e) {
            // read failed as expected
        }

        readOnlyData.subList(0, 3).clear();
    }

    /**
     * Verifies that the sublist is also read only.
     */
    public void testIterator() {
        try {
            Iterator i = readOnly.iterator();
            i.next();
            i.remove();
            fail();
        } catch(UnsupportedOperationException e) {
            // read failed as expected
        }

        Iterator i = readOnlyData.iterator();
        i.next();
        i.remove();
    }

    public void testReadMethods() {
        readOnlyData.clear();
        readOnlyData.addAll(GlazedListsTests.stringToList("ABCDEFGB"));

        assertEquals("A", readOnly.get(0));
        assertTrue(readOnly.contains("E"));
        assertEquals(readOnly, Arrays.asList(readOnly.toArray()));
        assertEquals(readOnly, Arrays.asList(readOnly.toArray(new String[0])));
        assertTrue(readOnly.containsAll(Collections.singletonList("B")));
        assertEquals(3, readOnly.indexOf("D"));
        assertEquals(readOnly.size()-1, readOnly.lastIndexOf("B"));
        assertEquals(GlazedListsTests.stringToList("CDE"), readOnly.subList(2, 5));
    }

    public void testWriteMethods() {
        try {
            readOnly.add(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.add(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.addAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.addAll(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.clear();
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.remove(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.remove(0);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.removeAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.retainAll(null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }

        try {
            readOnly.set(0, null);
            fail();
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}