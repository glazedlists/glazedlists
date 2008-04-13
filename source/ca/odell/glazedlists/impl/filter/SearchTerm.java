/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A SearchTerm object stores metadata around a single piece of text to be
 * located. Search engines like Google capture more information than the simple
 * search text to be located. This object tracks three other pieces of metadata
 * in addition to the text to be located:
 *
 * <ul>
 *   <li>{@link #isNegated()} returns <tt>true</tt> if the search term should
 *       NOT appear within the target text
 *
 *   <li>{@link #isRequired()} returns <tt>true</tt> if the search term MUST be
 *       used and cannot be discarded for any reason. (sometimes search engines
 *       discard frivolous search terms like "and", "of", and "the".
 *
 *   <li>{@link #getField()} controls the TextFilterator that produces the
 *      values to be searched when locating this SearchTerm. Specifically, a
 *      <code>null</code> Field indicates the TextFilterator of the
 *      {@link TextMatcher} will be used to produce values to be searched.
 *      A non-null Field will be used to obtain the TextFilterator that
 *      produces the values to be searched.
 * </ul>
 *
 * @author James Lemieux
 */
public final class SearchTerm<E> implements Serializable {

    // the text to be located
    private final String text;

    // true if this search term should NOT be found in the target text
    private final boolean negated;

    // true if this search term should be included absolutely
    private final boolean required;

    // When field is non-null, it contains the TextFilterator to use to extract
    // the values this SearchTerm should consider when matching a given object.
    // A null value indicates the values extracted by the TextMatcher are to be
    // used.
    private final SearchEngineTextMatcherEditor.Field<E> field;

    /**
     * A recyclable list of filter strings extracted by the TextFilterator of
     * the {@link #field}.
     */
    private final List<String> fieldFilterStrings = new ArrayList<String>();

    /**
     * Construct a new <code>SearchTerm</code> with the given <code>text</code>
     * that is neither negated nor required.
     */
    public SearchTerm(String text) {
        this(text, false, false, null);
    }

    /**
     * Construct a new <code>SearchTerm</code> with the given <code>text</code>
     * that is negated and required according to the given booleans.
     */
    public SearchTerm(String text, boolean negated, boolean required, SearchEngineTextMatcherEditor.Field<E> field) {
        if (text == null)
            throw new IllegalArgumentException("text may not be null");
        this.text = text;
        this.negated = negated;
        this.required = required;
        this.field = field;
    }

    /**
     * Returns the text to be located within a target text.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns <tt>true</tt> if the text must NOT be located within a target
     * text; <tt>false</tt> if the text MUST be located within a target text.
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * Returns <code>true</code> if this <code>SearchTerm</code> must be
     * included within a search and cannot be discarded for any reason. For
     * example, even if it contains typically frivolous search text such as
     * "the", "and" or "of".
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns the object that provides a custom TextFilterator to use when
     * locating this SearchTerm within an object. When this method returns
     * <code>null</code> it indicates that the TextFilterator of the
     * TextMatcher should be used to extract values from a target object.
     */
    public SearchEngineTextMatcherEditor.Field<E> getField() {
        return field;
    }

    List<String> getFieldFilterStrings() {
        return fieldFilterStrings;
    }

    /**
     * Return a new <code>SearchTerm</code> with identical information save for
     * the given <code>text</code>.
     */
    public SearchTerm<E> newSearchTerm(String text) {
        return new SearchTerm<E>(text, isNegated(), isRequired(), getField());
    }

    /**
     * Returns <tt>true</tt> if the given <code>term</code> is
     * <strong>guaranteed</strong> to match fewer text strings than this
     * SearchTerm; <tt>false</tt> otherwise. This method is the mirror opposite
     * of {@link #isRelaxation(SearchTerm)}.
     *
     * @param term the SearchTerm to be tested for constrainment
     * @return <tt>true</tt> if the given <code>term</code> is
     *      <strong>guaranteed</strong> to match fewer text strings than this
     *      SearchTerm; <tt>false</tt> otherwise
     *
     * @see #isRelaxation(SearchTerm)
     */
    boolean isConstrainment(SearchTerm term) {
        // if they're negated state doesn't match then we cannot really compare these search terms
        if (isNegated() != term.isNegated()) return false;

        // if they have a field that doesn't match then we cannot really compare these search terms
        if (!GlazedListsImpl.equal(getField(), term.getField())) return false;

        // if the text is equal then no strict constrainment exists
        if (getText().equals(term.getText())) return false;

        // otherwise constrainment is determined by whether we can locate one's text within the other
        return isNegated() ? term.getText().indexOf(getText()) != -1 : getText().indexOf(term.getText()) != -1;
    }

    /**
     * Returns <tt>true</tt> if the given <code>term</code> is
     * <strong>guaranteed</strong> to match more text strings than this
     * SearchTerm; <tt>false</tt> otherwise. This method is the mirror opposite
     * of {@link #isConstrainment(SearchTerm)}.
     *
     * @param term the SearchTerm to be tested for constrainment
     * @return <tt>true</tt> if the given <code>term</code> is
     *      <strong>guaranteed</strong> to match more text strings than this
     *      SearchTerm; <tt>false</tt> otherwise
     *
     * @see #isRelaxation(SearchTerm)
     */
    boolean isRelaxation(SearchTerm term) {
        return term.isConstrainment(this);
    }

    /** @inheritDoc */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchTerm that = (SearchTerm) o;

        if (negated != that.negated) return false;
        if (required != that.required) return false;
        if (field != null ? !field.equals(that.field) : that.field != null) return false;
        if (!text.equals(that.text)) return false;

        return true;
    }

    /** @inheritDoc */
    public int hashCode() {
        int result;
        result = text.hashCode();
        result = 31 * result + (negated ? 1 : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (field != null ? field.hashCode() : 0);
        return result;
    }
}