/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for specifying a sorting algorithm
import java.util.Comparator;


/**
 * A comparator that reverses the sequence of a source comparator.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ReverseComparator implements Comparator {
    
    /** the normal comparator to flip */
    private Comparator normal;
    
    /**
     * Create a new reverse comparator that reverses the sequence
     * of the specified comparator.
     */
    public ReverseComparator(Comparator normal) {
        this.normal = normal;
    }
    
    /**
     * Compares the specified objects and flips the result.
     */
    public int compare(Object alpha, Object beta) {
        return normal.compare(beta, alpha);
    }
}
