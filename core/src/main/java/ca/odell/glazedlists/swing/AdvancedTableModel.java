/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.table.TableModel;

/**
 * <code>AdvancedTableModel</code> is the extended interface intended to be implemented by
 * Glazed Lists table models. It provides additional methods for managing the
 * {@link TableFormat} and disposing, for example.
 *
 * @author Holger Brands
 */
public interface AdvancedTableModel<E> extends TableModel {

    /**
     * Gets the {@link TableFormat} used by this table model.
     */
    TableFormat<? super E> getTableFormat();

    /**
     * Sets the {@link TableFormat} that will extract column data from each
     * element. This has some very important consequences. Any cell selections
     * will be lost - this is due to the fact that the TableFormats may have
     * different numbers of columns, and JTable has no event to specify columns
     * changing without rows.
     */
    void setTableFormat(TableFormat<? super E> tableFormat);

    /**
     * Retrieves the value at the specified location from the table.
     *
     * <p>This may be used by renderers to paint the cells of a row differently
     * based on the entire value for that row.
     *
     * @see #getValueAt(int,int)
     */
    E getElementAt(int index);

    /**
     * Releases the resources consumed by this {@link AdvancedTableModel} so that it
     * may eventually be garbage collected.
     *
     * <p>An {@link AdvancedTableModel} will be garbage collected without a call to
     * {@link #dispose()}, but not before its source {@link EventList} is garbage
     * collected. By calling {@link #dispose()}, you allow the {@link AdvancedTableModel}
     * to be garbage collected before its source {@link EventList}. This is
     * necessary for situations where an {@link AdvancedTableModel} is short-lived but
     * its source {@link EventList} is long-lived.
     *
     * <p><strong><font color="#FF0000">Warning:</font></strong> It is an error
     * to call any method on an {@link AdvancedTableModel} after it has been disposed.
     * As such, this {@link AdvancedTableModel} should be detached from its
     * corresponding Component <strong>before</strong> it is disposed.
     */
    void dispose();

}
