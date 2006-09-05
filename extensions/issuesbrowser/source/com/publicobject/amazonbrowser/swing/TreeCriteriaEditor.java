/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.swing.EventListModel;
import com.publicobject.amazonbrowser.TreeCriterion;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A TreeCriteriaEditor is a fancy JList that allows the user the ability to
 * specify:
 *
 * <ul>
 *   <li>which {@link TreeCriterion} objects are active, and thus contribute to the treetable hierarchy
 *   <li>the order of the active TreeCriterion objects, and thus the order of the treetable hierarchies
 * </ul>
 *
 * PropertyChangeEvents are fired when the list of active {@link TreeCriterion}
 * objects as returned by {@link #getActiveCriteria()} changes in any way.
 *
 * @author James Lemieux
 */
public class TreeCriteriaEditor extends JPanel {

    /** The JList that edits {@link #allCriteria}. */
    private final JList treeCriteriaList;

    /** All {@link TreeCriterion} objects that are available to the user. */
    private final EventList<TreeCriterion> allCriteria;

    /** Only the active {@link TreeCriterion} objects. */
    private final EventList<TreeCriterion> activeCriteria;

    /** A ListEventListener that watches the {@link #activeCriteria} List for changes and broadcasts PropertyChangeEvents describing the change. */
    private final ListEventListener<TreeCriterion> activeCriteriaListener = new ActiveCriteriaListener();

    /**
     * Construct a TreeCriteriaEditor that allows users a chance to reorder the
     * {@link TreeCriterion} objects in the given <code>source</code> as well
     * as activating/deactivating them.
     *
     * @param source the List of all possible TreeCriterion objects available
     */
    public TreeCriteriaEditor(EventList<TreeCriterion> source) {
        super(new BorderLayout());
        setBackground(AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE);

        this.allCriteria = source;

        // build a filtered view of allCriteria that only contains active TreeCriterion objects
        this.activeCriteria = new FilterList<TreeCriterion>(allCriteria, Matchers.beanPropertyMatcher(TreeCriterion.class, "active", Boolean.TRUE));
        this.activeCriteria.addListEventListener(activeCriteriaListener);

        // configure a JList to edit allCriteria using only the mouse (clicks and drag n' drop)
        this.treeCriteriaList = new JList(new EventListModel<TreeCriterion>(source));
        this.treeCriteriaList.setCellRenderer(new Renderer());
        this.treeCriteriaList.addMouseListener(new MouseHandler());
        this.treeCriteriaList.setTransferHandler(new ReorderingTransferHandler());
        this.treeCriteriaList.setDragEnabled(true);

        // add the JList to this panel
        add(treeCriteriaList, BorderLayout.CENTER);
    }

    /**
     * Returns an unmodifiable <strong>snapshot</strong> List of active
     * {@link TreeCriterion} objects.
     */
    public List<TreeCriterion> getActiveCriteria() {
        return Collections.unmodifiableList(new ArrayList<TreeCriterion>(activeCriteria));
    }

    /**
     * This ListEventListener translates changes to the List of active criteria
     * into PropertyChangeEvents for the activeCriteria property of this
     * TreeCriteriaEditor.
     */
    private class ActiveCriteriaListener implements ListEventListener<TreeCriterion> {
        public void listChanged(ListEvent<TreeCriterion> listChanges) {
            firePropertyChange("activeCriteria", null, getActiveCriteria());
        }
    }

    /**
     * A special TransferHandler that allows the objects within the JList to be
     * reordered using drag and drop. Note that the only action supported by
     * this TransferHandler is a MOVE within the source JList, so it can only
     * reorder the JList's elements.
     */
    private class ReorderingTransferHandler extends TransferHandler {
        /** The single DataFlavor produced and consumed by this TransferHandler. */
        private final DataFlavor TREE_CRITERION_DATA_FLAVOR = new DataFlavor(TreeCriterion.class, "Tree Criterion");

        /**
         * To reorder elements within the JList we only need to support the
         * {@link TransferHandler#MOVE MOVE} source action.
         */
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (int i = 0; i < transferFlavors.length; i++) {
                if (transferFlavors[i] == TREE_CRITERION_DATA_FLAVOR)
                    return true;
            }

            return false;
        }

        protected Transferable createTransferable(JComponent c) {
            return new TreeCriterionTransferable(((JList) c).getSelectedValue());
        }

        public boolean importData(JComponent comp, Transferable t) {
            try {
                final int sourceIndex = allCriteria.indexOf(t.getTransferData(TREE_CRITERION_DATA_FLAVOR));
                final int targetIndex = ((JList) comp).getSelectedIndex();

                // move the TreeCriterion from the sourceIndex to the targetIndex
                allCriteria.add(targetIndex, allCriteria.remove(sourceIndex));

            } catch (UnsupportedFlavorException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return false;
        }

        /**
         * A no-op since we should *never* export anything to the clipboard.
         */
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) { }

        /**
         * This special Transferable contains only a single data item of our choosing.
         */
        private class TreeCriterionTransferable implements Transferable {
            private final DataFlavor[] TRANSFER_DATA_FLAVORS = {TREE_CRITERION_DATA_FLAVOR};

            private final Object data;

            public TreeCriterionTransferable(Object data) {
                this.data = data;
            }

            public DataFlavor[] getTransferDataFlavors() {
                return TRANSFER_DATA_FLAVORS;
            }

            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor == TREE_CRITERION_DATA_FLAVOR;
            }

            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (!isDataFlavorSupported(flavor))
                    throw new UnsupportedFlavorException(flavor);

                return data;
            }
        }
    }

    /**
     * This MouseHandler watches for clicks on the JList and reacts by checking
     * to see if the click occurred over the rendered image of the activeButton.
     * If it did, the active state of the TreeCriterion is toggled.
     */
    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            // search for the cell over which the click occurred
            for (int i = 0; i < treeCriteriaList.getModel().getSize(); i++) {
                final Rectangle cellBounds = treeCriteriaList.getCellBounds(i, i);

                // if the click occurred over the bounds of the current cell, we've found it
                if (cellBounds.contains(e.getPoint())) {
                    // translate the click location to be relative to the cell (instead of the JList)
                    final Point p = e.getPoint();
                    p.translate(-cellBounds.x, -cellBounds.y);

                    // fetch a bunch of information about the cell in order to produce the rendered component
                    final TreeCriterion value = (TreeCriterion) treeCriteriaList.getModel().getElementAt(i);
                    final boolean isSelected = treeCriteriaList.getSelectionModel().isSelectedIndex(i);
                    final ListCellRenderer renderer = treeCriteriaList.getCellRenderer();
                    final boolean hasFocus = hasFocus() && (treeCriteriaList.getSelectionModel().getLeadSelectionIndex() == i);

                    // render the cell, offscreen
                    final Renderer r = (Renderer) renderer.getListCellRendererComponent(treeCriteriaList, value, i, isSelected, hasFocus);
                    r.setBounds(cellBounds);
                    r.doLayout();

                    // if the component under the click is the activeButton, toggle the active state of the TreeCriterion
                    if (r.getComponentAt(p) == r.activeButton)
                        value.setActive(!value.isActive());
                }
            }
        }
    }

    /**
     * A special ListCellRenderer that combines a JButton which indicates the
     * active state of a TreeCriterion with a JLabel that displays the name of
     * the TreeCriterion.
     */
    private static final class Renderer extends JPanel implements ListCellRenderer {

        private final JButton activeButton = new JButton();
        private final JLabel nameLabel = new JLabel();

        public Renderer() {
            super(new BorderLayout());
            setBackground(AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE);

            activeButton.setBorder(BorderFactory.createEmptyBorder());
            activeButton.setContentAreaFilled(false);
            activeButton.setFont(nameLabel.getFont().deriveFont(8));
            nameLabel.setFont(nameLabel.getFont().deriveFont(8));

            add(activeButton, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final TreeCriterion treeCriterion = (TreeCriterion) value;

            activeButton.setText(treeCriterion.isActive() ? "X" : "O");
            nameLabel.setText(treeCriterion.getName());

            return this;
        }
    }
}