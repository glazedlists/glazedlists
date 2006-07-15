/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import com.publicobject.issuesbrowser.*;
import com.publicobject.misc.Exceptions;
import com.publicobject.misc.swing.Icons;
import com.publicobject.misc.swing.JSeparatorTable;
import com.publicobject.misc.swing.NoFocusRenderer;
import com.publicobject.misc.swing.IndeterminateToggler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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
    private EventSelectionModel issuesSelectionModel = null;

    private EventTableModel issuesTableModel = null;

    private Issue descriptionIssue = null;

    private IssueDetailsComponent issueDetails;

    /** monitor loading the issues */
    private JLabel throbber = null;

    /** a label to display the count of issues in the issue table */
    private IssueCounterLabel issueCounter = null;

    /** loads issues as requested */
    private IssueLoader issueLoader = null;

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
        Exceptions.getInstance().addHandler(new UnknownHostExceptionHandler());
        Exceptions.getInstance().addHandler(new ConnectExceptionHandler());
        Exceptions.getInstance().addHandler(new NoRouteToHostExceptionHandler());
        Exceptions.getInstance().addHandler(new AccessControlExceptionHandler());
        Exceptions.getInstance().addHandler(new IOExceptionCode500Handler());

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
        JSeparatorTable issuesJTable = new JSeparatorTable(issuesTableModel);
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
        new TableComparatorChooser<Issue>(issuesJTable, issuesSortedList, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
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
     * A customized panel which paints a color gradient for its background
     * rather than a single color. The start and end colors of the gradient
     * are specified via the constructor.
     */
    public static class GradientPanel extends JPanel {
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

    /**
     * Returns <tt>true</tt> if this application is executing on a Windows
     * operating system; <tt>false</tt> otherwise.
     */
    private static boolean isWindowsOS() {
        final String osname = System.getProperty("os.name");
        return osname != null && osname.toLowerCase().indexOf("windows") == 0;
    }

    /**
     * An abstract Exceptions.Handler for all types of Exceptions that indicate
     * a connection to the internet could not be established. It displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private abstract class AbstractCannotConnectExceptionHandler implements Exceptions.Handler {
        public void handle(Exception e) {
            final String title = "Unable to connect to the Internet";

            final String message;
            if (isWindowsOS()) {
                // explain how to configure a Proxy Server for Java on Windows
                message = "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.";
            } else {
                message = "Please check your Internet connection settings.";
            }

            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(title, message));
        }
    }

    /**
     * An Exceptions.Handler for UnknownHostExceptions that displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private class UnknownHostExceptionHandler extends AbstractCannotConnectExceptionHandler {
        public boolean recognize(Exception e) {
            return e instanceof UnknownHostException;
        }
    }

    /**
     * An Exceptions.Handler for ConnectExceptions that displays an
     * informative message stating how to configure Java to use a proxy
     * server.
     */
    private class ConnectExceptionHandler extends AbstractCannotConnectExceptionHandler {
        public boolean recognize(Exception e) {
            return e instanceof ConnectException;
        }
    }

    /**
     * An Exceptions.Handler for NoRouteToHostException that displays an
     * informative message stating the probable cause and how to configure
     * Java to use a proxy server.
     */
    private class NoRouteToHostExceptionHandler implements Exceptions.Handler {
        public boolean recognize(Exception e) {
            return e instanceof NoRouteToHostException;
        }

        public void handle(Exception e) {
            final String title = "Unable to find a route to the Host";

            final String message;
            if (isWindowsOS()) {
                // explain how to configure a Proxy Server for Java on Windows
                message = "Typically, the remote host cannot be reached because of an\n" +
                          "intervening firewall, or if an intermediate router is down.\n\n" +
                          "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.";
            } else {
                message = "Please check your Internet connection settings.";
            }

            // explain how to configure a Proxy Server for Java on Windows
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(title, message));
        }
    }

    /**
     * An Exceptions.Handler for an AccessControlException when attempting to resolve
     * a hostname to an IP address or connect to that IP. It displays an informative
     * message stating the probable cause and how to configure Java to use a proxy server.
     */
    private class AccessControlExceptionHandler implements Exceptions.Handler {
        // sample message 1: "access denied (java.net.SocketPermission javacc.dev.java.net resolve)"
        // sample message 2: "access denied (java.net.SocketPermission beavertn-svr-eh.ad.nike.com:8080 connect,resolve)"
        private final Matcher messageMatcher = Pattern.compile(".*access denied \\p{Punct}java.net.SocketPermission (\\S*) (.*)").matcher("");

        public boolean recognize(Exception e) {
            return e instanceof AccessControlException && messageMatcher.reset(e.getMessage()).matches();
        }

        public void handle(Exception e) {
            final String title = "Unable to connect to Host";

            final String message;
            if (isWindowsOS()) {
                final String hostname = messageMatcher.group(1);

                // explain how to configure a Proxy Server for Java on Windows
                message = MessageFormat.format(
                          "Insufficient security privileges to connect to:\n\n\t{0} \n\n" +
                          "If connecting to the Internet via a proxy server,\n" +
                          "ensure you have configured Java correctly in\n" +
                          "Control Panel \u2192 Java \u2192 General \u2192 Network Settings...\n\n" +
                          "You must restart this application if you adjust the settings.", new Object[] {hostname});
            } else {
                message = "Please check your Internet connection settings.";
            }

            // explain how to configure a Proxy Server for Java on Windows
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(title, message));
        }
    }

    /**
     * An Exceptions.Handler for an IOException containing a HTTP response code of 500
     * indicating some error occurred within the java.net webserver. All a use can do
     * at this point is retry the operation later.
     */
    private class IOExceptionCode500Handler implements Exceptions.Handler {
        // sample message: "Server returned HTTP response code: 500 for URL: https://javanettasks.dev.java.net/issues/xml.cgi?id=1:2:3:4:5:6:..."
        private final Matcher messageMatcher = Pattern.compile("Server returned HTTP response code: 500 (.*)").matcher("");

        public boolean recognize(Exception e) {
            return e instanceof IOException && messageMatcher.reset(e.getMessage()).matches();
        }

        public void handle(Exception e) {
            final String title = "Internal Server Error";

            // explain that this is not our fault
            final String message = "An error occurred within the java.net webserver.\n" +
                                   "Please retry your operation later.";

            // explain that this is java.net's fault
            SwingUtilities.invokeLater(new ShowMessageDialogRunnable(title, message));
        }
    }

    /**
     * A convenience class to show a message dialog to the user.
     */
    private class ShowMessageDialogRunnable implements Runnable {
        private final String title;
        private final String message;

        public ShowMessageDialogRunnable(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public void run() {
            JOptionPane.showMessageDialog(frame, message, title, JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Render the issues separator.
     */
    public class IssueSeparatorTableCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
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