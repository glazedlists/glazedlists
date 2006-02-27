/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import com.publicobject.issuesbrowser.IssuezillaXMLParser;
import com.publicobject.issuesbrowser.Project;
import com.publicobject.issuesbrowser.IssueTableFormat;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class JXTableTest {

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
        EventList issues = new BasicEventList();
        new Thread(new IssueLoader(issues, args[0])).start();

        JTextField filterEdit = new JTextField(12);
        TextComponentMatcherEditor textMatcherEditor = new TextComponentMatcherEditor(filterEdit, null);
        FilterList textFilteredIssues = new FilterList(issues, textMatcherEditor);
        SortedList sortedIssues = new SortedList(textFilteredIssues, null);

        EventTableModel tableModel = new EventTableModel(sortedIssues, new IssueTableFormat());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterEdit);

        JXTable table = new JXTable(tableModel);
        table.setColumnControlVisible(true);
        table.getColumnExt(3).putClientProperty(TableColumnExt.SORTER_COMPARATOR, new IssueStateComparator());
        EventListRowSorter.install(table, sortedIssues);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(filterPanel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

}