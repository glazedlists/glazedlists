/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Basic building block for {@link MatcherEditor} implementations that handles
 * the details of dealing with registered {@link MatcherEditor.Listener}s. In
 * addition, it provides helper methods for creating matcher events.
 *
 * @author Holger Brands
 */
public abstract class AbstractMatcherEditorListenerSupport<E> implements MatcherEditor<E> {

	/** listeners for this Editor */
	private CopyOnWriteArrayList<Listener<E>> listenerList = new CopyOnWriteArrayList<Listener<E>>();

	/** {@inheritDoc} */
	@Override
    public final void addMatcherEditorListener(MatcherEditor.Listener<E> listener) {
		if (listener != null) {
			listenerList.add(listener);
		}
	}

	/** {@inheritDoc} */
	@Override
    public final void removeMatcherEditorListener(MatcherEditor.Listener<E> listener) {
		if (listener != null) {
			listenerList.remove(listener);
		}
	}

	/** delivers the given matcher event to all registered listeners. */
	protected final void fireChangedMatcher(MatcherEditor.Event<E> event) {
		// NOTE: We are intentionally dispatching in LIFO order with an iterator
		// we need to clone before reverse iteration to be thread-safe, see http://stackoverflow.com/a/42046731/336169
		List<Listener<E>> listenerListCopy = (List<Listener<E>>) listenerList.clone();
	    ListIterator<Listener<E>> li = listenerListCopy.listIterator(listenerListCopy.size());
		while (li.hasPrevious()) {
			li.previous().changedMatcher(event);
		}
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