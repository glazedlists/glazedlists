/* Glazed Lists                                                 (c) 2003-2006 */
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
public class ListConsistencyListener<E> {
    
    /** a second copy of the list data */
    private List<E> expected;

    /** a name for reporting problems with the list */
    private String name;

    /** the source list to compare against */
    private EventList<E> source;

    /** whether to cough out changes to the console as they happen */
    private boolean verbose = false;

    /** count the number of changes per event */
    private List<Integer> changeCounts = new ArrayList<Integer>();
    private List<Boolean> reorderings = new ArrayList<Boolean>();

    /**
     * Creates a new ListConsistencyListener that ensures events from the source
     * list are consistent.
     *
     * @param verbose whether to print changes to the console as they happne
     */
    private ListConsistencyListener(EventList<E> source, String name, boolean verbose) {
        this.source = source;
        this.name = name;
        this.verbose = verbose;
        
        // populate the list of expected values
        expected = new ArrayList<E>(source);

        // handle changes to the source list
        source.addListEventListener(new ListChangeHandler());
    }

    /**
     * Creates a new ListConsistencyListener and installs it as a listener on
     * the specified source {@link EventList}. Every time that list changes, this
     * listener will verify the change reported equals the change applied.
     */
    public static <E> ListConsistencyListener<E> install(EventList<E> source, String name, boolean verbose) {
        ListConsistencyListener<E> result = new ListConsistencyListener<E>(source, name, verbose);
        return result;
    }
    public static <E> ListConsistencyListener<E> install(EventList<E> source) {
        return install(source, "event list", false);
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
        return changeCounts.get(event).intValue();
    }

    /**
     * Get whether the specified event was a reordering.
     */
    public boolean isReordering(int event) {
        return reorderings.get(event).booleanValue();
    }

    /**
     * Validate that this list is as expected.
     */
    public void assertConsistent() {
        Assert.assertEquals(expected, source);
    }

    /**
     * When the source {@link EventList} is changed, make sure the event reported
     * describes the differences between before and after.
     */
    private class ListChangeHandler implements ListEventListener<E> {

        public void listChanged(ListEvent<E> listChanges) {
            Assert.assertEquals(source, listChanges.getSource());
            assertEventsInIncreasingOrder(listChanges);

            // print the changes if necessary
            if(verbose) System.out.println(name + ": " + listChanges + ", size: " + source.size() + ", source: " + source);

            // record the changed indices
            List<Integer> changedIndices = new ArrayList<Integer>();

            // keep track of the highest change index so far
            int highestChangeIndex = 0;

            // handle sorting events
            if(listChanges.isReordering()) {
                int[] reorderMap = listChanges.getReorderMap();
                Assert.assertEquals(expected.size(), reorderMap.length);
                List<E> newExpectedValues = new ArrayList<E>(expected.size());
                for(int i = 0; i < reorderMap.length; i++) {
                    newExpectedValues.add(i, expected.get(reorderMap[i]));
                    changedIndices.add(new Integer(i));
                }
                expected = newExpectedValues;
                changeCounts.add(new Integer(2 * reorderMap.length));
                reorderings.add(Boolean.TRUE);

            // handle regular events
            } else {

                // for all changes, one index at a time
                int changesForEvent = 0;
                while(listChanges.next()) {
                    changesForEvent++;

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

                changeCounts.add(new Integer(changesForEvent));
                reorderings.add(Boolean.FALSE);
            }

            // verify the source is consistent with what we expect
            Assert.assertEquals(expected.size(), source.size());
            for(Iterator<Integer> c = changedIndices.iterator(); c.hasNext(); ) {
                int changeIndex = c.next().intValue();
                for(int i = Math.max(changeIndex - 1, 0); i < Math.min(changeIndex+2, expected.size()); i++) {
                    Assert.assertEquals(expected.get(i), source.get(i));
                }
            }
        }
    }

    /**
     * Ensure that events in the specified event flow in the legal order.
     */
    public static void assertEventsInIncreasingOrder(ListEvent listChanges) {
        listChanges.reset();
        StringBuffer changeDescription = new StringBuffer();
        int previousChangeIndex = -1;
        int previousChangeType = ListEvent.DELETE;
        boolean increasingOrder = true;

        while(listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            // maintain the change string
            if(changeType == ListEvent.UPDATE) {
                changeDescription.append("U");
            } else if(changeType == ListEvent.INSERT) {
                changeDescription.append("I");
            } else if(changeType == ListEvent.DELETE) {
                changeDescription.append("D");
            }
            changeDescription.append(changeIndex);

            // see if this was a failure
            if(changeIndex < previousChangeIndex
            || (changeIndex == previousChangeIndex && previousChangeType != ListEvent.DELETE)) {
                increasingOrder = false;
                changeDescription.append("*");
            }

            // prepare for the next change
            changeDescription.append(" ");
            previousChangeIndex = changeIndex;
            previousChangeType = changeType;
        }
        if(!increasingOrder) {
            System.out.println("List changes not in increasing order: " + changeDescription);
//            Assert.fail("List changes not in increasing order: " + changeDescription);
        }

        // reset the list iterator for other handlers
        listChanges.reset();
    }
}
