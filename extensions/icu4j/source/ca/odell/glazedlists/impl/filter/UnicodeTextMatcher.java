/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A text matching strategy that is implemented using the open source
 * package for Unicode: <a href="http://icu.sourceforge.net/">ICU4J</a>,
 * which provides a "Unicode-friendly, locale-sensitive, string-searching
 * class for Java that obeys the string comparison conventions in different
 * countries."
 *
 * In short, this strategy attempts to provide locale-sensitive
 * string-searching that correctly handles cases like:
 *
 * <ul>
 *   <li>accented characters (e.g. ≈ and A are equivalent in English but is NOT EQUIVALENT in Danish)
 *   <li>conjoined letters (e.g. Ê)
 *   <li>ignorable punctuation (e.g. "blackbird" vs. "black-bird")
 * </ul>
 *
 * For example, the special character called "ash" from Old English looks
 * like this '∆' in uppercase and 'Ê' in lowercase. The Unicode Strategy
 * considers the character sequence of two normal latin characters "a" and "e"
 * a match with the ash character. So, "Aeon Flux" and "∆on Flux" match.
 *
 * Similarly, the "eszett" character in the German alpabet resembles "ﬂ"
 * but is equivalent to "ss." Thus, "Ruﬂland" and "Russland" match.
 *
 * An article of all features found in this character matching strategy can
 * be found <a href="http://icu.sourceforge.net/userguide/searchString.html">here</a>.
 *
 * @author James Lemieux
 */
public class UnicodeTextMatcher<E> implements Matcher<E> {

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final TextFilterator<E> filterator;

    /** the filters being matched */
    private final String[] filters;

    /** one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH} */
    private final int mode;

    /** a parallel array to locate filter substrings in arbitrary text */
    private final TextSearchStrategy[] filterStrategies;

    /** a heavily recycled list of filter Strings, call clear() before use */
    private final List<String> filterStrings = new ArrayList<String>();

    /**
     * @param filters an array of filter Strings
     * @param filterator the object that will extract filter Strings from each
     *      object in the <code>source</code>; <code>null</code> indicates the
     *      list elements implement {@link TextFilterable}
     * @param mode one of {@link TextMatcherEditor#CONTAINS} or
     *      {@link TextMatcherEditor#STARTS_WITH} which indicates where to
     *      search for the filter text
     */
    public UnicodeTextMatcher(String[] filters, TextFilterator<E> filterator, int mode) {
        this.filters = filters;
        this.filterator = filterator;
        this.mode = mode;

        // build the parallel list of TextSearchStrategies for the new filters
        filterStrategies = new TextSearchStrategy[this.filters.length];
        for(int i = 0; i < this.filters.length; i++) {
            filterStrategies[i] = new UnicodeCaseInsensitiveTextSearchStrategy(mode);
            filterStrategies[i].setSubtext(this.filters[i]);
        }
    }

    /**
     * Returns the behaviour mode for this {@link UnicodeTextMatcher}.
     *
     * @return one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Returns the filters strings matched by this {@link UnicodeTextMatcher}.
     */
    public String[] getFilters() {
        return filters;
    }

    /** {@inheritDoc} */
    public boolean matches(E element) {
        return TextMatchers.matches(filterStrings, filterator, filterStrategies, element);
    }
}