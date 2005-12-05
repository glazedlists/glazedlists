/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.Platform;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import de.kupzog.ktable.SWTX;
import de.kupzog.ktable.KTableCellResizeListener;
import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.examples.TextModelExample;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class KTableDemo {

    public static void main(String[] args) {
        // create a shell...
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("KTable examples");

        // put a tab folder in it...
        TabFolder tabFolder = new TabFolder(shell, SWT.NONE);

        createTextTable(tabFolder);

        // display the shell...
        shell.setSize(600,600);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void createTextTable(TabFolder tabFolder) {
        TabItem item1 = new TabItem(tabFolder, SWT.NONE);
        item1.setText("Text Table");
        Composite comp1 = new Composite(tabFolder, SWT.NONE);
        item1.setControl(comp1);
        comp1.setLayout(new FillLayout());
        final KTable table = new KTable(comp1, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY);
        table.setModel(new TextModelExample());
        table.addCellSelectionListener(
            new KTableCellSelectionListener() {
                public void cellSelected(int col, int row, int statemask) {
                    System.out.println("Cell ["+col+";"+row+"] selected.");
                }
                public void fixedCellSelected(int col, int row, int statemask) {
                    System.out.println("Header ["+col+";"+row+"] selected.");
                }
            }
        );

        table.addCellResizeListener(
            new KTableCellResizeListener() {
                public void columnResized(int col, int newWidth) {
                    System.out.println("Column "+col+" resized to "+newWidth);
                }
                public void rowResized(int row, int newHeight) {
                    System.out.println("Row "+row+" resized to "+newHeight);
                }
            }
        );

        /**
         *  Set Excel-like table cursors
         */

        String platform = Platform.PLATFORM;

        if(platform.equals("win32")) {
            Image crossCursor = SWTX.loadImageResource(table.getDisplay(), "/icons/cross_win32.gif");
            Image row_resizeCursor = SWTX.loadImageResource(table.getDisplay(), "/icons/row_resize_win32.gif");
            Image column_resizeCursor  = SWTX.loadImageResource(table.getDisplay(), "/icons/column_resize_win32.gif");

            // we set the hotspot to the center, so calculate the number of pixels from hotspot to lower border:
            Rectangle crossBound        = crossCursor.getBounds();
            Rectangle rowresizeBound    = row_resizeCursor.getBounds();
            Rectangle columnresizeBound = column_resizeCursor.getBounds();

            Point crossSize        = new Point(crossBound.width/2, crossBound.height/2);
            Point rowresizeSize    = new Point(rowresizeBound.width/2, rowresizeBound.height/2);
            Point columnresizeSize = new Point(columnresizeBound.width/2, columnresizeBound.height/2);

            table.setDefaultCursor(new Cursor(table.getDisplay(), crossCursor.getImageData(), crossSize.x, crossSize.y), crossSize);
            table.setDefaultRowResizeCursor(new Cursor(table.getDisplay(), row_resizeCursor.getImageData(), rowresizeSize.x, rowresizeSize.y));
            table.setDefaultColumnResizeCursor(new Cursor(table.getDisplay(), column_resizeCursor.getImageData(), columnresizeSize.x, columnresizeSize.y));

        } else {

            // Cross

            Image crossCursor      = SWTX.loadImageResource(table.getDisplay(), "/icons/cross.gif");
            Image crossCursor_mask = SWTX.loadImageResource(table.getDisplay(), "/icons/cross_mask.gif");

            // Row Resize

            Image row_resizeCursor      = SWTX.loadImageResource(table.getDisplay(), "/icons/row_resize.gif");
            Image row_resizeCursor_mask = SWTX.loadImageResource(table.getDisplay(), "/icons/row_resize_mask.gif");

            // Column Resize

            Image column_resizeCursor      = SWTX.loadImageResource(table.getDisplay(), "/icons/column_resize.gif");
            Image column_resizeCursor_mask = SWTX.loadImageResource(table.getDisplay(), "/icons/column_resize_mask.gif");

            // we set the hotspot to the center, so calculate the number of pixels from hotspot to lower border:

            Rectangle crossBound        = crossCursor.getBounds();
            Rectangle rowresizeBound    = row_resizeCursor.getBounds();
            Rectangle columnresizeBound = column_resizeCursor.getBounds();

            Point crossSize        = new Point(crossBound.width/2, crossBound.height/2);
            Point rowresizeSize    = new Point(rowresizeBound.width/2, rowresizeBound.height/2);
            Point columnresizeSize = new Point(columnresizeBound.width/2, columnresizeBound.height/2);

            table.setDefaultCursor(new Cursor(table.getDisplay(), crossCursor_mask.getImageData(), crossCursor.getImageData(), crossSize.x, crossSize.y), crossSize);
            table.setDefaultRowResizeCursor(new Cursor(table.getDisplay(), row_resizeCursor_mask.getImageData(), row_resizeCursor.getImageData(), rowresizeSize.x, rowresizeSize.y));
            table.setDefaultColumnResizeCursor(new Cursor(table.getDisplay(), column_resizeCursor_mask.getImageData(), column_resizeCursor.getImageData(), columnresizeSize.x, columnresizeSize.y));

        }
    }
}