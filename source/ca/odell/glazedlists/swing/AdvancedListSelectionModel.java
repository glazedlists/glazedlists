/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.Matcher;

import javax.swing.ListSelectionModel;

/**
 * <code>AdvancedListSelectionModel</code> is an interface defining additional methods
 * for selection management beyond the standard {@link ListSelectionModel}.
 *
 * @author Holger Brands
 */
public interface AdvancedListSelectionModel<E> extends ListSelectionModel {

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
    EventList<E> getSelected();

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the selection state on mutation.
     *
     * Adding an item to this list selects it and removing an item deselects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown.
     */
    EventList<E> getTogglingSelected();

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the source list on mutation.
     *
     * Adding and removing items from this list performs the same operation on
     * the source list.
     */
    EventList<E> getDeselected();

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the selection state on mutation.
     *
     * Adding an item to this list deselects it and removing an item selects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown
     */
    EventList<E> getTogglingDeselected();

    /**
     * Set the EventSelectionModel as editable or not. This means that the user cannot
     * manipulate the selection by clicking. The selection can still be changed as
     * the source list changes.
     *
     * <p>Note that this will also disable the selection from being modified
     * <strong>programatically</strong>. Therefore you must use setEnabled(true) to
     * modify the selection in code.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns whether the EventSelectionModel is editable or not.
     */
    boolean getEnabled();

    /**
     * Add a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source element
     *      can be selected; <tt>false</tt> otherwise
     */
    void addValidSelectionMatcher(Matcher<E> validSelectionMatcher);

    /**
     * Remove a matcher which decides when source elements are valid for selection.
     *
     * @param validSelectionMatcher returns <tt>true</tt> if a source element
     *      can be selected; <tt>false</tt> otherwise
     */
    void removeValidSelectionMatcher(Matcher<E> validSelectionMatcher);

    /**
     * Inverts the current selection.
     */
    void invertSelection();

    /**
     * Releases the resources consumed by this {@link AdvancedListSelectionModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link AdvancedListSelectionModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link AdvancedListSelectionModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link AdvancedListSelectionModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link AdvancedListSelectionModel} after it has been disposed.
     */
    void dispose();

}
