/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import java.util.Comparator;
import java.util.Date;

/**
 * The IssueTableFormat specifies how an issue is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class IssueTableFormat implements WritableTableFormat, AdvancedTableFormat {

    public int getColumnCount() {
        return 8;
    }

    public String getColumnName(int column) {
        switch (column) {
            case 0: return "ID";
            case 1: return "Type";
            case 2: return "Created";
            case 3: return "Modified";
            case 4: return "Priority";
            case 5: return "Status";
            case 6: return "Result";
            case 7: return "Summary";
            default: return null;
        }
    }

    public Class getColumnClass(int column) {
        switch (column) {
            case 0: return Integer.class;
            case 2: return Date.class;
            case 3: return Date.class;
            case 4: return Priority.class;
            default: return String.class;
        }
    }

    public Comparator getColumnComparator(int column) {
        switch (column) {
            case 5: return GlazedLists.caseInsensitiveComparator();
            default: return GlazedLists.comparableComparator();
        }
    }

    public boolean isEditable(Object baseObject, int column) {
        return baseObject instanceof SeparatorList.Separator;
    }

    public Object setColumnValue(Object baseObject, Object editedValue, int column) {
        return null;
    }

    public Object getColumnValue(Object baseObject, int column) {
        if (baseObject == null) return null;
        if (baseObject instanceof SeparatorList.Separator) {
            SeparatorList.Separator<Issue> separator = (SeparatorList.Separator<Issue>)baseObject;
            if(column == 5) return separator.first().getSubcomponent();
            else return "------";
        }
        
        final Issue issue = (Issue) baseObject;
        switch (column) {
            case 0: return issue.getId();
            case 1: return issue.getIssueType();
            case 2: return issue.getCreationTimestamp();
            case 3: return issue.getDeltaTimestamp();
            case 4: return issue.getPriority();
            case 5: return issue.getStatus();
            case 6: return issue.getResolution();
            case 7: return issue.getShortDescription();
            default: return null;
        }
    }
}