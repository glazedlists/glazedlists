/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.swt.*;
import ca.odell.glazedlists.matchers.*;


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
        FilterList issuesTextFiltered = new FilterList(issuesUserFiltered);
        ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");
        SortedList issuesSortedList = new SortedList(priorityList);

        // This is the outer component for the demo
        SashForm demoForm = new SashForm(shell, SWT.HORIZONTAL);

        // Set the layout of the sash form
        GridData demoFormLayout = new GridData();
        demoFormLayout.horizontalAlignment = GridData.FILL;
        demoFormLayout.verticalAlignment = GridData.FILL;
        demoFormLayout.grabExcessHorizontalSpace = true;
        demoFormLayout.grabExcessVerticalSpace = true;
        demoForm.setLayoutData(demoFormLayout);

        // Set the layout for the contents of the form
        GridLayout demoFormContentLayout = new GridLayout(2, false);
        demoFormContentLayout.marginHeight = 0;
        demoFormContentLayout.marginWidth = 0;
        demoForm.setLayout(demoFormContentLayout);

        // A panel containing all of the filters
        Canvas filterPanel = new Canvas(demoForm, SWT.BORDER);

        // Set the layout for the panel containing all of the filters
        GridData filterPanelLayout = new GridData();
        filterPanelLayout.horizontalAlignment = GridData.FILL;
        filterPanelLayout.verticalAlignment = GridData.FILL;
        filterPanelLayout.grabExcessHorizontalSpace = true;
        filterPanelLayout.grabExcessVerticalSpace = true;
        filterPanel.setLayoutData(filterPanelLayout);

        // Set the layout for the contents of that panel
        GridLayout filterPanelContentLayout = new GridLayout(1, false);
        filterPanelContentLayout.marginHeight = 10;
        filterPanelContentLayout.marginWidth = 10;
        filterPanelContentLayout.verticalSpacing = 15;
        filterPanel.setLayout(filterPanelContentLayout);

        // Add the various filters
        Text filterText = createFilterText(filterPanel);
        issuesTextFiltered.setMatcherEditor(new ThreadedMatcherEditor(new TextComponentMatcherEditor(filterText, null)));
        createPrioritySlider(filterPanel, priorityList);
        createUsersList(shell, filterPanel);

        // A panel containing the two tables to display Issue data
        SashForm issuePanel = new SashForm(demoForm, SWT.VERTICAL);

        // Set the layout for the panel containing the issue data
        GridData issuePanelLayout = new GridData();
        issuePanelLayout.horizontalAlignment = GridData.FILL;
        issuePanelLayout.verticalAlignment = GridData.FILL;
        issuePanelLayout.grabExcessHorizontalSpace = true;
        issuePanelLayout.grabExcessVerticalSpace = true;
        issuePanel.setLayoutData(issuePanelLayout);

        // Set the layout for the contents of that panel
        GridLayout issuePanelContentLayout = new GridLayout(1, false);
        issuePanelContentLayout.marginHeight = 0;
        issuePanelContentLayout.marginWidth = 0;
        issuePanel.setLayout(issuePanelContentLayout);

        // Create the Issues Table
        Table issuesTable = createIssuesTable(issuePanel);
        EventTableViewer issuesTableViewer = new EventTableViewer(issuesSortedList, issuesTable, new IssueTableFormat());
        issuesTable = formatIssuesTable(issuesTable);
        new TableComparatorChooser(issuesTableViewer, issuesSortedList, false);

        // Create the Descriptions Table
        createDescriptionsTable(issuePanel, issuesTableViewer);

        // balance the issue table and the descriptions table
        issuePanel.setWeights(new int[] {50,50});

        // balance the filter panel and the issue panel
        demoForm.setWeights(new int[]{30, 70});

        // Start the demo
        issueLoader.start();
        issueLoader.setProject((Project)Project.getProjects().get(0));
    }

    private Text createFilterText(Composite parent) {
        // A panel containing text filter
        Canvas textPanel = new Canvas(parent, SWT.NONE);

        // Set the layout for the panel containing the text filter
        GridData textPanelLayout = new GridData();
        textPanelLayout.horizontalAlignment = GridData.FILL;
        textPanelLayout.verticalAlignment = GridData.BEGINNING;
        textPanelLayout.grabExcessHorizontalSpace = true;
        textPanelLayout.grabExcessVerticalSpace = false;
        textPanel.setLayoutData(textPanelLayout);

        // Set the layout for the contents of that panel
        GridLayout textPanelContentLayout = new GridLayout(1, false);
        textPanelContentLayout.marginHeight = 0;
        textPanelContentLayout.marginWidth = 0;
        textPanelContentLayout.verticalSpacing = 5;
        textPanel.setLayout(textPanelContentLayout);

        // Create the Label first
        Label filterLabel = new Label(textPanel, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        filterLabel.setText("Text Filter");

        // Change the font
        FontData[] fontData = filterLabel.getFont().getFontData();
        fontData[0].setStyle(SWT.BOLD);
        //fontData[0].setHeight(fontData[0].getHeight() + 1);
        filterLabel.setFont(new Font(textPanel.getDisplay(), fontData));

        // Set the layout for the label
        GridData filterLabelLayout = new GridData();
        filterLabelLayout.horizontalAlignment = GridData.BEGINNING;
        filterLabelLayout.verticalAlignment = GridData.CENTER;
        filterLabelLayout.horizontalSpan = 2;
        filterLabel.setLayoutData(filterLabelLayout);

        // Create the actual text box to user for filtering
        Text filterText = new Text(textPanel, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        GridData filterTextLayout = new GridData();
        filterTextLayout.horizontalAlignment = GridData.FILL;
        filterTextLayout.verticalAlignment = GridData.BEGINNING;
        filterTextLayout.horizontalSpan = 2;
        filterTextLayout.grabExcessHorizontalSpace = true;
        filterTextLayout.grabExcessVerticalSpace = false;
        filterText.setLayoutData(filterTextLayout);
        return filterText;
    }

    private void createPrioritySlider(Composite parent, ThresholdList priorityList) {
        // A panel containing the priority slider
        Canvas priorityPanel = new Canvas(parent, SWT.NONE);

        // Set the layout for the panel containing the priority slider
        GridData priorityPanelLayout = new GridData();
        priorityPanelLayout.horizontalAlignment = GridData.FILL;
        priorityPanelLayout.verticalAlignment = GridData.BEGINNING;
        priorityPanelLayout.grabExcessHorizontalSpace = true;
        priorityPanelLayout.grabExcessVerticalSpace = false;
        priorityPanel.setLayoutData(priorityPanelLayout);

        // Set the layout for the contents of that panel
        GridLayout priorityPanelContentLayout = new GridLayout(2, false);
        priorityPanelContentLayout.marginHeight = 0;
        priorityPanelContentLayout.marginWidth = 0;
        priorityPanelContentLayout.verticalSpacing = 5;
        priorityPanel.setLayout(priorityPanelContentLayout);

        // Create the Label first
        Label priorityLabel = new Label(priorityPanel, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        priorityLabel.setText("Minimum Priority");

        // Change the font
        FontData[] fontData = priorityLabel.getFont().getFontData();
        fontData[0].setStyle(SWT.BOLD);
        //fontData[0].setHeight(fontData[0].getHeight() + 1);
        priorityLabel.setFont(new Font(priorityPanel.getDisplay(), fontData));

        // Set the layout for the label
        GridData priorityLabelLayout = new GridData();
        priorityLabelLayout.horizontalAlignment = GridData.BEGINNING;
        priorityLabelLayout.verticalAlignment = GridData.CENTER;
        priorityLabelLayout.horizontalSpan = 2;
        priorityLabel.setLayoutData(priorityLabelLayout);

        // Create the slider widget to control priority filtering
        Slider prioritySlider = new Slider(priorityPanel, SWT.HORIZONTAL);
        prioritySlider.setValues(0, 0, 100, 10, 1, 25);
        GlazedListsSWT.lowerThresholdViewer(priorityList, prioritySlider);
        GridData prioritySliderLayout = new GridData();
        prioritySliderLayout.horizontalAlignment = GridData.FILL;
        prioritySliderLayout.verticalAlignment = GridData.BEGINNING;
        prioritySliderLayout.horizontalSpan = 2;
        prioritySliderLayout.grabExcessHorizontalSpace = true;
        prioritySliderLayout.grabExcessVerticalSpace = false;
        prioritySlider.setLayoutData(prioritySliderLayout);

        // Create the lower end Label
        Label lowPriorityLabel = new Label(priorityPanel, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        lowPriorityLabel.setText("Low");

        // Change the font to bold
        FontData[] lowerEndFontData = lowPriorityLabel.getFont().getFontData();
        lowerEndFontData[0].setStyle(SWT.BOLD);
        lowPriorityLabel.setFont(new Font(priorityPanel.getDisplay(), lowerEndFontData));

        // Set the layout for the label
        GridData lowPriorityLabelLayout = new GridData();
        lowPriorityLabelLayout.horizontalAlignment = GridData.BEGINNING;
        lowPriorityLabelLayout.verticalAlignment = GridData.CENTER;
        lowPriorityLabel.setLayoutData(lowPriorityLabelLayout);

        // Create the higher end Label
        Label highPriorityLabel = new Label(priorityPanel, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        highPriorityLabel.setText("High");

        // Change the font to bold
        FontData[] higherEndFontData = highPriorityLabel.getFont().getFontData();
        higherEndFontData[0].setStyle(SWT.BOLD);
        highPriorityLabel.setFont(new Font(priorityPanel.getDisplay(), higherEndFontData));

        // Set the layout for the label
        GridData highPriorityLabelLayout = new GridData();
        highPriorityLabelLayout.horizontalAlignment = GridData.END;
        highPriorityLabelLayout.verticalAlignment = GridData.CENTER;
        highPriorityLabel.setLayoutData(highPriorityLabelLayout);
    }

    private void createUsersList(Shell shell, Composite parent) {
        // A panel containing tthe users list
        Canvas usersListPanel = new Canvas(parent, SWT.NONE);

        // Set the layout for the panel containing the users list
        GridData usersListPanelLayout = new GridData();
        usersListPanelLayout.horizontalAlignment = GridData.FILL;
        usersListPanelLayout.verticalAlignment = GridData.FILL;
        usersListPanelLayout.grabExcessHorizontalSpace = true;
        usersListPanelLayout.grabExcessVerticalSpace = true;
        usersListPanel.setLayoutData(usersListPanelLayout);

        // Set the layout for the contents of that panel
        GridLayout usersListPanelContentLayout = new GridLayout(1, false);
        usersListPanelContentLayout.marginHeight = 0;
        usersListPanelContentLayout.marginWidth = 0;
        usersListPanelContentLayout.verticalSpacing = 5;
        usersListPanel.setLayout(usersListPanelContentLayout);

        // Create the Label first
        Label usersListLabel = new Label(usersListPanel, SWT.HORIZONTAL | SWT.SHADOW_NONE | SWT.CENTER);
        usersListLabel.setText("User");

        // Change the font
        FontData[] fontData = usersListLabel.getFont().getFontData();
        fontData[0].setStyle(SWT.BOLD);
        //fontData[0].setHeight(fontData[0].getHeight() + 1);
        usersListLabel.setFont(new Font(usersListPanel.getDisplay(), fontData));

        // Set the layout for the label
        GridData usersListLabelLayout = new GridData();
        usersListLabelLayout.horizontalAlignment = GridData.BEGINNING;
        usersListLabelLayout.verticalAlignment = GridData.BEGINNING;
        usersListLabelLayout.horizontalSpan = 2;
        usersListLabel.setLayoutData(usersListLabelLayout);

        // Create the issue owner's list
        usersList = new List(usersListPanel, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData usersListLayout = new GridData();
        usersListLayout.horizontalAlignment = GridData.FILL;
        usersListLayout.verticalAlignment = GridData.FILL;
        usersListLayout.horizontalSpan = 2;
        usersListLayout.grabExcessHorizontalSpace = true;
        usersListLayout.grabExcessVerticalSpace = true;
        usersList.setLayoutData(usersListLayout);

        // Add filtering based on selection of issue owners
        EventListViewer listViewer = new EventListViewer(issuesUserFiltered.getUsersList(), usersList);
        issuesUserFiltered.setSelectionList(listViewer.getSelected());
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

    private void createDescriptionsTable(Composite parent, EventTableViewer issuesTableViewer) {
        Table descriptionsTable = new Table(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
        GridData descriptionsTableLayout = new GridData();
        descriptionsTableLayout.horizontalAlignment = GridData.FILL;
        descriptionsTableLayout.verticalAlignment = GridData.FILL;
        descriptionsTableLayout.grabExcessHorizontalSpace = true;
        descriptionsTableLayout.grabExcessVerticalSpace = true;
        descriptionsTable.setLayoutData(descriptionsTableLayout);
        descriptionsTable.showColumn(new TableColumn(descriptionsTable, SWT.LEFT));
        descriptionsTable.getColumn(0).setWidth(618);
        descriptionsTable.getColumn(0).setText("Descriptions");
        descriptionsTable.setHeaderVisible(true);
        new IssueSelectionListener(issuesTableViewer.getSelected(), descriptionsTable);

    }

    /**
     * A listener to selection on the issues table that reflects the change
     * in selection within the description table.
     */
    class IssueSelectionListener implements ListEventListener {

        private EventList source = null;
        private Table descriptionsTable = null;

        IssueSelectionListener(EventList source, Table descriptionsTable) {
            this.source = source;
            this.descriptionsTable = descriptionsTable;
            source.addListEventListener(this);
        }

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            boolean selectionAffected = false;
            while(listChanges.next()) {
                if(!selectionAffected) {
                    selectionAffected = listChanges.getIndex() == 0;
                }
            }

            // Fix the description display value as selection changed
            if(selectionAffected) {
                descriptionsTable.removeAll();

                // The selected value to display was changed
                if(source.size() != 0) {
                    Issue selected = (Issue)source.get(0);
                    java.util.List descriptions = selected.getDescriptions();
                    for (int i = 0; i < descriptions.size(); i++) {
                       int rowOffset = descriptionsTable.getItemCount();
                       formatDescription((Description)descriptions.get(i), rowOffset);
                    }
                }
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