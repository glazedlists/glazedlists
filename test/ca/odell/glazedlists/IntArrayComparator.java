/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// Java collections are used for underlying data storage
import java.util.*;

/**
 * A comparator for comparing integer arrays, which are particularly well
 * suited to sorting and filtering tests.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IntArrayComparator implements Comparator<int[]> {
    public int index;
    public IntArrayComparator(int index) {
        this.index = index;
    }
    public int compare(int[] a, int[] b) {
        return a[index] - b[index];
    }
}
