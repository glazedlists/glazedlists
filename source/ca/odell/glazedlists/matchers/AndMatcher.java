/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:31:01 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches if <i>both</i> of the provided
 * matcher's filters apply. If no delegate {@link Matcher Matchers} are configured, this
 * will always match.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see OrMatcher
 * @see XorMatcher
 */
public final class AndMatcher extends CompositeMatcher implements MatcherListener {
    /**
     * {@inheritDoc}
     */
    public AndMatcher(Matcher one, Matcher two) {
        super(one, two);
    }

    /**
     * {@inheritDoc}
     */
    public AndMatcher(Matcher[] matchers) {
        super(matchers);
    }

    /**
     * {@inheritDoc}
     */
    public AndMatcher(EventList matcher_list) {
        super(matcher_list);
    }


    public boolean matches(Object item) {
        Matcher[] delegates = delegates();
        if (delegates.length == 0) return true;     // always match if no delegates

        for (int i = 0; i < delegates.length; i++) {
            if (!delegates[ i ].matches(item)) return false;
        }

        return true;        // everything matched (or there were no delegate matchers)
    }

    public void cleared(Matcher source) {
        // One Matcher was cleared, but the other(s) may not be...
        fireRelaxed();
    }

    public void changed(Matcher source) {
        fireChanged();
    }

    public void constrained(Matcher source) {
        fireConstrained();
    }

    public void relaxed(Matcher source) {
        fireRelaxed();
    }
}
