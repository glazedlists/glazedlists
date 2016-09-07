/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.TextFilterable;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.GlazedListsImpl;
import ca.odell.glazedlists.impl.filter.BoyerMooreCaseInsensitiveTextSearchStrategy;
import ca.odell.glazedlists.impl.filter.ExactCaseInsensitiveTextSearchStrategy;
import ca.odell.glazedlists.impl.filter.RegularExpressionTextSearchStrategy;
import ca.odell.glazedlists.impl.filter.SearchTerm;
import ca.odell.glazedlists.impl.filter.SingleCharacterCaseInsensitiveTextSearchStrategy;
import ca.odell.glazedlists.impl.filter.StartsWithCaseInsensitiveTextSearchStrategy;
import ca.odell.glazedlists.impl.filter.TextMatcher;
import ca.odell.glazedlists.impl.filter.TextMatchers;
import ca.odell.glazedlists.impl.filter.TextSearchStrategy;

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
 * <p>{@link TextMatcherEditor} is able to operate in one of three modes.
 * <ul>
 *   <li>{@link #CONTAINS} will produce {@link Matcher} objects that test if
 *        at least one searchable string for an Object <strong>contains</strong>
 *        one of the filter strings anywhere within itself.
 *
 *   <li>{@link #STARTS_WITH} will produce {@link Matcher} objects that test
 *        if at least one searchable string for an Object
 *        <strong>begins with</strong> at least one of the filter strings.
 *
 *   <li>{@link #REGULAR_EXPRESSION} will produce {@link Matcher} objects that
 *        test if at least one searchable string for an Object <strong>matches,
 *        using regular expression rules,</strong> at least one of the filter
 *        strings.
 * </ul>
 *
 * <p>{@link TextMatcherEditor} is able to operate with one of two strategies.
 * <ul>
 *   <li>{@link #IDENTICAL_STRATEGY} defines a text match as a precise
 *        character-for-character match between the filters and the text.
 *
 *   <li>{@link #NORMALIZED_STRATEGY} defines a text match more leniently for
 *        Latin-character based languages. Specifically, diacritics are
 *        stripped from all Latin characters before comparisons are made.
 *        Consequently, filters like "resume" match words like "résumé".
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

    /**
     * Matching mode where items are considered a match using a
     * {@link java.util.regex.Matcher} produced by compiling a regular
     * expression into {@link java.util.regex.Pattern}.
     */
    public static final int REGULAR_EXPRESSION = 2;

    /**
     * Matching mode where items are considered a match if they are an exact
     * character for character match with at least one of the filter strings.
     */
    public static final int EXACT = 3;

    /**
     * Character comparison strategy that assumes all characters can be
     * compared directly as though they are ASCII. This implies there is no
     * fuzzy matching with this strategy - each character must be precisely
     * matched.
     */
    public static final Object IDENTICAL_STRATEGY = new IdenticalStrategyFactory();
    // this would be an inner class if declawer supported it
    private static class IdenticalStrategyFactory implements TextSearchStrategy.Factory {
        @Override
        public TextSearchStrategy create(int mode, String filter) {
            if (mode == TextMatcherEditor.CONTAINS) {
                if (filter.length() == 1) {
                    return new SingleCharacterCaseInsensitiveTextSearchStrategy();
                } else {
                    return new BoyerMooreCaseInsensitiveTextSearchStrategy();
                }

            } else if (mode == TextMatcherEditor.STARTS_WITH) {
                return new StartsWithCaseInsensitiveTextSearchStrategy();

            } else if (mode == TextMatcherEditor.REGULAR_EXPRESSION) {
                return new RegularExpressionTextSearchStrategy();

            } else if (mode == TextMatcherEditor.EXACT) {
                return new ExactCaseInsensitiveTextSearchStrategy();

            } else {
                throw new IllegalArgumentException("unrecognized mode: " + mode);
            }
        }
    }

    /**
     * Character comparison strategy that assumes all Latin characters should
     * have their diacritical marks stripped in an effort to normalize words to
     * their most basic form. This allows a degree of fuzziness within the
     * matching algorithm, since words like "resume" will match similar words
     * with diacritics like "résumé". This strategy is particularly useful when
     * the text to be searched contains text like names in foreign languages,
     * and your application would like search terms such as "Muller" to match
     * the actual native spelling: "Müller".
     */
    public static final Object NORMALIZED_STRATEGY = new NormalizedStrategyFactory();
    // this would be an inner class if declawer supported it
    private static class NormalizedStrategyFactory extends IdenticalStrategyFactory {
        @Override
        public TextSearchStrategy create(int mode, String filter) {
            TextSearchStrategy result = super.create(mode, filter);
            // apply our simple character mapper
            result.setCharacterMap(GlazedListsImpl.getLatinDiacriticsStripper());
            return result;
        }
    }

    /** the filterator is used as an alternative to implementing the TextFilterable interface */
    private TextFilterator<? super E> filterator;

    /** one of {@link #CONTAINS}, {@link #STARTS_WITH}, or {@link #REGULAR_EXPRESSION} */
    private int mode = CONTAINS;

    /** one of {@link #IDENTICAL_STRATEGY} or {@link #NORMALIZED_STRATEGY} */
    private TextSearchStrategy.Factory strategy = (TextSearchStrategy.Factory)IDENTICAL_STRATEGY;

    /**
     * Creates a {@link TextMatcherEditor} whose Matchers can test only elements which
     * implement the {@link TextFilterable} interface.
     *
     * <p>The {@link Matcher}s from this {@link MatcherEditor} will throw a
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
    public TextMatcherEditor(TextFilterator<? super E> filterator) {
        this.filterator = filterator;
    }

    /**
     * Get the filterator used to extract Strings from the matched elements.
     */
    public TextFilterator<? super E> getFilterator() {
        return filterator;
    }

    /**
     * Set the filterator used to extract Strings from the matched elements.
     */
    public void setFilterator(TextFilterator<? super E> filterator) {
        if (filterator == this.filterator) {
            return;
        }
        this.filterator = filterator;

        // if no filter text exists, no Matcher change is necessary
        final TextMatcher<E> currentTextMatcher = getCurrentTextMatcher();
        if (currentTextMatcher == null) {
            return;
        }

        fireChanged(currentTextMatcher.newFilterator(filterator));
    }

    /**
     * Modify the behaviour of this {@link TextMatcherEditor} to one of the
     * predefined modes.
     *
     * @param mode either {@link #CONTAINS}, {@link #STARTS_WITH},
     *      {@link #REGULAR_EXPRESSION}, or {@link #EXACT}
     */
    public void setMode(int mode) {
        if (mode != CONTAINS && mode != STARTS_WITH && mode != REGULAR_EXPRESSION && mode != EXACT) {
            throw new IllegalArgumentException("mode must be one of: TextMatcherEditor.CONTAINS, STARTS_WITH, REGULAR_EXPRESSION or EXACT");
        }

        if (mode == this.mode) {
            return;
        }

        // apply the new mode
        final int oldMode = this.mode;
        this.mode = mode;

        // if no filter text exists, no Matcher change is necessary
        final TextMatcher<E> currentTextMatcher = getCurrentTextMatcher();
        if (currentTextMatcher == null) {
            return;
        }

        if (oldMode == CONTAINS && mode == STARTS_WITH) {
            // CONTAINS -> STARTS_WITH is a constraining change
            fireConstrained(currentTextMatcher.newMode(mode));

        } else if (oldMode == STARTS_WITH && mode == CONTAINS) {
            // STARTS_WITH -> CONTAINS is a relaxing change
            fireRelaxed(currentTextMatcher.newMode(mode));

        } else {
            // otherwise we can't do better than a raw change
            fireChanged(currentTextMatcher.newMode(mode));
        }
    }

    /**
     * Returns the behaviour mode for this {@link TextMatcherEditor}.
     *
     * @return one of {@link #CONTAINS} (default), {@link #STARTS_WITH},
     *      {@link #REGULAR_EXPRESSION}, or {@link #EXACT}
     */
    public int getMode() {
        return mode;
    }

    /**
     * Modify the character matching strategy of this {@link TextMatcherEditor}
     * to one of the predefined strategies. See the documentation for each
     * constant in order contrast the strategies.
     *
     * @param strategy either {@link #IDENTICAL_STRATEGY} or {@link #NORMALIZED_STRATEGY}
     */
    public void setStrategy(Object strategy) {
        if(strategy == this.strategy) {
            return;
        }
        if(!(strategy instanceof TextSearchStrategy.Factory)) {
            throw new IllegalArgumentException();
        }

        this.strategy = (TextSearchStrategy.Factory)strategy;

        // if no filter text exists, no Matcher change is necessary
        final TextMatcher<E> currentTextMatcher = getCurrentTextMatcher();
        if (currentTextMatcher == null) {
            return;
        }

        fireChanged(currentTextMatcher.newStrategy(strategy));
    }
    /**
     * Returns the character comparison strategy for this {@link TextMatcherEditor}.
     * See the documentation for each constant in order contrast the strategies.
     *
     * @return one of {@link #IDENTICAL_STRATEGY} or {@link #NORMALIZED_STRATEGY}
     */
    public Object getStrategy() {
        return strategy;
    }

    /**
     * Return the current Matcher if it is a {@link TextMatcher} or
     * <code>null</code> if no current Matcher exists or is something other
     * than a {@link TextMatcher}.
     */
    protected TextMatcher<E> getCurrentTextMatcher() {
        final Matcher<E> currentMatcher = getMatcher();
        if (currentMatcher instanceof TextMatcher) {
            return ((TextMatcher<E>) currentMatcher);
        }

        return null;
    }

    /**
     * Adjusts the filters of this {@link TextMatcherEditor} and fires a change
     * to the {@link Matcher}.
     *
     * @param newFilters the {@link String}s representing all of the filter values
     */
    public void setFilterText(String[] newFilters) {
        // wrap the filter Strings with SearchTerm objects
        final SearchTerm<E>[] searchTerms = new SearchTerm[newFilters.length];
        for (int i = 0; i < searchTerms.length; i++) {
            searchTerms[i] = new SearchTerm<E>(newFilters[i]);
        }

        // adjust the TextMatcher
        setTextMatcher(new TextMatcher<E>(searchTerms, getFilterator(), getMode(), getStrategy()));
    }

    /**
     * This method replaces the current Matcher in this TextMatcherEditor with
     * the <code>newMatcher</code> if it is different. If the current Matcher
     * is also a TextMatcher then many comparisons between the two in order to
     * determine if the new Matcher is a strict constrainment or relaxation of
     * the current TextMatcher.
     *
     * @param newMatcher new TextMatcher which defines the text filtering logic
     */
    protected void setTextMatcher(TextMatcher<E> newMatcher) {
        final TextMatcher<E> oldMatcher = getCurrentTextMatcher();

        // fire the event only as necessary
        if (newMatcher.equals(oldMatcher)) {
            return;
        }

        // if the newMatcher does not have any search terms then it
        // automatically matches
        if (newMatcher.getSearchTerms().length == 0) {
            if (!isCurrentlyMatchingAll()) {
                fireMatchAll();
            }
            return;
        }

        // this is the case when the current Matcher is not a TextMatcher
        if (isCurrentlyMatchingAll()) {
            fireConstrained(newMatcher);
        } else if (TextMatchers.isMatcherRelaxed(oldMatcher, newMatcher)) {
            fireRelaxed(newMatcher);
        } else if (TextMatchers.isMatcherConstrained(oldMatcher, newMatcher)) {
            fireConstrained(newMatcher);
        } else {
            fireChanged(newMatcher);
        }
    }
}