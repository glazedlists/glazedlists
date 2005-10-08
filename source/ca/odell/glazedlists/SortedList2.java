/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.Barcode;

import java.util.Comparator;
import java.util.Arrays;

/**
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class SortedList2<E> extends TransformedList<E,E> {


    private Comparator comparator;
    private final IndexSnapshot sourceToSnapshotDeltas = new IndexSnapshot();
    private final IndexSnapshot viewToSnapshotDeltas = new IndexSnapshot();
    private int[] sourceToViewSnapshotReordering;
    private int[] viewToSourceSnapshotReordering;


    public SortedList2(EventList<E> source) {
        super(source);

        // prepare initial values of data structures
        this.sourceToSnapshotDeltas.reset(source.size());
        this.viewToSnapshotDeltas.reset(source.size());
        this.sourceToViewSnapshotReordering = new int[source.size()];
        this.viewToSourceSnapshotReordering = new int[source.size()];
        for(int i = 0; i < source.size(); i++) {
            this.sourceToViewSnapshotReordering[i] = i;
            this.viewToSourceSnapshotReordering[i] = i;
        }

        source.addListEventListener(this);
    }

    public void listChanged(ListEvent<E> listChanges) {
        updates.beginEvent();
        while(listChanges.next()) {
            int sourceIndex = listChanges.getIndex();
            int type = listChanges.getType();

            if(type == ListEvent.INSERT) {
                sourceToSnapshotDeltas.add(sourceIndex);
                int sourceSnapshotIndex = sourceToSnapshotDeltas.currentToSnapshot(sourceIndex);
                // note that viewSnapshotIndex == sourceSnapshotIndex because
                // inserts aren't in the reorder map
                viewToSnapshotDeltas.add(viewToSnapshotDeltas.currentSize());
                int viewIndex = viewToSnapshotDeltas.snapshotToCurrent(sourceSnapshotIndex);
                updates.addInsert(viewIndex);

            } else if(type == ListEvent.UPDATE) {
                int sourceSnapshotIndex = sourceToSnapshotDeltas.currentToSnapshot(sourceIndex);
                int viewSnapshotIndex = sourceSnapshotIndex >= sourceToViewSnapshotReordering.length ? sourceSnapshotIndex : sourceToViewSnapshotReordering[sourceSnapshotIndex];
                int viewIndex = viewToSnapshotDeltas.snapshotToCurrent(viewSnapshotIndex);
                updates.addUpdate(viewIndex);

            } else if(type == ListEvent.DELETE) {
                int viewIndex = getViewIndex(sourceIndex);
                sourceToSnapshotDeltas.remove(sourceIndex);
                viewToSnapshotDeltas.remove(viewIndex);
                updates.addDelete(viewIndex);

            }
        }
        updates.commitEvent();
    }

    protected int getSourceIndex(int viewIndex) {
        int viewSnapshotIndex = viewToSnapshotDeltas.currentToSnapshot(viewIndex);
        int sourceSnapshotIndex = viewSnapshotIndex >= viewToSourceSnapshotReordering.length ? viewSnapshotIndex : viewToSourceSnapshotReordering[viewSnapshotIndex];
        int sourceIndex = sourceToSnapshotDeltas.snapshotToCurrent(sourceSnapshotIndex);
        return sourceIndex;
    }

    private int getViewIndex(int sourceIndex) {
        int sourceSnapshotIndex = sourceToSnapshotDeltas.currentToSnapshot(sourceIndex);
        int viewSnapshotIndex = sourceSnapshotIndex >= sourceToViewSnapshotReordering.length ? sourceSnapshotIndex : sourceToViewSnapshotReordering[sourceSnapshotIndex];
        int viewIndex = viewToSnapshotDeltas.snapshotToCurrent(viewSnapshotIndex);
        return viewIndex;
    }

    public void setComparator(Comparator<E> comparator) {
        this.comparator = comparator;

        // get a sorted order
        Integer[] viewToSourceAsIntegers = new Integer[source.size()];
        for(int i = 0; i < viewToSourceAsIntegers.length; i++) viewToSourceAsIntegers[i] = new Integer(i);
        Arrays.sort(viewToSourceAsIntegers, new IndexByValueComparator());

        // figure out the latest state
        int[] reorderMap = new int[source.size()];
        int[] newViewToSourceSnapshotReordering = new int[source.size()];
        int[] newSourceToViewSnapshotReordering = new int[source.size()];
        for(int viewIndex = 0; viewIndex < source.size(); viewIndex++) {
            int sourceIndex = viewToSourceAsIntegers[viewIndex].intValue();
            int oldViewIndex = getViewIndex(sourceIndex);
            newSourceToViewSnapshotReordering[sourceIndex] = viewIndex;
            newViewToSourceSnapshotReordering[viewIndex] = sourceIndex;
            reorderMap[viewIndex] = oldViewIndex;
        }
        this.viewToSourceSnapshotReordering = newViewToSourceSnapshotReordering;
        this.sourceToViewSnapshotReordering = newSourceToViewSnapshotReordering;
        sourceToSnapshotDeltas.reset(source.size());
        viewToSnapshotDeltas.reset(source.size());

        // fire events
        updates.beginEvent();
        updates.reorder(reorderMap);
        updates.commitEvent();
    }
    public Comparator<E> getComparator() {
        return comparator;
    }

    /**
     * Sort indices by sorting the values at those indices.
     */
    private class IndexByValueComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            E objectA = source.get(a.intValue());
            E objectB = source.get(b.intValue());
            return comparator.compare(objectA, objectB);
        }
    }
}

