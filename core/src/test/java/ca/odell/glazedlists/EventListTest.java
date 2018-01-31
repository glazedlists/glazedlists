/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Verifies that EventList matches the List API.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListTest {

    /**
     * Validates that removeAll() works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=169">Bug 169</a>
     */
    @Test
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
    @Test
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
    @Test
    public void testContainsNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertEquals(false, list.contains(null));
            assertEquals(true,  list.contains("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertEquals(true, list.contains(null));
            assertEquals(true, list.contains("Western"));
            assertEquals(false, list.contains("Molson"));
        }
    }

    /**
     * Validates that containsAll() works with null.
     */
    @Test
    public void testContainsAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertEquals(true, list.containsAll(Arrays.asList("Sleeman", "Molson")));
            assertEquals(false, list.containsAll(Arrays.asList("Molson", null)));
            assertEquals(false, list.containsAll(Arrays.asList("Molson", "Busch")));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertEquals(false, list.containsAll(Arrays.asList("Sleeman", "Molson")));
            assertEquals(true, list.containsAll(Arrays.asList("Sleeman", "Western")));
            assertEquals(true, list.containsAll(Arrays.asList("Western", null)));
            assertEquals(true, list.containsAll(Arrays.asList(null, null)));
        }
    }

    /**
     * Validates that indexOf() works with null.
     */
    @Test
    public void testIndexOfNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertTrue(-1 == list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertTrue(-1 != list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));
            assertTrue(-1 == list.indexOf("Molson"));
        }
    }

    /**
     * Validates that lastIndexOf() works with null.
     */
    @Test
    public void testLastIndexOfNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertTrue(-1 == list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertTrue(-1 != list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));
            assertTrue(-1 == list.lastIndexOf("Molson"));
        }
    }

    /**
     * Validates that remove() works with null.
     */
    @Test
    public void testRemoveNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();
            installConsistencyListener(list);

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertEquals(false, list.remove(null));
            assertEquals(true,  list.remove("Sleeman"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertEquals(true, list.remove(null));
            assertEquals(true, list.remove("Western"));
            assertEquals(false, list.remove("Molson"));
        }
    }

    /**
     * Validates that removeAll() works with null.
     */
    @Test
    public void testRemoveAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertEquals(true, list.removeAll(Arrays.asList("Western", null)));
            assertEquals(false,  list.removeAll(Arrays.asList(null, "Busch")));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertEquals(true, list.removeAll(Arrays.asList("Western", "Busch")));
            assertEquals(true, list.removeAll(Arrays.asList("Sleeman", null)));
            assertEquals(false, list.removeAll(Arrays.asList("Western", null)));
        }
    }

    /**
     * Validates that retainAll() works with null.
     */
    @Test
    public void testRetainAllNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            assertEquals(true,  list.retainAll(Arrays.asList("Western", null)));
            assertEquals(true, list.retainAll(Arrays.asList("Moslon", null)));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            assertEquals(true,  list.retainAll(Arrays.asList("Western", null)));
            assertEquals(true, list.retainAll(Arrays.asList("Moslon", null)));
        }
    }

    /**
     * Validates that hashCode() works with null.
     */
    @Test
    public void testHashCodeNull() {
        // get all different list types
        List<List<String>> listTypes = new ArrayList<List<String>>();
        listTypes.add(new ArrayList<String>());
        listTypes.add(new BasicEventList<String>());
        listTypes.add(SortedList.create(new BasicEventList<String>()));

        // test all different list types
        for(Iterator<List<String>> i = listTypes.iterator(); i.hasNext();) {
            List<String> list = i.next();
            List<String> copy = new ArrayList<String>();

            // test a list that doesn't contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList("Molson", "Sleeman", "Labatts", "Western"));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));

            // test a list that does contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList(null, "Sleeman", null, "Western"));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));
        }
    }

    /**
     * Test that the {@link GlazedLists#eventListOf(Object...)} factory
     * method works.
     */
    @Test
    public void testGlazedListsEventListUsingVarArgs() {
        // make sure they have different backing stores
        final EventList<String> eventList = GlazedLists.eventListOf("A", "B");
        assertEquals(Arrays.asList("A", "B"), eventList);

        // make sure null is supported
        final EventList<String> empty = GlazedLists.eventListOf((String[]) null);
        assertEquals(Collections.EMPTY_LIST, empty);
    }

    /**
     * Test that the {@link GlazedLists#eventList(java.util.Collection)} factory
     * method works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=234">Bug 234</a>
     */
    @Test
    public void testGlazedListsEventList() {
        // make sure they have different backing stores
        final List<String> list = new ArrayList<String>();
        EventList<String> eventList = GlazedLists.eventList(list);
        assertEquals(list, eventList);

        list.add("A");
        assertTrue(!list.equals(eventList));

        eventList.add("B");
        assertTrue(!list.equals(eventList));

        // make sure null is supported
        final EventList<String> empty = GlazedLists.eventList((Collection<String>) null);
        assertEquals(Collections.EMPTY_LIST, empty);
    }

    /**
     * Test that the
     * {@link GlazedLists#eventListOf(ListEventPublisher, ReadWriteLock, Object...)} factory
     * method works.
     */
    @Test
    public void testGlazedListsEventListUsingVarArgsAndPublisherLock() {
        // make sure they have different backing stores
        final ListEventPublisher publisher = ListEventAssembler.createListEventPublisher();
        final ReadWriteLock lock = LockFactory.DEFAULT.createReadWriteLock();
        final EventList<String> eventList = GlazedLists.eventListOf(publisher, lock, "A", "B");
        assertEquals(Arrays.asList("A", "B"), eventList);
        assertEquals(publisher, eventList.getPublisher());
        assertEquals(lock, eventList.getReadWriteLock());

        // make sure null is supported
        final EventList<String> empty = GlazedLists.eventListOf(publisher, lock, (String[]) null);
        assertEquals(Collections.EMPTY_LIST, empty);
        assertEquals(publisher, empty.getPublisher());
        assertEquals(lock, empty.getReadWriteLock());
    }

    /**
     * Test that the {@link GlazedLists#eventList(ListEventPublisher, ReadWriteLock, Collection)}
     * factory method works.
     */
    @Test
    public void testGlazedListsEventListUsingPublisherLock() {
        // make sure they have different backing stores
        final List<String> list = new ArrayList<String>();
        final ListEventPublisher publisher = ListEventAssembler.createListEventPublisher();
        final ReadWriteLock lock = LockFactory.DEFAULT.createReadWriteLock();
        final EventList<String> eventList = GlazedLists.eventList(publisher, lock, list);
        assertEquals(list, eventList);
        assertEquals(publisher, eventList.getPublisher());
        assertEquals(lock, eventList.getReadWriteLock());

        list.add("A");
        assertTrue(!list.equals(eventList));

        eventList.add("B");
        assertTrue(!list.equals(eventList));

        // make sure null is supported
        final EventList<String> empty = GlazedLists.eventList(publisher, lock, (Collection<String>) null);
        assertEquals(Collections.EMPTY_LIST, empty);
        assertEquals(publisher, empty.getPublisher());
        assertEquals(lock, empty.getReadWriteLock());
    }

    /**
     * Tests the {@link GlazedLists#syncEventListToList(EventList, List)} factory method.
     */
    @Test
    public void testGlazedListsSyncToList() {
        EventList<String> source = new BasicEventList<String>();
        source.add("McCallum");
        source.add("Keith");
        List<String> target = new ArrayList<String>();
        target.add("Greene");

        SyncListener<String> listener = GlazedLists.syncEventListToList(source, target);
        assertEquals(source, target);

        source.add("Szakra");
        assertEquals(source, target);

        source.addAll(Arrays.asList("Moore", "Holmes"));
        assertEquals(source, target);

        source.add(1, "Burris");
        assertEquals(source, target);

        source.set(1, "Crandell");
        assertEquals(source, target);

        Collections.sort(source);
        assertEquals(source, target);

        source.removeAll(Arrays.asList("Holmes", "Keith", "Szakra"));
        assertEquals(source, target);

        source.clear();
        assertEquals(source, target);

        listener.dispose();
        source.add("Davis");
        assertFalse(source.equals(target));
        
        // ensure double dispose is doing no harm
        listener.dispose();
    }
    
    /**
     * Tests the {@link GlazedLists#syncEventListToEventList(EventList, EventList)} factory method.
     */
    @Test
    public void testGlazedListsSyncToEventList() {
        EventList<String> source = new BasicEventList<String>();
        source.add("McCallum");
        source.add("Keith");
        EventList<String> target = new BasicEventList<String>();
        target.add("Greene");

        LockbasedSyncListener<String> listener = GlazedLists.syncEventListToEventList(source, target);
        assertEquals(source, target);

        source.add("Szakra");
        assertEquals(source, target);

        source.addAll(Arrays.asList("Moore", "Holmes"));
        assertEquals(source, target);

        source.add(1, "Burris");
        assertEquals(source, target);

        source.set(1, "Crandell");
        assertEquals(source, target);

        Collections.sort(source);
        assertEquals(source, target);

        source.removeAll(Arrays.asList("Holmes", "Keith", "Szakra"));
        assertEquals(source, target);

        source.clear();
        assertEquals(source, target);

        listener.dispose();
        source.add("Davis");
        assertFalse(source.equals(target));
        
        // ensure double dispose is doing no harm
        listener.dispose();
    }

    @Test
    public void testEventListTypeSafety() {
        EventList<Object> source = new BasicEventList<Object>();
        final Set<Class> acceptedTypes = new HashSet<Class>();
        acceptedTypes.add(null);
        acceptedTypes.add(Integer.class);
        acceptedTypes.add(String.class);
        ListEventListener<Object> typeSafetyListener = GlazedLists.typeSafetyListener(source, acceptedTypes);

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
        source = new BasicEventList<Object>();
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

    @Test
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

    @Test
    public void testRemoveAllOnView() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        FilterList<String> filtered = new FilterList<String>(original, Matchers.trueMatcher());
        filtered.removeAll(filtered);
        assertEquals(Collections.EMPTY_LIST, original);
    }

    @Test
    public void testRetainAllOnSelf() {
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("ABCDE"));
        original.retainAll(original);
        assertEquals(GlazedListsTests.stringToList("ABCDE"), original);
    }

    @Test
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

    @Test
    public void testAddAllFromView() {
        EventList<Integer> original = new BasicEventList<Integer>();
        original.addAll(Arrays.asList(0, 10, 20, 30, 40));
        FilterList<Integer> filtered = new FilterList<Integer>(original, GlazedListsTests.matchAtLeast(20));
        original.addAll(filtered);
        assertEquals(Arrays.asList(0, 10, 20, 30, 40, 20, 30, 40), original);
    }

    @Test
    public void testSimpleAddAll() {
        EventList<String> source = new BasicEventList<String>();
        installConsistencyListener(source);
        FilterList<String> filterList = new FilterList<String>(source, Matchers.trueMatcher());

        filterList.addAll(GlazedListsTests.stringToList("JESSE"));
        assertEquals(GlazedListsTests.stringToList("JESSE"), source);
        assertEquals(GlazedListsTests.stringToList("JESSE"), filterList);
    }

    @Test
    public void testReplace() {
        EventList<String> source = new BasicEventList<String>();
        installConsistencyListener(source);
        source.addAll(GlazedListsTests.stringToList("ROUGHRIDERS"));
        source.set(2, "G");
        source.set(0, "T");
        source.set(4, "R");
        source.set(1, "I");
        source.set(3, "E");
        assertEquals(GlazedListsTests.stringToList("TIGERRIDERS"), source);
    }

    /**
     * This test case was generated from a problem that we received in the field.
     * It occured when a crazy amount of list events were being combined into one,
     * and we failed to create a simpler test case that still demonstrated the
     * problematic behaviour. This is probably due to the way that we sort list
     * events while processing them.
     */
    @Test
    public void testCombineEvents() {
        TransactionList<Object> list = new TransactionList<Object>(new BasicEventList<Object>(), true);
        for (int i = 0; i < 16; i++) {
            list.add(new Integer(0));
        }

        ListConsistencyListener.install(list);

        list.beginEvent();

        for(int i = 0; i < 4; i++) {
            list.add(8, new Object());
        }
        for(int j = 7; j >= 0; j--) {
            for(int i = 0; i < 10; i++) {
                list.add(j, new Object());
            }
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
            for(int i = 0; i < 5; i++) {
                list.remove(0);
            }
            list.add(0, new Integer(102));
        }
        for(int i = 0; i < 10; i++) {
            list.remove(0);
        }
        list.add(0, new Integer(107));
        for(int i = 0; i < 2; i++) {
            list.remove(0);
        }

        list.commitEvent();
    }

    @Test
    public void testGenericsOfListEvent() {
        final EventList<? extends String> source = GlazedLists.eventListOf((String[]) null);
        source.addListEventListener(new ListEventListener<Object>() {
            @Override
            public void listChanged(ListEvent<Object> listChanges) {
                listChanges.next();
                Object o = listChanges.getSourceList().get(listChanges.getIndex());
                assertEquals(String.class, o.getClass());
            }
        });

        source.addListEventListener(new ListEventListener<String>() {
            @Override
            public void listChanged(ListEvent<String> listChanges) {
                listChanges.next();
                String s = listChanges.getSourceList().get(listChanges.getIndex());
                assertEquals(String.class, s.getClass());
            }
        });

        ((EventList)source).add("Test");
    }

    @Test
    public void testAddNullListener() {
    	final EventList<String> source = GlazedLists.eventListOf("TEST");
    	try {
    		source.addListEventListener(null);
    		fail("expected NullPointerException");
    	} catch (NullPointerException ex) {
    		// expected
    	}
    }

    @Test
    public void testRemoveNullListener() {
    	final EventList<String> source = GlazedLists.eventListOf("TEST");
    	try {
    		source.removeListEventListener(null);
    		fail("expected NullPointerException");
    	} catch (NullPointerException ex) {
    		// expected
    	}
    }

    @Test
    public void testRemoveInvalidListener() {
    	final EventList<String> source = GlazedLists.eventListOf("TEST");
    	final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<String>();
    	try {
      		source.removeListEventListener(eventCounter);
    		fail("expected IllegalArgumentException");
    	} catch (IllegalArgumentException ex) {
    		// expected
    	}
    }

    @Test
    public void testAddRemoveListener() {
    	final BasicEventList<String> source = new BasicEventList<String>();
    	final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<String>();
  		source.addListEventListener(eventCounter);
  		assertTrue(source.updates.getListEventListeners().contains(eventCounter));
  		source.add("Test");
  		source.removeListEventListener(eventCounter);
  		assertFalse(source.updates.getListEventListeners().contains(eventCounter));
  		assertEquals(1, eventCounter.getCountAndReset());
    }

    @Test
    public void testAcceptWithReadLock() {
        final BasicEventList<String> source = new BasicEventList<String>();
        source.add("ONE");
        source.add("TWO");
        source.add("THREE");
        source.acceptWithReadLock(list -> assertEquals(3, list.size()));
    }

    @Test
    public void testApplyWithReadLock() {
        final BasicEventList<String> source = new BasicEventList<String>();
        source.add("ONE");
        source.add("TWO");
        source.add("THREE");
        int size = source.applyWithReadLock(list -> list.size());
        assertEquals(3, size);
    }

    @Test
    public void testAcceptWithWriteLock() {
        final BasicEventList<String> source = new BasicEventList<String>();
        source.add("ONE");
        source.add("TWO");
        source.add("THREE");
        source.acceptWithWriteLock(list -> list.add("FOUR"));
        String five = "FIVE";
        source.acceptWithWriteLock(list -> list.add(five));
        assertEquals(5, source.size());
    }

    @Test
    public void testApplyWithWriteLock() {
        final BasicEventList<String> source = new BasicEventList<String>();
        source.add("ONE");
        source.add("TWO");
        source.add("THREE");
        String result = source.applyWithWriteLock(list -> list.remove(1));
        assertEquals("TWO", result);
        String three = "THREE";
        boolean removed = source.applyWithWriteLock(list -> list.remove(three));
        assertTrue(removed);
    }

    /**
     * Install a consistency listener to the specified list.
     */
    private static void installConsistencyListener(List<String> list) {
        if (list instanceof BasicEventList) {
            ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install((EventList<String>) list);
            listConsistencyListener.setPreviousElementTracked(true);
        }
    }
}
