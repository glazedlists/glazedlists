/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;

import java.util.Comparator;

/**
 * The IssueTableFormat specifies how an issue is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class IssueTableFormat implements AdvancedTableFormat {

	public int getColumnCount() {
		return 6;
	}

	public String getColumnName(int column) {
		if (column == 0) {
			return "ID";
		} else if (column == 1) {
			return "Type";
		} else if (column == 2) {
			return "Priority";
		} else if (column == 3) {
			return "State";
		} else if (column == 4) {
			return "Result";
		} else if (column == 5) {
			return "Summary";
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

	public Object getColumnValue(Object baseObject, int column) {
		if (baseObject == null) return null;
        if (baseObject instanceof SeparatorList.Separator) {
            SeparatorList.Separator<Issue> separator = (SeparatorList.Separator<Issue>)baseObject;
            if(column == 5) return separator.first().getSubcomponent();
            else return "------";
        }
        Issue issue = (Issue) baseObject;
		if (column == 0) {
			return issue.getId();
		} else if (column == 1) {
			return issue.getIssueType();
		} else if (column == 2) {
			return issue.getPriority();
		} else if (column == 3) {
			return issue.getStatus();
		} else if (column == 4) {
			return issue.getResolution();
		} else if (column == 5) {
			return issue.getShortDescription();
		}
		return null;
	}
}
