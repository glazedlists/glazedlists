/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

import ca.odell.glazedlists.util.impl.*;
// for recycling filter strings
import java.util.*;

/**
 * An {@link EventList} that shows only elements that contain a filter text
 * string. The {@link DefaultTextFilterList} is not coupled with any UI
 * component to allow the user to edit the filter text. That job is left to
 * subclasses. This list is fully concrete, and may be used directly by
 * headless applications.
 *
 * <p>The {@link DefaultTextFilterList} requires that either a
 * {@link TextFilterator} be specified in its constructor, or that every object
 * in the source list implements the {@link TextFilterable} interface. These
 * are used to specify the {@link String}s to search for each element.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 * 
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class
 * breaks the contract required by {@link java.util.List}. See {@link EventList}
 * for an example.
 *
 * @author James Lemieux
 */
public class DefaultTextFilterList extends AbstractFilterList {

    /** the filters list is currently just a list of Substrings to include */
    private String[] filters = new String[0];

    /** a map from each filter to a Strategy for locating that filter in arbitrary text */
    private Map filterToTextContainmentStrategyMap = new HashMap();

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private TextFilterator filterator = null;
    
    /** a heavily recycled list of filter Strings, call clear() before use */
    private List filterStrings = new ArrayList();

    /**
     * Creates a {@link DefaultTextFilterList} that filters the specified
     * {@link EventList} of elements, all of which implement the
     * {@link TextFilterable} interface.
     */
    public DefaultTextFilterList(EventList source) {
        this(source, null);
    }

    /**
     * Creates a {@link DefaultTextFilterList} that filters the specified
     * {@link EventList} of elements using the specified {@link TextFilterator}
     * to get the {@link String}s to search.
     *
     * @param source the {@link EventList} to wrap with text filtering
     * @param filterator the object that will extract filter Strings from each
     *      object in the <code>source</code>; <code>null</code> indicates the
     *      list elements implement {@link TextFilterable}
     */
    public DefaultTextFilterList(EventList source, TextFilterator filterator) {
        super(source);
        this.filterator = filterator;
    }

    /**
     * Adjusts the filters of this {@link DefaultTextFilterList} and then
     * applies the new filters to the list.
     *
     * @param filters the {@link String}s representing all of the filter values
     */
    public void setFilterText(final String[] filters) {
        // adjusting the filters and refiltering the source list happens "atomically"
        this.updates.beginEvent(true);

        // store the oldFilters so later we can determine the type of change that has occurred
        final String[] oldFilters = this.filters;
        // update the filters
        this.filters = normalizeFilter(filters);

        // rebuild the filter -> TextSearchStrategy map for the new filters
        filterToTextContainmentStrategyMap.clear();
        for(int i = 0; i < this.filters.length; i++) {
            final String filter = this.filters[i];
            final TextSearchStrategy strategy = this.selectTextSearchStrategy(filter);
            strategy.setSubtext(filter);
            filterToTextContainmentStrategyMap.put(filter, strategy);
        }

        // if the new filter may potentially change the current contents of this list
        if(!isFilterEqual(oldFilters, this.filters)) {

            // classify the change in filter and apply the new filter to this list
            if(this.filters.length == 0) {
                handleFilterCleared();
            } else if(isFilterRelaxed(oldFilters, this.filters)) {
                handleFilterRelaxed();
            } else if(isFilterConstrained(oldFilters, this.filters)) {
                handleFilterConstrained();
            } else {
                handleFilterChanged();
            }
        }

        // commit the changes and notify listeners
        this.updates.commitEvent();
    }

