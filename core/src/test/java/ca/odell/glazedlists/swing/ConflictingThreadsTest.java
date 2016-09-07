/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists package
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.JLabel;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * This test attempts to modify an {@link EventList} from two threads simultaneously.
 * The first thread is the Swing thread, the second thread is a background thread.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ConflictingThreadsTest extends SwingTestCase {

    /**
     * Verifies that conflicting threads are resolved intelligently.
     */
    @Ignore("Fix me")
    @Test
    public void testConflictingThreads_FixMe() {
        EventList<JLabel> labelsList = new BasicEventList<JLabel>();
        labelsList.add(new JLabel("7-up"));
        labelsList.add(new JLabel("Pepsi"));
        labelsList.add(new JLabel("Dr. Pepper"));
        EventList<JLabel> labelsProxyList = GlazedListsSwing.swingThreadProxyList(labelsList);

        String[] properties = new String[] { "text", "toolTipText" };
        String[] headers = new String[] { "Text", "Tool Tip" };
        boolean[] editable = new boolean[] { true, true };
        TableFormat<JLabel> labelsTableFormat = GlazedLists.tableFormat(JLabel.class, properties, headers, editable);

        DefaultEventTableModel<JLabel> labelsTable = new DefaultEventTableModel<JLabel>(labelsProxyList, labelsTableFormat);

        doBackgroundTask(new ClearListRunnable(labelsList), true);

        labelsList.getReadWriteLock().writeLock().lock();
        assertEquals(0, labelsList.size());
        labelsList.add(new JLabel("Coca-Cola"));
        labelsList.getReadWriteLock().writeLock().unlock();

        // This fails. We know it fails. Do not fix this!
        //
        // The problem is that changes pending on an EventList are invisible to
        // the EDT if those changes happened before the current dispatch but
        // the proxy to fire those events happened after the current dispatch.
        //
        // This is a really tricky problem that we need to do some serious
        // thinking on. One natural but impossible solution is to force updates
        // on the model read. The problem with this is that the JTable itself
        // may be doing the read, and firing updates from within the JTable's
        // call stack (and re-entering JTable) is likely quite problematic!
        assertEquals(1, labelsTable.getRowCount());
    }

    private static class ClearListRunnable implements Runnable {
        private final EventList labelsList;
        public ClearListRunnable(EventList labelsList) {
            this.labelsList = labelsList;
        }
        @Override
        public void run() {
            labelsList.getReadWriteLock().writeLock().lock();
            labelsList.clear();
            labelsList.getReadWriteLock().writeLock().unlock();
        }
    }
}
