package ca.odell.glazedlists;

import ca.odell.glazedlists.gui.TableFormat;

/**
 * <code>StringTableFormat</code> specifies how a string is displayed in a table.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class StringTableFormat implements TableFormat<String> {

    public int getColumnCount() {
        return 1;
    }

    public String getColumnName(int column) {
        return "Value";
    }

    public Object getColumnValue(String baseObject, int column) {
        return baseObject;
    }
}
