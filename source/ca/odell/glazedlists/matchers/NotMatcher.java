/*
 * Copyright(c) 2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Mar 23, 2005 - 10:04:47 AM
 */
package ca.odell.glazedlists.matchers;


import ca.odell.glazedlists.Matcher;

/**
 * A simple {@link Matcher} implementation that inverts the result of another
 * {@link Matcher Matcher's} {@link Matcher#matches(Object)} method.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class NotMatcher implements Matcher {
	private Matcher parent;

	public NotMatcher(Matcher parent) {
		if (parent == null ) throw new IllegalArgumentException("Parent cannot be null" );

		this.parent = parent;
	}


	public boolean matches(Object item) {
		return !parent.matches(item);
	}


	public String toString() {
		return "[NotMatcher parent:" + parent + "]";
	}
}
