package ca.odell.glazedlists.impl.adt;

import ca.odell.glazedlists.impl.adt.KeyedCollection.Position;
import ca.odell.glazedlists.GlazedLists;

import junit.framework.TestCase;

import java.util.Comparator;

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

    protected void setUp() {
        collection = new KeyedCollectionForComparableValues<String>((Comparator) GlazedLists.comparableComparator());
    }

    protected void tearDown() {
        collection = null;
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