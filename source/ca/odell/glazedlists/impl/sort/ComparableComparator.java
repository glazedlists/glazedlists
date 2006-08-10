/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;

/**
 * A trivial comparator that requires that compared objects implement
 * the comparable interface.
 *
 * <p>When this finds <code>null</code> objects, it orders them before all
 * other objects.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ComparableComparator implements Comparator<Comparable> {

    /**
     * Compares object alpha to object beta by casting object one
     * to Comparable, and calling its compareTo method.
     */
    public int compare(Comparable alpha, Comparable beta) {
        // compare using Comparable
        if(alpha != null && beta != null) {
            return alpha.compareTo(beta);
        }

        // compare nulls
        if(alpha == null) {
            if(beta == null) return 0;
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * This is equal to another comparator if it is a ComparableComparable.
     */
    public boolean equals(Object other) {
        return (other instanceof ComparableComparator);
    }
}