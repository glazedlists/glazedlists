/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeTableSupportTest extends SwingTestCase {

    public void guiTestUninstall() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final EventTableModel<String> model = new EventTableModel<String>(treeList, itemTableFormat);
        final JTable table = new JTable(model);
        final TableColumn hierarchyColumn = table.getColumnModel().getColumn(0);
        final TableCellRenderer originalRenderer = new DefaultTableCellRenderer();
        final TableCellEditor originalEditor = new DefaultCellEditor(new JComboBox());
        hierarchyColumn.setCellRenderer(originalRenderer);
        hierarchyColumn.setCellEditor(originalEditor);

        // extract some information from the JTable before installing TreeTableSupport
        final int originalKeyListenerCount = table.getKeyListeners().length;
        final Set<MouseListener> originalMouseListenerSet = new HashSet<MouseListener>(Arrays.asList(table.getMouseListeners()));

        // install TreeTableSupport
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);

        // extract some information from the JTable after installing TreeTableSupport
        final TreeTableCellRenderer newRenderer = (TreeTableCellRenderer) hierarchyColumn.getCellRenderer();
        final TreeTableCellEditor newEditor = (TreeTableCellEditor) hierarchyColumn.getCellEditor();
        final Set<MouseListener> newMouseListeners = new HashSet<MouseListener>(Arrays.asList(table.getMouseListeners()));
        newMouseListeners.removeAll(originalMouseListenerSet);

        // assert that the JTable's state changed in all the ways we expect
        assertSame(originalRenderer, newRenderer.getDelegate());
        assertSame(originalEditor, newEditor.getDelegate());
        assertEquals(2, table.getKeyListeners().length);        // arrow key KeyListener and space bar KeyListener
        assertEquals(1, newMouseListeners.size());              // this is the wrapped MouseListener from the UI Delegate

        // uninstall TreeTableSupport
        support.uninstall();

        // assert the JTable's state returned to the original state
        assertSame(originalRenderer, hierarchyColumn.getCellRenderer());
        assertSame(originalEditor, hierarchyColumn.getCellEditor());
        assertEquals(originalKeyListenerCount, table.getKeyListeners().length);
        assertEquals(originalMouseListenerSet, new HashSet<MouseListener>(Arrays.asList(table.getMouseListeners())));
    }

    public void guiTestSetDelegateRendererAndEditor() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, new CharacterTreeFormat());

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final EventTableModel<String> model = new EventTableModel<String>(treeList, itemTableFormat);
        final JTable table = new JTable(model);

        // install TreeTableSupport
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        final TableColumn hierarchyColumn = table.getColumnModel().getColumn(0);
        final TreeTableCellRenderer renderer = (TreeTableCellRenderer) hierarchyColumn.getCellRenderer();
        final TreeTableCellEditor editor = (TreeTableCellEditor) hierarchyColumn.getCellEditor();
        final TableCellRenderer newDelegateRenderer = new DefaultTableCellRenderer();
        final TableCellEditor newDelegateEditor = new DefaultCellEditor(new JComboBox());

        // installing new delegate renderers and editors should change them behind the TreeTableCellRenderer and TreeTableCellEditor
        assertNotSame(newDelegateRenderer, renderer.getDelegate());
        assertNotSame(newDelegateEditor, editor.getDelegate());

        support.setDelegateRenderer(newDelegateRenderer);
        support.setDelegateEditor(newDelegateEditor);

        assertSame(newDelegateRenderer, renderer.getDelegate());
        assertSame(newDelegateEditor, editor.getDelegate());

        // uninstall TreeTableSupport
        support.uninstall();

        assertNull(renderer.getDelegate());
        assertNull(editor.getDelegate());
    }

    private static class CharacterTreeFormat implements TreeList.Format<String> {
        public void getPath(List<String> path, String element) {
            for (int i = 0, n = element.length(); i < n; i++)
                path.add(String.valueOf(element.charAt(0)));
        }

        public boolean allowsChildren(String element) {
            return true;
        }
    }
}