/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

/**
 * Defines the contract for a {@link ThresholdEvaluator} to be used to provide
 * an absolute value for a given {@link Object} in a {@link ThresholdList}.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface ThresholdEvaluator {

    /**
     * Returns an integer value for an {@link Object} to be used to
     * compare that object against a threshold.  This value is
     * not relative to any other object unlike a {@link java.util.Comparator}.
     */
    public int evaluate(Object object);

}