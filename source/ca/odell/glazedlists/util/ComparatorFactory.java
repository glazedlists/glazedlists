/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util;

// for Comparators and Lists
import java.util.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.sort.*;


/**
 * A factory for creating some commonly used types of {@link Comparator}s.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class ComparatorFactory {

    /** Provide Singleton access for all Comparators with no internal state */
    private static Comparator booleanComparator = null;
    private static Comparator comparableComparator = null;
    private static Comparator reversedComparable = null;

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private ComparatorFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property must implement {@link Comparable}.
     */
    public static Comparator beanProperty(Class className, String property) {
        return beanProperty(className, property, comparable());
    }

    /**
     * Creates a {@link Comparator} that uses Reflection to compare two instances
     * of the specified {@link Class} by the given JavaBean property.  The JavaBean
     * property is compared using the provided {@link Comparator}.
     */
    public static Comparator beanProperty(Class className, String property, Comparator propertyComparator) {
        return new BeanPropertyComparator(className, property, propertyComparator);
    }

    /**
     * Creates a {@link Comparator} for use with {@link Boolean} objects.
     */
    public static Comparator booleanObject() {
        if(booleanComparator == null) booleanComparator = new BooleanComparator();
        return booleanComparator;
    }

    /**
     * Creates a {@link Comparator} that compares {@link String} objects in
     * a case-insensitive way.  This {@link Comparator} is equivalent to using
     * {@link String#CASE_INSENSITIVE_ORDER} and exists here for convenience.
     */
    public static Comparator caseInsensitive() {
        return String.CASE_INSENSITIVE_ORDER;
    }

    /**
     * Creates a chain of {@link Comparator}s that applies the provided
     * {@link Comparator}s in the sequence specified until differences or
     * absoulute equality.is determined.
     */
    public static Comparator chain(List comparators) {
        return new ComparatorChain(comparators);
    }

    /**
     * Creates a {@link Comparator} that compares {@link Comparable} objects.
     */
    public static Comparator comparable() {
        if(comparableComparator == null) comparableComparator = new ComparableComparator();
        return comparableComparator;
    }

    /**
     * Creates a reverse {@link Comparator} that works for {@link Comparable} objects.
     */
    public static Comparator reverse() {
        if(reversedComparable == null) reversedComparable = reverse(comparable());
        return reversedComparable;
    }

    /**
     * Creates a reverse {@link Comparator} that inverts the given {@link Comparator}.
     */
    public static Comparator reverse(Comparator forward) {
        return new ReverseComparator(forward);
    }
}