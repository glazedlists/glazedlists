/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import java.util.EventListener;

/**
 * A MatcherEditorListener handles changes fired by a {@link MatcherEditor}.
 * The most notable implementation will be {@link ca.odell.glazedlists.FilterList FilterList}
 * which uses these events to update its state.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 * @see ca.odell.glazedlists.Matcher
 */
public interface MatcherEditorListener extends EventListener {

   /**
    * Indicates a changes has occurred in the Matcher produced by the
    * MatcherEditor.
    *
    * @param matcherEvent a MatcherEvent describing the change in the Matcher
    *      produced by the MatcherEditor
    */
    public void changedMatcher(MatcherEvent matcherEvent);
}