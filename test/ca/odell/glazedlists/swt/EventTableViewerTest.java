package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.io.IntegerTableFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * Tests for the {@link EventTableViewer}.
 * 
 * @author hbrands
 */
public class EventTableViewerTest extends SwtTestCase {
    private static final List<Color> RGB = Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE });
    private static final List<Color> RGBNull = Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE, null });

    /** Tests creation of EventTableViewer with checkable table. */
    public void testConstructorWithCheckableTable() {
        EventTableViewer<Integer> viewer = new EventTableViewer<Integer>(new BasicEventList<Integer>(), new Table(shell, SWT.CHECK), new IntegerTableFormat());
        viewer.dispose();
    }
    
    /** Tests the default TableItemRenderer. */
    public void testDefaultTableItemRenderer() {
        final EventList<Color> source = GlazedLists.eventList(RGBNull);
        final TableFormat<Color> tableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final Table table = new Table(shell, SWT.CHECK);
        EventTableViewer<Color> viewer = new EventTableViewer<Color>(source, table, tableFormat);
        assertSame(TableItemRenderer.DEFAULT, viewer.getTableItemRenderer());
        assertEquals(4, table.getItemCount());
        assertEquals("255", table.getItem(0).getText(0));
        assertEquals("0", table.getItem(0).getText(1));
        assertEquals("0", table.getItem(0).getText(2));
        
        assertEquals("0", table.getItem(1).getText(0));
        assertEquals("255", table.getItem(1).getText(1));
        assertEquals("0", table.getItem(1).getText(2));
        
        assertEquals("0", table.getItem(2).getText(0));
        assertEquals("0", table.getItem(2).getText(1));
        assertEquals("255", table.getItem(2).getText(2));
        
        assertEquals("", table.getItem(3).getText(0));
        assertEquals("", table.getItem(3).getText(1));
        assertEquals("", table.getItem(3).getText(2));
        viewer.dispose();
    }

    /** Tests a custom TableItemRenderer. */
    public void testCustomTableItemRenderer() {
        final EventList<Color> source = GlazedLists.eventList(RGBNull);
        final TableFormat<Color> tableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final Table table = new Table(shell, SWT.CHECK);
        EventTableViewer<Color> viewer = new EventTableViewer<Color>(source, table, tableFormat, new ColorTableItemRenderer());
        assertNotSame(TableItemRenderer.DEFAULT, viewer.getTableItemRenderer());
        assertEquals(4, table.getItemCount());
        assertEquals("Red=255", table.getItem(0).getText(0));
        assertEquals("Green=0", table.getItem(0).getText(1));
        assertEquals("Blue=0", table.getItem(0).getText(2));
        
        assertEquals("Red=0", table.getItem(1).getText(0));
        assertEquals("Green=255", table.getItem(1).getText(1));
        assertEquals("Blue=0", table.getItem(1).getText(2));
        
        assertEquals("Red=0", table.getItem(2).getText(0));
        assertEquals("Green=0", table.getItem(2).getText(1));
        assertEquals("Blue=255", table.getItem(2).getText(2));
        
        assertEquals("Red=null", table.getItem(3).getText(0));
        assertEquals("Green=null", table.getItem(3).getText(1));
        assertEquals("Blue=null", table.getItem(3).getText(2));
        viewer.dispose();
    }
    
    /** Tests a TableFormat that is a TableColumnConfigurer. */
    public void testTableColumnConfigurer() {
        final EventList<Color> source = GlazedLists.eventList(RGBNull);
        final TableFormat<Color> tableFormat = new ColorTableFormat();
        final Table table = new Table(shell, SWT.CHECK);
        EventTableViewer<Color> viewer = new EventTableViewer<Color>(source, table, tableFormat);
        assertEquals(3, table.getColumnCount());
        assertEquals("Red", table.getColumn(0).getText());
        assertEquals(SWT.LEFT, table.getColumn(0).getAlignment());
        assertEquals(80, table.getColumn(0).getWidth());
        assertEquals(true, table.getColumn(0).getResizable());
        assertEquals(null, table.getColumn(0).getToolTipText());
        
        assertEquals("Green", table.getColumn(1).getText());
        assertEquals(SWT.LEFT, table.getColumn(1).getAlignment());
        assertEquals(100, table.getColumn(1).getWidth());
        assertEquals(false, table.getColumn(1).getResizable());
        assertEquals(null, table.getColumn(1).getToolTipText());
        
        assertEquals("Blue", table.getColumn(2).getText());
        assertEquals(SWT.CENTER, table.getColumn(2).getAlignment());
        assertEquals(80, table.getColumn(2).getWidth());
        assertEquals(true, table.getColumn(2).getResizable());
        assertEquals("Blue Column", table.getColumn(2).getToolTipText());
        viewer.dispose();
    }
    
    private static class ColorTableFormat implements TableFormat<Color>, TableColumnConfigurer {

        /** {@inheritedDoc} */
        public int getColumnCount() {
            return 3;
        }

        /** {@inheritedDoc} */
        public String getColumnName(int column) {
            if (column == 0) return "Red";
            if (column == 1) return "Green";
            if (column == 2) return "Blue";
            return "???";
        }

        /** {@inheritedDoc} */
        public Object getColumnValue(Color baseObject, int column) {
            if (baseObject == null) return null;
            if (column == 0) return new Integer(baseObject.getRed());
            if (column == 1) return new Integer(baseObject.getGreen());
            if (column == 2) return new Integer(baseObject.getBlue());
            return null;
        }

        /** {@inheritedDoc} */
        public void configure(TableColumn tableColumn, int column) {
            switch(column) {
                case 1: 
                    tableColumn.setWidth(100);
                    tableColumn.setResizable(false);
                    break;
                case 2:
                    tableColumn.setAlignment(SWT.CENTER);
                    tableColumn.setToolTipText("Blue Column");
                    break;
                default:
            }
        }
    }
    
    private static class ColorTableItemRenderer implements TableItemRenderer {

        /** {@inheritedDoc} */
        public void render(TableItem item, Object columnValue, int column) {
            switch(column) {
                case 0: item.setText(column, "Red=" + columnValue); break;
                case 1: item.setText(column, "Green=" + columnValue); break;
                case 2: item.setText(column, "Blue=" + columnValue); break;
                default: item.setText(column, "???");
            }
        }        
    }
}