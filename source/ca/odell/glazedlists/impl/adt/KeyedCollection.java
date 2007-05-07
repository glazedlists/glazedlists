package ca.odell.glazedlists.impl.adt;

/**
 * @typeparam P a position type for indexing values
 * @typeparam V a value type
 *
 * @author jessewilson
 */
public interface KeyedCollection<P, V> {

    /**
     * Inserts the specified value at the specified position.
     */
    void insert(P position, V value);

    /**
     * Returns the largest position that has been inserted into this
     * collection.
     */
    P last();

    /**
     * Returns the smallest position that has been inserted into this
     * collection.
     */
    P first();

    /**
     * Returns the first position of the specified value.
     *
     * @param min the returned value will be greater than or equal to this.
     * @param max the returned position will be less than this.
     * @returns the position, or <code>null</code> if no such value exists in
     *      the requested range.
     */
    P find(P min, P max, V value);
}
