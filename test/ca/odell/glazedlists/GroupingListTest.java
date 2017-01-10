/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.Matcher;

public class GroupingListTest {

    @Test
    public void testConstruct() {
        List<String> source = GlazedListsTests.stringToList("AAAAABBBBCCC");
        GroupingList<String> groupList = GroupingList.create(GlazedLists.eventList(source));

        assertEquals(3, groupList.size());
        assertEquals(GlazedListsTests.stringToList("AAAAA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BBBB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
    }

    @Test
    public void testAdd() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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

    @Test
    public void testAddAll() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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

    @Test
    public void testInsert() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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

    @Test
    public void testSourceSet() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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

    @Test
    public void testSet() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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

    @Test
    public void testRemoveAPair() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupList);
        listConsistencyListener.setPreviousElementTracked(false);

        sourceList.addAll(GlazedListsTests.stringToList("AABBBD"));
        assertEquals(GlazedListsTests.stringToLists("AA,BBB,D"), groupList);

        sourceList.remove(0);
        sourceList.remove(0);
        assertEquals(GlazedListsTests.stringToLists("BBB,D"), groupList);
    }

    @Test
    public void testRemove() {
        EventList<String> sourceList = new BasicEventList<String>();
        GroupingList<String> groupList = GroupingList.create(sourceList);
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
    @Test
    public void testGroupListAdd() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = GroupingList.create(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(0).add("A");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(0).add("D");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
        assertEquals(GlazedListsTests.stringToList("D"), groupList.get(3));
    }

    /**
     * This method tests a peculiar problem in GroupingList reported from the
     * field and captured here https://glazedlists.dev.java.net/issues/show_bug.cgi?id=412
     * The test involves building a ListEvent that describes multiple updates
     * within the same ListEvent (something that cannot be done through the
     * normal List API).
     *
     * The problem we were seeing is that the GroupingList fires a ListEvent
     * that includes an update to a group at an index that NEVER EXISTED.
     */
    @Test
    public void testGroupListMassUpdate() {
        BasicEventList<String> sourceList = new BasicEventList<String>();
        sourceList.addAll(GlazedListsTests.delimitedStringToList("A A A A"));
        GroupingList<String> groupList = new GroupingList<String>(sourceList, GlazedListsTests.getFirstLetterComparator());
        ListConsistencyListener<List<String>> checker = ListConsistencyListener.install(groupList);
        checker.setPreviousElementTracked(false);

        assertEquals(GlazedListsTests.delimitedStringToList("A A A A"), groupList.get(0));

        sourceList.updates.beginEvent(true);
        sourceList.set(0, "A");
        sourceList.set(1, "A");
        sourceList.set(2, "A");
        sourceList.updates.commitEvent();

        assertEquals(GlazedListsTests.delimitedStringToList("A A A A"), groupList.get(0));
    }

    @Test
    public void testGroupListRemove() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = GroupingList.create(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(1).remove("B");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(1).remove("B");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(1));

        groupList.get(0).remove("X");
        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(1));
    }

    @Test
    public void testGroupListSet() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = GroupingList.create(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(1).set(0, "A");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        groupList.get(1).set(0, "C");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("CCCC"), groupList.get(1));

        groupList.get(1).set(2, "B");
        assertEquals(GlazedListsTests.stringToList("AA"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("B"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));
    }

    @Test
    public void testGroupListGet() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = GroupingList.create(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        assertEquals(groupList.get(0).size(), 1);
        assertEquals(groupList.get(1).size(), 2);
        assertEquals(groupList.get(2).size(), 3);

        assertEquals(groupList.get(0).get(0), "A");
        assertEquals(groupList.get(1).get(0), "B");
        assertEquals(groupList.get(2).get(0), "C");
    }

    @Test
    public void testGroupListClear() {
        EventList<String> sourceList = GlazedLists.eventListOf("A", "B", "B", "C", "C", "C");
        GroupingList<String> groupList = GroupingList.create(sourceList);

        assertEquals(GlazedListsTests.stringToList("A"), groupList.get(0));
        assertEquals(GlazedListsTests.stringToList("BB"), groupList.get(1));
        assertEquals(GlazedListsTests.stringToList("CCC"), groupList.get(2));

        assertEquals(groupList.get(0).size(), 1);
        assertEquals(groupList.get(1).size(), 2);
        assertEquals(groupList.get(2).size(), 3);

        groupList.get(2).clear();
        assertEquals(groupList.get(0).size(), 1);
        assertEquals(groupList.get(1).size(), 2);

        groupList.get(0).clear();
        assertEquals(groupList.get(0).size(), 2);

        groupList.get(0).clear();
        assertEquals(groupList.size(), 0);
    }

    @Test
    public void testDispose() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = GroupingList.create(source);

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
    @Test
    public void testStaleGroupHandling() {
        final EventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = GroupingList.create(source);

        source.addAll(GlazedListsTests.stringToList("AAABBBCCC"));
        List<String> as = groupingList.get(0);
        List<String> bs = groupingList.get(1);
        List<String> cs = groupingList.get(2);
        assertEquals(3, as.size());
        assertEquals(3, bs.size());
        assertEquals(3, cs.size());

        source.removeAll(GlazedListsTests.stringToList("B"));
        assertEquals(3, as.size());
        assertEquals(0, bs.size());
        assertTrue(bs.isEmpty());
        assertEquals(3, cs.size());
    }

    @Test
    public void testWriteThroughGroupListElement() {
        final EventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source, GlazedListsTests.getFirstLetterComparator());

        source.addAll(GlazedListsTests.delimitedStringToList("Jesse James Jodie Mark Mariusz"));

        List<String> jNames = groupingList.get(0);
        List<String> mNames = groupingList.get(1);

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
    @Test
    public void testSeparatorListBreaks() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        GroupingList<String> grouped = new GroupingList<String>(source, GlazedLists.comparableComparator());
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(grouped);
        listConsistencyListener.setPreviousElementTracked(false);

        // adjust using an event known to put the separator in the wrong place
        source.addAll(0, GlazedListsTests.stringToList("CSC"));
        source.beginEvent();
        source.remove(0);
        source.remove(1);
        source.addAll(0, GlazedListsTests.stringToList("SC"));
        source.commitEvent();
        assertEquals(GlazedListsTests.stringToList("C"), grouped.get(0));
        assertEquals(GlazedListsTests.stringToList("SS"), grouped.get(1));
    }

    @Test
    public void testGenerics() {
        EventList<String> source = new BasicEventList<String>();

        EventList<List<String>> testList = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        testList.dispose();
    }

    /**
     * Test the replacement of the grouping Comparator.
     */
    @Test
    public void testSetComparator() {
        final EventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source, GlazedListsTests.getFirstLetterComparator());
        ListConsistencyListener<List<String>> listConsistencyListener = ListConsistencyListener.install(groupingList);
        listConsistencyListener.setPreviousElementTracked(false);

        source.add("Black");
        source.add("Blind");
        source.add("Bling");

        assertEquals(1, groupingList.size());
        assertEquals(Arrays.asList("Black", "Blind", "Bling"), groupingList.get(0));

        groupingList.setComparator(GlazedListsTests.getLastLetterComparator());
        assertEquals(3, groupingList.size());
        assertEquals(Arrays.asList("Blind"), groupingList.get(0));
        assertEquals(Arrays.asList("Bling"), groupingList.get(1));
        assertEquals(Arrays.asList("Black"), groupingList.get(2));

        groupingList.setComparator(GlazedListsTests.getFirstLetterComparator());
        assertEquals(1, groupingList.size());
        assertEquals(Arrays.asList("Black", "Blind", "Bling"), groupingList.get(0));

        groupingList.setComparator(null);
        assertEquals(3, groupingList.size());
        assertEquals(Arrays.asList("Black"), groupingList.get(0));
        assertEquals(Arrays.asList("Blind"), groupingList.get(1));
        assertEquals(Arrays.asList("Bling"), groupingList.get(2));
    }

    @Test
    public void testIndexOfGroup() {
        final EventList<String> source = new BasicEventList<String>();
        final GroupingList<String> groupingList = new GroupingList<String>(source, GlazedListsTests.getFirstLetterComparator());

        source.add("Bart");
        source.add("Brent");
        source.add("Brisket");

        source.add("Jackal");
        source.add("Jackalope");
        source.add("Juggernaut");

        source.add("Rib");
        source.add("Rub");
        source.add("Rubber");

        assertEquals(-1, groupingList.indexOfGroup("Ambrose"));
        assertEquals(0, groupingList.indexOfGroup("Bilbo"));
        assertEquals(-1, groupingList.indexOfGroup("Cardiac"));

        assertEquals(-1, groupingList.indexOfGroup("Ignatius"));
        assertEquals(1, groupingList.indexOfGroup("Jargon"));
        assertEquals(-1, groupingList.indexOfGroup("Korn"));

        assertEquals(-1, groupingList.indexOfGroup("Uranus"));
        assertEquals(2, groupingList.indexOfGroup("Rusty"));
        assertEquals(-1, groupingList.indexOfGroup("Steve"));
    }

    @Test
    public void testWithFilters() {
        EventList<String> source = new BasicEventList<String>();
        FilterList<String> filtered = new FilterList<String>(source);
        GroupingList<String> grouped = new GroupingList<String>(filtered, GlazedListsTests
                .getFirstLetterComparator());

        source.addAll(Arrays.asList("CGG"));
        assertEquals(1, grouped.size());

        filtered.setMatcher(new Matcher<String>() {
            @Override
            public boolean matches(String item) {
                return !item.equals("CGG");
            }
        });

        assertEquals(0, grouped.size());
    }

    /**
     * This tests a corner case where updates to the first element in a group caused the next
     * element to be marked as a potential start of a group. This worked fine until we had an
     * event that actually had an update on that next element - this caused things to go wrong in
     * the Grouper implementation.
     * <p>
     * See issue <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=491">491</a>
     * </p>
     */
    @Test
    public void testChangeOnFirstAndSecondElement() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, GlazedListsTests
                .getFirstLetterComparator());

        source.add("A1");
        source.add("A2");
        source.add("A4");

        assertEquals("[[A1, A2, A4]]", grouped.toString());

        source.beginEvent();
        source.set(0, "A1");
        source.set(1, source.get(1));
        source.add("A5");
        source.commitEvent();

        assertEquals("[[A1, A2, A4, A5]]", grouped.toString());
    }

    /**
     * This tests a corner case that arose from the first attempt at fixing issue <a
     * href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=491">491</a>
     */
    @Test
    public void testAddOnFirstAndUpdateOnSecondElement() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, GlazedListsTests
                .getFirstLetterComparator());

        source.add("A1");
        source.add("B1");
        source.add("B2");

        assertEquals("[[A1], [B1, B2]]", grouped.toString());

        source.beginEvent();
        source.add(1, "A2");
        source.set(2, source.get(2));
        source.commitEvent();

        assertEquals("[[A1, A2], [B1, B2]]", grouped.toString());

    }

    /**
     * Testcase from issue <a
     * href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=486">486</a>
     */
    @Test
    public void testIssue486() {
        TransactionList<String> txList = new TransactionList<String>(
                new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(txList);
        txList.beginEvent();
        txList.add("basket");
        txList.add("basket");
        txList.add("basket");
        txList.add("basket");
        txList.commitEvent();
        assertEquals("[[basket, basket, basket, basket]]", grouped.toString());

        txList.beginEvent();
        txList.set(0, txList.get(0));
        txList.set(1, txList.get(1));
        txList.set(2, txList.get(2));
        txList.set(3, txList.get(3));
        txList.commitEvent();
        assertEquals("[[basket, basket, basket, basket]]", grouped.toString());
    }

    @Test
    public void testIssue522() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        GroupingList<String> grouped = new GroupingList<String>(source, comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped);
        listener.setPreviousElementTracked(false);
        String[] s = new String[] {"A", "B", "C", "D", "DD", "DDD", "E", "F", "FF"};

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }
        assertEquals("[[A], [B], [C], [D, DD, DDD], [E], [F, FF]]", grouped.toString());

        source.beginEvent();
        source.set(7, "F");
        source.remove(8);
        source.commitEvent();
        assertEquals("[[A], [B], [C], [D, DD, DDD], [E], [F]]", grouped.toString());
    }

    @Test
    public void testMultipleUpdateDelete() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        GroupingList<String> grouped = new GroupingList<String>(source, comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped);
        listener.setPreviousElementTracked(false);
        String[] s = new String[] {"C", "CC", "D", "DD", "E", "EE"};

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }

        assertEquals("[[C, CC], [D, DD], [E, EE]]", grouped.toString());

        source.beginEvent();
        source.set(0, "C");
        source.remove(1);
        source.set(1, "D");
        source.remove(2);
        source.set(2, "E");
        source.remove(3);
        source.commitEvent();

        assertEquals("[[C], [D], [E]]", grouped.toString());
    }

    @Test
    public void testLastGroup() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        GroupingList<String> grouped = new GroupingList<String>(source, comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped);
        listener.setPreviousElementTracked(false);
        String[] s = new String[] {"A", "B", "C", "D", "DD", "DDD", "E", "F", "FF"};

        for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }
        assertEquals("[[A], [B], [C], [D, DD, DDD], [E], [F, FF]]", grouped.toString());

        source.beginEvent();
        source.remove(6);
        source.set(6, "DDDD");
        source.remove(7);
        source.add("E");
        source.commitEvent();
        assertEquals("[[A], [B], [C], [D, DD, DDD, DDDD], [E]]", grouped.toString());
    }

    @Test
	public void testIssue499() {
		TransactionList<String> source = new TransactionList<String>(
				new BasicEventList<String>());

		Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
		GroupingList<String> grouped = new GroupingList<String>(source,
				comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

		String[] s = new String[] { "MSFT", "MSFT", "IBM", "C", "IBM", "C",
				"C", "IBM", "IBM", "C" };
		String[] upd = new String[] { "MSFT", "MSFT", "MSFT", "C", "C", "IBM",
				"IBM", "IBM", "C", "MSFT" };

		for (int i = 0; i < s.length; i++) {
            source.add(s[i]);
        }

		assertEquals("[[C, C, C, C], [IBM, IBM, IBM, IBM], [MSFT, MSFT]]", grouped.toString());

		source.beginEvent();
		for (int i = 0; i < upd.length; i++) {
            source.set(i, upd[i]);
        }
		source.commitEvent();
		assertEquals("[[C, C, C], [IBM, IBM, IBM], [MSFT, MSFT, MSFT, MSFT]]", grouped.toString());
	}

    @Test
    public void testSplitFirstGroup() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        GroupingList<String> grouped = new GroupingList<String>(source, comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("C");
        source.add("CC");
        source.add("CCC");

        assertEquals("[[C, CC, CCC]]", grouped.toString());

        source.beginEvent();
        source.set(0, "A");
        source.set(1, "B");
        source.commitEvent();

        assertEquals("[[A], [B], [CCC]]", grouped.toString());
    }

    @Test
    public void testSplitFirstGroupLonger() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source,  GlazedListsTests.getFirstLetterComparator());
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("C1");
        source.add("C2");
        source.add("C3");
        source.add("C4");
        source.add("D");

        assertEquals("[[C1, C2, C3, C4], [D]]", grouped.toString());

        source.beginEvent();
        source.set(0, "A");
        source.set(1, "B");
        source.commitEvent();

        assertEquals("[[A], [B], [C3, C4], [D]]", grouped.toString());

        source.beginEvent();
        source.set(0, "A");
        source.set(1, "B1");
        source.set(2, "B2");
        source.set(3, "B3");
        source.set(4, "B4");
        source.commitEvent();

        assertEquals("[[A], [B1, B2, B3, B4]]", grouped.toString());
    }

    @Test
    public void testJoinToFirstGroup() {
        TransactionList<String> source = new TransactionList<String>(
                new BasicEventList<String>());

        Comparator<String> comparator = GlazedListsTests.getFirstLetterComparator();
        GroupingList<String> grouped = new GroupingList<String>(source, comparator);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.add("A");
        source.add("B");
        source.add("CCC");

        assertEquals("[[A], [B], [CCC]]", grouped.toString());

        source.beginEvent();
        source.set(0, "C");
        source.set(1, "CC");
        source.commitEvent();
        assertEquals("[[C, CC, CCC]]", grouped.toString());
    }

    @Test
    public void testTwoGroupsToOne() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("A");
        source.add("B");
        source.commitEvent();

        assertEquals("[[A, A], [B]]", grouped.toString());

        source.beginEvent();
        source.set(0, "B");
        source.set(1, "B");
        source.commitEvent();
        assertEquals("[[B, B, B]]", grouped.toString());
    }

    @Test
    public void testFourGroupsToOne() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());
        ListConsistencyListener<String> listener2 = ListConsistencyListener.<String>install(source, "SOURCE:", true);

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("D");
        source.commitEvent();

        assertEquals("[[A], [B], [C], [D]]", grouped.toString());

        source.beginEvent();
        source.set(0, "D");
        source.set(1, "D");
        source.set(2, "D");
        source.commitEvent();

        assertEquals("[[D, D, D, D]]", grouped.toString());
    }

    @Test
    public void testTakeLastElementFromPrevious() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("C");
        source.commitEvent();

        assertEquals("[[B, B, B, B], [C]]", grouped.toString());

        source.beginEvent();
        source.set(3, "C");
        source.commitEvent();

        assertEquals("[[B, B, B], [C, C]]", grouped.toString());
    }

    @Test
    public void testTakeDuplicatesFromPrevious() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("C");
        source.commitEvent();

        assertEquals("[[B, B, B, B], [C]]", grouped.toString());

        source.beginEvent();
        source.set(1, "C");
        source.set(2, "C");
        source.set(3, "C");
        source.commitEvent();

        assertEquals("[[B], [C, C, C, C]]", grouped.toString());
    }

    @Test
    public void testTakeDuplicatesFromPreviousInnerGroups() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("D");
        source.commitEvent();

        assertEquals("[[A], [B, B, B], [C, C, C], [D]]", grouped.toString());

        source.beginEvent();
        source.set(2, "C");
        source.set(3, "C");
        source.commitEvent();

        assertEquals("[[A], [B], [C, C, C, C, C], [D]]", grouped.toString());
    }

    @Test
    public void testMultipleUpdatesTakeAllFromPrevious() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
        listener.setPreviousElementTracked(false);

        source.beginEvent();
        source.add("A");
        source.add("B");
        source.add("B");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("D");
        source.commitEvent();

        assertEquals("[[A], [B, B, B], [C, C, C], [D]]", grouped.toString());

        source.beginEvent();
        source.set(1, "C");
        source.set(2, "C");
        source.set(3, "C");
        source.commitEvent();

        assertEquals("[[A], [C, C, C, C, C, C], [D]]", grouped.toString());
    }

    @Test
    public void testMultipleUpdatesCreateGroupOnStartOfPrev() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
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

        assertEquals("[[A], [C, C, C], [D, D, D], [E]]", grouped.toString());

        source.beginEvent();
        source.set(1, "B");
        source.commitEvent();

        assertEquals("[[A], [B], [C, C], [D, D, D], [E]]", grouped.toString());
    }

    @Test
    public void testMultipleUpdatesCreateGroupOnDuplicateOfPrev() {
        TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>());

        GroupingList<String> grouped = new GroupingList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener<List<String>> listener = ListConsistencyListener.<List<String>>install(grouped, "GROUPED:", true);
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

        assertEquals("[[A], [C, C, C, C], [D, D, D], [E]]", grouped.toString());

        source.beginEvent();
        source.set(2, "B");
        source.set(3, "B");
        source.commitEvent();

        assertEquals("[[A], [B, B], [C, C], [D, D, D], [E]]", grouped.toString());
    }
}
