/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.Matchers;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Collections;

/**
 * This test verifies that the EventSelectionModel works.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventSelectionModelTest extends SwingTestCase {

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
    }

    /**
     * Tests that selection survives a sorting.
     */
    public void guiTestSort() {
        BasicEventList<Comparable> list = new BasicEventList<Comparable>();
        SortedList<Comparable> sorted = new SortedList<Comparable>(list, null);
        EventSelectionModel<Comparable> eventSelectionModel = new EventSelectionModel<Comparable>(sorted);
        
        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("E C F B A D"));

        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        
        // select the vowels
        eventSelectionModel.addSelectionInterval(0, 0);
        eventSelectionModel.addSelectionInterval(4, 4);
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel.getSelected());
        
        // flip the list
        sorted.setComparator(GlazedLists.comparableComparator());
        assertEquals(GlazedListsTests.delimitedStringToList("A E"), eventSelectionModel.getSelected());
        
        // flip the list again
        sorted.setComparator(GlazedLists.reverseComparator());
        assertEquals(GlazedListsTests.delimitedStringToList("E A"), eventSelectionModel.getSelected());
    }

    /**
     * Verifies that the selected index is cleared when the selection is cleared.
     */
    public void guiTestClear() {
        BasicEventList<String> list = new BasicEventList<String>();
        EventSelectionModel eventSelectionModel = new EventSelectionModel<String>(list);

        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));

        // make a selection
        eventSelectionModel.addSelectionInterval(1, 4);

        // test the selection
        assertEquals(list.subList(1, 5), eventSelectionModel.getSelected());

        // clear the selection
        eventSelectionModel.clearSelection();

        // test the selection
        assertEquals(Collections.EMPTY_LIST, eventSelectionModel.getSelected());
        assertEquals(-1, eventSelectionModel.getMinSelectionIndex());
        assertEquals(-1, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(true, eventSelectionModel.isSelectionEmpty());
    }

    /**
     * Tests a problem where the {@link EventSelectionModel} fails to fire events
     *
     * This test was contributed by: Sergey Bogatyrjov
     */
    public void guiTestSelectionModel() {
        EventList<Object> source = new BasicEventList<Object>();
        source.addAll(GlazedListsTests.delimitedStringToList("one two three"));
        FilterList<Object> filtered = new FilterList<Object>(source, Matchers.trueMatcher());

        // create selection model
        EventSelectionModel<Object> model = new EventSelectionModel<Object>(filtered);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select the 1th
        model.setSelectionInterval(1, 1);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatcher(Matchers.falseMatcher());
        assertEquals(1, counter.getCountAndReset());

        // unclear the filter
        filtered.setMatcher(Matchers.trueMatcher());
        assertEquals(0, counter.getCountAndReset());

        // select the 0th
        model.setSelectionInterval(0, 0);
        assertEquals(1, counter.getCountAndReset());

        // clear the filter
        filtered.setMatcher(Matchers.falseMatcher());
        assertEquals(1, counter.getCountAndReset());
    }

    public void guiTestConstructorLocking() throws InterruptedException {
        // create a list which will record our multithreaded interactions with a list
        final ThreadRecorderEventList<Integer> atomicList = new ThreadRecorderEventList<Integer>(new BasicEventList<Integer>());

        // start a thread which adds new Integers every 50 ms
        final Thread writerThread = new Thread(GlazedListsTests.createJerkyAddRunnable(atomicList, null, 2000, 50), "WriterThread");
        writerThread.start();

        // make sure the writerThread has started writing
        Thread.sleep(200);

        // create a list whose get() method pauses for 50 ms before returning the value
        final EventList<Integer> delayList = new DelayList<Integer>(atomicList, 50);

        // the test: creating the EventSelectionModel should be atomic and pause the writerThread while it initializes its internal state
        new EventSelectionModel<Integer>(delayList);

        // wait until the writerThread finishes before asserting the recorded state
        writerThread.join();

        // correct locking should have produced a thread log like: WriterThread* AWT-EventQueue-0* WriterThread*
        // correct locking should have produced a read/write pattern like: W...W R...R W...W
        assertEquals(3, atomicList.getReadWriteBlockCount());
    }

    public void guiTestDeleteSelectedRows() {
        EventList<String> source = new BasicEventList<String>();
        source.addAll(GlazedListsTests.delimitedStringToList("one two three"));

        // create selection model
        EventSelectionModel<String> model = new EventSelectionModel<String>(source);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select all elements (should produce 1 ListSelectionEvent)
        model.setSelectionInterval(0, 2);
        assertEquals(source, model.getSelected());
        assertEquals(1, counter.getCountAndReset());

        // remove all selected elements (should produce 1 ListSelectionEvent)
        model.getSelected().clear();
        assertEquals(Collections.EMPTY_LIST, source);
        assertEquals(Collections.EMPTY_LIST, model.getSelected());
        // todo fix this defect (this is the functionality that Bruce Alspaugh is looking for)
        assertEquals(1, counter.getCountAndReset());
    }

    /**
     * Counts the number of ListSelectionEvents fired.
     */
    private class ListSelectionChangeCounter implements ListSelectionListener {
        private int count = 0;
        public void valueChanged(ListSelectionEvent e) {
            count++;
        }
        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }
}
