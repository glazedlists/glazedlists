/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.JEventListPanel;
import ca.odell.glazedlists.swing.EventComboBoxModel;
import com.publicobject.issuesbrowser.Issue;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Manage a bunch of issue filters in a panel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class FilterPanel {

    private EventList issues;

    /** the currently applied filters */
    private EventList<CloseableFilterComponent> selectedFilterComponents = new BasicEventList<CloseableFilterComponent>();
    private EventList<CloseableFilterComponent> remainingFilterComponents = new BasicEventList<CloseableFilterComponent>();

    private CompositeMatcherEditor<Issue> matcherEditor;

    private JPanel filtersPanel;
    private JPanel filtersPanelPlusAddButton;
    private JScrollPane filtersScrollPane;

    public FilterPanel(EventList<Issue> issues) {
        this.issues = issues;

        // select some initial filters
        this.selectedFilterComponents.add(new CloseableFilterComponent(new TextFilterComponent()));
        this.selectedFilterComponents.add(new CloseableFilterComponent(new SwingUsersMatcherEditor(issues)));
        // and then have the rest
        this.remainingFilterComponents.add(new CloseableFilterComponent(new StatusMatcherEditor(issues)));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new PriorityMatcherEditor()));
        this.remainingFilterComponents.add(new CloseableFilterComponent(new CreationDateMatcherEditor()));
        // make 'em into matcher editors
        EventList<MatcherEditor<Issue>> matcherEditors = new FunctionList<CloseableFilterComponent,MatcherEditor<Issue>>(selectedFilterComponents, new CloseableFilterComponentToMatcherEditor<Issue>());
        this.matcherEditor = new CompositeMatcherEditor<Issue>(matcherEditors);

        // create the filters panel
        this.filtersPanel = new JEventListPanel<CloseableFilterComponent>(selectedFilterComponents, new CloseableFilterComponentPanelFormat<Issue>());
        this.filtersPanel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN);
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

    private FilterComponent createFilterComponent(Class filterComponentClass) {
        try {
            return (FilterComponent)filterComponentClass.getConstructor(EventList.class).newInstance(issues);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A cute little panel with a kill button to remove this filter.
     */
    public class CloseableFilterComponent implements ActionListener {
        private FilterComponent filterComponent;
        private JButton closeButton;
        private JLabel headerLabel;
        private JPanel headerPanel;

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
            this.headerLabel.setForeground(Color.WHITE);

            this.headerPanel = new IssuesBrowser.GradientPanel(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_MEDIUM_BROWN, true);
            this.headerPanel.setLayout(new BorderLayout());
            this.headerPanel.add(headerLabel, BorderLayout.CENTER);
            this.headerPanel.add(closeButton, BorderLayout.EAST);

            setFilterComponent(filterComponent);
        }
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
            this.filterComponent = filterComponent;
            this.headerLabel.setText(filterComponent.toString());
        }
        public MatcherEditor getMatcherEditor() {
            return filterComponent.getMatcherEditor();
        }
        public JComponent getHeader() {
            return headerPanel;
        }
        public JComponent getComponent() {
            return filterComponent.getComponent();
        }
        public String toString() {
            return filterComponent.toString();
        }
    }

    /**
     * A combobox to add a new filter.
     */
    private class AddFilterControl implements ItemListener, ListEventListener {
        private JComboBox filterSelect;
        private JLabel selectLabel;
        private JComponent panel;

        public AddFilterControl() {
            filterSelect = new JComboBox(new EventComboBoxModel(remainingFilterComponents));
            filterSelect.setFont(filterSelect.getFont().deriveFont(10.0f));
            filterSelect.setOpaque(false);

            selectLabel = new JLabel("Add a filter:");
            selectLabel.setFont(selectLabel.getFont().deriveFont(10.0f));
            selectLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

            panel = new JPanel();
            panel.setBorder(BorderFactory.createMatteBorder(10, 0, 0, 0, IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN));
            panel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN_DARKER);
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
        public void listChanged(ListEvent listChanges) {
            boolean enabled = !remainingFilterComponents.isEmpty();
            filterSelect.setEnabled(enabled);
            selectLabel.setForeground(enabled ? Color.BLACK : Color.GRAY);
        }

        /** {@inheritDoc} */
         public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange() != ItemEvent.SELECTED) return;
            CloseableFilterComponent selectedFilterComponent = (CloseableFilterComponent)filterSelect.getSelectedItem();
            if(selectedFilterComponent == null) return;

            selectedFilterComponents.add(selectedFilterComponent);
            filterSelect.setSelectedItem(null);
            remainingFilterComponents.remove(selectedFilterComponent);
        }
    }

    /**
     * Provide layout for {@link CloseableFilterComponent}s. 
     */
    private class CloseableFilterComponentPanelFormat<E> extends JEventListPanel.AbstractFormat<CloseableFilterComponent> {
        protected CloseableFilterComponentPanelFormat() {
            setElementCells("min, 3px, pref", "3px, fill:pref:grow, 3px");
            setGaps("6px", null);
            setCellConstraints(new String[] { "1, 1, 3, 1", "2, 3" });
        }

        public JComponent getComponent(CloseableFilterComponent element, int component) {
            if(component == 0) return element.getHeader();
            else if(component == 1) return element.getComponent();
            else throw new IllegalStateException();
        }
    }

    /**
     * Convert a list of {@link CloseableFilterComponentToMatcherEditor}s into
     * {@link ca.odell.glazedlists.matchers.MatcherEditor}s.
     */
    private static class CloseableFilterComponentToMatcherEditor<E> implements FunctionList.Function<CloseableFilterComponent,MatcherEditor<E>> {
        public MatcherEditor<E> evaluate(CloseableFilterComponent sourceValue) {
            return sourceValue.getMatcherEditor();
        }
    }
}