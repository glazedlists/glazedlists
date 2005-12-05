/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

// to proxy access to listener registration
import org.eclipse.swt.events.SelectionListener;

/**
 * This interface is used by the Viewer classes that represent
 * SWT widgets that provide selection functionality.  SWT doesn't
 * seperate selection in the inheritance heirarchy.  As such, this
 * interface is used to access selection logic in a common and
 * widget-unaware manner.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
interface Selectable {

    /**
     * Adds the provided {@link SelectionListener} to the underlying
     * widget.
     */
    public void addSelectionListener(SelectionListener listener);

    /**
     * Removes the provided {@link SelectionListener} from the underlying
     * widget.
     */
    public void removeSelectionListener(SelectionListener listener);

    /**
     * Gets the index of the most recently selected item.
     */
    public int getSelectionIndex();

    /**
     * Gets an array of indices that are selected.
     */
    public int[] getSelectionIndices();

    /**
     * Gets the style bitmask.  This is necessary as the selection mode
     * is specified via style constants.
     */
    public int getStyle();

    /**
     * Selects the item at index.
     */
    public void select(int index);

    /**
     * Deselects the item at index.
     */
    public void deselect(int index);

}
