/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;
import javax.swing.event.*;

/**
 * This test verifies that the EventSelectionModel works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventSelectionModelTest extends SwingTestCase {

    /**
     * Prepare for the test.
     */
    public void guiSetUp() {
    }

    /**
     * Clean up after the test.
     */
    public void guiTearDown() {
    }

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
    }
    
    /**
     * Tests that selection survives a sorting.
     */
    public void guiTestSort() {
        BasicEventList list = new BasicEventList();
        SortedList sorted = new SortedList(list, null);
        EventSelectionModel eventSelectionModel = new EventSelectionModel(sorted);
        
        // populate the list
        list.add("E");
        list.add("C");
        list.add("F");
        list.add("B");
        list.add("A");
        list.add("D");
        assertEquals(Arrays.asList(new String[] { }), eventSelectionModel.getSelected());
        
        // select the vowels
        eventSelectionModel.addSelectionInterval(0, 0);
        eventSelectionModel.addSelectionInterval(4, 4);
        assertEquals(Arrays.asList(new String[] { "E", "A" }), eventSelectionModel.getSelected());
        
        // flip the list
        sorted.setComparator(GlazedLists.comparableComparator());
        assertEquals(Arrays.asList(new String[] { "A", "E" }), eventSelectionModel.getSelected());
        
        // flip the list again
        sorted.setComparator(GlazedLists.reverseComparator());
        assertEquals(Arrays.asList(new String[] { "E", "A" }), eventSelectionModel.getSelected());
    }

    /**
     * Verifies that the selected index is cleared when the selection is cleared.
     */
    public void guiTestClear() {
        BasicEventList list = new BasicEventList();
        EventSelectionModel eventSelectionModel = new EventSelectionModel(list);

        // populate the list
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        list.add("F");

        // make a selection
        eventSelectionModel.addSelectionInterval(1, 4);

        // test the selection
        assertEquals(list.subList(1, 5), eventSelectionModel.getSelected());

        // clear the selection
        eventSelectionModel.clearSelection();

        // test the selection
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(-1, eventSelectionModel.getMinSelectionIndex());
        assertEquals(-1, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(true, eventSelectionModel.isSelectionEmpty());
    }

    /**
     * Tests a problem where the {@link EventSelectionModel} fails to fire events
     *
     * This test was contributed by: Sergey Bogatyrjov
     */
    public void guiTestSelectionModel() {
        String[] data = new String[] { "one", "two", "three" };
        EventList source = new BasicEventList();
        source.addAll(Arrays.asList(data));
        BooleanFilteredList filtered = new BooleanFilteredList(source);

        // create selection model
        EventSelectionModel model = new EventSelectionModel(filtered);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select the 1th
        model.setSelectionInterval(1, 1);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatchAll(false);
        assertEquals(1, counter.getCountAndReset());

        // unclear the filter
        filtered.setMatchAll(true);
        assertEquals(0, counter.getCountAndReset());

        // select the 0th
        model.setSelectionInterval(0, 0);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatchAll(false);
        assertEquals(1, counter.getCountAndReset());
    }

    /**
     * A filter list that matches all or none.
     */
    private class BooleanFilteredList extends AbstractFilterList {
        private boolean matchAll = true;
        public BooleanFilteredList(EventList source) {
            super(source);
            handleFilterChanged();
        }
        public boolean filterMatches(Object element) {
            return matchAll;
        }
        public void setMatchAll(boolean matchAll) {
            this.matchAll = matchAll;
            handleFilterChanged();
        }
    }

    /**
     * Counts the number of ListSelectionEvents fired.
     */
    private class ListSelectionChangeCounter implements ListSelectionListener {
        private int count = 0;
        public void valueChanged(ListSelectionEvent e) {
            count++;
        }
        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }
}
