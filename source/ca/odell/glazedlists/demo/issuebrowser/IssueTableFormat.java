package ca.odell.glazedlists.demo.issuebrowser;

import java.util.Comparator;
// glazed lists
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.GlazedLists;

/**
 * The IssueTableFormat specifies how an issue is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class IssueTableFormat implements AdvancedTableFormat<Issue> {

	public int getColumnCount() {
		return 6;
	}

	public String getColumnName(int column) {
        switch (column) {
            case 0: return "ID";
            case 1: return "Type";
            case 2: return "Priority";
            case 3: return "State";
            case 4: return "Result";
            case 5: return "Summary";
        }
		return null;
	}

	public Class getColumnClass(int column) {
		switch(column) {
			case 0:
				return Integer.class;
			case 2:
				return Priority.class;
			default:
				return String.class;
		}
	}

	public Comparator getColumnComparator(int column) {
        if(column == 5) {
            return GlazedLists.caseInsensitiveComparator();
        } else {
            return GlazedLists.comparableComparator();
        }
	}

	public Object getColumnValue(Issue issue, int column) {
		if (issue == null) return null;

        switch (column) {
            case 0: return issue.getId();
            case 1: return issue.getIssueType();
            case 2: return issue.getPriority();
            case 3: return issue.getStatus();
            case 4: return issue.getResolution();
            case 5: return issue.getShortDescription();
        }
		return null;
	}
}