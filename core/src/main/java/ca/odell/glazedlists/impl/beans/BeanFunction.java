/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.FunctionList;

/**
 * A {@link FunctionList.Function} that uses a {@link BeanProperty} to produce
 * the result of the function.
 *
 * @author James Lemieux
 */
public class BeanFunction<E,V> implements FunctionList.Function<E,V> {

    /** The {@link BeanProperty} that is capable of extracting the function's value from source objects. */
    private final BeanProperty<E> property;

    /**
     * Create a new JavaBean property function that produces its value by
     * extracting a property from source objects. This should be accessed from the
     * {@link ca.odell.glazedlists.GlazedLists GlazedLists} tool factory.
     */
    public BeanFunction(Class<E> beanClass, String propertyName) {
        this.property = new BeanProperty<E>(beanClass, propertyName, true, false);
    }

    /** @inheritDoc */
    @Override
    public V evaluate(E sourceValue) {
        return (V) property.get(sourceValue);
    }
}