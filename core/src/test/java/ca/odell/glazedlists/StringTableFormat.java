package ca.odell.glazedlists;

import ca.odell.glazedlists.gui.TableFormat;

/**
 * <code>StringTableFormat</code> specifies how a string is displayed in a table.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class StringTableFormat implements TableFormat<String> {

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        return "Value";
    }

    @Override
    public Object getColumnValue(String baseObject, int column) {
        return baseObject;
    }
}
