/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import java.util.List;
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
import java.util.Arrays;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;

/**
 * Manage a bunch of issue filters in a panel.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class FilterPanel {

    private EventList issues;

    /** the currently applied filters */
    private EventList<CloseableFilterComponent> filterComponents = new BasicEventList<CloseableFilterComponent>();

    private CompositeMatcherEditor<Issue> matcherEditor;

    private JPanel filtersPanel;
    private JScrollPane filtersScrollPane;

    private List<Class> filterClasses = Arrays.asList(new Class[] {
        StatusMatcherEditor.class,
        TextFilterComponent.class,
        SwingUsersMatcherEditor.class,
        PriorityMatcherEditor.class,
        CreationDateMatcherEditor.class,
    });

    public FilterPanel(EventList<Issue> issues) {
        this.issues = issues;

        // create a MatcherEditor which edits the filter text
        for(Iterator<Class> i = filterClasses.iterator(); i.hasNext(); ) {
            Class filterComponentClass = i.next();
            FilterComponent filterComponent = createFilterComponent(filterComponentClass);
            this.filterComponents.add(new CloseableFilterComponent(filterComponent));
            break; // todo: comment this out
        }

        EventList<MatcherEditor<Issue>> matcherEditors = new FunctionList<CloseableFilterComponent,MatcherEditor<Issue>>(filterComponents, new CloseableFilterComponentToMatcherEditor<Issue>());
        this.matcherEditor = new CompositeMatcherEditor<Issue>(matcherEditors);

        // create the filters panel
        this.filtersPanel = new JEventListPanel<CloseableFilterComponent>(filterComponents, new CloseableFilterComponentPanelFormat<Issue>());
        this.filtersPanel.setBackground(IssuesBrowser.GLAZED_LISTS_LIGHT_BROWN);

        this.filtersScrollPane = new JScrollPane(this.filtersPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        private JButton previousButton;
        private JButton nextButton;
        private JButton newButton;
        private JButton closeButton;
        private JLabel headerLabel;
        private JPanel headerPanel;

        public CloseableFilterComponent(FilterComponent filterComponent) {
            this.closeButton = new IssuesBrowser.IconButton(IssuesBrowser.x_icons);
            this.closeButton.addActionListener(this);
            this.closeButton.setOpaque(false);

            this.previousButton = new IssuesBrowser.IconButton(IssuesBrowser.left_icons);
            this.previousButton.addActionListener(this);
            this.previousButton.setOpaque(false);

            this.nextButton = new IssuesBrowser.IconButton(IssuesBrowser.right_icons);
            this.nextButton.addActionListener(this);
            this.nextButton.setOpaque(false);

            this.newButton = new IssuesBrowser.IconButton(IssuesBrowser.plus_icons);
            this.newButton.addActionListener(this);
            this.newButton.setOpaque(false);

            this.headerLabel = new JLabel();
            this.headerLabel.setHorizontalAlignment(JLabel.CENTER);
            this.headerLabel.setFont(headerLabel.getFont().deriveFont(10.0f));
            this.headerLabel.setForeground(Color.WHITE);

            this.headerPanel = new IssuesBrowser.GradientPanel(IssuesBrowser.GLAZED_LISTS_MEDIUM_LIGHT_BROWN, IssuesBrowser.GLAZED_LISTS_MEDIUM_BROWN, true);
            this.headerPanel.setLayout(new GridBagLayout());
            this.headerPanel.add(newButton,      new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            this.headerPanel.add(previousButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            this.headerPanel.add(headerLabel,    new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            this.headerPanel.add(nextButton,     new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            this.headerPanel.add(closeButton,    new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

            setFilterComponent(filterComponent);
        }
        public void actionPerformed(ActionEvent actionEvent) {
            if(actionEvent.getSource() == closeButton) {
                getFilterComponent().dispose();
                filterComponents.remove(this);

            } else if(actionEvent.getSource() == newButton) {
                FilterComponent filterComponentCopy = createFilterComponent(getFilterComponent().getClass());
                int index = filterComponents.indexOf(CloseableFilterComponent.this);
                filterComponents.add(index + 1, new CloseableFilterComponent(filterComponentCopy));

            } else if(actionEvent.getSource() == previousButton) {
                adjustFilterComponent(-1);

            } else if(actionEvent.getSource() == nextButton) {
                adjustFilterComponent(1);

            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * Handle a click to the 'previous' and 'next' buttons.
         */
        private void adjustFilterComponent(int offset) {
            // clean up the old filter component
            FilterComponent currentFilterComponent = this.getFilterComponent();
            Class currentClass = currentFilterComponent.getClass();
            currentFilterComponent.dispose();

            // create the new filter component
            int indexOfCurrentClass = filterClasses.indexOf(currentClass);
            Class newClass = filterClasses.get((indexOfCurrentClass + offset + filterClasses.size()) % filterClasses.size());
            FilterComponent newFilterComponent = createFilterComponent(newClass);

            // set the component into the filter panel, and repaint the filter panel
            setFilterComponent(newFilterComponent);
            int index = filterComponents.indexOf(this);
            filterComponents.set(index, this);
        }
        public FilterComponent getFilterComponent() {
            return filterComponent;
        }
        public void setFilterComponent(FilterComponent filterComponent) {
            this.filterComponent = filterComponent;
            this.headerLabel.setText(filterComponent.getName());
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
    }

    /**
     * Provide layout for {@link CloseableFilterComponent}s. 
     */
    private class CloseableFilterComponentPanelFormat<E> extends JEventListPanel.AbstractFormat<CloseableFilterComponent> {
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
    private static class CloseableFilterComponentToMatcherEditor<E> implements FunctionList.Function<CloseableFilterComponent,MatcherEditor<E>> {
        public MatcherEditor<E> evaluate(CloseableFilterComponent sourceValue) {
            return sourceValue.getMatcherEditor();
        }
    }
}