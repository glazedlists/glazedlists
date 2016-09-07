package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.DisposableMap;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

public class GroupingListMultiMapTest {

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testPut() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

    @Test
    public void testPutAll() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());
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

    @Test
    public void testKeySet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        assertTrue(eventMap.isEmpty());
        assertTrue(eventMap.keySet().isEmpty());
        assertFalse(eventMap.keySet().contains("J"));

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson Jodie Ashford Katie Jiries"));
        assertEquals(8, source.size());
        assertEquals(5, eventMap.size());
        assertEquals(5, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList("A", "J", "K", "L", "W")), eventMap.keySet());
        assertTrue(eventMap.keySet().contains("A"));
        assertTrue(eventMap.keySet().contains("J"));
        assertTrue(eventMap.keySet().contains("K"));
        assertTrue(eventMap.keySet().contains("L"));
        assertTrue(eventMap.keySet().contains("W"));

        assertTrue(eventMap.keySet().remove("W"));
        assertEquals(7, source.size());
        assertEquals(4, eventMap.size());
        assertEquals(4, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList("A", "J", "K", "L")), eventMap.keySet());

        assertTrue(eventMap.keySet().remove("J"));
        assertEquals(3, source.size());
        assertEquals(3, eventMap.size());
        assertEquals(3, eventMap.keySet().size());
        assertEquals(new HashSet<String>(Arrays.asList("A", "K", "L")), eventMap.keySet());

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

    @Test
    public void testKeySetIterator() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

    @Test
    public void testEntrySet() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

    @Test
    public void testEntrySetIterator() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

    @Test
    public void testMapEntrySetValue() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

        // the exact same List should be the value in the entry, but the contents of the List have changed - so check if the List is identical
        assertSame(oldEntryValue, entry.getValue());

        try {
            entry.setValue(GlazedListsTests.delimitedStringToList("Xavier"));
            fail("failed to receive an IllegalArgumentException for a value to Entry.setValue() that does not produce the correct Entry.getKey()");
        } catch (IllegalArgumentException e) {
            // expected since Xavier does not start with A
        }
    }

    @Test
    public void testMapEntryEquals() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());
        final Map<Comparable<String>, List<String>> eventMap2 = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

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

    @Test
    public void testWriteThroughValues() {
        final EventList<String> source = new BasicEventList<String>();
        final Map<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        source.addAll(GlazedListsTests.delimitedStringToList("James Lemieux Jesse Wilson"));

        List<String> jNames = eventMap.get("J");
        runListMutationTest(jNames, "J");
        runListMutationTest(jNames.subList(2, 4), "J");

        runListIteratorMutationTest(jNames.listIterator(), "J");
        runListIteratorMutationTest(jNames.listIterator(3), "J");
    }

    @Test
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

    @Test
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

    @Test
    public void testExplicitComparatorForUncomparableValues() {
        final EventList<UncomparableValue> source = new BasicEventList<UncomparableValue>();
        final FunctionList.Function<UncomparableValue, UncomparableKey> keyFunction = GlazedLists.beanFunction(UncomparableValue.class, "key");
        final Map<UncomparableKey, List<UncomparableValue>> eventMap = GlazedLists.syncEventListToMultiMap(source, keyFunction, new UncomparableThingComparator());

        final UncomparableKey key1 = new UncomparableKey("a");
        final UncomparableKey key2 = new UncomparableKey("b");
        final UncomparableKey key3 = new UncomparableKey("a");

        final UncomparableValue value1 = new UncomparableValue(key1);
        final UncomparableValue value2 = new UncomparableValue(key2);
        final UncomparableValue value3 = new UncomparableValue(key3);

        source.add(value1);
        assertEquals(Collections.singletonList(value1), eventMap.get(key1));
        assertNull(eventMap.get(key2));
        assertEquals(Collections.singletonList(value1), eventMap.get(key3));

        source.add(value2);
        assertEquals(Collections.singletonList(value1), eventMap.get(key1));
        assertEquals(Collections.singletonList(value2), eventMap.get(key2));
        assertEquals(Collections.singletonList(value1), eventMap.get(key3));

        source.add(value3);
        assertEquals(Arrays.asList(value1, value3), eventMap.get(key1));
        assertEquals(Collections.singletonList(value2), eventMap.get(key2));
        assertEquals(Arrays.asList(value1, value3), eventMap.get(key3));
    }

    @Test
    public void testExplicitComparatorForUncomparableValues2() {
        final EventList<ComparableValue> source = new BasicEventList<ComparableValue>();

        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists guru"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "GlazedLists creator"));
        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists founder?"));
        source.add(new ComparableValue(new ComparableKey("Andy"), "Lowly patcher"));
        source.add(new ComparableValue(new ComparableKey("Fred"), "Flintstone"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "Coder"));

        final Map<AFirstLetterComparable,List<ComparableValue>> naturalMap = GlazedLists.syncEventListToMultiMap(source, new BAFirstLetterFunction());
        final Set<AFirstLetterComparable> expectedKeys = new LinkedHashSet<AFirstLetterComparable>();
        expectedKeys.add(new AFirstLetterComparable(new ComparableKey("James")));
        expectedKeys.add(new AFirstLetterComparable(new ComparableKey("Jesse")));
        expectedKeys.add(new AFirstLetterComparable(new ComparableKey("Andy")));
        expectedKeys.add(new AFirstLetterComparable(new ComparableKey("Fred")));

        assertEquals(3, naturalMap.size());
        assertEquals(expectedKeys, naturalMap.keySet());
        assertEquals(naturalMap.get(new AFirstLetterComparable(new ComparableKey("James"))), Arrays.asList(source.get(0), source.get(1), source.get(2), source.get(5)));
        assertEquals(naturalMap.get(new AFirstLetterComparable(new ComparableKey("Andy"))), Collections.singletonList(source.get(3)));
        assertEquals(naturalMap.get(new AFirstLetterComparable(new ComparableKey("Fred"))), Collections.singletonList(source.get(4)));
    }

    @Test
    public void testImplicitComparatorForComparableValues() {
        final EventList<ComparableValue> source = new BasicEventList<ComparableValue>();

        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists guru"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "GlazedLists creator"));
        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists founder?"));
        source.add(new ComparableValue(new ComparableKey("Andy"), "Lowly patcher"));
        source.add(new ComparableValue(new ComparableKey("Fred"), "Flintstone"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "Coder"));

        FunctionList.Function<ComparableValue, ComparableKey> keyMaker = GlazedLists.beanFunction(ComparableValue.class, "key");
        final Map<ComparableKey,List<ComparableValue>> naturalMap = GlazedLists.syncEventListToMultiMap(source, keyMaker);
        final Set<ComparableKey> expectedKeys = new LinkedHashSet<ComparableKey>();
        expectedKeys.add(new ComparableKey("James"));
        expectedKeys.add(new ComparableKey("Jesse"));
        expectedKeys.add(new ComparableKey("Andy"));
        expectedKeys.add(new ComparableKey("Fred"));

        assertEquals(4, naturalMap.size());
        assertEquals(expectedKeys, naturalMap.keySet());
        assertEquals(naturalMap.get(new ComparableKey("James")), Arrays.asList(source.get(0), source.get(2)));
        assertEquals(naturalMap.get(new ComparableKey("Jesse")), Arrays.asList(source.get(1), source.get(5)));
        assertEquals(naturalMap.get(new ComparableKey("Andy")), Collections.singletonList(source.get(3)));
        assertEquals(naturalMap.get(new ComparableKey("Fred")), Collections.singletonList(source.get(4)));
    }

    @Test
    public void testUnnaturalStringKeyGeneric() {
        final EventList<ComparableValue> source = new BasicEventList<ComparableValue>();

        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists guru"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "GlazedLists creator"));
        source.add(new ComparableValue(new ComparableKey("James"), "GlazedLists founder?"));
        source.add(new ComparableValue(new ComparableKey("Andy"), "Lowly patcher"));
        source.add(new ComparableValue(new ComparableKey("Fred"), "Flintstone"));
        source.add(new ComparableValue(new ComparableKey("Jesse"), "Coder"));

        final Map<String,List<ComparableValue>> naturalMap = GlazedLists.syncEventListToMultiMap(source, new BAFirstLetterStringFunction());
        final Set<String> expectedKeys = new LinkedHashSet<String>();
        expectedKeys.add("J");
        expectedKeys.add("A");
        expectedKeys.add("F");

        assertEquals(3, naturalMap.size());
        assertEquals(expectedKeys, naturalMap.keySet());
        assertEquals(naturalMap.get("J"), Arrays.asList(source.get(0), source.get(1), source.get(2), source.get(5)));
        assertEquals(naturalMap.get("A"), Collections.singletonList(source.get(3)));
        assertEquals(naturalMap.get("F"), Collections.singletonList(source.get(4)));
    }

    @Test
    public void testDispose() {
        final EventList<String> source = new BasicEventList<String>();
        final DisposableMap<Comparable<String>, List<String>> eventMap = GlazedLists.syncEventListToMultiMap(source, new FirstLetterFunction());

        // insert some data
        source.addAll(GlazedListsTests.delimitedStringToList("Jesse Jiries"));
        source.addAll(GlazedListsTests.delimitedStringToList("Katie"));
        source.addAll(GlazedListsTests.delimitedStringToList("Wilson"));

        assertEquals(3, eventMap.size());
        eventMap.dispose();
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
            names.addAll(Arrays.asList("****"));
            fail("failed to receive IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            names.addAll(0, Arrays.asList("****"));
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
        names.addAll(Arrays.asList(key + "****"));
        names.addAll(0, Arrays.asList(key + "****"));
        names.set(0, key + "****");
    }

    private static final class FirstLetterFunction implements FunctionList.Function<String,Comparable<String>> {
        @Override
        public String evaluate(String sourceValue) {
            return String.valueOf(sourceValue.charAt(0));
        }
    }

    public static final class ComparableKey implements Comparable<ComparableKey> {
        private final String name;

        public ComparableKey(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public int compareTo(ComparableKey o) {
            return name.compareTo(o.name);
        }

        @Override
        public String toString() {
            return "A{" + name + "}";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ComparableKey a = (ComparableKey) o;
            return name != null ? name.equals(a.name) : a.name == null;
        }

        @Override
        public int hashCode() {
            return (name != null ? name.hashCode() : 0);
        }
    }

    public static final class ComparableValue {
        private final ComparableKey a;
        private final String note;

        public ComparableValue(ComparableKey a, String note) {
            this.a = a;
            this.note = note;
        }

        public ComparableKey getKey() {
            return a;
        }

        public String getNote() {
            return note;
        }

        @Override
        public String toString() {
            return "B{a=" + a + ", note='" + note + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComparableValue b = (ComparableValue) o;

            if (a != null ? !a.equals(b.a) : b.a != null) return false;
            if (note != null ? !note.equals(b.note) : b.note != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            result = (a != null ? a.hashCode() : 0);
            result = 31 * result + (note != null ? note.hashCode() : 0);
            return result;
        }
    }

    private static final class BAFirstLetterFunction implements FunctionList.Function<ComparableValue,AFirstLetterComparable> {
        @Override
        public AFirstLetterComparable evaluate(ComparableValue sourceValue) {
            return new AFirstLetterComparable(sourceValue.getKey());
        }
    }


    private static final class BAFirstLetterStringFunction implements FunctionList.Function<ComparableValue,String> {
        @Override
        public String evaluate(ComparableValue sourceValue) {
            return sourceValue.getKey().getName().substring(0, 1);
        }
    }

    private static final class AFirstLetterComparable implements Comparable<AFirstLetterComparable> {
        private final ComparableKey contained;

        public AFirstLetterComparable(final ComparableKey contained) {
            this.contained = contained;
        }

        @Override
        public int compareTo(final AFirstLetterComparable o) {
            return new Character(contained.getName().charAt(0)).compareTo(new Character(o.contained.getName().charAt(0)));
        }

        @Override
        public String toString() {
            return "AFLC{" + contained.getName().charAt(0) + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AFirstLetterComparable that = (AFirstLetterComparable) o;
            return contained.getName().charAt(0) == that.contained.getName().charAt(0);
        }

        @Override
        public int hashCode() {
            return new Character(contained.getName().charAt(0)).hashCode();
        }
    }

    public static class UncomparableValue {
        private final UncomparableKey key;

        public UncomparableValue(UncomparableKey key) {
            this.key = key;
        }

        public UncomparableKey getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UncomparableValue that = (UncomparableValue) o;

            if (!key.equals(that.key)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    public static class UncomparableKey {
        private final String name;

        public UncomparableKey(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UncomparableKey that = (UncomparableKey) o;

            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    private static class UncomparableThingComparator implements Comparator<UncomparableKey> {
        @Override
        public int compare(UncomparableKey o1, UncomparableKey o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
