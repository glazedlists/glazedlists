/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 6:56:35 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;
import ca.odell.glazedlists.event.MatcherSourceListener;

import java.util.ArrayList;
import java.util.List;


/**
 * Basic building block for {@link ca.odell.glazedlists.MatcherSource} implementations that
 * handles the details of dealing with listeners. All <tt>MatcherSource</tt> implementations are
 * encouraged to extends this class rather than directly implementing <tt>Matcher</tt>.
 * <p/>
 * Extending classes can fire events to listener using "fire" methods: <ul> <li>{@link
 * #fireCleared}</li> <li>{@link #fireChanged}</li> <li>{@link #fireConstrained}</li>
 * <li>{@link #fireRelaxed}</li> </ul>
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public abstract class AbstractMatcherSource implements MatcherSource {
    /**
     * Contains MatcherListeners.
     */
    private final List listener_list = new ArrayList(1);  // normally only one listener


	/**
	 * The current matchersource in effect.
	 */
	private volatile Matcher current_matcher;


	protected AbstractMatcherSource(Matcher initial_matcher) {
		if (initial_matcher == null)
			throw new IllegalArgumentException("Initial Matcher cannot be null");

		this.current_matcher = initial_matcher;
	}


	/**
	 * {@inheritDoc}
	 */
	public Matcher getCurrentMatcher() {
		return current_matcher;
	}

	/**
	 * Used by extending classes to set the matchersource that is currently in use.
	 */
	public void setCurrentMatcher(Matcher matcher) {
		if (matcher == null) throw new IllegalArgumentException("Matcher cannot be null");

		this.current_matcher = matcher;
	}


	/**
     * {@inheritDoc}
     */
    public final void addMatcherSourceListener(MatcherSourceListener listener) {
        synchronized(listener_list) {
            listener_list.add(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void removeMatcherSourceListener(MatcherSourceListener listener) {
        synchronized(listener_list) {
            listener_list.remove(listener);
        }
    }


    /**
     * Indicates that the filter has been cleared (i.e., all elements should now be
     * visible.
     */
    protected final void fireCleared() {
        synchronized(listener_list) {
            for (int i = 0; i < listener_list.size(); i++) {
                ((MatcherSourceListener) listener_list.get(i)).cleared(this);
            }
        }
    }

    /**
     * Indicates that the filter has changed in an inditerminate way.
     */
    protected final void fireChanged(Matcher new_matcher) {
        synchronized(listener_list) {
            for (int i = 0; i < listener_list.size(); i++) {
                ((MatcherSourceListener) listener_list.get(i)).changed(new_matcher,this);
            }
        }
    }

    /**
     * Indicates that the filter has changed to be more restrictive. This should only be
     * called if all currently filtered items will remain filtered.
     */
    protected final void fireConstrained(Matcher new_matcher) {
        synchronized(listener_list) {
            for (int i = 0; i < listener_list.size(); i++) {
                ((MatcherSourceListener) listener_list.get(i)).constrained(new_matcher, this);
            }
        }
    }

    /**
     * Indicates that the filter has changed to be less restrictive. This should only be
     * called if all currently unfiltered items will remain unfiltered.
     */
    protected final void fireRelaxed(Matcher new_matcher) {
        synchronized(listener_list) {
            for (int i = 0; i < listener_list.size(); i++) {
                ((MatcherSourceListener) listener_list.get(i)).relaxed(new_matcher, this);
            }
        }
    }
}
