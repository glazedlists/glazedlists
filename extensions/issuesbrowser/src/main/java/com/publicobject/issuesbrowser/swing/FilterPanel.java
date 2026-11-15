/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.DefaultEventComboBoxModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueTrackingSystem;
import com.publicobject.issuesbrowser.Status;
import com.publicobject.misc.swing.RoundedBorder;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Manage a bunch of issue filters in a panel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class FilterPanel {

    /** the currently applied filters */
    private final EventList<CloseableFilterComponent> selectedFilterComponents = new BasicEventList<>();
    private final EventList<CloseableFilterComponent> remainingFilterComponents = new BasicEventList<>();

    private final CompositeMatcherEditor<Issue> matcherEditor;

    private final JPanel filtersPanel;
    private final JPanel filtersPanelPlusAddButton;
    private final JScrollPane filtersScrollPane;

    public FilterPanel(EventList<Issue> issues) {
        // select some initial filters
        this.selectedFilterComponents.add(new CloseableFilterComponent(new TextFilterComponent()));
        this.selectedFilterComponents.add(new CloseableFilterComponent(new SwingUsersMatcherEditor(issues)));
        // and then have the rest
        // TODO make StatusMatcherEditor dynamic, so we can switch issue tracking system (-> and supported stati) at runtime
//        final Status[] stati = IssueTrackingSystem.getJavaNetJira().getSupportedStati();
        final Status[] stati = IssueTrackingSystem.getGithub().getSupportedStati();
        this.remainingFilterComponents.add(new CloseableFilterComponent(new StatusMatcherEditor(issues, stati)));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new SwingLabelsMatcherEditor(issues)));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new PriorityMatcherEditor()));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new CreationDateMatcherEditor()));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new ModificationDateMatcherEditor()));
        // make 'em into matcher editors
        EventList<MatcherEditor<Issue>> matcherEditors = new FunctionList<>(selectedFilterComponents, CloseableFilterComponent::getMatcherEditor);
        this.matcherEditor = new CompositeMatcherEditor<>(matcherEditors);

        // create the filters panel
        this.filtersPanel = new JPanel(new GridBagLayout());
        this.filtersPanel.setBackground(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
        this.filtersPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        // and keep it in sync with the selected filters
        final EventList<CloseableFilterComponent> selectedFilterComponentsProxyList =
            GlazedListsSwing.swingThreadProxyList(selectedFilterComponents);
        selectedFilterComponentsProxyList.addListEventListener(listChanges -> layoutFilters(selectedFilterComponentsProxyList));
        layoutFilters(selectedFilterComponentsProxyList);
        // create a wrapped panel that adds the 'add' button
        this.filtersPanelPlusAddButton = new JPanel(new BorderLayout());
        this.filtersPanelPlusAddButton.add(filtersPanel, BorderLayout.NORTH);
        this.filtersPanelPlusAddButton.add(new AddFilterControl().getComponent(), BorderLayout.CENTER);

        this.filtersScrollPane = new JScrollPane(filtersPanelPlusAddButton, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public CompositeMatcherEditor<Issue> getMatcherEditor() {
        return matcherEditor;
    }

    public JComponent getComponent() {
        return filtersScrollPane;
    }

    /**
     * Show the specified filters in the filters panel, one row per filter in
     * list order. Rebuilding all rows is cheap for a handful of filters, and it
     * handles inserts, deletes and updates alike.
     */
    private void layoutFilters(EventList<CloseableFilterComponent> filters) {
        filtersPanel.removeAll();
        for(int i = 0; i < filters.size(); i++) {
            // 4px gap between two filters
            Insets insets = new Insets(i == 0 ? 0 : 4, 0, 0, 0);
            filtersPanel.add(filters.get(i).getWrapPanel(), new GridBagConstraints(0, i, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
        }
        filtersPanel.revalidate();
        filtersPanel.repaint();
    }

    /**
     * A cute little panel with a kill button to remove this filter.
     */
    public class CloseableFilterComponent implements ActionListener {
        private FilterComponent filterComponent;
        private JButton closeButton;
        private JLabel headerLabel;
        private JPanel headerPanel;
        private JPanel wrapPanel;

        public CloseableFilterComponent(FilterComponent filterComponent) {
            this.closeButton = new JButton();
            this.closeButton.addActionListener(this);
            this.closeButton.setOpaque(false);
            this.closeButton.setBorder(IssuesBrowser.EMPTY_ONE_PIXEL_BORDER);
            this.closeButton.setIcon(IssuesBrowser.X_ICON);
            this.closeButton.setContentAreaFilled(false);

            this.headerLabel = new JLabel();
            this.headerLabel.setHorizontalAlignment(JLabel.CENTER);
            this.headerLabel.setFont(headerLabel.getFont().deriveFont(10.0f));
            this.headerLabel.setForeground(IssuesBrowser.GLAZED_LISTS_DARK_BROWN);

            this.headerPanel = new JPanel();
            this.headerPanel.setOpaque(false);
            //this.headerPanel = new IssuesBrowser.GradientPanel(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_MEDIUM_BROWN, true);
            this.headerPanel.setLayout(new BorderLayout());
            this.headerPanel.add(headerLabel, BorderLayout.CENTER);
            this.headerPanel.add(closeButton, BorderLayout.EAST);

            // the header on top of the filter component, 3px apart
            this.wrapPanel = new JPanel(new BorderLayout(0, 3));
//            Border border = BorderFactory.createCompoundBorder(
//                    BorderFactory.createLineBorder(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, 1),
//                    BorderFactory.createLineBorder(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN, 2)
//            );
            // the rounded corners have a radius of 5px, but the content keeps a
            // distance of only 4px from the edges (RoundedBorder's insets are its radius)
            Border border = new RoundedBorder(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN, 5, 0) {
                @Override
                public Insets getBorderInsets(Component c) {
                    return new Insets(4, 4, 4, 4);
                }
            };
            this.wrapPanel.setBorder(border);
            this.wrapPanel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN);
            this.wrapPanel.add(headerPanel, BorderLayout.NORTH);

            setFilterComponent(filterComponent);
        }
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(actionEvent.getSource() == closeButton) {
                selectedFilterComponents.remove(this);
                remainingFilterComponents.add(this);

            } else {
                throw new IllegalStateException();
            }
        }

        public FilterComponent getFilterComponent() {
            return filterComponent;
        }
        public void setFilterComponent(FilterComponent filterComponent) {
            if(this.filterComponent != null) {
                this.wrapPanel.remove(this.filterComponent.getComponent());
            }
            this.filterComponent = filterComponent;
            this.headerLabel.setText(filterComponent.toString());
            this.wrapPanel.add(filterComponent.getComponent(), BorderLayout.CENTER);
        }
        public MatcherEditor getMatcherEditor() {
            return filterComponent.getMatcherEditor();
        }
        public JPanel getWrapPanel() {
            return wrapPanel;
        }
        @Override
        public String toString() {
            return filterComponent.toString();
        }
    }

    /**
     * A combobox to add a new filter.
     */
    private class AddFilterControl implements ItemListener, ListEventListener<CloseableFilterComponent> {
        private JComboBox<CloseableFilterComponent> filterSelect;
        private JLabel selectLabel;
        private JComponent panel;

        public AddFilterControl() {
            final EventList<CloseableFilterComponent> remainingFilterComponentsProxyList =
                GlazedListsSwing.swingThreadProxyList(remainingFilterComponents);
            filterSelect = new JComboBox<>(new DefaultEventComboBoxModel<>(remainingFilterComponentsProxyList));
            filterSelect.setFont(filterSelect.getFont().deriveFont(10.0f));
            filterSelect.setOpaque(false);

            selectLabel = new JLabel("Add a filter: ");
            selectLabel.setForeground(Color.BLACK);
            selectLabel.setFont(selectLabel.getFont().deriveFont(10.0f));
            selectLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            panel = new JPanel();
            panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            panel.setBackground(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
            panel.setLayout(new GridBagLayout());
            panel.add(selectLabel,              new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(0, 0, 0, 0), 0, 0));
            panel.add(filterSelect,             new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            panel.add(Box.createVerticalGlue(), new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets(0, 0, 0, 0), 0, 0));

            // handle selections of a filter
            filterSelect.addItemListener(this);
            // disable the combobox when there aren't any options
            remainingFilterComponents.addListEventListener(this);
        }

        /**
         * Get the filter control panel.
         */
        public JComponent getComponent() {
            return panel;
        }

        /** {@inheritDoc} */
        @Override
        public void listChanged(ListEvent<CloseableFilterComponent> listChanges) {
            boolean enabled = !remainingFilterComponents.isEmpty();
            filterSelect.setEnabled(enabled);
            selectLabel.setForeground(enabled ? Color.BLACK : IssuesBrowser.GLAZED_LISTS_MEDIUM_BROWN);
        }

        /** {@inheritDoc} */
         @Override
        public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange() != ItemEvent.SELECTED) return;
            CloseableFilterComponent selectedFilterComponent = (CloseableFilterComponent)filterSelect.getSelectedItem();
            if(selectedFilterComponent == null) return;

            selectedFilterComponents.add(selectedFilterComponent);
            filterSelect.setSelectedItem(null);
            remainingFilterComponents.remove(selectedFilterComponent);
        }
    }
}