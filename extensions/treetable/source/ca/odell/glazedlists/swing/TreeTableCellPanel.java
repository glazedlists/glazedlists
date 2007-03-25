/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This panel exists to relieve the burdens of creating a
 * {@link javax.swing.table.TableCellRenderer} or
 * {@link javax.swing.table.TableCellEditor} that is appropriate for use in a
 * hierarchy column of a tree table. Specifically, it combines three different
 * components to produce a single panel. Those components are:
 *
 * <ul>
 *   <li>A spacer component whose width reflects the depth of the tree node being rendered.
 *       This component really determines the look of the hierarchy.
 *   <li>A collapse/expand button that is visible for parent tree nodes but invisible for
 *       leaf nodes. If the button is visible it will display one of two icons to represent
 *       the expanded or collapsed state of the row.
 *   <li>A user supplied component representing the actual data for the tree node.
 * </ul>
 *
 * The components are arranged on the panel like this:
 *
 * <p><strong>[spacer component] [expand/collapse button] [nodeComponent]</strong>
 *
 * @author James Lemieux
 */
class TreeTableCellPanel extends JPanel {

    /** The border that spaces the delegate component over from the expander button. */
    private static final Border NO_FOCUS_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 0);

    /** A cache of appropriate spacer components for each depth in the tree. */
    private final List<Component> spacerComponentsCache = new ArrayList<Component>();

    /** The button to toggle the expanded/collapsed state of the tree node. */
    private final JButton expanderButton = new JButton();

    /** The last installed node component, if any. */
    private Component nodeComponent;

    public TreeTableCellPanel() {
        super(new TreeTableCellLayout());

        // configure the expander button to display its icon with no margins
        this.expanderButton.setBorder(BorderFactory.createEmptyBorder());
        this.expanderButton.setContentAreaFilled(false);
        this.expanderButton.setFocusable(false);
    }

    /**
     * This method adjusts the contents of this panel to display the given
     * <code>nodeComponent</code>. Specifically, the panel is layed out like so:
     *
     * <p><strong>[spacer component] [expand/collapse button] [nodeComponent]</strong>
     *
     * <p>The <strong>spacer component</strong>'s width is calculated using the
     * <code>depth</code> of the tree node.
     *
     * <p>The <strong>expand/collapse button</strong> is visible if
     * <code>isExpandable</code> is <tt>true</tt>. The expander button's icon
     * is either a traditional plus or minus icon depending on the value of
     * <code>isExpanded</code>.
     *
     * <p>The <strong>nodeComponent</strong> is displayed unmodified.
     *
     * @param treeNodeData hierarhical information about the node within the tree
     * @param nodeComponent a Component which displays the data of the tree node
     */
    public void configure(TreeNodeData treeNodeData, boolean showExpanderForEmptyParent, Component nodeComponent, boolean hasFocus) {
        // if the tree node is expandable, pick an icon for the expander button
        final boolean showExpanderButton = treeNodeData.hasChildren() || (treeNodeData.allowsChildren() && showExpanderForEmptyParent);
        if(showExpanderButton)
            expanderButton.setIcon(UIManager.getIcon(treeNodeData.isExpanded() ? "Tree.expandedIcon" : "Tree.collapsedIcon"));

        // assign a default background color to this panel to attempt to remain consistent with the nodeComponent
        super.setBackground(nodeComponent.getBackground());

        // replace any kind of border with an empty one that tabs the node component over from the expander button
        if(nodeComponent instanceof JComponent) {
            final JComponent jNodeComponent = (JComponent) nodeComponent;
            setToolTipText(jNodeComponent.getToolTipText());

            // if the nodeComponent has focus, steal its border as the panel's own
            setBorder(hasFocus ? jNodeComponent.getBorder() : NO_FOCUS_BORDER);

            // now clear away the nodeComponent's border
            jNodeComponent.setBorder(NO_FOCUS_BORDER);
        }

        // configure this panel with the updated space/expander button and the supplied nodeComponent
        // taking care to give the nodeComponent *ALL* excess space (not just its preferred size)
        removeAll();
        add(getSpacer(showExpanderButton ? treeNodeData.getDepth() : treeNodeData.getDepth() + 1), TreeTableCellLayout.SPACER);
        add(showExpanderButton ? expanderButton : createSpacer(1), TreeTableCellLayout.EXPANDER);
        add(nodeComponent, TreeTableCellLayout.NODE_COMPONENT);

        this.nodeComponent = nodeComponent;
    }

    /**
     * Set the background color of the TreeTableCellPanel and node component.
     */
    public void setBackground(Color bg) {
        super.setBackground(bg);

        if (nodeComponent != null)
            nodeComponent.setBackground(bg);
    }

    /**
     * Set the foreground color of the TreeTableCellPanel and node component.
     */
    public void setForeground(Color fg) {
        super.setForeground(fg);

        if (nodeComponent != null)
            nodeComponent.setForeground(fg);
    }

    /**
     * Set the font of the TreeTableCellPanel and its inner node component.
     */
    public void setFont(Font font) {
        super.setFont(font);

        if (nodeComponent != null)
            nodeComponent.setFont(font);
    }

    /**
     * Return the {@link Component} that displays the data of the tree node.
     */
    public Component getNodeComponent() {
        return nodeComponent;
    }

    /**
     * Returns <tt>true</tt> if <code>p</code> occurs within the bounds of the
     * expander button; <tt>false</tt> otherwise.
     */
    public boolean isPointOverExpanderButton(Point p) {
        return expanderButton.isVisible() && SwingUtilities.getDeepestComponentAt(this, p.x, p.y) == expanderButton;
    }

    /**
     * Returns <tt>true</tt> if <code>p</code> occurs within the bounds of the
     * node component; <tt>false</tt> otherwise.
     */
    public boolean isPointOverNodeComponent(Point p) {
        return nodeComponent != null && nodeComponent.isVisible() && SwingUtilities.getDeepestComponentAt(this, p.x, p.y) == nodeComponent;
    }

    /**
     * Return a spacer component and update the spacer component cache as needed.
     */
    private Component getSpacer(int depth) {
        if (depth >= spacerComponentsCache.size()) {
            for (int i = spacerComponentsCache.size(); i <= depth; i++) {
                final Component spacer = createSpacer(UIManager.getIcon("Tree.expandedIcon").getIconWidth() * i);
                spacerComponentsCache.add(spacer);
            }
        }

        return spacerComponentsCache.get(depth);
    }

    /**
     * Creates a component which fills the given <code>width</code> of space.
     */
    private static Component createSpacer(int width) {
        return Box.createHorizontalStrut(width);
    }

    /**
     * This method is called by Swing when the TreeTableCellPanel is installed
     * as a TableCellEditor. It gives the component a chance to process the
     * KeyEvent. For example, a JTextField will honour the keystroke and
     * add the letter to its Document.
     *
     * The TreeTableCellPanel's main job is to pass the KeyEvent on to the
     * underlying {@link #nodeComponent} so that it may have a chance to react.
     * This only need occur once for the KeyEvent that caused the cell edit,
     * after which time the focus will be within the {@link #nodeComponent} and
     * subsequent keystrokes should be ignored.
     */
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        // let the nodeComponent have a crack at processing the KeyEvent
        // (we'd love to call nodeComponent.processKeyBinding(ks, e, condition, pressed) but it's protected and thus out of scope)
        if (!nodeComponent.hasFocus())
            SwingUtilities.invokeLater(new RequestFocusAndDispatchKeyEventRunnable(e, nodeComponent));

        // now let the JComboBox react (important for arrow keys to work as expected)
        return super.processKeyBinding(ks, e, condition, pressed);
    }

    /**
     * This method is called by Swing when installing this TreeTableCellPanel
     * as a TableCellEditor. It ensures that focus will return to the JTable
     * when the cell edit is complete.
     *
     * <p>We override this method to ensure that if the {@link #nodeComponent}
     * acting as the child editor of the TreeTableCellPanel has focus when the
     * cell edit is complete, focus is returned to the JTable in that case as
     * well.
     */
    public void setNextFocusableComponent(Component aComponent) {
        super.setNextFocusableComponent(aComponent);

        // set the next focusable component for the nodeComponent as well
        if (nodeComponent instanceof JComponent)
            ((JComponent) nodeComponent).setNextFocusableComponent(aComponent);
    }

    /**
     * This class aids in providing reasonable behaviour when the
     * TreeTableCellPanel is installed as a TableCellEditor. Specifically, if
     * it is installed due to a KeyEvent, this Runnable can be dispatched to the
     * EventQueue and when executed allows the child nodeComponent a chance to
     * react to the KeyEvent, as is customary with most TableCellEditors.
     */
    private static final class RequestFocusAndDispatchKeyEventRunnable implements Runnable {

        private final KeyEvent keyEvent;
        private final Component component;

        public RequestFocusAndDispatchKeyEventRunnable(KeyEvent keyEvent, Component component) {
            this.keyEvent = keyEvent;
            this.component = component;
        }

        public void run() {
            component.requestFocus();
            component.dispatchEvent(keyEvent);
        }
    }

    /**
     * A custom layout that grants preferred width to a spacer component and
     * expander button and the remaining width to the node component. When
     * insufficient space exists for all 3 components, they are simply cropped.
     */
    private static class TreeTableCellLayout implements LayoutManager2 {

        // constraints Objects to be used when adding components to the layout
        public static final Object SPACER = new Object();
        public static final Object EXPANDER = new Object();
        public static final Object NODE_COMPONENT = new Object();

        // each of the known components layed out by this layout manager
        private Component spacer;
        private Component expander;
        private Component nodeComponent;

        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints == SPACER) spacer = comp;
            else if (constraints == EXPANDER) expander = comp;
            else if (constraints == NODE_COMPONENT) nodeComponent = comp;
            else throw new IllegalArgumentException("Unexpected constraints object: " + constraints);
        }

        public void removeLayoutComponent(Component comp) {
            if (comp == spacer) spacer = null;
            if (comp == expander) expander = null;
            if (comp == nodeComponent) nodeComponent = null;
        }

        public void layoutContainer(Container target) {
            // 0. calculate the amount of space we have to work with
            final Insets insets = target.getInsets();
            final int totalWidth = target.getWidth() - insets.left - insets.right;
            final int totalHeight = target.getHeight() - insets.top - insets.bottom;
            int availableWidth = totalWidth;

            // no height means no reason to layout
            if (totalHeight <= 0) return;

            // 1. layout the spacer
            if (availableWidth > 0 && spacer != null) {
                final int spacerWidth = spacer.getPreferredSize().width;
                spacer.setBounds(0, 0, spacerWidth, totalHeight);
                availableWidth -= spacerWidth;
            }

            // 2. layout the expander button (centered vertically)
            if (availableWidth > 0 && expander != null) {
                int expanderButtonX = totalWidth - availableWidth;
                int expanderButtonY = insets.top;
                int expanderButtonWidth = expander.getPreferredSize().width;
                int expanderButtonHeight = expander.getPreferredSize().height;

                if (expanderButtonHeight > totalHeight)
                    expanderButtonHeight = totalHeight;
                else if (expanderButtonHeight < totalHeight)
                    expanderButtonY += (totalHeight - expanderButtonHeight) / 2;

                expander.setBounds(expanderButtonX, expanderButtonY, expanderButtonWidth, expanderButtonHeight);
                availableWidth -= expanderButtonWidth;
            }

            // space the nodeComponent 2 pixels right of the expander button
            availableWidth -= 2;

            // 3. layout the node component (centered vertically and getting all remaining horizontal space)
            if (availableWidth > 0 && nodeComponent != null) {
                int nodeComponentX = totalWidth - availableWidth;
                int nodeComponentY = insets.top;
                int nodeComponentWidth = availableWidth;
                int nodeComponentHeight = nodeComponent.getPreferredSize().height;

                if (nodeComponentHeight > totalHeight)
                    nodeComponentHeight = totalHeight;
                else if (nodeComponentHeight < totalHeight)
                    nodeComponentY += (totalHeight - nodeComponentHeight) / 2;

                nodeComponent.setBounds(nodeComponentX, nodeComponentY, nodeComponentWidth, nodeComponentHeight);
            }
        }

        public void invalidateLayout(Container target) { }
        public float getLayoutAlignmentX(Container target) { throw new UnsupportedOperationException(); }
        public float getLayoutAlignmentY(Container target) { throw new UnsupportedOperationException(); }
        public void addLayoutComponent(String name, Component comp) { throw new UnsupportedOperationException(); }
        public Dimension minimumLayoutSize(Container parent) { throw new UnsupportedOperationException(); }
        public Dimension maximumLayoutSize(Container target) { throw new UnsupportedOperationException(); }
        public Dimension preferredLayoutSize(Container parent) { throw new UnsupportedOperationException(); }
    }
}