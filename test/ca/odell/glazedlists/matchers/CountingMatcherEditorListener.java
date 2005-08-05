/*
 * Copyright(c) 2005, NEXVU Technologies
 * All rights reserved.
 */
package ca.odell.glazedlists.matchers;

import junit.framework.Assert;


/**
 * Count matcher events, inserting a delay if requested to test multithreaded
 * matchers.
 */
class CountingMatcherEditorListener implements MatcherEditor.Listener {
	private int matchAll = 0;
	private int matchNone = 0;
	private int changed = 0;
	private int constrained = 0;
	private int relaxed = 0;

	private final long delay_ms;

    /**
     * Create a {@link CountingMatcherEditorListener} that blocks for the specified
     * duration whenever a matcher event is received.
     */
	CountingMatcherEditorListener(long delay_ms) {
		this.delay_ms = delay_ms;
	}
    /**
     * Create a {@link CountingMatcherEditorListener} with no delay.
     */
    CountingMatcherEditorListener() {
        this(0);
    }

	private void delay() {
		if (delay_ms == 0) return;

		try {
			Thread.sleep(delay_ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reset the change counters to zero.
	 */
	void resetCounterState() {
		matchAll = 0;
		matchNone = 0;
		changed = 0;
		constrained = 0;
		relaxed = 0;
	}

	/**
	 * Check the change counters match the expected values.
	 */
	void assertCounterState(int matchAll, int matchNone, int changed, int constrained,
		int relaxed) {

		Assert.assertEquals(matchAll, this.matchAll);
		Assert.assertEquals(matchNone, this.matchNone);
		Assert.assertEquals(changed, this.changed);
		Assert.assertEquals(constrained, this.constrained);
		Assert.assertEquals(relaxed, this.relaxed);
	}


	public void changedMatcher(MatcherEditor.Event matcherEvent) {
		switch (matcherEvent.getType()) {
			case MatcherEditor.Event.CONSTRAINED: this.constrained++; break;
			case MatcherEditor.Event.RELAXED: this.relaxed++; break;
			case MatcherEditor.Event.CHANGED: this.changed++; break;
			case MatcherEditor.Event.MATCH_ALL: this.matchAll++; break;
			case MatcherEditor.Event.MATCH_NONE: this.matchNone++; break;
		}

		this.delay();
	}
}
