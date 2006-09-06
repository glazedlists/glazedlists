package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * This editor removes some of the burden of producing an appropriate looking
 * component for the hierarchy column of a tree table. Specifically, it takes
 * care of adding components to a panel that render a node location within the
 * tree, but leaves the rendering of the <strong>data</strong> of the node to
 * a delegate {@link TableCellEditor} that is supplied in the constructor.
 *
 * <p>For example, in the following tree representation, the spacing and +/-
 * icons would be added to each tree node by this editor, while the data text
 * would be added by the component returned from the delegate
 * {@link TableCellEditor}.
 *
 * <pre>
 * - Cars
 *   + BMW
 *   - Ford
 *       Taurus
 *       Focus
 *   - Lexus
 *       ES 300
 *       LS 600h L
 * </pre>
 *
 * @author James Lemieux
 */
public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    /** The panel capable of laying out a spacer component, expander button, and data Component to produce the entire tree node display. */
    private final TreeTableCellPanel component = new TreeTableCellPanel();

    /** The data structure that answers questions about the tree node. */
    private TreeList treeList;

    /** The user-supplied editor that produces the look of the tree node's data. */
    private TableCellEditor delegate;

    /** Respond to editing changes in the delegate TableCellEditor. */
    private final CellEditorListener delegateListener = new DelegateTableCellEditorListener();

    /**
     * Decorate the component returned from the <code>delegate</code> with
     * extra components that display the tree nodes location within the tree.
     * If <code>delegate</code> is <tt>null</tt> then a
     * {@link DefaultCellEditor} using a {@link JTextField} will be used as
     * the delegate.
     *
     * @param delegate the editor that produces the data for the tree node
     * @param treeList the data structure that answers questions about the tree
     *      node and the tree that contains it
     */
    public TreeTableCellEditor(TableCellEditor delegate, TreeList treeList) {
        this.delegate = delegate == null ? createDelegateEditor() : delegate;
        this.delegate.addCellEditorListener(delegateListener);
        this.treeList = treeList;
    }

    /**
     * Build the delegate TableCellEditor that handles editing the data of
     * each tree node.
     */
    protected TableCellEditor createDelegateEditor() {
        return new DefaultCellEditor(new JTextField());
    }

    /**
     * Return a decorated form of the component returned by the data
     * {@link TableCellEditor}.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final Component c = delegate.getTableCellEditorComponent(table, value, isSelected, row, column);

        final int depth;
        final boolean isExpanded;
        final boolean hasChildren;
        final boolean supportsChildren;

        treeList.getReadWriteLock().readLock().lock();
        try {
            // read information about the tree node from the TreeList
            depth = treeList.depth(row);
            isExpanded = treeList.isExpanded(row);
            hasChildren = treeList.hasChildren(row);
            supportsChildren = treeList.supportsChildren(row);
        } finally {
            treeList.getReadWriteLock().readLock().unlock();
        }

        // ask our special component to configure itself for this tree node
        component.configure(depth, hasChildren, supportsChildren, isExpanded, c);
        return component;
    }

   /**
    * Returns true if <code>anEvent</code> is a <code>MouseEvent</code> with a
    * click count >= 2. This method's implementation is actually used for
    * another purpose besides determining if <code>anEvent</code> should
    * trigger a cell edit. If <code>anEvent</code> is a single mouse click
    * which is positioned over the expand/collapse label of the underlying
    * <code>DefaultTreeTableCellPanel</code> then this method actually toggles
    * the expanded/collapsed state of the rowObject and then returns
    * <tt>false</tt> from this method to indicate that we should not enter
    * edit mode.
    *
    * @param anEvent the event attempting to begin a cell edit
    * @return true if cell is ready for editing, false otherwise
    */
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            final MouseEvent me = (MouseEvent) anEvent;

            // we're going to check if the single click was overtop of the
            // expand button, and toggle the expansion state of the row if
            // it was but return false so we don't begin the cell edit

            // extract information about the location of the click
            final JTable table = (JTable) anEvent.getSource();
            final Point clickPoint = me.getPoint();
            final int row = table.rowAtPoint(clickPoint);
            final int column = table.columnAtPoint(clickPoint);

            // translate the click to be relative to the cellRect (and thus its rendered component)
            final Rectangle cellRect = table.getCellRect(row, column, true);
            clickPoint.translate(-cellRect.x, -cellRect.y);

            // get the component rendered at the clickPoint
            final Component renderedComponent = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
            final TreeTableCellPanel renderedPanel = (TreeTableCellPanel) renderedComponent;
            renderedPanel.setBounds(cellRect);
            renderedPanel.doLayout();

            // if a left-click occurred over the expand/collapse button
            if (SwingUtilities.isLeftMouseButton(me) && renderedPanel.isPointOverExpanderButton(clickPoint)) {
                treeList.getReadWriteLock().writeLock().lock();
                try {
                    // expand/collapse the rowObject if possible
                    if(treeList.supportsChildren(row))
                        TreeTableUtilities.toggleExpansion(table, treeList, row);
                } finally {
                    treeList.getReadWriteLock().writeLock().unlock();
                }
                return false;
            }

            // if the row has children, it is a synthetic row and its node component cannot be edited
            if (treeList.hasChildren(row))
                return false;

            // if the click occurred over the node name editor
            if (renderedPanel.isPointOverNodeComponent(clickPoint)) {
                // shift the click over by the expand icon and space
                Rectangle delegateRendererBounds = renderedPanel.getNodeComponent().getBounds();
                MouseEvent translatedMouseEvent = new MouseEvent(me.getComponent(), me.getID(), me.getWhen(), me.getModifiers(), me.getX() - delegateRendererBounds.x, me.getY() - delegateRendererBounds.y , me.getClickCount(), me.isPopupTrigger(), me.getButton());

                // allow the actual editor to decide
                return delegate.isCellEditable(translatedMouseEvent);
            }

            return false;
        }

        // otherwise, we're here because of an unrecognized EventObject
        return super.isCellEditable(anEvent);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    /**
     * Returns the delegate TableCellEditor being decorated.
     */
    public TableCellEditor getDelegate() {
        return delegate;
    }

    /**
     * Cleanup the data within this editor as it is no longer being used and
     * must be prepared for garbage collection.
     */
    public void dispose() {
        delegate.removeCellEditorListener(delegateListener);

        delegate = null;
        treeList = null;
    }

    /**
     * When the delegate TableCellEditor changes its editing state, we follow suit.
     */
    private class DelegateTableCellEditorListener implements CellEditorListener {
        public void editingCanceled(ChangeEvent e) {
            cancelCellEditing();
        }
        public void editingStopped(ChangeEvent e) {
            stopCellEditing();
        }
    }
}