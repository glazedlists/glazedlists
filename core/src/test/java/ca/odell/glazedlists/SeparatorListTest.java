/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.filter.StringLengthComparator;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorListTest {

    /**
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=364">Bug 364</a>
     */
    @Test
    public void testUpdateProblem() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.addAll(0, GlazedListsTests.stringToList("ABB"));
    	source.set(0, "B");
        assertSeparatorEquals(separatorList.get(0), 3, "B");

    	source.set(0, "A");
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 2, "B");
    }

    @Test
    public void testUpdate1() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("ABB"));
        source.set(0, "B");
        assertSeparatorEquals(separatorList.get(0), 3, "B");
        source.set(2, "C");
        assertSeparatorEquals(separatorList.get(0), 2, "B");
        assertSeparatorEquals(separatorList.get(3), 1, "C");
    }

    /**
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=364">Bug 364</a>
     */
    @Test
    public void testUpdateProblem2() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AACCC"));
        source.set(2, "B");
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 1, "B");
        assertSeparatorEquals(separatorList.get(5), 2, "C");
    }

    /** Change from AACCC -> AAACC. */
    @Test
    public void testUpdateBug500() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList, "SeparatorList", true);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AACCC"));
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 3, "C");
        source.set(2, "A");
        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertSeparatorEquals(separatorList.get(4), 2, "C");
    }

    /** Change from AAACC -> AACCC. */
    @Test
    public void testUpdate2Bug500() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList, "SeparatorList", true);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AAACC"));
        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertSeparatorEquals(separatorList.get(4), 2, "C");
        source.set(2, "C");
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 3, "C");
    }

    @Test
    public void testUpdate3() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AAACC"));
        source.set(2, "B");
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 1, "B");
        assertSeparatorEquals(separatorList.get(5), 2, "C");
    }


    @Test
    public void testUpdate4() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("ABB"));
        source.set(0, "B");
        assertSeparatorEquals(separatorList.get(0), 3, "B");
        source.set(0, "C");
        assertSeparatorEquals(separatorList.get(0), 2, "B");
        assertSeparatorEquals(separatorList.get(3), 1, "C");
    }
    @Test
    public void testUpdate5() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AABCC"));
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 1, "B");
        assertSeparatorEquals(separatorList.get(5), 2, "C");
        source.set(2, "A");
        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertSeparatorEquals(separatorList.get(4), 2, "C");
    }

    @Test
    public void testUpdate6() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("AABCC"));
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 1, "B");
        assertSeparatorEquals(separatorList.get(5), 2, "C");
        source.set(2, "C");
        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 3, "C");
    }

    /**
     * Update element in singleton list.
     */
    @Test
    public void testUpdateSingletonList() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("A"));
        source.set(0, "B");
        assertSeparatorEquals(separatorList.get(0), 1, "B");
        source.set(0, "A");
        assertSeparatorEquals(separatorList.get(0), 1, "A");
    }

    /**
     * Updating element leads to group removal.
     */
    @Test
    public void testUpdateElemRemoveGroup() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("ABC"));
        source.set(1, "C");
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 2, "C");
    }

    /**
     * Removing element leads to group removal.
     */
    @Test
    public void testRemoveElemRemoveGroup() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("ABBC"));
        source.remove(0);
        assertSeparatorEquals(separatorList.get(0), 2, "B");
        assertSeparatorEquals(separatorList.get(3), 1, "C");
        source.remove(2);
        assertSeparatorEquals(separatorList.get(0), 2, "B");
    }

    /**
     * Adding element leads to group addition.
     */
    @Test
    public void testAddElemAddGroup() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        source.addAll(0, GlazedListsTests.stringToList("BB"));
        assertSeparatorEquals(separatorList.get(0), 2, "B");
        source.add(0, "A");
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 2, "B");
        source.add(3, "C");
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 2, "B");
        assertSeparatorEquals(separatorList.get(5), 1, "C");
    }

    @Test
    public void testSimpleSetup() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAAABBBDDD"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertSeparatorEquals(separatorList.get(0), 4, "A");
        assertEquals("A", ((SeparatorList.Separator)(Object)separatorList.get(0)).first());
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("A", separatorList.get(4));
        assertSeparatorEquals(separatorList.get(5), 3, "B");
        assertEquals("B", separatorList.get(6));
        assertEquals("B", separatorList.get(7));
        assertEquals("B", separatorList.get(8));
        assertSeparatorEquals(separatorList.get(9), 3, "D");
        assertEquals("D", separatorList.get(10));
        assertEquals("D", separatorList.get(11));
        assertEquals("D", separatorList.get(12));
        assertEquals(13, separatorList.size());

        source.addAll(GlazedListsTests.stringToList("AAA"));
        assertSeparatorEquals(separatorList.get(0), 7, "A");
        assertEquals("A", ((SeparatorList.Separator)(Object)separatorList.get(0)).first());
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals("A", separatorList.get(6));
        assertEquals("A", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 3, "B");
        assertEquals("B", separatorList.get(9));
        assertEquals("B", separatorList.get(10));
        assertEquals("B", separatorList.get(11));
        assertSeparatorEquals(separatorList.get(12), 3, "D");
        assertEquals("D", separatorList.get(13));
        assertEquals("D", separatorList.get(14));
        assertEquals("D", separatorList.get(15));
        assertEquals(16, separatorList.size());

        source.addAll(GlazedListsTests.stringToList("BD"));
        assertSeparatorEquals(separatorList.get(0), 7, "A");
        assertEquals("A", ((SeparatorList.Separator)(Object)separatorList.get(0)).first());
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals("A", separatorList.get(6));
        assertEquals("A", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 4, "B");
        assertEquals("B", separatorList.get(9));
        assertEquals("B", separatorList.get(10));
        assertEquals("B", separatorList.get(11));
        assertEquals("B", separatorList.get(12));
        assertSeparatorEquals(separatorList.get(13), 4, "D");
        assertEquals("D", separatorList.get(14));
        assertEquals("D", separatorList.get(15));
        assertEquals("D", separatorList.get(16));
        assertEquals("D", separatorList.get(17));
        assertEquals(18, separatorList.size());

        source.addAll(GlazedListsTests.stringToList("CC"));
        assertSeparatorEquals(separatorList.get(0), 7, "A");
        assertEquals("A", ((SeparatorList.Separator)(Object)separatorList.get(0)).first());
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals("A", separatorList.get(6));
        assertEquals("A", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 4, "B");
        assertEquals("B", separatorList.get(9));
        assertEquals("B", separatorList.get(10));
        assertEquals("B", separatorList.get(11));
        assertEquals("B", separatorList.get(12));
        assertSeparatorEquals(separatorList.get(13), 2, "C");
        assertEquals("C", separatorList.get(14));
        assertEquals("C", separatorList.get(15));
        assertSeparatorEquals(separatorList.get(16), 4, "D");
        assertEquals("D", separatorList.get(17));
        assertEquals("D", separatorList.get(18));
        assertEquals("D", separatorList.get(19));
        assertEquals("D", separatorList.get(20));
        assertEquals(21, separatorList.size());

        source.removeAll(GlazedListsTests.stringToList("B"));
        assertSeparatorEquals(separatorList.get(0), 7, "A");
        assertEquals("A", ((SeparatorList.Separator)(Object)separatorList.get(0)).first());
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals("A", separatorList.get(6));
        assertEquals("A", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 2, "C");
        assertEquals("C", separatorList.get(9));
        assertEquals("C", separatorList.get(10));
        assertSeparatorEquals(separatorList.get(11), 4, "D");
        assertEquals("D", separatorList.get(12));
        assertEquals("D", separatorList.get(13));
        assertEquals("D", separatorList.get(14));
        assertEquals("D", separatorList.get(15));
        assertEquals(16, separatorList.size());

        source.removeAll(GlazedListsTests.stringToList("A"));
        assertSeparatorEquals(separatorList.get(0), 2, "C");
        assertEquals("C", separatorList.get(1));
        assertEquals("C", separatorList.get(2));
        assertSeparatorEquals(separatorList.get(3), 4, "D");
        assertEquals("D", separatorList.get(4));
        assertEquals("D", separatorList.get(5));
        assertEquals("D", separatorList.get(6));
        assertEquals("D", separatorList.get(7));
        assertEquals(8, separatorList.size());

        source.removeAll(GlazedListsTests.stringToList("D"));
        assertSeparatorEquals(separatorList.get(0), 2, "C");
        assertEquals("C", separatorList.get(1));
        assertEquals("C", separatorList.get(2));
        assertEquals(3, separatorList.size());

        source.remove(0);
        assertSeparatorEquals(separatorList.get(0), 1, "C");
        assertEquals("C", separatorList.get(1));
        assertEquals(2, separatorList.size());

        // the following test line is known to fail, we need to fix SeparatorList
        source.remove(0);
        assertEquals(0, separatorList.size());
    }

    private static void assertSeparatorEquals(Object separator, int expectedSize, Object expectedFirst) {
        SeparatorList.Separator separatorAsSeparator = ((SeparatorList.Separator) separator);
        assertEquals(expectedSize, separatorAsSeparator.size());
        assertEquals(expectedFirst, separatorAsSeparator.first());
    }


    @Test
    public void testLimit() {
        EventList<String> source = SortedList.create(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAAAAAABBCCCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, 3);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertSeparatorEquals(separatorList.get(0), 7, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertSeparatorEquals(separatorList.get(4), 2, "B");
        assertEquals("B", separatorList.get(5));
        assertEquals("B", separatorList.get(6));
        assertSeparatorEquals(separatorList.get(7), 5, "C");
        assertEquals("C", separatorList.get(8));
        assertEquals("C", separatorList.get(9));
        assertEquals("C", separatorList.get(10));
        assertEquals(11, separatorList.size());

        source.addAll(GlazedListsTests.stringToList("ABBB"));
        assertSeparatorEquals(separatorList.get(0), 8, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertSeparatorEquals(separatorList.get(4), 5, "B");
        assertEquals("B", separatorList.get(5));
        assertEquals("B", separatorList.get(6));
        assertEquals("B", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 5, "C");
        assertEquals("C", separatorList.get(9));
        assertEquals("C", separatorList.get(10));
        assertEquals("C", separatorList.get(11));
        assertEquals(12, separatorList.size());

        source.subList(5, 7).clear();
        assertSeparatorEquals(separatorList.get(0), 6, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertSeparatorEquals(separatorList.get(4), 5, "B");
        assertEquals("B", separatorList.get(5));
        assertEquals("B", separatorList.get(6));
        assertEquals("B", separatorList.get(7));
        assertSeparatorEquals(separatorList.get(8), 5, "C");
        assertEquals("C", separatorList.get(9));
        assertEquals("C", separatorList.get(10));
        assertEquals("C", separatorList.get(11));
        assertEquals(12, separatorList.size());

        source.subList(0, 5).clear();
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertEquals("A", separatorList.get(1));
        assertSeparatorEquals(separatorList.get(2), 5, "B");
        assertEquals("B", separatorList.get(3));
        assertEquals("B", separatorList.get(4));
        assertEquals("B", separatorList.get(5));
        assertSeparatorEquals(separatorList.get(6), 5, "C");
        assertEquals("C", separatorList.get(7));
        assertEquals("C", separatorList.get(8));
        assertEquals("C", separatorList.get(9));
        assertEquals(10, separatorList.size());

        source.subList(0, 6).clear();
        assertSeparatorEquals(separatorList.get(0), 5, "C");
        assertEquals("C", separatorList.get(1));
        assertEquals("C", separatorList.get(2));
        assertEquals("C", separatorList.get(3));
        assertEquals(4, separatorList.size());


    }

    @Test
    public void testMinimumSize() {
        EventList<String> source = SortedList.create(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 2, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertEquals("B", separatorList.get(4));
        assertSeparatorEquals(separatorList.get(5), 3, "C");
        assertEquals("C", separatorList.get(6));
        assertEquals("C", separatorList.get(7));
        assertEquals("C", separatorList.get(8));
        assertEquals(9, separatorList.size());
    }

    @Test
    public void testAdjustLimit() {
        EventList<String> source = SortedList.create(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABBBBBBBCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, 5);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertSeparatorEquals(separatorList.get(4), 7, "B");
        assertEquals("B", separatorList.get(5));
        assertEquals("B", separatorList.get(6));
        assertEquals("B", separatorList.get(7));
        assertEquals("B", separatorList.get(8));
        assertEquals("B", separatorList.get(9));
        assertSeparatorEquals(separatorList.get(10), 3, "C");
        assertEquals("C", separatorList.get(11));
        assertEquals("C", separatorList.get(12));
        assertEquals("C", separatorList.get(13));
        assertEquals(14, separatorList.size());

        SeparatorList.Separator bSeparator = (SeparatorList.Separator)(Object)separatorList.get(4);
        bSeparator.setLimit(0);

        assertSeparatorEquals(separatorList.get(0), 3, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertEquals("A", separatorList.get(3));
        assertSeparatorEquals(separatorList.get(4), 7, "B");
        assertSeparatorEquals(separatorList.get(5), 3, "C");
        assertEquals("C", separatorList.get(6));
        assertEquals("C", separatorList.get(7));
        assertEquals("C", separatorList.get(8));
        assertEquals(9, separatorList.size());
    }

    @Test
    public void testClear() {
        EventList<String> source = SortedList.create(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABBBBBBBCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, 5);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.clear();
        assertEquals(Collections.EMPTY_LIST, separatorList);
    }

    @Test
    public void testSortedSource() {
        Comparator<String> alphabetical = GlazedLists.comparableComparator();
        Comparator<String> length = GlazedLists.reverseComparator(new StringLengthComparator());

        BasicEventList<String> unsortedSource = new BasicEventList<String>();
        SortedList<String> source = new SortedList<String>(unsortedSource, null);
        unsortedSource.addAll(Arrays.asList("apple", "banana", "cat", "dear", "frog", "boat", "car", "jesse", "glazed", "shirt", "hat", "art", "dog", "puppy", "foot"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, length, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEqualsIgnoreSeparators(source, separatorList, length);

        source.setComparator(alphabetical);
        assertEqualsIgnoreSeparators(source, separatorList, length);

        // now add some duplicates
        unsortedSource.addAll(Arrays.asList("apple", "banana", "cat", "art", "dog", "puppy", "foot", "carrot", "beer"));
        assertEqualsIgnoreSeparators(source, separatorList, length);

        source.setComparator(alphabetical);
        assertEqualsIgnoreSeparators(source, separatorList, length);
    }

    /**
     * Make sure the SeparatorList handles sortings of the source list correctly,
     * by reflecting the new elements in that list.
     */
    @Test
    public void testSortSource() {
        Comparator<String> caseSensitive = GlazedLists.comparableComparator();
        Comparator<String> caseInsensitive = String.CASE_INSENSITIVE_ORDER;
        Random dice = new Random(0);
        int expectedEventCount = 0;

        BasicEventList<String> unsortedSource = new BasicEventList<String>();
        SortedList<String> source = new SortedList<String>(unsortedSource, null);
        unsortedSource.addAll(GlazedListsTests.stringToList("CcaCbCcCAaADdaAbBDbBdDb"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, caseInsensitive, 0, Integer.MAX_VALUE);
        ListConsistencyListener consistencyTest = ListConsistencyListener.install(separatorList);
        consistencyTest.setPreviousElementTracked(false);

        assertEqualsIgnoreSeparators(source, separatorList, caseInsensitive);

        source.setComparator(caseSensitive);
        expectedEventCount++;
        assertEquals(expectedEventCount, consistencyTest.getEventCount());
        assertTrue(consistencyTest.isReordering(0));

        // collapse some
        for(int i = 0; i < separatorList.size(); i++) {
            Object value = separatorList.get(i);
            if(!(value instanceof SeparatorList.Separator)) {
                continue;
            }
            SeparatorList.Separator separator = (SeparatorList.Separator)value;
            if(dice.nextBoolean()) {
                separator.setLimit(0);
                expectedEventCount++;
                assertEquals(expectedEventCount, consistencyTest.getEventCount());
            }
        }

        // do some more reorderings: to null comparator
        source.setComparator(null);
        expectedEventCount++;
        assertEquals(expectedEventCount, consistencyTest.getEventCount());
        assertTrue(consistencyTest.isReordering(expectedEventCount - 1));

        // do some more reorderings: back to case sensitive comparator
        source.setComparator(caseSensitive);
        expectedEventCount++;
        assertEquals(expectedEventCount, consistencyTest.getEventCount());
        assertTrue(consistencyTest.isReordering(expectedEventCount - 1));
    }

    /**
     * See what happens when we filter out separators.
     */
    @Test
    public void testRemoveInsertSeparators() {
        BasicEventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AABBBCCCCDDDDEFGHHHH"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        source.removeAll(GlazedListsTests.stringToList("BC"));
        source.removeAll(GlazedListsTests.stringToList("H"));
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        source.addAll(2, GlazedListsTests.stringToList("BBBBCCC"));
        source.addAll(source.size(), GlazedListsTests.stringToList("HHHH"));
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        collapseThenExpandAllSeparators(source, separatorList);

        // remove the 'D' element slowly
        source.remove(12);
        source.remove(9);
        source.remove(10);
        source.remove(9);
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        // remove the 'F' and 'E' elements slowly
        source.remove(10);
        source.remove(9);
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        // restore the 'F' and 'E' elements
        source.add(9, "F");
        source.add(9, "E");
        collapseThenExpandAllSeparators(source, separatorList);

        // do removes while "H" is collapsed
        ((SeparatorList.Separator)(Object)separatorList.get(18)).setLimit(0);
        source.remove(12);
        source.remove(14);
        source.remove(12);
        ((SeparatorList.Separator)(Object)separatorList.get(18)).setLimit(Integer.MAX_VALUE);
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        source.add(12, "H");
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());
    }

    @Test
    public void testSeparatorIsThreadSafe() throws InterruptedException {
        final BasicEventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AABBBCCCCDDDDEFGHHHH"));

        final SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 0, Integer.MAX_VALUE);
        ListConsistencyListener listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        SeparatorList.Separator separator = (SeparatorList.Separator)(Object)separatorList.get(0);
        assertEquals("A", separator.first());
        assertEquals(2, separator.size());

        source.clear();
        assertEquals(null, separator.first());
        assertEquals(0, separator.size());
    }

    /**
     * Test the separator list by collapsing and expanding the separators within.
     */
    private void collapseThenExpandAllSeparators(EventList source, SeparatorList separatorList) {
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());

        // collapse all
        for(int i = 0; i < separatorList.size(); i++) {
            SeparatorList.Separator separator = (SeparatorList.Separator)separatorList.get(i);
            separator.setLimit(0);
        }
        // expand all
        for(int i = 0; i < separatorList.size(); i++) {
            Object value = separatorList.get(i);
            if(!(value instanceof SeparatorList.Separator)) {
                continue;
            }
            SeparatorList.Separator separator = (SeparatorList.Separator)value;
            separator.setLimit(Integer.MAX_VALUE);
        }
        assertEqualsIgnoreSeparators(source, separatorList, GlazedLists.comparableComparator());
    }

    /**
     * Make sure that the {@link SeparatorList#setComparator} works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=328">Issue 328</a>
     */
    @Test
    public void testSetComparator() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAaaaBBBBbCCCddd"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, GlazedLists.caseInsensitiveComparator(), 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        // try with the first comparator
        assertSeparatorEquals(separatorList.get(0),  5, "A");
        assertSeparatorEquals(separatorList.get(6),  5, "B");
        assertSeparatorEquals(separatorList.get(12), 3, "C");
        assertSeparatorEquals(separatorList.get(16), 3, "d");
        assertEquals(20, separatorList.size());

        // change the comparator
        separatorList.setComparator(new CaseComparator());
        assertSeparatorEquals(separatorList.get(0),  7, "a");
        assertSeparatorEquals(separatorList.get(8),  9, "A");
        assertEquals(18, separatorList.size());

        // make list changes
        source.addAll(5, GlazedListsTests.stringToList("aaaA"));
        assertSeparatorEquals(separatorList.get(0),  10, "a");
        assertSeparatorEquals(separatorList.get(11),  10, "A");
        assertEquals(22, separatorList.size());

        // change the comparator again
        separatorList.setComparator(GlazedLists.caseInsensitiveComparator());
        assertSeparatorEquals(separatorList.get(0),  9, "A");
        assertSeparatorEquals(separatorList.get(10),  5, "B");
        assertSeparatorEquals(separatorList.get(16), 3, "C");
        assertSeparatorEquals(separatorList.get(20), 3, "d");
        assertEquals(24, separatorList.size());
    }

    /**
     * Make sure that the {@link SeparatorList#setComparator} works with limits set.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=453">Issue 453</a>
     */
    @Test
    public void testSetComparatorWithLimit() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAaaaBBBBbCCCddd"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source,
                GlazedLists.caseInsensitiveComparator(), 0, 2);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener
                .install(separatorList);
        listConsistencyListener.setPreviousElementTracked(false);

        // try with the first comparator
        assertSeparatorEquals(separatorList.get(0), 5, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertSeparatorEquals(separatorList.get(3), 5, "B");
        assertEquals("B", separatorList.get(4));
        assertEquals("B", separatorList.get(5));
        assertSeparatorEquals(separatorList.get(6), 3, "C");
        assertEquals("C", separatorList.get(7));
        assertEquals("C", separatorList.get(8));
        assertSeparatorEquals(separatorList.get(9), 3, "d");
        assertEquals("d", separatorList.get(10));
        assertEquals("d", separatorList.get(11));
        assertEquals(12, separatorList.size());

        separatorList.setComparator(new CaseComparator());
        assertSeparatorEquals(separatorList.get(0), 7, "a");
        assertEquals("a", separatorList.get(1));
        assertEquals("a", separatorList.get(2));
        assertSeparatorEquals(separatorList.get(3), 9, "A");
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals(6, separatorList.size());

        // make list changes
        source.addAll(5, GlazedListsTests.stringToList("aaaA"));
        assertSeparatorEquals(separatorList.get(0), 10, "a");
        assertEquals("a", separatorList.get(1));
        assertEquals("a", separatorList.get(2));
        assertSeparatorEquals(separatorList.get(3), 10, "A");
        assertEquals("A", separatorList.get(4));
        assertEquals("A", separatorList.get(5));
        assertEquals(6, separatorList.size());
        // change the comparator again
        separatorList.setComparator(GlazedLists.caseInsensitiveComparator());
        assertSeparatorEquals(separatorList.get(0), 9, "A");
        assertEquals("A", separatorList.get(1));
        assertEquals("A", separatorList.get(2));
        assertSeparatorEquals(separatorList.get(3), 5, "B");
        assertEquals("B", separatorList.get(4));
        assertEquals("B", separatorList.get(5));
        assertSeparatorEquals(separatorList.get(6), 3, "C");
        assertEquals("C", separatorList.get(7));
        assertEquals("C", separatorList.get(8));
        assertSeparatorEquals(separatorList.get(9), 3, "d");
        assertEquals("d", separatorList.get(10));
        assertEquals("d", separatorList.get(11));
        assertEquals(12, separatorList.size());
    }

    private void assertEqualsIgnoreSeparators(List source, SeparatorList separatorList, Comparator separatorComparator) {
        // create a protective copy that we can surely modify
        source = new ArrayList(source);
        Collections.sort(source, separatorComparator);
        int e = 0;
        int s = 0;

        while(true) {
            if(e == source.size() && s == separatorList.size()) {
                break;
            }

            Object sourceValue = source.get(e);
            Object separatorValue = separatorList.get(s);

            // if this is a separator, make sure it's worthwhile
            if(separatorValue instanceof SeparatorList.Separator) {
                SeparatorList.Separator separator = (SeparatorList.Separator)separatorValue;
                int separatorSize = separator.size();
                assertTrue(separatorSize > 0);
                assertTrue(separatorSize <= source.size());
                Object first = separator.first();
                assertSame(first, sourceValue);
                if(s > 0) {
                    Object before = separatorList.get(s - 1);
                    Object after = separatorList.get(s + 1);
                    assertTrue("Misplaced separator in " + separatorList.toString(), separatorComparator.compare(before, after) < 0);
                }
                s++;
                continue;
            }

            // otherwise the values should be identical
            assertSame(sourceValue, separatorValue);
            e++;
            s++;
        }
    }

    private static class CaseComparator implements Comparator<String> {
        @Override
        public int compare(String a, String b) {
            boolean aIsUpperCase = Character.isUpperCase(a.charAt(0));
            boolean bIsUpperCase = Character.isUpperCase(b.charAt(0));
            return (bIsUpperCase == aIsUpperCase ? 0 : (aIsUpperCase ? 1 : -1));
        }
    }

    /**
     * Test that certain weird changes are handled correctly by the
     * {@link SeparatorList}.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=322">Issue 322</a>
     */
    @Test
    public void testHandleChange() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        FilterList<String> filtered = new FilterList<String>(source);
        SeparatorList<String> separated = new SeparatorList<String>(filtered, GlazedLists.comparableComparator(), 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separated);
        listConsistencyListener.setPreviousElementTracked(false);

        // test clearing the separatorlist
        source.addAll(GlazedListsTests.stringToList("AAA"));
        source.clear();

        source.addAll(GlazedListsTests.stringToList("ABBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBCCCCCCDEFFFFFFFFFGGGGGGGGGGGGGGGGGHHHHHHHHHHHHHHHHHHHH"));
        filtered.setMatcher(new SubstringMatcher("BCDEFGH"));

        assertEquals(94, separated.size());
        filtered.setMatcher(new SubstringMatcher("A"));
        assertEquals(2, separated.size());
        filtered.setMatcher(new SubstringMatcher("ABCDEFGH"));
        assertEquals(96, separated.size());
        filtered.setMatcher(new SubstringMatcher("AEFGH"));
        assertEquals(53, separated.size());
        filtered.setMatcher(new SubstringMatcher("ABCDEFGH"));
        filtered.setMatcher(new SubstringMatcher("ABCDEFG"));
        filtered.setMatcher(new SubstringMatcher("ABCD"));
        assertEquals(45, separated.size());
        filtered.setMatcher(new SubstringMatcher("BD"));
        assertEquals(36, separated.size());

        filtered.setMatcher(Matchers.trueMatcher());

        source.clear();
        source.addAll(GlazedListsTests.stringToList("CCS"));

        source.beginEvent();
        source.addAll(0, GlazedListsTests.stringToList("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCDDDDDDFNSSSSSSS"));
        source.remove(48);
        source.remove(48);
        source.addAll(49, GlazedListsTests.stringToList("STTTTTTTTTTTTTTTTTWWWWWWWWWWWWWWWWWWWW"));
        source.commitEvent();

        // adjust using an event known to put the separator in the wrong place
        source.clear();
        source.addAll(0, GlazedListsTests.stringToList("CSC"));
        source.beginEvent();
        source.addAll(0, GlazedListsTests.stringToList("WWWWCWWWWWWWWWWWWFSCCWSCTTTTCCSTTWTCSSCDTSWNSCCDDTCDCCDCTCTDCTCTCCCCCCWCCCCCC"));
        source.remove(77);
        source.remove(78);
        source.addAll(0, GlazedListsTests.stringToList("TSTTCCCCT"));
        source.commitEvent();
        assertEqualsIgnoreSeparators(source, separated, GlazedLists.comparableComparator());
    }

    /**
     * A mechanically simplified version of {@link #testHandleChange()}.
     */
    @Test
    public void testHandleChangeSimplified() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        SeparatorList<String> separated = new SeparatorList<String>(source, GlazedLists.comparableComparator(), 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separated);
        listConsistencyListener.setPreviousElementTracked(false);

        // adjust using an event known to put the separator in the wrong place
        source.addAll(0, GlazedListsTests.stringToList("CSC"));
        source.beginEvent();
        source.remove(0);
        source.remove(1);
        source.addAll(0, GlazedListsTests.stringToList("SC"));
        source.commitEvent();
        assertEqualsIgnoreSeparators(source, separated, GlazedLists.comparableComparator());
    }

    /**
     * Match strings that are a substring of the specified String.
     */
    private static class SubstringMatcher implements Matcher<String> {
        private String enclosing;
        public SubstringMatcher(String enclosing) {
            this.enclosing = enclosing;
        }
        @Override
        public boolean matches(String contained) {
            return enclosing.indexOf(contained) != -1;
        }
    }

    /**
     * Make sure that SeparatorList handles update events properly.
     */
    @Test
    public void testHandleUpdateEvents() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separated = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);
        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(separated);
        listConsistencyListener.setPreviousElementTracked(false);

        // adjust using an event known to put the separator in the wrong place
        source.addAll(0, GlazedListsTests.stringToList("AAAA"));
        assertEquals(listConsistencyListener.getEventCount(), 1);
        source.set(3, "a");
        assertEquals(listConsistencyListener.getEventCount(), 2);

        // collapse the group, update events should still cause updates
        ((SeparatorList.Separator)(Object)separated.get(0)).setLimit(0);
        assertEquals(listConsistencyListener.getEventCount(), 3);
        assertEquals(1, separated.size());
        source.set(2, "a");
        assertEquals(listConsistencyListener.getEventCount(), 4);
        assertEquals(1, separated.size());
    }

    @Test
    public void testSet() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separated = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);

        source.addAll(GlazedListsTests.stringToList("ABCABCABC"));
        assertEquals(9, source.size());
        assertEquals(12, separated.size());

        // modify the first item in a group
        assertEquals("A", source.get(0));
        assertEquals("A", separated.get(1));
        separated.set(1, "D");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // modify the middle item in a group
        assertEquals("B", source.get(4));
        assertEquals("B", separated.get(5));
        separated.set(5, "E");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // modify the last item in a group
        assertEquals("C", source.get(8));
        assertEquals("C", separated.get(9));
        separated.set(9, "F");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // try to modify a Separator which is disallowed
        try {
            separated.set(0, "This will fail");
            fail("failed to receive an IllegalArgumentException when accessing an index that stores a separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testAdd() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separated = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);

        source.addAll(GlazedListsTests.stringToList("ABCABCABC"));
        assertEquals(9, source.size());
        assertEquals(12, separated.size());

        // add a new first item in a group
        assertEquals("A", source.get(0));
        assertEquals("A", separated.get(1));
        separated.add(1, "A");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // add a new middle item in a group
        assertEquals("B", source.get(5));
        assertEquals("B", separated.get(7));
        separated.add(7, "B");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // add the last item in a group
        assertEquals("C", source.get(10));
        assertEquals("C", separated.get(13));
        separated.add(13, "C");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // add the last item in a group
        assertEquals("C", source.get(11));
        assertEquals("C", separated.get(14));
        separated.add("C");
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // try to modify a Separator which is disallowed
        try {
            separated.add(0, "This should fail");
            fail("failed to receive an IllegalArgumentException when accessing an index that stores a separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRemove() {
        EventList<String> source = new BasicEventList<String>();
        SeparatorList<String> separated = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 1, Integer.MAX_VALUE);

        source.addAll(GlazedListsTests.stringToList("ABCABCABC"));
        assertEquals(9, source.size());
        assertEquals(12, separated.size());

        // remove the first item in a group
        assertEquals("A", source.get(0));
        assertEquals("A", separated.get(1));
        separated.remove(1);
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // remove the middle item in a group
        assertEquals("B", source.get(3));
        assertEquals("B", separated.get(5));
        separated.remove(5);
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // remove the last item in a group
        assertEquals("C", source.get(6));
        assertEquals("C", separated.get(9));
        separated.remove(9);
        assertEqualsIgnoreSeparators(source, separated, String.CASE_INSENSITIVE_ORDER);

        // try to modify a Separator which is disallowed
        try {
            separated.remove(0);
            fail("failed to receive an IllegalArgumentException when accessing an index that stores a separator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Make sure that SeparatorInjectorList.Group.Comparator stays in sync with SortedList.Comparator when the EventList
     * contains no objects.
     */
    @Test
    public void testSetComparatorTwice() {
        final EventList<Object> source = new BasicEventList<Object>();
        final SeparatorList<Object> separated = new SeparatorList<Object>(source, GlazedListsImpl.equalsComparator(), 0, 0);
        separated.setComparator(GlazedListsImpl.equalsComparator());

        final Object o = new Object();
        source.add(o);
        source.add(o);

        assertEquals(2, source.size());
        assertEquals(1, separated.size());
        assertEquals(2, ((SeparatorList.Separator) separated.get(0)).size());
    }

    @Test
    public void testIssue499()
   {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, comparator, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        String[] s = new String[] { "MSFT", "MSFT", "IBM", "C", "IBM", "C",
                "C", "IBM", "IBM", "C" };
        String[] upd = new String[] { "MSFT", "MSFT", "MSFT", "C", "C", "IBM",
                "IBM", "IBM", "C", "MSFT" };

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }

        assertSeparatorEquals(separatorList.get(0), 4, "C");
        assertSeparatorEquals(separatorList.get(5), 4, "IBM");
        assertSeparatorEquals(separatorList.get(10), 2, "MSFT");

        source.beginEvent();
        for (int i = 0; i < upd.length; i++) {
            source.set(i, upd[i]);
        }
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 3, "C");
        assertSeparatorEquals(separatorList.get(4), 3, "IBM");
        assertSeparatorEquals(separatorList.get(8), 4, "MSFT");
    }

    @Test
    public void testSplitFirstGroup() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, comparator, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("C");
        source.add("CC");
        source.add("CCC");

        assertSeparatorEquals(separatorList.get(0), 3, "C");

        source.beginEvent();
        source.set(0, "A");
        source.set(1, "B");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 1, "CCC");
    }

    @Test
    public void testJoinToFirstGroup() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, comparator, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("A");
        source.add("B");
        source.add("CCC");

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 1, "CCC");

        source.beginEvent();
        source.set(0, "C");
        source.set(1, "CC");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 3, "C");
    }

    @Test
    public void testMultipleUpdateDeleteSeparatorList() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, comparator, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        String[] s = new String[] {"C", "CC", "D", "DD", "E", "EE"};

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }

        assertSeparatorEquals(separatorList.get(0), 2, "C");
        assertSeparatorEquals(separatorList.get(3), 2, "D");
        assertSeparatorEquals(separatorList.get(6), 2, "E");

        source.beginEvent();
        source.set(0, "C");
        source.remove(1);
        source.set(1, "D");
        source.remove(2);
        source.set(2, "E");
        source.remove(3);
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "C");
        assertSeparatorEquals(separatorList.get(2), 1, "D");
        assertSeparatorEquals(separatorList.get(4), 1, "E");
    }

    @Test
    public void testIssue522() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        SeparatorList<String> separatorList = new SeparatorList<String>(source, comparator, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        String[] s = new String[] {"A", "B", "C", "D", "DD", "DDD", "E", "F", "FF"};

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 1, "C");
        assertSeparatorEquals(separatorList.get(6), 3, "D");
        assertSeparatorEquals(separatorList.get(10), 1, "E");
        assertSeparatorEquals(separatorList.get(12), 2, "F");

        source.beginEvent();
        source.set(7, "F");
        source.remove(8);
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 1, "C");
        assertSeparatorEquals(separatorList.get(6), 3, "D");
        assertSeparatorEquals(separatorList.get(10), 1, "E");
        assertSeparatorEquals(separatorList.get(12), 1, "F");
    }

    @Test
    public void testTwoGroupsToOne() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("A");
        source.add("B");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 2, "A");
        assertSeparatorEquals(separatorList.get(3), 1, "B");

        source.beginEvent();
        source.set(0, "B");
        source.set(1, "B");
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, "B");
    }

    @Test
    public void testFourGroupsToOne() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("D");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 1, "C");
        assertSeparatorEquals(separatorList.get(6), 1, "D");

        source.beginEvent();
        source.set(0, "D");
        source.set(1, "D");
        source.set(2, "D");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 4, "D");
    }

    @Test
    public void testMultipleUpdatesCreateGroupOnStartOfPrev() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("D");
        source.add("D");
        source.add("D");
        source.add("E");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 3, "C");
        assertSeparatorEquals(separatorList.get(6), 3, "D");
        assertSeparatorEquals(separatorList.get(10), 1, "E");

        source.beginEvent();
        source.set(1, "B");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 1, "B");
        assertSeparatorEquals(separatorList.get(4), 2, "C");
        assertSeparatorEquals(separatorList.get(7), 3, "D");
        assertSeparatorEquals(separatorList.get(11), 1, "E");
    }

    @Test
    public void testMultipleUpdatesCreateGroupOnDuplicateOfPrev() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        SeparatorList<String> separatorList = new SeparatorList<String>(source, String.CASE_INSENSITIVE_ORDER, 0, Integer.MAX_VALUE);
        ListConsistencyListener<String> listener = ListConsistencyListener.install(separatorList, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("D");
        source.add("D");
        source.add("D");
        source.add("E");

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 4, "C");
        assertSeparatorEquals(separatorList.get(7), 3, "D");
        assertSeparatorEquals(separatorList.get(11), 1, "E");


        source.beginEvent();
        source.set(2, "B");
        source.set(3, "B");
        source.commitEvent();

        assertSeparatorEquals(separatorList.get(0), 1, "A");
        assertSeparatorEquals(separatorList.get(2), 2, "B");
        assertSeparatorEquals(separatorList.get(5), 2, "C");
        assertSeparatorEquals(separatorList.get(8), 3, "D");
        assertSeparatorEquals(separatorList.get(12), 1, "E");
    }

    @Test
    public void testInsertBugFix() {
        TransactionList<Element> source = createElementSource();
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(1, 4));
        source.set(7, new Element(1, 8));
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 5, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(6), 3, new Element(2, 2));
        assertSeparatorEquals(separatorList.get(10), 4, new Element(3, 3));
    }

    @Test
    public void testDeleteBugFix() {
        TransactionList<Element> source = createElementSource();
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(1, 4));
        source.set(9, new Element(3, 10));
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(4), 4, new Element(2, 2));
        assertSeparatorEquals(separatorList.get(9), 5, new Element(3, 3));
    }

    @Test
    public void testSortingBug() {
        TransactionList<Element> source = createElementSource();
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(10, new Element(1, 11));
        source.set(1, new Element(3, 2));
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 5, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(6), 2, new Element(2, 5));
        assertSeparatorEquals(separatorList.get(9), 5, new Element(3, 2));
    }

    @Test
    public void testSortingBug2() {
        TransactionList<Element> source = createElementSource();
        source.set(2, new Element(2, 3));
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(10, new Element(1, 11));
        source.set(1, new Element(3, 2));
        source.set(2, new Element(3, 3));
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 5, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(6), 2, new Element(2, 5));
        assertSeparatorEquals(separatorList.get(9), 5, new Element(3, 2));
    }

    @Test
    public void testUpdateDeleteBug() {
        TransactionList<Element> source = createUpdateDeleteElementSource();
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(2, 4));
        source.remove(4);
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(4), 1, new Element(2, 4));
        assertSeparatorEquals(separatorList.get(6), 2, new Element(3, 6));
    }

    @Test
    public void testUpdateDeleteBug2() {
        TransactionList<Element> source = createUpdateDeleteElementSource();
        source.add(new Element(2, 6));
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(2, 4));
        source.remove(5);
        source.remove(4);
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(4), 2, new Element(2, 4));
        assertSeparatorEquals(separatorList.get(7), 1, new Element(3, 7));
    }

    @Test
    public void testUpdateDeleteBug3() {
        TransactionList<Element> source = createUpdateDeleteElementSource();
        source.remove(6);
        source.set(5, new Element(2, 6));
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(2, 4));
        source.remove(5);
        source.remove(4);
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(4), 1, new Element(2, 4));
    }

    @Test
    public void testUpdateDeleteBug4() {
        TransactionList<Element> source = createUpdateDeleteElementSource();
        source.remove(6);
        SeparatorList<Element> separatorList = new SeparatorList<Element>(source, elementComparator(), 0, Integer.MAX_VALUE);

        source.beginEvent();
        source.set(3, new Element(2, 4));
        source.remove(5);
        source.remove(4);
        source.commitEvent();
        assertSeparatorEquals(separatorList.get(0), 3, new Element(1, 1));
        assertSeparatorEquals(separatorList.get(4), 1, new Element(2, 4));
    }

    private TransactionList<Element> createElementSource() {
        TransactionList<Element> source = new TransactionList<Element>(new BasicEventList<Element>());
        source.add(new Element(1, 1));
        source.add(new Element(2, 2));
        source.add(new Element(3, 3));
        source.add(new Element(1, 4));
        source.add(new Element(2, 5));
        source.add(new Element(3, 6));
        source.add(new Element(1, 7));
        source.add(new Element(2, 8));
        source.add(new Element(3, 9));
        source.add(new Element(1, 10));
        source.add(new Element(2, 11));
        source.add(new Element(3, 12));
        return source;
    }

    private TransactionList<Element> createUpdateDeleteElementSource() {
        TransactionList<Element> source = new TransactionList<Element>(new BasicEventList<Element>());
        source.add(new Element(1, 1));
        source.add(new Element(1, 2));
        source.add(new Element(1, 3));
        source.add(new Element(2, 4));
        source.add(new Element(2, 5));
        source.add(new Element(3, 6));
        source.add(new Element(3, 7));
        return source;
    }

    private Comparator<Element> elementComparator() {
        return new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                int x = o1.getGroup();
                int y = o2.getGroup();
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        };
    }

    private class Element {
        private final int group;
        private final int id;

        public Element(int group, int id) {
            this.group = group;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Element that = (Element) o;

            return id == that.id;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "group=" + group +
                    ", id=" + id +
                    '}';
        }

        @Override
        public int hashCode() {
            return id;
        }

        public int getGroup() {
            return group;
        }
    }
}
