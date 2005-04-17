/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

import java.util.*;

/**
 * A Barcode is an ADT to replace the more general CompressableList
 * ADT.  CompressableList provides list compression capabilites that allow
 * a list to be accessed by both the real index and a compressed index.
 * The compressed index corresponds to the index of the current value as
 * though no nulls existed in the list.
 *
 * <p>This provides a huge performance boost over ArrayList on partially empty
 * lists.  However, the CompressableList is one of the volatile implementation
 * classes for internal development and isn't the best structure for the current
 * usage.  The GlazedLists use CompressableList to store only three values:
 * Boolean.TRUE, Boolean.FALSE, and null.  As such, it was slower and more
 * memory intensive than it could be due to its general purpose design.
 *
 * <p>The Barcode is designed such that a list of n elements of the same
 * colour will contain at most one node for BLACK and no nodes for WHITE.
 * This will improve the performance and scalability of the GlazedLists
 * which currently make use of CompressableList.
 *
 * <p>Barcode does not support more than two values stored in the list.
 * Three different values are used by one of the GlazedLists at this time.
 * Until UniqueList is refactored to make use of only two values, Barcode
 * cannot completely replace CompressableList and they will exist in parallel.
 *
 * <p>In an effort to maximize performance this ADT does NOT validate that arguments
 * passed to methods are valid in any way.  While this adds inherent risk to
 * the use of this code, this is a volatile implementation class.  As such, it
 * should only be used for internal GlazedList development.  It is up to the
 * calling code to do any argument validation which may be necessary.
 *
 * <p>Every effort has been made to squeeze the highest performance and smallest
 * footprint out of this data structure.  These benefits hopefully don't come at
 * the cost of code clarity or maintainability.  The memory usage of this ADT
 * is bound to the number of sequences of BLACK elements.  WHITE elements
 * have no memory impact on the data structure.
 *
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 *
 */
public final class Barcode {

    /** barcode colour constants */
    public static final Object WHITE = Boolean.FALSE;
    public static final Object BLACK = Boolean.TRUE;

    /** the root of the underlying tree */
    private BarcodeNode root = null;

    /** the size of the trailing whitespace */
    private int whiteSpace = 0;

    /** the size of tree */
    private int treeSize = 0;

    /**
     * Prints internal debug information for this barcode
     */
    public void printDebug() {
        System.out.println("\nTotal Size: " + size());
        System.out.println("Trailing Whitespace : " + whiteSpace);
        System.out.println("Tree Size: " + treeSize);
        System.out.println("Tree Structure:\n" + root);
    }

    /**
     * Validates the barcode's internal structure
     */
    public void validate() {
        if(root != null) root.validate();
    }

    /**
     * Gets the size of this barcode
     */
    public int size() {
        return treeSize + whiteSpace;
    }

    /**
     * Whether or not this barcode is empty
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Gets the size of the white portion of this barcode
     */
    public int whiteSize() {
        return root == null ? whiteSpace : root.whiteSize() + whiteSpace;
    }

    /**
     * Gets the size of the black portion of this barcode
     */
    public int blackSize() {
        return root == null ? 0 : root.blackSize();
    }

    /**
     * Gets the size of the given colour portion of this barcode
     */
    public int colourSize(Object colour) {
        if(colour == WHITE) return whiteSize();
        else return blackSize();
    }

    /**
     * Inserts a sequence of the specified colour into the barcode
     */
    public void add(int index, Object colour, int length) {
        if(colour == WHITE) addWhite(index, length);
        else addBlack(index, length);
    }

    /**
     * Inserts a sequence of white into the list
     */
    public void addWhite(int index, int length) {
        assert(length >= 0);
        if(length == 0) return;

        // Adding to the trailing whitespace
        if(root == null || index >= treeSize) {
            whiteSpace += length;

        // Adding whitespace to the actual list
        } else {
            root.insertWhite(index, length);
            treeSizeChanged();

        }
    }

    /**
     * Inserts a sequence of black into the list
     */
    public void addBlack(int index, int length) {
        assert(length >= 0);
        if(length == 0) return;

        // Make a new root
        if(root == null) {
            root = new BarcodeNode(this, null, length, index);
            treeSize = index + length;
            whiteSpace -= index;

        // Add in the trailing whitespace
        } else if(index >= treeSize) {
            int movingWhitespace = index - treeSize;
            whiteSpace -= movingWhitespace;
            root.insertBlackAtEnd(length, movingWhitespace);
            treeSizeChanged();

        // Add values to the actual list
        } else {
            root.insertBlack(index, length);
            treeSizeChanged();

        }
    }

