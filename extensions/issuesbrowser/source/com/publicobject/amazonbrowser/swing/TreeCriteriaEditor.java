package com.publicobject.amazonbrowser.swing;

import ca.odell.glazedlists.EventList;
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

/**
 * @author James Lemieux
 */
public class TreeCriteriaEditor extends JPanel {

    private final JList treeCriteriaList;

    public TreeCriteriaEditor(EventList<TreeCriterion> source) {
        super(new BorderLayout());
        setBackground(AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE);

        this.treeCriteriaList = new JList(new EventListModel<TreeCriterion>(source));
        this.treeCriteriaList.setCellRenderer(new Renderer());
        this.treeCriteriaList.addMouseListener(new MouseHandler());
        this.treeCriteriaList.setTransferHandler(new TreeCriterionTransferHandler());
        this.treeCriteriaList.setDragEnabled(true);

        add(treeCriteriaList, BorderLayout.CENTER);
    }

    private static class TreeCriterionTransferHandler extends TransferHandler {
        private static final DataFlavor TREE_CRITERION_DATA_FLAVOR = new DataFlavor(TreeCriterion.class, "Tree Criterion");

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
            System.out.println("TreeCriteriaEditor$TreeCriterionTransferHandler.importData");
            return false;
        }

        public void exportToClipboard(JComponent comp, Clipboard clip, int action) { }

        private static class TreeCriterionTransferable implements Transferable {
            private static final DataFlavor[] TRANSFER_DATA_FLAVORS = {TREE_CRITERION_DATA_FLAVOR};

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

    private class MouseHandler extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            for (int i = 0; i < treeCriteriaList.getModel().getSize(); i++) {
                final Rectangle cellBounds = treeCriteriaList.getCellBounds(i, i);

                if (cellBounds.contains(e.getPoint())) {
                    final Point p = e.getPoint();
                    p.translate(-cellBounds.x, -cellBounds.y);

                    final TreeCriterion value = (TreeCriterion) treeCriteriaList.getModel().getElementAt(i);
                    final boolean isSelected = treeCriteriaList.getSelectionModel().isSelectedIndex(i);
                    final ListCellRenderer renderer = treeCriteriaList.getCellRenderer();
                    final boolean hasFocus = hasFocus() && (treeCriteriaList.getSelectionModel().getLeadSelectionIndex() == i);

                    final Renderer r = (Renderer) renderer.getListCellRendererComponent(treeCriteriaList, value, i, isSelected, hasFocus);
                    r.setBounds(cellBounds);
                    r.doLayout();

                    if (r.getComponentAt(p) == r.activeButton)
                        value.setActive(!value.isActive());
                }
            }
        }
    }

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