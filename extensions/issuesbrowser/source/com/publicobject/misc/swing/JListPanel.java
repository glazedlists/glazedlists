/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.misc.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

/**
 * A JListPanel is akin to a JList except that the components are not rendered
 * but are actually present on the panel, and can be reordered via drag and
 * drop <strong>within</strong> the JListPanel. That is, components cannot be
 * dragged into or out of the JListPanel. They can only be reordered within the
 * JListPanel itself. The model behind the JListPanel is simply an
 * {@link EventList} containing {@link Component} objects. Changes made to that
 * {@link EventList} are immediately reflected in the JListPanel.
 *
 * @author James Lemieux
 */
public class JListPanel<C extends Component> extends JPanel implements ListEventListener<C> {

    /** The list of components contained on this JListPanel. */
    private EventList<C> model;

    /** The custom layout manager that lays out the model's components. */
    private final ListPanelLayoutManager layoutManager = new ListPanelLayoutManager();

    /** The model component being dragged or <code>null</code> if no component is being dragged. */
    private C dndComponent;

    /** The location at which the drag began. */
    private Point dndOrigin;

    /** The last sampled location of the drag. (the distance between dndOrigin and dndCurrent is the vertical drag distance) */
    private Point dndCurrent;

    /**
     * Create an empty JListPanel.
     */
    public JListPanel() {
        this(new BasicEventList<C>());
    }

    /**
     * Create a JListPanel backed by the given <code>model</code>.
     *
     * @param model an {@link EventList} of {@link Component} objects to be
     *      displayed within this JListPanel
     */
    public JListPanel(EventList<C> model) {
        // install a custom LayoutManager capable of laying out the model's components
        setLayout(layoutManager);

        // watch as components are add/removed to this JListPanel and install/uninstall dndMouseListener from them
        addContainerListener(new ContainerWatcher());

        // initialize the model
        setModel(model);
    }

    /**
     * Returns the {@link EventList} of {@link Component} objects to be displayed.
     * This list of components functions as the model for this JListPanel.
     */
    public EventList<C> getModel() {
        return model;
    }

    /**
     * Sets the {@link EventList} of {@link Component} objects to be displayed
     * by this JListPanel.
     *
     * @throws IllegalArgumentException if <code>model</code> is <code>null</code>
     */
    public void setModel(EventList<C> model) {
        if (model == null)
            throw new IllegalArgumentException("model cannot be null");

        // stop listening to the old model
        if (this.model != null)
            this.model.removeListEventListener(this);

        this.model = model;

        // add the components from the new model to the component hiearchy
        relayout();

        // start listening to the new model
        this.model.addListEventListener(this);
    }

    /**
     * A convenience method to tear off all components and then re-add them
     * such that the {@link #dndComponent} is always rendered on top of all
     * other components (if a dndComponent exists)
     */
    private void relayout() {
        // re-add all components so that their relative z-coordinates are correct
        removeAll();

        // add the dndComponent first (if there is one) so that it is drawn last (on top)
        if (model.contains(dndComponent))
            add(dndComponent);

        // add the remainder of the components in order
        for (Iterator<C> i = model.iterator(); i.hasNext();) {
            C c = i.next();
            if (c != dndComponent)
                add(c);
        }

        // have the LayoutManager recalculate positions before the next repaint
        revalidate();
    }

    /**
     * As changes occur to the underlying model of components, this method
     * reacts by keeping the component hierarchy of the JListPanel in sync
     * with those changes.
     */
    public void listChanged(ListEvent<C> listChanges) {
        // incorporate the model changes into the view
        relayout();

        // repaint the panel
        repaint();
    }

