package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;

import org.junit.Assume;
import org.junit.Test;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class TableComparatorChooserTest extends SwingTestCase {

    @Test
    public void testChangingUIDelegate() throws Exception {
        // test crashes on Windows with Java 7u60 and Java 7u80 with java.awt.headless=true
        Assume.assumeFalse(Boolean.getBoolean("java.awt.headless"));

        String[] properties = {"text"};
        String[] labels = {"Text"};
        TableFormat<JLabel> tableFormat = GlazedLists.tableFormat(properties, labels);
        EventList<JLabel> source = new BasicEventList<>();

        JTable table = new JTable(new DefaultEventTableModel<>(source, tableFormat));

        SortedList<JLabel> sorted = new SortedList<>(source);
        TableComparatorChooser.install(table, sorted, AbstractTableComparatorChooser.SINGLE_COLUMN);

        // install the System LnF
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.updateComponentTreeUI(table);

        // install the Cross-platform LnF
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtilities.updateComponentTreeUI(table);

        // this line throws an NPE without a fix from GL
        final TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
        defaultRenderer.getTableCellRendererComponent(table, "Text", false, false, 0, 0);
    }
}