/* Glazed Lists                                                 (c) 2003-2010 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueTableFormat;
import com.publicobject.issuesbrowser.IssueTextFilterator;
import com.publicobject.issuesbrowser.IssueTrackingSystem;
import com.publicobject.issuesbrowser.Project;

/**
 * Demonstrate sorting using JXTable with Glazed Lists' {@link ca.odell.glazedlists.SortedList}
 * and {@link TableComparatorChooser} by the use of {@link JXTableSupport}.
 *
 * @author Holger Brands
 */
class JXTableSupportTestApp implements Runnable {

    private EventList<Issue> issues;

    public JXTableSupportTestApp(EventList<Issue> issues) {
        this.issues = issues;
    }

    @Override
    public void run() {
        final List<Issue> issuesCopy = new ArrayList<>();
        issues.getReadWriteLock().writeLock().lock();
        try {
            JTextField filterEdit = new JTextField(12);
            TextComponentMatcherEditor<Issue> textMatcherEditor = new TextComponentMatcherEditor<>(filterEdit, new IssueTextFilterator());
            FilterList<Issue> textFilteredIssues = new FilterList<>(issues, textMatcherEditor);
            SortedList<Issue> sortedIssues = new SortedList<>(textFilteredIssues, null);
            JXTable table = new JXTable();
            final JXTableSupport<Issue> tableSupport = JXTableSupport.<Issue>install(table, sortedIssues,
                    new IssueTableFormat(), sortedIssues,
                    TableComparatorChooser.MULTIPLE_COLUMN_MOUSE_WITH_UNDO);

            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterPanel.add(new JLabel("Filter:"));
            filterPanel.add(filterEdit);
            final JButton clearButton = new JButton("Clear");
            final JButton addAllButton = new JButton("Add All");
            addAllButton.setEnabled(false);
            clearButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    issuesCopy.addAll(issues);
                    issues.clear();
                    addAllButton.setEnabled(true);
                    clearButton.setEnabled(false);
                }
            });
            addAllButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    issues.addAll(issuesCopy);
                    issuesCopy.clear();
                    clearButton.setEnabled(true);
                    addAllButton.setEnabled(false);
                }
            });
            filterPanel.add(clearButton);
            filterPanel.add(addAllButton);
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(filterPanel, BorderLayout.NORTH);
            frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        } finally {
            issues.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Load issues asynchronously.
     */
    private static class IssueLoader implements Runnable {
        private EventList<Issue> issues;
        private Project project;
        public IssueLoader(EventList<Issue> issues, String filename) {
            this.issues = issues;

            this.project = new Project("Glazed Lists", "Glazed Lists", IssueTrackingSystem.getTigrisIssuezilla());
            project.setFileName(filename);
        }
        @Override
        public void run() {
            try {

                project.getOwner().loadIssues(GlazedLists.threadSafeList(issues), new FileInputStream(project.getFileName()), project);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static class IssueStateComparator implements Comparator {
        private static final List STATES = Arrays.asList("UNCONFIRMED", "NEW", "STARTED", "REOPENED", "RESOLVED", "VERIFIED", "CLOSED");
        @Override
        public int compare(Object a, Object b) {
            int stateIndexA = STATES.indexOf(a);
            int stateIndexB = STATES.indexOf(b);
            if(stateIndexA == -1 || stateIndexB == -1) {
                throw new IllegalStateException();
            }
            return stateIndexA - stateIndexB;
        }
    }

    public static void main(String[] args) {
        EventList<Issue> issues = new BasicEventList<>();
        new Thread(new IssueLoader(issues, args[0])).start();
        SwingUtilities.invokeLater(new JXTableSupportTestApp(issues));
    }

}