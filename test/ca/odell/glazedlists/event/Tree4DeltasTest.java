/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

import ca.odell.glazedlists.impl.event.Tree4Deltas;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree4DeltasTest extends TestCase {

    /**
     * Make sure the deltas iterator works as expected.
     */
    public void testIterateByBlocks() {
        Tree4Deltas<Object> deltas = new Tree4Deltas<Object>();
        deltas.reset(10);
        deltas.targetInsert(3, 6, null);
        deltas.targetDelete(8, 10, ListEvent.UNKNOWN_VALUE);

        Tree4Deltas.Iterator iterator = deltas.iterator();
        assertEquals(true, iterator.hasNextNode());
        assertEquals(true, iterator.nextNode());
        assertEquals(3, iterator.getIndex());
        assertEquals(6, iterator.getEndIndex());
        assertEquals(ListEvent.INSERT, iterator.getType());

        assertEquals(true, iterator.hasNextNode());
        assertEquals(true, iterator.nextNode());
        assertEquals(8, iterator.getIndex());
        assertEquals(10, iterator.getEndIndex());
        assertEquals(ListEvent.DELETE, iterator.getType());

        assertEquals(false, iterator.hasNextNode());
    }

    public void testTargetChanges() {
        Tree4Deltas<Object> deltas = new Tree4Deltas<Object>();
        deltas.reset(10);
        assertEquals("__________", deltas.toString());

        deltas.targetDelete(5, 6, null);
        deltas.targetInsert(2, 3, null);
        deltas.targetUpdate(8, 9, null, null);

        assertEquals("__+___X__U_", deltas.toString());

        deltas.sourceInsert(0);
        assertEquals("___+___X__U_", deltas.toString());

        deltas.sourceDelete(0);
        assertEquals("__+___X__U_", deltas.toString());

        deltas.sourceDelete(8);
        assertEquals("__+___X___", deltas.toString());

        deltas.sourceDelete(5);
        assertEquals("__+______", deltas.toString());

        deltas.targetInsert(2, 6, null);
        assertEquals("__+++++______", deltas.toString());

        deltas.sourceInsert(8);
        assertEquals("__+++++_______", deltas.toString());
    }

    public void testTargetValues() {
        Tree4Deltas<String> deltas = new Tree4Deltas<String>();
        deltas.reset(10);
        assertEquals("__________", deltas.toString());

        deltas.targetDelete(5, 6, null);
        deltas.targetInsert(2, 3, "i");
        deltas.targetUpdate(8, 9, "u", null);
        assertEquals("i", deltas.getTargetValue(2));
        assertEquals("u", deltas.getTargetValue(8));
        assertEquals(ListEvent.UNKNOWN_VALUE, deltas.getTargetValue(0));
    }
}