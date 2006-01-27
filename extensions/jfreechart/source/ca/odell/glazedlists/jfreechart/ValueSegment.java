package ca.odell.glazedlists.jfreechart;

/**
 * A ValueSegment represents a stable value within a segment of
 * {@link Comparable} values. For example, instances could represent the
 * average temperature each hour of the day. The start and end values of the
 * {@link ValueSegment} would be the Timestamps of the start and end of the
 * hour and the value would be the Float representing the average temperature
 * for that hour.
 *
 * <p><strong><font color="#FF0000">Note:</font></strong> {@link ValueSegment}s
 * must be immutable objects. Callers are strongly encouraged to construct
 * {@link ValueSegment}s with defensive copies of the start, end, and value
 * arguments if they are not themselves immutable.
 *
 * @author James Lemieux
 */
public class ValueSegment<T extends Comparable, V extends Comparable> implements Comparable<ValueSegment> {

    /** The value marking the start of the segment. */
    private final T start;

    /** The value marking the end of the segment. */
    private final T end;

    /** The value to report for the segment described. */
    private final V value;

    /**
     * Create a ValueSegment indicating the <code>value</code> exists between
     * the <code>start</code> and <code>end</code> on some continuum of
     * {@link Comparable} objects.
     *
     * @param start the beginning of the segment
     * @param end the end of the segment
     * @param value the value observed between <code>start</code> and
     *      <code>end</code>
     * @throws IllegalArgumentException if <code>start</code> or
     *      <code>end</code> is <code>null</code>
     */
    public ValueSegment(T start, T end, V value) {
        if (start == null)
            throw new IllegalArgumentException("start may not be null");

        if (end == null)
            throw new IllegalArgumentException("end may not be null");

        this.start = start;
        this.end = end;
        this.value = value;
    }

    /**
     * Returns the value marking the start of this segment.
     */
    public T getStart() {
        return start;
    }

    /**
     * Returns the value marking the end of this segment.
     */
    public T getEnd() {
        return end;
    }

    /**
     * Returns the value observed within this segment.
     */
    public V getValue() {
        return value;
    }

    /**
     * ValueSegments are compared by value, then by segment start, then by
     * segment end.
     */
    public int compareTo(ValueSegment o) {
        // 1. compare by value
        final int valueComparison = value.compareTo(o.value);
        if (valueComparison != 0)
            return valueComparison;

        // 2. compare by segment start
        final int startComparison = start.compareTo(o.start);
        if (startComparison != 0)
            return startComparison;

        // 3. compare by segment end
        return end.compareTo(o.end);
    }
}