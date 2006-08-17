/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

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
    static final Border NODE_COMPONENT_BORDER = BorderFactory.createEmptyBorder(0, 3, 0, 0);

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
     * @param depth the depth of the tree node in the hierarchy
     * @param isExpandable <tt>true</tt> if the tree node can be expanded/collapsed;
     *      <tt>false</tt> otherwise
     * @param isExpanded <tt>true</tt> if the node is expanded and its children are thus visible;
     *      <tt>false</tt> if it is collapsed and its children are thus hidden. This argument
     *      only has meaning when <code>isExpandable</code> is true; otherwise it is ignored.
     * @param nodeComponent a Component which displays the data of the tree node
     */
    public void configure(int depth, boolean isExpandable, boolean isExpanded, Component nodeComponent) {
        // the expander button is only visible when the node can be expanded/collapsed
        expanderButton.setVisible(isExpandable);

        // if the tree node is expandable, pick an icon for the expander button
        if (isExpandable)
            expanderButton.setIcon(isExpanded ? TreeTableSupport.getExpandedIcon() : TreeTableSupport.getCollapsedIcon());

        // synchronize the background color of this entire panel with nodeComponent
        setBackground(nodeComponent.getBackground());

        // replace any kind of border with an empty one that tabs the node component over from the expander button
        if (nodeComponent instanceof JComponent) {
            final JComponent jNodeComponent = (JComponent) nodeComponent;
            jNodeComponent.setBorder(NODE_COMPONENT_BORDER);
            setToolTipText(jNodeComponent.getToolTipText());
        }

        // configure this panel with the updated space/expander button and the supplied nodeComponent
        // taking care to give the nodeComponent *ALL* excess space (not just its preferred size)
        removeAll();
        add(getSpacerComponent(depth), TreeTableCellLayout.SPACER);
        add(expanderButton, TreeTableCellLayout.EXPANDER_BUTTON);
        add(nodeComponent, TreeTableCellLayout.NODE_COMPONENT);

        this.nodeComponent = nodeComponent;
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
    private Component getSpacerComponent(int depth) {
        if (depth >= spacerComponentsCache.size()) {
            for (int i = spacerComponentsCache.size(); i <= depth; i++) {
                final Component spacer = Box.createHorizontalStrut(TreeTableSupport.getExpandedIcon().getIconWidth() * i);
                spacerComponentsCache.add(spacer);
            }
        }

        return spacerComponentsCache.get(depth);
    }

    /**
     * A custom layout that grants preferred width to a spacer component and
     * expander button and the remaining width to the node component. When
     * insufficient space exists for all 3 components, they are simply cropped.
     */
    private static class TreeTableCellLayout implements LayoutManager2 {

        // constraints Objects to be used when adding components to the layout
        public static final Object SPACER = new Object();
        public static final Object EXPANDER_BUTTON = new Object();
        public static final Object NODE_COMPONENT = new Object();

        // each of the known components layed out by this layout manager
        private Component spacer;
        private Component expanderButton;
        private Component nodeComponent;

        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints == SPACER) spacer = comp;
            else if (constraints == EXPANDER_BUTTON) expanderButton = comp;
            else if (constraints == NODE_COMPONENT) nodeComponent = comp;
            else throw new IllegalArgumentException("Unexpected constraints object: " + constraints);
        }

        public void removeLayoutComponent(Component comp) {
            if (comp == spacer) spacer = null;
            if (comp == expanderButton) expanderButton = null;
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
            if (availableWidth > 0 && expanderButton != null) {
                int expanderButtonX = totalWidth - availableWidth;
                int expanderButtonY = 0;
                int expanderButtonWidth = expanderButton.getPreferredSize().width;
                int expanderButtonHeight = expanderButton.getPreferredSize().height;

                if (expanderButtonHeight > totalHeight)
                    expanderButtonHeight = totalHeight;
                else if (expanderButtonHeight < totalHeight)
                    expanderButtonY = (totalHeight - expanderButtonHeight) / 2;

                expanderButton.setBounds(expanderButtonX, expanderButtonY, expanderButtonWidth, expanderButtonHeight);
                availableWidth -= expanderButtonWidth;
            }

            // 3. layout the node component (centered vertically and getting all remaining horizontal space)
            if (availableWidth > 0 && nodeComponent != null) {
                int nodeComponentX = totalWidth - availableWidth;
                int nodeComponentY = 0;
                int nodeComponentWidth = availableWidth;
                int nodeComponentHeight = nodeComponent.getPreferredSize().height;

                if (nodeComponentHeight > totalHeight)
                    nodeComponentHeight = totalHeight;
                else if (nodeComponentHeight < totalHeight)
                    nodeComponentY = (totalHeight - nodeComponentHeight) / 2;

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