/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.jtable;

// for rendering this component inside of a table
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
// for displaying the message as styled text
import javax.swing.JTextPane;
// the different message fields are distinct via styles
import java.awt.SystemColor;
import java.awt.Color;
import javax.swing.text.Style;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleContext.NamedStyle;
import javax.swing.text.SimpleAttributeSet; 
import javax.swing.text.BadLocationException;
import javax.swing.text.TabStop;
import javax.swing.text.TabSet;
import javax.swing.UIManager;


/**
 * Renders a table cell in a pretty way.
 *
 * A very small set of Styles are available by default. To create a new
 * style, use the protected <code>styledDocument</code> variable:
 *
 * <br>Style alert = styledDocument.addStyle("alert", null);
 * <br>StyleConstants.setFontFamily(alert, "sansserif");
 * <br>StyleConstants.setFontSize(alert, 16);
 * <br>StyleConstants.setForeground(alert, new Color(255, 00, 00));
 * 
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part5/index.html">Glazed
 * Lists Tutorial Part 5 - Custom Renderers</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public abstract class StyledDocumentRenderer implements TableCellRenderer {

    /** The view */
    protected JTextPane rendered;
    protected DefaultStyledDocument styledDocument;
    
    /** The default set of styles */
    protected Style base;
    protected Style plain;
    //protected Style plainInfo;
    //protected Style plainCritical;
    protected Style plainItalic;
    protected Style strong;
    protected Style strongItalic;
    protected Style small;
    protected Style smallBold;
    
    /** magic numbers */
    protected static int MINIMUM_ROW_HEIGHT = 16;
    protected static int FONT_SIZE_MEDIUM = 12;
    protected static int FONT_SIZE_BIG = 14;
    protected static int FONT_SIZE_SMALL = 10;
    //protected static Color COLOR_INFO = new Color(00, 00, 99); 
    //protected static Color COLOR_CRITICAL = new Color(99, 00, 00);
    
    /** change the row height upon completion of a render */
    private boolean controlHeight;

    /**
     * Creates a message renderer.
     *
     * @param controlHeight True implies that this renderer will update
     *      the table row height whenever a row is updated. There should
     *      only be one such renderer per row.
     */
    public StyledDocumentRenderer(boolean controlHeight) {
        this.controlHeight = controlHeight;
        styledDocument = new DefaultStyledDocument();
        rendered = new JTextPane(styledDocument);
        rendered.setEnabled(false);
        createStyles();
    }
    
    /**
     * add all of the styles used for message
     */
    private void createStyles() {
        // base style
        base = styledDocument.addStyle("base", null);
        StyleConstants.setFontFamily(base, "sansserif");
        // medium sized text
        plain = styledDocument.addStyle("plain", base);
        StyleConstants.setFontSize(plain, FONT_SIZE_MEDIUM);
        // plain text with italics
        plainItalic = styledDocument.addStyle("plainItalic", plain);
        StyleConstants.setItalic(plainItalic, true);
        // big, bold text
        strong = styledDocument.addStyle("strong", base);
        StyleConstants.setBold(strong, true);
        StyleConstants.setFontSize(strong, FONT_SIZE_BIG);
        // strong text with italics
        strongItalic = styledDocument.addStyle("strongItalic", strong);
        StyleConstants.setItalic(strongItalic, true);
        // small text
        small = styledDocument.addStyle("small", base);
        StyleConstants.setFontSize(small, FONT_SIZE_SMALL);
        // small and bold
        smallBold = styledDocument.addStyle("smallBold", small);
        StyleConstants.setBold(smallBold, true);
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
        writeObject(table, value, isSelected, hasFocus, row, column);
        // match the row height to the document height
        getRendered(table, value, isSelected, hasFocus, row, column);
        // give back the rendered textpane
        return rendered;
    }
    
    /**
     * Implementing classes fill this method with a series of append() calls
     * to write their message to the textpane.
     */
    public abstract void writeObject(JTable table, Object value, 
        boolean isSelected, boolean hasFocus, int row, int column);
        
    /**
     * Colours the result component to be the selection colour if isSelected,
     * or the non-selected color otherwise.
     */
    private void colorSelected(boolean isSelected) {
        if(isSelected) {
            rendered.setBackground(UIManager.getColor("Table.selectionBackground"));
            StyleConstants.setBackground(base, UIManager.getColor("Table.selectionBackground"));
            StyleConstants.setForeground(base, UIManager.getColor("Table.selectionForeground"));
        } else {
            rendered.setBackground(UIManager.getColor("Table.background"));
            StyleConstants.setBackground(base, UIManager.getColor("Table.background"));
            StyleConstants.setForeground(base, UIManager.getColor("Table.foreground"));
        }
    }
    
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
        clear();
    }

    /**
     * Gets the completely rendered document. This also resizes the table row
     * of the current row to be the height of this cell. 
     *
     * The table row is resized with the following heuristic: If the column is
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
    
    /**
     * Appends the specified text to the textpane.
     *
     * @param text The text to append. The characters "\n" and "\t" are
     *      useful for creating newlines.
     * @param format The format to render text in. This class comes with
     *      a small set of predefined formats accessible only to extending
     *      classes via protected members.
     */
    protected void append(String text, Style format) {
        try {
            int offset = styledDocument.getLength();
            styledDocument.insertString(offset, text, format);
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Clears the styled document.
     */
    protected void clear() {
        try {
            styledDocument.remove(0, styledDocument.getLength());
        } catch(BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Appends the specified component inline with the text.
     *
     * This method is currently broken because JTextPanes cannot be used as
     * a 'rubber stamp' when writing components. When this method is repaired,
     * the append(Component) method will be marked protected.
     */
    private void append(Component component) {
         rendered.insertComponent(component);
    }

    /**
     * Gets a single tabstop in an attribute set, which can be applied
     * using styledDocument.setParagraphAttributes()
     *
     * @todo complete this method
     */
    protected void appendTab(String align, String lead, int location, Style format) {
        if(!align.equals("R")) {
            throw new IllegalArgumentException("Tab alignment must be \"L\", \"R\" or \"C\"");
        }
        if(!lead.equals("")) {
            throw new IllegalArgumentException("Lead must be \"\", \"_\" or \".\"");
        }
        SimpleAttributeSet tabsStyle = new SimpleAttributeSet();
        TabStop right = new TabStop(location, TabStop.ALIGN_RIGHT, TabStop.LEAD_NONE);
        TabSet tabs = new TabSet(new TabStop[] { right });
        StyleConstants.setTabSet(tabsStyle, tabs);
        int offset = styledDocument.getLength();
        styledDocument.setParagraphAttributes(offset, 1, tabsStyle, false);
        append("\t", format);
    }
    protected void appendRightTab(JTable table, int column, Style format) {
        String align = "R";
        String lead = "";
        int location = table.getColumnModel().getColumn(column).getWidth() - 10;
        appendTab(align, lead, location, format);
    }
}
