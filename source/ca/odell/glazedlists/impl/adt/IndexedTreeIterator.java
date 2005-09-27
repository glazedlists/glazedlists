/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for iterators
import java.util.*;

/**
 * A simple {@link ListIterator} for the {@link IndexedTree}.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IndexedTreeIterator<V> implements Iterator<V> {

    /** the index of the current node */
    private int currentIndex = 0;

    /** the host tree */
    private IndexedTree<V> host = null;

    /** the last tree node returned by this iterator */
    private IndexedTreeNode currentNode = null;

    /** the direction of iteration */
    private boolean goingForward = true;

    /**
     * Creates an iterator that iterates the tree starting at the specified
     * node.
     */
    public IndexedTreeIterator(IndexedTree<V> host) {
        this(host, 0);
    }

    /**
     * Creates an iterator that iterates the tree starting at the specified
     * node.
     *
     * @param index the value at index will be the value returned by the
     *              first call to next().
     */
    public IndexedTreeIterator(IndexedTree<V> host, int index) {
        this.host = host;
        this.currentIndex = index - 1;

        // the tree is empty
        if(host.root == null) {
            currentNode = null;

        // starting in the middle
        } else if(currentIndex > -1) {
            currentNode = host.getNode(currentIndex);

        // starting at the very beginning
        } else {
            currentNode = host.root.getSmallestChildNode();
        }
    }

    /**
     * Returns true if the iteration forward has more elements.
     */
    public boolean hasNext() {
        return currentIndex < host.size() - 1;
    }

    /**
     * Returns the index of the value last returned by this Iterator.
     */
    public int nextIndex() {
        return currentIndex + 1;
    }

    /**
     * Returns the next element in the iteration.
     */
    public V next() {
        // handle the empty tree and end of tree cases
        if(currentNode == null || currentIndex >= host.size() - 1) {
            throw new NoSuchElementException();

        // at the very beginning
        } else if(currentIndex == -1) {
            currentIndex = 0;
            goingForward = true;
            return (V)currentNode;

        // anywhere else in the tree is the same
        } else {
            // already iterating forwards
            if(goingForward) {
                currentIndex++;
                findNextNode();
                return (V)currentNode;

            // switching iteration directions
            } else {
                goingForward = true;
                return (V)currentNode;
            }
        }
    }

    /**
     * Returns true if the iteration backwards has more elements.
     */
    public boolean hasPrevious() {
        return currentIndex > 0;
    }

    /**
     * Returns the index of the value last returned by this Iterator.
     */
    public int previousIndex() {
        return currentIndex;
    }

    /**
     * Returns the previous element in the iteration.
     */
    public IndexedTreeNode<V> previous() {
        // handle the empty tree and start of tree cases
        if(currentNode == null || currentIndex < 0) {
            throw new NoSuchElementException();

        // at the beginning of the tree
        } else if(currentIndex == 0) {
            currentIndex = -1;
            goingForward = false;
            return currentNode;

        // anywhere else in the tree
        } else {
            // already iterating backwards
            if(!goingForward) {
                currentIndex--;
                findPreviousNode();
                return currentNode;

            // switching iteration directions
            } else {
                goingForward = false;
                return currentNode;
            }
        }
    }

    /**
     * Removes from IndexedTree the last node returned by the iterator.
     */
    public void remove() {
        // empty tree case
        if(currentNode == null || currentIndex == -1) {
            throw new NoSuchElementException();

        // last one in the tree
        } else if(host.size() == 1) {
            currentIndex = -1;
            currentNode = null;

        // first node in a significantly sized tree
        } else if(currentIndex == 0) {
            currentIndex = -1;
            IndexedTreeNode<V> nodeToRemove = currentNode;
            findNextNode();
            nodeToRemove.removeFromTree(host);

        // anywhere else in the tree
        } else {
            currentIndex--;
            IndexedTreeNode<V> nodeToRemove = currentNode;
            findPreviousNode();
            nodeToRemove.removeFromTree(host);
        }
    }

    /**
     * Adds a value into a new IndexedTreeNode after the last node returned.
     * <strong>Warning: this method should not be used if the tree is sorted.</strong>
     */
    public void addValue(V value) {
        // empty tree case
        if(currentNode == null || currentIndex == -1) {
            host.addByNode(0, value);

        // otherwise insert just after the current value
        } else {
            currentNode.insert(host, 1, value);
            currentIndex++;
            findNextNode();
        }
    }

    /**
     * Sets the value on the last IndexedTreeNode that was returned.
     * <strong>Warning: this method should not be used if the tree is sorted.</strong>
     */
    public void setValue(V value) {
        // empty tree case
        if(currentNode == null || currentIndex == -1) {
            throw new NoSuchElementException();

        // otherwise just set the value
        } else {
            currentNode.setValue(value);
        }
    }

    /**
     * Finds the next node in the tree.
     */
    private void findNextNode() {
        //  go into the right subtree for the next node
        if(currentNode.right != null) {
            currentNode = currentNode.right;
            while(currentNode.left != null) {
                currentNode = currentNode.left;
            }

        // go to the parent for the next node
        } else if(currentNode.parent.left == currentNode) {
            currentNode = currentNode.parent;

        // get out of the right subtree
        } else if(currentNode.parent.right == currentNode) {
            // move to the top of the current subtree
            while(currentNode.parent.right == currentNode) {
                currentNode = currentNode.parent;
            }
            // Move up one more node to leave the subtree
            currentNode = currentNode.parent;

        // the iterator is out of state
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Finds the previous node in the tree.
     */
    private void findPreviousNode() {
        //  go into the left subtree for the previous node
        if(currentNode.left != null) {
            currentNode = currentNode.left;
            while(currentNode.right != null) {
                currentNode = currentNode.right;
            }

        // go to the parent for the next node
        } else if(currentNode.parent.right == currentNode) {
            currentNode = currentNode.parent;

        // get out of the left subtree
        } else if(currentNode.parent.left == currentNode) {
            // move to the top of the current subtree
            while(currentNode.parent.left == currentNode) {
                currentNode = currentNode.parent;
            }
            // Move up one more node to leave the subtree
            currentNode = currentNode.parent;

        // the iterator is out of state
        } else {
            throw new IllegalStateException();
        }
    }
}