/* Glazed Lists                                                 (c) 2003-2013 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.swing;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.swing.MutableTableModelEvent;
import ca.odell.glazedlists.swing.TableModelEventAdapter;

import java.awt.EventQueue;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * DefaultTableModelEventAdapter encapsulates the default strategy for
 * converting list events to table model events.
 *
 * <p>
 * The goal here is to be as accurate as possible. In particular, each list
 * event block is converted to and fired as a separate {@link TableModelEvent}.
 * So, one list event can cause multiple table model events.
 * </p>
 * <p>
 * In some cases, this conversion strategy can lead to undesirable effects, such
 * as table repainting issues. One known case is when the table property
 * {@link JTable#getFillsViewportHeight() fillsViewportHeight} is
 * <code>true</code>. Using the {@link ManyToOneTableModelEventAdapter} instead is then
 * recommended.
 *
 * @see DefaultTableModelEventAdapterFactory
 *
 * @author Holger Brands
 */
class DefaultTableModelEventAdapter<E> implements TableModelEventAdapter<E> {

    /** reusable TableModelEvent for broadcasting changes */
    private final MutableTableModelEvent tableModelEvent;

    /** the associated table model. */
    private final AbstractTableModel tableModel;

    /**
     * Constructor with {@link TableModel}.
     *
     * @param tableModel the adapted table model
     */
    DefaultTableModelEventAdapter(AbstractTableModel tableModel) {
        tableModelEvent = new MutableTableModelEvent(tableModel);
        this.tableModel = tableModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listChanged(ListEvent<E> listChanges) {
        checkAccessThread();

        // for all changes, one block at a time
        while (listChanges.nextBlock()) {
            // get the current change info
            int startIndex = listChanges.getBlockStartIndex();
            int endIndex = listChanges.getBlockEndIndex();
            int changeType = listChanges.getType();
            // create a table model event for this block
            tableModelEvent.setValues(startIndex, endIndex, changeType);
            tableModel.fireTableChanged(tableModelEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fireTableStructureChanged() {
        tableModelEvent.setStructureChanged();
        tableModel.fireTableChanged(tableModelEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fireTableDataChanged() {
        tableModelEvent.setAllDataChanged();
        tableModel.fireTableChanged(tableModelEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void fireTableChanged(int startIndex, int endIndex, int listChangeType) {
        tableModelEvent.setValues(startIndex, endIndex, listChangeType);
        tableModel.fireTableChanged(tableModelEvent);
    }

    /**
     * A convenience method to ensure {@link DefaultTableModelEventAdapter} is being
     * accessed from the Event Dispatch Thread.
     */
    protected final void checkAccessThread() {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Events to " + tableModel.getClass().getSimpleName()
                    + " must arrive on the EDT - consider adding GlazedListsSwing.swingThreadProxyList(source) somewhere in your list pipeline");
        }
    }
}
