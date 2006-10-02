/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.amazonbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matchers;
import com.publicobject.amazonbrowser.TreeCriterion;
import com.publicobject.misc.swing.JListPanel;
import com.publicobject.misc.swing.RoundedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A TreeCriteriaEditor is a UI Component capable of editing a List of
 * TreeCriteria objects to specify:
 *
 * <ul>
 *   <li>which {@link TreeCriterion} objects are active, and thus contribute to the treetable hierarchy
 *   <li>the order of the active TreeCriterion objects, and thus the order of the treetable hierarchies
 * </ul>
 *
 * PropertyChangeEvents are fired when the list of active {@link TreeCriterion}
 * objects as returned by {@link #getActiveCriteria()} changes in any way.
 *
 * @author James Lemieux
 */
public class TreeCriteriaEditor extends JListPanel<TreeCriteriaEditor.TreeCriterionEditorPanel> {

    /** Only the active {@link TreeCriterion} objects. */
    private final EventList<TreeCriterion> activeCriteria;

    /** A ListEventListener that watches the {@link #activeCriteria} List for changes and broadcasts PropertyChangeEvents describing the change. */
    private final ListEventListener<TreeCriterion> activeCriteriaListener = new ActiveCriteriaListener();

    /**
     * Construct a TreeCriteriaEditor that allows users a chance to reorder the
     * {@link TreeCriterion} objects in the given <code>source</code> as well
     * as activating/deactivating them.
     *
     * @param source the List of all possible TreeCriterion objects available
     */
    public TreeCriteriaEditor(EventList<TreeCriterion> source) {
        super(new FunctionList<TreeCriterion,TreeCriterionEditorPanel>(source, new ForwardFunction(), new ReverseFunction()));
        setBackground(AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE);

        // build a filtered view of allCriteria that only contains active TreeCriterion objects
        this.activeCriteria = new FilterList<TreeCriterion>(source, Matchers.beanPropertyMatcher(TreeCriterion.class, "active", Boolean.TRUE));
        this.activeCriteria.addListEventListener(activeCriteriaListener);
    }

    /**
     * Returns an unmodifiable <strong>snapshot</strong> List of active
     * {@link TreeCriterion} objects.
     */
    public List<TreeCriterion> getActiveCriteria() {
        return Collections.unmodifiableList(new ArrayList<TreeCriterion>(activeCriteria));
    }

    /**
     * This ListEventListener translates changes to the List of active criteria
     * into PropertyChangeEvents for the activeCriteria property of this
     * TreeCriteriaEditor.
     */
    private class ActiveCriteriaListener implements ListEventListener<TreeCriterion> {
        public void listChanged(ListEvent<TreeCriterion> listChanges) {
            firePropertyChange("activeCriteria", null, getActiveCriteria());
        }
    }

    /**
     * A function capable of mapping TreeCriterion objects to the
     * TreeCriterionEditorPanel that edits them.
     */
    private static class ForwardFunction implements FunctionList.Function<TreeCriterion,TreeCriterionEditorPanel> {
        public TreeCriterionEditorPanel evaluate(TreeCriterion tc) {
            return new TreeCriterionEditorPanel(tc);
        }
    }

    /**
     * A function capable of mapping TreeCriterionEditorPanel objects back to the
     * TreeCriterion that edits them.
     */
    private static class ReverseFunction implements FunctionList.Function<TreeCriterionEditorPanel,TreeCriterion> {
        public TreeCriterion evaluate(TreeCriterionEditorPanel c) {
            return c.getTreeCriterion();
        }
    }

    /**
     * A special panel that combines a JButton which indicates the active state
     * of a TreeCriterion with a JLabel that displays the name of the
     * TreeCriterion.
     */
    public static class TreeCriterionEditorPanel extends JPanel implements PropertyChangeListener {

        private final JButton activeButton = new JButton();
        private final JLabel nameLabel = new JLabel();
        private final TreeCriterion treeCriterion;

        public TreeCriterionEditorPanel(TreeCriterion tc) {
            super(new BorderLayout());
            this.treeCriterion = tc;
            this.treeCriterion.addPropertyChangeListener(this);
            setBackground(AmazonBrowser.AMAZON_SEARCH_DARK_BLUE);
            setBorder(new RoundedBorder(AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE, AmazonBrowser.AMAZON_SEARCH_LIGHT_BLUE, AmazonBrowser.AMAZON_SEARCH_DARK_BLUE, 6, 1));

            activeButton.addActionListener(new ButtonHandler());
            activeButton.setBorder(BorderFactory.createEmptyBorder());
            activeButton.setContentAreaFilled(false);
            activeButton.setFocusable(false);
            activeButton.setFont(nameLabel.getFont().deriveFont(8));

            nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            nameLabel.setForeground(AmazonBrowser.AMAZON_TAB_LIGHT_BEIGE);
            nameLabel.setFont(nameLabel.getFont().deriveFont(8));

            add(activeButton, BorderLayout.WEST);
            add(nameLabel, BorderLayout.CENTER);

            update();
        }

        public TreeCriterion getTreeCriterion() {
            return treeCriterion;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            update();
        }

        private void update() {
            // todo, Jesse: make this a cool image!
            activeButton.setText(treeCriterion.isActive() ? "X" : "O");
            nameLabel.setText(treeCriterion.getName());
        }

        private class ButtonHandler implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                treeCriterion.setActive(!treeCriterion.isActive());
            }
        }
    }
}