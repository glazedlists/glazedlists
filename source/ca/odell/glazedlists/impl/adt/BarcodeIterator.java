/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

import java.util.*;

/**
 * A BarcodeIterator is a specialized {@link Iterator} implementation for moving
 * over a Barcode efficiently.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class BarcodeIterator implements Iterator {

    /** keep a reference for removes in the trailing whitespace */
    private Barcode barcode = null;

    /** the current node being inspected */
    private BarcodeNode currentNode = null;

    /** the number of requests on the current node */
    private int localIndex = -1;

    /** the number of black elements before this node */
    private int blackSoFar = 0;

    /** the number of white elements before this node */
    private int whiteSoFar = 0;

    /**
     * Creates a new Iterator for the given Barcode.
     */
    BarcodeIterator(Barcode barcode) {
        BarcodeNode root = barcode.getRootNode();
        // move the Iterator to the start position.
        if(root != null) {
            currentNode = root;
            while(currentNode.left != null) {
                currentNode = currentNode.left;
            }
        }
        this.barcode = barcode;
    }

    /**
     * Returns whether or not there are more values in the SparseList to
     * iterate over.
     */
    public boolean hasNext() {
        if(getIndex() == barcode.size() - 1) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if there are more BLACK elements in the {@link Barcode} to
     * move the {@link Iterator} to.
     */
    public boolean hasNextBlack() {
        if(getIndex() >= barcode.treeSize() - 1) return false;
        else if(currentNode == null) return false;
        else return true;
    }

    /**
     * Returns true if there are more WHITE elements in the {@link Barcode} to
     * move the {@link Iterator} to.
     */
    public boolean hasNextWhite() {
        if(barcode.size() != barcode.treeSize()) return hasNext();
        else if(currentNode == null) return false;
        else if(localIndex < currentNode.whiteSpace - 1 || whiteSoFar + currentNode.whiteSpace < barcode.whiteSize()) return true;
        else return false;
    }

    /**
     * Returns true if there are more elements in the {@link Barcode} to
     * move the {@link Iterator} to that match the provided colour.
     */
    public boolean hasNextColour(Object colour) {
        if(colour == Barcode.BLACK) return hasNextBlack();
        return hasNextWhite();
    }

    /**
     * Gets the next value in this SparseList.
     */
    public Object next() {
        // iterate on this node
        localIndex++;
        // handle the empty tree case
        if(currentNode == null) {
            // beyond the tree in the trailing whitespace
            if(getIndex() < barcode.size()) {
                return Barcode.WHITE;

            // at the end of the list
            } else {
                throw new NoSuchElementException();
            }

        // at the edge of the current node
        } else if(localIndex >= currentNode.whiteSpace + currentNode.rootSize) {
            // move to the next node
            if(getIndex() < barcode.treeSize()) {
                blackSoFar += currentNode.rootSize;
                whiteSoFar += currentNode.whiteSpace;
                findNextNode();
                localIndex = 0;

            // act on the trailing whitespace
            } else {
                // beyond the tree in the trailing whitespace
                if(getIndex() < barcode.size()) {
                    return Barcode.WHITE;

                // at the end of the list
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        // next() was a WHITE value
        if(localIndex < currentNode.whiteSpace) {
            return Barcode.WHITE;

        // next() was a BLACK value
        } else {
            return Barcode.BLACK;
        }
    }

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that is BLACK.
     *
     * @throws NoSuchElementException if hasNextBlack() returns false.
     */
    public Object nextBlack() {
        // iterate on this node
        localIndex++;
        // handle the empty tree case
        if(currentNode == null) {
            throw new NoSuchElementException();

        // currently in the whitespace of this node
        } else if(localIndex < currentNode.whiteSpace) {
            localIndex = currentNode.whiteSpace;

        // at the edge of the current node
        } else if(localIndex >= currentNode.whiteSpace + currentNode.rootSize) {
            // move to the next node
            if(getIndex() < barcode.treeSize()) {
                whiteSoFar += currentNode.whiteSpace;
                blackSoFar += currentNode.rootSize;
                findNextNode();
                localIndex = currentNode.whiteSpace;

            // act on the trailing whitespace
            } else {
                throw new NoSuchElementException();
            }
        }
        assert(localIndex >= currentNode.whiteSpace);
        return Barcode.BLACK;
    }

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that is WHITE.
     *
     * @throws NoSuchElementException if hasNextWhite() returns false.
     */
    public Object nextWhite() {
        // iterate on this node
        localIndex++;
        // handle the empty tree case
        if(currentNode == null) {
            // beyond the tree in the trailing whitespace
            if(getIndex() < barcode.size()) {
                return Barcode.WHITE;

            // at the end of the list
            } else {
                throw new NoSuchElementException();
            }

        // at the edge of the current node
        } else if(localIndex >= currentNode.whiteSpace) {
            // move to the trailing whitespace
            if(getIndex() < barcode.treeSize() && getIndex() + currentNode.rootSize >= barcode.treeSize()) {
                localIndex = currentNode.whiteSpace;
                localIndex += currentNode.rootSize;
            }

            // move to the next node
            if(getIndex() < barcode.treeSize()) {
                blackSoFar += currentNode.rootSize;
                whiteSoFar += currentNode.whiteSpace;
                findNextNode();
                localIndex = 0;

            // act on the trailing whitespace
            } else {
                // beyond the tree in the trailing whitespace
                if(getIndex() < barcode.size()) {
                    return Barcode.WHITE;

                // at the end of the list
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
        assert(localIndex < currentNode.whiteSpace);
        return Barcode.WHITE;
    }

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that matches the provided colour.
     *
     * @throws NoSuchElementException if hasNextColour(colour) returns false.
     */
    public Object nextColour(Object colour) {
        if(colour == Barcode.BLACK) return nextBlack();
        return nextWhite();
    }

    /**
     * Removes the current value at the Iterator from the {@link Barcode}.
     */
    public void remove() {
        // Fast fail if the Iterator isn't set up right yet
        if(localIndex == -1) {
            throw new NoSuchElementException("Cannot call remove() before next() is called.");

        // Removing from the trailing whitespace
        } else if(currentNode == null || getIndex() >= barcode.treeSize()) {
            barcode.remove(getIndex(), 1);
            localIndex--;

        // Removing from the tree
        } else {
            BarcodeNode affectedNode = currentNode;
            // The currentNode gets compressed to the left
            if(localIndex == 0 && currentNode.whiteSpace == 1 && getIndex() != 0) {
                findPreviousNode();
                blackSoFar -= currentNode.rootSize;
                whiteSoFar -= currentNode.whiteSpace;
                localIndex += currentNode.whiteSpace + currentNode.rootSize;

            // The currentNode gets compressed to the right
            } else if(localIndex == currentNode.whiteSpace && currentNode.rootSize == 1) {
                // The only node in the tree is going to be unlinked
                if(localIndex == barcode.treeSize() - 1) {
                    currentNode = null;

                // This was the end of the tree go to trailing whitespace
                } else if(getIndex() == barcode.treeSize() - 1) {
                    findPreviousNode();
                    blackSoFar -= currentNode.rootSize;
                    whiteSoFar -= currentNode.whiteSpace;
                    localIndex += currentNode.whiteSpace + currentNode.rootSize;

                // There is a node after this one so go to it
                } else {
                    findNextNode();
                }
            }
            // Remove the value
            affectedNode.removeBaseCase(getIndex(), localIndex);
            localIndex--;
        }
    }

    /**
     * Sets the most recently viewed element to WHITE and returns the white-centric
     * index of the element after the set is complete.
     */
    public int setWhite() {
        // Fast fail for a non-existant element
        if(localIndex == -1) {
            throw new NoSuchElementException("Cannot call setWhite() before next() is called.");

        // No-op for a WHITE element
        } else if(currentNode == null || getIndex() >= barcode.treeSize() || localIndex < currentNode.whiteSpace) {
            return getWhiteIndex();

        // Not at the end of the tree
        } else if(getIndex() != barcode.treeSize() - 1) {
            // This whole node gets compressed right
            if(currentNode.rootSize == 1) {
                BarcodeNode affectedNode = currentNode;
                findNextNode();
                affectedNode.setWhite(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                if(currentNode.whiteSpace == 0 && currentNode.rootSize == 0) currentNode = affectedNode;
                return whiteSoFar + localIndex;

            // Special case where set just changes values on this node
            } else if(localIndex == currentNode.whiteSpace) {
                currentNode.setWhite(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                return whiteSoFar + localIndex;

            // Create a new node or part of this node will compress right
            } else {
                currentNode.setWhite(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                blackSoFar += currentNode.rootSize;
                whiteSoFar += currentNode.whiteSpace;
                findNextNode();
                localIndex = 0;
                return whiteSoFar;
            }

        // Set causes multiple values to move to the trailing whitespace
        } else if(currentNode.rootSize == 1) {
            BarcodeNode affectedNode = currentNode;
            // This was the last node in the tree and now it's gone
            if(currentNode.whiteSpace + 1 == barcode.treeSize()) {
                currentNode = null;
                affectedNode.setWhite(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                return localIndex;

            // There are more nodes before this one
            } else {
                findPreviousNode();
                int currentLocalIndex = localIndex;
                blackSoFar -= currentNode.rootSize;
                whiteSoFar -= currentNode.whiteSpace;
                localIndex += currentNode.whiteSpace + currentNode.rootSize;
                affectedNode.setWhite(getIndex(), currentLocalIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                if(currentNode.whiteSpace == 0 && currentNode.rootSize == 0) currentNode = affectedNode;
                return whiteSoFar + currentNode.whiteSpace;
            }

        // Setting only one element into the trailing whitespace
        } else {
            currentNode.setWhite(getIndex(), localIndex, 1);
            if(barcode.getRootNode() != null) barcode.treeSizeChanged();
            return whiteSoFar + localIndex - currentNode.rootSize;
        }
    }

    /**
     * Sets the most recently viewed element to BLACK and returns the white-centric
     * index of the element after the set is complete.
     */
    public int setBlack() {
        // Fast fail for a non-existant element
        if(localIndex == -1) {
            throw new NoSuchElementException("Cannot call setBlack() before next() is called.");

        // Set in the trailing whitespace without a tree
        } else if(currentNode == null) {
            barcode.setBlack(getIndex(), 1);
            currentNode = barcode.getRootNode();
            return 0;

        // Set at the edge of the trailing whitespace
        } else if(getIndex() == barcode.treeSize()) {
            barcode.setBlack(getIndex(), 1);
            if(barcode.getRootNode() != null) barcode.treeSizeChanged();

        // Set within the trailing whitespace
        } else if(getIndex() > barcode.treeSize()) {
            barcode.setBlack(getIndex(), 1);
            if(barcode.getRootNode() != null) barcode.treeSizeChanged();
            whiteSoFar += currentNode.whiteSpace;
            blackSoFar += currentNode.rootSize;
            localIndex -= currentNode.whiteSpace + currentNode.rootSize;
            findNextNode();

        // Node gets compressed to the left
        } else if(localIndex < currentNode.whiteSpace && currentNode.whiteSpace == 1) {
            // At the start of the tree, no compression
            if(getIndex() == 0) {
                currentNode.setBlack(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();

            // Either this node or the node before it is about to disappear
            } else {
                BarcodeNode affectedNode = currentNode;
                findPreviousNode();
                int currentLocalIndex = localIndex;
                blackSoFar -= currentNode.rootSize;
                whiteSoFar -= currentNode.whiteSpace;
                localIndex += currentNode.whiteSpace + currentNode.rootSize;
                affectedNode.setBlack(getIndex(), currentLocalIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                if(currentNode.whiteSpace == 0 && currentNode.rootSize == 0) currentNode = affectedNode;
            }

        // Any other WHITE element set to BLACK
        } else if(localIndex < currentNode.whiteSpace) {
            // Element gets compressed left
            if(localIndex == 0) {
                currentNode.setBlack(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                blackSoFar++;
                localIndex--;
                return blackSoFar - 1;

            // Element is at the edge of the existing BLACK
            } else if(localIndex == currentNode.whiteSpace - 1) {
                currentNode.setBlack(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                return blackSoFar;

            // Created a new node to the left
            } else {
                currentNode.setBlack(getIndex(), localIndex, 1);
                if(barcode.getRootNode() != null) barcode.treeSizeChanged();
                whiteSoFar += localIndex;
                blackSoFar++;
                localIndex = -1;
                return blackSoFar - 1;
            }
        }
        return getBlackIndex();
    }

    /**
     * Sets the most recently viewed element to the value of colour and returns
     * the colour specific index of the element after the set is complete.
     */
    public int set(Object colour) {
        if(colour == Barcode.BLACK) return setBlack();
        return setWhite();
    }

    /**
     * Gets the index of the last element visited.
     */
    public int getIndex() {
        return blackSoFar + whiteSoFar + localIndex;
    }

    /**
     * Gets the black-centric index of the last element visited or -1 if that
     * element is white.
     */
    public int getBlackIndex() {
        if(localIndex == -1) {
            return blackSoFar - 1;
        } else if(currentNode == null || localIndex < currentNode.whiteSpace
            || localIndex >= currentNode.whiteSpace + currentNode.rootSize) {
            return -1;
        }
        return blackSoFar + localIndex - currentNode.whiteSpace;
    }

    /**
     * Gets the white-centric index of the last element visited or -1 if that
     * element is black.
     */
    public int getWhiteIndex() {
        if(currentNode == null) {
            if(localIndex == -1 && whiteSoFar != 0) return whiteSoFar - 1;
            else return localIndex;
        } else if(localIndex >= currentNode.whiteSpace
            && localIndex < currentNode.whiteSpace + currentNode.rootSize) return -1;
        else if(localIndex >= currentNode.whiteSpace + currentNode.rootSize) return whiteSoFar + localIndex - currentNode.rootSize;
        return whiteSoFar + localIndex;
    }

    /**
     * Gets the colour-centric index of the last element based on the value of
     * colour.  If the element didn't match the colour specified, this method
     * returns -1.
     */
    public int getColourIndex(Object colour) {
        if(colour == Barcode.WHITE) return getWhiteIndex();
        return getBlackIndex();
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