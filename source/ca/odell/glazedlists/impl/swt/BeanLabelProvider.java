/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.swt;

// beans
import ca.odell.glazedlists.impl.beans.*;
// to implement the LabelProvider interface
import org.eclipse.jface.viewers.*;

/**
 * A LabelProvider that uses Reflection on JavaBeans to provide a label
 * for an Object.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanLabelProvider extends LabelProvider {

    /** The name of the JavaBean property to use */
    private String propertyName = null;

    /** The easy way to work with JavaBean-like object properties */
    private BeanProperty beanProperty = null;

    /**
     * Creates a new LabelProvider that uses the value of a JavaBean property
     * as the label for an Object.
     */
    public BeanLabelProvider(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets a label for the given Object from the JavaBean property.
     */
    public String getText(Object object) {
        if(beanProperty == null) loadPropertyDescriptors(object);
        Object property = beanProperty.get(object);
        return property.toString();
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(Object beanObject) {
        Class beanClass = beanObject.getClass();
        beanProperty = new BeanProperty(beanClass, propertyName, true, false);
    }
}