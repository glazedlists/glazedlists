/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

/**
 * Adds a prototype value to a JTextComponent, such as the word "Search" in
 * a search field or the perhaps "user@example.com" in an email field. This
 * also changes the color scheme of the field when the prototype value is
 * in place.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class PrototypeValueSupport {

    /** when the document or text component state changes, handle that */
    private final ShowPrototypeListener listener = new ShowPrototypeListener();

    private final JTextComponent textComponent;

    private final Document masterDocument;
    private final Color masterForeground;

    private final Document prototypeDocument;
    private Color prototypeForeground = Color.gray;

    private PrototypeValueSupport(JTextComponent textComponent, String prototypeValue) {
        // the text component to toggle the documents on
        this.textComponent = textComponent;

        // the master document that the user edits
        this.masterDocument = textComponent.getDocument();
        this.masterForeground = textComponent.getForeground();

        // the prototype document to show when the value is empty
        this.prototypeDocument = new PlainDocument();
        setPrototypeValue(prototypeValue);

        // handle changes to the document and focus
        this.masterDocument.addDocumentListener(listener);
        this.textComponent.addFocusListener(listener);

        // install the initial document
        updateDisplayedDocument();         
    }

    public static PrototypeValueSupport install(JTextComponent textComponent, String prototypeValue) {
        return new PrototypeValueSupport(textComponent, prototypeValue);
    }

    public void uninstall() {
        textComponent.setDocument(masterDocument);
        textComponent.setForeground(masterForeground);
        this.masterDocument.removeDocumentListener(listener);
        this.textComponent.removeFocusListener(listener);
    }

    /**
     * Set the value displayed when the value of the text component is the
     * empty string and it does not have the focus of the mouse.
     */
    public void setPrototypeValue(String prototypeValue) {
        try {
            this.prototypeDocument.insertString(0, prototypeValue, null);
        } catch (BadLocationException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }
    public String getPrototypeValue() {
        try {
            int length = prototypeDocument.getLength();
            return prototypeDocument.getText(0, length);
        } catch (BadLocationException e) {
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    /**
     * The color that the prototype value is rendered in. This is set as the
     * text component's foreground color.
     */
    public Color getPrototypeForeground() {
        return prototypeForeground;
    }
    public void setPrototypeForeground(Color prototypeForeground) {
        this.prototypeForeground = prototypeForeground;
        if(textComponent.getDocument() == prototypeDocument) {
            textComponent.setForeground(prototypeForeground);
        }
    }

    /**
     * Change the displayed document if necessary.
     */
    private void updateDisplayedDocument() {
        Document documentToDisplay = getDocumentToDisplay();
        if(textComponent.getDocument() == documentToDisplay) return;

        // use the new document
        textComponent.setDocument(documentToDisplay);
        if(documentToDisplay == prototypeDocument) textComponent.setForeground(prototypeForeground);
        else textComponent.setForeground(masterForeground);
    }

    /**
     * Use the state of the documents and the text component to
     * determine which document should be displayed.
     */
    private Document getDocumentToDisplay() {
        if(textComponent.hasFocus()) return masterDocument;
        if(masterDocument.getLength() > 0) return masterDocument;
        return prototypeDocument;
    }

    /**
     * Respond to changes in the document and focus to change which value is
     * displayed.
     */
    private final class ShowPrototypeListener implements DocumentListener, FocusListener {
        public void insertUpdate(DocumentEvent e) {
            updateDisplayedDocument();
        }
        public void removeUpdate(DocumentEvent e) {
            updateDisplayedDocument();
        }
        public void changedUpdate(DocumentEvent e) {
            updateDisplayedDocument();
        }
        public void focusGained(FocusEvent e) {
            updateDisplayedDocument();
        }
        public void focusLost(FocusEvent e) {
            updateDisplayedDocument();
        }
    }
}