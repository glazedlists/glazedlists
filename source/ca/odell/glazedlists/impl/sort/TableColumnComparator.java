/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.sort;

// to work with Comparators
import java.util.Comparator;
import ca.odell.glazedlists.GlazedLists;
// to work with TableFormats
import ca.odell.glazedlists.gui.TableFormat;

/**
 * A comparator that sorts a table by the column that was clicked.
 */
public class TableColumnComparator implements Comparator {

    /** the table format knows to map objects to their fields */
    private TableFormat tableFormat;

    /** the field of interest */
    private int column;

    /** comparison is delegated to a ComparableComparator */
    private Comparator comparator = null;

    /**
     * Creates a new TableColumnComparator that sorts objects by the specified
     * column using the specified table format.
     */
    public TableColumnComparator(TableFormat tableFormat, int column) {
        this(tableFormat, column, GlazedLists.comparableComparator());
    }

    /**
     * Creates a new TableColumnComparator that sorts objects by the specified
     * column using the specified table format and the specified comparator.
     */
    public TableColumnComparator(TableFormat tableFormat, int column, Comparator comparator) {
        this.column = column;
        this.tableFormat = tableFormat;
        this.comparator = comparator;
    }

    /**
     * Compares the two objects, returning a result based on how they compare.
     */
    public int compare(Object alpha, Object beta) {
        Object alphaField = tableFormat.getColumnValue(alpha, column);
        Object betaField = tableFormat.getColumnValue(beta, column);
        try {
            return comparator.compare(alphaField, betaField);
        // throw a 'nicer' exception if the class does not implement Comparable
        } catch(ClassCastException e) {
            IllegalStateException illegalStateException = null;
            if(comparator == GlazedLists.comparableComparator()) {
                illegalStateException = new IllegalStateException("TableComparatorChooser can not sort objects \"" + alphaField + "\", \"" + betaField + "\" that do not implement Comparable.");
            } else {
                illegalStateException = new IllegalStateException("TableComparatorChooser can not sort objects \"" + alphaField + "\", \"" + betaField + "\" using the provided Comparator.");
            }
            illegalStateException.initCause(e);
            throw illegalStateException;
        }
    }

    /**
     * Test if this TableColumnComparator is equal to the other specified
     * TableColumnComparator.
     */
    public boolean equals(Object other) {
        if(!(other instanceof TableColumnComparator)) return false;

        TableColumnComparator otherTableColumnComparator = (TableColumnComparator)other;
        if(!otherTableColumnComparator.tableFormat.equals(tableFormat)) return false;
        if(otherTableColumnComparator.column != column) return false;
        if(!comparator.equals(otherTableColumnComparator.comparator)) return false;

        return true;
    }
}