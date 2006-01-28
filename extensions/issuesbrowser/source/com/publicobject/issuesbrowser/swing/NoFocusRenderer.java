/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Render cells without the focus ring.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NoFocusRenderer implements TableCellRenderer, ListCellRenderer {

    private TableCellRenderer delegateTableCellRenderer;
    private ListCellRenderer delegateListCellRenderer;

    public NoFocusRenderer(TableCellRenderer delegate) {
        this.delegateTableCellRenderer = delegate;
    }
    public NoFocusRenderer(ListCellRenderer delegate) {
        this.delegateListCellRenderer = delegate;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return delegateTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        return delegateListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, false);
    }
}