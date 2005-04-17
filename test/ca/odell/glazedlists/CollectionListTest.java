/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
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
		assertEquivalent(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collection_list);
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
		assertEquivalent(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collection_list);

		// Put it on the end (test Insert)
		parent_list.add(DEV_ROB);
		assertEquivalent(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, DEV_ROB }, collection_list);

		// Replace it with something else (tests Update)
		parent_list.set(parent_list.size() - 1, "Nede Bor");
		assertEquivalent(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES, "Nede Bor" }, collection_list);

		// Remove that (tests Delete)
		parent_list.remove(parent_list.size() - 1);
		assertEquivalent(new String[]{ DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collection_list);

		// Add an empty item in the middle  (tests empty parents)
		parent_list.add(1, "");
		assertEquivalent(new String[]{ DEV_JESSE, "", DEV_KEVIN, DEV_JAMES }, collection_list);

		// Clear the parent (tests a bunch of Deletes and size 0)
		parent_list.clear();
		assertEquivalent(new String[]{ }, collection_list);

		// Put it all back to normal (tests a bunch on Inserts)
		parent_list.add(DEV_ROB);
		parent_list.add(DEV_JESSE);
		parent_list.add(DEV_KEVIN);
		parent_list.add(DEV_JAMES);
		assertEquivalent(new String[]{ DEV_ROB, DEV_JESSE, DEV_KEVIN, DEV_JAMES }, collection_list);
	}

	/**
	 * Test modifying the children.
	 */
	public void testChildModification() {
        // use a list of Lists instead of Strings
		parent_list = new BasicEventList();
        collection_list = new CollectionList(parent_list, new ListCollectionListModel());
        collection_list.addListEventListener(new ConsistencyTestList(collection_list, "collection list", false));
        parent_list.add(stringToList(DEV_ROB));
        parent_list.add(stringToList(DEV_JESSE));
        parent_list.add(stringToList(DEV_KEVIN));
        parent_list.add(stringToList(DEV_JAMES));
        
        StringBuffer DEV_ROB_MODIFIED = new StringBuffer();
        DEV_ROB_MODIFIED.append(DEV_ROB);
        StringBuffer DEV_KEVIN_MODIFIED = new StringBuffer();
        DEV_KEVIN_MODIFIED.append(DEV_KEVIN);

        // they should match out-of-the-gate
		assertEquivalent(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, collection_list);
        
        // remove a single element
        DEV_ROB_MODIFIED.deleteCharAt(2);
        collection_list.remove(2);
		assertEquivalent(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, collection_list);

        // set a few elements
        DEV_KEVIN_MODIFIED.setCharAt(2, 'r');
        DEV_KEVIN_MODIFIED.setCharAt(1, 'a');
        DEV_KEVIN_MODIFIED.setCharAt(3, 'e');
        collection_list.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 2, new Character('r'));
        collection_list.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 1, new Character('a'));
        collection_list.set(DEV_ROB_MODIFIED.length() + DEV_JESSE.length() + 3, new Character('e'));
		assertEquivalent(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, collection_list);

        // set a few more elements
        DEV_ROB_MODIFIED.setCharAt(1, '.');
        collection_list.set(1, new Character('.'));
		assertEquivalent(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, collection_list);

        // remove a few more elements
        DEV_ROB_MODIFIED.deleteCharAt(1);
        DEV_ROB_MODIFIED.deleteCharAt(1);
        collection_list.remove(1);
        collection_list.remove(1);
		assertEquivalent(new CharSequence[]{ DEV_ROB_MODIFIED, DEV_JESSE, DEV_KEVIN_MODIFIED, DEV_JAMES }, collection_list);
	}
    
	/**
	 * Check the basic data on a CollectionList to make sure it's showing the characters
	 * from the given Strings.
	 */
	private void assertEquivalent(CharSequence[] data, CollectionList collection_list) {
        assertEquals(stringsToList(data), collection_list);
    }

	/**
	 * Model that breaks a String into a list of characters.
	 */
	private class StringDecomposerModel implements CollectionListModel {
		public List getChildren(Object parent) {
			return stringToList((String)parent);
        }
	}
    /**
     * Returns the List itself for a List of Lists.
     */
    private static class ListCollectionListModel implements CollectionListModel {
        public List getChildren(Object parent) {
            return (List)parent;
        }
    }

    /**
     * Convert the characters of the specified String to a list.
     */
    private static List stringToList(CharSequence chars) {
        List result = new ArrayList(chars.length());
        for (int i = 0; i < chars.length(); i++) {
            result.add(new Character(chars.charAt(i)));
        }
        return result;
    }
    private static List stringsToList(CharSequence[] data) {
        List result = new ArrayList();
        for(int s = 0; s < data.length; s++) {
            result.addAll(stringToList(data[s]));
        }
        return result;
    }
}
