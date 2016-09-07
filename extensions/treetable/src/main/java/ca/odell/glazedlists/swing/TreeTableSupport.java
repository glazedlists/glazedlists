/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * This class {@link #install}s support for a single hierarchical column within
 * a standard {@link JTable} that is backed by an {@link AdvancedTableModel}. The
 * {@link AdvancedTableModel}, in turn <strong>must</strong> be backed by the same
 * {@link TreeList} which is given as a parameter to {@link #install}.
 *
 * <p>Typical usage of {@link TreeTableSupport} resembles this:
 * <br><br>
 * <pre><code>
 * // create an EventList of data
 * EventList myEventList = ...
 *
 * // create a TreeList which uses a TreeFormat object to infer a hierarchy for each element in myEventList
 * TreeList treeList = new TreeList(myEventList, new MyTreeFormat());
 *
 * // create a JTable that displays the contents of treeList in a tabular format
 * AdvancedTableModel myTableModel = GlazedListsSwing.eventTableModel(treeList, new MyTableFormat());
 * JTable myTable = new JTable(myTableModel);
 *
 * // make the 3rd table column a hierarchical column to create a TreeTable
 * TreeTableSupport.install(myTable, treeList, 2);
 * </code></pre>
 *
 * In order to achieve all of the treetable behaviours, the following occurs
 * when {@link #install} is called:
 *
 * <ul>
 *   <li>a {@link TreeTableCellRenderer} will be installed on the hierarchical
 *       {@link TableColumn}. It wraps any {@link TableCellRenderer} previously
 *       installed on the {@link TableColumn}, or if none was present, a
 *       DefaultTableCellRenderer. The delegate renderer can be changed at any
 *       time using {@link #setDelegateRenderer}.</li>
 *
 *   <li>a {@link TreeTableCellEditor} will be installed on the hierarchical
 *       {@link TableColumn}. It wraps any {@link TableCellEditor} previously
 *       installed on the {@link TableColumn}, or if none was present, a
 *       DefaultCellEditor. The delegate editor can be changed at any
 *       time using {@link #setDelegateEditor}.</li>
 *
 *   <li>the UI Delegate's MouseListener will be decorated with extra
 *       functionality that detects clicks overtop of the expand/collapse icon
 *       and reacts accordingly by expanding/collapsing the hierarchy. In these
 *       cases the normal mechanism for handling clicks over the table is
 *       circumvented and thus cell edits and/or row selection changes are not
 *       honoured.</li>
 * </ul>
 *
 * <font size="+1"><u>Customizing TreeTableSupport</u></font>
 *
 * <p>After installing the basic TreeTableSupport, its specific behaviour can
 * be refined by setting properties on the instance of TreeTableSupport
 * returned by the {@link #install} method. Below are descriptions of some of
 * the available customizations.
 *
 * <p><i><u>Left & Right Arrow Keys</u></i>
 *
 * <p>Within a treetable there is ambiguity about the functions performed by
 * the left and right arrow keys. Tables normally use them to advance cell
 * focus to the left or right while trees commonly use them to expand or
 * collapse hierarchies. What behaviour is desirable for any given treetable?
 * By default, TreeTableSupport assumes the table-style behaviour but that
 * can be adjusted to the tree-style behaviour calling
 * {@link #setArrowKeyExpansionEnabled} with an argument of <tt>true</tt>.
 * Generally speaking, tree-style behaviour is preferrable when the treetable
 * is readonly.
 *
 * <p><i><u>Space Key</u></i>
 *
 * <p>As a default, TreeTableSupport enables the space key to toggle expand/collapse
 * of the table hierachy of the currently selected node. This behaviour can be adjusted
 * by calling {@link #setSpaceKeyExpansionEnabled(boolean)} with an argument of <code>false</code>.
 * In this case, the space key will not be used to control expand and collapse
 * of the table hierachy.
 *
 * <p><i><u>Displaying Expander Buttons</u></i>
 *
 * <p>{@link TreeList} draws a distinction between three unique situations
 * regarding the display of the expander button for a given tree node, N:
 *
 * <ul>
 *   <li>Does node N currently have children? If it does, the expander button
 *       <code>must</code> be displayed.
 *       <p>e.g.: A directory in a file system that contains files and/or subdirectories.</li>
 *
 *   <li>If node N does not currently have children, is it capable of having
 *       children in the future? If it is not, then it is a true leaf node and
 *       the expander button <code>must not</code> be displayed.
 *       <p>e.g.: A file in a file system.</li>
 *
 *   <li>If node N currently has no children but is capable of having children
 *       in the future, should it be rendered with an expander now? Both yes
 *       and no are valid choices and thus the decision is left to the API
 *       user. The preference for this situation can be controlled using
 *       {@link #setShowExpanderForEmptyParent(boolean)}.
 *       <br>e.g.: Empty directories in Mac OS X Finder are displayed with
 *       visible expanders while Windows Explorer does not display them.</li>
 * </ul>
 *
 * @author James Lemieux
 */
public final class TreeTableSupport {

    /**
     * A special ListSelectionModel implementation that always reports itself
     * as empty. It is useful for readonly tree tables that don't want to
     * display a cell selection and instead want left/right arrow keys to
     * expand/collapse the tree hierarchy.
     */
    private static final ListSelectionModel NOOP_SELECTION_MODEL = new NoopColumnSelectionModel();

    /**
     * A ListEventListener that tests whether events are delivered to the
     * underlying TreeList on the Swing Event Dispath Thread. If they are not,
     * an {@link IllegalStateException} is thrown to indicate the error.
     */
    private static final ListEventListener SWING_THREAD_CHECKER = new EventDispathThreadChecker();

    /** The table to decorate with tree table behaviour. */
    private final JTable table;

    /** The TreeList that stores the state of the tree. */
    private final TreeList treeList;

    /** The model index of the table column that is the hierarchical column. */
    private final int hierarchyColumnModelIndex;

    /** Expands and collapses the tree structure based on key strokes (the space bar). */
    private final KeyListener expandAndCollapseKeyListener = new ExpandAndCollapseKeyListener();

    /** Expands and collapses the tree structure based on left and right arrow key strokes. */
    private final KeyListener arrowKeyListener = new ArrowKeyListener();

    /** Controls whether the left and right arrow keys perform simple table navigations or expand/collapse the table hierarchy. */
    private boolean arrowKeyExpansionEnabled;

    /** Controls whether the space key performs expand/collapse the table hierarchy when performed on a cell in the hierachical column. */
    private boolean spaceKeyExpansionEnabled = true;

    /**
     * When {@link #arrowKeyExpansionEnabled} is enabled, we must replace the
     * column selection model, so we remember the original selection model in
     * order to restore it if necessary.
     */
    private ListSelectionModel originalColumnSelectionModel;

    /** Controls whether the expander is displayed for empty tree nodes that may eventually have children. */
    private boolean showExpanderForEmptyParent;

    /** The renderer installed on the hierarchical TableColumn. */
    private TreeTableCellRenderer treeTableCellRenderer;

    /** The editor installed on the hierarchical TableColumn. */
    private TreeTableCellEditor treeTableCellEditor;

    /** The orignal TableCellRenderer, if any, to be replaced when TreeTableSupport is {@link #uninstall() uninstalled}. */
    private final TableCellRenderer originalRenderer;

    /** The orignal TableCellEditor, if any, to be replaced when TreeTableSupport is {@link #uninstall() uninstalled}. */
    private final TableCellEditor originalEditor;

    /**
     * This private constructor creates a TreeTableSupport object which adds
     * tree table behaviours to a single column that is nominated as the
     * "hierarchical column." Specifically, the rendering and editing of values
     * in the hierarchical column will be augmented to display position within
     * a hierarchy by indenting the "normal renderer and editor" according to
     * the height within a tree and adding a collapse/expand button.
     *
     * @param table the table to convert into a tree table
     * @param treeList The TreeList capable of answering hierarchical questions
     *        for the hierachical TableColumn's renderer and editor
     * @param modelColumnIndex the model index of the hierarchical column
     */
    private TreeTableSupport(JTable table, TreeList treeList, int modelColumnIndex) {
        this.table = table;
        this.treeList = treeList;
        this.hierarchyColumnModelIndex = modelColumnIndex;

        final int viewColumnIndex = table.convertColumnIndexToView(modelColumnIndex);

        // ensure we can find the view column index of the hierarchical column
        if (viewColumnIndex == -1) {
            throw new IllegalArgumentException("Unable to locate a view index for the given model index: " + modelColumnIndex);
        }

        // look up the hierarchical TableColumn
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);

        // wrap the existing TableCellRenderer with a TreeTableCellRenderer
        this.originalRenderer = viewColumn.getCellRenderer();
        this.treeTableCellRenderer = new TreeTableCellRenderer(originalRenderer, treeList);
        viewColumn.setCellRenderer(treeTableCellRenderer);

        // wrap the existing TableCellEditor with a TreeTableCellEditor
        this.originalEditor = viewColumn.getCellEditor();
        this.treeTableCellEditor = new TreeTableCellEditor(originalEditor, treeList);
        viewColumn.setCellEditor(treeTableCellEditor);

        // install some listeners that govern expand/collapse behaviour
        this.table.addKeyListener(arrowKeyListener);
        this.table.addKeyListener(expandAndCollapseKeyListener);
        decorateUIDelegateMouseListener(this.table);

        // verify that all elements on the TreeList arrive on the Swing EDT
        this.treeList.addListEventListener(SWING_THREAD_CHECKER);
    }

    /**
     * This method is a complete hack, but is necessary to achieve the
     * desired behaviour when adding collapse/expand capabilities to a
     * treetable.
     *
     * The problem is that when a mouse button is pressed, the MouseListener
     * installed by the UI Delegate is executed <strong>first</strong> and
     * changes the selected row to the row that was just clicked. This is
     * undesirable from the standpoint of treetables. One should be able to
     * expand or collapse tree nodes without adjusting the selection.
     *
     * To solve the problem, we rip out the MouseListener installed by
     * the BasicTableUI and decorate it before reinstalling it. The decorated
     * version of the MouseListener detects mouse clicks that actually toggle
     * the collapsed/expanded state and ensures that we do not change the
     * selected row in that case.
     */
    private void decorateUIDelegateMouseListener(Component component) {
        // replace the first MouseListener that appears to be installed by the UI Delegate
        final MouseListener[] mouseListeners = component.getMouseListeners();
        for (int i = mouseListeners.length-1; i >= 0; i--) {
            if (mouseListeners[i].getClass().getName().indexOf("TableUI") != -1) {
                component.removeMouseListener(mouseListeners[i]);
                component.addMouseListener(new ExpandAndCollapseMouseListener(mouseListeners[i]));
                break;
            }
        }
    }

    /**
     * This method will undo the work performed by {@link #decorateUIDelegateMouseListener(Component)}.
     */
    private void undecorateUIDelegateMouseListener(Component component) {
        // replace all decorated MouseListeners with original UI Delegate MouseListener
        final MouseListener[] mouseListeners = component.getMouseListeners();
        for (int i = 0; i < mouseListeners.length; i++) {
            if (mouseListeners[i] instanceof ExpandAndCollapseMouseListener) {
                component.removeMouseListener(mouseListeners[i]);
                component.addMouseListener(((ExpandAndCollapseMouseListener) mouseListeners[i]).getDelegate());
            }
        }
    }

    /**
     * Installs support for a hierarchical table column into the
     * <code>table</code> on the column with the given
     * <code>hierarchyColumnModelIndex</code>. The data returned by the
     * <code>TableModel</code> for the hierarchical column will remain
     * unchanged. But, the renderer and editor will be wrapped with instances
     * of {@link TreeTableCellRenderer} and {@link TreeTableCellEditor}
     * which take care of displaying the hierarchical information (namely the
     * whitespace indenting and expand/collapse button).
     *
     * <p>The following must be true in order to successfully install
     * TreeTableSupport on a {@link JTable}:
     *
     * <ul>
     *   <li>the <code>hierarchyColumnModelIndex</code> must correspond to a valid
     *       view column index</li>
     * </ul>
     *
     * @param table the table to convert into a tree table
     * @param treeList The TreeList capable of answering hierarchical questions
     *        for the hierachical TableColumn's renderer and editor
     * @param modelColumnIndex the model index of the hierarchical column
     * @return an instance of the support class providing treetable behaviour
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static TreeTableSupport install(JTable table, TreeList treeList, int modelColumnIndex) {
        checkAccessThread();

        return new TreeTableSupport(table, treeList, modelColumnIndex);
    }

    /**
     * Removes treetable support from the {@link JTable} it was
     * installed on. This method is useful when the {@link TreeList} of items
     * that backs the JTable must outlive the JTable itself. Calling this method
     * will return the hierarchical column to its original state before
     * treetable support was installed, and it will be available for garbage
     * collection independently of the {@link TreeList} of items.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void uninstall() {
        checkAccessThread();

        // stop verifying that all elements on the TreeList arrive on the Swing EDT
        treeList.removeListEventListener(SWING_THREAD_CHECKER);

        // disable arrow key expansion if it was enabled
        setArrowKeyExpansionEnabled(false);

        // stop trying to toggle tree nodes due to keystrokes and mouse clicks
        table.removeKeyListener(arrowKeyListener);
        table.removeKeyListener(expandAndCollapseKeyListener);
        undecorateUIDelegateMouseListener(table);

        // fetch information about the hierarchical column
        final int viewColumnIndex = table.convertColumnIndexToView(hierarchyColumnModelIndex);
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);
        final TableCellRenderer renderer = viewColumn.getCellRenderer();
        final TableCellEditor editor = viewColumn.getCellEditor();

        // if the TreeTableCellRenderer is still installed, reinstall the original TableCellRenderer
        if (renderer == treeTableCellRenderer) {
            viewColumn.setCellRenderer(originalRenderer);
        }

        // if the TreeTableCellEditor is still installed, reinstall the original TableCellEditor
        if (editor == treeTableCellEditor) {
            viewColumn.setCellEditor(originalEditor);
        }

        treeTableCellRenderer.dispose();
        treeTableCellEditor.dispose();
    }

    /**
     * Sets whether the expander is displayed for nodes that do not contain
     * children but are allowed to contain children, and thus may accumulate
     * children in the future. If this property is <tt>true</tt> then empty
     * nodes that may contain children in the future are displayed with a
     * visible expander; otherwise they are displayed without the expander.
     * A node signals that it may contain children in the future by returning
     * <tt>true</tt> from {@link ca.odell.glazedlists.TreeList.Format#allowsChildren(Object)}.
     */
    public void setShowExpanderForEmptyParent(boolean showExpanderForEmptyParent) {
        checkAccessThread();

        if (this.showExpanderForEmptyParent == showExpanderForEmptyParent) {
            return;
        }

        this.showExpanderForEmptyParent = showExpanderForEmptyParent;

        // indicate the new property value to the renderer and editor
        treeTableCellRenderer.setShowExpanderForEmptyParent(showExpanderForEmptyParent);
        treeTableCellEditor.setShowExpanderForEmptyParent(showExpanderForEmptyParent);

        // repaint the table so the display is updated
        table.repaint();
    }

    /**
     * Returns <tt>true</tt> if empty tree nodes that are allowed to have
     * children are displayed with visible expanders while they are empty;
     * <tt>false</tt> otherwise.
     */
    public boolean getShowExpanderForEmptyParent() {
        return showExpanderForEmptyParent;
    }

    /**
     * If <code>arrowKeyExpansionEnabled</code> is <tt>true</tt> then two
     * things are changed in tandem:
     *
     * <ul>
     *   <li> The left and right arrow keys will toggle the expansion state of
     *        hierarchy nodes in the tree rather than adjust cell focus in the
     *        table.</li>
     *
     *   <li> The table column model's selection model is replaced with a no-op
     *        implementation so that column selection and column lead/anchor
     *        indexes are NOT tracked. Thus, column selection is disabled and
     *        there is no cell focus adjusted when the left and right arrow
     *        keys are used.</li>
     * </ul>
     *
     * <p>If <code>arrowKeyExpansionEnabled</code> is <tt>false</tt> then the
     * behaviour is reverted to default. That is, left and right arrow keys
     * adjust cell focus in the table and not expansion, and the original
     * column selection model is reinstalled.
     */
    public void setArrowKeyExpansionEnabled(boolean arrowKeyExpansionEnabled) {
        checkAccessThread();

        // make sure we're actually changing the value
        if (this.arrowKeyExpansionEnabled == arrowKeyExpansionEnabled) {
            return;
        }

        final TableColumnModel tableColumnModel = table.getColumnModel();
        if (arrowKeyExpansionEnabled) {
            // if arrow key expansion is turned on, install the noop column selection model
            originalColumnSelectionModel = tableColumnModel.getSelectionModel();
            tableColumnModel.setSelectionModel(NOOP_SELECTION_MODEL);
        } else {
            // if arrow key expansion is turned off, restore the original column selection model
            if (tableColumnModel.getSelectionModel() == NOOP_SELECTION_MODEL) {
                tableColumnModel.setSelectionModel(originalColumnSelectionModel);
            }
            originalColumnSelectionModel = null;
        }

        this.arrowKeyExpansionEnabled = arrowKeyExpansionEnabled;
    }

    /**
     * Returns <tt>true</tt> if the left and right arrow keys are currently
     * used to control the tree hierarchy expansion; <tt>false</tt> if they are
     * used to control the table's cell focus.
     */
    public boolean getArrowKeyExpansionEnabled() {
        return arrowKeyExpansionEnabled;
    }

    /**
     * Returns <code>true</code> if the space key is currently used to control the table hierachy
     * expansion, <code>false</code> otherwise
     */
    public boolean getSpaceKeyExpansionEnabled() {
        return spaceKeyExpansionEnabled;
    }

    /**
     * If <code>spaceKeyExpansionEnabled</code> is <code>true</code>, then control of the
     * table hierachy expansion via the space key is enabled. If <code>false</code>, the space
     * key will not control the table hierachie expansion.
     */
    public void setSpaceKeyExpansionEnabled(boolean spaceKeyExpansionEnabled) {
        checkAccessThread();
        this.spaceKeyExpansionEnabled = spaceKeyExpansionEnabled;
    }

    /**
     * Use the given <code>treeTableCellRenderer</code> when rendering the
     * hierarchy column of the tree table.
     */
    public void setRenderer(TreeTableCellRenderer treeTableCellRenderer) {
        checkAccessThread();

        // ensure we can find the view column index of the hierarchical column
        final int viewColumnIndex = table.convertColumnIndexToView(hierarchyColumnModelIndex);
        if (viewColumnIndex == -1) {
            throw new IllegalArgumentException("Unable to locate a view index for the given model index: " + hierarchyColumnModelIndex);
        }

        // look up the hierarchical TableColumn
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);

        // dispose the old renderer
        this.treeTableCellRenderer.dispose();

        // install the new renderer
        this.treeTableCellRenderer = treeTableCellRenderer;
        treeTableCellRenderer.setShowExpanderForEmptyParent(showExpanderForEmptyParent);
        viewColumn.setCellRenderer(treeTableCellRenderer);
    }

    /**
     * Returns the <code>treeTableCellRenderer</code> used to render the
     * hierarchy column of the tree table.
     */
    public TreeTableCellRenderer getRenderer() {
        return treeTableCellRenderer;
    }

    /**
     * Use the given <code>renderer</code> as the new delegate renderer of the
     * {@link TreeTableCellRenderer} which is responsible for rendering the
     * data associated with each tree node in the hierarchy column.
     */
    public void setDelegateRenderer(TableCellRenderer renderer) {
        checkAccessThread();

        treeTableCellRenderer.setDelegate(renderer);
    }

    /**
     * Returns the <code>renderer</code> responsible for rendering the data
     * associated with each tree node in the hierarchy column.
     */
    public TableCellRenderer getDelegateRenderer() {
        return treeTableCellRenderer.getDelegate();
    }

    /**
     * Use the given <code>treeTableCellEditor</code> when editing the
     * hierarchy column of the tree table.
     */
    public void setEditor(TreeTableCellEditor treeTableCellEditor) {
        checkAccessThread();

        // ensure we can find the view column index of the hierarchical column
        final int viewColumnIndex = table.convertColumnIndexToView(hierarchyColumnModelIndex);
        if (viewColumnIndex == -1) {
            throw new IllegalArgumentException("Unable to locate a view index for the given model index: " + hierarchyColumnModelIndex);
        }

        // look up the hierarchical TableColumn
        final TableColumn viewColumn = table.getColumnModel().getColumn(viewColumnIndex);

        // dispose the old editor
        this.treeTableCellEditor.dispose();

        // install the new editor
        this.treeTableCellEditor = treeTableCellEditor;
        treeTableCellEditor.setShowExpanderForEmptyParent(showExpanderForEmptyParent);
        viewColumn.setCellEditor(treeTableCellEditor);
    }

    /**
     * Returns the <code>treeTableCellEditor</code> used to render the
     * hierarchy column of the tree table.
     */
    public TreeTableCellEditor getEditor() {
        return treeTableCellEditor;
    }

    /**
     * Use the given <code>editor</code> as the new delegate editor of the
     * {@link TreeTableCellEditor} which is responsible for editing the data
     * associated with each tree node in the hierarchy column.
     */
    public void setDelegateEditor(TableCellEditor editor) {
        checkAccessThread();

        treeTableCellEditor.setDelegate(editor);
    }

    /**
     * Returns the <code>editor</code> responsible for editing the data
     * associated with each tree node in the hierarchy column.
     */
    public TableCellEditor getDelegateEditor() {
        return treeTableCellEditor.getDelegate();
    }

    /**
     * A convenience method to ensure {@link TreeTableSupport} is being
     * accessed from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("TreeTableSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
        }
    }

    /**
     * This listener watches for mouse clicks overtop of the expander button
     * within a renderer in a hierarchical column and toggles the expansion
     * of the tree node, if possible, without calling the delegate. In all
     * other cases (where the mouse click does not toggle expansion), the
     * delegate is allowed to handle the mouse click as normal.
     */
    private class ExpandAndCollapseMouseListener implements MouseListener {

        /** An optional MouseListener to which we delegate after performing our own processing. */
        private final MouseListener delegate;

        /**
         * Decorate the given <code>delegate</code> with extra functionality.
         */
        public ExpandAndCollapseMouseListener(MouseListener delegate) {
            if (delegate == null) {
                throw new IllegalArgumentException("delegate may not be null");
            }

            this.delegate = delegate;
        }

        /**
         * Return the {@link MouseListener} being decorated.
         */
        public MouseListener getDelegate() {
            return delegate;
        }

        @Override
        public void mousePressed(MouseEvent me) {
            // if the table isn't enabled, break early
            if (!table.isEnabled()) {
                return;
            }

            // we're going to check if the single click was overtop of the
            // expander button and toggle the expansion of the row if it was

            // extract information about the location of the click
            final JTable table = (JTable) me.getSource();
            final Point clickPoint = me.getPoint();
            final int row = table.rowAtPoint(clickPoint);
            final int column = table.columnAtPoint(clickPoint);

            // ensure a valid cell has clicked
            if (row == -1 || column == -1) {
                return;
            }

            // translate the clickPoint to be relative to the rendered component
            final Rectangle cellRect = table.getCellRect(row, column, true);
            clickPoint.translate(-cellRect.x, -cellRect.y);

            // if a left-click occurred over the expand/collapse button
            final TreeTableCellPanel renderedPanel = TreeTableUtilities.prepareRenderer(me);
            if (SwingUtilities.isLeftMouseButton(me) && renderedPanel != null && renderedPanel.isPointOverExpanderButton(clickPoint)) {
                treeList.getReadWriteLock().writeLock().lock();
                try {
                    // expand/collapse the row if possible
                    if (treeList.getAllowsChildren(row)) {
                        TreeTableUtilities.toggleExpansion(table, treeList, row).run();
                    }
                } finally {
                    treeList.getReadWriteLock().writeLock().unlock();
                }

                // return early and don't allow the delegate a chance to react to the mouse click
                return;
            }

            delegate.mousePressed(me);
        }

        @Override
        public void mouseClicked(MouseEvent me) { delegate.mouseClicked(me); }
        @Override
        public void mouseReleased(MouseEvent me) { delegate.mouseReleased(me); }
        @Override
        public void mouseEntered(MouseEvent me) { delegate.mouseEntered(me); }
        @Override
        public void mouseExited(MouseEvent me) { delegate.mouseExited(me); }
    }

    /**
     * This listener watches for space bar presses when cell focus is within
     * a hierarchical column and toggles the expansion of the tree node, if
     * possible.
     */
    private class ExpandAndCollapseKeyListener extends KeyAdapter {
        /**
         * A Runnable which restores the state of the selection model and the
         * client property "JTable.autoStartsEdit". We save the Runnable when
         * the backspace key is pressed and the focus is within the hierarchy
         * column. We execute it when the key is released to resume normal
         * handling of future keystrokes.
         */
        private Runnable restoreStateRunnable;

        @Override
        public void keyPressed(KeyEvent e) {
            if (!getSpaceKeyExpansionEnabled()) {
                return;
            }
            // if the table isn't enabled, break early
            if (!table.isEnabled()) {
                return;
            }

            // if the table doesn't own the focus, break early
            if (!table.isFocusOwner()) {
                return;
            }

            // if the key pressed is not the space bar, break early
            if (e.getKeyCode() != KeyEvent.VK_SPACE || e.getModifiers() != 0) {
                return;
            }

            final int row = table.getSelectionModel().getLeadSelectionIndex();
            final int column = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();

            // ensure a valid row has focus
            if (row == -1) {
                return;
            }

            // if a column is selected, ensure it is the hierarchy column
            if (column != -1 && table.convertColumnIndexToModel(column) != hierarchyColumnModelIndex) {
                return;
            }

            treeList.getReadWriteLock().writeLock().lock();
            try {
                // if the row is expandable, toggle its expanded state
                if (treeList.getAllowsChildren(row)) {
                    final Runnable r = TreeTableUtilities.toggleExpansion(table, treeList, row);
                    if (restoreStateRunnable == null) {
                        restoreStateRunnable = r;
                    }
                }
            } finally {
                treeList.getReadWriteLock().writeLock().unlock();
            }
        }

        /**
         * When the key is released, execute the Runnable which restores the
         * state of the selection model and "JTable.autoStartsEdit" client property.
         */
        @Override
        public void keyReleased(KeyEvent e) {
            if (restoreStateRunnable != null) {
                restoreStateRunnable.run();
                restoreStateRunnable = null;
            }
        }
    }

    /**
     * This listener watches for ENTER key presses when cell focus is within
     * a hierarchical column and toggles the expansion of the tree node, if
     * possible.
     */
    private class ArrowKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!arrowKeyExpansionEnabled) {
                return;
            }

            // if the table isn't enabled, break early
            if (!table.isEnabled()) {
                return;
            }

            // if column selection is allowed, we want normal table handling of
            // the arrow key, not this special tree node toggling logic
            if (table.getColumnSelectionAllowed()) {
                return;
            }

            // if the key pressed is not the space bar, break early
            final int c = e.getKeyCode();
            final boolean isLeftArrowKey = c == KeyEvent.VK_LEFT;
            final boolean isRightArrowKey = c == KeyEvent.VK_RIGHT;
            if (!(isLeftArrowKey || isRightArrowKey) || e.getModifiers() != 0) {
                return;
            }

            final int row = table.getSelectionModel().getLeadSelectionIndex();

            // ensure a valid cell has focus
            if (row == -1) {
                return;
            }

            treeList.getReadWriteLock().writeLock().lock();
            try {
                // if the row is expandable, toggle its expanded state
                if (treeList.getAllowsChildren(row)) {
                    final boolean expanded = treeList.isExpanded(row);
                    if ((expanded && isLeftArrowKey) || (!expanded && isRightArrowKey)) {
                        TreeTableUtilities.toggleExpansion(table, treeList, row).run();
                    }
                }
            } finally {
                treeList.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    /**
     * A ListEventListener that exists only to ensure that the TreeList
     * receives all ListEvents on the Swing Event Dispatch Thread, and not some
     * other random Thread. This is important because the treetable renderer
     * and editor read from the TreeList on the Swing EDT and must be able to
     * guarantee that their reads are atomic.
     */
    private static class EventDispathThreadChecker implements ListEventListener {
        @Override
        public void listChanged(ListEvent listChanges) {
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("TreeTableSupport has detected that its underlying TreeList was changed on a Thread that is NOT the Swing Event Dispatch Thread. " +
                        "This can cause unreliable results including sporadic exceptions since the TreeList is read from the EDT. Two solutions exist for this problem:\n\n" +
                        "a) ensure each and every write to the EventList pipeline occurs on the Swing EDT\n" +
                        "b) wrap the source EventList of TreeList in a Swing Thread Proxy List using GlazedListsSwing.swingThreadProxyList(...) before passing it to the TreeList constructor");
            }
        }
    }

    /**
     * This is an implementation of the ListSelectionModel interface that does
     * nothing. Consequently it never reports a change to the selection. Most
     * importantly it never reports lead or anchor indexes which implies that
     * when this is used as a column selection model, there is never a cell
     * selection, only row selections. This is desirable for readonly
     * treetables.
     */
    private static class NoopColumnSelectionModel implements ListSelectionModel {
        @Override
        public void setSelectionInterval(int index0, int index1) { }
        @Override
        public void addSelectionInterval(int index0, int index1) { }
        @Override
        public void removeSelectionInterval(int index0, int index1) { }
        @Override
        public int getMinSelectionIndex() { return -1; }
        @Override
        public int getMaxSelectionIndex() { return -1; }
        @Override
        public boolean isSelectedIndex(int index) { return false; }
        @Override
        public int getAnchorSelectionIndex() { return -1; }
        @Override
        public void setAnchorSelectionIndex(int index) { }
        @Override
        public int getLeadSelectionIndex() { return -1; }
        @Override
        public void setLeadSelectionIndex(int index) { }
        @Override
        public void clearSelection() { }
        @Override
        public boolean isSelectionEmpty() { return true; }
        @Override
        public void insertIndexInterval(int index, int length, boolean before) { }
        @Override
        public void removeIndexInterval(int index0, int index1) { }
        @Override
        public void setValueIsAdjusting(boolean valueIsAdjusting) { }
        @Override
        public boolean getValueIsAdjusting() { return false; }
        @Override
        public void setSelectionMode(int selectionMode) { }
        @Override
        public int getSelectionMode() { return ListSelectionModel.SINGLE_SELECTION; }
        @Override
        public void addListSelectionListener(ListSelectionListener x) { }
        @Override
        public void removeListSelectionListener(ListSelectionListener x) { }
    }
}