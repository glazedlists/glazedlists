/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the toString() method on all lists is consistent
 * with the toString() method of <code>ArrayList</code>.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ToStringTest extends TestCase {

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
     * Validate that toString() is consistent with ArrayList on all lists.
     */
    public void testToStringConsistency() {
        ArrayList controlList = new ArrayList();
        BasicEventList basicEventList = new BasicEventList();
        TrivialFilterList filterList = new TrivialFilterList(basicEventList);
        SortedList sortedList = new SortedList(basicEventList);

        // Test On Empty Lists

        assertEquals(controlList.toString(), basicEventList.toString());
        assertEquals(controlList.toString(), filterList.toString());
        assertEquals(controlList.toString(), sortedList.toString());

        // Test On Lists With One Element
        controlList.add("Apple");
        basicEventList.add("Apple");

        assertEquals(controlList.toString(), basicEventList.toString());
        assertEquals(controlList.toString(), filterList.toString());
        assertEquals(controlList.toString(), sortedList.toString());

        // Test On Lists With Multiple Elements

        controlList.add("Banana");
        controlList.add("Cherry");
        controlList.add("Donut");
        basicEventList.add("Banana");
        basicEventList.add("Cherry");
        basicEventList.add("Donut");

        assertEquals(controlList.toString(), basicEventList.toString());
        assertEquals(controlList.toString(), filterList.toString());
        assertEquals(controlList.toString(), sortedList.toString());

    }

}

/**
 * The trivial filter list is a simple extension of AbstractFilterList
 * that simply never filters anything out!
 */
class TrivialFilterList extends AbstractFilterList {
    public TrivialFilterList(EventList source) {
        super(source);
    }
    public boolean filterMatches(Object element) {
        return true;
    }
}