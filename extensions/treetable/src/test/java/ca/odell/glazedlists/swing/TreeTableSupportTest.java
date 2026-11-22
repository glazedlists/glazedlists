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
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class TreeTableSupportTest extends SwingTestCase {

    /**
     * The modifiers that prevent the arrow and space keys from toggling the expansion: the
     * modifier keys and the left mouse button, which KeyEvent.getModifiers() reports for a key event.
     */
    private static final int[] BLOCKING_MODIFIERS = {InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
        InputEvent.META_DOWN_MASK, InputEvent.ALT_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK, InputEvent.BUTTON1_DOWN_MASK};

    @Test
    public void testUninstall() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<>();
        final TreeList<String> treeList = new TreeList<>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);
        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<>(proxyList, itemTableFormat);
        final JTable table = new JTable(model);
        final TableColumn hierarchyColumn = table.getColumnModel().getColumn(0);
        final TableCellRenderer originalRenderer = new DefaultTableCellRenderer();
        final TableCellEditor originalEditor = new DefaultCellEditor(new JComboBox<>());
        hierarchyColumn.setCellRenderer(originalRenderer);
        hierarchyColumn.setCellEditor(originalEditor);

        // extract some information from the JTable before installing TreeTableSupport
        final int originalKeyListenerCount = table.getKeyListeners().length;
        final Set<MouseListener> originalMouseListenerSet = new HashSet<>(Arrays.asList(table.getMouseListeners()));

        // install TreeTableSupport
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);

        // extract some information from the JTable after installing TreeTableSupport
        final TreeTableCellRenderer newRenderer = (TreeTableCellRenderer) hierarchyColumn.getCellRenderer();
        final TreeTableCellEditor newEditor = (TreeTableCellEditor) hierarchyColumn.getCellEditor();
        final Set<MouseListener> newMouseListeners = new HashSet<>(Arrays.asList(table.getMouseListeners()));
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
        assertEquals(originalMouseListenerSet, new HashSet<>(Arrays.asList(table.getMouseListeners())));
    }

    @Test
    public void testSetDelegateRendererAndEditor() {
        // build a TreeList
        final EventList<String> source = new BasicEventList<>();
        final TreeList<String> treeList = new TreeList<>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<>(proxyList, itemTableFormat);
        final JTable table = new JTable(model);

        // install TreeTableSupport
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        final TableColumn hierarchyColumn = table.getColumnModel().getColumn(0);
        final TreeTableCellRenderer renderer = (TreeTableCellRenderer) hierarchyColumn.getCellRenderer();
        final TreeTableCellEditor editor = (TreeTableCellEditor) hierarchyColumn.getCellEditor();
        final TableCellRenderer newDelegateRenderer = new DefaultTableCellRenderer();
        final TableCellEditor newDelegateEditor = new DefaultCellEditor(new JComboBox<>());

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
        final EventList<String> source = new BasicEventList<>();
        final TreeList<String> treeList = new TreeList<>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final String[] propertyNames = {""};
        final String[] columnLabels = {"Column 1"};
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(propertyNames, columnLabels);
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<>(proxyList, itemTableFormat);
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
        final EventList<String> source = new BasicEventList<>();
        final TreeList<String> treeList = new TreeList<>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

        // build a regular JTable around the TreeList
        final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(new String[] {""}, new String[] {"Column 1"});
        final DefaultEventTableModel<String> model = new DefaultEventTableModel<>(proxyList, itemTableFormat);
        final JTable table = new JTable(model);

        // install TreeTableSupport
        TreeTableSupport.install(table, treeList, 0);

        final TryToModifyTreeListFromBackgroundThreadRunnable r = new TryToModifyTreeListFromBackgroundThreadRunnable(source);
        Thread t = new Thread(r);
        t.start();
        t.join();

        assertEquals(IllegalStateException.class, r.getRuntimeException().getClass());
    }

	@Test
	public void testKeepSelectionWhenUpdatingElements()	throws InterruptedException {
		// build a TreeList
		final EventList<String> source = new BasicEventList<>();
		final TreeList<String> treeList = new TreeList<>(source, TreeListTest.UNCOMPRESSED_CHARACTER_TREE_FORMAT,	TreeList.<String> nodesStartExpanded());
		final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);

		// build a regular JTable around the TreeList
		final TableFormat<String> itemTableFormat = GlazedLists.tableFormat(new String[] { "" }, new String[] { "Column 1" });
		final DefaultEventTableModel<String> model = new DefaultEventTableModel<>(proxyList, itemTableFormat);
		final JTable table = new JTable(model);

		// install TreeTableSupport
		TreeTableSupport.install(table, treeList, 0);

		source.add(0, "A");
		source.add(1, "B");
		source.add(2, "C");
		source.add(3, "D");

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setRowSelectionInterval(0, 0);

		assertEquals(0, table.getSelectedRow());
		source.set(0, "A");
		assertEquals(0, table.getSelectedRow());
	}

    /**
     * The left and right arrow keys toggle the expansion only if no modifier
     * key and not the left mouse button is pressed.
     */
    @Test
    public void testArrowKeyExpansionWithModifiers() {
        final TreeList<String> treeList = createParentAndChildTreeList();
        final JTable table = createFocusedTable(treeList);
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        support.setArrowKeyExpansionEnabled(true);
        table.setRowSelectionInterval(0, 0);
        assertTrue(treeList.isExpanded(0));

        for (int modifier : BLOCKING_MODIFIERS) {
            pressKey(table, KeyEvent.VK_LEFT, modifier);
            assertTrue("collapsed with modifier mask " + modifier, treeList.isExpanded(0));
        }

        // other keys don't toggle the expansion (VK_UP, because the space key toggles it independently)
        pressKey(table, KeyEvent.VK_UP, 0);
        assertTrue(treeList.isExpanded(0));

        pressKey(table, KeyEvent.VK_LEFT, InputEvent.BUTTON3_DOWN_MASK);
        assertFalse(treeList.isExpanded(0));
        pressKey(table, KeyEvent.VK_RIGHT, InputEvent.BUTTON2_DOWN_MASK);
        assertTrue(treeList.isExpanded(0));
        pressKey(table, KeyEvent.VK_LEFT, 0);
        assertFalse(treeList.isExpanded(0));

        support.uninstall();
    }

    /**
     * The space key toggles the expansion only if no modifier key and not the
     * left mouse button is pressed.
     */
    @Test
    public void testSpaceKeyExpansionWithModifiers() {
        final TreeList<String> treeList = createParentAndChildTreeList();
        final JTable table = createFocusedTable(treeList);
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        table.setRowSelectionInterval(0, 0);
        assertTrue(treeList.isExpanded(0));

        for (int modifier : BLOCKING_MODIFIERS) {
            pressKey(table, KeyEvent.VK_SPACE, modifier);
            assertTrue("collapsed with modifier mask " + modifier, treeList.isExpanded(0));
        }

        pressKey(table, KeyEvent.VK_SPACE, InputEvent.BUTTON3_DOWN_MASK);
        assertFalse(treeList.isExpanded(0));
        pressKey(table, KeyEvent.VK_SPACE, 0);
        assertTrue(treeList.isExpanded(0));

        support.uninstall();
    }

    /**
     * A mouse press over the node component is passed to the delegate editor as
     * a translated copy with the same type, modifiers, button and click count.
     */
    @Test
    public void testCellEditorPassesMouseModifiersToDelegate() {
        final TreeList<String> treeList = createParentAndChildTreeList();
        final JTable table = createFocusedTable(treeList);
        final TreeTableSupport support = TreeTableSupport.install(table, treeList, 0);
        final MouseEvent[] received = new MouseEvent[1];
        support.setDelegateEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean isCellEditable(EventObject event) {
                received[0] = (MouseEvent) event;
                return true;
            }
        });

        // the node component gets all the space right of the expander button
        final Rectangle cell = table.getCellRect(0, 0, true);
        final int x = cell.x + cell.width - 2;
        final int y = cell.y + cell.height / 2;

        final int[][] modifiersAndButtons = {
            {InputEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1},
            {InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1},
            // left button pressed while the right button is held down
            {InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, MouseEvent.BUTTON1},
            {InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK, MouseEvent.BUTTON3},
        };
        for (int[] modifiersAndButton : modifiersAndButtons) {
            final MouseEvent pressed = new MouseEvent(table, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(),
                    modifiersAndButton[0], x, y, 1, false, modifiersAndButton[1]);
            received[0] = null;
            assertTrue(support.getEditor().isCellEditable(pressed));

            final MouseEvent translated = received[0];
            assertNotNull(translated);
            assertNotSame(pressed, translated);
            assertEquals(pressed.getID(), translated.getID());
            assertEquals(pressed.getModifiersEx(), translated.getModifiersEx());
            assertEquals(oldModifiers(pressed), oldModifiers(translated));
            assertEquals(pressed.getButton(), translated.getButton());
            assertEquals(pressed.getClickCount(), translated.getClickCount());
        }

        support.uninstall();
    }

    /**
     * Returns a TreeList with the parent "A", initially expanded, and its child "AB".
     */
    private static TreeList<String> createParentAndChildTreeList() {
        final EventList<String> source = new BasicEventList<>();
        source.add("A");
        source.add("AB");
        final TreeList<String> treeList = new TreeList<>(source, TreeListTest.COMPRESSED_CHARACTER_TREE_FORMAT, TreeList.<String>nodesStartExpanded());
        assertEquals(2, treeList.size());
        assertTrue(treeList.getAllowsChildren(0));
        return treeList;
    }

    /**
     * Returns a JTable around the TreeList that reports to own the focus,
     * which the space key handling of TreeTableSupport requires.
     */
    private static JTable createFocusedTable(TreeList<String> treeList) {
        final EventList<String> proxyList = GlazedListsSwing.swingThreadProxyList(treeList);
        // shows the element itself, so the cells can be rendered
        final TableFormat<String> elementTableFormat = new TableFormat<String>() {
            @Override
            public int getColumnCount() {
                return 1;
            }
            @Override
            public String getColumnName(int column) {
                return "Column 1";
            }
            @Override
            public Object getColumnValue(String baseObject, int column) {
                return baseObject;
            }
        };
        return new JTable(new DefaultEventTableModel<>(proxyList, elementTableFormat)) {
            @Override
            public boolean isFocusOwner() {
                return true;
            }
        };
    }

    /**
     * Sends a key press and release with the given extended modifiers to the
     * key listeners that TreeTableSupport installed on the table.
     */
    private static void pressKey(JTable table, int keyCode, int modifiersEx) {
        final long when = System.currentTimeMillis();
        final KeyEvent pressed = new KeyEvent(table, KeyEvent.KEY_PRESSED, when, modifiersEx, keyCode, KeyEvent.CHAR_UNDEFINED);
        final KeyEvent released = new KeyEvent(table, KeyEvent.KEY_RELEASED, when, modifiersEx, keyCode, KeyEvent.CHAR_UNDEFINED);
        int listeners = 0;
        for (KeyListener listener : table.getKeyListeners()) {
            if (listener.getClass().getEnclosingClass() == TreeTableSupport.class) {
                listener.keyPressed(pressed);
                listener.keyReleased(released);
                listeners++;
            }
        }
        assertEquals("arrow key and space key listener", 2, listeners);
    }

    // delegate editors may still read the old modifier masks, so the test compares them too
    @SuppressWarnings("deprecation")
    private static int oldModifiers(InputEvent event) {
        return event.getModifiers();
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