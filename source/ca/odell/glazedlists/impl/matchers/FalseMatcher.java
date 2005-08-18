/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.Matcher;

/**
 * A {@link Matcher} implementation that never matches. Use {@link #getInstance()} to
 * obtain a singleton instance.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class FalseMatcher<E> implements Matcher<E> {

	/** Singleton instance of FalseMatcher. */
	private static final Matcher INSTANCE = new FalseMatcher();

    /**
	 * Return a singleton instance.
	 */
	public static <E> Matcher<E> getInstance() {
		return (Matcher<E>) INSTANCE;
	}

    /** {@inheritDoc} */
	public boolean matches(E item) {
        return false;
	}
}