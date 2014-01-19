/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;

/**
 * A combo box model for displaying Glazed Lists in a combo box.
 *
 * <p>The DefaultEventComboBoxModel class is <strong>not thread-safe</strong>. Unless
 * otherwise noted, all methods are only safe to be called from the event
 * dispatch thread. To do this programmatically, use {@link SwingUtilities#invokeAndWait(Runnable)}
 * and wrap the source list (or some part of the source list's pipeline) using
 * {@link GlazedListsSwing#swingThreadProxyList(EventList)}.
 *
 * <p>The implementation of {@link #setSelectedItem} and {@link #getSelectedItem}
 * is not in any way tied to the contents of the list.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class DefaultEventComboBoxModel<E> extends DefaultEventListModel<E> implements ComboBoxModel {

    /** the currently selected item which typically belong to the source list */
    private Object selected;

    /**
     * Creates a new combo box model that contains the elements of the given
     * <code>source</code> and tracks further changes made to it.
     *
     * @param source the EventList that provides the elements
     */
    public DefaultEventComboBoxModel(EventList<E> source) {
        this(source, false);
    }

    /**
     * Creates a new combo box model that contains the elements of the given
     * <code>source</code> and tracks further changes made to it.
     *
     * @param source the EventList that provides the elements
     * @param disposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     */
    protected DefaultEventComboBoxModel(EventList<E> source, boolean disposeSource) {
        super(source, disposeSource);
    }

    /**
     * Gets the currently selected item.
     */
    @Override
    public Object getSelectedItem() {
        return selected;
    }

    /**
     * Sets the currently selected item.
     *
     * <p>The selection notification process is very much a hack. This fires
     * a ListDataEvent where the range is between -1 and -1. This is identical
     * to the notification process used by the {@link DefaultComboBoxModel}.
     */
    @Override
    public void setSelectedItem(Object selected) {
        // if the selected item isn't actually changing values, avoid the work
        if (this.selected == selected) {
            return;
        }

        this.selected = selected;
        listDataEvent.setRange(-1, -1);
        listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);
        fireListDataEvent(listDataEvent);
    }
}