/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.demo.issuebrowser.Description;

/**
 * The DescriptionTableFormat specifies how a description is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DescriptionTableFormat implements TableFormat<Description> {

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return "Description";
    }

    public Object getColumnValue(Description baseObject, int column) {
        return baseObject;
    }
}