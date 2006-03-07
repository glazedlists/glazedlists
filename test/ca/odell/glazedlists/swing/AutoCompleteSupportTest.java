package ca.odell.glazedlists.swing;

import junit.framework.TestCase;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.plaf.ComboBoxUI;

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
        final AbstractDocument originalEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();

        AutoCompleteSupport support = AutoCompleteSupport.install(combo, items);

        AbstractDocument currentEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();

        assertTrue(originalUI != combo.getUI());
        assertTrue(originalModel != combo.getModel());
        assertTrue(originalEditable != combo.isEditable());
        assertTrue(originalEditor != combo.getEditor());
        assertTrue(currentEditorDocument != originalEditorDocument);
        assertTrue(currentEditorDocument.getDocumentFilter() != null);

        support.dispose();

        currentEditorDocument = (AbstractDocument) ((JTextField) combo.getEditor().getEditorComponent()).getDocument();

        assertTrue(originalUI == combo.getUI());
        assertTrue(originalModel == combo.getModel());
        assertTrue(originalEditable == combo.isEditable());
        assertTrue(originalEditor == combo.getEditor());
        assertTrue(originalEditorDocument == currentEditorDocument);
        assertTrue(currentEditorDocument.getDocumentFilter() == null);

        try {
            support.dispose();
            fail("Double disposing AutoCompleteSupport did not fail as expected");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}