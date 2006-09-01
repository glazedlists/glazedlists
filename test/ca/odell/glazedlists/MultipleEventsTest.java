/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

/**
 * Tests to verify that for each list change, only one event is fired.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=46">Bug 46</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MultipleEventsTest extends TestCase {

    /**
     * Tests that clearing the filter list does not fire multiple
     * events on the original list.
     */
    public void testFilterList() {
        // create a list
        EventList<int[]> source = new BasicEventList<int[]>();
        source.add(new int[] { 1 });
        source.add(new int[] { 0 });
        source.add(new int[] { 1 });
        source.add(new int[] { 0 });

        // prepare a filter list
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        matcherEditor.setFilter(0, 1);
        FilterList<int[]> filterList = new FilterList<int[]>(source, matcherEditor);

        // listen to changes on the filter list
        ListConsistencyListener<int[]> counter = ListConsistencyListener.install(filterList);

        // clear the filter list
        filterList.clear();

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }

    /**
     * Tests that clearing a sub list does not fire multiple
     * events on the original list.
     */
    public void testSubList() {
        // create a list
        EventList<String> source = new BasicEventList<String>();
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("D");

        // prepare a sub list
        EventList<String> subList = (EventList<String>)source.subList(1, 3);

        // listen to changes on the sub list
        ListConsistencyListener<String> counter = ListConsistencyListener.install(subList);

        // clear the sub list
        subList.clear();

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }

    /**
     * Tests that clearing a unique list does not fire multiple
     * events on the original list.
     */
    public void testUniqueList() {
        // create a list
        EventList<String> source = new BasicEventList<String>();
        source.add("A");
        source.add("B");
        source.add("B");
        source.add("C");

        // prepare a unique list
        EventList<String> uniqueList = new UniqueList<String>(source);

        // listen to changes on the unique list
        ListConsistencyListener<String> counter = ListConsistencyListener.install(uniqueList);

        // clear the unique list
        uniqueList.clear();

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
    }
}