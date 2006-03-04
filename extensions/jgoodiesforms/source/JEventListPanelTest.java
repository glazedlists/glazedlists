/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Random;
// JGoodies is industrial layout
// observable lists are used to store rules
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.JEventListPanel;

/**
 * A panel that shows the contents of an EventList containing JComponents.
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
public class JEventListPanelTest {

    private final EventList sillyObjects = new BasicEventList();

    public class SillyObjectFormat extends JEventListPanel.AbstractFormat {

        public SillyObjectFormat() {
            super("0dlu, pref, 0dlu, pref, 0dlu", "0dlu, pref, 0dlu, fill:pref:grow, 0dlu", "45dlu", "45dlu", new String[] { "2, 2, 1, 3", "4, 2", "4, 4" });
//            super("0dlu, pref, 0dlu, pref, 0dlu", "0dlu, pref, 0dlu, fill:pref:grow, 0dlu", null, null, new String[] { "2, 2, 1, 3", "4, 2", "4, 4" });
        }

        public int getComponentsPerElement() {
            return 3;
        }

        public JComponent getComponent(Object element, int component) {
            final String sillyObject = (String)element;
            if(component == 0) {
                JButton button = new JButton(sillyObject);
                button.addActionListener(new ButtonActionListener(sillyObject));
                return button;
            } else if(component == 1) {
                return new JSlider();
            } else if(component == 2) {
                return new JTextField();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private class ButtonActionListener implements ActionListener {
        private String sillyObject;

        public ButtonActionListener(String sillyObject) {
            this.sillyObject = sillyObject;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            sillyObjects.remove(sillyObject);
        }
    }

    public void start() {
        sillyObjects.add("X 1");
        sillyObjects.add("X 3");
        sillyObjects.add("X 9");

        JEventListPanel panel = new JEventListPanel(sillyObjects, new SillyObjectFormat());
        panel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.RED));

        JButton addButton = new JButton("ADD");
        addButton.addActionListener(new AddAction(sillyObjects));

        JFrame frame = new JFrame("Silly Objects");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(addButton, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new JEventListPanelTest().start();
    }

    static class AddAction implements ActionListener {
        private Random dice = new Random();
        private EventList target;
        public AddAction(EventList target) { this.target = target; }
        public void actionPerformed(ActionEvent e) {
            target.add("X " + (dice.nextInt(100)));
        }
    }
}