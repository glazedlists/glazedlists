/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.event.*;
import javax.swing.table.*;

public class EventTreeTableModel<E> extends EventTableModel implements TreeTableModel {

    private final TreeFormat treeFormat;

    /**
     * Creates a new table that renders the specified list in the specified
     * format.
     */
    public EventTreeTableModel(EventList<E> source, TableFormat<E> tableFormat, TreeFormat treeFormat) {
        super(source, tableFormat);
        this.treeFormat = treeFormat;
    }

    /**
     * Returns the height of the row object at the given <code>rowIndex</code>
     * within the tree represented by this {@link TreeTableModel}.
     */
    public int getDepth(int rowIndex) {
        swingThreadSource.getReadWriteLock().readLock().lock();
        try {
            return TreeTableSupport.getDepth(treeFormat, swingThreadSource.get(rowIndex));
        } finally {
            swingThreadSource.getReadWriteLock().readLock().unlock();
        }
    }
}