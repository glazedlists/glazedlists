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
     * row selections within the given <code>table</code>, if possible.
     *
     * <p>In practice, this can only be achieved if the <code>table</code> uses
     * an {@link EventSelectionModel} as its selection model.
     */
    static void toggleExpansionWithoutAdjustingSelection(JTable table, TreeList treeList, int row) {
        final ListSelectionModel selectionModel = table.getSelectionModel();
        final EventSelectionModel eventSelectionModel = selectionModel instanceof EventSelectionModel ? (EventSelectionModel) selectionModel : null;
        final boolean isEventSelectionModelEnabled = eventSelectionModel != null && eventSelectionModel.getEnabled();

        // disable the EventSelectionModel so it does not respect our attempted change to row selection (due to treeList.toggleExpanded(row);)
        if (eventSelectionModel != null)
            eventSelectionModel.setEnabled(false);

        treeList.toggleExpanded(row);

        // post a Runnable to the Swing EDT that will reenable the EventSelectionModel after this Runnable completes
        if (eventSelectionModel != null)
            SwingUtilities.invokeLater(new ReEnableSelectionModelRunnable(eventSelectionModel, isEventSelectionModelEnabled));
    }

    /**
     * Instances of this Runnable can be created and posted to the Swing EDT
     * in order to enabled/disable the given {@link EventSelectionModel} at
     * some point in the future after the current Runnable is finished executing.
     */
    private static class ReEnableSelectionModelRunnable implements Runnable {

        private final EventSelectionModel eventSelectionModel;
        private final boolean enabled;

        public ReEnableSelectionModelRunnable(EventSelectionModel eventSelectionModel, boolean enabled) {
            this.eventSelectionModel = eventSelectionModel;
            this.enabled = enabled;
        }

        public void run() {
            eventSelectionModel.setEnabled(enabled);
        }
    }
}