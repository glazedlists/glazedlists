/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

/**
 * The default implementation of the {@link ValueSegment} interface.
 *
 * <p><strong><font color="#FF0000">Note:</font></strong>
 * {@link DefaultValueSegment}s must be immutable objects. Callers are strongly
 * encouraged to construct {@link DefaultValueSegment}s with defensive copies
 * of the start, end, and value arguments if they are not themselves immutable.
 *
 * @author James Lemieux
 */
public class DefaultValueSegment<T extends Comparable, V extends Comparable> implements ValueSegment<T,V> {

    /** The value marking the start of the segment. */
    private final T start;

    /** The value marking the end of the segment. */
    private final T end;

    /** The value to report for the segment described. */
    private final V value;

    /**
     * Create a DefaultValueSegment indicating the <code>value</code> exists
     * between the <code>start</code> and <code>end</code> on some continuum of
     * {@link Comparable} objects.
     *
     * @param start the beginning of the segment
     * @param end the end of the segment
     * @param value the value observed between <code>start</code> and
     *      <code>end</code>
     * @throws IllegalArgumentException if <code>start</code> or
     *      <code>end</code> is <code>null</code>
     */
    public DefaultValueSegment(T start, T end, V value) {
        if (start == null)
            throw new IllegalArgumentException("start may not be null");

        if (end == null)
            throw new IllegalArgumentException("end may not be null");

        this.start = start;
        this.end = end;
        this.value = value;
    }

    /** {@inheritDoc} */
    public T getStart() {
        return start;
    }

    /** {@inheritDoc} */
    public T getEnd() {
        return end;
    }

    /** {@inheritDoc} */
    public V getValue() {
        return value;
    }

    /**
     * DefaultValueSegments are compared by value, then by segment start,
     * then by segment end.
     */
    public int compareTo(ValueSegment<T,V> o) {
        // 1. compare by value
        final int valueComparison = value.compareTo(o.getValue());
        if (valueComparison != 0)
            return valueComparison;

        // 2. compare by segment start
        final int startComparison = start.compareTo(o.getStart());
        if (startComparison != 0)
            return startComparison;

        // 3. compare by segment end
        return end.compareTo(o.getEnd());
    }
}