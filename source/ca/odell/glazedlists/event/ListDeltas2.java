/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.event;

import ca.odell.glazedlists.impl.adt.barcode2.ListToByteCoder;
import ca.odell.glazedlists.impl.adt.barcode2.Tree;
import ca.odell.glazedlists.impl.adt.barcode2.TreeIterator;

import java.util.Arrays;

/**
 * Working copy of a class to eventually become a proper replacement for
 * {@link ListEventBlock}s. This class doesn't work at the moment, but hopefully
 * we soon have time to comb it over and fix its weaknesses. Current problems:
 * <li>Test cases fail!
 * <li>ListDelta Iterator is index by index, not node by node
 * <li>Tree Iterator is index by index, not node by node
 * <li>Logic to find appropriate index doing an extra layer of mapping isn't nice
 * <li>reset() / ensureCapacity() could be fixed by making ListEventAssembler
 *     cache the list size whenever an event is fired
 * <li>Tree nodes could have count attributes created lazily
 * <li>Tree could be optimized!
 * <li>Where necessary, asserts should be pulled out because they're slow!
 * <li>Fix the API to accept blocks
 * <li>Clarify tree's rules regarding combining of nodes
 * <li>Provide special-case support for increasing event indices, such as
 *     those from FilterList.matcherChanged
 * <li>Faster Tree.clear() method
 *
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class ListDeltas2 {

    private static final Object LIST_CHANGE = new Object();
    private static final ListToByteCoder<String> BYTE_CODER = new ListToByteCoder<String>(Arrays.asList(new String[] { "+", "U", "X", "_" }));
    private static final byte INSERT = BYTE_CODER.colorToByte("+");
    private static final byte UPDATE = BYTE_CODER.colorToByte("U");
    private static final byte DELETE = BYTE_CODER.colorToByte("X");
    private static final byte NO_CHANGE = BYTE_CODER.colorToByte("_");

    private static final byte SNAPSHOT_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "_" }));
    private static final byte CURRENT_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "+", "_" }));
    private static final byte ALL_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+", "_" }));
    private static final byte CHANGE_INDICES = BYTE_CODER.colorsToByte(Arrays.asList(new String[] { "U", "X", "+" }));

    private Tree tree = new Tree(BYTE_CODER);
    private boolean allowContradictingEvents = true;

    /**
     * When the first change to a list happens, we need to guess what the list's
     * capacity is. After that change, we reliably know the list's capacity, so
     * we don't need to keep testing the capacity one index at a time.
     */
    private boolean initialCapacityKnown = false;


    public boolean isAllowContradictingEvents() {
        return allowContradictingEvents;
    }
    public void setAllowContradictingEvents(boolean allowContradictingEvents) {
        this.allowContradictingEvents = allowContradictingEvents;
    }

    public int currentToSnapshot(int currentIndex) {
        if(!initialCapacityKnown) ensureCapacity(currentIndex + 1);
        return tree.indexOf(currentIndex, CURRENT_INDICES, SNAPSHOT_INDICES);
    }

    public int snapshotToCurrent(int snapshotIndex) {
        if(!initialCapacityKnown) ensureCapacity(snapshotIndex + 1);
        return tree.indexOf(snapshotIndex, SNAPSHOT_INDICES, CURRENT_INDICES);
    }

    public void  update(int currentIndex) {
        if(!initialCapacityKnown) ensureCapacity(currentIndex + 1);
        int overallIndex = tree.indexOf(currentIndex, CURRENT_INDICES, ALL_INDICES);
        // don't bother updating an inserted element
        if(tree.get(overallIndex, ALL_INDICES).getColor() == INSERT) return;
        tree.set(overallIndex, ALL_INDICES, UPDATE, LIST_CHANGE, 1);
    }

    public void insert(int currentIndex) {
        if(!initialCapacityKnown) ensureCapacity(currentIndex + 1);
        tree.add(currentIndex, CURRENT_INDICES, INSERT, LIST_CHANGE, 1);
    }

    public void delete(int currentIndex) {
        if(!initialCapacityKnown) ensureCapacity(currentIndex + 1);
        int overallIndex = tree.indexOf(currentIndex, CURRENT_INDICES, ALL_INDICES);
        // if its an insert, simply remove that insert
        if(tree.get(overallIndex, ALL_INDICES).getColor() == INSERT) {
            if(!allowContradictingEvents) throw new IllegalStateException("Remove " + currentIndex + " undoes prior insert at the same index! Consider enabling contradicting events.");
            tree.remove(overallIndex, ALL_INDICES, 1);

        // otherwise apply the delete
        } else {
            tree.set(overallIndex, ALL_INDICES, DELETE, LIST_CHANGE, 1);
        }
    }

    public int currentSize() {
        return tree.size(CURRENT_INDICES);
    }

    public int snapshotSize() {
        return tree.size(SNAPSHOT_INDICES);
    }

    public void reset(int size) {
        tree.clear();
        initialCapacityKnown = true;
        ensureCapacity(size);
    }
    private void ensureCapacity(int size) {
        int currentSize = tree.size(CURRENT_INDICES);
        int delta = size - currentSize;
        if(delta > 0) {
            int endOfTree = tree.size(ALL_INDICES);
            tree.add(endOfTree, ALL_INDICES, NO_CHANGE, LIST_CHANGE, delta);
        }
    }

    /**
     * @return <code>true</code> if this event contains no changes.
     */
    public boolean isEmpty() {
        return tree.size(CHANGE_INDICES) == 0;
    }

    public Iterator iterator() {
        return new Iterator(tree);
    }

    public String toString() {
        return tree.asSequenceOfColors();
    }

    /**
     * Iterate through the list of changes in this tree.
     */
    public static class Iterator {

        private final Tree tree;
        private final TreeIterator treeIterator;

        private Iterator(Tree tree) {
            this.tree = tree;
            this.treeIterator = new TreeIterator(tree);
        }
        public int getIndex() {
            return treeIterator.index(CURRENT_INDICES);
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
        public boolean hasNext() {
            return treeIterator.hasNext(CHANGE_INDICES);
        }
    }
}