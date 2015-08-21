/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.gui.TableFormat;
import org.junit.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TreeTableSupportTest extends SwingTestCase {

    @Test
    public void testUninstall() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);
        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(proxyList, itemTableFormat);
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
        assertEquals(originalKeyListenerCount + 2,
            table.getKeyListeners().length);        // arrow key KeyListener and space bar KeyListener
        assertEquals(1, newMouseListeners.size());              // this is the wrapped MouseListener from the UI Delegate

        // uninstall TreeTableSupport
        support.uninstall();

        // assert the JTable's state returned to the original state
        assertSame(originalRenderer, hierarchyColumn.getCellRenderer());
        assertSame(originalEditor, hierarchyColumn.getCellEditor());
        assertEquals(originalKeyListenerCount, table.getKeyListeners().length);
        assertEquals(originalMouseListenerSet, new HashSet<MouseListener>(Arrays.asList(table.getMouseListeners())));
    }

    @Test
    public void testSetDelegateRendererAndEditor() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(proxyList, itemTableFormat);
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

    @Test
    public void testSetRendererAndEditor() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(proxyList, itemTableFormat);
        final JTable table = new JTable(model);

        // install TreeTableSupport
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        final TableColumn hierarchyColumn = table.getColumnModel().getColumn(0);
        final TreeTableCellRenderer renderer = (TreeTableCellRenderer) hierarchyColumn.getCellRenderer();
        final TreeTableCellEditor editor = (TreeTableCellEditor) hierarchyColumn.getCellEditor();

        // validate the state of the editors
        assertSame(renderer, support.getRenderer());
        assertSame(editor, support.getEditor());
        assertSame(renderer.getDelegate(), support.getDelegateRenderer());
        assertSame(editor.getDelegate(), support.getDelegateEditor());

        // install a new TreeTableCellRenderer and TreeTableCellEditor
        final TreeTableCellRenderer newRenderer = new TreeTableCellRenderer(support.getDelegateRenderer(), treeList);
        final TreeTableCellEditor newEditor = new TreeTableCellEditor(support.getDelegateEditor(), treeList);

        assertNotSame(support.getRenderer(), newRenderer);
        assertNotSame(support.getEditor(), newEditor);
        support.setRenderer(newRenderer);
        support.setEditor(newEditor);
        assertSame(support.getRenderer(), newRenderer);
        assertSame(support.getEditor(), newEditor);
        assertSame(newRenderer.getDelegate(), support.getDelegateRenderer());
        assertSame(newEditor.getDelegate(), support.getDelegateEditor());

        // uninstall TreeTableSupport
        support.uninstall();

        assertNull(renderer.getDelegate());
        assertNull(editor.getDelegate());
        assertNull(newRenderer.getDelegate());
        assertNull(newEditor.getDelegate());
    }

    @Test
    public void testListEventsArriveOnEDT() throws InterruptedException {
        // build a TreeList
        final EventList<String> source = new BasicEventList<String>();
        final TreeList<String> treeList = new TreeList<String>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(new String[] {""}, new String[] {"Column 1"});
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<String>(proxyList, itemTableFormat);
        final JTable table = new JTable(model);

        // install TreeTableSupport
        TreeTableSupport.install(table, treeList, 0);

        final TryToModifyTreeListFromBackgroundThreadRunnable r = new TryToModifyTreeListFromBackgroundThreadRunnable(source);
        Thread t = new Thread(r);
        t.start();
        t.join();

        assertEquals(IllegalStateException.class, r.getRuntimeException().getClass());
    }

    /**
     * A Runnable that tries to execute an operation on an EventList and records
     * any resulting RuntimeException which is expected.
     */
    private static class TryToModifyTreeListFromBackgroundThreadRunnable implements Runnable {

        private final EventList<String> list;
        private RuntimeException runtimeException;

        public TryToModifyTreeListFromBackgroundThreadRunnable(EventList<String> list) {
            this.list = list;
        }

        @Override
        public void run() {
            try {
                list.add("this should fail");
            } catch (RuntimeException re) {
                runtimeException = re;
            }
        }

        public RuntimeException getRuntimeException() {
            return runtimeException;
        }
    }
}