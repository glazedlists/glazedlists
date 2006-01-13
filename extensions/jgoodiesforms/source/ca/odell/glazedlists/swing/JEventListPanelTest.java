/* O'Dell Swing Extensions                                                    */
/* COPYRIGHT 2005 O'DELL ENGINEERING LTD.                                     */
package ca.odell.glazedlists.swing;

// user interface
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
// JGoodies is industrial layout
import com.jgoodies.forms.layout.*;
// observable lists are used to store rules
import ca.odell.glazedlists.*;

/**
 * A panel that shows the contents of an EventList containing JComponents.
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
public class JEventListPanelTest {

    static class SillyObjectFormat extends JEventListPanel.AbstractFormat {

        public SillyObjectFormat() {
            super(true, "3dlu, pref, 3dlu, pref, 3dlu", "3dlu, pref, 3dlu, fill:pref:grow, 3dlu", "15dlu", "15dlu", new String[] { "2,2,1,3", "4,2", "4,4" });
        }

        public int getComponentsPerElement() {
            return 3;
        }

        public JComponent getComponent(Object element, int component) {
            String sillyObject = (String)element;
            if(component == 0) {
                return new JButton(sillyObject);
            } else if(component == 1) {
                return new JSlider();
            } else if(component == 2) {
                return new JProgressBar();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public static void main(String[] args) {
        EventList sillyObjects = new BasicEventList();
        sillyObjects.add("hat");
        sillyObjects.add("head");
        sillyObjects.add("helmet");

        JPanel panel = new JEventListPanel(sillyObjects, new SillyObjectFormat());

        JButton addButton = new JButton("ADD");
        addButton.addActionListener(new AddAction(sillyObjects));
        JButton removeButton = new JButton("REMOVE");
        removeButton.addActionListener(new RemoveAction(sillyObjects));

        JFrame frame = new JFrame("Silly Objects");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(panel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(addButton, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(removeButton, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.pack();
        frame.setVisible(true);
    }

    static class AddAction implements ActionListener {
        private EventList target;
        public AddAction(EventList target) { this.target = target; }
        public void actionPerformed(ActionEvent e) {
            target.add("horse");
        }
    }
    static class RemoveAction implements ActionListener {
        private EventList target;
        public RemoveAction(EventList target) { this.target = target; }
        public void actionPerformed(ActionEvent e) {
            target.remove(2);
        }
    }
}