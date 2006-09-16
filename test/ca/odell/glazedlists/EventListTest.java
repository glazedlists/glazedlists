/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import junit.framework.TestCase;

import java.util.*;

/**
 * Verifies that EventList matches the List API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListTest extends TestCase {

    /**
     * Validates that removeAll() works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=169">Bug 169</a>
     */
    public void testRemoveAll() {
        List<String> jesse = GlazedListsTests.stringToList("JESSE");
        List<String> wilson = GlazedListsTests.stringToList("WILSON");

        // create the reference list
        List<String> jesseArrayList = new ArrayList<String>();
        jesseArrayList.addAll(jesse);
        jesseArrayList.removeAll(wilson);

        // test the BasicEventList list
        List<String> jesseBasicEventList = new BasicEventList<String>();
        installConsistencyListener(jesseBasicEventList);
        jesseBasicEventList.addAll(jesse);
        jesseBasicEventList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseBasicEventList);

        // test the SortedList list
        List<String> jesseSortedList = new SortedList<String>(new BasicEventList<String>(), null);
        jesseSortedList.addAll(jesse);
        jesseSortedList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseSortedList);

        List<String> removeMultipleTestList = GlazedListsTests.stringToList("booblah");
        removeMultipleTestList.removeAll(GlazedListsTests.stringToList("bo"));
        assertEquals(GlazedListsTests.stringToList("lah"), removeMultipleTestList);
    }

    /**
     * Validates that retainAll() works.
     */
    public void testRetainAll() {
        List<String> jesse = GlazedListsTests.stringToList("JESSE");
        List<String> wilson = GlazedListsTests.stringToList("WILSON");

        // create the reference list
        List<String> jesseArrayList = new ArrayList<String>();
        jesseArrayList.addAll(jesse);
        jesseArrayList.retainAll(wilson);

        // test the BasicEventList list
        List<String> jesseBasicEventList = new BasicEventList<String>();
        installConsistencyListener(jesseBasicEventList);
        jesseBasicEventList.addAll(jesse);
        jesseBasicEventList.retainAll(wilson);
        assertEquals(jesseArrayList, jesseBasicEventList);

        // test the SortedList list
        List<String> jesseSortedList = new SortedList<String>(new BasicEventList<String>(), null);
        jesseSortedList.addAll(jesse);
        jesseSortedList.retainAll(wilson);
        assertEquals(jesseArrayList, jesseSortedList);
    }

    /**
     * Validates that contains() works with null.
     */
    public void testContainsNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(false, list.contains(null));
            assertEquals(true,  list.contains("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.contains(null));
            assertEquals(true, list.contains("Western"));
            assertEquals(false, list.contains("Molson"));
        }
    }

    /**
     * Validates that containsAll() works with null.
     */
    public void testContainsAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Molson" })));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Molson", null })));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Molson", "Busch" })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Molson" })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Western" })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { null, null })));
        }
    }

    /**
     * Validates that indexOf() works with null.
     */
    public void testIndexOfNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertTrue(-1 == list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertTrue(-1 != list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));
            assertTrue(-1 == list.indexOf("Molson"));
        }
    }



    /**
     * Validates that lastIndexOf() works with null.
     */
    public void testLastIndexOfNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertTrue(-1 == list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertTrue(-1 != list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));
            assertTrue(-1 == list.lastIndexOf("Molson"));
        }
    }

    /**
     * Validates that remove() works with null.
     */
    public void testRemoveNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();
            installConsistencyListener(list);

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(false, list.remove(null));
            assertEquals(true,  list.remove("Sleeman"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.remove(null));
            assertEquals(true, list.remove("Western"));
            assertEquals(false, list.remove("Molson"));
        }
    }

    /**
     * Validates that removeAll() works with null.
     */
    public void testRemoveAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(false,  list.removeAll(Arrays.asList(new String[] { null, "Busch" })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Western", "Busch" })));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Sleeman", null })));
            assertEquals(false, list.removeAll(Arrays.asList(new String[] { "Western", null })));
        }
    }

    /**
     * Validates that retainAll() works with null.
     */
    public void testRetainAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true,  list.retainAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.retainAll(Arrays.asList(new String[] { "Moslon", null })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true,  list.retainAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.retainAll(Arrays.asList(new String[] { "Moslon", null })));
        }
    }


    /**
     * Validates that hashCode() works with null.
     */
    public void testHashCodeNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(new SortedList<String>(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext(); ) {
            List<String> list = i.next();
            List<String> copy = new ArrayList<String>();

            // test a list that doesn't contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));

            // test a list that does contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));
        }
    }

    /**
     * Test that the {@link GlazedLists#eventList(java.util.Collection)} factory
     * method works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=234">Bug 234</a>
     */
    public void testGlazedListsEventList() {
        // make sure they have different backing stores
        List<String> list = new ArrayList<String>();
        EventList<String> eventList = GlazedLists.eventList(list);
        assertEquals(list, eventList);

        list.add("A");
        assertTrue(!list.equals(eventList));

        eventList.add("B");
        assertTrue(!list.equals(eventList));

        // make sure null is supported
        EventList<String> empty = GlazedLists.eventList(null);
        assertEquals(Collections.EMPTY_LIST, empty);
    }


    /**
     * Tests taht the {@link GlazedLists#syncEventListToList(EventList, java.util.List)}
     * factory method.
     */
    public void testGlazedListsSync() {
        EventList<String> source = new BasicEventList<String>();
        source.add("McCallum");
        source.add("Keith");
        List<String> target = new ArrayList<String>();
        target.add("Greene");

        ListEventListener<String> listener = GlazedLists.syncEventListToList(source, target);
        assertEquals(source, target);

        source.add("Szakra");
        assertEquals(source, target);

        source.addAll(Arrays.asList(new String[] { "Moore", "Holmes" }));
        assertEquals(source, target);

        source.add(1, "Burris");
        assertEquals(source, target);

        source.set(1, "Crandell");
        assertEquals(source, target);

        Collections.sort(source);
        assertEquals(source, target);

        source.clear();
        assertEquals(source, target);

        source.removeListEventListener(listener);
        source.add("Davis");
        assertFalse(source.equals(target));
    }

    public void testEventListTypeSafety() {
        EventList source = new BasicEventList();
        final Set acceptedTypes = new HashSet();
        acceptedTypes.add(null);
        acceptedTypes.add(Integer.class);
        acceptedTypes.add(String.class);
        ListEventListener typeSafetyListener = GlazedLists.typeSafetyListener(source, acceptedTypes);

        source.add(null);
        source.add(new Integer(0));
        source.add("Testing");

        try {
            source.add(new Long(23));
            fail("Expected an IllegalArgumentException for disallowed type");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // the source list is in an inconsistent state so we rebuild the list
        source = new BasicEventList();
        typeSafetyListener = GlazedLists.typeSafetyListener(source, acceptedTypes);
        source.add(null);

        try {
            source.set(0, new Long(23));
            fail("Expected an IllegalArgumentException for disallowed type");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // recover from the exception
        source.clear();

        source.removeListEventListener(typeSafetyListener);

        // these should now succeed now that we're not using the type safety checker any longer
        source.add(new Long(23));
        source.set(0, new Long(23));
    }

    public void testEventListLock() {
        final EventList<String> source = new BasicEventList<String>();

        // asymmetric unlocking of the readlock should fail-fast
        try {
            source.getReadWriteLock().readLock().unlock();
            fail("failed to receive an IllegalStateException when unlocking and unlocked readlock");
        } catch (IllegalMonitorStateException iae) {}

        // asymmetric unlocking of the writelock should fail-fast
        try {
            source.getReadWriteLock().writeLock().unlock();
            fail("failed to receive an IllegalStateException when unlocking and unlocked writelock");
        } catch (IllegalMonitorStateException iae) {}

        // symmetric locking/unlocking of the readlock should succeed
        source.getReadWriteLock().readLock().lock();
        source.getReadWriteLock().readLock().unlock();

        // symmetric locking/unlocking of the writelock should succeed
        source.getReadWriteLock().writeLock().lock();
        source.getReadWriteLock().writeLock().unlock();
    }

    public void testRemoveAllOnSelf() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        original.removeAll(original);
        assertEquals(Collections.EMPTY_LIST, original);
    }

    public void testRemoveAllFromView() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        FilterList<String> filtered = new FilterList<String>(original, (Matcher) Matchers.trueMatcher());
        original.removeAll(filtered);
        assertEquals(Collections.EMPTY_LIST, original);
    }

    public void testRemoveAllOnView() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        FilterList<String> filtered = new FilterList<String>(original, (Matcher)Matchers.trueMatcher());
        filtered.removeAll(filtered);
        assertEquals(Collections.EMPTY_LIST, original);
    }

    public void testRetainAllOnSelf() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        original.retainAll(original);
        assertEquals(GlazedListsTests.stringToList("ABCDE"), original);
    }

    public void testRetainAllFromView() {
        EventList<Integer> original = new BasicEventList<Integer>();
        original.addAll(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40) }));
        FilterList<Integer> filtered = new FilterList<Integer>(original, GlazedListsTests.matchAtLeast(20));
        original.retainAll(filtered);
        assertEquals(Arrays.asList(new Integer[] { new Integer(20), new Integer(30), new Integer(40) }), original);
    }

    public void testRetainAllOnView() {
        EventList<Integer> original = new BasicEventList<Integer>();
        original.addAll(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40) }));
        FilterList<Integer> filtered = new FilterList<Integer>(original, GlazedListsTests.matchAtLeast(20));
        filtered.retainAll(filtered);
        assertEquals(Arrays.asList(new Integer[] { new Integer(20), new Integer(30), new Integer(40) }), original);
    }

    public void testAddAllOnSelf() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        original.addAll(original);
        assertEquals(GlazedListsTests.stringToList("ABCDEABCDE"), original);
    }

    public void testSublistClear() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));

        Iterator<String> iterator = original.subList(2, 4).iterator();
        iterator.next();
        iterator.remove();
        iterator.next();
        iterator.remove();

        assertEquals(GlazedListsTests.stringToList("ABE"), original);
    }

    public void testAddAllFromView() {
        EventList<Integer> original = new BasicEventList<Integer>();
        original.addAll(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40) }));
        FilterList<Integer> filtered = new FilterList<Integer>(original, GlazedListsTests.matchAtLeast(20));
        original.addAll(filtered);
        assertEquals(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40), new Integer(20), new Integer(30), new Integer(40) }), original);
    }

    public void testAddAllOnView() {
        if(true) throw new UnsupportedOperationException("This test case fails, but it takes a long time to do so!");
        EventList<Integer> original = new BasicEventList<Integer>();
        original.addAll(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40) }));
        FilterList<Integer> filtered = new FilterList<Integer>(original, GlazedListsTests.matchAtLeast(20));
        filtered.addAll(filtered);
        assertEquals(Arrays.asList(new Integer[] { new Integer(0), new Integer(10), new Integer(20), new Integer(30), new Integer(40), new Integer(20), new Integer(30), new Integer(40) }), original);
    }

    public void testSimpleAddAll() {
        EventList<String> source = new BasicEventList<String>();
        installConsistencyListener(source);
        FilterList<String> filterList = new FilterList<String>(source, (Matcher)Matchers.trueMatcher());

        filterList.addAll(GlazedListsTests.stringToList("JESSE"));
        assertEquals(GlazedListsTests.stringToList("JESSE"), source);
        assertEquals(GlazedListsTests.stringToList("JESSE"), filterList);


    }

    /**
     * This test case was generated from a problem that we received in the field.
     * It occured when a crazy amount of list events were being combined into one,
     * and we failed to create a simpler test case that still demonstrated the
     * problematic behaviour. This is probably due to the way that we sort list
     * events while processing them.
     */
    public void testCombineEvents() {
        ExternalNestingEventList<Object> list = new ExternalNestingEventList<Object>(new BasicEventList<Object>(), true);
        for (int i = 0; i < 16; i++)
             list.add(new Integer(0));

        ListConsistencyListener.install(list);

        list.beginEvent(true);

        for(int i = 0; i < 4; i++) list.add(8, new Object());
        for(int j = 7; j >= 0; j--) {
            for(int i = 0; i < 10; i++) list.add(j, new Object());
        }
        list.remove(55);
        list.remove(95);
        list.remove(14);
        list.remove(22);
        list.remove(27);
        list.remove(78);
        list.remove(1);
        list.remove(85);
        list.remove(52);
        list.remove(14);
        list.remove(39);
        list.remove(38);
        list.remove(61);
        list.remove(69);
        list.remove(8);
        list.remove(57);
        list.remove(10);
        list.remove(5);
        list.remove(71);
        list.remove(60);
        list.remove(42);
        list.remove(21);
        list.remove(15);
        list.remove(59);
        list.remove(15);
        list.remove(14);
        list.remove(24);
        list.remove(43);
        list.remove(35);
        list.remove(12);
        list.remove(11);
        list.remove(34);
        list.remove(42);
        list.remove(32);
        list.remove(19);
        list.add(32, new Integer(92));
        list.remove(44);
        list.remove(19);
        list.remove(45);
        list.remove(55);
        list.remove(23);
        list.remove(11);
        list.remove(8);
        list.remove(50);
        list.remove(29);
        list.remove(31);
        list.remove(33);
        list.remove(45);
        list.remove(15);
        list.remove(25);
        list.remove(8);
        list.add(40, new Integer(95));
        list.remove(32);
        list.remove(3);
        list.remove(26);
        list.remove(14);
        list.remove(36);
        list.add(39, new Integer(96));
        list.remove(34);
        list.remove(21);
        list.remove(13);
        list.remove(32);
        list.remove(30);
        list.add(36, new Integer(97));
        list.remove(43);
        list.remove(2);
        list.remove(34);
        list.remove(35);
        list.remove(17);
        list.add(39, new Integer(98));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.add(29, new Integer(99));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.add(22, new Integer(100));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.set(25, new Integer(101)); // critical
        for(int j = 0; j < 4; j++) {
            for(int i = 0; i < 5; i++) list.remove(0);
            list.add(0, new Integer(102));
        }
        for(int i = 0; i < 10; i++) list.remove(0);
        list.add(0, new Integer(107));
        for(int i = 0; i < 2; i++) list.remove(0);

        list.commitEvent();
    }

    /**
     * Install a consistency listener to the specified list.
     */
    private static void installConsistencyListener(List list) {
        if(list instanceof BasicEventList) {
            ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install((BasicEventList)list);
            listConsistencyListener.setRemovedElementTracked(true);
        }
    }
}