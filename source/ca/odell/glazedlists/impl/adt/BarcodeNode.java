/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

// for iterators
import java.util.*;

/**
 * A BarcodeNode models a node in an Barcode.  This class
 * does the bulk of the heavy lifting for Barcode.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 *
 */
final class BarcodeNode {

    /** the parent node */
    BarcodeNode parent;

    /** the tree that this node is a member of */
    private Barcode host;

    /** the left and right child nodes */
    BarcodeNode left = null;
    BarcodeNode right = null;

    /** the size of the black portion of the left and right subtrees */
    private int blackLeftSize = 0;
    private int blackRightSize = 0;

    /** the total size of the left and right subtrees */
    private int treeLeftSize = 0;
    private int treeRightSize = 0;

    /** the amount of empty space that precedes this node */
    int whiteSpace = 0;

    /** the number of values represented by this node */
    int rootSize = 1;

    /** the height of this subtree */
    private int height = 1;

    /**
     * Creates a new BarcodeNode with the specified parent node and host tree.
     */
    private BarcodeNode(Barcode host, BarcodeNode parent) {
        this.host = host;
        this.parent = parent;
    }

    /**
     * This is a convenience constructor for creating a new BarcodeNode
     * with a given number of values and amount of preceding empty space.
     */
    BarcodeNode(Barcode host, BarcodeNode parent, int values, int whiteSpace) {
        this(host, parent);
        this.whiteSpace = whiteSpace;
        this.rootSize = values;
    }

    /**
     * Returns the size of the subtree rooted at this node
     */
    int size() {
        return treeLeftSize + whiteSpace + rootSize + treeRightSize;
    }

    /**
     * Returns the size of the black portion of the subtree rooted at this
     */
    int blackSize() {
        return blackLeftSize + rootSize + blackRightSize;
    }

    /**
     * Returns the size of the white portion of the subtree rooted at this
     */
    int whiteSize() {
        return (treeLeftSize - blackLeftSize) + whiteSpace + (treeRightSize - blackRightSize);
    }

