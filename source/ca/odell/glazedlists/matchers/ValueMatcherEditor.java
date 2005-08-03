/* Glazed Lists                                                      (c) 2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

/**
 * Indicates that a {@link MatcherEditor} matches based on a value that can be dynamically
 * set. Aside from implementing the methods defined in this interface there is an
 * additional requirement that implementing classes must match everything when no match
 * value is set. {@link AbstractValueMatcherEditor} can be used to correctly fulfill this
 * requirement.
 *
 * @author <a href="mailto:rob@starlight-systems.com>Rob Eden</a>
 *
 * @see AbstractValueMatcherEditor
 * @see MatcherEditor
 */
public interface ValueMatcherEditor extends MatcherEditor {
	/**
	 * Indicates whether or not the basic logic of the matcher is inverted as it relates
	 * to the value set for it. Not that this is <strong>not</strong> the same as wrapping
	 * a {@link ca.odell.glazedlists.impl.matchers.NotMatcher} around the {@link
	 * ca.odell.glazedlists.Matcher} returned by {@link #getMatcher()}. See the class
	 * documentation for more details.
	 */
	public boolean isLogicInverted();

	/**
	 * @see #isLogicInverted()
	 */
	public void setLogicInverted(boolean logic_inverted);
}