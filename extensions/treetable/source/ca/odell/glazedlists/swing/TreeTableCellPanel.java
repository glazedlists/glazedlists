/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import javax.swing.*;
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
 *       leaves. If the button is visible it will display one of two icon to represent the
 *       expanded or collapsed state of the row.
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

    /** A cache of appropriate spacer components for each depth in the tree. */
    private final List<Component> spacerComponentsCache = new ArrayList<Component>();

    /** The button to toggle the expanded/collapsed state of the tree node. */
    public JButton expanderButton = new JButton();

    /** A panel containing the spacer component and expander button. */
    private final JPanel spacerAndExpanderPanel = new JPanel();

    /** The last installed node component, if any. */
    public Component spaceComponent;

    /** The last installed node component, if any. */
    private Component nodeComponent;

    public TreeTableCellPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));

        spacerAndExpanderPanel.setOpaque(false);
        spacerAndExpanderPanel.setLayout(new BoxLayout(spacerAndExpanderPanel, BoxLayout.X_AXIS));

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

        // configure the spacer/expander button panel with the new spacer
        spacerAndExpanderPanel.removeAll();
        spacerAndExpanderPanel.add(getSpacerComponent(depth));
        spacerAndExpanderPanel.add(expanderButton);

        // configure this panel with the updated space/expander button and the supplied nodeComponent
        // taking care to give the nodeComponent *ALL* excess space (not just its preferred size)
        removeAll();
        add(spacerAndExpanderPanel, BorderLayout.WEST);
        add(nodeComponent, BorderLayout.CENTER);

        spaceComponent = getSpacerComponent(depth);
        this.nodeComponent = nodeComponent;
    }

    /**
     * Return the {@link Component} that displays the data of the tree node.
     */
    public JButton getNodeComponent() {
        return expanderButton;
    }

    /**
     * Returns <tt>true</tt> if <code>p</code> occurs within the bounds of the
     * expander button; <tt>false</tt> otherwise.
     */
    public boolean isPointOverExpanderButton(Point p) {
        return expanderButton.isVisible() && SwingUtilities.getDeepestComponentAt(spacerAndExpanderPanel, p.x, p.y) == expanderButton;
    }

    /**
     * Returns <tt>true</tt> if <code>p</code> occurs within the bounds of the
     * node component; <tt>false</tt> otherwise.
     */
    public boolean isPointOverNodeComponent(Point p) {
        return nodeComponent != null && nodeComponent.isVisible() && nodeComponent.getBounds().contains(p);
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
}