/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// standard collections
import java.util.*;
// the integer array classes are useful for testing
import com.odellengineeringltd.glazedlists.util.test.*;

/**
 * This test verifies that the SortedList works.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class SortedListTest extends TestCase {

    /** the source list */
    private BasicEventList unsortedList = null;
    
    /** the sorted list */
    private SortedList sortedList = null;
    
    /** for randomly choosing list indicies */
    private Random random = new Random(2);
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
        unsortedList = new BasicEventList();
        sortedList = new SortedList(unsortedList);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        unsortedList = null;
        sortedList = null;
    }

    /**
     * Test to verify that the sorted list is working correctly when it is
     * applied to a list that already has values.
     */
    public void testSortBeforeAndAfter() {
        // populate a list with strings
        for(int i = 0; i < 4000; i++) {
            unsortedList.add(new Integer(random.nextInt()));
        }
        
        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);
        
        // verify the lists are equal
        assertEquals(controlList, sortedList);
        
        // re-sort the list
        sortedList = new SortedList(unsortedList);
        
        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }

    /**
     * Test to verify that the SortedList is working correctly when the
     * list is changing by adds, removes and deletes.
     */
    public void testSortDynamic() {
        // apply various operations to the list of Integers
        for(int i = 0; i < 4000; i++) {
            int operation = random.nextInt(4);
            int index = unsortedList.isEmpty() ? 0 : random.nextInt(unsortedList.size());
            
            if(operation <= 1 || unsortedList.isEmpty()) {
                unsortedList.add(index, new Integer(random.nextInt()));
            } else if(operation == 2) {
                unsortedList.remove(index);
            } else if(operation == 3) {
                unsortedList.set(index, new Integer(random.nextInt()));
            }
        }
        
        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);
        
        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }
    
    /**
     * Tests to verify that the SortedList correctly handles modification.
     *
     * This performs a sequence of operations. Each operation is performed on
     * either the sorted list or the unsorted list. The list where the operation
     * is performed is selected at random.
     */
    public void testSortedListWritable() {
        // apply various operations to the either list
        for(int i = 0; i < 4000; i++) {
            List list;
            if(random.nextBoolean()) list = sortedList;
            else list = unsortedList;
            int operation = random.nextInt(4);
            int index = list.isEmpty() ? 0 : random.nextInt(list.size());
            
            if(operation <= 1 || list.isEmpty()) {
                list.add(index, new Integer(random.nextInt()));
            } else if(operation == 2) {
                list.remove(index);
            } else if(operation == 3) {
                list.set(index, new Integer(random.nextInt()));
            }
        }
        
        // build a control list of the desired results
        ArrayList controlList = new ArrayList();
        controlList.addAll(unsortedList);
        Collections.sort(controlList);
        
        // verify the lists are equal
        assertEquals(controlList, sortedList);
    }


    /**
     * Tests that sorting works on a large set of filter changes.
     */
    public void testAgressiveFiltering() {
        BasicEventList source = new BasicEventList();
        IntArrayFilterList filterList = new IntArrayFilterList(source);
        SortedList sorted = new SortedList(filterList, new IntArrayComparator(0));
        
        // populate a list with 1000 random arrays between 0 and 1000
        for(int i = 0; i < 20; i++) {
            int value = random.nextInt(10);
            int[] array = new int[] { value, random.nextInt(2), random.nextInt(2), random.nextInt(2) };
            source.add(array);
        }
        
        // try ten different filters
        for(int i = 0; i < 100; i++) {
            // apply the filter
            int filterColumn = random.nextInt(3);
            filterList.setFilter(filterColumn + 1, 1);
            
            // construct the control list
            ArrayList controlList = new ArrayList();
            controlList.addAll(filterList);
            Collections.sort(controlList, new IntArrayComparator(0));
            
            // verify that the control and sorted list are the same
            System.out.println("s: " + sorted.size() + ", c: " + controlList.size());
            System.out.print("CONTROL: ");
            for(int j = 0; j < controlList.size(); j++) {
                System.out.print(((int[])controlList.get(j))[0] + ", ");
            }
            System.out.println("");
            System.out.print("FILTER: ");
            for(int j = 0; j < filterList.size(); j++) {
                System.out.print(((int[])filterList.get(j))[0] + ", ");
            }
            System.out.println("");
            System.out.print("SORTED: ");
            for(int j = 0; j < sorted.size(); j++) {
                System.out.print(((int[])sorted.get(j))[0] + ", ");
            }
            System.out.println("");
            
            assertEquals(sorted.size(), controlList.size());
            for(int j = 0; j < sorted.size(); j++) {
                assertEquals(((int[])sorted.get(j))[0], ((int[])controlList.get(j))[0]);
            }
        }
    }
}
