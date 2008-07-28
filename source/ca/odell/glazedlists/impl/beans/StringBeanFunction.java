/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.FunctionList;

/**
 * A {@link FunctionList.Function} that uses a {@link BeanProperty} to extract
 * a raw result and then formats that result as a String.
 *
 * @author James Lemieux
 */
public class StringBeanFunction<E> implements FunctionList.Function<E,String> {

    /** The {@link BeanProperty} that is capable of extracting the function's raw value from source objects. */
    private final BeanProperty<E> property;

    /**
     * Create a new JavaBean property function that produces its value by
     * extracting a property from source objects. This should be accessed from the
     * {@link ca.odell.glazedlists.GlazedLists GlazedLists} tool factory.
     */
    public StringBeanFunction(Class<E> beanClass, String propertyName) {
        this.property = new BeanProperty<E>(beanClass, propertyName, true, false);
    }

    /** @inheritDoc */
    public String evaluate(E sourceValue) {
        final Object rawValue = property.get(sourceValue);
        return rawValue == null ? null : String.valueOf(rawValue);
    }
}