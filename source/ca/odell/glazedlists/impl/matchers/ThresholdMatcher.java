/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 7:39:25 AM
 */
package ca.odell.glazedlists.impl.matchers;

import java.util.Comparator;


/**
 * A {@link ca.odell.glazedlists.Matcher} that filters elements based on whether they are
 * greater than or less than a threshold. The implementation is based on elements
 * implementing {@link Comparable} unless the constructor specifies a {@link
 * java.util.Comparator}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class ThresholdMatcher extends AbstractMatcher {
    public static MatchOperation GREATER_THAN = new MatchOperation(">");
    public static MatchOperation GREATER_THAN_OR_EQUAL = new MatchOperation(">=");
    public static MatchOperation LESS_THAN = new MatchOperation("<");
    public static MatchOperation LESS_THAN_OR_EQUAL = new MatchOperation("<=");
    public static MatchOperation EQUAL = new MatchOperation("==");
    public static MatchOperation NOT_EQUAL = new MatchOperation("!=");

    // Changes for MatchOperation.changeType(...)
    private static final int CHANGE_NONE = 0;
    private static final int CHANGE_CONSTRAINED = 1;
    private static final int CHANGE_RELAXED = 2;
    private static final int CHANGE_UNKNOWN = 3;

    // Operations for MatchOperation
    private static final int OP_GREATER = 0;
    private static final int OP_GREATER_EQUAL = 1;
    private static final int OP_LESS = 2;
    private static final int OP_LESS_EQUAL = 3;
    private static final int OP_EQUAL = 4;
    private static final int OP_NOT_EQUAL = 5;


    private volatile Object threshold;
    private volatile MatchOperation operation;
    private volatile Comparator comparator;
    private volatile boolean match_on_no_threshold;

    /**
     * Construct an instance that will require elements to be greater than the threshold
     * (which is not initially set) and relies on the thresold object and elements in the
     * list implementing {@link Comparable}.
     */
    public ThresholdMatcher() {
        this(null, null, null, true);
    }

    /**
     * Construct an instance that will require elements to be greater than the given
     * threshold and relies on the thresold object and elements in the list implementing
     * {@link Comparable}.
     *
     * @param threshold The initial threshold, or null if none.
     */
    public ThresholdMatcher(Comparable threshold) {
        this(threshold, null, null, true);
    }

    /**
     * Construct an instance that will require elements to be greater than the given
     * threshold and relies on the thresold object and elements in the list implementing
     * {@link Comparable}.
     *
     * @param threshold The initial threshold, or null if none.
     * @param operation The operation to determine what relation list elements should have
     *                  to the threshold in order to match (i.e., be visible). Specifying
     *                  null will use {@link #GREATER_THAN}.
     */
    public ThresholdMatcher(Comparable threshold, MatchOperation operation) {
        this(threshold, operation, null, true);
    }

    /**
     * Construct an instance that causes elements to match if there is no threshold set.
     *
     * @param threshold  The initial threshold, or null if none.
     * @param operation  The operation to determine what relation list elements should
     *                   have to the threshold in order to match (i.e., be visible).
     *                   Specifying null will use {@link #GREATER_THAN}.
     * @param comparator Determines how objects compare. If null, the threshold object and
     *                   list elements must implement {@link Comparable}.
     */
    public ThresholdMatcher(Object threshold, MatchOperation operation,
        Comparator comparator) {

        this(threshold, operation, comparator, true);
    }

    /**
     * Construct an instance.
     *
     * @param threshold             The initial threshold, or null if none.
     * @param operation             The operation to determine what relation list elements
     *                              should have to the threshold in order to match (i.e.,
     *                              be visible). Specifying null will use {@link
     *                              #GREATER_THAN}.
     * @param comparator            Determines how objects compare. If null, the threshold
     *                              object and list elements must implement {@link
     *                              Comparable}.
     * @param match_on_no_threshold Determines whether a null threshold causes all
     *                              elements to match (true) or be hidden (false).
     */
    public ThresholdMatcher(Object threshold, MatchOperation operation,
        Comparator comparator, boolean match_on_no_threshold) {

        // Defaults
        if (operation == null) operation = GREATER_THAN;
        if (comparator == null) comparator = new DefaultComparator();

        this.threshold = threshold;
        this.operation = operation;
        this.comparator = comparator;
        this.match_on_no_threshold = match_on_no_threshold;
    }


    /**
     * {@inheritDoc
     */
    public boolean matches(Object item) {
        if (threshold == null) return match_on_no_threshold;

        return operation.applies(item, threshold, comparator);
    }


    /**
     * Update the threshold.
     */
    public synchronized void setThreshold(Object threshold) {
        Object old_threshold = this.threshold;
        this.threshold = threshold;

        if (threshold == null) {
            // If there didn't used to be a threshold, it's a noop
            if (old_threshold == null) return;

            // Either cleared filter or restricted it (all the way!)
            if (match_on_no_threshold)
                fireCleared();
            else
                fireConstrained();
        } else if (old_threshold == null) {
            if (match_on_no_threshold)
                fireConstrained();
            else
                fireRelaxed();
        } else {
            // The operation can be smart about how the threshold changed
            int change = operation.changeType(old_threshold, threshold, comparator);
            fireChange(change);
        }
    }

    public Object getThreshold() {
        return threshold;
    }


    /**
     * Update the operation used to determine what relation list elements should have to
     * the threshold in order to match (i.e., be visible). Must be non-null.
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

        MatchOperation old_operation = this.operation;
        this.operation = operation;

        int change = operation.changeType(old_operation);
        fireChange(change);
    }

    public MatchOperation getMatchOperation() {
        return operation;
    }


    /**
     * Update the comparator. Setting to null will require that thresholds and elements in
     * the list implement {@link Comparable}.
     */
    public synchronized void setComparator(Comparator comparator) {
        if (comparator == null) comparator = new DefaultComparator();

        this.comparator = comparator;

        fireChanged();
    }

    public Comparator getComparator() {
        return comparator;
    }


    /**
     * Update whether or not an empty threshold indicates that all elements should match
     * (true) or be hidden (false).
     */
    public synchronized void setMatchOnNoThreshold(boolean match_on_no_threshold) {
        this.match_on_no_threshold = match_on_no_threshold;

        if (threshold == null) fireChanged();
    }

    public boolean getMatchOnThreshold() {
        return match_on_no_threshold;
    }


    /**
     * Fire the change for the given op code.
     */
    private void fireChange(int op_code) {
        if (op_code == CHANGE_NONE)
            return;
        else if (op_code == CHANGE_CONSTRAINED)
            fireConstrained();
        else if (op_code == CHANGE_RELAXED)
            fireRelaxed();
        else
            fireChanged();
    }


    // TODO: This class must exist somewhere already, but don't have javadocs handy
    private static final class DefaultComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    }


    /**
     * Enumeration class that allows the relating to the threshold that will match items.
     */
    private static final class MatchOperation {
        private final int operation;

        private MatchOperation(String operation) {
            if (operation.equals(">"))
                this.operation = OP_GREATER;
            else if (operation.equals(">="))
                this.operation = OP_GREATER_EQUAL;
            else if (operation.equals("<"))
                this.operation = OP_LESS;
            else if (operation.equals("<="))
                this.operation = OP_LESS_EQUAL;
            else if (operation.equals("=="))
                this.operation = OP_EQUAL;
            else if (operation.equals("!="))
                this.operation = OP_NOT_EQUAL;
            else
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }


        boolean applies(Object threshold, Object item, Comparator comparator) {
            int value = comparator.compare(threshold, item);

            switch(operation) {
                case OP_GREATER:             // >
                    return value > 0;
                case OP_GREATER_EQUAL:       // >=
                    return value >= 0;
                case OP_LESS:                // <
                    return value < 0;
                case OP_LESS_EQUAL:          // <=
                    return value <= 0;
                case OP_EQUAL:               // ==
                    return value == 0;
                case OP_NOT_EQUAL:           // !=
                    return value != 0;
            }

            // Umm, shouldn't get here!?!?  (See validity check in constructor)
            throw new IllegalStateException("Unknown state: invalid operation: " +
                operation);
        }


        /**
         * Indicates the type of change for the given threshold change.
         */
        int changeType(Object old_threshold, Object new_threshold, Comparator comparator) {
            int value = comparator.compare(old_threshold, new_threshold);

            // No change
            if (value == 0) return CHANGE_NONE;

            if (value > 0) {
                if (operation == OP_GREATER || operation == OP_GREATER_EQUAL) {   // > or >=
                    return CHANGE_RELAXED;
                } else if (operation == OP_LESS || operation == OP_LESS_EQUAL) {    // < or <=
                    return CHANGE_CONSTRAINED;
                }
                // == or !=   fall through...
            } else {
                if (operation == OP_GREATER || operation == OP_GREATER_EQUAL) {   // > or >=
                    return CHANGE_CONSTRAINED;
                } else if (operation == OP_LESS || operation == OP_LESS_EQUAL) {    // < or <=
                    return CHANGE_RELAXED;
                }
                // == or !=   fall through...
            }

            return CHANGE_UNKNOWN;
        }


        /**
         * Indicates the type of change for the given operation change.
         */
        int changeType(MatchOperation old_operation) {
            int old_op = old_operation.operation;
            int new_op = operation;

            if (old_op == new_op) return CHANGE_NONE;

            if (old_op == OP_GREATER && new_op == OP_GREATER_EQUAL) return CHANGE_RELAXED;
            if (old_op == OP_LESS && new_op == OP_LESS_EQUAL) return CHANGE_RELAXED;

            if (old_op == OP_GREATER_EQUAL && new_op == OP_GREATER) return CHANGE_CONSTRAINED;
            if (old_op == OP_LESS_EQUAL && new_op == OP_LESS) return CHANGE_CONSTRAINED;

            return CHANGE_UNKNOWN;
        }
    }
}
