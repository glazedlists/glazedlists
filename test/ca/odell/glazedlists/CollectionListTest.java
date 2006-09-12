/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.List;

/**
 * This test verifies that the {@link CollectionList} behaves as expected.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class CollectionListTest extends TestCase {

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
    public void setUp() {
        parentList = new BasicEventList<String>();
        collectionList = new CollectionList<String, String>(parentList, new StringDecomposerModel());
        ListConsistencyListener.install(collectionList);
        parentList.add(DEV_ROB);
        parentList.add(DEV_JESSE);
        parentList.add(DEV_KEVIN);
        parentList.add(DEV_JAMES);
    }

    /**
     * Clean up after the tests.
     */
    public void tearDown() {
        collectionList.dispose();
        collectionList = null;
        parentList.clear();
        parentList = null;
    }

    /**
     * Make sure the correct events are being fired.
     */
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


    /**
     * Make sure all the data is there in the order we expect.
     */
    public void testBasicData() {
        assertEquals(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);
    }

    /**
     * Test the starting index mappings.
     */
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
    public void testModification() {
        // Remove an item  (tests Delete)
        parentList.remove(DEV_ROB);
        assertEquals(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);

        // Put it on the end (test Insert)
        parentList.add(DEV_ROB);
        assertEquals(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, DEV_ROB }, collectionList);

        // Replace it with something else (tests Update)
        parentList.set(parentList.size() - 1, "Nede Bor");
        assertEquals(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, "Nede Bor" }, collectionList);

        // Remove that (tests Delete)
        parentList.remove(parentList.size() - 1);
        assertEquals(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);

        // Add an empty item in the middle  (tests empty parents)
        parentList.add(1, "");
        assertEquals(new String[]{ DEV_JESSE, "", DEV_KEVIN, DEV_JAMES }, collectionList);

        // Clear the parent (tests a bunch of Deletes and size 0)
        parentList.clear();
        assertEquals(new String[]{ }, collectionList);

        // Put it all back to normal (tests a bunch on Inserts)
        parentList.add(DEV_ROB);
        parentList.add(DEV_JESSE);
        parentList.add(DEV_KEVIN);
        parentList.add(DEV_JAMES);
        assertEquals(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collectionList);
    }

    /**
     * Test modifying the children.
     */
    public void testChildModification() {
        // use a list of Lists instead of Strings
        BasicEventList<List<String>> characterLists = new BasicEventList<List<String>>();
        CollectionList<List<String>, String> characters = new CollectionList<List<String>, String>(characterLists, (CollectionList.Model)GlazedLists.listCollectionListModel());
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
        assertEquals(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // remove a single element
        DEV_ROB_MODIFIED.deleteCharAt(2);
        characters.remove(2);
        assertEquals(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // set a few elements
        DEV_KEVIN_MODIFIED.setCharAt(2, 'r');
        DEV_KEVIN_MODIFIED.setCharAt(1, 'a');
        DEV_KEVIN_MODIFIED.setCharAt(3, 'e');
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 2, "r");
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 1, "a");
        characters.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 3, "e");
        assertEquals(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // set a few more elements
        DEV_ROB_MODIFIED.setCharAt(1, '.');
        characters.set(1, ".");
        assertEquals(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);

        // remove a few more elements
        DEV_ROB_MODIFIED.deleteCharAt(1);
        DEV_ROB_MODIFIED.deleteCharAt(1);
        characters.remove(1);
        characters.remove(1);
        assertEquals(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, characters);
    }

    /**
     * Tests disposing of CollectionList, where the model produces plain lists containing the
     * children.  
     */
    public void testDisposeWithPlainListChildren() {
        final List<String> abcList = GlazedListsTests.stringToList("ABC");
        final List<String> defList = GlazedListsTests.stringToList("DEF");
        final List<String> ghiList = GlazedListsTests.stringToList("GHI");
        final EventList<List<String>> parentEventList = new BasicEventList<List<String>>();
        parentEventList.add(abcList);
        parentEventList.add(defList);
        parentEventList.add(ghiList);

        final CollectionList<List<String>, String> collectionList =
            new CollectionList<List<String>, String>(parentEventList, (CollectionList.Model) GlazedLists.listCollectionListModel());

        final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<String>();
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
     * Tests disposing of CollectionList, where the model produces EventLists containing the
     * children.  
     */
    public void testDisposeWithEventListChildren() {
        final List<String> abcList = GlazedListsTests.stringToList("ABC");
        final EventList<String> abcEventList = GlazedLists.eventList(abcList);
        final List<String> defList = GlazedListsTests.stringToList("DEF");
        final EventList<String> defEventList = GlazedLists.eventList(defList);
        final List<String> ghiList = GlazedListsTests.stringToList("GHI");
        final EventList<String> ghiEventList = GlazedLists.eventList(ghiList);
        final EventList<List<String>> parentEventList = new BasicEventList<List<String>>();
        parentEventList.add(abcEventList);
        parentEventList.add(defEventList);
        parentEventList.add(ghiEventList);

        final CollectionList<List<String>, String> collectionList =
            new CollectionList<List<String>, String>(parentEventList, (CollectionList.Model) GlazedLists.listCollectionListModel());

        final GlazedListsTests.ListEventCounter<String> eventCounter =
            new GlazedListsTests.ListEventCounter<String>();
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
    private void assertEquals(CharSequence[] data, CollectionList collection_list) {
        assertEquals(GlazedListsTests.stringsToList(data), collection_list);
    }

    /**
     * Model that breaks a String into a list of characters.
     */
    private static class StringDecomposerModel implements CollectionList.Model<String,String> {
        public List<String> getChildren(String parent) {
            return GlazedListsTests.stringToList(parent);
        }
    }
}
