/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.jfreechart.EventListPieDataset;
import com.publicobject.issuesbrowser.Issue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.PieDataset;

import javax.swing.*;
import java.util.List;
import java.util.Comparator;

/**
 * This component is placed below the issues table and is shown when no issues
 * are selected in the issues table. It is meant to demonstrate the binding of
 * Glazed Lists to the JFreeChart project.
 *
 * @author James Lemieux
 */
class IssueSummaryChartsComponent {

    // the panel which displays Issues By Status
    private final ChartPanel pieChartPanel_IssuesByStatus;

    public IssueSummaryChartsComponent(EventList<Issue> issuesList) {
        // build a PieDataset representing Issues by Status
        final Comparator<Issue> issuesByStatusGrouper = GlazedLists.beanPropertyComparator(Issue.class, "status");
        final StatusFunction statusFunction = new StatusFunction();
        final PieDataset IssuesByStatusDataset = new EventListPieDataset(new SwingThreadProxyEventList<Issue>(issuesList), statusFunction, issuesByStatusGrouper);

        // build a Pie Chart and a panel to display it
        final JFreeChart pieChart_IssuesByStatus = ChartFactory.createPieChart("Issues By Status", IssuesByStatusDataset, true, true, false);
        this.pieChartPanel_IssuesByStatus = new ChartPanel(pieChart_IssuesByStatus, true);
    }

    /**
     * Returns the component that displays the charts.
     */
    public JComponent getComponent() {
        return this.pieChartPanel_IssuesByStatus;
    }

    /**
     * A function to extract the status from the first element in a list of
     * {@link Issue} objects that share the same status.
     */
    private static class StatusFunction implements FunctionList.Function<List,Comparable> {
        public Comparable evaluate(List sourceValue) {
            return ((Issue) sourceValue.get(0)).getStatus();
        }
    }
}