/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.SearchEngineTextMatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /** A Matcher that only accepts SearchTerms with null Fields. */
    private static final Matcher<SearchTerm> NO_FIELD_MATCHER = Matchers.beanPropertyMatcher(SearchTerm.class, "negated", null);

    /** A Matcher that only accepts SearchTerms without null Fields. */
    private static final Matcher<SearchTerm> FIELD_MATCHER = Matchers.invert(NO_FIELD_MATCHER);

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
    public static <E> boolean matches(List<String> filterStrings, TextFilterator<? super E> filterator, SearchTerm<E>[] searchTerms, TextSearchStrategy[] filterStrategies, E element) {
        boolean filterStringsPopulated = false;

        // ensure each filter matches at least one field
        filters:
        for(int f = 0; f < filterStrategies.length; f++) {
            // get the text search strategy for the current filter
            TextSearchStrategy textSearchStrategy = filterStrategies[f];
            SearchTerm<E> searchTerm = searchTerms[f];
            final SearchEngineTextMatcherEditor.Field searchTermField = searchTerm.getField();

            // if the SearchTerm has a Field, use its TextFilterator to extract the filterStrings
            final List<String> strings;
            if (searchTermField != null) {
                strings = searchTerm.getFieldFilterStrings();
                // populate the strings for this object using the SearchTerm's TextFilterator
                strings.clear();
                searchTermField.getTextFilterator().getFilterStrings(strings, element);
            } else {
                if (!filterStringsPopulated) {
                    // populate the strings for this object
                    filterStrings.clear();
                    if(filterator == null) {
                        ((TextFilterable)element).getFilterStrings(filterStrings);
                    } else {
                        filterator.getFilterStrings(filterStrings, element);
                    }
                    filterStringsPopulated = true;
                }
                strings = filterStrings;
            }

            if(searchTerm.isNegated()) {
                // search through all fields for the current filter
                for(int i = 0, n = strings.size(); i < n; i++) {
                    Object filterString = strings.get(i);
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
                for(int i = 0, n = strings.size(); i < n; i++) {
                    Object filterString = strings.get(i);
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
        final SearchTerm[] nonNullFieldSearchTerms = Matchers.select(filters, NO_FIELD_MATCHER);
        final SearchTerm[] nullFieldSearchTerms = Matchers.select(filters, FIELD_MATCHER);

        // fetch all negated and non-negated SearchTerm object into two different Lists
        final List<SearchTerm> negatedUnrequiredSearchTerms = Arrays.asList((SearchTerm[]) Matchers.select(nullFieldSearchTerms, NEGATED_MATCHER));
        final List<SearchTerm> nonNegatedUnrequiredSearchTerms = Arrays.asList((SearchTerm[]) Matchers.select(nullFieldSearchTerms, NON_NEGATED_MATCHER));

        // reassemble a super List of all normalized (necessary) SearchTerms
        final Collection<SearchTerm> allSearchTerms = new ArrayList<SearchTerm>(filters.length);
        allSearchTerms.addAll(Arrays.asList(nonNullFieldSearchTerms));
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
        return parse(text, Collections.EMPTY_SET);
    }

    /**
     * Parse the given <code>text</code> and produce an array of
     * {@link SearchTerm} objects that describe text to match as well as
     * metadata about the way it should be used. When parsing the text, the
     * given Set of {@link SearchEngineTextMatcherEditor.Field} objects
     * should be considered to detect when the user has entered a
     * field-specific SearchTerm.
     *
     * @param text the raw text entered by a user
     * @param fields a Set of objects describing each field that can be independently matched
     * @return SearchTerm an object encapsulating a single raw search term as
     *      well as metadata related to the use of the SearchTerm
     */
    public static <E> SearchTerm<E>[] parse(String text, Set<SearchEngineTextMatcherEditor.Field<E>> fields) {
        final List<SearchTerm<E>> searchTerms = new ArrayList<SearchTerm<E>>();

        // map each field name to the corresponding field
        final Map<String, SearchEngineTextMatcherEditor.Field<E>> fieldMap = new HashMap<String, SearchEngineTextMatcherEditor.Field<E>>();
        for (Iterator<SearchEngineTextMatcherEditor.Field<E>> f = fields.iterator(); f.hasNext();) {
            SearchEngineTextMatcherEditor.Field<E> field = f.next();
            fieldMap.put(field.getName(), field);
        }

        StringBuffer searchTermText = new StringBuffer();
        SearchEngineTextMatcherEditor.Field<E> field = null;
        boolean negated = false, required = false, insideTerm = false, insideQuotedTerm = false;

        // step through the text one character at a time
        for (int i = 0, n = text.length(); i < n; i++) {
            final char c = text.charAt(i);

            if (insideTerm) {
                // determine if the current character signifies the end of the term text
                final boolean endOfTerm = c == '"' || (!insideQuotedTerm && Character.isWhitespace(c));

                if (endOfTerm) {
                    if (searchTermText.length() > 0) {
                        // record the current SearchTerm
                        searchTerms.add(new SearchTerm<E>(searchTermText.toString(), negated, required, field));
                    }

                    // reset the state for collecting the next SearchTerm
                    searchTermText = new StringBuffer();
                    field = null;
                    negated = required = insideTerm = insideQuotedTerm = false;

                } else {
                    // if a colon is encountered and a field does not yet exist,
                    // check if the colon signifies the end of a known field name
                    if (c == ':' && field == null && !insideQuotedTerm) {
                        field = fieldMap.get(searchTermText.toString());

                        // if a field was located, clear the searchTermText as it contains the field name
                        if (field != null) {
                            searchTermText = new StringBuffer();
                            negated = required = insideTerm = insideQuotedTerm = false;
                            continue;
                        }
                    }

                    searchTermText.append(c);
                }

            } else {
                // clear the state and continue searching for the next term
                if (Character.isWhitespace(c)) {
                    field = null;
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
            searchTerms.add(new SearchTerm<E>(searchTermText.toString(), negated, required, field));

        return searchTerms.toArray(new SearchTerm[searchTerms.size()]);
    }

    /**
     * A method to determine if <code>newMatcher</code> is an absolute
     * constrainment of <code>oldMatcher</code>, meaning it is guaranteed to
     * match fewer items than the <code>oldMatcher</code>.
     *
     * @param oldMatcher the old TextMatcher being replaced
     * @param newMatcher the new TextMatcher to be used
     * @return <tt>true</tt> iff the <code>newMatcher</code> is guaranteed to
     *      match the same or fewer items than <code>oldMatcher</code>
     */
    public static boolean isMatcherConstrained(TextMatcher oldMatcher, TextMatcher newMatcher) {
        // equal TextMatchers are never considered constrained or relaxed
        if (oldMatcher.equals(newMatcher)) return false;

        // if the strategies don't match we cannot report a constrainment
        if (oldMatcher.getStrategy() != newMatcher.getStrategy()) return false;

        // if the mode went from STARTS_WITH to CONTAINS the TextMatcher cannot be a constrainment
        if (oldMatcher.getMode() == TextMatcherEditor.STARTS_WITH && newMatcher.getMode() == TextMatcherEditor.CONTAINS)
            return false;

        // if either mode is REGULAR_EXPRESSION we cannot reliably report a constrainment
        if (oldMatcher.getMode() == TextMatcherEditor.REGULAR_EXPRESSION || newMatcher.getMode() == TextMatcherEditor.REGULAR_EXPRESSION)
            return false;

        // if either mode is EXACT we cannot reliably report a constrainment
        if (oldMatcher.getMode() == TextMatcherEditor.EXACT || newMatcher.getMode() == TextMatcherEditor.EXACT)
            return false;

        // extract the SearchTerms for comparison
        final SearchTerm[] oldTerms = oldMatcher.getSearchTerms();
        final SearchTerm[] newTerms = newMatcher.getSearchTerms();

        // we search the newTerms to locate an oldTerm whose matching power isn't covered
        oldTermsCoveredByNew:
        for (int i = 0; i < oldTerms.length; i++) {
            for (int j = 0; j < newTerms.length; j++) {
                if (newTerms[j].equals(oldTerms[i])) continue oldTermsCoveredByNew;
                if (newTerms[j].isConstrainment(oldTerms[i])) continue oldTermsCoveredByNew;
            }
            return false;
        }

        return true;
    }

    /**
     * A method to determine if <code>newMatcher</code> is an absolute
     * relaxation of <code>oldMatcher</code>, meaning it is guaranteed to
     * match more items than the <code>oldMatcher</code>.
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