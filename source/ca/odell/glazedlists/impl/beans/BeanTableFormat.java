/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.beans;

//import java.util.*;
import ca.odell.glazedlists.gui.*;

/**
 * TableFormat implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanTableFormat implements TableFormat, WritableTableFormat {

    /** Java Beans property names */
    protected String[] propertyNames;

    /** methods for extracting field values */
    protected BeanProperty[] beanProperties = null;

    /** column labels are pretty-print column header labels */
    protected String[] columnLabels;

    /** whether all columns can be edited */
    private boolean[] editable;

    /**
     * Create a BeanTableFormat that uses the specified column names
     * and the specified field names while offering editable columns.
     */
    public BeanTableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
        this.propertyNames = propertyNames;
        this.columnLabels = columnLabels;
        this.editable = editable;
    }
    public BeanTableFormat(String[] propertyNames, String[] columnLabels) {
        this(propertyNames, columnLabels, new boolean[propertyNames.length]);
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    protected void loadPropertyDescriptors(Object beanObject) {
        Class beanClass = beanObject.getClass();
        beanProperties = new BeanProperty[propertyNames.length];
        for(int p = 0; p < propertyNames.length; p++) {
            beanProperties[p] = new BeanProperty(beanClass, propertyNames[p], true, editable[p]);
        }
    }

    // TableFormat // // // // // // // // // // // // // // // // // // // //

    /**
     * The number of columns to display.
     */
    public int getColumnCount() {
        return columnLabels.length;
    }

    /**
     * Gets the title of the specified column.
     */
    public String getColumnName(int column) {
        return columnLabels[column];
    }

    /**
     * Gets the value of the specified field for the specified object. This
     * is the value that will be passed to the editor and renderer for the
     * column. If you have defined a custom renderer, you may choose to return
     * simply the baseObject.
     */
    public Object getColumnValue(Object baseObject, int column) {
        if(baseObject == null) return null;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors(baseObject);

        // get the property
        return beanProperties[column].get(baseObject);
    }

    
    // WritableTableFormat // // // // // // // // // // // // // // // // // //

    /**
     * For editing fields. This returns true if the specified Object in the
     * specified column can be edited by the user.
     *
     * @param baseObject the Object to test as editable or not. This will be
     *      an element from the source list.
     * @param column the column to test.
     * @return true if the object and column are editable, false otherwise.
     * @since 2004-August-27, as a replacement for isColumnEditable(int).
     */
    public boolean isEditable(Object baseObject, int column) {
        return editable[column];
    }

    /**
     * Sets the specified field of the base object to the edited value. When
     * a column of a table is edited, this method is called so that the user
     * can specify how to modify the base object for each column.
     *
     * @param baseObject the Object to be edited. This will be the original
     *      Object from the source list.
     * @param editedValue the newly constructed value for the edited column
     * @param column the column which has been edited
     * @return the revised Object, or null if the revision shall be discarded.
     *      If not null, the EventTableModel will set() this revised value in
     *      the list and overwrite the previous value.
     */
    public Object setColumnValue(Object baseObject, Object editedValue, int column) {
        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors(baseObject);

        // set the property
        beanProperties[column].set(baseObject, editedValue);

        // return the modified result
        return baseObject;
    }
}
