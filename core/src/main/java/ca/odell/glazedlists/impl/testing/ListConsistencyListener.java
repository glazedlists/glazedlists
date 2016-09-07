/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.testing;

// Java collections are used for underlying data storage
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A very basic listener that ensures that lists are kept consistent and that
 * the change events are consistent.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
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

    /** whether to fail when the removed element is incorrectly reported */
    private boolean previousElementTracked = true;

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
        this.name = name != null ? name : source.getClass().getName();
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
        return new ListConsistencyListener<E>(source, name, verbose);
    }
    public static <E> ListConsistencyListener<E> install(EventList<E> source) {
        return install(source, null, false);
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
        assertTrue(expected.size() == source.size());
        for(int i = 0; i < expected.size(); i++) {
            assertTrue("Different elements at " + i + " (expected=" + expected.get(i) + ", is=" + source.get(i), expected.get(i) == source.get(i));
        }
    }

    public void assertTrue(boolean condition) {
        if(!condition) {
            System.out.println("");
        }
        assertTrue("Assertion failed", condition);
    }

    public void assertTrue(String message, boolean condition) {
        if(!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Configure whether errors shall be thrown if the previous value isn't
     * what's expected.
     */
    public void setPreviousElementTracked(boolean previousElementTracked) {
        this.previousElementTracked = previousElementTracked;
    }

    /**
     * When the source {@link EventList} is changed, make sure the event reported
     * describes the differences between before and after.
     */
    private class ListChangeHandler implements ListEventListener<E> {

        @Override
        public void listChanged(ListEvent<E> listChanges) {
            try {

            assertTrue(source == listChanges.getSource());
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
                assertTrue(expected.size() == reorderMap.length);
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
                    assertTrue(changeIndex >= 0);
                    assertTrue(changeIndex >= highestChangeIndex);
                    highestChangeIndex = changeIndex;

                    // verify the index is small enough, and adjust the size
                    if(changeType == ListEvent.INSERT) {
                        E inserted = source.get(changeIndex);
                        expected.add(changeIndex, inserted);
                        if(previousElementTracked) {
                            Object reportedNew = listChanges.getNewValue();
//                            assertTrue(inserted == reportedNew);
                        }
                    } else if(changeType == ListEvent.DELETE) {
                        Object removed = expected.remove(changeIndex);
                        if(previousElementTracked) {
                            Object reportedRemoved = listChanges.getOldValue();
                            assertTrue(removed == reportedRemoved);
                        }
                    } else if(changeType == ListEvent.UPDATE) {
                        E updated = source.get(changeIndex);
                        E replaced = expected.set(changeIndex, updated);
                        if(previousElementTracked) {
                            Object reportedReplaced = listChanges.getOldValue();
                            assertTrue(replaced == reportedReplaced);
//                            Object reportedNew = listChanges.getNewValue();
//                            assertTrue(updated == reportedNew);
                        }
                    }
                }

                changeCounts.add(new Integer(changesForEvent));
                reorderings.add(Boolean.FALSE);
            }

            // verify the source is consistent with what we expect
            assertConsistent();
            } catch (RuntimeException unexpected) {
                throw new RuntimeException("Failure for " + name, unexpected);
            }
        }

        @Override
        public String toString() {
            return "ConsistencyListener:" + name;
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
