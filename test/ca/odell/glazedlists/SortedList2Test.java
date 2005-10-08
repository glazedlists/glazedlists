/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.Comparator;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SortedList2Test extends TestCase {

    /**
     * Test that {@link PassiveSorting} handles it's source changing
     * underneath it.
     */
    public void testSourceChanges() {
        EventList<String> source = new BasicEventList<String>();
        SortedList<String> sorted = new SortedList<String>(source, false);
        sorted.addListEventListener(new ListConsistencyListener(sorted, "sorted", false));

        source.add("J");
        assertEquals(1, sorted.size());
        assertEquals("J", sorted.get(0));

        source.addAll(1, GlazedListsTests.stringToList("ESSE"));
        assertEquals(GlazedListsTests.stringToList("JESSE"), sorted);

        source.remove(2);
        assertEquals(GlazedListsTests.stringToList("JESE"), sorted);

        source.set(0, "M");
        assertEquals(GlazedListsTests.stringToList("MESE"), sorted);
    }

    /**
     * Test that {@link PassiveSorting} handles it's source changing
     * underneath it.
     */
    public void testSourceChangesMore() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("JESSE"));
        SortedList<String> sorted = new SortedList<String>(source, false);
        sorted.addListEventListener(new ListConsistencyListener(sorted, "sorted", false));

        source.add("N");
        assertEquals(6, sorted.size());
        assertEquals("N", sorted.get(5));
        assertEquals("S", sorted.get(2));

        source.addAll(5, GlazedListsTests.stringToList("WILSO"));
        assertEquals(GlazedListsTests.stringToList("JESSEWILSON"), sorted);

        source.remove(2);
        assertEquals(GlazedListsTests.stringToList("JESEWILSON"), sorted);

        source.set(0, "M");
        assertEquals(GlazedListsTests.stringToList("MESEWILSON"), sorted);
    }

    /**
     * Test that {@link PassiveSorting} handles it's source changing
     * underneath it.
     */
    public void testSorting() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("FCDABE"));
        SortedList<String> sorted = new SortedList<String>(source, false);
        sorted.addListEventListener(new ListConsistencyListener(sorted, "sorted", false));

        sorted.setComparator((Comparator)GlazedLists.comparableComparator());
        assertEquals(GlazedListsTests.stringToList("ABCDEF"), sorted);

        source.add(3, "G");
        assertEquals(GlazedListsTests.stringToList("FCDGABE"), source);
        assertEquals(GlazedListsTests.stringToList("ABCDEFG"), sorted);

        source.remove(2);
        assertEquals(GlazedListsTests.stringToList("FCGABE"), source);
        assertEquals(GlazedListsTests.stringToList("ABCEFG"), sorted);

        source.add(4, "H");
        assertEquals(GlazedListsTests.stringToList("FCGAHBE"), source);
        assertEquals(GlazedListsTests.stringToList("ABCEFGH"), sorted);

        source.add(1, "I");
        assertEquals(GlazedListsTests.stringToList("FICGAHBE"), source);
        assertEquals(GlazedListsTests.stringToList("ABCEFIGH"), sorted);

        source.add(3, "J");
        assertEquals(GlazedListsTests.stringToList("FICJGAHBE"), source);
        assertEquals(GlazedListsTests.stringToList("ABCEFIJGH"), sorted);

        sorted.setComparator((Comparator)GlazedLists.comparableComparator());
        assertEquals(GlazedListsTests.stringToList("ABCEFGHIJ"), sorted);
    }

    /**
     * Test that {@link PassiveSorting} handles it's source changing
     * underneath it.
     */
    public void testInsertInMiddle() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("ABCDEF"));
        SortedList<String> sorted = new SortedList<String>(source, false);
        sorted.addListEventListener(new ListConsistencyListener(sorted, "sorted", false));

        assertEquals(GlazedListsTests.stringToList("ABCDEF"), sorted);

        source.add(3, "G");
        assertEquals(GlazedListsTests.stringToList("ABCGDEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABCDEFG"), sorted);

        source.remove(2);
        assertEquals(GlazedListsTests.stringToList("ABGDEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABDEFG"), sorted);

        source.add(4, "H");
        assertEquals(GlazedListsTests.stringToList("ABGDHEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABDEFGH"), sorted);

        source.add(1, "I");
        assertEquals(GlazedListsTests.stringToList("AIBGDHEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABDEFIGH"), sorted);

        source.add(3, "J");
        assertEquals(GlazedListsTests.stringToList("AIBJGDHEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABDEFIJGH"), sorted);
    }

    public void testCompoundEvents() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.stringToList("FDACEB"));
        SortedList<String> sorted = new SortedList<String>(source, false);
        sorted.addListEventListener(new ListConsistencyListener(sorted, "sorted", false));

        sorted.setComparator((Comparator)GlazedLists.comparableComparator());
        assertEquals(GlazedListsTests.stringToList("ABCDEF"), sorted);

        source.addAll(2, GlazedListsTests.stringToList("JGKHI"));
        assertEquals(GlazedListsTests.stringToList("FDJGKHIACEB"), source);
        assertEquals(GlazedListsTests.stringToList("ABCDEFJGKHI"), sorted);

        source.retainAll(GlazedListsTests.stringToList("ABEHI"));
        assertEquals(GlazedListsTests.stringToList("HIAEB"), source);
        assertEquals(GlazedListsTests.stringToList("ABEHI"), sorted);
    }
}