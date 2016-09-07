/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.*;

/**
 * This interface is intended to be implemented by custom TableCellRenderers
 * installed on the JTableHeader of a sortable JTable. The custom renderer
 * need only implement this interface and it will be notified of the
 * appropriate sorting icon immediately before the renderer is asked to provide
 * a component.
 *
 * @author James Lemieux
 */
public interface SortableRenderer {

    /**
     * Sets the icon to display in order to indicate sorting direction or
     * <code>null</code> if no sorting is taking place.
     *
     * @param sortIcon the Icon indicating the sort direction or
     *      <code>null</code> if there is not sorting
     */
    public void setSortIcon(Icon sortIcon);
}