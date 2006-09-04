/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import junit.framework.TestCase;

import java.util.*;

/**
 * This test attempts to cause atomic change events that have change blocks
 * with indexes in random order.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IndexOrderTest extends TestCase {

    /** for randomly choosing list indices */
    private Random random = new Random();
    
    /**
     * Test to verify that the list changes occur in increasing order.
     *GlazedL
     * <p>This creates a long chain of lists designed to cause events where the indices
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIncreasingOrder() {
        EventList<int[]> unsorted = new BasicEventList<int[]>();
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 50);
        new FilterList<int[]>(unsorted, matcherEditor);

        // add a block of new elements one hundred times
        for(int a = 0; a < 100; a++) {

            // create a block of ten elements
            List<int[]> currentChange = new ArrayList<int[]>();
            for(int b = 0; b < 10; b++) {
                currentChange.add(new int[] { random.nextInt(100), random.nextInt(100) });
            }
            
            // add that block
            unsorted.addAll(currentChange);
        }
        
        for(int b = 0; b < 100; b++) {
            matcherEditor.setFilter(random.nextInt(2), random.nextInt(100));
        }
    }

    /**
     * Test to verify that the lists work with change indices out of order.
     *
     * <p>This creates a long chain of lists designed to cause events where the indices
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIndexOutOfOrder() {
        EventList<int[]> unsorted = new BasicEventList<int[]>();
        SortedList<int[]> sortedOnce = new SortedList<int[]>(unsorted, GlazedListsTests.intArrayComparator(0));
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 50);
        FilterList<int[]> filteredOnce = new FilterList<int[]>(sortedOnce, matcherEditor);
        SortedList<int[]> sortedTwice = new SortedList<int[]>(filteredOnce, GlazedListsTests.intArrayComparator(0));
        
        ListConsistencyListener.install(unsorted);
        ListConsistencyListener.install(sortedOnce);
        ListConsistencyListener.install(filteredOnce);
        ListConsistencyListener.install(sortedTwice);

        ArrayList<int[]> controlList = new ArrayList<int[]>();
        
        // add a block of new elements one hundred times
        for(int a = 0; a < 15; a++) {

            // create a block of ten elements
            List<int[]> currentChange = new ArrayList<int[]>();
            for(int b = 0; b < controlList.size() || b < 10; b++) {
                currentChange.add(new int[] { random.nextInt(100), random.nextInt(100) });
            }
            
            // add that block
            unsorted.addAll(currentChange);
            
            // manually create a replica
            controlList.addAll(currentChange);
            Collections.sort(controlList, sortedTwice.getComparator());
            for(Iterator<int[]> i = controlList.iterator(); i.hasNext(); ) {
                if(matcherEditor.getMatcher().matches(i.next())) continue;
                i.remove();
            }
            
            // verify the replica matches
            assertEquals(controlList, filteredOnce);
        }
    }
}
