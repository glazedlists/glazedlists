/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// for implementing Java Collections Map
import java.util.*;

/**
 * A bounded map entry is both a node in a tree for access by key, and
 * a node in a linked list for access by time.
 *
 * This class does not implement MapEntry, although it will when BoundedMap
 * is feature complete. It is still usable, although it is currently only used by
 * the BoundedMap class.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class BoundedMapEntry implements Comparable, Map.Entry {

    /** the value of the this node */
    private Comparable key;
    private Object value;

    /** the location of this node in the tree */
    private IndexedTreeNode treeNode;

    /** linked list links to neighbour nodes */
    private BoundedMapEntry previous;
    private BoundedMapEntry next;

    /**
     * Creates a new entry that holds the specified key
     * and value.
     */
    public BoundedMapEntry(Comparable key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Sets the tree node that knows where this entry is in the tree.
     */
    public void setTreeNode(IndexedTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    /**
     * Gets the key that this BoundedMapEntry is sorted and retrieved by.
     */
    public Object getKey() {
        return key;
    }

    /**
     * Gets the value that this BoundedMapEntry stores.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the next node in the linked list.
     */
    public BoundedMapEntry getNext() {
        return next;
    }

    /**
     * Gets the previous node in the linked list.
     */
    public BoundedMapEntry getPrevious() {
        return previous;
    }

    /**
     * Removes this BoundedMapEntry from the linked list. This links the
     * previous node to the next node and removes the entries own
     * links within the list.
     */
    public void removeFromLinkedList() {
        if(previous != null) previous.next = next;
        if(next != null) next.previous = previous;
        previous = null;
        next = null;
    }

    /**
     * Removes this BoundedMapEntry from the tree. This unlinks the treenode
     * from the tree and that object is no longer valid.
     */
    public void removeFromTree() {
        treeNode.removeFromTree();
        treeNode = null;
    }

    /**
     * Adds the specified BoundedMapEntry to the end of this BoundedMapEntry's
     * linked list.
     *
     * @return the new last element in the list
     */
    public BoundedMapEntry addLast(BoundedMapEntry boundedMapEntry) {
        // if this is not the end node
        if(next != null) {
            return next.addLast(boundedMapEntry);
        // if this is the end node, add here
        } else {
            next = boundedMapEntry;
            boundedMapEntry.previous = this;
            return boundedMapEntry;
        }
    }

    /**
     * Adds the specified BoundedMapEntry to the beginning of this BoundedMapEntry's
     * linked list.
     *
     * @return the new first element in the list
     */
    public BoundedMapEntry addFirst(BoundedMapEntry boundedMapEntry) {
        // if this is not the start node
        if(previous != null) {
            return previous.addFirst(boundedMapEntry);
        // if this is the start node, add here
        } else {
            previous = boundedMapEntry;
            boundedMapEntry.next = this;
            return boundedMapEntry;
        }
    }

    /**
     * When CacheEntries are compared, they are compared by keys. This
     * is for storage in the tree by keys.
     */
    public int compareTo(Object other) {
        BoundedMapEntry otherBoundedMapEntry = (BoundedMapEntry)other;
        return key.compareTo(otherBoundedMapEntry.key);
    }

    /**
     * Replaces the value corresponding to this entry with the specified value
     * (optional operation). (Writes through to the map.) The behavior of this
     * call is undefined if the mapping has already been removed from the map
     * (by the iterator's remove operation).
     *
     * @param value The new value to be stored in this entry
     *
     * @return The old value which had been stored in this entry
     *
     * @throws UnsupportedOperationException - if the put operation is not supported by the backing map.
     * @throws ClassCastException - if the class of the specified value prevents it from being stored in the backing map.
     * @throws IllegalArgumentException - if some aspect of this value prevents it from being stored in the backing map.
     * @throws NullPointerException - the backing map does not permit null values, and the specified value is null.
     *
     */
    public Object setValue(Object value) throws UnsupportedOperationException,
    ClassCastException, IllegalArgumentException, NullPointerException {
        Object oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    /**
     * Compares the specified object with this entry for equality. Returns true
     * if the given object is also a map entry and the two entries represent the same mapping.
     *
     * @param other The object to be compared for equality with this map entry.
     *
     * @return true if the specified object is equal to this map entry
     *
     */
    public boolean equals(Object other) {
        return hashCode() == other.hashCode ();
    }

    /**
     * Returns an appropriate hashCode as defined by the API documentation
     * for the Map.Entry interface.
     *
     * @return A hash code for this map entry such that <code>e1.equals(e2)</code>
     *         implies that <code>e1.hashCode()==e2.hashCode()</code>
     */
    public int hashCode() {
        return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }
}
