package ca.odell.glazedlists.impl.adt;

import java.util.*;

/**
 * A Collection that stores keys in a map, with positions as the values.
 *
 * @author jplemieux
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public final class KeyedCollection<P, V> {

    private final Map<V, Object> values;
    private final Comparator<P> positionComparator;
    private P first, last;

    public KeyedCollection(Comparator<P> positionComparator, Map<V, Object> mapWithValuesAsKeys) {
        if (!mapWithValuesAsKeys.isEmpty())
            throw new IllegalArgumentException("mapWithValuesAsKeys must be empty");

        this.positionComparator = positionComparator;
        this.values = mapWithValuesAsKeys;
    }

    /**
     * Inserts the specified value at the specified position.
     */
    public void insert(P position, V value) {
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

    /**
     * Returns the first position of the specified value.
     *
     * @param min the returned value will be greater than or equal to this.
     * @param max the returned position will be less than this.
     * @return the position, or <code>null</code> if no such value exists in
     *      the requested range.
     */
    public P find(P min, P max, V value) {
        if (positionComparator.compare(min, max) > 0)
            throw new IllegalArgumentException("min " + min + " > max " + max);

        Object positionsAsSingleOrSet = values.get(value);

        if (positionsAsSingleOrSet == null) {
            return null;

        } else if (positionsAsSingleOrSet instanceof SortedSet) {
            SortedSet<P> positions = (SortedSet<P>)positionsAsSingleOrSet;
            SortedSet<P> positionsInRange = positions.subSet(min, max);
            return positionsInRange.isEmpty() ? null : positionsInRange.iterator().next();

        } else {
            P position = (P)positionsAsSingleOrSet;
            if (!lessThan(position, min) && lessThan(position, max)) {
                return position;
            }
        }

        return null;
    }

    /**
     * Returns the largest position that has been inserted into this
     * collection.
     */
    public P last() { return last; }
    /**
     * Returns the smallest position that has been inserted into this
     * collection.
     */
    public P first() { return first; }

    private boolean lessThan(P a, P b) {
        return positionComparator.compare(a, b) < 0;
    }

    private boolean greaterThan(P a, P b) {
        return positionComparator.compare(a, b) > 0;
    }
}