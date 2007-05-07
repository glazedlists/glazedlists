package ca.odell.glazedlists.impl.adt;

import java.util.*;

public class KeyedCollectionForComparableValues<T> implements KeyedCollection<T> {

    private final TreeMap<T, SortedSet<Position>> values;
    private Position first, last;

    public KeyedCollectionForComparableValues(Comparator<T> valueComparator) {
        values = new TreeMap<T, SortedSet<Position>>(valueComparator);
    }

    public void insert(Position position, T value) {
        SortedSet<Position> positions = values.get(value);

        if (positions == null) {
            positions = new TreeSet<Position>();
            values.put(value, positions);
        }

        if (first == null || first.compareTo(position) > 0)
            first = position;

        if (last == null || last.compareTo(position) < 0)
            last = position;

        positions.add(position);
    }

    public Position find(Position min, Position max, T value) {
        if (min.compareTo(max) > 0)
            throw new IllegalArgumentException("min " + min + " > max " + max);
        
        SortedSet<Position> positions = values.get(value);

        if (positions == null)
            return null;

        for (Iterator<Position> positionIter = positions.iterator(); positionIter.hasNext();) {
            Position position = positionIter.next();

            if (position.compareTo(max) >= 0)
                break;

            if (position.compareTo(min) >= 0)
                return position;
        }

        return null;
    }

    public Position last() { return last; }
    public Position first() { return first; }
}