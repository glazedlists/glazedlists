package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.junit.Test;

public class TableComparatorChooserTest extends SwingTestCase {

    @Test
    public void testChangingUIDelegate() throws Exception {
        String[] properties = {"text"};
        String[] labels = {"Text"};
        TableFormat<JLabel> tableFormat = GlazedLists.tableFormat(properties, labels);
        EventList<JLabel> source = new BasicEventList<JLabel>();

        JTable table = new JTable(new DefaultEventTableModel<JLabel>(source, tableFormat));

        SortedList<JLabel> sorted = new SortedList<JLabel>(source);
        TableComparatorChooser.install(table, sorted, AbstractTableComparatorChooser.SINGLE_COLUMN);

        // install the Windows LnF
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        SwingUtilities.updateComponentTreeUI(table);

        // install the Windows LnF
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        SwingUtilities.updateComponentTreeUI(table);

        // this line throws an NPE without a fix from GL
        final TableCellRenderer defaultRenderer = table.getTableHeader().getDefaultRenderer();
        defaultRenderer.getTableCellRendererComponent(table, "Text", false, false, 0, 0);
    }
}