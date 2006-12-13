/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

import java.util.Collection;

/**
 * A Matcher that matches if any child elements match.
 */
public class OrMatcher<E> implements Matcher<E> {

    /** The Matchers being combined with an "or" operator. */
    private final Matcher<? super E>[] matchers;

    public OrMatcher(Collection<Matcher<? super E>> matchers) {
        this.matchers = matchers.toArray(new Matcher[matchers.size()]);
    }
    
    /** {@inheritDoc} */
    public boolean matches(E item) {
        for (int i = 0; i < matchers.length; i++) {
            if (matchers[i].matches(item))
                return true;
        }
        return false;
    }
}