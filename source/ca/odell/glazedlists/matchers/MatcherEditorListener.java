/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;

/**
 * A MatcherEditorListener handles changes fired by a {@link MatcherEditor}.
 * The most notable implementation will be {@link ca.odell.glazedlists.FilterList FilterList}
 * which uses these events to update its state.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see ca.odell.glazedlists.Matcher
 */
public interface MatcherEditorListener {

    /**
     * Indicates that the {@link MatcherEditor} has been changed to always return true..
     * In response to this change, all elements will be included.
     */
    public void matchAll(MatcherEditor source);

    /**
     * Indicates that the {@link MatcherEditor} has been changed to always return false..
     * In response to this change, no elements will be included.
     */
    public void matchNone(MatcherEditor source);

    /**
     * Indicates that the {@link Matcher} has changed.  In response to this
     * change, all elements must be tested.
     *
     * @param matcher a {@link Matcher} that has no relationship to the previous
     *      value held by the {@link MatcherEditor}.
     */
    public void changed(MatcherEditor source, Matcher matcher);

    /**
     * Indicates that the {@link Matcher} has become more restrictive. In response
     * to this change, the same or fewer elements will be included.
     *
     * @param matcher a {@link Matcher} that returns false for every element that
     *      the previous {@link Matcher} returned false.
     */
    public void constrained(MatcherEditor source, Matcher matcher);

    /**
     * Indicates that the {@link Matcher} has become less restrictive. In response
     * to this change, the same or more elements will be included.
     *
     * @param matcher a {@link Matcher} that returns true for every element that
     *      the previous {@link Matcher} returned true.
     */
    public void relaxed(MatcherEditor source, Matcher matcher);
}
