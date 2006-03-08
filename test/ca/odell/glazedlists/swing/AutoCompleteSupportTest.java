package ca.odell.glazedlists.swing;

import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;

public class AutoCompleteSupportTest extends TestCase {

    public void testDispose() {
        final JComboBox combo = new JComboBox();
        final EventList items = new BasicEventList();

        final ComboBoxUI originalUI = combo.getUI();
        final ComboBoxModel originalModel = combo.getModel();
        final boolean originalEditable = combo.isEditable();
        final ComboBoxEditor originalEditor = combo.getEditor();
        final int originalEditorPropertyChangeListenerCount = originalEditor.getEditorComponent().getPropertyChangeListeners().length;
        final AbstractDocument originalEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();
        final int originalComboBoxPropertyChangeListenerCount = combo.getPropertyChangeListeners().length;

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);

        JTextField currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        AbstractDocument currentEditorDocument = (AbstractDocument) currentEditor.getDocument();

        assertTrue(originalUI != combo.getUI());
        assertTrue(originalModel != combo.getModel());
        assertTrue(originalEditable != combo.isEditable());
        assertTrue(originalEditor != combo.getEditor());
        assertTrue(currentEditorDocument != originalEditorDocument);
        assertTrue(currentEditorDocument.getDocumentFilter() != null);
        assertTrue(originalComboBoxPropertyChangeListenerCount + 2 == combo.getPropertyChangeListeners().length);
        assertTrue(originalEditorPropertyChangeListenerCount + 1 == currentEditor.getPropertyChangeListeners().length);

        support.dispose();

        currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        currentEditorDocument = (AbstractDocument) currentEditor.getDocument();

        assertTrue(originalUI == combo.getUI());
        assertTrue(originalModel == combo.getModel());
        assertTrue(originalEditable == combo.isEditable());
        assertTrue(originalEditor == combo.getEditor());
        assertTrue(originalEditorDocument == currentEditorDocument);
        assertTrue(currentEditorDocument.getDocumentFilter() == null);
        assertTrue(originalComboBoxPropertyChangeListenerCount == combo.getPropertyChangeListeners().length);
        assertTrue(originalEditorPropertyChangeListenerCount == currentEditor.getPropertyChangeListeners().length);

        try {
            support.dispose();
            fail("Double disposing AutoCompleteSupport did not fail as expected");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testChangeModel() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList());

        try {
            combo.setModel(new DefaultComboBoxModel());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testChangeUI() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList());

        try {
            combo.setUI(new BasicComboBoxUI());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testChangeEditorDocumentToNonAbstractDocument() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList());

        try {
            final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
            editor.setDocument(new NoopDocument());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }


    private static class NoopDocument implements Document {
        private Element root = new NoopElement();

        public int getLength() { return 0; }
        public void addDocumentListener(DocumentListener listener) { }
        public void removeDocumentListener(DocumentListener listener) { }
        public void addUndoableEditListener(UndoableEditListener listener) { }
        public void removeUndoableEditListener(UndoableEditListener listener) { }
        public Object getProperty(Object key) { return null; }
        public void putProperty(Object key, Object value) { }
        public void remove(int offs, int len) { }
        public void insertString(int offset, String str, AttributeSet a) { }
        public String getText(int offset, int length) { return ""; }
        public void getText(int offset, int length, Segment txt) { }
        public Position getStartPosition() { return null; }
        public Position getEndPosition() { return null; }
        public Position createPosition(int offs) { return null; }
        public Element[] getRootElements() { return null; }
        public Element getDefaultRootElement() { return root; }
        public void render(Runnable r) { }

        private class NoopElement implements Element {
            public Document getDocument() { return NoopDocument.this; }
            public Element getParentElement() { return null; }
            public String getName() { return null; }
            public AttributeSet getAttributes() { return null; }
            public int getStartOffset() { return 0; }
            public int getEndOffset() { return 0; }
            public int getElementIndex(int offset) { return 0; }
            public int getElementCount() { return 0; }
            public Element getElement(int index) { return null; }
            public boolean isLeaf() { return false; }
        }
    }
}