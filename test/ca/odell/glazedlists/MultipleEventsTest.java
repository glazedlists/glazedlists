/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.util.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Tests to verify that for each list change, only one event is fired.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=46">Bug 46</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class MultipleEventsTest extends TestCase {

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
     * Tests that clearing the filter list does not fire multiple
     * events on the original list.
     */
    public void testFilterList() {
        // create a list
        EventList source = new BasicEventList();
        source.add(new int[] { 1 });
        source.add(new int[] { 0 });
        source.add(new int[] { 1 });
        source.add(new int[] { 0 });
        
        // prepare a filter list
        IntArrayFilterList filterList = new IntArrayFilterList(source);
        filterList.setFilter(0, 1);
        
        // listen to changes on the filter list
        ListEventCounter counter = new ListEventCounter();
        filterList.addListEventListener(counter);

        // clear the filter list
        filterList.clear();
        
        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }

    /**
     * Tests that clearing a sub list does not fire multiple
     * events on the original list.
     */
    public void testSubList() {
        // create a list
        EventList source = new BasicEventList();
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("D");
        
        // prepare a sub list
        EventList subList = (EventList)source.subList(1, 3);
        
        // listen to changes on the sub list
        ListEventCounter counter = new ListEventCounter();
        subList.addListEventListener(counter);

        // clear the sub list
        subList.clear();
        
        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }

    /**
     * Tests that clearing a unique list does not fire multiple
     * events on the original list.
     */
    public void testUniqueList() {
        // create a list
        EventList source = new BasicEventList();
        source.add("A");
        source.add("B");
        source.add("B");
        source.add("C");
        
        // prepare a unique list
        EventList uniqueList = new UniqueList(source);
        
        // listen to changes on the unique list
        ListEventCounter counter = new ListEventCounter();
        uniqueList.addListEventListener(counter);

        // clear the unique list
        uniqueList.clear();
        
        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }
}


/**
 * Counts how many ListEvents are received.
 */
class ListEventCounter implements ListEventListener {
    
    /** count the number of changes per event */
    private List changeCounts = new ArrayList();

    /**
     * When an event occurs, count that.
     */
    public void listChanged(ListEvent listChanges) {
        int changesForEvent = 0;
        while(listChanges.next()) {
            changesForEvent++;
        }
        changeCounts.add(new Integer(changesForEvent));
    }

    /**
     * Gets the number of events that have occured thus far.
     */
    public int getEventCount() {
        return changeCounts.size();
    }

    /**
     * Gets the number of changes for the specified event.
     */
    public int getChangeCount(int event) {
        return ((Integer)changeCounts.get(event)).intValue();
    }
}