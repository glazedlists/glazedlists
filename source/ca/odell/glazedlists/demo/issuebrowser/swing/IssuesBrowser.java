/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swing;

// demo
import ca.odell.glazedlists.demo.issuebrowser.*;
import ca.odell.glazedlists.demo.Launcher;
// swing
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.URL;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;
// for setting up the bounded range model
import java.util.Hashtable;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesBrowser extends Applet {

	/** this doesn't belong here at all */
	private static final Color GLAZED_LISTS_ORANGE = new Color(255, 119, 0);

	/** an event list to host the issues */
	private UniqueList issuesEventList = new UniqueList(new BasicEventList());

	/** the currently selected issues */
	private EventSelectionModel issuesSelectionModel;

	/** an event list to host the descriptions */
	private EventList descriptions = new BasicEventList();

	/** monitor loading the issues */
	private JLabel throbber = null;
	private ImageIcon throbberActive = null;
	private ImageIcon throbberStatic = null;
	private JTextField issuesLoadingText = null;

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

		frame.setSize(640, 480);
		frame.getContentPane().setLayout(new GridBagLayout());
		frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		frame.show();
	}

	/**
	 * Display a frame for browsing issues.
	 */
	private JPanel constructView() {
		// create the lists
		IssuesUserFilter issuesUserFiltered = new IssuesUserFilter(issuesEventList);
		TextFilterList issuesTextFiltered = new TextFilterList(issuesUserFiltered);
		ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");
		SortedList issuesSortedList = new SortedList(priorityList);

		// issues table
	    EventTableModel issuesTableModel = new EventTableModel(issuesSortedList, new IssueTableFormat());
		JTable issuesJTable = new JTable(issuesTableModel);
		issuesSelectionModel = new EventSelectionModel(issuesSortedList);
		issuesSelectionModel.addListSelectionListener(new IssuesSelectionListener());
		issuesJTable.setSelectionModel(issuesSelectionModel);
		issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
		issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
		issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
		issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
		issuesJTable.setDefaultRenderer(Priority.class, new PriorityTableCellRenderer());
		TableComparatorChooser tableSorter = new TableComparatorChooser(issuesJTable, issuesSortedList, true);
		JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// users table
		JScrollPane usersListScrollPane = new JScrollPane(issuesUserFiltered.getUserSelect(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// descriptions
		EventTableModel descriptionsTableModel = new EventTableModel(descriptions, new DescriptionTableFormat());
		JTable descriptionsTable = new JTable(descriptionsTableModel);
		descriptionsTable.getColumnModel().getColumn(0).setCellRenderer(new DescriptionRenderer());
		JScrollPane descriptionsTableScrollPane = new JScrollPane(descriptionsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		// priority slider
		BoundedRangeModel priorityRangeModel = ThresholdRangeModelFactory.createLower(priorityList);
		priorityRangeModel.setRangeProperties(0, 0, 0, 100, false);
		JSlider prioritySlider = new JSlider(priorityRangeModel);
		Hashtable prioritySliderLabels = new Hashtable();
		prioritySliderLabels.put(new Integer(0), new JLabel("Low"));
		prioritySliderLabels.put(new Integer(100), new JLabel("High"));
		prioritySlider.setLabelTable(prioritySliderLabels);
		prioritySlider.setSnapToTicks(true);
		prioritySlider.setPaintLabels(true);
		prioritySlider.setPaintTicks(true);
		prioritySlider.setMajorTickSpacing(25);

		// projects
        EventList projects = Project.getProjects();

		// project select combobox
		EventComboBoxModel projectsComboModel = new EventComboBoxModel(projects);
		JComboBox projectsCombo = new JComboBox(projectsComboModel);
		projectsCombo.setEditable(false);
		projectsCombo.setBackground(GLAZED_LISTS_ORANGE);
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
		iconBar.add(projectsCombo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		iconBar.add(throbber, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		// create the filters panel
		JPanel filtersPanel = new JPanel();
		filtersPanel.setLayout(new GridBagLayout());
		filtersPanel.setBorder(BorderFactory.createLineBorder(Color.white));
		filtersPanel.add(new JLabel("Text Filter"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 5, 10), 0, 0));
		filtersPanel.add(issuesTextFiltered.getFilterEdit(), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 15, 10), 0, 0));
		filtersPanel.add(new JLabel("Minimum Prioriy"),      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(5,  10, 5,   10), 0, 0));
		filtersPanel.add(prioritySlider,                     new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,  10, 15,  10), 0, 0));
		filtersPanel.add(new JLabel("Issue Owner"), new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 10), 0, 0));
		filtersPanel.add(usersListScrollPane, new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 10, 10, 10), 0, 0));

		// a panel with a table
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.add(iconBar, new GridBagConstraints(0, 0, 2, 1, 1.00, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
		panel.add(filtersPanel, new GridBagConstraints(0, 1, 1, 2, 0.15, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(issuesTableScrollPane, new GridBagConstraints(1, 1, 1, 1, 0.85, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		panel.add(descriptionsTableScrollPane, new GridBagConstraints(1, 2, 1, 1, 0.85, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		return panel;
	}

	/**
	 * Listens for changes in the selection on the issues table.
	 */
	class IssuesSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			descriptions.clear();
			if(issuesSelectionModel.getEventList().size() > 0) {
				Issue selectedIssue = (Issue) issuesSelectionModel.getEventList().get(0);
				descriptions.addAll(selectedIssue.getDescriptions());
			}
		}
	}

	/**
	 * Listens for changes to the project combo box and updates which project is
	 * being loaded.
	 */
	class ProjectChangeListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			Project selected = (Project) e.getItem();
			if (selected.isValid()) issueLoader.setProject((Project) selected);
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

        /** whether the throbber will be turned on and off */
        private boolean on = false;
        
        public synchronized void setOn() {
            on = true;
            SwingUtilities.invokeLater(this);
        }
        
        public synchronized void setOff() {
            on = false;
            SwingUtilities.invokeLater(this);
        }

        public synchronized void run() {
            if(on) throbber.setIcon(throbberActive);
            else throbber.setIcon(throbberStatic);
        }
    }
}
