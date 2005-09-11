/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.matchers.*;

/**
 * A {@link Matcher} which uses a {@link BeanProperty} to read a bean property
 * from a given bean and check it for equality with a given value.
 * <code>null</code> property values are allowed.
 *
 * @author James Lemieux
 */
public final class BeanPropertyMatcher implements Matcher {

    /** The BeanProperty containing logic for extracting the property value from an item. */
    private final BeanProperty beanProperty;

    /** The value with which to compare the bean property. */
    private final Object value;

    public BeanPropertyMatcher(BeanProperty beanProperty, Object value) {
        if (beanProperty == null)
            throw new IllegalArgumentException("beanProperty may not be null");
        
        this.beanProperty = beanProperty;
        this.value = value;
    }

    public boolean matches(Object item) {
        return GlazedListsImpl.equal(this.beanProperty.get(item), this.value);
    }
}