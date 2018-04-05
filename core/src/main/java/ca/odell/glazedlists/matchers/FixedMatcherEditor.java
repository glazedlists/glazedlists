/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

/**
 * A {@link MatcherEditor} whose {@link Matcher} never changes.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
final class FixedMatcherEditor<E> extends AbstractMatcherEditor<E> {

    /**
     * Create a {@link FixedMatcherEditor} for the specified {@link Matcher}.
     */
    FixedMatcherEditor(Matcher<E> matcher) {
        super.fireChanged(matcher);
    }
}