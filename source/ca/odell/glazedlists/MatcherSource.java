/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003-2005 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;


import ca.odell.glazedlists.event.MatcherSourceListener;

/**
 * TODO: comment
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public interface MatcherSource {
	// TODO: comment
	public void addMatcherSourceListener(MatcherSourceListener listener);
	public void removeMatcherSourceListener(MatcherSourceListener listener);


	/**
	 * Return the current {@link Matcher} provided by the source. Null must
	 * <strong>never</strong> be returned.
	 */
	public Matcher getCurrentMatcher();
}
