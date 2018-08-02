package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the class {@link Guard}.
 */
public class GuardTest {

    @Test
    public void testAcceptWithWriteLock() {
        EventList<String> source = new BasicEventList<>();
        TransactionList<String> txList = new TransactionList<>(source);

        source.addAll(GlazedListsTests.stringToList("ABC"));
        assertEquals(GlazedListsTests.stringToList("ABC"), source);
        assertEquals(GlazedListsTests.stringToList("ABC"), txList);

        Guard.acceptWithWriteLock(txList, list -> {
            // 1. rollback add, remove, set as a single transaction
            txList.beginEvent();
            txList.add("D");
            txList.remove(1);
            txList.set(0, "B");
            assertEquals(GlazedListsTests.stringToList("BCD"), source);
            assertEquals(GlazedListsTests.stringToList("BCD"), txList);
            txList.rollbackEvent();
            assertEquals(GlazedListsTests.stringToList("ABC"), source);
            assertEquals(GlazedListsTests.stringToList("ABC"), txList);
        });
    }

    @Test
    public void testApplyWithWriteLock() {
        CompositeList<String> compositeList = new CompositeList<>();

        EventList<String> memberList = Guard.applyWithWriteLock(compositeList, list -> {
            final EventList<String> newMemberList = compositeList.createMemberList();
            compositeList.addMemberList(newMemberList);
            return newMemberList;
        });
        assertEquals(memberList, compositeList);
    }

    @Test
    public void testAcceptWithReadLock() {
        EventList<String> source = new BasicEventList<>();
        source.addAll(GlazedListsTests.stringToList("ABC"));
        RangeList<String> rangeList = new RangeList<>(source);
        rangeList.setHeadRange(0, 2);
        Guard.acceptWithReadLock(rangeList, list -> {
            assertEquals(0, list.getStartIndex());
            assertEquals(2, list.getEndIndex());
        });
    }

    @Test
    public void testApplyWithReadLock() {
        EventList<String> source = new BasicEventList<>();
        source.addAll(GlazedListsTests.stringToList("ABC"));
        RangeList<String> rangeList = new RangeList<>(source);
        rangeList.setHeadRange(0, 2);
        int sum = Guard.applyWithReadLock(rangeList, list -> {
            return list.getStartIndex() + list.getEndIndex();
        });
        assertEquals(2, sum);
    }

}
