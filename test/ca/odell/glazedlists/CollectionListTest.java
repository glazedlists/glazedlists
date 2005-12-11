/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;
import java.util.*;

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
        collectionList.addListEventListener(new ListConsistencyListener(collectionList, "collection list", false));
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
        characters.addListEventListener(new ListConsistencyListener(characters, "characters", false));
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
	 * Check the basic data on a CollectionList to make sure it's showing the characters
	 * from the given Strings.
	 */
	private void assertEquals(CharSequence[] data, CollectionList collection_list) {
        assertEquals(GlazedListsTests.stringsToList(data), collection_list);
    }

	/**
	 * Model that breaks a String into a list of characters.
	 */
	private class StringDecomposerModel implements CollectionList.Model<String,String> {
		public List<String> getChildren(String parent) {
			return GlazedListsTests.stringToList(parent);
        }
	}
}
