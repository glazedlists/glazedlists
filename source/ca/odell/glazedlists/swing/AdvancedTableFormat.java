/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// records fit inside of tables
import javax.swing.JTable;

/**
 * Allows the ability to specify column class information in addition to the standard
 * {@link TableFormat} information.
 *
 * <p>This class can be used as an alternative to the simple {@link TableFormat}
 * class to provide column class information that is used to determine what cell renderer
 * and/or editor should be used for a column. If no custom renderers or editors are
 * required, it is sufficient to implement {@link TableFormat} only.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 *
 * @see WritableTableFormat
 * @see TableFormat
 */
public interface AdvancedTableFormat extends TableFormat {
    
	/**
	 * Returns the most specific superclass for all the cell values in the column. This
	 * is used by the table to set up a default renderer and editor for the column.
	 *
	 * @param column		The index of the column being edited.
	 */
	public Class getColumnClass(int column);
}
