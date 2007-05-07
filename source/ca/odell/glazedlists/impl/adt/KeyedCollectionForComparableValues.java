package ca.odell.glazedlists.impl.adt;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * @author jessewilson
 */
public class KeyedCollectionForComparableValues<P,V> extends AbstractKeyedCollection<P,V> {
    public KeyedCollectionForComparableValues(Comparator<P> positionComparator, Comparator<V> valueComparator) {
        super(positionComparator, new TreeMap(valueComparator));
    }
}
