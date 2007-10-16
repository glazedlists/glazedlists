/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.NestableEventsList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.impl.event.Tree4Deltas;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListDeltasTest extends TestCase {


    public void testInsert() {
        List<String> original = GlazedListsTests.stringToList("FILTER");
        List<String> current = new ArrayList<String>(original);

        BarcodeListDeltas deltas = new BarcodeListDeltas();
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
        List<String> original = GlazedListsTests.stringToList("FILTER");
        List<String> current = new ArrayList<String>(original);

        BarcodeListDeltas deltas = new BarcodeListDeltas();
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

    public void testIterator() {
        List<String> original = GlazedListsTests.stringToList("FILTER");
        List<String> current = new ArrayList<String>(original);

        BarcodeListDeltas deltas = new BarcodeListDeltas();
        deltas.reset(original.size());

        current.remove(1);
        deltas.remove(1);
        current.remove(3);
        deltas.remove(3);
        assertEquals(GlazedListsTests.stringToList("FLTR"), current);

        current.add(2, "A");
        deltas.add(2);
        current.add(3, "S");
        deltas.add(3);
        current.add(5, "E");
        deltas.add(5);
        assertEquals(GlazedListsTests.stringToList("FLASTER"), current);

        current.set(0, "B");
        deltas.update(0);
        current.set(6, "D");
        deltas.update(6);
        assertEquals(GlazedListsTests.stringToList("BLASTED"), current);

        assertConsistent(original, deltas, current);

        BarcodeListDeltas.Iterator iterator = deltas.iterator();
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.UPDATE, iterator.getType());
        assertEquals(0, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.DELETE, iterator.getType());
        assertEquals(1, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.INSERT, iterator.getType());
        assertEquals(2, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.INSERT, iterator.getType());
        assertEquals(3, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.DELETE, iterator.getType());
        assertEquals(5, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.INSERT, iterator.getType());
        assertEquals(5, iterator.getIndex());
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.UPDATE, iterator.getType());
        assertEquals(6, iterator.getIndex());
        assertEquals(false, iterator.next());
    }

    public void testIteratorTrailingInsert() {
        List<String> original = GlazedListsTests.stringToList("JA");
        List<String> current = new ArrayList<String>(original);

        BarcodeListDeltas deltas = new BarcodeListDeltas();
        deltas.reset(original.size());

        current.add(2, "M");
        deltas.add(2);
        assertEquals(GlazedListsTests.stringToList("JAM"), current);

        assertConsistent(original, deltas, current);

        BarcodeListDeltas.Iterator iterator = deltas.iterator();
        assertEquals(true, iterator.next());
        assertEquals(ListEvent.INSERT, iterator.getType());
        assertEquals(2, iterator.getIndex());
        assertEquals(false, iterator.next());
    }


    /**
     * Test a simple problem that initially failed in the second round
     * of ListDeltas.
     */
    public void testAddAndRemove() {
        EventList<String> source = new BasicEventList<String>();
        NestableEventsList<String> nesting = new NestableEventsList<String>(source);
        ListConsistencyListener.install(nesting);

        nesting.beginEvent(true);
        source.addAll(GlazedListsTests.stringToList("ABCDE"));
        nesting.commitEvent();

        nesting.beginEvent(true);
        source.remove(4);
        source.add(4, "F");
        nesting.commitEvent();
    }

    public void assertConsistent(List original, BarcodeListDeltas deltas, List current) {
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


    /**
     * Tests that update does the right thing in list deltas.
     */
    public void testInsertedElementUpdated() {
        EventList<String> source = new BasicEventList<String>();
        NestableEventsList<String> nesting = new NestableEventsList<String>(source);
        ListConsistencyListener.install(nesting);

        nesting.beginEvent(true);
        source.addAll(GlazedListsTests.stringToList("AB"));
        nesting.commitEvent();

        nesting.beginEvent(true);
        source.add(0, "C");
        source.add(1, "D");
        source.set(1, "d");
        source.remove(2);
        source.set(2, "b");
        nesting.commitEvent();
    }



    /**
     *
     */
    public void testInsertsAndDeletes() {
        EventList<String> source = new BasicEventList<String>();
        NestableEventsList<String> nesting = new NestableEventsList<String>(source);
        ListConsistencyListener.install(nesting);

        nesting.beginEvent(true);
        source.addAll(GlazedListsTests.stringToList("ABC"));
        nesting.commitEvent();

        nesting.beginEvent(true);
        source.add(0, "D");
        source.add(1, "E");
        source.add(2, "F");
        source.add(3, "G");
        source.add(4, "H");
        source.remove(3);
        source.remove(3);
        source.remove(3);
        source.remove(3);
        source.remove(3);
        nesting.commitEvent();
    }

    public void testDeleteIndexBeforeAnotherDeleteIndex() {
        EventList<String> source = new BasicEventList<String>();
        NestableEventsList<String> nesting = new NestableEventsList<String>(source);
        ListConsistencyListener.install(nesting);

        nesting.beginEvent(true);
        source.addAll(GlazedListsTests.stringToList("ABCDEFGHIJKLM"));
        nesting.commitEvent();

        nesting.beginEvent(true);
        source.remove(0);
        source.remove(2);
        source.remove(9);
        source.remove(7);
        nesting.commitEvent();
    }

    /**
     * Make sure the deltas iterator works as expected.
     */
    public void testIterateByBlocks() {
        Tree4Deltas deltas = new Tree4Deltas();
        deltas.reset(10);
        deltas.targetInsert(3, 6, ListEvent.UNKNOWN_VALUE);

        Tree4Deltas.Iterator iterator = deltas.iterator();
        assertEquals(true, iterator.hasNextNode());
        assertEquals(true, iterator.nextNode());
        assertEquals(3, iterator.getIndex());
        assertEquals(6, iterator.getEndIndex());
        assertEquals(false, iterator.hasNextNode());
    }
}