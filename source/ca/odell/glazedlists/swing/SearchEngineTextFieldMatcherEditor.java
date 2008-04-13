/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A MatcherEditor that matches Objects that contain the filter text located
 * within a {@link JTextField}. This {@link TextMatcherEditor} is directly
 * coupled with a JTextField that is meant to emulate a search engine's text
 * box. This matcher is fully concrete and is expected to be used
 * by Swing applications that want to present a text filtering interface
 * similar to that of Google and other search engines.
 *
 * <p>The MatcherEditor registers itself as an {@link ActionListener} on the
 * given JTextField. If this MatcherEditor must be garbage collected before
 * the underlying JTextField, the listener can be unregistered by calling
 * {@link #dispose()}.
 *
 * @author James Lemieux
 */
public class SearchEngineTextFieldMatcherEditor<E> extends SearchEngineTextMatcherEditor<E> {

    /** the JTextField being observed for actions */
    private final JTextField textField;

    /** the listener attached to the given {@link #textField} */
    private final FilterHandler filterHandler = new FilterHandler();

    /**
     * Creates a TextMatcherEditor bound to the given <code>textField</code>
     * with the given <code>textFilterator</code>.
     *
     * @param textField the text component that edits and supplies text filter values
     * @param textFilterator an object capable of producing Strings from the
     *      objects being filtered. If <code>textFilterator</code> is
     *      <code>null</code> then all filtered objects are expected to
     *      implement {@link ca.odell.glazedlists.TextFilterable}.
     */
    public SearchEngineTextFieldMatcherEditor(JTextField textField, TextFilterator<? super E> textFilterator) {
        super(textFilterator);

        this.textField = textField;
        this.textField.addActionListener(this.filterHandler);

        // if the document is non-empty to begin with!
        refilter(textField.getText());
    }

    /**
     * A cleanup method which stops this MatcherEditor from listening to
     * the underlying {@link JTextField}, thus freeing the
     * SearchEngineTextMatcherEditor to be garbage collected.
     */
    public void dispose() {
        textField.removeActionListener(this.filterHandler);
    }

    /**
     * This class responds to ActionEvents from the JTextField by setting the
     * filter text of this TextMatcherEditor to the contents of the JTextField.
     */
    private class FilterHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refilter(textField.getText());
        }
    }
}