/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:31:33 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;
import ca.odell.glazedlists.event.MatcherSourceListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches for the inverse elements of the
 * given {@link ca.odell.glazedlists.Matcher}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public final class NotMatcherSource extends AbstractMatcherSource implements MatcherSourceListener {
	protected NotMatcherSource(Matcher initial_matcher) {
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
	//    private final Matcher source;
//
//    public NotMatcherSource(Matcher source) {
//        if (source == null) throw new IllegalArgumentException("Matcher cannot be null");
//
//        this.source = source;
//    }
//
//    public boolean matches(Object item) {
//        return !source.matches(item);
//    }
//
//    public void cleared(Matcher source) {
//        // Clearing the source actually means that everything should be hidden
//        fireConstrained();
//    }
//
//    public void changed(Matcher source) {
//        fireChanged();
//    }
//
//    public void constrained(Matcher source) {
//        fireRelaxed();
//    }
//
//    public void relaxed(Matcher source) {
//        fireConstrained();
//    }
}
