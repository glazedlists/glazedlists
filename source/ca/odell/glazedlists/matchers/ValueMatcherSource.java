/*
 * Copyright(c) 2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Mar 22, 2005 - 3:55:51 PM
 */
package ca.odell.glazedlists.matchers;


import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.MatcherSource;

/**
 * Indicates that a {@link MatcherSource} matches based on a value that can be dynamically
 * set. Aside from implementing the methods defined in this interface there is an
 * additional that implementing classes must match everything when no match value is set.
 * {@link AbstractValueMatcherSource} can be used to correctly fulfill this requirement.
 *
 * @see AbstractValueMatcherSource
 * @see MatcherSource
 *
 * @author <a href="mailto:rob@starlight-systems.com>Rob Eden</a>
 */
public interface ValueMatcherSource extends MatcherSource {
	/**
	 * Indicates whether or not the basic logic of the matcher is inverted as it relates
	 * to the value set for it. Not that this is <strong>not</strong> the same as
	 * wrapping a {@link NotMatcher} around the {@link Matcher} returned by
	 * {@link #getCurrentMatcher()}. See the class documentation for more details.
	 */
	public boolean isLogicInverted();

	/**
	 * @see #isLogicInverted()
	 */
	public void setLogicInverted(boolean logic_inverted);
}
