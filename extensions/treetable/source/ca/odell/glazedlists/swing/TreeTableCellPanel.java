/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * This panel exists to relieve the burdens of creating a
 * {@link javax.swing.table.TableCellRenderer} or
 * {@link javax.swing.table.TableCellEditor} that is appropriate for use in a
 * hierarchy column of a tree table. Specifically, it combines three different
 * components to produce a single panel. Those components are:
 *
 * <ul>
 *   <li>An indenter component whose width reflects the depth of the tree node being rendered.
 *       This component really determines the look of the hierarchy.
 *   <li>A collapse/expand button that is visible for parent tree nodes but invisible for
 *       leaf nodes. If the button is visible it will display one of two icons to represent
 *       the expanded or collapsed state of the row.
 *   <li>A spacer component whose width provides the space between the collapse/expand button
 *       and the user supplied component
 *   <li>A user supplied component representing the actual data for the tree node.
 * </ul>
 *
 * The components are arranged on the panel like this:
 *
 * <p><strong>[indent pixels] [expand/collapse button] [spacer pixels] [nodeComponent]</strong>
 *
 * @author James Lemieux
 */
public class TreeTableCellPanel extends JPanel {

    /** The border that spaces the delegate component over from the expander button. */
    private static final Border NO_FOCUS_BORDER = BorderFactory.createEmptyBorder(0, 0, 0, 0);

    /** A cache of appropriate indenter/spacer components for each unique width in the tree. */
    private final Map<Integer, Component> spacerComponentsCache = new HashMap<Integer, Component>();

    /** The button to toggle the expanded/collapsed state of the tree node. */
    private final JButton expanderButton = new JButton();

    /** The last installed node component, if any. */
    private Component nodeComponent;

    public TreeTableCellPanel() {
        super(new TreeTableCellLayout());

        // configure the expander button to display its icon with no margins
        expanderButton.setBorder(BorderFactory.createEmptyBorder());
        expanderButton.setContentAreaFilled(false);
        expanderButton.setFocusable(false);
    }

    /**
     * This method adjusts the contents of this panel to display thse given
     * <code>nodeComponent</code>. Specifically, the panel is layed out like so:
     *
     * <p><strong>[indent pixels] [expand/collapse button] [spacer pixels] [nodeComponent]</strong>
     *
     * <p>The <strong>expand/collapse button</strong> is visible if
     * {@link TreeNodeData#isExpanded()} returns <tt>true</tt>. The expander
     * button's icon is either a traditional plus or minus icon depending on
     * the value of {@link TreeNodeData#isExpanded()}.
     *
     * <p>The <strong>nodeComponent</strong> is displayed unmodified.
     *
     * @param treeNodeData hierarhical information about the node within the tree
     * @param showExpanderForEmptyParent <tt>true</tt> indicates the expander
     *      button should always be present, even when no children yet exist
     * @param nodeComponent a Component which displays the data of the tree node
     * @param hasFocus <tt>true</tt> indicates the cell currently has the focus
     * @param indent the amount of space between the left cell edge and the expand/collapse button
     * @param spacer the amount of space between the right edge of the expand/collapse button and the nodeComponent
     */
    public void configure(TreeNodeData treeNodeData, boolean showExpanderForEmptyParent, Component nodeComponent, boolean hasFocus, int indent, int spacer) {
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
        add(getSpacer(indent), TreeTableCellLayout.INDENTER);
        add(showExpanderButton ? expanderButton : createSpacer(UIManager.getIcon("Tree.expandedIcon").getIconWidth()), TreeTableCellLayout.EXPANDER);
        add(getSpacer(spacer), TreeTableCellLayout.SPACER);
        add(nodeComponent, TreeTableCellLayout.NODE_COMPONENT);

        this.nodeComponent = nodeComponent;

        // bring the nodeComponent and its enclosing panel into agreement about their shared UI properties
        super.setBackground(nodeComponent.getBackground());
        super.setForeground(nodeComponent.getForeground());
        super.setFont(nodeComponent.getFont());
    }

