/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
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
     * Test to verify that the list changes occur in increasing order.
     *GlazedL
     * <p>This creates a long chain of lists designed to cause events where the indicies
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIncreasingOrder() {
        EventList<int[]> unsorted = new BasicEventList<int[]>();
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 50);
        FilterList<int[]> filteredOnce = new FilterList<int[]>(unsorted, matcherEditor);

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
     * Test to verify that the lists work with change indicies out of order.
     *
     * <p>This creates a long chain of lists designed to cause events where the indicies
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIndexOutOfOrder() {
        EventList<int[]> unsorted = new BasicEventList<int[]>();
        SortedList<int[]> sortedOnce = new SortedList<int[]>(unsorted, new IntArrayComparator(0));
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 50);
        FilterList<int[]> filteredOnce = new FilterList<int[]>(sortedOnce, matcherEditor);
        SortedList<int[]> sortedTwice = new SortedList<int[]>(filteredOnce, new IntArrayComparator(0));
        
        unsorted.addListEventListener(new IncreasingChangeIndexListener());
        sortedOnce.addListEventListener(new IncreasingChangeIndexListener());
        filteredOnce.addListEventListener(new IncreasingChangeIndexListener());
        
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
