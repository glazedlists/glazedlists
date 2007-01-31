/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.*;
import com.publicobject.issuesbrowser.*;
import com.publicobject.misc.Exceptions;
import com.publicobject.misc.swing.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.text.MessageFormat;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IssuesBrowser implements Runnable {

    /** application appearance */
    public static final Color GLAZED_LISTS_DARK_BROWN = new Color(36, 23, 10);
    public static final Color GLAZED_LISTS_MEDIUM_BROWN = new Color(69, 64, 56);
    public static final Color GLAZED_LISTS_MEDIUM_LIGHT_BROWN = new Color(150, 140, 130);
    public static final Color GLAZED_LISTS_LIGHT_BROWN = new Color(246, 237, 220);
    public static final Color GLAZED_LISTS_LIGHT_BROWN_DARKER = new Color(231, 222, 205);
    public static final Icon THROBBER_ACTIVE = loadIcon("resources/throbber-active.gif");
    public static final Icon THROBBER_STATIC = loadIcon("resources/throbber-static.gif");
    public static final Icon EXPANDED_ICON = Icons.triangle(9, SwingConstants.EAST, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Icon COLLAPSED_ICON = Icons.triangle(9, SwingConstants.SOUTH, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Icon X_ICON = Icons.x(10, 5, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Border EMPTY_ONE_PIXEL_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    public static final Border EMPTY_TWO_PIXEL_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    /** an event list to host the issues */
    private EventList<Issue> issuesEventList = new BasicEventList<Issue>();

    /** all the filters currently applied to the issues list */
    private FilterPanel filterPanel = new FilterPanel(issuesEventList);

    /** the currently selected issues */
    private EventSelectionModel issuesSelectionModel;

    /** the TableModel containing the filtered and sorted issues */
    private EventTableModel issuesTableModel;

    /** the currently selected issue for which the details are displayed */
    private Issue descriptionIssue;

    /** the component that displays the details of the selected issue, if any */
    private IssueDetailsComponent issueDetails;

    /** monitor loading the issues */
    private JLabel throbber;

    /** a label to display the count of issues in the issue table */
    private IssueCounterLabel issueCounter;

    /** loads issues as requested */
    private IssueLoader issueLoader;

    /** the application window */
    private JFrame frame;

    /** things to handle when booting the issues loader */
    private String[] startupArgs;

    /**
     * Tell the IssuesBrowser how to configure itself when starting up.
     */
    public void setStartupArgs(String[] startupArgs) {
        this.startupArgs = startupArgs;
    }

    /**
     * Loads the issues browser as standalone application.
     */
    public void run() {
        constructStandalone();

        // we have advice for the user when we cannot connect to a host
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.unknownHostExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.connectExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.noRouteToHostExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.accessControlExceptionHandler(frame));
        Exceptions.getInstance().addHandler(ExceptionHandlerFactory.ioExceptionCode500Handler(frame));

        // create the issue loader and start loading issues
        issueLoader = new IssueLoader(issuesEventList, new IndeterminateToggler(throbber, THROBBER_ACTIVE, THROBBER_STATIC));
        issueLoader.start();

        // load issues from a file if requested
        if(startupArgs.length == 1) {
            issueLoader.setFileName(startupArgs[0]);
        }
    }

    /**
     * Load the specified icon from the pathname on the classpath.
     */
    private static ImageIcon loadIcon(String pathname) {
        ClassLoader jarLoader = IssuesBrowser.class.getClassLoader();
        URL url = jarLoader.getResource(pathname);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        frame = new JFrame("Issues");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 600);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(constructView(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // sort the original issues list
        final SortedList<Issue> issuesSortedList = new SortedList<Issue>(issuesEventList, null);

        // filter the sorted issues
        FilterList<Issue> filteredIssues = new FilterList<Issue>(issuesSortedList, filterPanel.getMatcherEditor());

        SeparatorList<Issue> separatedIssues = new SeparatorList<Issue>(filteredIssues, GlazedLists.beanPropertyComparator(Issue.class, "subcomponent"), 0, Integer.MAX_VALUE);

        // build the issues table
        issuesTableModel = new EventTableModel<Issue>(separatedIssues, new IssueTableFormat());
        final TableColumnModel issuesTableColumnModel = new EventTableColumnModel(new BasicEventList<TableColumn>());
        JSeparatorTable issuesJTable = new JSeparatorTable(issuesTableModel, issuesTableColumnModel);
        issuesJTable.setAutoCreateColumnsFromModel(true);
        
        issuesJTable.setSeparatorRenderer(new IssueSeparatorTableCell(separatedIssues));
        issuesJTable.setSeparatorEditor(new IssueSeparatorTableCell(separatedIssues));
        issuesSelectionModel = new EventSelectionModel<Issue>(separatedIssues);
        issuesSelectionModel.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE); // multi-selection best demos our awesome selection management
        issuesSelectionModel.addListSelectionListener(new IssuesSelectionListener());
        issuesJTable.setSelectionModel(issuesSelectionModel);
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(300);
        issuesJTable.getColumnModel().getColumn(6).setPreferredWidth(300);
        issuesJTable.getColumnModel().getColumn(7).setPreferredWidth(1000);
        // turn off cell focus painting
        issuesJTable.setDefaultRenderer(String.class, new NoFocusRenderer(issuesJTable.getDefaultRenderer(String.class)));
        issuesJTable.setDefaultRenderer(Integer.class, new NoFocusRenderer(issuesJTable.getDefaultRenderer(Integer.class)));
        issuesJTable.setDefaultRenderer(Priority.class, new NoFocusRenderer(new PriorityTableCellRenderer()));
        LookAndFeelTweaks.tweakTable(issuesJTable);
        TableComparatorChooser.install(issuesJTable, issuesSortedList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        issuesTableScrollPane.getViewport().setBackground(UIManager.getColor("EditorPane.background"));
        issuesTableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        issueDetails = new IssueDetailsComponent(filteredIssues);

        // projects
        EventList<Project> projects = Project.getProjects();

        // project select combobox
        EventComboBoxModel projectsComboModel = new EventComboBoxModel<Project>(projects);
        JComboBox projectsCombo = new JComboBox(projectsComboModel);
        projectsCombo.setEditable(false);
        projectsCombo.setOpaque(false);
        projectsCombo.addItemListener(new ProjectChangeListener());
        projectsComboModel.setSelectedItem(new Project(null, "Select a Java.net project..."));

        // build a label to display the number of issues in the issue table
        issueCounter = new IssueCounterLabel(filteredIssues);
        issueCounter.setHorizontalAlignment(SwingConstants.CENTER);
        issueCounter.setForeground(Color.WHITE);

        // throbber
        throbber = new JLabel(THROBBER_STATIC);
        throbber.setHorizontalAlignment(SwingConstants.RIGHT);

        // header bar
        JPanel iconBar = new GradientPanel(GLAZED_LISTS_MEDIUM_BROWN, GLAZED_LISTS_DARK_BROWN, true);
        iconBar.setLayout(new GridLayout(1, 3));
        iconBar.add(projectsCombo);
        iconBar.add(issueCounter);
        iconBar.add(throbber);
        iconBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // assemble all data components on a common panel
        JPanel dataPanel = new JPanel();
        JComponent issueDetailsComponent = issueDetails;
        dataPanel.setLayout(new GridLayout(2, 1));
        dataPanel.add(issuesTableScrollPane);
        dataPanel.add(issueDetailsComponent);

        // draw lines between components
        JComponent filtersPanel = filterPanel.getComponent();
        filtersPanel.setBorder(BorderFactory.createEmptyBorder());
        //filtersPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, IssuesBrowser.GLAZED_LISTS_DARK_BROWN));
        issueDetailsComponent.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, GLAZED_LISTS_DARK_BROWN));

        // the outermost panel to layout the icon bar with the other panels
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(iconBar,                        new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(filtersPanel,                   new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(Box.createHorizontalStrut(240), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(dataPanel,                      new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

        return mainPanel;
    }

    /**
     * Listens for changes in the selection on the issues table.
     */
    class IssuesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            // get the newly selected issue
            Issue selectedIssue = null;
            if(issuesSelectionModel.getSelected().size() > 0) {
                Object selectedObject = issuesSelectionModel.getSelected().get(0);
                if(selectedObject instanceof Issue) {
                    selectedIssue = (Issue)selectedObject;
                }
            }

            // update the description issue
            if(selectedIssue == descriptionIssue) return;
            descriptionIssue = selectedIssue;
            issueDetails.setIssue(descriptionIssue);
        }
    }

    /**
     * Listens for changes to the project combo box and updates which project is
     * being loaded.
     */
    class ProjectChangeListener implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() != ItemEvent.SELECTED) return;

            final Project selectedProject = (Project) e.getItem();
            if(selectedProject.isValid()) issueLoader.setProject(selectedProject);
        }
    }

    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new IssuesBrowserStarter(args));
    }

    /**
     * This Runnable contains the logic to start the IssuesBrowser application.
     * It is guaranteed to be executed on the EventDispatch Thread.
     */
    private static class IssuesBrowserStarter implements Runnable {
        private final String[] args;

        public IssuesBrowserStarter(String[] args) {
            this.args = args;
        }

        public void run() {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // do nothing - fall back to default look and feel
            }

            // load the issues and display the browser
            final IssuesBrowser browser = new IssuesBrowser();
            browser.setStartupArgs(args);
            browser.run();
        }
    }

    /**
     * A custom label designed for displaying the number of issues in the issue
     * table. Use {@link #setIssueCount(int)} to update the text of the label
     * to reflect a new issue count.
     */
    private static class IssueCounterLabel extends JLabel implements ListEventListener<Issue> {
        private static final MessageFormat issueCountFormat = new MessageFormat("{0} {0,choice,0#issues|1#issue|1<issues}");

        private int issueCount = -1;

        public IssueCounterLabel(EventList<Issue> source) {
            source.addListEventListener(this);
            this.setIssueCount(source.size());
        }

        public void setIssueCount(int issueCount) {
            if (this.issueCount == issueCount) return;
            this.issueCount = issueCount;
            this.setText(issueCountFormat.format(new Object[] {new Integer(issueCount)}));
        }
        public void listChanged(ListEvent<Issue> listChanges) {
            setIssueCount(listChanges.getSourceList().size());
        }
    }

    /**
     * Render the issues separator.
     */
    public static class IssueSeparatorTableCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        private final MessageFormat nameFormat = new MessageFormat("{0} ({1})");

        /** the separator list to lock */
        private final SeparatorList separatorList;

        private final JPanel panel = new JPanel(new BorderLayout());
        private final JButton expandButton;
        private final JLabel nameLabel = new JLabel();

        private SeparatorList.Separator<Issue> separator;

        public IssueSeparatorTableCell(SeparatorList separatorList) {
            this.separatorList = separatorList;

            this.expandButton = new JButton(EXPANDED_ICON);
            this.expandButton.setOpaque(false);
            this.expandButton.setBorder(EMPTY_TWO_PIXEL_BORDER);
            this.expandButton.setIcon(EXPANDED_ICON);
            this.expandButton.setContentAreaFilled(false);

            this.nameLabel.setFont(nameLabel.getFont().deriveFont(10.0f));
            this.nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            this.expandButton.addActionListener(this);

            this.panel.setBackground(GLAZED_LISTS_LIGHT_BROWN);
            this.panel.add(expandButton, BorderLayout.WEST);
            this.panel.add(nameLabel, BorderLayout.CENTER);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            configure(value);
            return panel;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            configure(value);
            return panel;
        }

        public Object getCellEditorValue() {
            return this.separator;
        }

        private void configure(Object value) {
            this.separator = (SeparatorList.Separator<Issue>)value;
            Issue issue = separator.first();
            if(issue == null) return; // handle 'late' rendering calls after this separator is invalid
            expandButton.setIcon(separator.getLimit() == 0 ? EXPANDED_ICON : COLLAPSED_ICON);
            nameLabel.setText(nameFormat.format(new Object[] {issue.getSubcomponent(), new Integer(separator.size())}));
        }

        public void actionPerformed(ActionEvent e) {
            separatorList.getReadWriteLock().writeLock().lock();
            boolean collapsed;
            try {
                collapsed = separator.getLimit() == 0;
                separator.setLimit(collapsed ? Integer.MAX_VALUE : 0);
            } finally {
                separatorList.getReadWriteLock().writeLock().unlock();
            }
            expandButton.setIcon(collapsed ? COLLAPSED_ICON : EXPANDED_ICON);
        }
    }

    /**
     * A button that shows icons in one of three states for
     * up, over and down.
     */
    public static class IconButton extends JButton implements MouseListener {
        private static final Border emptyBorder = BorderFactory.createEmptyBorder();

        private static final int UP = 0;
        private static final int OVER = 1;
        private static final int DOWN = 2;
        private int state = -1;

        private Icon[] icons;

        public IconButton(Icon[] icons) {
            this.icons = icons;
            super.setBorder(emptyBorder);
            setState(UP);
            setContentAreaFilled(false);

            addMouseListener(this);
        }

        public Icon[] getIcons() {
            return icons;
        }

        public void setIcons(Icon[] icons) {
            this.icons = icons;
            super.setIcon(this.icons[state]);
        }

        private void setState(int state) {
            this.state = state;
            super.setIcon(icons[state]);
        }

        public void mouseClicked(MouseEvent e) {
            // do nothing
        }
        public void mousePressed(MouseEvent e) {
            setState(DOWN);
        }
        public void mouseReleased(MouseEvent e) {
            setState(OVER);
        }
        public void mouseEntered(MouseEvent e) {
            setState(OVER);
        }
        public void mouseExited(MouseEvent e) {
            setState(UP);
        }
    }
}