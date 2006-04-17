/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.glazedlists.swing.EventListJXTableSorting;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.BasicEventList;
import com.publicobject.issuesbrowser.IssuezillaXMLParser;
import com.publicobject.issuesbrowser.Project;
import com.publicobject.issuesbrowser.IssueTableFormat;
import com.publicobject.issuesbrowser.Issue;

/**
 * Demonstrate sorting using JXTable's header indicator icons and Glazed Lists'
 * {@link ca.odell.glazedlists.SortedList}.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class JXTableTest implements Runnable {

    private EventList<Issue> issues;

    public JXTableTest(EventList<Issue> issues) {
        this.issues = issues;
    }

    public void run() {
        JTextField filterEdit = new JTextField(12);
        TextComponentMatcherEditor<Issue> textMatcherEditor = new TextComponentMatcherEditor<Issue>(filterEdit, null);
        FilterList<Issue> textFilteredIssues = new FilterList<Issue>(issues, textMatcherEditor);
        SortedList<Issue> sortedIssues = new SortedList<Issue>(textFilteredIssues, null);

        EventTableModel<Issue> tableModel = new EventTableModel<Issue>(sortedIssues, new IssueTableFormat());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterEdit);

        JXTable table = new JXTable(tableModel);
        table.setColumnControlVisible(true);
        table.getColumnExt(3).setComparator(new IssueStateComparator());
        EventListJXTableSorting.install(table, sortedIssues);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(filterPanel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Load issues asynchronously.
     */
    private static class IssueLoader implements Runnable {
        private EventList issues;
        private Project project;
        public IssueLoader(EventList issues, String filename) {
            this.issues = issues;

            this.project = new Project("Glazed Lists", "Glazed Lists");
            project.setFileName(filename);
        }
        public void run() {
            try {
                IssuezillaXMLParser.loadIssues(issues, new FileInputStream(project.getFileName()), project);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static class IssueStateComparator implements Comparator {
        private static final List STATES = Arrays.asList(new String[] { "UNCONFIRMED", "NEW", "STARTED", "REOPENED", "RESOLVED", "VERIFIED", "CLOSED" });
        public int compare(Object a, Object b) {
            int stateIndexA = STATES.indexOf(a);
            int stateIndexB = STATES.indexOf(b);
            if(stateIndexA == -1 || stateIndexB == -1) throw new IllegalStateException();
            return stateIndexA - stateIndexB;
        }
    }

    public static void main(String[] args) {
        EventList<Issue> issues = new BasicEventList<Issue>();
        new Thread(new IssueLoader(issues, args[0])).start();
        SwingUtilities.invokeLater(new JXTableTest(issues));
    }

}