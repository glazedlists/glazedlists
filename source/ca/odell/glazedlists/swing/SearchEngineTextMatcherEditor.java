/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.SearchTerm;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.filter.TextMatchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.io.Serializable;

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
public class SearchEngineTextMatcherEditor<E> extends TextMatcherEditor<E> {

    /** the JTextField being observed for actions */
    private final JTextField textField;

    /** the listener attached to the given {@link #textField} */
    private final FilterHandler filterHandler = new FilterHandler();

    /**
     * the Set of Fields recognized by this TextMatcherEditor when the text of
     * the {@link #textField} is parsed into SearchTerms
     */
    private final Set<Field<E>> fields = new HashSet<Field<E>>();

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
    public SearchEngineTextMatcherEditor(JTextField textField, TextFilterator<? super E> textFilterator) {
        super(textFilterator);

        this.textField = textField;
        this.textField.addActionListener(this.filterHandler);

        // if the document is non-empty to begin with!
        refilter();
    }

    public void setFields(Set<Field<E>> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    public Set<Field<E>> getFields() {
        return new HashSet<Field<E>>(fields);
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
     * Update the filter text from the contents of the JTextField.
     */
    private void refilter() {
        final SearchTerm[] filterTerms = TextMatchers.parse(textField.getText(), getFields());
        setTextMatcher(new TextMatcher<E>(filterTerms, getFilterator(), getMode(), getStrategy()));
    }

    /**
     * This class responds to ActionEvents from the JTextField by setting the
     * filter text of this TextMatcherEditor to the contents of the JTextField.
     */
    private class FilterHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            refilter();
        }
    }

    /**
     * A Field object contains information specific to a given field found
     * within the Objects being text matched. Specifically, a Field object
     * describes two distinct things:
     *
     * <ol>
     *   <li>what the text is that identifies this Field when parsing the text
     *       of the JTextField of the {@link SearchEngineTextMatcherEditor}
     *
     *   <li>what TextFilterator to use when extracting all values to text
     *       search when matching an Object
     * </ol>
     *
     * For example, entering "city:Toronto" into the JTextField of the
     * {@link SearchEngineTextMatcherEditor} indicates that the text "Toronto"
     * should only be matched against the values of the "city" field within
     * the Objects being searched. As such, a Field object with "city" as its
     * name and a TextFilterator that only returns the value of the "city"
     * field from the Objects being text matched must be present in the Set
     * of Field objects on the {@link SearchEngineTextMatcherEditor}.
     */
    public static final class Field<E> implements Serializable {

        /**
         * The text which which uniquely identifies this Field relative to all
         * other registered Field objects.
         */
        private final String name;

        /**
         * The TextFilterator that extracts only the field values to be
         * considered when matching a given SearchTerm.
         */
        private final TextFilterator<? super E> textFilterator;

        public Field(String name, TextFilterator<? super E> textFilterator) {
            if (name == null)
                throw new IllegalArgumentException("name may not be null");
            if (textFilterator == null)
                throw new IllegalArgumentException("textFilterator may not be null");

            this.name = name;
            this.textFilterator = textFilterator;
        }

        /**
         * Returns the text to be located which uniquely identifies this Field.
         * For example, if this method returns "city", then filter text of
         * "city:Toronto", when parsed, would construct a SearchTerm for
         * "Toronto" that reports this Field object from
         * {@link SearchTerm#getField()}.
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the TextFilterator capable of extracting only the fields
         * that should be considered by SearchTerms using this Field. It is
         * this TextFilterator that contains the custom logic to return a much
         * smaller subset of the total text-searchable fields on the object.
         * Often the TextFilterators returned by this method only report the
         * value of a single field from the Object being matched.
         */
        public TextFilterator<? super E> getTextFilterator() {
            return textFilterator;
        }

        /** @inheritDoc */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Field field = (Field) o;

            return name.equals(field.name);
        }

        /** @inheritDoc */
        public int hashCode() {
            return name.hashCode();
        }
    }
}