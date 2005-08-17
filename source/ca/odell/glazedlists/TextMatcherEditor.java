/* Glazed Lists                                                 (c) 2003-2005 */
/* <a href="http://publicobject.com/glazedlists/">http://publicobject.com/glazedlists/</a>                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.matchers.*;
import ca.odell.glazedlists.impl.filter.*;

/**
 * A matcher editor that matches Objects that contain a filter text
 * string. This {@link TextMatcherEditor} is not coupled with any UI
 * component that allows the user to edit the filter text. That job is left to
 * subclasses. This matcher is fully concrete, and may be used directly by
 * headless applications.
 *
 * &lt;p&gt;The {@link TextMatcherEditor} requires that either a
 * {@link TextFilterator} be specified in its constructor, or that every Object
 * matched implements the {@link TextFilterable} interface. These
 * are used to specify the {@link String}s to search for each Object.
 *
 * @author James Lemieux
 * @author &lt;a href="mailto:<a href="mailto:jesse@odel.on.ca">jesse@odel.on.ca</a>"&gt;Jesse Wilson&lt;/a&gt;
 */
public class TextMatcherEditor extends AbstractMatcherEditor {

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final TextFilterator filterator;

    /** the filters list is currently just a list of Substrings to include */
    private String[] filters = new String[0];

    /**
     * Creates a {@link TextMatcherEditor} whose Matchers can test only elements which
     * implement the {@link TextFilterable} interface.
     *
     * &lt;p&gt;The {@link Matcher}s from this {@link MatcherEditor} will fire a
     * {@link ClassCastException} when called with an Object that does not implement
     * {@link TextFilterable}.
     */
    public TextMatcherEditor() {
        this(null);
    }

    /**
     * Get the filterator used to extract Strings from the matched elements.
     */
    public TextFilterator getFilterator() {
        return filterator;
    }

    /**
     * Creates a {@link TextMatcherEditor} that matches Objects using the
     * specified {@link TextFilterator} to get the {@link String}s to search.
     *
     * @param filterator the object that will extract filter Strings from each
     *      object in the &lt;code&gt;source&lt;/code&gt;; &lt;code&gt;null&lt;/code&gt; indicates the
     *      list elements implement {@link TextFilterable}
     */
    public TextMatcherEditor(TextFilterator filterator) {
        this.filterator = filterator;
    }

    /**
     * Adjusts the filters of this {@link TextMatcherEditor} and fires a change
     * to the {@link Matcher}.
     *
     * @param filterStrings the {@link String}s representing all of the filter values
     */
    public void setFilterText(String[] filterStrings) {
        String[] oldFilters = this.filters;
        this.filters = TextMatcher.normalizeFilters(filterStrings);

        // fire the event only as necessary
        if(!TextMatcher.isFilterEqual(oldFilters, filters)) {

            // classify the change in filter and apply the new filter to this list
            if(filters.length == 0) {
                fireMatchAll();
            } else if(TextMatcher.isFilterRelaxed(oldFilters, filters)) {
                fireRelaxed(new TextMatcher(filters, filterator));
            } else if(TextMatcher.isFilterConstrained(oldFilters, filters)) {
                fireConstrained(new TextMatcher(filters, filterator));
            } else {
                fireChanged(new TextMatcher(filters, filterator));
            }
        }
    }
}