    /**
     * Inserts multiple values into the host tree
     */
    void insertBlack(int index, int length) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left adjusting sizes as you go
        if(localIndex < 0) {
            blackLeftSize += length;
            treeLeftSize += length;
            left.insertBlack(index, length);

        // Recurse to the Right adjusting sizes as you go
        } else if(localIndex > whiteSpace + rootSize) {
            blackRightSize += length;
            treeRightSize += length;
            right.insertBlack(localIndex - whiteSpace - rootSize, length);

        // The new values should be compressed into this node
        } else if(localIndex == whiteSpace + rootSize) {
            rootSize += length;

        // Insert in the middle of the empty space
        } else if(localIndex < whiteSpace) {
            whiteSpace -= localIndex;
            blackLeftSize += length;
            treeLeftSize += localIndex + length;
            if(left == null) {
                left = new BarcodeNode(host, this, length, localIndex);
                ensureAVL();
            } else {
                left.insertBlackAtEnd(length, localIndex);
            }

        // Insert within this node
        } else {
            rootSize += length;
        }
    }

    /**
     * Inserts a value at the end of the tree rooted at this.
     */
    void insertBlackAtEnd(int values, int leadingWhite) {
        // Recurse to the right
        if(right != null) {
            blackRightSize += values;
            treeRightSize += values + leadingWhite;
            right.insertBlackAtEnd(values, leadingWhite);

        // Insert on the right
        } else {
            if(leadingWhite == 0) {
               rootSize += values;
            } else {
                blackRightSize += values;
                treeRightSize += values + leadingWhite;
                right = new BarcodeNode(host, this, values, leadingWhite);
                ensureAVL();
            }
        }
    }

    /**
     * Inserts multiple null values as empty space in the host tree.
     */
    void insertWhite(int index, int length) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) {
            treeLeftSize += length;
            left.insertWhite(index, length);

        // Recurse to the Right
        } else if(localIndex > whiteSpace + rootSize - 1) {
            treeRightSize += length;
            right.insertWhite(localIndex - whiteSpace - rootSize, length);

        // Insert in the whitespace for this node
        } else if(localIndex <= whiteSpace) {
            whiteSpace += length;

        // Insert within this node
        } else {
            localIndex -= whiteSpace;
            int movingRoot = rootSize - localIndex;
            rootSize  = localIndex;
            blackRightSize += movingRoot;
            treeRightSize += movingRoot + length;

            if(right == null) {
                right = new BarcodeNode(host, this, movingRoot, length);
                ensureAVL();
            } else {
                BarcodeNode node = new BarcodeNode(host, null, movingRoot, length);
                right.moveToSmallest(node);
            }
        }
    }

    /**
     * Moves a given node to be the smallest node in the subtree rooted at
     * this.
     */
    private void moveToSmallest(BarcodeNode movingNode) {
        // Recurse to the left
        if(left != null) {
            blackLeftSize += movingNode.rootSize;
            treeLeftSize += movingNode.whiteSpace + movingNode.rootSize;
            left.moveToSmallest(movingNode);

        // Add the node as a left child of this
        } else {
            // This node will be compressed now
            if(whiteSpace == 0) {
                rootSize += movingNode.rootSize;
                whiteSpace += movingNode.whiteSpace;
                movingNode.clear();

            // Add the moving node on the left
            } else {
                blackLeftSize += movingNode.rootSize;
                treeLeftSize += movingNode.whiteSpace + movingNode.rootSize;
                movingNode.parent = this;
                left = movingNode;
                ensureAVL();
            }
        }
    }

    /**
     * Gets the white-centric index from the given list index or returns -1
     * if that list index has a value of <code>Barcode.BLACK</code>.
     */
    int getWhiteIndex(int index) {
        return getWhiteIndex(index, 0);
    }
    private int getWhiteIndex(int index, int accumulation) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) return left.getWhiteIndex(index, accumulation);

        // Recurse to the Right
        else if(localIndex > whiteSpace + rootSize - 1) {
            accumulation += (treeLeftSize - blackLeftSize) + whiteSpace;
            return right.getWhiteIndex(localIndex - whiteSpace - rootSize, accumulation);

        // Get the white index from this node
        } else if(localIndex < whiteSpace) return accumulation + (treeLeftSize - blackLeftSize) + localIndex;

        // Get the white index from the black portion of this node
        else return -1;
    }

    /**
     * Gets the black-centric index from the given list index or returns -1
     * if that list index has a value of <code>Barcode.WHITE</code>.
     */
    int getBlackIndex(int index) {
        return getBlackIndex(index, 0);
    }
    private int getBlackIndex(int index, int accumulation) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) return left.getBlackIndex(index, accumulation);

        // Recurse to the Right
        else if(localIndex > whiteSpace + rootSize - 1) {
            return right.getBlackIndex(localIndex - whiteSpace - rootSize, accumulation + blackLeftSize + rootSize);

        // Get the black index from the white portion of this node
        } else if(localIndex < whiteSpace) return -1;

        // Get the black index from this node
        else return accumulation + blackLeftSize + localIndex - whiteSpace;
    }

    /**
     * Gets the white-centric index from the given list index.
     *
     * @param lead true for an index with a value of Barcode.BLACK to return
     *      the white-centric index of the previous white value in the Barcode.
     *      False for an index with a value of Barcode.BLACK to return
     *      the white-centric index of the next white value in the Barcode.
     */
    public int getWhiteIndex(int index, boolean lead) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) return left.getWhiteIndex(index, lead);

        // Recurse to the Right
        else if(localIndex > whiteSpace + rootSize - 1) {
            return right.getWhiteIndex(localIndex - whiteSpace - rootSize, lead) + treeLeftSize - blackLeftSize + whiteSpace;

        // Get the white index from within this node
        } else if(localIndex < whiteSpace) {
            return treeLeftSize - blackLeftSize + localIndex;

        // Get the white index based on lead
        } else {
            if(lead) return treeLeftSize - blackLeftSize + whiteSpace - 1;
            return treeLeftSize - blackLeftSize + whiteSpace;
        }
    }

    /**
     * Gets the black-centric index from the given list index.
     *
     * @param lead true for an index with a value of Barcode.WHITE to return
     *      the black-centric index of the previous black value in the Barcode.
     *      False for an index with a value of Barcode.WHITE to return
     *      the black-centric index of the next black value in the Barcode.
     */
    public int getBlackIndex(int index, boolean lead) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) return left.getBlackIndex(index, lead);

        // Recurse to the Right
        else if(localIndex > whiteSpace + rootSize - 1) {
            return right.getBlackIndex(localIndex - whiteSpace - rootSize, lead) + blackLeftSize + rootSize;

        // Get the black index based on lead
        } else if(localIndex < whiteSpace) {
            if(lead) return blackLeftSize - 1;
            return blackLeftSize;

        // Get the black index at this node
        } else return blackLeftSize + localIndex - whiteSpace;
    }

    /**
     * Gets the list index from a given white-centric index.
     */
    public int getIndexByWhiteIndex(int whiteIndex) {
        int localIndex = whiteIndex - (treeLeftSize - blackLeftSize);

        // Recurse to the Left
        if(localIndex < 0) return left.getIndexByWhiteIndex(whiteIndex);

        // Recurse to the Right
        else if(localIndex >= whiteSpace) {
            return right.getIndexByWhiteIndex(localIndex - whiteSpace)
                + treeLeftSize + whiteSpace + rootSize;

        // Get the list index from this node
        } else return treeLeftSize + localIndex;
    }

    /**
     * Gets the list index from a given black-centric index.
     */
    public int getIndexByBlackIndex(int blackIndex) {
        int localIndex = blackIndex - blackLeftSize;

        // Recurse to the Left
        if(localIndex < 0) return left.getIndexByBlackIndex(blackIndex);

        // Recurse to the Right
        else if(localIndex >= rootSize) {
            return right.getIndexByBlackIndex(localIndex - rootSize)
                + treeLeftSize + whiteSpace + rootSize;

        // Get the list index from this node
        } else return treeLeftSize + whiteSpace + localIndex;
    }

    /**
     * Gets the sequence relative index given a white-centric index.
     */
    public int getWhiteSequenceIndex(int whiteIndex) {
        int localIndex = whiteIndex - (treeLeftSize - blackLeftSize);

        // Recurse to the Left
        if(localIndex < 0) return left.getWhiteSequenceIndex(whiteIndex);

        // Recurse to the Right
        else if(localIndex >= whiteSpace) {
            return right.getWhiteSequenceIndex(localIndex - whiteSpace);

        // once the recursion is done you have the relative index
        } else return localIndex;
    }

    /**
     * This method exists for CollectionList which needs a way to call
     * getBlackIndex(index, true) with a white-centric index.
     */
    public int getBlackBeforeWhite(int whiteIndex) {
        int localIndex = whiteIndex - (treeLeftSize - blackLeftSize);

        // Recurse to the Left
        if(localIndex < 0) return left.getBlackBeforeWhite(whiteIndex);

        // Recurse to the Right
        else if(localIndex >= whiteSpace) {
            return right.getBlackBeforeWhite(localIndex - whiteSpace) + blackLeftSize + rootSize;

        // Get the black index before this node
        } else {
            return blackLeftSize - 1;
        }
    }

    /**
     * Finds a sequence of the given colour that is at least size elements
     * in length.
     *
     * @param size the minimum size of a matching sequence.
     *
     * @return The natural index of the first element in the sequence or -1 if
     *         no sequences of that length exist.
     */
    public int findSequenceOfMinimumSize(int size, Object colour) {
        return findFirstFitSequence(size, colour, 0);
    }
    /**
     * The depth-first, FIRST FIT implementation.
     */
    private int findFirstFitSequence(int size, Object colour, int accumulation) {
        int result = -1;

        // Recurse to the Left
        if(left != null) {
            result = left.findFirstFitSequence(size, colour, accumulation);
        }

        // Inspect this node
        if(result == -1) {
            // Looking for a WHITE sequence
            if(colour == Barcode.WHITE && size <= whiteSpace) {
                return accumulation + treeLeftSize;

            // Looking for a BLACK sequence
            } else if(colour == Barcode.BLACK && size <= rootSize) {
                return accumulation + treeLeftSize + whiteSpace;
            }
        }

        // Recurse to the Right
        if(result == -1 && right != null) {
            result = right.findFirstFitSequence(size, colour, accumulation + treeLeftSize + whiteSpace + rootSize);
        }

        return result;
    }

    /**
     * Sets the values from index to index + length.
     */
    void set(int index, Object value, int length) {
        if(length == 1) setBaseCase(index, index, value);
        else set(index, index, value, length);
    }
    private void set(int absoluteIndex, int localIndex, Object value, int length) {
        int localizedIndex = localIndex - treeLeftSize;

        // Recurse to the Left
        if(localizedIndex < 0) {
            left.set(absoluteIndex, localIndex, value, length);

        // Recurse to the Right
        } else if(localizedIndex > whiteSpace + rootSize - 1) {
            right.set(absoluteIndex, localizedIndex - whiteSpace - rootSize, value, length);

        // Set values on this node to white
        } else if(value == Barcode.WHITE) {
            setWhite(absoluteIndex, localizedIndex, length);

        // Set values on this node to black
        } else {
            setBlack(absoluteIndex, localizedIndex, length);
        }
    }

    void setWhite(int absoluteIndex, int localIndex, int length) {
        int endIndex = localIndex + length - 1;

        // Set only whitespace so no change at all
        if(endIndex < whiteSpace) {
            // Do Nothing

        // Set only within the black
        } else if(localIndex > whiteSpace - 1) {
            int rootChange = Math.min(length, whiteSpace + rootSize - localIndex);
            // This node will be removed
            if(rootSize == rootChange) {
                whiteSpace += rootChange;
                rootSize = 0;
                correctSizes(-rootChange, 0);
                unlink(absoluteIndex - localIndex);

            // Update root and add white space
            } else {
                rootSize -= rootChange;
                if(localIndex < whiteSpace + rootSize) {
                    correctSizes(-rootChange, 0);
                    insertWhite(localIndex + treeLeftSize, rootChange);
                } else {
                    correctSizes(-rootChange, -rootChange);
                    host.addWhite(absoluteIndex, rootChange);
                }
            }

            // Set is larger than just this node
            if(rootChange != length) {
                host.remove(absoluteIndex + rootChange, length - rootChange);
                host.addWhite(absoluteIndex + rootChange, length - rootChange);
            }

        // Set both black and white
        } else if(localIndex < whiteSpace + 1 && endIndex < whiteSpace + rootSize) {
            int rootChange = Math.min(length, whiteSpace + rootSize - localIndex) + (localIndex - whiteSpace);
            rootSize -= rootChange;
            whiteSpace += rootChange;
            correctSizes(-rootChange, 0);

        // Set this entire node to white
        } else {
            whiteSpace += rootSize;
            int localLength = whiteSpace - localIndex;
            unlink(absoluteIndex - localIndex);
            if(localLength != length) {
                host.remove(absoluteIndex + localLength, length - localLength);
                host.addWhite(absoluteIndex + localLength, length - localLength);
            }
        }
    }

    void setBlack(int absoluteIndex, int localIndex, int length) {
        int endIndex = localIndex + length - 1;
        int localLength = Math.min(length, whiteSpace + rootSize - localIndex);

        // Set only black so no change at this node
        if(localIndex > whiteSpace - 1) {
            // Do Nothing

        // Set some or all white to black
        } else if(endIndex > whiteSpace - 1) {
            int whiteChange = whiteSpace - localIndex;
            rootSize += whiteChange;
            whiteSpace -= whiteChange;
            correctSizes(whiteChange, 0);
            compressNode(absoluteIndex - localIndex);

        // Set within the whitespace
        } else {
            whiteSpace -= length;
            correctSizes(0, -length);
            host.addBlack(absoluteIndex, length);
            compressNode(absoluteIndex - localIndex);
        }

        // Remove/Add if the length spills over to another node
        if(localLength != length) {
            host.remove(absoluteIndex + localLength, length - localLength);
            host.addBlack(absoluteIndex + localLength, length - localLength);
        }
    }

    /**
     * Sets the value of the element at a given index.
     */
    private void setBaseCase(int absoluteIndex, int index, Object value) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) {
            left.setBaseCase(absoluteIndex, index, value);

        // Recurse to the Right
        } else if(localIndex > whiteSpace + rootSize) {
            right.setBaseCase(absoluteIndex, localIndex - whiteSpace - rootSize, value);

        // Edge case where leading white moves to this
        } else if(localIndex == whiteSpace + rootSize) {
            // Add the new value to this root
            if(value != Barcode.WHITE) {
                rootSize++;
                treeRightSize--;
                correctSizes(1, 0);
                right.setFirstNullToTrue(absoluteIndex, localIndex - whiteSpace - rootSize + 1);
            }

        // Set a value in the middle of the white space
        } else if(localIndex < whiteSpace) {
            if(value == Barcode.WHITE) return;
            whiteSpace--;
            correctSizes(1, 0);
            insertBlack(index, 1);
            compressNode(absoluteIndex);

        // Set a value at the leading edge of this node
        } else if(localIndex == whiteSpace) {
            if(value == Barcode.WHITE) {
                whiteSpace++;
                rootSize--;
                correctSizes(-1, 0);
                if(rootSize == 0) unlink(absoluteIndex - localIndex);
            }

        // Set a value at the trailing edge of this node
        } else if(localIndex == whiteSpace + rootSize - 1) {
            if(value == Barcode.WHITE) {
                rootSize--;
                if(right != null) {
                    treeRightSize++;
                    right.insertWhite(localIndex - whiteSpace - rootSize, 1);
                    correctSizes(-1, 0);

                } else if(parent != null && parent.left == this) {
                    parent.whiteSpace++;
                    parent.treeLeftSize--;
                    parent.correctSizes(true, -1, 0);

                } else {
                    correctSizes(-1, -1);
                    host.addWhite(absoluteIndex, 1);
                }
            }

        // Set the value in this node
        } else {
            if(value == Barcode.WHITE) {
                rootSize--;
                correctSizes(-1, 0);
                insertWhite(index, 1);
            }
        }
    }

    /**
     * A helper method for a base-case condition where the first null on a node
     * is set to a value.  This value is moved to the node that it is compressed
     * into before this method is called.  This method may result in further
     * compression.
     */
    private void setFirstNullToTrue(int absoluteIndex, int index) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) {
            treeLeftSize--;
            left.setFirstNullToTrue(absoluteIndex, index);

        // Recurse to the Right
        } else if(localIndex > whiteSpace + rootSize - 1) {
            treeRightSize--;
            right.setFirstNullToTrue(absoluteIndex, localIndex - whiteSpace - rootSize);

        // Affect this node
        } else {
            whiteSpace--;
            compressNode(absoluteIndex);
        }
    }

    /**
     * Removes the values from the given index to index + length
     */
    void remove(int index, int length) {
        if(length == 1) removeBaseCase(index, index);
        else remove(index, index, length);
    }
    private void remove(int absoluteIndex, int index, int length) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) {
            left.remove(absoluteIndex, index, length);

        // Recurse to the Right
        } else if(localIndex > whiteSpace + rootSize - 1) {
            right.remove(absoluteIndex, localIndex - whiteSpace - rootSize, length);

        } else {
            // Trim the length to only affect this node
            length = Math.min(localIndex + length, whiteSpace + rootSize) - localIndex;
            int endIndex = localIndex + length - 1;

            // Remove white and possibly some, but not all, black
            if(localIndex < whiteSpace && endIndex < whiteSpace + rootSize) {
                int whiteChange = Math.min(whiteSpace - localIndex, length);
                int blackChange = Math.max(endIndex - whiteSpace + 1, 0);
                whiteSpace -= whiteChange;
                rootSize -= blackChange;
                correctSizes(-blackChange, -(whiteChange + blackChange));
                compressNode(absoluteIndex - localIndex);

            // Remove only black
            } else if(localIndex > whiteSpace - 1) {
                // Remove all black so unlink this node
                if(length == rootSize) {
                    unlink(absoluteIndex - localIndex);

                // Only remove some of the black
                } else {
                    rootSize -= length;
                    correctSizes(-length, -length);
                }

            // Remove this entire node
            } else {
                int whiteChange = whiteSpace;
                int blackChange = rootSize;
                whiteSpace = 0;
                rootSize = 0;
                correctSizes(-blackChange, -(whiteChange + blackChange));
                unlink(absoluteIndex - localIndex);
            }
        }
    }

    /**
     * Removes the single value at a given index.
     */
    void removeBaseCase(int absoluteIndex, int index) {
        int localIndex = index - treeLeftSize;

        // Recurse to the Left
        if(localIndex < 0) {
            treeLeftSize--;
            left.removeBaseCase(absoluteIndex, index);

        // Recurse to the Right
        } else if(localIndex > whiteSpace + rootSize - 1) {
            treeRightSize--;
            right.removeBaseCase(absoluteIndex, localIndex - whiteSpace - rootSize);

        // Remove from the middle of the white space
        } else if(localIndex < whiteSpace) {
            whiteSpace--;
            compressNode(absoluteIndex);

        // Remove from the black portion of this node
        } else {
            rootSize--;
            if(rootSize == 0) {
                rootSize = 1;
                unlink(absoluteIndex - localIndex, false);
            } else correctSizes(-1, 0);
        }
    }

    /**
     * Unlinks this node from the tree and clears it.
     */
    private void unlink(int absoluteIndex) {
        unlink(absoluteIndex, true);
    }
    private void unlink(int absoluteIndex, boolean consistent) {

        // Two children exist
        if(right != null && left != null) {
            if(rootSize != 0) correctSizes(-rootSize, -rootSize, consistent);
            unlinkWithTwoChildren();

        // Only a right child exists
        } else if(right != null) {
            unlinkWithRightChild(consistent);

        // A left child or no child exists, which are handled almost the same way
        } else {
            BarcodeNode replacement = null;

            // Only a left child exists
            if(left != null) {
                replacement = left;
                replacement.parent = parent;

            // No children exist
            } else replacement = null;

            // Parent is null so significant empty space moves to the trailing nulls
            if(parent == null) {
                host.setRootNode(replacement);
                if(whiteSpace != 0) host.addWhite(host.size() + 1, whiteSpace);

            // This is a left child so empty space goes to the parent
            } else if(parent.left == this) {
                parent.whiteSpace += whiteSpace;
                parent.treeLeftSize -= whiteSpace;
                parent.left = replacement;
                parent.ensureAVL();
                if(rootSize != 0) parent.correctSizes(true, -rootSize, -rootSize, consistent);
                clear();

            // This is a right child so significant empty space must be reinserted
            } else {
                parent.right = replacement;
                parent.ensureAVL();
                if(whiteSpace != 0) {
                    parent.correctSizes(false, -rootSize, -(whiteSpace + rootSize), consistent);
                    host.addWhite(absoluteIndex, whiteSpace);
                } else if(rootSize != 0) {
                    parent.correctSizes(false, -rootSize, -rootSize, consistent);
                }
                clear();
            }
        }
    }

    /**
     * Unlinks this node in the special case where this node has both
     * a left and right child.
     */
    private void unlinkWithTwoChildren() {
        // Get the replacement from the right subtree
        BarcodeNode replacement = right.pruneSmallestChild();
        BarcodeNode repParent = replacement.parent;

        // Adjust sizes on this node
        whiteSpace += replacement.whiteSpace;
        rootSize = replacement.rootSize;
        treeRightSize -= replacement.whiteSpace + replacement.rootSize;
        blackRightSize -= replacement.rootSize;

        // The smallest node is the right child of this
        if(repParent == this) {
            right = replacement.right;
            if(right != null) right.parent = this;
            ensureAVL();

        //  The smallest node is a left child in the right subtree
        } else {
            // linking on the right subtree needs updating
            repParent.left = replacement.right;
            if(repParent.left != null) repParent.left.parent = repParent;
            repParent.ensureAVL();
        }
        replacement.clear();
    }

    /**
     * Unlinks a node that has only a right child
     */
    private void unlinkWithRightChild(boolean consistent) {
        whiteSpace += right.whiteSpace;
        int oldSize = rootSize;
        rootSize = right.rootSize;
        right.clear();
        right = null;
        blackRightSize = 0;
        treeRightSize = 0;
        height = 1;
        if(parent != null) {
            if(oldSize != 0) parent.correctSizes(parent.left == this, -oldSize, -oldSize, consistent);
            parent.ensureAVL();
        }
    }

    /**
     * Prunes and returns the smallest child of the subtree rooted at this.
     * Tree references are maintained out of necessity of the calling method,
     * but sizes in the subtree are corrected accordingly.
     */
    private BarcodeNode pruneSmallestChild() {
        // Recurse to the left
        if(left != null) {
            BarcodeNode prunedNode = left.pruneSmallestChild();
            blackLeftSize -= prunedNode.rootSize;
            treeLeftSize -= prunedNode.whiteSpace + prunedNode.rootSize;
            return prunedNode;

        // return this node
        } else return this;
    }

    /**
     * A method to corrects sizes taking into account that the state of the
     * cached tree sizes may be inconsistent from base-case set or remove.
     */
    private void correctSizes(int blackOffset, int totalOffset, boolean consistent) {
        if(consistent) correctSizes(blackOffset, totalOffset);
        else correctSizes(-1, totalOffset - blackOffset);
    }

    /**
     * A method to corrects sizes taking into account that the state of the
     * cached tree sizes may be inconsistent from base-case set or remove.
     */
    private void correctSizes(boolean leftChild, int blackOffset, int totalOffset, boolean consistent) {
        if(consistent) correctSizes(leftChild, blackOffset, totalOffset);
        else correctSizes(leftChild, -1, totalOffset - blackOffset);
    }

     /**
      * Corrects all of the cached sizes up the tree by the given offsets starting
      * at the parent if it exists.
      */
    private void correctSizes(int blackOffset, int totalOffset) {
        if(parent != null) parent.correctSizes(parent.left == this, blackOffset, totalOffset);
        else host.treeSizeChanged();
    }

    /**
     * Corrects all of the cached sizes up the tree by the given offsets starting
     * from this.
     */
    private void correctSizes(boolean leftChild, int blackOffset, int totalOffset) {
        // left subtree is smaller
        if(leftChild) {
            blackLeftSize += blackOffset;
            treeLeftSize += totalOffset;

        // right subtree is smaller
        } else {
            blackRightSize += blackOffset;
            treeRightSize += totalOffset;
        }

        // recurse up the tree to the root
        if(parent != null) parent.correctSizes(parent.left == this, blackOffset, totalOffset);

        // Notify the tree size has changed
        else host.treeSizeChanged();
    }

    /**
     * Clears this node and returns the value it had.
     */
    private void clear() {
        // clear the children
        left = null;
        blackLeftSize = 0;
        treeLeftSize = 0;
        right = null;
        blackRightSize = 0;
        treeRightSize = 0;

        // clear this node
        host = null;
        parent = null;
        whiteSpace = 0;
        rootSize = 0;
        height = -1;
    }

    /**
     * Replaces a given child with the replacement node
     */
    private void replace(BarcodeNode child, BarcodeNode replacement) {
        // replacing the left child
        if(child == left) left = replacement;

        // Replacing the right child
        else right = replacement;
    }

    /**
     * Attempts to compress the current node out of the tree if possible
     */
    private void compressNode(int absoluteIndex) {
        // Fast fail if this node cannot be compressed
        if(whiteSpace != 0) return;

        // This is the root
        if(parent == null) compressRoot(absoluteIndex);

        // This is a left child
        else if(parent.left == this) compressLeftChild(absoluteIndex);

        // This is a right child
        else compressRightChild(absoluteIndex);
    }

    /**
     * Compresses the root node
     */
    private void compressRoot(int absoluteIndex) {
        // Compress to the left
        if(left != null) {
            // special case that's really fast
            if(right == null) {
                left.rootSize += rootSize;
                left.parent = null;
                host.setRootNode(left);
                clear();
            } else {
                left.compressToTheRight(rootSize);
                blackLeftSize += rootSize;
                treeLeftSize += rootSize;
                rootSize = 0;
                unlink(absoluteIndex);
            }

        // The node is as compressed as possible
        } else {
            // Do Nothing
        }
    }

    /**
     * Compresses a node that is a left child
     */
    private void compressLeftChild(int absoluteIndex) {
        // Compress to the left
        if(left != null) {
            left.compressToTheRight(rootSize);
            blackLeftSize += rootSize;
            treeLeftSize += rootSize;
            rootSize = 0;
            unlink(absoluteIndex);

        // Painful re-addition case
        } else {
            // This is the first value, can't compress it
            if(absoluteIndex == 0) return;

            // move the right child onto the parent
            parent.left = right;
            if(right != null) parent.left.parent = parent;

            // fix tree state and re-add these values
            parent.correctSizes(true, -rootSize, -rootSize);
            parent.ensureAVL();
            host.addBlack(absoluteIndex - 1, rootSize);
            clear();
        }
    }

    /**
     * Compresses a node that is a right child
     */
    private void compressRightChild(int absoluteIndex) {
        // Compress to the parent
        if(left == null) {
            parent.blackRightSize -= rootSize;
            parent.treeRightSize -= rootSize;
            parent.rootSize += rootSize;
            rootSize = 0;
            unlink(absoluteIndex);

        // Compress to the left
        } else {
            left.compressToTheRight(rootSize);
            blackLeftSize += rootSize;
            treeLeftSize += rootSize;
            rootSize = 0;
            unlink(absoluteIndex);
        }
    }

    /**
     * Compresses the given values into the largest node in this subtree.
     */
    private void compressToTheRight(int values) {
        if(right != null) {
            blackRightSize += values;
            treeRightSize += values;
            right.compressToTheRight(values);
        } else {
            rootSize += values;
        }
    }

    /**
     * Ensures that the tree satisfies the AVL property.  It is sufficient to
     * recurse up the tree only as long as height recalculations are needed.
     * As such, this method is intended to be called only on a node whose height
     * may be out of sync due to an insertion or deletion.  For example, calling
     * this method on a leaf node will not guarantee that this tree satisfies the
     * AVL property as it will not recurse.
     */
    private void ensureAVL() {
        int oldHeight = height;
        recalculateHeight();
        avlRotate();

        // If adjustments were made, recurse up the tree
        if(height != oldHeight && parent != null) parent.ensureAVL();
    }

    /**
     * Recalculates the cached height at this level.
     */
    private void recalculateHeight() {
        int leftHeight = left == null ? 0 : left.height;
        int rightHeight = right == null ? 0 : right.height;
        height = 1 + Math.max(leftHeight, rightHeight);
    }

    /**
     * Determines if AVL rotations are required and performs them if they are.
     */
    private void avlRotate() {
        // look up the left and right heights
        int leftHeight = (left != null ? left.height : 0);
        int rightHeight = (right != null ? right.height : 0);

        // rotations will be on the left
        if(leftHeight - rightHeight >= 2) {
            // determine if a double rotation is necessary
            int leftLeftHeight = (left.left != null ? left.left.height : 0);
            int leftRightHeight = (left.right != null ? left.right.height : 0);

            // Perform first half of double rotation if necessary
            if(leftRightHeight > leftLeftHeight) left.rotateRight();

            // Do the rotation for this node
            rotateLeft();

        // rotations will be on the right
        } else if(rightHeight - leftHeight >= 2) {
            // determine if a double rotation is necessary
            int rightLeftHeight = (right.left != null ? right.left.height : 0);
            int rightRightHeight = (right.right != null ? right.right.height : 0);

            // Perform first half of double rotation if necessary
            if(rightLeftHeight > rightRightHeight) right.rotateLeft();

            // Do the rotation for this node
            rotateRight();
        }
    }

    /**
     * AVL-Rotates this subtree with its left child.
     */
    private void rotateLeft() {
        // The replacement node is on the left
        BarcodeNode replacement = left;

        // take the right child of the replacement as my left child
        left = replacement.right;
        blackLeftSize = replacement.blackRightSize;
        treeLeftSize = replacement.treeRightSize;
        if(replacement.right != null) replacement.right.parent = this;

        // set the right child of the replacement to this
        replacement.right = this;
        replacement.blackRightSize = blackSize();
        replacement.treeRightSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replace(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        // fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    /**
     * AVL-Rotates this subtree with its right child.
     */
    private void rotateRight() {
        // The replacement node is on the right
        BarcodeNode replacement = right;

        // take the left child of the replacement as my right child
        right = replacement.left;
        blackRightSize = replacement.blackLeftSize;
        treeRightSize = replacement.treeLeftSize;
        if(replacement.left != null) replacement.left.parent = this;

        // set the left child of the replacement to this
        replacement.left = this;
        replacement.blackLeftSize = blackSize();
        replacement.treeLeftSize = size();

        // set the replacement's parent to my parent and mine to the replacement
        if(parent != null) parent.replace(this, replacement);

        // set a new tree root
        else host.setRootNode(replacement);

        // fix parent links on this and the replacement
        replacement.parent = parent;
        parent = replacement;

        // recalculate height at this node
        recalculateHeight();

        // require height to be recalculated on the replacement node
        replacement.height = 0;
    }

    public String toString() {
        return "[ " + left + " ("+ blackLeftSize +", " +treeLeftSize+")"
            +" <"+whiteSpace+"> " + rootSize +" <"+height+"> "
            +"(" + blackRightSize +", " +treeRightSize+") " + right + " ]";
    }

    /**
     * Validates this node's state
     */
    public void validate() {
        validateLineage();
        validateHeight();
        validateTreeSize();
        validateBlackSize();
        validateCompression();
        validateRootSize();
    }

    private int validateBlackSize() {
        int leftTreeSize = left == null ? 0 : left.validateBlackSize();
        int rightTreeSize = right == null ? 0 : right.validateBlackSize();

        if(leftTreeSize != blackLeftSize) throw new IllegalStateException("Black Size Validation Failure in Left Subtree\n" + "Expected: " + leftTreeSize + "\nActual: " + blackLeftSize + "\n" + this);
        if(rightTreeSize != blackRightSize) throw new IllegalStateException("Black Size Validation Failure in Right Subtree\n" + "Expected: " + rightTreeSize + "\nActual: " + blackRightSize + "\n" + this);

        return leftTreeSize + rightTreeSize + rootSize;
    }

    private int validateHeight() {
        int leftHeight = left == null ? 0 : left.validateHeight();
        int rightHeight = right == null ? 0 : right.validateHeight();

        // Validate that height is accurate at all
        if(height != 1 + Math.max(leftHeight, rightHeight)) throw new IllegalStateException("Height Validation Failure\n" + "Expected: " + (1 + Math.max(leftHeight, rightHeight)) + "\nActual: " + height + "\n" + this);

        // Validate that height meets the AVL property
        if(Math.abs(leftHeight - rightHeight) > 1) throw new IllegalStateException("AVL Property Validation Failure\n" + this);

        return 1 + Math.max(leftHeight, rightHeight);
    }

    private void validateLineage() {
        if(left != null) {
            if(left.parent != this) throw new IllegalStateException("Lineage Validation Failure\n" + "Left child is orphaned :\n" + left);
            left.validateLineage();
        }
        if(right != null) {
            if(right.parent != this) throw new IllegalStateException("Lineage Validation Failure\n" + "Right child is orphaned :\n" + right);
            right.validateLineage();
        }
    }

    private void validateCompression() {
        if(left != null) left.validateCompression();
        if(right != null) right.validateCompression();

        if(whiteSpace == 0 && getIndexForValidation() != 0) throw new IllegalStateException("Compression Validation Failure\n" + "The following node was found that could be compressed: \n" + this);
    }

    private int validateTreeSize() {
        int leftTreeSize = left == null ? 0 : left.validateTreeSize();
        int rightTreeSize = right == null ? 0 : right.validateTreeSize();

        if(treeLeftSize != leftTreeSize) throw new IllegalStateException("Tree Size Validation Failure\n" + "The following node was found that had a tree size failure on the left subtree: \n" + this);
        if(treeRightSize != rightTreeSize) throw new IllegalStateException("Tree Size Validation Failure\n" + "The following node was found that had a tree size failure on the right subtree: \n" + this);

        return treeLeftSize + whiteSpace + rootSize + treeRightSize;
    }

    /**
     * Gets the index of the first element on this node.  This is the index of
     * the first WHITE element (or first BLACK if there is no whitespace on this node)
     * indexed by this node.
     */
    private int getIndexForValidation() {
        if(parent != null) return parent.getIndexForValidation(this) + treeLeftSize;
        return treeLeftSize;
    }
    private int getIndexForValidation(BarcodeNode child) {
        // the child is on the left, return the index recursively
        if(child == left) {
            if(parent != null) return parent.getIndexForValidation(this);
            return 0;

        // the child is on the right, return the index recursively
        } else {
            if(parent != null) return parent.getIndexForValidation(this) + treeLeftSize + whiteSpace + rootSize;
            return treeLeftSize + whiteSpace + rootSize;
        }
    }

    private void validateRootSize() {
        if(left != null) left.validateRootSize();
        if(right != null) right.validateRootSize();

        if(rootSize == 0) throw new IllegalStateException("Root Size Validation Failure\n" + "A node was found with a root size of zero.");
    }
}