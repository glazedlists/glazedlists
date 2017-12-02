/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class AutoCompleteSupportTestApp {

    private static final List<String> LOOK_AND_FEEL_SELECTIONS = new ArrayList<String>(Arrays.asList(
        "javax.swing.plaf.metal.MetalLookAndFeel",
        "apple.laf.AquaLookAndFeel",
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
        "com.incors.plaf.kunststoff.KunststoffLookAndFeel",
        "com.jgoodies.looks.plastic.PlasticLookAndFeel",
        "net.java.plaf.windows.WindowsLookAndFeel"
    ));

    private static final String[] URL_SAMPLE_DATA = {
        null,
        "http://mail.google.com/mail/",
        "http://slashdot.org/",
        "http://www.clientjava.com/blog",
        "del.icio.us",
        "http://java.sun.com/javase/6/",
        "http://java.sun.com/",
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

    private static final Location[] STATE_CAPITALS_DATA = {
        new Location("USA", "Alabama", "Montgomery"),
        new Location("USA", "Alaska", "Juneau"),
        new Location("USA", "Arizona", "Phoenix"),
        new Location("USA", "Arkansas", "Little Rock"),
        new Location("USA", "California", "Sacramento"),
        new Location("USA", "Colorado", "Denver"),
        new Location("USA", "Connecticut", "Hartford"),
        new Location("USA", "Delaware", "Dover"),
        new Location("USA", "Florida", "Tallahassee"),
        new Location("USA", "Georgia", "Atlanta"),
        new Location("USA", "Hawaii", "Honolulu"),
        new Location("USA", "Idaho", "Boise"),
        new Location("USA", "Illinois", "Springfield"),
        new Location("USA", "Indiana", "Indianapolis"),
        new Location("USA", "Iowa", "Des Moines"),
        new Location("USA", "Kansas", "Topeka"),
        new Location("USA", "Kentucky", "Frankfort"),
        new Location("USA", "Louisiana", "Baton Rouge"),
        new Location("USA", "Maine", "Augusta"),
        new Location("USA", "Maryland", "Annapolis"),
        new Location("USA", "Massachusetts", "Boston"),
        new Location("USA", "Michigan", "Lansing"),
        new Location("USA", "Minnesota", "Saint Paul"),
        new Location("USA", "Mississippi", "Jackson"),
        new Location("USA", "Missouri", "Jefferson City"),
        new Location("USA", "Montana", "Helena"),
        new Location("USA", "Nebraska", "Lincoln"),
        new Location("USA", "Nevada", "Carson City"),
        new Location("USA", "New Hampshire", "Concord"),
        new Location("USA", "New Jersey", "Trenton"),
        new Location("USA", "New Mexico", "Santa Fe"),
        new Location("USA", "New York", "Albany"),
        new Location("USA", "North Carolina", "Raleigh"),
        new Location("USA", "North Dakota", "Bismarck"),
        new Location("USA", "Ohio", "Columbus"),
        new Location("USA", "Oklahoma", "Oklahoma City"),
        new Location("USA", "Oregon", "Salem"),
        new Location("USA", "Pennsylvania", "Harrisburg"),
        new Location("USA", "Rhode Island", "Providence"),
        new Location("USA", "South Carolina", "Columbia"),
        new Location("USA", "South Dakota", "Pierre"),
        new Location("USA", "Tennessee", "Nashville"),
        new Location("USA", "Texas", "Austin"),
        new Location("USA", "Utah", "Salt Lake City"),
        new Location("USA", "Vermont", "Montpelier"),
        new Location("USA", "Virginia", "Richmond"),
        new Location("USA", "Washington", "Olympia"),
        new Location("USA", "West Virginia", "Charleston"),
        new Location("USA", "Wisconsin", "Madison"),
        new Location("USA", "Wyoming", "Cheyenne")
    };

    /** The currently installed look and feel. */
    private String currentLookAndFeel;

    /** The last AutoCompleteSupport object installed. */
    private AutoCompleteSupport<String> autoCompleteSupport;

    /** The single JComboBox on which AutoCompleteSupport is repeatedly installed and uninstalled. */
    private final JComboBox<String> autoCompleteComboBox = new JComboBox<>();
    private final JComboBox<String> regularComboBox = new JComboBox<>();

    /** The test application's frame. */
    private final JFrame frame;

    /** The panel which allows the user to change the behaviour of the {@link #autoCompleteComboBox}. */
    private final JPanel tweakerPanel;

    private final JPanel autocompleteActionPanel;
    private final JPanel regularActionPanel;

    private final DefaultListModel<String> autocompleteActionListModel = new DefaultListModel<>();
    private final DefaultListModel<String> regularActionListModel = new DefaultListModel<>();

    private final JList<String> autocompleteActionList = new JList<>(autocompleteActionListModel);
    private final JList<String> regularActionList = new JList<>(regularActionListModel);

    private final JRadioButton filterModeStartsWith = new JRadioButton("Starts With");
    private final JRadioButton filterModeContains = new JRadioButton("Contains");
    private final ButtonGroup filterModeButtonGroup = new ButtonGroup();
    {
        filterModeStartsWith.addActionListener(new FilterModeActionHandler(TextMatcherEditor.STARTS_WITH));
        filterModeContains.addActionListener(new FilterModeActionHandler(TextMatcherEditor.CONTAINS));

        filterModeButtonGroup.add(filterModeStartsWith);
        filterModeButtonGroup.add(filterModeContains);
        filterModeButtonGroup.setSelected(filterModeStartsWith.getModel(), true);
    }
    private final JPanel filterModePanel = new JPanel();
    {
        filterModePanel.add(filterModeStartsWith);
        filterModePanel.add(filterModeContains);
    }

    private final JTable table = new JTable();

    /** A checkbox to toggle whether the {@link #autoCompleteSupport} toggles the case of the user input to match the autocomplete term. */
    private final JCheckBox correctsCaseCheckBox = new JCheckBox();

    /** A checkbox to toggle whether the {@link #autoCompleteSupport} is in strict mode or not. */
    private final JCheckBox strictModeCheckBox = new JCheckBox();

    /** A checkbox to toggle whether the {@link #autoCompleteSupport} selects the editors text when gaining focus. */
    private final JCheckBox selectTextOnFocusGainCheckBox = new JCheckBox();

    /** A checkbox to toggle whether the {@link #autoCompleteSupport} hides the popup menu when losing focus. */
    private final JCheckBox hidesPopupOnFocusLostCheckBox = new JCheckBox();

    /** The ButtonGroup to which all Look & Feel radio buttons belong. */
    private final ButtonGroup lafMenuGroup = new ButtonGroup();

    public AutoCompleteSupportTestApp() {
        final EventList<Location> locations = new BasicEventList<Location>();
        locations.addAll(Arrays.asList(STATE_CAPITALS_DATA));
        final EventList<Location> locationsProxylist = GlazedListsSwing.swingThreadProxyList(locations);
        final String[] propertyNames = {"country", "state", "city"};
        final String[] columnLabels = {"Country", "State", "City"};
        final boolean[] writable = {true, true, true};
        final TableFormat<Location> tableFormat = GlazedLists.tableFormat(propertyNames, columnLabels, writable);
        table.setModel(new DefaultEventTableModel<Location>(locationsProxylist, tableFormat));

        // install a DefaultCellEditor with autocompleting support in each column
        final DefaultCellEditor cellEditor = AutoCompleteSupport.createTableCellEditor(tableFormat, locations, 2);
        table.getColumnModel().getColumn(2).setCellEditor(cellEditor);
        table.setSurrendersFocusOnKeystroke(true);

        autocompleteActionList.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");
        autocompleteActionList.setPreferredSize(new Dimension(autocompleteActionList.getPreferredSize().width, 600));
        autoCompleteComboBox.addActionListener(new RecordActionHandler(autocompleteActionListModel));
        autocompleteActionPanel = createActionPanel("AutoComplete ActionEvent Log", autocompleteActionList);

        regularActionList.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");
        regularActionList.setPreferredSize(new Dimension(regularActionList.getPreferredSize().width, 600));
        regularComboBox.addActionListener(new RecordActionHandler(regularActionListModel));
        regularActionPanel = createActionPanel("Normal ActionEvent Log", regularActionList);

        tweakerPanel = createTweakerPanel();

        frame = new JFrame("AutoCompleteSupport Test Application");
        frame.setJMenuBar(createLafMenuBar());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        rebuildContentPane();

        // initialize the tweaker panel
        correctsCaseCheckBox.addActionListener(new CorrectCaseActionHandler());
        correctsCaseCheckBox.setSelected(autoCompleteSupport.getCorrectsCase());

        strictModeCheckBox.addActionListener(new StrictModeActionHandler());
        strictModeCheckBox.setSelected(autoCompleteSupport.isStrict());

        selectTextOnFocusGainCheckBox.addActionListener(new SelectTextOnFocusGainActionHandler());
        selectTextOnFocusGainCheckBox.setSelected(autoCompleteSupport.getSelectsTextOnFocusGain());

        hidesPopupOnFocusLostCheckBox.addActionListener(new HidePopupOnFocusLostActionHandler());
        hidesPopupOnFocusLostCheckBox.setSelected(autoCompleteSupport.getHidesPopupOnFocusLost());

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Rebuilds the main panel from scratch and refreshes the UI.
     */
    private void rebuildContentPane() {
        frame.getContentPane().removeAll();
        frame.getContentPane().setLayout(new GridBagLayout());

        final JPanel mainPanel = createMainPanel();

        final JPanel autoCompleteModelPanel = createComboBoxModelPanel("AutoComplete ComboBoxModel", autoCompleteComboBox.getModel());

        final JPanel regularModelPanel = createComboBoxModelPanel("Normal ComboBoxModel", regularComboBox.getModel());

        frame.getContentPane().add(tweakerPanel,                 new GridBagConstraints(0, 0, 3, 1, 1.00, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(autocompleteActionPanel,      new GridBagConstraints(0, 1, 1, 1, 0.20, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(mainPanel,                    new GridBagConstraints(1, 1, 1, 1, 0.60, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(regularActionPanel,           new GridBagConstraints(2, 1, 1, 1, 0.20, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(autoCompleteModelPanel,       new GridBagConstraints(0, 2, 1, 1, 0.20, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(Box.createHorizontalStrut(1), new GridBagConstraints(1, 2, 1, 1, 0.60, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.getContentPane().add(regularModelPanel,            new GridBagConstraints(2, 2, 1, 1, 0.20, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    /**
     * A local factory method to produce a JMenuBar that allows the L&F
     * to be changed to any of the common Swing L&Fs.
     */
    private JMenuBar createLafMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        // add a L&F menu to the menu bar
        final JMenu lafMenu = menuBar.add(new JMenu("Look & Feel"));
        lafMenu.setMnemonic('L');

        // add the currently installed L&F to the list of available L&Fs if it isn't present
        final String currentLandF = UIManager.getLookAndFeel().getClass().getName();
        if (!LOOK_AND_FEEL_SELECTIONS.contains(currentLandF))
            LOOK_AND_FEEL_SELECTIONS.add(0, currentLandF);

        // add a menu item for each of the L&Fs
        for (Iterator<String> i = LOOK_AND_FEEL_SELECTIONS.iterator(); i.hasNext();)
            createLafMenuItem(lafMenu, i.next());

        return menuBar;
    }

    /**
     * A local factory method to produce a JMenuItem for a given L&F.
     */
    private JMenuItem createLafMenuItem(JMenu menu, String laf) {
        final JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(laf));
        menuItem.addActionListener(new ChangeLookAndFeelAction());
        menuItem.setEnabled(isAvailableLookAndFeel(laf));

        if (laf.equals(UIManager.getLookAndFeel().getClass().getName())) {
            menuItem.setSelected(true);
            currentLookAndFeel = laf;
        }

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
        @Override
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem selectedMenuItem = (JRadioButtonMenuItem) e.getSource();

            currentLookAndFeel = selectedMenuItem.getText();

            try {
                // set the LookAndFeel
                UIManager.setLookAndFeel(currentLookAndFeel);
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (Exception ex) {
                System.err.println("Failed loading L&F: " + currentLookAndFeel);
                ex.printStackTrace();
            }
        }
    }

    private class CorrectCaseActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setCorrectsCase(correctsCaseCheckBox.isSelected());
        }
    }

    private class StrictModeActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setStrict(strictModeCheckBox.isSelected());
        }
    }

    private class SelectTextOnFocusGainActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setSelectsTextOnFocusGain(selectTextOnFocusGainCheckBox.isSelected());
        }
    }

    private class HidePopupOnFocusLostActionHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setHidesPopupOnFocusLost(hidesPopupOnFocusLostCheckBox.isSelected());
        }
    }

    private class FilterModeActionHandler implements ActionListener {
        private final int mode;

        public FilterModeActionHandler(int mode) {
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setFilterMode(this.mode);
        }
    }

    private static class SetValueProgrammaticallyActionHandler extends AbstractAction {
        private final JComboBox<String> comboBox;

        public SetValueProgrammaticallyActionHandler(JComboBox<String> comboBox) {
            super("Set Value Programmatically");
            this.comboBox = comboBox;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.comboBox.setSelectedItem(URL_SAMPLE_DATA[URL_SAMPLE_DATA.length-1]);
        }
    }

    private JPanel createTweakerPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel("Corrects Case:"),             new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(correctsCaseCheckBox,                     new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(new JLabel("Strict:"),                    new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(strictModeCheckBox,                       new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(new JLabel("Select Text on Focus Gain:"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(selectTextOnFocusGainCheckBox,            new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(new JLabel("Hide Popup on Focus Lost:"),  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(hidesPopupOnFocusLostCheckBox,            new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(new JLabel("Filter Mode:"),               new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
        panel.add(filterModePanel,                          new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        return panel;
    }

    private JPanel createActionPanel(String title, JList<String> list) {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel(title),     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        panel.add(new JScrollPane(list), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

        return panel;
    }

    private JPanel createComboBoxModelPanel(String title, ComboBoxModel<String> model) {
        final JPanel panel = new JPanel(new GridBagLayout());

        final JList<String> list = new JList<>(model);
        list.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");

        panel.add(new JLabel(title),     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        panel.add(new JScrollPane(list), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

        return panel;
    }

    private JPanel createMainPanel() {
        final TextFilterator<String> filterator = new URLTextFilterator();
        final EventList<String> items = new BasicEventList<String>();
        items.addAll(Arrays.asList(URL_SAMPLE_DATA));
        final EventList<String> itemProxyList = GlazedListsSwing.swingThreadProxyList(items);
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int comboBoxCount = 0;

        // setting a prototype value prevents the combo box from resizing when
        // the model contents are filtered away
        autoCompleteComboBox.setPrototypeDisplayValue("http://java.sun.com/j2se/1.5.0/download.jsp");
        autoCompleteSupport = AutoCompleteSupport.install(autoCompleteComboBox, items, filterator);

        final JComboBox<String> plainComboBox = regularComboBox;
        plainComboBox.setEditable(true);
        plainComboBox.setModel(new DefaultEventComboBoxModel<String>(itemProxyList));

        final JScrollPane tableScroller = new JScrollPane(table);
        tableScroller.setPreferredSize(new Dimension(1, 200));

        final JButton setSelectedItemProgrammaticallyAutoComplete = new JButton(new SetValueProgrammaticallyActionHandler(autoCompleteComboBox));
        final JButton setSelectedItemProgrammaticallyPlain = new JButton(new SetValueProgrammaticallyActionHandler(plainComboBox));

        if (comboBoxCount > 1)
            panel.add(Box.createVerticalStrut(1), new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

        panel.add(new JLabel("AutoComplete ComboBox"),          new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(autoCompleteComboBox,                         new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(setSelectedItemProgrammaticallyAutoComplete,  new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(Box.createHorizontalStrut(5),                 new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(new JLabel("Normal JComboBox"),               new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(plainComboBox,                                new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(setSelectedItemProgrammaticallyPlain,         new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        panel.add(tableScroller,                                new GridBagConstraints(0, 4, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 0, 0, 0), 0, 0));

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Starter());
    }

    private static class Starter implements Runnable {
        @Override
        public void run() {
            new AutoCompleteSupportTestApp();
        }
    }

    private static final class RecordActionHandler implements ActionListener {
        private int count = 0;

        private final DefaultListModel<String> model;

        public RecordActionHandler(DefaultListModel<String> model) {
            this.model = model;
        }

        @SuppressWarnings("unchecked")
		@Override
        public void actionPerformed(ActionEvent e) {
            final JComboBox<String> comboBox = (JComboBox<String>) e.getSource();
            final String actionSummary = String.valueOf(++count) + ": " + comboBox.getSelectedItem();
            model.add(0, actionSummary);
        }
    }

    public static final class Location {
        private String country;
        private String state;
        private String city;

        public Location(String country, String state, String city) {
            this.country = country;
            this.state = state;
            this.city = city;
        }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
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
        @Override
        public void getFilterStrings(List<String> baseList, String element) {
            baseList.add(element);
            if (element != null) {
                if (element.startsWith("http://"))
                    baseList.add(element.substring(7));
                if (element.startsWith("http://www."))
                    baseList.add(element.substring(11));
            }
        }
    }
}