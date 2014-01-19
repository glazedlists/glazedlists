/* Glazed Lists                                                 (c) 2003-2007 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <code>PropertyEventNameMatcher</code> matches {@link java.beans.PropertyChangeEvent}s by property name.
 * One or more property names to match or filter against may be given. The concrete behaviour of
 * a PropertyEventNameMatcher depends on the {@link #matchPropertyNames} property. If you want to
 * match property change events against a known set of property names, use a value of <code>true</code>
 * for the #matchPropertyNames} property. Alternatively, when you specify <code>false</code>,
 * the specified property names will serve as an exclude list, e.g. if an event matches a specified
 * property name, it will be filtered out.
 * 
 * @see #isMatchPropertyNames()
 * 
 * @author Holger Brands
 */
public final class PropertyEventNameMatcher implements Matcher<PropertyChangeEvent> {

    /** Property names to consider. */
    private final Set<String> propertyNames = new HashSet<String>();

    /**
     * Specifies how to use the {@link #propertyNames} to match property change events.
     * 
     * @see #isMatchPropertyNames()
     */
    private boolean matchPropertyNames;
   
    /**
     * Creates a PropertyEventNameMatcher.
     * 
     * @param matchPropertyNames if <code>true</code> the property names are used to match events
     *        by name, if <code>false</code> they are used to filter events
     * @param properties the property names to consider
     * 
     * @see #isMatchPropertyNames()
     */
    public PropertyEventNameMatcher(boolean matchPropertyNames, String... properties) {
        if (properties == null) throw new IllegalArgumentException("Array of property names may not be null");
        this.matchPropertyNames = matchPropertyNames;
        for (int i = 0, n = properties.length; i < n; i++) {
            propertyNames.add(properties[i]);
        }
    }
    
    /**
     * Creates a PropertyEventNameMatcher.
     * 
     * @param matchPropertyNames if <code>true</code> the property names are used to match
     *        events by name, if <code>false</code> they are used to filter events
     * @param properties the property names to consider
     * 
     * @see #isMatchPropertyNames()
     */    
    public PropertyEventNameMatcher(boolean matchPropertyNames, Collection<String> properties) {
        if (properties == null) throw new IllegalArgumentException("Collection of property names may not be null");
        this.matchPropertyNames = matchPropertyNames;
        propertyNames.addAll(properties);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean matches(PropertyChangeEvent event) {
        final boolean containsProperty = propertyNames.contains(event.getPropertyName());
        return matchPropertyNames ? containsProperty : !containsProperty;
    }

    /**
     * Determines how to use the {@link #propertyNames} to match the property change events. If
     * <code>true</code>, the specified property names serve as a positive list, e.g. if the
     * property name of an event is contained in the {@link #propertyNames}, the event is matched.
     * If <code>false</code>, the specified property names serve as a negative list, e.g. if the
     * property name of an event is contained in the {@link #propertyNames}, the event is
     * <strong>not</strong> matched.
     */
    public boolean isMatchPropertyNames() {
        return matchPropertyNames;
    }
}
