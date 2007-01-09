package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AutoCompleteSupportComplexTestApp {

    private static final List<String> LOOK_AND_FEEL_SELECTIONS = new ArrayList<String>(Arrays.asList(new String[] {
        "javax.swing.plaf.metal.MetalLookAndFeel",
        "apple.laf.AquaLookAndFeel",
        "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
        "com.incors.plaf.kunststoff.KunststoffLookAndFeel",
        "com.jgoodies.looks.plastic.PlasticLookAndFeel",
        "net.java.plaf.windows.WindowsLookAndFeel"
    }));

    public static final class Url {
        private final String location;

        public Url(String location) {
            this.location = location;
        }

        public String getLocation() {
            return location;
        }

        public static Url valueOf(String s) {
            return new Url(s);
        }

        public String toString() {
            return "Url: " + getLocation();
        }
    }

    private static final AutoCompleteSupportComplexTestApp.Url[] URL_SAMPLE_DATA = {
        new AutoCompleteSupportComplexTestApp.Url("http://mail.google.com/mail/"),
        new AutoCompleteSupportComplexTestApp.Url("http://slashdot.org/"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.clientjava.com/blog"),
        new AutoCompleteSupportComplexTestApp.Url("del.icio.us"),
        new AutoCompleteSupportComplexTestApp.Url("http://java.sun.com/javase/6/"),
        new AutoCompleteSupportComplexTestApp.Url("http://java.sun.com/"),
        new AutoCompleteSupportComplexTestApp.Url("http://java.sun.com/j2se/1.5.0/download.jsp"),
        new AutoCompleteSupportComplexTestApp.Url("http://java.sun.com/javaone/sf/"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.jetbrains.com/"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.jetbrains.com/idea/?ggl502"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.wilshipley.com/blog/"),
        new AutoCompleteSupportComplexTestApp.Url("http://jroller.com/page/fate"),
        new AutoCompleteSupportComplexTestApp.Url("http://wilwheaton.typepad.com/"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.theonion.com/content/"),
        new AutoCompleteSupportComplexTestApp.Url("http://www.indeed.com/")
    };

    private static final AutoCompleteSupportComplexTestApp.Location[] STATE_CAPITALS_DATA = {
        new AutoCompleteSupportComplexTestApp.Location("USA", "Alabama", "Montgomery"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Alaska", "Juneau"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Arizona", "Phoenix"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Arkansas", "Little Rock"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "California", "Sacramento"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Colorado", "Denver"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Connecticut", "Hartford"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Delaware", "Dover"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Florida", "Tallahassee"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Georgia", "Atlanta"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Hawaii", "Honolulu"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Idaho", "Boise"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Illinois", "Springfield"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Indiana", "Indianapolis"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Iowa", "Des Moines"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Kansas", "Topeka"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Kentucky", "Frankfort"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Louisiana", "Baton Rouge"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Maine", "Augusta"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Maryland", "Annapolis"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Massachusetts", "Boston"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Michigan", "Lansing"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Minnesota", "Saint Paul"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Mississippi", "Jackson"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Missouri", "Jefferson City"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Montana", "Helena"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Nebraska", "Lincoln"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Nevada", "Carson City"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "New Hampshire", "Concord"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "New Jersey", "Trenton"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "New Mexico", "Santa Fe"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "New York", "Albany"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "North Carolina", "Raleigh"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "North Dakota", "Bismarck"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Ohio", "Columbus"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Oklahoma", "Oklahoma City"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Oregon", "Salem"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Pennsylvania", "Harrisburg"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Rhode Island", "Providence"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "South Carolina", "Columbia"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "South Dakota", "Pierre"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Tennessee", "Nashville"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Texas", "Austin"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Utah", "Salt Lake City"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Vermont", "Montpelier"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Virginia", "Richmond"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Washington", "Olympia"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "West Virginia", "Charleston"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Wisconsin", "Madison"),
        new AutoCompleteSupportComplexTestApp.Location("USA", "Wyoming", "Cheyenne")
    };

    /** The currently installed look and feel. */
    private String currentLookAndFeel;

    /** The last AutoCompleteSupport object installed. */
    private AutoCompleteSupport autoCompleteSupport;

    /** The single JComboBox on which AutoCompleteSupport is repeatedly installed and uninstalled. */
    private final JComboBox autoCompleteComboBox = new JComboBox();
    private final JComboBox regularComboBox = new JComboBox();

    /** The test application's frame. */
    private final JFrame frame;

    /** The panel which allows the user to change the behaviour of the {@link #autoCompleteComboBox}. */
    private final JPanel tweakerPanel;

    private final JPanel autocompleteActionPanel;
    private final JPanel regularActionPanel;

    private final DefaultListModel autocompleteActionListModel = new DefaultListModel();
    private final DefaultListModel regularActionListModel = new DefaultListModel();

    private final JList autocompleteActionList = new JList(autocompleteActionListModel);
    private final JList regularActionList = new JList(regularActionListModel);

    private final JRadioButton filterModeStartsWith = new JRadioButton("Starts With");
    private final JRadioButton filterModeContains = new JRadioButton("Contains");
    private final ButtonGroup filterModeButtonGroup = new ButtonGroup();
    {
        filterModeStartsWith.addActionListener(new AutoCompleteSupportComplexTestApp.FilterModeActionHandler(TextMatcherEditor.STARTS_WITH));
        filterModeContains.addActionListener(new AutoCompleteSupportComplexTestApp.FilterModeActionHandler(TextMatcherEditor.CONTAINS));

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

    public AutoCompleteSupportComplexTestApp() {
        final EventList<Location> locations = new BasicEventList<Location>();
        locations.addAll(Arrays.asList(AutoCompleteSupportComplexTestApp.STATE_CAPITALS_DATA));
        final String[] propertyNames = {"country", "state", "city"};
        final String[] columnLabels = {"Country", "State", "City"};
        final boolean[] writable = {true, true, true};
        final TableFormat<Location> tableFormat = GlazedLists.tableFormat(propertyNames, columnLabels, writable);
        table.setModel(new EventTableModel<AutoCompleteSupportComplexTestApp.Location>(locations, tableFormat));

        // install a DefaultCellEditor with autocompleting support in each column
        for (int i = 0; i < propertyNames.length; i++) {
            final DefaultCellEditor cellEditor = AutoCompleteSupport.createTableCellEditor(tableFormat, locations, i);
            table.getColumnModel().getColumn(i).setCellEditor(cellEditor);
        }

        autocompleteActionList.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");
        autocompleteActionList.setPreferredSize(new Dimension(autocompleteActionList.getPreferredSize().width, 600));
        autoCompleteComboBox.addActionListener(new AutoCompleteSupportComplexTestApp.RecordActionHandler(autocompleteActionListModel));
        autocompleteActionPanel = createActionPanel("AutoComplete ActionEvent Log", autocompleteActionList);

        regularActionList.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");
        regularActionList.setPreferredSize(new Dimension(regularActionList.getPreferredSize().width, 600));
        regularComboBox.addActionListener(new AutoCompleteSupportComplexTestApp.RecordActionHandler(regularActionListModel));
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
        if (!AutoCompleteSupportComplexTestApp.LOOK_AND_FEEL_SELECTIONS.contains(currentLandF))
            AutoCompleteSupportComplexTestApp.LOOK_AND_FEEL_SELECTIONS.add(0, currentLandF);

        // add a menu item for each of the L&Fs
        for (Iterator<String> i = AutoCompleteSupportComplexTestApp.LOOK_AND_FEEL_SELECTIONS.iterator(); i.hasNext();)
            createLafMenuItem(lafMenu, i.next());

        return menuBar;
    }

    /**
     * A local factory method to produce a JMenuItem for a given L&F.
     */
    private JMenuItem createLafMenuItem(JMenu menu, String laf) {
        final JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) menu.add(new JRadioButtonMenuItem(laf));
        menuItem.addActionListener(new AutoCompleteSupportComplexTestApp.ChangeLookAndFeelAction());
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
        public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem selectedMenuItem = (JRadioButtonMenuItem) e.getSource();

            currentLookAndFeel = selectedMenuItem.getText();

            try {
                // set the LookAndFeel
                UIManager.setLookAndFeel(currentLookAndFeel);
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (Exception ex) {
                System.err.println("Failed loading L&F: " + currentLookAndFeel);
                System.err.println(ex);
            }
        }
    }

    private class CorrectCaseActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setCorrectsCase(correctsCaseCheckBox.isSelected());
        }
    }

    private class StrictModeActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setStrict(strictModeCheckBox.isSelected());
        }
    }

    private class SelectTextOnFocusGainActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setSelectsTextOnFocusGain(selectTextOnFocusGainCheckBox.isSelected());
        }
    }

    private class HidePopupOnFocusLostActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setHidesPopupOnFocusLost(hidesPopupOnFocusLostCheckBox.isSelected());
        }
    }

    private class FilterModeActionHandler implements ActionListener {
        private final int mode;

        public FilterModeActionHandler(int mode) {
            this.mode = mode;
        }

        public void actionPerformed(ActionEvent e) {
            autoCompleteSupport.setFilterMode(this.mode);
        }
    }

    private static class SetValueProgrammaticallyActionHandler extends AbstractAction {
        private final JComboBox comboBox;

        public SetValueProgrammaticallyActionHandler(JComboBox comboBox) {
            super("Set Value Programmatically");
            this.comboBox = comboBox;
        }

        public void actionPerformed(ActionEvent e) {
            final ComboBoxModel model = this.comboBox.getModel();
            this.comboBox.setSelectedItem(model.getElementAt(model.getSize()-1));
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

    private JPanel createActionPanel(String title, JList list) {
        final JPanel panel = new JPanel(new GridBagLayout());

        panel.add(new JLabel(title),     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        panel.add(new JScrollPane(list), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

        return panel;
    }

    private JPanel createComboBoxModelPanel(String title, ComboBoxModel model) {
        final JPanel panel = new JPanel(new GridBagLayout());

        final JList list = new JList(model);
        list.setPrototypeCellValue("100: http://java.sun.com/j2se/1.5.0/download.jsp");

        panel.add(new JLabel(title),     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));
        panel.add(new JScrollPane(list), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));

        return panel;
    }

    private JPanel createMainPanel() {
        final TextFilterator<Url> filterator = new AutoCompleteSupportComplexTestApp.URLTextFilterator();
        final UrlFormat format = new UrlFormat();
        final EventList<AutoCompleteSupportComplexTestApp.Url> items = new BasicEventList<AutoCompleteSupportComplexTestApp.Url>();
        items.addAll(Arrays.asList(AutoCompleteSupportComplexTestApp.URL_SAMPLE_DATA));

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int comboBoxCount = 0;

        // setting a prototype value prevents the combo box from resizing when
        // the model contents are filtered away
        autoCompleteComboBox.setPrototypeDisplayValue(new AutoCompleteSupportComplexTestApp.Url("http://java.sun.com/j2se/1.5.0/download.jsp"));
        autoCompleteSupport = AutoCompleteSupport.install(autoCompleteComboBox, items, filterator, format);

        final JComboBox plainComboBox = regularComboBox;
        plainComboBox.setEditable(true);
        plainComboBox.setModel(new EventComboBoxModel<AutoCompleteSupportComplexTestApp.Url>(items));

        final JScrollPane tableScroller = new JScrollPane(table);
        tableScroller.setPreferredSize(new Dimension(1, 200));

        final JButton setSelectedItemProgrammaticallyAutoComplete = new JButton(new AutoCompleteSupportComplexTestApp.SetValueProgrammaticallyActionHandler(autoCompleteComboBox));
        final JButton setSelectedItemProgrammaticallyPlain = new JButton(new AutoCompleteSupportComplexTestApp.SetValueProgrammaticallyActionHandler(plainComboBox));

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
        SwingUtilities.invokeLater(new AutoCompleteSupportComplexTestApp.Starter());
    }

    private static class Starter implements Runnable {
        public void run() {
            new AutoCompleteSupportComplexTestApp();
        }
    }

    private static final class RecordActionHandler implements ActionListener {
        private int count = 0;

        private final DefaultListModel model;

        public RecordActionHandler(DefaultListModel model) {
            this.model = model;
        }

        public void actionPerformed(ActionEvent e) {
            final JComboBox comboBox = (JComboBox) e.getSource();
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
    private static final class URLTextFilterator implements TextFilterator<AutoCompleteSupportComplexTestApp.Url> {
        public void getFilterStrings(List<String> baseList, AutoCompleteSupportComplexTestApp.Url url) {
            final String location = url.getLocation();
            baseList.add(location);
            if (location != null) {
                if (location.startsWith("http://"))
                    baseList.add(location.substring(7));
                if (location.startsWith("http://www."))
                    baseList.add(location.substring(11));
            }
        }
    }

    private static final class UrlFormat extends Format {
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (obj != null)
                toAppendTo.append(((Url) obj).getLocation());
            return toAppendTo;
        }

        public Object parseObject(String source, ParsePosition pos) {
            return new Url(source);
        }
    }
}