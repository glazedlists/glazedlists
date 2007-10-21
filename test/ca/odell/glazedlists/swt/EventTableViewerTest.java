package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.io.IntegerTableFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
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
    public void guiTestConstructorWithCheckableTable() {
        EventTableViewer<Integer> viewer = new EventTableViewer<Integer>(new BasicEventList<Integer>(), new Table(getShell(), SWT.CHECK), new IntegerTableFormat());
        viewer.dispose();
    }

    /** Tests the default TableItemConfigurer. */
    public void guiTestTableItemConfigurer() {
        final EventList<Color> source = GlazedLists.eventList(RGBNull);
        final TableFormat<Color> tableFormat = GlazedLists.tableFormat(new String[] { "red",
                "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final Table table = new Table(getShell(), SWT.CHECK | SWT.VIRTUAL);
        EventTableViewer<Color> viewer = new EventTableViewer<Color>(source, table, tableFormat);
        doTestDefaultConfigurer(table, viewer);
        // setting custom configurer
        viewer.setTableItemConfigurer(new ColorTableItemConfigurer());
        assertNotSame(TableItemConfigurer.DEFAULT, viewer.getTableItemConfigurer());
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

        // restoring default configurer
        viewer.setTableItemConfigurer(TableItemConfigurer.DEFAULT);
        doTestDefaultConfigurer(table, viewer);

        try {
            viewer.setTableItemConfigurer(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            // expected because null is not allowed
        }
        assertSame(TableItemConfigurer.DEFAULT, viewer.getTableItemConfigurer());
        viewer.dispose();
    }

    /**
     * Helper method to test default TableItemConfigurer.
     */
    private void doTestDefaultConfigurer(Table table, EventTableViewer viewer) {
        assertSame(TableItemConfigurer.DEFAULT, viewer.getTableItemConfigurer());
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
    }

    /** Tests a TableFormat that is a TableColumnConfigurer. */
    public void guiTestTableColumnConfigurer() {
        final EventList<Color> source = GlazedLists.eventList(RGBNull);
        final TableFormat<Color> tableFormat = new ColorTableFormat();
        final Table table = new Table(getShell(), SWT.CHECK);
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

    public void guiTestBug413_FixMe() {
        final BasicEventList<String> source = new BasicEventList<String>();
        for (int i = 0; i < 3; i++) {
            source.add("Str" + i);
        }
        final Table table = new Table(getShell(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final EventTableViewer<String> viewer = new EventTableViewer<String>(source, table, new SimpleTableFormat());
        // BUG:If comment the following line. The table will show Str0, Str1, Str2.
        // If donot comment the following line. The table will show Str1, Str2, Str0.
        // The item order is different.
        table.getItem(2).getBounds();

        assertEquals(3, table.getItemCount());
        assertEquals("Str0", table.getItem(0).getText(0));
        assertEquals("Str1", table.getItem(1).getText(0));
        assertEquals("Str2", table.getItem(2).getText(0));
        viewer.dispose();
    }

    private static class SimpleTableFormat implements TableFormat<String> {
        public int getColumnCount() {
            return 1;
        }
        public String getColumnName(int column) {
            return "Hello";
        }
        public Object getColumnValue(String baseObject, int column) {
            return baseObject;
        }
    }

    /**
     * This test ensures subclasses can prevent the building of a
     * SWTThreadProxyEventList by overriding
     * {@link EventTableViewer#createSwtThreadProxyList}.
     */
    public void guiTestNoThreadProxyingDesired() {
        final EventList<Color> source = new BasicEventList<Color>();
        source.add(Color.RED);
        final TableFormat<Color> tableFormat = new ColorTableFormat();
        final Table table = new Table(getShell(), SWT.VIRTUAL);
        // 1. test with a thread proxy
        final EventTableViewer<Color> colorViewerWithProxy = new EventTableViewer<Color>(source, table, tableFormat);
        assertNotNull(colorViewerWithProxy.swtThreadSource);
        assertSame(colorViewerWithProxy.source, colorViewerWithProxy.swtThreadSource);
        assertEquals(0, colorViewerWithProxy.getSelected().size());
        assertEquals(1, colorViewerWithProxy.getDeselected().size());
        assertSame(Color.RED, colorViewerWithProxy.getSourceList().get(0));
        colorViewerWithProxy.dispose();
        assertNull(colorViewerWithProxy.swtThreadSource);
        assertNull(colorViewerWithProxy.source);

        // 2. test without a thread proxy
        final NoProxyingEventTableViewer<Color> colorViewerNoProxy = new NoProxyingEventTableViewer<Color>(source, table, tableFormat);
        assertNull(colorViewerNoProxy.swtThreadSource);
        assertSame(colorViewerNoProxy.source, source);
        assertEquals(0, colorViewerNoProxy.getSelected().size());
        assertEquals(1, colorViewerNoProxy.getDeselected().size());
        assertSame(Color.RED, colorViewerNoProxy.getSourceList().get(0));
        colorViewerNoProxy.dispose();
        assertNull(colorViewerNoProxy.swtThreadSource);
        assertNull(colorViewerNoProxy.source);
    }

    private static class NoProxyingEventTableViewer<E> extends EventTableViewer<E> {

        public NoProxyingEventTableViewer(EventList<E> source, Table table, TableFormat<? super E> tableFormat) {
            super(source, table, tableFormat);
        }

        /**
         * Returning null implies no ThreadProxyEventList is necessary.
         */
        protected TransformedList<E, E> createSwtThreadProxyList(EventList<E> source, Display display) {
            return null;
        }
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

    private static class ColorTableItemConfigurer implements TableItemConfigurer<Color> {

        /** {@inheritedDoc} */
        public void configure(TableItem item, Color rowValue, Object columnValue, int row, int column) {
            switch(column) {
                case 0: item.setText(column, "Red=" + columnValue); break;
                case 1: item.setText(column, "Green=" + columnValue); break;
                case 2: item.setText(column, "Blue=" + columnValue); break;
                default: item.setText(column, "???");
            }
        }
    }
}