/**
 * This class manages a mapping between one fixed set of indices (a "snapshot")
 * and a different set of indices that is a revision of that snapshot. Each
 * time the list is changed (due to inserts or deletes), the mapping between
 * these indices can be adjusted.
 *
 * <p>For example, suppose you have the sequence:
 * <code>F, I, L, T, E, R</code>, and you have performed some time-intensive
 * calculation on those elements (such as creating a sorted view of it). In the
 * initial case, the {@link IndexSnapshot} provides a natural function to convert
 * between indices:
 * <code>currentToSnapshot(0) -> 0</code>,
 * <code>currentToSnapshot(5) -> 5</code>.
 *
 * <p>When you change that sequence by removing the 'I' and 'E', the
 * {@link IndexSnapshot} we have the sequence <code>F, L, T, R</code>.
 * The {@link IndexSnapshot} provides a more value in this case:
 * <code>currentToSnapshot(0) -> 0</code>,
 * <code>currentToSnapshot(3) -> 5</code>,
 * <code>snapshotToCurrent(0) -> 0</code>,
 * <code>snapshotToCurrent(1) -> -1</code>,
 * <code>snapshotToCurrent(5) -> 3</code>.
 *
 * <p>Similarly you can change the sequence again by adding elements
 * <code>O, A</code> to create the sequence <code>F, L, O, A, T, R</code>.
 * The {@link IndexSnapshot} provides these results:
 * <code>currentToSnapshot(1) -> 2</code>,
 * <code>currentToSnapshot(2) -> 7</code>,
 * <code>currentToSnapshot(3) -> 8</code>,
 * <code>currentToSnapshot(4) -> 3</code>,
 * <code>snapshotToCurrent(1) -> -1</code>,
 * <code>snapshotToCurrent(3) -> 4</code>.
 */
class IndexSnapshot {

    /**
     * This barcode is black for every element in the snapshot that hasn't
     * yet been deleted. It's white for each of those elements that has been
     * deleted. It is always exactly the same size as the snapshot, and
     * does not change in length.
     */
    private final Barcode deletes = new Barcode();

    /**
     * This barcode is black for every element in the snapshot that still
     * exists. If an element has been removed, it's also removed from this.
     * It also includes a white for each element inserted. It's length is
     * always equal to the length of the current view.
     */
    private final Barcode inserts = new Barcode();


    public void reset(int length) {
        deletes.clear();
        deletes.addBlack(0, length);
        inserts.clear();
        inserts.addBlack(0, length);
    }

    public int currentToSnapshot(int currentIndex) {
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // adjust for inserts
        if(inserts.get(currentIndex) == Barcode.WHITE) {
            // this element has been inserted since the snapshot,
            // so put it beyond the length of the snapshots
            return snapshotSize() + inserts.getWhiteIndex(currentIndex);
        } else {
            // this element existed in the snapshot, so adjust it
            // for all nodes that have been inserted since the snapshot
            // was made (this decreases the value).
            currentIndex = inserts.getBlackIndex(currentIndex);
        }

        // adjust for deletes by adding back all elements
        // in the snapshot that have since been deleted
        // (this increases the value)
        return deletes.getIndex(currentIndex, Barcode.BLACK);
    }
    public int snapshotToCurrent(int snapshotIndex) {
        if(snapshotIndex < 0 || snapshotIndex >= (inserts.whiteSize() + snapshotSize())) throw new IndexOutOfBoundsException();

        // if this is beyond the snapshot, we want the white index
        if(snapshotIndex >= snapshotSize()) {
            return inserts.getIndex(snapshotIndex - snapshotSize(), Barcode.WHITE);
        }

        // this snapshot element has been deleted
        if(deletes.get(snapshotIndex) == Barcode.WHITE) {
            return -1;
        }

        // adjust for deleted snapshot elements
        snapshotIndex = deletes.getBlackIndex(snapshotIndex);

        // adjust for inserts by shifting past all elements
        // that have since been inserted. (this increases the value)
        return inserts.getIndex(snapshotIndex, Barcode.BLACK);
    }

    public void add(int currentIndex) {
        if(currentIndex < 0 || currentIndex > currentSize()) throw new IndexOutOfBoundsException();
        inserts.addWhite(currentIndex, 1);
    }

    public int currentSize() {
        return inserts.size();
    }

    public int snapshotSize() {
        return deletes.size();
    }

    public void remove(int currentIndex) {
        if(currentIndex < 0 || currentIndex >= currentSize()) throw new IndexOutOfBoundsException();

        // if this was a snapshot element, we need to mark
        // this as deleted from the deletes list
        if(inserts.get(currentIndex) == Barcode.BLACK) {
            int insertAdjusted = inserts.getBlackIndex(currentIndex);
            int deleteAdjusted = deletes.getIndex(insertAdjusted, Barcode.BLACK);
            deletes.set(deleteAdjusted, Barcode.WHITE, 1);
            inserts.remove(currentIndex, 1);

        // if this was not a snapshot element, we can just
        // de-insert it
        } else {
            inserts.remove(currentIndex, 1);
        }
    }
}