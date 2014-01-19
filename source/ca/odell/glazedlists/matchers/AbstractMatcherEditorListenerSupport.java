/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import javax.swing.event.EventListenerList;

/**
 * Basic building block for {@link MatcherEditor} implementations that handles
 * the details of dealing with registered {@link MatcherEditor.Listener}s. In
 * addition, it provides helper methods for creating matcher events.
 * 
 * @author Holger Brands
 */
public abstract class AbstractMatcherEditorListenerSupport<E> implements
		MatcherEditor<E> {

	/** listeners for this Editor */
	private EventListenerList listenerList = new EventListenerList();

	/** {@inheritDoc} */
	@Override
    public final void addMatcherEditorListener(
			MatcherEditor.Listener<E> listener) {
		listenerList.add(MatcherEditor.Listener.class, listener);
	}

	/** {@inheritDoc} */
	@Override
    public final void removeMatcherEditorListener(
			MatcherEditor.Listener<E> listener) {
		listenerList.remove(Listener.class, listener);
	}

	/** delivers the given matcher event to all registered listeners. */
	protected final void fireChangedMatcher(MatcherEditor.Event<E> event) {
		// Guaranteed to return a non-null array
		final Object[] listeners = this.listenerList.getListenerList();

		// Process the listenerList last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			((Listener<E>) listeners[i + 1]).changedMatcher(event);
	}

	/** creates a changed event. */
	protected final MatcherEditor.Event<E> createChangedEvent(Matcher<E> matcher) {
		return createEvent(Event.CHANGED, matcher);
	}

	/** creates a constrained event. */
	protected final MatcherEditor.Event<E> createConstrainedEvent(
			Matcher<E> matcher) {
		return createEvent(Event.CONSTRAINED, matcher);
	}

	/** creates a relaxed event. */
	protected final MatcherEditor.Event<E> createRelaxedEvent(Matcher<E> matcher) {
		return createEvent(Event.RELAXED, matcher);
	}

	/** creates a match none event. */
	protected final MatcherEditor.Event<E> createMatchNoneEvent(
			Matcher<E> matcher) {
		return createEvent(Event.MATCH_NONE, matcher);
	}

	/** creates a match all event. */
	protected final MatcherEditor.Event<E> createMatchAllEvent(
			Matcher<E> matcher) {
		return createEvent(Event.MATCH_ALL, matcher);
	}

	/** creates a matcher event for the given type and matcher. */
	private MatcherEditor.Event<E> createEvent(int eventType, Matcher<E> matcher) {
		return new MatcherEditor.Event<E>(this, eventType, matcher);
	}

}