/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// for swt Lists
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;

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

    /** the EventList to respond to */
    private EventList source = null;

    /** the formatter for list elements */
    private ListFormat listFormat = null;

    /** For selection management */
    private SelectionList selectionList = null;

    /**
     * Creates a new List that displays and responds to changes in source.
     * List elements will simply be displayed as the result of calling
     * toString() on the contents of the source list.
     */
    public EventListViewer(EventList source, List list) {
        this(source, list, new DefaultListFormat());
    }

    /**
     * Creates a new List that displays and responds to changes in source.
     * List elements are formatted using the provided {@link ListFormat}.
     */
    public EventListViewer(EventList source, List list, ListFormat listFormat) {
        this.source = source;
        this.list = list;
        this.listFormat = listFormat;

		// Enable the selection lists
		selectionList = new SelectionList(source, this);

        populateList();
        source.addListEventListener(new UserInterfaceThreadProxy(this, list.getDisplay()));
    }

    /**
     * Populates the list with the original contents of the source event list.
     */
    private void populateList() {
        // set the initial data
        for(int i = 0; i < source.size(); i++) {
            addRow(i, source.get(i));
        }
    }

    /**
     * Gets the List Format.
     */
    public ListFormat getListFormat() {
        return listFormat;
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
        list.add(listFormat.getDisplayValue(value), row);
    }

    /**
     * Updates the value at the specified row.
     */
    private void updateRow(int row, Object value) {
        list.setItem(row, listFormat.getDisplayValue(value));
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
		int firstModified = source.size();
        source.getReadWriteLock().readLock().lock();
        try {
            // Apply the list changes
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                if(changeType == ListEvent.INSERT) {
                    addRow(changeIndex, source.get(changeIndex));
                    firstModified = Math.min(changeIndex, firstModified);
                } else if(changeType == ListEvent.UPDATE) {
                    updateRow(changeIndex, source.get(changeIndex));
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
            source.getReadWriteLock().readLock().unlock();
        }
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
	public void addListener(int type, Listener listener) {
		list.addListener(type, listener);
	}

	/** {@inheritDoc}*/
	public void removeListener(int type, Listener listener) {
		list.removeListener(type, listener);
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

    /**
     * Provides simple list formatting where each element will be displayed
     * as the result of calling toString() on the source Object.
     */
    private static final class DefaultListFormat implements ListFormat {

        /**
         * Gets the toString() value for a particular element.
         */
        public String getDisplayValue(Object element) {
            return element.toString();
        }
    }
}