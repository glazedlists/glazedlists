/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// for swt Lists
import org.eclipse.swt.widgets.List;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// to preserve selection
import java.util.ArrayList;


/**
* A helper that displays an EventList in an SWT List widget.
 *
 * <p>This class is not thread safe. It must be used exclusively with the SWT
 * event handler thread.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class EventListViewer implements ListEventListener {

    /** the SWT List */
    private List list = null;

    /** the EventList to respond to */
    private EventList source = null;

    /** the formatter for list elements */
    private ListFormat listFormat = null;
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

    /**
     * When the source list is changed, this forwards the change to the
     * displayed List.
     *
     * <p>This implementation saves the entire List's selection in an ArrayList before
     * walking through the list of changes. It then walks through the List's changes.
     * Finally it adjusts the selection on the List in response to the changes.
     * Although simple, this implementation has much higher memory and runtime
     * requirements than necessary. It is desirable to optimize this method by
     * not storing a second copy of the selection list. Such an implementation would
     * use only the selection data available in the List plus a list of entries
     * which have been since overwritten.
     */
    public void listChanged(ListEvent listChanges) {
        source.getReadWriteLock().readLock().lock();
        try {

            // save the former selection
            ArrayList selection = new ArrayList();
            for(int i = 0; i < list.getItemCount(); i++) {
                selection.add(i, Boolean.valueOf(list.isSelected(i)));
            }

            // walk the list
            while(listChanges.next()) {
                int changeIndex = listChanges.getIndex();
                int changeType = listChanges.getType();

                if(changeType == ListEvent.INSERT) {
                    selection.add(changeIndex, Boolean.FALSE);
                    addRow(changeIndex, source.get(changeIndex));
                } else if(changeType == ListEvent.UPDATE) {
                    updateRow(changeIndex, source.get(changeIndex));
                } else if(changeType == ListEvent.DELETE) {
                    selection.remove(changeIndex);
                    deleteRow(changeIndex);
                }
            }

            // apply the saved selection
            for(int i = 0; i < list.getItemCount(); i++) {
                boolean selected = ((Boolean)selection.get(i)).booleanValue();
                if(selected) {
                    list.select(i);
                } else {
                    list.deselect(i);
                }
            }
        } finally {
            source.getReadWriteLock().readLock().unlock();
        }
    }
}