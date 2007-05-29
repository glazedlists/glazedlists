package ca.odell.glazedlists.impl.adt;

import junit.framework.TestCase;

/**
 * @author jessewilson
 */
public abstract class AbstractKeyedCollectionTest extends TestCase {

    private NamePosition a = new NamePosition("A");
    private NamePosition b = new NamePosition("B");
    private NamePosition c = new NamePosition("C");
    private NamePosition d = new NamePosition("D");
    private NamePosition e = new NamePosition("E");
    private NamePosition f = new NamePosition("F");

    private final KeyedCollection<NamePosition, String> collection;

    public AbstractKeyedCollectionTest(KeyedCollection<NamePosition, String> collection) {
        this.collection = collection;
    }

    public void testSimpleQueries() {
        collection.insert(a, "alpha");

        assertEquals(a, collection.find(a, b, "alpha"));
        assertNull(collection.find(a, a, "alpha"));
        assertNull(collection.find(b, c, "alpha"));

        collection.insert(b, "beta");
        collection.insert(c, "beta");
        assertEquals(b, collection.find(a, d, "beta"));
        assertEquals(b, collection.find(b, d, "beta"));
        assertEquals(c, collection.find(c, d, "beta"));
    }

    public void testHeavilyPopulatedCollection() {
        collection.insert(a, "alpha");
        collection.insert(b, "beta");
        collection.insert(c, "cat");
        collection.insert(d, "dog");
        collection.insert(e, "elephant");
        collection.insert(f, "firehouse");

        assertEquals(a, collection.find(a, f, "alpha"));
        assertEquals(b, collection.find(a, f, "beta"));
        assertEquals(c, collection.find(a, f, "cat"));
        assertEquals(d, collection.find(a, f, "dog"));
        assertEquals(e, collection.find(a, f, "elephant"));
        assertNull(collection.find(a, f, "firehouse"));
    }

    public void testTheSameValueManyTimes() {
        collection.insert(a, "alpha");
        collection.insert(b, "alpha");
        collection.insert(c, "alpha");
        collection.insert(d, "alpha");
        collection.insert(e, "alpha");
        collection.insert(f, "alpha");

        assertEquals(a, collection.find(a, f, "alpha"));
        assertEquals(c, collection.find(c, d, "alpha"));
        assertNull(collection.find(d, d, "alpha"));
    }

    public void testInconsistentMinMax() {
        try {
            collection.find(b, a, "alpha");
            fail();
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    static class NamePosition implements Comparable<NamePosition> {
        final String name;
        public NamePosition(String name) {
            this.name = name;
        }
        public int compareTo(NamePosition other) {
            return name.compareTo(other.name);
        }
        public String toString() {
            return name;
        }
    }
}