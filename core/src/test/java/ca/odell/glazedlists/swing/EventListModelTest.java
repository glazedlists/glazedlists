/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.DelayList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ThreadRecorderEventList;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.awt.Color;

import javax.swing.JList;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test EventListModel from the Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
@SuppressWarnings("deprecation")
public class EventListModelTest extends SwingTestCase {

    /**
     * Verifies that the new getElementAt() method of EventListModel works.
     */
    @Test
    public void testGetElementAt() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);

        EventListModel<Color> listModel = new EventListModel<Color>(colors);

        assertEquals(Color.RED, listModel.getElementAt(0));
        assertEquals(Color.GREEN, listModel.getElementAt(1));
        assertEquals(Color.BLUE, listModel.getElementAt(2));

        try {
            listModel.getElementAt(100);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }

        try {
            listModel.getElementAt(-1);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }
    }

    @Test
    public void testConstructorLocking() throws InterruptedException {
        // create a list which will record our multithreaded interactions with a list
        final ThreadRecorderEventList<Integer> atomicList = new ThreadRecorderEventList<Integer>(new BasicEventList<Integer>());

        // start a thread which adds new Integers every 50 ms
        final Thread writerThread = new Thread(GlazedListsTests.createJerkyAddRunnable(atomicList, null, 2000, 50), "WriterThread");
        writerThread.start();

        // make sure the writerThread has started writing
        Thread.sleep(200);

        // create a list whose get() method pauses for 50 ms before returning the value
        final EventList<Integer> delayList = new DelayList<Integer>(atomicList, 50);

        // the test: creating the EventListModel should be atomic and pause the writerThread while it initializes its internal state
        new EventListModel<Integer>(delayList);

        // wait until the writerThread finishes before asserting the recorded state
        writerThread.join();

        // correct locking should have produced a thread log like: WriterThread* AWT-EventQueue-0* WriterThread*
        // correct locking should have produced a read/write pattern like: W...W R...R W...W
        assertEquals(3, atomicList.getReadWriteBlockCount());
    }

    /**
     * Verifies that list selection is preserved, when handling a complex ListEvent with blocks,
     * which triggers a "data changed" ListDateEvent.
     */
    @Test
    public void testRemoveWithBlocksInListEvent() {
        // setup JList with EventListModel and EventSelectionModel
        final EventList<String> list = new BasicEventList<String>();
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final EventListModel<String> model = new EventListModel<>(list);
        final JList<String> jList = new JList<String>(model);
        final EventSelectionModel<String> selModel = new EventSelectionModel<>(list);
        jList.setSelectionModel(selModel);
        // establish a selection
        selModel.setSelectionInterval(1, 1);
        assertEquals(GlazedListsTests.stringToList("B"), selModel.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D E F"), selModel.getDeselected());
        // trigger a ListEvent with blocks
        list.removeAll(GlazedListsTests.delimitedStringToList("E F"));
        // selection should be preserved
        assertEquals(GlazedListsTests.stringToList("B"), selModel.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D"), selModel.getDeselected());
    }
}