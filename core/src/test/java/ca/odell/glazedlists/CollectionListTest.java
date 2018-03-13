/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEventAssembler;
import ca.odell.glazedlists.event.ListEventPublisher;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.util.concurrent.LockFactory;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the {@link CollectionList} behaves as expected.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class CollectionListTest {

    private static final String DEV_ROB = "Rob Eden";
    private static final String DEV_JESSE = "Jesse Wilson";
    private static final String DEV_KEVIN = "Kevin Maltby";
    private static final String DEV_JAMES = "James Lemieux";
    private static final String DEV_EMPTY = "";

    private CollectionList<String, String> collectionList;
    private BasicEventList<String> parentList;

    /**
     * Do basic setup for the tests.
     */
    @Before
    public void setUp() {
        parentList = new BasicEventList<>();
        collectionList = new CollectionList<>(parentList, new StringDecomposerModel());
        parentList.add(DEV_ROB);
        parentList.add(DEV_JESSE);
        parentList.add(DEV_KEVIN);
        parentList.add(DEV_JAMES);

        ListConsistencyListener.install(collectionList);
    }

    /**
     * Clean up after the tests.
     */
    @After
    public void tearDown() {
        collectionList.dispose();
        collectionList = null;
        parentList.clear();
        parentList = null;
    }

    /**
     * Make sure the correct events are being fired.
     */
    @Test
    public void testFireEvents() {
        parentList.add(DEV_ROB);
        parentList.add(DEV_JESSE);
        parentList.add(DEV_KEVIN);
        parentList.add(DEV_JAMES);
        parentList.remove(2);
        parentList.remove(2);
        parentList.add(DEV_EMPTY);
        parentList.remove(2);
        parentList.add(0, DEV_EMPTY);
        parentList.remove(0);

        assertEquals(parentList.get(0), DEV_ROB);
        assertEquals(parentList.get(1), DEV_JESSE);
        assertEquals(collectionList.get(0), "R");
        assertEquals(collectionList.get(8), "J");
    }

    @Test
    public void testMutateThroughCollectionList() {
        final EventList<List<String>> source = new BasicEventList<>();
        final CollectionList<List<String>, String> collectionList = new CollectionList<>(source, new SimpleListModel());
        ListConsistencyListener.install(collectionList);

        // test with a simple List
        final List<String> list1 = new ArrayList<>();
        list1.add("Jesse");

        source.add(list1);
        assertEquals("Jesse", collectionList.get(0));

        collectionList.set(0, "James");
        assertEquals("James", collectionList.get(0));

        collectionList.remove(0);
        assertTrue(collectionList.isEmpty());

        // test with a simple EventList
        final List<String> list2 = new BasicEventList<>(collectionList.getPublisher(), collectionList.getReadWriteLock());
        list2.add("Jesse");

        source.set(0, list2);
        assertEquals("Jesse", collectionList.get(0));

        collectionList.set(0, "James");
        assertEquals("James", collectionList.get(0));

        collectionList.remove(0);
        assertTrue(collectionList.isEmpty());
    }

    /**
     * Make sure all the data is there in the order we expect.
     */
    @Test
    public void testBasicData() {
        assertEquals2(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);
    }

    /**
     * Test the starting index mappings.
     */
    @Test
    public void testStartMappings() {
        // Normal cases

        // Rob Eden
        // ^
        assertEquals(0, collectionList.childStartingIndex(0));	// Rob

        // Rob EdenJesse Wilson
        //         ^
        assertEquals(DEV_ROB.length(), collectionList.childStartingIndex(1));	// Jesse

        // Rob EdenJesse WilsonKevin Maltby
        //                     ^
        assertEquals(DEV_ROB.length() + DEV_JESSE.length(),
            collectionList.childStartingIndex(2));	// Kevin

        // Rob EdenJesse WilsonKevin MaltbyJames Lemieux
        //                                 ^
        assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length(),
            collectionList.childStartingIndex(3));	// James

        // Too low
        try {
            collectionList.childStartingIndex(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        // Too high
        try {
            collectionList.childStartingIndex(4);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        // extra cases for empty strings
        parentList.add(1, DEV_EMPTY);
        assertEquals(-1, collectionList.childStartingIndex(1)); // EMPTY

        // extra cases for empty strings
        parentList.add(5, DEV_EMPTY);
        assertEquals(-1, collectionList.childStartingIndex(5)); // EMPTY
    }

    /**
     * Test the ending index mappings.
     */
    @Test
    public void testEndMappings() {
        // Normal cases

        // Rob Eden
        //        ^
        assertEquals(DEV_ROB.length() - 1,
            collectionList.childEndingIndex(0));	// Rob

        // Rob EdenJesse Wilson
        //                    ^
        assertEquals(DEV_ROB.length() + DEV_JESSE.length() - 1,
            collectionList.childEndingIndex(1));	// Jesse

        // Rob EdenJesse WilsonKevin Maltby
        //                                ^
        assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length() - 1,
            collectionList.childEndingIndex(2));	// Kevin

        // Rob EdenJesse WilsonKevin MaltbyJames Lemieux
        //                                             ^
        assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length() +
            DEV_JAMES.length() - 1, collectionList.childEndingIndex(3));	// James

        // Too low
        try {
            assertEquals(-1, collectionList.childEndingIndex(-1));
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        // Too high
        try {
            assertEquals(-1, collectionList.childEndingIndex(4));
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        // extra cases for empty strings
        parentList.add(1, DEV_EMPTY);
        assertEquals(-1, collectionList.childEndingIndex(1)); // EMPTY

        // extra cases for empty strings
        parentList.add(5, DEV_EMPTY);
        assertEquals(-1, collectionList.childEndingIndex(5)); // EMPTY
    }


    /**
     * Test modifying the list
     */
    @Test
    public void testModification() {
        // Remove an item  (tests Delete)
        parentList.remove(DEV_ROB);
        assertEquals2(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);

        // Put it on the end (test Insert)
        parentList.add(DEV_ROB);
        assertEquals2(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, DEV_ROB }, collectionList);

        // Replace it with something else (tests Update)
        parentList.set(parentList.size() - 1, "Nede Bor");
        assertEquals2(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, "Nede Bor" }, collectionList);

        // Remove that (tests Delete)
        parentList.remove(parentList.size() - 1);
        assertEquals2(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);

        // Add an empty item in the middle  (tests empty parents)
        parentList.add(1, "");
        assertEquals2(new String[]{ DEV_JESSE, "", DEV_KEVIN, DEV_JAMES }, collectionList);

        // Clear the parent (tests a bunch of Deletes and size 0)
        parentList.clear();
        assertEquals2(new String[]{ }, collectionList);

        // Put it all back to normal (tests a bunch on Inserts)
        parentList.add(DEV_ROB);
        parentList.add(DEV_JESSE);
        parentList.add(DEV_KEVIN);
        parentList.add(DEV_JAMES);
        assertEquals2(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);
    }

    /**
     * Test modifying the children.
     */
    @Test
    public void testChildModification() {
        // use a list of Lists instead of Strings
        BasicEventList<List<String>> characterLists = new BasicEventList<>();
        CollectionList<List<String>, String> characters = new CollectionList<>(characterLists, GlazedLists.<String>listCollectionListModel());
        ListConsistencyListener.install(characters);

        characterLists.add(GlazedListsTests.stringToList(DEV_ROB));
        characterLists.add(GlazedListsTests.stringToList(DEV_JESSE));
        characterLists.add(GlazedListsTests.stringToList(DEV_KEVIN));
        characterLists.add(GlazedListsTests.stringToList(DEV_JAMES));

        StringBuffer DEV_ROB_MODIFIED = new StringBuffer();
        DEV_ROB_MODIFIED.append(DEV_ROB);
        StringBuffer DEV_KEVIN_MODIFIED = new StringBuffer();
        DEV_KEVIN_MODIFIED.append(DEV_KEVIN);

        // they should match out-of-the-gate
        assertEquals2(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // remove a single element
        DEV_ROB_MODIFIED.deleteCharAt(2);
        characters.remove(2);
        assertEquals2(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // set a few elements
        DEV_KEVIN_MODIFIED.setCharAt(2, 'r');
        DEV_KEVIN_MODIFIED.setCharAt(1, 'a');
        DEV_KEVIN_MODIFIED.setCharAt(3, 'e');
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 2, "r");
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 1, "a");
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 3, "e");
        assertEquals2(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // set a few more elements
        DEV_ROB_MODIFIED.setCharAt(1, '.');
        characters.set(1, ".");
        assertEquals2(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // remove a few more elements
        DEV_ROB_MODIFIED.deleteCharAt(1);
        DEV_ROB_MODIFIED.deleteCharAt(1);
        characters.remove(1);
        characters.remove(1);
        assertEquals2(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);
    }

    /**
     * Tests disposing of CollectionList, where the model produces plain lists containing the
     * children.
     */
    @Test
    public void testDisposeWithPlainListChildren() {
        final List<String> abcList = GlazedListsTests.stringToList("ABC");
        final List<String> defList = GlazedListsTests.stringToList("DEF");
        final List<String> ghiList = GlazedListsTests.stringToList("GHI");
        final EventList<List<String>> parentEventList = new BasicEventList<>();
        parentEventList.add(abcList);
        parentEventList.add(defList);
        parentEventList.add(ghiList);

        final CollectionList<List<String>, String> collectionList =
            new CollectionList<>(parentEventList, GlazedLists.<String>listCollectionListModel());

        final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<>();
        collectionList.addListEventListener(eventCounter);

        // modify source list
        parentEventList.add(ghiList);
        assertEquals(1, eventCounter.getCountAndReset());
        parentEventList.remove(defList);
        assertEquals(1, eventCounter.getCountAndReset());

        // modify child lists
        // these are no EventLists, so no ListEvents are expected
        abcList.add("D");
        assertEquals(0, eventCounter.getCountAndReset());
        abcList.remove("A");
        assertEquals(0, eventCounter.getCountAndReset());

        // dispose CollectionList, no ListEvents are expected further on
        collectionList.dispose();

        // modify source list after disposing
        parentEventList.remove(ghiList);
        assertEquals(0, eventCounter.getCountAndReset());
        parentEventList.add(0, defList);
        assertEquals(0, eventCounter.getCountAndReset());

        // modify child lists after disposing
        abcList.add("E");
        assertEquals(0, eventCounter.getCountAndReset());
        abcList.remove("B");
        assertEquals(0, eventCounter.getCountAndReset());
    }

    /**
     * Ensure all child EventLists are validated to ensure they share the same
     * locks and publisher as the CollectionList they are members of. This
     * involves checking in two areas:
     *
     * 1) The CollectionList's constructor needs to validate the invariant
     * 2) adding further EventLists to the CollectionList after construction
     *    also needs to validate the invariant
     */
    @Test
    public void testEventListChildrenMustUseSameLocksAsCollectionList() {
        EventList<List<String>> source;

        source = new BasicEventList<>();
        new CollectionList<>(source, GlazedLists.<String>listCollectionListModel());
        // this works because the BasicEventList we are adding shares a publisher and locks with the CollectionList
        source.add(new BasicEventList<String>(source.getPublisher(), source.getReadWriteLock()));

        source = new BasicEventList<>();
        new CollectionList<>(source, GlazedLists.<String>listCollectionListModel());
        try {
            // try to add a new BasicEventList that uses its own publisher - it should fail
            source.add(new BasicEventList<String>(ListEventAssembler.createListEventPublisher(), source.getReadWriteLock()));
            fail("failed to receive an IllegalArgumentException when child EventList did not share the same publisher");
        } catch (IllegalArgumentException e) {
            // expected
        }

        source = new BasicEventList<>();
        new CollectionList<>(source, GlazedLists.<String>listCollectionListModel());
        try {
            // try to add a new BasicEventList that uses its own locks - it should fail
            source.add(new BasicEventList<String>(source.getPublisher(), LockFactory.DEFAULT.createReadWriteLock()));
            fail("failed to receive an IllegalArgumentException when child EventList did not share the same locks");
        } catch (IllegalArgumentException e) {
            // expected
        }

        source = new BasicEventList<>();
        source.add(new BasicEventList<String>(ListEventAssembler.createListEventPublisher(), source.getReadWriteLock()));
        try {
            // try to create a CollectionList where one of the child EventLists uses a bad publisher - it should fail
            new CollectionList<>(source, GlazedLists.<String>listCollectionListModel());
            fail("failed to receive an IllegalArgumentException when child EventList did not share the same publisher");
        } catch (IllegalArgumentException e) {
            // expected
        }

        source = new BasicEventList<>();
        source.add(new BasicEventList<String>(source.getPublisher(), LockFactory.DEFAULT.createReadWriteLock()));
        try {
            // try to create a CollectionList where one of the child EventLists uses a bad set of locks - it should fail
            new CollectionList<>(source, GlazedLists.<String>listCollectionListModel());
            fail("failed to receive an IllegalArgumentException when child EventList did not share the same publisher");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests disposing of CollectionList, where the model produces EventLists
     * containing the children.
     */
    @Test
    public void testDisposeWithEventListChildren() {
        final EventList<List<String>> parentEventList = new BasicEventList<>();
        final ListEventPublisher sharedpublisher = parentEventList.getPublisher();
        final ReadWriteLock sharedLocks = parentEventList.getReadWriteLock();

        // make sure all of the child lists are built using the shared ListEventPublisher and ReadWriteLocks
        final List<String> abcList = GlazedListsTests.stringToList("ABC");
        final EventList<String> abcEventList = new BasicEventList<>(sharedpublisher, sharedLocks);
        abcEventList.addAll(abcList);

        final List<String> defList = GlazedListsTests.stringToList("DEF");
        final EventList<String> defEventList = new BasicEventList<>(sharedpublisher, sharedLocks);
        defEventList.addAll(defList);

        final List<String> ghiList = GlazedListsTests.stringToList("GHI");
        final EventList<String> ghiEventList = new BasicEventList<>(sharedpublisher, sharedLocks);
        ghiEventList.addAll(ghiList);

        parentEventList.add(abcEventList);
        parentEventList.add(defEventList);
        parentEventList.add(ghiEventList);

        final CollectionList<List<String>, String> collectionList =
            new CollectionList<>(parentEventList, GlazedLists.<String>listCollectionListModel());

        final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<>();
        collectionList.addListEventListener(eventCounter);

        // modify source list
        parentEventList.add(ghiEventList);
        assertEquals(1, eventCounter.getCountAndReset());
        parentEventList.remove(defEventList);
        assertEquals(1, eventCounter.getCountAndReset());

        // modify child lists
        // these are EventLists, so changes are propagated to CollectionList
        abcEventList.add("D");
        assertEquals(1, eventCounter.getCountAndReset());
        abcEventList.remove("A");
        assertEquals(1, eventCounter.getCountAndReset());

        // dispose CollectionList, no ListEvents are expected further on
        collectionList.dispose();

        // modify source list after disposing
        parentEventList.remove(ghiEventList);
        assertEquals(0, eventCounter.getCountAndReset());
        parentEventList.add(0, defEventList);
        assertEquals(0, eventCounter.getCountAndReset());

        // modify child lists after disposing
        abcEventList.add("E");
        assertEquals(0, eventCounter.getCountAndReset());
        abcEventList.remove("B");
        assertEquals(0, eventCounter.getCountAndReset());
    }

    /**
     * Check the basic data on a CollectionList to make sure it's showing the characters
     * from the given Strings.
     */
    private void assertEquals2(CharSequence[] data, CollectionList collection_list) {
        assertEquals(GlazedListsTests.stringsToList(data), collection_list);
    }

    /**
     * Model that breaks a String into a list of characters.
     */
    private static class StringDecomposerModel implements CollectionList.Model<String,String> {
        @Override
		public List<String> getChildren(String parent) {
            return GlazedListsTests.stringToList(parent);
        }
    }

    /**
     * Model that returns the given List unchanged.
     */
    private static class SimpleListModel implements CollectionList.Model<List<String>,String> {
        @Override
		public List<String> getChildren(List<String> parent) {
            return parent;
        }
    }
}
