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
}
