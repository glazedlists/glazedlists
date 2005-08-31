/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// to work with Comparators
import java.util.Comparator;
import ca.odell.glazedlists.GlazedLists;
// to work with TableFormats
import ca.odell.glazedlists.gui.TableFormat;

/**
 * A comparator that sorts a table by the column that was clicked.
 */
public class TableColumnComparator<E> implements Comparator<E> {

    /** the table format knows to map objects to their fields */
    private TableFormat<E> tableFormat;

    /** the field of interest */
    private int column;

    /** comparison is delegated to a ComparableComparator */
    private Comparator comparator = null;

    /**
     * Creates a new TableColumnComparator that sorts objects by the specified
     * column using the specified table format.
     */
    public TableColumnComparator(TableFormat<E> tableFormat, int column) {
        this(tableFormat, column, GlazedLists.comparableComparator());
    }

    /**
     * Creates a new TableColumnComparator that sorts objects by the specified
     * column using the specified table format and the specified comparator.
     */
    public TableColumnComparator(TableFormat<E> tableFormat, int column, Comparator comparator) {
        this.column = column;
        this.tableFormat = tableFormat;
        this.comparator = comparator;
    }

    /**
     * Compares the two objects, returning a result based on how they compare.
     */
    public int compare(E alpha, E beta) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TableColumnComparator that = (TableColumnComparator) o;

        if (column != that.column) return false;
        if (!comparator.equals(that.comparator)) return false;
        if (!tableFormat.equals(that.tableFormat)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = tableFormat.hashCode();
        result = 29 * result + column;
        result = 29 * result + comparator.hashCode();
        return result;
    }
}