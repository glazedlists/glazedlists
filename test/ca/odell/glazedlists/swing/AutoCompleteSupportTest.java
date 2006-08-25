/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.io.IntegerTableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
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
        final int originalEditorKeyListenerCount = originalEditor.getEditorComponent().getKeyListeners().length;
        final AbstractDocument originalEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();
        final int originalComboBoxPropertyChangeListenerCount = combo.getPropertyChangeListeners().length;
        final int originalComboBoxPopupMouseListenerCount = ((ComboPopup) combo.getUI().getAccessibleChild(combo, 0)).getList().getMouseListeners().length;
        final int originalMaxRowCount = combo.getMaximumRowCount();
        final int originalComboBoxPopupMenuListenerCount = ((JPopupMenu) combo.getUI().getAccessibleChild(combo, 0)).getPopupMenuListeners().length;
        final Action originalSelectNextAction = combo.getActionMap().get("selectNext");
        final Action originalSelectPreviousAction = combo.getActionMap().get("selectPrevious");
        final Action originalSelectNext2Action = combo.getActionMap().get("selectNext2");
        final Action originalSelectPrevious2Action = combo.getActionMap().get("selectPrevious2");
        final Action originalAquaSelectNextAction = combo.getActionMap().get("aquaSelectNext");
        final Action originalAquaSelectPreviousAction = combo.getActionMap().get("aquaSelectPrevious");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);

        JTextField currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        AbstractDocument currentEditorDocument = (AbstractDocument) currentEditor.getDocument();

        assertSame(originalUI, combo.getUI());
        assertSame(currentEditorDocument, originalEditorDocument);
        assertNotSame(originalEditor, combo.getEditor());
        assertNotSame(originalModel, combo.getModel());
        assertNotSame(originalEditable, combo.isEditable());
        assertNotSame(originalSelectNextAction, combo.getActionMap().get("selectNext"));
        assertNotSame(originalSelectPreviousAction, combo.getActionMap().get("selectPrevious"));
        assertNotSame(originalSelectNext2Action, combo.getActionMap().get("selectNext2"));
        assertNotSame(originalSelectPrevious2Action, combo.getActionMap().get("selectPrevious2"));
        assertNotSame(originalAquaSelectNextAction, combo.getActionMap().get("aquaSelectNext"));
        assertNotSame(originalAquaSelectPreviousAction, combo.getActionMap().get("aquaSelectPrevious"));
        assertNotNull(currentEditorDocument.getDocumentFilter());


        // two PropertyChangeListeners are added to the JComboBox:
        // * one to watch for ComboBoxUI changes
        // * one to watch for Model changes
        assertEquals(originalComboBoxPropertyChangeListenerCount + 2, combo.getPropertyChangeListeners().length);

        // one PopupMenuListener is added to the ComboBoxPopup to size the popup before it is shown on the screen -
        // the other PopupMenuListener is added to the ComboBoxPopup by ComboBoxPopupLocationFix, which fixes location
        // problems that occur only on the Apple L&F
        assertEquals(originalComboBoxPopupMenuListenerCount + 2, ((JPopupMenu) combo.getUI().getAccessibleChild(combo, 0)).getPopupMenuListeners().length);

        // one PopupMenuListener is added to the JComboBox to size the popup before it is shown on the screen
        assertEquals(originalComboBoxPopupMouseListenerCount + 1, ((ComboPopup) combo.getUI().getAccessibleChild(combo, 0)).getList().getMouseListeners().length);

        // one PropertyChangeListener is added to the ComboBoxEditor to watch for Document changes
        assertEquals(originalEditorPropertyChangeListenerCount + 1, currentEditor.getPropertyChangeListeners().length);

        // one KeyListener is added to the ComboBoxEditor to watch for Backspace in strict mode
        assertEquals(originalEditorKeyListenerCount + 1, currentEditor.getKeyListeners().length);


        support.uninstall();

        currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        currentEditorDocument = (AbstractDocument) currentEditor.getDocument();

        assertSame(originalUI, combo.getUI());
        assertSame(originalModel, combo.getModel());
        assertSame(originalEditable, combo.isEditable());
        assertSame(originalEditor, combo.getEditor());
        assertSame(originalEditorDocument, currentEditorDocument);
        assertSame(currentEditorDocument.getDocumentFilter(), null);
        assertSame(originalComboBoxPropertyChangeListenerCount, combo.getPropertyChangeListeners().length);
        assertSame(originalEditorPropertyChangeListenerCount, currentEditor.getPropertyChangeListeners().length);
        assertSame(originalEditorKeyListenerCount, currentEditor.getKeyListeners().length);
        assertSame(originalMaxRowCount, combo.getMaximumRowCount());
        assertSame(originalComboBoxPopupMenuListenerCount, ((JPopupMenu) combo.getUI().getAccessibleChild(combo, 0)).getPopupMenuListeners().length);
        assertSame(originalComboBoxPopupMouseListenerCount, ((ComboPopup) combo.getUI().getAccessibleChild(combo, 0)).getList().getMouseListeners().length);
        assertSame(originalSelectNextAction, combo.getActionMap().get("selectNext"));
        assertSame(originalSelectPreviousAction, combo.getActionMap().get("selectPrevious"));
        assertSame(originalSelectNext2Action, combo.getActionMap().get("selectNext2"));
        assertSame(originalSelectPrevious2Action, combo.getActionMap().get("selectPrevious2"));
        assertSame(originalAquaSelectNextAction, combo.getActionMap().get("aquaSelectNext"));
        assertSame(originalAquaSelectPreviousAction, combo.getActionMap().get("aquaSelectPrevious"));

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
        final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setDocument(new NoopDocument());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<Object>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            AutoCompleteSupport.install(combo, new BasicEventList<Object>());
            fail("failed to throw an IllegalArgumentException on double installation of AutoCompleteSupport");
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
        assertEquals(1, listener.getCount());

        // typing over all the text in a Document
        doc.replace(0, doc.getLength(), "x", null);
        assertEquals(0, combo.getItemCount());
        assertEquals("x", textField.getText());
        assertEquals(2, listener.getCount());

        // appending to a Document
        doc.insertString(1, "y", null);
        assertEquals(0, combo.getItemCount());
        assertEquals("xy", textField.getText());
        assertEquals(2, listener.getCount());

        // removing from a Document
        doc.remove(1, 1);
        assertEquals(0, combo.getItemCount());
        assertEquals("x", textField.getText());
        assertEquals(2, listener.getCount());

        // removing the last char from a Document
        doc.remove(0, 1);
        assertEquals(items.size(), combo.getItemCount());
        assertEquals("", textField.getText());
        assertEquals(2, listener.getCount());

        // setting in text through the JTextField
        textField.setText("Prince");
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(" Edward Island", textField.getSelectedText());
        assertEquals(3, listener.getCount());

        // simulate the enter key
        textField.postActionEvent();
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(null, textField.getSelectedText());
        assertEquals(4, listener.getCount());

        // select an index programmatically
        combo.setSelectedIndex(-1);
        assertEquals(1, combo.getItemCount());
        assertEquals("", textField.getText());
        assertEquals(5, listener.getCount());

        // select a value programmatically
        combo.setSelectedItem("Prince Edward Island");
        assertEquals(1, combo.getItemCount());
        assertEquals("Prince Edward Island", textField.getText());
        assertEquals(6, listener.getCount());
    }

    public void guiTestCorrectCase() throws BadLocationException {
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

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        assertEquals(0, listener.getCount());

        // without case correction and without strict mode
        support.setStrict(false);
        support.setCorrectsCase(false);
        doc.replace(0, doc.getLength(), "NEW", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("NEW Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // with case correction and without strict mode
        support.setCorrectsCase(true);
        support.setStrict(false);
        doc.replace(0, doc.getLength(), "NEW", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // without case correction but WITH strict mode should still cause case correction to occur
        support.setCorrectsCase(false);
        support.setStrict(true);
        doc.replace(0, doc.getLength(), "NEW", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // with case correction and strict mode
        support.setCorrectsCase(true);
        support.setStrict(true);
        doc.replace(0, doc.getLength(), "NEW", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());
    }

    public void guiTestSwitchingToStrictMode() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();

        final JComboBox combo = new JComboBox();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<String>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        assertEquals(0, listener.getCount());
        support.setCorrectsCase(false);

        // without case correction and without strict mode
        support.setStrict(false);
        doc.replace(0, doc.getLength(), "NEW", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("NEW Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // switching to strict mode should correct the case
        support.setStrict(true);
        assertEquals(4, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // typing garbage in strict mode should be ignored
        doc.replace(0, doc.getLength(), "garbage", null);
        assertEquals(4, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        // typing garbage in non-strict mode should be honoured
        support.setStrict(false);
        doc.replace(0, doc.getLength(), "garbage", null);
        assertEquals(0, combo.getItemCount());
        assertEquals("garbage", textField.getText());
        assertEquals(2, listener.getCount());

        // switching to strict mode should select the first element in the model
        support.setStrict(true);
        assertEquals(4, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(3, listener.getCount());
    }

    public void guiTestDeleteKey() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();
        final JComboBox combo = new JComboBox();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<String>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        assertEquals(0, listener.getCount());

        support.setStrict(false);
        doc.replace(0, doc.getLength(), "New Brunswick", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        doc.remove(3, 10);
        assertEquals(2, combo.getItemCount());
        assertEquals("New", textField.getText());
        assertEquals(1, listener.getCount());

        support.setStrict(true);
        doc.replace(0, doc.getLength(), "New Brunswick", null);
        assertEquals(4, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(1, listener.getCount());

        doc.remove(3, 10);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(" Brunswick", textField.getSelectedText());
        assertEquals(1, listener.getCount());
    }

    public void guiTestFilterMode() throws BadLocationException {
        final JComboBox combo = new JComboBox();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<String>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel model = combo.getModel();
        assertEquals(TextMatcherEditor.STARTS_WITH, support.getFilterMode());

        doc.replace(0, doc.getLength(), "u", null);
        assertEquals(0, combo.getItemCount());

        support.setFilterMode(TextMatcherEditor.CONTAINS);
        assertEquals(TextMatcherEditor.CONTAINS, support.getFilterMode());
        assertEquals("u", textField.getText());
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Newfoundland", model.getElementAt(1));

        doc.replace(0, doc.getLength(), "n", null);
        assertEquals(4, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Nova Scotia", model.getElementAt(1));
        assertEquals("Newfoundland", model.getElementAt(2));
        assertEquals("Prince Edward Island", model.getElementAt(3));

        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        assertEquals(TextMatcherEditor.STARTS_WITH, support.getFilterMode());
        assertEquals("New Brunswick", textField.getText());
        assertEquals("ew Brunswick", textField.getSelectedText());
        assertEquals(3, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Nova Scotia", model.getElementAt(1));
        assertEquals("Newfoundland", model.getElementAt(2));
    }

    public void guiTestFilterator() {
        AutoCompleteSupport support = AutoCompleteSupport.install(new JComboBox(), new BasicEventList<String>());
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox(), new BasicEventList<String>(), null);
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox(), new BasicEventList<String>(), GlazedLists.toStringTextFilterator());
        assertSame(GlazedLists.toStringTextFilterator(), support.getTextFilterator());

        support = AutoCompleteSupport.install(new JComboBox(), new BasicEventList<String>(), null, null);
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox(), new BasicEventList<String>(), GlazedLists.toStringTextFilterator(), null);
        assertSame(GlazedLists.toStringTextFilterator(), support.getTextFilterator());
    }

    public void guiTestCreateTableCellEditor() {
        final EventList<Integer> ints = new BasicEventList<Integer>();
        ints.add(new Integer(0));
        ints.add(new Integer(10));
        ints.add(new Integer(199));
        ints.add(new Integer(199)); // should be removed by createTableCellEditor(...)
        ints.add(new Integer(10));  // should be removed by createTableCellEditor(...)
        ints.add(new Integer(0));   // should be removed by createTableCellEditor(...)

        DefaultCellEditor editor = AutoCompleteSupport.createTableCellEditor(new IntegerTableFormat(), ints, 0);
        JComboBox comboBox = (JComboBox) editor.getComponent();
        assertSame(ints.get(0), comboBox.getItemAt(0));
        assertSame(ints.get(1), comboBox.getItemAt(1));
        assertSame(ints.get(2), comboBox.getItemAt(2));

        editor = AutoCompleteSupport.createTableCellEditor(GlazedLists.reverseComparator(), new IntegerTableFormat(), ints, 0);
        comboBox = (JComboBox) editor.getComponent();
        assertSame(ints.get(2), comboBox.getItemAt(0));
        assertSame(ints.get(1), comboBox.getItemAt(1));
        assertSame(ints.get(0), comboBox.getItemAt(2));
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

    private static class CountingActionListener implements ActionListener {
        private int count;

        public int getCount() { return count; }
        public void actionPerformed(ActionEvent e) { count++; }
    }
}