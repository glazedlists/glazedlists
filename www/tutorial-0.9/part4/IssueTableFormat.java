/**
 * Glazed Lists Tutorial
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2004 O'DELL ENGINEERING LTD.
 */

// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;

/**
 * The IssueTableFormat specifies how an issue is displayed in a table.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssueTableFormat implements TableFormat {
    
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
}
