/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util;

// for specifying a sorting algorithm
import java.util.Comparator;


/**
 * A comparator that reverses the sequence of a source comparator.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ReverseComparator implements Comparator {

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
     * Retrieves the source <code>Comparator</code> for this ReverseComparator
     */
    public Comparator getSourceComparator() {
        return source;
    }
}
