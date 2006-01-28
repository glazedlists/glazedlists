/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
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
 * must be immutable objects.
 *
 * @author James Lemieux
 */
public interface ValueSegment<T extends Comparable, V extends Comparable> extends Comparable<ValueSegment<T,V>> {
    /**
     * Returns the value marking the start of this segment.
     */
    public T getStart();

    /**
     * Returns the value marking the end of this segment.
     */
    public T getEnd();

    /**
     * Returns the value observed within this segment.
     */
    public V getValue();
}