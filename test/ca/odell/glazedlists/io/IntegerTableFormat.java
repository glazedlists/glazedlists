/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import ca.odell.glazedlists.gui.*;

/**
 * The IntegerTableFormat specifies how an integer is displayed in a table.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IntegerTableFormat implements TableFormat {
    
    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return "Value";
    }

    public Object getColumnValue(Object baseObject, int column) {
        return baseObject;
    }
}
