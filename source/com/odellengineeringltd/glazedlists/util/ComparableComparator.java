/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for specifying a sorting algorithm
import java.util.Comparator;


/**
 * A trivial comparator that requires that compared objects implement
 * the comparable interface.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ComparableComparator implements Comparator {
    
    /**
     * Compares object alpha to object beta by casting object one
     * to Comparable, and calling its compareTo method.
     */
    public int compare(Object alpha, Object beta) {
        Comparable alphaComparable = (Comparable)alpha;
        return alphaComparable.compareTo(beta);
    }
}
