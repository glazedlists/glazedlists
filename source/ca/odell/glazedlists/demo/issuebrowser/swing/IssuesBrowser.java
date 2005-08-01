/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

// demo
import ca.odell.glazedlists.demo.issuebrowser.*;
import ca.odell.glazedlists.demo.Launcher;
// swing
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.border.Border;
import javax.swing.event.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.*;
import java.net.URL;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.swing.*;
// for setting up the bounded range model
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.text.MessageFormat;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesBrowser extends Applet {

    /** these don't belong here at all */
    private static final Color GLAZED_LISTS_ORANGE = new Color(255, 119, 0);
    private static final Color GLAZED_LISTS_ORANGE_LIGHT = new Color(241, 212, 189);

    private static final Color BLUE_DARK = new Color(126, 165, 232);
    private static final Color BLUE_LIGHT = new Color(197, 210, 232);

    private static final Border BLACK_LINE_BORDER = BorderFactory.createLineBorder(Color.BLACK);

    /** A header renderer that paints a gradient background from BLUE_DARK to BLUE_LIGHT */
    private static final GradientTableHeaderCellRenderer DEFAULT_HEADER_RENDERER = new GradientTableHeaderCellRenderer(BLUE_DARK, BLUE_LIGHT);

    /** an event list to host the issues */
    private UniqueList issuesEventList = new UniqueList(new BasicEventList());

    /** the currently selected issues */
    private EventSelectionModel issuesSelectionModel = null;

    private TableModel issuesTableModel = null;

    private Issue descriptionIssue = null;
    /** an event list to host the descriptions */
    private EventList descriptions = new BasicEventList();

    /** monitor loading the issues */
    private JLabel throbber = null;
    private ImageIcon throbberActive = null;
    private ImageIcon throbberStatic = null;

    /** a label to display the count of issues in the issue table */
    private IssueCounterLabel issueCounter = null;

    /** loads issues as requested */
    private IssueLoader issueLoader = new IssueLoader(issuesEventList, new IndeterminateToggler());

    /**
     * Load the issues browser as an applet.
     */
    public IssuesBrowser() {
        this(true);
    }

    /**
     * Loads the issues browser as standalone or as an applet.
     */
    public IssuesBrowser(boolean applet) {
        if (applet) {
            constructApplet();
        } else {
            constructStandalone();
        }

        // debug a problem where the thread is getting interrupted
        if (Thread.currentThread().isInterrupted()) {
            new Exception("thread has been interrupted").printStackTrace();
        }

        // start loading the issues
        issueLoader.start();
    }

    /**
     * Constructs the browser as an Applet.
     */
    private void constructApplet() {
        setLayout(new GridBagLayout());
        add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        // create a frame with that panel
        JFrame frame = new JFrame("Issues");
        if (!Launcher.runningInLauncher()) {
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        }

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.setVisible(true);
    }

    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // create a MatcherEditor which edits the filter text
        final JTextField filterTextField = new JTextField();
        filterTextField.setBorder(BLACK_LINE_BORDER);
        final MatcherEditor textFilterMatcherEditor = new ThreadedMatcherEditor(new TextComponentMatcherEditor(filterTextField, null));

        // create a MatcherEditor which edits the state filter
        StateMatcherEditor stateMatcherEditor = new StateMatcherEditor();

        // create the pipeline of glazed lists
        IssuesUserFilter issuesUserFiltered = new IssuesUserFilter(issuesEventList);
        FilterList issuesStateFiltered = new FilterList(issuesUserFiltered, stateMatcherEditor);
        FilterList issuesTextFiltered = new FilterList(issuesStateFiltered, textFilterMatcherEditor);
        ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");
        final SortedList issuesSortedList = new SortedList(priorityList);

        // issues table
        issuesTableModel = new EventTableModel(issuesSortedList, new IssueTableFormat());
        issuesSortedList.addListEventListener(new ListEventListener() {
            public void listChanged(ListEvent listChanges) {
                issueCounter.setIssueCount(issuesSortedList.size());
            }
        });
        JTable issuesJTable = new JTable(issuesTableModel);
        issuesSelectionModel = new EventSelectionModel(issuesSortedList);
        issuesSelectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE); // multi-selection best demos our awesome selection management
        issuesSelectionModel.addListSelectionListener(new IssuesSelectionListener());
        issuesJTable.setSelectionModel(issuesSelectionModel);
        issuesJTable.getTableHeader().setDefaultRenderer(DEFAULT_HEADER_RENDERER);
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        issuesJTable.setDefaultRenderer(Priority.class, new PriorityTableCellRenderer());
        new TableComparatorChooser(issuesJTable, issuesSortedList, true);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        issuesTableScrollPane.setBorder(BLACK_LINE_BORDER);
        issuesTableScrollPane.setOpaque(false);
        issuesTableScrollPane.getViewport().setOpaque(false);

        // users table
        JScrollPane usersListScrollPane = new JScrollPane(issuesUserFiltered.getUserSelect(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        usersListScrollPane.setBorder(BLACK_LINE_BORDER);

        // descriptions
        EventTableModel descriptionsTableModel = new EventTableModel(descriptions, new DescriptionTableFormat());
        JTable descriptionsTable = new JTable(descriptionsTableModel);
        descriptionsTable.getTableHeader().setDefaultRenderer(DEFAULT_HEADER_RENDERER);
        descriptionsTable.getColumnModel().getColumn(0).setCellRenderer(new DescriptionRenderer());
        JScrollPane descriptionsTableScrollPane = new JScrollPane(descriptionsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionsTableScrollPane.setBorder(BLACK_LINE_BORDER);
        descriptionsTableScrollPane.setOpaque(false);
        descriptionsTableScrollPane.getViewport().setBackground(UIManager.getColor("List.background"));

        // priority slider
        BoundedRangeModel priorityRangeModel = GlazedListsSwing.lowerRangeModel(priorityList);
        priorityRangeModel.setRangeProperties(0, 0, 0, 100, false);
        JSlider prioritySlider = new JSlider(priorityRangeModel);
        Hashtable prioritySliderLabels = new Hashtable();
        prioritySliderLabels.put(new Integer(0), new JLabel("Low"));
        prioritySliderLabels.put(new Integer(100), new JLabel("High"));
        prioritySlider.setOpaque(false);
        prioritySlider.setLabelTable(prioritySliderLabels);
        prioritySlider.setSnapToTicks(true);
        prioritySlider.setPaintLabels(true);
        prioritySlider.setPaintTicks(true);
        prioritySlider.setForeground(UIManager.getColor("Label.foreground"));
        prioritySlider.setMajorTickSpacing(25);

        // projects
        EventList projects = Project.getProjects();

        // project select combobox
        EventComboBoxModel projectsComboModel = new EventComboBoxModel(projects);
        JComboBox projectsCombo = new JComboBox(projectsComboModel);
        projectsCombo.setEditable(false);
        projectsCombo.setBackground(GLAZED_LISTS_ORANGE_LIGHT);
        projectsCombo.addItemListener(new ProjectChangeListener());
        projectsComboModel.setSelectedItem(new Project(null, "Select a Java.net project..."));

        // throbber icons
        JPanel iconBar = new JPanel();
        iconBar.setBackground(GLAZED_LISTS_ORANGE);
        iconBar.setLayout(new GridBagLayout());
        ClassLoader jarLoader = IssuesBrowser.class.getClassLoader();
        URL url = jarLoader.getResource("resources/demo/throbber-static.gif");
        if (url != null) throbberStatic = new ImageIcon(url);
        url = jarLoader.getResource("resources/demo/throbber-active.gif");
        if (url != null) throbberActive = new ImageIcon(url);
        throbber = new JLabel(throbberStatic);
        iconBar.add(projectsCombo,                           new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
        iconBar.add(throbber,                                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

        // create the filters panel
        JPanel filtersPanel = new GradientPanel(BLUE_DARK, BLUE_LIGHT);
        filtersPanel.setBorder(BLACK_LINE_BORDER);
        filtersPanel.setPreferredSize(new Dimension(200, 400));
        filtersPanel.setLayout(new GridBagLayout());
        filtersPanel.add(new JLabel("Text Filter"),          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 5, 10), 0, 0));
        filtersPanel.add(filterTextField,                    new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 15, 10), 0, 0));
        filtersPanel.add(new JLabel("State"),                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 10), 0, 0));
        filtersPanel.add(stateMatcherEditor.getComponent(),  new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 15, 10), 0, 0));
        filtersPanel.add(new JLabel("Minimum Priority"),     new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 10), 0, 0));
        filtersPanel.add(prioritySlider,                     new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 15, 10), 0, 0));
        filtersPanel.add(new JLabel("User"),                 new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 10), 0, 0));
        filtersPanel.add(usersListScrollPane,                new GridBagConstraints(0, 7, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));

        // put some insets around the filters panel
        JPanel filterSpacerPanel = new GradientPanel(GLAZED_LISTS_ORANGE, GLAZED_LISTS_ORANGE_LIGHT);
        filterSpacerPanel.setLayout(new GridBagLayout());
        filterSpacerPanel.add(filtersPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 5, 5, 5), 0, 0));

        // build a label to display the number of issues in the issue table
        issueCounter = new IssueCounterLabel();

        // assemble all data components on a common panel
        JPanel dataPanel = new GradientPanel(GLAZED_LISTS_ORANGE, GLAZED_LISTS_ORANGE_LIGHT);
        dataPanel.setLayout(new GridBagLayout());
        dataPanel.add(issuesTableScrollPane,       new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 5, 0, 5), 0, 0));
        dataPanel.add(issueCounter,                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
        dataPanel.add(descriptionsTableScrollPane, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 5, 5, 5), 0, 0));

        // the outermost panel to layout the icon bar with the other panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(iconBar,           BorderLayout.NORTH);
        mainPanel.add(filterSpacerPanel, BorderLayout.WEST);
        mainPanel.add(dataPanel,         BorderLayout.CENTER);

        return mainPanel;
    }

    /**
     * Listens for changes in the selection on the issues table.
     */
    class IssuesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            // get the newly selected issue
            Issue selected = null;
            if(issuesSelectionModel.getSelected().size() > 0) selected = (Issue)issuesSelectionModel.getSelected().get(0);

            // update the description issue
            if(selected == descriptionIssue) return;
            descriptionIssue = selected;
            descriptions.clear();
            if(descriptionIssue != null) descriptions.addAll(descriptionIssue.getDescriptions());
        }
    }

    /**
     * Listens for changes to the project combo box and updates which project is
     * being loaded.
     */
    class ProjectChangeListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            Project selected = (Project) e.getItem();
            if(selected.isValid()) issueLoader.setProject((Project) selected);
        }
    }

    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(String[] args) {
        // load the issues and display the browser
        IssuesBrowser browser = new IssuesBrowser(false);

        // load the initial project, if requested
        if(args.length == 1) {
            Project initialProject = new Project("url", "Url", args[0]);
            browser.issueLoader.setProject(initialProject);
        }
    }


    /**
     * Toggles the throbber on and off.
     */
    private class IndeterminateToggler implements Runnable, Throbber {

        /** whether the throbber will be turned on or off */
        private boolean on = false;

        public synchronized void setOn() {
            if (!on) {
                on = true;
                SwingUtilities.invokeLater(this);
            }
        }

        public synchronized void setOff() {
            if (on) {
                on = false;
                SwingUtilities.invokeLater(this);
            }
        }

        public synchronized void run() {
            if(on) throbber.setIcon(throbberActive);
            else throbber.setIcon(throbberStatic);
        }
    }

    /**
     * A custom label designed for displaying the number of issues in the issue
     * table. Use {@link #setIssueCount(int)} to update the text of the label
     * to reflect a new issue count.
     */
    private static class IssueCounterLabel extends JLabel {
        private int issueCount = -1;

        {
            this.setIssueCount(0);
        }

        public void setIssueCount(int issueCount) {
            if (this.issueCount != issueCount) {
                this.issueCount = issueCount;
                this.setText(MessageFormat.format("{0} {0,choice,0#issues|1#issue|1<issues}", new Object[] {new Integer(issueCount)}));
            }
        }
    }

    /**
     * A MatcherEditor that produces Matchers that filter the issues based on the selected states.
     */
    private static class StateMatcherEditor extends AbstractMatcherEditor implements ActionListener {
        /** A panel housing a checkbox for each state. */
        private JPanel checkBoxPanel = new JPanel(new GridLayout(2, 2));

        /** A checkbox for each possible state. */
        private final JCheckBox[] stateCheckBoxes;

        public StateMatcherEditor() {
            final JCheckBox newStateCheckBox = buildCheckBox("New");
            final JCheckBox resolvedStateCheckBox = buildCheckBox("Resolved");
            final JCheckBox startedStateCheckBox = buildCheckBox("Started");
            final JCheckBox closeStateCheckBox = buildCheckBox("Closed");

            this.stateCheckBoxes = new JCheckBox[] {newStateCheckBox, resolvedStateCheckBox, startedStateCheckBox, closeStateCheckBox};

            this.checkBoxPanel.setOpaque(false);

            // add each checkbox to the panel and start listening to selections
            for (int i = 0; i < this.stateCheckBoxes.length; i++) {
                this.stateCheckBoxes[i].addActionListener(this);
                this.checkBoxPanel.add(this.stateCheckBoxes[i]);
            }
        }

        /**
         * Returns the component responsible for editing the state filter
         */
        public Component getComponent() {
            return this.checkBoxPanel;
        }

        /**
         * A convenience method to build a state checkbox with the given name.
         */
        private static JCheckBox buildCheckBox(String name) {
            final JCheckBox checkBox = new JCheckBox(name, true);
            checkBox.setOpaque(false);
            checkBox.setFocusable(false);
            checkBox.setMargin(new Insets(0, 0, 0, 0));
            return checkBox;
        }

        /**
         * Returns a StateMatcher which matches Issues if their state is one
         * of the selected states.
         */
        private StateMatcher buildMatcher() {
            final Set allowedStates = new HashSet();
            for (int i = 0; i < this.stateCheckBoxes.length; i++) {
                if (this.stateCheckBoxes[i].isSelected())
                    allowedStates.add(this.stateCheckBoxes[i].getText().toUpperCase().intern());
            }

            return new StateMatcher(allowedStates);
        }

        public void actionPerformed(ActionEvent e) {
            // determine if the checkbox that generated this ActionEvent is freshly checked or freshly unchecked
            // - we'll use that information to determine whether this is a constrainment or relaxation of the matcher
            final boolean isCheckBoxSelected = ((JCheckBox) e.getSource()).isSelected();

            // build a StateMatcher
            final StateMatcher stateMatcher = this.buildMatcher();

            // fire a MatcherEvent of the appropriate type
            if (stateMatcher.getStateCount() == 0)
                this.fireMatchNone();
            else if (stateMatcher.getStateCount() == this.stateCheckBoxes.length)
                this.fireMatchAll();
            else if (isCheckBoxSelected)
                this.fireRelaxed(stateMatcher);
            else
                this.fireConstrained(stateMatcher);
        }

        /**
         * A StateMatcher returns <tt>true</tt> if the state of the Issue is
         * one of the viewable states selected by the user.
         */
        private static class StateMatcher implements Matcher {
            private final Set allowedStates;

            public StateMatcher(Set allowedStates) {
                this.allowedStates = allowedStates;
            }

            public int getStateCount() {
                return this.allowedStates.size();
            }

            public boolean matches(Object item) {
                final Issue issue = (Issue) item;
                return this.allowedStates.contains(issue.getStatus());
            }
        }
    }

    /**
     * A customized TableCellRenderer which paints a color gradient for its
     * background rather than a single color. The start and end colors of the
     * gradient are specified via the constructor.
     */
    private static class GradientTableHeaderCellRenderer extends DefaultTableCellRenderer {
        private Color gradientStartColor;
        private Color gradientEndColor;

        public GradientTableHeaderCellRenderer(Color gradientStartColor, Color gradientEndColor) {
            this.gradientStartColor = gradientStartColor;
            this.gradientEndColor = gradientEndColor;
            this.setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            setText((value == null) ? "" : value.toString());
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            return this;
        }

        public void paintComponent(Graphics g) {
            paintGradient((Graphics2D) g, this.gradientStartColor, this.gradientEndColor, this.getHeight());
            super.paintComponent(g);
        }
    }

    /**
     * A customized panel which paints a color gradient for its background
     * rather than a single color. The start and end colors of the gradient
     * are specified via the constructor.
     */
    private static class GradientPanel extends JPanel {
        private Color gradientStartColor;
        private Color gradientEndColor;

        public GradientPanel(Color gradientStartColor, Color gradientEndColor) {
            this.gradientStartColor = gradientStartColor;
            this.gradientEndColor = gradientEndColor;
        }

        public void paintComponent(Graphics g) {
            if (this.isOpaque())
                paintGradient((Graphics2D) g, this.gradientStartColor, this.gradientEndColor, this.getHeight());
        }
    }

    /**
     * A convenience method to paint a gradient between <code>gradientStartColor</code>
     * and <code>gradientEndColor</code> over <code>height</code> pixels.
     */
    private static void paintGradient(Graphics2D g2d, Color gradientStartColor, Color gradientEndColor, int height) {
        final Paint oldPainter = g2d.getPaint();
        try {
            g2d.setPaint(new GradientPaint(0, 0, gradientStartColor, 0, height, gradientEndColor));
            g2d.fill(g2d.getClip());
        } finally {
            g2d.setPaint(oldPainter);
        }
    }
}
