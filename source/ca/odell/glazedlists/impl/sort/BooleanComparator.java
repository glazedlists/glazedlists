/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;

/**
 * A {@link Comparator} that compares two {@link Boolean} objects such that
 * the sort order will be:
 * null, Boolean.FALSE, Boolean.TRUE.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
 public final class BooleanComparator implements Comparator {
    /**
     * Compares two Boolean objects using the following sort order:
     * null, Boolean.FALSE, Boolean.TRUE;
     */
    public int compare(Object alpha, Object beta) {
        // inspect the value of alpha
        boolean alphaValue;
        if(alpha != null) alphaValue = ((Boolean)alpha).booleanValue();

        // equal comparison of nulls
        else if(beta == null) return 0;

        // null then boolean
        else return -1;


        // inspect the value of beta
        boolean betaValue;
        if(beta != null) betaValue = ((Boolean)beta).booleanValue();

        // boolean then null
        else return 1;


        // equal comparison of values
        if(alphaValue == betaValue) return 0;

        // true then false
        else if(alphaValue && !betaValue) return 1;

        // false then true
        else return -1;
    }

    /**
     * This is equal to another comparator if it is a BooleanComparator.
     */
    public boolean equals(Object other) {
        return (other instanceof BooleanComparator);
    }
}