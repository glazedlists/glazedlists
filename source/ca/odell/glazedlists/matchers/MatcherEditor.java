/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.FilterList;

import java.util.EventObject;
import java.util.EventListener;

/**
 * A facility for modifying the {@link Matcher}s which specify the behaviour of a
 * {@link ca.odell.glazedlists.FilterList FilterList}.
 *
 * <p>Although this interface is called an <i>Editor</i>, the
 * implementor should create new {@link Matcher} instances on each
 * change rather than modifying the existing {@link Matcher}s. This is because
 * {@link Matcher}s work best when they are immutable. Further information
 * on this immutability can be found in the {@link Matcher Matcher Javadoc}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 * @author James Lemieux
 */
public interface MatcherEditor {
    
    /**
     * Add a listener to be notified when this editor's {@link Matcher} changes.
     */
	public void addMatcherEditorListener(Listener listener);
    
    /**
     * Remove the listener so that it no longer receives notification when the
     * {@link Matcher} changes.
     */
	public void removeMatcherEditorListener(Listener listener);

	/**
     * Return the current {@link Matcher} specified by this {@link MatcherEditor}.
     *
     * @return a non-null {@link Matcher}.
	 */
	public Matcher getMatcher();

    /**
     * A MatcherEditor.Listener handles changes fired by a {@link MatcherEditor}.
     * The most notable implementation will be {@link ca.odell.glazedlists.FilterList FilterList}
     * which uses these events to update its state.
     */
    public interface Listener extends EventListener {

       /**
        * Indicates a changes has occurred in the Matcher produced by the
        * MatcherEditor.
        *
        * @param matcherEvent a MatcherEditor.Event describing the change in the
        *      Matcher produced by the MatcherEditor
        */
        public void changedMatcher(Event matcherEvent);
    }

    /**
     * A MatcherEditor event models a change in the {@link MatcherEditor} that
     * creates a new  {@link Matcher}.
     *
     * <p>The event gives access to:
     * <ul>
     *   <li> the {@link MatcherEditor} which was the source of the change
     *   <li> the new {@link Matcher} which was produced from the MatcherEditor
     *   <li> a type value which indicates whether the new Matcher may be
     *        considered a relaxing, constraining, or changing of the prior Matcher
     *        produced from the MatcherEditor. Special types also exist for the
     *        edge cases where the new Matcher is guaranteed to match everything or
     *        nothing
     * </ul>
     *
     * The type constants are found in this event class:
     * <ul>
     *   <li> {@link #MATCH_ALL}
     *   <li> {@link #MATCH_NONE}
     *   <li> {@link #CONSTRAINED}
     *   <li> {@link #RELAXED}
     *   <li> {@link #CHANGED}
     * </ul
     */
    public class Event extends EventObject {

        /** Indicates the associated Matcher will match anything. */
        public static final int MATCH_ALL = 0;

        /** Indicates the associated Matcher will match nothing. */
        public static final int MATCH_NONE = 1;

        /**
         * Indicates the associated Matcher is a constrained version of the
         * previous Matcher, implying it can be expected to match at most the same
         * values matched by the previous Matcher, and possibly fewer.
         */
        public static final int CONSTRAINED = 2;

        /**
         * Indicates the associated Matcher is a relaxed version of the previous
         * Matcher, implying it can be expected to match at least the same values
         * matched by the previous Matcher, and possibly more.
         */
        public static final int RELAXED = 3;

        /**
         * Indicates the associated Matcher is a complete change from the previous
         * Matcher. No guarantees can be made for any values which were matched or
         * unmatched by the previous Matcher.
         */
        public static final int CHANGED = 4;

        private MatcherEditor matcherEditor;
        private final Matcher matcher;
        private final int type;

        public Event(MatcherEditor matcherEditor, int changeType) {
            this(matcherEditor, changeType, null);
        }

        public Event(MatcherEditor matcherEditor, int changeType, Matcher matcher) {
            super(matcherEditor);
            this.matcherEditor = matcherEditor;
            this.type = changeType;
            this.matcher = matcher;
        }

        public Event(FilterList eventSource, int changeType, Matcher matcher) {
            super(eventSource);
            this.type = changeType;
            this.matcher = matcher;
        }

        /**
         * Get the {@link MatcherEditor} that originated this event, or null
         * if this event originated directly from a {@link FilterList} in a call
         * to {@link FilterList#setMatcher(Matcher}}.
         */
        public MatcherEditor getMatcherEditor() {
            return this.matcherEditor;
        }

        public Matcher getMatcher() {
            return this.matcher;
        }

        public int getType() {
            return this.type;
        }
    }
}
