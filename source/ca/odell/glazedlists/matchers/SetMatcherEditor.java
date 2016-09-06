package ca.odell.glazedlists.matchers;

import static ca.odell.glazedlists.impl.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import ca.odell.glazedlists.FunctionList.Function;

public final class SetMatcherEditor<E, O> extends AbstractMatcherEditor<E> {

    // ~ Static fields ---------------------------------------------------------------------------------------------

    private static final Logger L = Logger.getLogger(SetMatcherEditor.class.toString());

    // ~ Static methods --------------------------------------------------------------------------------------------

    public static <E, O> SetMatcherEditor<E, O> create(final Function<E, O> fn) {
        return new SetMatcherEditor<E, O>(fn);
    }

    public static <E, O> SetMatcherEditor<E, O> create(final Mode mode, final Function<E, O> fn) {
        return new SetMatcherEditor<E, O>(mode, fn);
    }

    // ~ Instance fields -------------------------------------------------------------------------------------------

    private final Function<E, O>  function;
    private       Mode            mode;
    private       Set<O>          set = new HashSet<O>();

    // ~ Constructors ----------------------------------------------------------------------------------------------

    public SetMatcherEditor(final Mode mode, final Function<E, O> function) {
        this.function = checkNotNull(function);
        this.setMode(mode);
    }

    public SetMatcherEditor(final Function<E, O> function) {
        this(Mode.EMPTY_MATCHES_NONE, function);
    }

    // ~ Methods ---------------------------------------------------------------------------------------------------

    public void setMode(Mode mode) {
        this.mode = checkNotNull(mode);
        if (this.mode == Mode.EMPTY_MATCHES_ALL) {
            this.fireMatchAll();
        } else {
            this.fireMatchNone();
        }
    }

    public void setMatchSet(final Set<O> newSet) {
        Set<O> oldSet = this.set;
        this.set = newSet;

        if (oldSet.equals(this.set)) {
            L.fine("new set equals old -> no change to filter");

        } else if (this.set.isEmpty()) {
            if (this.mode == Mode.EMPTY_MATCHES_ALL) {
                L.fine("empty set -> firing matchAll");
                this.fireMatchAll();

            } else {
                L.fine("empty set -> firing matchNone");
                this.fireMatchNone();
            }

        } else if (oldSet.isEmpty()) {
            L.fine("old set was empty, new set is not -> firing change");
            this.fireChanged(new SetMatcher<E, O>(this.set, this.function));

        } else if (oldSet.containsAll(this.set)) {
            L.fine("old set contains new set -> firing constrained");
            this.fireConstrained(new SetMatcher<E, O>(this.set, this.function));

        } else if (this.set.containsAll(oldSet)) {
            L.fine("new set contains old set -> firing relaxed");
            this.fireRelaxed(new SetMatcher<E, O>(this.set, this.function));

        } else {
            L.fine("firing change");
            this.fireChanged(new SetMatcher<E, O>(this.set, this.function));
        }
    }

    // ~ Enumerations ----------------------------------------------------------------------------------------------

    public enum Mode {
        EMPTY_MATCHES_NONE, EMPTY_MATCHES_ALL
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
