/*             Glazed Lists  http://publicobject.com/glazedlists/             */                        
/*        Copyright 2003-2005 publicobject.com, O'Dell Engineering Ltd.       */
package ca.odell.glazedlists;

// Java collections are used for underlying data storage
import java.util.*;

/**
 * A filter list for filtering integerr arrays, which are particularly well
 * suited for sorting and filtering tests.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IntArrayFilterList extends AbstractFilterList {
    public int index = 0;
    public int threshhold = 0;
    public IntArrayFilterList(EventList source) {
        super(source);
    }
    public boolean filterMatches(Object element) {
        int[] array = (int[])element;
        return array[index] >= threshhold;
    }
    public void setFilter(int index, int threshhold) {
        this.index = index;
        this.threshhold = threshhold;
        handleFilterChanged();
    }
}