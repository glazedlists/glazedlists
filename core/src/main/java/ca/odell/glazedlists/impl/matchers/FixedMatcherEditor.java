/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * A {@link MatcherEditor} whose {@link Matcher} never changes.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class FixedMatcherEditor<E> extends AbstractMatcherEditor<E> {

    /**
     * Create a {@link FixedMatcherEditor} for the specified {@link Matcher}.
     */
    public FixedMatcherEditor(Matcher<E> matcher) {
        super.fireChanged(matcher);
    }
}