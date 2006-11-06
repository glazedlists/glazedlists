package ca.odell.glazedlists;

import junit.framework.TestCase;

import javax.swing.*;
import java.util.List;

public class TransactionListTest extends TestCase {

    public void testBasicCommit() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        // no tx, the changes should propagate immediately
        txList.add("Jesse");
        assertEquals("Jesse", txList.get(0));

        // begin a tx and make some changes
        txList.begin();
        txList.remove("Jesse");
        txList.add("James");
        txList.set(0, "Holger");

        // the txList changes should not yet propagate to the source (we must commit())
        assertEquals("Jesse", source.get(0));
        assertEquals("Holger", txList.get(0));
        assertEquals("Jesse", readValueFromEDT(txList, 0));

        // commit the tx and verify that the changes are now visible in the source
        txList.commit();
        assertEquals("Holger", source.get(0));
        assertEquals("Holger", txList.get(0));
        assertEquals("Holger", readValueFromEDT(txList, 0));
    }

    public void testBasicRollback() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        // no tx, the changes should propagate immediately
        txList.add("Jesse");
        assertEquals("Jesse", txList.get(0));

        // begin a tx and make some changes
        txList.begin();
        txList.remove("Jesse");
        txList.add("James");
        txList.set(0, "Holger");

        // the txList changes should not yet propagate to the source (we must commit())
        assertEquals("Jesse", source.get(0));
        assertEquals("Holger", txList.get(0));
        assertEquals("Jesse", readValueFromEDT(txList, 0));

        // rollback the tx and verify that the changes are now gone in the txList
        txList.rollback();
        assertEquals("Jesse", source.get(0));
        assertEquals("Jesse", txList.get(0));
        assertEquals("Jesse", readValueFromEDT(txList, 0));
    }

    public void testComplexCommit() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        // no tx, the changes should propagate immediately
        txList.add("Jesse");
        txList.add("James");
        txList.add("Holger");
        assertEquals("Jesse", txList.get(0));
        assertEquals("James", txList.get(1));
        assertEquals("Holger", txList.get(2));

        // begin a tx and make some changes (a remove, add, and set)
        txList.begin();
        txList.remove("Jesse");
        txList.add("Kevin");
        txList.set(0, "Bruce");

        // the txList changes should not yet propagate to the source (we must commit())
        assertEquals("Jesse", source.get(0));
        assertEquals("James", source.get(1));
        assertEquals("Holger", source.get(2));
        assertEquals("Bruce", txList.get(0));
        assertEquals("Holger", txList.get(1));
        assertEquals("Kevin", txList.get(2));

        // commit the tx and verify that the changes are now visible in the source
        txList.commit();
        assertEquals("Bruce", source.get(0));
        assertEquals("Holger", source.get(1));
        assertEquals("Kevin", source.get(2));
        assertEquals("Bruce", txList.get(0));
        assertEquals("Holger", txList.get(1));
        assertEquals("Kevin", txList.get(2));
    }

    public void testComplexRollback() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        // no tx, the changes should propagate immediately
        txList.add("Jesse");
        txList.add("James");
        txList.add("Holger");
        assertEquals("Jesse", txList.get(0));
        assertEquals("James", txList.get(1));
        assertEquals("Holger", txList.get(2));

        // begin a tx and make some changes (a remove, add, and set)
        txList.begin();
        txList.remove("Jesse");
        txList.add("Kevin");
        txList.set(0, "Bruce");

        // the txList changes should not yet propagate to the source (we must commit())
        assertEquals("Jesse", source.get(0));
        assertEquals("James", source.get(1));
        assertEquals("Holger", source.get(2));
        assertEquals("Bruce", txList.get(0));
        assertEquals("Holger", txList.get(1));
        assertEquals("Kevin", txList.get(2));

        // rollback the tx and verify that the changes are now gone from the source
        txList.rollback();
        assertEquals("Jesse", source.get(0));
        assertEquals("James", source.get(1));
        assertEquals("Holger", source.get(2));
        assertEquals("Jesse", txList.get(0));
        assertEquals("James", txList.get(1));
        assertEquals("Holger", txList.get(2));
    }

    public void testBeginTwoTransactions() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        txList.begin();
        try {
            txList.begin();
            fail("failed to receive an IllegalStateException when calling begin() with an active transaction");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testCommitNonExistentTransactions() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        try {
            txList.commit();
            fail("failed to receive an IllegalStateException when calling commit() with no active transaction");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testRollbackNonExistentTransactions() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        try {
            txList.rollback();
            fail("failed to receive an IllegalStateException when calling rollback() with no active transaction");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    private Object readValueFromEDT(List list, int index) {
        try {
            final GetAtIndexRunnable runnable = new GetAtIndexRunnable(list, index);
            SwingUtilities.invokeAndWait(runnable);
            return runnable.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class GetAtIndexRunnable implements Runnable {

        private final List list;
        private final int index;
        private Object value;

        public GetAtIndexRunnable(List list, int index) {
            this.list = list;
            this.index = index;
        }

        public void run() {
            value = list.get(index);
        }

        public Object getValue() {
            return value;
        }
    }
}