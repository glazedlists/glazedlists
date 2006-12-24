/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import java.awt.*;

/**
 * Helper class for decorating a {@link java.awt.LayoutManager2}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
abstract class LayoutDecorator implements LayoutManager2 {
    protected LayoutManager2 delegateLayout;

    public void addLayoutComponent(Component component, Object constraints) {
        delegateLayout.addLayoutComponent(component, constraints);
    }

    public Dimension maximumLayoutSize(Container target) {
        return delegateLayout.maximumLayoutSize(target);
    }

    public float getLayoutAlignmentX(Container target) {
        return delegateLayout.getLayoutAlignmentX(target);
    }

    public float getLayoutAlignmentY(Container target) {
        return delegateLayout.getLayoutAlignmentY(target);
    }

    public void invalidateLayout(Container target) {
        delegateLayout.invalidateLayout(target);
    }

    public void addLayoutComponent(String name, Component component) {
        throw new UnsupportedOperationException();
    }

    public void removeLayoutComponent(Component component) {
        delegateLayout.removeLayoutComponent(component);
    }

    public Dimension preferredLayoutSize(Container container) {
        return delegateLayout.preferredLayoutSize(container);
    }

    public Dimension minimumLayoutSize(Container container) {
        return delegateLayout.minimumLayoutSize(container);
    }

    public void layoutContainer(Container container) {
        delegateLayout.layoutContainer(container);
    }
}