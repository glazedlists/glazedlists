/**
 * Glazed Lists Tutorial
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2004 O'DELL ENGINEERING LTD.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;

/**
 * An IssueBrowser is a program for finding and viewing issues.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesBrowser {
    
    // create an event list to host the issues
    EventList issuesEventList = new BasicEventList();
    
    /**
     * Load the issues from the specified URL.
     */
    public void load(String issuesUrl) {
        try {
            InputStream issuesIn = new URL(issuesUrl).openConnection().getInputStream();
            Collection sourceIssues = Issue.parseIssuezillaXML(issuesIn);
            issuesEventList.addAll(sourceIssues);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Display a frame for browsing issues.
     */
    public void display() {
        // create a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        SortedList issuesSortedList = new SortedList(issuesEventList);
        TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);
        EventTableModel issuesTableModel = new EventTableModel(issuesTextFiltered, new IssueTableFormat());
        JTable issuesJTable = new JTable(issuesTableModel);
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        TableComparatorChooser tableSorter = new TableComparatorChooser(issuesJTable, issuesSortedList, true);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable);
        panel.add(issuesTableScrollPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(new JLabel("Filter: "), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
        panel.add(issuesTextFiltered.getFilterEdit(), new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        
        // create a frame with that panel
        JFrame frame = new JFrame("Issues");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(540, 380);
        frame.getContentPane().add(panel);
        frame.show();
    }
    
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: IssueBrowser <issues url>");
            return;
        }
        
        // load the issues and display the browser
        String issuesUrl = args[0];
        IssuesBrowser browser = new IssuesBrowser();
        browser.load(issuesUrl);
        browser.display();
    }
}
