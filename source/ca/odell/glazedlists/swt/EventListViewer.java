/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.swt;

// for swt Lists
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionListener;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
// JFace label providers
import org.eclipse.jface.viewers.*;

/**
 * A view helper that displays an {@link EventList} in a {@link List}.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class EventListViewer implements ListEventListener, Selectable {

    /** the SWT List */
    private List list = null;

    /** the proxy moves events to the SWT user interface thread */
    private TransformedList swtSource = null;

    /** the formatter for list elements */
    private ILabelProvider labelProvider = null;

    /** For selection management */
    private SelectionList selectionList = null;

    /**
     * Creates a new List that displays and responds to changes in source.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     */
    public EventListViewer(EventList source, List list) {
        this(source, list, new LabelProvider());
    }

    /**
     * Creates a new List that displays and responds to changes in source.
     * List elements are formatted using the provided {@link ILabelProvider}.
     */
    public EventListViewer(EventList source, List list, ILabelProvider labelProvider) {
        swtSource = GlazedListsSWT.swtThreadProxyList(source, list.getDisplay());
        this.list = list;
        this.labelProvider = labelProvider;

        // Enable the selection lists
        selectionList = new SelectionList(swtSource, this);

        // setup initial values
        populateList();

        // listen for changes
        swtSource.addListEventListener(this);
    }

    /**
     * Populates the list with the original contents of the source event list.
     */
    private void populateList() {
        // set the initial data
        for(int i = 0; i < swtSource.size(); i++) {
            addRow(i, swtSource.get(i));
        }
    }

    /**
     * Gets the List's {@link ILabelProvider}.
     */
    public ILabelProvider getLabelProvider() {
        return labelProvider;
    }

    /**
     * Gets the List being managed by this {@link EventListViewer}.
     */
    public List getList() {
        return list;
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed Table that are not currently selected.
     */
    public EventList getDeselected() {
        return selectionList.getDeselected();
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed Table that are currently selected.
     */
    public EventList getSelected() {
        return selectionList.getSelected();
    }

    /**
     * Adds the value at the specified row.
     */
    private void addRow(int row, Object value) {
        list.add(labelProvider.getText(value), row);
    }

    /**
     * Updates the value at the specified row.
     */
    private void updateRow(int row, Object value) {
        list.setItem(row, labelProvider.getText(value));
    }

    /**
     * Removes the value at the specified row.
     */
    private void deleteRow(int row) {
        list.remove(row);
    }

    /**
     * When the source list is changed, this forwards the change to the
     * displayed List.
     */
    public void listChanged(ListEvent listChanges) {
        int firstModified = swtSource.size();
        swtSource.getReadWriteLock().readLock().lock();
        try {
            // Apply the list changes
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                if(changeType == ListEvent.INSERT) {
                    addRow(changeIndex, swtSource.get(changeIndex));
                    firstModified = Math.min(changeIndex, firstModified);
                } else if(changeType == ListEvent.UPDATE) {
                    updateRow(changeIndex, swtSource.get(changeIndex));
                } else if(changeType == ListEvent.DELETE) {
                    deleteRow(changeIndex);
                    firstModified = Math.min(changeIndex, firstModified);
                }
            }

            // Reapply selection to the List
            for(int i = firstModified;i < list.getItemCount();i++) {
                if(selectionList.isSelected(i)) {
                    list.select(i);
                } else {
                    list.deselect(i);
                }
            }
        } finally {
            swtSource.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        selectionList.invertSelection();
    }

    /** Methods for the Selectable Interface */

    /** {@inheritDoc} */
    public void addSelectionListener(SelectionListener listener) {
        list.addSelectionListener(listener);
    }

    /** {@inheritDoc} */
    public void removeSelectionListener(SelectionListener listener) {
        list.removeSelectionListener(listener);
    }

    /** {@inheritDoc} */
    public int getSelectionCount() {
        return list.getSelectionCount();
    }

    /** {@inheritDoc} */
    public int getSelectionIndex() {
        return list.getSelectionIndex();
    }

    /** {@inheritDoc} */
    public int[] getSelectionIndices() {
        return list.getSelectionIndices();
    }

    /** {@inheritDoc} */
    public int getStyle() {
        return list.getStyle();
    }

    /** {@inheritDoc} */
    public boolean isSelected(int index) {
        return list.isSelected(index);
    }

    /** {@inheritDoc} */
    public void deselectAll() {
        list.deselectAll();
    }

    /** {@inheritDoc} */
    public void select(int[] selectionIndices) {
        list.select(selectionIndices);
    }

    /**
     * Releases the resources consumed by this {@link EventListViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link EventListViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link EventListViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link EventListViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link EventListViewer} after it has been disposed.
     */
    public void dispose() {
        swtSource.dispose();
        if(!selectionList.isDisposed()) selectionList.dispose();
    }
}