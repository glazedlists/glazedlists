/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.util.*;
// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
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
    
    /**
     * Tests a UniqueList version of a SortedList is safe when that SortedList
     * is re-sorted.
     */
    public void testReSortSource() {
        // create a unique list with a sorted source
        source = new BasicEventList();
        SortedList sortedList = new SortedList(source);
        unique = new UniqueList(sortedList);
        
        // populate the source
        for(int i = 0; i < 1000; i++) {
            source.add(new Integer(random.nextInt(100)));
        }
        
        // build a control list
        SortedSet uniqueSource = new TreeSet();
        uniqueSource.addAll(source);
        ArrayList controlList = new ArrayList();
        controlList.addAll(uniqueSource);
        
        // verify the unique list is correct initially
        assertEquals(unique, controlList);
        
        // verify the unique list is correct when the sorted list is unsorted
        sortedList.setComparator(null);
        assertEquals(unique, controlList);
        
        // verify the unique list is correct when the sorted list is sorted
        sortedList.setComparator(new ReverseComparator(new ComparableComparator()));
        assertEquals(unique, controlList);
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

    /**
     * Verify that a unique list can be cleared.
     */
    public void testUniqueListClear() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("C");
        unique.clear();
        assertEquals(0, source.size());
        assertEquals(0, unique.size());
    }

    /**
     * Verify that a unique list can have elements removed.
     */
    public void testUniqueListRemoveByValue() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("C");
        unique.remove("B");
        assertEquals(4, source.size());
        assertEquals(2, unique.size());
    }

    /**
     * Verify that a unique list can have elements removed.
     */
    public void testUniqueListRemoveByIndex() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("C");
        unique.remove(1);
        assertEquals(4, source.size());
        assertEquals(2, unique.size());
    }

    /**
     * Verify that a unique list can have elements removed.
     */
    public void testUniqueSet() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("D");
        unique.set(1, "C");
        assertEquals(5, source.size());
        assertEquals(3, unique.size());
    }

    /**
     * Verifies that the UniqueList sends update events and
     * not insert/delete event combinations.
     */
    public void testUpdateCount() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("C");

        SortedSet replacementSet = new TreeSet();
        replacementSet.addAll(source);

        // listen to changes on the unique list
        ListEventCounter counter = new ListEventCounter();
        unique.addListEventListener(counter);

        // replace the values with the replacement set
        unique.replaceAll(replacementSet);

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());
        assertEquals(3, counter.getChangeCount(0));
    }

    /**
     * Verify that replaceAll() works in the simplest of cases.
     */
    public void testReplaceAll() {
        unique.add("B");
        unique.add("D");
        unique.add("E");
        unique.add("F");

        SortedSet replacementSet = new TreeSet();
        replacementSet.add("A");
        replacementSet.add("B");
        replacementSet.add("C");
        replacementSet.add("D");
        replacementSet.add("G");

        unique.replaceAll(replacementSet);

        ArrayList controlList = new ArrayList();
        controlList.addAll(replacementSet);
        assertEquals(controlList, unique);
    }

    /**
     * Verify that replaceAll() works in a more sophisticated case.
     */
    public void testReplaceAllRigorous() {
        for(int i = 0; i < 100; i++) {
            unique.add(new Integer(random.nextInt(100)));
        }

        SortedSet replacementSet = new TreeSet();
        for(int i = 0; i < 100; i++) {
            replacementSet.add(new Integer(random.nextInt(100)));
        }

        // listen to changes on the unique list
        ListEventCounter counter = new ListEventCounter();
        unique.addListEventListener(counter);

        // replace the values with the replacement set
        unique.replaceAll(replacementSet);

        // verify that only one event has occured
        assertEquals(1, counter.getEventCount());

        // verify that the change applies to the replacement set
        ArrayList controlList = new ArrayList();
        controlList.addAll(replacementSet);
        assertEquals(controlList, unique);
    }



    /**
     * Test indexOf() consistency
     */
    public void testIndexOf() {
        BasicEventList source = new BasicEventList();
        UniqueList unique = new UniqueList(source, new IntegerComparator());

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Look for the index of a 1
        int firstTestIndex = unique.indexOf(one);
        assertEquals(0, firstTestIndex);

        // Look for the index of a 5
        int secondTestIndex = unique.indexOf(five);
        assertEquals(1, secondTestIndex);

        // Look for the index of a 9
        int thirdTestIndex = unique.indexOf(nine);
        assertEquals(2, thirdTestIndex);

        // Test containment of a 10
        Integer ten = new Integer(10);
        int fourthTest = unique.indexOf(ten);
        assertEquals(-1, fourthTest);
    }

    /**
     * Test lastIndexOf() consistency
     */
    public void testLastIndexOf() {
        BasicEventList source = new BasicEventList();
        UniqueList unique = new UniqueList(source, new IntegerComparator());

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Look for the index of a 1
        int firstTestIndex = unique.lastIndexOf(one);
        assertEquals(0, firstTestIndex);

        // Look for the index of a 5
        int secondTestIndex = unique.lastIndexOf(five);
        assertEquals(1, secondTestIndex);

        // Look for the index of a 9
        int thirdTestIndex = unique.lastIndexOf(nine);
        assertEquals(2, thirdTestIndex);

        // Test containment of a 10
        Integer ten = new Integer(10);
        int fourthTest = unique.lastIndexOf(ten);
        assertEquals(-1, fourthTest);
    }

    /**
     * Test containment accuracy
     */
    public void testContains() {
        BasicEventList source = new BasicEventList();
        UniqueList unique = new UniqueList(source, new IntegerComparator());

        // Add 12 leading 1's
        Integer one = new Integer(1);
        for(int i = 0; i < 12; i++) {
            source.add(one);
        }

        // Add 13 5's in the middle
        Integer five = new Integer(5);
        for(int i = 0; i < 13; i++) {
            source.add(five);
        }

        // Add 10 trailing 9's
        Integer nine = new Integer(9);
        for(int i = 0; i < 10; i++) {
            source.add(nine);
        }

        // Test containment of a 1
        boolean firstTest = unique.contains(one);
        assertEquals(true, firstTest);

        // Test containment of a 5
        boolean secondTest = unique.contains(five);
        assertEquals(true, secondTest);

        // Test containment of a 9
        boolean thirdTest = unique.contains(nine);
        assertEquals(true, thirdTest);

        // Test containment of a 10
        Integer ten = new Integer(10);
        boolean fourthTest = unique.contains(ten);
        assertEquals(false, fourthTest);
    }
    
    /**
     * Tests that the unique list works correctly when using a renegate comparator.
     */
    public void testReverseComparator() {
        // prepare a unique list with data in reverse order
        UniqueList uniqueSource = new UniqueList(new BasicEventList(), new ReverseComparator());
        uniqueSource.add("E");
        uniqueSource.add("D");
        uniqueSource.add("C");

        // count changes to the unique source
        ListEventCounter counter = new ListEventCounter();
        uniqueSource.addListEventListener(counter);
        
        // modify the unique list
        SortedSet data = new TreeSet(new ReverseComparator());
        data.add("A");
        data.add("B");
        data.add("C");
        uniqueSource.replaceAll(data);
        
        // verify the modifications are consistent
        List consistencyTestList = new ArrayList();
        consistencyTestList.addAll(data);
        assertEquals(consistencyTestList, uniqueSource);
        
        // verify that the "D" and "E" were deleted and "A" and "B" were added
        assertEquals(1, counter.getEventCount());
        assertEquals(5, counter.getChangeCount(0));
    }

    /**
	 * Explicit comparator for Kevin's sanity!
	 */
	class IntegerComparator implements Comparator {
        public int compare(Object a, Object b) {
            int number1 = ((Integer)a).intValue();
            int number2 = ((Integer)b).intValue();

            return number1 - number2;
        }
    }
}
