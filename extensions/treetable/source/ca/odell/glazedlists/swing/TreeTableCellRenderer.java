/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

public class TreeTableCellRenderer implements TableCellRenderer {

    private TableCellRenderer delegate;
    private TreeList treeList;

    private final TreeTableCellPanel component = new TreeTableCellPanel();

    public TreeTableCellRenderer(TableCellRenderer delegate, TreeList treeList) {
        this.delegate = delegate == null ? new DefaultTableCellRenderer() : delegate;
        this.treeList = treeList;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        final Component c = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (c instanceof JComponent) {
            final JComponent jc = (JComponent) c;
            component.setToolTipText(jc.getToolTipText());
        }

        final int depth = treeList.depth(row);
        final boolean isExpanded = true; // treeList.isExpanded(row);
        final boolean isLeaf = false; // treeList.isLeaf(row);

        component.configure(depth, isExpanded, isLeaf, c);
        return component;
    }

    public TableCellRenderer getDelegate() {
        return delegate;
    }

    public void dispose() {
        this.delegate = null;
        this.treeList = null;
    }
}