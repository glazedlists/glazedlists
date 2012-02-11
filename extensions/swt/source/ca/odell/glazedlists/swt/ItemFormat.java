/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

/**
 * This interface is used by {@link DefaultEventListViewer} and
 * {@link DefaultEventComboViewer} to convert an element of the backing
 * EventList to a String format.
 *
 * @author Holger Brands
 */
public interface ItemFormat<E> {

	/**
	 * Converts a list element to a string.
	 *
	 * @param element the element to convert
	 * @return the string representation
	 */
	String format(E element);
}
