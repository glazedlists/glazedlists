/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import junit.framework.TestCase;
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.*;

public class FunctionListMapTest extends TestCase {

    public void testConstructor() {
        // 1. test constructor with filled source list
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("Wilson Lemieux Jiries Ashford"));
        Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertEquals(4, eventMap.size());
        assertEquals("Wilson", eventMap.get("W"));
        assertEquals("Lemieux", eventMap.get("L"));
        assertEquals("Jiries", eventMap.get("J"));
        assertEquals("Ashford", eventMap.get("A"));

        // 2. test constructor with empty source list filled after construction
        source = new BasicEventList<String>();
        eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());
        assertEquals(0, eventMap.size());

        source.addAll(GlazedListsTests.delimitedStringToList("Wilson Lemieux Jiries Ashford"));

        assertEquals(4, eventMap.size());
        assertEquals("Wilson", eventMap.get("W"));
        assertEquals("Lemieux", eventMap.get("L"));
        assertEquals("Jiries", eventMap.get("J"));
        assertEquals("Ashford", eventMap.get("A"));

        // 3. test constructor with null source list
        try {
            GlazedLists.syncEventListToMap(null, new FirstLetterFunction());
            fail("Failed to received NullPointerException on null source list");
        } catch (NullPointerException npe) {
            // expected
        }

        // 4. test constructor with null key function
        try {
            GlazedLists.syncEventListToMap(source, null);
            fail("Failed to received IllegalArgumentException on null key function");
        } catch (IllegalArgumentException npe) {
            // expected
        }
    }

    public void testGet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.add("James");
        assertEquals(1, eventMap.size());
        assertEquals("James", eventMap.get("J"));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertEquals("James", eventMap.get("J"));
        assertEquals("Lemieux", eventMap.get("L"));
        assertEquals(null, eventMap.get("W"));

        source.addAll(GlazedListsTests.delimitedStringToList("Holger Brands"));
        assertEquals(4, eventMap.size());
        assertEquals("James", eventMap.get("J"));
        assertEquals("Lemieux", eventMap.get("L"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        source.remove("Lemieux");
        assertEquals(3, eventMap.size());
        assertEquals("James", eventMap.get("J"));
        assertEquals(null, eventMap.get("L"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertEquals(null, eventMap.get("J"));
        assertEquals(null, eventMap.get("L"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        source.set(0, "Andy");
        assertEquals("Andy", eventMap.get("A"));
        assertEquals(null, eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        source.clear();
        assertEquals(0, eventMap.size());
        assertEquals(null, eventMap.get("J"));
        assertEquals(null, eventMap.get("L"));
        assertEquals(null, eventMap.get("H"));
        assertEquals(null, eventMap.get("B"));
        assertTrue(eventMap.isEmpty());

        try {
            source.addAll(GlazedListsTests.delimitedStringToList("Bobby Boucher"));
            fail("Failed to receive an IllegalStateException when violating the single key uniqueness constraint");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testContainsKey() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.add("James");
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsKey("J"));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertTrue(eventMap.containsKey("L"));
        assertFalse(eventMap.containsKey("H"));

        source.addAll(GlazedListsTests.delimitedStringToList("Holger Brands"));
        assertEquals(4, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertTrue(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("H"));
        assertTrue(eventMap.containsKey("B"));

        source.remove("Lemieux");
        assertEquals(3, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("H"));
        assertTrue(eventMap.containsKey("B"));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertFalse(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("H"));
        assertTrue(eventMap.containsKey("B"));

        source.clear();
        assertEquals(0, eventMap.size());
        assertFalse(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertFalse(eventMap.containsKey("H"));
        assertFalse(eventMap.containsKey("B"));
        assertTrue(eventMap.isEmpty());
    }

    public void testContainsValue() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertEquals(null, eventMap.get("J"));

        source.add("James");
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsValue("James"));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsValue("James"));
        assertTrue(eventMap.containsValue("Lemieux"));
        assertFalse(eventMap.containsValue("Holger"));

        source.addAll(GlazedListsTests.delimitedStringToList("Holger Brands"));
        assertEquals(4, eventMap.size());
        assertTrue(eventMap.containsValue("James"));
        assertTrue(eventMap.containsValue("Lemieux"));
        assertTrue(eventMap.containsValue("Holger"));
        assertTrue(eventMap.containsValue("Brands"));

        source.remove("Lemieux");
        assertEquals(3, eventMap.size());
        assertTrue(eventMap.containsValue("James"));
        assertFalse(eventMap.containsValue("Lemieux"));
        assertTrue(eventMap.containsValue("Holger"));
        assertTrue(eventMap.containsValue("Brands"));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertFalse(eventMap.containsValue("James"));
        assertFalse(eventMap.containsValue("Lemieux"));
        assertTrue(eventMap.containsValue("Holger"));
        assertTrue(eventMap.containsValue("Brands"));

        source.clear();
        assertEquals(0, eventMap.size());
        assertFalse(eventMap.containsValue("James"));
        assertFalse(eventMap.containsValue("Lemieux"));
        assertFalse(eventMap.containsValue("Holger"));
        assertFalse(eventMap.containsValue("Brands"));
        assertTrue(eventMap.isEmpty());
    }

    public void testRemove() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertNull(eventMap.remove("W"));

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson Holger Brands"));
        assertEquals(4, eventMap.size());

        eventMap.remove("W");
        assertEquals(3, eventMap.size());
        assertEquals("Jesse", eventMap.get("J"));
        assertEquals(null, eventMap.get("W"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        eventMap.remove("J");
        assertEquals(2, eventMap.size());
        assertEquals(null, eventMap.get("J"));
        assertEquals(null, eventMap.get("W"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        eventMap.remove("H");
        eventMap.remove("B");
        assertEquals(0, eventMap.size());
        assertTrue(eventMap.isEmpty());

        assertNull(eventMap.remove("J"));
        assertNull(eventMap.remove("W"));
        assertNull(eventMap.remove("H"));
        assertNull(eventMap.remove("B"));
    }

    public void testValues() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertTrue(eventMap.values().isEmpty());

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson Holger Brands"));
        assertEquals(4, eventMap.values().size());
        assertEquals("Jesse", eventMap.get("J"));
        assertEquals("Wilson", eventMap.get("W"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));

        // add through the values()
        eventMap.values().add("Andy");
        eventMap.values().add("Depue");
        assertEquals(6, eventMap.size());
        assertEquals("Jesse", eventMap.get("J"));
        assertEquals("Wilson", eventMap.get("W"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));
        assertEquals("Andy", eventMap.get("A"));
        assertEquals("Depue", eventMap.get("D"));

        // remove through the values()
        eventMap.values().remove("Jesse");
        eventMap.values().remove("Wilson");
        assertEquals(4, eventMap.size());
        assertEquals(null, eventMap.get("J"));
        assertEquals(null, eventMap.get("W"));
        assertEquals("Holger", eventMap.get("H"));
        assertEquals("Brands", eventMap.get("B"));
        assertEquals("Andy", eventMap.get("A"));
        assertEquals("Depue", eventMap.get("D"));

        assertTrue(eventMap.values().contains("Holger"));
        assertTrue(eventMap.values().contains("Brands"));
        assertTrue(eventMap.values().contains("Andy"));
        assertTrue(eventMap.values().contains("Depue"));
    }

    public void testPut() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertTrue(eventMap.values().isEmpty());

        // try inserting a single element
        assertEquals(null, eventMap.put("J", "James"));
        assertEquals(1, eventMap.size());
        assertEquals("James", eventMap.get("J"));

        // try inserting an element overtop
        assertEquals("James", eventMap.put("J", "Jesse"));
        assertEquals(1, eventMap.size());
        assertEquals("Jesse", eventMap.get("J"));

        // try inserting another value
        assertEquals(null, eventMap.put("K", "Katie"));
        assertEquals(2, eventMap.size());
        assertEquals("Jesse", eventMap.get("J"));
        assertEquals("Katie", eventMap.get("K"));

        // try inserting a bad key/value pair
        try {
            eventMap.put("Z", "Ashford");
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    public void testPutAll() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());
        Map<String, String> values = new HashMap<String, String>();

        assertTrue(eventMap.values().isEmpty());

        // try inserting a single element
        values.put("J", "James");
        eventMap.putAll(values);
        assertEquals(1, eventMap.size());
        assertEquals("James", eventMap.get("J"));

        // try inserting multiple elements
        values.clear();
        values.put("J", "Jesse");
        values.put("K", "Katie");
        values.put("W", "Wilson");
        eventMap.putAll(values);
        assertEquals(3, eventMap.size());
        assertEquals("Jesse", eventMap.get("J"));
        assertEquals("Katie", eventMap.get("K"));
        assertEquals("Wilson", eventMap.get("W"));

        // try inserting a bad list
        try {
            values.clear();
            values.put("Z", "Ashford");
            eventMap.putAll(values);
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    public void testKeySet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.keySet().isEmpty());
        assertFalse(eventMap.keySet().contains("J"));

        source.addAll(GlazedListsTests.delimitedStringToList("James Kevin Andy Holger"));
        assertEquals(4, source.size());
        assertEquals(4, eventMap.size());
        assertEquals(4, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"J", "K", "A", "H"})), eventMap.keySet());
        assertTrue(eventMap.keySet().contains("J"));
        assertTrue(eventMap.keySet().contains("K"));
        assertTrue(eventMap.keySet().contains("A"));
        assertTrue(eventMap.keySet().contains("H"));

        assertTrue(eventMap.keySet().remove("J"));
        assertEquals(3, source.size());
        assertEquals(3, eventMap.size());
        assertEquals(3, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"K", "A", "H"})), eventMap.keySet());

        assertFalse(eventMap.keySet().remove("J"));
        assertEquals(3, source.size());
        assertEquals(3, eventMap.size());
        assertEquals(3, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"K", "A", "H"})), eventMap.keySet());

        try {
            eventMap.keySet().add("J");
            fail("failed to throw an UnsupportedOperationException! Implementing add() makes no sense!");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        assertFalse(eventMap.keySet().remove("J"));

        eventMap.keySet().clear();
        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.keySet().isEmpty());
        assertFalse(eventMap.keySet().contains("J"));
    }

    public void testKeySetIterator() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Andy Depue Holger Brands"));

        Iterator<String> keySetIter = eventMap.keySet().iterator();

        assertTrue(keySetIter.hasNext());
        assertEquals("J", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("L", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("A", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("D", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("H", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("B", keySetIter.next());
        assertFalse(keySetIter.hasNext());

        try {
            keySetIter.next();
            fail("failed to throw NoSuchElementException when iterating off the end of the keySet()");
        } catch (NoSuchElementException nse) {
            // expected
        }

        keySetIter = eventMap.keySet().iterator();
        try {
            keySetIter.remove();
            fail("failed to throw IllegalStateException when iterator positioned before the start of the keySet()");
        } catch (IllegalStateException ise) {
            // expected
        }

        assertEquals(6, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("J", keySetIter.next());
        assertNotNull(eventMap.get("J"));
        keySetIter.remove();
        assertNull(eventMap.get("J"));

        assertEquals(5, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("L", keySetIter.next());
        assertNotNull(eventMap.get("L"));
        keySetIter.remove();
        assertNull(eventMap.get("L"));

        assertEquals(4, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("A", keySetIter.next());
        assertNotNull(eventMap.get("A"));
        keySetIter.remove();
        assertNull(eventMap.get("A"));

        assertEquals(3, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("D", keySetIter.next());
        assertNotNull(eventMap.get("D"));
        keySetIter.remove();
        assertNull(eventMap.get("D"));

        assertEquals(2, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("H", keySetIter.next());
        assertNotNull(eventMap.get("H"));
        keySetIter.remove();
        assertNull(eventMap.get("H"));

        assertEquals(1, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("B", keySetIter.next());
        assertNotNull(eventMap.get("B"));
        keySetIter.remove();
        assertNull(eventMap.get("B"));

        assertEquals(0, eventMap.size());
        assertFalse(keySetIter.hasNext());
        try {
            keySetIter.remove();
            fail("failed to throw IllegalStateException when iterator positioned after the end of the keySet()");
        } catch (IllegalStateException ise) {
            // expected
        }
    }

    public void testEntrySet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.entrySet().isEmpty());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Andy Depue Holger Brands"));
        assertEquals(6, source.size());
        assertEquals(6, eventMap.size());
        assertEquals(6, eventMap.entrySet().size());

        assertFalse(eventMap.entrySet().contains(null));

        // remove James
        assertTrue(eventMap.entrySet().remove(eventMap.entrySet().iterator().next()));
        assertEquals(5, source.size());
        assertEquals(5, eventMap.size());
        assertEquals(5, eventMap.entrySet().size());

        // remove Lemieux
        assertTrue(eventMap.entrySet().remove(eventMap.entrySet().iterator().next()));
        assertEquals(4, source.size());
        assertEquals(4, eventMap.size());
        assertEquals(4, eventMap.entrySet().size());

        eventMap.entrySet().clear();

        assertEquals(0, source.size());
        assertEquals(0, eventMap.size());
        assertEquals(0, eventMap.entrySet().size());
        assertTrue(source.isEmpty());
        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.entrySet().isEmpty());
    }

    public void testEntrySetIterator() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Andy Depue Holger Brands"));

        Iterator<Map.Entry<String, String>> entrySetIter = eventMap.entrySet().iterator();
        Map.Entry<String, String> entry;

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("J", entry.getKey());
        assertEquals("James", entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("L", entry.getKey());
        assertEquals("Lemieux", entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("A", entry.getKey());
        assertEquals("Andy", entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("D", entry.getKey());
        assertEquals("Depue", entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("H", entry.getKey());
        assertEquals("Holger", entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("B", entry.getKey());
        assertEquals("Brands", entry.getValue());
        assertFalse(entrySetIter.hasNext());

        try {
            entrySetIter.next();
            fail("failed to throw NoSuchElementException when iterating off the end of the entrySet()");
        } catch (NoSuchElementException nse) {
            // expected
        }

        entrySetIter = eventMap.entrySet().iterator();
        try {
            entrySetIter.remove();
            fail("failed to throw IllegalStateException when iterator positioned before the start of the entrySet()");
        } catch (IllegalStateException ise) {
            // expected
        }

        assertEquals(6, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("J"));

        assertEquals(5, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("L"));

        assertEquals(4, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("A"));

        assertEquals(3, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("D"));

        assertEquals(2, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("H"));

        assertEquals(1, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("B"));

        assertEquals(0, eventMap.size());
        assertFalse(entrySetIter.hasNext());
        try {
            entrySetIter.remove();
            fail("failed to throw IllegalStateException when iterator positioned after the end of the keySet()");
        } catch (IllegalStateException ise) {
            // expected
        }
    }

    public void testMapEntrySetValue() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Andy Depue Holger Brands"));

        Iterator<Map.Entry<String, String>> entrySetIter = eventMap.entrySet().iterator();
        Map.Entry<String, String> entry;

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertEquals("J", entry.getKey());
        assertEquals("James", entry.getValue());

        final String oldEntryValue = entry.getValue();

        entry.setValue("Jesse");
        assertEquals("J", entry.getKey());
        assertEquals("Jesse", entry.getValue());

        try {
            entry.setValue("Xavier");
            fail("failed to receive an IllegalArgumentException for a value to Entry.setValue() that does not produce the correct Entry.getKey()");
        } catch (IllegalArgumentException e) {
            // expected since Xavier does not start with J
        }
    }

    public void testMapEntryEquals() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());
        final Map<String, String> eventMap2 = GlazedLists.syncEventListToMap(source, new FirstLetterFunction());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Andy Depue Holger Brands"));

        Iterator<Map.Entry<String, String>> iterator = eventMap2.entrySet().iterator();

        Map.Entry<String, String> entry = eventMap.entrySet().iterator().next();
        Map.Entry<String, String> entry2 = iterator.next();

        assertNotSame(entry, entry2);
        assertNotSame(entry.getKey(), entry2.getKey());
        assertSame(entry.getValue(), entry2.getValue());
        assertEquals(entry, entry2);

        Map.Entry<String, String> entry2Next = iterator.next();

        assertNotSame(entry, entry2Next);
        assertNotSame(entry.getKey(), entry2Next.getKey());
        assertNotSame(entry.getValue(), entry2Next.getValue());
        assertFalse(entry.equals(entry2Next));
    }

    /**
     * This testcase highlights a VERY specific use of the Map which used to
     * produce a NullPointerException. What's happening here is that:
     *
     * 1. A UniqueList is the source of data for the Map
     * 2. The UniqueList uses a Comparator that doesn't tolerate comparing null values.
     * 3. eventMap.remove("X") relies on UniqueList.indexOf() in its implementation.
     * 4. Since "X" is not a valid key in the Map, UniqueList.indexOf() ends up asking
     *    the Comparator to compare a *NULL* value with some other value from the UniqueList.
     * 5. A NullPointerException occurs because UniqueList's Comparator was not written
     *    to expect null values, and shouldn't have had to since no null value exists in
     *    UniqueList!
     */
    public void testRemoveOnMapBackedByUniqueList() {
        final EventList<String> source = new BasicEventList<String>();
        final EventList<String> unique = new UniqueList<String>(source, new FirstLetterComparator());
        final Map<String, String> eventMap = GlazedLists.syncEventListToMap(unique, new FirstLetterFunction());

        source.add("Bluto");
        source.add("Popeye");
        source.add("Olive");

        assertEquals(3, eventMap.size());
        assertTrue(eventMap.containsKey("B"));
        assertTrue(eventMap.containsKey("P"));
        assertTrue(eventMap.containsKey("O"));

        assertNull(eventMap.remove("X"));
    }

    private static final class FirstLetterComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.charAt(0) - o2.charAt(0);
        }
    }

    private static final class FirstLetterFunction implements FunctionList.Function<String, String> {
        public String evaluate(String sourceValue) {
            return String.valueOf(sourceValue.charAt(0));
        }
    }
}