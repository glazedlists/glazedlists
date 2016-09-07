/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

// for being a JUnit test case
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the EventListIterator works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IteratorTest {

    /** for randomly choosing list indices */
    private final Random random = new Random();

    /**
     * Tests to verify that the Iterator can iterate through the list both
     * forwards and backwards.
     */
    @Test
    public void testIterateThrough() {
        // create a list of values
        final EventList<Integer> originalList = new BasicEventList<Integer>();
        for (int i = 0; i < 26; i++)
            originalList.add(new Integer(i));

        // iterate through that list forwards and add the results to a new list
        final List<Integer> forwardsControlList = new ArrayList<Integer>();
        for (Iterator<Integer> i = originalList.iterator(); i.hasNext();)
            forwardsControlList.add(i.next());

        // verify the lists are equal
        assertEquals(originalList, forwardsControlList);

        // iterate through that list backwards and add the results to a new list
        final List<Integer> backwardsControlList = new ArrayList<Integer>();
        for(ListIterator<Integer> i = originalList.listIterator(originalList.size()); i.hasPrevious();)
            backwardsControlList.add(i.previous());

        Collections.reverse(backwardsControlList);

        // verify the lists are equal
        assertEquals(originalList, backwardsControlList);
    }

    /**
     * Tests to verify that the ListIterator can iterate through the list
     * while removes are performed directly on the list.
     */
    @Test
    public void testIterateWithExternalRemove() {
        // create a list of values
        final EventList<Integer> deleteFromList = new BasicEventList<Integer>();
        final List<Integer> originalList = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            final Integer value = new Integer(i);
            deleteFromList.add(value);
            originalList.add(value);
        }

        final List<Integer> iteratedElements = new ArrayList<Integer>();
        final Iterator<Integer> iterator = deleteFromList.listIterator();

        // iterate through the list forwards for the first 50 values
        for (int a = 0; a < 50; a++)
            iteratedElements.add(iterator.next());

        // delete 50 elements from the beginning of the list
        for (int a = 50; a > 0; a--)
            deleteFromList.remove(random.nextInt(a));

        // continue iterating for the last 50 values
        for (int a = 0; a < 50; a++)
            iteratedElements.add(iterator.next());

        // verify the lists are equal and that we're out of elements
        assertEquals(originalList, iteratedElements);
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests to verify that the EventListIterator and the SimpleTreeIterator can
     * iterate through the list and remove its elements as it goes without
     * incident.
     */
    @Test
    public void testIterateWithInternalRemove() {
        // create a list of values
        final EventList<Integer> iterateForwardList = new BasicEventList<Integer>();
        final EventList<Integer> iterateBackwardList = new BasicEventList<Integer>();
        final List<Integer> originalList = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            final Integer value = new Integer(random.nextInt(100));
            iterateForwardList.add(value);
            iterateBackwardList.add(value);
            originalList.add(value);
        }

        // walk through the forward lists, removing all values greater than 50
        for (ListIterator<Integer> i = iterateForwardList.listIterator(); i.hasNext();)
            if (i.next().intValue() > 50)
                i.remove();

        // walk through the backward list, removing all values greater than 50
        for (ListIterator<Integer> i = iterateBackwardList.listIterator(iterateBackwardList.size()); i.hasPrevious();)
            if (i.previous().intValue() > 50)
                i.remove();

        // verify the lists are equal and that we're out of elements
        for (int i = 0; i < originalList.size(); ) {
            if (originalList.get(i).intValue() > 50)
                originalList.remove(i);
            else
                i++;
        }

        assertEquals(originalList, iterateForwardList);
        assertEquals(originalList, iterateBackwardList);
    }

    /**
     * Tests the edge condition of the previous method.
     */
    @Test
    public void testPreviousEdgeCondition() {
        // create a list of values
        final EventList<Integer> iterationList = new BasicEventList<Integer>();
        for (int i = 0; i < 20; i++)
            iterationList.add(new Integer(random.nextInt(100)));

        final ListIterator<Integer> i = iterationList.listIterator();

        // Test before next is called
        assertFalse(i.hasPrevious());
        try {
            i.previous();
            fail("A call to previous() was allowed before next() was called");
        } catch (NoSuchElementException e) {
            // expected
        }

        // Test when the iterator is at the first element
        i.next();
        assertTrue(i.hasPrevious());
    }

    /**
     * Tests that changing the underlying list externally to the ListIterator
     * doesn't break the expectation of the remove operation.
     */
    @Test
    public void testRemoveAfterInsertAtCursor() {
        final EventList<String> testList = new BasicEventList<String>();
        String hello = "Hello, world.";
        String bye = "Goodbye, cruel world.";
        String end = "the end";
        testList.add(bye);
        testList.add(end);

        final ListIterator<String> iterator = testList.listIterator();

        // move iterator to bye
        iterator.next();
        testList.add(0, hello);
        iterator.remove();
        assertFalse(testList.contains(bye));
    }

    /**
     * Tests that changing the underlying list externally to the ListIterator
     * doesn't break the expectation of the remove operation.
     */
    @Test
    public void testRemoveAfterInsertAtNext() {
        final EventList<String> testList = new BasicEventList<String>();
        String hello = "Hello, world.";
        String bye = "Goodbye, cruel world.";
        String end = "the end";
        testList.add(bye);
        testList.add(end);

        final ListIterator<String> iterator = testList.listIterator();

        // move iterator to bye
        iterator.next();
        testList.add(1, hello);
        iterator.remove();
        assertFalse(testList.contains(bye));
    }

    /**
     * Tests that changing the underlying list externally to the ListIterator
     * doesn't break the expectation of the remove operation.
     */
    @Test
    public void testRemoveAfterInsertAtCursorReverse() {
        BasicEventList<String> testList = new BasicEventList<String>();
        String hello = "Hello, world.";
        String bye = "Goodbye, cruel world.";
        String end = "the end";
        testList.add(end);
        testList.add(bye);

        final ListIterator<String> iterator = testList.listIterator(testList.size());

        // move iterator to bye
        iterator.previous();
        testList.add(1, hello);
        iterator.remove();
        assertFalse(testList.contains(bye));
    }

    /**
     * Tests that changing the underlying list externally to the ListIterator
     * doesn't break the expectation of the remove operation.
     */
    @Test
    public void testRemoveAfterInsertAtPrevious() {
        BasicEventList<String> testList = new BasicEventList<String>();
        String hello = "Hello, world.";
        String bye = "Goodbye, cruel world.";
        String end = "the end";
        testList.add(end);
        testList.add(bye);

        final ListIterator<String> iterator = testList.listIterator(testList.size());

        // move iterator to bye
        iterator.previous();
        testList.add(0, hello);
        iterator.remove();
        assertFalse(testList.contains(bye));
    }

    /**
     * Tests that adding at a particular element does the right thing.
     */
    @Test
    public void testAdding() {
        // Create a control list to test against
        final List<String> controlList = new ArrayList<String>();
        controlList.add("zero");
        controlList.add("one");
        controlList.add("two");
        controlList.add("three");
        controlList.add("four");

        // Create a list to be iterated on forwards
        final EventList<String> forwardsList = new BasicEventList<String>();
        forwardsList.add("one");
        forwardsList.add("three");

        // Iterate through the list forwards adding in places of interest
        final ListIterator<String> iterator = forwardsList.listIterator(0);
        iterator.add("zero");
        assertEquals("one", iterator.next());
        iterator.add("two");
        assertEquals("three", iterator.next());
        iterator.add("four");
        assertEquals(controlList, forwardsList);

        // Create a list to be iterated of backwards
        final EventList<String> backwardsList = new BasicEventList<String>();
        backwardsList.add("one");
        backwardsList.add("three");

        // Iterate through the list backwards adding in places of interest
        final ListIterator<String> backwardsIterator = backwardsList.listIterator(backwardsList.size());
        backwardsIterator.add("four");
        assertEquals("four", backwardsIterator.previous());
        assertEquals("three", backwardsIterator.previous());
        backwardsIterator.add("two");
        assertEquals("two", backwardsIterator.previous());
        assertEquals("one", backwardsIterator.previous());
        backwardsIterator.add("zero");
        assertEquals("zero", backwardsIterator.previous());
        assertEquals(false, backwardsIterator.hasPrevious());
        assertEquals(controlList, backwardsList);
    }

    /**
     * Tests empty list adds
     */
    @Test
    public void testEmptyListAdding() {
        final List<String> testList = new BasicEventList<String>();
        final ListIterator<String> iterator = testList.listIterator();
        iterator.add("just one element");

        assertEquals(1, testList.size());
        assertFalse(iterator.hasNext());
        assertTrue(iterator.hasPrevious());
    }

    /**
     * Tests that the EventListIterator responds correctly to adding on an
     * empty list from an external call to remove().
     */
    @Test
    public void testExternalAddingOnEmptyList() {
        final EventList<String> testList = new BasicEventList<String>();
        final ListIterator<String> iterator = testList.listIterator();
        assertFalse(iterator.hasPrevious());
        assertFalse(iterator.hasNext());

        // add one element to validate the iterator responds accordingly
        String hello = "hello, world";
        testList.add(hello);
        assertTrue(iterator.hasPrevious());
        assertFalse(iterator.hasNext());
        assertEquals(hello, iterator.previous());
        assertFalse(iterator.hasPrevious());
        assertTrue(iterator.hasNext());
    }

    /**
     * This manually executed test runs forever creating iterators and
     * testing how memory responds.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=316">Issue 316</a>
     */
    public static void main(String[] args) {
        final List<String> list = new BasicEventList<String>();
        list.addAll(GlazedListsTests.stringToList("ABCDEFGHIJK"));

        long memoryUsage = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(1024);
        int repetitions = 0;

        while (true) {
            // iterate the list a few times, with each iterator type
            for(Iterator regularIterator = list.iterator(); regularIterator.hasNext(); ) {
                regularIterator.next();
            }
            for(ListIterator listIterator = list.listIterator(); listIterator.hasNext(); ) {
                listIterator.next();
            }

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
}
