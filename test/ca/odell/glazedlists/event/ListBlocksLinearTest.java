/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListBlocksLinearTest extends TestCase {

    /**
     * Test if simple changes to the linear blocks work as expected.
     */
    public void testSimpleChanges() {
        BlockSequence listBlocks = new BlockSequence();
        listBlocks.insert(0, 2);
        listBlocks.insert(4, 6);
        listBlocks.delete(8, 10);
        listBlocks.delete(10, 12);
        listBlocks.update(12, 14);
        listBlocks.update(16, 18);

        BlockSequence.Iterator iterator = listBlocks.iterator();
        assertNext(0, ListEvent.INSERT, iterator);
        assertNext(1, ListEvent.INSERT, iterator);
        assertNext(4, ListEvent.INSERT, iterator);
        assertNext(5, ListEvent.INSERT, iterator);
        assertNext(8, ListEvent.DELETE, iterator);
        assertNext(8, ListEvent.DELETE, iterator);
        assertNext(10, ListEvent.DELETE, iterator);
        assertNext(10, ListEvent.DELETE, iterator);
        assertNext(12, ListEvent.UPDATE, iterator);
        assertNext(13, ListEvent.UPDATE, iterator);
        assertNext(16, ListEvent.UPDATE, iterator);
        assertNext(17, ListEvent.UPDATE, iterator);
        assertComplete(iterator);
    }

    /**
     * Test if combining changes to the linear blocks work as expected.
     */
    public void testCombiningChanges() {
        BlockSequence listBlocks = new BlockSequence();
        listBlocks.insert(0, 2);
        listBlocks.insert(2, 4);
        listBlocks.delete(4, 6);
        listBlocks.delete(4, 6);
        listBlocks.delete(4, 6);
        listBlocks.update(4, 6);
        listBlocks.update(6, 8);
        listBlocks.delete(8, 10);
        listBlocks.insert(8, 10);
        listBlocks.update(10, 12);

        BlockSequence.Iterator iterator = listBlocks.iterator();
        assertNext(0, ListEvent.INSERT, iterator);
        assertNext(1, ListEvent.INSERT, iterator);
        assertNext(2, ListEvent.INSERT, iterator);
        assertNext(3, ListEvent.INSERT, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.DELETE, iterator);
        assertNext(4, ListEvent.UPDATE, iterator);
        assertNext(5, ListEvent.UPDATE, iterator);
        assertNext(6, ListEvent.UPDATE, iterator);
        assertNext(7, ListEvent.UPDATE, iterator);
        assertNext(8, ListEvent.DELETE, iterator);
        assertNext(8, ListEvent.DELETE, iterator);
        assertNext(8, ListEvent.INSERT, iterator);
        assertNext(9, ListEvent.INSERT, iterator);
        assertNext(10, ListEvent.UPDATE, iterator);
        assertNext(11, ListEvent.UPDATE, iterator);
        assertComplete(iterator);
    }

    /**
     * Test if linear blocks returns false appropriately when changes are
     * out of order.
     */
    public void testOutOfOrder() {
        BlockSequence listBlocks;

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.insert(0, 2));
        assertTrue(listBlocks.insert(2, 3));
        assertFalse(listBlocks.insert(2, 3));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.delete(2, 4));
        assertTrue(listBlocks.delete(2, 3));
        assertFalse(listBlocks.delete(1, 2));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.update(2, 4));
        assertTrue(listBlocks.update(4, 5));
        assertFalse(listBlocks.update(4, 5));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.insert(2, 4));
        assertFalse(listBlocks.update(3, 4));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.insert(2, 4));
        assertFalse(listBlocks.delete(3, 4));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.update(2, 4));
        assertFalse(listBlocks.delete(3, 4));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.update(2, 4));
        assertFalse(listBlocks.insert(3, 4));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.delete(2, 4));
        assertTrue(listBlocks.insert(2, 4));
        listBlocks = new BlockSequence();
        assertTrue(listBlocks.delete(2, 4));
        assertFalse(listBlocks.insert(1, 2));

        listBlocks = new BlockSequence();
        assertTrue(listBlocks.delete(2, 4));
        assertTrue(listBlocks.update(2, 4));
        listBlocks = new BlockSequence();
        assertTrue(listBlocks.delete(2, 4));
        assertFalse(listBlocks.update(1, 2));
    }

    public static final void assertNext(int index, int type, BlockSequence.Iterator iterator) {
        assertEquals(true, iterator.hasNext());
        assertEquals(true, iterator.next());
        assertEquals(index, iterator.getIndex());
        assertEquals(type, iterator.getType());
    }

    public static final void assertComplete(BlockSequence.Iterator iterator) {
        assertEquals(false, iterator.hasNext());
    }
}