/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

/**
 * Validate that the {@link IndexSnapshot} class works as expected.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IndexSnapshotTest extends TestCase {

    public void testNaturalOrder() {
        List original = new ArrayList(GlazedListsTests.stringToList("FILTER"));

        IndexSnapshot indexSnapshot = new IndexSnapshot();

        indexSnapshot.reset(original.size());
        List copy = new ArrayList(original);

        copy.remove(1);
        indexSnapshot.remove(1);
        copy.remove(3);
        indexSnapshot.remove(3);
        assertEquals(copy, GlazedListsTests.stringToList("FLTR"));

        assertEquals( 0, indexSnapshot.currentToSnapshot(0));
        assertEquals( 2, indexSnapshot.currentToSnapshot(1));
        assertEquals( 3, indexSnapshot.currentToSnapshot(2));
        assertEquals( 5, indexSnapshot.currentToSnapshot(3));
        assertEquals( 0, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals( 1, indexSnapshot.snapshotToCurrent(2));
        assertEquals( 2, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals( 3, indexSnapshot.snapshotToCurrent(5));

        copy.add(2, "O");
        indexSnapshot.add(2);
        copy.add(3, "A");
        indexSnapshot.add(3);
        assertEquals(copy, GlazedListsTests.stringToList("FLOATR"));

        assertEquals( 0, indexSnapshot.currentToSnapshot(0));
        assertEquals( 2, indexSnapshot.currentToSnapshot(1));
        assertEquals( 6, indexSnapshot.currentToSnapshot(2));
        assertEquals( 7, indexSnapshot.currentToSnapshot(3));
        assertEquals( 3, indexSnapshot.currentToSnapshot(4));
        assertEquals( 5, indexSnapshot.currentToSnapshot(5));
        assertEquals( 0, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals( 1, indexSnapshot.snapshotToCurrent(2));
        assertEquals( 4, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals( 5, indexSnapshot.snapshotToCurrent(5));
        assertEquals( 5, indexSnapshot.snapshotToCurrent(5));

        copy.add(1, "E");
        indexSnapshot.add(1);
        copy.remove(3);
        indexSnapshot.remove(3);
        assertEquals(copy, GlazedListsTests.stringToList("FELATR"));

        assertEquals( 0, indexSnapshot.currentToSnapshot(0));
        assertEquals( 6, indexSnapshot.currentToSnapshot(1));
        assertEquals( 2, indexSnapshot.currentToSnapshot(2));
        assertEquals( 7, indexSnapshot.currentToSnapshot(3));
        assertEquals( 3, indexSnapshot.currentToSnapshot(4));
        assertEquals( 5, indexSnapshot.currentToSnapshot(5));
        assertEquals( 0, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals( 2, indexSnapshot.snapshotToCurrent(2));
        assertEquals( 4, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals( 5, indexSnapshot.snapshotToCurrent(5));

        copy.remove(3);
        indexSnapshot.remove(3);
        copy.add(4, "I");
        indexSnapshot.add(4);
        assertEquals(copy, GlazedListsTests.stringToList("FELTIR"));

        assertEquals( 0, indexSnapshot.currentToSnapshot(0));
        assertEquals( 6, indexSnapshot.currentToSnapshot(1));
        assertEquals( 2, indexSnapshot.currentToSnapshot(2));
        assertEquals( 3, indexSnapshot.currentToSnapshot(3));
        assertEquals( 7, indexSnapshot.currentToSnapshot(4));
        assertEquals( 5, indexSnapshot.currentToSnapshot(5));
        assertEquals( 0, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals( 2, indexSnapshot.snapshotToCurrent(2));
        assertEquals( 3, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals( 5, indexSnapshot.snapshotToCurrent(5));

        copy.remove(0);
        indexSnapshot.remove(0);
        copy.remove(1);
        indexSnapshot.remove(1);
        assertEquals(copy, GlazedListsTests.stringToList("ETIR"));

        assertEquals( 6, indexSnapshot.currentToSnapshot(0));
        assertEquals( 3, indexSnapshot.currentToSnapshot(1));
        assertEquals( 7, indexSnapshot.currentToSnapshot(2));
        assertEquals( 5, indexSnapshot.currentToSnapshot(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(2));
        assertEquals( 1, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals( 3, indexSnapshot.snapshotToCurrent(5));

        copy.remove(1);
        indexSnapshot.remove(1);
        copy.remove(2);
        indexSnapshot.remove(2);
        assertEquals(copy, GlazedListsTests.stringToList("EI"));

        assertEquals( 6, indexSnapshot.currentToSnapshot(0));
        assertEquals( 7, indexSnapshot.currentToSnapshot(1));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(2));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(5));

        copy.add(0, "V");
        indexSnapshot.add(0);
        copy.add(3, "X");
        indexSnapshot.add(3);
        assertEquals(copy, GlazedListsTests.stringToList("VEIX"));

        assertEquals( 6, indexSnapshot.currentToSnapshot(0));
        assertEquals( 7, indexSnapshot.currentToSnapshot(1));
        assertEquals( 8, indexSnapshot.currentToSnapshot(2));
        assertEquals( 9, indexSnapshot.currentToSnapshot(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(0));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(1));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(2));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(3));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(4));
        assertEquals(-1, indexSnapshot.snapshotToCurrent(5));
    }


    public void testIndexBoundsExceptions() {

        IndexSnapshot indexSnapshot = new IndexSnapshot();
        indexSnapshot.reset(2);
        indexSnapshot.add(1);
        indexSnapshot.add(1);

        indexSnapshot.currentToSnapshot(3);
        try {
            indexSnapshot.currentToSnapshot(4);
            fail();
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        indexSnapshot.currentToSnapshot(0);
        try {
            indexSnapshot.currentToSnapshot(-1);
            fail();
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        indexSnapshot.snapshotToCurrent(0);
        try {
            indexSnapshot.snapshotToCurrent(-1);
            fail();
        } catch(IndexOutOfBoundsException e) {
            // expected
        }

        indexSnapshot.snapshotToCurrent(1);
        try {
            indexSnapshot.snapshotToCurrent(2);
            fail();
        } catch(IndexOutOfBoundsException e) {
            // expected
        }
    }
}