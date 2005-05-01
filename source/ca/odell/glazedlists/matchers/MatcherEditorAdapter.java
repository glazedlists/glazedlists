package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;

/**
 * Often implementations of {@link MatcherEditorListener} wish to perform
 * specific logic based on the type of change which occurred to the Matcher.
 * This base class extracts the information from the MatcherEvent and delegates
 * the processing to individual methods based on the type of the MatcherEvent.
 * This allows subclasses to clearly separate the logic they contain for each
 * type of Matcher change.
 *
 * @author James Lemieux
 */
public abstract class MatcherEditorAdapter implements MatcherEditorListener {

    /**
     * This implementation of this method simply delegates the handling of the
     * given <code>matcherEvent</code> to one of the protected methods defined
     * by this class. This allows subclasses to clearly separate the logic they
     * contain for each type of Matcher change.
     *
     * @param matcherEvent a MatcherEvent describing the change in the Matcher
     *      produced by the MatcherEditor
     */
    public void changedMatcher(MatcherEvent matcherEvent) {
        final MatcherEditor matcherEditor = matcherEvent.getMatcherEditor();
        final Matcher matcher = matcherEvent.getMatcher();

        switch (matcherEvent.getType()) {
            case MatcherEvent.MATCH_ALL: this.matchAll(matcherEditor); break;
            case MatcherEvent.MATCH_NONE: this.matchNone(matcherEditor); break;
            case MatcherEvent.CHANGED: this.changed(matcherEditor, matcher); break;
            case MatcherEvent.RELAXED: this.relaxed(matcherEditor, matcher); break;
            case MatcherEvent.CONSTRAINED: this.constrained(matcherEditor, matcher); break;
        }
    }

    /**
     * Indicates that the {@link MatcherEditor} has been changed to always return true..
     * In response to this change, all elements will be included.
     */
    protected void matchAll(MatcherEditor source) {}

    /**
     * Indicates that the {@link MatcherEditor} has been changed to always return false..
     * In response to this change, no elements will be included.
     */
    protected void matchNone(MatcherEditor source) {}

    /**
     * Indicates that the {@link ca.odell.glazedlists.Matcher} has changed.  In response to this
     * change, all elements must be tested.
     *
     * @param matcher a {@link ca.odell.glazedlists.Matcher} that has no relationship to the previous
     *      value held by the {@link MatcherEditor}.
     */
    protected void changed(MatcherEditor source, Matcher matcher) {}

    /**
     * Indicates that the {@link Matcher} has become more restrictive. In response
     * to this change, the same or fewer elements will be included.
     *
     * @param matcher a {@link Matcher} that returns false for every element that
     *      the previous {@link Matcher} returned false.
     */
    protected void constrained(MatcherEditor source, Matcher matcher) {}

    /**
     * Indicates that the {@link Matcher} has become less restrictive. In response
     * to this change, the same or more elements will be included.
     *
     * @param matcher a {@link Matcher} that returns true for every element that
     *      the previous {@link Matcher} returned true.
     */
    protected void relaxed(MatcherEditor source, Matcher matcher) {}
}