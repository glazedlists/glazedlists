/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * A comparator chain compares objects using a list of Comparators. The
 * first comparison where the objects differ is returned.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class ComparatorChain<T> implements Comparator<T> {

    /** the comparators to execute in sequence */
    private final Comparator<T>[] comparators;

    /**
     * Creates a comparator chain that evaluates the specified comparators in
     * sequence. A defensive copy of the
     *
     * @param comparators a list of objects implementing {@link Comparator}
     */
    public ComparatorChain(List<Comparator<T>> comparators) {
        this.comparators = comparators.toArray(new Comparator[comparators.size()]);
    }

    /**
     * Compares the two objects with each comparator in sequence.
     */
    @Override
    public int compare(T alpha, T beta) {
        for (int i = 0; i < comparators.length; i++) {
            int compareResult = comparators[i].compare(alpha, beta);
            if(compareResult != 0) return compareResult;
        }
        return 0;
    }

    /**
     * Retrieves the {@link Comparator}s composing this
     * <code>ComparatorChain</code>.
     */
    public Comparator<T>[] getComparators() {
        return comparators;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        final ComparatorChain that = (ComparatorChain) o;

        if(!Arrays.equals(comparators, that.comparators)) return false;

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return 0;
    }
}