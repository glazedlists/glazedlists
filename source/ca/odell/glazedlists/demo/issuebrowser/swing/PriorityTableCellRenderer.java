/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

import ca.odell.glazedlists.demo.issuebrowser.Priority;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Displays Priority cells with pretty colors.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
class PriorityTableCellRenderer extends DefaultTableCellRenderer {

    private static final Map<Priority,Color> PRIORITY_COLOR_MAP = new HashMap<Priority,Color>();
    static {
        PRIORITY_COLOR_MAP.put(Priority.P1, Color.RED.darker());
        PRIORITY_COLOR_MAP.put(Priority.P2, Color.ORANGE.darker());
        PRIORITY_COLOR_MAP.put(Priority.P3, Color.YELLOW.darker());
        PRIORITY_COLOR_MAP.put(Priority.P4, Color.DARK_GRAY);
        PRIORITY_COLOR_MAP.put(Priority.P5, Color.GRAY);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        final Color foreground = PRIORITY_COLOR_MAP.get((Priority) value);
        component.setForeground(foreground != null ? foreground : this.getForeground());

        return component;
    }
}