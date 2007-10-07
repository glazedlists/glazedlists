/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package com.publicobject.issuesbrowser.swt;

import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swt.TableColumnConfigurer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

import com.publicobject.issuesbrowser.IssueTableFormat;

/**
 * Extended {@link TableFormat} for configuring the SWT issue table columns.
 * 
 * @author hbrands
 */
public class SwtIssueTableFormat extends IssueTableFormat implements TableColumnConfigurer {

    /** {@inheritedDoc} */
    public void configure(TableColumn tableColumn, int column) {
        switch (column) {
            case 0: 
                tableColumn.setWidth(30);
                tableColumn.setResizable(false);
                tableColumn.setAlignment(SWT.RIGHT);
                break;
            case 1: tableColumn.setWidth(90); break;
            case 2: tableColumn.setWidth(80); break;
            case 3: tableColumn.setWidth(80); break;
            case 4: tableColumn.setWidth(50); tableColumn.setResizable(false); break;
            case 5: tableColumn.setWidth(80); break;
            case 6: tableColumn.setWidth(90); break;
            case 7: tableColumn.setWidth(600); break;
            default: throw new AssertionError("Unexpected column index: " + column);
        }
    }
}
