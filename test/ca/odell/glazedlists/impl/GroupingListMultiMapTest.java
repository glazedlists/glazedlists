package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import junit.framework.TestCase;

import java.util.*;

public class GroupingListMultiMapTest extends TestCase {

    public void testConstructor() {
        // 1. test constructor with filled source list
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson James Lemieux Katie Jiries"));
        Map<? extends Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        assertEquals(4, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James Jiries"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Katie"), eventMap.get("K"));
        assertEquals(GlazedListsTests.delimitedStringToList("Lemieux"), eventMap.get("L"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        // 2. test constructor with empty source list filled after construction
        source = new BasicEventList<String>();
        eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());
        assertEquals(0, eventMap.size());

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson James Lemieux Katie Jiries"));

        assertEquals(4, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James Jiries"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Katie"), eventMap.get("K"));
        assertEquals(GlazedListsTests.delimitedStringToList("Lemieux"), eventMap.get("L"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        // 3. test constructor with null source list
        try {
            GlazedLists.syncEventListToMultiMap(null, new FirstLetterFunction());
            fail("Failed to received NullPointerException on null source list");
        } catch (NullPointerException npe) {
            // expected
        }

        // 4. test constructor with null key function
        try {
            GlazedLists.syncEventListToMultiMap(source, null);
            fail("Failed to received IllegalArgumentException on null key function");
        } catch (IllegalArgumentException npe) {
            // expected
        }
    }

    public void testGet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<? extends Comparable, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        source.add("James");
        assertEquals(1, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James"), eventMap.get("J"));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Lemieux"), eventMap.get("L"));
        assertEquals(null, eventMap.get("W"));

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson"));
        assertEquals(3, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Lemieux"), eventMap.get("L"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        source.remove("Lemieux");
        assertEquals(2, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse"), eventMap.get("J"));
        assertEquals(null, eventMap.get("L"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse"), eventMap.get("J"));
        assertEquals(null, eventMap.get("L"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        source.clear();
        assertEquals(0, eventMap.size());
        assertEquals(null, eventMap.get("J"));
        assertTrue(eventMap.isEmpty());
    }

    public void testContainsKey() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<? extends Comparable, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        source.add("James");
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsKey("J"));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertTrue(eventMap.containsKey("L"));
        assertFalse(eventMap.containsKey("W"));

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson"));
        assertEquals(3, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertTrue(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("W"));

        source.remove("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("W"));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertTrue(eventMap.containsKey("W"));

        source.clear();
        assertEquals(0, eventMap.size());
        assertFalse(eventMap.containsKey("J"));
        assertFalse(eventMap.containsKey("L"));
        assertFalse(eventMap.containsKey("W"));
        assertTrue(eventMap.isEmpty());
    }

    public void testContainsValue() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<? extends Comparable, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        assertEquals(null, eventMap.get("J"));

        source.add("James");
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("James")));

        source.add("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("James")));
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Wilson")));

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson"));
        assertEquals(3, eventMap.size());
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("James Jesse")));
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Wilson")));

        source.remove("Lemieux");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("James Jesse")));
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Wilson")));

        source.remove("James");
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Jesse")));
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertTrue(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Wilson")));

        source.clear();
        assertEquals(0, eventMap.size());
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Jesse")));
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertFalse(eventMap.containsValue(GlazedListsTests.delimitedStringToList("Wilson")));
        assertTrue(eventMap.isEmpty());
    }

    public void testRemove() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<? extends Comparable, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        assertNull(eventMap.remove("W"));

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson James Lemieux"));
        assertEquals(3, eventMap.size());

        eventMap.remove("L");
        assertEquals(2, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        eventMap.remove("J");
        assertEquals(1, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        eventMap.remove("W");
        assertEquals(0, eventMap.size());
        assertTrue(eventMap.isEmpty());

        assertNull(eventMap.remove("J"));
        assertNull(eventMap.remove("L"));
        assertNull(eventMap.remove("W"));
    }

    public void testValues() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<? extends Comparable, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        assertTrue(eventMap.values().isEmpty());

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Wilson James Lemieux"));
        assertEquals(3, eventMap.values().size());
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Jesse James")));
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Wilson")));

        // add through the values()
        eventMap.values().add(GlazedListsTests.delimitedStringToList("Katie"));
        eventMap.values().add(GlazedListsTests.delimitedStringToList("Jiries"));
        assertEquals(4, eventMap.size());
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Jesse James Jiries")));
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Katie")));
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Lemieux")));
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Wilson")));

        // remove through the values()
        eventMap.values().remove(GlazedListsTests.delimitedStringToList("Jesse James Jiries"));
        eventMap.values().remove(GlazedListsTests.delimitedStringToList("Katie"));
        eventMap.values().remove(GlazedListsTests.delimitedStringToList("Lemieux"));
        assertEquals(1, eventMap.size());
        assertTrue(eventMap.values().contains(GlazedListsTests.delimitedStringToList("Wilson")));
    }

    public void testPut() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        assertTrue(eventMap.values().isEmpty());

