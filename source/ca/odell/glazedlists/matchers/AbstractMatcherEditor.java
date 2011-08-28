/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;


/**
 * Basic building block for {@link MatcherEditor} implementations that
 * handles the details of dealing with registered {@link MatcherEditor.Listener}s.
 * All {@link MatcherEditor} implementations should extend this class for its
 * convenience methods.
 *
 * <p>Extending classes can fire events to registered listeners using the
 * "fire" methods:
 * <ul>
 *    <li>{@link #fireMatchNone()}</li>
 *    <li>{@link #fireConstrained(Matcher)}</li>
 *    <li>{@link #fireChanged(Matcher)}</li>
 *    <li>{@link #fireRelaxed(Matcher)}</li>
 *    <li>{@link #fireMatchAll()}</li>
 * </ul>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public abstract class AbstractMatcherEditor<E> extends AbstractMatcherEditorListenerSupport<E> {

    /** the current Matcher in effect */
	private Matcher<E> currentMatcher = Matchers.trueMatcher();

	/** {@inheritDoc} */
	public final Matcher<E> getMatcher() {
		return currentMatcher;
	}

	/**
     * Indicates that the filter matches all.
     */
    protected final void fireMatchAll() {
		currentMatcher = Matchers.trueMatcher();
        fireChangedMatcher(createMatchAllEvent(currentMatcher));
    }

    /**
     * Indicates that the filter has changed in an indeterminate way.
     */
    protected final void fireChanged(Matcher<E> matcher) {
		if (matcher == null) throw new NullPointerException();
		currentMatcher = matcher;
		fireChangedMatcher(createChangedEvent(currentMatcher));
    }

    /**
     * Indicates that the filter has changed to be more restrictive. This should only be
     * called if all currently filtered items will remain filtered.
     */
    protected final void fireConstrained(Matcher<E> matcher) {
		if (matcher == null) throw new NullPointerException();
		currentMatcher = matcher;
        fireChangedMatcher(createConstrainedEvent(currentMatcher));
    }

    /**
     * Indicates that the filter has changed to be less restrictive. This should only be
     * called if all currently unfiltered items will remain unfiltered.
     */
    protected final void fireRelaxed(Matcher<E> matcher) {
		if (matcher == null) throw new NullPointerException();
		currentMatcher = matcher;
        fireChangedMatcher(createRelaxedEvent(currentMatcher));
    }

    /**
     * Indicates that the filter matches none.
     */
    protected final void fireMatchNone() {
		currentMatcher = Matchers.falseMatcher();
        fireChangedMatcher(createMatchNoneEvent(currentMatcher));
    }

    /**
     * Returns <tt>true</tt> if the current matcher will match everything.
     */
    protected final boolean isCurrentlyMatchingAll() {
        return currentMatcher == Matchers.trueMatcher();
    }

    /**
     * Returns <tt>true</tt> if the current matcher will match nothing.
     */
    protected final boolean isCurrentlyMatchingNone() {
        return currentMatcher == Matchers.falseMatcher();
    }
}