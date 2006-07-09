/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;
import ca.odell.glazedlists.GlazedLists;

/**
 * A TreePair contains two tree structures which store the start and end
 * {@link Comparable} values of {@link ValueSegment} objects in their natural
 * orders. This allows for efficient answers to questions like
 *
 * <ul>
 *   <li> How many {@link ValueSegment}s exist after X?
 *   <li> How many {@link ValueSegment}s exist before Y?
 *   <li> How many {@link ValueSegment}s exist between X and Y?
 * </ul>
 *
 * @author James Lemieux
 */
final class TreePair<V extends Comparable> {
    /** The tree which orders the start indices of all ValueSegments. */
    private SimpleTree<V> start = new SimpleTree<V>(GlazedLists.comparableComparator());

    /** The tree which orders the start indices of all ValueSegments. */
    private SimpleTree<V> end = new SimpleTree<V>(GlazedLists.comparableComparator());

    /**
     * Inserts the given <code>segment</code> into the trees.
     */
    public void insert(ValueSegment<V,?> segment) {
        start.addInSortedOrder((byte)1, segment.getStart(), 1);
        end.addInSortedOrder((byte)1, segment.getEnd(), 1);
    }

    /**
     * Removes the <code>previousSegment</code> and inserts the
     * <code>newSegment</code> into the trees.
     */
    public void update(ValueSegment<V,?> previousSegment, ValueSegment<V,?> newSegment) {
        delete(previousSegment);
        insert(newSegment);
    }

    /**
     * Removes the <code>segment</code> from the trees.
     */
    public void delete(ValueSegment<V,?> segment) {
        int startIndex = start.indexOfValue(segment.getStart(), true, false, (byte)1);
        int endIndex = end.indexOfValue(segment.getEnd(), true, false, (byte)1);
        start.remove(startIndex, 1);
        end.remove(endIndex, 1);
    }

    /**
     * Clears the data from the trees efficiently.
     */
    public void clear() {
        start = new SimpleTree<V>(GlazedLists.comparableComparator());
        end = new SimpleTree<V>(GlazedLists.comparableComparator());
    }

    /**
     * Returns <tt>true</tt> if the trees contain any data; <tt>false</tt>
     * otherwise.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the number of {@link ValueSegment}s which appear between the
     * given <code>start</code> and <code>end</code> values.
     */
    public int getCount(V start, V end) {
        final int numStartedBeforeSegmentEnd = this.start.indexOfValue(end, true, true, (byte)1);
        final int numEndedBeforeSegmentStart = this.end.indexOfValue(start, true, true, (byte)1);

        return numStartedBeforeSegmentEnd - numEndedBeforeSegmentStart;
    }

    /**
     * Returns the number of {@link ValueSegment} objects contained within.
     */
    public int size() {
        return start.size();
    }
}