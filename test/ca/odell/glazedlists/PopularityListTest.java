/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.util.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A PopularityListTest tests the functionality of the PopularityList.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class PopularityListTest extends TestCase {

    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Test that the Popularity List works with simple data.
     */
    public void testSimpleData() {
        EventList source = new BasicEventList();
        PopularityList popularityList = new PopularityList(source);
        
        source.add("Mike");
        source.add("Kevin");
        source.add("Graham");
        source.add("Mike");
        source.add("Melissa");
        source.add("Melissa");
        source.add("Jonathan");
        source.add("Jodie");
        source.add("Andrew");
        source.add("Melissa");
        
        assertEquals("Melissa", popularityList.get(0));
        assertEquals("Mike", popularityList.get(1));
        
        source.add("Jonathan");
        source.add("Jonathan");
        source.remove("Mike");
        source.remove("Melissa");

        assertEquals("Jonathan", popularityList.get(0));
        assertEquals("Melissa", popularityList.get(1));

        source.add("Mike");
        source.add("Mike");
        source.add("Mike");

        assertEquals("Mike", popularityList.get(0));
        assertEquals("Jonathan", popularityList.get(1));
        assertEquals("Melissa", popularityList.get(2));
        
        source.clear();
    }
    
    /**
     * Tests that the Popularity List works by using a random sequence of operations.
     */
    public void testRandom() {
        Random dice = new Random(0);
        
        EventList source = new BasicEventList();
        SortedList sortedSource = new SortedList(source);
        PopularityList popularityList = new PopularityList(source);
        new PopularityListValidator(popularityList, sortedSource);
        
        // add 1000
        for(int i = 0; i < 1000; i++) {
            source.add(new Integer(dice.nextInt(50)));
        }

        // remove 900
        for(int i = 0; i < 900; i++) {
            int remIndex = dice.nextInt(source.size());
            source.remove(remIndex);
        }
        
        // set 800
        for(int i = 0; i < 800; i++) {
            int updateIndex = dice.nextInt(source.size());
            Integer updateValue = new Integer(dice.nextInt(50));
            source.set(updateIndex, updateValue);
        }
    }
    
    /**
     * Validates that the state of the PopularityList is correct. Because this class
     * is a Listener, it can detect the exact change that causes the PopularityList
     * to come out of sync.
     */
    class PopularityListValidator implements ListEventListener {

        private List elementCounts = new ArrayList();
        private PopularityList popularityList;
        private SortedList sortedSource;

        /**
         * Creates a PopularityListValidator that validates that the specified
         * popularity list ranks the elements in the speceified sortedlist in 
         * order of popularity. A SortedList is used because it has much better
         * performance for {@link List#indexOf(Object)} and {@link List#lastIndexOf(Object)}
         * operations.
         */
        public PopularityListValidator(PopularityList popularityList, SortedList sortedSource) {
            this.popularityList = popularityList;
            this.sortedSource = sortedSource;
            for(int i = 0; i < popularityList.size(); i++) {
                elementCounts.add(new Integer(count(popularityList.get(i))));
            }
            
            popularityList.addListEventListener(this);
        }
        
        /**
         * Handle the source PopularityList changing by validating that list.
         */
        public void listChanged(ListEvent listEvent) {
            List changedIndices = new ArrayList();
            
            // apply the changes
            while(listEvent.next()) {
                int changeIndex = listEvent.getIndex();
                int changeType = listEvent.getType();
                changedIndices.add(new Integer(changeIndex));
                
                if(changeType == ListEvent.DELETE) {
                    elementCounts.remove(changeIndex);
                } else if(changeType == ListEvent.INSERT) {
                    elementCounts.add(changeIndex, new Integer(count(popularityList.get(changeIndex))));
                } else if(changeType == ListEvent.UPDATE) {
                    elementCounts.set(changeIndex, new Integer(count(popularityList.get(changeIndex))));
                }
            }
            
            // validate the changes
            assertEquals(popularityList.size(), elementCounts.size());
            for(Iterator c = changedIndices.iterator(); c.hasNext(); ) {
                int changeIndex = ((Integer)c.next()).intValue();
                for(int i = Math.max(changeIndex - 1, 0); i < Math.min(changeIndex+2, popularityList.size()); i++) {
                    assertEquals("Test index " + i + ", value: " + popularityList.get(i), elementCounts.get(i), new Integer(count(popularityList.get(i))));
                }
            }
        }

        /**
         * Get the number of copies of the specified value exist in the source list.
         */
        public int count(Object value) {
            int first = sortedSource.indexOf(value);
            if(first == -1) return 0;
            int last = sortedSource.lastIndexOf(value);
            return (last - first + 1);
        }
    }
}
