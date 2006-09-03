/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;

/**
 * A collection of static utility methods to ease the burdens of implementing
 * correct TreeTable behaviour in Swing.
 *
 * @author James Lemieux
 */
class TreeTableUtilities {

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
     */
    static void toggleExpansionWithoutAdjustingSelection(JTable table, TreeList treeList, int row) {
        final ListSelectionModel selectionModel = table.getSelectionModel();
        final EventSelectionModel eventSelectionModel = selectionModel instanceof EventSelectionModel ? (EventSelectionModel) selectionModel : null;
        final boolean isEventSelectionModelEnabled = eventSelectionModel != null && eventSelectionModel.getEnabled();
        final Boolean autoStartsEdit = (Boolean) table.getClientProperty("JTable.autoStartsEdit");

        // disable the EventSelectionModel so it does not respect our attempted change to row selection (due to treeList.toggleExpanded(row);)
        if (eventSelectionModel != null)
            eventSelectionModel.setEnabled(false);

        // disable attempts to start an edit because of a keystroke
        table.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);

        treeList.toggleExpanded(row);

        // post a Runnable to the Swing EDT to restore the state of the table and selection model
        SwingUtilities.invokeLater(new RestoreStateRunnable(table, autoStartsEdit, eventSelectionModel, isEventSelectionModelEnabled));
    }

    /**
     * Instances of this Runnable can be created and posted to the Swing EDT
     * in order to restore a captured state of a JTable and EventSelectionModel
     * at some point in the future after the current Runnable is done executing.
     */
    private static class RestoreStateRunnable implements Runnable {

        private final JTable table;
        private final Boolean autoStartsEdit;
        private final EventSelectionModel eventSelectionModel;
        private final boolean eventSelectionModelEnabled;

        public RestoreStateRunnable(JTable table, Boolean autoStartsEdit, EventSelectionModel eventSelectionModel, boolean eventSelectionModelEnabled) {
            this.table = table;
            this.autoStartsEdit = autoStartsEdit;
            this.eventSelectionModel = eventSelectionModel;
            this.eventSelectionModelEnabled = eventSelectionModelEnabled;
        }

        public void run() {
            table.putClientProperty("JTable.autoStartsEdit", autoStartsEdit);

            if (eventSelectionModel != null)
                eventSelectionModel.setEnabled(eventSelectionModelEnabled);
        }
    }
}