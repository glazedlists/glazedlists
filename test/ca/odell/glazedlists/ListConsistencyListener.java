/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// Java collections are used for underlying data storage
import java.util.*;
// the Glazed Lists
import ca.odell.glazedlists.event.*;
// for being a JUnit test case
import junit.framework.*;

/**
 * A very basic list that ensures that lists are kept consistent and that
 * the change events are consistent.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListConsistencyListener implements ListEventListener {
    
    /** a second copy of the list data */
    private List expected;

    /** a name for reporting problems with the list */
    private String name;

    /** the source list to compare against */
    private EventList source;

    /** whether to cough out changes to the console as they happen */
    private boolean verbose = false;

    /**
     * Creates a new ListConsistencyListener that ensures events from the source
     * list are consistent.
     *
     * @param verbose whether to print changes to the console as they happne
     */
    public ListConsistencyListener(EventList source, String name, boolean verbose) {
        this.source = source;
        this.name = name;
        this.verbose = verbose;
        
        // populate the list of expected values
        expected = new ArrayList();
        for(int i = 0; i < source.size(); i++) {
            expected.add(source.get(i));
        }
    }

    /**
     * Creates a new ListConsistencyListener that ensures events from the source
     * list are consistent.
     */
    public ListConsistencyListener(EventList source, String name) {
        this(source, name, false);
    }
    
    /**
     * Validate that this list is as expected.
     */
    public void assertConsistent() {
        Assert.assertEquals(expected, source);
    }

    /**
     * For implementing the ListEventListener interface.
     */
    public void listChanged(ListEvent listChanges) {
        Assert.assertEquals(source, listChanges.getSource());

        // print the changes if necessary
        if(verbose) System.out.println(name + ": " + listChanges + ", size: " + source.size() + ", source: " + source);
        
        // record the changed indices
        List changedIndices = new ArrayList();
        
        // keep track of the highest change index so far
        int highestChangeIndex = 0;

        // handle sorting events
        if(listChanges.isReordering()) {
            int[] reorderMap = listChanges.getReorderMap();
            Assert.assertEquals(expected.size(), reorderMap.length);
            List newExpectedValues = new ArrayList(expected.size());
            for(int i = 0; i < reorderMap.length; i++) {
                newExpectedValues.add(i, expected.get(reorderMap[i]));
                changedIndices.add(new Integer(i));
            }
            this.expected = newExpectedValues;

        // handle regular events
        } else {
        
            // for all changes, one index at a time
            while(listChanges.next()) {

                // get the current change info
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                // save this index for validation later
                changedIndices.add(new Integer(changeIndex));

                // make sure the change indices are positive and not descreasing
                Assert.assertTrue(changeIndex >= 0);
                Assert.assertTrue(changeIndex >= highestChangeIndex);
                highestChangeIndex = changeIndex;

                // verify the index is small enough, and adjust the size
                if(changeType == ListEvent.INSERT) {
                    expected.add(changeIndex, source.get(changeIndex));
                } else if(changeType == ListEvent.DELETE) {
                    expected.remove(changeIndex);
                } else if(changeType == ListEvent.UPDATE) {
                    expected.set(changeIndex, source.get(changeIndex));
                }
            }
        }
        
        // verify the source is consistent with what we expect
        Assert.assertEquals(expected.size(), source.size());
        for(Iterator c = changedIndices.iterator(); c.hasNext(); ) {
            int changeIndex = ((Integer)c.next()).intValue();
            for(int i = Math.max(changeIndex - 1, 0); i < Math.min(changeIndex+2, expected.size()); i++) {
                Assert.assertEquals(expected.get(i), source.get(i));
            }
        }
    }
}
