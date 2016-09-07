/* Glazed Lists                                                 (c) 2003-2012 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swt.DefaultEventKTableModel;
import ca.odell.glazedlists.swt.GlazedListsKTable;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.IssueLoader;
import com.publicobject.issuesbrowser.Project;
import com.publicobject.misc.Throbber;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.SWTX;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class KTableDemo {

    public static void main(String[] args) {
        // create a shell...
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setText("Issues");

        createIssuesTable(shell);

        // display the shell...
        shell.setSize(600,600);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private static void createIssuesTable(Shell shell) {

        BasicEventList issuesEventList = new BasicEventList();

        Composite comp1 = new Composite(shell, SWT.NONE);
        comp1.setLayout(new FillLayout());

        final KTable table = new KTable(comp1, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL | SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY);

        DefaultEventKTableModel tableModel = GlazedListsKTable.eventKTableModelWithThreadProxyList(table, issuesEventList, new IssuesTableFormat());
        table.setModel(tableModel);

        // loads issues
        final IssueLoader issueLoader = new IssueLoader(issuesEventList, new SimpleThrobber());
        issueLoader.start();
        issueLoader.setProject(Project.getProjects().get(0));
    }

    private static class SimpleThrobber implements Throbber {
        @Override
        public void setOn() {
            System.out.println("Throb on");
        }

        @Override
        public void setOff() {
            System.out.println("Throb off");
        }
    }

    private static final class IssuesTableFormat implements TableFormat {

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public String getColumnName(int column) {
            switch(column) {
                case 0: return "ID";
                case 1: return "Type";
                case 2: return "Priority";
                case 3: return "State";
                case 4: return "Result";
                case 5: return "Summary";
            }
            throw new IllegalStateException();
        }

        @Override
        public Object getColumnValue(Object baseObject, int column) {
            Issue issue = (Issue)baseObject;
            switch(column) {
                case 0: return issue.getId();
                case 1: return issue.getIssueType();
                case 2: return issue.getPriority();
                case 3: return issue.getStatus();
                case 4: return issue.getResolution();
                case 5: return issue.getShortDescription();
            }
            throw new IllegalStateException();
        }
    }
}