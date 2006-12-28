/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

/**
 * A SearchTerm object stores metadata around a single piece of text to be
 * located. Search engines like Google capture more information than the simple
 * search text to be located. This object tracks two other pieces of metadata
 * in addition to the text to be located:
 *
 * <ul>
 *   <li>{@link #isNegated()} returns <tt>true</tt> if the search term should
 *       NOT appear within the target text
 *
 *   <li>{@link #isRequired()} returns <tt>true</tt> if the search term MUST be
 *       used and cannot be discarded for any reason. (sometimes search engines
 *       discard frivolous search terms like "and", "of", and "the".
 * </ul>
 *
 * @author James Lemieux
 */
public final class SearchTerm {

    // the text to be located
    private final String text;

    // true if this search term should NOT be found in the target text
    private final boolean negated;

    // true if this search term should be included absolutely
    private final boolean required;

    /**
     * Construct a new <code>SearchTerm</code> with the given <code>text</code>
     * that is neither negated nor required.
     */
    public SearchTerm(String text) {
        this(text, false, false);
    }

    /**
     * Construct a new <code>SearchTerm</code> with the given <code>text</code>
     * that is negated and required according to the given booleans.
     */
    public SearchTerm(String text, boolean negated, boolean required) {
        this.text = text;
        this.negated = negated;
        this.required = required;
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
     * Return a new <code>SearchTerm</code> with identical information save for
     * the given <code>text</code>.
     */
    public SearchTerm newSearchTerm(String text) {
        return new SearchTerm(text, isNegated(), isRequired());
    }

    /** @inheritDoc */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SearchTerm that = (SearchTerm) o;

        if (negated != that.negated) return false;
        if (required != that.required) return false;
        if (!text.equals(that.text)) return false;

        return true;
    }

    /** @inheritDoc */
    public int hashCode() {
        int result;
        result = text.hashCode();
        result = 31 * result + (negated ? 1 : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
    }
}