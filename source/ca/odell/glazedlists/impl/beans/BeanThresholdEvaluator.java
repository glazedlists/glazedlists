/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.beans;

// to implement the ThresholdEvaluator interface
import ca.odell.glazedlists.ThresholdEvaluator;

/**
 * A ThresholdEvaluator that is powered by JavaBeans and Reflection.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanThresholdEvaluator implements ThresholdEvaluator {

    private String propertyName = null;

    private BeanProperty beanProperty = null;

    public BeanThresholdEvaluator(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns an integer value for an Object to be used to
     * compare that object against a threshold.  This value is
     * not relative to any other object unlike a <code>Comparator</code>.
     */
    public int evaluate(Object object) {
        if(beanProperty == null) loadPropertyDescriptors(object);
        Object property = beanProperty.get(object);
        return ((Integer)property).intValue();
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