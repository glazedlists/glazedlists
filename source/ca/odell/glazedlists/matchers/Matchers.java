/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

// for access to volatile classes
import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.impl.beans.BeanProperty;
import ca.odell.glazedlists.impl.matchers.*;

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
        return new BeanPropertyMatcher<E>(new BeanProperty<E>(beanClass, propertyName, true, false), matchValue);
    }

    /**
     * Creates a {@link Matcher} that matches {@link Comparable} objects for
     * containment within the range between the given <code>start</code>
     * and <code>end</code>.
     */
    public static <D extends Comparable,E> Matcher<E> rangeMatcher(D start, D end) {
        return new RangeMatcher<D,E>(start, end);
    }

    /**
     * Creates a {@link Matcher} that uses the given <code>filterator</code>
     * to extract {@link Comparable} objects from filtered objects and compares
     * those Comparables against the range between the given <code>start</code>
     * and <code>end</code>. If at least one Comparable returned by the
     * <code>filterator</code> is within the range, the object is considered
     * a match.
     *
     * <p><code>null</code> <code>start</code> or <code>end</code> values are
     * allowed and are interpreted as <code>"no start"</code> or
     * <code>"no end"</code> to the range respectively.
     *
     * @param start the {@link Comparable} which starts the range
     * @param end the {@link Comparable} which ends the range
     * @param filterator the logic for extracting filter {@link Comparable}s
     *      from filtered objects
     */
    public static <D extends Comparable,E> Matcher<E> rangeMatcher(D start, D end, Filterator<D,E> filterator) {
        return new RangeMatcher<D,E>(start, end, filterator);
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