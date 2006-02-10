/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Comparator;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SeparatorListTest extends TestCase {

    public void testSimpleSetup() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("AAAABBBDDD"));

        SeparatorList<String> separatorList = new SeparatorList<String>(source, (Comparator)GlazedLists.comparableComparator());
        ListConsistencyListener consistencyTest = new ListConsistencyListener(separatorList, "separatorList");

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

        source.remove(0);
        assertEquals(0, separatorList.size());
    }

    private static void assertSeparatorEquals(Object separator, int expectedSize, Object expectedFirst) {
        SeparatorList.Separator separatorAsSeparator = ((SeparatorList.Separator) separator);
        assertEquals(expectedSize, separatorAsSeparator.size());
        assertEquals(expectedFirst, separatorAsSeparator.first());
    }
}