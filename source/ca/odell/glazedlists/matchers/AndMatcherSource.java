/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:31:01 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;
import ca.odell.glazedlists.event.MatcherSourceListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches if <i>both</i> of the provided
 * matchersource's filters apply. If no delegate {@link Matcher Matchers} are configured, this
 * will always match.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see OrMatcherSource
 * @see XorMatcherSource
 */
public final class AndMatcherSource extends CompositeMatcherSource
	implements MatcherSourceListener {

	protected AndMatcherSource(Matcher initial_matcher) {
		super(initial_matcher);	// TODO: implement
	}

	public void cleared(MatcherSource source) {
		// TODO: implement
	}

	public void changed(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}

	public void constrained(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}

	public void relaxed(Matcher new_matcher, MatcherSource source) {
		// TODO: implement
	}

	//    /**
//     * {@inheritDoc}
//     */
//    public AndMatcherSource(Matcher one, Matcher two) {
//        super(one, two);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public AndMatcherSource(Matcher[] matchers) {
//        super(matchers);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public AndMatcherSource(EventList matcher_list) {
//        super(matcher_list);
//    }
//
//
//    public boolean matches(Object item) {
//        Matcher[] delegates = delegates();
//        if (delegates.length == 0) return true;     // always match if no delegates
//
//        for (int i = 0; i < delegates.length; i++) {
//            if (!delegates[ i ].matches(item)) return false;
//        }
//
//        return true;        // everything matched (or there were no delegate matchers)
//    }
//
//    public void cleared(Matcher source) {
//        // One Matcher was cleared, but the other(s) may not be...
//        fireRelaxed();
//    }
//
//    public void changed(Matcher source) {
//        fireChanged();
//    }
//
//    public void constrained(Matcher source) {
//        fireConstrained();
//    }
//
//    public void relaxed(Matcher source) {
//        fireRelaxed();
//    }
}
