/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists;

// Java collections are used for underlying data storage
import java.util.*;

/**
 * A comparator for comparing integer arrays, which are particularly well
 * suited to sorting and filtering tests.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IntArrayComparator implements Comparator {
    public int index;
    public IntArrayComparator(int index) {
        this.index = index;
    }
    public int compare(Object a, Object b) {
        int[] aArray = (int[])a;
        int[] bArray = (int[])b;
        return aArray[index] - bArray[index];
    }
}
