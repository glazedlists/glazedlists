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
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.swing.*;

/**
 * An IssuesList is a list of issues that automatically updates itself.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesList extends TransformedList {
    
    /** the source of the issues */
    private URL issuesUrl;
    
    /** refresh every five minutes */
    //private static long ISSUES_REFRESH_INTERVAL = 1000 * 60 * 5; 
    private static long ISSUES_REFRESH_INTERVAL = 1000 * 5; 
    
    /**
     * Create a new IssuesList that shows issues from the specified URL.
     */
    public IssuesList() {
        super(new UniqueList(new BasicEventList()));
        
        // listen to changes and propagate them
        source.addListEventListener(this);
    }
    
    /**
     * Starts the issues list update daemon.
     */
    public void start(URL issuesUrl) {
        this.issuesUrl = issuesUrl;
        new Timer().scheduleAtFixedRate(new IssuesRefreshTask(), 0, ISSUES_REFRESH_INTERVAL); 
    }
    
    /**
     * A task that updates the issues list.
     */
    class IssuesRefreshTask extends TimerTask {

        /**
         * When run, this fetches the issues from the issues URL and refreshes
         * the issues list.
         */
        public void run() {
            try {
                // load the issues
                InputStream issuesIn = issuesUrl.openStream();
                Collection issues = Issue.parseIssuezillaXML(issuesIn);

                // replace the current issues list with the new issues list
                SortedSet issuesSorted = new TreeSet();
                issuesSorted.addAll(issues);
                UniqueList uniqueSource = (UniqueList)source;
                uniqueSource.replaceAll(issuesSorted);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * When the source issues list changes, propogate the exact same changes.
     */
    public void listChanged(ListEvent listChanges) {
        updates.beginEvent();
        while(listChanges.next()) {
            updates.addChange(listChanges.getType(), listChanges.getIndex());
        }
        updates.commitEvent();
    }
}
