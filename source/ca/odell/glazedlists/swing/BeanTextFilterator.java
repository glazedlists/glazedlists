/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swing;

import java.util.*;
import ca.odell.glazedlists.TextFilterator;
// for using beans' reflection on property names
import java.beans.*;
import java.lang.reflect.*;

/**
 * TextFilterator implementation that uses reflection to be used for any
 * JavaBean-like Object with getProperty() and setProperty() style API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class BeanTextFilterator implements TextFilterator {

    /** Java Beans property names */
    private List propertyNames;
    
    /** methods for extracting field values */
    private List propertyDescriptors = null;
    
    /**
     * Create a BeanTextFilterator that uses the specified property names.
     */
    public BeanTextFilterator(String[] propertyNames) {
        this.propertyNames = Arrays.asList(propertyNames);
    }
    
    /**
     * Gets the specified object as a list of Strings. These Strings should contain
     * all object information so that it can be compared to the filter set.
     */
    public void getFilterStrings(List baseList, Object element) {
        if(element == null) return;
        
        // load the property descriptors on first request
        if(propertyDescriptors == null) loadPropertyDescriptors(element);
        
        // get the filter strings
        try {
            for(Iterator i = propertyDescriptors.iterator(); i.hasNext(); ) {
                PropertyDescriptor property = (PropertyDescriptor)i.next();
                Method getter = property.getReadMethod();
                if(getter == null) throw new IllegalStateException("Bean property " + property + " not readable");
                Object propertyValue = getter.invoke(element, null);
                if(propertyValue == null) continue;
                baseList.add(propertyValue.toString());
            }
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
}
