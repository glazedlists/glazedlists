/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class Tree4DeltasTest extends TestCase {

    /**
     * Make sure the deltas iterator works as expected.
     */
    public void testIterateByBlocks() {
        Tree4Deltas deltas = new Tree4Deltas();
        deltas.reset(10);
        deltas.insert(3, 6);
        deltas.delete(8, 10, ListEvent.UNKNOWN_VALUE);

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
}
