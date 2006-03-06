package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.TimeZone;

public class AutoCompleteSupportTestApp {

    private JComboBox combo;

    /**
     * Creates a new instance of Main
     */
    public AutoCompleteSupportTestApp() {
        JFrame frame = new JFrame("AutoCompleteSupport Test Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane ().add(_createPanel(), BorderLayout.NORTH);
        frame.setBounds(100, 100, 550, 350);
        frame.setVisible(true);
    }

    private JPanel _createPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 2, 5, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final EventList items = new BasicEventList();
        items.addAll(Arrays.asList(TimeZone.getAvailableIDs()));

        combo = new JComboBox();
        AutoCompleteSupport.install(combo, items);

        panel.add(new JLabel("Auto-complete combo:"));
        panel.add(combo);

        return panel;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new AutoCompleteSupportTestApp();
    }
}