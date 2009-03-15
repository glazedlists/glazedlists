/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.matchers.Matcher;

/**
 * A simple {@link Matcher} implementation that only matches null objects.
 *
 * @author James Lemieux
 */
public final class NullMatcher<E> implements Matcher<E> {

	/** Singleton instance of NullMatcher. */
	private static final Matcher INSTANCE = new NullMatcher();

    private NullMatcher() {}

    /**
	 * Return a singleton instance.
	 */
	public static <E> Matcher<E> getInstance() {
		return (Matcher<E>) INSTANCE;
	}

    /** {@inheritDoc} */
	public boolean matches(E item) {
		return item == null;
	}

    /** {@inheritDoc} */
	@Override
    public String toString() {
		return "[NullMatcher]";
	}
}