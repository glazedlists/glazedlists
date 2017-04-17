/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.*;

/**
 * A panel that shows the contents of an EventList containing JComponents.
 *
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
class JEventListPanelDemo {

    private final EventList<String> sillyObjects = new BasicEventList<String>();

    public class SillyObjectFormat extends JEventListPanel.AbstractFormat<String> {

        public SillyObjectFormat() {
            super("0dlu, pref, 0dlu, pref, 0dlu", "0dlu, pref, 0dlu, fill:pref:grow, 0dlu", "45dlu", "45dlu", new String[] { "2, 2, 1, 3", "4, 2", "4, 4" });
//            super("0dlu, pref, 0dlu, pref, 0dlu", "0dlu, pref, 0dlu, fill:pref:grow, 0dlu", null, null, new String[] { "2, 2, 1, 3", "4, 2", "4, 4" });
        }

        @Override
        public int getComponentsPerElement() {
            return 3;
        }

        @Override
        public JComponent getComponent(String element, int component) {
            final String sillyObject = element;
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

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sillyObjects.remove(sillyObject);
        }
    }

    public void start() {
        sillyObjects.add("X 1");
        sillyObjects.add("X 3");
        sillyObjects.add("X 9");

        JEventListPanel<String> panel = new JEventListPanel<String>(sillyObjects, new SillyObjectFormat());
        panel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.RED));
        panel.setElementColumns(1);

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
        new JEventListPanelDemo().start();
    }

    static class AddAction implements ActionListener {
        private Random dice = new Random();
        private EventList<String> target;
        public AddAction(EventList<String> target) {
            this.target = target;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            target.add(3, "X " + (dice.nextInt(100)));
        }
    }
}