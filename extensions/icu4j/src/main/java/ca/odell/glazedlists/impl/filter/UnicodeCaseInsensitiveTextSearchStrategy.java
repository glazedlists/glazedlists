/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.matchers.TextMatcherEditor;

import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RuleBasedCollator;
import com.ibm.icu.text.StringSearch;

import java.text.StringCharacterIterator;

/**
 * This implementation of {@link TextSearchStrategy} is a thin wrapper around
 * ICU4J's <a href="http://icu.sourceforge.net/userguide/searchString.html">StringSearch</a>.
 *
 * This provides us with locale-sensitive text searching. ICU4J handles
 * complicated text matching scenarios like
 *
 * <ul>
 *   <li>'ä' matches "ae"
 *   <li>'ß' matches "ss"
 *   <li>"resume" matches "résumé"
 * </ul>
 *
 * Since text matching algorithms is not the focus of Glazed Lists, it makes
 * sense for us to use the open-source, IBM-backed ICU4J project to accomplish
 * this highly specialized behaviour.
 *
 * @author James Lemieux
 */
public class UnicodeCaseInsensitiveTextSearchStrategy implements TextSearchStrategy {

    private static final RuleBasedCollator COLLATOR;
    static {
        COLLATOR = (RuleBasedCollator) Collator.getInstance();
        // setting the strength property of a Collator determines the minimum
        // level of difference considered significant during comparison
        //
        // PRIMARY is the strongest collator strength value. It is typically
        // used to denote differences between base characters. We use it to
        // make the StringSearch case insensitive.
        COLLATOR.setStrength(Collator.PRIMARY);
    }

    /** The string to locate within a larger text. */
    private String pattern;

    /** one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH} */
    private final int mode;

    /**
     * Construct a UnicodeCaseInsensitiveTextSearchStrategy that matches in one of two ways
     * depending on the value of <code>mode</code>.
     *
     * @param mode one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     */
    public UnicodeCaseInsensitiveTextSearchStrategy(int mode) {
        this.mode = mode;
    }

    /**
     * @throws UnsupportedOperationException since UnicodeCaseInsensitiveTextSearchStrategy
     *  does not support remapping characters
     */
    @Override
    public void setCharacterMap(char[] charMap) {
        throw new UnsupportedOperationException("character maps are not supported by the UnicodeCaseInsensitiveTextSearchStrategy");
    }

    /**
     * Sets the subtext to locate when {@link #indexOf(String)} is called.
     *
     * @param subtext the String to locate in {@link #indexOf(String)}
     */
    @Override
    public void setSubtext(String subtext) {
        this.pattern = subtext;
    }

    /**
     * Returns the index of the first occurrence of <code>subtext</code> within
     * <code>text</code>; or <code>-1</code> if <code>subtext</code> does not
     * occur within <code>text</code>.
     *
     * @param text String in which to locate <code>subtext</code>
     * @return the index of the first occurrence of <code>subtext</code> within
     *      <code>text</code>; or <code>-1</code>
     * @throws IllegalStateException if no subtext has been set
     */
    @Override
    public int indexOf(String text) {
        if (pattern == null) {
            throw new IllegalStateException("setSubtext must be called with a valid value before this method can operate");
        }

        if (text.length() == 0) {
            return -1;
        }

        final int index = new StringSearch(pattern, new StringCharacterIterator(text), COLLATOR).first();
        return mode == TextMatcherEditor.STARTS_WITH && index != 0 ? -1 : index;
    }
}