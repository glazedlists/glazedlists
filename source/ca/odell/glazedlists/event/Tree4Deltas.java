/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.impl.adt.barcode2.FourColorTree;
import ca.odell.glazedlists.impl.adt.barcode2.FourColorTreeIterator;
import ca.odell.glazedlists.impl.adt.barcode2.ListToByteCoder;
import ca.odell.glazedlists.impl.adt.barcode2.Element;

import java.util.Arrays;

/**
 * Manage and describe the differences between two revisions of the
 * same List, assuming either one can change at any time.
 *
 * <p>Initially, the source and target lists are equal. Over time, the
 * target list changes. It's also possible that the source list can change,
 * which is necessary for long-lived buffered changes.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class Tree4Deltas<E> {

    /** all the names of the index sets are with respect to the target */
    private static final ListToByteCoder<String> BYTE_CODER = new ListToByteCoder<String>(Arrays.asList(new String[] { "+", "U", "X", "_" }));
    private static final byte INSERT = BYTE_CODER.colorToByte("+");
    private static final byte UPDATE = BYTE_CODER.colorToByte("U");
    private static final byte DELETE = BYTE_CODER.colorToByte("X");
    private static final byte NO_CHANGE = BYTE_CODER.colorToByte("_");

    private static final byte SOURCE_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "_" }));
    private static final byte TARGET_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "+", "_" }));
    private static final byte ALL_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+", "_" }));
    private static final byte CHANGE_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+" }));

    /** the trees values include removed elements */
    private FourColorTree<E> tree = new FourColorTree<E>(BYTE_CODER);
    private boolean allowContradictingEvents = false;

    /**
     * When the first change to a list happens, we need to guess what the list's
     * capacity is. After that change, we reliably know the list's capacity, so
     * we don't need to keep testing the capacity one index at a time.
     */
    private boolean initialCapacityKnown = false;


    public boolean getAllowContradictingEvents() {
        return allowContradictingEvents;
    }
    public void setAllowContradictingEvents(boolean allowContradictingEvents) {
        this.allowContradictingEvents = allowContradictingEvents;
    }

    public int targetToSource(int targetIndex) {
        if(!initialCapacityKnown) ensureCapacity(targetIndex + 1);
        return tree.convertIndexColor(targetIndex, TARGET_INDICES, SOURCE_INDICES);
    }

    public int sourceToTarget(int sourceIndex) {
        if(!initialCapacityKnown) ensureCapacity(sourceIndex + 1);
        return tree.convertIndexColor(sourceIndex, SOURCE_INDICES, TARGET_INDICES);
    }

    /**
     * <p>We should consider removing the loop by only setting on removed elements.
     *
     * @param value the previous value being replaced
     * @param startIndex the first updated element, inclusive
     * @param endIndex the last index, exclusive
     */
    public void targetUpdate(int startIndex, int endIndex, E value) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        for(int i = startIndex; i < endIndex; i++) {
            int overallIndex = tree.convertIndexColor(i, TARGET_INDICES, ALL_INDICES);
            Element<E> standingChangeToIndex = tree.get(overallIndex, ALL_INDICES);

            // don't bother updating an inserted element
            if(standingChangeToIndex.getColor() == INSERT) {
                continue;
            }

            // if we're updating an update, the original replaced value stands.
            if(standingChangeToIndex.getColor() == UPDATE) {
                value = standingChangeToIndex.get();
            }

            // apply the update to our change description
            tree.set(overallIndex, ALL_INDICES, UPDATE, value, 1);
        }
    }

    /**
     * @param startIndex the first inserted element, inclusive
     * @param endIndex the last index, exclusive
     */
    public void targetInsert(int startIndex, int endIndex) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        tree.add(startIndex, TARGET_INDICES, INSERT, (E)ListEvent.UNKNOWN_VALUE, endIndex - startIndex);
    }

    /**
     * Add a value to the target only.
     *
     * <p>Since this method takes a value parameter, is is only needed
     * when the target doesn't store its value, for example with buffered
     * changes.
     *
     * @param value the inserted value
     */
    public void targetInsert(int startIndex, int endIndex, E value) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        tree.add(startIndex, TARGET_INDICES, INSERT, value, endIndex - startIndex);
    }

    /**
     * <p>We should consider removing the loop from this method by counting
     * the inserted elements between startIndex and endIndex, removing those,
     * then removing everything else...
     *
     * @param startIndex the index of the first element to remove
     * @param endIndex the last index, exclusive
     * @param value the removed value
     */
    public void targetDelete(int startIndex, int endIndex, E value) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        for(int i = startIndex; i < endIndex; i++) {
            if(startIndex > 0 && startIndex > tree.size(TARGET_INDICES)) {
                throw new IllegalArgumentException();
            }
            int overallIndex = tree.convertIndexColor(startIndex, TARGET_INDICES, ALL_INDICES);
            Element<E> standingChangeToIndex = tree.get(overallIndex, ALL_INDICES);

            // if we're deleting an insert, remove that insert
            if(standingChangeToIndex.getColor() == INSERT) {
                if(!allowContradictingEvents) throw new IllegalStateException("Remove " + i + " undoes prior insert at the same index! Consider enabling contradicting events.");
                tree.remove(overallIndex, ALL_INDICES, 1);
                continue;
            }

            // if we're deleting an update, the original replaced value stands.
            if(standingChangeToIndex.getColor() == UPDATE) {
                value = standingChangeToIndex.get();
            }

            tree.set(overallIndex, ALL_INDICES, DELETE, value, 1);
       }
    }

    public void sourceInsert(int sourceIndex) {
        tree.add(sourceIndex, SOURCE_INDICES, NO_CHANGE, (E)ListEvent.UNKNOWN_VALUE, 1);
    }

    public void sourceDelete(int sourceIndex) {
        tree.remove(sourceIndex, SOURCE_INDICES, 1);
    }

    public int currentSize() {
        return tree.size(TARGET_INDICES);
    }

    public int snapshotSize() {
        return tree.size(SOURCE_INDICES);
    }

    /**
     * Get the value at the specified target index.
     *
     * @return the value, or {@link ListEvent#UNKNOWN_VALUE} if this index
     *     holds a value that hasn't been buffered. In this case, the value
     *     can be obtained from the source list. 
     */
    public E getTargetValue(int targetIndex) {
        return tree.get(targetIndex, TARGET_INDICES).get();
    }

    public void reset(int size) {
        tree.clear();
        initialCapacityKnown = true;
        ensureCapacity(size);
    }
    private void ensureCapacity(int size) {
        int currentSize = tree.size(TARGET_INDICES);
        int delta = size - currentSize;
        if(delta > 0) {
            int endOfTree = tree.size(ALL_INDICES);
            tree.add(endOfTree, ALL_INDICES, NO_CHANGE, (E)ListEvent.UNKNOWN_VALUE, delta);
        }
    }

    /**
     * Add all the specified changes to this.
     */
    void addAll(BlockSequence<E> blocks) {
        for(BlockSequence<E>.Iterator i = blocks.iterator(); i.nextBlock(); ) {
            int blockStart = i.getBlockStart();
            int blockEnd = i.getBlockEnd();
            int type = i.getType();
            E value = i.getPreviousValue();

            if(type == ListEvent.INSERT) {
                targetInsert(blockStart, blockEnd);
            } else if(type == ListEvent.UPDATE) {
                targetUpdate(blockStart, blockEnd, value);
            } else if(type == ListEvent.DELETE) {
                targetDelete(blockStart, blockEnd, value);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return <code>true</code> if this event contains no changes.
     */
    public boolean isEmpty() {
        return tree.size(CHANGE_INDICES) == 0;
    }

    public Iterator<E> iterator() {
        return new Iterator<E>(tree);
    }

    public String toString() {
        return tree.asSequenceOfColors();
    }

    /**
     * Iterate through the list of changes in this tree.
     */
    public static class Iterator<E> {

        private final FourColorTree<E> tree;
        private final FourColorTreeIterator<E> treeIterator;

        private Iterator(FourColorTree<E> tree) {
            this.tree = tree;
            this.treeIterator = new FourColorTreeIterator<E>(tree);
        }

        private Iterator(FourColorTree<E> tree, FourColorTreeIterator<E> treeIterator) {
            this.tree = tree;
            this.treeIterator = treeIterator;
        }
        public Iterator<E> copy() {
            return new Iterator<E>(tree, treeIterator.copy());
        }

        public int getIndex() {
            return treeIterator.index(TARGET_INDICES);
        }
        public int getEndIndex() {
            // this is peculiar. We add mixed types - an index of current indices
            // plus the size of "all indices". . . this is because we describe the
            // range of deleted indices from its start to finish, although it's
            // finish will ultimately go to zero once the change is applied. 
            return treeIterator.nodeStartIndex(TARGET_INDICES) + treeIterator.nodeSize(ALL_INDICES);
        }

        public int getType() {
            byte color = treeIterator.color();
            if(color == INSERT) return ListEvent.INSERT;
            else if(color == UPDATE) return ListEvent.UPDATE;
            else if(color == DELETE) return ListEvent.DELETE;
            else throw new IllegalStateException();
        }
        public boolean next() {
            if(!hasNext()) return false;
            treeIterator.next(CHANGE_INDICES);
            return true;
        }
        public boolean nextNode() {
            if(!hasNextNode()) return false;
            treeIterator.nextNode(CHANGE_INDICES);
            return true;
        }
        public boolean hasNext() {
            return treeIterator.hasNext(CHANGE_INDICES);
        }
        public boolean hasNextNode() {
            return treeIterator.hasNextNode(CHANGE_INDICES);
        }

        public E getPreviousValue() {
            return treeIterator.node().get();
        }
    }
}
