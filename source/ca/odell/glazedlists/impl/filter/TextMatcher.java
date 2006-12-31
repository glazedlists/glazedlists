/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.util.*;

/**
 * Matcher for matching text.
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextMatcher<E> implements Matcher<E> {

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final TextFilterator<? super E> filterator;

    /** one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH} */
    private final int mode;

    /** one of {@link TextMatcherEditor#IDENTICAL_STRATEGY}, {@link TextMatcherEditor#NORMALIZED_STRATEGY} or {@link ca.odell.glazedlists.matchers.GlazedListsICU4J#UNICODE_TEXT_SEARCH_STRATEGY} */
    private final Object strategy;

    /** the search terms being matched */
    private final SearchTerm[] searchTerms;

    /** a parallel array to locate filter substrings in arbitrary text */
    private final TextSearchStrategy[] filterStrategies;

    /** a heavily recycled list of filter Strings, call clear() before use */
    private final List<String> filterStrings = new ArrayList<String>();

    /**
     * @param searchTerms an array of search terms to be matched
     * @param filterator the object that will extract filter Strings from each
     *      object to be matched; <code>null</code> indicates the objects
     *      implement {@link TextFilterable}
     * @param mode one of {@link TextMatcherEditor#CONTAINS} or
     *      {@link TextMatcherEditor#STARTS_WITH} which indicates where to
     *      locate the search terms for a successful match
     * @param strategy one of {@link TextMatcherEditor#IDENTICAL_STRATEGY},
     *      {@link TextMatcherEditor#NORMALIZED_STRATEGY} or
     *      {@link ca.odell.glazedlists.matchers.GlazedListsICU4J#UNICODE_TEXT_SEARCH_STRATEGY}
     *      which indicates what kind of algorithm to use when determining a match
     */
    public TextMatcher(SearchTerm[] searchTerms, TextFilterator<? super E> filterator, int mode, Object strategy) {
        this.filterator = filterator;
        this.searchTerms = TextMatchers.normalizeSearchTerms(searchTerms, (TextSearchStrategy.Factory)strategy);
        this.mode = mode;
        this.strategy = strategy;

        // build the parallel list of TextSearchStrategies for the new searchTerms
        filterStrategies = new TextSearchStrategy[this.searchTerms.length];
        for(int i = 0; i < this.searchTerms.length; i++) {
            filterStrategies[i] = selectTextSearchStrategy(this.searchTerms[i], mode, (TextSearchStrategy.Factory)strategy);
        }
    }

    /**
     * Returns the behaviour mode which indicates where to locate the search
     * terms for a successful match.
     *
     * @return one of {@link TextMatcherEditor#CONTAINS}, {@link TextMatcherEditor#STARTS_WITH}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Returns the strategy which indicates what kind of algorithm to use when determining a match.
     *
     * @return one of {@link TextMatcherEditor#IDENTICAL_STRATEGY}, {@link TextMatcherEditor#NORMALIZED_STRATEGY} or
     *      {@link ca.odell.glazedlists.matchers.GlazedListsICU4J#UNICODE_TEXT_SEARCH_STRATEGY}
     */
    public Object getStrategy() {
        return strategy;
    }

    /**
     * Returns the searchTerms strings matched by this {@link TextMatcher}.
     */
    public SearchTerm[] getSearchTerms() {
        return searchTerms;
    }

    /**
     * Returns the search term strings matched by this {@link TextMatcher}.
     */
    public String[] getSearchTermStrings() {
        final String[] strings = new String[searchTerms.length];

        for (int i = 0; i < searchTerms.length; i++)
            strings[i] = searchTerms[i].getText();

        return strings;
    }

    /** {@inheritDoc} */
    public boolean matches(E element) {
        return TextMatchers.matches(filterStrings, filterator, searchTerms, filterStrategies, element);
    }

    /**
     * Return a new TextMatcher identical to this TextMatcher save for the
     * given <code>mode</code>.
     */
    public TextMatcher newMode(int mode) {
        return new TextMatcher(searchTerms, filterator, mode, strategy);
    }

    /**
     * Return a new TextMatcher identical to this TextMatcher save for the
     * given <code>strategy</code>.
     */
    public TextMatcher newStrategy(Object strategy) {
        return new TextMatcher(searchTerms, filterator, mode, strategy);
    }

    /**
     * This local factory method allows fine grained control over the choice of
     * text search strategies for a given <code>filter</code>.
     *
     * @param filter the filter for which to locate a TextSearchStrategy
     * @param mode the type of search behaviour to use; either
     *      {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     * @param strategy a hint about the character matching strategy to use; either
     *      {@link TextMatcherEditor#IDENTICAL_STRATEGY}, {@link TextMatcherEditor#NORMALIZED_STRATEGY}
     *      or {@link ca.odell.glazedlists.matchers.GlazedListsICU4J#UNICODE_TEXT_SEARCH_STRATEGY}
     * @return a TextSearchStrategy capable of locating the given
     *      <code>filter</code> within arbitrary text
     */
    private static TextSearchStrategy selectTextSearchStrategy(SearchTerm filter, int mode, TextSearchStrategy.Factory strategy) {
        final TextSearchStrategy result = strategy.create(mode, filter.getText());
        result.setSubtext(filter.getText());
        return result;
    }

    /**
     * TextMatcher objects are considered equal if they agree on the mode,
     * strategy, and set of SearchTerms.
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextMatcher that = (TextMatcher) o;

        Set<SearchTerm> thisSearchTerms = new HashSet<SearchTerm>(Arrays.asList(searchTerms));
        Set<SearchTerm> thatSearchTerms = new HashSet<SearchTerm>(Arrays.asList(that.searchTerms));

        if (mode != that.mode) return false;
        if (!thisSearchTerms.equals(thatSearchTerms)) return false;
        if (!strategy.equals(that.strategy)) return false;

        return true;
    }

    /** @inheritDoc */
    public int hashCode() {
        int result;
        result = mode;
        result = 31 * result + strategy.hashCode();
        result = 31 * result + new HashSet<SearchTerm>(Arrays.asList(searchTerms)).hashCode();
        return result;
    }
}