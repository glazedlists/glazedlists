/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

// To work with Lists
import java.util.*;
// To implement the interface
import ca.odell.glazedlists.TextFilterator;

/**
 * TextFilterator implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanTextFilterator implements TextFilterator {

    /** Java Beans property names */
    private String[] propertyNames;

    /** methods for extracting field values */
    private BeanProperty[] beanProperties = null;

    /**
     * Create a BeanTextFilterator that uses the specified property names.
     */
    public BeanTextFilterator(String[] propertyNames) {
        this.propertyNames = propertyNames;
    }

    /**
     * Gets the specified object as a list of Strings. These Strings should contain
     * all object information so that it can be compared to the filter set.
     */
    public void getFilterStrings(List baseList, Object element) {
        if(element == null) return;

        // load the property descriptors on first request
        if(beanProperties == null) loadPropertyDescriptors(element);

        // get the filter strings
        for(int p = 0; p < beanProperties.length; p++) {
            Object propertyValue = beanProperties[p].get(element);
            if(propertyValue == null) continue;
            baseList.add(propertyValue.toString());
        }
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(Object beanObject) {
        Class beanClass = beanObject.getClass();
        beanProperties = new BeanProperty[propertyNames.length];
        for(int p = 0; p < propertyNames.length; p++) {
            beanProperties[p] = new BeanProperty(beanClass, propertyNames[p], true, false);
        }
    }
}
