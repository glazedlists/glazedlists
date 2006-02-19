/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.impl.swing.SwingThreadProxyEventList;
import ca.odell.glazedlists.jfreechart.EventListPieDataset;
import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.OpenIssuesByMonthCategoryDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.PieDataset;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

    // the panel which displays Open Issues Over Time
    private final ChartPanel lineChartPanel_OpenIssuesOverTime;

    // the panel containing all ChartPanels
    private final JPanel allChartsPanel = new JPanel(new GridLayout(1, 2));

    public IssueSummaryChartsComponent(EventList<Issue> issuesList) {
        // build a PieDataset representing Issues by Status
        final Comparator<Issue> issuesByStatusGrouper = GlazedLists.beanPropertyComparator(Issue.class, "status");
        final StatusFunction statusFunction = new StatusFunction();
        final PieDataset IssuesByStatusDataset = new EventListPieDataset(new SwingThreadProxyEventList<Issue>(issuesList), statusFunction, issuesByStatusGrouper);

        // build a Pie Chart and a panel to display it
        final JFreeChart pieChart_IssuesByStatus = ChartFactory.createPieChart("Issues By Status", IssuesByStatusDataset, true, true, false);
        this.pieChartPanel_IssuesByStatus = new ChartPanel(pieChart_IssuesByStatus, true);

        // build a Line Chart and a panel to display it
        final JFreeChart lineChart_OpenIssuesOverTime = ChartFactory.createLineChart("Open Issues Over Time", "Time", "Open Issues", new OpenIssuesByMonthCategoryDataset(issuesList), PlotOrientation.VERTICAL, true, true, false);
        lineChart_OpenIssuesOverTime.getCategoryPlot().setDomainAxis(new CustomCategoryAxis());
        this.lineChartPanel_OpenIssuesOverTime = new ChartPanel(lineChart_OpenIssuesOverTime, true);

        // add all ChartPanels to a master panel
        this.allChartsPanel.add(this.pieChartPanel_IssuesByStatus);
        this.allChartsPanel.add(this.lineChartPanel_OpenIssuesOverTime);
    }

    /**
     * Returns the component that displays the charts.
     */
    public JComponent getComponent() {
        return this.allChartsPanel;
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

    /**
     * This custom CategoryAxis formats the labels of the Date axis as desired.
     */
    private static class CustomCategoryAxis extends CategoryAxis {
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMMM yyyy");

        public CustomCategoryAxis() {
            // display the labels vertically rather than horizontally
            this.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        }

        protected TextBlock createLabel(Comparable category, float width, RectangleEdge edge, Graphics2D g2) {
            // formate the date string before creating the label
            final String date = DATE_FORMAT.format((Date) category);
            return super.createLabel(date, width, edge, g2);
        }
    }
}