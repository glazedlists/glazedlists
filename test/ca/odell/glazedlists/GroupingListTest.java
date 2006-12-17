package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.Comparator;
import java.util.List;
import java.util.Arrays;

public class GroupingListTest extends TestCase {

    public void testConstruct() {
        List<String> source = GlazedListsTests.stringToList("AAAAABBBBCCC");
        GroupingList<String> groupList = new GroupingList<String>(GlazedLists.eventList(source));

        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
    }

    public void testAdd() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEquals(0, groupList.size());

        // add into empty list
        sourceList.add("A");
        assertEquals(1, groupList.size());
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));

        // add at beginning of first group
        sourceList.add(0, "A");
        assertEquals(1, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));

        // add at end of first group
        sourceList.add(2, "A");
        assertEquals(1, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));

        // add new last group
        sourceList.add("Z");
        assertEquals(2, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("Z"), groupList.get(1));

        // add at beginning of last group
        sourceList.add(sourceList.size()-2, "Z");
        assertEquals(2, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("ZZ"), groupList.get(1));

        // add at end of last group
        sourceList.add(sourceList.size()-1, "Z");
        assertEquals(2, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("ZZZ"), groupList.get(1));

        // add new middle group
        sourceList.add("J");
        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("J"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("ZZZ"), groupList.get(2));

        // add at beginning of middle group
        sourceList.add(3, "J");
        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("JJ"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("ZZZ"), groupList.get(2));

        // add at end of middle group
        sourceList.add(5, "J");
        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("JJJ"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("ZZZ"), groupList.get(2));
    }

    public void testAddAll() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEquals(0, groupList.size());

        // addAll into empty list
        sourceList.addAll(GlazedListsTests.stringToList("ABCD"));
        assertEquals(4, groupList.size());
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("C"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(3));

        // addAll into a non empty list
        sourceList.addAll(GlazedListsTests.stringToList("DCBA"));
        assertEquals(4, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CC"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("DD"), groupList.get(3));
    }

    public void testInsert() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEquals(0, groupList.size());

        sourceList.add("A");
        assertEquals(1, groupList.size());
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));

        sourceList.addAll(GlazedListsTests.stringToList("AAAABBBBCCC"));
        assertEquals(GlazedListsTests.stringToList("AAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        sourceList.add("A");
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        sourceList.add("C");
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(2));

        sourceList.add("D");
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(3));

        sourceList.add("D");
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("DD"), groupList.get(3));

        sourceList.add("!");
        assertEquals(GlazedListsTests.stringToList("!"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(3));
        assertEquals(GlazedListsTests.stringToList("DD"), groupList.get(4));

        sourceList.add(0, "!");
        assertEquals(GlazedListsTests.stringToList("!!"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("AAAAAA"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(3));
        assertEquals(GlazedListsTests.stringToList("DD"), groupList.get(4));
    }

    public void testSourceSet() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEquals(0, groupList.size());

        sourceList.addAll(GlazedListsTests.stringToList("ABCD"));
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("C"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(3));

        sourceList.set(2, "B");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(2));

        sourceList.set(0, "B");
        assertEquals(GlazedListsTests.stringToList("BBB"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(1));
    }

    public void testSet() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> groupListConsistencyListener = ListConsistencyListener.install(groupList);
        groupListConsistencyListener.setPreviousElementTracked(false);
        ListConsistencyListener.install(sourceList);

        assertEquals(0, groupList.size());

        sourceList.addAll(GlazedListsTests.stringToList("ABCDD"));

        groupList.set(3, GlazedListsTests.stringToList("BB"));
        assertEquals(GlazedListsTests.stringToLists("A,BBB,C"), groupList);

        groupList.set(2, GlazedListsTests.stringToList("AD"));
        assertEquals(GlazedListsTests.stringToLists("AA,BBB,D"), groupList);

        groupList.set(0, GlazedListsTests.stringToList("CC"));
        assertEquals(GlazedListsTests.stringToLists("BBB,CC,D"), groupList);

        groupList.set(2, GlazedListsTests.stringToList(""));
        assertEquals(GlazedListsTests.stringToLists("BBB,CC"), groupList);
    }

    public void testRemoveAPair() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        sourceList.addAll(GlazedListsTests.stringToList("AABBBD"));
        assertEquals(GlazedListsTests.stringToLists("AA,BBB,D"), groupList);

        sourceList.remove(0);
        sourceList.remove(0);
        assertEquals(GlazedListsTests.stringToLists("BBB,D"), groupList);
    }

    public void testRemove() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = new GroupingList<String>(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        assertEquals(0, groupList.size());

        sourceList.addAll(GlazedListsTests.stringToList("ABCDD"));

        groupList.remove(2);
        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("DD"), groupList.get(2));

        groupList.remove(2);
        assertEquals(2, groupList.size());
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));

        groupList.remove(GlazedListsTests.stringToList("A"));
        assertEquals(1, groupList.size());
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(0));
    }

    /**
     * Test the write-through operations of GroupList, the type of list
     * returned as the elements of a GroupingList.
     */
    public void testGroupListAdd() {
        EventList<String> sourceList = GlazedLists.eventListOf(new String[] {"A", "B", "B", "C", "C", "C"});
        GroupingList<String> groupList = new GroupingList<String>(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(0)).add("A");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(0)).add("D");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(3));
    }

    public void testGroupListRemove() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = new GroupingList<String>(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(1)).remove("B");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(1)).remove("B");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(1));

        ((List<String>)groupList.get(0)).remove("X");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(1));
    }

    public void testGroupListSet() {
        EventList<String> sourceList = GlazedLists.eventListOf(new String[] {"A", "B", "B", "C", "C", "C"});
        GroupingList<String> groupList = new GroupingList<String>(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(1)).set(0, "A");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        ((List<String>)groupList.get(1)).set(0, "C");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(1));

        ((List<String>)groupList.get(1)).set(2, "B");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
    }

    public void testGroupListGet() {
        EventList<String> sourceList = GlazedLists.eventListOf(new String[] {"A", "B", "B", "C", "C", "C"});
        GroupingList<String> groupList = new GroupingList<String>(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        assertEquals(((List)groupList.get(0)).size(), 1);
        assertEquals(((List)groupList.get(1)).size(), 2);
        assertEquals(((List)groupList.get(2)).size(), 3);

        assertEquals(((List)groupList.get(0)).get(0), "A");
        assertEquals(((List)groupList.get(1)).get(0), "B");
        assertEquals(((List)groupList.get(2)).get(0), "C");
    }

    public void testGroupListClear() {
        EventList<String> sourceList = GlazedLists.eventListOf(new String[] {"A", "B", "B", "C", "C", "C"});
        GroupingList<String> groupList = new GroupingList<String>(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        assertEquals(((List)groupList.get(0)).size(), 1);
        assertEquals(((List)groupList.get(1)).size(), 2);
        assertEquals(((List)groupList.get(2)).size(), 3);

        ((List<String>)groupList.get(2)).clear();
        assertEquals(((List)groupList.get(0)).size(), 1);
        assertEquals(((List)groupList.get(1)).size(), 2);

        ((List<String>)groupList.get(0)).clear();
        assertEquals(((List)groupList.get(0)).size(), 2);

        ((List<String>)groupList.get(0)).clear();
        assertEquals(groupList.size(), 0);
    }

    public void testDispose() {
        final BasicEventList<Object> source = new BasicEventList<Object>();
        final GroupingList<Object> groupingList = new GroupingList<Object>(source);

        assertEquals(1, source.updates.getListEventListeners().size());

        // disposing of the GroupingList should leave nothing listening to the source list
        groupingList.dispose();
        assertEquals(0, source.updates.getListEventListeners().size());
    }

    /**
     * Make sure a GroupingList group is still sane after its elements have
     * all been removed.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=326">Issue 326</a>
     */
    public void testStaleGroupHandling() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source);

        source.addAll(GlazedListsTests.stringToList("AAABBBCCC"));
        List<String> as = (List<String>) groupingList.get(0);
        List<String> bs = (List<String>) groupingList.get(1);
        List<String> cs = (List<String>) groupingList.get(2);
        assertEquals(3, as.size());
        assertEquals(3, bs.size());
        assertEquals(3, cs.size());

        source.removeAll(GlazedListsTests.stringToList("B"));
        assertEquals(3, as.size());
        assertEquals(0, bs.size());
        assertTrue(bs.isEmpty());
        assertEquals(3, cs.size());
    }

    public void testWriteThroughGroupListElement() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source, new FirstLetterComparator());

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse James Jodie Mark Mariusz"));

        List<String> jNames = (List<String>) groupingList.get(0);
        List<String> mNames = (List<String>) groupingList.get(1);

        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James Jodie"), jNames);
        assertEquals(GlazedListsTests.delimitedStringToList("Mark Mariusz"), mNames);

        jNames.add("Jekyll");
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James Jodie Jekyll"), jNames);
        jNames.add(2, "Jamal");
        assertEquals(GlazedListsTests.delimitedStringToList("Jesse James Jamal Jodie Jekyll"), jNames);

        mNames.addAll(GlazedListsTests.delimitedStringToList("Mya"));
        assertEquals(GlazedListsTests.delimitedStringToList("Mark Mariusz Mya"), mNames);
        mNames.addAll(2, GlazedListsTests.delimitedStringToList("Mankar"));
        assertEquals(GlazedListsTests.delimitedStringToList("Mark Mariusz Mankar Mya"), mNames);
    }

    /**
     * Make sure a simple test case that breaks the {@link SeparatorList} doesn't
     * impact the {@link GroupingList}.
     */
    public void testSeparatorListBreaks() {
        ExternalNestingEventList<String> source = new ExternalNestingEventList<String>(new BasicEventList<String>());
        GroupingList<String> grouped = new GroupingList<String>(source, (Comparator)GlazedLists.comparableComparator());
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(grouped);
        listConsistencyListener.setPreviousElementTracked(false);

        // adjust using an event known to put the separator in the wrong place
        source.addAll(0, GlazedListsTests.stringToList("CSC"));
        source.beginEvent(true);
        source.remove(0);
        source.remove(1);
        source.addAll(0, GlazedListsTests.stringToList("SC"));
        source.commitEvent();
        assertEquals(GlazedListsTests.stringToList("C"), grouped.get(0));
        assertEquals(GlazedListsTests.stringToList("SS"), grouped.get(1));
    }

    public void testGenerics() {
        EventList<String> source = new BasicEventList<String>();

        EventList<List<String>> testList = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Test the replacement of the grouping Comparator.
     */
    public void testSetComparator() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source, new FirstLetterComparator());
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupingList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("Black");
        source.add("Blind");
        source.add("Bling");

        assertEquals(1, groupingList.size());
        assertEquals(Arrays.asList(new String[] {"Black", "Blind", "Bling"}), groupingList.get(0));

        groupingList.setComparator(new LastLetterComparator());
        assertEquals(3, groupingList.size());
        assertEquals(Arrays.asList(new String[] {"Blind"}), groupingList.get(0));
        assertEquals(Arrays.asList(new String[] {"Bling"}), groupingList.get(1));
        assertEquals(Arrays.asList(new String[] {"Black"}), groupingList.get(2));

        groupingList.setComparator(new FirstLetterComparator());
        assertEquals(1, groupingList.size());
        assertEquals(Arrays.asList(new String[] {"Black", "Blind", "Bling"}), groupingList.get(0));

        groupingList.setComparator(null);
        assertEquals(3, groupingList.size());
        assertEquals(Arrays.asList(new String[] {"Black"}), groupingList.get(0));
        assertEquals(Arrays.asList(new String[] {"Blind"}), groupingList.get(1));
        assertEquals(Arrays.asList(new String[] {"Bling"}), groupingList.get(2));
    }

    private static class FirstLetterComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.charAt(0) - o2.charAt(0);
        }
    }

    private static class LastLetterComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.charAt(o1.length()-1) - o2.charAt(o2.length()-1);
        }
    }
}