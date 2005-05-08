/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.adt;

/**
 * A thin wrapper class for Objects to be inserted in the
 * age sorted tree representation of the cache.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public final class AgedNode {

    /** The corresponding node in the index tree */
    private SparseListNode indexNode = null;
    /** The timestamp corresponding to the last access of this node */
    private long timestamp = 0;
    /** The value to this node */
    private Object value = null;

    /**
     * Creates a new AgedNode object to store in the age sorted tree
     *
     * @param indexNode The related node in the indexing tree
     * @param value The value to assign to this node
     */
    public AgedNode(SparseListNode indexNode, Object value) {
        this.indexNode = indexNode;
        this.value =  value;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Retrieves the value of this node.
     *
     * This is an access, and as such, this node must be re-ordered
     *
     * @return The value of this node
     */
    public Object getValue() {
        timestamp = System.currentTimeMillis();
        return value;
    }

    /**
     * Retrieves the index node for this node.
     *
     * @return The index node
     */
    public SparseListNode getIndexNode() {
        return indexNode;
    }

    /**
     * Retrieves the age of this node.
     *
     * @return The age of this node
     */
    public long getTimestamp() {
        return timestamp;
    }

}