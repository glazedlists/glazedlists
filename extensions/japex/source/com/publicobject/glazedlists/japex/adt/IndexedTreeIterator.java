/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.glazedlists.japex.adt;

// for iterators
import java.util.*;

/**
 * A simple itertor of the nodes in an {@link IndexedTree}.
 *
 * @deprecated, replaced with {@link ca.odell.glazedlists.impl.adt.barcode2.BciiTreeIterator BC2}
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class IndexedTreeIterator<V> {

    /** the host tree */
    private IndexedTree<V> host = null;

    /**
     * The node for the next call to next(), or null if there are no nodes
     * left (because we've iterated to the end of the tree.
     */
    private IndexedTreeNode<V> nextNode = null;

    /** cache the index of the next node */
    private int nextIndex = 0;

    /** the most recent node returned from {@link #next} or {@link #previous}. */
    private IndexedTreeNode last = null;

    /**
     * Creates an iterator that iterates the tree starting at the specified index.
     *
     * @param index the value at index will be the value returned by the
     *              first call to next().
     */
    public IndexedTreeIterator(IndexedTree<V> host, int index) {
        if(index < 0 || index > host.size()) throw new IndexOutOfBoundsException();

        this.host = host;
        this.nextIndex = index;
        this.nextNode = nextIndex == host.size() ? null : host.root.getNodeWithIndex(nextIndex);
    }

    /**
     * Creates an iterator that iterates the tree starting at the specified node.
     */
    public IndexedTreeIterator(IndexedTree<V> host, IndexedTreeNode<V> nextNode) {
        if(host == null || nextNode == null) throw new IllegalArgumentException();

        this.host = host;
        this.nextNode = nextNode;
        this.nextIndex = this.nextNode.getIndex();
    }

    /**
     * Returns true if the iteration forward has more elements.
     */
    public boolean hasNext() {
        return nextIndex < host.size();
    }

    /**
     * Returns the index of the value last returned by this Iterator.
     */
    public int nextIndex() {
        if(!hasNext()) throw new NoSuchElementException();
        return nextIndex;
    }

    /**
     * Returns the next element in the iteration.
     */
    public IndexedTreeNode<V> next() {
        if(!hasNext()) throw new NoSuchElementException();

        this.last = this.nextNode;
        incrementNextNode();
        return this.last;
    }

    /**
     * Returns true if the iteration backwards has more elements.
     */
    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    /**
     * Returns the index of the value last returned by this Iterator.
     */
    public int previousIndex() {
        if(!hasPrevious()) throw new NoSuchElementException();
        return nextIndex - 1;
    }

    /**
     * Returns the previous element in the iteration.
     */
    public IndexedTreeNode<V> previous() {
        // handle the empty tree and start of tree cases
        if(!hasPrevious()) throw new NoSuchElementException();

        decrementNextNode();
        this.last = this.nextNode;
        return this.last;
    }

    /**
     * Remove the element most recently returned from {@link #next()} or
     * {@link #previous()}.
     */
    public void remove() {
        if(this.last == null) throw new NoSuchElementException();

        // remove the most recently requested element
        this.last.removeFromTree(host);

        // adjust indices
        this.last = null;
        this.nextIndex = (nextNode == null) ? host.size() : nextNode.getIndex();
    }

    /**
     * Increment {@link nextNode} to its follower.
     */
    private void incrementNextNode() {
        nextNode = nextNode.next();
        nextIndex++;
    }

    /**
     * Increment {@link nextNode} to its predecessor.
     */
    private void decrementNextNode() {
        // we've incremented past the end of the tree
        if(nextNode == null) {
            nextNode = host.getNode(nextIndex - 1);
        } else {
            nextNode = nextNode.previous();
        }
        
        nextIndex--;
    }
}
