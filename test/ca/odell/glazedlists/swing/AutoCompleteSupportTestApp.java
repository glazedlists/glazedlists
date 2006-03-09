package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TextFilterator;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class AutoCompleteSupportTestApp {

    private static final String[] uiDelegates = {
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
        "com.sun.java.swing.plaf.mac.MacLookAndFeel",
        "javax.swing.plaf.metal.MetalLookAndFeel",
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
        "com.incors.plaf.kunststoff.KunststoffLookAndFeel",
        "apple.laf.AquaLookAndFeel",
    };

    private static final String[] urlData = {
        "http://mail.google.com/mail/",
        "http://slashdot.org/",
        "http://www.clientjava.com/blog",
        "http://java.sun.com/",
        "http://java.sun.com/javase/6/",
        "http://java.sun.com/j2se/1.5.0/download.jsp",
        "http://java.sun.com/javaone/sf/",
        "http://www.jetbrains.com/",
        "http://www.jetbrains.com/idea/?ggl502",
        "http://www.wilshipley.com/blog/",
        "http://jroller.com/page/fate",
        "http://wilwheaton.typepad.com/",
        "http://www.theonion.com/content/",
        "http://www.indeed.com/",
    };

    public AutoCompleteSupportTestApp() {
        JFrame frame = new JFrame("AutoCompleteSupport Test Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(createPanel(), BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createPanel() {
        final TextFilterator<String> filterator = new URLTextFilterator();
        final EventList<String> items = new BasicEventList<String>();
        items.addAll(Arrays.asList(urlData));

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int comboBoxCount = 0;
        for (int i = 0; i < uiDelegates.length; i++) {
            try {
                UIManager.setLookAndFeel(uiDelegates[i]);
            } catch (Exception e) {
                continue;
            }
            comboBoxCount++;

            final String[] lookAndFeelNameParts = uiDelegates[i].split("\\p{Punct}");
            final String lookAndFeelName = lookAndFeelNameParts[lookAndFeelNameParts.length-1];

            final JLabel nameLabel = new JLabel(lookAndFeelName);

            final JComboBox comboBox = new JComboBox();
            AutoCompleteSupport.install(comboBox, items, filterator);

            final JComboBox plainComboBox = new JComboBox();
            plainComboBox.setEditable(true);
            plainComboBox.setModel(new EventComboBoxModel(items));

            if (comboBoxCount > 1)
                panel.add(Box.createVerticalStrut(1), new GridBagConstraints(0, i*3, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(nameLabel, new GridBagConstraints(0, i*3+1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(comboBox, new GridBagConstraints(0, i*3+2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

            panel.add(Box.createHorizontalStrut(5), new GridBagConstraints(1, i*3, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
            panel.add(plainComboBox, new GridBagConstraints(2, i*3+2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        }

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Starter());
    }

    private static class Starter implements Runnable {
        public void run() {
            System.out.println(UIManager.getSystemLookAndFeelClassName());
            new AutoCompleteSupportTestApp();
        }
    }

    private static final class URLTextFilterator implements TextFilterator<String> {
        public void getFilterStrings(List<String> baseList, String element) {
            baseList.add(element);
            if (element.startsWith("http://"))
                baseList.add(element.substring(7));
            if (element.startsWith("http://www."))
                baseList.add(element.substring(11));
        }
    }
}