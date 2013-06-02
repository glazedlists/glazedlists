/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.event.ListEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * ManyToOneTableModelEventAdapter encapsulates an alternative strategy for converting
 * list events to table model events.
 *
 * <p>
 * Whereas the default TableModelEventAdapter converts each ListEvent block to a
 * TableModelEvent, this adapter tries to create only one TableModelEvent for a
 * ListEvent, if it does not represent a reorder. If the ListEvent contains
 * multiple blocks, a special <em>data changed</em> TableModelEvent will be
 * fired, indicating that all row data has changed. Note, that such a
 * <em>data changed</em> TableModelEvent can lead to a loss of the table
 * selection.
 * </p>
 * <p>
 * Therefore you should use this adapter only, when the
 * {@link DefaultTableModelEventAdapter default adapter} doesn't fit your needs
 * or causes undesirable effects/behaviour.
 * </p>
 *
 * @see ManyToOneTableModelEventAdapterFactory
 *
 * @author Holger Brands
 */
class ManyToOneTableModelEventAdapter<E> extends DefaultTableModelEventAdapter<E> {

    /**
     * Constructor with {@link TableModel}.
     *
     * @param tableModel the adapted table model
     */
    ManyToOneTableModelEventAdapter(AbstractTableModel tableModel) {
        super(tableModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        if (listChanges.isReordering()) {
            super.listChanged(listChanges);
        } else {
            checkAccessThread();
            fireOneTableModelEvent(listChanges);
        }
    }

    /**
     * Ensures that only one TableModelEvent is created and fired for the given ListEvent.
     */
    private void fireOneTableModelEvent(ListEvent listChanges) {
        // build an "optimized" TableModelEvent describing the precise range of rows in the first block
        listChanges.nextBlock();
        final int startIndex = listChanges.getBlockStartIndex();
        final int endIndex = listChanges.getBlockEndIndex();
        final int changeType = listChanges.getType();

        // if another block exists, fallback to using a generic "data changed" TableModelEvent
        if (listChanges.nextBlock()) {
            fireTableDataChanged();
        } else {
            fireTableChanged(startIndex, endIndex, changeType);
        }
    }
}
