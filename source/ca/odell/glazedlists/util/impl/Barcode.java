/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

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
public final class Barcode extends AbstractList {

    /** barcode colour constants */
    public static final Object WHITE = Boolean.FALSE;
    public static final Object BLACK = Boolean.TRUE;

    /** the root of the tree */
    private BarcodeNode root = null;

    /** views of this list that contain only one colour */
    private BlackList blackList = new BlackList();
    private WhiteList whiteList = new WhiteList();

    /** the size of the trailing whitespace */
    private int whiteSpace = 0;

    /** the size of tree */
    private int treeSize = 0;

    /**
     * Prints internal debug information for this list
     */
    public void printDebug() {
        System.out.println("\nTotal Size: " + size());
        System.out.println("Trailing Whitespace : " + whiteSpace);
        System.out.println("Tree Size: " + treeSize);
        System.out.println("Tree Structure:\n" + root);
    }

    /**
     * Validates this list's internal structure
     */
    public void validate() {
        if(root != null) root.validate();
    }

    /**
     * Gets the size of this list
     */
    public int size() {
        return treeSize + whiteSpace;
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
        // Make a new root
        if(root == null) {
            root = new BarcodeNode(this, null, length, index);
            treeSize = index + length;

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
     * TO BE REMOVED
     */
    public Object remove(int index) {
        remove(index, 1);
        return WHITE;
    }

    /**
     * Removes the values from the given index to index + length
     */
    public void remove(int index, int length) {
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
     * Sets the root for this list.  This method is exposed for the
     * BarcodeNode in the event that the list's root is involved in
     * an AVL rotation.
     */
    void setRootNode(BarcodeNode root) {
        this.root = root;
        if(root == null) treeSize = 0;
    }

    /**
     * Notifies the list that the underlying list size has changed. This method
     * is exposed for BarcodeNode to propagate size adjustments.
     */
    void treeSizeChanged() {
        treeSize = root.size();
    }

    /**
     * Gets the colour-based view of this list.
     */
    public List getColourList(Object colour) {
        if(colour == WHITE) return whiteList;
        else return blackList;
    }

    /**
     * Gets the white-only view of this list.
     */
    public List getWhiteList() {
        return whiteList;
    }

    /**
     * Gets the black-only view of this list.
     */
    public List getBlackList() {
        return blackList;
    }

    /**
     * A WhiteList is a read-only view of the list where all of the BLACK elements
     * are not included.
     */
    class WhiteList extends AbstractList {

        /**
         * Gets the value at the specified index.
         */
        public Object get(int index) {
            if(root == null || index > root.whiteSize() - 1) throw new IndexOutOfBoundsException("cannot get from list of size "+ root.whiteSize()+" at " + index);
            return WHITE;
        }

        /**
         * Gets the real index of the element with the specified whiteIndex.
         */
        public int getIndex(int whiteIndex) {
            if(root == null && whiteSpace == 0) throw new IndexOutOfBoundsException("cannot get from a list of size 0 at " + whiteIndex);
            else if(root == null) return whiteIndex;
            else return root.getIndexByWhiteIndex(whiteIndex);
        }

        /**
         * Gets the size of the white list.
         */
        public int size() {
            if(root == null) return whiteSpace;
            return root.whiteSize() + whiteSpace;
        }
    }

    /**
     * A BlackList is a read-only view of the list where all of the WHITE elements
     * are not included.
     */
    class BlackList extends AbstractList {

        /**
         * Gets the value at the specified index.
         */
        public Object get(int index) {
            if(root == null || index > root.blackSize() - 1) throw new IndexOutOfBoundsException("cannot get from list of size "+ root.blackSize()+" at " + index);
            return BLACK;
        }

        /**
         * Gets the real index of the element with the specified blackIndex.
         */
        public int getIndex(int blackIndex) {
            if(root == null) throw new IndexOutOfBoundsException("cannot get from a list of size 0 at " + blackIndex);
            return root.getIndexByBlackIndex(blackIndex);
        }

        /**
         * Gets the size of the black list.
         */
        public int size() {
            if(root == null) return 0;
            return root.blackSize();
        }
    }

    /**
     * Gets the real index of an element given the black index or white index.
     */
    public int getIndex(int colourIndex, Object colour) {
        if(colour == WHITE) return whiteList.getIndex(colourIndex);
        else return blackList.getIndex(colourIndex);
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
            if(left) return treeSize - 1;
            return treeSize;

        // get from the tree
        } else {
            return root.getBlackIndex(index, left);
        }
    }
}