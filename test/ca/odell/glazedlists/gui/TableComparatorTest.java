/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseEvent;

/**
 * Verify that the {@link AbstractTableComparatorChooser} works, especially
 * with respect to handling state dumps as a String.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TableComparatorTest extends TestCase {

    /** a table comparator choosers to test with */
    private SortedList<String> sortedList;
    private AbstractTableComparatorChooser<String> tableComparatorChooser;

    protected void setUp() {
        sortedList = SortedList.create(new BasicEventList<String>());
        tableComparatorChooser = new TestTableComparatorChooser(sortedList, 10);

        // add some comparators to column 2
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.caseInsensitiveComparator());
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.comparableComparator());
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.reverseComparator());
    }

    protected void tearDown() {
        tableComparatorChooser.dispose();
        sortedList.dispose();

        tableComparatorChooser = null;
        sortedList = null;
    }

    /**
     * Test the parsing behind a table comparator.
     */
    public void testParsing() {
        assertFromAndToString("");
        assertFromAndToString("column 1");
        assertFromAndToString("column 1 reversed");
        assertFromAndToString("column 1 comparator 3");
        assertFromAndToString("column 1 comparator 3 reversed");
        assertFromAndToString("column 1 reversed", "column 1 reversed, column 1 comparator 2");
        assertFromAndToString("column 1", "  column 1  ");
        assertFromAndToString("column 1", " ,, column 1  ");
        assertFromAndToString("column 1", ", column 1  ");
        assertFromAndToString("column 1", "column 1, ");
        assertFromAndToString("column 1", "column 1,");
        assertFromAndToString("column 1 comparator 3 reversed, column 2 reversed");
        assertFromAndToString("column 1 reversed", "column 1 REVERSED");
        assertFromAndToString("column 1 comparator 1 reversed", "column 1 COMPARATOR 1 REVERSED");
        assertFromAndToString("column 1 comparator 1", "column 1 COMPARATOR 1");
        assertFromAndToString("", "column 1 comparator 4"); // only 3 comparators on column 1
        assertFromAndToString("", "column 2 comparator 1"); // only 1 comparator on column 2
        assertFromAndToString("column 5 reversed", "column 2 comparator 1, column 5 reversed"); // only 1 comparator on column 2
        assertFromAndToString("", "column 10"); // only 10 columns
        assertFromAndToString("column 0", "column 0");

        assertParseFails("column 1reversed");
        assertParseFails("column 1 reversed1");
        assertParseFails("column reversed");
        assertParseFails("column comparator");
        assertParseFails("column comparator reversed");
        assertParseFails("column 1 comparator1");
        assertParseFails("column 1 reversed comparator");
        assertParseFails("column 1 reversed comparator 3");
        assertParseFails("column 1 comparator");
        assertParseFails("column 1 comparator reversed");
        assertParseFails("column 1.0");
        assertParseFails("column 1.0 reversed");
        assertParseFails("column -1");
        assertParseFails("column 1 2");
        assertParseFails("column 1 reversed 2");
        assertParseFails("column reversed 2");
        assertParseFails("column 1 comparator -1");
        assertParseFails("column 1 comparator five");
        assertParseFails("column 1 comparator comparator");
        assertParseFails("column 1 reversed reversed");
        assertParseFails("column");
        assertParseFails("column1");
        assertParseFails("1");
        assertParseFails("1 1");
    }

    public void assertFromAndToString(String toStringExpected, String fromString) {
        tableComparatorChooser.fromString(fromString);
        String toStringResult = tableComparatorChooser.toString();
        assertEquals(toStringExpected, toStringResult);
    }
    public void assertFromAndToString(String fromAndToString) {
        assertFromAndToString(fromAndToString, fromAndToString);
    }
    public void assertParseFails(String input) {
        try {
            tableComparatorChooser.fromString(input);
            fail("Accepted invalid input \"" + input + "\"");
        } catch(IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test that TableComparatorChooser handles the swapping of
     * EventTableModels behind the JTable.
     */
    public void guiTestTableModelStructureChanged() {
        final EventList<String> source = GlazedLists.eventListOf(new String[] {"James", "Jodie", "Jesse"});
        final SortedList<String> sorted = new SortedList<String>(source, null);

        final JTable table = new JTable();
        final EventTableModel<String> firstModel;
        final EventTableModel<String> secondModel;

        final int initialNumPropertyChangeListenersOnJTable = table.getPropertyChangeListeners().length;

        // 1. set up an EventTableModel to display 3 columns
        firstModel = new EventTableModel<String>(sorted, new TestTableFormat(3));
        table.setModel(firstModel);

        assertEquals(1, firstModel.getTableModelListeners().length);
        assertEquals(3, table.getColumnCount());


        // 2. wire up a TableComparatorChooser and make the third column sort
        TableComparatorChooser chooser = TableComparatorChooser.install(table, sorted, AbstractTableComparatorChooser.SINGLE_COLUMN);
        chooser.appendComparator(2, 0, false);

        // the TableComparatorChooser should install a PropertyChangeListener on the JTable
        assertEquals(initialNumPropertyChangeListenersOnJTable+1, table.getPropertyChangeListeners().length);
        // the TableComparatorChooser should install a TableModelListener on the EventTableModel
        assertEquals(2, firstModel.getTableModelListeners().length);

        // this assertion proves that the TableComparatorChooser cleared its sorting state
        assertEquals(1, chooser.getSortingColumns().size());


        // 3. changing the TableFormat within the existing EventTableModel should clear the sorting state
        firstModel.setTableFormat(new TestTableFormat(1));

        // this assertion proves that the JTable has rebuilt the TableColumnModel
        assertEquals(1, table.getColumnCount());
        // this assertion proves that the TableComparatorChooser cleared its sorting state
        assertTrue(chooser.getSortingColumns().isEmpty());
        assertEquals(2, firstModel.getTableModelListeners().length);


        // 4. reapply a sort to the JTable on the first column
        chooser.appendComparator(0, 0, false);
        assertEquals(1, chooser.getSortingColumns().size());


        // 5. set a new EventTableModel that only uses 2 columns
        secondModel = new EventTableModel<String>(sorted, new TestTableFormat(2));
        table.setModel(secondModel);

        // nothing should listen to firstModel, and secondModel is now active
        assertEquals(0, firstModel.getTableModelListeners().length);
        assertEquals(2, secondModel.getTableModelListeners().length);

        // this assertion proves that the JTable has rebuilt the TableColumnModel
        assertEquals(2, table.getColumnCount());
        // this assertion proves that the TableComparatorChooser cleared its sorting state
        assertTrue(chooser.getSortingColumns().isEmpty());


        // 6. disposing of the TableComparatorChooser should remove all listeners
        assertEquals(initialNumPropertyChangeListenersOnJTable+1, table.getPropertyChangeListeners().length);
        chooser.dispose();
        assertEquals(initialNumPropertyChangeListenersOnJTable, table.getPropertyChangeListeners().length);
        assertEquals(0, firstModel.getTableModelListeners().length);
        assertEquals(1, secondModel.getTableModelListeners().length); // the JTable is still listening to the secondModel

        // replacing the JTable's model should remove the remaining listener from secondModel
        table.setModel(new DefaultTableModel());
        assertEquals(0, secondModel.getTableModelListeners().length);
    }

    public void testMouseOnlySortingStrategyWithUndo() {
        final TestTableFormat tableFormat = new TestTableFormat(10);
        final JTable table = new JTable(new EventTableModel<String>(new BasicEventList<String>(), tableFormat));
        tableComparatorChooser = TableComparatorChooser.install(table, sortedList, AbstractTableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO, tableFormat);

        // add some comparators to column 1 (for a total of 3 comparators)
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.caseInsensitiveComparator());
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.comparableComparator());

        assertEquals("", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1", tableComparatorChooser.toString());

        clickColumnHeader(table, 0);
        assertEquals("column 1, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1 reversed, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1 comparator 1, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1 comparator 1 reversed, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1 comparator 2, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 1);
        assertEquals("column 1 comparator 2 reversed, column 0", tableComparatorChooser.toString());

        clickColumnHeader(table, 0);
        assertEquals("column 1 comparator 2 reversed, column 0 reversed", tableComparatorChooser.toString());

        // click on the 1st column (secondary sort column) will simply toggle it from
        // reversed to normal order again
        clickColumnHeader(table, 0);
        assertEquals("column 1 comparator 2 reversed, column 0", tableComparatorChooser.toString());

        // clicking the 2nd column (primary sort column) one more time will invoke the special logic of
        // MULTIPLE_COLUMN_MOUSE_WITH_UNDO and will clear the entire sort
        clickColumnHeader(table, 1);
        assertEquals("", tableComparatorChooser.toString());

        // next click starts the sorting anew
        clickColumnHeader(table, 1);
        assertEquals("column 1", tableComparatorChooser.toString());
    }

    /**
     * A convenience method to simulate a mouse click on the table header
     * for the given table in the given column.
     */
    private static void clickColumnHeader(JTable table, int column) {
        final TableColumnModel columnModel = table.getColumnModel();

        // position the x coordinate half way through the column header in question
        int x = 0;
        for (int i = 0; i < column; i++)
            x += columnModel.getColumn(i).getWidth();

        x += columnModel.getColumn(column).getWidth() / 2;

        // position the y coordinate half may down the header
        int y = table.getTableHeader().getPreferredSize().height / 2;

        table.getTableHeader().dispatchEvent(new MouseEvent(table, MouseEvent.MOUSE_CLICKED, 0, 0, x, y, 1, false, MouseEvent.BUTTON1));
    }

    private static class TestTableComparatorChooser extends AbstractTableComparatorChooser<String> {
        protected TestTableComparatorChooser(SortedList<String> sortedList, int columns) {
            super(sortedList, new TestTableFormat(columns));
        }
    }

    private static class TestTableFormat implements TableFormat<String> {
        private int columns;

        public TestTableFormat(int columns) {
            this.columns = columns;
        }
        public int getColumnCount() {
            return columns;
        }
        public String getColumnName(int column) {
            return "Column " + column;
        }
        public Object getColumnValue(String baseObject, int column) {
            return "Row " + baseObject + ", Column " + column;
        }
    }
}