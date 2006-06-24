// Copyright 2006 Google Inc. All Rights Reserved.

package ca.odell.glazedlists.event;

import ca.odell.glazedlists.impl.adt.barcode2.*;

import java.util.Arrays;

/**
 * Working copy of a class to eventually become a proper replacement for
 * {@link ca.odell.glazedlists.event.Block}s.
 *
 * <li>Test cases fail due to no copy constructor in {@link ca.odell.glazedlists.event.ListEvent}
 * <li>Logic to find appropriate index doing an extra layer of mapping isn't nice
 * <li>Clarify tree's rules regarding combining of nodes
 * <li>Provide special-case support for increasing event indices, such as
 *     those from FilterList.matcherChanged
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
class Tree4Deltas {

    private static final String LIST_CHANGE = "*";
    private static final ListToByteCoder<String> BYTE_CODER = new ListToByteCoder<String>(Arrays.asList(new String[] { "+", "U", "X", "_" }));
    private static final byte INSERT = Tree4Deltas.BYTE_CODER.colorToByte("+");
    private static final byte UPDATE = Tree4Deltas.BYTE_CODER.colorToByte("U");
    private static final byte DELETE = Tree4Deltas.BYTE_CODER.colorToByte("X");
    private static final byte NO_CHANGE = Tree4Deltas.BYTE_CODER.colorToByte("_");

    private static final byte SNAPSHOT_INDICES = Tree4Deltas.BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "_" }));
    private static final byte CURRENT_INDICES = Tree4Deltas.BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "+", "_" }));
    private static final byte ALL_INDICES = Tree4Deltas.BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+", "_" }));
    private static final byte CHANGE_INDICES = Tree4Deltas.BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+" }));

    private Tree4<String> tree = new Tree4<String>(Tree4Deltas.BYTE_CODER);
    private boolean allowContradictingEvents = true;

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

    public int currentToSnapshot(int currentIndex) {
        if(!initialCapacityKnown) ensureCapacity(currentIndex + 1);
        return tree.convertIndexColor(currentIndex, Tree4Deltas.CURRENT_INDICES, Tree4Deltas.SNAPSHOT_INDICES);
    }

    public int snapshotToCurrent(int snapshotIndex) {
        if(!initialCapacityKnown) ensureCapacity(snapshotIndex + 1);
        return tree.convertIndexColor(snapshotIndex, Tree4Deltas.SNAPSHOT_INDICES, Tree4Deltas.CURRENT_INDICES);
    }

    /**
     * <p>We should consider removing the loop by only setting on removed elements.
     *
     * @param startIndex the first updated element, inclusive
     * @param endIndex the last index, exclusive
     */
    public void  update(int startIndex, int endIndex) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        for(int i = startIndex; i < endIndex; i++) {
            int overallIndex = tree.convertIndexColor(i, Tree4Deltas.CURRENT_INDICES, Tree4Deltas.ALL_INDICES);
            // don't bother updating an inserted element
            if(tree.get(overallIndex, Tree4Deltas.ALL_INDICES).getColor() == Tree4Deltas.INSERT) return;
            tree.set(overallIndex, Tree4Deltas.ALL_INDICES, Tree4Deltas.UPDATE, Tree4Deltas.LIST_CHANGE, 1);
        }
    }

    /**
     * @param startIndex the first inserted element, inclusive
     * @param endIndex the last index, exclusive
     */
    public void insert(int startIndex, int endIndex) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        tree.add(startIndex, Tree4Deltas.CURRENT_INDICES, Tree4Deltas.INSERT, Tree4Deltas.LIST_CHANGE, endIndex - startIndex);
    }

    /**
     * <p>We should consider removing the loop from this method by counting
     * the inserted elements between startIndex and endIndex, removing those,
     * then removing everything else...
     *
     * @param startIndex the index of the first element to remove
     * @param endIndex the last index, exclusive
     */
    public void delete(int startIndex, int endIndex) {
        if(!initialCapacityKnown) ensureCapacity(endIndex);
        for(int i = startIndex; i < endIndex; i++) {
            int overallIndex = tree.convertIndexColor(startIndex, Tree4Deltas.CURRENT_INDICES, Tree4Deltas.ALL_INDICES);
            // if its an insert, simply remove that insert
            if(tree.get(overallIndex, Tree4Deltas.ALL_INDICES).getColor() == Tree4Deltas.INSERT) {
                if(!allowContradictingEvents) throw new IllegalStateException("Remove " + i + " undoes prior insert at the same index! Consider enabling contradicting events.");
                tree.remove(overallIndex, Tree4Deltas.ALL_INDICES, 1);

            // otherwise apply the delete
            } else {
                tree.set(overallIndex, Tree4Deltas.ALL_INDICES, Tree4Deltas.DELETE, Tree4Deltas.LIST_CHANGE, 1);
            }
        }
    }

    public int currentSize() {
        return tree.size(Tree4Deltas.CURRENT_INDICES);
    }

    public int snapshotSize() {
        return tree.size(Tree4Deltas.SNAPSHOT_INDICES);
    }

    public void reset(int size) {
        tree.clear();
        initialCapacityKnown = true;
        ensureCapacity(size);
    }
    private void ensureCapacity(int size) {
        int currentSize = tree.size(Tree4Deltas.CURRENT_INDICES);
        int delta = size - currentSize;
        if(delta > 0) {
            int endOfTree = tree.size(Tree4Deltas.ALL_INDICES);
            tree.add(endOfTree, Tree4Deltas.ALL_INDICES, Tree4Deltas.NO_CHANGE, Tree4Deltas.LIST_CHANGE, delta);
        }
    }

    /**
     * Add all the specified changes to this.
     */
    void addAll(BlockSequence blocks) {
        for(BlockSequence.Iterator i = blocks.iterator(); i.nextBlock(); ) {
            int blockStart = i.getBlockStart();
            int blockEnd = i.getBlockEnd();
            int type = i.getType();

            if(type == ListEvent.INSERT) {
                insert(blockStart, blockEnd);
            } else if(type == ListEvent.UPDATE) {
                update(blockStart, blockEnd);
            } else if(type == ListEvent.DELETE) {
                delete(blockStart, blockEnd);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /**
     * @return <code>true</code> if this event contains no changes.
     */
    public boolean isEmpty() {
        return tree.size(Tree4Deltas.CHANGE_INDICES) == 0;
    }

    public Tree4Deltas.Iterator iterator() {
        return new Tree4Deltas.Iterator<String>(tree);
    }

    public String toString() {
        return tree.asSequenceOfColors();
    }

    /**
     * Iterate through the list of changes in this tree.
     */
    public static class Iterator<V> {

        private final Tree4<V> tree;
        private final Tree4Iterator<V> treeIterator;

        private Iterator(Tree4<V> tree) {
            this.tree = tree;
            this.treeIterator = new Tree4Iterator<V>(tree);
        }

        private Iterator(Tree4<V> tree, Tree4Iterator<V> treeIterator) {
            this.tree = tree;
            this.treeIterator = treeIterator;
        }
        public Tree4Deltas.Iterator<V> copy() {
            return new Tree4Deltas.Iterator<V>(tree, treeIterator.copy());
        }

        public int getIndex() {
            return treeIterator.index(Tree4Deltas.CURRENT_INDICES);
        }
        public int getType() {
            byte color = treeIterator.color();
            if(color == Tree4Deltas.INSERT) return ListEvent.INSERT;
            else if(color == Tree4Deltas.UPDATE) return ListEvent.UPDATE;
            else if(color == Tree4Deltas.DELETE) return ListEvent.DELETE;
            else throw new IllegalStateException();
        }
        public boolean next() {
            if(!hasNext()) return false;
            treeIterator.next(Tree4Deltas.CHANGE_INDICES);
            return true;
        }
        public boolean hasNext() {
            return treeIterator.hasNext(Tree4Deltas.CHANGE_INDICES);
        }
    }
}
