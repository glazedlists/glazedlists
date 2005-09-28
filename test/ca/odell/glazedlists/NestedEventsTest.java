/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// the Glazed Lists
import ca.odell.glazedlists.event.*;
// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

/**
 * Verifies that list events are nested properly.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=165">Bug 165</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class NestedEventsTest extends TestCase {

    private EventList source = null;
    private ExternalNestingEventList nestingList = null;
    private ListEventCounter counter = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source  = new BasicEventList();
        nestingList = new ExternalNestingEventList(source);
        counter = new ListEventCounter();
        nestingList.addListEventListener(counter);
        nestingList.addListEventListener(new ListConsistencyListener(nestingList, "nesting list", false));
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        counter = null;
        nestingList = null;
        source = null;
    }
    
    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testFullyDeletedInserts() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.remove(1);
        source.remove(1);
        source.remove(1);
        nestingList.commitEvent();
        
        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "E", "F", "G" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(2, counter.getChangeCount(1));
    }

    /**
     * Validates that complex contradicting events can be nested.
     */
    public void testDeletedInserts() {
        List jesse = Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') });
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });
        boolean contradictionsAllowed = true;
        
        // make nested events
        nestingList.beginEvent(contradictionsAllowed);
        source.addAll(jesse);
        source.removeAll(wilson);
        nestingList.commitEvent();
        
        // ensure the number of changes is limited
        assertEquals(3, nestingList.size());
        assertEquals(1, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(0));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testFullyUpdatedInserts() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        nestingList.commitEvent();
        
        // net change is: add 'c', 'd', 'e'
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "B", "c", "d", "e", "F", "G" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(1));
    }
    
    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testUpdatedInserts() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.set(1, "b");
        source.set(2, "c");
        source.set(3, "d");
        nestingList.commitEvent();
        
        // net change is: replace 'B' with 'b' and add 'd', 'E'
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "b", "c", "d", "E", "F", "G" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testFullyDeletedUpdates() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.remove(2);
        source.remove(2);
        source.remove(2);
        nestingList.commitEvent();
        
        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "B", "F", "G" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testDeletedUpdates() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.remove(1);
        source.remove(1);
        source.remove(1);
        nestingList.commitEvent();
        
        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "e", "F", "G" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }
    
    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testUpdatedUpdates() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.add("H");
        source.set(3, "d");
        source.set(4, "e");
        source.set(5, "f");
        nestingList.commitEvent();
        
        // net change is: replacing C, D, E, F with c, d, e, f
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "B", "c", "d", "e", "f", "G", "H" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(5, counter.getChangeCount(1));
    }
    
    /**
     * Validates that simple contradicting events can be nested.
     */
    public void testFullyUpdatedUpdates() {
        boolean contradictionsAllowed = true;
        nestingList.beginEvent(false);
        source.addAll(Arrays.asList(new String[] { "A", "B", "C", "D", "E", "F", "G" }));
        nestingList.commitEvent();
        
        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestingList.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.add("H");
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        nestingList.commitEvent();
        
        // net change is: replacing C, D, E with c, d, e and add H
        assertEquals(nestingList, Arrays.asList(new String[] { "A", "B", "c", "d", "e", "F", "G", "H" }));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }
    
    /**
     * Validates that simple contradicting events throw an exception if not allowed.
     */
    public void testSimpleContradictingEventsFail() {
        boolean contradictionsAllowed = false;
        
        // test nested events
        try {
            nestingList.beginEvent(contradictionsAllowed);
            source.add("hello");
            source.remove(0);
            nestingList.commitEvent();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
    
    /**
     * Validates that complex contradicting events throw an exception if not allowed.
     */
    public void testComplexContradictingEventsFail() {
        List jesse = Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') });
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });
        boolean contradictionsAllowed = false;
        
        // test nested events
        try {
            nestingList.beginEvent(contradictionsAllowed);
            source.addAll(jesse);
            source.removeAll(wilson);
            nestingList.commitEvent();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
    
    /**
     * A list that allows nested events to be managed externally.
     */
    private class ExternalNestingEventList extends TransformedList {
        public ExternalNestingEventList(EventList source) {
            super(source);
            source.addListEventListener(this);
        }
        public void beginEvent(boolean allowNested) {
            updates.beginEvent(allowNested);
        }
        public void commitEvent() {
            updates.commitEvent();
        }
        public void listChanged(ListEvent listChanges) {
            if(listChanges.isReordering()) {
                int[] reorderMap = listChanges.getReorderMap();
                updates.reorder(reorderMap);
            } else {
                while(listChanges.next()) {
                    updates.addChange(listChanges.getType(), listChanges.getIndex());
                }
            }
        }
    }
}
