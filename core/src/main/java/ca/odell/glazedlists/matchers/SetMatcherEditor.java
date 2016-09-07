package ca.odell.glazedlists.matchers;

import static ca.odell.glazedlists.impl.Preconditions.checkNotNull;
import static ca.odell.glazedlists.impl.Preconditions.checkState;

import ca.odell.glazedlists.FunctionList.Function;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A {@link MatcherEditor} with blacklist or whitelist matching functionality.
 * To do its work, SetMatcherEditor needs a match set, a function and a mode.
 * A match set of <em>type O</em> needs to be specified as black- or whitelist. The match set can be set at any time.
 * The specified function transforms the list elements of <em> type E</em> into <em>type O</em> for comparison with the match set.
 * The given mode determines the behaviour of the matcher editor:
 * <ul>
 * <li>With {@link Mode#BLACKLIST} transformed list elements that are contained in the match set are filtered out.</li>
 * <li>With {@link Mode#WHITELIST_EMPTY_MATCH_ALL} transformed list elements that are contained in the match set are matched.
 * An empty match set matches all elements.</li>
 * <li>With {@link Mode#WHITELIST_EMPTY_MATCH_NONE} transformed list elements that are contained in the match set are matched.
 * An empty match set matches no elements.</li>
 * </ul>
 *
 * @param <E> type of list elements
 * @param <O> type of match set
 *
 * @see #create(Mode, Function)
 * @see #setMatchSet(Set)
 * @see Mode
 */
public final class SetMatcherEditor<E, O> extends AbstractMatcherEditor<E> {

    private static final Logger L = Logger.getLogger(SetMatcherEditor.class.toString());

    /**
     * Creates a {@link SetMatcherEditor} with the specified {@link Mode} and transformation function.
     * @param mode operation mode of the MatcherEditor (!= null)
     * @param fn function to extract the value from a list element to be matched against the match set (!= null)
     * @return the constructed MatcherEditor
     *s
     * @see Mode
     */
    public static <E, O> SetMatcherEditor<E, O> create(final Mode mode, final Function<E, O> fn) {
        return new SetMatcherEditor<E, O>(mode, fn);
    }

	private final Function<E, O> function;
	private Mode mode;

    private SetMatcherEditor(final Mode mode, final Function<E, O> function) {
        this.function = checkNotNull(function);
        this.mode = checkNotNull(mode);

        /* set the matcher */
        checkState(isCurrentlyMatchingAll());
        if (mode == Mode.WHITELIST_EMPTY_MATCH_NONE) {
            fireMatchNone();
        }
    }

    /**
     * Sets a new match set which triggers a refiltering.
     *
     * @param newSet the new match set (!= null)
     */
    public void setMatchSet(final Set<O> newSet) {
        checkNotNull(newSet);

        /* get the old set */
        Set<O> oldSet;
        if (getMatcher() instanceof SetMatcher) {
            oldSet = ((SetMatcher<E,O>) getMatcher()).matchSet;

        } else {
            oldSet = new HashSet<O>();
        }

        if (oldSet.equals(newSet)) {
            L.fine("new set equals old -> no change to filter");

        } else if (newSet.isEmpty()) {
            if (this.mode == Mode.WHITELIST_EMPTY_MATCH_NONE) {
                L.fine("empty set (" + this.mode + ") -> firing matchNone");
                this.fireMatchNone();

            } else {
                L.fine("empty set (" + this.mode + ") -> firing matchAll");
                this.fireMatchAll();
            }

        } else if (oldSet.isEmpty()) {
            L.fine("old set was empty, new set is not -> firing change");
            this.fireChanged(new SetMatcher<E, O>(newSet, this.mode, this.function));

        } else if (oldSet.containsAll(newSet)) {

            if (this.mode == Mode.BLACKLIST) {
                L.fine("old set contains new set (blacklist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(newSet, this.mode, this.function));

            } else {
                L.fine("old set contains new set (whitelist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(newSet, this.mode, this.function));
            }

        } else if (newSet.containsAll(oldSet)) {
            if (this.mode == Mode.BLACKLIST) {
                L.fine("new set contains old set (blacklist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(newSet, this.mode, this.function));

            } else {
                L.fine("new set contains old set (whitelist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(newSet, this.mode, this.function));
            }

        } else {
            L.fine("old and new set differ -> firing change");
            this.fireChanged(new SetMatcher<E, O>(newSet, this.mode, this.function));
        }
    }

    /**
     * Supported modes of operation for the MatcherEditor.
     */
    public enum Mode {
    	/** the match set specifies the elements to be filtered out. */
        BLACKLIST,
        /** the match set specifies the elements to be matched. The empty match set matches none. */
        WHITELIST_EMPTY_MATCH_NONE,
        /** the match set specifies the elements to be matched. The empty match set matches all. */
        WHITELIST_EMPTY_MATCH_ALL
    }

    private final static class SetMatcher<E, O> implements Matcher<E> {

		private final Set<O> matchSet;
		private final Function<E, O> fn;
		private final Mode mode;

        private SetMatcher(final Set<O> matchSet, final Mode mode, final Function<E, O> fn) {
            this.matchSet = new HashSet<O>(matchSet);
            this.mode = mode;
            this.fn = checkNotNull(fn);
        }

        @Override
        public boolean matches(final E input) {
            boolean result = this.matchSet.contains(this.fn.evaluate(input));
            return (mode == mode.BLACKLIST) ? !result : result;
        }
    }
}
