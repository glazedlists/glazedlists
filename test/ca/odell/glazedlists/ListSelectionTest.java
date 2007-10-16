/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Verifies that {@link ListSelection} works as expected.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class ListSelectionTest extends TestCase {

    /** to generate some random values */
    private Random dice = new Random(167);

    /** the target list */
    private EventList<Integer> source = null;

    /** the list selection */
    private ListSelection<Integer> listSelection = null;

    /** the list of selected elements */
    private EventList<Integer> selectedList = null;

    /** the list of deselected elements */
    private EventList<Integer> deselectedList = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new BasicEventList<Integer>();
        listSelection = new ListSelection<Integer>(source);
        selectedList = listSelection.getSelected();
        deselectedList = listSelection.getDeselected();
        ListConsistencyListener.install(source);
        ListConsistencyListener.install(selectedList);
        ListConsistencyListener.install(deselectedList);
        ListConsistencyListener.install(listSelection.getTogglingSelected());
        ListConsistencyListener.install(listSelection.getTogglingDeselected());
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        listSelection.dispose();
        listSelection = null;
        source = null;
        selectedList = null;
        deselectedList = null;
    }

    /**
     * Tests selecting all elements.
     */
    public void testSelectAll() {
        source.add(0, new Integer(15));
        source.add(1, new Integer(155));
        source.add(2, new Integer(1555));
        source.add(0, new Integer(1));

        // select on a completely deselected list
        listSelection.selectAll();
        assertEquals(source.size(), selectedList.size());
        assertEquals(0, deselectedList.size());

        // select on an already selected list
        listSelection.selectAll();
        assertEquals(source.size(), selectedList.size());
        assertEquals(0, deselectedList.size());
    }

    /**
     * Test deselecting all elements
     */
    public void testDeselectAll() {
        source.add(0, new Integer(15));
        source.add(1, new Integer(155));
        source.add(2, new Integer(1555));
        source.add(0, new Integer(1));

        // deselect on an already deselected list
        listSelection.deselectAll();
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());

        // deselect on a completely selected list
        listSelection.selectAll();
        listSelection.deselectAll();
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
    }

    /**
     * Tests that adding to the source affects the lists in the expected way
     * for the default selection mode, which is MULTIPLE_INTERVAL_SELECTION_DEFENSIVE.
     */
    public void testDefaultSelectionMode() {
        source.add(0, new Integer(15));
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
        listSelection.select(0);

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
     * Tests that adding to the source affects the lists in the expected way
     * for the MULTIPLE_INTERVAL_SELECTION mode.
     */
    public void testMultipleIntervalSelectionMode() {
        listSelection.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
        source.add(0, new Integer(15));
        assertEquals(0, selectedList.size());
        assertEquals(source.size(), deselectedList.size());
        listSelection.select(0);

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
     * Test setting selection by index.
     */
    public void testSettingSelectionByIndex() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        for(int i = 0;i < 100; i++) {
            int selectionIndex = dice.nextInt(20);
            listSelection.setSelection(selectionIndex);
            assertEquals(1, selectedList.size());
            assertEquals(source.get(selectionIndex), selectedList.get(0));
            assertEquals(true, listSelection.isSelected(selectionIndex));
            assertEquals(19, deselectedList.size());
        }
    }

    /**
     * Test adding to selection by index.
     */
    public void testSelectingByIndex() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        int oldSize = 0;
        for(int i = 0;i < 100; i++) {
            int selectionIndex = dice.nextInt(20);
            boolean wasSelected = listSelection.isSelected(selectionIndex);
            listSelection.select(selectionIndex);
            if(!wasSelected) oldSize++;
            assertEquals(oldSize, selectedList.size());
            assertEquals(20 - oldSize, deselectedList.size());
            assertEquals(true, listSelection.isSelected(selectionIndex));

            if(selectedList.size() == 20) {
                listSelection.deselectAll();
                oldSize = 0;
            }
        }
    }

    /**
     * Test deselecting by index
     */
    public void testDeselectingByIndex() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }
        listSelection.selectAll();

        int oldSize = 0;
        for(int i = 0;i < 100; i++) {
            int selectionIndex = dice.nextInt(20);
            boolean wasSelected = listSelection.isSelected(selectionIndex);
            listSelection.deselect(selectionIndex);
            if(wasSelected) oldSize++;
            assertEquals(oldSize, deselectedList.size());
            assertEquals(20 - oldSize, selectedList.size());
            assertEquals(false, listSelection.isSelected(selectionIndex));

            if(deselectedList.size() == 20) {
                listSelection.selectAll();
                oldSize = 0;
            }
        }
    }

    /**
     * Test setting selection with ranges
     */
    public void testSettingSelectionRange() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select an initial range
        listSelection.setSelection(5, 14);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(14), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select a unique range
        listSelection.setSelection(15, 16);
        assertEquals(2, selectedList.size());
        assertEquals(new Integer(15), selectedList.get(0));
        assertEquals(new Integer(16), selectedList.get(1));
        assertEquals(18, deselectedList.size());

        // select a range with some overlap
        listSelection.setSelection(10, 19);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(10), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select an overlapping range
        listSelection.setSelection(10, 15);
        assertEquals(6, selectedList.size());
        assertEquals(new Integer(10), selectedList.get(0));
        assertEquals(new Integer(15), selectedList.get(5));
        assertEquals(14, deselectedList.size());
    }

    /**
     * Test appending to selection with ranges
     */
    public void testAppendingSelectionRange() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select an initial range
        listSelection.select(5, 14);
        assertEquals(10, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(14), selectedList.get(9));
        assertEquals(10, deselectedList.size());

        // select a mutually exclusive range
        listSelection.select(15, 16);
        assertEquals(12, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(16), selectedList.get(11));
        assertEquals(8, deselectedList.size());

        // select a range with some overlap
        listSelection.select(10, 19);
        assertEquals(15, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(14));
        assertEquals(5, deselectedList.size());

        // select an entirely overlapping range
        listSelection.select(10, 15);
        assertEquals(15, selectedList.size());
        assertEquals(new Integer(5), selectedList.get(0));
        assertEquals(new Integer(19), selectedList.get(14));
        assertEquals(5, deselectedList.size());
    }

    /**
     * Test deselecting with ranges
     */
    public void testDeselectionByRange() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        listSelection.selectAll();

        // deselect an initial range
        listSelection.deselect(5, 14);
        assertEquals(10, deselectedList.size());
        assertEquals(new Integer(5), deselectedList.get(0));
        assertEquals(new Integer(14), deselectedList.get(9));
        assertEquals(10, selectedList.size());

        // deselect a mutually exclusive range
        listSelection.deselect(15, 16);
        assertEquals(12, deselectedList.size());
        assertEquals(new Integer(5), deselectedList.get(0));
        assertEquals(new Integer(16), deselectedList.get(11));
        assertEquals(8, selectedList.size());

        // deselect a range with some overlap
        listSelection.deselect(10, 19);
        assertEquals(15, deselectedList.size());
        assertEquals(new Integer(5), deselectedList.get(0));
        assertEquals(new Integer(19), deselectedList.get(14));
        assertEquals(5, selectedList.size());

        // deselect an entirely overlapping range
        listSelection.deselect(10, 15);
        assertEquals(15, deselectedList.size());
        assertEquals(new Integer(5), deselectedList.get(0));
        assertEquals(new Integer(19), deselectedList.get(14));
        assertEquals(5, selectedList.size());
    }

    /**
     * Test setting selection with an index array.
     */
    public void testSettingSelectionByArray() {
        int[] testArray1 = {0, 1, 5, 6, 8, 9, 14, 15, 18, 19};
        int[] testArray2 = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14};
        int[] testArray3 = {0, 1, 2, 3, 4, 15, 16, 17, 18, 19};
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select with array 1
        listSelection.setSelection(testArray1);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 2
        listSelection.setSelection(testArray2);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 3
        listSelection.setSelection(testArray3);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());
    }

    /**
     * Test appending to selection with an index array.
     */
    public void testAddingSelectionByArray() {
        int[] allUnselected = {0, 1, 5, 6, 8, 9, 14, 15, 18, 19};
        int[] totallyOverlapping = {5, 6, 9, 14, 19};
        int[] partialOverlap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] remainingElements = {11, 12, 13, 16, 17};
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select with array 1
        listSelection.select(allUnselected);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 2
        listSelection.select(totallyOverlapping);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // select with array 3
        listSelection.select(partialOverlap);
        assertEquals(15, selectedList.size());
        assertEquals(5, deselectedList.size());

        // select with array 4
        listSelection.select(remainingElements);
        assertEquals(20, selectedList.size());
        assertEquals(0, deselectedList.size());
    }

    /**
     * Test deselecting with an index array.
     */
    public void testDeselectingByArray() {
        int[] allDeselected = {0, 1, 5, 6, 8, 9, 14, 15, 18, 19};
        int[] totallyOverlapping = {5, 6, 9, 14, 19};
        int[] partialOverlap = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] remainingElements = {11, 12, 13, 16, 17};
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        listSelection.selectAll();

        // deselect with array 1
        listSelection.deselect(allDeselected);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // deselect with array 2
        listSelection.deselect(totallyOverlapping);
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());

        // deselect with array 3
        listSelection.deselect(partialOverlap);
        assertEquals(5, selectedList.size());
        assertEquals(15, deselectedList.size());

        // deselect with array 4
        listSelection.deselect(remainingElements);
        assertEquals(0, selectedList.size());
        assertEquals(20, deselectedList.size());
    }

    /**
     * Tests inverting selection
     */
    public void testSelectionInversion() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        // select all the even values
        for(int i = 0; i < 20; i += 2) {
            listSelection.select(i);
        }

        // invert once
        listSelection.invertSelection();
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());
        for(int i = 1; i < 20; i += 2) {
            assertEquals(true, listSelection.isSelected(i));
        }
        for(int i = 0; i < 20; i += 2) {
            assertEquals(false, listSelection.isSelected(i));
        }

        // invert again
        listSelection.invertSelection();
        assertEquals(10, selectedList.size());
        assertEquals(10, deselectedList.size());
        for(int i = 1; i < 20; i += 2) {
            assertEquals(false, listSelection.isSelected(i));
        }
        for(int i = 0; i < 20; i += 2) {
            assertEquals(true, listSelection.isSelected(i));
        }
    }

    /**
     * Test that the selection list supports change operations.
     */
    public void testChangeOperations() {
        for(int i = 0; i < 20; i++) {
            source.add(new Integer(i));
        }

        listSelection.select(1, 3);
        selectedList.set(2, new Integer(30));
        assertEquals(new Integer(30), source.get(3));
        assertFalse(listSelection.isSelected(5));
        assertTrue(listSelection.isSelected(1));
        assertTrue(listSelection.isSelected(2));
        assertTrue(listSelection.isSelected(3));
        assertFalse(listSelection.isSelected(4));

        deselectedList.set(2, new Integer(50));
        assertEquals(new Integer(50), source.get(5));
        assertFalse(listSelection.isSelected(0));
        assertTrue(listSelection.isSelected(1));
        assertTrue(listSelection.isSelected(2));
        assertTrue(listSelection.isSelected(3));
        assertFalse(listSelection.isSelected(4));
        assertFalse(listSelection.isSelected(5));
        assertFalse(listSelection.isSelected(6));

        selectedList.clear();
        assertEquals(17, source.size());
        assertEquals(17, deselectedList.size());
        assertEquals(0, selectedList.size());
        assertEquals(new Integer(0), source.get(0));
        assertEquals(new Integer(4), source.get(1));
        assertEquals(new Integer(50), source.get(2));
        assertEquals(new Integer(19), source.get(16));

        listSelection.select(14, 16);
        deselectedList.clear();
        assertEquals(3, source.size());
        assertEquals(0, deselectedList.size());
        assertEquals(3, selectedList.size());
        assertEquals(new Integer(17), source.get(0));
        assertEquals(new Integer(18), source.get(1));
        assertEquals(new Integer(19), source.get(2));
    }

    public void testContradictingUpdates() {
        NestableEventsList<String> source = new NestableEventsList<String>(new BasicEventList<String>());

        ListSelection<String> listSelection = new ListSelection<String>(source);
        ListConsistencyListener.install(listSelection.getSelected());

        source.beginEvent(false);
        source.add("C");
        source.add("E");
        source.commitEvent();
        source.beginEvent(true);
        source.add(0, "A");
        source.add(1, "B");
        source.set(2, "C");
        source.set(3, "E");
        source.add(3, "D");
        source.set(4, "E");
        source.add(5, "F");
        source.commitEvent();

        source.beginEvent(false);
        while(!source.isEmpty()) source.remove(0);
        source.add("A");
        source.add("B");
        source.commitEvent();
        source.beginEvent(true);
        source.set(0, "a");
        source.set(1, "b");
        source.add(2, "c");
        source.set(0, "a");
        source.set(1, "b");
        source.set(2, "c");
        source.add(3, "d");
        source.add(1, "e");
        source.set(2, "c");
        source.set(3, "d");
        source.commitEvent();

//        [U.0-1, I.2, U.0-2, I.3, I.1, U.2-3]
    }

    /**
     * See feature request,
     * <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=250">Issue 250</a>
     */
    public void testSelectByValue() {
        int result;
        boolean changed;
        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));
        source.add(new Integer(3));
        source.add(new Integer(4));
        source.add(new Integer(5));

        result = listSelection.select(new Integer(4));
        assertEquals(4, result);
        assertEquals(1, selectedList.size());
        assertEquals(5, deselectedList.size());
        assertEquals(4, listSelection.getMinSelectionIndex());
        assertEquals(4, listSelection.getMaxSelectionIndex());

        listSelection.deselectAll();
        assertEquals(0, selectedList.size());
        assertEquals(6, deselectedList.size());

        result = listSelection.select(new Integer(8));
        assertEquals(-1, result);
        assertEquals(0, selectedList.size());
        assertEquals(6, deselectedList.size());

        result = listSelection.select(new Integer(2));
        assertEquals(2, result);
        result = listSelection.select(new Integer(4));
        assertEquals(4, result);
        assertEquals(2, selectedList.size());
        assertEquals(4, deselectedList.size());
        assertEquals(2, listSelection.getMinSelectionIndex());
        assertEquals(4, listSelection.getMaxSelectionIndex());

        listSelection.deselectAll();
        assertEquals(0, selectedList.size());
        assertEquals(6, deselectedList.size());

        List<Integer> toSelect = new ArrayList<Integer>();
        changed = listSelection.select(toSelect);
        assertEquals(false, changed);
        assertEquals(0, selectedList.size());
        assertEquals(6, deselectedList.size());

        toSelect.add(new Integer(2));
        changed = listSelection.select(toSelect);
        assertEquals(true, changed);
        assertEquals(1, selectedList.size());
        assertEquals(5, deselectedList.size());
        assertEquals(2, listSelection.getMinSelectionIndex());
        assertEquals(2, listSelection.getMaxSelectionIndex());

        listSelection.deselectAll();
        assertEquals(0, selectedList.size());
        assertEquals(6, deselectedList.size());

        toSelect.add(0, new Integer(5));
        toSelect.add(1, new Integer(7));
        changed = listSelection.select(toSelect);
        assertEquals(true, changed);
        assertEquals(2, selectedList.size());
        assertEquals(4, deselectedList.size());
        assertEquals(2, listSelection.getMinSelectionIndex());
        assertEquals(5, listSelection.getMaxSelectionIndex());
    }

    public void testTogglingViewAddingAndRemoving(){

        source.add(new Integer(0));
        source.add(new Integer(1));
        source.add(new Integer(2));
        source.add(new Integer(3));
        source.add(new Integer(4));
        source.add(new Integer(5));

        EventList<Integer> selectedToggler = listSelection.getTogglingSelected();
        EventList<Integer> deselectedToggler = listSelection.getTogglingDeselected();
        assertEquals(0, selectedToggler.size());
        selectedToggler.add(source.get(0));
        assertEquals(1, selectedToggler.size());
        assertEquals(6, source.size());
        assertEquals(5, deselectedToggler.size());
        deselectedToggler.remove(source.get(1));
        deselectedToggler.add(source.get(0));
        assertEquals(1, selectedToggler.size());
        assertEquals(6, source.size());
        assertEquals(5, deselectedToggler.size());
        selectedToggler.remove(source.get(1));
        assertEquals(0, selectedToggler.size());
        assertEquals(6, source.size());
        assertEquals(6, deselectedToggler.size());


        try {
            listSelection.getTogglingSelected().remove(6);
            fail("IndexOutOfBoundsException not thrown when removing beyond end");
        } catch(IndexOutOfBoundsException e) {}
        try {
            listSelection.getTogglingDeselected().remove(6);
            fail("IndexOutOfBoundsException not thrown when removing beyond end");
        } catch(IndexOutOfBoundsException e) {}

    }

    public void testAddingItemNotInSourceToTogglingView(){
        try {
            listSelection.getTogglingSelected().add(new Integer(6));
            fail("IllegalArgumentException not thrown when item not in source list added");
        } catch(IllegalArgumentException e) {}
        try {
            listSelection.getTogglingDeselected().add(new Integer(6));
            fail("IllegalArgumentException not thrown when item not in source list added");
        } catch(IllegalArgumentException e) {}
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(new Integer(0));
        ints.add(new Integer(1));
        try {
            listSelection.getTogglingSelected().addAll(ints);
            fail("IllegalArgumentException not thrown when item not in source list added");
        } catch(IllegalArgumentException e) {}
        try {
            listSelection.getTogglingDeselected().addAll(ints);
            fail("IllegalArgumentException not thrown when item not in source list added");
        } catch(IllegalArgumentException e) {}
    }

    public void testRemovingItemNotInSourceFromTogglingView() {
        EventList<Integer> togglingSelected = listSelection.getTogglingSelected();
        EventList<Integer> togglingDeselected = listSelection.getTogglingDeselected();
        assertFalse(togglingSelected.remove(new Integer(6)));
        assertFalse(togglingDeselected.remove(new Integer(6)));
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(new Integer(0));
        ints.add(new Integer(1));
        assertFalse(togglingSelected.removeAll(ints));
        assertFalse(togglingDeselected.removeAll(ints));
    }

    public void testTogglingViewBulkOperations(){
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(new Integer(0));
        ints.add(new Integer(1));
        source.addAll(ints);
        listSelection.getTogglingSelected().addAll(ints);
        assertEquals(2, listSelection.getTogglingSelected().size());
        assertEquals(0, listSelection.getTogglingDeselected().size());
        listSelection.getTogglingDeselected().addAll(ints);
        assertEquals(0, listSelection.getTogglingSelected().size());
        assertEquals(2, source.size());
        assertEquals(2, listSelection.getTogglingDeselected().size());
        listSelection.getTogglingDeselected().removeAll(ints);
        assertEquals(2, listSelection.getTogglingSelected().size());
        assertEquals(2, source.size());
        assertEquals(0, listSelection.getTogglingDeselected().size());
        listSelection.getTogglingSelected().removeAll(ints);
        assertEquals(0, listSelection.getTogglingSelected().size());
        assertEquals(2, source.size());
        assertEquals(2, listSelection.getTogglingDeselected().size());
    }
}