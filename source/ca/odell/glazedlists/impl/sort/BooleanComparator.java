/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.sort;

// for specifying a sorting algorithm
import java.util.Comparator;

/**
 * A {@link Comparator} that compares two {@link Boolean} objects such that
 * the sort order will be:
 * null, Boolean.FALSE, Boolean.TRUE.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
 public final class BooleanComparator implements Comparator<Boolean> {
    /**
     * Compares two Boolean objects using the following sort order:
     * null, Boolean.FALSE, Boolean.TRUE;
     */
    @Override
    public int compare(Boolean alpha, Boolean beta) {
        final int alphaOrdinal = alpha == null ? 0 : !alpha.booleanValue() ? 1 : 2;
        final int betaOrdinal = beta == null ? 0 : !beta.booleanValue() ? 1 : 2;
        return alphaOrdinal - betaOrdinal;
    }

    /**
     * This is equal to another comparator if it is a BooleanComparator.
     */
    @Override
    public boolean equals(Object other) {
        return (other instanceof BooleanComparator);
    }
}