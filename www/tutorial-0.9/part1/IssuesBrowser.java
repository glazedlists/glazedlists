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
        EventListModel issuesListModel = new EventListModel(issuesEventList);
        JList issuesJList = new JList(issuesListModel);
        JScrollPane issuesListScrollPane = new JScrollPane(issuesJList);
        panel.add(issuesListScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        
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
