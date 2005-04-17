/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.matchers;


import ca.odell.glazedlists.Matcher;

/**
 * A {@link Matcher} implementation that always matches. Use {@link #getInstance()} to
 * obtain a singleton instance.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class TrueMatcher implements Matcher {
	/**
	 * Singleton instance of TrueMatcher.
	 */
	private static final Matcher INSTANCE = new TrueMatcher();


	/**
	 * Return a singleton instance.
	 */
	public static Matcher getInstance() {
		return INSTANCE;
	}



	public boolean matches( Object item ) {
		return true;
	}
}
