/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

import junit.framework.TestCase;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * Verify that the {@link AbstractTableComparatorChooser} works, especially
 * with respect to handling state dumps as a String.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TableComparatorTest extends TestCase {

    /** a table comparator choosers to test with */
    private SortedList sortedList = new SortedList(new BasicEventList());
    private AbstractTableComparatorChooser tableComparatorChooser = new TestTableComparatorChooser(sortedList, 10, true);

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
        assertFromAndToString("1");
        assertFromAndToString("1 reversed");
        assertFromAndToString("1 comparator 3");
        assertFromAndToString("1 comparator 3 reversed");
        assertFromAndToString("1 reversed", "1 reversed, 1 comparator 2");
        assertFromAndToString("1", "  1  ");
        assertFromAndToString("1", " ,, 1  ");
        assertFromAndToString("1", ", 1  ");
        assertFromAndToString("1", "1, ");
        assertFromAndToString("1", "1,");
        assertFromAndToString("1 comparator 3 reversed, 2 reversed");
        assertFromAndToString("1 reversed", "1 REVERSED");
        assertFromAndToString("1 comparator 1 reversed", "1 COMPARATOR 1 REVERSED");
        assertFromAndToString("1 comparator 1", "1 COMPARATOR 1");
        assertFromAndToString("", "1 comparator 4"); // only 3 comparators on column 1
        assertFromAndToString("", "2 comparator 1"); // only 1 comparator on column 2
        assertFromAndToString("5 reversed", "2 comparator 1, 5 reversed"); // only 1 comparator on column 2
        assertFromAndToString("", "10"); // only 10 columns
        assertFromAndToString("0", "0");

        assertParseFails("1reversed");
        assertParseFails("1 reversed1");
        assertParseFails("reversed");
        assertParseFails("comparator");
        assertParseFails("comparator reversed");
        assertParseFails("1 comparator1");
        assertParseFails("1 reversed comparator");
        assertParseFails("1 reversed comparator 3");
        assertParseFails("1 comparator");
        assertParseFails("1 comparator reversed");
        assertParseFails("1.0");
        assertParseFails("1.0 reversed");
        assertParseFails("-1");
        assertParseFails("1 2");
        assertParseFails("1 reversed 2");
        assertParseFails("reversed 2");
        assertParseFails("1 comparator -1");
        assertParseFails("1 comparator five");
        assertParseFails("1 comparator comparator");
        assertParseFails("1 reversed reversed");
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
        protected TestTableComparatorChooser(SortedList sortedList, int columns, boolean multipleColumnSort) {
            super(sortedList, new TestTableFormat(columns), true);
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