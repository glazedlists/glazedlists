/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

// to implement the ThresholdEvaluator interface
import ca.odell.glazedlists.ThresholdList;

/**
 * A ThresholdEvaluator that is powered by JavaBeans and Reflection.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanThresholdEvaluator<E> implements ThresholdList.Evaluator<E> {

    private String propertyName = null;

    private BeanProperty<E> beanProperty = null;

    public BeanThresholdEvaluator(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns an integer value for an Object to be used to
     * compare that object against a threshold.  This value is
     * not relative to any other object unlike a <code>Comparator</code>.
     */
    public int evaluate(E object) {
        if(beanProperty == null) loadPropertyDescriptors(object);
        Object property = beanProperty.get(object);
        return ((Integer)property).intValue();
    }

    /**
     * Loads the property descriptors which are used to invoke property
     * access methods using the property names.
     */
    private void loadPropertyDescriptors(Object beanObject) {
        Class<E> beanClass = (Class<E>) beanObject.getClass();
        beanProperty = new BeanProperty<E>(beanClass, propertyName, true, false);
    }
}