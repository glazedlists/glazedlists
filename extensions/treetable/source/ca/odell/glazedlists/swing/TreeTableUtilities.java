/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * A collection of static utility methods to ease the burdens of implementing
 * correct TreeTable behaviour in Swing.
 *
 * @author James Lemieux
 */
class TreeTableUtilities {

    /**
     * Assuming the <code>mouseEvent</code> occurred over a {@link JTable},
     * retrieve the row over which the event occurred.
     *
     * @param mouseEvent a MouseEvent whose source is a {@link JTable}
     * @return the index of the table row over which the event occurred
     */
    static int rowAtPoint(MouseEvent mouseEvent) {
        final JTable table = (JTable) mouseEvent.getSource();
        final Point clickPoint = mouseEvent.getPoint();
        return table.rowAtPoint(clickPoint);
    }

    /**
     * Given a <code>mouseEvent</code> which occurred over a {@link JTable},
     * and further assuming that the column is a hierarchical column that
     *
     * @param mouseEvent a MouseEvent whose source is a {@link JTable}
     * @return the TreeTableCellPanel returned by the renderer, or
     *      <code>null</code> if the click was outside the table or the
     *      renderer does not return a TreeTableCellPanel
     */
    static TreeTableCellPanel prepareRenderer(MouseEvent mouseEvent) {
        // we're going to check if the single click was overtop of the
        // expand button, and toggle the expansion state of the row if
        // it was but return false so we don't begin the cell edit

        // extract information about the location of the click
        final JTable table = (JTable) mouseEvent.getSource();
        final Point clickPoint = mouseEvent.getPoint();
        final int row = table.rowAtPoint(clickPoint);
        final int column = table.columnAtPoint(clickPoint);

        // if the coordinates are not within the table, bail early
        if (row == -1 || column == -1)
            return null;

        // translate the click to be relative to the cellRect (and thus its rendered component)
        final Rectangle cellRect = table.getCellRect(row, column, true);

        // get the component rendered at the clickPoint
        final Component renderedComponent = table.prepareRenderer(table.getCellRenderer(row, column), row, column);

        // if the component is not a TreeTableCellPanel, bail early
        if (!(renderedComponent instanceof TreeTableCellPanel))
            return null;

        // layout the panel within its bounds
        final TreeTableCellPanel renderedPanel = (TreeTableCellPanel) renderedComponent;
        renderedPanel.setBounds(cellRect);
        renderedPanel.doLayout();

        return renderedPanel;
    }

    /**
     * This method toggles the expanded/collapsed state of the given
     * <code>row</code> in the given <code>treeList</code> without altering the
     * row selections within the given <code>table</code>, if possible. In
     * practice, this can only be achieved if the <code>table</code> uses an
     * {@link EventSelectionModel} as its selection model.
     *
     * <p>This method also turns off JTable's <code>JTable.autoStartsEdit</code>
     * client property during the toggling to stop any attempts at beginning a
     * cell edit. This behaviour is only relevant when the toggling action is
     * triggered by the keyboard. We do this to avoid invoking two behaviours
     * with only a single keystroke: toggling the expand/collapse state AND
     * starting a cell edit.
     *
     * A Runnable is returned which, when executed, restores the settings in
     * the EventSelectionModel and the JTable's client property. It is up to
     * the caller to decide when the appropriate time to re-enable those
     * settings may be and execute the Runnable.
     */
    static Runnable toggleExpansion(JTable table, TreeList treeList, int row) {
        final RestoreStateRunnable restoreStateRunnable = new RestoreStateRunnable(table);
        final EventSelectionModel selectionModel = restoreStateRunnable.getEventSelectionModel();

        // disable the EventSelectionModel so it does not respect our attempted change to row selection (due to treeList.toggleExpanded(row);)
        if (selectionModel != null)
            selectionModel.setEnabled(false);

        // disable attempts to start an edit because of a keystroke
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        treeList.toggleExpanded(row);

        // return a Runnable that restores the state of the table and selection model
        return restoreStateRunnable;
    }

    /**
     * Instances of this Runnable restore a captured state of a JTable and its
     * EventSelectionModel at some point in the future (when it is executed)
     */
    private static class RestoreStateRunnable implements Runnable {

        private final JTable table;
        private final Boolean autoStartsEdit;
        private final EventSelectionModel eventSelectionModel;
        private final boolean eventSelectionModelEnabled;

        public RestoreStateRunnable(JTable table) {
            this.table = table;

            final ListSelectionModel selectionModel = table.getSelectionModel();
            eventSelectionModel = selectionModel instanceof EventSelectionModel ? (EventSelectionModel) selectionModel : null;
            eventSelectionModelEnabled = eventSelectionModel != null && eventSelectionModel.getEnabled();
            autoStartsEdit = (Boolean) table.getClientProperty("JTable.autoStartsEdit");
        }

        public EventSelectionModel getEventSelectionModel() {
            return eventSelectionModel;
        }

        public void run() {
            table.putClientProperty("JTable.autoStartsEdit", autoStartsEdit);

            if (eventSelectionModel != null)
                eventSelectionModel.setEnabled(eventSelectionModelEnabled);
        }
    }
}