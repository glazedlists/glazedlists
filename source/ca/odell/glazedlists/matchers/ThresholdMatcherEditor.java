/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.FunctionList;
import ca.odell.glazedlists.impl.GlazedListsImpl;

import java.util.Comparator;

/**
 * A {@link MatcherEditor} that filters elements based on whether they are
 * greater than or less than a threshold. The implementation is based on
 * elements implementing {@link Comparable} unless the constructor specifies
 * a {@link Comparator}.
 *
 * By default the elements themselves are compared with the threshold value,
 * however, an optional Function can be provided which can be used to extract
 * a value that is appropriate for comparison with the threshold. In this way,
 * ThreshholdMatcherEditor provides a level of indirection when locating the
 * exact value to compare for a given element.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcherEditor<E, T> extends AbstractMatcherEditor<E> {

	public static final MatchOperation GREATER_THAN = new MatchOperation(1, false);
	public static final MatchOperation GREATER_THAN_OR_EQUAL = new MatchOperation(1, true);
	public static final MatchOperation LESS_THAN = new MatchOperation(-1, false);
	public static final MatchOperation LESS_THAN_OR_EQUAL = new MatchOperation(-1, true);
	public static final MatchOperation EQUAL = new MatchOperation(0, true);
	public static final MatchOperation NOT_EQUAL = new MatchOperation(0, false);

    private MatchOperation currentMatcher;

    private Comparator<T> comparator;
    private MatchOperation operation;
    private T threshold;
    private FunctionList.Function<E, T> function;

	/**
	 * Construct an instance that will require elements to be greater than the
     * threshold (which is not initially set) and relies on the threshold
     * object and elements in the list implementing {@link Comparable}.
	 */
	public ThresholdMatcherEditor() {
		this(null);
	}

	/**
	 * Construct an instance that will require elements to be greater than the
     * given threshold and relies on the threshold object and elements in the
     * list implementing {@link Comparable}.
	 *
	 * @param threshold the initial threshold, or null if none.
	 */
	public ThresholdMatcherEditor(T threshold) {
		this(threshold, null);
	}

	/**
	 * Construct an instance that will require elements to be greater than the
     * given threshold and relies on the threshold object and elements in the
     * list implementing {@link Comparable}.
	 *
	 * @param threshold the initial threshold, or null if none.
	 * @param operation the operation to determine what relation list elements
     *      should have to the threshold in order to match (i.e., be visible).
     *      Specifying null will use {@link #GREATER_THAN}.
	 */
	public ThresholdMatcherEditor(T threshold, MatchOperation operation) {
		this(threshold, operation, null);
	}

    /**
	 * Construct an instance.
	 *
	 * @param threshold rhe initial threshold, or null if none.
	 * @param operation rhe operation to determine what relation list elements
     *      should have to the threshold in order to match (i.e., be visible).
     *      Specifying null will use {@link #GREATER_THAN}.
	 * @param comparator determines how objects compare. If null, the threshold
     *      object and list elements must implement {@link Comparable}.
	 */
	public ThresholdMatcherEditor(T threshold, MatchOperation operation, Comparator<T> comparator) {
		this(threshold, operation, comparator, null);
	}

	/**
	 * Construct an instance.
	 *
	 * @param threshold the initial threshold, or null if none.
	 * @param operation the operation to determine what relation list elements
     *      should have to the threshold in order to match (i.e., be visible).
     *      Specifying null will use {@link #GREATER_THAN}.
	 * @param comparator determines how objects compare with the threshold value.
     *      If null, the threshold object and list elements must implement
     *      {@link Comparable}.
     * @param function an optional Function which produces a value fit to be
     *      compared against the threshold. This argument is optional, and if
     *      it is <tt>null</tt>, the raw values will compared against the
     *      threshold.
	 */
	public ThresholdMatcherEditor(T threshold, MatchOperation operation, Comparator<T> comparator, FunctionList.Function<E, T> function) {
		if (operation == null) operation = GREATER_THAN;
		if (comparator == null) comparator = (Comparator<T>) GlazedLists.comparableComparator();
        if (function == null) function = (FunctionList.Function<E, T>) GlazedListsImpl.identityFunction();

        this.operation = operation;
		this.comparator = comparator;
        this.threshold = threshold;
        this.function = function;

        // if this is our first matcher, it's automatically a constrain
        currentMatcher = operation.instance(comparator, threshold, function);
	}

    /**
	 * Update the threshold used to determine what is matched by the list. This coupled
	 * with the {@link #setMatchOperation match operation} determines what's matched.
	 *
	 * @param threshold	The threshold, or null to match everything.
	 */
	public void setThreshold(T threshold) {
        this.threshold = threshold;
        rebuildMatcher();
	}
	/**
	 * See {@link #getThreshold()}.
	 */
	public T getThreshold() {
		return threshold;
	}

	/**
	 * Update the operation used to determine what relation list elements should
     * have to the threshold in order to match (i.e. be visible). Must be non-null.
	 *
	 * @see #GREATER_THAN
	 * @see #GREATER_THAN_OR_EQUAL
	 * @see #LESS_THAN
	 * @see #LESS_THAN_OR_EQUAL
	 * @see #EQUAL
	 * @see #NOT_EQUAL
	 */
	public void setMatchOperation(MatchOperation operation) {
		if (operation == null)
			throw new IllegalArgumentException("Operation cannot be null");

        this.operation = operation;
        rebuildMatcher();
	}
	/**
	 * See {@link #setMatchOperation}.
	 */
	public MatchOperation getMatchOperation() {
		return operation;
	}

	/**
	 * Update the comparator. Setting to null will require that thresholds and elements in
	 * the list implement {@link Comparable}.
	 */
	public void setComparator(Comparator<T> comparator) {
		if (comparator == null) comparator = (Comparator<T>) GlazedLists.comparableComparator();

		this.comparator = comparator;
        rebuildMatcher();
	}
	public Comparator<T> getComparator() { return comparator; }

	/** {@inheritDoc} */
	private void rebuildMatcher() {
        final MatchOperation newMatcher = operation.instance(comparator, threshold, function);

        // otherwise test how the matchers relate
        final boolean moreStrict = newMatcher.isMoreStrict(currentMatcher);
        final boolean lessStrict = currentMatcher.isMoreStrict(newMatcher);

        // if they're equal we're done and we won't change the matcher
        if (!moreStrict && !lessStrict)
            return;

        // otherwise, fire the appropriate event
        currentMatcher = newMatcher;
        if (moreStrict && lessStrict)
            fireChanged(currentMatcher);
        else if (moreStrict)
            fireConstrained(currentMatcher);
        else
            fireRelaxed(currentMatcher);
	}

    /**
     * A {@link MatchOperation} serves as both a {@link Matcher} in and of itself
     * and as an enumerated type representing its type as an operation.
     */
    private static class MatchOperation<E, T> implements Matcher<E> {

        /** the comparator to compare values against */
        protected final Comparator<T> comparator;
        /** the pivot value to compare with */
        protected final T threshold;
        /** either 1 for greater, 0 for equal, or -1 for less than */
        private final int polarity;
        /** either true for equal or false for not equal */
        private final boolean inclusive;
        /** a function which produces a comparable value for a given element */
        private final FunctionList.Function<E,T> function;

        private MatchOperation(Comparator<T> comparator, T threshold, int polarity, boolean inclusive, FunctionList.Function<E,T> function) {
            this.comparator = comparator;
            this.threshold = threshold;
            this.polarity = polarity;
            this.inclusive = inclusive;
            this.function = function;
        }
        private MatchOperation(int polarity, boolean inclusive) {
            this(null, null, polarity, inclusive, (FunctionList.Function<E,T>) GlazedListsImpl.identityFunction());
        }

        /**
         * Factory method to create a {@link MatchOperation} of the same type
         * as this {@link MatchOperation}.
         */
        private MatchOperation<E, T> instance(Comparator<T> comparator, T threshold, FunctionList.Function<E,T> function) {
            return new MatchOperation<E, T>(comparator, threshold, polarity, inclusive, function);
        }

        /**
         * Compare this to another {@link MatchOperation}.
         *
         * @return true if there exists some Object i such that <code>this.matches(i)</code>
         *      is <code>false</code> when <code>other.matches(i)<code> is
         *      <code>true</code>. Two MatcherOperations can be mutually more
         *       strict than each other.
         */
        boolean isMoreStrict(MatchOperation<E, T> other) {
            if(other.polarity != polarity) return true;
            if(other.comparator != comparator) return true;
            if(other.threshold == threshold) {
                if(polarity == 0) return other.inclusive != inclusive;
                else return (other.inclusive && !inclusive);
            } else {
                if(polarity == 0) return true;
                else if(!matchesThreshold(other.threshold)) return true;
            }
            return false;
        }

        /** {@inheritDoc} */
        public boolean matches(E item) {
            return matchesThreshold(function.evaluate(item));
        }

        public boolean matchesThreshold(T t) {
            // compare the extracted value with the threshold
            final int compareResult = comparator.compare(t, threshold);
            // item equals threshold, match <=, == and >=
            if(compareResult == 0) return inclusive;
            // for == and !=, handle the case when the item is not equal to threshold
            if(polarity == 0) return !inclusive;
            // item is below threshold and match <, <= or item is above and match >, >=
            return ((compareResult < 0) == (polarity < 0));
        }
    }
}