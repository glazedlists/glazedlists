/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueStatusComparator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

/**
 * A MatcherEditor that produces Matchers that filter the issues based on the
 * selected statuses.
 */
class StatusMatcherEditor extends AbstractMatcherEditor<Issue> implements ListEventListener<List<Issue>>, ActionListener, FilterComponent<Issue> {
    /** A MessageFormat to generate pretty names for our CheckBoxes which include the number of bugs with that status. */
    private static final MessageFormat checkboxFormat = new MessageFormat("{0} {1,choice,0#|0<({1})}");

    /** A panel housing a checkbox for each status. */
    private JPanel checkBoxPanel = new JPanel(new GridLayout(4, 2));

    /** A checkbox for each displayed status. */
    private final Map<String, JCheckBox> statusCheckBoxes = new LinkedHashMap<String, JCheckBox>();

    /** Issues grouped together by status. */
    private final GroupingList<Issue> issuesByStatus;
    private final EventList<List<Issue>> issuesByStatusSwingThread;

    /**
     * A cache of the list of statuses that mirrors the statuses of the issuesByStatus List.
     * It is used to determine which status is deleted when DELETE events arrive.
     */
    private List<String> statuses = new ArrayList<String>();

    public StatusMatcherEditor(EventList<Issue> issues) {
        // group the issues according to their status
        issuesByStatus = new GroupingList<Issue>(issues, new IssueStatusComparator());
        this.issuesByStatusSwingThread = GlazedListsSwing.swingThreadProxyList(issuesByStatus);
        this.issuesByStatusSwingThread.addListEventListener(this);

        this.statusCheckBoxes.put("NEW", buildCheckBox("New"));
        this.statusCheckBoxes.put("UNCONFIRMED", buildCheckBox("Unconfirmed"));
        this.statusCheckBoxes.put("STARTED", buildCheckBox("Started"));
        this.statusCheckBoxes.put("REOPENED", buildCheckBox("Reopened"));
        this.statusCheckBoxes.put("CLOSED", buildCheckBox("Closed"));
        this.statusCheckBoxes.put("VERIFIED", buildCheckBox("Verified"));
        this.statusCheckBoxes.put("RESOLVED", buildCheckBox("Resolved"));

        this.checkBoxPanel.setOpaque(false);

        // add each checkbox to the panel and start listening to selections
        for (Iterator<JCheckBox> iter = statusCheckBoxes.values().iterator(); iter.hasNext();) {
            JCheckBox checkBox = iter.next();
            checkBox.addActionListener(this);
            this.checkBoxPanel.add(checkBox);
        }
    }

    /**
     * Returns the component responsible for editing the status filter.
     */
    public JComponent getComponent() {
        return this.checkBoxPanel;
    }

    public String toString() {
        return "Status";
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    /**
     * A convenience method to build a status checkbox with the given name.
     */
    private static JCheckBox buildCheckBox(String name) {
        final JCheckBox checkBox = new JCheckBox(name, true);
        checkBox.setName(name);
        checkBox.setOpaque(false);
        checkBox.setFocusable(false);
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        return checkBox;
    }

    /**
     * Returns a StatusMatcher which matches Issues if their status is one
     * of the selected statuses.
     */
    private StatusMatcher buildMatcher() {
        final Set<String> allowedStates = new HashSet<String>();

        for (Iterator<Map.Entry<String, JCheckBox>> iter = statusCheckBoxes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, JCheckBox> entry = iter.next();
            if (entry.getValue().isSelected())
                allowedStates.add(entry.getKey());
        }

        return new StatusMatcher(allowedStates);
    }

    public void listChanged(ListEvent<List<Issue>> listChanges) {
        while (listChanges.next()) {
            final int type = listChanges.getType();
            final int index = listChanges.getIndex();

            // determine the status which changed and the new number
            // of bugs that match that status after the change
            final String status;
            final int count;
            if (type == ListEvent.INSERT) {
                List issuesOfThisStatus = (List)issuesByStatusSwingThread.get(index);
                status = ((Issue)issuesOfThisStatus.get(0)).getStatus();
                statuses.add(index, status);
                count = issuesOfThisStatus.size();
            } else if (type == ListEvent.UPDATE) {
                List issuesOfThisStatus = (List)issuesByStatusSwingThread.get(index);
                status = (String)statuses.get(index);
                count = issuesOfThisStatus.size();
            } else if (type == ListEvent.DELETE) {
                status = statuses.remove(index);
                count = 0;
            } else {
                throw new IllegalStateException();
            }

            final JCheckBox checkBox = statusCheckBoxes.get(status);

            // update the text of the checkbox to reflect the new bug count for that status
            checkBox.setText(checkboxFormat.format(new Object[] { checkBox.getName(), new Integer(count)}));
        }
    }

    public void actionPerformed(ActionEvent e) {
        // determine if the checkbox that generated this ActionEvent is freshly checked or freshly unchecked
        // - we'll use that information to determine whether this is a constrainment or relaxation of the matcher
        final boolean isCheckBoxSelected = ((JCheckBox) e.getSource()).isSelected();

        // build a StatusMatcher
        final StatusMatcher statusMatcher = this.buildMatcher();

        // fire a MatcherEvent of the appropriate type
        if (statusMatcher.getStateCount() == 0)
            this.fireMatchNone();
        else if (statusMatcher.getStateCount() == this.statusCheckBoxes.size())
            this.fireMatchAll();
        else if (isCheckBoxSelected)
            this.fireRelaxed(statusMatcher);
        else
            this.fireConstrained(statusMatcher);
    }

    /**
     * A StatusMatcher returns <tt>true</tt> if the status of the Issue is
     * one of the viewable status selected by the user.
     */
    private static class StatusMatcher implements Matcher<Issue> {
        private final Set<String> allowedStatuses;

        public StatusMatcher(Set<String> allowedStatuses) {
            this.allowedStatuses = allowedStatuses;
        }

        public int getStateCount() {
            return this.allowedStatuses.size();
        }

        public boolean matches(Issue issue) {
            return this.allowedStatuses.contains(issue.getStatus());
        }
    }
}