/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists
import ca.odell.glazedlists.event.*;
// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * Verifies that EventList matches the List API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventListTest extends TestCase {

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
     * Validates that removeAll() works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=169">Bug 169</a>
     */
    public void testRemoveAll() {
        List jesse = new ArrayList(); jesse.addAll(Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') }));
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });

        // create the reference list
        List jesseArrayList = new ArrayList();
        jesseArrayList.addAll(jesse);
        jesseArrayList.removeAll(wilson);
        
        // test the BasicEventList list
        List jesseBasicEventList = new BasicEventList();
        jesseBasicEventList.addAll(jesse);
        jesseBasicEventList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseBasicEventList);
        
        // test the SortedList list
        List jesseSortedList = new SortedList(new BasicEventList(), null);
        jesseSortedList.addAll(jesse);
        jesseSortedList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseSortedList);
    }
}
