/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// to proxy access to listeners
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Display;

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
     * Gets the Display for this Selectable widget
     */
    public Display getDisplay();

    /**
     * Gets the number of items selected.
     */
    public int getSelectionCount();

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
     * Returns whether or not the item at index is selected.
     */
    public boolean isSelected(int index);

    /**
     * Clears the selection
     */
    public void deselectAll();

    /**
     * Selects items at the indices specified in the array.  This
     * appends to the currect selection rather than replacing it.
     */
    public void select(int[] selectionIndices);
}
