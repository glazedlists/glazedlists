/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swing;

import java.io.*;
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
import ca.odell.glazedlists.demo.issuebrowser.*;
import ca.odell.glazedlists.demo.Launcher;
import ca.odell.glazedlists.swing.*;


/**
 * An IssueBrowser is a program for finding and viewing issues.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesBrowser extends Applet {

	/**
	 * this doesn't belong here at all
	 */
	private static final Color GLAZED_LISTS_ORANGE = new Color(255, 119, 0);

	/**
	 * an event list to host the issues
	 */
	private UniqueList issuesEventList = new UniqueList(new BasicEventList());

	/**
	 * the currently selected issues
	 */
	private EventSelectionModel issuesSelectionModel;

	/**
	 * an event list to host the descriptions
	 */
	private EventList descriptions = new BasicEventList();

	/**
	 * monitor loading the issues
	 */
	private JLabel throbber = null;
	private ImageIcon throbberActive = null;
	private ImageIcon throbberStatic = null;
	private JTextField issuesLoadingText = null;

	/**
	 * loads issues as requested
	 */
	private IssueLoader issueLoader = new IssueLoader();
	private Thread issueLoaderThread = new Thread(issueLoader);

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
		issueLoaderThread.start();
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
		SortedList issuesSortedList = new SortedList(issuesUserFiltered);
		TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);
		//ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");

		// issues table
		//EventTableModel issuesTableModel = new EventTableModel(priorityList, new IssueTableFormat());
		EventTableModel issuesTableModel = new EventTableModel(issuesTextFiltered, new IssueTableFormat());
		JTable issuesJTable = new JTable(issuesTableModel);
		issuesSelectionModel = new EventSelectionModel(issuesTextFiltered);
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
		/*BoundedRangeModel priorityRangeModel = ThresholdRangeModelFactory.createLower(priorityList);
		priorityRangeModel.setRangeProperties(0, 0, 0, 100, false);
		JSlider prioritySlider = new JSlider(priorityRangeModel);
		Hashtable prioritySliderLabels = new Hashtable();
		prioritySliderLabels.put(new Integer(0), new JLabel("Low"));
		prioritySliderLabels.put(new Integer(100), new JLabel("High"));
		prioritySlider.setLabelTable(prioritySliderLabels);
		prioritySlider.setSnapToTicks(true);
		prioritySlider.setPaintLabels(true);
		prioritySlider.setPaintTicks(true);
		prioritySlider.setMajorTickSpacing(25);*/

		// projects
		EventList projects = new BasicEventList();
		projects.add(new JavaNetProject("glazedlists", "Glazed Lists"));
		projects.add(new JavaNetProject("lg3d-core", "Project Looking Glass Core"));
		projects.add(new JavaNetProject("java-net", "Java.net Watercooler"));
		projects.add(new JavaNetProject("javacc", "Java Compiler Compiler"));
		projects.add(new JavaNetProject("sqlexplorer", "SQLExplorer Eclipse Database Plugin"));
		projects.add(new JavaNetProject("ofbiz", "Open For Business"));
		projects.add(new JavaNetProject("jogl", "JOGL Java OpenGL Bindings"));
		projects.add(new JavaNetProject("sip-communicator", "SIP Communicator"));
		projects.add(new JavaNetProject("jdic", "JavaDesktop Integration Components"));
		projects.add(new JavaNetProject("jdnc", "JavaDesktop Network Components"));

		// project select combobox
		EventComboBoxModel projectsComboModel = new EventComboBoxModel(projects);
		JComboBox projectsCombo = new JComboBox(projectsComboModel);
		projectsCombo.setEditable(false);
		projectsCombo.setBackground(GLAZED_LISTS_ORANGE);
		projectsCombo.addItemListener(new ProjectChangeListener());
		projectsComboModel.setSelectedItem(new JavaNetProject(null, "Select a Java.net project..."));

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
		//filtersPanel.add(new JLabel("Minimum Prioriy"),      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(5,  10, 5,   10), 0, 0));
		//filtersPanel.add(prioritySlider,                     new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,  10, 15,  10), 0, 0));
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
			if (issuesSelectionModel.getEventList().size() > 0) {
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
			JavaNetProject selected = (JavaNetProject) e.getItem();
			if (selected.isValid()) issueLoader.setProject((JavaNetProject) selected);
		}
	}

	/**
	 * When started via a main method, this creates a standalone issues browser.
	 */
	public static void main(String[] args) {
		if (args.length != 0) {
			System.out.println("Usage: IssueBrowser");
			return;
		}

		// load the issues and display the browser
		IssuesBrowser browser = new IssuesBrowser(false);
	}

	/**
	 * Models a project on Java.net.
	 */
	class JavaNetProject {

		private String projectName;
		private String projectTitle;

		public JavaNetProject(String projectName, String projectTitle) {
			this.projectName = projectName;
			this.projectTitle = projectTitle;
		}

		public boolean isValid() {
			return (projectName != null);
		}

		public String getXMLUri() {
			return "https://" + projectName + ".dev.java.net/issues/xml.cgi";
		}

		public String toString() {
			return projectTitle;
		}
	}


	/**
	 * This loads issues by project as they are requested. When a new project is
	 * requested, a working project may be interrupted. This may have violent side
	 * effects such as InterruptedExceptions printed to the console by certain
	 * XML parsing libararies that aren't exactly interruption friendly.
	 * <p/>
	 * <p>Issues are streamed to the issues list as they are loaded.
	 */
	class IssueLoader implements Runnable {
		private JavaNetProject project = null;

		public void setProject(JavaNetProject project) {
			synchronized(this) {
				this.project = project;
				issueLoaderThread.interrupt();
				notify();
			}
		}

		public void run() {
			// loop forever, loading projects
			JavaNetProject currentProject = null;
			while (true) {
				try {
					// get a project to load
					synchronized(this) {
						if (project == null) wait();
						Thread.interrupted();

						// we should still be asleep
						if (project == null) continue;

						// we have a project to load
						currentProject = project;
						project = null;
					}

					// start the progress bar
					SwingUtilities.invokeLater(new IndeterminateToggler(throbberActive, "Downloading issues..."));

					// load the issues
					EventList threadSafeIssuesEventList = new ThreadSafeList(issuesEventList);
					threadSafeIssuesEventList.clear();
					IssuezillaXMLParser.loadIssues(threadSafeIssuesEventList, currentProject.getXMLUri());

					// stop the progress bar
					SwingUtilities.invokeLater(new IndeterminateToggler(throbberStatic, ""));

					// handling interruptions is really gross
				} catch(IOException e) {
					if (e.getCause() instanceof InterruptedException) {
						// do nothing, we were just interrupted as expected
					} else if (e.getMessage().equals("Parsing failed java.lang.InterruptedException")) {
						// do nothing, we were just interrupted as expected
					} else {
						e.printStackTrace();
					}
				} catch(RuntimeException e) {
					if (e.getCause() instanceof InterruptedException) {
						// do nothing, we were just interrupted as expected
					} else if (e.getCause() instanceof IOException && e.getCause().getMessage().equals("Parsing failed Lock interrupted")) {
						// do nothing, we were just interrupted as expected
					} else {
						throw e;
					}
				} catch(InterruptedException e) {
					// do nothing, we were just interrupted as expected
				}
			}
		}

		private class IndeterminateToggler implements Runnable {
			private ImageIcon throbberIcon;
			private String message;

			public IndeterminateToggler(ImageIcon throbberIcon, String message) {
				this.throbberIcon = throbberIcon;
				this.message = message;
			}

			public void run() {
				throbber.setIcon(throbberIcon);
			}
		}
	}
}
