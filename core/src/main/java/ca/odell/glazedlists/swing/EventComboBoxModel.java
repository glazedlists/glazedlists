/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;

/**
 * A combo box model for displaying Glazed Lists in a combo box.
 *
 * <p>The implementation of {@link #setSelectedItem} and {@link #getSelectedItem}
 * is not in any way tied to the contents of the list.
 *
 * @see <a href="http://publicobject.com/glazedlists/tutorial/">Glazed Lists Tutorial</a>
 *
 * @deprecated Use {@link DefaultEventComboBoxModel} instead. This class will be removed in the GL
 *             2.0 release. The wrapping of the source list with an EDT safe list has been
 *             determined to be undesirable (it is better for the user to provide their own EDT
 *             safe list).
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
@Deprecated
public class EventComboBoxModel<E> extends EventListModel<E> implements ComboBoxModel {

    /** the currently selected item which typically belong to the source list */
    private Object selected;

    /**
     * Creates a new combo box model that contains the elements of the given
     * <code>source</code> and tracks further changes made to it.
     */
    public EventComboBoxModel(EventList<E> source) {
        super(source);
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
        if (this.selected == selected)
            return;

        this.selected = selected;
        listDataEvent.setRange(-1, -1);
        listDataEvent.setType(ListDataEvent.CONTENTS_CHANGED);
        fireListDataEvent(listDataEvent);
    }
}