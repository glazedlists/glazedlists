/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UndoRedoSupportTest {

    private EventList<String> source;
    private TransactionList<String> nestedSource;
    private UndoRedoSupport undoRedoSupport;
    private UndoSupportWatcher undoSupportWatcher;

    @Before
    public void setUp() {
        source = new BasicEventList<String>();
        nestedSource = new TransactionList<String>(source, true);
        undoRedoSupport = UndoRedoSupport.install(nestedSource);
        undoSupportWatcher = new UndoSupportWatcher();

        undoRedoSupport.addUndoSupportListener(undoSupportWatcher);
    }

    @After
    public void tearDown() {
        source = null;
        nestedSource = null;
        undoRedoSupport = null;
        undoSupportWatcher = null;
    }

    @Test
    public void testAdd() {
        source.add("First");
        assertEquals(1, undoSupportWatcher.getEditStack().size());

        UndoRedoSupport.Edit lastEdit = undoSupportWatcher.getEditStack().remove(0);
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(1, source.size());

        lastEdit.undo();
        assertFalse(lastEdit.canUndo());
        assertTrue(lastEdit.canRedo());
        assertEquals(0, source.size());
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.redo();
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));
        assertEquals(0, undoSupportWatcher.getEditStack().size());
    }

    @Test
    public void testRemove() {
        source.add("First");
        undoSupportWatcher.getEditStack().remove(0);
        source.remove(0);
        assertEquals(1, undoSupportWatcher.getEditStack().size());

        UndoRedoSupport.Edit lastEdit = undoSupportWatcher.getEditStack().remove(0);
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(0, source.size());

        lastEdit.undo();
        assertFalse(lastEdit.canUndo());
        assertTrue(lastEdit.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.redo();
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(0, source.size());
        assertEquals(0, undoSupportWatcher.getEditStack().size());
    }

    @Test
    public void testUpdate() {
        source.add("First");
        undoSupportWatcher.getEditStack().remove(0);
        source.set(0, "Second");
        assertEquals(1, undoSupportWatcher.getEditStack().size());

        UndoRedoSupport.Edit lastEdit = undoSupportWatcher.getEditStack().remove(0);
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(1, source.size());

        lastEdit.undo();
        assertFalse(lastEdit.canUndo());
        assertTrue(lastEdit.canRedo());
        assertEquals(1, source.size());
        assertSame("First", source.get(0));
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.redo();
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertEquals(1, source.size());
        assertSame("Second", source.get(0));
        assertEquals(0, undoSupportWatcher.getEditStack().size());
    }

    @Test
    public void testMutate() {
        source.add("First");
        undoSupportWatcher.getEditStack().remove(0);
        source.set(0, "First");
        assertEquals(0, undoSupportWatcher.getEditStack().size());
    }

    @Test
    public void testComplexEdit() {
        source.add("First");
        source.add("Second");
        source.add("Third");
        source.add("Fourth");
        source.add("Fifth");

        final List<String> beforeSnapshot = new ArrayList<String>(source);
        undoSupportWatcher.getEditStack().clear();
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        nestedSource.beginEvent();
        nestedSource.add(5, "Sixth");
        nestedSource.remove(2);
        nestedSource.set(3, "Updated");
        nestedSource.commitEvent();

        final List<String> afterSnapshot = new ArrayList<String>(source);

        assertEquals(1, undoSupportWatcher.getEditStack().size());
        UndoRedoSupport.Edit lastEdit = undoSupportWatcher.getEditStack().remove(0);
        assertTrue(lastEdit.canUndo());
        assertFalse(lastEdit.canRedo());
        assertSame("First", source.get(0));
        assertSame("Second", source.get(1));
        assertSame("Fourth", source.get(2));
        assertSame("Updated", source.get(3));
        assertSame("Sixth", source.get(4));
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.undo();
        assertEquals(beforeSnapshot, source);
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.redo();
        assertEquals(afterSnapshot, source);
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.undo();
        assertEquals(beforeSnapshot, source);
        assertEquals(0, undoSupportWatcher.getEditStack().size());

        lastEdit.redo();
        assertEquals(afterSnapshot, source);
        assertEquals(0, undoSupportWatcher.getEditStack().size());
    }

    @Test
    public void testLongChainOfRandomEdits() {
        int value = 0;
        Random random = new Random();

        // create a series of 500 changes
        for (int i = 0; i < 500; i++) {
            int changeType = nestedSource.isEmpty() ? ListEvent.INSERT : random.nextInt(3);
            int index = nestedSource.isEmpty() ? 0 : random.nextInt(nestedSource.size());

            if (changeType == ListEvent.INSERT) {
                nestedSource.add(index, String.valueOf(++value));
            } else if (changeType == ListEvent.DELETE) {
                nestedSource.remove(index);
            } else if (changeType == ListEvent.UPDATE) {
                nestedSource.set(index, String.valueOf(++value));
            }
        }

        // take a snapshot of the List after 500 random edits
        final List<String> snapshot = new ArrayList<String>(nestedSource);
        assertEquals(500, undoSupportWatcher.getEditStack().size());

        // undo all edits (should result in an empty list)
        final ListIterator<UndoRedoSupport.Edit> i = undoSupportWatcher.getEditStack().listIterator();
        while (i.hasNext()) {
            i.next().undo();
        }

        assertTrue(nestedSource.isEmpty());

        // redo all edits (should result in snapshot)
        while (i.hasPrevious()) {
            i.previous().redo();
        }

        assertEquals(snapshot, nestedSource);
    }

    private static class UndoSupportWatcher implements UndoRedoSupport.Listener {
        private List<UndoRedoSupport.Edit> editStack = new ArrayList<UndoRedoSupport.Edit>();

        @Override
        public void undoableEditHappened(UndoRedoSupport.Edit edit) {
            editStack.add(0, edit);
        }

        public List<UndoRedoSupport.Edit> getEditStack() { return editStack; }
    }
}
