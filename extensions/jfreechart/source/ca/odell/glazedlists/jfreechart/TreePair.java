/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.jfreechart;

import ca.odell.glazedlists.impl.adt.IndexedTree;
import ca.odell.glazedlists.impl.adt.IndexedTreeNode;
import ca.odell.glazedlists.GlazedLists;

/**
 * A TreePair contains a two tree structures which store the start and end
 * {@link Comparable} values of {@link ValueSegment} objects in their natural
 * orders. This allows for efficient answers to questions like
 *
 * <ul>
 *   <li> How many {@link ValueSegment}s exist after X?
 *   <li> How many {@link ValueSegment}s exist before Y?
 *   <li> How many {@link ValueSegment}s exist between X and Y?
 * </ul>
 *
 * @author Jame Lemieux
 */
public class TreePair {
    /** The tree which orders the start indices of all ValueSegments. */
    private IndexedTree<Comparable> start = new IndexedTree<Comparable>(GlazedLists.comparableComparator());

    /** The tree which orders the start indices of all ValueSegments. */
    private IndexedTree<Comparable> end = new IndexedTree<Comparable>(GlazedLists.comparableComparator());

    /**
     * Inserts the given <code>segment</code> into the trees.
     */
    public void insert(ValueSegment segment) {
        start.addByNode(segment.getStart());
        end.addByNode(segment.getEnd());
    }

    /**
     * Removes the <code>previousSegment</code> and inserts the
     * <code>newSegment</code> into the trees.
     */
    public void update(ValueSegment previousSegment, ValueSegment newSegment) {
        delete(previousSegment);
        insert(newSegment);
    }

    /**
     * Removes the <code>segment</code> from the trees.
     */
    public void delete(ValueSegment segment) {
        final IndexedTreeNode<Comparable> startNode = start.getNode(segment.getStart());
        final IndexedTreeNode<Comparable> endNode = end.getNode(segment.getEnd());

        startNode.removeFromTree(start);
        endNode.removeFromTree(end);
    }

    /**
     * Clears the data from the trees efficiently.
     */
    public void clear() {
        this.start = new IndexedTree<Comparable>(GlazedLists.comparableComparator());
        this.end = new IndexedTree<Comparable>(GlazedLists.comparableComparator());
    }

    /**
     * Returns <tt>true</tt> if the trees contain any data; <tt>false</tt>
     * otherwise.
     */
    public boolean isEmpty() {
        return this.start.size() == 0;
    }

    /**
     * Returns the number of {@link ValueSegment}s which appear between the
     * start and end values of the given <code>segment</code>.
     */
    public int getCount(ValueSegment segment) {
        final int numStartedBeforeSegmentEnd = start.indexOfSimulated(segment.getEnd());
        final int numEndedBeforeSegmentStart = end.indexOfSimulated(segment.getStart());

        return numStartedBeforeSegmentEnd - numEndedBeforeSegmentStart;
    }

    public int size() {
        return this.start.size();
    }
}