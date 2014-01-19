/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A {@link Matcher} implementation that matches String objects if they are
 * non-null and non-empty. Use {@link #getInstance()} to obtain a singleton
 * instance.
 *
 * James Lemieux
 */
public class NonNullAndNonEmptyStringMatcher implements Matcher<String> {

    /** Singleton instance of NonNullAndNonEmptyStringMatcher. */
    private static final Matcher<String> INSTANCE = new NonNullAndNonEmptyStringMatcher();

    /**
     * Return a singleton instance.
     */
    public static Matcher<String> getInstance() {
        return INSTANCE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean matches(String item) {
        return item != null && item.length() > 0;
    }
}