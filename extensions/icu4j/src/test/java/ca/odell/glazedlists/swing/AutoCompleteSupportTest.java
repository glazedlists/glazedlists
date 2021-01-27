/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ExecuteOnNonUiThread;
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

import javax.swing.Action;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.*;

public class AutoCompleteSupportTest extends SwingTestCase {

    @Test
    public void testUninstall() {
        final JComboBox<Object> combo = new JComboBox<>();
        final EventList<Object> items = new BasicEventList<>();
        items.add("First");
        items.add("Second");

        final ComboBoxUI originalUI = combo.getUI();
        final ComboBoxModel<Object> originalModel = combo.getModel();
        final boolean originalEditable = combo.isEditable();
        final ListCellRenderer<? super Object> originalRenderer = combo.getRenderer();
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

        AutoCompleteSupport<Object> support = AutoCompleteSupport.install(combo, items);

        JTextField currentEditor = ((JTextField) combo.getEditor().getEditorComponent());
        AbstractDocument currentEditorDocument = (AbstractDocument) currentEditor.getDocument();
        ListCellRenderer<? super Object> currentRenderer = combo.getRenderer();
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

    @Test
    public void testInstall() {
        JComboBox<Object> combo = new JComboBox<>();
        combo.setEditor(new NoopComboBoxEditor());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }

        combo = new JComboBox<>();
        final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
        editor.setDocument(new NoopDocument());
        try {
            AutoCompleteSupport.install(combo, new BasicEventList<>());
            fail("failed to throw an IllegalArgumentException on bad ComboBoxEditor");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            AutoCompleteSupport.install(combo, new BasicEventList<>());
            fail("failed to throw an IllegalArgumentException on double installation of AutoCompleteSupport");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Tests that a custom renderer gets not overwritten, when a format is specified.
     */
    @Test
    public void testRenderer() {
        final JComboBox<Object> combo = new JComboBox<>();
        final ListCellRenderer<Object> renderer = new NoopListCellRenderer();
        combo.setRenderer(renderer);
        final EventList<Object> items = new BasicEventList<>();
        items.add("First");
        items.add("Second");
        final AutoCompleteSupport<Object> support = AutoCompleteSupport.install(combo, items, null, new Format() {
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

    @Test
    public void testChangeModel() {
        final JComboBox<Object> combo = new JComboBox<>();
        AutoCompleteSupport.install(combo, new BasicEventList<>());

        try {
            combo.setModel(new DefaultComboBoxModel<>());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testChangeEditorDocumentToNonAbstractDocument() {
        final JComboBox<Object> combo = new JComboBox<>();
        AutoCompleteSupport.install(combo, new BasicEventList<>());

        try {
            final JTextField editor = (JTextField) combo.getEditor().getEditorComponent();
            editor.setDocument(new NoopDocument());
            fail("Expected to trigger environmental invariant violation");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testNullElements() {
        final JComboBox<String> combo = new JComboBox<>();
        final EventList<String> items = new BasicEventList<>();
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

    @Test
    public void testFiringActionEvent() throws BadLocationException {
        // Failing on 1.8 Travis builds (GLAZEDLISTS-586), so temporarily disabling on
        // 1.8/Linux
        Assume.assumeFalse( "Failing on 1.8 Travis builds: see, " +
            "https://java.net/jira/browse/GLAZEDLISTS-586",
            System.getProperty( "java.version" ).startsWith( "1.8" ) &&
                System.getProperty( "os.name" ).toLowerCase().contains( "linux" ) );

        final CountingActionListener listener = new CountingActionListener();

        final JComboBox<String> combo = new JComboBox<>();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<>();
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

    @Test
    public void testCorrectCase() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();

        final JComboBox<String> combo = new JComboBox<>();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<>();
        items.add(null);
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");
        items.add(null);

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    @Test
    public void testStrictMode() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();

        final JComboBox<String> combo = new JComboBox<>();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    @Test
    public void testStrictModeWithNull() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();
        final EventList<String> items = new BasicEventList<>();
        items.add("New Brunswick");
        items.add(null);
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    @Test
    public void testStrictModeAndFirstItem() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final EventList<String> items = new BasicEventList<>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    @Test
    public void testDeleteKey() throws BadLocationException {
        final CountingActionListener listener = new CountingActionListener();
        final JComboBox<String> combo = new JComboBox<>();
        combo.addActionListener(listener);

        final EventList<String> items = new BasicEventList<>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    @Test
    public void testFilterMode() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("New Brunswick");
        items.add("Nova Scotia");
        items.add("Newfoundland");
        items.add("Prince Edward Island");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
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

    @Test
    public void testTextMatchingStrategy() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
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

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
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

    @Test
    public void testFilterator() {
        AutoCompleteSupport<String> support = AutoCompleteSupport.install(new JComboBox<String>(), new BasicEventList<String>());
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox<String>(), new BasicEventList<String>(), null);
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox<String>(), new BasicEventList<String>(), GlazedLists.toStringTextFilterator());
        assertSame(GlazedLists.toStringTextFilterator(), support.getTextFilterator());

        support = AutoCompleteSupport.install(new JComboBox<String>(), new BasicEventList<String>(), null, null);
        assertSame(AutoCompleteSupport.DefaultTextFilterator.class, support.getTextFilterator().getClass());

        support = AutoCompleteSupport.install(new JComboBox<String>(), new BasicEventList<String>(), GlazedLists.toStringTextFilterator(), null);
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
    @Test
    public void testExactMatchWhenEditingText() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("foobar");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
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

    @SuppressWarnings("unchecked")
	@Test
    public void testCreateTableCellEditor() {
        final EventList<Integer> ints = new BasicEventList<>();
        ints.add(new Integer(0));
        ints.add(new Integer(10));
        ints.add(new Integer(199));
        ints.add(new Integer(199)); // should be removed by createTableCellEditor(...)
        ints.add(new Integer(10));  // should be removed by createTableCellEditor(...)
        ints.add(new Integer(0));   // should be removed by createTableCellEditor(...)

        AutoCompleteSupport.AutoCompleteCellEditor<Integer> editor = AutoCompleteSupport.createTableCellEditor(new IntegerTableFormat(), ints, 0);
        JComboBox<Integer> comboBox = (JComboBox<Integer>) editor.getComponent();
        assertSame(comboBox, editor.getAutoCompleteSupport().getComboBox());
        assertSame(ints.get(0), comboBox.getItemAt(0));
        assertSame(ints.get(1), comboBox.getItemAt(1));
        assertSame(ints.get(2), comboBox.getItemAt(2));

        editor = AutoCompleteSupport.createTableCellEditor(GlazedLists.reverseComparator(), new IntegerTableFormat(), ints, 0);
        comboBox = (JComboBox<Integer>) editor.getComponent();
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
    @Test
    public void testSettingSelectedItemInEmptyModel() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        final EventList<String> items = new BasicEventList<>();

        AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> comboBoxModel = combo.getModel();

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

    @Test
    public void testFirstItem() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();
        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();
        final EventList<String> items = new BasicEventList<>();

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        assertNull(support.getFirstItem());

        support.setFirstItem("Special First Item");
        support.removeFirstItem();
        support.setFirstItem("Special First Item");
        final ComboBoxModel<String> comboBoxModel = combo.getModel();

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
    @Test
    public void testChangingComboBoxDataDoesNotAlterComboBoxEditor() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("foobar");

        AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
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
    @Test
    @ExecuteOnNonUiThread
    public void testOnMainThreadChangingComboBoxDataRetainsStrictModeInvariant() throws Exception {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();

        final EventList<String> basicItems = new BasicEventList<>();
        final EventList<String> items = GlazedLists.threadSafeList(basicItems);
        items.add("foobar");

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                final AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
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

    /**
     * If there is no selection, then return caret position.
     * If there is a selection, it must include the end of the text, else null;
     * return the lowest value of selection begin/end.
     */
    private static Integer findSelectionStartToEnd(JTextComponent text) {
        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();
        if (start == end)
            return start;
        if (Integer.max(start,end) != text.getDocument().getLength())
            return null;
        return Integer.min(start,end);
    }

    private static final String GL_DISABLE_CONTAINS_PREFER_STARTS_WITH = "GL:DisableContainsPreferStartsWith";

    /**
     * Test contains/exactMatch/preferStartsWith interactions;
     * verify text, selected text, caret position and selected item after each input.
     * With dropdown values "xabcdex", "ab", "abcd",
     * enter the chars: "abcde", matching "ab", "abcd", "xabcdex".
     * {@literal <BS>} removes selection "x",
     * {@literal <BS>} get's back to "abcd" input/match
     */
    @Test
    public void testContainsPreferStartsWith() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("xabcdex");
        items.add("ab");
        items.add("abcd");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        assertEquals(false, Boolean.TRUE.equals(combo.getClientProperty(GL_DISABLE_CONTAINS_PREFER_STARTS_WITH)));
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, support.getTextMatchingStrategy());

        Integer selStart;

        // type "a", match "ab" since prefer starts with
        doc.replace(0, 0, "a", null);
        assertEquals(2, textField.getCaretPosition());
        assertEquals("ab", model.getSelectedItem());
        assertEquals("b", textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(1, (int)selStart);

        // type "b" replaces "b", match "ab" since exact and prefer starts with
        doc.replace(selStart, doc.getLength() - selStart, "b", null);
        assertEquals(2, textField.getCaretPosition());
        assertEquals("ab", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(2, (int)selStart);

        // type "c", match "abcd"
        doc.replace(selStart, doc.getLength() - selStart, "c", null);
        assertEquals(4, textField.getCaretPosition());
        assertEquals("abcd", model.getSelectedItem());
        assertEquals("d", textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(3, (int)selStart);

        // type "d", match "abcd"
        doc.replace(selStart, doc.getLength() - selStart, "d", null);
        assertEquals(4, textField.getCaretPosition());
        assertEquals("abcd", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(4, (int)selStart);

        // type "e", match "xabcdex" since contains and no starts with match
        doc.replace(selStart, doc.getLength() - selStart, "e", null);
        assertEquals(7, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals("x", textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(6, (int)selStart);
        
        // type <BS>, remove selection "x", input is "abcde",
        // still see "xabcdex" in combo list
        // caret is after "e", no selection
        doc.remove(6, 1);
        assertEquals(6, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());
        
        // type <BS>, remove "e", input is now "abcd", back to "abcd" match
        doc.remove(5, 1);
        assertEquals(4, textField.getCaretPosition());
        assertEquals("abcd", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());
    }

    /**
     * Test contains/exactMatch/notPreferStartsWith interactions.
     * Like the previous, but disable ContainsPreferStartsWith
     * Enter the chars: "abcde",
     * match: "xabcdex", "ab", "xabcdex", "abcd", "xabcdex".
     * The matches to "ab" and "abcd" are because of exact match.
     */
    @Test
    public void testContainsPreferStartsWithNot() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("xabcdex");
        items.add("ab");
        items.add("abcd");

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        final ComboBoxModel<String> model = combo.getModel();
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        combo.putClientProperty(GL_DISABLE_CONTAINS_PREFER_STARTS_WITH, true);
        assertEquals(TextMatcherEditor.IDENTICAL_STRATEGY, support.getTextMatchingStrategy());

        Integer selStart;

        // type "a", match "xabcdex" since prefer starts with
        doc.replace(0, 0, "a", null);
        assertEquals(7, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals("bcdex", textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(2, (int)selStart);

        // type "b" replaces "bcdex", match "ab" since exact match
        doc.replace(selStart, doc.getLength() - selStart, "b", null);
        assertEquals(2, textField.getCaretPosition());
        assertEquals("ab", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(2, (int)selStart);

        // type "c", match "xabcdex"
        doc.replace(selStart, doc.getLength() - selStart, "c", null);
        assertEquals(7, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals("dex", textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(4, (int)selStart);
    
        // type "d", match "abcd" match since exact
        doc.replace(selStart, doc.getLength() - selStart, "d", null);
        assertEquals(4, textField.getCaretPosition());
        assertEquals("abcd", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());

        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(4, (int)selStart);

        // type "e", match "xabcdex" since contains
        doc.replace(selStart, doc.getLength() - selStart, "e", null);
        assertEquals(7, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals("x", textField.getSelectedText());
        
        selStart = findSelectionStartToEnd(textField);
        assertNotNull(selStart);
        assertEquals(6, (int)selStart);

        // type <BS>, remove selection "x", input is "abcde",
        // still see "xabcdex" in combo list
        // caret is after "e", no selection
        doc.remove(6, 1);
        assertEquals(6, textField.getCaretPosition());
        assertEquals("xabcdex", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());
        
        // type <BS>, remove "e", input is now "abcd", back to "abcd" match
        doc.remove(5, 1);
        assertEquals(4, textField.getCaretPosition());
        assertEquals("abcd", model.getSelectedItem());
        assertEquals(null, textField.getSelectedText());
    }

    /**
     * Test that newlines get replaced by space
     */
    @Test
    public void testFilterNewlines() throws BadLocationException {
        final JComboBox<String> combo = new JComboBox<>();

        final JTextField textField = (JTextField) combo.getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) textField.getDocument();

        final EventList<String> items = new BasicEventList<>();
        items.add("a\nb"); // "a b"
        items.add("xxx"); // "xxx"

        AutoCompleteSupport<String> support = AutoCompleteSupport.install(combo, items);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final ComboBoxModel<String> model = combo.getModel();

        // replace string should get filtered
        doc.replace(0, 0, "\n\n", null);
        assertEquals("  ", textField.getText());
        assertEquals(null, model.getSelectedItem());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        // insert string should get filtered
        doc.insertString(0, "\n\n", null);
        assertEquals("  ", textField.getText());
        assertEquals(null, model.getSelectedItem());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        // when a match is found in combo list, any newline from match get filtered

        // type "a", match "a\nb" since prefer starts with
        doc.replace(0, 0, "A", null);
        assertEquals("a\nb", model.getSelectedItem());
        assertEquals("a b", textField.getText());
        assertEquals(" b", textField.getSelectedText());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        // setSelectedItem text should get filterNewlines
        model.setSelectedItem("a\nb");
        assertEquals("a\nb", model.getSelectedItem());
        assertEquals("a b", textField.getText());
        assertEquals(null, textField.getSelectedText());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        // setSelectedIndex text should get filterNewlines
        combo.setSelectedIndex(0);
        assertEquals("a\nb", model.getSelectedItem());
        assertEquals("a b", textField.getText());
        assertEquals(null, textField.getSelectedText());

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());

        // random check that filterNewlines works with CorectsCase
        support.setCorrectsCase(false);
        doc.replace(0, 0, "A", null);
        assertEquals("a\nb", model.getSelectedItem());
        assertEquals("A b", textField.getText());
        assertEquals(" b", textField.getSelectedText());

        // There was a strict mode failure, detect it.
        // First check the transition to strict mode.

        doc.remove(0, doc.getLength());
        assertEquals("", textField.getText());
        assertEquals(null, model.getSelectedItem());
        
        // test going into strict mode (needed for next test)
        // the first item should become selected
        support.setStrict(true);
        assertEquals("a\nb", model.getSelectedItem());
        combo.setSelectedIndex(1);
        assertEquals("xxx", model.getSelectedItem());

        // combo.setSelectedItem text should also get filterNewlines
        // strict matters for a bug
        combo.setSelectedItem("a\nb");
        assertEquals("a\nb", model.getSelectedItem());
        assertEquals("a b", textField.getText());
        assertEquals(null, textField.getSelectedText());
    }

    private static class NoopDocument implements Document {
        private Element root = new NoopElement();

        @Override
        public int getLength() { return 0; }
        @Override
        public void addDocumentListener(DocumentListener listener) { }
        @Override
        public void removeDocumentListener(DocumentListener listener) { }
        @Override
        public void addUndoableEditListener(UndoableEditListener listener) { }
        @Override
        public void removeUndoableEditListener(UndoableEditListener listener) { }
        @Override
        public Object getProperty(Object key) { return null; }
        @Override
        public void putProperty(Object key, Object value) { }
        @Override
        public void remove(int offs, int len) { }
        @Override
        public void insertString(int offset, String str, AttributeSet a) { }
        @Override
        public String getText(int offset, int length) { return ""; }
        @Override
        public void getText(int offset, int length, Segment txt) { }
        @Override
        public Position getStartPosition() { return null; }
        @Override
        public Position getEndPosition() { return null; }
        @Override
        public Position createPosition(int offs) { return null; }
        @Override
        public Element[] getRootElements() { return null; }
        @Override
        public Element getDefaultRootElement() { return root; }
        @Override
        public void render(Runnable r) { }

        private class NoopElement implements Element {
            @Override
            public Document getDocument() { return NoopDocument.this; }
            @Override
            public Element getParentElement() { return null; }
            @Override
            public String getName() { return null; }
            @Override
            public AttributeSet getAttributes() { return null; }
            @Override
            public int getStartOffset() { return 0; }
            @Override
            public int getEndOffset() { return 0; }
            @Override
            public int getElementIndex(int offset) { return 0; }
            @Override
            public int getElementCount() { return 0; }
            @Override
            public Element getElement(int index) { return null; }
            @Override
            public boolean isLeaf() { return false; }
        }
    }

    private static class NoopComboBoxEditor implements ComboBoxEditor {
        @Override
        public Component getEditorComponent() { return null; }
        @Override
        public void setItem(Object anObject) { }
        @Override
        public Object getItem() { return null; }
        @Override
        public void selectAll() { }
        @Override
        public void addActionListener(ActionListener l) { }
        @Override
        public void removeActionListener(ActionListener l) { }
    }

    private static class NoopListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class CountingActionListener implements ActionListener {
        private int count;

        public int getCount() { return count; }
        @Override
        public void actionPerformed(ActionEvent e) { count++; }
    }
}