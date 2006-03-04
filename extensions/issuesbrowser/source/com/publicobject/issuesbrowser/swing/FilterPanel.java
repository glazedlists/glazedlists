/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.JEventListPanel;
import com.publicobject.issuesbrowser.Issue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Manage a bunch of issue filters in a panel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class FilterPanel {
    /** the currently applied filters */
    private EventList<CloseableFilterComponent<Issue>> filterComponents = new BasicEventList<CloseableFilterComponent<Issue>>();

    private CompositeMatcherEditor<Issue> matcherEditor;

    private JPanel filtersPanel;
    private JScrollPane filtersScrollPane;

    public FilterPanel(EventList<Issue> issues) {
        // create a MatcherEditor which edits the filter text
        this.filterComponents.add(new CloseableFilterComponent<Issue>(new StatusMatcherEditor(issues)));
        this.filterComponents.add(new CloseableFilterComponent<Issue>(new TextFilterComponent()));
        this.filterComponents.add(new CloseableFilterComponent<Issue>(new SwingUsersMatcherEditor(issues)));
        this.filterComponents.add(new CloseableFilterComponent<Issue>(new PriorityMatcherEditor()));
        this.filterComponents.add(new CloseableFilterComponent<Issue>(new CreationDateMatcherEditor()));

        EventList<MatcherEditor<Issue>> matcherEditors = new FunctionList<CloseableFilterComponent<Issue>,MatcherEditor<Issue>>(filterComponents, new CloseableFilterComponentToMatcherEditor<Issue>());
        this.matcherEditor = new CompositeMatcherEditor<Issue>(matcherEditors);

        // create the filters panel
        this.filtersPanel = new JEventListPanel<CloseableFilterComponent<Issue>>(filterComponents, new CloseableFilterComponentPanelFormat<Issue>());
        this.filtersPanel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN);

        this.filtersScrollPane = new JScrollPane(this.filtersPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public CompositeMatcherEditor<Issue> getMatcherEditor() {
        return matcherEditor;
    }

    public JComponent getComponent() {
        return filtersScrollPane;
    }

    /**
     * A cute little panel with a kill button to remove this filter.
     */
    public class CloseableFilterComponent<E> implements ActionListener {
        private FilterComponent<E> filterComponent;
        private JButton closeButton;
        private JLabel headerLabel;
        private JPanel headerPanel;

        public CloseableFilterComponent(FilterComponent<E> filterComponent) {
            this.filterComponent = filterComponent;

            this.closeButton = new IssuesBrowser.IconButton(IssuesBrowser.x_icons);
            this.closeButton.addActionListener(this);
            this.closeButton.setOpaque(false);
//            this.closeButton.setPreferredSize(new Dimension(25, 25));
//            this.closeButton.setFont(closeButton.getFont().deriveFont(8.0f));

            this.headerLabel = new JLabel();
            this.headerLabel.setHorizontalAlignment(JLabel.CENTER);
            this.headerLabel.setFont(headerLabel.getFont().deriveFont(10.0f));
            this.headerLabel.setText(filterComponent.getName());
            this.headerLabel.setForeground(Color.WHITE);

            this.headerPanel = new IssuesBrowser.GradientPanel(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_MEDIUM_BROWN, true);
            this.headerPanel.setLayout(new BorderLayout());
            this.headerPanel.add(headerLabel, BorderLayout.CENTER);
            this.headerPanel.add(closeButton, BorderLayout.EAST);

//            this.panel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN);
        }
        public void actionPerformed(ActionEvent actionEvent) {
            filterComponents.remove(this);
        }
        public MatcherEditor<E> getMatcherEditor() {
            return filterComponent.getMatcherEditor();
        }
        public JComponent getHeader() {
            return headerPanel;
        }
        public JComponent getComponent() {
            return filterComponent.getComponent();
        }
    }

    /**
     * Provide layout for {@link CloseableFilterComponent}s. 
     */
    private class CloseableFilterComponentPanelFormat<E> extends JEventListPanel.AbstractFormat<CloseableFilterComponent<E>> {
        protected CloseableFilterComponentPanelFormat() {
            setElementCells("min, 2px, pref", "2px, fill:pref:grow, 2px");
            setGaps("4px", null);
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
    private static class CloseableFilterComponentToMatcherEditor<E> implements FunctionList.Function<CloseableFilterComponent<E>,MatcherEditor<E>> {
        public MatcherEditor<E> evaluate(CloseableFilterComponent<E> sourceValue) {
            return sourceValue.getMatcherEditor();
        }
    }
}