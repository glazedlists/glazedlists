/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:30:19 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherSourceListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches if <i>either</i> or </i>both</i> of
 * the provided matchersource's filters apply. If no delegate {@link Matcher Matchers} are
 * configured, this will always match.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see XorMatcherSource
 * @see AndMatcherSource
 */
public final class OrMatcherSource extends CompositeMatcherSource implements MatcherSourceListener {
	protected OrMatcherSource(Matcher initial_matcher) {
		super(initial_matcher);	// TODO: implement
	}

	//    /**
//     * {@inheritDoc}
//     */
//    public OrMatcherSource(Matcher one, Matcher two) {
//        super(one, two);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public OrMatcherSource(Matcher[] matchers) {
//        super(matchers);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public OrMatcherSource(EventList matcher_list) {
//        super(matcher_list);
//    }
//
//    public boolean matches(Object item) {
//        Matcher[] delegates = delegates();
//        if (delegates.length == 0) return true;     // always match if no delegates
//
//        for (int i = 0; i < delegates.length; i++) {
//            if (delegates[ i ].matches(item)) return true;
//        }
//
//        return false;        // nothing matched
//    }
//
//    public void cleared(Matcher source) {
//        // It only takes one to be cleared for the whole thing to clear
//        fireCleared();
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
