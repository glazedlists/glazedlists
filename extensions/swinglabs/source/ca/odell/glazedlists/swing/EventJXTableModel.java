/* Glazed Lists                                                 (c) 2003-2008 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.event.TableModelEvent;

/**
 * An extension of the {@link EventTableModel} for better integration with
 * JXTable.
 * <p>
 * In particular, this table model implements a different strategy to tranform
 * {@link ListEvent}s to {@link TableModelEvent}s. Whereas EventTableModel
 * converts each ListEvent block to a TableModelEvent, EventJXTableModel tries
 * to create only one TableModelEvent for a ListEvent, that does not represent a
 * reorder. If the ListEvent contains multiple blocks, a special
 * <em>data changed</em> TableModelEvent will be fired, indicating that all
 * row data has changed. Note, that such a <em>data changed</em> TableModelEvent
 * can lead to a loss of the table selection.
 * </p>
 *
 * @author Holger Brands
 */
public class EventJXTableModel<E> extends EventTableModel<E> {

    /**
     * {@inheritDoc}
     */
    public EventJXTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels,
            boolean[] writable) {
        super(source, propertyNames, columnLabels, writable);
    }

    /**
     * {@inheritDoc}
     */
    public EventJXTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        super(source, tableFormat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleListChange(ListEvent<E> listChanges) {
        if (listChanges.isReordering()) {
            super.handleListChange(listChanges);
        } else {
            fireOneTableModelEvent(listChanges);
        }
    }

    /**
     * Ensures that only one TableModelEvent is created and fired for the given ListEvent.
     */
    private void fireOneTableModelEvent(ListEvent<E> listChanges) {
        // build an "optimized" TableModelEvent describing the precise range of rows in the first block
        listChanges.nextBlock();
        final int startIndex = listChanges.getBlockStartIndex();
        final int endIndex = listChanges.getBlockEndIndex();
        final int changeType = listChanges.getType();
        getMutableTableModelEvent().setValues(startIndex, endIndex, changeType);

        // if another block exists, fallback to using a generic "data changed" TableModelEvent
        if (listChanges.nextBlock())
            getMutableTableModelEvent().setAllDataChanged();

        // fire the single TableModelEvent representing the entire ListEvent
        fireTableChanged(getMutableTableModelEvent());
    }
}
