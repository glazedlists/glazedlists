/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.Matcher;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

/**
 * Test DefaultEventTableModel
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultEventTableModelTest extends SwingTestCase {

    /**
     * Verifies that the EDT check works, e.g. an IllegalStateException is thrown when a ListEvent
     * arrives on a non-EDT thread
     */
    public void testOnMainThreadEDTViolation() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);

        final TableFormat<Color> colorTableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        final DefaultEventTableModel<Color> tableModel = new DefaultEventTableModel<Color>(colors, colorTableFormat);
        assertEquals(2, tableModel.getRowCount());
        try {
            colors.add(Color.BLUE);
            fail("failed to receive IllegalStateException because of missing ThreadProxyList");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    /**
     * Verifies that the new getElementAt() method of EventTableModel works.
     */
    public void guiTestGetElementAt() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);

        TableFormat<Color> colorTableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        DefaultEventTableModel<Color> tableModel = new DefaultEventTableModel<Color>(colors, colorTableFormat);

        assertEquals(Color.RED, tableModel.getElementAt(0));
        assertEquals(Color.GREEN, tableModel.getElementAt(1));
        assertEquals(Color.BLUE, tableModel.getElementAt(2));

        try {
            tableModel.getElementAt(100);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            tableModel.getElementAt(-1);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    /**
     * Verifies that the getValueAt() method of EventTableModel works.
     */
    public void guiTestGetValueAt() {
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);

        TableFormat<Color> colorTableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        DefaultEventTableModel<Color> tableModel = new DefaultEventTableModel<Color>(colors, colorTableFormat);

        assertEquals(new Integer(Color.RED.getRed()), tableModel.getValueAt(0, 0));
        assertEquals(new Integer(Color.GREEN.getGreen()), tableModel.getValueAt(1, 1));
        assertEquals(new Integer(Color.BLUE.getBlue()), tableModel.getValueAt(2, 2));

        try {
            tableModel.getValueAt(100, 0);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            tableModel.getValueAt(-1, 0);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

//    public void guiTestConstructorLocking() throws InterruptedException {
//        fail("I don't think this is appropriate for testing DefaultEventTableModel, or do we still want to deal with locking during construction of DefaultEventTableModel??");
//    }

    public void guiTestSetValueAt_FilterList() {
        final EventList<JLabel> labels = new BasicEventList<JLabel>();
        labels.add(new JLabel("saskatchewan"));
        labels.add(new JLabel("saskwatch"));
        labels.add(new JLabel("sasky"));

        final ObservableElementList<JLabel> observedLabels = new ObservableElementList<JLabel>(labels, GlazedLists.beanConnector(JLabel.class));

        final FilterList<JLabel> saskLabels = new FilterList<JLabel>(observedLabels, new SaskLabelMatcher());

        final DefaultEventTableModel<JLabel> tableModel = new DefaultEventTableModel<JLabel>(saskLabels, new LabelTableFormat());

        assertEquals(3, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
        assertEquals("saskwatch", tableModel.getValueAt(1, 0));
        assertEquals("sasky", tableModel.getValueAt(2, 0));

        tableModel.setValueAt("maskwatch", 1, 0);
        assertEquals(2, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
        assertEquals("sasky", tableModel.getValueAt(1, 0));

        tableModel.setValueAt("maskwatch", 1, 0);
        assertEquals(1, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
    }

    public void guiTestSetValueAtWithCopyingTableFormat_FilterList() {
        final EventList<JLabel> labels = new BasicEventList<JLabel>();
        labels.add(new JLabel("saskatchewan"));
        labels.add(new JLabel("saskwatch"));
        labels.add(new JLabel("sasky"));

        final ObservableElementList<JLabel> observedLabels = new ObservableElementList<JLabel>(labels, GlazedLists.beanConnector(JLabel.class));

        final FilterList<JLabel> saskLabels = new FilterList<JLabel>(observedLabels, new SaskLabelMatcher());

        final DefaultEventTableModel<JLabel> tableModel = new DefaultEventTableModel<JLabel>(saskLabels, new CopyingLabelTableFormat());

        assertEquals(3, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
        assertEquals("saskwatch", tableModel.getValueAt(1, 0));
        assertEquals("sasky", tableModel.getValueAt(2, 0));

        tableModel.setValueAt("maskwatch", 1, 0);
        assertEquals(2, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
        assertEquals("sasky", tableModel.getValueAt(1, 0));

        tableModel.setValueAt("maskwatch", 1, 0);
        assertEquals(1, tableModel.getRowCount());
        assertEquals("saskatchewan", tableModel.getValueAt(0, 0));
    }

    public void guiTestSetValueAt_SortedList() {
        final EventList<JLabel> labels = new BasicEventList<JLabel>();
        labels.add(new JLabel("banana"));
        labels.add(new JLabel("cherry"));
        labels.add(new JLabel("apple"));

        final ObservableElementList<JLabel> observedLabels = new ObservableElementList<JLabel>(labels, GlazedLists.beanConnector(JLabel.class));

        final SortedList<JLabel> sortedLabels = new SortedList<JLabel>(observedLabels, GlazedLists.beanPropertyComparator(JLabel.class, "text"));

        final DefaultEventTableModel<JLabel> tableModel = new DefaultEventTableModel<JLabel>(sortedLabels, new LabelTableFormat());

        assertEquals(3, tableModel.getRowCount());
        assertEquals("apple", tableModel.getValueAt(0, 0));
        assertEquals("banana", tableModel.getValueAt(1, 0));
        assertEquals("cherry", tableModel.getValueAt(2, 0));

        tableModel.setValueAt("orange", 1, 0);
        assertEquals(3, tableModel.getRowCount());
        assertEquals("apple", tableModel.getValueAt(0, 0));
        assertEquals("cherry", tableModel.getValueAt(1, 0));
        assertEquals("orange", tableModel.getValueAt(2, 0));
    }

    /**
     * This test ensures the compiler allows us to use a TableFormat of a more
     * generic type than the actual EventTableModel.
     */
    public void guiTestGenericTypeRelationships() {
        final EventList<DefaultListCellRenderer> source = new BasicEventList<DefaultListCellRenderer>();
        final TableFormat<JLabel> tableFormat = new LabelTableFormat();
        new DefaultEventTableModel<DefaultListCellRenderer>(source, tableFormat);
    }



    /**
     * This TableFormat returns new JLabels from its setValueAt()
     * method rather than modifying the existing one in place.
     */
    private static final class CopyingLabelTableFormat implements WritableTableFormat<JLabel> {
        public boolean isEditable(JLabel baseObject, int column) { return true; }

        public JLabel setColumnValue(JLabel baseObject, Object editedValue, int column) {
            return new JLabel(editedValue == null ? null : editedValue.toString());
        }

        public int getColumnCount() { return 1; }
        public String getColumnName(int column) { return "Label Text"; }
        public Object getColumnValue(JLabel baseObject, int column) { return baseObject.getText(); }
    }

    /**
     * This TableFormat modifyies existing JLabels in place.
     */
    private static final class LabelTableFormat implements WritableTableFormat<JLabel> {
        public boolean isEditable(JLabel baseObject, int column) { return true; }

        public JLabel setColumnValue(JLabel baseObject, Object editedValue, int column) {
            baseObject.setText(editedValue == null ? null : editedValue.toString());
            return baseObject;
        }

        public int getColumnCount() { return 1; }
        public String getColumnName(int column) { return "Label Text"; }
        public Object getColumnValue(JLabel baseObject, int column) { return baseObject.getText(); }
    }

    private static final class SaskLabelMatcher implements Matcher<JLabel> {
        public boolean matches(JLabel item) {
            return item.getText().startsWith("sask");
        }
    }

    private static final List<Color> rgb = Arrays.asList(Color.RED, Color.GREEN, Color.BLUE);
    private static final List<Color> rbg = Arrays.asList(Color.RED, Color.BLUE, Color.GREEN);
    private static final List<Color> gbr = Arrays.asList(Color.GREEN, Color.BLUE, Color.RED);

    /**
     * Perform a quick run through the basics of TableComparatorChooser.
     */
    public void guiTestTableComparatorChooser() {
        // build the data
        EventList<Color> colors = GlazedLists.eventList(rgb);
        SortedList<Color> sortedColors = new SortedList<Color>(colors, null);

        // build a sorted table and model
        TableFormat<Color> greenBlueTableFormat = GlazedLists.tableFormat(new String[] { "green", "blue" }, new String[] { "Green", "Blue" });
        DefaultEventTableModel<Color> tableModel = new DefaultEventTableModel<Color>(colors, greenBlueTableFormat);

        // prepare the table for sorting and rendering its header
        JTable table = new JTable(tableModel);
        TableComparatorChooser<Color> tableComparatorChooser = TableComparatorChooser.install(table, sortedColors, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        // sort by each column in sequence
        clickColumnHeader(table, 0);
        assertEquals(rbg, sortedColors);
        clickColumnHeader(table, 1);
        assertEquals(rgb, sortedColors);

        // make sure we can still paint the header cells
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 0);
        // make sure we can handle negative indexes
        final Component rendered = headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, -1, -1);
        // no sort icon expected
        if (rendered instanceof JLabel) {
            assertNull(((JLabel) rendered).getIcon());
        }

        // uninstall the table comparator chooser and make sure no ill effects are left behind
        tableComparatorChooser.dispose();
        headerRenderer = table.getTableHeader().getDefaultRenderer();

        // clicking column headers shouldn't change anything after the comparator
        // chooser is disposed
        clickColumnHeader(table, 0);
        assertEquals(rgb, sortedColors);
        clickColumnHeader(table, 1);
        assertEquals(rgb, sortedColors);

        // make sure we can still paint the header cells
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 0);
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 1);

        // now create a three column table
        TableFormat<Color> redGreenBlueTableFormat = GlazedLists.tableFormat(new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
        tableModel.setTableFormat(redGreenBlueTableFormat);

        // make sure we can paint all three header cells
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 0);
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 1);
        headerRenderer.getTableCellRendererComponent(table, tableModel.getColumnName(0), false, false, 0, 2);

        // try out the new table for sorting
        TableComparatorChooser.install(table, sortedColors, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
        sortedColors.setComparator(null);
        assertEquals(rgb, sortedColors);
        clickColumnHeader(table, 0);
        assertEquals(gbr, sortedColors);
    }

    /**
     * Ensure correct locking of EventTableModel such that it does not block calls to
     * ListSelectionModel.
     */
    public void guiTestLocking1() {
        final BasicEventList<String> list = new BasicEventList<String>();
        list.add("Member_one");
        final TableFormat<String> tableFormat = GlazedLists.tableFormat(new String[] {"bytes"}, new String[] {"Test"});
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(list, tableFormat);
        final JTable table = new JTable(model);
        final EventSelectionModel selectionModel = new EventSelectionModel<String>(list);
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionModel(selectionModel);
        list.add("Member_two");
    }

    /**
     * Ensure correct locking of EventTableModel such that it does not block calls to
     * ListSelectionModel.
     */
    public void guiTestLocking2() {
        final BasicEventList<String> list = new BasicEventList<String>();
        list.add("Member_one");
        final TableFormat<String> tableFormat = GlazedLists.tableFormat(new String[] {"bytes"}, new String[] {"Test"});
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(list, tableFormat);
        final JTable table = new JTable(model);
        final EventSelectionModel<String> selectionModel = new EventSelectionModel<String>(list);
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionModel(selectionModel);
        selectionModel.setSelectionInterval(0, 0);
        list.remove(0);
    }

    /**
     * Verifies that table selection is preserved, when handling a complex ListEvent with blocks.
     */
    public void guiTestRemoveWithBlocksInListEvent() {
        // setup JTable with EventTableModel and EventSelectionModel
        final EventList<String> list = new BasicEventList<String>();
        list.addAll(GlazedListsTests.delimitedStringToList("A B C D E F"));
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(list,
                GlazedLists.tableFormat(new String[] {"bytes"}, new String [] {"Bytes"}));
        final TableModelChangeCounter counter = new TableModelChangeCounter();
        model.addTableModelListener(counter);
        final JTable table = new JTable(model);
        final EventSelectionModel<String> selModel = new EventSelectionModel<String>(list);
        table.setSelectionModel(selModel);
        // establish a selection
        selModel.setSelectionInterval(1, 1);
        assertEquals(GlazedListsTests.stringToList("B"), selModel.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D E F"), selModel.getDeselected());
        list.removeAll(GlazedListsTests.delimitedStringToList("E F"));
        assertEquals(2, counter.getCountAndReset());
        assertEquals(GlazedListsTests.stringToList("B"), selModel.getSelected());
        assertEquals(GlazedListsTests.delimitedStringToList("A C D"), selModel.getDeselected());
    }


    /**
     * Tests that a table selection is correctly reflected when the user presses UP- and DOWN-arrow
     * keys while the table is in sorted state.
     */
    public void guiTestChangeSelectionByKeysInSortedState_FixMe() {
        // build the data
        final EventList<JLabel> labels = new BasicEventList<JLabel>();
        labels.add(new JLabel("def"));
        labels.add(new JLabel("ghi"));
        labels.add(new JLabel("abc"));
        final SortedList<JLabel> sortedLabels = new SortedList<JLabel>(labels, null);
        // build a sorted table and model
        final DefaultEventTableModel<JLabel> tableModel = new DefaultEventTableModel<JLabel>(sortedLabels, new LabelTableFormat());
        final EventSelectionModel<JLabel> selectionModel = new EventSelectionModel<JLabel>(sortedLabels);
        final JTable table = new JTable(tableModel);
        table.setSelectionModel(selectionModel);
        // set an initial selection
        selectionModel.setSelectionInterval(1, 1);
        assertEquals(Arrays.asList(labels.get(1)), selectionModel.getSelected());
        assertEquals(labels, sortedLabels);
        final TableComparatorChooser<JLabel> tableComparatorChooser = TableComparatorChooser.install(table, sortedLabels, TableComparatorChooser.MULTIPLE_COLUMN_KEYBOARD);
        // sort the table by the first column
        clickColumnHeader(table, 0);
        // check that selected element is preserved
        assertEquals(Arrays.asList(labels.get(1)), selectionModel.getSelected());
        assertEquals(Arrays.asList(labels.get(2), labels.get(0), labels.get(1)), sortedLabels);
        // check current indexes of selection model
        assertEquals(2, selectionModel.getMinSelectionIndex());
        assertEquals(2, selectionModel.getMaxSelectionIndex());
        // this could be the problem for the following failure: the lead selection index is -1
        assertEquals(-1, selectionModel.getLeadSelectionIndex());
        // call the action that is triggered on an UP-arrow-key press
        final Action action = table.getActionMap().get("selectPreviousRow");
        assertNotNull("Action 'selectPreviousRow' not found", action);
        action.actionPerformed(new ActionEvent(table, 1, "selectPreviousRow"));
        // the element of the previous row should be selected
        assertEquals(1, selectionModel.getMinSelectionIndex());
        assertEquals(1, selectionModel.getMaxSelectionIndex());
        assertEquals(Arrays.asList(labels.get(0)), selectionModel.getSelected());
    }

    /**
     * Fake a click on the specified column. This is useful for tests where the
     * table has not been layed out on screen and may have invalid dimensions.
     */
    private void clickColumnHeader(JTable table, int column) {
        Rectangle columnHeader = table.getTableHeader().getHeaderRect(column);
        MouseEvent mouseEvent = new MouseEvent(table.getTableHeader(), 0, 0, 0, columnHeader.x, columnHeader.y, 1, false, MouseEvent.BUTTON1);
        MouseListener[] listeners = table.getTableHeader().getMouseListeners();
        for(int i = 0; i < listeners.length; i++) {
            listeners[i].mouseClicked(mouseEvent);
        }
    }

    /**
     * Counts the number of TableModelEvents fired.
     */
    private static class TableModelChangeCounter implements TableModelListener {
        private int count = 0;
        public void tableChanged(TableModelEvent e) {
            count++;
        }

        public int getCountAndReset() {
            int result = count;
            count = 0;
            return result;
        }
    }
}
