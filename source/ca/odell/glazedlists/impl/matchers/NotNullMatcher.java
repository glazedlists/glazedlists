/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A simple {@link Matcher} implementation that only matches non-null objects.
 *
 * @author James Lemieux
 */
public final class NotNullMatcher<E> implements Matcher<E> {

    /** Singleton instance of NotNullMatcher. */
	private static final Matcher INSTANCE = new NotNullMatcher();

    private NotNullMatcher() {}

    /**
	 * Return a singleton instance.
	 */
	public static <E> Matcher<E> getInstance() {
		return (Matcher<E>) INSTANCE;
	}

    /** {@inheritDoc} */
	public boolean matches(E item) {
		return item != null;
	}

    /** {@inheritDoc} */
	@Override
    public String toString() {
		return "[NotNullMatcher]";
	}
}