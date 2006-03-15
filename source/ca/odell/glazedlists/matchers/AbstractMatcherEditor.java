/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import javax.swing.event.EventListenerList;

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
public abstract class AbstractMatcherEditor<E> implements MatcherEditor<E> {

    /** listeners for this Editor */
    private EventListenerList listenerList = new EventListenerList();

	/** the current Matcher in effect */
	protected Matcher<E> currentMatcher = Matchers.trueMatcher();

	/** {@inheritDoc} */
	public Matcher<E> getMatcher() {
		return currentMatcher;
	}

	/** {@inheritDoc} */
    public final void addMatcherEditorListener(MatcherEditor.Listener<E> listener) {
        listenerList.add(MatcherEditor.Listener.class, listener);
    }

    /** {@inheritDoc} */
    public final void removeMatcherEditorListener(MatcherEditor.Listener<E> listener) {
        listenerList.remove(Listener.class, listener);
    }

    /**
     * Indicates that the filter matches all.
     */
    protected final void fireMatchAll() {
		this.currentMatcher = Matchers.trueMatcher();
        this.fireChangedMatcher(new MatcherEditor.Event<E>(this, Event.MATCH_ALL, this.currentMatcher));
    }

    /**
     * Indicates that the filter has changed in an indeterminate way.
     */
    protected final void fireChanged(Matcher<E> matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;
        this.fireChangedMatcher(new MatcherEditor.Event<E>(this, Event.CHANGED, this.currentMatcher));
    }

    /**
     * Indicates that the filter has changed to be more restrictive. This should only be
     * called if all currently filtered items will remain filtered.
     */
    protected final void fireConstrained(Matcher<E> matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;
        this.fireChangedMatcher(new MatcherEditor.Event<E>(this, Event.CONSTRAINED, this.currentMatcher));
    }

    /**
     * Indicates that the filter has changed to be less restrictive. This should only be
     * called if all currently unfiltered items will remain unfiltered.
     */
    protected final void fireRelaxed(Matcher<E> matcher) {
		if(matcher == null) throw new NullPointerException();
		this.currentMatcher = matcher;
        this.fireChangedMatcher(new MatcherEditor.Event<E>(this, Event.RELAXED, this.currentMatcher));
    }

    /**
     * Indicates that the filter matches none.
     */
    protected final void fireMatchNone() {
		this.currentMatcher = Matchers.falseMatcher();
        this.fireChangedMatcher(new MatcherEditor.Event<E>(this, Event.MATCH_NONE, this.currentMatcher));
    }

    protected final void fireChangedMatcher(MatcherEditor.Event<E> event) {
        // Guaranteed to return a non-null array
        final Object[] listeners = this.listenerList.getListenerList();

        // Process the listenerList last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2)
            ((Listener<E>) listeners[i+1]).changedMatcher(event);
    }
}