    /**
     * This convenience method returns a copy of the <code>filterStrings</code>
     * in upper case format with null and <code>""</code> values removed. It
     * also removes irrelevant search filters which are search filters that do
     * not constrain the filter. For example, if <code>filterStrings</code>
     * contained both <code>"black"</code> and <code>"blackened"</code> then
     * this method's return value would not contain <code>"black"</code> since
     * <code>"blackened"</code> is a more precise search term that contains
     * <code>"black"</code>.
     *
     * @param filterStrings an array of Strings to convert to upper case
     * @return a copy of the minimal array of <code>filterStrings</code> in
     *      upper case format
     */
    protected String[] normalizeFilter(String[] filterStrings) {
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

                // attempt to search for another minimal filter that starts
                // with minimalFilter[i] to prove that minimalFilter[i] is not
                // a *required* filter
                for(int j = 0; j < minimalFilter.length; j++) {
                    if(minimalFilter[j] && i != j && filterStrings[j].startsWith(filterStrings[i])) {
                        minimalFilter[i] = false;
                        validFilters--;
                        break;
                    }
                }
            }
        }

        // convert all valid filters to uppercase
        final String[] upperCaseFilterStrings = new String[validFilters];
        for(int i = 0, j = 0; j < validFilters; i++)
            if(minimalFilter[i])
                upperCaseFilterStrings[j++] = filterStrings[i].toUpperCase();

        return upperCaseFilterStrings;
    }

    /**
     * This local factory method allows fine grained control over the choice of
     * text search strategies for a given <code>filter</code>. Subclasses are
     * welcome to override this method to return any custom TextSearchStrategy
     * implementations which may exploit valid assumptions about the text being
     * searched or the subtext being located.
     *
     * @param filter the filter for which to locate a TextSearchStrategy
     * @return a TextSearchStrategy capable of locating the given
     *      <code>filter</code> within arbitrary text
     */
    protected TextSearchStrategy selectTextSearchStrategy(String filter) {
        // uncomment me to test the old text search algorithm
        //return new OldCaseInsensitiveTextSearchStrategy();

        // if the filter is only 1 character, use the optimized SingleCharacter strategy
        if(filter.length() == 1) {
            return new SingleCharacterCaseInsensitiveTextSearchStrategy();
        }

        // default to using the Boyer-Moore algorithm
        return new BoyerMooreCaseInsensitiveTextSearchStrategy();
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
     * logically equal to a filter of <code>"abc", "ab", "a"</code>.
     *
     * @param oldFilters a filter value
     * @param newFilters another filter value
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

        // each new filter value must have a precise match with an old filter
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
     *        <code>o[j].startsWith(n[i])</code> is <tt>true</tt>. This is
     *        a modified notion of Set coverage. This requirement stipulates
     *        that <code>newFilter</code> must be covered by
     *        <code>oldFilter</code>.
     *
     *   <li> there exists a value, o[i], in <code>oldFilter</code> for which
     *        there is <strong>NO</strong> value, n[j], in
     *        <code>newFilter</code> that satisifies
     *        <code>o[i].startsWith(n[j])</code>. This is a modified notion of
     *        full Set coverage. This requirement stipulates that
     *        <code>oldFilter</code> must <strong>NOT</strong> be
     *        <strong>fully</strong> covered by <code>newFilter</code>. This
     *        requirement prevents filters that are equal from being considered
     *        relaxed.
     * </ol>
     *
     * Note: filter relaxing and filter constraining are considered inverses of
     *       each other. For any <code>newFilter</code>, n, and
     *       <code>oldFilter</code>, o, <code>isFilterRelaxed(o, n)</code>
     *       returning <tt>true</tt> implies
     *       <code>isFilterConstrained(o, n)</code> will return <tt>false</tt>.
     *       Similarly, if <code>isFilterConstrained(o, n)</code> returns
     *       <tt>true</tt> then <code>isFilterRelaxed(o, n)</code> will return
     *       <tt>false</tt>.
     *
     * @param oldFilters the current value filtering the source list
     * @param newFilters the next value to filter the source list
     * @return <tt>true</tt> if <code>newFilter</code> is a relaxed version of
     *      <code>oldFilter</code>; <tt>false</tt> otherwise
     */
    public static boolean isFilterRelaxed(final String[] oldFilters, final String[] newFilters) {
        // ensure each new filter value has a counterpart in the old filter value that
        // starts with it (and thus the new filter value is covered by the old filter value)
        newFiltersCoveredByOld:
        for(int i = 0; i < newFilters.length; i++) {
            for(int j = 0; j < oldFilters.length; j++) {
                if(oldFilters[j].startsWith(newFilters[i])) continue newFiltersCoveredByOld;
            }
            return false;
        }

        // search for 1 old filter value has no counterpart in the new filter value that
        // starts with it (and thus the old filter value is not covered by the new filter value)
        oldFiltersNotCoveredByNew:
        for(int i = 0; i < oldFilters.length; i++) {
            for(int j = 0; j < newFilters.length; j++) {
                if(newFilters[j].startsWith(oldFilters[i])) continue oldFiltersNotCoveredByNew;
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
     * @param oldFilter the current value filtering the source list
     * @param newFilter the next value to filter the source list
     * @return <tt>true</tt> if <code>newFilter</code> is a constrained version
     *      of <code>oldFilter</code>; <tt>false</tt> otherwise
     * @see #isFilterRelaxed
     */
    public static boolean isFilterConstrained(String[] oldFilter, String[] newFilter) {
        return isFilterRelaxed(newFilter, oldFilter);
    }

    /** {@inheritDoc} */
    public boolean filterMatches(Object element) {
        // populate the strings for this object
        filterStrings.clear();
        if(filterator == null) {
            ((TextFilterable)element).getFilterStrings(filterStrings);
        } else {
            filterator.getFilterStrings(filterStrings, element);
        }

        TextSearchStrategy textSearchStrategy;
        Object filterString;

        // ensure each filter matches at least one field
        filters:
        for(int f = 0; f < filters.length; f++) {
            // get the text search strategy for the current filter
            textSearchStrategy = (TextSearchStrategy)filterToTextContainmentStrategyMap.get(this.filters[f]);

            // search through all fields for the current filter
            for(int c = 0; c < filterStrings.size(); c++) {
                filterString = filterStrings.get(c);
                if(filterString != null && textSearchStrategy.indexOf(filterString.toString()) != -1) {
                    continue filters;
                }
            }
            // no field matched this filter 
            return false;
        }
        // all filters have been matched
        return true;
    }
}