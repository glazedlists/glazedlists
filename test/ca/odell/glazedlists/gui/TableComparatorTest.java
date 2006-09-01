/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import junit.framework.TestCase;

/**
 * Verify that the {@link AbstractTableComparatorChooser} works, especially
 * with respect to handling state dumps as a String.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TableComparatorTest extends TestCase {

    /** a table comparator choosers to test with */
    private SortedList sortedList = new SortedList(new BasicEventList());
    private AbstractTableComparatorChooser tableComparatorChooser = new TestTableComparatorChooser(sortedList, 10);

    protected void setUp() throws Exception {
        // add some comparators to column 3
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.caseInsensitiveComparator());
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.comparableComparator());
        tableComparatorChooser.getComparatorsForColumn(1).add(GlazedLists.reverseComparator());
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

    private static class TestTableComparatorChooser extends AbstractTableComparatorChooser {
        protected TestTableComparatorChooser(SortedList sortedList, int columns) {
            super(sortedList, new TestTableFormat(columns));
        }
    }
    private static class TestTableFormat implements TableFormat {
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
        public Object getColumnValue(Object baseObject, int column) {
            return "Row " + baseObject + ", Column " + column;
        }
    }
}