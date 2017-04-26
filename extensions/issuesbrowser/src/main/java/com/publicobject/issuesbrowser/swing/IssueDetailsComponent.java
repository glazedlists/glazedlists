/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import com.publicobject.issuesbrowser.Issue;

import javax.swing.*;
import java.awt.*;

/**
 * This component simply delegates between two other subcomponents. When
 * no issues are selected, summary charts are shown in the details section.
 * When a selection does exist, the details of the selected Issue are shown
 * in the details section.
 *
 * @author James Lemieux
 */
class IssueDetailsComponent extends JComponent {

    /** Shown when an Issue is selected. */
    private final IssueDescriptionsPanel issueDetailComponent;

    /** Shown when no Issue is selected. */
    private final IssueSummaryChartsComponent issueSummaryChartsComponent;

    /**
     * Display details for the given <code>issuesList</code>.
     */
    public IssueDetailsComponent(EventList<Issue> issuesList) {
        this.issueDetailComponent = new IssueDescriptionsPanel();
        this.issueSummaryChartsComponent = new IssueSummaryChartsComponent(issuesList);

        this.setLayout(new BorderLayout());
        // initially there is no selection so show the chart component
        this.showDetailComponent(this.issueSummaryChartsComponent.getComponent());
    }

    /**
     * If <code>issue</code> is <tt>null</tt> then summary charts are displayed
     * summarizing the current list of Issues. Otherwise, details of the given
     * <code>issue</code> are displayed.
     *
     * @param issue the newly selected <code>Issue</code> or <code>null</code>
     *      if the issue selection was cleared
     */
    public void setIssue(Issue issue) {
        if (issue != null) {
            this.showDetailComponent(this.issueDetailComponent.getComponent());
            this.issueDetailComponent.setIssue(issue);
        } else {
            this.showDetailComponent(this.issueSummaryChartsComponent.getComponent());
        }
    }

    /**
     * A helper method to display a given <code>detailComponent</code> within
     * this component.
     */
    private void showDetailComponent(JComponent detailComponent) {
        if (this.getComponentCount() == 0 || this.getComponent(0) != detailComponent) {
            this.removeAll();
            this.add(detailComponent, BorderLayout.CENTER);
            this.validate();
            this.repaint();
        }
    }
}