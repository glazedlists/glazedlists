/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// swt
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
// demo
import ca.odell.glazedlists.demo.Issue;
// glazed lists
import ca.odell.glazedlists.*;

/**
 */
public class Demo {
    
    public Demo(Shell shell) {
        BasicEventList issues = new BasicEventList();
        issues.addAll(Issue.loadIssues());
        
        // sash
        SashForm sashForm = new SashForm(shell, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // everything table
		//Table everythingTable = new Table(sashForm, SWT.MULTI | SWT.FULL_SELECTION);
		Table everythingTable = new Table(sashForm, SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);

        // checked table
		Table checkedTable = new Table(sashForm, SWT.MULTI | SWT.FULL_SELECTION | SWT.CHECK);

        // filter edit
        Text filterEdit = new Text(shell, SWT.SINGLE);
        filterEdit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // filter list
        TextFilterList issuesFiltered = new TextFilterList(issues, filterEdit);

        // sort the list of all issues
        SortedList allSorted = new SortedList(issuesFiltered);

        // all table list
		EventTableViewer allTableViewer = new EventTableViewer(allSorted, everythingTable, new IssueTableFormat());

        // set up table sorting for column-clicking
        new TableComparatorChooser(allTableViewer, allSorted, false);
        
        // checked table list
		EventTableViewer checkedTableViewer = new EventTableViewer(issuesFiltered, checkedTable, new IssueTableFormat());
		checkedTableViewer.setCheckedOnly(true);
    }

	public static void main(String[] args) {
		System.setProperty("java.library.path", ".");
		System.out.println("Library Path: " +  System.getProperty("java.library.path"));

		Display display = new Display();
		Shell shell = new Shell(display);
        shell.setLayout(new GridLayout(1, false));

		new Demo(shell);
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
         Issue issue = (Issue)baseObject;
         if(checked) issue.setPriority("P3");
         else issue.setPriority("P2");
    }
    
    /**
     * Gets whether the specified object is checked.
     */
    public boolean getChecked(Object baseObject) {
        Issue issue = (Issue)baseObject;
        return issue.getPriority().equals("P3");
    }
}
