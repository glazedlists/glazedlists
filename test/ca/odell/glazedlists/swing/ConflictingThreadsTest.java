/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

// the core Glazed Lists package
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.*;
// the objects to play with
import javax.swing.JLabel;

/**
 * This test attempts to modify an {@link EventList} from two threads simultaneously.
 * The first thread is the Swing thread, the second thread is a background thread.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ConflictingThreadsTest extends SwingTestCase {
    
    /** the list of labels */
    private EventList labelsList;
    
    /** the table of labels */
    private EventTableModel labelsTable;

    /**
     * Prepare for the test.
     */
    public void guiSetUp() {
        labelsList = new BasicEventList();
        labelsList.add(new JLabel("7-up"));
        labelsList.add(new JLabel("Pepsi"));
        labelsList.add(new JLabel("Dr. Pepper"));
        
        String[] properties = new String[] { "text", "toolTipText" };
        String[] headers = new String[] { "Text", "Tool Tip" };
        boolean[] editable = new boolean[] { true, true };
        TableFormat labelsTableFormat = GlazedLists.tableFormat(JLabel.class, properties, headers, editable);
        
        labelsTable = new EventTableModel(labelsList, labelsTableFormat);
    }

    /**
     * Clean up after the test.
     */
    public void guiTearDown() {
    }

    /**
     * Tests the user interface. This is a mandatory method in SwingTestCase classes.
     */
    public void testGui() {
        super.testGui();
    }

    /**
     * Verifies that conflicting threads are resolved intelligently.
     */
    public void guiTestConflictingThreads() {
        
        doBackgroundTask(new Runnable() {
            public void run() {
                labelsList.getReadWriteLock().writeLock().lock();
                labelsList.clear();
                labelsList.getReadWriteLock().writeLock().unlock();
            }
        });
        
        assertEquals(0, labelsTable.getRowCount());
        
        labelsList.getReadWriteLock().readLock().lock();
        assertEquals(0, labelsList.size());
        labelsList.getReadWriteLock().readLock().unlock();
    }
    
    public static void main(String[] args) {
        new ConflictingThreadsTest().testGui();
    }
}
