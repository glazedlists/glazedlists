/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.UndoRedoSupport;

import javax.swing.undo.*;
import javax.swing.*;

/**
 * This class adapts the generic {@link UndoRedoSupport} provided by Glazed
 * Lists for specific use with Swing's native {@link UndoManager}. Each
 * {@link UndoRedoSupport.Edit} produced by Glazed List's
 * {@link UndoRedoSupport} is adapted to Swing's {@link UndoableEdit} interface
 * and then {@link UndoManager#addEdit added} into the given UndoManager.
 *
 * <p>Fine grain control of the {@link UndoableEdit} that is ultimately added
 * to the {@link UndoableEdit} can be achieved by using
 * {@link #install(UndoManager, EventList, FunctionList.Function) this} install
 * method and specifying a custom Function.
 *
 * @author James Lemieux
 */
public final class UndoSupport<E> {

    /** the manager of all undoable edits for the entire Swing application */
    private UndoManager undoManager;

    /** glazed List's undo/redo support which is being adapted to Swing's native undo/redo framework */
    private UndoRedoSupport undoRedoSupport;

    /** a listener that transforms GL-style edits into Swing-style edits using the {@link #editAdapter} and adds them to the {@link #undoManager} */
    private UndoRedoSupport.Listener undoSupportHandler = new UndoSupportHandler();

    /** the function which transforms GL-style edits into Swing-style edits */
    private FunctionList.Function<UndoRedoSupport.Edit, UndoableEdit> editAdapter;

    /**
     * The private constructor creates an UndoSupport that provides undo/redo
     * capabilties for changes to the given <code>source</code>. Whenever
     * changes occur to the <code>source</code>, a GL-style
     * {@link UndoRedoSupport.Edit} is produced, which is then transformed into
     * a Swing-style {@link UndoableEdit} and ultimately added to the given
     * <code>undoManager</code>.
     *
     * @param undoManager the manager of all undoable edits for the entire Swing application
     * @param source the EventList to watch for undoable edits
     * @param editAdapter the function that converts GL-style edits into Swing-style edits
     */
    private UndoSupport(UndoManager undoManager, EventList<E> source, FunctionList.Function<UndoRedoSupport.Edit, UndoableEdit> editAdapter) {
        this.undoManager = undoManager;
        this.undoRedoSupport = UndoRedoSupport.install(source);
        this.editAdapter = editAdapter;

        // begin watching the GL undoRedoSupport for undoable edits
        this.undoRedoSupport.addUndoSupportListener(undoSupportHandler);
    }

    /**
     * Installs support for undoing/redoing edits on the given
     * <code>source</code>. Specifically, {@link UndoableEdit}s are added to the
     * <code>undoManager</code> each time the <code>source</code> changes.
     * {@link UndoableEdit#undo Undoing} and {@link UndoableEdit#redo redoing}
     * these edits will unapply/reapply the corresponding changes to the
     * <code>source</code>.
     *
     * <p>This method uses a default strategy for mapping the GL-style edits
     * to {@link UndoableEdit}s.
     *
     * @param undoManager the manager of all undoable edits for the entire Swing application
     * @param source the EventList to watch for undoable edits
     * @return an instance of the support class providing undo/redo edit features
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> UndoSupport install(UndoManager undoManager, EventList<E> source) {
        return install(undoManager, source, new DefaultEditAdapter());
    }

    /**
     * Installs support for undoing/redoing edits on the given
     * <code>source</code>. Specifically, {@link UndoableEdit}s are added to the
     * <code>undoManager</code> each time the <code>source</code> changes.
     * {@link UndoableEdit#undo Undoing} and {@link UndoableEdit#redo redoing}
     * these edits will unapply/reapply the corresponding changes to the
     * <code>source</code>.
     *
     * <p>This method uses the given <code>editAdapter</code> for mapping the
     * GL-style edits to {@link UndoableEdit}s.
     *
     * @param undoManager the manager of all undoable edits for the entire Swing application
     * @param source the EventList to watch for undoable edits
     * @param editAdapter the function that converts GL-style edits into Swing-style edits
     * @return an instance of the support class providing undo/redo edit features
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public static <E> UndoSupport install(UndoManager undoManager, EventList<E> source, FunctionList.Function<UndoRedoSupport.Edit, UndoableEdit> editAdapter) {
        checkAccessThread();

        return new UndoSupport<E>(undoManager, source, editAdapter);
    }

    /**
     * This method removes undo/redo support from the {@link EventList} it was
     * installed on. This method is useful when the {@link EventList} must
     * outlive the undo/redo support itself. Calling this method will make
     * this support object available for garbage collection independently of
     * the {@link EventList} of items.
     *
     * @throws IllegalStateException if this method is called from any Thread
     *      other than the Swing Event Dispatch Thread
     */
    public void uninstall() {
        checkAccessThread();

        undoRedoSupport.removeUndoSupportListener(undoSupportHandler);
        undoRedoSupport.uninstall();
        
        undoSupportHandler = null;
        undoRedoSupport = null;
        undoManager = null;
        editAdapter = null;
    }

    /**
     * A convenience method to ensure {@link UndoSupport} is being accessed
     * from the Event Dispatch Thread.
     */
    private static void checkAccessThread() {
        if (!SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("UndoRedoSupport must be accessed from the Swing Event Dispatch Thread, but was called on Thread \"" + Thread.currentThread().getName() + "\"");
    }

    /**
     * Listens for GL edits and responds by transforming them into Swing edits
     * and posting them to the given UndoManager.
     */
    private class UndoSupportHandler implements UndoRedoSupport.Listener {
        public void undoableEditHappened(UndoRedoSupport.Edit edit) {
            undoManager.addEdit(editAdapter.evaluate(edit));
        }
    }

    /**
     * The default strategy for transforming GL edits into Swing edits.
     */
    private static class DefaultEditAdapter implements FunctionList.Function<UndoRedoSupport.Edit, UndoableEdit> {
        public UndoableEdit evaluate(UndoRedoSupport.Edit edit) {
            return new EditAdapter(edit);
        }

        private static class EditAdapter extends AbstractUndoableEdit {
            private final UndoRedoSupport.Edit edit;

            public EditAdapter(UndoRedoSupport.Edit edit) {
                this.edit = edit;
            }

            public void undo() { edit.undo(); }
            public boolean canUndo() { return edit.canUndo(); }
            public void redo() { edit.redo(); }
            public boolean canRedo() { return edit.canRedo(); }
        }
    }
}