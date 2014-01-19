/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;

/**
 * A comparator that reverses the sequence of a source comparator.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ReverseComparator<T> implements Comparator<T> {

    /** the normal comparator to flip */
    private Comparator<T> source;

    /**
     * Create a new reverse comparator that reverses the sequence
     * of the specified comparator.
     */
    public ReverseComparator(Comparator<T> source) {
        this.source = source;
    }

    /**
     * Compares the specified objects and flips the result.
     */
    @Override
    public int compare(T alpha, T beta) {
        return source.compare(beta, alpha);
    }

    /**
     * Retrieves the source {@link Comparator} for this ReverseComparator
     */
    public Comparator<T> getSourceComparator() {
        return source;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final ReverseComparator that = (ReverseComparator) o;

        if(!source.equals(that.source)) return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return source.hashCode();
    }
}