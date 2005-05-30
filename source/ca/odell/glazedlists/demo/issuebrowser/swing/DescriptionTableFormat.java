/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
