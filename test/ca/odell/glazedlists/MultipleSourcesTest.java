/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matchers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests that an event list can depend upon a single source via multiple independent
 * transformations.
 *
 * @see <a href="https://glazedlists.dev.java.net/servlets/ReadMsg?list=users&msgNo=117">Users list #117</a>
 * @see <a href="https://glazedlists.dev.java.net/servlets/ReadMsg?list=users&msgNo=214">Users list #214</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class MultipleSourcesTest {

    /**
     * Tests whether an EventList can depend upon multiple sources simultaneously.
     * This test populates a source list and two transformation lists that depend
     * upon it. There is also a listener that 'depends' upon both of these
     * transformation lists. When the source changes, the transformation lists will
     * be notified one at a time. This verifies that Glazed Lists behaves correctly
     * after the first has been notified but before the second has been notified.
     */
    @Test
    public void testMultipleSources() {
        BasicEventList<String> source = new BasicEventList<String>();
        FilterList<String> filterOne = new FilterList<String>(source, Matchers.trueMatcher());
        FilterList<String> filterTwo = new FilterList<String>(source, Matchers.trueMatcher());
        
        source.add("Game Cube");
        source.add("Genesis");
        source.add("XBox");
        source.add("PlayStation");
        source.add("Turbo Graphics 16");
        
        List<EventList<String>> filterLists = new ArrayList<EventList<String>>();
        filterLists.add(filterOne);
        filterLists.add(filterTwo);
        MultipleSourcesListener filtersListener = new MultipleSourcesListener(filterLists, true);
        
        // modify the source
        source.clear();
        assertEquals(2, filtersListener.getChangeCount());
        source.add("Atari 2600");
        source.add("Intellivision");
        source.add("Game Gear");
        assertEquals(8, filtersListener.getChangeCount());

        // modify filter one
        filterOne.setMatcher(Matchers.falseMatcher());
        assertEquals(9, filtersListener.getChangeCount());
        
        // modify filter two
        filterTwo.setMatcher(Matchers.trueMatcher());
        assertEquals(9, filtersListener.getChangeCount());
    }
    
    /**
     * Tests whether an EventList can depend upon multiple sources simultaneously
     * when not all of the sources are directly registered as ListEventListeners.
     */
    @Test
    public void testMultipleSourcesNoListener() {
        BasicEventList<String> source = new BasicEventList<String>();
        FilterList<String> filterOne = new FilterList<String>(source, Matchers.trueMatcher());
        FilterList<String> filterTwo = new FilterList<String>(source, Matchers.trueMatcher());
        
        source.add("Game Cube");
        source.add("Genesis");
        source.add("XBox");
        source.add("PlayStation");
        source.add("Turbo Graphics 16");
        
        List<EventList<String>> filterLists = new ArrayList<EventList<String>>();
        filterLists.add(filterOne);
        filterLists.add(filterTwo);
        MultipleSourcesListener filtersListener = new MultipleSourcesListener(filterLists, false);
        filterOne.addListEventListener(filtersListener);
        filterOne.getPublisher().addDependency(filterTwo, filtersListener);
        
        // modify the source
        source.clear();
        assertEquals(1, filtersListener.getChangeCount());
        source.add("Atari 2600");
        source.add("Intellivision");
        source.add("Game Gear");
        assertEquals(4, filtersListener.getChangeCount());

        // modify filter one
        filterOne.setMatcher(Matchers.falseMatcher());
        assertEquals(5, filtersListener.getChangeCount());
        
        // modify filter two
        filterTwo.setMatcher(Matchers.trueMatcher());
        assertEquals(5, filtersListener.getChangeCount());
    }
    

    /**
     * Listens to multiple sources, and when one source changes, this iterates all
     * sources.
     */
    static class MultipleSourcesListener implements ListEventListener<String> {
        private List<EventList<String>> sources;
        private int changeCount = 0;

        public MultipleSourcesListener(List<EventList<String>> sources, boolean addListeners) {
            this.sources = sources;
            if(addListeners) {
                for(Iterator<EventList<String>> i = sources.iterator(); i.hasNext(); ) {
                    EventList<String> eventList = i.next();
                    eventList.addListEventListener(this);
                }
            }
        }

        public int getChangeCount() {
            return changeCount;
        }

        @Override
        public void listChanged(ListEvent<String> e) {
            changeCount++;
            for(Iterator<EventList<String>> i = sources.iterator(); i.hasNext(); ) {
                EventList<String> eventList = i.next();
                eventList.toArray();
            }
        }
    }
}
