/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.util.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the EventListIterator works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IteratorTest extends TestCase {
    
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
     * Tests to verify that the Iterator can iterate through the list both
     * forwards and backwards.
     */
    public void testIterateThrough() {
        // create a list of values
        BasicEventList originalList = new BasicEventList();
        for(int i = 0; i < 26; i++) {
            originalList.add(new Integer(i));
        }
        
        // iterate through that list forwards and add the results to a new list
        List forwardsControlList = new ArrayList();
        for(Iterator i = originalList.iterator(); i.hasNext(); ) {
            forwardsControlList.add(i.next());
        }
        
        // verify the lists are equal
        assertEquals(forwardsControlList, originalList);

        // iterate through that list backwards and add the results to a new list
        List backwardsControlList = new ArrayList();
        for(ListIterator i = originalList.listIterator(originalList.size()); i.hasPrevious(); ) {
            backwardsControlList.add(i.previous());
        }
        Collections.reverse(backwardsControlList);
        
        // verify the lists are equal
        assertEquals(backwardsControlList, originalList);
    }

    /**
     * Tests to verify that the Iterator can iterate through the list both
     * and remove its elements.
     */
    public void testIterateWithExternalRemove() {
        // create a list of values
        BasicEventList deleteFromList = new BasicEventList();
        ArrayList originalList = new ArrayList();
        for(int i = 0; i < 100; i++) {
            Object value = new Integer(i);
            deleteFromList.add(value);
            originalList.add(value);
        }
        
        List iteratedElements = new ArrayList();
        Iterator iterator = deleteFromList.iterator();

        // iterate through the list forwards for the first 50 values
        for(int a = 0; a < 50; a++) {
            iteratedElements.add(iterator.next());
        }
        // delete 50 elements from the beginning of the list
        for(int a = 50; a > 0; a--) {
            deleteFromList.remove(random.nextInt(a));
        }
        // continue iterating for the last 50 values
        for(int a = 0; a < 50; a++) {
            iteratedElements.add(iterator.next());
        }
        
        // verify the lists are equal and that we're out of elements
        assertEquals(originalList, iteratedElements);
        assertFalse(iterator.hasNext());
    }


    /**
     * Tests to verify that the Iterator can iterate through the list both
     * and remove its elements.
     */
    public void testIterateWithInternalRemove() {
        // create a list of values
        BasicEventList iterateForwardList = new BasicEventList();
        BasicEventList iterateBackwardList = new BasicEventList();
        ArrayList originalList = new ArrayList();
        for(int i = 0; i < 20; i++) {
            Integer value = new Integer(random.nextInt(100));
            iterateForwardList.add(value);
            iterateBackwardList.add(value);
            originalList.add(value);
        }
        
        // walk through the forward lists, removing all values greater than 50
        for(ListIterator i = iterateForwardList.listIterator(); i.hasNext(); ) {
            Integer current = (Integer)i.next();
            if(current.intValue() > 50) i.remove();
        }
        
        // walk through the backward list, removing all values greater than 50
        for(ListIterator i = iterateBackwardList.listIterator(iterateBackwardList.size()); i.hasPrevious(); ) {
            Integer current = (Integer)i.previous();
            if(current.intValue() > 50) i.remove();
        }
        
        // verify the lists are equal and that we're out of elements
        for(int i = 0; i < originalList.size(); ) {
            Integer current = (Integer)originalList.get(i);
            if(current.intValue() > 50) originalList.remove(i);
            else i++;
        }
        assertEquals(originalList, iterateForwardList);
        assertEquals(originalList, iterateBackwardList);
    }
    
    /**
     * This manually executed test runs forever creating iterators and
     * sublists of a source list, and modifying that list.
     */
    public static void main(String[] args) {
        List list = new BasicEventList();
        long memoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024);
        int repetitions = 0;
        Random random = new Random();
        
        while(true) {
            // perform a random operation on this list
            int operation = random.nextInt(3);
            int index = list.isEmpty() ? 0 : random.nextInt(list.size());
            if(operation <= 1 || list.isEmpty()) {
                list.add(index, new Integer(random.nextInt()));
            } else if(operation == 2) {
                list.remove(index);
            } else if(operation == 3) {
                list.set(index, new Integer(random.nextInt()));
            }
            
            // create an iterator for this list
            Iterator iterator = list.iterator();
            // create a SubList for this list
            List subList = list.subList(0, list.size()/2);
            
            // test and output memory usage
            long newMemoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024);
            if(newMemoryUsage > memoryUsage) {
                memoryUsage = newMemoryUsage;
                System.out.println(repetitions + ": " + memoryUsage + "k, HIGHER");
            } else if(repetitions % 10000 == 0) {
                System.out.println(repetitions + ": " + newMemoryUsage + "k");
            }
            
            repetitions++;
        }
    }

    /**
     * Tests that the iterator works despite changes to the set of listeners.
     */
    public void testIterateLists() {
        BasicEventList list = new BasicEventList();
        for(int i = 0; i < 10000; i++) {
            Iterator iterator = list.iterator();
            list.add(Boolean.TRUE);
            while(iterator.hasNext()) {
                fail("Iterator is out of date");
            }
            list.clear();
        }
    }
}
