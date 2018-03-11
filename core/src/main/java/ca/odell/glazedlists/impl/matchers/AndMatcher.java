/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A Matcher that matches if all child elements match.
 */
public class AndMatcher<E> implements Matcher<E> {

    /** The Matchers being combined with an "and" operator. */
    private final Matcher<? super E>[] matchers;

    @SafeVarargs
    public AndMatcher(Matcher<? super E>... matchers) {
        this.matchers = matchers;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(E item) {
        for (int i = 0; i < matchers.length; i++) {
            if (!matchers[i].matches(item))
                return false;
        }
        return true;
    }
}