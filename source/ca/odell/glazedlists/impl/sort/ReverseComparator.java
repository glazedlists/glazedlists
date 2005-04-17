/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;


/**
 * A comparator that reverses the sequence of a source comparator.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ReverseComparator implements Comparator {

    /** the normal comparator to flip */
    private Comparator source;

    /**
     * Create a new reverse comparator that reverses the sequence
     * of the specified comparator.
     */
    public ReverseComparator(Comparator source) {
        this.source = source;
    }

    /**
     * Compares the specified objects and flips the result.
     */
    public int compare(Object alpha, Object beta) {
        return source.compare(beta, alpha);
    }

    /**
     * Retrieves the source {@link Comparator} for this ReverseComparator
     */
    public Comparator getSourceComparator() {
        return source;
    }

    /**
     * This is equal to another comparator if and only if they both
     * are reverse comparators for equal source comparators.
     */
    public boolean equals(Object other) {
        if(!(other instanceof ReverseComparator)) return false;
        ReverseComparator reverseOther = (ReverseComparator)other;
        return source.equals(reverseOther.source);
    }
}
