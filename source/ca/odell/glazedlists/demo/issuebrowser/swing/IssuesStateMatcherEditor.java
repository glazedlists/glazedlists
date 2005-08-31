package ca.odell.glazedlists.demo.issuebrowser.swing;

import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.Matcher;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

/**
 * A MatcherEditor that produces Matchers that filter the issues based on the selected states.
 */
class IssuesStateMatcherEditor extends AbstractMatcherEditor<Issue> implements ActionListener {
    /** A panel housing a checkbox for each state. */
    private JPanel checkBoxPanel = new JPanel(new GridLayout(2, 2));

    /** A checkbox for each possible state. */
    private final JCheckBox[] stateCheckBoxes;

    public IssuesStateMatcherEditor() {
        final JCheckBox newStateCheckBox = buildCheckBox("New");
        final JCheckBox resolvedStateCheckBox = buildCheckBox("Resolved");
        final JCheckBox startedStateCheckBox = buildCheckBox("Started");
        final JCheckBox closeStateCheckBox = buildCheckBox("Closed");

        this.stateCheckBoxes = new JCheckBox[] {newStateCheckBox, resolvedStateCheckBox, startedStateCheckBox, closeStateCheckBox};

        this.checkBoxPanel.setOpaque(false);

        // add each checkbox to the panel and start listening to selections
        for (int i = 0; i < this.stateCheckBoxes.length; i++) {
            this.stateCheckBoxes[i].addActionListener(this);
            this.checkBoxPanel.add(this.stateCheckBoxes[i]);
        }
    }

    /**
     * Returns the component responsible for editing the state filter
     */
    public Component getComponent() {
        return this.checkBoxPanel;
    }

    /**
     * A convenience method to build a state checkbox with the given name.
     */
    private static JCheckBox buildCheckBox(String name) {
        final JCheckBox checkBox = new JCheckBox(name, true);
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        return checkBox;
    }

    /**
     * Returns a StateMatcher which matches Issues if their state is one
     * of the selected states.
     */
    private StateMatcher buildMatcher() {
        final Set<String> allowedStates = new HashSet<String>();
        for (int i = 0; i < this.stateCheckBoxes.length; i++) {
            if (this.stateCheckBoxes[i].isSelected())
                allowedStates.add(this.stateCheckBoxes[i].getText().toUpperCase().intern());
        }

        return new StateMatcher(allowedStates);
    }

    public void actionPerformed(ActionEvent e) {
        // determine if the checkbox that generated this ActionEvent is freshly checked or freshly unchecked
        // - we'll use that information to determine whether this is a constrainment or relaxation of the matcher
        final boolean isCheckBoxSelected = ((JCheckBox) e.getSource()).isSelected();

        // build a StateMatcher
        final StateMatcher stateMatcher = this.buildMatcher();

        // fire a MatcherEvent of the appropriate type
        if (stateMatcher.getStateCount() == 0)
            this.fireMatchNone();
        else if (stateMatcher.getStateCount() == this.stateCheckBoxes.length)
            this.fireMatchAll();
        else if (isCheckBoxSelected)
            this.fireRelaxed(stateMatcher);
        else
            this.fireConstrained(stateMatcher);
    }

    /**
     * A StateMatcher returns <tt>true</tt> if the state of the Issue is
     * one of the viewable states selected by the user.
     */
    private static class StateMatcher implements Matcher<Issue> {
        private final Set<String> allowedStates;

        public StateMatcher(Set<String> allowedStates) {
            this.allowedStates = allowedStates;
        }

        public int getStateCount() {
            return this.allowedStates.size();
        }

        public boolean matches(Issue issue) {
            return this.allowedStates.contains(issue.getStatus());
        }
    }
}
