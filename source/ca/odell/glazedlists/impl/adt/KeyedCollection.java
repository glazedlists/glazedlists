package ca.odell.glazedlists.impl.adt;

/**
 * @author jessewilson
 */
public interface KeyedCollection<T> {

    /**
     * Inserts the specified value at the
     * specified position.
     *
     * @returns the previous value at the
     *      specified position.
     */
    T insert(Position position, T value);

    /**
     * Returns the largest position that has
     * been inserted into this collection.
     */
    Position last();

    /**
     * Returns the smallest position that has
     * been inserted into this collection.
     */
    Position first();

    /**
     * Returns the first position that the specified
     * value is stored at.
     *
     * @param min the returned value will be greater
     *     than or equal to this.
     * @param max the returned position will be less
     *     than this.
     * @returns the position, or <code>null</code> if
     *     no such value exists in the requested range.
     */
    Position find(Position min, Position max, T value);

    interface Position extends Comparable<Position> {

    }
}
