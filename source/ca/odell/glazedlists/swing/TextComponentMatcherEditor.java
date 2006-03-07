/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.*;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A MatcherEditor that matches Objects that contain the filter text located
 * within a {@link Document}. This {@link TextMatcherEditor} is directly
 * coupled with a Document and fires MatcherEditor changes in response to
 * Document changes. This matcher is fully concrete and is expected to be used
 * by Swing applications.
 *
 * <p>The {@link TextComponentMatcherEditor} constructors require that either a
 * {@link Document} or a {@link JTextComponent} (from which a {@link Document}
 * is extracted) be specified.
 *
 * <p>The MatcherEditor registers itself as a {@link DocumentListener} on the
 * given Document, or {@link ActionListener} for non-live filtering. If this
 * MatcherEditor must be garbage collected before the underlying Document,
 * or JTextField, the listener can be unregistered by calling {@link #dispose()}.
 *
 * @author James Lemieux
 */
public class TextComponentMatcherEditor<E> extends TextMatcherEditor<E> {

    /** The Document that provides the filter values. */
    private final Document document;

    /** the JTextField being observed for actions */
    private JTextComponent textComponent;

    /** whether we're listening to each keystroke */
    private boolean live;

    /** The listener attached to the given {@link #document}. */
    private final FilterHandler filterHandler = new FilterHandler();

    /**
     * Creates a TextMatcherEditor bound to the {@link Document} backing the
     * given <code>textComponent</code> with the given
     * <code>textFilterator</code>.
     *
     * @param textComponent the text component backed by the {@link Document}
     *      that is the source of text filter values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     */
    public TextComponentMatcherEditor(JTextComponent textComponent, TextFilterator<E> textFilterator) {
        this(textComponent, textFilterator, true);
    }

    /**
     * Creates a TextMatcherEditor bound to the {@link Document} backing the
     * given <code>textComponent</code> with the given
     * <code>textFilterator</code>.
     *
     * @param textComponent the text component backed by the {@link Document}
     *      that is the source of text filter values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     * @param live <code>true</code> to filter by the keystroke or <code>false</code>
     *      to filter only when {@link java.awt.event.KeyEvent#VK_ENTER Enter} is pressed
     *      within the {@link JTextField}. Note that non-live filtering is only
     *      supported if <code>textComponent</code> is a {@link JTextField}.
     * @throws IllegalArgumentException if the <code>textComponent</code> does
     *      is not a {@link JTextField} and non-live filtering is specified.
     */
    public TextComponentMatcherEditor(JTextComponent textComponent, TextFilterator<E> textFilterator, boolean live) {
        super(textFilterator);

        this.textComponent = textComponent;
        this.document = textComponent.getDocument();
        this.live = live;
        registerListeners(live);

        // if the document is non-empty to begin with!
        refilter();
    }

    /**
     * Creates a TextMatcherEditor bound to the given <code>document</code>
     * with the given <code>textFilterator</code>.
     *
     * @param document the {@link Document} that is the source of text filter
     *      values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     */
    public TextComponentMatcherEditor(Document document, TextFilterator<E> textFilterator) {
        super(textFilterator);
        this.document = document;
        registerListeners(true);

        // if the document is non-empty to begin with!
        refilter();
    }

    /**
     * Whether filtering occurs by the keystroke or not.
     */
    public boolean isLive() {
        return this.live;
    }

    /**
     * Toggle between filtering by the keystroke and not.
     *
     * @param live <code>true</code> to filter by the keystroke or <code>false</code>
     *      to filter only when {@link java.awt.event.KeyEvent#VK_ENTER Enter} is pressed
     *      within the {@link JTextField}. Note that non-live filtering is only
     *      supported if <code>textComponent</code> is a {@link JTextField}.
     */
    public void setLive(boolean live) {
        if(live == this.live) return;
        deregisterListeners(this.live);
        this.live = live;
        registerListeners(this.live);
    }

    /**
     * Listen live or on action performed.
     */
    private void registerListeners(boolean live) {
        if(live) {
            this.document.addDocumentListener(this.filterHandler);
        } else {
            if(textComponent == null) throw new IllegalArgumentException("Non-live filtering supported only for JTextField (document provided)");
            if(!(textComponent instanceof JTextField)) throw new IllegalArgumentException("Non-live filtering supported only for JTextField (argument class " + textComponent.getClass().getName() + ")");
            JTextField textField = (JTextField)textComponent;
            textField.addActionListener(this.filterHandler);
        }
    }

    /**
     * Stop listening.
     */
    private void deregisterListeners(boolean live) {
        if(live) {
            this.document.removeDocumentListener(this.filterHandler);
        } else {
            JTextField textField = (JTextField)textComponent;
            textField.removeActionListener(this.filterHandler);
        }
    }

    /**
     * A cleanup method which stops this MatcherEditor from listening to
     * changes on the underlying {@link Document}, thus freeing the
     * MatcherEditor or Document to be garbage collected.
     */
    public void dispose() {
        deregisterListeners(this.live);
    }

    /**
     * Update the filter text from the contents of the Document.
     */
    private void refilter() {
        try {
            final String text = document.getText(0, document.getLength());
            String[] filters = null;

            // in CONTAINS mode we treat the string as whitespace delimited
            if (this.getMode() == CONTAINS)
                filters = text.split("[ \t]");

            // in STARTS_WITH mode we use the string in its entirety
            else if (this.getMode() == STARTS_WITH)
                filters = new String[] {text};

            setFilterText(filters);
        } catch (BadLocationException ble) {
            // this shouldn't ever, ever happen
            throw new RuntimeException(ble);
        }
    }

    /**
     * This class responds to any change in the Document by setting the filter
     * text of this TextMatcherEditor to the contents of the Document.
     */
    private class FilterHandler implements DocumentListener, ActionListener {
        public void insertUpdate(DocumentEvent e) {
            refilter();
        }
        public void removeUpdate(DocumentEvent e) {
            refilter();
        }
        public void changedUpdate(DocumentEvent e) {
            refilter();
        }
        public void actionPerformed(ActionEvent e) {
            refilter();
        }
    }
}