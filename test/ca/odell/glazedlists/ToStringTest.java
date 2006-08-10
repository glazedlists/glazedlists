/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

import ca.odell.glazedlists.matchers.Matchers;

/**
 * This test verifies that the toString() method on all lists is consistent
 * with the toString() method of {@link ArrayList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ToStringTest extends TestCase {

    /**
     * Validate that toString() is consistent with ArrayList on all lists.
     */
    public void testToStringConsistency() {
        ArrayList controlList = new ArrayList();
        BasicEventList basicEventList = new BasicEventList();
        FilterList filterList = new FilterList(basicEventList, Matchers.trueMatcher());
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