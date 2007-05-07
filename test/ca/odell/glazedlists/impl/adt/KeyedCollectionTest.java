package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.impl.adt.KeyedCollection.Position;

import junit.framework.TestCase;

/**
 * @author jessewilson
 */
public class KeyedCollectionTest extends TestCase {

    private Position a = new NamePosition("A");
    private Position b = new NamePosition("B");
    private Position c = new NamePosition("C");
    private Position d = new NamePosition("D");
    private Position e = new NamePosition("E");
    private Position f = new NamePosition("F");

    private KeyedCollection<String> collection = null;

    public void testSimpleQueries() {
        String result = collection.insert(a, "alpha");
        assertNull(result);

        assertEquals(a, collection.find(a, b, "alpha"));
        assertNull(collection.find(a, a, "alpha"));
        assertNull(collection.find(b, c, "alpha"));
        assertSame(a, collection.first());
        assertSame(a, collection.last());

        collection.insert(b, "beta");
        collection.insert(c, "beta");
        assertEquals(b, collection.find(a, d, "beta"));
        assertEquals(b, collection.find(b, d, "beta"));
        assertEquals(c, collection.find(c, d, "beta"));
        assertSame(a, collection.first());
        assertSame(c, collection.last());
    }

    public void testLastAndFirst() {
        assertNull(collection.first());
        assertNull(collection.last());

        collection.insert(c, "alpha");
        assertSame(c,  collection.first());
        assertSame(c,  collection.last());

        collection.insert(d, "alpha");
        assertSame(c,  collection.first());
        assertSame(d,  collection.last());

        collection.insert(b, "alpha");
        assertSame(b,  collection.first());
        assertSame(d,  collection.last());
    }

    public void testHeavilyPopulatedCollection() {
        collection.insert(a, "alpha");
        collection.insert(b, "beta");
        collection.insert(c, "cat");
        collection.insert(d, "dog");
        collection.insert(e, "elephant");
        collection.insert(f, "firehouse");

        assertSame(a, collection.first());
        assertSame(f, collection.last());

        assertEquals(a, collection.find(a, f, "alpha"));
        assertEquals(b, collection.find(a, f, "beta"));
        assertEquals(c, collection.find(a, f, "cat"));
        assertEquals(d, collection.find(a, f, "dog"));
        assertEquals(e, collection.find(a, f, "elephant"));
        assertNull(collection.find(a, f, "firehouse"));
    }

    public void testReplacedValue() {
        collection.insert(b, "beta");
        assertEquals("beta", collection.insert(b, "boop"));
        assertEquals("boop", collection.insert(b, "beep"));

        assertSame(b, collection.first());
        assertSame(b, collection.last());
    }

    public void testInconsistentMinMax() {
        try {
            collection.find(b, a, "alpha");
            fail();
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    static class NamePosition implements Position {
        final String name;
        public NamePosition(String name) {
            this.name = name;
        }
        public int compareTo(Position o) {
            return name.compareTo(((NamePosition)o).name);
        }
        public String toString() {
            return name;
        }
    }
}
