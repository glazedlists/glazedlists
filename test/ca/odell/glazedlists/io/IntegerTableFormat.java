/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import ca.odell.glazedlists.gui.TableFormat;

/**
 * The IntegerTableFormat specifies how an integer is displayed in a table.
 * 
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IntegerTableFormat implements TableFormat<Integer> {
    
    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return "Value";
    }

    public Object getColumnValue(Integer baseObject, int column) {
        return baseObject;
    }
}