/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:39:25 AM
 */
package ca.odell.glazedlists.matchers;


import ca.odell.glazedlists.Matcher;

/**
 * A {@link ca.odell.glazedlists.Matcher} that filters elements based on whether they are
 * equal to a given value.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EqualsMatcherSource extends AbstractValueMatcherSource {
	/**
	 * Construct an instance with no initial match value that use {@link Object#equals} for
	 * comparisons.
	 */
	public EqualsMatcherSource() {
		this(null, true);
	}

	/**
	 * Construct an instance with an initial match value that use {@link Object#equals} for
	 * comparisons.
	 *
	 * @param match_value The initial value to match, or null if none. See {@link
	 *                    #setMatchValue(Object)} for complete rules.
	 */
	public EqualsMatcherSource(Object match_value) {
		this(match_value, true);
	}

	/**
	 * Construct an instance.
	 *
	 * @param match_value           The initial value to match, or null if none. See {@link
	 *                              #setMatchValue(Object)} for complete rules.
	 * @param logic_inverted        If false, items will match if they are equal to the
	 *                              <tt>match_value</tt>, otherwise they will match if they
	 *                              are not equal.
	 */
	public EqualsMatcherSource(Object match_value, boolean logic_inverted) {
		super(new EqualsMatcher(match_value), logic_inverted, match_value);
	}


	/**
	 * Update the match value. The object used may not change its behavior while used by the
	 * matchersource. That is, equals() must always return the same value for a given object.
	 * Immutable objects are highly recommended to avoid unintended, unpredicatble behavior
	 * caused by accidentally changing the state of the value.
	 */
	public synchronized void setMatchValue(Object match_value) {
		boolean need_to_fire_update = setValue(match_value);

		if (need_to_fire_update) {
			fireChanged(getCurrentMatcher());
		}
	}

	/**
	 * @see #setMatchValue(Object)
	 */
	public Object getMatchValue() {
		return super.getValue();
	}


	/**
	 * {@inheritDoc}
	 */
	protected Matcher createMatcher(Object value) {
		return new EqualsMatcher(value);
	}


	private static class EqualsMatcher implements Matcher {
		private final Object match_value;

		private EqualsMatcher(Object value) {
			this.match_value = value;

			if (value == null) throw new IllegalArgumentException("Value cannot be null" );
		}

		public boolean matches(Object item) {
			return match_value.equals(item);
		}


		public String toString() {
			return "[EqualsMatcher value:" + match_value + "]";
		}
	}
}