    /**
     * This MouseListener is used to simulate drag and drop abilities within the
     * JListPanel. Each Component in the model is watched by this MouseListener
     * for mouse clicks and drags. When they occur, the listener reacts by
     * recording information about the component being dragged and then
     * revalidates/repaints the JListPanel. The layout manager then cooperates
     * by positioning the dragged component according to the mouse position. In
     * this way, the visual effects of drag and drop gestures are recreated.
     */
    private class DnDMouseListener extends MouseAdapter implements MouseMotionListener {
        @Override
        public void mousePressed(MouseEvent e) {
            // record the particulars of the new DND component
            dndComponent = (C) e.getComponent();
            dndOrigin = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), JListPanel.this);
            dndCurrent = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), JListPanel.this);

            // adjust the component z-orders so that the DND component is drawn on top
            relayout();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // when the mouse is released, we must try to insert the component into its new location
            final Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), JListPanel.this);

            // try to locate the new index for the DND component
            int newIndexOfDnDComponent = -1;
            for (int i = 0, n = model.size(); i < n; i++) {
                final Component c = model.get(i);
                if (c == dndComponent) continue;

                final Rectangle bounds = c.getBounds();
                if (bounds.y <= p.y && (bounds.y + bounds.height) >= p.y) {
                    newIndexOfDnDComponent = i;
                    break;
                }
            }

            // check if the drop location is above the first component
            if (p.y < model.get(0).getBounds().y)
                newIndexOfDnDComponent = 0;

            // check if the drop location is below the last component
            final Rectangle lastComponentBounds = model.get(model.size() - 1).getBounds();
            if (p.y > lastComponentBounds.y + lastComponentBounds.height)
                newIndexOfDnDComponent = model.size()-1;

            if (newIndexOfDnDComponent != -1) {
                // move the component from its old location to its new location
                model.remove(dndComponent);
                model.add(newIndexOfDnDComponent, dndComponent);
            }

            // remove all traces of the DND component
            dndComponent = null;
            dndOrigin = null;
            dndCurrent = null;

            revalidate();
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            // reposition the DND component relative to the mouse cursor
            dndCurrent = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), JListPanel.this);

            revalidate();
            repaint();
        }

        public void mouseMoved(MouseEvent e) { }
    }

    /**
     * This ContainerListener watches components as they are added/removed from
     * the container. A MouseListener is installed/uninstalled appropriately to
     * aid in providing drag and drop services to each model component.
     */
    private class ContainerWatcher implements ContainerListener {
        /** The MouseListener installed on each of the model's components to detect drag and drop gestures. */
        private final DnDMouseListener dndMouseListener = new DnDMouseListener();

        public void componentAdded(ContainerEvent e) {
            e.getChild().addMouseListener(dndMouseListener);
            e.getChild().addMouseMotionListener(dndMouseListener);
        }

        public void componentRemoved(ContainerEvent e) {
            e.getChild().removeMouseListener(dndMouseListener);
            e.getChild().removeMouseMotionListener(dndMouseListener);
        }
    }

    /**
     * A custom LayoutManager that lays out out each component in the
     * JListPanel's model.
     */
    private class ListPanelLayoutManager implements LayoutManager2 {
        public void layoutContainer(Container target) {
            final Insets insets = target.getInsets();
            final int totalWidth = target.getWidth() - insets.left - insets.right;

            int y = insets.top;

            // position each Component within the model
            for (Iterator<C> i = model.iterator(); i.hasNext();) {
                Component jc = i.next();
                final Dimension preferredSize = jc.getPreferredSize();
                jc.setBounds(insets.left, y, totalWidth, preferredSize.height);
                y += preferredSize.height;
            }

            // if a drag is currently happening, reposition the dragged component
            if (dndComponent != null) {
                final int verticalDistance = dndCurrent.y - dndOrigin.y;
                final Rectangle dndComponentBounds = dndComponent.getBounds();
                dndComponentBounds.translate(0, verticalDistance);
                dndComponent.setBounds(dndComponentBounds);
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            int x = 0, y = 0;

            for (Iterator<C> i = model.iterator(); i.hasNext();) {
                final Dimension minimumSize = i.next().getMinimumSize();
                x = Math.max(minimumSize.width, x);
                y += minimumSize.height;
            }

            final Insets i = parent.getInsets();
            x += i.left + i.right;
            y += i.top + i.bottom;

            return new Dimension(x, y);
        }

        public Dimension maximumLayoutSize(Container parent) {
            int x = 0, y = 0;

            for (Iterator<C> i = model.iterator(); i.hasNext();) {
                final Dimension maximumSize = i.next().getMaximumSize();
                x = Math.max(maximumSize.width, x);
                y += maximumSize.height;
            }

            final Insets i = parent.getInsets();
            x += i.left + i.right;
            y += i.top + i.bottom;

            return new Dimension(x, y);
        }

        public Dimension preferredLayoutSize(Container parent) {
            int x = 0, y = 0;

            for (Iterator<C> i = model.iterator(); i.hasNext();) {
                final Dimension preferredSize = i.next().getPreferredSize();
                x = Math.max(preferredSize.width, x);
                y += preferredSize.height;
            }

            final Insets i = parent.getInsets();
            x += i.left + i.right;
            y += i.top + i.bottom;

            return new Dimension(x, y);
        }

        public void addLayoutComponent(Component comp, Object constraints) { }
        public void removeLayoutComponent(Component comp) { }
        public void invalidateLayout(Container target) { }
        public float getLayoutAlignmentX(Container target) { throw new UnsupportedOperationException(); }
        public float getLayoutAlignmentY(Container target) { throw new UnsupportedOperationException(); }
        public void addLayoutComponent(String name, Component comp) { throw new UnsupportedOperationException(); }
    }
}