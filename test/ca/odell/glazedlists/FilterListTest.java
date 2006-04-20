/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import junit.framework.TestCase;
import ca.odell.glazedlists.matchers.*;
import java.util.*;

/**
 * Tests the generic FilterList class.
 */
public class FilterListTest extends TestCase {

    /**
     * This test demonstrates Issue 213.
     */
    public void testRelax() {
        // construct a (contrived) list of initial values
        EventList<Integer> original = new BasicEventList<Integer>();
        List<Integer> values = GlazedListsTests.intArrayToIntegerCollection(new int [] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 1 });
        original.addAll(values);
        
        // prepare a filter to filter our list
        MinimumValueMatcherEditor editor = new MinimumValueMatcherEditor();
        FilterList<Integer> myFilterList = new FilterList<Integer>(original, editor);
        ListConsistencyListener.install(myFilterList);
        
        // relax the list
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        
        // now try constrain
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 });
        original.clear();
        original.addAll(values);
        
        // constrain the list
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(12);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

        // now try more changes
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 8, 6, 7, 5, 3, 0, 9 });
        original.clear();
        original.addAll(values);

        // constrain the list
        editor.setMinimum(5);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

    }

	/**
	 * Test Matchers that fire matchAll() and matchNone() events.
	 */
	public void testMatchAllOrNothing() {
		EventList<Integer> baseList = new BasicEventList<Integer>();
		baseList.add(new Integer(1));
		baseList.add(new Integer(2));
		baseList.add(new Integer(3));
		baseList.add(new Integer(4));
		baseList.add(new Integer(5));

		AllOrNothingMatcherEditor matcher = new AllOrNothingMatcherEditor();
		FilterList<Integer> filterList = new FilterList<Integer>(baseList,matcher);
		ListConsistencyListener.install(filterList);

		// Test initial size
		assertEquals(5, filterList.size());

		// Clear it
		matcher.showAll(false);
		assertEquals(0, filterList.size());

		// Clear it again
		matcher.showAll(false);
		assertEquals(0, filterList.size());

		// Put it back
		matcher.showAll(true);
		assertEquals(5, filterList.size());

		// Put it back again
		matcher.showAll(true);
		assertEquals(5, filterList.size());
	}

    public void testConstructorLocking_Matcher() throws InterruptedException {
        // create a list which will record our multithreaded interactions with a list
        final ThreadRecorderEventList<Integer> atomicList = new ThreadRecorderEventList<Integer>(new BasicEventList<Integer>());

        // start a thread which adds new Integers every 50 ms
        final Thread writerThread = new Thread(GlazedListsTests.createJerkyAddRunnable(atomicList, null, 2000, 50), "WriterThread");
        writerThread.start();

        // make sure the writerThread has started writing
        Thread.sleep(200);

        // create a list whose get() method pauses for 50 ms before returning the value
        final EventList<Integer> delayList = GlazedListsTests.delayList(atomicList, 50);

        // the test: creating the FilterList should be atomic and pause the writerThread while it initializes its internal state
        new FilterList(delayList, Matchers.trueMatcher());

        // wait until the writerThread finishes before asserting the recorded state
        writerThread.join();

        // correct locking should have produced a thread log like: WriterThread(n) main(n) ...
        // correct locking should have produced a read/write pattern like: n Writes n Reads ...

        // count the number of initial writes
        int startingWrites = 0;
        for (Iterator<String> i = atomicList.getReadWriteLog().iterator(); i.hasNext(); startingWrites++) {
            if (!"W".equals(i.next()))
                break;
        }

        // check if the number of reads following the initial writes matches 1 for 1
        // (which indicates we locked the pipeline during the initialization of FilterList)
        int i = startingWrites;
        for (; i < startingWrites * 2; i++) {
            assertEquals("R", atomicList.getReadWriteLog().get(i));
        }

        // the next operation should be a Write occurring after the initialization
        assertEquals("W", atomicList.getReadWriteLog().get(i));
    }

    public void testConstructorLocking_MatcherEditor() throws InterruptedException {
        // create a list which will record our multithreaded interactions with a list
        final ThreadRecorderEventList<Integer> atomicList = new ThreadRecorderEventList<Integer>(new BasicEventList<Integer>());

        // start a thread which adds new Integers every 50 ms
        final Thread writerThread = new Thread(GlazedListsTests.createJerkyAddRunnable(atomicList, new Integer(0), 2000, 50), "WriterThread");
        writerThread.start();

        // make sure the writerThread has started writing
        Thread.sleep(200);

        // create a list whose get() method pauses for 50 ms before returning the value
        final EventList<Integer> delayList = GlazedListsTests.delayList(atomicList, 50);

        // the test: creating the FilterList should be atomic and pause the writerThread while it initializes its internal state
        new FilterList(delayList, new RangeMatcherEditor());

        // wait until the writerThread finishes before asserting the recorded state
        writerThread.join();

        // correct locking should have produced a thread log like: WriterThread(n) main(n) ...
        // correct locking should have produced a read/write pattern like: n Writes n Reads ...

        // count the number of initial writes
        int startingWrites = 0;
        for (Iterator<String> i = atomicList.getReadWriteLog().iterator(); i.hasNext(); startingWrites++) {
            if (!"W".equals(i.next()))
                break;
        }

        // check if the number of reads following the initial writes matches 1 for 1
        // (which indicates we locked the pipeline during the initialization of FilterList)
        int i = startingWrites;
        for (; i < startingWrites * 2; i++) {
            assertEquals("R", atomicList.getReadWriteLog().get(i));
        }

        // the next operation should be a Write occurring after the initialization
        assertEquals("W", atomicList.getReadWriteLog().get(i));
    }
}


/**
 * A MatcherEditor for minimum values.
 */
class MinimumValueMatcherEditor extends AbstractMatcherEditor<Integer> {
    private int minimum = 0;
    public MinimumValueMatcherEditor() {
        minimum = 0;
        currentMatcher = GlazedListsTests.matchAtLeast(0);
    }
    public void setMinimum(int value) {
        if(value < minimum) {
            this.minimum = value;
            fireRelaxed(GlazedListsTests.matchAtLeast(minimum));
        } else if(value == minimum) {
            // do nothing
        } else {
            this.minimum = value;
            fireConstrained(GlazedListsTests.matchAtLeast(minimum));
        }
    }
}

/**
 * Matcher that allows testing matchAll() and matchNone().
 */
class AllOrNothingMatcherEditor extends AbstractMatcherEditor {
    /**
     * @param state True show everything, otherwise show nothing
     */
    public void showAll(boolean state) {
        if (state) fireMatchAll();
        else fireMatchNone();
    }
}
