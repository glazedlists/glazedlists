/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
// standard collections
import java.util.*;

/**
 * This test verifies that the SubList works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SubListTest extends TestCase {

    /**
     * Tests to verify that the SubList views a segment of a list.
     */
    public void testSubList() {
        // create a source list of values
        BasicEventList eventList = new BasicEventList();
        List controlList = new ArrayList();
        for(int i = 0; i < 26; i++) {
            eventList.add(new Integer(i));
            controlList.add(new Integer(i));
        }

        // ensure all sublists are equal
        for(int i = 0; i < eventList.size(); i++) {
            for(int j = i + 1; j < eventList.size(); j++) {
                assertEquals(eventList.subList(i,j), controlList.subList(i,j));
            }
        }
    }

    /**
     * Tests to verify that the SubList views a segment of a list while
     * that segment changes.
     */
    public void testSubListChanges() {
        // create a source list of values, from 0,1,2...49,100,101..149
        BasicEventList eventList = new BasicEventList();
        for(int i = 0; i < 50; i++) {
            eventList.add(new Integer(i));
        }
        for(int i = 0; i < 50; i++) {
            eventList.add(new Integer(i + 100));
        }

        // get the sublist
        EventList subListBefore = (EventList)eventList.subList(25, 75);
        ListConsistencyListener.install(subListBefore);

        // change the source list to be 0,1,2,3,...49,50,51,..99,100,101...149
        for(int i = 0; i < 50; i++) {
            eventList.add(50+i, new Integer(50+i));
        }

        // ensure the sublist took the change
        EventList subListAfter = (EventList)eventList.subList(25, 125);
        assertEquals(subListBefore, subListAfter);

        // change the lists again, deleting all odd numbered entries
        for(Iterator i = eventList.iterator(); i.hasNext(); ) {
            Integer current = (Integer)i.next();
            if(current.intValue() % 2 == 1) i.remove();
        }

        // ensure the sublists took the change
        subListAfter = (EventList)eventList.subList(13, 63);
        assertEquals(subListBefore, subListAfter);
        
        // make some set calls
        eventList.set(15, "Fifteen");
        eventList.set(18, "Eighteen");
        eventList.set(21, "Twenty-One");
        eventList.set(24, "Twenty-Four");
        assertEquals("Fifteen", subListAfter.get(2));
        assertEquals("Eighteen", subListAfter.get(5));
        subListBefore.set(14, "Twenty-Seven");
        assertEquals("Twenty-Seven", eventList.get(27));
        assertEquals(subListAfter, subListBefore);
    }

    /**
     * Test that SubList works with a single index, even if the list is sorted.
     */
    public void testSingleIndexSorting() {
        EventList eventList = new BasicEventList();
        SortedList sortedList = new SortedList(eventList);
        sortedList.setComparator(null);

        eventList.add("Lions");
        eventList.add("Eskimos");
        eventList.add("Stampeders");
        eventList.add("Roughriders");
        eventList.add("Blue Bombers");
        eventList.add("Tiger-Cats");
        eventList.add("Argonauts");
        eventList.add("Renegades");
        eventList.add("Alouettes");
        assertEquals(eventList, sortedList);

        List riders = sortedList.subList(3, 4);
        List expectedRiders = new ArrayList();
        expectedRiders.add("Roughriders");
        assertEquals(expectedRiders, riders);

        sortedList.setComparator(GlazedLists.comparableComparator());
        assertEquals(expectedRiders, riders);

        sortedList.setComparator(GlazedLists.reverseComparator());
        assertEquals(expectedRiders, riders);

        eventList.remove("Stampeders");
        eventList.remove("Blue Bombers");
        eventList.remove("Renegades");
        assertEquals(expectedRiders, riders);

        sortedList.setComparator(GlazedLists.comparableComparator());
        assertEquals(expectedRiders, riders);

        eventList.remove("Eskimos");
        eventList.remove("Tiger-Cats");
        assertEquals(expectedRiders, riders);

        eventList.remove("Roughriders");
        eventList.remove("Alouettes");
        assertEquals(Collections.EMPTY_LIST, riders);

        eventList.remove("Argonauts");
        eventList.remove("Lions");
        assertEquals(Collections.EMPTY_LIST, eventList);
    }

    /**
     * Test that SubList works while the underlying list changes.
     */
    public void testSingleIndexListChanges() {
        EventList eventList = new BasicEventList();
        eventList.add("Viper");
        eventList.add("Mustang");
        eventList.add("Camaro");

        List mustang = eventList.subList(1, 2);
        List expectedMustang = new ArrayList();
        expectedMustang.add("Mustang");
        assertEquals(expectedMustang, mustang);

        eventList.add(0, "Boxter");
        eventList.add(2, "G35");
        eventList.add(4, "Supra");
        eventList.add(6, "Firebird");
        assertEquals(expectedMustang, mustang);

        eventList.subList(0, 3).clear();
        assertEquals(expectedMustang, mustang);

        eventList.subList(1, 4).clear();
        assertEquals(expectedMustang, mustang);

        eventList.add("Focus");
        eventList.add(0, "500");
        eventList.add(1, "GT");
        eventList.add("F150");
        assertEquals(expectedMustang, mustang);

        eventList.clear();
        assertEquals(Collections.EMPTY_LIST, mustang);

        eventList.add("Ka");
        eventList.add("Escape Hybrid");
        assertEquals(Collections.EMPTY_LIST, mustang);
    }
}