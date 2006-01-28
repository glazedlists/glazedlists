/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import junit.framework.TestCase;

import java.awt.*;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * Test EventListModel from the Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventListModelTest extends SwingTestCase {
    /**
     * Verifies that the new getElementAt() method of EventListModel works.
     */
    public void guiTestGetElementAt() {
        EventList colors = new BasicEventList();
        colors.add(Color.red);
        colors.add(Color.green);
        colors.add(Color.blue);

        EventListModel tableModel = new EventListModel(colors);

        assertEquals(Color.red, tableModel.getElementAt(0));
        assertEquals(Color.green, tableModel.getElementAt(1));
        assertEquals(Color.blue, tableModel.getElementAt(2));

        try {
            tableModel.getElementAt(100);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }

        try {
            tableModel.getElementAt(-1);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }
    }
}