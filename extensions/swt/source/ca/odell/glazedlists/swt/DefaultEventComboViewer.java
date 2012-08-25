/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import org.eclipse.swt.widgets.Combo;

/**
 * A view helper that displays an {@link EventList} in a {@link Combo} component.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author Holger Brands
 */
public class DefaultEventComboViewer<E> implements ListEventListener<E> {

	/** indicator to dispose source list */
    private boolean disposeSource;

    /** the SWT Combo component */
    private Combo combo;

    /** the EventList to respond to */
    protected EventList<E> source;

    /** the {@link ItemFormat} to pretty print a String representation of each Object */
    private ItemFormat<? super E> itemFormat;

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} component will be
     * the result of calling toString() on the Objects found in source.
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public DefaultEventComboViewer(EventList<E> source, Combo combo) {
        this(source, combo, new DefaultItemFormat<E>());
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} component will be
     * formatted using the provided {@link ItemFormat}.
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     *
     * @see ItemFormat
     * @see GlazedListsSWT#beanItemFormat(String)
     */
    public DefaultEventComboViewer(EventList<E> source, Combo combo, ItemFormat<? super E> itemFormat) {
    	this(source, combo, itemFormat, false);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} component will be
     * formatted using the provided {@link ItemFormat}.
     *
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param itemFormat an optional {@link ItemFormat} for formatting the displayed values
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     *
     * @see ItemFormat
     * @see GlazedListsSWT#beanItemFormat(String)
     */
	protected DefaultEventComboViewer(EventList<E> source, Combo combo,
			ItemFormat<? super E> itemFormat, boolean disposeSource) {
		this.disposeSource = disposeSource;
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventComboViewer
        source.getReadWriteLock().readLock().lock();
        try {
            this.source = source;
            this.combo = combo;
            this.itemFormat = itemFormat;

            // set the initial data
            for(int i = 0, n = source.size(); i < n; i++) {
                addRow(i, source.get(i));
            }

            // listen for changes
            this.source.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets the Combo being managed by this {@link DefaulEventComboViewer}.
     */
    public Combo getCombo() {
        return combo;
    }

    /**
     * Gets the Combo's {@link ItemFormat}.
     */
    public ItemFormat<? super E> getItemFormat() {
        return itemFormat;
    }

    /**
     * Adds the value at the specified row.
     */
    private void addRow(int row, E value) {
        combo.add(itemFormat.format(value), row);
    }

    /**
     * Updates the value at the specified row.
     */
    private void updateRow(int row, E value) {
        combo.setItem(row, itemFormat.format(value));
    }

    /**
     * Removes the value at the specified row.
     */
    private void deleteRow(int row) {
        combo.remove(row);
    }

    /**
     * When the source combo is changed, this forwards the change to the
     * displayed combo.
     */
    public void listChanged(ListEvent<E> listChanges) {
        // apply the combo changes
        while (listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            if (changeType == ListEvent.INSERT)
                addRow(changeIndex, source.get(changeIndex));
            else if (changeType == ListEvent.UPDATE)
                updateRow(changeIndex, source.get(changeIndex));
            else if (changeType == ListEvent.DELETE)
                deleteRow(changeIndex);
        }
    }

    /**
     * Releases the resources consumed by this {@link DefaultEventComboViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link DefaultEventComboViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DefaultEventComboViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link DefaultEventComboViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DefaultEventComboViewer} after it has been disposed.
     */
    public void dispose() {
        source.removeListEventListener(this);
        if (disposeSource) source.dispose();
    }
}