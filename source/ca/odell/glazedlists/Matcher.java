/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.MatcherListener;


/**
 * Interface that can be implemented to determine when items match in a {@link
 * ca.odell.glazedlists.FilterList}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see ca.odell.glazedlists.FilterList
 * @see ca.odell.glazedlists.event.MatcherListener
 */
public interface Matcher {
    /**
     * Return true if an item matches a filter.
     *
     * @param item The item possibly beig filtered.
     */
    public boolean matches(Object item);


    /**
     * Add a listener for Matcher changes.
     */
    public void addMatcherListener(MatcherListener listener);

    /**
     * Remove a listener for Matcher changes.
     */
    public void removeMatcherListener(MatcherListener listener);
}
