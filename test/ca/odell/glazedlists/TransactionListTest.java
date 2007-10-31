/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import junit.framework.TestCase;

import java.util.List;

public class TransactionListTest extends TestCase {

    private EventList<String> source;
    private TransactionList<String> txList;
    private GlazedListsTests.ListEventCounter<String> counter;

    protected void setUp() {
        source = new BasicEventList<String>();
        txList = new TransactionList<String>(source);
        counter = new GlazedListsTests.ListEventCounter<String>();
        txList.addListEventListener(counter);
    }

    protected void tearDown() {
        txList.removeListEventListener(counter);
        txList.dispose();

        counter = null;
        txList = null;
        source = null;
    }

    public void testRollbackSimpleListEvent() {
        assertState(GlazedListsTests.stringToList(""), 0);

        // 1. rollback simple add
        txList.beginEvent();
            txList.add("0");
            assertState(GlazedListsTests.stringToList("0"), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList(""), 0);

        source.add("0");
        assertEquals(1, counter.getCountAndReset());

        // 2. rollback simple remove
        txList.beginEvent();
            assertState(GlazedListsTests.stringToList("0"), 0);
            txList.remove(0);
            assertState(GlazedListsTests.stringToList(""), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList("0"), 0);

        // 3. rollback simple update
        txList.beginEvent();
            assertState(GlazedListsTests.stringToList("0"), 0);
            txList.set(0, "1");
            assertState(GlazedListsTests.stringToList("1"), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList("0"), 0);
    }

    public void testRollbackComplexListEvent() {
        source.addAll(GlazedListsTests.stringToList("ABC"));
        assertState(GlazedListsTests.stringToList("ABC"), 1);

        // 1. rollback add, remove, set as a single transaction
        txList.beginEvent();
            txList.add("D");
            txList.remove(1);
            txList.set(0, "B");
            assertState(GlazedListsTests.stringToList("BCD"), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList("ABC"), 0);
    }

    public void testCommitComplex() {
        source.addAll(GlazedListsTests.stringToList("ABC"));
        assertState(GlazedListsTests.stringToList("ABC"), 1);

        // 1. commit add, remove, set as a single transaction
        txList.beginEvent();
            txList.add("D");
            txList.remove(1);
            txList.set(0, "B");
            assertState(GlazedListsTests.stringToList("BCD"), 0);
        txList.commitEvent();
        assertState(GlazedListsTests.stringToList("BCD"), 1);
    }

    /**
     * If a transaction is "buffered" it accumulates all ListEvents during the
     * transaction into a single uber-ListEvent and fires it on commit.
     */
    public void testCommitOnBufferedTransactionFiresEvent() {
        assertState(GlazedListsTests.stringToList(""), 0);

        // 1. commit should fire a single ListEvent
        txList.beginEvent(true);
            txList.add("A");
            txList.add("B");
            assertState(GlazedListsTests.stringToList("AB"), 0);
        txList.commitEvent();
        assertState(GlazedListsTests.stringToList("AB"), 1);

        // 2. do it again to ensure that the txList is still usable
        txList.beginEvent(true);
            txList.add("C");
            txList.add("D");
            assertState(GlazedListsTests.stringToList("ABCD"), 0);
        txList.commitEvent();
        assertState(GlazedListsTests.stringToList("ABCD"), 1);
    }

    /**
     * If a transaction is "buffered" it accumulates all ListEvents during the
     * transaction into a single uber-ListEvent and thus fires no event on rollback.
     */
    public void testRollbackOnBufferedTransactionFiresNoEvent() {
        assertState(GlazedListsTests.stringToList(""), 0);

        // 1. rollback should no ListEvent
        txList.beginEvent(true);
            txList.add("A");
            txList.add("B");
            assertState(GlazedListsTests.stringToList("AB"), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList(""), 0);

        // 2. do it again to ensure that the txList is still usable
        txList.beginEvent(true);
            txList.add("C");
            txList.add("D");
            assertState(GlazedListsTests.stringToList("CD"), 0);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList(""), 0);
    }

    /**
     * If a transaction is "live" it forwards all ListEvents as they are
     * received and thus no ListEvent is fired on commit.
     */
    public void testCommitOnLiveTransactionFiresNoEvent() {
        assertState(GlazedListsTests.stringToList(""), 0);

        // 1. commit should fire no ListEvent
        txList.beginEvent(false);
            txList.add("A");
            txList.add("B");
            assertState(GlazedListsTests.stringToList("AB"), 2);
        txList.commitEvent();
        assertState(GlazedListsTests.stringToList("AB"), 0);

        // 2. do it again to ensure that the txList is still usable
        txList.beginEvent(false);
            txList.add("C");
            txList.add("D");
            assertState(GlazedListsTests.stringToList("ABCD"), 2);
        txList.commitEvent();
        assertState(GlazedListsTests.stringToList("ABCD"), 0);
    }

    /**
     * If a transaction is "live" it forwards all ListEvents as they are
     * received and thus a ListEvent must be fired on rollback.
     */
    public void testRollbackOnLiveTransactionFiresEvent() {
        assertState(GlazedListsTests.stringToList(""), 0);

        // 1. rollback should fire ListEvent
        txList.beginEvent(false);
            txList.add("A");
            txList.add("B");
            assertState(GlazedListsTests.stringToList("AB"), 2);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList(""), 1);

        // 2. do it again to ensure that the txList is still usable
        txList.beginEvent(false);
            txList.add("C");
            txList.add("D");
            assertState(GlazedListsTests.stringToList("CD"), 2);
        txList.rollbackEvent();
        assertState(GlazedListsTests.stringToList(""), 1);
    }

    public void testDoubleBegin() {
        txList.beginEvent();

        try {
            txList.beginEvent();
        } catch (IllegalStateException e) {
            assertEquals("Unable to begin a new transaction before committing or rolling back the previous transaction", e.getMessage());
        }
    }

    public void testCommitNothing() {
        try {
            txList.commitEvent();
        } catch (IllegalStateException e) {
            assertEquals("No ListEvent exists to commit", e.getMessage());
        }
    }

    public void testRollbackNothing() {
        try {
            txList.rollbackEvent();
        } catch (IllegalStateException e) {
            assertEquals("No ListEvent exists to roll back", e.getMessage());
        }
    }
    
    public void testNoRollbackSupport() {
        source = new BasicEventList<String>();
        txList = new TransactionList<String>(source, false);
        counter = new GlazedListsTests.ListEventCounter<String>();
        txList.addListEventListener(counter);

        txList.beginEvent();

        try {
            txList.rollbackEvent();
        } catch (IllegalStateException e) {
            assertEquals("This TransactionList does not support rollback", e.getMessage());
        }
    }

    private void assertState(List expected, int numEvents) {
        assertEquals(expected, source);
        assertEquals(expected, txList);
        assertEquals(numEvents, counter.getCountAndReset());
    }
}