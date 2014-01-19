/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.impl.text.LatinDiacriticsStripper;
import ca.odell.glazedlists.impl.adt.KeyedCollection;

import java.util.*;

/**
 * A utility class containing all sorts of random things that are useful for
 * implementing Glazed Lists but not for using Glazed Lists.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class GlazedListsImpl {

    /**
     * A dummy constructor to prevent instantiation of this class
     */
    private GlazedListsImpl() {
        throw new UnsupportedOperationException();
    }

    // Utility Methods // // // // // // // // // // // // // // // // // // //

    /**
     * Compare the specified objects for equality.
     */
    public static boolean equal(Object a, Object b) {
        if(a == b) return true;
        if(a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Concatenate two lists to create a third list.
     */
    public static <E> List<E> concatenate(List<E> a, List<E> b) {
        List<E> aAndB = new ArrayList<E>(a.size() + b.size());
        aAndB.addAll(a);
        aAndB.addAll(b);
        return aAndB;
    }

    /**
     * Replace all elements in the target {@link EventList} with the elements in
     * the source {@link List}.
     *
     * <p>Note that both collections must be sorted prior to execution using the
     * {@link Comparator} specified.
     */
    public static <E> void replaceAll(EventList<E> target, Collection<E> source, boolean updates, Comparator<E> comparator) {
        // use the default comparator if none is specified
        if(comparator == null) {
            comparator = (Comparator<E>)GlazedLists.comparableComparator();
        }

        // walk through each list simultaneously
        int targetIndex = -1;
        Iterator<E> sourceIterator = source.iterator();

        // define marker objects that mean we need new objects
        final E NEW_VALUE_NEEDED = (E)Void.class;
        E targetObject = NEW_VALUE_NEEDED;
        E sourceObject = NEW_VALUE_NEEDED;

        // while there are elements left in either list
        while(true) {

            // do our best to get new objects if necessary
            if(targetObject == NEW_VALUE_NEEDED) {
                if(targetIndex < target.size()) targetIndex++;
                if(targetIndex < target.size()) targetObject = target.get(targetIndex);
            }
            if(sourceObject == NEW_VALUE_NEEDED) {
                if(sourceIterator.hasNext()) sourceObject = sourceIterator.next();
            }

            // we've exhausted our dataset
            if(targetObject == NEW_VALUE_NEEDED && sourceObject == NEW_VALUE_NEEDED) {
                break;
            }

            // figure out if this is an insert, update or delete on the target
            int compareResult;
            if(targetObject == NEW_VALUE_NEEDED) compareResult = 1;
            else if(sourceObject == NEW_VALUE_NEEDED) compareResult = -1;
            else compareResult = comparator.compare(targetObject, sourceObject);

            // the target value precedes the source value, delete it
            if(compareResult < 0) {
                target.remove(targetIndex);
                targetIndex--;
                targetObject = NEW_VALUE_NEEDED;

            // the values are equal, keep them
            } else if(compareResult == 0) {
                if(updates) {
                    target.set(targetIndex, sourceObject);
                }
                targetObject = NEW_VALUE_NEEDED;
                sourceObject = NEW_VALUE_NEEDED;

            // the source value precedes the target, insert it
            } else if(compareResult > 0) {
                target.add(targetIndex, sourceObject);
                targetIndex++;
                sourceObject = NEW_VALUE_NEEDED;
            }
        }
    }

    /**
     * Get a character mapper array which strips the diacritics from Latin
     * characters in order to normalize word spellings between Latin-based
     * languages. This allows users from any Latin-based language to search
     * the text of any other Latin-based language intuitively.
     *
     * <p>For example, with the returned character mapper array, a French
     * user searching for the text "résumé" would match the English text
     * "resume". Similarly, an English user searching for the text "resume"
     * would match the French text "résumé." In this way, neither user needs to
     * know the specifics of the foreign language they are searching, and thus
     * the quality of their search increases.
     */
    public static char[] getLatinDiacriticsStripper() {
        return LatinDiacriticsStripper.getMapper();
    }

    // Date Utility Methods // // // // // // // // // // // // // // // // //

    /**
     * Returns a new Date representing the first millisecond of the month for
     * the given <code>date</code>.
     */
    public static Date getMonthBegin(Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getMonthStart(cal);
    }

    /**
     * Adjusts the given <code>calendar</code> to the start of the month and
     * returns the resulting {@link Date}.
     */
    public static Date getMonthStart(Calendar calendar) {
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Returns <tt>true</tt> if the given <code>calendar</code> represents the
     * first millisecond of a month; <tt>false</tt> otherwise.
     */
    public static boolean isMonthStart(Calendar calendar) {
        return calendar.get(Calendar.MILLISECOND) == 0 &&
               calendar.get(Calendar.SECOND) == 0 &&
               calendar.get(Calendar.MINUTE) == 0 &&
               calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
               calendar.get(Calendar.DATE) == 1;
    }

    /**
     * Returns a non-symmetric Comparator that returns 0 if two Objects are
     * equal as specified by {@link Object#equals(Object)}, or 1 otherwise.
     */
    public static Comparator<? extends Object> equalsComparator() {
        return new EqualsComparator<Object>();
    }
    private static class EqualsComparator<T> implements Comparator<T> {
        @Override
        public int compare(T alpha, T beta) {
            boolean equal = alpha == null ? beta == null : alpha.equals(beta);
            return equal ? 0 : 1;
        }
    }

    /**
     * Returns a {@link FunctionList.Function} that simply reflects the
     * function's argument as its result.
     */
    public static <E> FunctionList.Function<E,E> identityFunction() {
        return new IdentityFunction<E>();
    }
    private static class IdentityFunction<E> implements FunctionList.Function<E,E> {
        @Override
        public E evaluate(E sourceValue) {
            return sourceValue;
        }
    }

    /**
     * Returns a {@link KeyedCollection} optimized for the values which can be
     * compared.
     *
     * @param positionComparator a Comparator to order position objects
     * @param valueComparator a Comparator to order value objects
     * @return a {@link KeyedCollection} optimized for values which can be
     *      compared
     */
    public static <P, V> KeyedCollection<P, V> keyedCollection(Comparator<P> positionComparator, Comparator<V> valueComparator) {
        return new KeyedCollection<P, V>(positionComparator, new TreeMap<V, Object>(valueComparator));
    }

    /**
     * Returns a {@link KeyedCollection} optimized for the values which cannot
     * be compared.
     *
     * @param positionComparator a Comparator to order position objects
     * @return a {@link KeyedCollection} optimized for values which cannot be
     *      compared
     */
    public static <P, V> KeyedCollection<P, V> keyedCollection(Comparator<P> positionComparator) {
        return new KeyedCollection<P, V>(positionComparator, new HashMap<V, Object>());
    }

    /**
     * Removes the given <code>value</code> from <code>c</code> if it can be
     * located by identity. This method is thus faster than standard removes
     * from a Collection and conveys a stronger meaning than normal Collection
     * removes (namely that the exact item must be found in order to be removed)
     *
     * @param c the Collection from which to remove
     * @param value the value to be removed
     * @return <tt>true</tt> if <code>c</code> was altered as a result of this call
     */
    public static <V> boolean identityRemove(Collection<V> c, V value) {
        for (Iterator<V> i = c.iterator(); i.hasNext();) {
            if (i.next() == value) {
                i.remove();
                return true;
            }
        }

        return false;
    }
}