        // try inserting a single element list
        assertEquals(null, eventMap.put("J", GlazedListsTests.delimitedStringToList("James")));
        assertEquals(1, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James"), eventMap.get("J"));

        // try inserting a multi element list
        assertEquals(GlazedListsTests.delimitedStringToList("James"), eventMap.put("J", GlazedListsTests.delimitedStringToList("Jesse Jiries")));
        assertEquals(1, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse Jiries"), eventMap.get("J"));

        // try inserting an empty list
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse Jiries"), eventMap.put("J", new BasicEventList<String>()));
        assertTrue(eventMap.isEmpty());

        // try inserting a bad list
        try {
            eventMap.put("Z", GlazedListsTests.delimitedStringToList("Ashford"));
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }

        // try inserting a partially bad list
        try {
            eventMap.put("Z", GlazedListsTests.delimitedStringToList("Zorro Ashford"));
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    public void testPutAll() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);
        Map<Comparable<String>, List<String>> values = new HashMap<Comparable<String>, List<String>>();

        assertTrue(eventMap.values().isEmpty());

        // try inserting a single element list
        values.put("J", GlazedListsTests.delimitedStringToList("James"));
        eventMap.putAll(values);
        assertEquals(1, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("James"), eventMap.get("J"));

        // try inserting a multi element list
        values.clear();
        values.put("J", GlazedListsTests.delimitedStringToList("Jesse Jiries"));
        values.put("K", GlazedListsTests.delimitedStringToList("Katie"));
        values.put("W", GlazedListsTests.delimitedStringToList("Wilson"));
        eventMap.putAll(values);
        assertEquals(3, eventMap.size());
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse Jiries"), eventMap.get("J"));
        assertEquals(GlazedListsTests.delimitedStringToList("Katie"), eventMap.get("K"));
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), eventMap.get("W"));

        // try inserting a bad list
        try {
            values.clear();
            values.put("Z", GlazedListsTests.delimitedStringToList("Ashford"));
            eventMap.putAll(values);
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }

