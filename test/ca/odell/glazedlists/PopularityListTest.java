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
        validateRanked(popularityList, source);
        
        source.add("Jonathan");
        source.add("Jonathan");
        source.remove("Mike");
        source.remove("Melissa");

        assertEquals("Jonathan", popularityList.get(0));
        assertEquals("Melissa", popularityList.get(1));
        validateRanked(popularityList, source);

        source.add("Mike");
        source.add("Mike");
        source.add("Mike");

        assertEquals("Mike", popularityList.get(0));
        assertEquals("Jonathan", popularityList.get(1));
        assertEquals("Melissa", popularityList.get(2));
        validateRanked(popularityList, source);
        
        source.clear();
    }
    
    /**
     * Tests that the Popularity List works by using a random sequence of operations.
     */
    public void testRandom() {
        Random dice = new Random(0);
        
        EventList source = new BasicEventList();
        PopularityList popularityList = new PopularityList(source);
        new PopularityListValidator(popularityList, source);
        
        for(int j = 0; j < 10; j++) {
            // add 1000
            for(int i = 0; i < 100; i++) {
                source.add(new Integer(dice.nextInt(10)));
            }

            // remove 900
            for(int i = 0; i < 90; i++) {
                int remIndex = dice.nextInt(source.size());
                source.remove(remIndex);
            }
            
            // set 800
            for(int i = 0; i < 80; i++) {
                int updateIndex = dice.nextInt(source.size());
                Integer updateValue = new Integer(dice.nextInt(10));
                source.set(updateIndex, updateValue);
            }
        }
    }
    
    /**
     * Returns the number of repetitions of the specified value in the specified
     * source.
     */
    private int count(Object value, List source) {
        int count = 0;
        for(Iterator i = source.iterator(); i.hasNext(); ) {
            if(value.equals(i.next())) count++;
        }
        return count;
    }
    
    /**
     * Verifies that the specified list is in order of popular elements. This verifies
     * that the rank of the elements in popularity is decreasing.
     */
    private void validateRanked(List popularity, List source) {
        // verify the collections contain the same set of elements
        Set uniqueSource = new TreeSet();
        uniqueSource.addAll(source);
        assertTrue(popularity.containsAll(uniqueSource));
        assertTrue(uniqueSource.containsAll(popularity));
        assertEquals(uniqueSource.size(), popularity.size());
        
        // validate proper rank order
        int lastRank = Integer.MAX_VALUE;
        for(Iterator p = popularity.iterator(); p.hasNext(); ) {
            Object element = p.next();
            int currentRank = count(element, source);
            assertTrue("lastRank=" + lastRank + " < currentRank=" + currentRank + "\nelement=" + element +", \npop=" + popularity + "\nall=" + source, lastRank >= currentRank);
            lastRank = currentRank;
        }
    }
    
    class PopularityListValidator implements ListEventListener {
        private List elementCounts = new ArrayList();
        private List popularityList;
        private List source;
        public PopularityListValidator(PopularityList popularityList, List source) {
            this.popularityList = popularityList;
            this.source = source;
            for(int i = 0; i < popularityList.size(); i++) {
                elementCounts.add(new Integer(count(popularityList.get(i), source)));
            }
            
            popularityList.addListEventListener(this);
        }
        public void listChanged(ListEvent listEvent) {
            // apply the changes
            while(listEvent.next()) {
                int changeIndex = listEvent.getIndex();
                int changeType = listEvent.getType();
                
                if(changeType == ListEvent.DELETE) {
                    elementCounts.remove(changeIndex);
                } else if(changeType == ListEvent.INSERT) {
                    elementCounts.add(changeIndex, new Integer(count(popularityList.get(changeIndex), source)));
                } else if(changeType == ListEvent.UPDATE) {
                    elementCounts.set(changeIndex, new Integer(count(popularityList.get(changeIndex), source)));
                }
            }
            
            // validate the changes
            assertEquals(popularityList.size(), elementCounts.size());
            for(int i = 0; i < popularityList.size(); i++) {
                assertEquals("Test index " + i + ", value: " + popularityList.get(i), elementCounts.get(i), new Integer(count(popularityList.get(i), source)));
            }
        }
    }
}
