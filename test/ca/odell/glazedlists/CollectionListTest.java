/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
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

	private CollectionList collection_list;
	private BasicEventList parent_list;


	/**
	 * Do basic setup for the tests.
	 */
	public void setUp() {
		parent_list = new BasicEventList();
		collection_list = new CollectionList(parent_list, new StringDecomposerModel());
        collection_list.addListEventListener(new ConsistencyTestList(collection_list, "collection list", false));
		parent_list.add(DEV_ROB);
		parent_list.add(DEV_JESSE);
		parent_list.add(DEV_KEVIN);
		parent_list.add(DEV_JAMES);

	}

	/**
	 * Clean up after the tests.
	 */
	public void tearDown() {
		collection_list.dispose();
		collection_list = null;
		parent_list.clear();
		parent_list = null;
	}
    
    /**
     * Make sure the correct events are being fired.
     */
    public void testFireEvents() {
		parent_list.add(DEV_ROB);
		parent_list.add(DEV_JESSE);
		parent_list.add(DEV_KEVIN);
		parent_list.add(DEV_JAMES);
        parent_list.remove(2);
        parent_list.remove(2);
        parent_list.add(DEV_EMPTY);
        parent_list.remove(2);
        parent_list.add(0, DEV_EMPTY);
        parent_list.remove(0);
        
        assertEquals(parent_list.get(0), DEV_ROB);
        assertEquals(parent_list.get(1), DEV_JESSE);
        assertEquals(collection_list.get(0), new Character('R'));
        assertEquals(collection_list.get(8), new Character('J'));
    }


	/**
	 * Make sure all the data is there in the order we expect.
	 */
	public void testBasicData() {
		checkBasicListData(collection_list,
			new String[]{DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES});
	}

	/**
	 * Test the starting index mappings.
	 */
	public void testStartMappings() {
		// Normal cases

		// Rob Eden
		// ^
		assertEquals(0, collection_list.childStartingIndex(0));	// Rob

		// Rob EdenJesse Wilson
		//         ^
		assertEquals(DEV_ROB.length(), collection_list.childStartingIndex(1));	// Jesse

		// Rob EdenJesse WilsonKevin Maltby
		//                     ^
		assertEquals(DEV_ROB.length() + DEV_JESSE.length(),
			collection_list.childStartingIndex(2));	// Kevin

		// Rob EdenJesse WilsonKevin MaltbyJames Lemieux
		//                                 ^
		assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length(),
			collection_list.childStartingIndex(3));	// James
            
		// Too low
        try {
		    collection_list.childStartingIndex(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

		// Too high
        try {
            collection_list.childStartingIndex(4);
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
        
        // extra cases for empty strings
        parent_list.add(1, DEV_EMPTY);
		assertEquals(-1, collection_list.childStartingIndex(1)); // EMPTY

        // extra cases for empty strings
        parent_list.add(5, DEV_EMPTY);
		assertEquals(-1, collection_list.childStartingIndex(5)); // EMPTY
	}

	/**
	 * Test the ending index mappings.
	 */
	public void testEndMappings() {
		// Normal cases

		// Rob Eden
		//        ^
		assertEquals(DEV_ROB.length() - 1,
			collection_list.childEndingIndex(0));	// Rob

		// Rob EdenJesse Wilson
		//                    ^
		assertEquals(DEV_ROB.length() + DEV_JESSE.length() - 1,
			collection_list.childEndingIndex(1));	// Jesse

		// Rob EdenJesse WilsonKevin Maltby
		//                                ^
		assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length() - 1,
			collection_list.childEndingIndex(2));	// Kevin

		// Rob EdenJesse WilsonKevin MaltbyJames Lemieux
		//                                             ^
		assertEquals(DEV_ROB.length() + DEV_JESSE.length() + DEV_KEVIN.length() +
			DEV_JAMES.length() - 1, collection_list.childEndingIndex(3));	// James

		// Too low
        try {
            assertEquals(-1, collection_list.childEndingIndex(-1));
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

		// Too high
        try {
            assertEquals(-1, collection_list.childEndingIndex(4));
            fail("Expected IndexOutOfBoundsException");
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
        
        // extra cases for empty strings
        parent_list.add(1, DEV_EMPTY);
		assertEquals(-1, collection_list.childEndingIndex(1)); // EMPTY
        
        // extra cases for empty strings
        parent_list.add(5, DEV_EMPTY);
		assertEquals(-1, collection_list.childEndingIndex(5)); // EMPTY
	}


	/**
	 * Test modifying the list
	 */
	public void testModification() {
		// Remove an item  (tests Delete)
		parent_list.remove(DEV_ROB);
		checkBasicListData(collection_list,
			new String[]{DEV_JESSE, DEV_KEVIN, DEV_JAMES});

		// Put it on the end (test Insert)
		parent_list.add(DEV_ROB);
		checkBasicListData(collection_list,
			new String[]{DEV_JESSE, DEV_KEVIN, DEV_JAMES, DEV_ROB});

		// Replace it with something else (tests Update)
		parent_list.set(parent_list.size() - 1, "Nede Bor");
		checkBasicListData(collection_list,
			new String[]{DEV_JESSE, DEV_KEVIN, DEV_JAMES, "Nede Bor"});

		// Remove that (tests Delete)
		parent_list.remove(parent_list.size() - 1);
		checkBasicListData(collection_list,
			new String[]{DEV_JESSE, DEV_KEVIN, DEV_JAMES});

		// Add an empty item in the middle  (tests empty parents)
		parent_list.add(1, "");
		checkBasicListData(collection_list,
			new String[]{DEV_JESSE, "", DEV_KEVIN, DEV_JAMES});

		// Clear the parent (tests a bunch of Deletes and size 0)
		parent_list.clear();
		checkBasicListData(collection_list, new String[]{});

		// Put it all back to normal (tests a bunch on Inserts)
		parent_list.add(DEV_ROB);
		parent_list.add(DEV_JESSE);
		parent_list.add(DEV_KEVIN);
		parent_list.add(DEV_JAMES);
		checkBasicListData(collection_list,
			new String[]{DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES});
	}


	/**
	 * Check the basic data on a CollectionList to make sure it's showing the characters
	 * from the given Strings.
	 */
	private void checkBasicListData(CollectionList collection_list, String[] data) {
		// Make sure the size is the total of the lengths of the strings
		int expected_length = 0;
		for (int i = 0; i < data.length; i++) {
			expected_length += data[ i ].length();
		}
		assertEquals(expected_length, collection_list.size());

		// Now make sure everything in the list is a Character
		Iterator it = collection_list.iterator();
		while (it.hasNext()) {
			Object obj = it.next();

			assertTrue(obj instanceof Character);
		}

		// Now make sure the characters are in the right place
		char[] expected_chars = new char[ expected_length ];
		int index = 0;
		for (int i = 0; i < data.length; i++) {
			String str = data[ i ];
			System.arraycopy(str.toCharArray(), 0, expected_chars, index,
				str.length());
			index += str.length();
		}

		it = collection_list.iterator();
		for (int i = 0; it.hasNext(); i++) {
			Character c = (Character) it.next();

			assertEquals(expected_chars[ i ], c.charValue());
		}
	}


	/**
	 * Model that breaks a String into a list of characters.
	 */
	private class StringDecomposerModel implements CollectionListModel {
		public List getChildren(Object parent) {
			String str = (String) parent;

			char[] chars = str.toCharArray();

			List list = new ArrayList(chars.length);
			for (int i = 0; i < chars.length; i++) {
				list.add(new Character(chars[ i ]));
			}
			return list;
		}
	}
}
