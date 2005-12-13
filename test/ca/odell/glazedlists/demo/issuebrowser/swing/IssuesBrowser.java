/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

// demo
import ca.odell.glazedlists.demo.issuebrowser.*;
// swing
import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.event.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.*;
import java.net.URL;
import java.net.UnknownHostException;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.swing.*;
// for setting up the bounded range model
import java.util.Hashtable;
import java.text.MessageFormat;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesBrowser extends Applet {

    /** these don't belong here at all */
    private static final Color GLAZED_LISTS_DARK_BROWN = new Color(36, 23, 10);
    private static final Color GLAZED_LISTS_MEDIUM_BROWN = new Color(69, 64, 56);
    private static final Color GLAZED_LISTS_LIGHT_BROWN = new Color(246, 237, 220);

    /** an event list to host the issues */
    private EventList issuesEventList = new BasicEventList();

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

        // if this is running on Windows, we have advice for the user when we cannot lookup a hostname
        final String osname = System.getProperty("os.name");
        if (osname != null && osname.toLowerCase().contains("windows")) {
            Exceptions.getInstance().addHandler(new Exceptions.Handler() {
                public boolean recognize(Exception e) {
                    return e instanceof UnknownHostException;
                }
                public void handle(Exception e) {
                    // explain how to configure a Proxy Server for Java on Windows
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            final String message = "If connecting to the Internet via a proxy server,\n" +
                                                   "ensure you have configured Java correctly in\n" +
                                                   "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...";
                            JOptionPane.showMessageDialog(IssuesBrowser.this, message, "Unable to connect to the Internet", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                }
            });
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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        final MatcherEditor textFilterMatcherEditor = new ThreadedMatcherEditor(new TextComponentMatcherEditor(filterTextField, null));

        // create a MatcherEditor which edits the state filter
        StatusMatcherEditor statusMatcherEditor = new StatusMatcherEditor(issuesEventList);

        // create the pipeline of glazed lists
        SwingUsersMatcherEditor userMatcherEditor = new SwingUsersMatcherEditor(issuesEventList);
        FilterList issuesUserFiltered = new FilterList(issuesEventList, userMatcherEditor);
        FilterList issuesStatusFiltered = new FilterList(issuesUserFiltered, statusMatcherEditor);
        FilterList issuesTextFiltered = new FilterList(issuesStatusFiltered, textFilterMatcherEditor);
        ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");
        final SortedList issuesSortedList = new SortedList(priorityList, null);
        issuesSortedList.setMode(SortedList.AVOID_MOVING_ELEMENTS); // temp hack for playing with the new sorting mode

        // issues table
        issuesTableModel = new EventTableModel(issuesSortedList, new IssueTableFormat());
        JTable issuesJTable = new JTable(issuesTableModel);
        issuesSelectionModel = new EventSelectionModel(issuesSortedList);
        issuesSelectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE); // multi-selection best demos our awesome selection management
        issuesSelectionModel.addListSelectionListener(new IssuesSelectionListener());
        issuesJTable.setSelectionModel(issuesSelectionModel);
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        issuesJTable.setDefaultRenderer(Priority.class, new PriorityTableCellRenderer());
        new TableComparatorChooser(issuesJTable, issuesSortedList, true);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // users table
        JScrollPane usersListScrollPane = new JScrollPane(userMatcherEditor.getUserSelect(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // descriptions
        EventTableModel descriptionsTableModel = new EventTableModel(descriptions, new DescriptionTableFormat());
        JTable descriptionsTable = new JTable(descriptionsTableModel);
        descriptionsTable.setRowSelectionAllowed(false);
        descriptionsTable.getColumnModel().getColumn(0).setCellRenderer(new DescriptionRenderer());
        JScrollPane descriptionsTableScrollPane = new JScrollPane(descriptionsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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
        prioritySlider.setForeground(Color.BLACK);

        // projects
        EventList projects = Project.getProjects();

        // project select combobox
        EventComboBoxModel projectsComboModel = new EventComboBoxModel(projects);
        JComboBox projectsCombo = new JComboBox(projectsComboModel);
        projectsCombo.setEditable(false);
        projectsCombo.setOpaque(false);
        projectsCombo.addItemListener(new ProjectChangeListener());
        projectsComboModel.setSelectedItem(new Project(null, "Select a Java.net project..."));

        // build a label to display the number of issues in the issue table
        issueCounter = new IssueCounterLabel(issuesSortedList);
        issueCounter.setHorizontalAlignment(SwingConstants.CENTER);
        issueCounter.setForeground(Color.WHITE);

        // throbber icons
        ClassLoader jarLoader = IssuesBrowser.class.getClassLoader();
        URL url = jarLoader.getResource("ca/odell/glazedlists/demo/throbber-static.gif");
        if (url != null) throbberStatic = new ImageIcon(url);
        url = jarLoader.getResource("ca/odell/glazedlists/demo/throbber-active.gif");
        if (url != null) throbberActive = new ImageIcon(url);
        throbber = new JLabel(throbberStatic);
        throbber.setHorizontalAlignment(SwingConstants.RIGHT);

        // header bar
        JPanel iconBar = new GradientPanel(GLAZED_LISTS_MEDIUM_BROWN, GLAZED_LISTS_DARK_BROWN, true);
        iconBar.setLayout(new GridLayout(1, 3));
        iconBar.add(projectsCombo);
        iconBar.add(issueCounter);
        iconBar.add(throbber);
        iconBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create the filters panel
        JLabel textFilterLabel = new JLabel("Filter");
        textFilterLabel.setFont(textFilterLabel.getFont().deriveFont(11.0f));
        JLabel priorityLabel = new JLabel("Priority");
        priorityLabel.setFont(priorityLabel.getFont().deriveFont(11.0f));
        JLabel stateLabel = new JLabel("State");
        stateLabel.setFont(stateLabel.getFont().deriveFont(11.0f));
        JLabel userLabel = new JLabel("User");
        userLabel.setFont(userLabel.getFont().deriveFont(11.0f));

        JPanel filtersPanel = new JPanel();
        filtersPanel.setBackground(GLAZED_LISTS_LIGHT_BROWN);
        filtersPanel.setLayout(new GridBagLayout());
        filtersPanel.add(textFilterLabel,                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        filtersPanel.add(filterTextField,                    new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 10, 5), 0, 0));
        filtersPanel.add(stateLabel,                         new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        filtersPanel.add(statusMatcherEditor.getComponent(), new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 10, 5), 0, 0));
        filtersPanel.add(priorityLabel,                      new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        filtersPanel.add(prioritySlider,                     new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 10, 5), 0, 0));
        filtersPanel.add(userLabel,                          new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        filtersPanel.add(usersListScrollPane,                new GridBagConstraints(0, 7, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 0, 0, 0), 0, 0));

        // assemble all data components on a common panel
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());
        dataPanel.add(issuesTableScrollPane,       new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        dataPanel.add(descriptionsTableScrollPane, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        // the outermost panel to layout the icon bar with the other panels
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(iconBar,                     new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(filtersPanel,                new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(dataPanel,                   new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

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
            Project selectedProject = (Project) e.getItem();
            if(selectedProject.isValid()) issueLoader.setProject(selectedProject);
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
    private static class IssueCounterLabel extends JLabel implements ListEventListener {
        private static final MessageFormat issueCountFormat = new MessageFormat("{0} {0,choice,0#issues|1#issue|1<issues}");

        private int issueCount = -1;

        public IssueCounterLabel(EventList source) {
            source.addListEventListener(this);
            this.setIssueCount(source.size());
        }

        public void setIssueCount(int issueCount) {
            if (this.issueCount == issueCount) return;
            this.issueCount = issueCount;
            this.setText(issueCountFormat.format(new Object[] {new Integer(issueCount)}));
        }
        public void listChanged(ListEvent listChanges) {
            setIssueCount(listChanges.getSourceList().size());
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
        private boolean vertical;

        public GradientPanel(Color gradientStartColor, Color gradientEndColor, boolean vertical) {
            this.gradientStartColor = gradientStartColor;
            this.gradientEndColor = gradientEndColor;
            this.vertical = vertical;
        }

        public void paintComponent(Graphics g) {
            if (this.isOpaque())
                paintGradient((Graphics2D) g, this.gradientStartColor, this.gradientEndColor, vertical ? this.getHeight() : this.getWidth(), vertical);
        }
    }

    /**
     * A convenience method to paint a gradient between <code>gradientStartColor</code>
     * and <code>gradientEndColor</code> over <code>length</code> pixels.
     */
    private static void paintGradient(Graphics2D g2d, Color gradientStartColor, Color gradientEndColor, int length, boolean vertical) {
        final Paint oldPainter = g2d.getPaint();
        try {
            if(vertical) g2d.setPaint(new GradientPaint(0, 0, gradientStartColor, 0, length, gradientEndColor));
            else g2d.setPaint(new GradientPaint(0, 0, gradientStartColor, length, 0, gradientEndColor));
            g2d.fill(g2d.getClip());
        } finally {
            g2d.setPaint(oldPainter);
        }
    }
}