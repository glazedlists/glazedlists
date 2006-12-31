/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.GlazedLists;

import java.util.*;

/**
 * Common services required when implementing a Matcher that performs text
 * matching.
 *
 * @author James Lemieux
 */
public final class TextMatchers {
    /** A Comparator that orders SearchTerms according to the length of their text. */
    private static final Comparator<SearchTerm> SEARCHTERM_LENGTH_COMPARATOR = new SearchTermLengthComparator();

    /** A Matcher that only accepts non-negated SearchTerms. */
    private static final Matcher<SearchTerm> NON_NEGATED_MATCHER = Matchers.beanPropertyMatcher(SearchTerm.class, "negated", Boolean.FALSE);

    /** A Matcher that only accepts negated SearchTerms. */
    private static final Matcher<SearchTerm> NEGATED_MATCHER = Matchers.beanPropertyMatcher(SearchTerm.class, "negated", Boolean.TRUE);

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
     * @param searchTerms SearchTerm objects defining each piece of search text as well as metadata about the text
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
     * This convenience method returns a copy of the <code>searchTerms</code>
     * with null and <code>""</code> values removed. It also removes irrelevant
     * search filters which are filters strings that do not add to the
     * precision of the string filtering. It also orders the search filters by
     * length so that the most discriminating filters occur near the start of
     * the array.
     *
     * The <code>negated</code> flag indicates whether the
     * <code>searchTerms</code> must be present (negated == false) or absent
     * (negated == true) in the target text for the SearchTerm to be considered
     * a match.
     *
     * <p>For example, if <code>searchTerms</code> contained both
     * <code>"black"</code> and <code>"blackened"</code> then the return value
     * would not contain <code>"black"</code> since <code>"blackened"</code> is
     * a more precise search term that contains <code>"black"</code>.
     *
     * <p>If <code>{"this", "blackened"}</code> are passed into this method as
     * the <code>searchTerms</code>, they'll be returned as
     * <code>{"blackened", "this"}</code> since <code>"blackened"</code> is
     * longer and thus faster to search for (via Boyer Moore) than the shorter
     * filter string, <code>"this"</code>.
     *
     * @param searchTerms an array of Strings to normalize
     * @param negated <tt>true</tt> if the searchTerms are all negated and must
     *      be absent in the target text; <tt>false</tt> if they are not negated
     *      and thus should be present in the target text
     * @return a copy of the minimal array of <code>searchTerms</code> in
     *      the order of longest to shortest
     */
    private static List<SearchTerm> normalizeSearchTerms(List<SearchTerm> searchTerms, boolean negated) {
        List<SearchTerm> result = new ArrayList<SearchTerm>(searchTerms);

        // filter out null and 0-length SearchTerms - they have no filtering value
        for(Iterator<SearchTerm> i = result.iterator(); i.hasNext();) {
            SearchTerm searchTerm = i.next();
            if(searchTerm == null || searchTerm.getText().length() == 0)
                i.remove();
        }

        // remove the filters that are not minimal (i.e. "blackened" removes "black")
        for(int i = 0; i < result.size(); i++) {
            SearchTerm termI = result.get(i);

            // attempt to find another SearchTerm that contains termI to prove
            // that one of termI or termJ is unnecessary
            for(int j = 0; j < result.size(); j++) {
                SearchTerm termJ = result.get(j);

                if(i != j && termJ.getText().indexOf(termI.getText()) != -1) {
                    if(negated) {
                        if(termJ.isRequired()) continue;
                        result.remove(j);
                    } else {
                        if(termI.isRequired()) continue;
                        result.remove(i);
                        break;
                    }
                }
            }
        }

        // order the elements of the list according to their lengths
        // so the most discriminating filter strings are considered first
        Collections.sort(result, negated ? GlazedLists.reverseComparator(SEARCHTERM_LENGTH_COMPARATOR) : SEARCHTERM_LENGTH_COMPARATOR);

        return result;
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
     * @param oldFilters an array of {@link #normalizeSearchTerms normalized}
     *      filter Strings
     * @param newFilters another array of {@link # normalizeSearchTerms normalized}
     *      filter Strings
     * @return <tt>true</tt> if <code>filter1</code> is the same logical filter
     *      as <code>filter2</code>; <tt>false</tt> otherwise
     */
    static boolean isFilterEqual(final String[] oldFilters, final String[] newFilters) {
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
     * @param oldFilters an array of {@link #normalizeSearchTerms normalized}
     *      filter Strings
     * @param newFilters another array of {@link # normalizeSearchTerms normalized}
     *      filter Strings
     * @return <tt>true</tt> if <code>newFilter</code> is a relaxed version of
     *      <code>oldFilter</code>; <tt>false</tt> otherwise
     */
    static boolean isFilterRelaxed(final String[] oldFilters, final String[] newFilters) {
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
     * @param oldFilter an array of {@link #normalizeSearchTerms normalized}
     *      filter Strings
     * @param newFilter another array of {@link # normalizeSearchTerms normalized}
     *      filter Strings
     * @return <tt>true</tt> if <code>newFilter</code> is a constrained version
     *      of <code>oldFilter</code>; <tt>false</tt> otherwise
     * @see #isFilterRelaxed
     */
    static boolean isFilterConstrained(String[] oldFilter, String[] newFilter) {
        return isFilterRelaxed(newFilter, oldFilter);
    }

    /**
     * This convenience function maps each of the <code>filters</code> by
     * running each of their characters through the <code>characterMap</code>.
     * It also removes unnecessary SearchTerms which are not marked as
     * required.
     *
     * @param filters the filter Strings to be normalized
     * @param strategy the strategy for mapping a character
     * @return mapped versions of the filter Strings
     */
    public static SearchTerm[] normalizeSearchTerms(SearchTerm[] filters, TextSearchStrategy.Factory strategy) {
        // if the "normalized latin strategy" is used, strip the diacritics
        if (strategy == TextMatcherEditor.NORMALIZED_STRATEGY) {
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

            filters = mappedFilters;
        }

        // fetch all negated and non-negated SearchTerm object into two different Lists
        final List<SearchTerm> negatedUnrequiredSearchTerms = Arrays.asList((SearchTerm[]) Matchers.select(filters, NEGATED_MATCHER));
        final List<SearchTerm> nonNegatedUnrequiredSearchTerms = Arrays.asList((SearchTerm[]) Matchers.select(filters, NON_NEGATED_MATCHER));

        // reassemble a super List of all normalized (necessary) SearchTerms
        final Collection<SearchTerm> allSearchTerms = new ArrayList<SearchTerm>(filters.length);
        allSearchTerms.addAll(normalizeSearchTerms(negatedUnrequiredSearchTerms, true));
        allSearchTerms.addAll(normalizeSearchTerms(nonNegatedUnrequiredSearchTerms, false));

        // return the normalized SearchTerms as an array
        return allSearchTerms.toArray(new SearchTerm[allSearchTerms.size()]);
    }

    /**
     * Parse the given <code>text</code> and produce an array of
     * {@link SearchTerm} objects that describe text to match as well as
     * metadata about the way it should be used.
     *
     * @param text the raw text entered by a user
     * @return SearchTerm an object encapsulating a single raw search term as
     *      well as metadata related to the use of the SearchTerm
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

    /**
     * A convenience function to extract the text out of each SearchTerm and
     * return them in a parallel array.
     */
    private static String[] getFilterStrings(SearchTerm[] searchTerms) {
        final String[] filterStrings = new String[searchTerms.length];

        for (int i = 0; i < searchTerms.length; i++)
            filterStrings[i] = searchTerms[i].getText();

        return filterStrings;
    }

    /**
     * A method to determine if <code>newMatcher</code> is an absolute
     * constrainment of <code>oldMatcher</code>, meaning it is guaranteed to
     * match the same or fewer items than the <code>oldMatcher</code>.
     *
     * @param oldMatcher the old TextMatcher being replaced
     * @param newMatcher the new TextMatcher to be used
     * @return <tt>true</tt> iff the <code>newMatcher</code> is guaranteed to
     *      match the same or fewer items than <code>oldMatcher</code>
     */
    public static boolean isMatcherConstrained(TextMatcher oldMatcher, TextMatcher newMatcher) {
        // if the mode or strategy differs it cannot be considered a strict constrainment
        if (oldMatcher.getMode() != newMatcher.getMode()) return oldMatcher.getMode() == TextMatcherEditor.CONTAINS;
        if (oldMatcher.getStrategy() != newMatcher.getStrategy()) return false;

        // now we must test the filter strings to determine if they agree it is a constrainment
        final String[] negatedOldFilters = getFilterStrings(Matchers.select(oldMatcher.getSearchTerms(), NEGATED_MATCHER));
        final String[] negatedNewFilters = getFilterStrings(Matchers.select(newMatcher.getSearchTerms(), NEGATED_MATCHER));

        final boolean negatedFiltersAreEqual = isFilterEqual(negatedOldFilters, negatedNewFilters);

        // if negated SearchTerms exist and are not constrained, short-circuit
        if (!negatedFiltersAreEqual && !isFilterConstrained(negatedOldFilters, negatedNewFilters))
            return false;

        final String[] nonNegatedOldFilters = getFilterStrings(Matchers.select(oldMatcher.getSearchTerms(), NON_NEGATED_MATCHER));
        final String[] nonNegatedNewFilters = getFilterStrings(Matchers.select(newMatcher.getSearchTerms(), NON_NEGATED_MATCHER));

        final boolean nonNegatedFiltersAreEqual = isFilterEqual(nonNegatedOldFilters, nonNegatedNewFilters);

        // if non-negated SearchTerms exist and are not constrained, short-circuit
        if (!nonNegatedFiltersAreEqual && !isFilterConstrained(nonNegatedOldFilters, nonNegatedNewFilters))
            return false;

        // base the return value on whether any filters were actually found - equal TextMatchers are not considered constrained
        return !negatedFiltersAreEqual || !nonNegatedFiltersAreEqual;
    }

    /**
     * A method to determine if <code>newMatcher</code> is an absolute
     * relaxation of <code>oldMatcher</code>, meaning it is guaranteed to
     * match the same or more items than the <code>oldMatcher</code>.
     *
     * @param oldMatcher the old TextMatcher being replaced
     * @param newMatcher the new TextMatcher to be used
     * @return <tt>true</tt> iff the <code>newMatcher</code> is guaranteed to
     *      match the same or more items than <code>oldMatcher</code>
     */
    public static boolean isMatcherRelaxed(TextMatcher oldMatcher, TextMatcher newMatcher) {
        return isMatcherConstrained(newMatcher, oldMatcher);
    }

    /**
     * This Comparator orders {@link SearchTerm}s in descending order by their text lengths.
     */
    private static final class SearchTermLengthComparator implements Comparator<SearchTerm> {
        /** {@inheritDoc} */
        public int compare(SearchTerm a, SearchTerm b) {
            return b.getText().length() - a.getText().length();
        }
    }
}