    /**
     * Gets the value in this list at the given index
     */
    public Object get(int index) {
        if(getBlackIndex(index) == -1) return WHITE;
        return BLACK;
    }

    /**
     * Sets all of the values between index and index + length to either
     * WHITE or BLACK depending on the value of colour
     *
     * @param colour Determines which colour to set the values in the range
     * to.  Valid values for colour are <code>Barcode.WHITE</code> and
     * <code>Barcode.BLACK</code>.
     */
    public void set(int index, Object colour, int length) {
        assert(length >= 1);

        // The set affects the trailing whitespace
        int trailingChange = index > treeSize - 1 ? length : index + length - treeSize;
        if(trailingChange > 0) {
            if(colour == BLACK) {
                whiteSpace -= trailingChange;
                addBlack(index, trailingChange);
            }
            length -= trailingChange;
            if(length == 0) return;
        }

        // The set affects the list
        if(root != null) {
            root.set(index, colour, length);
            if(root != null) treeSizeChanged();
        }
    }

    /**
     * Sets all of the values between index and index + length to WHITE
     */
    public void setWhite(int index, int length) {
        set(index, WHITE, length);
    }

    /**
     * Sets all of the values between index and index + length to WHITE
     */
    public void setBlack(int index, int length) {
        set(index, BLACK, length);
    }

    /**
     * Removes the values from the given index to index + length
     */
    public void remove(int index, int length) {
        assert(length >= 1);

        // The remove affects the trailing whitespace
        int trailingChange = index > treeSize ? length : index + length - treeSize;
        if(trailingChange > 0) {
            whiteSpace -= trailingChange;
            length -= trailingChange;
        }

        // The remove occurs in the actual list
        if(root != null && index < treeSize) {
            int oldTreeSize = -1;
            while(length > 0) {
                oldTreeSize = treeSize;
                root.remove(index, length);
                if(root != null) treeSizeChanged();
                length -= (oldTreeSize - treeSize);
            }
            if(root != null) treeSizeChanged();
        }
    }

    /**
     * Clears the list
     */
    public void clear() {
        treeSize = 0;
        whiteSpace = 0;
        root = null;
    }

    /**
     * Gets the root for this Barcode.  This method is exposed for
     * Iterators on Barcode whose set() operations may create a
     * root node on a Barcode where none existed.
     */
    BarcodeNode getRootNode() {
        return root;
    }

    /**
     * Sets the root for this list.  This method is exposed for the
     * BarcodeNode in the event that the list's root is involved in
     * an AVL rotation.
     */
    void setRootNode(BarcodeNode root) {
        this.root = root;
        if(root == null) treeSize = 0;
    }

    /**
     * Gets the size of the underlying tree structure for this Barcode.  This
     * method is exposed for Iterators on Barcode who would otherwise have to
     * maintain the state of treeSize themselves.
     */
    int treeSize() {
        return treeSize;
    }

    /**
     * Notifies the list that the underlying list size has changed. This method
     * is exposed for BarcodeNode to propagate size adjustments.
     */
    void treeSizeChanged() {
        treeSize = root.size();
    }

    /**
     * Gets the real index of an element given the black index or white index.
     */
    public int getIndex(int colourIndex, Object colour) {
        // Get the real index of a WHITE element
        if(colour == WHITE) {
            // There are no black elements
            if(root == null) {
                return colourIndex;

            // Retrieving from the trailing whitespace with a tree
            } else if(colourIndex >= root.whiteSize()) {
                return colourIndex - root.whiteSize() + treeSize;

            // The index maps to an element in the tree
            } else {
                return root.getIndexByWhiteIndex(colourIndex);
            }

        // Get the real index of a BLACK element
        } else {
            return root.getIndexByBlackIndex(colourIndex);

        }
    }

    /**
     * Gets the colour-based index of the element with the given real
     * index.
     *
     * @param index the real index.
     * @param colour the colour to retrieve the colour-based index for.
     *
     * @return The colour-based index of the element at index or -1 if that
     *         element does not match the given colour.
     */
    public int getColourIndex(int index, Object colour) {
        if(colour == WHITE) return getWhiteIndex(index);
        else return getBlackIndex(index);
    }

    /**
     * Gets the white index of the node with the given real
     * index.
     *
     * @param index specifies the real index.
     *
     * @return The white index of the element at index or -1 if that element is BLACK.
     */
    public int getWhiteIndex(int index) {
        // Get a white index from the list
        if(root != null && index < treeSize) return root.getWhiteIndex(index);

        // There are only white indexes in the trailing whitespace
        else {
            if(root != null) return index - treeSize + root.whiteSize();
            else return index;
        }
    }

