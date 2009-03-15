/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import ca.odell.glazedlists.impl.beans.BeanProperty;

import java.util.Comparator;

/**
 * A {@link Comparator} that uses Reflection to compare two instances
 * of the specified {@link Class} by a JavaBean property.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanPropertyComparator<T> implements Comparator<T> {

    /** the comparator to use on the JavaBean property */
    private Comparator propertyComparator;

    /** the accessor for the JavaBean property */
    private BeanProperty beanProperty = null;

    /**
     * Create a new JavaBean property comparator that compares properties using
     * the provided {@link Comparator}.  This should be accessed from the
     * {@link ca.odell.glazedlists.GlazedLists GlazedLists} tool factory.
     */
    public BeanPropertyComparator(Class<T> className, String property, Comparator propertyComparator) {
        beanProperty = new BeanProperty<T>(className, property, true, false);
        this.propertyComparator = propertyComparator;
    }

    /**
     * Compares the specified objects by the JavaBean property.
     */
    public int compare(T alpha, T beta) {
        // Inspect alpha
        Object alphaProperty = null;
        if(alpha != null) alphaProperty = beanProperty.get(alpha);

        // Inspect beta
        Object betaProperty = null;
        if(beta != null) betaProperty = beanProperty.get(beta);

        // Compare the property values
        return propertyComparator.compare(alphaProperty, betaProperty);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final BeanPropertyComparator that = (BeanPropertyComparator) o;

        if(!beanProperty.equals(that.beanProperty)) return false;
        if(!propertyComparator.equals(that.propertyComparator)) return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result;
        result = propertyComparator.hashCode();
        result = 29 * result + beanProperty.hashCode();
        return result;
    }
}