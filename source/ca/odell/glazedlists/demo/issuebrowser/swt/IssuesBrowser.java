/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swt;

// demo
import ca.odell.glazedlists.demo.issuebrowser.*;
// swt
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.swt.*;


/**
 * This is the SWT version of the demo application designed to look good on a
 * Windows machine.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IssuesBrowser {

	/** an event list to host the issues */
	private UniqueList issuesEventList = new UniqueList(new BasicEventList());

	/** To get access to the unique list of users */
	private IssuesUserFilter issuesUserFiltered = null;
	private List usersList = null;
    
    /** status bar is a temporary throbber */
    private Text statusText = null;

	/** loads issues as requested */
	private IssueLoader issueLoader = new IssueLoader(issuesEventList, new IndeterminateToggler());

	/**
	 * Constructs a new IssuesBrowser in the given window
	 */
	private IssuesBrowser(Shell shell) {
        
		// Various Layered List Transformations
		issuesUserFiltered = new IssuesUserFilter(issuesEventList);
		SortedList issuesSortedList = new SortedList(issuesUserFiltered);
		TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);

		// A SashFrom to layout the whole demo
		SashForm demoForm = new SashForm(shell, SWT.VERTICAL);

		// Set the layout of the form
		GridData demoFormLayout = new GridData();
		demoFormLayout.horizontalAlignment = GridData.FILL;
		demoFormLayout.verticalAlignment = GridData.FILL;
		demoFormLayout.grabExcessHorizontalSpace = true;
		demoFormLayout.grabExcessVerticalSpace = true;
		demoForm.setLayoutData(demoFormLayout);

		// Set the layout for the contents fo the form
		GridLayout demoFormContentLayout = new GridLayout(1, false);
		demoFormContentLayout.marginHeight = 0;
		demoFormContentLayout.marginWidth = 0;
		demoForm.setLayout(demoFormContentLayout);

		// Layout the top half of the demo
		SashForm topSash = new SashForm(demoForm, SWT.HORIZONTAL);

		// Set the layout of the sash form
		GridData topSashLayout = new GridData();
		topSashLayout.horizontalAlignment = GridData.FILL;
		topSashLayout.verticalAlignment = GridData.FILL;
		topSashLayout.grabExcessHorizontalSpace = true;
		topSashLayout.grabExcessVerticalSpace = true;
		topSash.setLayoutData(topSashLayout);

		// Set the layout for the contents of the form
		GridLayout topSashContentLayout = new GridLayout(2, false);
		topSashContentLayout.marginHeight = 0;
		topSashContentLayout.marginWidth = 0;
		topSash.setLayout(topSashContentLayout);

		createUsersList(topSash);
		Canvas topForm = createTopForm(topSash);
		topSash.setWeights(new int[]{25, 75});
		createFilterLabel(topForm);
		Text filterText = createFilterText(topForm);
		issuesTextFiltered.setFilterEdit(filterText);
		Table issuesTable = createIssuesTable(topForm);
		EventTableViewer issuesTableViewer = new EventTableViewer(issuesTextFiltered, issuesTable, new IssueTableFormat());
		issuesTable = formatIssuesTable(issuesTable);
		new TableComparatorChooser(issuesTableViewer, issuesSortedList, false);

		// Layout the bottom half of the demo
		Canvas bottomForm = createBottomForm(demoForm);
		createDescriptionsHeader(bottomForm);
		createDescriptionsTable(bottomForm, issuesTableViewer);
		createStatusBar(bottomForm);

		// Apply weight to the demo form
		demoForm.setWeights(new int[]{50, 50});

		// Start the demo
		issueLoader.start();
        issueLoader.setProject((Project)Project.getProjects().get(0));

	}

	private Canvas createTopForm(Composite parent) {
		Canvas topForm = new Canvas(parent, 0);

		// Set the layout of the form
		GridData topFormLayout = new GridData();
		topFormLayout.horizontalAlignment = GridData.FILL;
		topFormLayout.verticalAlignment = GridData.FILL;
		topFormLayout.grabExcessHorizontalSpace = true;
		topFormLayout.grabExcessVerticalSpace = true;
		topForm.setLayoutData(topFormLayout);

		// Set the layout for the contents of the form
		GridLayout topFormContentLayout = new GridLayout(2, false);
		topFormContentLayout.marginHeight = 0;
		topFormContentLayout.marginWidth = 0;
		topForm.setLayout(topFormContentLayout);

		return topForm;
	}

	private Canvas createBottomForm(Composite parent) {
		Canvas bottomForm = new Canvas(parent, 0);

		// Set the layout of the form
		GridData bottomFormLayout = new GridData();
		bottomFormLayout.horizontalAlignment = GridData.FILL;
		bottomFormLayout.verticalAlignment = GridData.FILL;
		bottomFormLayout.grabExcessHorizontalSpace = true;
		bottomFormLayout.grabExcessVerticalSpace = true;
		bottomForm.setLayoutData(bottomFormLayout);

		// Set the layout for the contents of the form
		GridLayout bottomFormContentLayout = new GridLayout(1, false);
		bottomFormContentLayout.marginHeight = 0;
		bottomFormContentLayout.marginWidth = 0;
		bottomForm.setLayout(bottomFormContentLayout);

		return bottomForm;
	}

	private void createUsersList(Composite parent) {
		usersList = new List(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
		GridData usersListLayout = new GridData();
		usersListLayout.horizontalSpan = 1;
		usersListLayout.verticalSpan = 2;
		usersListLayout.horizontalAlignment = GridData.FILL;
		usersListLayout.verticalAlignment = GridData.FILL;
		usersListLayout.grabExcessHorizontalSpace = true;
		usersListLayout.grabExcessVerticalSpace = true;
		usersList.setLayoutData(usersListLayout);
		issuesUserFiltered.setList(usersList);

		EventListViewer listViewer = new EventListViewer(issuesUserFiltered.getUsersList(), usersList);
	}

	private void createFilterLabel(Composite parent) {
		Label filterLabel = new Label(parent, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
		filterLabel.setText("Filter: ");
		GridData filterLabelLayout = new GridData();
		filterLabelLayout.horizontalAlignment = GridData.BEGINNING;
		filterLabelLayout.verticalAlignment = GridData.CENTER;
		filterLabel.setLayoutData(filterLabelLayout);
	}

	private Text createFilterText(Composite parent) {
		Text filterText = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		GridData filterTextLayout = new GridData();
		filterTextLayout.horizontalAlignment = GridData.FILL;
		filterTextLayout.verticalAlignment = GridData.BEGINNING;
		filterTextLayout.grabExcessHorizontalSpace = true;
		filterTextLayout.grabExcessVerticalSpace = false;
		filterText.setLayoutData(filterTextLayout);
		return filterText;
	}

	private Table createIssuesTable(Composite parent) {
		Table issuesTable = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
		GridData issuesTableLayout = new GridData();
		issuesTableLayout.horizontalSpan = 2;
		issuesTableLayout.verticalSpan = 1;
		issuesTableLayout.horizontalAlignment = GridData.FILL;
		issuesTableLayout.verticalAlignment = GridData.FILL;
		issuesTableLayout.grabExcessHorizontalSpace = true;
		issuesTableLayout.grabExcessVerticalSpace = true;
		issuesTable.setLayoutData(issuesTableLayout);
		return issuesTable;
	}

	private Table formatIssuesTable(Table issuesTable) {
		issuesTable.getVerticalBar().setEnabled(true);
		issuesTable.getColumn(0).setWidth(30);
		issuesTable.getColumn(0).setResizable(false);
		issuesTable.getColumn(1).setWidth(50);
		issuesTable.getColumn(2).setWidth(46);
		issuesTable.getColumn(2).setResizable(false);
		issuesTable.getColumn(3).setWidth(50);
		issuesTable.getColumn(4).setWidth(60);
		issuesTable.getColumn(5).setWidth(250);
		return issuesTable;
	}

	private void createDescriptionsHeader(Composite parent) {
		Label descriptionsLabel = new Label(parent, SWT.HORIZONTAL | SWT.CENTER | SWT.SHADOW_NONE);
		descriptionsLabel.setText("Description");
		GridData descriptionsLabelLayout = new GridData();
		descriptionsLabelLayout.horizontalAlignment = GridData.FILL;
		descriptionsLabelLayout.verticalAlignment = GridData.BEGINNING;
		descriptionsLabelLayout.grabExcessHorizontalSpace = true;
		descriptionsLabelLayout.grabExcessVerticalSpace = false;
		descriptionsLabel.setLayoutData(descriptionsLabelLayout);
	}

	private void createDescriptionsTable(Composite parent,
		EventTableViewer issuesTableViewer) {
		Table descriptionsTable = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
		GridData descriptionsTableLayout = new GridData();
		descriptionsTableLayout.horizontalAlignment = GridData.FILL;
		descriptionsTableLayout.verticalAlignment = GridData.FILL;
		descriptionsTableLayout.grabExcessHorizontalSpace = true;
		descriptionsTableLayout.grabExcessVerticalSpace = true;
		descriptionsTable.setLayoutData(descriptionsTableLayout);
		descriptionsTable.showColumn(new TableColumn(descriptionsTable, SWT.LEFT));
		descriptionsTable.getColumn(0).setWidth(618);
		descriptionsTable.setHeaderVisible(false);
		issuesTableViewer.getTable().addSelectionListener(new IssueSelectionListener(issuesTableViewer, descriptionsTable));

	}

	private void createStatusBar(Composite parent) {
		statusText = new Text(parent, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER);
		GridData statusTextLayout = new GridData();
		statusTextLayout.horizontalAlignment = GridData.FILL;
		statusTextLayout.verticalAlignment = GridData.END;
		statusTextLayout.grabExcessHorizontalSpace = true;
		statusTextLayout.grabExcessVerticalSpace = false;
		statusText.setLayoutData(statusTextLayout);
		statusText.setText("Java Application Window");
	}

	/**
	 * A listener to selection on the issues table that reflects the change
	 * in selection within the description table.
	 */
	class IssueSelectionListener implements SelectionListener {

		private EventTableViewer issuesTableViewer = null;
		private Table descriptionsTable = null;
		int oldSelectionIndex = 0;

		IssueSelectionListener(EventTableViewer issuesTableViewer, Table descriptionsTable) {
			this.issuesTableViewer = issuesTableViewer;
			this.descriptionsTable = descriptionsTable;
		}

		public void widgetSelected(SelectionEvent e) {
			respondToSelection();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			respondToSelection();
		}

		private void respondToSelection() {
			int selectionIndex = issuesTableViewer.getTable().getSelectionIndex();
			if (selectionIndex == oldSelectionIndex) return;

			// There was a change in selection so respond to it
			oldSelectionIndex = selectionIndex;
			descriptionsTable.removeAll();
			if (selectionIndex == -1) return;

			// There is a new issue selected so display its description
			Issue selected = (Issue) issuesTableViewer.getSourceList().get(selectionIndex);
			java.util.List descriptions = selected.getDescriptions();
			for (int i = 0; i < descriptions.size(); i++) {
				int rowOffset = descriptionsTable.getItemCount();
				formatDescription((Description) descriptions.get(i), rowOffset);
			}
		}

		private void formatDescription(Description description, int rowOffset) {
			// Print the user name
			TableItem userName = new TableItem(descriptionsTable, SWT.LEFT, rowOffset);
			FontData[] userNameFontData = userName.getFont().getFontData();
			userNameFontData[ 0 ].setStyle(SWT.BOLD);
			Font userNameFont = new Font(descriptionsTable.getDisplay(), userNameFontData);
			userName.setFont(userNameFont);
			userName.setText(description.getWho() + ":");
			rowOffset++;

			// Print the details on several lines if necessary
			String[] detailLines = description.getText().split("\n");
			for (int i = 0; i < detailLines.length; i++) {
				TableItem details = new TableItem(descriptionsTable, SWT.LEFT, rowOffset);
				details.setText(detailLines[ i ]);
				rowOffset++;
			}

			// Print a seperator
			TableItem seperator = new TableItem(descriptionsTable, SWT.LEFT, rowOffset);
			seperator.setText("___________________________________________________________________");
		}
	}

	/**
	 *
	 */
	public static void main(String[] args) {
		System.setProperty("java.library.path", ".");

		Display display = new Display();
		Shell parent = new Shell(display);
		parent.setText("Issues");
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		parent.setLayout(gridLayout);

		new IssuesBrowser(parent);
		parent.setSize(640, 480);
		parent.open();

		while (!parent.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
		System.exit(0);
	}


    /**
     * Toggles the throbber on and off.
     */
    private class IndeterminateToggler implements Runnable, Throbber {

        /** whether the throbber will be turned on and off */
        private boolean on = false;
        
        public synchronized void setOn() {
            on = true;
            System.out.println("THROB ON");
        }
        
        public synchronized void setOff() {
            on = false;
            System.out.println("THROB OFF");
        }

        public synchronized void run() {
            //if(on) throbber.setIcon(throbberActive);
            //else throbber.setIcon(throbberStatic);
        }
    }
}