/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swing;

import ca.odell.glazedlists.demo.issuebrowser.Priority;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;


/**
 * Displays Priority cells with pretty colors.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
class PriorityTableCellRenderer extends DefaultTableCellRenderer {

	private static final Color P1_COLOR = Color.RED.darker();
	private static final Color P2_COLOR = Color.ORANGE.darker();
	private static final Color P3_COLOR = Color.YELLOW.darker();
	private static final Color P4_COLOR = Color.DARK_GRAY;
	private static final Color P5_COLOR = Color.GRAY;

	private final Color default_foreground;

	PriorityTableCellRenderer() {
		default_foreground = getForeground();
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

		Component component = super.getTableCellRendererComponent(table, value,
			isSelected, hasFocus, row, column);

		// If null or not a priority, use the standard foreground
		if (value == null || !(value instanceof Priority)) {
			component.setForeground(default_foreground);

			// ... otherwise, draw with a colored foreground
		} else {
			if (value.equals(Priority.P1))
				component.setForeground(P1_COLOR);
			else if (value.equals(Priority.P2))
				component.setForeground(P2_COLOR);
			else if (value.equals(Priority.P3))
				component.setForeground(P3_COLOR);
			else if (value.equals(Priority.P4))
				component.setForeground(P4_COLOR);
			else if (value.equals(Priority.P5))
				component.setForeground(P5_COLOR);
			else
				component.setForeground(default_foreground);	// unknown
		}

		return component;
	}
}
