/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:31:33 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches for the inverse elements of the
 * given {@link ca.odell.glazedlists.Matcher}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public final class NotMatcher extends AbstractMatcher implements MatcherListener {
    private final Matcher source;

    public NotMatcher(Matcher source) {
        if (source == null) throw new IllegalArgumentException("Matcher cannot be null");

        this.source = source;
    }

    public boolean matches(Object item) {
        return !source.matches(item);
    }

    public void cleared(Matcher source) {
        // Clearing the source actually means that everything should be hidden
        fireConstrained();
    }

    public void changed(Matcher source) {
        fireChanged();
    }

    public void constrained(Matcher source) {
        fireRelaxed();
    }

    public void relaxed(Matcher source) {
        fireConstrained();
    }
}
