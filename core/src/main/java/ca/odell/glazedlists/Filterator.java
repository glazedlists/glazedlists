package ca.odell.glazedlists;

import java.util.List;

/**
 * An interface for extracting a list of values to be considered by a Matcher
 * when matching a particular element of a list.
 *
 * @author James Lemieux
 */
@FunctionalInterface
public interface Filterator<C,E> {

    /**
     * Extracts the list of filterable values from a given <code>element</code>.
     * These values will be accessed within a Matcher to judge whether the
     * <code>element</code> matches some criteria.
     *
     * @param baseList a list that the implementor shall add their filter
     *      values to via <code>baseList.add()</code>. This may be a non-empty
     *      List and it is an error to call any method other than add().
     * @param element the object to extract the filter values from
     */
    public void getFilterValues(List<C> baseList, E element);
}