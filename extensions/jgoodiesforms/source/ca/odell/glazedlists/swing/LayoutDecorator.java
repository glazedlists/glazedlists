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

    @Override
    public void addLayoutComponent(Component component, Object constraints) {
        delegateLayout.addLayoutComponent(component, constraints);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return delegateLayout.maximumLayoutSize(target);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return delegateLayout.getLayoutAlignmentX(target);
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return delegateLayout.getLayoutAlignmentY(target);
    }

    @Override
    public void invalidateLayout(Container target) {
        delegateLayout.invalidateLayout(target);
    }

    @Override
    public void addLayoutComponent(String name, Component component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeLayoutComponent(Component component) {
        delegateLayout.removeLayoutComponent(component);
    }

    @Override
    public Dimension preferredLayoutSize(Container container) {
        return delegateLayout.preferredLayoutSize(container);
    }

    @Override
    public Dimension minimumLayoutSize(Container container) {
        return delegateLayout.minimumLayoutSize(container);
    }

    @Override
    public void layoutContainer(Container container) {
        delegateLayout.layoutContainer(container);
    }
}