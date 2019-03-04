/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ExecuteOnNonUiThread;

import org.junit.Test;

import javax.swing.JComboBox;

import java.awt.Color;

/**
 * Test <code>DefaultEventComboBoxModelTest</code> from the Swing thread.
 *
 * @author Holger Brands
 */
public class DefaultEventComboBoxModelTest extends SwingTestCase {
    /**
     * Verifies that the EDT check works, e.g. an IllegalStateException is thrown when a ListEvent
     * arrives on a non-EDT thread
     */
    @Test
    @ExecuteOnNonUiThread
    public void testOnMainThreadEDTViolation() {
        EventList<Color> colors = new BasicEventList<>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);

        final DefaultEventComboBoxModel<Color> comboModel = new DefaultEventComboBoxModel<>(colors);
        assertEquals(2, comboModel.getSize());
        try {
            colors.add(Color.BLUE);
            fail("failed to receive IllegalStateException because of missing ThreadProxyList");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    /**
     * Verifies that factory method for creating model with thread proxy list works.
     */
    @Test
    @ExecuteOnNonUiThread
    public void testOnMainThreadNoEDTViolation() {
        EventList<Color> colors = new BasicEventList<>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);

        final DefaultEventComboBoxModel<Color> comboModel = GlazedListsSwing.eventComboBoxModelWithThreadProxyList(colors);
        assertEquals(2, comboModel.getSize());
        try {
            colors.acceptWithWriteLock(list -> list.add(Color.BLUE));
        } catch (IllegalStateException ex) {
            fail("failed to proxy source list with ThreadProxyList");
        }
        comboModel.dispose();
    }

    /**
     * Verifies the selected item.
     */
    @Test
    public void testSelectedItem() {
        EventList<Color> colors = new BasicEventList<>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        final DefaultEventComboBoxModel<Color> comboModel = new DefaultEventComboBoxModel<>(colors);
        final JComboBox<Color> comboBox = new JComboBox<>(comboModel);
        assertEquals(null, comboBox.getSelectedItem());
        assertEquals(-1, comboBox.getSelectedIndex());
        assertEquals(null, comboModel.getSelectedItem());
        comboModel.setSelectedItem(Color.GREEN);
        assertEquals(Color.GREEN, comboBox.getSelectedItem());
        assertEquals(1, comboBox.getSelectedIndex());
        assertEquals(Color.GREEN, comboModel.getSelectedItem());

        colors.add(1, Color.BLUE);
        assertEquals(Color.GREEN, comboBox.getSelectedItem());
        assertEquals(2, comboBox.getSelectedIndex());
        assertEquals(Color.GREEN, comboModel.getSelectedItem());

        colors.remove(0);
        assertEquals(Color.GREEN, comboBox.getSelectedItem());
        assertEquals(1, comboBox.getSelectedIndex());
        assertEquals(Color.GREEN, comboModel.getSelectedItem());
        comboModel.setSelectedItem(null);
        assertEquals(null, comboBox.getSelectedItem());
        assertEquals(-1, comboBox.getSelectedIndex());
        assertEquals(null, comboModel.getSelectedItem());
        // trying to select an non-existing item in a non-editable Combobox will fail
        assertFalse(comboBox.isEditable());
        comboBox.setSelectedItem(Color.RED);
        assertEquals(null, comboBox.getSelectedItem());
        assertEquals(-1, comboBox.getSelectedIndex());
        assertEquals(null, comboModel.getSelectedItem());
        comboBox.setSelectedItem(Color.BLUE);
        assertEquals(Color.BLUE, comboBox.getSelectedItem());
        assertEquals(0, comboBox.getSelectedIndex());
        assertEquals(Color.BLUE, comboModel.getSelectedItem());
    }
}
