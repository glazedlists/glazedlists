/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.*;

/**
 * A comparator chain compares objects using a list of comparators. The
 * first comparison where the objects differ is returned.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class ComparatorChain<T> implements Comparator<T> {

    /** the comparators to execute in sequence */
    private List<Comparator<T>> comparators;

    /**
     * Creates a comparator chain that views the specified comparators in
     * sequence.
     *
     * @param comparators a list of classes implementing Comparator. It is
     *      an error to modify the specified list after using it as an
     *      argument to comparator chain.
     */
    public ComparatorChain(List<Comparator<T>> comparators) {
        this.comparators = comparators;
    }

    /**
     * Compares the two objects with each comparator in sequence.
     */
    public int compare(T alpha, T beta) {
        for(Iterator<Comparator<T>> i = comparators.iterator(); i.hasNext(); ) {
            Comparator<T> currentComparator = i.next();
            int compareResult = currentComparator.compare(alpha, beta);
            if(compareResult != 0) return compareResult;
        }
        return 0;
    }

    /**
     * Retrieves a {@link List} of the {@link Comparator}s in this
     * <code>ComparatorChain</code>.
     */
    public List<Comparator<T>> getComparators() {
        return comparators;
    }

    /**
     * A list of comparators are equal only if the lists are equal.
     */
    public boolean equals(Object other) {
        if(!(other instanceof ComparatorChain)) return false;
        ComparatorChain chainOther = (ComparatorChain)other;
        return comparators.equals(chainOther.comparators);
    }
}