/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
/*                                                          StarLight Systems */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.GlazedLists;

import java.util.Comparator;

/**
 * A {@link MatcherEditor} that filters elements based on whether they are greater than or
 * less than a threshold. The implementation is based on elements implementing {@link
 * Comparable} unless the constructor specifies a {@link Comparator}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcherEditor<E> extends AbstractMatcherEditor<E> {

	public static final MatchOperation GREATER_THAN = new MatchOperation(1, false);
	public static final MatchOperation GREATER_THAN_OR_EQUAL = new MatchOperation(1, true);
	public static final MatchOperation LESS_THAN = new MatchOperation(-1, false);
	public static final MatchOperation LESS_THAN_OR_EQUAL = new MatchOperation(-1, true);
	public static final MatchOperation EQUAL = new MatchOperation(0, true);
	public static final MatchOperation NOT_EQUAL = new MatchOperation(0, false);

    private MatchOperation currentMatcher = null;

    private Comparator<E> comparator = null;
    private MatchOperation operation = null;
    private E threshold = null;

	/**
	 * Construct an instance that will require elements to be greater than the threshold
	 * (which is not initially set) and relies on the thresold object and elements in the
	 * list implementing {@link Comparable}.
	 */
	public ThresholdMatcherEditor() {
		this(null, null, null);
	}

	/**
	 * Construct an instance that will require elements to be greater than the given
	 * threshold and relies on the thresold object and elements in the list implementing
	 * {@link Comparable}.
	 *
	 * @param threshold The initial threshold, or null if none.
	 */
	public ThresholdMatcherEditor(E threshold) {
		this(threshold, null, null);
	}

	/**
	 * Construct an instance that will require elements to be greater than the given
	 * threshold and relies on the thresold object and elements in the list implementing
	 * {@link Comparable}.
	 *
	 * @param threshold The initial threshold, or null if none.
	 * @param operation The operation to determine what relation list elements should have to
	 *                  the threshold in order to match (i.e., be visible). Specifying null
	 *                  will use {@link #GREATER_THAN}.
	 */
	public ThresholdMatcherEditor(E threshold, MatchOperation operation) {
		this(threshold, operation, null);
	}

	/**
	 * Construct an instance.
	 *
	 * @param threshold  The initial threshold, or null if none.
	 * @param operation  The operation to determine what relation list elements should have
	 *                   to the threshold in order to match (i.e., be visible). Specifying
	 *                   null will use {@link #GREATER_THAN}.
	 * @param comparator Determines how objects compare. If null, the threshold object and
	 *                   list elements must implement {@link Comparable}.
	 */
	public ThresholdMatcherEditor(E threshold, MatchOperation operation, Comparator<E> comparator) {
		// Defaults
		if (operation == null) operation = GREATER_THAN;
		if (comparator == null) comparator = (Comparator<E>) GlazedLists.comparableComparator();

		this.operation = operation;
		this.comparator = comparator;
        this.threshold = threshold;

        // if this is our first matcher, it's automatically a constrain
        currentMatcher = operation.instance(comparator, threshold);
        fireConstrained(currentMatcher);
	}


	/**
	 * Update the threshold used to determine what is matched by the list. This coupled
	 * with the {@link #setMatchOperation match operation} determines what's matched.
	 *
	 * @param threshold		The threshold, or null to match everything.
	 */
	public synchronized void setThreshold(E threshold) {
        this.threshold = threshold;
        rebuildMatcher();
	}
	/**
	 * See {@link #getThreshold()}.
	 */
	public E getThreshold() {
		return threshold;
	}


	/**
	 * Update the operation used to determine what relation list elements should
     * have to the threshold in order to match (i.e., be visible). Must be non-null.
	 *
	 * @see #GREATER_THAN
	 * @see #GREATER_THAN_OR_EQUAL
	 * @see #LESS_THAN
	 * @see #LESS_THAN_OR_EQUAL
	 * @see #EQUAL
	 * @see #NOT_EQUAL
	 */
	public synchronized void setMatchOperation(MatchOperation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("Operation cannot be null");
		}

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
	public synchronized void setComparator(Comparator<E> comparator) {
		if (comparator == null) comparator = (Comparator<E>) GlazedLists.comparableComparator();

		this.comparator = comparator;
        rebuildMatcher();
	}
	/**
	 * See {@link #setComparator}.
	 */
	public Comparator<E> getComparator() {
		return comparator;
	}


	/**
	 * {@inheritDoc}
	 */
	private void rebuildMatcher() {
        MatchOperation newMatcher = operation.instance(comparator, threshold);

        // otherwise test how the matchers relate
        boolean moreStrict = newMatcher.isMoreStrict(currentMatcher);
        boolean lessStrict = currentMatcher.isMoreStrict(newMatcher);

        // if they're equal we're done and we won't change the matcher
        if(!moreStrict && !lessStrict) {
            return;
        }

        // otherwise, fire the appropriate event
        this.currentMatcher = newMatcher;
        if(moreStrict && lessStrict) {
            fireChanged(this.currentMatcher);
        } else if(moreStrict) {
            fireConstrained(this.currentMatcher);
        } else if(lessStrict) {
            fireRelaxed(this.currentMatcher);
        }
	}

    /**
     * A {@link MatchOperation} serves as both a {@link Matcher} in and of itself
     * and as an enumerated type representing its type as an operation.
     */
    private static class MatchOperation implements Matcher {

        /** the comparator to compare values against */
        protected final Comparator comparator;
        /** the pivot value to compare with */
        protected final Object threshold;
        /** either 1 for greater, 0 for equal, or -1 for less than */
        private final int polarity;
        /** either true for equal or false for not equal */
        private final boolean inclusive;

        private MatchOperation(Comparator comparator, Object threshold, int polarity, boolean inclusive) {
            this.comparator = comparator;
            this.threshold = threshold;
            this.polarity = polarity;
            this.inclusive = inclusive;
        }
        private MatchOperation(int polarity, boolean inclusive) {
            this(null, null, polarity, inclusive);
        }

        /**
         * Factory method to create a {@link MatchOperation} of the same type
         * as this {@link MatchOperation}.
         */
        private MatchOperation instance(Comparator comparator, Object threshold) {
            return new MatchOperation(comparator, threshold, this.polarity, this.inclusive);
        }

        /**
         * Compare this to another {@link MatchOperation}.
         *
         * @return true if there exists some Object i such that <code>this.matches(i)</code>
         *      is <code>false</code> when <code>other.matches(i)<code> is
         *      <code>true</code>. Two MatcherOperations can be mutually more
         *       strict than each other.
         */
        boolean isMoreStrict(MatchOperation other) {
            if(other.polarity != this.polarity) return true;
            if(other.comparator != this.comparator) return true;
            if(this.threshold == other.threshold) {
                if(polarity == 0) return this.inclusive != other.inclusive;
                else return (!this.inclusive && other.inclusive);
            } else {
                if(this.polarity == 0) return true;
                else if(!this.matches(other.threshold)) return true;
            }
            return false;
        }

        /** {@inheritDoc} */
        public boolean matches(Object item) {
            int compareResult = comparator.compare(item, this.threshold);
            // item equals threshold, match <=, == and >=
            if(compareResult == 0) return inclusive;
            // for == and !=, handle the case when the item is not equal to threshold
            if(polarity == 0) return !inclusive;
            // item is below threshold and match <, <= or item is above and match >, >=
            return ((compareResult < 0) == (polarity < 0));
        }
    }
}