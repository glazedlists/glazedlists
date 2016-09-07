/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ExecuteOnMainThread;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.awt.Color;

import javax.swing.JList;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test <code>DefaultEventListModelTest</code> from the Swing thread.
 *
 * @author Holger Brands
 */
public class DefaultEventListModelTest extends SwingTestCase {
    /**
     * Verifies that the EDT check works, e.g. an IllegalStateException is thrown when a ListEvent
     * arrives on a non-EDT thread
     */
    @Test
    @ExecuteOnMainThread
    public void testOnMainThreadEDTViolation() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);

        final DefaultEventListModel<Color> listModel = new DefaultEventListModel<Color>(colors);
        assertEquals(2, listModel.getSize());
        try {
            colors.add(Color.BLUE);
            fail("failed to receive IllegalStateException because of missing ThreadProxyList");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    /**
     * Verifies that the factory method for model creation with thread proxy list works.
     */
    @Test
    @ExecuteOnMainThread
    public void testOnMainThreadNoEDTViolation() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);

        final DefaultEventListModel<Color> listModel = GlazedListsSwing.eventListModelWithThreadProxyList(colors);
        assertEquals(2, listModel.getSize());
        try {
            colors.add(Color.BLUE);
        } catch (IllegalStateException ex) {
            fail("failed to proxy source list with ThreadProxyList");
        }
        listModel.dispose();
    }

    /**
     * Verifies that the new getElementAt() method of EventListModel works.
     */
    @Test
    public void testGetElementAt() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);

        DefaultEventListModel<Color> listModel = new DefaultEventListModel<Color>(colors);

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

    //  public void guiTestConstructorLocking() throws InterruptedException {
    //  fail("I don't think this is appropriate for testing DefaultEventTableModel, or do we still want to deal with locking during construction of DefaultEventTableModel??");
    //}

    /**
     * Verifies that list selection is preserved, when handling a complex ListEvent with blocks,
     * which triggers a "data changed" ListDateEvent.
     */
    @Test
    public void testRemoveWithBlocksInListEvent() {
        // setup JList with EventListModel and EventSelectionModel
        final EventList<String> list = new BasicEventList<String>();
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final DefaultEventListModel<String> model = new DefaultEventListModel<String>(list);
        final JList jList = new JList(model);
        final DefaultEventSelectionModel<String> selModel = new DefaultEventSelectionModel<String>(list);
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
