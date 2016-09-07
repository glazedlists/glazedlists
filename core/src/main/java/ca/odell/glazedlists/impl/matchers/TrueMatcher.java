/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.matchers;


import ca.odell.glazedlists.matchers.Matcher;

/**
 * A {@link Matcher} implementation that always matches. Use {@link #getInstance()} to
 * obtain a singleton instance.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public final class TrueMatcher<E> implements Matcher<E> {

	/** Singleton instance of TrueMatcher. */
	private static final Matcher INSTANCE = new TrueMatcher();

    private TrueMatcher() {}

    /**
	 * Return a singleton instance.
	 */
	public static <E> Matcher<E> getInstance() {
		return INSTANCE;
	}

    /** {@inheritDoc} */
	@Override
    public boolean matches(E item) {
		return true;
	}
}