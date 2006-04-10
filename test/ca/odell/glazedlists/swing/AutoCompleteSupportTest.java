/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AutoCompleteSupportTest extends SwingTestCase {

    public void guiTestUninstall() {
        final JComboBox combo = new JComboBox();
        final EventList<Object> items = new BasicEventList<Object>();
        items.add("First");
        items.add("Second");

        final ComboBoxUI originalUI = combo.getUI();
        final ComboBoxModel originalModel = combo.getModel();
        final boolean originalEditable = combo.isEditable();
        final ComboBoxEditor originalEditor = combo.getEditor();
        final int originalEditorPropertyChangeListenerCount = originalEditor.getEditorComponent().getPropertyChangeListeners().length;
        final AbstractDocument originalEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();
        final int originalComboBoxPropertyChangeListenerCount = combo.getPropertyChangeListeners().length;
        final int originalMaxRowCount = combo.getMaximumRowCount();

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);

        JTextField currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        AbstractDocument currentEditorDocument = (AbstractDocument) currentEditor.getDocument();

        assertTrue(originalUI != combo.getUI());
        assertTrue(originalModel != combo.getModel());
        assertTrue(originalEditable != combo.isEditable());
        assertTrue(originalEditor != combo.getEditor());
        assertTrue(currentEditorDocument != originalEditorDocument);
        assertTrue(currentEditorDocument.getDocumentFilter() != null);

        // two PropertyChangeListeners are added to the JComboBox:
        // * one to watch for ComboBoxUI changes
        // * one to watch for Model changes
        assertTrue(originalComboBoxPropertyChangeListenerCount + 2 == combo.getPropertyChangeListeners().length);

        // one PropertyChangeListener is added to the ComboBoxEditor to watch for Document changes
        assertTrue(originalEditorPropertyChangeListenerCount + 1 == currentEditor.getPropertyChangeListeners().length);

        support.uninstall();

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
        assertTrue(originalMaxRowCount == combo.getMaximumRowCount());

        try {
            support.uninstall();
            fail("Double disposing AutoCompleteSupport did not fail as expected");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void guiTestInstall() {
        JComboBox combo = new JComboBox();
        combo.setEditor(new NoopComboBoxEditor());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<Object>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }

        combo = new JComboBox();
        combo.setUI(new NoopComboBoxUI());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<Object>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }

        combo = new JComboBox();
        final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setDocument(new NoopDocument());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<Object>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void guiTestChangeModel() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList<Object>());

        try {
            combo.setModel(new DefaultComboBoxModel());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void guiTestChangeUI() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList<Object>());

        try {
            combo.setUI(new BasicComboBoxUI());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void guiTestChangeEditorDocumentToNonAbstractDocument() {
        final JComboBox combo = new JComboBox();
        AutoCompleteSupport.install(combo, new BasicEventList<Object>());

        try {
            final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
            editor.setDocument(new NoopDocument());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void guiTestNullElements() {
        final JComboBox combo = new JComboBox();
        final EventList<String> items = new BasicEventList<String>();
        items.add(null);
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");
        items.add(null);

        AutoCompleteSupport.install(combo, items);

        assertEquals(6, combo.getModel().getSize());

        final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setText("New");

        assertEquals(2, combo.getModel().getSize());
    }

    public void guiTestFiringActionEvent() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();

        final JComboBox combo = new JComboBox();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<String>();
        items.add(null);
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");
        items.add(null);

        AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        assertEquals(0, listener.getCount());

        // typing into an empty Document
        doc.replace(0, 0, "New", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(0, listener.getCount());

        // typing over all the text in a Document
        doc.replace(0, doc.getLength(), "x", null);
        assertEquals(0, combo.getItemCount());
        assertEquals("x", textField.getText());
        assertEquals(0, listener.getCount());

        // appending to a Document
        doc.insertString(1, "y", null);
        assertEquals(0, combo.getItemCount());
        assertEquals("xy", textField.getText());
        assertEquals(0, listener.getCount());

        // removing from a Document
        doc.remove(1, 1);
        assertEquals(0, combo.getItemCount());
        assertEquals("x", textField.getText());
        assertEquals(0, listener.getCount());

        // removing the last char from a Document
        doc.remove(0, 1);
        assertEquals(items.size(), combo.getItemCount());
        assertEquals("", textField.getText());
        assertEquals(0, listener.getCount());

        // setting in text through the JTextField
        textField.setText("Prince");
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(" Edward Island", textField.getSelectedText());
        assertEquals(0, listener.getCount());

        // simulate the enter key
        textField.postActionEvent();
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(null, textField.getSelectedText());
        assertEquals(1, listener.getCount());

        // select an index programmatically
        combo.setSelectedIndex(-1);
        assertEquals(1, combo.getItemCount());
        assertEquals("", textField.getText());
        assertEquals(2, listener.getCount());

        // select a value programmatically
        combo.setSelectedItem("Prince Edward Island");
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(3, listener.getCount());
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

    private static class NoopComboBoxEditor implements ComboBoxEditor {
        public Component getEditorComponent() { return null; }
        public void setItem(Object anObject) { }
        public Object getItem() { return null; }
        public void selectAll() { }
        public void addActionListener(ActionListener l) { }
        public void removeActionListener(ActionListener l) { }
    }

    private static class NoopComboBoxUI extends ComboBoxUI {
        public void installUI(JComponent c) {
            JComboBox combo = (JComboBox) c;
            combo.setEditor(new BasicComboBoxEditor());
        }

        public void setPopupVisible(JComboBox c, boolean v) { }
        public boolean isPopupVisible(JComboBox c) { return false; }
        public boolean isFocusTraversable(JComboBox c) { return false; }
    }

    private static class CountingActionListener implements ActionListener {
        private int count;

        public int getCount() { return count; }
        public void actionPerformed(ActionEvent e) { count++; }
    }
}