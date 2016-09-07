/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import junit.framework.Assert;

/**
 * Count matcher events, inserting a delay if requested to test multithreaded
 * matchers.
 */
public class CountingMatcherEditorListener<E> implements MatcherEditor.Listener<E> {
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
	public CountingMatcherEditorListener(long delay_ms) {
		this.delay_ms = delay_ms;
	}

    /**
     * Create a {@link CountingMatcherEditorListener} with no delay.
     */
    public CountingMatcherEditorListener() {
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
	public void resetCounterState() {
		matchAll = 0;
		matchNone = 0;
		changed = 0;
		constrained = 0;
		relaxed = 0;
	}

    /**
     * Returns the total number of changes seen by this listener since the last
     * time it was reset, regardless of the actual change types.
     */
    public int getChangeCount() {
        return matchAll + matchNone + changed + constrained + relaxed;
    }

    /**
	 * Check the change counters match the expected values.
	 */
	public void assertCounterState(int matchAll, int matchNone, int changed, int constrained, int relaxed) {
		Assert.assertEquals(matchAll, this.matchAll);
		Assert.assertEquals(matchNone, this.matchNone);
		Assert.assertEquals(changed, this.changed);
		Assert.assertEquals(constrained, this.constrained);
		Assert.assertEquals(relaxed, this.relaxed);
	}

	@Override
    public void changedMatcher(MatcherEditor.Event<E> matcherEvent) {
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