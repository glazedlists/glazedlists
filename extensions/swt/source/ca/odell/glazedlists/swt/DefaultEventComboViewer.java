/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
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

    /** the label provider to pretty print a String representation of each Object */
    private ILabelProvider labelProvider;

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * the result of calling toString() on the Objects found in source.
     * 
     * @param source the EventList that provides the elements
     * @param combo the combo box
     */
    public DefaultEventComboViewer(EventList<E> source, Combo combo) {
        this(source, combo, new LabelProvider());
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * formatted using the provided {@link ILabelProvider}.
     * 
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param labelProvider a LabelProvider for formatting the displayed values
     *            
     * @see ILabelProvider
     * @see GlazedListsSWT#beanLabelProvider(String)
     */
    public DefaultEventComboViewer(EventList<E> source, Combo combo, ILabelProvider labelProvider) {
    	this(source, combo, labelProvider, false);
    }

    /**
     * Binds the contents of a {@link Combo} component to an {@link EventList}
     * source.  This allows the selection choices in a {@link Combo} to change
     * dynamically to reflect chances to the source {@link EventList}.  The
     * {@link String} values displayed in the {@link Combo} compoment will be
     * formatted using the provided {@link ILabelProvider}.
     * 
     * @param source the EventList that provides the elements
     * @param combo the combo box
     * @param labelProvider an optional LabelProvider for formatting the displayed values
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     *
     * @see ILabelProvider
     * @see GlazedListsSWT#beanLabelProvider(String)
     */
	protected DefaultEventComboViewer(EventList<E> source, Combo combo,
			ILabelProvider labelProvider, boolean disposeSource) {
		this.disposeSource = disposeSource;
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventComboViewer
        source.getReadWriteLock().readLock().lock();
        try {
            this.source = source;
            this.combo = combo;
            this.labelProvider = labelProvider;

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
     * Adds the value at the specified row.
     */
    private void addRow(int row, Object value) {
        combo.add(labelProvider.getText(value), row);
    }

    /**
     * Updates the value at the specified row.
     */
    private void updateRow(int row, Object value) {
        combo.setItem(row, labelProvider.getText(value));
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