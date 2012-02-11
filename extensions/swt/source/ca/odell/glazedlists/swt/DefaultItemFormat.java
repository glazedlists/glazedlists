/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

/**
 * Default implementation of {@link ItemFormat} that converts an element by
 * calling <code>toString()</code> on it. If the element is <code>null</code>,
 * by default, the empty string is returned, but another default value can be
 * specified.
 *
 * @see #DefaultItemFormat(String)
 *
 * @author Holger Brands
 */
public class DefaultItemFormat<E> implements ItemFormat<E> {

	/** string value to be used for a <code>null</code> element. */
	private final String valueForNullElement;

	/**
	 * Default constructor which uses the empty string as representation for
	 * <code>null</code> elements.
	 */
	public DefaultItemFormat() {
		this("");
	}

	/**
	 * Constructor which takes a default value for representing a
	 * <code>null</code> element.
	 *
	 * @param valueForNullElement string value to be used for a <code>null</code> element
	 */
	public DefaultItemFormat(String valueForNullElement) {
		this.valueForNullElement = valueForNullElement;
	}

	/** @inheritDoc */
	public String format(E element) {
		return (element == null) ? valueForNullElement : element.toString();
	}
}
