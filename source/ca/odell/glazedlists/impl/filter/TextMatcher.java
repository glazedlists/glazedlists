/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Matcher for matching text.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextMatcher<E> implements Matcher<E> {

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
     * @param strategy one of {@link TextMatcherEditor#IDENTICAL_STRATEGY} or
     *      {@link TextMatcherEditor#NORMALIZED_STRATEGY} which
     *      indicates what kind of algorithm to use when determining a match
     */
    public TextMatcher(String[] filters, TextFilterator<E> filterator, int mode, Object strategy) {
        this.filterator = filterator;
        this.filters = TextMatchers.mapFilters(filters, (TextSearchStrategy.Factory)strategy);
        this.mode = mode;

        // build the parallel list of TextSearchStrategies for the new filters
        filterStrategies = new TextSearchStrategy[this.filters.length];
        for(int i = 0; i < this.filters.length; i++) {
            filterStrategies[i] = selectTextSearchStrategy(this.filters[i], mode, (TextSearchStrategy.Factory)strategy);
        }
    }

    /**
     * Returns the behaviour mode for this {@link TextMatcher}.
     *
     * @return one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Returns the filters strings matched by this {@link TextMatcher}.
     */
    public String[] getFilters() {
        return filters;
    }

    /** {@inheritDoc} */
    public boolean matches(E element) {
        return TextMatchers.matches(filterStrings, filterator, filterStrategies, element);
    }

    /**
     * This local factory method allows fine grained control over the choice of
     * text search strategies for a given <code>filter</code>.
     *
     * @param filter the filter for which to locate a TextSearchStrategy
     * @param mode the type of search behaviour to use; either
     *      {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     * @param strategy a hint about the character matching strategy to use; either
     *      {@link TextMatcherEditor#IDENTICAL_STRATEGY} or
     *      {@link TextMatcherEditor#NORMALIZED_STRATEGY}
     * @return a TextSearchStrategy capable of locating the given
     *      <code>filter</code> within arbitrary text
     */
    private static TextSearchStrategy selectTextSearchStrategy(String filter, int mode, TextSearchStrategy.Factory strategy) {
        final TextSearchStrategy result = strategy.create(mode, filter);
        result.setSubtext(filter);
        return result;
    }
}