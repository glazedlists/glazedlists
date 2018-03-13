package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.SearchTerm;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.filter.TextMatchers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A MatcherEditor that matches Objects against search text in a format similiar
 * to search engines. It supports fielded data and search terms (<code>city:Toronto</code>),
 * phrases (<code>city:"New York"</code>), the "+" or required operator as
 * well as the "-" or prohibit operator.
 * <p>
 * This MatcherEditor is fully concrete, but GUI toolkit agnostic, as the search
 * text is passed into the {@link #refilter(String) refilter} method.
 * </p>
 * <p>
 * Subclasses for Swing and SWT applications are provided that present a text
 * filtering interface similar to that of Google and other search engines.
 * </p>
 *
 * @see #refilter(String)
 * @author James Lemieux
 * @author Holger Brands
 */
public class SearchEngineTextMatcherEditor<E> extends TextMatcherEditor<E> {
    /**
     * the Set of Fields recognized by this TextMatcherEditor when the input
     * text is parsed into SearchTerms.
     */
    private final Set<Field<E>> fields = new HashSet<>();

    /**
     * Creates a SearchEngineTextMatcherEditor whose Matchers can test only
     * elements which implement the {@link TextFilterable} interface.
     */
    public SearchEngineTextMatcherEditor() {
        super();
    }

    /**
     * Creates a SearchEngineTextMatcherEditor with the given
     * <code>textFilterator</code>.
     *
     * @param textFilterator an object capable of producing Strings from the
     *        objects being filtered. If <code>textFilterator</code> is
     *        <code>null</code> then all filtered objects are expected to
     *        implement {@link ca.odell.glazedlists.TextFilterable}.
     */
    public SearchEngineTextMatcherEditor(TextFilterator<? super E> textFilterator) {
        super(textFilterator);
    }

    /**
     * Replaces the current set of search fields. This method does not trigger a refilter.
     *
     * @param fields the new search fields to use
     *
     * @see #refilter(String)
     */
    public void setFields(Set<Field<E>> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    /**
     * @return a copy of the defined search fields
     */
    public Set<Field<E>> getFields() {
        return new HashSet<>(fields);
    }

    /**
     * Creates and applies a new {@link TextMatcher} based on the given input
     * text.
     *
     * @param inputText input text (not <code>null</code>) that is parsed
     *        into search terms for the new text matcher
     * @todo explain the supported syntax for the input text in detail
     */
    public void refilter(String inputText) {
        final SearchTerm[] filterTerms = TextMatchers.parse(inputText, getFields());
        setTextMatcher(new TextMatcher<E>(filterTerms, getFilterator(), getMode(), getStrategy()));
    }

    /**
     * A Field object contains information specific to a given field found
     * within the Objects being text matched. Specifically, a Field object
     * describes two distinct things:
     * <ol>
     * <li>what the text is that identifies this Field when parsing the input text of
     * the {@link #refilter(String) refilter} method
     * <li>what TextFilterator to use when extracting all values to text search
     * when matching an Object
     * </ol>
     * For example, the input text "city:Toronto" indicates that the text
     * "Toronto" should only be matched against the values of the "city" field
     * within the Objects being searched. As such, a Field object with "city" as
     * its name and a TextFilterator that only returns the value of the "city"
     * field from the Objects being text matched must be present in the Set of
     * Field objects on the {@link SearchEngineTextMatcherEditor}.
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

        /**
         * Creates a field with a name and {@link TextFilterator}.
         *
         * @param name uniquely identifies this Field relative to all other
         *        registered Field objects
         * @param textFilterator extracts only the field values to be considered
         *        when matching a given SearchTerm
         */
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
         * Returns the TextFilterator capable of extracting only the fields that
         * should be considered by SearchTerms using this Field. It is this
         * TextFilterator that contains the custom logic to return a much
         * smaller subset of the total text-searchable fields on the object.
         * Often the TextFilterators returned by this method only report the
         * value of a single field from the Object being matched.
         */
        public TextFilterator<? super E> getTextFilterator() {
            return textFilterator;
        }

        /** @inheritDoc */
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Field field = (Field) o;

            return name.equals(field.name);
        }

        /** @inheritDoc */
        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}