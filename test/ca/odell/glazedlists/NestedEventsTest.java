/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
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
    public void testSimpleContradictingEventsAllowed() {
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
    public void testComplexContradictingEventsAllowed() {
        List jesse = Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') });
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });
        boolean contradictionsAllowed = true;
        
        // test nested events
        nestingList.beginEvent(contradictionsAllowed);
        source.addAll(jesse);
        source.removeAll(wilson);
        nestingList.commitEvent();
        
        System.out.println(nestingList);
        assertEquals(3, nestingList.size());
        assertEquals(3, counter.getEventCount());
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
