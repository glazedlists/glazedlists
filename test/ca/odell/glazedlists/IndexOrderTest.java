/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
// standard collections
import java.util.*;

/**
 * This test attempts to cause atomic change events that have change blocks
 * with indexes in random order.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IndexOrderTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random();
    
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
     * Test to verify that the list changes occur in increasing order.
     *
     * <p>This creates a long chain of lists designed to cause events where the indicies
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIncreasingOrder() {
        EventList unsorted = new BasicEventList();
        IntegerArrayFilterList filteredOnce = new IntegerArrayFilterList(unsorted, 0, 50);
        SortedList sortedOnce = new SortedList(filteredOnce, new IntegerArrayComparator(0));
        SortedList sortedTwice = new SortedList(sortedOnce, new IntegerArrayComparator(1));
        
        // add a block of new elements one hundred times
        for(int a = 0; a < 100; a++) {

            // create a block of ten elements
            List currentChange = new ArrayList();
            for(int b = 0; b < 10; b++) {
                currentChange.add(new int[] { random.nextInt(100), random.nextInt(100) });
            }
            
            // add that block
            unsorted.addAll(currentChange);
        }
        
        for(int b = 0; b < 100; b++) {
            filteredOnce.setFilter(random.nextInt(2), random.nextInt(100));
        }
    }

    /**
     * Test to verify that the lists work with change indicies out of order.
     *
     * <p>This creates a long chain of lists designed to cause events where the indicies
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIndexOutOfOrder() {
        EventList unsorted = new BasicEventList();
        SortedList sortedOnce = new SortedList(unsorted, new IntegerArrayComparator(0));
        AbstractFilterList filteredOnce = new IntegerArrayFilterList(sortedOnce, 0, 50);
        SortedList sortedTwice = new SortedList(filteredOnce, new IntegerArrayComparator(0));
        
        unsorted.addListEventListener(new IncreasingChangeIndexListener());
        sortedOnce.addListEventListener(new IncreasingChangeIndexListener());
        filteredOnce.addListEventListener(new IncreasingChangeIndexListener());
        
        ArrayList controlList = new ArrayList();
        
        // add a block of new elements one hundred times
        for(int a = 0; a < 15; a++) {

            // create a block of ten elements
            List currentChange = new ArrayList();
            for(int b = 0; b < controlList.size() || b < 10; b++) {
                currentChange.add(new int[] { random.nextInt(100), random.nextInt(100) });
            }
            
            // add that block
            unsorted.addAll(currentChange);
            
            // manually create a replica
            controlList.addAll(currentChange);
            Collections.sort(controlList, sortedTwice.getComparator());
            for(Iterator i = controlList.iterator(); i.hasNext(); ) {
                if(filteredOnce.filterMatches(i.next())) continue;
                i.remove();
            }
            
            // verify the replica matches
            assertEquals(controlList, filteredOnce);
        }
    }
    
    /**
     * A special comparator that compares two integer arrays by the element
     * at a specified index.
     *
     * If the elements are identical it compares by the opposite index.
     * If those elements are idential it compares by System.identityHashCode.
     */
    class IntegerArrayComparator implements Comparator {
        private int index;
        public IntegerArrayComparator(int index) {
            this.index = index;
        }
        public int compare(Object alpha, Object beta) {
            int[] alphaArray = (int[])alpha;
            int[] betaArray = (int[])beta;
            int compared = alphaArray[index] - betaArray[index];
            if(compared != 0) return compared;
            compared = alphaArray[1 - index] - betaArray[1 - index];
            if(compared != 0) return compared;
            return System.identityHashCode(alpha) - System.identityHashCode(beta);
        }
    }

    /**
     * A special filter list that filters out integer arrays that don't have
     * an element lower than a specified thresshold.
     */
    class IntegerArrayFilterList extends AbstractFilterList {
        private int index;
        private int threshhold;
        public IntegerArrayFilterList(EventList source, int index, int threshhold) {
            super(source);
            this.index = index;
            this.threshhold = threshhold;
            handleFilterChanged();
        }
        public void setFilter(int index, int threshhold) {
            this.index = index;
            this.threshhold = threshhold;
            handleFilterChanged();
        }
        public boolean filterMatches(Object element) {
            int[] array = (int[])element;
            if(array[index] <= threshhold) return true;
            return false;
        }
    }
    
    /**
     * A special list change listener that verifies that the change indicies
     * within each atomic change are in increasing order.
     */
    class IncreasingChangeIndexListener implements ListEventListener {
        public void listChanged(ListEvent listChanges) {
            StringBuffer changeDescription = new StringBuffer();
            int previousChangeIndex = -1;
            boolean increasingOrder = true;
            
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();
                
                // maintain the change string
                if(changeType == ListEvent.UPDATE) {
                    changeDescription.append("U");
                } else if(changeType == ListEvent.INSERT) {
                    changeDescription.append("I");
                } else if(changeType == ListEvent.DELETE) {
                    changeDescription.append("D");
                }
                changeDescription.append(changeIndex);
                
                // see if this was a failure
                if(changeType == ListEvent.DELETE) {
                    if(changeIndex < previousChangeIndex) {
                        increasingOrder = false;
                        changeDescription.append("*");
                    }
                } else {
                    if(changeIndex <= previousChangeIndex) {
                        increasingOrder = false;
                        changeDescription.append("*");
                    }
                }
                
                // prepare for the next change
                changeDescription.append(" ");
                previousChangeIndex = changeIndex;
            }
            
            if(!increasingOrder) {
                fail("List changes not in increasing order: " + changeDescription);
            }
        }
    }
}
