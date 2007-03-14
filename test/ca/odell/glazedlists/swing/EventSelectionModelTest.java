/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.Matchers;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.*;
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
        EventList<Comparable> list = new BasicEventList<Comparable>();
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
        EventList<String> list = new BasicEventList<String>();
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
        EventList<Object> source = GlazedLists.eventListOf(new Object[] {"one", "two", "three"});
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

    /**
     * If EventList changes are relegated to indexes AFTER the maxSelectionIndex
     * then no ListSelectionEvent needs to be fired. This test method verifies
     * that the expected number of ListSelectionEvents are produced when
     * inserting and removing at all locations relative to the range of list selections.
     */
    public void guiTestFireOnlyNecessaryEvents() {
        EventList<String> source = GlazedLists.eventListOf(new String[] {"Albert", "Alex", "Aaron", "Brian", "Bruce"});

        // create selection model
        EventSelectionModel<String> model = new EventSelectionModel<String>(source);
        ListSelectionChangeCounter counter = new ListSelectionChangeCounter();
        model.addListSelectionListener(counter);

        // select 2nd element (should produce 1 ListSelectionEvent)
        model.setSelectionInterval(1, 3);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes before the minSelectionIndex shift the existing selections and thus produce ListSelectionEvents
        source.add(0, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(2, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(0);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes on the minSelectionIndex shift the existing selections and thus produce ListSelectionEvents
        source.add(1, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(2, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(1);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes between the minSelectionIndex and maxSelectionIndex change existing selections and thus produce ListSelectionEvents
        source.add(2, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(2);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes on the maxSelectionIndex change the existing selections and thus produce ListSelectionEvents
        source.add(3, "Bart");
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(4, model.getMaxSelectionIndex());
        source.remove(3);
        assertEquals(1, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes after the maxSelectionIndex do not produce ListSelectionEvents
        source.add(4, "Bart");
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
        source.remove(4);
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());

        // inserts and removes after the maxSelectionIndex do not produce ListSelectionEvents
        source.add(5, "Bart");
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
        source.remove(5);
        assertEquals(0, counter.getCountAndReset());
        assertEquals(1, model.getMinSelectionIndex());
        assertEquals(3, model.getMaxSelectionIndex());
    }

    public void guiTestModelChangesProducingSelectionModelEvents() {
        EventList<String> source = GlazedLists.eventListOf(new String[] {"Albert", "Alex", "Aaron", "Brian", "Bruce"});

        // create EventListModel (data model)
        EventListModel<String> model = new EventListModel<String>(source);

        // create EventSelectionModel (our selection model)
        EventSelectionModel<String> eventSelectionModel = new EventSelectionModel<String>(source);
        ListSelectionChangeCounter eventSelectionModelCounter = new ListSelectionChangeCounter();
        eventSelectionModel.addListSelectionListener(eventSelectionModelCounter);

        // create DefaultListSelectionModel (SUN's selection model)
        DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
        ListSelectionChangeCounter defaultListSelectionModelCounter = new ListSelectionChangeCounter();
        defaultListSelectionModel.addListSelectionListener(defaultListSelectionModelCounter);

        // create two different JLists (one with EventSelectionModel and one with DefaultListSelectionModel) that share the same data model
        JList eventList = new JList(model);
        eventList.setSelectionModel(eventSelectionModel);

        JList defaultList = new JList(model);
        defaultList.setSelectionModel(defaultListSelectionModel);

        // select the first element in both selection models
        eventSelectionModel.setSelectionInterval(0, 0);
        defaultListSelectionModel.setSelectionInterval(0, 0);

        // verify that each selection model broadcasted the selection event
        assertEquals(1, defaultListSelectionModelCounter.getCountAndReset());
        assertEquals(1, eventSelectionModelCounter.getCountAndReset());

        // change an element in the model which is selected in the selection models
        source.set(0, "Bart");

        // selection should not have changed in either selection model
        assertEquals(0, defaultListSelectionModel.getMinSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getMaxSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getLeadSelectionIndex());
        assertEquals(0, defaultListSelectionModel.getAnchorSelectionIndex());

        assertEquals(0, eventSelectionModel.getMinSelectionIndex());
        assertEquals(0, eventSelectionModel.getMaxSelectionIndex());
        assertEquals(0, eventSelectionModel.getLeadSelectionIndex());
        assertEquals(0, eventSelectionModel.getAnchorSelectionIndex());

        // verify that neither DefaultListSelectionModel nor EventSelectionModel broadcasted a needless event for this model change
        assertEquals(0, defaultListSelectionModelCounter.getCountAndReset());
        assertEquals(0, eventSelectionModelCounter.getCountAndReset());
    }
    
    public void guiTestDeleteSelectedRows_FixMe() {
        EventList<String> source = GlazedLists.eventListOf(new String[] {"one", "two", "three"});

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
    private static class ListSelectionChangeCounter implements ListSelectionListener {
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