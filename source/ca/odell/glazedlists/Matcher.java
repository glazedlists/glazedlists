/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

/**
 * Immutable interface that is implemented to represent the value of a filter contained
 * by a {@link MatcherSource}. The implementation simply determines
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see ca.odell.glazedlists.FilterList
 * @see ca.odell.glazedlists.event.MatcherSourceListener
 */
public interface Matcher {
    /**
     * Return true if an item matches a filter.
     *
     * @param item The item possibly beig filtered.
     */
    public boolean matches(Object item);
}
