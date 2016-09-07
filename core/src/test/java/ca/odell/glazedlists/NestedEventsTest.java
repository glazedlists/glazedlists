/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Verifies that list events are nested properly.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=165">Bug 165</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NestedEventsTest {

    private BasicEventList<String> source = null;
    private NestableEventsList<String> nestableSource = null;
    private ListConsistencyListener<String> counter = null;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        source  = new BasicEventList<String>();
        nestableSource = new NestableEventsList<String>(source);
        counter = ListConsistencyListener.install(nestableSource);
    }

    /**
     * Clean up after the test.
     */
    @After
    public void tearDown() {
        counter = null;
        nestableSource = null;
        source = null;
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testFullyDeletedInserts() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.remove(1);
        source.remove(1);
        source.remove(1);
        nestableSource.commitEvent();

        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestableSource, GlazedListsTests.stringToList("AEFG"));
        assertEquals(2, counter.getEventCount());
        assertEquals(2, counter.getChangeCount(1));
    }

    /**
     * Validates that complex contradicting events can be nested.
     */
    @Test
    public void testDeletedInserts() {
        List<String> jesse = GlazedListsTests.stringToList("JESSE");
        List<String> wilson = GlazedListsTests.stringToList("WILSON");
        boolean contradictionsAllowed = true;

        // make nested events
        nestableSource.beginEvent(contradictionsAllowed);
        source.addAll(jesse);
        source.removeAll(wilson);
        nestableSource.commitEvent();

        // ensure the number of changes is limited
        assertEquals(3, nestableSource.size());
        assertEquals(1, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(0));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testFullyUpdatedInserts() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        nestableSource.commitEvent();

        // net change is: add 'c', 'd', 'e'
        assertEquals(nestableSource, GlazedListsTests.stringToList("ABcdeFG"));
        assertEquals(2, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testUpdatedInserts() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.add(2, "C");
        source.add(3, "D");
        source.add(4, "E");
        source.set(1, "b");
        source.set(2, "c");
        source.set(3, "d");
        nestableSource.commitEvent();

        // net change is: replace 'B' with 'b' and add 'd', 'E'
        assertEquals(nestableSource, GlazedListsTests.stringToList("AbcdEFG"));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testFullyDeletedUpdates() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABCDEFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.remove(2);
        source.remove(2);
        source.remove(2);
        nestableSource.commitEvent();

        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestableSource, GlazedListsTests.stringToList("ABFG"));
        assertEquals(2, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testDeletedUpdates() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABCDEFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.remove(1);
        source.remove(1);
        source.remove(1);
        nestableSource.commitEvent();

        // net change is: remove 1 element at 1 and add 1 element at 1
        assertEquals(nestableSource, GlazedListsTests.stringToList("AeFG"));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testUpdatedUpdates() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABCDEFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.add("H");
        source.set(3, "d");
        source.set(4, "e");
        source.set(5, "f");
        nestableSource.commitEvent();

        // net change is: replacing C, D, E, F with c, d, e, f
        assertEquals(nestableSource, GlazedListsTests.stringToList("ABcdefGH"));
        assertEquals(2, counter.getEventCount());
        assertEquals(5, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events can be nested.
     */
    @Test
    public void testFullyUpdatedUpdates() {
        boolean contradictionsAllowed = true;
        nestableSource.beginEvent(false);
        source.addAll(GlazedListsTests.stringToList("ABCDEFG"));
        nestableSource.commitEvent();

        // test nested events: add 3 elements at 2 and delete 3 elements at 1
        nestableSource.beginEvent(contradictionsAllowed);
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        source.add("H");
        source.set(2, "c");
        source.set(3, "d");
        source.set(4, "e");
        nestableSource.commitEvent();

        // net change is: replacing C, D, E with c, d, e and add H
        assertEquals(nestableSource, GlazedListsTests.stringToList("ABcdeFGH"));
        assertEquals(2, counter.getEventCount());
        assertEquals(4, counter.getChangeCount(1));
    }

    /**
     * Validates that simple contradicting events throw an exception if not allowed.
     */
    @Test
    public void testSimpleContradictingEventsFail() {
        // test nested events
        try {
            nestableSource.updates.beginEvent(false);
            source.add("hello");
            source.remove(0);
            nestableSource.updates.commitEvent();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }

    /**
     * Validates that complex contradicting events throw an exception if not allowed.
     */
    @Test
    public void testComplexContradictingEventsFail() {
        List<String> jesse = GlazedListsTests.stringToList("JESSE");
        List<String> wilson = GlazedListsTests.stringToList("WILSON");

        // test nested events
        try {
            nestableSource.beginEvent(false);
            source.addAll(jesse);
            source.removeAll(wilson);
            nestableSource.commitEvent();
            fail("Expected IllegalStateException");
        } catch(IllegalStateException e) {
            // expected
        }
    }
}