    /**
     * Gets the black index of the node with the given real
     * index.
     *
     * @param index specifies the real index.
     *
     * @return The black index of the element at index or -1 if that element is WHITE.
     */
    public int getBlackIndex(int index) {
        if(root != null && index < treeSize) return root.getBlackIndex(index);
        else return -1;
    }

    /**
     * Gets the colour-based index of the element with the given real index or
     * the colour-based index of the previous or next element matching the given
     * colour if that element is of the opposite colour.
     *
     * @param left true for opposite colour elements to return the colour-based
     *      index of the first matching element before it in the list. Such
     *      values will range from <code>-1</code> through <code>size()-1</code>.
     *      False for opposite colour elements to return the colour-based index
     *      of the first matching element after it in the list. Such values will
     *      range from <code>0</code> through <code>size()</code>.
     */
    public int getColourIndex(int index, boolean left, Object colour) {
        if(colour == WHITE) return getWhiteIndex(index, left);
        else return getBlackIndex(index, left);
    }

    /**
     * Gets the white index of the element with the given real index or
     * the white index of the previous or next WHITE element if that element
     * is BLACK.
     *
     * @param left true for BLACK elements to return the white index of the
     *      first WHITE element before it in the list. Such values will range
     *      from <code>-1</code> through <code>size()-1</code>. False for BLACK
     *      elements to return the white index of the first WHITE element after
     *      it in the list. Such values will range from <code>0</code> through
     *      <code>size()</code>.
     */
    public int getWhiteIndex(int index, boolean left) {
        if(root == null || index >= treeSize) return index;
        else return root.getWhiteIndex(index, left);
    }

    /**
     * Gets the black index of the element with the given real index or
     * the black index of the previous or next BLACK element if that element
     * is WHITE.
     *
     * @param left true for WHITE elements to return the black index of the
     *      first BLACK element before it in the list. Such values will range
     *      from <code>-1</code> through <code>size()-1</code>. False for WHITE
     *      elements to return the black index of the first BLACK element after
     *      it in the list. Such values will range from <code>0</code> through
     *      <code>size()</code>.
     */
    public int getBlackIndex(int index, boolean left) {
        // there is no tree
        if(root == null) {
            if(left) return -1;
            else return 0;

        // if it is beyond the tree
        } else if(index >= treeSize) {
            if(left) return root.blackSize() - 1;
            return root.blackSize();

        // get from the tree
        } else {
            return root.getBlackIndex(index, left);
        }
    }

    /**
     * Gets the index of the WHITE element at whiteIndex relative to the WHITE
     * element after the previous BLACK element or the start of the list if no
     * BLACK element exists before this node.
     */
    public int getWhiteSequenceIndex(int whiteIndex) {
        // There is no tree sequence is beyond the tree
        if(root == null) {
            return whiteIndex;

        // The sequence is beyond the tree
        } else if(whiteIndex >= root.whiteSize()) {
            return whiteIndex - root.whiteSize();

        // lookup the sequence index within the tree
        } else {
            return root.getWhiteSequenceIndex(whiteIndex);
        }
    }

    /**
     * This method exists for CollectionList which needs a way to call
     * getBlackIndex(index, true) with a white-centric index.
     */
    public int getBlackBeforeWhite(int whiteIndex) {
        // there is no tree
        if(root == null) {
            return -1;

        // starting from beyond the tree
        } else if(whiteIndex >= root.whiteSize()) {
            return root.blackSize() - 1;

        // the index is from within the tree
        } else {
            return root.getBlackBeforeWhite(whiteIndex);
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
        // there is no tree
        if(root == null) {
            // There are no black sequences
            if(colour == BLACK) return -1;

            // The trailing whitespace matches
            else if(whiteSpace >= size) return 0;

            // nothing matches
            else return -1;

        // focus only within the tree
        } else if(colour == BLACK) {
            return root.findSequenceOfMinimumSize(size, colour);

        // check the tree first, if it fails check the trailing whitespace
        } else {
            int result = root.findSequenceOfMinimumSize(size, colour);
            if(result == -1 && whiteSpace >= size) result = treeSize;
            return result;
        }
    }

    /**
     * Provides a specialized {@link Iterator} that iterates over a
     * {@link Barcode} to provide high performance access to {@link Barcode}
     * functionality.
     */
    public BarcodeIterator iterator() {
        return new BarcodeIterator(this);
    }
}