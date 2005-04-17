/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;

/**
 * A facility for modifying the {@link Matcher}s which specify the behaviour of a
 * {@link ca.odell.glazedlists.FilterList FilterList}.
 *
 * <p>Although this interface is called an <i>Editor</i>, the
 * implementor should create new {@link Matcher} instances on each
 * change rather than modifying the existing {@link Matcher}s. This is because
 * {@link Matcher}s work best when they are immutable. Further information
 * on this immutability can be found in the {@link Matcher Matcher Javadoc}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface MatcherEditor {
    
    /**
     * Add a listener to be notified when this editor's {@link Matcher} changes.
     */
	public void addMatcherEditorListener(MatcherEditorListener listener);
    
    /**
     * Remove the listener so that it no longer receives notification when the
     * {@link Matcher} changes.
     */
	public void removeMatcherEditorListener(MatcherEditorListener listener);

	/**
     * Return the current {@link Matcher} specified by this {@link MatcherEditor}.
     *
     * @return a non-null {@link Matcher}.
	 */
	public Matcher getMatcher();
}
