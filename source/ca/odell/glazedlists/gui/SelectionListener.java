/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

/**
 * A generic interface to respond to changes in selection that doesn't
 * require including a particular GUI toolkit.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface SelectionListener {

	/**
	 * Notifies this SelectionListener of a change in selection.
	 *
	 * @param changeStart The first zero-relative index affected by a change in selection.
	 * @param changeEnd   The last zero-relative index affected by a change in selection.
	 */
	public void selectionChanged(int changeStart, int changeEnd);

}