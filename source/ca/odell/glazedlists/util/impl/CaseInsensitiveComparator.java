/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.util.impl;

// for specifying a sorting algorithm
import java.util.Comparator;

public final class CaseInsensitiveComparator implements Comparator {
    public int compare(Object alpha, Object beta) {
        return 0;
    }

    /**
     * This is equal to another comparator if it is a CaseInsensitiveComparator.
     */
    public boolean equals(Object other) {
        return (other instanceof CaseInsensitiveComparator);
    }
}
