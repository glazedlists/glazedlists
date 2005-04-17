/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

// glazed lists

import ca.odell.glazedlists.demo.issuebrowser.Description;

// for displaying the table cell as HTML
import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.*;
import javax.swing.table.*;
// for rendering dates
import java.text.*;
// for rendering this component inside of a table
import javax.swing.text.*;


/**
 * A {@link TableCellRenderer} for displaying descriptions in a cell.
 * <p/>
 * <p>This displays a multi-line description in HTML using a custom renderer
 * from the renderpack project.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @see <a href="http://renderpack.dev.java.net">renderpack</a>
 */
public class DescriptionRenderer extends StyledRenderer {

    /** for displaying dates */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
    
	private Style whoStyle = null;
	private Style plain = null;

	public DescriptionRenderer() {
		super(true);

		plain = styledDocument.addStyle("plain", null);
		whoStyle = styledDocument.addStyle("boldItalicRed", null);
		StyleConstants.setBold(whoStyle, true);
		StyleConstants.setFontSize(whoStyle, 14);
	}

	public void writeObject(DefaultStyledDocument doc, JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

		if (value == null) return;
		Description description = (Description) value;

		// write who
		append(doc, description.getWho(), whoStyle);
        append(doc, " - ", whoStyle);
        append(doc, dateFormat.format(description.getWhen()), whoStyle);
		append(doc, "\n", whoStyle);

		// write the body
		append(doc, description.getText(), plain);
	}
}

/**
 * Base class for styled renderers.
 * <p/>
 * <p>To create styles, use the protected <code>styledDocument</code> variable:
 * <pre><code>
 *  boldItalicRed = styledDocument.addStyle("boldItalicRed", null);
 *  StyleConstants.setFontFamily(boldItalicRed, "sansserif");
 *  StyleConstants.setItalic(boldItalicRed, true);
 *  StyleConstants.setBold(boldItalicRed, true);
 *  StyleConstants.setFontSize(boldItalicRed, 14);
 *  StyleConstants.setForeground(boldItalicRed, new Color(255, 00, 00));
 * </code></pre>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
abstract class StyledRenderer implements TableCellRenderer {

	/**
	 * The view
	 */
	protected JTextPane rendered;
	protected DefaultStyledDocument styledDocument;

	/**
	 * change the row height upon completion of a render
	 */
	private boolean controlHeight;

	/**
	 * Creates a message renderer.
	 *
	 * @param controlHeight True implies that this renderer will update
	 *                      the table row height whenever a row is updated. There should
	 *                      only be one such renderer per row.
	 */
	public StyledRenderer(boolean controlHeight) {
		this.controlHeight = controlHeight;
		styledDocument = new DefaultStyledDocument();
		rendered = new JTextPane(styledDocument);
		rendered.setEnabled(false);
	}

	/**
	 * Gets this Component for display in a table. This sets the rendered width,
	 * then calls writeObject, and finally sets the table row height.
	 */
	public final Component getTableCellRendererComponent(JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {
		// match the document width to the column width
		beforeWrite(styledDocument, table, value, isSelected, hasFocus, row, column);
		// append the actual contents of this cell
		writeObject(styledDocument, table, value, isSelected, hasFocus, row, column);
		// match the row height to the document height
		afterWrite(styledDocument, table, value, isSelected, hasFocus, row, column);
		// give back the rendered textpane
		return rendered;
	}

	/**
	 * Implementing classes fill this method with a series of append() calls
	 * to write their message to the textpane.
	 */
	public abstract void writeObject(DefaultStyledDocument doc, JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column);

	/**
	 * Prepares the resultant rendered document to the supplied table and selection
	 * settings. The rendered document may behave strangely if the resultant
	 * rendered document has a height greater than 1000 pixels.
	 */
	protected void beforeWrite(DefaultStyledDocument doc, JTable table, Object value,
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
	 * Colours the result component to be the selection colour if isSelected,
	 * or the non-selected color otherwise.
	 */
	private void colorSelected(boolean isSelected) {
		if (isSelected) {
			rendered.setBackground(UIManager.getColor("Table.selectionBackground"));
			//StyleConstants.setBackground(base, UIManager.getColor("Table.selectionBackground"));
			//StyleConstants.setForeground(base, UIManager.getColor("Table.selectionForeground"));
		} else {
			rendered.setBackground(UIManager.getColor("Table.background"));
			//StyleConstants.setBackground(base, UIManager.getColor("Table.background"));
			//StyleConstants.setForeground(base, UIManager.getColor("Table.foreground"));
		}
	}

	/**
	 * Gets the completely rendered document. This also resizes the table row
	 * of the current row to be the height of this cell.
	 * <p/>
	 * The table row is resized with the following heuristic: If the column is
	 * zero, the row height is always resized. If the column is non-zero, the
	 * row height is only resized if it needs to be increased. This is due
	 * to the fact that tables render left to right (0 to n), and so it is only
	 * necessary to shrink the table at zero to shrink it at all. This will
	 * fail when a row that self-resizes is in a column other than zero.
	 */
	protected void afterWrite(DefaultStyledDocument doc, JTable table, Object value,
		boolean isSelected, boolean hasFocus, int row, int column) {

		// resize the table row to an appropriate height if necessary
		int requiredRowHeight = rendered.getPreferredSize().height;
		int currentRowHeight = table.getRowHeight(row);
		if (controlHeight && currentRowHeight != requiredRowHeight) {
			table.setRowHeight(row, requiredRowHeight);
		}
	}

	/**
	 * Convenience method for appending the specified text to the specified document.
	 *
	 * @param text   The text to append. The characters "\n" and "\t" are
	 *               useful for creating newlines.
	 * @param format The format to render text in. This class comes with
	 *               a small set of predefined formats accessible only to extending
	 *               classes via protected members.
	 */
	public static void append(DefaultStyledDocument targetDocument, String text,
		Style format) {
		try {
			int offset = targetDocument.getLength();
			targetDocument.insertString(offset, text, format);
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
}

