/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.migrationkit;

// table stuff
import javax.swing.table.*;
import javax.swing.*;
// color stuff
import java.awt.Color;
// render stuff
import java.awt.Component;

/**
 * A renderer that simply takes a base table cell renderer and adds
 * stripes to alternating rows. This makes the rows look more like a ledger.
 *
 * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=11">bug 11</a>
 */
public class StripedTableCellRenderer implements TableCellRenderer {
        
    /** the colours for odd and even rows */
    private Color oddRowsColor;
    private Color evenRowsColor;
    
    /** the renderer to perform the initial rendering, may be null */
    private TableCellRenderer baseRenderer = null;
    
    /**
     * Create a new StripedRowsTableRenderer that alternates between
     * the specified colours in rendering cells. This uses the default
     * renderer to do the initial rendering.
     *
     * @see javax.swing.JTable#getDefaultRenderer(Class) JTable.getDefaultRenderer()
     */
    public StripedTableCellRenderer(Color oddRowsColor, Color evenRowsColor) {
        this.oddRowsColor = oddRowsColor;
        this.evenRowsColor = evenRowsColor;
    }

    /**
     * Create a new StripedRowsTableRenderer that alternates between
     * the specified colours in rendering cells. This uses the renderer
     * as specified to do the initial rendering.
     */
    public StripedTableCellRenderer(Color oddRowsColor, Color evenRowsColor,
    TableCellRenderer baseRenderer) {
        this.baseRenderer = baseRenderer;
        this.oddRowsColor = oddRowsColor;
        this.evenRowsColor = evenRowsColor;
    }
    
    /**
     * Returns the component used for drawing the cell. 
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        // get the renderer to use for this cell
        TableCellRenderer renderer;
        if(baseRenderer == null) {
            Class rendererClass = Object.class;
            if(value != null) rendererClass = value.getClass();
            renderer = table.getDefaultRenderer(rendererClass);
        } else {
            renderer = baseRenderer;
        }

        // do the initial rendering with no striping
        Component rendered = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        // skip striping if there's a selection
        if(isSelected) return rendered;
        
        // do the striping
        if(row % 2 == 0) {
            rendered.setBackground(evenRowsColor);
        } else {
            rendered.setBackground(oddRowsColor);
        }
        
        return rendered;
    }
}
