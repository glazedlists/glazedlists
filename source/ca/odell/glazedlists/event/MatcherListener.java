/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.Matcher;


/**
 * Listener interface to provide for notifications the a {@link
 * ca.odell.glazedlists.Matcher} has changed.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see ca.odell.glazedlists.Matcher
 */
public interface MatcherListener {
    /**
     * Indicates that the filter on the {@link ca.odell.glazedlists.Matcher} has been
     * cleared (i.e., all elements should now be visible).
     */
    public void cleared(Matcher source);

    /**
     * Indicates that the {@link Matcher} has changed in an inditerminate way.
     *
     * @param source The source of the event.
     */
    public void changed(Matcher source);

    /**
     * Indicates that the {@link Matcher} has changed to be more restrictive. This should
     * only be called if all currently filtered items will remain filtered.
     *
     * @param source The source of the event.
     */
    public void constrained(Matcher source);

    /**
     * Indicates that the {@link Matcher} has changed to be less restrictive. This should
     * only be called if all currently unfiltered items will remain unfiltered.
     *
     * @param source The source of the event.
     */
    public void relaxed(Matcher source);
}
