/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TextFilterator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

public class AutoCompleteSupportTestApp {

    private static final String[] LOOK_AND_FEEL_SELECTIONS = {
        "javax.swing.plaf.metal.MetalLookAndFeel",
        "apple.laf.AquaLookAndFeel",
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
        "com.incors.plaf.kunststoff.KunststoffLookAndFeel",
        "com.jgoodies.looks.plastic.PlasticLookAndFeel",
        "net.java.plaf.windows.WindowsLookAndFeel"
    };

    private static final String[] URL_SAMPLE_DATA = {
        "http://mail.google.com/mail/",
        "http://slashdot.org/",
        "http://www.clientjava.com/blog",
        "del.icio.us",
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

    /** The currently installed look and feel. */
    protected String currentLookAndFeel = LOOK_AND_FEEL_SELECTIONS[0];

    /** The last AutoCompleteSupport object installed. */
    private AutoCompleteSupport autoCompleteSupport;

    private final JComboBox autocompleteComboBox = new JComboBox();

    /** The test application's frame. */
    private final JFrame frame;

    private final JPanel tweakerPanel;

    private final JCheckBox correctCase = new JCheckBox();

    private final ButtonGroup lafMenuGroup = new ButtonGroup();

    public AutoCompleteSupportTestApp() {
        correctCase.addActionListener(new CorrectCaseActionHandler());
        correctCase.setSelected(true);

        tweakerPanel = createTweakerPanel();

        frame = new JFrame("AutoCompleteSupport Test Application");
        frame.setJMenuBar(createLafMenuBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        rebuildPanel();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void rebuildPanel() {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(tweakerPanel, BorderLayout.NORTH);
        frame.getContentPane().add(createMainPanel(), BorderLayout.CENTER);
    }

    /**
     * A local factory method to produce a JMenuBar that allows the Look&Feel
     * to be changed to any of the common Swing Look&Feels.
     */
    private JMenuBar createLafMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu lafMenu = menuBar.add(new JMenu("Look And Feel"));
        lafMenu.setMnemonic('L');

        // add the look and feel menu items
        for (int i = 0; i < LOOK_AND_FEEL_SELECTIONS.length; i++)
            createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_SELECTIONS[i]);

        ((JMenuItem) lafMenu.getMenuComponent(0)).setSelected(true);

        return menuBar;
    }

    private JMenuItem createLafMenuItem(JMenu menu, ButtonGroup lafMenuGroup, String laf) {
        final JMenuItem menuItem = menu.add(new JRadioButtonMenuItem(laf));
        menuItem.addActionListener(new ChangeLookAndFeelAction());
        menuItem.setEnabled(isAvailableLookAndFeel(laf));

        lafMenuGroup.add(menuItem);

        return menuItem;
    }

    private boolean isAvailableLookAndFeel(String laf) {
        try {
            LookAndFeel newLAF = (LookAndFeel) (Class.forName(laf).newInstance());
            return newLAF.isSupportedLookAndFeel();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * An action to change the current LookAndFeel of <code>Demo</code> to
     * <code>laf</code>.
     */
    private class ChangeLookAndFeelAction extends AbstractAction {
        protected ChangeLookAndFeelAction() {
            super("Change Look And Feel");
        }

        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.uninstall();

            JRadioButtonMenuItem selectedMenuItem = (JRadioButtonMenuItem) e.getSource();

            currentLookAndFeel = selectedMenuItem.getText();

            try {
                // set the LookAndFeel
                UIManager.setLookAndFeel(currentLookAndFeel);
                SwingUtilities.updateComponentTreeUI(frame);

                rebuildPanel();
            } catch (Exception ex) {
                System.err.println("Failed loading L&F: " + currentLookAndFeel);
                System.err.println(ex);
            }
        }
    }

    private class CorrectCaseActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final boolean willCorrectCase = correctCase.isSelected();
            autoCompleteSupport.setCorrectsCase(willCorrectCase);
        }
    }

    private JPanel createTweakerPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel("Correct Case:"),      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(correctCase,                      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    private JPanel createMainPanel() {
        final TextFilterator<String> filterator = new URLTextFilterator();
        final EventList<String> items = new BasicEventList<String>();
        items.addAll(Arrays.asList(URL_SAMPLE_DATA));

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int comboBoxCount = 0;

        // setting a prototype value prevents the combo box from resizing when
        // the model contents are filtered away
        autocompleteComboBox.setPrototypeDisplayValue("http://java.sun.com/j2se/1.5.0/download.jsp");
        autoCompleteSupport = AutoCompleteSupport.install(autocompleteComboBox, items, filterator);
        autoCompleteSupport.setCorrectsCase(correctCase.isSelected());

        final JComboBox plainComboBox = new JComboBox();
        plainComboBox.setEditable(true);
        plainComboBox.setModel(new EventComboBoxModel<String>(items));

        final String[] lookAndFeelNameParts = currentLookAndFeel.split("\\p{Punct}");
        final String lookAndFeelName = lookAndFeelNameParts[lookAndFeelNameParts.length - 1];

        final JLabel nameLabel = new JLabel(lookAndFeelName);


        if (comboBoxCount > 1)
            panel.add(Box.createVerticalStrut(1),   new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(nameLabel,                        new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(autocompleteComboBox,             new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(Box.createHorizontalStrut(5),     new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(plainComboBox,                    new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Starter());
    }

    private static class Starter implements Runnable {
        public void run() {
            new AutoCompleteSupportTestApp();
        }
    }

    /**
     * This text filterator produces three filter strings from the following URL:
     * <code>http://www.slashdot.org</code> which are:
     * <p/>
     * <ul>
     * <li>http://www.slashdot.org
     * <li>www.slashdot.org
     * <li>slashdot.org
     * </ul>
     */
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