/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Counts how many ListEvents are received.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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