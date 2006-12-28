/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Common services required when implementing a Matcher that performs text
 * matching.
 *
 * @author James Lemieux
 */
public final class TextMatchers {
    /** The single instance of the {@link StringLengthComparator}. */
    private static final Comparator<String> LENGTH_COMPARATOR = new StringLengthComparator();

    /**
     * Execute the logic that determines whether the given <code>element</code>
     * is matched by all of the given <code>filterStrategies</code>. An optional
     * <code>filterator</code> can be supplied which is responsible for
     * extracting all of the filter strings from the given <code>element</code>.
     * The given <code>filterStrings</code> is passed into this method simply
     * to avoid reallocating a new List object each time this method is called.
     * The caller may and should recycle the <code>filterStrings</code> List.
     *
     * @param filterStrings a recyclable List into which the filter Strings can stored
     * @param filterator the logic capable of extracting filtering Strings from the <code>element</code>
     * @param filterStrategies the optimized logic for locating given search text within the <code>filterStrings</code>
     * @param element the list element on which we are text filtering
     * @return <tt>true</tt> if all <code>filterStrategies</code> located
     *      matching text within the <code>filterStrings</code> extracted from
     *      the given <code>element</code>
     */
    public static <E> boolean matches(List<String> filterStrings, TextFilterator<? super E> filterator, SearchTerm[] searchTerms, TextSearchStrategy[] filterStrategies, E element) {
        // populate the strings for this object
        filterStrings.clear();
        if(filterator == null) {
            ((TextFilterable)element).getFilterStrings(filterStrings);
        } else {
            filterator.getFilterStrings(filterStrings, element);
        }

        // ensure each filter matches at least one field
        filters:
        for(int f = 0; f < filterStrategies.length; f++) {
            // get the text search strategy for the current filter
            TextSearchStrategy textSearchStrategy = filterStrategies[f];
            SearchTerm searchTerm = searchTerms[f];

            if(searchTerm.isNegated()) {
                // search through all fields for the current filter
                for(int i = 0, n = filterStrings.size(); i < n; i++) {
                    Object filterString = filterStrings.get(i);
                    // the call to .toString() appears redundant, but is not, since we
                    // are backwards compatible with old behaviour which allows arbitrary
                    // objects in the filterStrings list

                    // if a match was found, then we have violated the negated search term
                    if(filterString != null && textSearchStrategy.indexOf(filterString.toString()) != -1)
                        return false;
                }

                // the text for the negated search term could not be located, so it is a match!

            } else {
                // search through all fields for the current filter
                for(int i = 0, n = filterStrings.size(); i < n; i++) {
                    Object filterString = filterStrings.get(i);
                    // the call to .toString() appears redundant, but is not, since we
                    // are backwards compatible with old behaviour which allows arbitrary
                    // objects in the filterStrings list

                    // if a match was found, then proceed to the next filter string
                    if(filterString != null && textSearchStrategy.indexOf(filterString.toString()) != -1)
                        continue filters;
                }

                // no field matched this filter
                return false;
            }
        }

        // all filters have been matched
        return true;
    }

    /**
     * This convenience function maps each of the <code>filters</code> by
     * running each of their characters through the <code>characterMap</code>.
     *
     * @param filters the filter Strings to be mapped
     * @param strategy the strategy for mapping a character
     * @return mapped versions of the filter Strings
     */
    public static SearchTerm[] mapSearchTerms(SearchTerm[] filters, TextSearchStrategy.Factory strategy) {
        // if something other than the "normalized latin strategy" is used, return the filters untouched
        if (strategy != TextMatcherEditor.NORMALIZED_STRATEGY)
            return filters;

        final char[] mapper = GlazedListsImpl.getLatinDiacriticsStripper();
        final SearchTerm[] mappedFilters = new SearchTerm[filters.length];

        // map the filter Strings by running each character through the characterMap
        for (int i = 0; i < filters.length; i++) {
            final String filter = filters[i].getText();
            final char[] mappedFilter = new char[filter.length()];

            // map each character in the filter
            for (int j = 0; j < filter.length(); j++) {
                char c = filter.charAt(j);
                mappedFilter[j] = c < mapper.length ? mapper[c] : c;
            }

            // record the mapped filter
            mappedFilters[i] = filters[i].newSearchTerm(new String(mappedFilter));
        }

        return mappedFilters;
    }

