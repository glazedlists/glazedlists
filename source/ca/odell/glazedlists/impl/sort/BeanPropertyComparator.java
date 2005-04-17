/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;
// Bean reflection
import ca.odell.glazedlists.impl.beans.BeanProperty;

/**
 * A {@link Comparator} that uses Reflection to compare two instances
 * of the specified {@link Class} by a JavaBean property.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class BeanPropertyComparator implements Comparator {

    /** the comparator to use on the JavaBean property */
    private Comparator propertyComparator;

    /** the accessor for the JavaBean property */
    private BeanProperty beanProperty = null;

    /**
     * Create a new JavaBean property comparator that compares properties using
     * the provided {@link Comparator}.  This should be accessed from the
     * {@link ca.odell.glazedlists.GlazedLists GlazedLists} tool factory.
     */
    public BeanPropertyComparator(Class className, String property, Comparator propertyComparator) {
        beanProperty = new BeanProperty(className, property, true, false);
        this.propertyComparator = propertyComparator;
    }

    /**
     * Compares the specified objects by the JavaBean property.
     */
    public int compare(Object alpha, Object beta) {
        // Inspect alpha
        Object alphaProperty = null;
        if(alpha != null) alphaProperty = beanProperty.get(alpha);

        // Inspect beta
        Object betaProperty = null;
        if(beta != null) betaProperty = beanProperty.get(beta);

        // Compare the property values
        return propertyComparator.compare(alphaProperty, betaProperty);
    }

    /**
     * This is equal to another comparator if and only if they both
     * are BeanPropertyComparators and have equal property comparators.
     */
    public boolean equals(Object other) {
        if(!(other instanceof BeanPropertyComparator)) return false;
        BeanPropertyComparator otherBeanPropertyComparator = (BeanPropertyComparator)other;
        if(!beanProperty.equals(otherBeanPropertyComparator.beanProperty)) return false;
        if(!propertyComparator.equals(otherBeanPropertyComparator.propertyComparator)) return false;
        return true;
    }
}