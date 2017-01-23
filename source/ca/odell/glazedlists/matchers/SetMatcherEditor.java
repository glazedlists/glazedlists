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
    private       Set<O>         set = new HashSet<O>();

    // ~ Constructors ----------------------------------------------------------------------------------------------

    private SetMatcherEditor(final Mode mode, final Function<E, O> function) {
        this.function = checkNotNull(function);
        this.mode = checkNotNull(mode);

        /* set the matcher */
        checkState(isCurrentlyMatchingAll());
        if (mode == Mode.WHITELIST) {
            fireMatchNone();
        }
    }

    // ~ Methods ---------------------------------------------------------------------------------------------------

    public void setMatchSet(final Set<O> value) {
        Set<O> newSet = new HashSet<O>(value);
        Set<O> oldSet = this.set;
        this.set = newSet;

        if (oldSet.equals(newSet)) {
            L.fine("new set equals old -> no change to filter");

        } else if (newSet.isEmpty()) {
            if (this.mode == Mode.WHITELIST) {
                L.fine("empty set (whitelist) -> firing matchNone");
                this.fireMatchNone();

            } else {
                L.fine("empty set (blacklist) -> firing matchAll");
                this.fireMatchAll();
            }

        } else if (oldSet.isEmpty()) {
            L.fine("old set was empty, new set is not -> firing change");
            this.fireChanged(new SetMatcher<E, O>(this.set, this.function));

        } else if (oldSet.containsAll(this.set)) {
            if (this.mode == Mode.WHITELIST) {
                L.fine("old set contains new set (whitelist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(this.set, this.function));

            } else {
                L.fine("old set contains new set (blacklist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(this.set, this.function));
            }

        } else if (this.set.containsAll(oldSet)) {
            if (this.mode == Mode.WHITELIST) {
                L.fine("new set contains old set (whitelist) -> firing relaxed");
                this.fireRelaxed(new SetMatcher<E, O>(this.set, this.function));

            } else {
                L.fine("new set contains old set (blacklist) -> firing constrained");
                this.fireConstrained(new SetMatcher<E, O>(this.set, this.function));
            }

        } else {
            L.fine("old and new set differ -> firing change");
            this.fireChanged(new SetMatcher<E, O>(this.set, this.function));
        }
    }

    // ~ Enumerations ----------------------------------------------------------------------------------------------

    public enum Mode {
        BLACKLIST, WHITELIST
    }

    // ~ Inner Classes ---------------------------------------------------------------------------------------------

    private final static class SetMatcher<E, O> implements Matcher<E> {

        // ~ Instance fields -------------------------------------------------------------------------------------------

        private final Set<O> matchSet;
        private final Function<E, O>  fn;

        // ~ Constructors ----------------------------------------------------------------------------------------------

        private SetMatcher(final Set<O> matchSet, final Function<E, O> fn) {
            this.matchSet = checkNotNull(matchSet);
            this.fn = checkNotNull(fn);
        }

        // ~ Methods ---------------------------------------------------------------------------------------------------

        @Override
        public boolean matches(final E input) {
            return this.matchSet.contains(this.fn.evaluate(input));
        }
    }
}
