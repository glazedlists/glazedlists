/*
 * Copyright(c) 2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Mar 23, 2005 - 4:55:30 PM
 */
package ca.odell.glazedlists.matchers;


import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;
import ca.odell.glazedlists.event.MatcherSourceListener;

/**
 * @author reden
 */
public class AndMatcherSource extends AbstractValueMatcherSource
	implements MatcherSourceListener {

	public AndMatcherSource(MatcherSource[] sources) {
		super(TrueMatcher.getInstance(), false, null);

		if (sources != null) {
			// Use null rather than an empty array for AbstractValueMatcherSource can
			// be intelligent
			if (sources.length == 0) sources = null;
			else {
				for(int i = 0; i < sources.length; i++ ) {
					sources[i].addMatcherSourceListener(this);
				}
			}
		}

		setValue(sources);
	}

	public void cleared(MatcherSource source) {
		fireChanged(createMatcher(getValue()));
	}

	public void changed(Matcher new_matcher, MatcherSource source) {
		fireChanged(createMatcher(getValue()));
	}

	public void constrained(Matcher new_matcher, MatcherSource source) {
		if (isLogicInverted()) {
			fireRelaxed(createMatcher(getValue()));
		} else {
			fireConstrained(createMatcher(getValue()));
		}
	}

	public void relaxed(Matcher new_matcher, MatcherSource source) {
		if (isLogicInverted()) {
			fireConstrained(createMatcher(getValue()));
		} else {
			fireRelaxed(createMatcher(getValue()));
		}
	}


	protected Matcher createMatcher(Object value) {
		MatcherSource[] sources = (MatcherSource[]) value;

		Matcher[] matchers = new Matcher[sources.length];
		for (int i = 0; i < matchers.length; i++) {
			matchers[i] = sources[i].getCurrentMatcher();
		}

		return new AndMatcher(matchers);
	}




	private static class AndMatcher implements Matcher {
		private final Matcher[] matchers;

		AndMatcher(Matcher[] matchers) {
			this.matchers = matchers;
		}

		public boolean matches(Object item) {
			for(int i = 0; i < matchers.length; i++ ) {
				if (!matchers[i].matches(item)) return false;
			}

			return true;
		}
	}
}
