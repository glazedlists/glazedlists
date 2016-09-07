/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.matchers.Matchers;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the toString() method on all lists is consistent
 * with the toString() method of {@link ArrayList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ToStringTest {

    /**
     * Validate that toString() is consistent with ArrayList on all lists.
     */
    @Test
    public void testToStringConsistency() {
        ArrayList<String> controlList = new ArrayList<String>();
        BasicEventList<String> basicEventList = new BasicEventList<String>();
        FilterList<String> filterList = new FilterList<String>(basicEventList, Matchers.trueMatcher());
        SortedList<String> sortedList = SortedList.create(basicEventList);

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
