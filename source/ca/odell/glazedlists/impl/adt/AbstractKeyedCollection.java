package ca.odell.glazedlists.impl.adt;

import java.util.*;

/**
 * Abstract implementation of {@link KeyedCollection} that stores values as keys
 * in a map, with positions as the values.
 *
 * @author jplemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class AbstractKeyedCollection<P, V> implements KeyedCollection<P, V> {

    private final Map<V, Object> values;
    private final Comparator<P> positionComparator;
    private P first, last;

    public AbstractKeyedCollection(Comparator<P> positionComparator,
            Map mapWithValuesAsKeys) {
        this.positionComparator = positionComparator;
        this.values = mapWithValuesAsKeys;
    }

    public final void insert(P position, V value) {
        Object previousPositions = values.get(value);

        if (previousPositions == null) {
            values.put(value,  position);

        } else if (previousPositions instanceof SortedSet) {
            SortedSet<P> allPositions = (SortedSet)previousPositions;
            allPositions.add(position);

        } else {
            SortedSet<P> allPositions = new TreeSet<P>(positionComparator);
            allPositions.add((P)previousPositions);
            allPositions.add(position);
            values.put(value, allPositions);
        }

        if (first == null || lessThan(position, first)) {
            first = position;
        }

        if (last == null || greaterThan(position, last)) {
            last = position;
        }
    }

    public final P find(P min, P max, V value) {
        if (positionComparator.compare(min, max) > 0)
            throw new IllegalArgumentException("min " + min + " > max " + max);

        Object positionsAsSingleOrSet = values.get(value);

        if (positionsAsSingleOrSet == null) {
            return null;

        } else if (positionsAsSingleOrSet instanceof SortedSet) {
            SortedSet<P> positions = (SortedSet<P>)positionsAsSingleOrSet;
            SortedSet<P> positionsInRange = positions.subSet(min, max);
            return positionsInRange.isEmpty()
                    ? null
                    : positionsInRange.iterator().next();

        } else {
            P position = (P)positionsAsSingleOrSet;
            if (!lessThan(position, min) && lessThan(position, max)) {
                return position;
            }
        }


        return null;
    }

    final boolean lessThan(P a, P b) {
        return positionComparator.compare(a, b) < 0;
    }
    final boolean greaterThan(P a, P b) {
        return positionComparator.compare(a, b) > 0;
    }

    public final P last() { return last; }
    public final P first() { return first; }
}