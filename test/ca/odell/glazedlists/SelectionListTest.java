/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * Verifies that {@link SelectionList} works as expected.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class SelectionListTest extends TestCase {

    /** the selection list */
    private SelectionList source = null;

    /** the list of selected elements */
    private EventList selectedList = null;

    /** the list of deselected elements */
    private EventList deselectedList = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new SelectionList(new BasicEventList());
        selectedList = source.getSelected();
        deselectedList = source.getDeselected();
        source.addListEventListener(new ConsistencyTestList(source, "SelectionList: ", false));
        selectedList.addListEventListener(new ConsistencyTestList(selectedList, "selected: ", false));
        deselectedList.addListEventListener(new ConsistencyTestList(deselectedList, "deselected: ", false));

    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        source.dispose();
        source = null;
        selectedList = null;
        deselectedList = null;
    }

    /**
     * Tests that adding to the source affects the lists in the expected way.
     */
    public void testAdding() {
        source.add(0, new Integer(15));
        source.add(1, new Integer(155));
        source.add(2, new Integer(1555));
        source.add(0, new Integer(1));

        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
        assertEquals(new Integer(1), deselectedList.get(0));
    }

    /**
     * Tests that adding to the source affects the lists in the expected way
     * for the default selection mode.
     */
    public void testMultipleIntervalSelectionMode() {
        source.setSelectionMode(SelectionList.MULTIPLE_INTERVAL_SELECTION);
        source.add(0, new Integer(15));
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
        source.select(0);

        source.add(1, new Integer(155));
        source.add(2, new Integer(1555));
        source.add(0, new Integer(1));

        assertEquals(2, selectedList.size());
        assertEquals(new Integer(1), selectedList.get(0));
        assertEquals(new Integer(15), selectedList.get(1));
        assertEquals(2, deselectedList.size());
        assertEquals(new Integer(155), deselectedList.get(0));
        assertEquals(new Integer(1555), deselectedList.get(1));
    }

    /**
     * Tests that adding to the source affects the lists in the expected way
     * for the default selection mode.
     */
    public void testDefaultSelectionMode() {
        source.add(0, new Integer(15));
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
        source.select(0);

        source.add(1, new Integer(155));
        source.add(2, new Integer(1555));
        source.add(0, new Integer(1));

        assertEquals(1, selectedList.size());
        assertEquals(new Integer(15), selectedList.get(0));
        assertEquals(3, deselectedList.size());
        assertEquals(new Integer(1), deselectedList.get(0));
        assertEquals(new Integer(155), deselectedList.get(1));
        assertEquals(new Integer(1555), deselectedList.get(2));
    }

    /**
     * Test appending to selection via range selection
     */
    public void testAppendingSelectionRange() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select an initial range
        source.select(5, 14);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(14), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select a unique range
        source.select(15, 16);
        assertEquals(12, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(16), selectedList.get(11));
        assertEquals(8, deselectedList.size());

        // select a range with some overlap
        source.select(10, 19);
        assertEquals(15, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(14));
        assertEquals(5, deselectedList.size());

        // select an overlapping range
        source.select(10, 15);
        assertEquals(15, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(14));
        assertEquals(5, deselectedList.size());
    }

    /**
     * Test setting selection via range selection
     */
    public void testSettingSelectionRange() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select an initial range
        source.setSelection(5, 14);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(14), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select a unique range
        source.setSelection(15, 16);
        assertEquals(2, selectedList.size());
        assertEquals(new Integer(15), selectedList.get(0));
        assertEquals(new Integer(16), selectedList.get(1));
        assertEquals(18, deselectedList.size());

        // select a range with some overlap
        source.setSelection(10, 19);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(10), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select an overlapping range
        source.setSelection(10, 15);
        assertEquals(6, selectedList.size());
        assertEquals(new Integer(10), selectedList.get(0));
        assertEquals(new Integer(15), selectedList.get(5));
        assertEquals(14, deselectedList.size());
    }

    /**
     * Test setting selection via an index array.
     */
    public void testSettingSelectionByArray() {
        int[] testArray1 = {0, 1, 5, 6, 8, 9, 14, 15, 18, 19};
        int[] testArray2 = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        int[] testArray3 = {0, 1, 2, 3, 4, 15, 16, 17, 18, 19};
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select with array 1
        source.setSelection(testArray1);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 2
        source.setSelection(testArray2);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 3
        source.setSelection(testArray3);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());
    }
}