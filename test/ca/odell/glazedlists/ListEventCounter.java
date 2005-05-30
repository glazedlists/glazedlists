/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * Counts how many ListEvents are received.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListEventCounter implements ListEventListener {
    
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