/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import java.util.*;

/**
 * A utility class containing all sorts of random things that are useful for
 * implementing Glazed Lists but not for using Glazed Lists.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
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

    // Date Utility Methods // // // // // // // // // // // // // // // // //

    /**
     * Returns a new Date representing the first millisecond of the month for
     * the given <code>date</code>.
     */
    public static Date getMonthBegin(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return getMonthBegin(cal);
    }

    /**
     * Adjusts the given <code>calendar</code> to the start of the month and
     * return the resulting Date.
     */
    public static Date getMonthBegin(Calendar calendar) {
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        return calendar.getTime();
    }

    /**
     * Returns <tt>true</tt> if the given <code>calendar</code> represents the
     * first millisecond of a month; <tt>false</tt> otherwise.
     */
    public static boolean isBeginningOfMonth(Calendar calendar) {
        return calendar.get(Calendar.MILLISECOND) == 0 &&
               calendar.get(Calendar.SECOND) == 0 &&
               calendar.get(Calendar.MINUTE) == 0 &&
               calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
               calendar.get(Calendar.DATE) == 1;
    }
}