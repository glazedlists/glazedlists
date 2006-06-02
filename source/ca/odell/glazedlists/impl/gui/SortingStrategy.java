/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.gui;

/**
 * Behaviour that defines how to interpret a mouse click on a table's header.
 *
 * <p>This interface is intentionally stateless so that a single
 * instance can be shared between multiple <code>TableComparatorChooser</code>s.
 *
 * <p>This interface is <strong>not</strong> designed to be implemented by
 * users of <code>TableComparatorChooser</code> and is not guaranteed to be
 * backward compatible between releases. Users are encouraged to use the
 * predefined constant values in
 * {@link ca.odell.glazedlists.gui.AbstractTableComparatorChooser}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface SortingStrategy {

    /**
     * This method is called each time a user attempts to adjust the sort order
     * enforced by a <code>TableComparatorChooser</code> by clicking in a table
     * header. The implementation is expected to adjust the
     * <code>sortingState</code> as necessary and call
     * {@link SortingState#fireSortingChanged()} afterward to honour the new
     * sorting state.
     *
     * @param sortingState an object which models the details regarding which
     *      columns are enforcing sort order
     * @param column the column that was clicked on
     * @param clicks the number of recorded mouse clicks
     * @param shift <tt>true</tt> if the shift key was down at the time of the click
     * @param control <tt>true</tt> if the control key was down at the time of the click
     */
    public void columnClicked(SortingState sortingState, int column, int clicks, boolean shift, boolean control);
}