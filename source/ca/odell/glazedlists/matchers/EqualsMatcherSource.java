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
public class EqualsMatcherSource extends AbstractMatcherSource {
	/**
	 * The current value we're matching against.
	 */
	private Object match_value;
	/**
	 * If false, objects match if they are not equal to the match value.
	 */
	private boolean match_on_equal;
	/**
	 * If true, all items will match if no match value is set. Otherwise, nothing will.
	 */
	private boolean match_on_no_value;


	/**
	 * Construct an instance with no initial match value that use {@link Object#equals} for
	 * comparisons.
	 */
	public EqualsMatcherSource() {
		this(null, true, true);
	}

	/**
	 * Construct an instance with an initial match value that use {@link Object#equals} for
	 * comparisons.
	 *
	 * @param match_value The initial value to match, or null if none. See {@link
	 *                    #setMatchValue(Object)} for complete rules.
	 */
	public EqualsMatcherSource(Object match_value) {
		this(match_value, true, true);
	}

	/**
	 * Construct an instance.
	 *
	 * @param match_value           The initial value to match, or null if none. See {@link
	 *                              #setMatchValue(Object)} for complete rules.
	 * @param match_on_equal        If true, items will match if they are equal to the
	 *                              <tt>match_value</tt>, otherwise they will match if they
	 *                              are not equal.
	 * @param match_on_no_threshold Determines whether a null threshold causes all elements
	 *                              to match (true) or be hidden (false).
	 */
	public EqualsMatcherSource(Object match_value, boolean match_on_equal,
		boolean match_on_no_threshold) {

		super(new EqualsMatcher(match_value, match_on_equal, match_on_no_threshold));

		this.match_value = match_value;
		this.match_on_equal = match_on_equal;
		this.match_on_no_value = match_on_no_threshold;
	}


	/**
	 * Update the match value. The object used may not change its behavior while used by the
	 * matcher. That is, equals() must always return the same value for a given object.
	 * Immutable objects are highly recommended to avoid unintended, unpredicatble behavior
	 * caused by accidentally changing the state of the value.
	 */
	public synchronized void setMatchValue(Object match_value) {
		Object old_threshold = this.match_value;
		this.match_value = match_value;

		EqualsMatcher matcher = resetCurrentMatcher();

		if (match_value == null) {
			// If there didn't used to be a match value, it's a noop
			if (old_threshold == null) return;

			// Either cleared filter or restricted it (all the way!)
			if (match_on_no_value) {
				fireCleared();
			} else {
				fireConstrained(matcher);
			}
		} else if (old_threshold == null) {
			if (match_on_no_value) {
				fireConstrained(matcher);
			} else {
				fireRelaxed(matcher);
			}
		} else {
			fireChanged(matcher);
		}
	}

	/**
	 * @see #setMatchValue(Object)
	 */
	public Object getMatchValue() {
		return ((EqualsMatcher) getCurrentMatcher()).match_value;
	}


	/**
	 * Update whether items match if they are equal to (when <tt>true</tt>) or not equal to
	 * (when <tt>false</tt>) the {@link #setMatchValue match value}.
	 */
	public synchronized void setMatchOnEqual(boolean match_on_equal) {
		if (this.match_on_equal == match_on_equal) return;

		this.match_on_equal = match_on_equal;

		EqualsMatcher matcher = resetCurrentMatcher();

		fireChanged(matcher);
	}

	/**
	 * @see #setMatchOnEqual(boolean)
	 */
	public boolean getMatchOnEqual() {
		return match_on_equal;
	}


	/**
	 * Update whether or not an empty (null) value indicates that all elements should match
	 * (true) or be hidden (false).
	 */
	public synchronized void setMatchOnNoValue(boolean match_on_no_value) {
		if (this.match_on_no_value == match_on_no_value) return;

		this.match_on_no_value = match_on_no_value;

		EqualsMatcher matcher = resetCurrentMatcher();

		// Only need to update if there is current no match value
		if (getMatchValue() == null) fireChanged(matcher);
	}

	/**
	 * @see #setMatchOnNoValue(boolean)
	 */
	public boolean getMatchOnNoValue() {
		return match_on_no_value;
	}


	private EqualsMatcher resetCurrentMatcher() {
		EqualsMatcher matcher =
			new EqualsMatcher(match_value, match_on_equal, match_on_no_value);

		setCurrentMatcher(matcher);

		return matcher;
	}


	private static class EqualsMatcher implements Matcher {
		private final Object match_value;
		private final boolean match_on_equal;
		private final boolean match_on_no_value;

		private EqualsMatcher(Object value, boolean match_on_equal,
										  boolean match_on_no_value) {

			this.match_value = value;
			this.match_on_equal = match_on_equal;
			this.match_on_no_value = match_on_no_value;
		}


		public boolean matches(Object item) {
			if (match_value == null) {
				return match_on_no_value;
			} else {
				boolean equal = match_value.equals(item);

				return match_on_equal ? equal : !equal;
			}
		}
	}
}
