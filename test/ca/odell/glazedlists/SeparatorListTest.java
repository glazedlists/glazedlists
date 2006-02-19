/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.*;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorListTest extends TestCase {

    public void testSimpleSetup() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAAABBBDDD"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator(), 0, Integer.MAX_VALUE);
        ListConsistencyListener.install(separatorList);

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


    public void testLimit() {
        EventList<String> source = new SortedList<String>(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAAAAAABBCCCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator(), 0, 3);
        ListConsistencyListener.install(separatorList);

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

    public void testMinimumSize() {
        EventList<String> source = new SortedList<String>(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator(), 2, Integer.MAX_VALUE);
        ListConsistencyListener.install(separatorList);

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

    public void testAdjustLimit() {
        EventList<String> source = new SortedList<String>(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABBBBBBBCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator(), 0, 5);
        ListConsistencyListener.install(separatorList);

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

    public void testClear() {
        EventList<String> source = new SortedList<String>(new BasicEventList<String>());
        source.addAll(GlazedListsTests.stringToList("AAABBBBBBBCCC"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator(), 0, 5);
        ListConsistencyListener.install(separatorList);

        source.clear();
        assertEquals(Collections.EMPTY_LIST, separatorList);
    }

    public void testSortedSource() {
        Comparator<String> alphabetical = (Comparator)GlazedLists.comparableComparator();
        Comparator<String> length = new StringLengthComparator();

        BasicEventList<String> unsortedSource = new BasicEventList<String>();
        SortedList<String> source = new SortedList<String>(unsortedSource, null);
        unsortedSource.addAll(Arrays.asList("apple", "banana", "cat", "dear", "frog", "boat", "car", "jesse", "glazed", "shirt", "hat", "art", "dog", "puppy", "foot"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, length, 0, Integer.MAX_VALUE);
        ListConsistencyListener.install(separatorList);


        assertEqualsIgnoreSeparators(source, separatorList, length);

        source.setComparator(alphabetical);
        assertEqualsIgnoreSeparators(source, separatorList, length);

        // now add some duplicates
        unsortedSource.addAll(Arrays.asList("apple", "banana", "cat", "art", "dog", "puppy", "foot", "carrot", "beer"));
        assertEqualsIgnoreSeparators(source, separatorList, length);

        source.setComparator(alphabetical);
        assertEqualsIgnoreSeparators(source, separatorList, length);
    }

    public void testSortSource() {
        Comparator<String> caseSensitive = (Comparator)GlazedLists.comparableComparator();
        Comparator<String> caseInsensitive = String.CASE_INSENSITIVE_ORDER;

        BasicEventList<String> unsortedSource = new BasicEventList<String>();
        SortedList<String> source = new SortedList<String>(unsortedSource, null);
        unsortedSource.addAll(GlazedListsTests.stringToList("CcaCbCcCAaADdaAbBDbBdDb"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, caseInsensitive, 0, Integer.MAX_VALUE);
        ListConsistencyListener consistencyTest = ListConsistencyListener.install(separatorList);

        assertEqualsIgnoreSeparators(source, separatorList, caseInsensitive);

        source.setComparator(caseSensitive);
        assertEquals(1, consistencyTest.getEventCount());
        assertTrue(consistencyTest.isReordering(0));


    }

    private void assertEqualsIgnoreSeparators(List separatorSource, SeparatorList separatorList, Comparator separatorComparator) {
        // create a protective copy that we can surely modify
        separatorSource = new ArrayList(separatorSource);
        Collections.sort(separatorSource, separatorComparator);
        int e = 0;
        int s = 0;

        while(true) {
            if(e == separatorSource.size() && s == separatorList.size()) break;

            Object source = separatorSource.get(e);
            Object separator = separatorList.get(s);

            // if this is a separator, make sure it's worthwhile
            if(separator instanceof SeparatorList.Separator) {
                if(s > 0) {
                    Object before = separatorList.get(s - 1);
                    Object after = separatorList.get(s + 1);
                    assertTrue(separatorComparator.compare(before, after) < 0);
                }
                s++;
                continue;
            }

            // otherwise the values should be identical
            assertSame(source, separator);
            e++;
            s++;
        }
    }

    private static class StringLengthComparator implements Comparator<String> {
        public int compare(String a, String b) {
            return a.length() - b.length();
        }
    }
}