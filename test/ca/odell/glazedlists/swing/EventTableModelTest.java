/* Glazed Lists                                                 (c) 2003-2005 */
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
 * Test EventTableModel from the Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventTableModelTest extends SwingTestCase {

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
    }

    /**
     * Verifies that the new elementAt() method of EventTableModel works.
     */
    public void guiTestElementAt() {
        EventList colors = new BasicEventList();
        colors.add(Color.red);
        colors.add(Color.green);
        colors.add(Color.blue);

        TableFormat colorTableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        EventTableModel tableModel = new EventTableModel(colors, colorTableFormat);

        assertEquals(Color.red, tableModel.getElementAt(0));
        assertEquals(Color.green, tableModel.getElementAt(1));
        assertEquals(Color.blue, tableModel.getElementAt(2));
    }
}