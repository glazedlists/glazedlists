/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// swt
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
// demo
import ca.odell.glazedlists.demo.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;


/**
 * This is the SWT version of the demo application designed to look good on a
 * Windows machine.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IssuesBrowser {

    /** an event list to host the issues */
    private IssuesList issuesEventList = new IssuesList();

    /** To get access to the unique list of users */
    private IssuesUserFilter issuesUserFiltered = null;
    private List usersList = null;

    /**
     * Constructs a new IssuesBrowser in the given window
     */
    private IssuesBrowser(Shell shell) {

        // Various Layered List Transformations
        issuesUserFiltered = new IssuesUserFilter(issuesEventList);
        SortedList issuesSortedList = new SortedList(issuesUserFiltered);
        TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);

        // Users List
        createUsersList(shell);

        // Filter Label
        createFilterLabel(shell);

        // Filter Text
        Text filterText = createFilterText(shell);
        issuesTextFiltered.setFilterEdit(filterText);

        // Issues Table
        Table issuesTable = createIssuesTable(shell);
        EventTableViewer issuesTableViewer = new EventTableViewer(issuesTextFiltered, issuesTable, new IssueTableFormat());
        issuesTable = formatIssuesTable(issuesTable);
        new TableComparatorChooser(issuesTableViewer, issuesSortedList, false);

        // Descriptions Header
        createDescriptionsHeader(shell);

        // Descriptions Table
        createDescriptionsTable(shell, issuesTableViewer);

        // Status Bar
        createStatusBar(shell);

        // Start the demo
        issuesEventList.start();

    }

    private void createUsersList(Shell shell) {
        usersList = new List(shell, SWT.MULTI | SWT.BORDER);
        GridData usersListLayout = new GridData();
        usersListLayout.horizontalSpan = 1;
        usersListLayout.verticalSpan = 2;
        usersListLayout.horizontalAlignment = GridData.FILL;
        usersListLayout.verticalAlignment = GridData.FILL;
        usersListLayout.grabExcessHorizontalSpace = true;
        usersListLayout.grabExcessVerticalSpace = true;
        usersList.setLayoutData(usersListLayout);
        issuesUserFiltered.setList(usersList);
        issuesUserFiltered.addUserListListener(new UserInterfaceThreadProxy(new UserListListener(), shell.getDisplay()));
    }

    private void createFilterLabel(Shell shell) {
        Label filterLabel = new Label(shell, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        filterLabel.setText("Filter: ");
        GridData filterLabelLayout = new GridData();
        filterLabelLayout.horizontalSpan = 1;
        filterLabelLayout.verticalSpan = 1;
        filterLabelLayout.horizontalAlignment = GridData.BEGINNING;
        filterLabelLayout.verticalAlignment = GridData.CENTER;
        filterLabel.setLayoutData(filterLabelLayout);
    }

    private Text createFilterText(Shell shell) {
        Text filterText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        GridData filterTextLayout = new GridData();
        filterTextLayout.horizontalSpan = 1;
        filterTextLayout.verticalSpan = 1;
        filterTextLayout.horizontalAlignment = GridData.FILL;
        filterTextLayout.verticalAlignment = GridData.BEGINNING;
        filterTextLayout.grabExcessHorizontalSpace = true;
        filterText.setLayoutData(filterTextLayout);
        return filterText;
    }

    private Table createIssuesTable(Shell shell) {
        Table issuesTable = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
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

    private void createDescriptionsHeader(Shell shell) {
        Label descriptionsLabel = new Label(shell, SWT.HORIZONTAL | SWT.CENTER | SWT.SHADOW_NONE);
        descriptionsLabel.setText("Description");
        GridData descriptionsLabelLayout = new GridData();
        descriptionsLabelLayout.horizontalSpan = 3;
        descriptionsLabelLayout.verticalSpan = 1;
        descriptionsLabelLayout.horizontalAlignment = GridData.FILL;
        descriptionsLabelLayout.verticalAlignment = GridData.CENTER;
        descriptionsLabelLayout.grabExcessHorizontalSpace = true;
        descriptionsLabel.setLayoutData(descriptionsLabelLayout);
    }

    private void createDescriptionsTable(Shell shell, EventTableViewer issuesTableViewer) {
        Table descriptionsTable = new Table(shell, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
        GridData descriptionsTableLayout = new GridData();
        descriptionsTableLayout.horizontalSpan = 3;
        descriptionsTableLayout.verticalSpan = 1;
        descriptionsTableLayout.horizontalAlignment = GridData.FILL;
        descriptionsTableLayout.verticalAlignment = GridData.FILL;
        descriptionsTableLayout.grabExcessHorizontalSpace = true;
        descriptionsTableLayout.grabExcessVerticalSpace = true;
        descriptionsTableLayout.heightHint = 30;
        descriptionsTable.setLayoutData(descriptionsTableLayout);
        descriptionsTable.showColumn(new TableColumn(descriptionsTable, SWT.LEFT));
        descriptionsTable.getColumn(0).setWidth(618);
        descriptionsTable.getVerticalBar().setEnabled(true);
        descriptionsTable.setHeaderVisible(false);
        issuesTableViewer.getTable().addSelectionListener(new IssueSelectionListener(issuesTableViewer, descriptionsTable));

    }

    private void createStatusBar(Shell shell) {
        Text statusText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.READ_ONLY | SWT.BORDER);
        GridData statusTextLayout = new GridData();
        statusTextLayout.horizontalSpan = 3;
        statusTextLayout.verticalSpan = 1;
        statusTextLayout.horizontalAlignment = GridData.FILL;
        statusTextLayout.verticalAlignment = GridData.END;
        statusTextLayout.grabExcessHorizontalSpace = true;
        statusText.setLayoutData(statusTextLayout);
        statusText.setText("Java Application Window");
    }

    /**
     * A simple listener class to listen to events on the unique view of the
     * users lists.  Totally not optimized, but I just want it to work.
     */
    class UserListListener implements ListEventListener {

        public void listChanged(ListEvent listChanges) {
            EventList list = issuesUserFiltered.getUsersList();
            usersList.removeAll();
            for(int i = 0; i < list.size(); i++) {
                usersList.add((String)list.get(i));
            }
        }
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
            if(selectionIndex == oldSelectionIndex) return;

            // There was a change in selection so respond to it
            oldSelectionIndex = selectionIndex;
            descriptionsTable.removeAll();
            if(selectionIndex == -1) return;

            // There is a new issue selected so display its description
            Issue selected = (Issue)issuesTableViewer.getSourceList().get(selectionIndex);
            java.util.List descriptions = selected.getDescriptions();
            for(int i = 0; i < descriptions.size(); i ++) {
                int rowOffset = descriptionsTable.getItemCount();
                formatDescription((Description)descriptions.get(i), rowOffset);
            }
        }

        private void formatDescription(Description description, int rowOffset) {
            // Print the user name
            TableItem userName = new TableItem(descriptionsTable, SWT.LEFT, rowOffset);
            FontData[] userNameFontData = userName.getFont().getFontData();
            userNameFontData[0].setStyle(SWT.BOLD);
            Font userNameFont = new Font(descriptionsTable.getDisplay(), userNameFontData);
            userName.setFont(userNameFont);
            userName.setText(description.getWho() + ":");
            rowOffset++;

            // Print the details on several lines if necessary
            String[] detailLines = description.getText().split("\n");
            for(int i = 0; i < detailLines.length; i++) {
                TableItem details = new TableItem(descriptionsTable, SWT.LEFT, rowOffset);
                details.setText(detailLines[i]);
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
        System.out.println("Library Path: " +  System.getProperty("java.library.path"));

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Issues");
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        shell.setLayout(gridLayout);

        new IssuesBrowser(shell);
        shell.setSize(640, 480);
        //shell.pack();
        shell.open();

        while(!shell.isDisposed()) {
            if(!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

}

/**
 * The IssueTableFormat specifies how an issue is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class IssueTableFormat implements CheckableTableFormat {

    public int getColumnCount() {
        return 6;
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "ID";
        } else if(column == 1) {
            return "Type";
        } else if(column == 2) {
            return "Priority";
        } else if(column == 3) {
            return "State";
        } else if(column == 4) {
            return "Result";
        } else if(column == 5) {
            return "Summary";
        }
        return null;
    }

    public Object getColumnValue(Object baseObject, int column) {
        if(baseObject == null) return null;
        Issue issue = (Issue)baseObject;
        if(column == 0) {
            return issue.getId();
        } else if(column == 1) {
            return issue.getIssueType();
        } else if(column == 2) {
            return issue.getPriority();
        } else if(column == 3) {
            return issue.getStatus();
        } else if(column == 4) {
            return issue.getResolution();
        } else if(column == 5) {
            return issue.getShortDescription();
        }
        return null;
    }

    /**
     * Sets the specified object as checked.
     */
    public void setChecked(Object baseObject, boolean checked) {
        // Do Nothing
    }

    /**
     * Gets whether the specified object is checked.
     */
    public boolean getChecked(Object baseObject) {
        Issue issue = (Issue)baseObject;
        return issue.getPriority().equals("P3");
    }
}

/**
 * The DescriptionsTableFormat specifies how an Issue description is displayed in a table.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
class DescriptionsTableFormat implements CheckableTableFormat {

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        if(column == 0) {
            return "Description";
        }
        return null;
    }

    public Object getColumnValue(Object baseObject, int column) {
        if(baseObject == null) return null;
        Issue issue = (Issue)baseObject;
        if(column == 0) {
            return issue.getId();
        }
        return null;
    }

    /**
     * Sets the specified object as checked.
     */
    public void setChecked(Object baseObject, boolean checked) {
        // Do nothing
    }

    /**
     * Gets whether the specified object is checked.
     */
    public boolean getChecked(Object baseObject) {
        return true;
    }
}