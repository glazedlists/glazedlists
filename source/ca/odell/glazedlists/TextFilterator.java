/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import java.util.List;

/**
 * An utility class that can get a list of Strings for a given object
 * for testing whether a filter matches.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part2/index.html">Glazed
 * Lists Tutorial Part 2 - Text Filtering</a>
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part8/index.html#filtering">Glazed
 * Lists Tutorial Part 8 - Performance Tuning</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface TextFilterator {

    /**
     * Gets the specified object as a list of Strings. These Strings
     * should contain all object information so that it can be compared
     * to the filter set.
     *
     * @param baseList a list that the implementor shall add their filter
     *      strings to via <code>baseList.add()</code>. This may be a non-empty
     *      List and it is an error to call any method other than add().
     * @param element the object to extract the filter strings from.
     */
    public void getFilterStrings(List baseList, Object element);
}