    /**
     * Set the background color of the TreeTableCellPanel and node component.
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);

        if (nodeComponent != null)
            nodeComponent.setBackground(bg);
    }

    /**
     * Set the foreground color of the TreeTableCellPanel and node component.
     */
    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);

        if (nodeComponent != null)
            nodeComponent.setForeground(fg);
    }

    /**
     * Set the font of the TreeTableCellPanel and its inner node component.
     */
    @Override
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
    private Component getSpacer(int width) {
        final Integer key = new Integer(width);

        Component spacer = spacerComponentsCache.get(key);
        if (spacer == null) {
            spacer = createSpacer(width);
            spacerComponentsCache.put(key, spacer);
        }

        return spacer;
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
    @Override
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
    @Override
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

        @Override
        public void run() {
            component.requestFocus();
            component.dispatchEvent(keyEvent);
        }
    }

    /**
     * A custom layout that grants preferred width to a indenter component,
     * expander button and spacer component and the remaining width to the node
     * component. When insufficient space exists for all 4 components, they are
     * simply cropped.
     */
    private static class TreeTableCellLayout implements LayoutManager2 {

        // constraints Objects to be used when adding components to the layout
        public static final Object INDENTER = new Object();
        public static final Object EXPANDER = new Object();
        public static final Object SPACER = new Object();
        public static final Object NODE_COMPONENT = new Object();

        // each of the known components layed out by this layout manager
        private Component indenter;
        private Component expander;
        private Component spacer;
        private Component nodeComponent;

        @Override
        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints == INDENTER) indenter = comp;
            else if (constraints == EXPANDER) expander = comp;
            else if (constraints == SPACER) spacer = comp;
            else if (constraints == NODE_COMPONENT) nodeComponent = comp;
            else throw new IllegalArgumentException("Unexpected constraints object: " + constraints);
        }

        @Override
        public void removeLayoutComponent(Component comp) {
            if (comp == indenter) indenter = null;
            if (comp == expander) expander = null;
            if (comp == spacer) spacer = null;
            if (comp == nodeComponent) nodeComponent = null;
        }

        @Override
        public void layoutContainer(Container target) {
            // 0. calculate the amount of space we have to work with
            final Insets insets = target.getInsets();
            final int totalWidth = target.getWidth() - insets.left - insets.right;
            final int totalHeight = target.getHeight() - insets.top - insets.bottom;
            int availableWidth = totalWidth;

            // no height means no reason to layout
            if (totalHeight <= 0) return;

            // 1. layout the indenter
            if (availableWidth > 0 && indenter != null) {
                final int spacerWidth = indenter.getPreferredSize().width;
                indenter.setBounds(0, 0, spacerWidth, totalHeight);
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

            // 3. layout the spacer
            if (availableWidth > 0 && spacer != null) {
                final int spacerX = totalWidth - availableWidth;
                final int spacerWidth = spacer.getPreferredSize().width;
                spacer.setBounds(spacerX, 0, spacerWidth, totalHeight);
                availableWidth -= spacerWidth;
            }

            // 4. layout the node component (centered vertically and getting all remaining horizontal space)
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

        @Override
        public Dimension preferredLayoutSize(Container target) {
            final Dimension preferredSize = new Dimension(0, 0);

            if (indenter != null) {
                final Dimension indenterSize = indenter.getPreferredSize();
                preferredSize.height = Math.max(preferredSize.height, indenterSize.height);
                preferredSize.width += indenterSize.width;
            }

            if (expander != null) {
                final Dimension expanderSize = expander.getPreferredSize();
                preferredSize.height = Math.max(preferredSize.height, expanderSize.height);
                preferredSize.width += expanderSize.width;
            }

            if (spacer != null) {
                final Dimension spacerSize = spacer.getPreferredSize();
                preferredSize.height = Math.max(preferredSize.height, spacerSize.height);
                preferredSize.width += spacerSize.width;
            }

            if (nodeComponent != null) {
                final Dimension nodeComponentSize = nodeComponent.getPreferredSize();
                preferredSize.height = Math.max(preferredSize.height, nodeComponentSize.height);
                preferredSize.width += nodeComponentSize.width;
            }

            final Insets insets = target.getInsets();
            preferredSize.height += insets.top + insets.bottom;
            preferredSize.width += insets.left + insets.right;

            return preferredSize;
        }

        @Override
        public void invalidateLayout(Container target) { }
        @Override
        public float getLayoutAlignmentX(Container target) { throw new UnsupportedOperationException(); }
        @Override
        public float getLayoutAlignmentY(Container target) { throw new UnsupportedOperationException(); }
        @Override
        public void addLayoutComponent(String name, Component comp) { throw new UnsupportedOperationException(); }
        @Override
        public Dimension minimumLayoutSize(Container parent) { throw new UnsupportedOperationException(); }
        @Override
        public Dimension maximumLayoutSize(Container target) { throw new UnsupportedOperationException(); }
    }
}