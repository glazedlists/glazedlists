/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

/**
 * Test EventTableModel from the Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventTableModelTest extends SwingTestCase {

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
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
        EventTableModel tableModel = new EventTableModel<Color>(colors, colorTableFormat);

        assertEquals(Color.RED, tableModel.getElementAt(0));
        assertEquals(Color.GREEN, tableModel.getElementAt(1));
        assertEquals(Color.BLUE, tableModel.getElementAt(2));

        try {
            tableModel.getElementAt(100);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }

        try {
            tableModel.getElementAt(-1);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }
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
        EventTableModel tableModel = new EventTableModel<Color>(colors, colorTableFormat);

        assertEquals(new Integer(Color.RED.getRed()), tableModel.getValueAt(0, 0));
        assertEquals(new Integer(Color.GREEN.getGreen()), tableModel.getValueAt(1, 1));
        assertEquals(new Integer(Color.BLUE.getBlue()), tableModel.getValueAt(2, 2));

        try {
            tableModel.getValueAt(100, 0);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }

        try {
            tableModel.getValueAt(-1, 0);
            fail("failed to receive IndexOutOfBoundsException for invalid index");
        } catch (IndexOutOfBoundsException e) { }
    }

    public void guiTestConstructorLocking() throws InterruptedException {
        // create a list which will record our multithreaded interactions with a list
        final ThreadRecorderEventList<Integer> atomicList = new ThreadRecorderEventList<Integer>(new BasicEventList<Integer>());

        // start a thread which adds new Integers every 50 ms
        final Thread writerThread = new Thread(GlazedListsTests.createJerkyAddRunnable(atomicList, null, 2000, 50), "WriterThread");
        writerThread.start();

        // make sure the writerThread has started writing
        Thread.sleep(200);

        // create a list whose get() method pauses for 50 ms before returning the value
        final EventList<Integer> delayList = GlazedListsTests.delayList(atomicList, 50);

        // the test: creating the EventTableModel should be atomic and pause the writerThread while it initializes its internal state
        new EventTableModel(delayList, GlazedLists.tableFormat(new String[0], new String[0]));

        // wait until the writerThread finishes before asserting the recorded state
        writerThread.join();

        // correct locking should have produced a thread log like: WriterThread* AWT-EventQueue-0* WriterThread*
        // correct locking should have produced a read/write pattern like: W...W R...R W...W
        assertEquals(3, atomicList.getReadWriteBlockCount());
    }

    public void guiTestSetValueAt_FilterList() {
        final EventList<JLabel> labels = new BasicEventList<JLabel>();
        labels.add(new JLabel("saskatchewan"));
        labels.add(new JLabel("saskwatch"));
        labels.add(new JLabel("sasky"));

        final ObservableElementList<JLabel> observedLabels = new ObservableElementList<JLabel>(labels, GlazedLists.beanConnector(JLabel.class));

        final FilterList<JLabel> saskLabels = new FilterList<JLabel>(observedLabels, new SaskLabelMatcher());

        final EventTableModel tableModel = new EventTableModel<JLabel>(saskLabels, new SaskTableFormat());

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

        final EventTableModel tableModel = new EventTableModel<JLabel>(saskLabels, new CopyingSaskTableFormat());

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

        final EventTableModel tableModel = new EventTableModel<JLabel>(sortedLabels, new SaskTableFormat());

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
     * This TableFormat returns new JLabels from its setValueAt()
     * method rather than modifying the existing one in place.
     */
    private static final class CopyingSaskTableFormat implements WritableTableFormat<JLabel> {
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
    private static final class SaskTableFormat implements WritableTableFormat<JLabel> {
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

    /**
     * Perform a quick run through the basics of TableComparatorChooser.
     */
    public void guiTestTableComparatorChooser() {

        // build the data
        EventList<Color> colors = new BasicEventList<Color>();
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLUE);
        SortedList<Color> sortedColors = new SortedList<Color>(colors, null);

        // build a sorted table and model
        TableFormat<Color> greenBlueTableFormat = GlazedLists.tableFormat(new String[] { "green", "blue" }, new String[] { "Green", "Blue" });
        EventTableModel<Color> tableModel = new EventTableModel<Color>(colors, greenBlueTableFormat);

        // prepare the table for sorting and rendering its header
        JTable table = new JTable(tableModel);
        TableComparatorChooser<Color> tableComparatorChooser = new TableComparatorChooser<Color>(table, sortedColors, false);
        TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        // sort by each column in sequence
        clickColumnHeader(table, 0);
        assertEquals(Arrays.asList(new Color[] { Color.RED, Color.BLUE, Color.GREEN }), sortedColors);
        clickColumnHeader(table, 1);
        assertEquals(Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE }), sortedColors);

        // uninstall the table comparator chooser and make sure no ill effects are left behind
        tableComparatorChooser.dispose();
        headerRenderer = table.getTableHeader().getDefaultRenderer();

        // clicking column headers shouldn't change anything after the comparator
        // chooser is disposed
        clickColumnHeader(table, 0);
        assertEquals(Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE }), sortedColors);
        clickColumnHeader(table, 1);
        assertEquals(Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE }), sortedColors);

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
        tableComparatorChooser = new TableComparatorChooser<Color>(table, sortedColors, false);
        sortedColors.setComparator(null);
        assertEquals(Arrays.asList(new Color[] { Color.RED, Color.GREEN, Color.BLUE,  }), sortedColors);
        clickColumnHeader(table, 0);
        assertEquals(Arrays.asList(new Color[] { Color.GREEN, Color.BLUE, Color.RED }), sortedColors);
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
}