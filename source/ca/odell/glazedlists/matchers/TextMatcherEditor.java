/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.util.text.CharacterNormalizer;
import ca.odell.glazedlists.impl.filter.TextMatcher;

/**
 * A matcher editor that matches Objects that contain a filter text string.
 * This {@link TextMatcherEditor} is not coupled with any UI component that
 * allows the user to edit the filter text. That job is left to subclasses.
 * This matcher editor is fully concrete, and may be used directly by headless
 * applications.
 *
 * <p>The {@link TextMatcherEditor} requires that either a
 * {@link TextFilterator} be specified in its constructor, or that every Object
 * to be matched implements the {@link TextFilterable} interface. These are
 * used to extract the searchable {@link String}s for each Object.
 *
 * <p>{@link TextMatcherEditor} is able to operate in one of two modes.
 * <ul>
 *   <li>{@link #CONTAINS} will produce {@link Matcher} objects that test if
 *        at least one searchable string for an Object contains one of the
 *        filter strings <strong>anywhere</strong> within itself.
 *
 *   <li>{@link #STARTS_WITH} will produce {@link Matcher} objects that test
 *        if at least one searchable string for an Object
 *        <strong>begins with</strong> at least one of the filter strings.
 * </ul>
 *
 * @author James Lemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextMatcherEditor<E> extends AbstractMatcherEditor<E> {

    /**
     * Matching mode where items are considered a match if at least one of the
     * filter strings extracted from an object contains one of the given search
     * strings.
     */
    public static final int CONTAINS = 0;

    /**
     * Matching mode where items are considered a match if at least one of the
     * filter strings extracted from an object starts with one of the given search
     * strings.
     */
    public static final int STARTS_WITH = 1;

    private static final String[] EMPTY_FILTER = new String[0];

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private final TextFilterator<E> filterator;

    /** one of {@link #CONTAINS} or {@link #STARTS_WITH} */
    private int mode = CONTAINS;

    /** the optional strategy for normalizing characters to control the "fuzziness" of text matches */
    private CharacterNormalizer characterNormalizer;

    /**
     * Creates a {@link TextMatcherEditor} whose Matchers can test only elements which
     * implement the {@link TextFilterable} interface.
     *
     * <p>The {@link Matcher}s from this {@link MatcherEditor} will fire a
     * {@link ClassCastException} when called with an Object that does not implement
     * {@link TextFilterable}.
     */
    public TextMatcherEditor() {
        this(null);
    }

    /**
     * Creates a {@link TextMatcherEditor} that matches Objects using the
     * specified {@link TextFilterator} to get the {@link String}s to search.
     *
     * @param filterator the object that will extract filter Strings from each
     *      object in the <code>source</code>; <code>null</code> indicates the
     *      list elements implement {@link TextFilterable}
     */
    public TextMatcherEditor(TextFilterator<E> filterator) {
        this.filterator = filterator;
    }

    /**
     * Get the filterator used to extract Strings from the matched elements.
     */
    public TextFilterator<E> getFilterator() {
        return filterator;
    }

    /**
     * Modify the behaviour of this {@link TextMatcherEditor} to one of the
     * predefined modes.
     *
     * @param mode either {@link #CONTAINS} or {@link #STARTS_WITH}.
     */
    public void setMode(int mode) {
        if(mode != CONTAINS && mode != STARTS_WITH)
            throw new IllegalArgumentException("Mode must be either TextMatcherEditor.CONTAINS or TextMatcherEditor.STARTS_WITH");
        if(mode == this.mode) return;

        // apply the new mode
        this.mode = mode;

        // if no filter text exists, no Matcher change is necessary
        if (getCurrentFilter().length == 0)
            return;

        if (mode == STARTS_WITH) {
            // CONTAINS -> STARTS_WITH is a constraining change
            fireConstrained(new TextMatcher<E>(getCurrentFilter(), filterator, mode, characterNormalizer));
        } else {
            // STARTS_WITH -> CONTAINS is a relaxing change
            fireRelaxed(new TextMatcher<E>(getCurrentFilter(), filterator, mode, characterNormalizer));
        }
    }
    /**
     * Returns the behaviour mode for this {@link TextMatcherEditor}.
     *
     * @return one of {@link #CONTAINS} (default) or {@link #STARTS_WITH}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Returns the {@link CharacterNormalizer} being used to normalize (remap)
     * characters immediately before they are compared. A
     * {@link CharacterNormalizer} is commonly used to control the "fuzziness"
     * of text matches since it can decide, for example, that 'é' maps to 'e',
     * and thus "resume" and "résumé" are a fuzzy match.
     */
    public CharacterNormalizer getCharacterNormalizer() {
        return characterNormalizer;
    }

    /**
     * Sets the strategy for normalizing (remapping) characters immediately
     * before they are compared. A {@link CharacterNormalizer} is commonly
     * used to control the "fuzziness" of text matches since it can decide,
     * for example, that 'é' maps to 'e', and thus "resume" and "résumé" are
     * a fuzzy match.
     *
     * <p>Note that {@link CharacterNormalizer} assumes each character is
     * mapped to EXACTLY one other character. This implies that, for example,
     * it cannot be used to normalize 'Æ' to "AE".
     */
    public void setCharacterNormalizer(CharacterNormalizer characterNormalizer) {
        this.characterNormalizer = characterNormalizer;
    }

    /**
     * Returns the filter that was last produced from this editor or an empty
     * filter if one doesn't exist.
     */
    private String[] getCurrentFilter() {
        if (currentMatcher instanceof TextMatcher)
            return ((TextMatcher<E>) currentMatcher).getFilters();

        return EMPTY_FILTER;
    }

    /**
     * Adjusts the filters of this {@link TextMatcherEditor} and fires a change
     * to the {@link Matcher}.
     *
     * @param newFilters the {@link String}s representing all of the filter values
     */
    public void setFilterText(String[] newFilters) {
        final String[] oldFilters = getCurrentFilter();
        newFilters = TextMatcher.normalizeFilters(newFilters);

        // fire the event only as necessary
        if (TextMatcher.isFilterEqual(oldFilters, newFilters))
            return;

        // classify the change in filter and apply the new filter to this list
        if (newFilters.length == 0) {
            fireMatchAll();
            return;
        }

        // classify the change in filter and apply the new filter to this list
        final TextMatcher<E> matcher = new TextMatcher<E>(newFilters, filterator, mode, characterNormalizer);

        if (TextMatcher.isFilterRelaxed(oldFilters, newFilters))
            fireRelaxed(matcher);
        else if (TextMatcher.isFilterConstrained(oldFilters, newFilters))
            fireConstrained(matcher);
        else
            fireChanged(matcher);
    }
}