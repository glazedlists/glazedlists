/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;
// swing utilities for interacting with the event dispatch thread
import javax.swing.SwingUtilities;
import javax.swing.event.*;

/**
 * This test verifies that the EventSelectionModel works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventSelectionModelTest extends TestCase {

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
     * Verifies that the selected index is cleared when the selection is cleared.
     */
    public void testClear() {
        BasicEventList list = new BasicEventList();
        EventSelectionModel eventSelectionModel = new EventSelectionModel(list);
        
        // populate the list
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        list.add("F");
        
        // make sure Swing is caught up
        flushEventDispatchThread();
        
        // make a selection
        eventSelectionModel.addSelectionInterval(1, 4);
        
        // test the selection
        assertEquals(list.subList(1, 5), eventSelectionModel.getEventList());
        
        // clear the selection
        eventSelectionModel.clearSelection();
        
        // test the selection
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getEventList());
        assertEquals(-1, eventSelectionModel.getMinSelectionIndex());
        assertEquals(-1, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(true, eventSelectionModel.isSelectionEmpty());
    }
    
    /**
     * Tests a problem where the {@link EventSelectionModel} fails to fire events
     *
     * @author Sergey Bogatyrjov
     */
    public void testSelectionModel() {
        String[] data = new String[] { "one", "two", "three" };
        EventList source = new BasicEventList(Arrays.asList(data));
        BooleanFilteredList filtered = new BooleanFilteredList(source);

        // create selection model
        EventSelectionModel model = new EventSelectionModel(filtered);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select the 1th
        flushEventDispatchThread();
        model.setSelectionInterval(1, 1);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatchAll(false);
        flushEventDispatchThread();
        assertEquals(1, counter.getCountAndReset());

        // unclear the filter
        filtered.setMatchAll(true);
        flushEventDispatchThread();
        assertEquals(0, counter.getCountAndReset());

        // select the 0th
        model.setSelectionInterval(0, 0);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatchAll(false);
        flushEventDispatchThread();
        assertEquals(1, counter.getCountAndReset());
    }

    /**
     * Ensures that all waiting Swing events have been handled before proceeding.
     * This hack method can be used when unit testing classes that interact with
     * the Swing event dispatch thread.
     *
     * <p>This guarantees that all events in the event dispatch queue before this
     * method is called will have executed. It provides no guarantee that the event
     * dispatch thread will be empty upon return.
     */
    private void flushEventDispatchThread() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                }
            });
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } catch(java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
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
