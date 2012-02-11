/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swt;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.List;

/**
 * A view helper that displays an {@link EventList} in a {@link List}.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author Holger Brands
 */
public class DefaultEventListViewer<E> implements ListEventListener<E> {

	/** indicator to dispose source list */
    private boolean disposeSource;

    /** the SWT List */
    private List list;

    /** the proxy moves events to the SWT user interface thread */
    protected EventList<E> source;

    /** the formatter for list elements */
    private ItemFormat<? super E> itemFormat;

    /** For selection management */
    private SelectionManager<E> selection;

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     *
     * @param source the EventList that provides the elements
     * @param list the list
     */
    public DefaultEventListViewer(EventList<E> source, List list) {
        this(source, list, new DefaultItemFormat<E>());
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements are formatted using the provided {@link ItemFormat}.
     *
     * @param source the EventList that provides the elements
     * @param list the list
     * @param itemFormat an {@link ItemFormat} for formatting the displayed values
     *
     * @see ItemFormat
     * @see GlazedListsSWT#beanItemFormat(String)
     */
    public DefaultEventListViewer(EventList<E> source, List list, ItemFormat<? super E> itemFormat) {
    	this(source, list, itemFormat, false);
    }

    /**
     * Creates a new List that displays and responds to changes in the source list.
     * List elements are formatted using the provided {@link ItemFormat}.
     *
     * @param source the EventList that provides the elements
     * @param list the list
     * @param itemFormat an optional {@link ItemFormat} for formatting the displayed values
     * @param diposeSource <code>true</code> if the source list should be disposed when disposing
     *            this model, <code>false</code> otherwise
     *
     * @see ItemFormat
     * @see GlazedListsSWT#beanItemFormat(String)
     */
    protected DefaultEventListViewer(EventList<E> source, List list, ItemFormat<? super E> itemFormat, boolean disposeSource) {
    	this.disposeSource = disposeSource;
        // lock the source list for reading since we want to prevent writes
        // from occurring until we fully initialize this EventListViewer
        source.getReadWriteLock().readLock().lock();
        try {
            this.source = source;
            this.list = list;
            this.itemFormat = itemFormat;

            // Enable the selection lists
            selection = new SelectionManager<E>(this.source, new SelectableList());

            // setup initial values
            for(int i = 0, n = this.source.size(); i < n; i++) {
                addRow(i, this.source.get(i));
            }

            // listen for changes
            this.source.addListEventListener(this);
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets the List's {@link ItemFormat}.
     */
    public ItemFormat<? super E> getItemFormat() {
        return itemFormat;
    }

    /**
     * Gets the List being managed by this {@link DefaultEventListViewer}.
     */
    public List getList() {
        return list;
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed Table that are not currently selected.
     */
    public EventList<E> getDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets an {@link EventList} that contains only deselected values and
     * modifies the selection state on mutation.
     *
     * Adding an item to this list deselects it and removing an item selects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown
     */
    public EventList<E> getTogglingDeselected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getTogglingDeselected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Provides access to an {@link EventList} that contains items from the
     * viewed Table that are currently selected.
     */
    public EventList<E> getSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Gets an {@link EventList} that contains only selected
     * values and modifies the selection state on mutation.
     *
     * Adding an item to this list selects it and removing an item deselects it.
     * If an item not in the source list is added an
     * {@link IllegalArgumentException} is thrown.
     */
    public EventList<E> getTogglingSelected() {
        source.getReadWriteLock().readLock().lock();
        try {
            return selection.getSelectionList().getTogglingSelected();
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }

    /**
     * Adds the value at the specified row.
     */
    private void addRow(int row, E value) {
        list.add(itemFormat.format(value), row);
    }

    /**
     * Updates the value at the specified row.
     */
    private void updateRow(int row, E value) {
        list.setItem(row, itemFormat.format(value));
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
    public void listChanged(ListEvent<E> listChanges) {
        int firstModified = source.size();
        // Apply the list changes
        while (listChanges.next()) {
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();

            if (changeType == ListEvent.INSERT) {
                addRow(changeIndex, source.get(changeIndex));
                firstModified = Math.min(changeIndex, firstModified);
            } else if (changeType == ListEvent.UPDATE) {
                updateRow(changeIndex, source.get(changeIndex));
            } else if (changeType == ListEvent.DELETE) {
                deleteRow(changeIndex);
                firstModified = Math.min(changeIndex, firstModified);
            }
        }

        // Reapply selection to the List
        selection.fireSelectionChanged(firstModified, source.size() - 1);
    }

    /**
     * Inverts the current selection.
     */
    public void invertSelection() {
        selection.getSelectionList().invertSelection();
    }


    /**
     * To use common selectable widget logic in a widget unaware fashion.
     */
    private final class SelectableList implements Selectable {
        /** {@inheritDoc} */
        public void addSelectionListener(SelectionListener listener) {
            list.addSelectionListener(listener);
        }

        /** {@inheritDoc} */
        public void removeSelectionListener(SelectionListener listener) {
            list.removeSelectionListener(listener);
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
        public void select(int index) {
            list.select(index);
        }

        /** {@inheritDoc} */
        public void deselect(int index) {
            list.deselect(index);
        }
    }

    /**
     * Releases the resources consumed by this {@link DefaultEventListViewer} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link DefaultEventListViewer} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link DefaultEventListViewer}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link DefaultEventListViewer} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on a {@link DefaultEventListViewer} after it has been disposed.
     */
    public void dispose() {
        selection.dispose();

        source.removeListEventListener(this);
        if (disposeSource) source.dispose();
    }
}