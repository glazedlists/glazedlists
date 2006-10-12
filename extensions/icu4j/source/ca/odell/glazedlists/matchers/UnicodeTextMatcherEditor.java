/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.filter.TextMatchers;
import ca.odell.glazedlists.impl.filter.UnicodeTextMatcher;

/**
 * A matcher editor that matches Objects that contain a filter text string.
 * This {@link UnicodeTextMatcherEditor} is not coupled with any
 * UI component that allows the user to edit the filter text. That job is left to
 * subclasses. This matcher editor is fully concrete, and may be used directly
 * by headless applications.
 *
 * <p>The {@link UnicodeTextMatcherEditor} requires that either a
 * {@link TextFilterator} be specified in its constructor, or that every Object
 * to be matched implements the {@link ca.odell.glazedlists.TextFilterable}
 * interface. These are used to extract the searchable {@link String}s for each
 * Object.
 *
 * <p>{@link UnicodeTextMatcherEditor} is able to operate in one of two modes.
 * <ul>
 *   <li>{@link TextMatcherEditor#CONTAINS} will produce {@link Matcher}
 *        objects that test if at least one searchable string for an Object
 *        contains one of the filter strings <strong>anywhere</strong> within
 *        itself.
 *
 *   <li>{@link TextMatcherEditor#STARTS_WITH} will produce {@link Matcher}
 *        objects that test if at least one searchable string for an Object
 *        <strong>begins with</strong> at least one of the filter strings.
 * </ul>
 *
 * @author James Lemieux
 */
public class UnicodeTextMatcherEditor<E> extends AbstractMatcherEditor<E> {

    private static final String[] EMPTY_FILTER = new String[0];

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final TextFilterator<E> filterator;

    /** one of {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH} */
    private int mode = TextMatcherEditor.CONTAINS;

    /**
     * Creates a {@link UnicodeTextMatcherEditor} whose Matchers can test only elements which
     * implement the {@link ca.odell.glazedlists.TextFilterable} interface.
     *
     * <p>The {@link Matcher}s from this {@link MatcherEditor} will fire a
     * {@link ClassCastException} when called with an Object that does not implement
     * {@link ca.odell.glazedlists.TextFilterable}.
     */
    public UnicodeTextMatcherEditor() {
        this(null);
    }

    /**
     * Creates a {@link UnicodeTextMatcherEditor} that matches Objects using the
     * specified {@link TextFilterator} to get the {@link String}s to search.
     *
     * @param filterator the object that will extract filter Strings from each
     *      object in the <code>source</code>; <code>null</code> indicates the
     *      list elements implement {@link ca.odell.glazedlists.TextFilterable}
     */
    public UnicodeTextMatcherEditor(TextFilterator<E> filterator) {
        this.filterator = filterator;
    }

    /**
     * Get the filterator used to extract Strings from the matched elements.
     */
    public TextFilterator<E> getFilterator() {
        return filterator;
    }

    /**
     * Modify the behaviour of this {@link UnicodeTextMatcherEditor} to one of the
     * predefined modes.
     *
     * @param mode either {@link TextMatcherEditor#CONTAINS} or {@link TextMatcherEditor#STARTS_WITH}
     */
    public void setMode(int mode) {
        if(mode != TextMatcherEditor.CONTAINS && mode != TextMatcherEditor.STARTS_WITH)
            throw new IllegalArgumentException("mode must be either TextMatcherEditor.CONTAINS or TextMatcherEditor.STARTS_WITH");
        if(mode == this.mode) return;

        // apply the new mode
        this.mode = mode;

        // if no filter text exists, no Matcher change is necessary
        if (getCurrentFilter().length == 0)
            return;

        if (mode == TextMatcherEditor.STARTS_WITH) {
            // CONTAINS -> STARTS_WITH is a constraining change
            fireConstrained(new UnicodeTextMatcher<E>(getCurrentFilter(), filterator, mode));
        } else {
            // STARTS_WITH -> CONTAINS is a relaxing change
            fireRelaxed(new UnicodeTextMatcher<E>(getCurrentFilter(), filterator, mode));
        }
    }
    /**
     * Returns the behaviour mode for this {@link UnicodeTextMatcherEditor}.
     *
     * @return one of {@link TextMatcherEditor#CONTAINS} (default) or {@link TextMatcherEditor#STARTS_WITH}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Returns the filter that was last produced from this editor or an empty
     * filter if one doesn't exist.
     */
    private String[] getCurrentFilter() {
        if (currentMatcher instanceof UnicodeTextMatcher)
            return ((UnicodeTextMatcher<E>) currentMatcher).getFilters();

        return EMPTY_FILTER;
    }

    /**
     * Adjusts the filters of this {@link UnicodeTextMatcherEditor} and fires a change
     * to the {@link Matcher}.
     *
     * @param newFilters the {@link String}s representing all of the filter values
     */
    public void setFilterText(String[] newFilters) {
        final String[] oldFilters = getCurrentFilter();
        newFilters = TextMatchers.normalizeFilters(newFilters);

        // fire the event only as necessary
        if (TextMatchers.isFilterEqual(oldFilters, newFilters))
            return;

        // classify the change in filter and apply the new filter to this list
        if (newFilters.length == 0) {
            fireMatchAll();
            return;
        }

        // classify the change in filter and apply the new filter to this list
        final UnicodeTextMatcher<E> matcher = new UnicodeTextMatcher<E>(newFilters, filterator, mode);

        if (TextMatchers.isFilterRelaxed(oldFilters, newFilters))
            fireRelaxed(matcher);
        else if (TextMatchers.isFilterConstrained(oldFilters, newFilters))
            fireConstrained(matcher);
        else
            fireChanged(matcher);
    }
}