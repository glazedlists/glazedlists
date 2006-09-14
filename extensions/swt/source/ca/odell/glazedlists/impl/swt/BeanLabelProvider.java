/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swt;

// beans
import ca.odell.glazedlists.impl.beans.BeanProperty;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * A LabelProvider that uses Reflection on JavaBeans to provide a label
 * for an Object.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanLabelProvider<E> extends LabelProvider {

    /** The name of the JavaBean property to use */
    private String propertyName = null;

    /** The easy way to work with JavaBean-like object properties */
    private BeanProperty<E> beanProperty = null;

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
        if(beanProperty == null) loadPropertyDescriptors((E)object);
        Object property = beanProperty.get((E)object);
        return property.toString();
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(E beanObject) {
        Class beanClass = beanObject.getClass();
        beanProperty = new BeanProperty<E>(beanClass, propertyName, true, false);
    }
}