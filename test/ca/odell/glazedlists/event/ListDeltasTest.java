/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;

import ca.odell.glazedlists.event.ListDeltas;
import ca.odell.glazedlists.GlazedListsTests;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListDeltasTest extends TestCase {


    public void testInsert() {
        List original = GlazedListsTests.stringToList("FILTER");
        List current = new ArrayList(original);

        ListDeltas deltas = new ListDeltas();
        deltas.reset(original.size());

        current.remove(2);
        deltas.remove(2);
        assertConsistent(original, deltas, current);

        current.add(3, "P");
        deltas.add(3);
        assertConsistent(original, deltas, current);

        current.add(1, "X");
        deltas.add(1);
        assertConsistent(original, deltas, current);

        current.add(5, "Y");
        deltas.add(5);
        assertConsistent(original, deltas, current);

        current.add(0, "Z");
        deltas.add(0);
        assertConsistent(original, deltas, current);

        current.remove(1);
        deltas.remove(1);
        assertConsistent(original, deltas, current);

        current.remove(4);
        deltas.remove(4);
        assertConsistent(original, deltas, current);
    }


    public void testUpdates() {
        List original = GlazedListsTests.stringToList("FILTER");
        List current = new ArrayList(original);

        ListDeltas deltas = new ListDeltas();
        deltas.reset(original.size());

        current.remove(1);
        deltas.remove(1);
        assertConsistent(original, deltas, current);
        current.remove(3);
        deltas.remove(3);
        assertConsistent(original, deltas, current);

        assertEquals(GlazedListsTests.stringToList("FLTR"), current);

        current.add(2, "O");
        deltas.add(2);
        assertConsistent(original, deltas, current);
        current.add(3, "A");
        deltas.add(3);
        current.add(5, "E");
        deltas.add(5);
        assertConsistent(original, deltas, current);

        assertEquals(GlazedListsTests.stringToList("FLOATER"), current);

        current.set(0, "B");
        deltas.update(0);
        current.set(3, "S");
        deltas.update(3);
        current.set(2, "A");
        deltas.update(2);
        current.set(6, "D");
        deltas.update(6);
        assertConsistent(original, deltas, current);

        assertEquals(GlazedListsTests.stringToList("BLASTED"), current);
    }

    public void assertConsistent(List original, ListDeltas deltas, List current) {
        // test the original to current mapping
        for(int i = 0; i < original.size(); i++) {
            Object valueInOriginal = original.get(i);
            int indexInCurrent = deltas.snapshotToCurrent(i);
            if(indexInCurrent == -1) continue;
            Object valueInCurrent = current.get(indexInCurrent);
            boolean updated = deltas.currentUpdated(indexInCurrent);
            if(updated) {
                assertNotSame(valueInOriginal, valueInCurrent);
            } else {
                assertSame(valueInOriginal, valueInCurrent);
            }
        }

        // test the current to original mapping
        for(int i = 0; i < current.size(); i++) {
            Object valueInCurrent = current.get(i);
            int indexInOriginal = deltas.currentToSnapshot(i);
            if(indexInOriginal < 0) continue;
            Object valueInOriginal = original.get(indexInOriginal);
            if(deltas.currentUpdated(i)) {
                assertNotSame(valueInOriginal, valueInCurrent);
            } else {
                assertSame(valueInOriginal, valueInCurrent);
            }
        }



    }

}