    /**
     * This convenience method returns a copy of the <code>filterStrings</code>
     * with null and <code>""</code> values removed. It also removes irrelevant
     * search filters which are filters strings that do not add to the
     * precision of the string filtering. It also orders the search filters in
     * order of descending length so that the longest, most discriminating
     * filters occur near the start of the array.
     *
     * <p>For example, if <code>filterStrings</code> contained both
     * <code>"black"</code> and <code>"blackened"</code> then this method's
     * return value would not contain <code>"black"</code> since
     * <code>"blackened"</code> is a more precise search term that contains
     * <code>"black"</code>.
     *
     * <p>Similarly, <code>"this"</code> and <code>"his"</code> would prune
     * <code>"his"</code> since <code>"his"</code> is less precise than
     * <code>"this"</code> and thus adds no filtering value.
     *
     * <p>If <code>{"this", "blackened"}</code> are passed into this method as
     * the filter strings, they'll be returned as
     * <code>{"blackened", "this"}</code> since <code>"blackened"</code> is
     * longer and thus faster to search for (via Boyer Moore) than the shorter
     * filter string, <code>"this"</code>.
     *
     * @param filterStrings an array of Strings to normalize
     * @return a copy of the minimal array of <code>filterStrings</code> in
     *      the order of longest to shortest
     */
    public static String[] normalizeFilters(String[] filterStrings) {
        // flags to indicate the valid and minimal filters
        final boolean[] minimalFilter = new boolean[filterStrings.length];

        // count the number of valid (non-null and non-empty) filters
        int validFilters = 0;
        for(int i = 0; i < filterStrings.length; i++) {
            if(filterStrings[i] != null && filterStrings[i].length() > 0) {
                validFilters++;
                minimalFilter[i] = true;
            }
        }

        // remove the filters that are not minimal (i.e. "blackened" removes "black")
        for(int i = 0; i < minimalFilter.length; i++) {
            if(minimalFilter[i]) {

                // attempt to search for another minimal filter that contains
                // minimalFilter[i] to prove that minimalFilter[i] is not
                // a *required* filter string
                for(int j = 0; j < minimalFilter.length; j++) {
                    if(minimalFilter[j] && i != j && filterStrings[j].indexOf(filterStrings[i]) != -1) {
                        minimalFilter[i] = false;
                        validFilters--;
                        break;
                    }
                }
            }
        }

        // extract all valid filters into a List
        final List<String> minimalFilters = new ArrayList<String>(validFilters);
        for(int i = 0, j = 0; j < validFilters; i++) {
            if(minimalFilter[i]) {
                minimalFilters.add(filterStrings[i]);
                j++;
            }
        }

        // order the elements of the list according to their lengths
        // so the most discriminating filter strings are considered first
        Collections.sort(minimalFilters, LENGTH_COMPARATOR);

        return minimalFilters.toArray(new String[validFilters]);
    }

    /**
     * By default, text filters are considered to be equal if:
     *
     * <ol>
     *   <li> <code>filter1</code> and <code>filter2</code> are both
     *        non-null arrays of {@link String} objects
     *   <li> <code>filter1</code> is fully covered by <code>filter2</code>
     *   <li> <code>filter2</code> is fully covered by <code>filter1</code>
     * </ol>
     *
     * Note: the number of filter values in <code>filter1</code> or
     * <code>filter2</code> is meaningless. A filter of <code>"abc"</code> is
     * logically equal to a filter of <code>"abc", "abc"</code>.
     *
     * @param oldFilters an array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @param newFilters another array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @return <tt>true</tt> if <code>filter1</code> is the same logical filter
     *      as <code>filter2</code>; <tt>false</tt> otherwise
     */
    public static boolean isFilterEqual(final String[] oldFilters, final String[] newFilters) {
        // each new filter value must have a precise match with an old filter
        // value for the text filters to be considered equal
        newFiltersCoveredByOld:
        for(int i = 0; i < newFilters.length; i++) {
            for(int j = 0; j < oldFilters.length; j++) {
                if(oldFilters[j].equals(newFilters[i])) continue newFiltersCoveredByOld;
            }
            return false;
        }

        // each old filter value must have a precise match with a new filter
        // value for the text filters to be considered equal
        oldFiltersCoveredByNew:
        for(int i = 0; i < oldFilters.length; i++) {
            for(int j = 0; j < newFilters.length; j++) {
                if(newFilters[j].equals(oldFilters[i])) continue oldFiltersCoveredByNew;
            }
            return false;
        }

        return true;
    }

