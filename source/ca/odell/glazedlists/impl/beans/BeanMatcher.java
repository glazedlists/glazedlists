/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.beans;

import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.impl.GlazedListsImpl;

/**
 * Matcher that uses reflection to be used for any JavaBean-like
 * Object with getProperty() or isProperty() style API.
 *
 * <p>Anything that doesn't equal the match value will not
 * be matched.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class BeanMatcher<E> implements Matcher<E> {

    /** the property to pull the expected value from */
    private final BeanProperty<E> property;

    /** the match value of the target property */
    private final Object matchValue;

    /**
     * Create a new {@link Matcher} that matches whenever the
     * value of the specified property equals the specified value.
     */
    public BeanMatcher(Class<E> beanClass, String propertyName, Object matchValue) {
        this.property = new BeanProperty<E>(beanClass, propertyName, true, false);
        this.matchValue = matchValue;
    }

    /** {@inheritDoc} */
    public boolean matches(E item) {
        if(item == null) return false;

        Object propertyValue = property.get(item);
        return GlazedListsImpl.equal(propertyValue, matchValue);
    }
}