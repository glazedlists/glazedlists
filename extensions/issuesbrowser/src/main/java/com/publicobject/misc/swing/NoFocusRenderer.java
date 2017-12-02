/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Render cells without the focus ring.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class NoFocusRenderer<E> implements TableCellRenderer, ListCellRenderer<E> {

    private TableCellRenderer delegateTableCellRenderer;
    private ListCellRenderer<? super E> delegateListCellRenderer;

    public NoFocusRenderer(TableCellRenderer delegate) {
        this.delegateTableCellRenderer = delegate;
    }
    public NoFocusRenderer(ListCellRenderer<? super E> delegate) {
        this.delegateListCellRenderer = delegate;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return delegateTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
    }
    @Override
    public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean hasFocus) {
        return delegateListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, false);
    }
}