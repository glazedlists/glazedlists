/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.util.*;
// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A UniqueListTest tests the functionality of the UniqueList
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class UniqueListTest extends TestCase {

    private UniqueList unique = null;
    private BasicEventList source = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source = new BasicEventList();
        unique = new UniqueList(source, new ComparableComparator());
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        unique = null;
        source = null;
    }

    /** Testing response to INSERT event */

    /** Testing add() with an empty source list */

    public void testSimpleInsert() {
        source.add("A");
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testSortedNonDuplicateInsert() {
        source.add("A");
        source.add("B");
        source.add("C");
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testUnsortedNonDuplicateInsert() {
        source.add("C");
        source.add("A");
        source.add("B");
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testSimpleDuplicateInsert() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testMultipleDuplicateInserts() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    /** Testing add() with a non-empty source list */

    public void testSimpleNonEmptySource() {
        unique = null;
        source.add("A");
        unique = new UniqueList(source, new ComparableComparator());
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testUnsortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("C");
        source.add("A");
        source.add("B");
        unique = new UniqueList(source, new ComparableComparator());
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testSimpleDuplicateInNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList(source, new ComparableComparator());
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testMultipleDuplicatesInNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testSimpleAddToEndOfSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        source.add("D");
        assertEquals(4, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
        assertEquals("D", (String)unique.get(3));
    }

    public void testSimpleAddToFrontOfSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("B");
        source.add("C");
        source.add("D");
        unique = new UniqueList(source, new ComparableComparator());
        source.add("A");
        assertEquals(4, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
        assertEquals("D", (String)unique.get(3));
    }

    public void testSimpleAddToUnsortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("D");
        source.add("A");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        source.add("B");
        assertEquals(4, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
        assertEquals("D", (String)unique.get(3));
    }

    public void testAddingSimpleDuplicateToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList(source, new ComparableComparator());
        source.add("C");
        source.add("C");
        source.add("C");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testAddingMultipleDuplicatesToNonEmptySource() {
        unique = null;
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList(source, new ComparableComparator());
        source.add("D");
        source.add("D");
        source.add("D");
        source.add("B");
        source.add("B");
        source.add("B");
        assertEquals(4, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
        assertEquals("D", (String)unique.get(3));
    }

    /** Testing response to addAll() */

    public void testSimpleAddOfSortedCollection() {
        LinkedList duplicates = new LinkedList();
        duplicates.add("A");
        duplicates.add("B");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testSimpleAddOfUnsortedCollection() {
        LinkedList duplicates = new LinkedList();
        duplicates.add("B");
        duplicates.add("C");
        duplicates.add("A");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testAddOfCollectionContainingDuplicates() {
        LinkedList duplicates = new LinkedList();
        duplicates.add("A");
        duplicates.add("C");
        duplicates.add("A");
        duplicates.add("C");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testAddingCollectionDuplicatingContentsOfNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        LinkedList duplicates = new LinkedList();
        duplicates.add("A");
        duplicates.add("B");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testAddingCollectionOfSingleUniqueToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        LinkedList duplicates = new LinkedList();
        duplicates.add("B");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testAddingCollectionWithNewDuplicatesToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("A");
        source.add("C");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        LinkedList duplicates = new LinkedList();
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testAddingCollectionOfDuplicatesToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList(source, new ComparableComparator());
        LinkedList duplicates = new LinkedList();
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    /** Testing response to REMOVE event */

    public void testSimpleRemoveByIndex() {
        source.add("A");
        source.remove(0);
        assertEquals(0, unique.size());
    }

    public void testRemoveOfSingleDuplicateByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(2);
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testRemoveOfDuplicatesByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(2);
        source.remove(1);
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testRemoveWithMultipleDuplicatesByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        source.remove(6);
        source.remove(5);
        source.remove(2);
        source.remove(1);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testSimpleRemoveOfOriginalByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(0);
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testRemoveOfMultipleOriginalsByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("E");
        source.add("E");
        source.add("E");
        source.remove(0);
        source.remove(3);
        source.remove(6);
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
        assertEquals("E", (String)unique.get(2));
    }

    public void testSimpleRemoveByObject() {
        source.add("A");
        source.remove("A");
        assertEquals(0, unique.size());
    }

    public void testSimpleRemoveWithMultipleUniqueValuesByObject() {
        source.add("A");
        source.add("B");
        source.add("C");
        source.remove("B");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testRemoveOfASingleDuplicateByObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove("A");
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testRemoveOfAllDuplicatesByObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove("A");
        source.remove("A");
        source.remove("A");
        assertEquals(1, unique.size());
        assertEquals("A", (String)unique.get(0));
    }

    public void testRemoveOfAllValuesByObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove("A");
        source.remove("A");
        source.remove("A");
        source.remove("A");
        assertEquals(0, unique.size());
    }

    public void testRemoveOfMultipleDuplicatesByObject() {
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.remove("A");
        source.remove("C");
        source.remove("C");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testClear() {
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("D");
        source.add("E");
        source.add("E");
        source.clear();
        assertEquals(0, unique.size());
    }

    public void testUpdateDeleteCollide() {
        source = new BasicEventList();
        source.add(new int[] { 2, 0, 1 });
        source.add(new int[] { 2, 0, 1 });
        source.add(new int[] { 3, 0, 1 });
        source.add(new int[] { 4, 1, 0 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(2, 1);
        filterList.setFilter(1, 1);
    }

    /**
     * Tests the change from A, B, B, D to A, C, C, D
     */
    public void testMultipleDeleteWithMultipleInsert() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 1 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 3, 0, 1 });
        source.add(new int[] { 3, 0, 1 });
        source.add(new int[] { 4, 1, 1 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        
        assertEquals(3, unique.size());
        assertEquals(1, ((int[])unique.get(0))[0]);
        assertEquals(3, ((int[])unique.get(1))[0]);
        assertEquals(4, ((int[])unique.get(2))[0]);
    }

    /**
     * Tests the change from A, B, D to A, C, D
     */
    public void testDeleteWithInsert() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 1 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 3, 0, 1 });
        source.add(new int[] { 4, 1, 1 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        
        assertEquals(3, unique.size());
        assertEquals(1, ((int[])unique.get(0))[0]);
        assertEquals(3, ((int[])unique.get(1))[0]);
        assertEquals(4, ((int[])unique.get(2))[0]);
    }

    /**
     * Tests the change from A, B, C to C, D, E
     */
    public void testSingleValueKept() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 0 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 3, 1, 1 });
        source.add(new int[] { 4, 0, 1 });
        source.add(new int[] { 5, 0, 1 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        
        assertEquals(3, unique.size());
        assertEquals(3, ((int[])unique.get(0))[0]);
        assertEquals(4, ((int[])unique.get(1))[0]);
        assertEquals(5, ((int[])unique.get(2))[0]);
    }


    /**
     * Tests the change from A, A, B, B, C, C to C, C, D, D, E, E
     */
    public void testMultipleValuesKept() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 0 });
        source.add(new int[] { 1, 1, 0 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 2, 1, 0 });
        source.add(new int[] { 3, 1, 1 });
        source.add(new int[] { 3, 1, 1 });
        source.add(new int[] { 4, 0, 1 });
        source.add(new int[] { 4, 0, 1 });
        source.add(new int[] { 5, 0, 1 });
        source.add(new int[] { 5, 0, 1 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        
        assertEquals(3, unique.size());
        assertEquals(3, ((int[])unique.get(0))[0]);
        assertEquals(4, ((int[])unique.get(1))[0]);
        assertEquals(5, ((int[])unique.get(2))[0]);
    }
    
    
    /**
     * Tests the change from A, A, B, B, C, C, D, D, E, E to B, B, E, E
     */
    public void testSubset() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 0 });
        source.add(new int[] { 1, 1, 0 });
        source.add(new int[] { 2, 1, 1 });
        source.add(new int[] { 2, 1, 1 });
        source.add(new int[] { 3, 1, 0 });
        source.add(new int[] { 3, 1, 0 });
        source.add(new int[] { 4, 1, 1 });
        source.add(new int[] { 4, 1, 1 });
        source.add(new int[] { 5, 1, 0 });
        source.add(new int[] { 5, 1, 0 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        
        assertEquals(2, unique.size());
        assertEquals(2, ((int[])unique.get(0))[0]);
        assertEquals(4, ((int[])unique.get(1))[0]);
    }

    /**
     * Tests the change from A, A, B, B, C to empty to A, B, B, C, C
     */
    public void testMultipleChanges() {
        source = new BasicEventList();
        source.add(new int[] { 1, 1, 0, 0 });
        source.add(new int[] { 1, 1, 0, 1 });
        source.add(new int[] { 2, 1, 0, 1 });
        source.add(new int[] { 2, 1, 0, 1 });
        source.add(new int[] { 3, 1, 0, 1 });
        source.add(new int[] { 3, 0, 0, 1 });

        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));

        filterList.setFilter(1, 1);
        filterList.setFilter(2, 1);
        filterList.setFilter(3, 1);
        
        assertEquals(3, unique.size());
        assertEquals(1, ((int[])unique.get(0))[0]);
        assertEquals(2, ((int[])unique.get(1))[0]);
        assertEquals(3, ((int[])unique.get(2))[0]);
    }
    
    /** the dice for the random tests */
    private Random random = new Random();

    /**
     * Tests a large set of random events.
     */
    public void testLargeRandomSet() {
        source = new BasicEventList();
        IntArrayFilterList filterList = new IntArrayFilterList(source);
        unique = new UniqueList(filterList, new IntArrayComparator(0));
        SortedList sorted = new SortedList(filterList, new IntArrayComparator(0));
        
        // populate a list with 1000 random arrays between 0 and 1000
        for(int i = 0; i < 1000; i++) {
            int value = random.nextInt(1000);
            int[] array = new int[] { value, random.nextInt(2), random.nextInt(2), random.nextInt(2) };
            source.add(array);
        }
        
        // try ten different filters
        for(int i = 0; i < 10; i++) {
            // apply the filter
            int filterColumn = random.nextInt(3);
            filterList.setFilter(filterColumn + 1, 1);
            
            // construct the control list
            SortedSet controlSet = new TreeSet(new IntArrayComparator(0));
            controlSet.addAll(filterList);
            ArrayList controlList = new ArrayList();
            controlList.addAll(controlSet);
            Collections.sort(controlList, new IntArrayComparator(0));
            
            // verify that the control and unique list are the same
            assertEquals(unique.size(), controlList.size());
            for(int j = 0; j < unique.size(); j++) {
                assertEquals(((int[])unique.get(j))[0], ((int[])controlList.get(j))[0]);
            }
        }
    }


    /** Test response to an UPDATE event  */

    public void testLeftEdgeUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(0, "B");
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testLeftEdgeUpdateToLeftDuplicateObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(6, "A");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testLeftEdgeUpdateToRightDuplicateObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(0, "C");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testRightEdgeUpdateToNewObject() {;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(5, "B");
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
        assertEquals("C", (String)unique.get(2));
    }

    public void testRightEdgeUpdateToRightDuplicateObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(5, "C");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
    }

    public void testUniqueEndUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.set(4, "B");
        assertEquals(2, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("B", (String)unique.get(1));
    }

    public void testDuplicateEndUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.set(4, "D");
        assertEquals(3, unique.size());
        assertEquals("A", (String)unique.get(0));
        assertEquals("C", (String)unique.get(1));
        assertEquals("D", (String)unique.get(2));
    }
}