/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.applet.*;
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
public class IssuesBrowser extends Applet {
    
    /** an event list to host the issues */
    IssuesList issuesEventList = new IssuesList();
    
    /** the file in the .jar with the issues */
    private static final String ISSUES_FILE = "resources/demo/issues.xml";
    
    /**
     * Load the issues browser as an applet.
     */
    public IssuesBrowser() {
        this(true);
    }
    
    /**
     * Loads the issues browser as standalone or as an applet.
     */
    public IssuesBrowser(boolean applet) {
        if(applet) {
            constructApplet();
            
        } else {
            constructStandalone();
            
        }

        ClassLoader classLoader = getClass().getClassLoader();
        URL issuesFile = classLoader.getResource(ISSUES_FILE);
        startIssuesList(issuesFile);
    }
     
    /**
     * Load the issues from the specified URL.
     */
    public void startIssuesList(URL issuesUrl) {
        issuesEventList.start(issuesUrl);
    }
    
    /**
     * Constructs the browser as an Applet.
     */
    private void constructApplet() {
        setLayout(new GridBagLayout());
        add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
    
    /**
     * Constructs the browser as a standalone frame.
     */
    private void constructStandalone() {
        // create a frame with that panel
        JFrame frame = new JFrame("Issues");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(540, 380);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.show();
    }
    
    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // create a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        IssuesUserFilter issuesUserFiltered = new IssuesUserFilter(issuesEventList);
        SortedList issuesSortedList = new SortedList(issuesUserFiltered);
        TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);
        EventTableModel issuesTableModel = new EventTableModel(issuesTextFiltered, new IssueTableFormat());
        JTable issuesJTable = new JTable(issuesTableModel);
        issuesJTable.setSelectionModel(new EventSelectionModel(issuesTextFiltered).getListSelectionModel());
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        TableComparatorChooser tableSorter = new TableComparatorChooser(issuesJTable, issuesSortedList, true);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable);
        JScrollPane usersListScrollPane = new JScrollPane(issuesUserFiltered.getUserSelect());
        panel.add(usersListScrollPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 0.3, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));
        panel.add(issuesTableScrollPane, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        panel.add(new JLabel("Filter: "), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
        panel.add(issuesTextFiltered.getFilterEdit(), new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
        return panel;
    }
    
    /**
     * When started via a main method, this creates a standalone issues browser.
     */
    public static void main(String[] args) {
        if(args.length != 0) {
            System.out.println("Usage: IssueBrowser");
            return;
        }
        
        // load the issues and display the browser
        IssuesBrowser browser = new IssuesBrowser(false);
    }
}
