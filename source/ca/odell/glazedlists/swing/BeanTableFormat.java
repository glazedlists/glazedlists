/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

import java.util.*;
// for using beans' reflection on property names
import java.beans.*;
import java.lang.reflect.*;

/**
 * TableFormat implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class BeanTableFormat implements WritableTableFormat {

    /** Java Beans property names */
    private List propertyNames;
    
    /** methods for extracting field values */
    private List propertyDescriptors = null;
    
    /** column labels are pretty-print column header labels */
    private List columnLabels;
    
    /** whether all columns can be edited */
    private boolean[] editable;
    
    /**
     * Create a BeanTableFormat that uses the specified column names
     * and the specified field names.
     */
    public BeanTableFormat(String[] propertyNames, String[] columnLabels, boolean[] editable) {
        this.propertyNames = Arrays.asList(propertyNames);
        this.columnLabels = Arrays.asList(columnLabels);
        this.editable = editable;
    }
    
    /**
     * The number of columns to display.
     */
    public int getColumnCount() {
        return columnLabels.size();
    }

    /**
     * Gets the title of the specified column. 
     */
    public String getColumnName(int column) {
        return (String)columnLabels.get(column);
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
        if(propertyDescriptors == null) loadPropertyDescriptors(baseObject);

        // get the property
        try {
            PropertyDescriptor property = (PropertyDescriptor)propertyDescriptors.get(column);
            Method getter = property.getReadMethod();
            if(getter == null) throw new IllegalStateException("Bean property " + property + " not readable");
            return getter.invoke(baseObject, null);
        } catch(InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(Object beanObject) {
        try {
            Class beanClass = beanObject.getClass();
            propertyDescriptors = new ArrayList();
            for(Iterator i = propertyNames.iterator(); i.hasNext(); ) {
                propertyDescriptors.add(new PropertyDescriptor((String)i.next(), beanClass));
            }
        } catch(IntrospectionException e) {
            throw new RuntimeException(e);
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
        // if not editable at all
        if(!editable[column]) return false;
        
        // if there is no setter
        PropertyDescriptor property = (PropertyDescriptor)propertyDescriptors.get(column);
        Method setter = property.getWriteMethod();
        if(setter == null) return false;
        
        // it must be writable
        return true;
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
        if(propertyDescriptors == null) loadPropertyDescriptors(baseObject);

        // set the property
        try {
            PropertyDescriptor property = (PropertyDescriptor)propertyDescriptors.get(column);
            Method setter = property.getWriteMethod();
            setter.invoke(baseObject, new Object[] { editedValue });
        } catch(InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch(IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        
        // return the modified result
        return baseObject;
    }
}
