/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import javax.swing.*;
import java.awt.*;

/**
 * Test EventTableModel from the Swing thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class EventTableModelTest extends SwingTestCase {

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
        // create a list whose get() method pauses for 100 ms before returning the value
        final EventList<Integer> integers = new SlowReadList<Integer>(new BasicEventList<Integer>(), 100);
        integers.add(new Integer(0));
        integers.add(new Integer(1));
        integers.add(new Integer(2));
        integers.add(new Integer(3));
        integers.add(new Integer(5));

        // start a thread which adds new Integers every 50 ms
        new Thread(new ListAddRunnable(integers, 1000, 50)).start();

        // make sure the ListAddRunnable has started
        Thread.sleep(200);

        // at this point we have created an environment where writes are
        // happening twice as fast as reads (50 ms between reads, 100 ms between writes),
        // thus if our locking code is not correct when we try initializing an
        // EventTableModel, we'll get an assertion failure in SlowReadList.get(), which
        // records the size of the list on the first call to SlowReadList.get() and then
        // rechecks to that size on every subsequent call to SlowReadList.get(). With the
        // read lock held in the constructor of EventTableModel, the size of SlowReadList
        // should remain consistent through the construction of EventTableModel.
        final String[] properties = {"class"};
        new EventTableModel(integers, GlazedLists.tableFormat(properties, properties));
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

    private static class SlowReadList<S> extends TransformedList<S,S> {
        private int sizeAtFirstRead;
        private final long pause;

        public SlowReadList(EventList<S> source, long pause) {
            super(source);
            source.addListEventListener(this);
            this.pause = pause;
        }

        public void listChanged(ListEvent<S> listChanges) {
            // nothing to do - this is just a test class
        }

        protected boolean isWritable() {
            return true;
        }

        public S get(int index) {
            if (sizeAtFirstRead == 0)
                sizeAtFirstRead = this.size();
            else
                assertEquals(sizeAtFirstRead, this.size());

            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {}
            return super.get(index);
        }
    }

    private static final class ListAddRunnable implements Runnable {
        private final EventList<Integer> list;
        private final long endTime;
        private final long pause;

        public ListAddRunnable(EventList<Integer> list, long duration, long pause) {
            this.list = list;
            this.endTime = System.currentTimeMillis() + duration;
            this.pause = pause;
        }

        public void run() {
            while (System.currentTimeMillis() < this.endTime) {
                // acquire the write lock and add a new element
                this.list.getReadWriteLock().writeLock().lock();
                this.list.add(null);
                this.list.getReadWriteLock().writeLock().unlock();

                // pause before adding another element
                try {
                    Thread.sleep(this.pause);
                } catch (InterruptedException e) {}
            }
        }
    }
}