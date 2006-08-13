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
 * {@link javax.swing.table.TableCellRenderer} that is appropriate for use in a
 * hierarchy column of a tree table. Specifically, it combines three different
 * components to product a single panel. Those components are:
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
 * @author James Lemieux
 */
class TreeTableCellPanel extends JPanel {

    /** A cache of appropriate spacer components for each depth in the tree. */
    private final List<Component> spacerComponentsCache = new ArrayList<Component>();

    /** The button to toggle the expanded/collapsed state of the tree node. */
    private JButton expanderButton = new JButton();

    public TreeTableCellPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // configure the expander button to only display its icon
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
     * <code>isLeaf</code> is <tt>false</tt>. The expander button's icon is
     * either a traditional plus or minus icon depending on the value of
     * <code>isExpanded</code>.
     *
     * <p>The <strong>nodeComponent</strong> is displayed unmodified.
     *
     * @param depth the depth of the tree node in the hierarchy
     * @param isExpanded <tt>true</tt> if the node is expanded and its children are thus visible; <tt>false</tt> if it is collapsed and the 
     * @param isLeaf
     * @param nodeComponent
     */
    public void configure(int depth, boolean isExpanded, boolean isLeaf, Component nodeComponent) {
        expanderButton.setVisible(!isLeaf);

        if (!isLeaf)
            expanderButton.setIcon(isExpanded ? TreeTableSupport.getExpandedIcon() : TreeTableSupport.getCollapsedIcon());

        setBackground(nodeComponent.getBackground());

        removeAll();
        add(getSpacerComponent(depth));
        add(expanderButton);
        add(nodeComponent);
    }

    private Component getSpacerComponent(int depth) {
        if (depth >= spacerComponentsCache.size()) {
            for (int i = spacerComponentsCache.size(); i <= depth; i++) {
                spacerComponentsCache.add(Box.createHorizontalStrut(TreeTableSupport.getExpandedIcon().getIconWidth() * i));
            }
        }

        return spacerComponentsCache.get(depth);
    }
}