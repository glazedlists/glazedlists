/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * A UniqueListTest tests the functionality of the UniqueList
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class UniqueListTest {

    private UniqueList<Object> unique;
    private BasicEventList<Object> source;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        source = new BasicEventList<Object>();
        unique = new UniqueList<Object>(source);
    }

    /**
     * Clean up after the test.
     */
    @After
    public void tearDown() {
        unique = null;
        source = null;
    }

    /** Testing response to INSERT event */

    /** Testing add() with an empty source list */

    @Test
    public void testSimpleInsert() {
        source.add("A");
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testSortedNonDuplicateInsert() {
        source.add("A");
        source.add("B");
        source.add("C");
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testUnsortedNonDuplicateInsert() {
        source.add("C");
        source.add("A");
        source.add("B");
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testSimpleDuplicateInsert() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testMultipleDuplicateInserts() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    /** Testing add() with a non-empty source list */

    @Test
    public void testSimpleNonEmptySource() {
        unique = null;
        source.add("A");
        unique = new UniqueList<Object>(source);
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList<Object>(source);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testUnsortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("C");
        source.add("A");
        source.add("B");
        unique = new UniqueList<Object>(source);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testSimpleDuplicateInNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList<Object>(source);
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testMultipleDuplicatesInNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList<Object>(source);
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testSimpleAddToEndOfSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList<Object>(source);
        source.add("D");
        assertEquals(4, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
        assertEquals("D", unique.get(3));
    }

    @Test
    public void testSimpleAddToFrontOfSortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("B");
        source.add("C");
        source.add("D");
        unique = new UniqueList<Object>(source);
        source.add("A");
        assertEquals(4, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
        assertEquals("D", unique.get(3));
    }

    @Test
    public void testSimpleAddToUnsortedNonDuplicateNonEmptySource() {
        unique = null;
        source.add("D");
        source.add("A");
        source.add("C");
        unique = new UniqueList<Object>(source);
        source.add("B");
        assertEquals(4, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
        assertEquals("D", unique.get(3));
    }

    @Test
    public void testAddingSimpleDuplicateToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList<Object>(source);
        source.add("C");
        source.add("C");
        source.add("C");
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testAddingMultipleDuplicatesToNonEmptySource() {
        unique = null;
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.add("A");
        source.add("A");
        unique = new UniqueList<Object>(source);
        source.add("D");
        source.add("D");
        source.add("D");
        source.add("B");
        source.add("B");
        source.add("B");
        assertEquals(4, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
        assertEquals("D", unique.get(3));
    }

    /** Testing response to addAll() */

    @Test
    public void testSimpleAddOfSortedCollection() {
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("A");
        duplicates.add("B");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testSimpleAddOfUnsortedCollection() {
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("B");
        duplicates.add("C");
        duplicates.add("A");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testAddOfCollectionContainingDuplicates() {
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("A");
        duplicates.add("C");
        duplicates.add("A");
        duplicates.add("C");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testAddingCollectionDuplicatingContentsOfNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("B");
        source.add("C");
        unique = new UniqueList<Object>(source);
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("A");
        duplicates.add("B");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testAddingCollectionOfSingleUniqueToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList<Object>(source);
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("B");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testAddingCollectionWithNewDuplicatesToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("A");
        source.add("C");
        source.add("C");
        unique = new UniqueList<Object>(source);
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testAddingCollectionOfDuplicatesToNonEmptySource() {
        unique = null;
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        source.add("C");
        unique = new UniqueList<Object>(source);
        List<Object> duplicates = new LinkedList<Object>();
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("B");
        duplicates.add("A");
        duplicates.add("C");
        source.addAll(duplicates);
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    /** Testing response to REMOVE event */

    @Test
    public void testSimpleRemoveByIndex() {
        source.add("A");
        source.remove(0);
        assertEquals(0, unique.size());
    }

    @Test
    public void testRemoveOfSingleDuplicateByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(2);
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testRemoveOfDuplicatesByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(2);
        source.remove(1);
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
    public void testSimpleRemoveOfOriginalByIndex() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove(0);
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
        assertEquals("E", unique.get(2));
    }

    @Test
    public void testSimpleRemoveByObject() {
        source.add("A");
        source.remove("A");
        assertEquals(0, unique.size());
    }

    @Test
    public void testSimpleRemoveWithMultipleUniqueValuesByObject() {
        source.add("A");
        source.add("B");
        source.add("C");
        source.remove("B");
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testRemoveOfASingleDuplicateByObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove("A");
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
    public void testRemoveOfAllDuplicatesByObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.remove("A");
        source.remove("A");
        source.remove("A");
        assertEquals(1, unique.size());
        assertEquals("A", unique.get(0));
    }

    @Test
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

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
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

    @Test
    public void testUpdateDeleteCollide() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 2, 0, 1 });
        sourceList.add(new int[] { 2, 0, 1 });
        sourceList.add(new int[] { 3, 0, 1 });
        sourceList.add(new int[] { 4, 1, 0 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(2, 1);
        matcherEditor.setFilter(1, 1);
    }

    /**
     * Tests the change from A, B, B, D to A, C, C, D
     */
    @Test
    public void testMultipleDeleteWithMultipleInsert() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 1 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 3, 0, 1 });
        sourceList.add(new int[] { 3, 0, 1 });
        sourceList.add(new int[] { 4, 1, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);

        assertEquals(3, uniqueList.size());
        assertEquals(1, uniqueList.get(0)[0]);
        assertEquals(3, uniqueList.get(1)[0]);
        assertEquals(4, uniqueList.get(2)[0]);
    }

    /**
     * Tests the change from A, B, D to A, C, D
     */
    @Test
    public void testDeleteWithInsert() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 1 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 3, 0, 1 });
        sourceList.add(new int[] { 4, 1, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);

        assertEquals(3, uniqueList.size());
        assertEquals(1, uniqueList.get(0)[0]);
        assertEquals(3, uniqueList.get(1)[0]);
        assertEquals(4, uniqueList.get(2)[0]);
    }

    /**
     * Tests the change from A, B, C to C, D, E
     */
    @Test
    public void testSingleValueKept() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 0 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 3, 1, 1 });
        sourceList.add(new int[] { 4, 0, 1 });
        sourceList.add(new int[] { 5, 0, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);

        assertEquals(3, uniqueList.size());
        assertEquals(3, uniqueList.get(0)[0]);
        assertEquals(4, uniqueList.get(1)[0]);
        assertEquals(5, uniqueList.get(2)[0]);
    }


    /**
     * Tests the change from A, A, B, B, C, C to C, C, D, D, E, E
     */
    @Test
    public void testMultipleValuesKept() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 0 });
        sourceList.add(new int[] { 1, 1, 0 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 2, 1, 0 });
        sourceList.add(new int[] { 3, 1, 1 });
        sourceList.add(new int[] { 3, 1, 1 });
        sourceList.add(new int[] { 4, 0, 1 });
        sourceList.add(new int[] { 4, 0, 1 });
        sourceList.add(new int[] { 5, 0, 1 });
        sourceList.add(new int[] { 5, 0, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);

        assertEquals(3, uniqueList.size());
        assertEquals(3, uniqueList.get(0)[0]);
        assertEquals(4, uniqueList.get(1)[0]);
        assertEquals(5, uniqueList.get(2)[0]);
    }


    /**
     * Tests the change from A, A, B, B, C, C, D, D, E, E to B, B, E, E
     */
    @Test
    public void testSubset() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 0 });
        sourceList.add(new int[] { 1, 1, 0 });
        sourceList.add(new int[] { 2, 1, 1 });
        sourceList.add(new int[] { 2, 1, 1 });
        sourceList.add(new int[] { 3, 1, 0 });
        sourceList.add(new int[] { 3, 1, 0 });
        sourceList.add(new int[] { 4, 1, 1 });
        sourceList.add(new int[] { 4, 1, 1 });
        sourceList.add(new int[] { 5, 1, 0 });
        sourceList.add(new int[] { 5, 1, 0 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);

        assertEquals(2, uniqueList.size());
        assertEquals(2, uniqueList.get(0)[0]);
        assertEquals(4, uniqueList.get(1)[0]);
    }

    /**
     * Tests the change from A, A, B, B, C to empty to A, B, B, C, C
     */
    @Test
    public void testMultipleChanges() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        sourceList.add(new int[] { 1, 1, 0, 0 });
        sourceList.add(new int[] { 1, 1, 0, 1 });
        sourceList.add(new int[] { 2, 1, 0, 1 });
        sourceList.add(new int[] { 2, 1, 0, 1 });
        sourceList.add(new int[] { 3, 1, 0, 1 });
        sourceList.add(new int[] { 3, 0, 0, 1 });

        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        matcherEditor.setFilter(1, 1);
        matcherEditor.setFilter(2, 1);
        matcherEditor.setFilter(3, 1);

        assertEquals(3, uniqueList.size());
        assertEquals(1, uniqueList.get(0)[0]);
        assertEquals(2, uniqueList.get(1)[0]);
        assertEquals(3, uniqueList.get(2)[0]);
    }

    /** the dice for the random tests */
    private Random random = new Random();

    /**
     * Tests a large set of random events.
     */
    @Test
    public void testLargeRandomSet() {
        BasicEventList<int[]> sourceList = new BasicEventList<int[]>();
        IntegerArrayMatcherEditor matcherEditor = new IntegerArrayMatcherEditor(0, 0);
        FilterList<int[]> filterList = new FilterList<int[]>(sourceList, matcherEditor);
        UniqueList<int[]> uniqueList = new UniqueList<int[]>(filterList, GlazedListsTests.intArrayComparator(0));

        // populate a list with 1000 random arrays between 0 and 1000
        for(int i = 0; i < 1000; i++) {
            int value = random.nextInt(1000);
            int[] array = new int[] { value, random.nextInt(2), random.nextInt(2), random.nextInt(2) };
            sourceList.add(array);
        }

        // try ten different filters
        for(int i = 0; i < 10; i++) {
            // apply the filter
            int filterColumn = random.nextInt(3);
            matcherEditor.setFilter(filterColumn + 1, 1);

            // construct the control list
            SortedSet<int[]> controlSet = new TreeSet<int[]>(GlazedListsTests.intArrayComparator(0));
            controlSet.addAll(filterList);
            List<int[]> controlList = new ArrayList<int[]>();
            controlList.addAll(controlSet);
            Collections.sort(controlList, GlazedListsTests.intArrayComparator(0));

            // verify that the control and unique list are the same
            assertEquals(uniqueList.size(), controlList.size());
            for(int j = 0; j < uniqueList.size(); j++) {
                assertEquals(uniqueList.get(j)[0], controlList.get(j)[0]);
            }
        }
    }

    /**
     * Tests a UniqueList version of a SortedList is safe when that SortedList
     * is re-sorted.
     */
    @Test
    public void testReSortSource() {
        // create a unique list with a sorted source
        BasicEventList<Integer> sourceList = new BasicEventList<Integer>();
        SortedList<Integer> sortedList = new SortedList<Integer>(sourceList);
        UniqueList<Integer> uniqueList = new UniqueList<Integer>(sortedList);

        // populate the source
        for(int i = 0; i < 1000; i++) {
            sourceList.add(new Integer(random.nextInt(100)));
        }

        // build a control list
        SortedSet<Integer> uniqueSource = new TreeSet<Integer>();
        uniqueSource.addAll(sourceList);
        List<Integer> controlList = new ArrayList<Integer>();
        controlList.addAll(uniqueSource);

        // verify the unique list is correct initially
        assertEquals(uniqueList, controlList);

        // verify the unique list is correct when the sorted list is unsorted
        sortedList.setComparator(null);
        assertEquals(uniqueList, controlList);

        // verify the unique list is correct when the sorted list is sorted
        sortedList.setComparator(GlazedLists.reverseComparator());
        assertEquals(uniqueList, controlList);
    }


    /** Test response to an UPDATE event  */

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testRightEdgeUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.add("C");
        source.add("A");
        source.set(5, "B");
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
        assertEquals("C", unique.get(2));
    }

    @Test
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
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
    }

    @Test
    public void testUniqueEndUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.set(4, "B");
        assertEquals(2, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("B", unique.get(1));
    }

    @Test
    public void testDuplicateEndUpdateToNewObject() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("C");
        source.add("C");
        source.set(4, "D");
        assertEquals(3, unique.size());
        assertEquals("A", unique.get(0));
        assertEquals("C", unique.get(1));
        assertEquals("D", unique.get(2));
    }

    /**
     * Verify that a unique list can be cleared.
     */
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testUpdateCount() {
        unique.add("A");
        unique.add("A");
        unique.add("A");
        unique.add("B");
        unique.add("B");
        unique.add("C");

        SortedSet<Object> replacementSet = new TreeSet<Object>();
        replacementSet.addAll(source);

        // listen to changes on the unique list
        ListConsistencyListener counter = ListConsistencyListener.install(unique);

        // replace the values with the replacement set
        GlazedLists.replaceAllSorted(unique, replacementSet, true, null);

        // verify that only one event has occured
        assertEquals(3, counter.getEventCount());
    }

    /**
     * Verify that replaceAll() works in the simplest of cases.
     */
    @Test
    public void testReplaceAll() {
        unique.add("B");
        unique.add("D");
        unique.add("E");
        unique.add("F");

        SortedSet<Object> replacementSet = new TreeSet<Object>();
        replacementSet.add("A");
        replacementSet.add("B");
        replacementSet.add("C");
        replacementSet.add("D");
        replacementSet.add("G");

        GlazedLists.replaceAllSorted(unique, replacementSet, false, null);

        List<Object> controlList = new ArrayList<Object>();
        controlList.addAll(replacementSet);
        assertEquals(controlList, unique);
    }

    /**
     * Verify that replaceAll() works in a more sophisticated case.
     */
    @Test
    public void testReplaceAllRigorous() {
        for(int i = 0; i < 100; i++) {
            unique.add(new Integer(random.nextInt(100)));
        }

        SortedSet<Object> replacementSet = new TreeSet<Object>();
        for(int i = 0; i < 100; i++) {
            replacementSet.add(new Integer(random.nextInt(100)));
        }

        // listen to changes on the unique list
        ListConsistencyListener.install(unique);

        // replace the values with the replacement set
        GlazedLists.replaceAllSorted(unique, replacementSet, false, null);

        // verify that the change applies to the replacement set
        List<Object> controlList = new ArrayList<Object>();
        controlList.addAll(replacementSet);
        assertEquals(controlList, unique);
    }


    @Test
    public void testNewReplaceAll() {
        EventList<String> target = new BasicEventList<String>();
        EventList<String> source = SortedList.create(new BasicEventList<String>());

        source.addAll(GlazedListsTests.stringToList("ACDF"));

        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("ACDF"), target);

        source.add(1, "B");
        source.add(4, "E");
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("ABCDEF"), source);
        assertEquals(GlazedListsTests.stringToList("ABCDEF"), target);

        source.remove(5);
        source.remove(4);
        source.remove(0);
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("BCD"), source);
        assertEquals(GlazedListsTests.stringToList("BCD"), target);

        source.remove(1);
        source.add(2, "F");
        source.add(3, "G");
        source.add(4, "H");
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("BDFGH"), source);
        assertEquals(GlazedListsTests.stringToList("BDFGH"), target);

        source.remove(1);
        source.add(2, "F");
        source.add(4, "F");
        source.add(4, "F");
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("BFFFFGH"), source);
        assertEquals(GlazedListsTests.stringToList("BFFFFGH"), target);

        source.add(0, "A");
        source.add(0, "A");
        source.add(0, "A");
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList("AAABFFFFGH"), source);
        assertEquals(GlazedListsTests.stringToList("AAABFFFFGH"), target);

        source.clear();
        GlazedListsImpl.replaceAll(target, source, true, GlazedLists.<String>comparableComparator());
        assertEquals(GlazedListsTests.stringToList(""), source);
        assertEquals(GlazedListsTests.stringToList(""), target);
    }


    /**
     * Test indexOf() consistency
     */
    @Test
    public void testIndexOf() {
        BasicEventList<Object> source = new BasicEventList<Object>();
        UniqueList<Object> unique = new UniqueList<Object>(source);

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
    @Test
    public void testLastIndexOf() {
        BasicEventList<Object> source = new BasicEventList<Object>();
        UniqueList<Object> unique = new UniqueList<Object>(source);

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
    @Test
    public void testContains() {
        BasicEventList<Object> source = new BasicEventList<Object>();
        UniqueList<Object> unique = new UniqueList<Object>(source);

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
//    public void testReverseComparator() {
//        // prepare a unique list with data in reverse order
//        UniqueList uniqueSource = new UniqueList(new BasicEventList(), GlazedLists.reverseComparator());
//        uniqueSource.add("E");
//        uniqueSource.add("D");
//        uniqueSource.add("C");
//
//        // count changes to the unique source
//        ListEventCounter counter = new ListEventCounter();
//        uniqueSource.addListEventListener(counter);
//
//        // modify the unique list
//        SortedSet data = new TreeSet(GlazedLists.reverseComparator());
//        data.add("A");
//        data.add("B");
//        data.add("C");
//        uniqueSource.replaceAll(data);
//
//        // verify the modifications are consistent
//        List consistencyTestList = new ArrayList();
//        consistencyTestList.addAll(data);
//        assertEquals(consistencyTestList, uniqueSource);
//
//        // verify that the "D" and "E" were deleted and "A" and "B" were added
//        assertEquals(1, counter.getEventCount());
//        assertEquals(5, counter.getChangeCount(0));
//    }

    /**
     * Test that replacing the entire contents of the source list works on the
     * unique list.
     */
//    public void testSourceUpdateAll() {
//        Comparator compareAtZero = GlazedListsTests.intArrayComparator(0);
//        Comparator compareAtOne = GlazedListsTests.intArrayComparator(1);
//
//        UniqueList uniqueListZero = new UniqueList(new BasicEventList(), compareAtZero);
//        uniqueListZero.addListEventListener(new ListConsistencyListener(uniqueListZero, "uniquezero"));
//
//        UniqueList uniqueListOne = new UniqueList(uniqueListZero, compareAtOne);
//        uniqueListOne.addListEventListener(new ListConsistencyListener(uniqueListOne, "uniqueone"));
//
//        SortedSet data = new TreeSet(compareAtZero);
//        data.add(new int[] { 0, 0 });
//        data.add(new int[] { 1, 0 });
//        uniqueListZero.replaceAll(data);
//
//        assertEquals(2, uniqueListZero.size());
//        assertEquals(1, uniqueListOne.size());
//
//        uniqueListZero.replaceAll(data);
//        assertEquals(2, uniqueListZero.size());
//        assertEquals(1, uniqueListOne.size());
//    }

    /**
     * Tests that getCount() works.
     */
    @Test
    public void testGetCount() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        assertEquals(3, unique.getCount(0));
        assertEquals(1, unique.getCount(1));
        assertEquals(2, unique.getCount(2));
        assertEquals(3, unique.getCount("A"));
        assertEquals(1, unique.getCount("B"));
        assertEquals(2, unique.getCount("C"));
        assertEquals(0, unique.getCount("D"));
    }

    /**
     * Tests that getAll() works.
     */
    @Test
    public void testGetAll() {
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("B");
        source.add("C");
        source.add("C");
        assertEquals(source.subList(0, 3), unique.getAll(0));
        assertEquals(source.subList(3, 4), unique.getAll(1));
        assertEquals(source.subList(4, 6), unique.getAll(2));
        assertEquals(source.subList(0, 3), unique.getAll("A"));
        assertEquals(source.subList(3, 4), unique.getAll("B"));
        assertEquals(source.subList(4, 6), unique.getAll("C"));
        assertEquals(Collections.EMPTY_LIST, unique.getAll("D"));
    }

    /**
     * Tests that the UniqueList can handle sets on the edge of two pairs of duplicates.
     * This was inspired by a similar test case in PopularityListTest.
     */
    @Test
    public void testRightEdgeSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Ford");        // C F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Chevy");    // C C
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testLeftEdgeSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Ford");        // C F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(0, "Ford");    // F F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testUpdateSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Datsun");      // C D
        source.add("Ford");        // C D F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Dodge");    // C D F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testUpdateLeftSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Datsun");      // C D
        source.add("Ford");        // C D F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Chevy");    // C C F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testUpdateRightSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Datsun");      // C D
        source.add("Ford");        // C D F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Ford");    // C F F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testLeftUpdateSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Chevy");       // C C
        source.add("Ford");        // C C F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Chevy");    // C C F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testLeftInsertSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Chevy");       // C C
        source.add("Ford");        // C C F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Datsun");   // C D F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testLeftMoveSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Chevy");       // C C
        source.add("Ford");        // C C F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Ford");   // C F F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testRightUpdateSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Ford");        // C F
        source.add("Ford");        // C F F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Ford");   // C F F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testRightInsertSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Ford");        // C F
        source.add("Ford");        // C F F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Datsun");   // C D F
    }

    /**
     * Tests that the UniqueList can handle sets on the edge.
     */
    @Test
    public void testRightMoveSet() {
        EventList<Object> source = new BasicEventList<Object>();
        source.add("Chevy");       // C
        source.add("Ford");        // C F
        source.add("Ford");        // C F F

        UniqueList<Object> uniqueList = new UniqueList<Object>(source);
        ListConsistencyListener.install(uniqueList);

        // in sorted order changes
        source.set(1, "Chevy");    // C C F
    }

    @Test
    public void testRemoveAPair() {
        EventList<String> source = new BasicEventList<String>();
        UniqueList<String> uniqueList = UniqueList.create(source);
        ListConsistencyListener.install(uniqueList);

        source.addAll(GlazedListsTests.stringToList("AABBBD"));
        assertEquals(GlazedListsTests.stringToList("ABD"), uniqueList);

        source.remove(0);
        source.remove(0);
        assertEquals(GlazedListsTests.stringToList("BD"), uniqueList);
    }

    @Test
    public void testDispose() {
        assertEquals(1, source.updates.getListEventListeners().size());

        // disposing of the UniqueList should leave nothing listening to the source list
        unique.dispose();
        assertEquals(0, source.updates.getListEventListeners().size());
    }

    /**
     * When {@link UniqueList#set} is called, this should fire an updated event.
     */
    @Test
    public void testEventsFiredBySet() {
        source.addAll(GlazedListsTests.stringToList("AAABBBDDD"));
        ListConsistencyListener consistencyListener = ListConsistencyListener.install(unique);

        unique.set(1, "B");
        assertEquals(1, consistencyListener.getEventCount());
        assertEquals(1, consistencyListener.getChangeCount(0));

        unique.set(1, "C");
        assertEquals(2, consistencyListener.getEventCount());
        assertEquals(1, consistencyListener.getChangeCount(1));
    }

    /**
     * Test the replacement of the grouping Comparator.
     */
    @Test
    public void testSetComparator() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final UniqueList<String> uniqueList = new UniqueList<String>(source, GlazedListsTests.getFirstLetterComparator());
        ListConsistencyListener.install(uniqueList);

        source.add("Black");
        source.add("Blind");
        source.add("Bling");

        assertEquals(1, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));

        uniqueList.setComparator(GlazedListsTests.getLastLetterComparator());
        assertEquals(3, uniqueList.size());
        assertEquals("Blind", uniqueList.get(0));
        assertEquals("Bling", uniqueList.get(1));
        assertEquals("Black", uniqueList.get(2));

        uniqueList.setComparator(GlazedListsTests.getFirstLetterComparator());
        assertEquals(1, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));

        uniqueList.setComparator(null);
        assertEquals(3, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));
        assertEquals("Blind", uniqueList.get(1));
        assertEquals("Bling", uniqueList.get(2));
    }

    /**
     * Test the replacement of the grouping Comparator when there's multiple elements.
     */
    @Test
    public void testSetComparatorWithDuplicates() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final UniqueList<String> uniqueList = new UniqueList<String>(source, GlazedListsTests.getFirstLetterComparator());
        ListConsistencyListener.install(uniqueList);

        source.add("Black");
        source.add("Blind");
        source.add("Bling");
        source.add("Frack");
        source.add("Flack");
        source.add("Fling");

        assertEquals(2, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));
        assertEquals("Frack", uniqueList.get(1));

        uniqueList.setComparator(GlazedListsTests.getLastLetterComparator());
        assertEquals(3, uniqueList.size());
        assertEquals("Blind", uniqueList.get(0));
        assertEquals("Bling", uniqueList.get(1));
        assertEquals("Black", uniqueList.get(2));

        uniqueList.setComparator(GlazedListsTests.getFirstLetterComparator());
        assertEquals(2, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));
        assertEquals("Frack", uniqueList.get(1));

        uniqueList.setComparator(null);
        assertEquals(6, uniqueList.size());
        assertEquals("Black", uniqueList.get(0));
        assertEquals("Blind", uniqueList.get(1));
        assertEquals("Bling", uniqueList.get(2));
        assertEquals("Flack", uniqueList.get(3));
        assertEquals("Fling", uniqueList.get(4));
        assertEquals("Frack", uniqueList.get(5));
    }

    /**
     * Tests that previous and new values are populaetd properly.
     */
    @Test
    public void testPreviousAndNewValues() {
        final BasicEventList<String> source = new BasicEventList<String>();
        final UniqueList<String> uniqueList = new UniqueList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener.install(uniqueList);

        source.add("a");
        source.add("c");
        source.add("d");

        source.add("D");
        assertEquals(3, uniqueList.size());

        source.add(1, "b");
        assertEquals(4, uniqueList.size());

        source.add(1, "B");
        assertEquals(4, uniqueList.size());
    }

    @Test
    public void testGenerics() {
        final EventList<Integer> source = new BasicEventList<Integer>();
        final Comparator<Number> comparator = new Comparator<Number>() {
            @Override
            public int compare(Number o1, Number o2) {
                return o1.intValue() - o2.intValue();
            }
        };
        final UniqueList<Integer> unique = new UniqueList<Integer>(source, comparator);
    }

    @Test
    public void testAllPossibleGrouperStateChanges() {
        final TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>(), true);
        final UniqueList<String> uniqueList = new UniqueList<String>(source, String.CASE_INSENSITIVE_ORDER);
        ListConsistencyListener.install(uniqueList);

        // insert: new group
        source.add("A");
        source.add("C");
        source.add("E");

        // insert: join a group on the right and left
        source.add(1, "a");
        source.add(2, "c");
        assertEquals(source, GlazedListsTests.stringToList("AacCE"));

        // update: new group before and after
        source.add(2, "B");
        source.set(2, "b");
        assertEquals(source, GlazedListsTests.stringToList("AabcCE"));

        // update: new group from left group and right group
        source.set(4, "d");
        source.set(4, "e");
        source.set(4, "d");
        assertEquals(source, GlazedListsTests.stringToList("AabcdE"));

        // update: join the left group
        source.set(2, "A");
        source.set(4, "c");
        source.beginEvent();
            source.set(3, "a");
            source.set(4, "A");
        source.commitEvent();
        source.beginEvent();
            source.set(3, "e");
            source.set(4, "e");
        source.commitEvent(); // this failure proves a bug in the reporting of previous elements in ListConsistencyListener
        assertEquals(source, GlazedListsTests.stringToList("AaAeeE"));
    }

    @Test
    public void testMassUpdates() {
        final TransactionList<String> source = new TransactionList<String>(new BasicEventList<String>(), true);
        final UniqueList<String> uniqueList = new UniqueList<String>(source, String.CASE_INSENSITIVE_ORDER);

        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");
        source.add("A");

        assertEquals("A", uniqueList.get(0));

        source.beginEvent(true);
            source.set(0, "B");
            source.set(1, "B");
            source.set(2, "B");
            source.set(3, "B");
            source.set(4, "B");
        source.commitEvent();

        assertEquals("B", uniqueList.get(0));
    }
}
