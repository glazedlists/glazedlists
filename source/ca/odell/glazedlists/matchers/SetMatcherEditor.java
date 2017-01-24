package ca.odell.glazedlists.matchers;

import static ca.odell.glazedlists.impl.Preconditions.checkNotNull;
import static ca.odell.glazedlists.impl.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import ca.odell.glazedlists.FunctionList.Function;

public final class SetMatcherEditor<E, O> extends AbstractMatcherEditor<E> {

    // ~ Static fields ---------------------------------------------------------------------------------------------

    private static final Logger L = Logger.getLogger(SetMatcherEditor.class.toString());

    // ~ Static methods --------------------------------------------------------------------------------------------

    public static <E, O> SetMatcherEditor<E, O> create(final Mode mode, final Function<E, O> fn) {
        return new SetMatcherEditor<E, O>(mode, fn);
    }

    // ~ Instance fields -------------------------------------------------------------------------------------------

    private final Function<E, O> function;
    private       Mode           mode;

    // ~ Constructors ----------------------------------------------------------------------------------------------

    private SetMatcherEditor(final Mode mode, final Function<E, O> function) {
        this.function = checkNotNull(function);
        this.mode = checkNotNull(mode);

        /* set the matcher */
        checkState(isCurrentlyMatchingAll());
        if (mode == Mode.WHITELIST_EMPTY_MATCH_NONE) {
            fireMatchNone();
        }
    }

    // ~ Methods ---------------------------------------------------------------------------------------------------

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
            this.fireChanged(new SetMatcher<E, O>(newSet, this.function));

        } else if (oldSet.containsAll(newSet)) {

            if (this.mode == Mode.BLACKLIST) {
                L.fine("old set contains new set (blacklist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(newSet, this.function));

            } else {
                L.fine("old set contains new set (whitelist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(newSet, this.function));
            }

        } else if (newSet.containsAll(oldSet)) {
            if (this.mode == Mode.BLACKLIST) {
                L.fine("new set contains old set (blacklist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(newSet, this.function));

            } else {
                L.fine("new set contains old set (whitelist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(newSet, this.function));
            }

        } else {
            L.fine("old and new set differ -> firing change");
            this.fireChanged(new SetMatcher<E, O>(newSet, this.function));
        }
    }

    // ~ Enumerations ----------------------------------------------------------------------------------------------

    public enum Mode {
        BLACKLIST, WHITELIST_EMPTY_MATCH_NONE, WHITELIST_EMPTY_MATCH_ALL
    }

    // ~ Inner Classes ---------------------------------------------------------------------------------------------

    private final static class SetMatcher<E, O> implements Matcher<E> {

        // ~ Instance fields -------------------------------------------------------------------------------------------

        private final Set<O> matchSet;
        private final Function<E, O>  fn;

        // ~ Constructors ----------------------------------------------------------------------------------------------

        private SetMatcher(final Set<O> matchSet, final Function<E, O> fn) {
            this.matchSet = new HashSet<O>(matchSet);
            this.fn = checkNotNull(fn);
        }

        // ~ Methods ---------------------------------------------------------------------------------------------------

        @Override
        public boolean matches(final E input) {
            return this.matchSet.contains(this.fn.evaluate(input));
        }
    }
}
