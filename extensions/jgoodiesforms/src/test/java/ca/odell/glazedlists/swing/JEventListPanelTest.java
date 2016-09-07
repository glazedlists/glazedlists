/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import org.junit.Test;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class JEventListPanelTest extends SwingTestCase {

    /**
     * Verifies that JEventListPanel works with all operations in sequence.
     */
    @Test
    public void testAddUpdateDelete() {
        EventList<JCheckBox> checkBoxes = new BasicEventList<JCheckBox>();
        JEventListPanel<JCheckBox> checkboxPanel = new JEventListPanel<JCheckBox>(checkBoxes, new CheckBoxFormat());
        checkBoxes.add(new JCheckBox("Saskatchewan"));
        checkBoxes.add(new JCheckBox("Manitoba"));
        checkBoxes.set(0, new JCheckBox("Ontario"));
        checkBoxes.remove(1);
        checkBoxes.remove(0);
    }

    /**
     * Trivial implementation of {@link JEventListPanel.Format} for testing.
     */
    class CheckBoxFormat extends JEventListPanel.AbstractFormat<JCheckBox> {
        public CheckBoxFormat() {
            super("pref", "pref", null, null, new String[] { "1, 1" });
        }
        @Override
        public JComponent getComponent(JCheckBox element, int component) {
            return element;
        }
    }

}