/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

/**
 * Defines the contract for a ThresholdEvaluator to be used to provide an
 * absolute value for a given Object in a <code>ThresholdList</code>.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface ThresholdEvaluator {

    /**
     * Returns an integer value for an Object to be used to
     * compare that object against a threshold.  This value is
     * not relative to any other object unlike a <code>Comparator</code>.
     */
    public int evaluate(Object object);

}