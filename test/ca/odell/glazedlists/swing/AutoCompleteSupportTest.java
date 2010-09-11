/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.IntegerTableFormat;
import ca.odell.glazedlists.matchers.GlazedListsICU4J;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

public class AutoCompleteSupportTest extends SwingTestCase {

    public void guiTestUninstall() {
        final JComboBox combo = new JComboBox();
        final EventList<Object> items = new BasicEventList<Object>();
        items.add("First");
        items.add("Second");

        final ComboBoxUI originalUI = combo.getUI();
        final ComboBoxModel originalModel = combo.getModel();
        final boolean originalEditable = combo.isEditable();
        final ListCellRenderer originalRenderer = combo.getRenderer();
        final ComboBoxEditor originalEditor = combo.getEditor();
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
        ListCellRenderer currentRenderer = combo.getRenderer();
        assertSame(originalUI, combo.getUI());
        assertSame(currentEditorDocument, originalEditorDocument);
        assertSame(originalRenderer, currentRenderer);
        assertNotSame(originalEditor, combo.getEditor());
        assertNotSame(originalModel, combo.getModel());
        assertEquals(!originalEditable, combo.isEditable());
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

        // one KeyListener is added to the ComboBoxEditor to watch for Backspace in strict mode
        assertEquals(originalEditorKeyListenerCount + 1, currentEditor.getKeyListeners().length);


        support.uninstall();

        currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        currentEditorDocument = (AbstractDocument) currentEditor.getDocument();
        currentRenderer = combo.getRenderer();
        assertSame(originalUI, combo.getUI());
        assertSame(originalModel, combo.getModel());
        assertEquals(originalEditable, combo.isEditable());
        assertSame(originalEditor, combo.getEditor());
        assertSame(originalRenderer, currentRenderer);
        assertSame(originalEditorDocument, currentEditorDocument);
        assertSame(currentEditorDocument.getDocumentFilter(), null);
        assertEquals(originalComboBoxPropertyChangeListenerCount, combo.getPropertyChangeListeners().length);
        assertEquals(originalEditorKeyListenerCount, currentEditor.getKeyListeners().length);
        assertEquals(originalMaxRowCount, combo.getMaximumRowCount());
        assertEquals(originalComboBoxPopupMenuListenerCount, ((JPopupMenu) combo.getUI().getAccessibleChild(combo, 0)).getPopupMenuListeners().length);
        assertEquals(originalComboBoxPopupMouseListenerCount, ((ComboPopup) combo.getUI().getAccessibleChild(combo, 0)).getList().getMouseListeners().length);
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

    /**
     * Tests that a custom renderer gets not overwritten, when a format is specified.
     */
    public void guiTestRenderer() {
        final JComboBox combo = new JComboBox();
        final ListCellRenderer renderer = new NoopListCellRenderer();
        combo.setRenderer(renderer);
        final EventList<Object> items = new BasicEventList<Object>();
        items.add("First");
        items.add("Second");
        final AutoCompleteSupport support = AutoCompleteSupport.install(combo, items, null, new Format() {
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                toAppendTo.append("item");
                toAppendTo.append(obj);
                return toAppendTo;
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return source.substring(4);
            }
        });
        assertSame(renderer, combo.getRenderer());
        support.uninstall();
        assertSame(renderer, combo.getRenderer());
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

    public void guiTestStrictMode() throws BadLocationException {
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

        // switching to strict mode should correct the case and fire an ActionEvent
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

    public void guiTestStrictModeWithNull() throws BadLocationException {
        final JComboBox combo = new JComboBox();
        final EventList<String> items = new BasicEventList<String>();
        items.add("New Brunswick");
        items.add(null);
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        support.setStrict(true);

        assertEquals("", textField.getText());
        assertEquals(-1, combo.getSelectedIndex()); // should be 1, but JComboBox.getSelecteditem() always returns -1 for null element
        assertNull(combo.getSelectedItem());

        combo.setSelectedItem("New Brunswick");
        assertEquals("New Brunswick", textField.getText());
        assertEquals(0, combo.getSelectedIndex());

        // typing garbage in strict mode should be ignored
        doc.replace(0, doc.getLength(), "garbage", null);
        assertEquals(5, combo.getItemCount());
        assertEquals("New Brunswick", textField.getText());
        assertEquals(0, combo.getSelectedIndex());

        // select second item (=null)
        combo.setSelectedItem(null);
        assertNull(combo.getSelectedItem());
        assertEquals("", textField.getText());
        assertEquals(-1, combo.getSelectedIndex()); // should be 1, but JComboBox.getSelecteditem() always returns -1 for null element
    }

    public void guiTestStrictModeAndFirstItem() throws BadLocationException {
        final JComboBox combo = new JComboBox();

        final EventList<String> items = new BasicEventList<String>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();

        assertNull(combo.getSelectedItem());
        support.setStrict(true);
        assertEquals("New Brunswick", textField.getText());

        support.setStrict(false);
        support.setFirstItem("Saskatchewan");
        combo.setSelectedItem(null);
        assertNull(combo.getSelectedItem());
        assertEquals("", textField.getText());

        support.setStrict(true);
        assertEquals("Saskatchewan", combo.getSelectedItem());
        assertEquals(0, combo.getSelectedIndex());
        assertEquals("Saskatchewan", textField.getText());

        support.setStrict(false);
        support.setFirstItem(null);
        combo.setSelectedItem(null);
        assertNull(combo.getSelectedItem());
        assertEquals(-1, combo.getSelectedIndex());

        // at the moment, a null firstItem yields "no selection" in the case of strict mode
        // it remains to be seen if this is correct / desirable
        support.setStrict(true);
        assertNull(combo.getSelectedItem());
        assertEquals(-1, combo.getSelectedIndex());
        assertEquals("", textField.getText());
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

    public void guiTestTextMatchingStrategy() throws BadLocationException {
        final JComboBox combo = new JComboBox();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<String>();
        items.add("Muller");
        items.add("New Brunswick");
        items.add("Aenima");
        items.add("M\u00fcller");
        items.add("\u00c6nima"); // Aenima (with the A and e smashed together)
        items.add("Ru\u00dfland"); // Russland (with a special char that means 'ss')
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("école");
        items.add("wei\u00dfe Wasserwelle");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel model = combo.getModel();
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, support.getTextMatchingStrategy());
        doc.replace(0, doc.getLength(), "New", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Newfoundland", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "Mull", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Muller", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "M\u00fcll", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("M\u00fcller", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Aenima", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Aenima", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "\u00c6nima", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("\u00c6nima", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "ecole", null);
        assertEquals(0, combo.getItemCount());
        doc.replace(0, doc.getLength(), "école", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("école", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Ru\u00df", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Ru\u00dfland", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Russland", null);
        assertEquals(0, combo.getItemCount());
        doc.replace(0, doc.getLength(), "wei\u00dfe Wasser", null);
        assertEquals(0, combo.getItemCount());

        support.setTextMatchingStrategy(TextMatcherEditor.NORMALIZED_STRATEGY);
        doc.replace(0, doc.getLength(), "New", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Newfoundland", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "Mull", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Muller", model.getElementAt(0));
        assertEquals("M\u00fcller", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "M\u00fcll", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Muller", model.getElementAt(0));
        assertEquals("M\u00fcller", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "Aenima", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Aenima", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "\u00c6nima", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("\u00c6nima", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "ecole", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("école", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "école", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("école", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Ru\u00df", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Ru\u00dfland", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Russland", null);
        assertEquals(0, combo.getItemCount());
        doc.replace(0, doc.getLength(), "wei\u00dfe Wasser", null);
        assertEquals(0, combo.getItemCount());

        support.setTextMatchingStrategy(GlazedListsICU4J.UNICODE_TEXT_SEARCH_STRATEGY);
        doc.replace(0, doc.getLength(), "New", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("New Brunswick", model.getElementAt(0));
        assertEquals("Newfoundland", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "Mull", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Muller", model.getElementAt(0));
        assertEquals("M\u00fcller", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "M\u00fcll", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Muller", model.getElementAt(0));
        assertEquals("M\u00fcller", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "Aenima", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Aenima", model.getElementAt(0));
        assertEquals("\u00c6nima", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "\u00c6nima", null);
        assertEquals(2, combo.getItemCount());
        assertEquals("Aenima", model.getElementAt(0));
        assertEquals("\u00c6nima", model.getElementAt(1));
        doc.replace(0, doc.getLength(), "ecole", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("école", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "école", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("école", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "Ru\u00df", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("Ru\u00dfland", model.getElementAt(0));
        doc.replace(0, doc.getLength(), "wei\u00dfe Wasser", null);
        assertEquals(1, combo.getItemCount());
        assertEquals("wei\u00dfe Wasserwelle", model.getElementAt(0));

        // @todo activate when ICU4J bug 5420 is fixed!
//        doc.replace(0, doc.getLength(), "Russland", null);
//        assertEquals(1, combo.getItemCount());
//        assertEquals("Ru\u00dfland", model.getElementAt(0));
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

    /**
     * This test ensures that editing text works smoothly, particularly w.r.t. the caret position.
     * Specifically, this test starts with a single dropdown value "foobar", and the initial text
     * "fobar".
     *
     * Positioning the caret at index 2 and typing "o" should:
     * a) change the text to "foobar"
     * b) select "foobar" in the model
     * c) leave the caret at position 3 (i.e. don't move it to the end of the term)
     *
     * Typing t should then:
     * a) change the text to "footbar"
     * b) clear the selection in the model
     * c) leave the caret at position 4
     */
    public void guiTestExactMatchWhenEditingText() throws BadLocationException {
        final JComboBox combo = new JComboBox();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<String>();
        items.add("foobar");

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel model = combo.getModel();
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, support.getTextMatchingStrategy());
        doc.replace(0, doc.getLength(), "fobar", null);
        textField.setCaretPosition(2);

        assertEquals("fobar", textField.getText());
        assertEquals(2, textField.getCaretPosition());
        assertEquals(null, model.getSelectedItem());

        // simulate typing "o" to make "foobar", a match
        doc.insertString(2, "o", null);
        assertEquals("foobar", textField.getText());
        assertEquals(3, textField.getCaretPosition());
        assertEquals("foobar", model.getSelectedItem());

        // simulate typing "t" to make "footbar", a non match
        doc.insertString(3, "t", null);
        assertEquals("footbar", textField.getText());
        assertEquals(4, textField.getCaretPosition());
        assertEquals(null, model.getSelectedItem());

        // simulate deleting "t" to make "foobar" again, a match
        doc.remove(3, 1);
        assertEquals("foobar", textField.getText());
        assertEquals(3, textField.getCaretPosition());
        assertEquals("foobar", model.getSelectedItem());
    }

    public void guiTestCreateTableCellEditor() {
        final EventList<Integer> ints = new BasicEventList<Integer>();
        ints.add(new Integer(0));
        ints.add(new Integer(10));
        ints.add(new Integer(199));
        ints.add(new Integer(199)); // should be removed by createTableCellEditor(...)
        ints.add(new Integer(10));  // should be removed by createTableCellEditor(...)
        ints.add(new Integer(0));   // should be removed by createTableCellEditor(...)

        AutoCompleteSupport.AutoCompleteCellEditor editor = AutoCompleteSupport.createTableCellEditor(new IntegerTableFormat(), ints, 0);
        JComboBox comboBox = (JComboBox) editor.getComponent();
        assertSame(comboBox, editor.getAutoCompleteSupport().getComboBox());
        assertSame(ints.get(0), comboBox.getItemAt(0));
        assertSame(ints.get(1), comboBox.getItemAt(1));
        assertSame(ints.get(2), comboBox.getItemAt(2));

        editor = AutoCompleteSupport.createTableCellEditor(GlazedLists.reverseComparator(), new IntegerTableFormat(), ints, 0);
        comboBox = (JComboBox) editor.getComponent();
        assertSame(comboBox, editor.getAutoCompleteSupport().getComboBox());
        assertSame(ints.get(2), comboBox.getItemAt(0));
        assertSame(ints.get(1), comboBox.getItemAt(1));
        assertSame(ints.get(0), comboBox.getItemAt(2));
    }

    /**
     * This test ensures that calling ComboBoxModel.setSelectedItem *always*
     * honours the given value and *never* replaces it due to autocompletion.
     * Our interpretation is that the user has *explicitly* set a value, and
     * that value should be honoured at all costs.
     */
    public void guiTestSettingSelectedItemInEmptyModel() throws BadLocationException {
        final JComboBox combo = new JComboBox();
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        final EventList<String> items = new BasicEventList<String>();

        AutoCompleteSupport.install(combo, items);
        final ComboBoxModel comboBoxModel = combo.getModel();

        assertEquals(0, comboBoxModel.getSize());
        assertNull(comboBoxModel.getSelectedItem());

        combo.setSelectedItem("Foo");
        assertEquals(0, comboBoxModel.getSize());
        assertEquals("Foo", comboBoxModel.getSelectedItem());

        items.add("Foobar");
        assertEquals(1, comboBoxModel.getSize());
        assertEquals("Foo", comboBoxModel.getSelectedItem());

        items.add("Blarg");
        assertEquals(2, comboBoxModel.getSize());
        assertEquals("Foo", comboBoxModel.getSelectedItem());

        // type a "b" at the end of "Foo" which should cause autocompletion to happen
        // which in turn will filter the model and select an autocompletion term
        doc.insertString(3, "b", null);
        assertEquals(1, comboBoxModel.getSize());
        assertEquals("Foobar", comboBoxModel.getSelectedItem());

        // setting "Foo" as the selected item, even when a "better" autocompletion term
        // exists in the model ("Foobar"), should keep "Foo" as the selected item
        combo.setSelectedItem("Foo");
        assertEquals(1, comboBoxModel.getSize());
        assertEquals("Foo", comboBoxModel.getSelectedItem());
    }

    public void guiTestFirstItem() throws BadLocationException {
        final JComboBox combo = new JComboBox();
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        final EventList<String> items = new BasicEventList<String>();

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        assertNull(support.getFirstItem());

        support.setFirstItem("Special First Item");
        support.removeFirstItem();
        support.setFirstItem("Special First Item");
        final ComboBoxModel comboBoxModel = combo.getModel();

        assertEquals(comboBoxModel.getSize(), 1);
        assertSame("Special First Item", comboBoxModel.getElementAt(0));

        items.add("one");
        items.add("two");
        items.add("three");

        assertEquals(comboBoxModel.getSize(), 4);
        assertSame("Special First Item", comboBoxModel.getElementAt(0));
        assertSame("one", comboBoxModel.getElementAt(1));
        assertSame("two", comboBoxModel.getElementAt(2));
        assertSame("three", comboBoxModel.getElementAt(3));

        // type a "t" which will filter the contents to "two" and "three" but should
        // leave "Special First Item" as the initial value
        doc.insertString(0, "t", null);
        assertEquals("two", doc.getText(0, doc.getLength()));
        assertEquals(comboBoxModel.getSize(), 3);
        assertSame("Special First Item", comboBoxModel.getElementAt(0));
        assertSame("two", comboBoxModel.getElementAt(1));
        assertSame("three", comboBoxModel.getElementAt(2));

        // type an "x" which will filter the contents to leave only "Special First Item" as the initial value
        doc.insertString(1, "x", null);
        assertEquals("txwo", doc.getText(0, doc.getLength()));
        assertEquals(1, comboBoxModel.getSize());
        assertSame("Special First Item", comboBoxModel.getElementAt(0));

        // removing the first item should empty the model
        assertSame("Special First Item", support.removeFirstItem());
        assertEquals(0, comboBoxModel.getSize());
        assertNull(support.getFirstItem());

        // setting the first item should reestablishthe single element in the model
        support.setFirstItem("blah");
        assertEquals(1, comboBoxModel.getSize());
        assertSame("blah", comboBoxModel.getElementAt(0));

        // setting the first item should change the single element in the model
        support.setFirstItem("Special First Item");
        assertEquals(1, comboBoxModel.getSize());
        assertSame("Special First Item", comboBoxModel.getElementAt(0));

        assertEquals("txwo", doc.getText(0, doc.getLength()));
        doc.remove(0, doc.getLength());
        assertEquals(4, comboBoxModel.getSize());
        assertSame("Special First Item", comboBoxModel.getElementAt(0));
        assertSame("one", comboBoxModel.getElementAt(1));
        assertSame("two", comboBoxModel.getElementAt(2));
        assertSame("three", comboBoxModel.getElementAt(3));
    }

    /**
     * This is a text case sent in by Andy Depue. It highlighted this problem:
     *
     * 1. a new item is added into the EventList pipeline
     * 2. AutoCompleteComboBoxModel receives the ListEvent
     * 3. AutoCompleteComboBoxModel broadcasts a ListDataEvent
     * 4. BasicComboBoxUI.Handler receives the ListDataEvent
     * 5. BasicComboBoxUI.Handler tries to set the ComboBoxEditor text to be the selectedItem
     *
     * To fix the problem we set the doNotChangeDocument flag when *any* ListDataEvent
     * is fired from AutoCompleteComboBoxModel so that AutoCompleteSupport retains
     * ultimate control over the text in the ComboBoxEditor.
     */
    public void guiTestChangingComboBoxDataDoesNotAlterComboBoxEditor() throws BadLocationException {
        final JComboBox combo = new JComboBox();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<String>();
        items.add("foobar");

        AutoCompleteSupport.install(combo, items);
        final ComboBoxModel model = combo.getModel();
        doc.replace(0, doc.getLength(), "fobar", null);

        assertEquals("fobar", textField.getText());
        assertEquals(null, model.getSelectedItem());

        // Adding a new item should not alter the filter text
        items.add("fobar");
        assertEquals("fobar", textField.getText());
        assertEquals(null, model.getSelectedItem());

        // Changing an existing item should not alter the filter text
        items.set(1, "wheeble");
        assertEquals("fobar", textField.getText());
        assertEquals(null, model.getSelectedItem());

        // Deleting an existing item should not alter the filter text
        items.remove(1);
        assertEquals("fobar", textField.getText());
        assertEquals(null, model.getSelectedItem());
    }

    /**
     * When the data under a strict-mode AutoCompleting JComboBox is changed we
     * must recheck the strict-mode invariant that the filter text matches (at
     * least partially) an element in the ComboBoxModel. This test case
     * verifies that the invariant is maintained while data changes underneath.
     */
    public void testOnMainThreadChangingComboBoxDataRetainsStrictModeInvariant() throws Exception {
        final JComboBox combo = new JComboBox();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();

        final EventList<String> basicItems = new BasicEventList<String>();
        final EventList<String> items = GlazedLists.threadSafeList(basicItems);
        items.add("foobar");

        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                final AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);
                support.setStrict(true);
            }
        });

        assertEquals("foobar", textField.getText());
        assertEquals("foobar", combo.getSelectedItem());

        // add a new item
        items.add("whiggedy");
        Thread.sleep(250); // wait for the EDT to process the change to items
        assertEquals("foobar", textField.getText());
        assertEquals("foobar", combo.getSelectedItem());

        // change an existing item
        items.set(1, "whack");
        Thread.sleep(250); // wait for the EDT to process the change to items
        assertEquals("foobar", textField.getText());
        assertEquals("foobar", combo.getSelectedItem());

        // remove an existing item
        items.remove(1);
        Thread.sleep(250); // wait for the EDT to process the change to items
        assertEquals("foobar", textField.getText());
        assertEquals("foobar", combo.getSelectedItem());

        // add an existing item and remove the current strict-mode selection
        items.set(0, "hoobedy");
        Thread.sleep(250); // wait for the EDT to process the change to items
        assertEquals("hoobedy", textField.getText());
        assertEquals("hoobedy", combo.getSelectedItem());
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

    private static class NoopListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class CountingActionListener implements ActionListener {
        private int count;

        public int getCount() { return count; }
        public void actionPerformed(ActionEvent e) { count++; }
    }
}