/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 * A TableModelEventAdapter is used by a {@link DefaultEventTableModel} for
 * converting list events to table model events. The adapter is also responsible for
 * firing the created {@link TableModelEvent}s. It offers convience methods for
 * firing special table model events.
 * <p>
 * A particular TableModelEventAdapter is usually created via an implementation of {@link Factory}
 * and then passed to the table model.
 *
 * @see DefaultEventTableModel#setEventAdapter(TableModelEventAdapter)
 *
 * @param <E> list element type
 *
 * @author Holger Brands
 */
public interface TableModelEventAdapter<E> extends ListEventListener<E> {

    /**
     * Notifies all listeners that the table's structure has changed.
     * The number of columns in the table, and the names and types of
     * the new columns may be different from the previous state.
     * If the <code>JTable</code> receives this event and its
     * <code>autoCreateColumnsFromModel</code>
     * flag is set it discards any table columns that it had and reallocates
     * default columns in the order they appear in the model. This is the
     * same as calling <code>setModel(TableModel)</code> on the
     * <code>JTable</code>.
     *
     * @see AbstractTableModel#fireTableStructureChanged()
     */
    void fireTableStructureChanged();

    /**
     * Notifies all listeners that all cell values in the table's
     * rows may have changed. The number of rows may also have changed
     * and the <code>JTable</code> should redraw the
     * table from scratch. The structure of the table (as in the order of the
     * columns) is assumed to be the same.
     *
     * @see AbstractTableModel#fireTableDataChanged()
     */
    void fireTableDataChanged();

    /**
     * Notifies all listeners that rows in the range
     * <code>[startIndex, endIndex]</code>, inclusive, have been changed (inserted,
     * updated or deleted).
     *
     * @param startIndex the first row index
     * @param endIndex the last row index
     * @param listChangeType the list change type (insert, update or delete)
     *
     * @see ListEvent
     * @see TableModelEvent
     */
    void fireTableChanged(int startIndex, int endIndex, int listChangeType);

    /**
     * Factory for creating {@link TableModelEventAdapter}s.
     * <p>The {@link GlazedListsSwing} helper class offers methods for creating an event table model with
     * a TableModelEventAdapter factory of your choice.
     *
     * @param <E> list element type
     *
     * @see GlazedListsSwing#eventTableModel(ca.odell.glazedlists.EventList, ca.odell.glazedlists.gui.TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)
     * @see GlazedListsSwing#eventTableModelWithThreadProxyList(ca.odell.glazedlists.EventList, ca.odell.glazedlists.gui.TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)
     *
     * @author Holger Brands
     */
    public interface Factory<E> {

        /**
         * Creates a new {@link TableModelEventAdapter} for the given table model.
         *
         * @param tableModel the table model
         * @return the new {@link TableModelEventAdapter}
         */
        TableModelEventAdapter<E> create(AbstractTableModel tableModel);
    }
}
