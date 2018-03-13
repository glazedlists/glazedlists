/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.Objects;

/**
 * A {@link Matcher} which uses a {@link BeanProperty} to read a bean property
 * from a given bean and check it for equality with a given value.
 * <code>null</code> property values are allowed.
 *
 * @author James Lemieux
 */
public final class BeanPropertyMatcher<E> implements Matcher<E> {

    /** The BeanProperty containing logic for extracting the property value from an item. */
    private final BeanProperty<E> beanProperty;

    /** The value with which to compare the bean property. */
    private final Object value;

    /**
     * Create a new {@link Matcher} that matches whenever the given property
     * equals the given <code>value</code>.
     */
    public BeanPropertyMatcher(Class<E> beanClass, String propertyName, Object value) {
        this.beanProperty = new BeanProperty<>(beanClass, propertyName, true, false);
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(E item) {
        if (item == null) return false;
        return Objects.equals(this.beanProperty.get(item), this.value);
    }
}