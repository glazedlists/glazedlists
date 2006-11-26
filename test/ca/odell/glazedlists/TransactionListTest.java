package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import javax.swing.*;
import java.util.List;
import java.util.Collections;

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
        assertEquals("Holger", readValueFromEDT(txList, 0));

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
        assertEquals("Holger", readValueFromEDT(txList, 0));

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

    public void testSourceUpdateConflictsWithTargetUpdate() {
        final EventList<String> source = new BasicEventList<String>();
        TransactionList<String> txList = new TransactionList<String>(source, TransactionList.PREFER_TARGET_CHANGES);
        ListConsistencyListener<String> consistencyListener = ListConsistencyListener.install(txList);

        txList.add("Jesse");
        assertEquals(1, consistencyListener.getEventCount());

        txList.begin();
        txList.set(0, "James");
        assertEquals("Jesse", source.get(0));
        assertEquals("James", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());

        source.set(0, "Holger");
        assertEquals("Holger", source.get(0));
        assertEquals("James", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());

        txList.commit();
        assertEquals("James", source.get(0));
        assertEquals("James", txList.get(0));
        assertEquals(3, consistencyListener.getEventCount());

        // try the same test over again but use a policy that prefers the source values over the target
        txList.dispose();
        txList = new TransactionList<String>(source, TransactionList.PREFER_SOURCE_CHANGES);
        consistencyListener = ListConsistencyListener.install(txList);

        txList.begin();
        txList.set(0, "Jesse");
        assertEquals("James", source.get(0));
        assertEquals("Jesse", txList.get(0));
        assertEquals(1, consistencyListener.getEventCount());

        source.set(0, "Holger");
        assertEquals("Holger", source.get(0));
        assertEquals("Holger", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());

        txList.commit();
        assertEquals("Holger", source.get(0));
        assertEquals("Holger", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());
    }

    public void testSourceDeleteConflictsWithTargetUpdate() {
        final EventList<String> source = new BasicEventList<String>();
        TransactionList<String> txList = new TransactionList<String>(source, TransactionList.PREFER_TARGET_CHANGES);
        ListConsistencyListener<String> consistencyListener = ListConsistencyListener.install(txList);

        txList.add("Jesse");
        assertEquals(1, consistencyListener.getEventCount());

        txList.begin();
        txList.set(0, "James");
        assertEquals("Jesse", source.get(0));
        assertEquals("James", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());

        source.remove(0);
        assertTrue(source.isEmpty());
        assertEquals("James", txList.get(0));
        assertEquals(2, consistencyListener.getEventCount());

        txList.commit();
        assertEquals("James", source.get(0));
        assertEquals("James", txList.get(0));
        assertEquals(3, consistencyListener.getEventCount());

        // try the same test over again but use a policy that prefers the source values over the target
        txList.dispose();
        txList = new TransactionList<String>(source, TransactionList.PREFER_SOURCE_CHANGES);
        consistencyListener = ListConsistencyListener.install(txList);

        txList.begin();
        txList.set(0, "Jesse");
        assertEquals("James", source.get(0));
        assertEquals("Jesse", txList.get(0));
        assertEquals(1, consistencyListener.getEventCount());

        source.remove(0);
        assertTrue(source.isEmpty());
        assertTrue(txList.isEmpty());
        assertEquals(2, consistencyListener.getEventCount());

        txList.commit();
        assertTrue(source.isEmpty());
        assertTrue(txList.isEmpty());
        assertEquals(2, consistencyListener.getEventCount());
    }

    public void testSourceUpdateConflictsWithTargetDelete() {
        final EventList<String> source = new BasicEventList<String>();
        TransactionList<String> txList = new TransactionList<String>(source, TransactionList.PREFER_TARGET_CHANGES);
        ListConsistencyListener<String> consistencyListener = ListConsistencyListener.install(txList);

        txList.add("Jesse");
        assertEquals(1, consistencyListener.getEventCount());

        txList.begin();
        txList.remove(0);
        assertEquals("Jesse", source.get(0));
        assertTrue(txList.isEmpty());
        assertEquals(2, consistencyListener.getEventCount());

        source.set(0, "James");
        assertEquals("James", source.get(0));
        assertTrue(txList.isEmpty());
        assertEquals(2, consistencyListener.getEventCount());

        txList.commit();
        assertTrue(source.isEmpty());
        assertTrue(txList.isEmpty());
        assertEquals(3, consistencyListener.getEventCount());

        // try the same test over again but use a policy that prefers the source values over the target
        txList.dispose();
        txList = new TransactionList<String>(source, TransactionList.PREFER_SOURCE_CHANGES);
        consistencyListener = ListConsistencyListener.install(txList);
        txList.add("James");
        assertEquals(1, consistencyListener.getEventCount());

        txList.begin();
        txList.remove(0);
        assertEquals("James", source.get(0));
        assertTrue(txList.isEmpty());
        assertEquals(2, consistencyListener.getEventCount());

        source.set(0, "Jesse");
        assertEquals("Jesse", source.get(0));
        assertEquals("Jesse", txList.get(0));
        assertEquals(3, consistencyListener.getEventCount());

        txList.commit();
        assertEquals("Jesse", source.get(0));
        assertEquals("Jesse", txList.get(0));
        assertEquals(3, consistencyListener.getEventCount());
    }
    
    public void testEmptyTransaction() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        source.add("James");
        assertEquals("James", txList.get(0));
        assertEquals("James", source.get(0));

        txList.begin();
        txList.commit();
        assertEquals("James", txList.get(0));
        assertEquals("James", source.get(0));

        txList.begin();
        txList.rollback();
        assertEquals("James", txList.get(0));
        assertEquals("James", source.get(0));
    }

    public void testWriteThroughTransactionList() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source);

        assertEquals(0, txList.size());

        txList.add("James");
        assertEquals(1, txList.size());
        assertEquals("James", txList.get(0));

        txList.set(0, "Jesse");
        assertEquals(1, txList.size());
        assertEquals("Jesse", txList.get(0));

        txList.remove(0);
        assertEquals(0, txList.size());
    }

    public void testCustomMergePolicy() {
        final EventList<String> source = new BasicEventList<String>();
        final TransactionList<String> txList = new TransactionList<String>(source, new MergeConflictingUpdatesPolicy());

        txList.add("James");
        txList.begin();

        txList.set(0, "Jackson");
        source.set(0, "Jesse");

        assertEquals("JesseJackson", txList.get(0));
        assertEquals("Jesse", source.get(0));
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

    public void testCommitCausesChange() {
        final EventList<String> source = new BasicEventList<String>();
        final CollectionMatcherEditor matcherEditor = new CollectionMatcherEditor();
        final FilterList<String> filtered = new FilterList<String>(source, matcherEditor);
        final TransactionList<String> txList = new TransactionList<String>(filtered);
        ListConsistencyListener<String> consistencyListener = ListConsistencyListener.install(txList);
        consistencyListener.setPreviousElementTracked(false);

        matcherEditor.setCollection(GlazedListsTests.stringToList("ABC"));
        txList.begin();
        txList.add(0, "D");
        assertEquals(GlazedListsTests.stringToList("D"), txList);

        txList.commit();
        assertEquals(Collections.EMPTY_LIST, txList);

        txList.add("A");
        assertEquals(GlazedListsTests.stringToList("A"), txList);
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

    /**
     * Read from the given List at the given index when the Runnable is executed.
     */
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

    /**
     * This Policy simply combines the updates from the source EventList and transaction.
     */
    private static class MergeConflictingUpdatesPolicy implements TransactionList.Policy<String> {
        public Result sourceDeletedTargetUpdated(String deletedFromSource, String updatedFromTarget) {
            return TransactionList.Policy.KEEP_SOURCE;
        }

        public Result sourceUpdatedTargetDeleted(String updatedFromSource, String deletedFromTarget) {
            return TransactionList.Policy.KEEP_SOURCE;
        }

        public String sourceUpdatedTargetUpdated(String updatedFromSource, String updatedFromTarget) {
            return updatedFromSource + updatedFromTarget;
        }
    }
}