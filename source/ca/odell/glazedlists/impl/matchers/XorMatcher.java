/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:30:19 AM
 */
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches if and only if <i>either</i> of the
 * provided matcher's filters apply (but not if both match). If no delegate {@link Matcher
 * Matchers} are configured, this will always match.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see OrMatcher
 * @see AndMatcher
 */
public final class XorMatcher extends CompositeMatcher implements MatcherListener {
    /**
     * {@inheritDoc}
     */
    public XorMatcher(Matcher one, Matcher two) {
        super(one, two);
    }

    /**
     * {@inheritDoc}
     */
    public XorMatcher(Matcher[] matchers) {
        super(matchers);
    }

    /**
     * {@inheritDoc}
     */
    public XorMatcher(EventList matcher_list) {
        super(matcher_list);
    }

    public boolean matches(Object item) {
        Matcher[] delegates = delegates();
        if (delegates.length == 0) return true;     // always match if no delegates

        // Exit if we find a fail and a match
        boolean found_miss = false;
        boolean found_match = false;
        for (int i = 0; i < delegates.length; i++) {
            if (delegates[ i ].matches(item)) {
                found_match = true;
            } else {
                found_miss = true;
            }

            if (found_match && found_miss) return true;
        }

        return false;        // didn't find a miss and a match
    }

    public void cleared(Matcher source) {
        // Since all could now match (which would make this object not match), must
        // check the whole thing
        fireChanged();
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