        // try inserting a partially bad list
        try {
            values.clear();
            values.put("Z", GlazedListsTests.delimitedStringToList("Zorro Ashford"));
            eventMap.putAll(values);
            fail("failed to receive IllegalArgumentException when using a key and value that do not agree");
        } catch (IllegalArgumentException iae) {
            // expected
        }
    }

    public void testKeySet() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.keySet().isEmpty());
        assertFalse(eventMap.keySet().contains("J"));

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));
        assertEquals(8, source.size());
        assertEquals(5, eventMap.size());
        assertEquals(5, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"A", "J", "K", "L", "W"})), eventMap.keySet());
        assertTrue(eventMap.keySet().contains("A"));
        assertTrue(eventMap.keySet().contains("J"));
        assertTrue(eventMap.keySet().contains("K"));
        assertTrue(eventMap.keySet().contains("L"));
        assertTrue(eventMap.keySet().contains("W"));

        assertTrue(eventMap.keySet().remove("W"));
        assertEquals(7, source.size());
        assertEquals(4, eventMap.size());
        assertEquals(4, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"A", "J", "K", "L"})), eventMap.keySet());

        assertTrue(eventMap.keySet().remove("J"));
        assertEquals(3, source.size());
        assertEquals(3, eventMap.size());
        assertEquals(3, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList(new String[] {"A", "K", "L"})), eventMap.keySet());

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
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));

        Iterator<Comparable<String>> keySetIter = eventMap.keySet().iterator();

        assertTrue(keySetIter.hasNext());
        assertEquals("A", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("J", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("K", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("L", keySetIter.next());
        assertTrue(keySetIter.hasNext());
        assertEquals("W", keySetIter.next());
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

        assertEquals(5, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("A", keySetIter.next());
        assertNotNull(eventMap.get("A"));
        keySetIter.remove();
        assertNull(eventMap.get("A"));

        assertEquals(4, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("J", keySetIter.next());
        assertNotNull(eventMap.get("J"));
        keySetIter.remove();
        assertNull(eventMap.get("J"));

        assertEquals(3, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("K", keySetIter.next());
        assertNotNull(eventMap.get("K"));
        keySetIter.remove();
        assertNull(eventMap.get("K"));

        assertEquals(2, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("L", keySetIter.next());
        assertNotNull(eventMap.get("L"));
        keySetIter.remove();
        assertNull(eventMap.get("L"));

        assertEquals(1, eventMap.size());
        assertTrue(keySetIter.hasNext());
        assertEquals("W", keySetIter.next());
        assertNotNull(eventMap.get("W"));
        keySetIter.remove();
        assertNull(eventMap.get("W"));

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
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.entrySet().isEmpty());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));
        assertEquals(8, source.size());
        assertEquals(5, eventMap.size());
        assertEquals(5, eventMap.entrySet().size());

        assertFalse(eventMap.entrySet().contains(null));

        // remove Ashford
        assertTrue(eventMap.entrySet().remove(eventMap.entrySet().iterator().next()));
        assertEquals(7, source.size());
        assertEquals(4, eventMap.size());
        assertEquals(4, eventMap.entrySet().size());

        // remove James Jesse Jodie Jiries
        assertTrue(eventMap.entrySet().remove(eventMap.entrySet().iterator().next()));
        assertEquals(3, source.size());
        assertEquals(3, eventMap.size());
        assertEquals(3, eventMap.entrySet().size());

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
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));

        Iterator<Map.Entry<Comparable<String>, List<String>>> entrySetIter = eventMap.entrySet().iterator();
        Map.Entry<Comparable<String>, List<String>> entry;

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("A", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Ashford"), entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("J", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jodie Jiries"), entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("K", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Katie"), entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("L", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Lemieux"), entry.getValue());

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        assertEquals("W", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Wilson"), entry.getValue());
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

        assertEquals(5, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("A"));

        assertEquals(4, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("J"));

        assertEquals(3, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("K"));

        assertEquals(2, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("L"));

        assertEquals(1, eventMap.size());
        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertTrue(eventMap.entrySet().contains(entry));
        entrySetIter.remove();
        assertFalse(eventMap.entrySet().contains(entry));
        assertNull(eventMap.get("W"));

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
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));

        Iterator<Map.Entry<Comparable<String>, List<String>>> entrySetIter = eventMap.entrySet().iterator();
        Map.Entry<Comparable<String>, List<String>> entry;

        assertTrue(entrySetIter.hasNext());
        entry = entrySetIter.next();
        assertEquals("A", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Ashford"), entry.getValue());

        final List<String> oldEntryValue = entry.getValue();

        entry.setValue(GlazedListsTests.delimitedStringToList("Angela Applegate"));
        assertEquals("A", entry.getKey());
        assertEquals(GlazedListsTests.delimitedStringToList("Angela Applegate"), entry.getValue());
        assertSame(oldEntryValue, entry.getValue());

        try {
            entry.setValue(GlazedListsTests.delimitedStringToList("Xavier"));
            fail("failed to receive an IllegalArgumentException for a value to Entry.setValue() that does not produce the correct Entry.getKey()");
        } catch (IllegalArgumentException e) {
            // expected since Xavier does not start with A
        }
    }

    public void testMapEntryEquals() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);
        final Map<Comparable<String>, List<String>> eventMap2 = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));

        Iterator<Map.Entry<Comparable<String>, List<String>>> iterator = eventMap2.entrySet().iterator();

        Map.Entry<Comparable<String>, List<String>> entry = eventMap.entrySet().iterator().next();
        Map.Entry<Comparable<String>, List<String>> entry2 = iterator.next();

        assertNotSame(entry, entry2);
        assertNotSame(entry.getKey(), entry2.getKey());
        assertNotSame(entry.getValue(), entry2.getValue());
        assertEquals(entry, entry2);

        Map.Entry<Comparable<String>, List<String>> entry2Next = iterator.next();

        assertNotSame(entry, entry2Next);
        assertNotSame(entry.getKey(), entry2Next.getKey());
        assertNotSame(entry.getValue(), entry2Next.getValue());
        assertFalse(entry.equals(entry2Next));
    }

    public void testWriteThroughValues() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson"));

        List<String> jNames = eventMap.get("J");
        runListMutationTest(jNames, "J");
        runListMutationTest(jNames.subList(2, 4), "J");

        runListIteratorMutationTest(jNames.listIterator(), "J");
        runListIteratorMutationTest(jNames.listIterator(3), "J");
    }

    public void testWriteThroughValues2() {
        final EventList<String> source = new BasicEventList<String>();
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        source.addAll(GlazedListsTests.delimitedStringToList("James Jesse"));

        List<String> jNames = eventMap.get("J");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse"), jNames);
        jNames.add("Jordan");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jordan"), jNames);
        jNames.add(2, "Jordache");
        assertEquals(GlazedListsTests.delimitedStringToList("James Jesse Jordache Jordan"), jNames);
    }

    public void testWriteThroughValues3() {
        final EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson"));
        FirstLetterFunction f = new FirstLetterFunction();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, f);

        List<String> jNames = eventMap.get("J");
        runListMutationTest(jNames, "J");
        runListMutationTest(jNames.subList(2, 4), "J");

        runListIteratorMutationTest(jNames.listIterator(), "J");
        runListIteratorMutationTest(jNames.listIterator(3), "J");
    }

    private void runListIteratorMutationTest(ListIterator<String> listIterator, String key) {
        listIterator.next();

        try {
            listIterator.set("****");
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            listIterator.add("****");
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        listIterator.set(key + "****");
        listIterator.add(key + "****");
    }

    private void runListMutationTest(List<String> names, String key) {
        try {
            names.add("****");
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            names.add(0, "****");
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            names.addAll(Arrays.asList(new String[] {"****"}));
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            names.addAll(0, Arrays.asList(new String[] {"****"}));
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            names.set(0, "****");
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        names.add(key + "****");
        names.add(0, key + "****");
        names.addAll(Arrays.asList(new String[] {key + "****"}));
        names.addAll(0, Arrays.asList(new String[] {key + "****"}));
        names.set(0, key + "****");
    }

    private static final class FirstLetterFunction implements FunctionList.Function<String,Comparable<String>> {
        public String evaluate(String sourceValue) {
            return String.valueOf(sourceValue.charAt(0));
        }
    }
}