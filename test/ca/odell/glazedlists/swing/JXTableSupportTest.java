package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.TableComparatorChooser.SortArrowHeaderRenderer;

import java.awt.Color;
import java.awt.EventQueue;
import java.util.Comparator;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
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
    private JXTable table;
    private Comparator<Color> redComparator = GlazedLists.beanPropertyComparator(Color.class, "red");

    @Override
    protected void setUp() {
        colors = new BasicEventList<Color>();
        colors.add(Color.GRAY);
        colors.add(Color.WHITE);
        colors.add(Color.BLACK);
        sortedColors = new SortedList<Color>(colors, redComparator);
        table = new JXTable();
    }

    public void guiTestInstall() {
        final JXTableSupport support = JXTableSupport.install(table, colors, colorTableFormat, sortedColors, TableComparatorChooser.SINGLE_COLUMN);
        assertTrue(support.isInstalled());
        assertNotNull(support.getTableModel());
        assertEquals(support.getTableModel(), table.getModel());
        assertTrue(table.getModel() instanceof AdvancedTableModel);
        assertNotNull(support.getTableSelectionModel());
        assertEquals(support.getTableSelectionModel(), table.getSelectionModel());
        assertTrue(table.getSelectionModel() instanceof AdvancedListSelectionModel);
        assertNotNull(support.getTableComparatorChooser());
        assertEquals(table, support.getTable());
        assertEquals(colorTableFormat, support.getTableFormat());
        assertFalse(table.isSortable());
        assertTrue(table.getTableHeader().getDefaultRenderer() instanceof SortArrowHeaderRenderer);
    }

    public void guiTestUninstall() {
        final TableModel oldTableModel = table.getModel();
        final ListSelectionModel oldSelectionModel = table.getSelectionModel();
        final boolean isSortable = table.isSortable();
        final TableCellRenderer oldDefaultRenderer = table.getTableHeader().getDefaultRenderer();

        final JXTableSupport support = JXTableSupport.install(table, colors, colorTableFormat, sortedColors, TableComparatorChooser.SINGLE_COLUMN);
        assertTrue(support.isInstalled());
        support.uninstall();
        assertFalse(support.isInstalled());
        assertNull(support.getTableModel());
        assertNull(support.getTableSelectionModel());
        assertNull(support.getTableComparatorChooser());
        assertNull(support.getTable());
        assertNull(support.getTableFormat());
        assertEquals(oldTableModel, table.getModel());
        assertEquals(oldSelectionModel, table.getSelectionModel());
        assertEquals(isSortable, table.isSortable());
        assertEquals(oldDefaultRenderer, table.getTableHeader().getDefaultRenderer());
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
