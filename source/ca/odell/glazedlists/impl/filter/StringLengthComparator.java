/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

import java.util.Comparator;

/**
 * This Comparator orders {@link String}s in descending order by their lengths.
 *
 * @author James Lemieux
 */
public final class StringLengthComparator implements Comparator<String> {
    /** {@inheritDoc} */
    public int compare(String a, String b) {
        return b.length() - a.length();
    }
}
