/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.beans;

//import java.util.*;
import ca.odell.glazedlists.gui.TableFormat;

/**
 * TableFormat implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanTableFormat implements TableFormat {

    /** Java Beans property names */
    protected String[] propertyNames;

    /** methods for extracting field values */
    protected BeanProperty[] beanProperties = null;

    /** column labels are pretty-print column header labels */
    protected String[] columnLabels;

    /**
     * Create a BeanTableFormat that uses the specified column names
     * and the specified field names.
     */
    public BeanTableFormat(String[] propertyNames, String[] columnLabels) {
        this.propertyNames = propertyNames;
        this.columnLabels = columnLabels;
    }

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

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    protected void loadPropertyDescriptors(Object beanObject) {
        Class beanClass = beanObject.getClass();
        beanProperties = new BeanProperty[propertyNames.length];
        for(int p = 0; p < propertyNames.length; p++) {
            beanProperties[p] = new BeanProperty(beanClass, propertyNames[p], true, false);
        }
    }
}
