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
}
