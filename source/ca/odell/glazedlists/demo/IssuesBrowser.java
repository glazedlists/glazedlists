/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.applet.*;
import java.awt.Color;
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
    private UniqueList issuesEventList = new UniqueList(new BasicEventList());
    
    /** the currently selected issues */
    private EventSelectionModel issuesSelectionModel;
    
    /** an event list to host the descriptions */
    private EventList descriptions = new BasicEventList();
    
    /** monitor loading the issues */
    private JProgressBar issuesLoading = null;
    private JTextField issuesLoadingText = null;
    
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
        
        // debug a problem where the thread is getting interrupted
        if(Thread.currentThread().isInterrupted()) {
            new Exception("thread has been interrupted").printStackTrace();
        }
        
        // start loading the issues
        new Thread(new IssueLoader()).start();
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
        frame.setSize(640, 480);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.getContentPane().add(constructView(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.show();
    }
    
    /**
     * Display a frame for browsing issues.
     */
    private JPanel constructView() {
        // create the lists
        IssuesUserFilter issuesUserFiltered = new IssuesUserFilter(issuesEventList);
        SortedList issuesSortedList = new SortedList(issuesUserFiltered);
        TextFilterList issuesTextFiltered = new TextFilterList(issuesSortedList);
        //ThresholdList priorityList = new ThresholdList(issuesTextFiltered, "priority.rating");

        // issues table
        //EventTableModel issuesTableModel = new EventTableModel(priorityList, new IssueTableFormat());
        EventTableModel issuesTableModel = new EventTableModel(issuesTextFiltered, new IssueTableFormat());
        JTable issuesJTable = new JTable(issuesTableModel);
        issuesSelectionModel = new EventSelectionModel(issuesTextFiltered);
        issuesSelectionModel.addListSelectionListener(new IssuesSelectionListener());
        issuesJTable.setSelectionModel(issuesSelectionModel);
        issuesJTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(2).setPreferredWidth(10);
        issuesJTable.getColumnModel().getColumn(3).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(4).setPreferredWidth(30);
        issuesJTable.getColumnModel().getColumn(5).setPreferredWidth(200);
		issuesJTable.setDefaultRenderer(Priority.class, new PriorityTableCellRenderer());
        TableComparatorChooser tableSorter = new TableComparatorChooser(issuesJTable, issuesSortedList, true);
        JScrollPane issuesTableScrollPane = new JScrollPane(issuesJTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // users table
        JScrollPane usersListScrollPane = new JScrollPane(issuesUserFiltered.getUserSelect(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // descriptions
        EventTableModel descriptionsTableModel = new EventTableModel(descriptions, new DescriptionTableFormat());
        JTable descriptionsTable = new JTable(descriptionsTableModel);
        descriptionsTable.getColumnModel().getColumn(0).setCellRenderer(new DescriptionRenderer());
        JScrollPane descriptionsTableScrollPane = new JScrollPane(descriptionsTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // priority slider
        /*BoundedRangeModel priorityRangeModel = ThresholdRangeModelFactory.createLower(priorityList);
        priorityRangeModel.setRangeProperties(0, 0, 0, 100, false);
        JSlider prioritySlider = new JSlider(priorityRangeModel);
        Hashtable prioritySliderLabels = new Hashtable();
        prioritySliderLabels.put(new Integer(0), new JLabel("Low"));
        prioritySliderLabels.put(new Integer(100), new JLabel("High"));
        prioritySlider.setLabelTable(prioritySliderLabels);        
        prioritySlider.setSnapToTicks(true);
        prioritySlider.setPaintLabels(true);
        prioritySlider.setPaintTicks(true);
        prioritySlider.setMajorTickSpacing(25);*/
        
        
        // create the filters panel
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new GridBagLayout());
        filtersPanel.setBorder(BorderFactory.createLineBorder(Color.white));
        filtersPanel.add(new JLabel("Text Filter"),          new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(10, 10, 5,   10), 0, 0));
        filtersPanel.add(issuesTextFiltered.getFilterEdit(), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,  10, 15,  10), 0, 0));
        //filtersPanel.add(new JLabel("Minimum Prioriy"),      new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(5,  10, 5,   10), 0, 0));
        //filtersPanel.add(prioritySlider,                     new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,  10, 15,  10), 0, 0));
        filtersPanel.add(new JLabel("Issue Owner"),          new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,   GridBagConstraints.NONE,       new Insets(5,  10, 5,   10), 0, 0));
        filtersPanel.add(usersListScrollPane,                new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets(0,  10, 10,  10), 0, 0));
        
        // create the progress bar panel
        issuesLoading = new JProgressBar();
        issuesLoadingText = new JTextField();
        issuesLoadingText.setEnabled(false);
        issuesLoadingText.setForeground(UIManager.getColor("Panel.foreground"));
        issuesLoadingText.setBackground(UIManager.getColor("Panel.background"));
        issuesLoadingText.setDisabledTextColor(UIManager.getColor("Panel.foreground"));
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new GridBagLayout());
        progressPanel.add(issuesLoadingText,               new GridBagConstraints(0, 0, 1, 1, 0.90, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0,  0,  0,  5), 0, 0));
        progressPanel.add(issuesLoading,                   new GridBagConstraints(1, 0, 2, 1, 0.10, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 0,  0,  0,  0), 0, 0));
        
        // a panel with a table
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(filtersPanel,                new GridBagConstraints(0, 0, 1, 2, 0.15, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets( 5,  5,  5,  5), 0, 0));
        panel.add(issuesTableScrollPane,       new GridBagConstraints(1, 0, 1, 1, 0.85, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets( 5,  5,  5,  5), 0, 0));
        panel.add(descriptionsTableScrollPane, new GridBagConstraints(1, 1, 1, 1, 0.85, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH,       new Insets( 5,  5,  5,  5), 0, 0));
        panel.add(progressPanel,               new GridBagConstraints(0, 2, 2, 1, 1.00, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets( 2,  2,  2,  2), 0, 0));

        return panel;
    }
    
    /**
     * Listens for changes in the selection on the issues table.
     */
    class IssuesSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            descriptions.clear();
            if(issuesSelectionModel.getEventList().size() > 0) {
                Issue selectedIssue = (Issue)issuesSelectionModel.getEventList().get(0);
                descriptions.addAll(selectedIssue.getDescriptions());
            }
        }
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
        
    /**
     * Load the issues from the Internet.
     */
    class IssueLoader implements Runnable {
        public void run() {
            // start the progress bar
            SwingUtilities.invokeLater(new IndeterminateToggler(true, "Downloading issues..."));
                
            // load the issues
            List urlIssues = null;
            try {
                String issuesUrl = "https://glazedlists.dev.java.net/issues/xml.cgi";
                urlIssues = IssuezillaXMLParser.loadIssues(issuesUrl, 1, 150);
            } catch(IOException e) {
                e.printStackTrace();
            }
    
            // apply the issues to the event list
            SortedSet urlIssuesAsSet = new TreeSet();
            urlIssuesAsSet.addAll(urlIssues);
            
            issuesEventList.getReadWriteLock().writeLock().lock();
            try {
                issuesEventList.replaceAll(urlIssuesAsSet);
            } finally {
                issuesEventList.getReadWriteLock().writeLock().unlock();
            }
            
            // stop the progress bar
            SwingUtilities.invokeLater(new IndeterminateToggler(false, ""));
        }
        private class IndeterminateToggler implements Runnable {
            private boolean indeterminite;
            private String message;
            public IndeterminateToggler(boolean indeterminite, String message) {
                this.indeterminite = indeterminite;
                this.message = message;
            }
            public void run() {
                issuesLoading.setIndeterminate(indeterminite);
                issuesLoadingText.setText(message);
            }
        }
    }    
}
