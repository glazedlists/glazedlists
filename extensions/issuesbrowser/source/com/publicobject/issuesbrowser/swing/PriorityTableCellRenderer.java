/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import com.publicobject.issuesbrowser.Priority;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;
import java.util.IdentityHashMap;

/**
 * Displays Priority cells with pretty colors.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
class PriorityTableCellRenderer extends DefaultTableCellRenderer {

    private static Map<Priority,Color> PRIORITY_COLOR_MAP = new IdentityHashMap<Priority,Color>();

    static {
        PRIORITY_COLOR_MAP.put(Priority.P1, Color.RED.darker());
        PRIORITY_COLOR_MAP.put(Priority.P2, Color.ORANGE.darker());
        PRIORITY_COLOR_MAP.put(Priority.P3, Color.YELLOW.darker());
        PRIORITY_COLOR_MAP.put(Priority.P4, Color.DARK_GRAY);
        PRIORITY_COLOR_MAP.put(Priority.P5, Color.GRAY);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        component.setForeground(PRIORITY_COLOR_MAP.get(value));

        return component;
    }
}