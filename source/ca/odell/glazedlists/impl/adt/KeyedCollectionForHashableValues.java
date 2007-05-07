package ca.odell.glazedlists.impl.adt;

import java.util.Comparator;
import java.util.HashMap;

/**
 * @author jessewilson
 */
public class KeyedCollectionForHashableValues<P,V> extends AbstractKeyedCollection<P,V> {
    public KeyedCollectionForHashableValues(Comparator<P> positionComparator) {
        super(positionComparator, new HashMap());
    }
}
