/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.event.ListEvent;

import javax.swing.undo.UndoManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UndoSupportTest extends SwingTestCase {

    private static final int NUMBER_OF_UNDOS = 500;

    private EventList<String> source;
    private TransactionList<String> txSource;
    private UndoManager undoManager;
    private UndoSupport undoSupport;

    @Override
    public void guiSetUp() {
        source = new BasicEventList<String>();
        txSource = new TransactionList<String>(source);
        undoManager = new UndoManager();
        undoManager.setLimit(NUMBER_OF_UNDOS);
        undoSupport = UndoSupport.install(undoManager, txSource);
        System.out.println("UndoSupportTest.guiSetUp");
    }

    @Override
    public void guiTearDown() {
        undoSupport.uninstall();
        txSource.dispose();
        source = null;
        txSource = null;
        undoManager = null;
    }

    public void guiTestAdd() {
        source.add("First");

        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(1, source.size());

        undoManager.undo();
        assertFalse(undoManager.canUndo());
        assertTrue(undoManager.canRedo());
        assertEquals(0, source.size());

        undoManager.redo();
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));
    }

    public void guiTestRemove() {
        source.add("First");
        undoManager.discardAllEdits();
        source.remove(0);

        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(0, source.size());

        undoManager.undo();
        assertFalse(undoManager.canUndo());
        assertTrue(undoManager.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));

        undoManager.redo();
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(0, source.size());
    }

    public void guiTestUpdate() {
        source.add("First");
        undoManager.discardAllEdits();
        source.set(0, "Second");

        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(1, source.size());

        undoManager.undo();
        assertFalse(undoManager.canUndo());
        assertTrue(undoManager.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));

        undoManager.redo();
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals(1, source.size());
        assertSame("Second", source.get(0));
    }

    public void guiTestMutate() {
        source.add("First");
        undoManager.discardAllEdits();
        source.set(0, "First");
        assertFalse(undoManager.canUndoOrRedo());
    }

    public void guiTestComplexEdit() {
        source.add("First");
        source.add("Second");
        source.add("Third");
        source.add("Fourth");
        source.add("Fifth");

        final List<String> beforeSnapshot = new ArrayList<String>(source);
        undoManager.discardAllEdits();
        assertFalse(undoManager.canUndoOrRedo());

        txSource.beginEvent();
        txSource.add(5, "Sixth");
        txSource.remove(2);
        txSource.set(3, "Updated");
        txSource.commitEvent();

        final List<String> afterSnapshot = new ArrayList<String>(source);

        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertSame("First", source.get(0));
        assertSame("Second", source.get(1));
        assertSame("Fourth", source.get(2));
        assertSame("Updated", source.get(3));
        assertSame("Sixth", source.get(4));

        undoManager.undo();
        assertTrue(undoManager.canRedo());
        assertFalse(undoManager.canUndo());
        assertEquals(beforeSnapshot, source);

        undoManager.redo();
        assertEquals(afterSnapshot, source);

        undoManager.undo();
        assertEquals(beforeSnapshot, source);

        undoManager.redo();
        assertEquals(afterSnapshot, source);
    }

    public void guiTestLongChainOfRandomEdits() {
        int value = 0;
        Random random = new Random();

        // create a series of 500 changes
        for (int i = 0; i < NUMBER_OF_UNDOS; i++) {
            int changeType = txSource.isEmpty() ? ListEvent.INSERT : random.nextInt(3);
            int index = txSource.isEmpty() ? 0 : random.nextInt(txSource.size());

            if (changeType == ListEvent.INSERT)
                txSource.add(index, String.valueOf(++value));
            else if (changeType == ListEvent.DELETE)
                txSource.remove(index);
            else if (changeType == ListEvent.UPDATE)
                txSource.set(index, String.valueOf(++value));
        }

        // take a snapshot of the List after 500 random edits
        final List<String> snapshot = new ArrayList<String>(txSource);

        // undo all edits (should result in an empty list)
        for (int i = 0; i < NUMBER_OF_UNDOS; i++)
            undoManager.undo();

        assertTrue(txSource.isEmpty());

        // redo all edits (should result in snapshot)
        for (int i = 0; i < NUMBER_OF_UNDOS; i++)
            undoManager.redo();

        assertEquals(snapshot, txSource);
    }
}