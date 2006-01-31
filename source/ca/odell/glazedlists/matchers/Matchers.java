/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.*;
// for access to volatile classes
import ca.odell.glazedlists.impl.matchers.*;
import ca.odell.glazedlists.impl.beans.BeanMatcher;

import java.util.Collection;
import java.util.Iterator;

/**
 * A factory for creating Matchers.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class Matchers {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private Matchers() {
        throw new UnsupportedOperationException();
    }

    // Matchers // // // // // // // // // // // // // // // // // // // // //

    /**
     * Get a {@link Matcher} that always returns true, therefore matching everything.
     */
    public static <E> Matcher<E> trueMatcher() {
         return TrueMatcher.getInstance();
    }

    /**
     * Get a {@link Matcher} that always returns false, therefore matching nothing..
     */
    public static <E> Matcher<E> falseMatcher() {
         return FalseMatcher.getInstance();
    }

    /**
     * Get a {@link Matcher} that returns the opposite of the specified {@link Matcher}.
     */
    public static <E> Matcher<E> invert(Matcher<E> original) {
         return new NotMatcher<E>(original);
    }

    /**
     * Creates a {@link Matcher} that uses Reflection to compare the value
     * of the specified property to the expected value of that property. If
     * the property value equals the expected, the element matches. Otherwise
     * it doesn't.
     */
    public static <E> Matcher<E> beanPropertyMatcher(Class<E> beanClass, String propertyName, Object matchValue) {
        return new BeanMatcher<E>(beanClass, propertyName, matchValue);
    }

    /**
     * Iterate through the specified collection and remove all elements
     * that don't match the specified matcher.
     *
     * @return <code>true</code> if any elements were removed from the specified
     *      {@link Collection}.
     */
    public static <E> boolean filter(Collection<E> collection, Matcher<E> matcher) {
        boolean changed = false;
        for(Iterator<E> i = collection.iterator(); i.hasNext(); ) {
            E element = i.next();
            if(!matcher.matches(element)) {
                i.remove();
                changed = true;
            }
        }
        return changed;
    }
}