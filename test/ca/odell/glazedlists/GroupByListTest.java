package ca.odell.glazedlists;

import junit.framework.TestCase;

import java.util.*;

public class GroupByListTest extends TestCase {

    public void testAdd() {
        final EventList<String> basic = new BasicEventList<String>();
        final GroupByList<String, String> groups = new GroupByList<String, String>(basic, new StringCountGrouper());

        assertEquals(0, groups.size());

        basic.add("James");
        assertEquals(1, groups.size());
        assertEquals("1 James", groups.get(0));

        basic.add("Jesse");
        assertEquals(2, groups.size());
        assertEquals("1 James", groups.get(0));
        assertEquals("1 Jesse", groups.get(1));

        basic.add("James");
        assertEquals(2, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("1 Jesse", groups.get(1));

        basic.add("Jesse");
        assertEquals(2, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));

        basic.add("Kevin");
        assertEquals(3, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));

        groups.add("Rob");
        assertEquals(4, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));
        assertEquals("1 Rob", groups.get(3));
    }

    public void testRemove() {
        final EventList<String> basic = new BasicEventList<String>();
        basic.addAll(Arrays.asList(new String[] {"James", "Jesse", "James", "Jesse", "Kevin"}));
        final GroupByList<String, String> groups = new GroupByList<String, String>(basic, new StringCountGrouper());

        assertEquals(3, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));

        basic.remove("James");
        assertEquals(3, groups.size());
        assertEquals("1 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));

        basic.remove("James");
        assertEquals(2, groups.size());
        assertEquals("2 Jesse", groups.get(0));
        assertEquals("1 Kevin", groups.get(1));

        basic.remove("Kevin");
        assertEquals(1, groups.size());
        assertEquals("2 Jesse", groups.get(0));

        basic.remove("Rob");
        assertEquals(1, groups.size());
        assertEquals("2 Jesse", groups.get(0));

        groups.remove("2 Jesse");
        assertEquals(0, groups.size());
    }

    public void testSet() {
        final EventList<String> basic = new BasicEventList<String>();
        basic.addAll(Arrays.asList(new String[] {"James", "Jesse", "James", "Jesse", "Kevin"}));
        final GroupByList<String, String> groups = new GroupByList<String, String>(basic, new StringCountGrouper());

        assertEquals(3, groups.size());
        assertEquals("2 James", groups.get(0));
        assertEquals("2 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));

        basic.set(0, "Jesse");
        assertEquals(3, groups.size());
        assertEquals("1 James", groups.get(0));
        assertEquals("3 Jesse", groups.get(1));
        assertEquals("1 Kevin", groups.get(2));

        basic.set(4, "Jesse");
        assertEquals(2, groups.size());
        assertEquals("1 James", groups.get(0));
        assertEquals("4 Jesse", groups.get(1));

        groups.set(1, "6 Kevin");
        assertEquals(2, groups.size());
        assertEquals("1 James", groups.get(0));
        assertEquals("6 Kevin", groups.get(1));
    }

    private static class StringCountGrouper implements GroupByList.Grouper<String, String> {
        public Comparator<String> getComparator() {
            return String.CASE_INSENSITIVE_ORDER;
        }

        public String group(List<String> elements) {
            return elements.size() + " " + elements.get(0);
        }

        public List<String> ungroup(String group) {
            final String[] strings = group.split(" ");
            final int numCopies = new Integer(strings[0]);

            List<String> elements = new ArrayList<String>(numCopies);
            for (int i = 0; i < numCopies; i++)
                 elements.add(strings[1]);
            return elements;
        }
    }
}