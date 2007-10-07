/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package com.publicobject.issuesbrowser.swt;

import ca.odell.glazedlists.swt.TableItemRenderer;

import org.eclipse.swt.widgets.TableItem;

import java.text.DateFormat;
import java.util.Date;

/**
 * {@link TableItemRenderer} for the issues table.
 * 
 * @author hbrands
 */
public class IssueTableItemRenderer implements TableItemRenderer {

    /** DateFormatter. */
    private final DateFormat dateFormatter = DateFormat.getDateInstance();
    
    /** {@inheritDoc} */
    public void render(TableItem item, Object columnValue, int column) {
        switch (column) {
            case 2: item.setText(column, dateToString(columnValue)); break;            
            case 3: item.setText(column, dateToString(columnValue)); break;
            default: TableItemRenderer.DEFAULT.render(item, columnValue, column);
        }
    }

    /**
     * Converts a Date to a String with the default {@link DateFormat}.
     */
    private String dateToString(Object value) {
        String result = "";
        if (value instanceof Date) {
            result = dateFormatter.format((Date) value);
        }
        return result;
    }
}