    /**
     * By default, text filters are considered to be relaxed if:
     *
     * <ol>
     *   <li> <code>oldFilter</code> and <code>newFilter</code> are both
     *        non-null arrays of {@link String} objects.
     *
     *   <li> for each value, n[i], in <code>newFilter</code> there is a value
     *        o[j], in <code>oldFilter</code>, for which
     *        <code>o[j].indexOf(n[i])</code> is <tt>&gt; &nbsp; -1</tt>. This is
     *        a modified notion of Set coverage. This requirement stipulates
     *        that <code>newFilter</code> must be covered by
     *        <code>oldFilter</code>.
     *
     *   <li> there exists a value, o[i], in <code>oldFilter</code> for which
     *        there is <strong>NO</strong> value, n[j], in
     *        <code>newFilter</code> that satisifies
     *        <code>o[i].indexOf(n[j]) &gt; &nbsp; -1</code>. This is a modified
     *        notion of full Set coverage. This requirement stipulates that
     *        <code>oldFilter</code> must <strong>NOT</strong> be
     *        <strong>fully</strong> covered by <code>newFilter</code>. This
     *        requirement prevents filters that are equal from being considered
     *        relaxed.
     * </ol>
     *
     * <p>Note: filter relaxing and filter constraining are considered inverses of
     *       each other. For any <code>newFilter</code>, n, and
     *       <code>oldFilter</code>, o, <code>isFilterRelaxed(o, n)</code>
     *       returning <tt>true</tt> implies
     *       <code>isFilterConstrained(o, n)</code> will return <tt>false</tt>.
     *       Similarly, if <code>isFilterConstrained(o, n)</code> returns
     *       <tt>true</tt> then <code>isFilterRelaxed(o, n)</code> will return
     *       <tt>false</tt>.
     *
     * @param oldFilters an array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @param newFilters another array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @return <tt>true</tt> if <code>newFilter</code> is a relaxed version of
     *      <code>oldFilter</code>; <tt>false</tt> otherwise
     */
    public static boolean isFilterRelaxed(final String[] oldFilters, final String[] newFilters) {
        // ensure each new filter value has a counterpart in the old filter value that
        // contains it (and thus the new filter value is covered by the old filter value)
        newFiltersCoveredByOld:
        for(int i = 0; i < newFilters.length; i++) {
            for(int j = 0; j < oldFilters.length; j++) {
                if(oldFilters[j].indexOf(newFilters[i]) != -1) continue newFiltersCoveredByOld;
            }
            return false;
        }

        // search for an old filter value has no counterpart in the new filter value that
        // contains it (and thus the old filter value is not covered by the new filter value)
        oldFiltersNotCoveredByNew:
        for(int i = 0; i < oldFilters.length; i++) {
            for(int j = 0; j < newFilters.length; j++) {
                if(newFilters[j].indexOf(oldFilters[i]) != -1) continue oldFiltersNotCoveredByNew;
            }
            return true;
        }

        return false;
    }

    /**
     * This method simply returns the result of
     * <code>isFilterRelaxed(newFilter, oldFilter)</code>. See
     * {@link #isFilterRelaxed} for a description of why that holds.
     *
     * @param oldFilter an array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @param newFilter another array of {@link #normalizeFilters(String[]) normalized}
     *      filter Strings.
     * @return <tt>true</tt> if <code>newFilter</code> is a constrained version
     *      of <code>oldFilter</code>; <tt>false</tt> otherwise
     * @see #isFilterRelaxed
     */
    public static boolean isFilterConstrained(String[] oldFilter, String[] newFilter) {
        return isFilterRelaxed(newFilter, oldFilter);
    }

    /**
     * Parse the given <code>text</code> and produce an array of
     * {@link SearchTerm} objects that describe text to match as well as
     * details about the way it should be used.
     */
    public static SearchTerm[] parse(String text) {
        final List<SearchTerm> searchTerms = new ArrayList<SearchTerm>();

        StringBuffer searchTermText = new StringBuffer();
        boolean negated = false, required = false, insideTerm = false, insideQuotedTerm = false;

        // step through the text one character at a time
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (insideTerm) {
                // determine if the current character signifies the end of the term text
                final boolean endOfTerm = c == '"' || (!insideQuotedTerm && Character.isWhitespace(c));

                if (endOfTerm) {
                    // record the current SearchTerm
                    searchTerms.add(new SearchTerm(searchTermText.toString(), negated, required));

                    // reset the state for collecting the next SearchTerm
                    searchTermText = new StringBuffer();
                    negated = required = insideTerm = insideQuotedTerm = false;

                } else {
                    searchTermText.append(c);
                }

            } else {
                // clear the state and continue searching for the next term
                if (Character.isWhitespace(c)) {
                    negated = required = insideTerm = insideQuotedTerm = false;
                    continue;
                }

                switch (c) {
                    // quotes mean a term has started and contains no text yet
                    case '"': insideTerm = true; insideQuotedTerm = true; break;

                    // plus means the term that immediately follows is required
                    case '+': required = true; break;

                    // minus means the term that immediately follows must NOT be found
                    case '-': negated = true; break;

                    // any other character is the first character in a new SearchTerm
                    default: searchTermText.append(c); insideTerm = true; break;
                }
            }
        }

        // if a SearchTerm is left hanging, use it as well
        if (searchTermText.length() > 0)
            searchTerms.add(new SearchTerm(searchTermText.toString(), negated, required));

        return searchTerms.toArray(new SearchTerm[searchTerms.size()]);
    }
}