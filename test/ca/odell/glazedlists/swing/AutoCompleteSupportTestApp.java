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
import java.util.Arrays;
import java.util.List;

public class AutoCompleteSupportTestApp {

    public static final String LOOK_AND_FEEL_MAC = "apple.laf.AquaLookAndFeel";
    public static final String LOOK_AND_FEEL_METAL = "javax.swing.plaf.metal.MetalLookAndFeel";
    public static final String LOOK_AND_FEEL_MOTIF = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
    public static final String LOOK_AND_FEEL_WINDOWS = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
    public static final String LOOK_AND_FEEL_KUNSTSTOFF = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";

    /** The current look and feel. */
     protected String currentLookAndFeel = LOOK_AND_FEEL_METAL;

    private final JFrame frame;

    private AutoCompleteSupport autocompleteSupport;

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
        // create look and feel switcher menu
        final JMenuBar menuBar = new JMenuBar();
        final ButtonGroup lafMenuGroup = new ButtonGroup();

        final JMenu lafMenu = menuBar.add(new JMenu("Look And Feel"));
        lafMenu.setMnemonic('L');

        // add the look and feel menu items
        final JMenuItem menuItem = createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_METAL);
        menuItem.setSelected(true); // this is the default l&f
        this.createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_MAC);
        this.createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_MOTIF);
        this.createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_WINDOWS);
        this.createLafMenuItem(lafMenu, lafMenuGroup, LOOK_AND_FEEL_KUNSTSTOFF);

        frame = new JFrame("AutoCompleteSupport Test Application");
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        rebuildPanel();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void rebuildPanel() {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(createPanel(), BorderLayout.CENTER);
    }

    protected JMenuItem createLafMenuItem(JMenu menu, ButtonGroup lafMenuGroup, String laf) {
        final JMenuItem menuItem = menu.add(new JRadioButtonMenuItem(laf));

//        menuItem.setMnemonic(getMnemonic(resourceMnemonicKey));
        menuItem.addActionListener(new ChangeLookAndFeelAction(laf));
        menuItem.setEnabled(isAvailableLookAndFeel(laf));

        lafMenuGroup.add(menuItem);

        return menuItem;
    }

    protected boolean isAvailableLookAndFeel(String laf) {
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
        private String laf;

        protected ChangeLookAndFeelAction(String laf) {
            super("Change Look And Feel");
            this.laf = laf;
        }

        public void actionPerformed(ActionEvent e) {
            if (currentLookAndFeel != laf) {
                currentLookAndFeel = laf;

                autocompleteSupport.dispose();

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
    }

    private JPanel createPanel() {
        final TextFilterator<String> filterator = new URLTextFilterator();
        final EventList<String> items = new BasicEventList<String>();
        items.addAll(Arrays.asList(urlData));

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int comboBoxCount = 0;

        final JComboBox autocompleteComboBox = new JComboBox();

        // setting a prototype value prevents the combo box from resizing when
        // the model contents are filtered away
        autocompleteComboBox.setPrototypeDisplayValue("http://java.sun.com/j2se/1.5.0/download.jsp");
        autocompleteSupport = AutoCompleteSupport.install(autocompleteComboBox, items, filterator);

        final JComboBox plainComboBox = new JComboBox();
        plainComboBox.setEditable(true);
        plainComboBox.setModel(new EventComboBoxModel(items));

        final String[] lookAndFeelNameParts = this.currentLookAndFeel.split("\\p{Punct}");
        final String lookAndFeelName = lookAndFeelNameParts[lookAndFeelNameParts.length-1];

        final JLabel nameLabel = new JLabel(lookAndFeelName);


        if (comboBoxCount > 1)
            panel.add(Box.createVerticalStrut(1), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(nameLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(autocompleteComboBox, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(Box.createHorizontalStrut(5), new GridBagConstraints(1, 3, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(plainComboBox, new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

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