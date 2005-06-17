package ca.odell.glazedlists.matchers;

import java.util.EventObject;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.FilterList;

/**
 * A MatcherEvent models a change in the {@link Matcher} produced by a
 * {@link MatcherEditor}. <p>
 *
 * The event gives access to:
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
 *
 * <ul>
 *   <li> {@link #MATCH_ALL}
 *   <li> {@link #MATCH_NONE}
 *   <li> {@link #CONSTRAINED}
 *   <li> {@link #RELAXED}
 *   <li> {@link #CHANGED}
 * </ul
 *
 * @author James Lemieux
 */
public class MatcherEvent extends EventObject {

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

    public MatcherEvent(MatcherEditor matcherEditor, int changeType) {
        this(matcherEditor, changeType, null);
    }

    public MatcherEvent(MatcherEditor matcherEditor, int changeType, Matcher matcher) {
        super(matcherEditor);
		this.matcherEditor = matcherEditor;
        this.type = changeType;
        this.matcher = matcher;
    }

	public MatcherEvent(FilterList eventSource, int changeType, Matcher matcher) {
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