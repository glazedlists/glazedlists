/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.IntegerTableFormat;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the {@link EventTableViewer}.
 *
 * @author Holger Brands
 */
public class EventTableViewerTest extends SwtTestCase {

    private static final List<Color> RGB = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE);
    private static final List<Color> RGBNull = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, null);

    /** Tests creation of EventTableViewer with checkable table. */
    @Test
    public void testConstructorWithCheckableTable() {
        EventTableViewer<Integer> viewer = new EventTableViewer<Integer>(new BasicEventList<Integer>(), new Table(getShell(), SWT.CHECK), new IntegerTableFormat());
        viewer.dispose();
    }

    /** Tests SWT table sort column and sort direction with GL single column sort. */
    @Test
    public void testSingleColumnSort() {
        final EventList<Color> source = GlazedLists.eventList(RGB);
        final SortedList<Color> sorted = new SortedList<Color>(source, null);
        final TableFormat<Color> tableFormat = GlazedLists.tableFormat(new String[] { "red",
                "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final Table table = new Table(getShell(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final EventTableViewer<Color> viewer = new EventTableViewer<Color>(sorted, table, tableFormat);
        final TableComparatorChooser<Color> chooser = TableComparatorChooser.install(viewer, sorted, false);
        assertNull(table.getSortColumn());
        assertEquals(SWT.NONE, table.getSortDirection());
        chooser.getComparatorsForColumn(0).add(GlazedLists.beanPropertyComparator(Color.class, "red"));
        chooser.getComparatorsForColumn(1).add(GlazedLists.beanPropertyComparator(Color.class, "green"));
        chooser.getComparatorsForColumn(2).add(GlazedLists.beanPropertyComparator(Color.class, "blue"));

        chooser.appendComparator(0, 0, false);
        assertEquals(table.getColumn(0), table.getSortColumn());
        assertEquals(SWT.UP, table.getSortDirection());

        chooser.clearComparator();
        assertNull(table.getSortColumn());
        assertEquals(SWT.NONE, table.getSortDirection());

        chooser.appendComparator(0, 0, true);
        assertEquals(table.getColumn(0), table.getSortColumn());
        assertEquals(SWT.DOWN, table.getSortDirection());

        chooser.fromString("column 2");
        assertEquals(table.getColumn(2), table.getSortColumn());
        assertEquals(SWT.UP, table.getSortDirection());

        chooser.fromString("column 1 reversed");
        assertEquals(table.getColumn(1), table.getSortColumn());
        assertEquals(SWT.DOWN, table.getSortDirection());
    }

    /**
     * Tests SWT table sort column and sort direction with GL multiple column sort.
     * <p>As SWT table only supports one sort column, it'll always be the GL primary sort column.</p>
     */
    @Test
    public void testMultiColumnSort() {
        final EventList<Color> source = GlazedLists.eventList(RGB);
        final SortedList<Color> sorted = new SortedList<Color>(source, null);
        final TableFormat<Color> tableFormat = GlazedLists.tableFormat(new String[] { "red",
                "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final Table table = new Table(getShell(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final EventTableViewer<Color> viewer = new EventTableViewer<Color>(sorted, table, tableFormat);
        final TableComparatorChooser<Color> chooser = TableComparatorChooser.install(viewer, sorted, true);
        assertNull(table.getSortColumn());
        assertEquals(SWT.NONE, table.getSortDirection());
        chooser.getComparatorsForColumn(0).add(GlazedLists.beanPropertyComparator(Color.class, "red"));
        chooser.getComparatorsForColumn(1).add(GlazedLists.beanPropertyComparator(Color.class, "green"));
        chooser.getComparatorsForColumn(2).add(GlazedLists.beanPropertyComparator(Color.class, "blue"));

        chooser.appendComparator(0, 0, false);
        assertEquals(table.getColumn(0), table.getSortColumn());
        assertEquals(SWT.UP, table.getSortDirection());

        chooser.clearComparator();
        assertNull(table.getSortColumn());
        assertEquals(SWT.NONE, table.getSortDirection());

        chooser.appendComparator(0, 0, true);
        assertEquals(table.getColumn(0), table.getSortColumn());
        assertEquals(SWT.DOWN, table.getSortDirection());

        // primary sort column remains swt table sort column
        chooser.appendComparator(2, 0, false);
        assertEquals(table.getColumn(0), table.getSortColumn());
        assertEquals(SWT.DOWN, table.getSortDirection());

        chooser.fromString("column 2");
        assertEquals(table.getColumn(2), table.getSortColumn());
        assertEquals(SWT.UP, table.getSortDirection());

        chooser.fromString("column 1 reversed, column 2");
        assertEquals(table.getColumn(1), table.getSortColumn());
        assertEquals(SWT.DOWN, table.getSortDirection());
    }

    /** Tests the default TableItemConfigurer. */
    @Test
    public void testTableItemConfigurer() {
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
    @Test
    public void testTableColumnConfigurer() {
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

    /**
     * Tests the lists {@link EventTableViewer#getTogglingSelected()} and
     * {@link EventTableViewer#getTogglingDeselected()} for programmatic selection control.
     */
    @Test
    public void testToggleSelection() {
        final BasicEventList<String> list = new BasicEventList<String>();
        final Table table = new Table(getShell(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final EventTableViewer<String> viewer = new EventTableViewer<String>(list, table, new SimpleTableFormat());

        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(list, viewer.getDeselected());
        assertEquals(list, viewer.getTogglingDeselected());
        assertEquals(0, table.getSelectionCount());

        // remove on TogglingDeselected selects
        viewer.getTogglingDeselected().remove("A");
        viewer.getTogglingDeselected().remove(1);
        viewer.getTogglingDeselected().removeAll(GlazedListsTests.delimitedStringToList("F D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getTogglingSelected());
        assertEquals(4, table.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 2, 3, 5}, table.getSelectionIndices()));

        // add on TogglingDeselected deselects
        viewer.getTogglingDeselected().add("F");
        viewer.getTogglingDeselected().addAll(GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B C D E F"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A"), viewer.getTogglingSelected());
        assertEquals(1, table.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0}, table.getSelectionIndices()));

        // add on TogglingSelected selects
        viewer.getTogglingSelected().add("F");
        viewer.getTogglingSelected().addAll(GlazedListsTests.delimitedStringToList("C D"));
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("B E"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D F"), viewer.getTogglingSelected());
        assertEquals(4, table.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 2, 3, 5}, table.getSelectionIndices()));

        // remove on TogglingSelected deselects
        viewer.getTogglingSelected().remove("A");
        viewer.getTogglingSelected().remove(1);
        viewer.getTogglingSelected().removeAll(GlazedListsTests.delimitedStringToList("F"));
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A B D E F"), viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("C"), viewer.getTogglingSelected());
        assertEquals(1, table.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {2}, table.getSelectionIndices()));

        // remove on source list
        list.remove("C");
        list.removeAll(GlazedListsTests.delimitedStringToList("B E"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, table.getSelectionCount());

        // add on source list
        list.add("E");
        list.addAll(GlazedListsTests.delimitedStringToList("C B"));
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, table.getSelectionCount());

        // clear on TogglingDeselected selects all deselected
        viewer.getTogglingDeselected().clear();
        assertEquals(Collections.EMPTY_LIST, viewer.getDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingSelected());
        assertEquals(6, table.getSelectionCount());
        assertTrue(Arrays.equals(new int[] {0, 1, 2, 3, 4, 5}, table.getSelectionIndices()));

        // clear on TogglingSelected deselects all selected
        viewer.getTogglingSelected().clear();
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getDeselected());
        assertEquals(GlazedListsTests.delimitedStringToList("A D F E C B"), viewer.getTogglingDeselected());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(Collections.EMPTY_LIST, viewer.getTogglingSelected());
        assertEquals(0, table.getSelectionCount());
        viewer.dispose();
    }

    @Test
    public void testBug413() {
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
        @Override
        public int getColumnCount() {
            return 1;
        }
        @Override
        public String getColumnName(int column) {
            return "Hello";
        }
        @Override
        public Object getColumnValue(String baseObject, int column) {
            return baseObject;
        }
    }

    /**
     * Tests clearing the source list of {@link EventTableViewer}.
     */
    @Test
    public void testClearOnThreadProxy_FixMe() {
        final BasicEventList<String> list = new BasicEventList<String>();
        final Table table = new Table(getShell(), SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        final EventTableViewer<String> viewer = new EventTableViewer<String>(list, table, new SimpleTableFormat());
        assertEquals(list, viewer.getSourceList());
        // populate the list
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        assertEquals(list, viewer.getSourceList());
        assertEquals(6, table.getItemCount());
        viewer.getTogglingSelected().add("F");
        viewer.getTogglingSelected().add("A");
        assertEquals(GlazedListsTests.delimitedStringToList("A F"), viewer.getSelected());
        assertEquals(2, table.getSelectionCount());
        viewer.getSourceList().getReadWriteLock().writeLock().lock();
        try {
            viewer.getSourceList().clear();
        } finally {
            viewer.getSourceList().getReadWriteLock().writeLock().unlock();
        }
        assertEquals(list, viewer.getSourceList());
        assertTrue(list.isEmpty());
        assertEquals(0, table.getItemCount());
        assertEquals(Collections.EMPTY_LIST, viewer.getSelected());
        assertEquals(0, table.getSelectionCount());
    }

    private static class ColorTableFormat implements TableFormat<Color>, TableColumnConfigurer {

        /** {@inheritedDoc} */
        @Override
        public int getColumnCount() {
            return 3;
        }

        /** {@inheritedDoc} */
        @Override
        public String getColumnName(int column) {
            if (column == 0) {
                return "Red";
            }
            if (column == 1) {
                return "Green";
            }
            if (column == 2) {
                return "Blue";
            }
            return "???";
        }

        /** {@inheritedDoc} */
        @Override
        public Object getColumnValue(Color baseObject, int column) {
            if (baseObject == null) {
                return null;
            }
            if (column == 0) {
                return new Integer(baseObject.getRed());
            }
            if (column == 1) {
                return new Integer(baseObject.getGreen());
            }
            if (column == 2) {
                return new Integer(baseObject.getBlue());
            }
            return null;
        }

        /** {@inheritedDoc} */
        @Override
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
        @Override
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