/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;

// for displaying the table cell as HTML
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.table.*;


/**
 * A {@link TableCellRenderer} for displaying descriptions in a cell.
 *
 * <p>This displays a multi-line description in HTML using a custom renderer
 * from the renderpack project.
 *
 * @see <a href="http://renderpack.dev.java.net">renderpack</a>
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DescriptionRenderer extends HTMLTableCellRenderer {

    public DescriptionRenderer() {
        super(true);
    }
    
    public void writeObject(StringBuffer buffer, JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
        if(value == null) return;
        Description description = (Description)value;
        // write who
        buffer.append("<strong>");
        buffer.append(description.getWho());
        buffer.append(":</strong><br>");

        // write the body
        String[] lines = description.getText().split("\n");
        for(int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            buffer.append("<br>");
        }
    }
}
/**
 * Base class for HTML renderers
 *
 * <p>It is simple to modify the StyleSheet in the constructor of your derived class:
 * <br><code>styleSheet.addRule("H1 {font-family: sans-serif; font-size: 14; color: rgb(0,0,153) }");</code>
 * <br><code>styleSheet.addRule(".excerpt {font-family: sans-serif; font-size: 12; color: black }");</code>
 * <br><code>styleSheet.addRule(".url {font-family: sans-serif; font-size: 9; color: rgb(0,153,0) }");</code>
 *
 * @see <a href="http://www.htmlhelp.com/reference/html40/">HTML Reference</a>
 * @see <a href="http://www.htmlhelp.com/reference/css/">CSS Reference</a>
 *
 * @author <a href="mailto:jeffa@wolfram.com">Jeff Adams</a>
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 * @author <a href="mailto:dmarquis@neopeak.com">David Marquis</a>
 */
class HTMLRenderer {
    /** the stylesheet of this document */
    protected StyleSheet styleSheet;

    /** the model */
    protected HTMLDocument html;

    /** the view */
    protected JTextPane rendered;

    /**
     * Creates an HTML cell renderer.
     */
    public HTMLRenderer() {


        // prepare the document to display non-editable html
        rendered = new JTextPane();
        rendered.setContentType("text/html");
        rendered.setEditable(false);

        html = (HTMLDocument)rendered.getDocument();
        styleSheet = html.getStyleSheet();
    }

    /**
     * Colours the result component to be the selection colour if isSelected,
     * or the non-selected color otherwise.
     */
    protected void colorSelected(boolean isSelected) {
        if(isSelected) {
            rendered.setBackground(UIManager.getColor("Table.selectionBackground"));
        } else {
            rendered.setBackground(UIManager.getColor("Table.background"));
        }
    }
}


/**
 * Renders a table cell using HTML formatting.
 *
 * <p>It is simple to modify the StyleSheet in the constructor of your derived class:
 * <br><code>styleSheet.addRule("H1 {font-family: sans-serif; font-size: 14; color: rgb(0,0,153) }");</code>
 * <br><code>styleSheet.addRule(".excerpt {font-family: sans-serif; font-size: 12; color: black }");</code>
 * <br><code>styleSheet.addRule(".url {font-family: sans-serif; font-size: 9; color: rgb(0,153,0) }");</code>
 *
 * @see <a href="http://www.htmlhelp.com/reference/html40/">HTML Reference</a>
 * @see <a href="http://www.htmlhelp.com/reference/css/">CSS Reference</a>
 *
 * @author <a href="mailto:jeffa@wolfram.com">Jeff Adams</a>
 * @author <a href="mailto:jesse@odell.ca">Jesse Wilson</a>
 */
abstract class HTMLTableCellRenderer extends HTMLRenderer implements TableCellRenderer {

    /** String composition */
    private StringBuffer stringBuffer = new StringBuffer();

    /** change the row height upon completion of a render */
    private boolean controlHeight;

    /**
     * Creates an HTML cell renderer.
     *
     * @param controlHeight True implies that this renderer will update
     *      the table row height whenever a row is updated. There should
     *      only be one such renderer per row.
     */
    public HTMLTableCellRenderer(boolean controlHeight) {
        this.controlHeight = controlHeight;
    }


    /**
     * Gets this Component for display in a table. This sets the rendered width,
     * then calls writeObject, and finally sets the table row height.
     */
    public final Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {

        // match the document width to the column width
        prepareRendered(table, value, isSelected, hasFocus, row, column);

        // append the actual contents of this cell
        stringBuffer.delete(0, stringBuffer.length());
        writeObject(stringBuffer, table, value, isSelected, hasFocus, row, column);
        rendered.setText(stringBuffer.toString());

        // match the row height to the document height
        getRendered(table, value, isSelected, hasFocus, row, column);

        // give back the rendered HTML
        return rendered;
    }

    /**
     * Implementors of this class should examine the source object and use it to
     * write HTML for that object to the String buffer. For example:
     * <br><code>Customer cust = (Customer)value;</code>
     * <br><code>buffer.append("&lt;h1&gt;");</code>
     * <br><code>buffer.append(cust.getName());</code>
     * <br><code>buffer.append("&lt;/h1&gt;");</code>
     */
    public abstract void writeObject(StringBuffer buffer, JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column);



    /**
     * Prepares the resultant rendered document to the supplied table and selection
     * settings. The rendered document may behave strangely if the resultant
     * rendered document has a height greater than 1000 pixels.
     */
    protected void prepareRendered(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    
        // make it look selected
        colorSelected(isSelected);
        
        // compute where to put tabs using the column width
        int columnWidth = table.getColumnModel().getColumn(column).getWidth();
        Rectangle bounds = rendered.getBounds();
        bounds.width = columnWidth;
        bounds.height = 1000;
        rendered.setBounds(bounds);
        
        // clear the old document value
        rendered.setText("");
    }

    /**
     * Gets the completely rendered document. This also resizes the table row
     * of the current row to be the height of this cell.
     *
     * <p>The table row is resized with the following heuristic: If the column is
     * zero, the row height is always resized. If the column is non-zero, the
     * row height is only resized if it needs to be increased. This is due
     * to the fact that tables render left to right (0 to n), and so it is only
     * necessary to shrink the table at zero to shrink it at all. This will
     * fail when a row that self-resizes is in a column other than zero.
     */
    protected void getRendered(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    
        // resize the table row to an appropriate height if necessary
        int requiredRowHeight = rendered.getPreferredSize().height;
        int currentRowHeight = table.getRowHeight(row);
        if(controlHeight && currentRowHeight != requiredRowHeight) {
            table.setRowHeight(row, requiredRowHeight);
        }
    }
}
