/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt.barcode2;

/**
 * The exposed interface of a node.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public interface Element<V> {

    /** a node that's greater than its predecessor and less than its successor */
    public static final int SORTED = 0;
    /** a node whose value is unrelated to those of its predecessor or successor */
    public static final int UNSORTED = 1;
    /** a node that's in-work, no inserts or deletes should be performed when nodes are in this state */
    public static final int PENDING = 2;


    V get();

    void set(V value);

    byte getColor();

    void setSorted(int sorted);

    int getSorted();

    Element<V> next();

    Element<V> previous();
}