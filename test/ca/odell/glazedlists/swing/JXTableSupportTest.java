package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

/**
 * <b>work in progress</b>
 * <code>JXTableSupportTest</code>.
 */
public class JXTableSupportTest extends SwingTestCase {
    private EventList<Color> colors;
    private SortedList<Color> sortedColors;
    private TableFormat<Color> colorTableFormat = GlazedLists.tableFormat(
            new String[] { "red", "green", "blue" }, new String[] { "Red", "Green", "Blue" });
    private TableModel tableModel;
    private JXTable table;
    private Comparator<Color> redComparator = GlazedLists.beanPropertyComparator(Color.class, "red");
    private Comparator<Color> alphaComparator = GlazedLists.beanPropertyComparator(Color.class, "alpha");

    @Override
    protected void setUp() {
        colors = new BasicEventList<Color>();
        colors.add(Color.GRAY);
        colors.add(Color.WHITE);
        colors.add(Color.BLACK);
        sortedColors = new SortedList<Color>(colors, redComparator);
        table = new JXTable();
    }

    public void testOnMainThreadInstallEDTViolation() {
        try {
            JXTableSupport.install(table, colors, colorTableFormat, sortedColors, TableComparatorChooser.SINGLE_COLUMN);
            fail("failed to receive IllegalStateException because of wrong thread usage");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testOnMainThreadUninstallEDTViolation() throws Exception {
        final InstallRunnable runnable = new InstallRunnable();
        EventQueue.invokeAndWait(runnable);
        try {
            runnable.getJXTableSupport().uninstall();
            fail("failed to receive IllegalStateException because of wrong thread usage");
        } catch (IllegalStateException ex) {
            // expected
        }
        EventQueue.invokeAndWait(new Runnable() {
            public void run() {
                runnable.getJXTableSupport().uninstall();
            }
        });
    }

    public void guiTestInstall() {
    }

    private class InstallRunnable implements Runnable {
        private JXTableSupport support;
        public void run() {
            support = JXTableSupport.install(table, colors, colorTableFormat, sortedColors, TableComparatorChooser.SINGLE_COLUMN);
        }
        public JXTableSupport getJXTableSupport() {
            return support;
        }
    }
}
