/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.adt;

import java.util.*;

/**
 * A BarcodeIterator represents a specialized {@link Iterator} for moving over
 * a Barcode efficiently.  Currently, this {@link Iterator} is read-only, but
 * that will change in the future.  For now, this interface simply defines the
 * set of common read operations that occur during Barcode access.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public interface BarcodeIterator extends Iterator {

    /**
     * Returns true if there are more BLACK elements in the {@link Barcode} to
     * move the {@link Iterator} to.
     */
    public boolean hasNextBlack();

    /**
     * Returns true if there are more WHITE elements in the {@link Barcode} to
     * move the {@link Iterator} to.
     */
    public boolean hasNextWhite();

    /**
     * Returns true if there are more elements in the {@link Barcode} to
     * move the {@link Iterator} to that match the provided colour.
     */
    public boolean hasNextColour(Object colour);

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that is BLACK.
     *
     * @throws NoSuchElementException if hasNextBlack() returns false.
     */
    public Object nextBlack();

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that is WHITE.
     *
     * @throws NoSuchElementException if hasNextWhite() returns false.
     */
    public Object nextWhite();

    /**
     * Moves this {@link Iterator} to the next element in the {@link Barcode}
     * that matches the provided colour.
     *
     * @throws NoSuchElementException if hasNextColour(colour) returns false.
     */
    public Object nextColour(Object colour);

    /**
     * Gets the index of the last element visited.
     */
    public int getIndex();

    /**
     * Gets the black-centric index of the last element visited or -1 if that
     * element is white.
     */
    public int getBlackIndex();

    /**
     * Gets the white-centric index of the last element visited or -1 if that
     * element is black.
     */
    public int getWhiteIndex();

    /**
     * Gets the colour-centric index of the last element based on the value of
     * colour.  If the element didn't match the colour specified, this method
     * returns -1.
     */
    public int getColourIndex(Object colour);

    /**
     * Sets the most recently viewed element to WHITE and returns the white-centric
     * index of the element after the set is complete.
     */
    public int setWhite();

    /**
     * Sets the most recently viewed element to BLACK and returns the white-centric
     * index of the element after the set is complete.
     */
    public int setBlack();

    /**
     * Sets the most recently viewed element to the value of colour and returns
     * the colour specific index of the element after the set is complete.
     */
    public int set(Object colour);
}
