/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swing;

// glazed lists

import ca.odell.glazedlists.demo.issuebrowser.Description;
import ca.odell.glazedlists.gui.*;


/**
 * The DescriptionTableFormat specifies how a description is displayed in a table.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DescriptionTableFormat implements TableFormat {

	public int getColumnCount() {
		return 1;
	}

	public String getColumnName(int column) {
		return "Description";
	}

	public Object getColumnValue(Object baseObject, int column) {
		if (baseObject == null) return null;
		Description description = (Description) baseObject;
		return description;
	}
}
