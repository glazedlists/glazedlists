/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.beans;

//import java.util.*;
import ca.odell.glazedlists.gui.WritableTableFormat;

/**
 * A WritableTableFormat implementation that uses Reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class BeanWritableTableFormat extends BeanTableFormat implements WritableTableFormat {

    /** whether all columns can be edited */
    private boolean[] editable;

    /**
     * Create a BeanWritableTableFormat that uses the specified column names
     * and the specified field names while offering editable columns.
     */
    public BeanWritableTableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
        super(propertyNames, columnLabels);
        this.editable = editable;
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