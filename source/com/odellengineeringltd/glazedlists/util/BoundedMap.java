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
 * A BoundedMap is collection  of objects in memory that could also be
 * constructed using a longer running process such as a database lookup
 * or calculation. It is used when there are too many objects to keep in
 * memory simultaneously and object access happens in clusters.
 *
 * All objects are stored in a BoundedMap by their key, which must
 * implement Comparable. A bound on the number of objects in the bounded
 * map is kept so that after <i>size</i> other objects have been accessed,
 * an object is no longer stored in the bounded map.
 *
 * This implementation uses a IndexedTree to store objects by their key
 * and a linked list to store them by their time of last access.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BoundedMap implements Map {
    
    /** the size to bound the map by */
    private int maxSize = 0;
    
    /** the current size of the map */
    private int size = 0;
    
    /** linked list stores most recently thru least recently accessed */
    private BoundedMapEntry headEntry = null;
    private BoundedMapEntry tailEntry = null;

    /** the tree stores objects by key */
    private IndexedTree tree = new IndexedTree(new ComparableComparator());
    
    /**
     * Creates a bounded map with the specified maximum size. Only the
     * last <i>size</i> elements to be accessed are retained in the map.
     */
    public BoundedMap(int maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * Removes all mappings from this map (optional operation).
     */
    public void clear() {
        while(size > 0) {
            removeEntry(headEntry);
        }
    }
    
    /**
     * Returns true if this map contains a mapping for the specified key.
     * More formally, returns true if and only if this map contains at a
     * mapping for a key k such that (key==null ? k==null : key.equals(k)).
     * (There can be at most one such mapping.)
     */
    public boolean containsKey(Object key) {
        BoundedMapEntry tempBoundedMapEntry = new BoundedMapEntry((Comparable)key, null);
        IndexedTreeNode previousNode = tree.getNode(tempBoundedMapEntry);
        return (previousNode != null);
    }
    
    /**
     * Returns true if this map maps one or more keys to the specified
     * value. More formally, returnstrue if and only if this map
     * contains at least one mapping to a value v such that
     * (value==null ? v==null : value.equals(v)). This operation will
     * probably require time linear in the map size for most implementations
     * of the Map interface.
     */
    public boolean containsValue(Object value) {
        // iterate through all entries looking for value
        BoundedMapEntry currentEntry = headEntry;
        while(currentEntry != null) {
            Object currentValue = currentEntry.getValue();
            if(currentValue == value) return true;
            if(currentValue == null) continue;
            if(currentValue.equals(value)) return true;
            currentEntry = currentEntry.getNext();
        }
        return false;
    }
    
    /**
     * Returns a set view of the mappings contained in this map. Each
     * element in the returned set is aMap.Entry. The set is backed by
     * the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the
     * set is in progress, the results of the iteration are undefined.
     * The set supports element removal, which removes the corresponding
     * mapping from the map, via the Iterator.remove, Set.remove,
     * removeAll, retainAlland clear operations. It does not support the
     * add or addAll operations.
     */
    public Set entrySet() {
        throw new UnsupportedOperationException("The entrySet() method is not supported by BoundedMap!");
    }
    
    /**
     * Compares the specified object with this map for equality.
     * Returns true if the given object is also a map and the two Maps
     * represent the same mappings. More formally, two maps t1 andt2
     * represent the same mappings if t1.entrySet().equals(t2.entrySet()).
     * This ensures that the equals method works properly across different
     * implementations of the Map interface.
     */
    public boolean equals(Object other) {
        throw new UnsupportedOperationException("The equals() method is not supported by BoundedMap!");
    }
    
    /**
     * Returns the value to which this map maps the specified key.
     * Returns null if the map contains no mapping for this key. A
     * return value of null does not necessarily indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to null. The containsKey operation may be
     * used to distinguish these two cases.
     */
    public Object get(Object key) {
        // lookup the node in the tree
        BoundedMapEntry temp = new BoundedMapEntry((Comparable)key, null);
        IndexedTreeNode node = tree.getNode(temp);
        
        // return the value of the map entry owned by the tree node
        if(node == null) return null;
        BoundedMapEntry boundedMapEntry = (BoundedMapEntry)node.getValue();
        touchEntry(boundedMapEntry);
        return boundedMapEntry.getValue();
    }

    /**
     * Returns the hash code value for this map. The hash code of a map
     * is defined to be the sum of the hashCodes of each entry in the map's
     * entrySet view. This ensures that t1.equals(t2)implies that
     * t1.hashCode()==t2.hashCode() for any two maps t1 and t2, as required
     * by the general contract of Object.hashCode.
     */
    public int hashCode() {
        throw new UnsupportedOperationException("The hashCode() method is not supported by BoundedMap!");
    }
    
    /**
     * Returns true if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return (size == 0);
    }
    
    /**
     * Returns a set view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set,
     * and vice-versa. If the map is modified while an iteration over the
     * set is in progress, the results of the iteration are undefined. The
     * set supports element removal, which removes the corresponding mapping
     * from the map, via theIterator.remove, Set.remove, removeAll retainAll,
     * and clear operations. It does not support the add or addAll operations.
     */
    public Set keySet() {
        throw new UnsupportedOperationException("The keySet() method is not supported by BoundedMap!");
    }
    
    
    /**
     * Associates the specified value with the specified key in this map
     * (optional operation). If the map previously contained a mapping 
     * for this key, the old value is replaced by the specified value. 
     * (A map m is said to contain a mapping for a key k if and only if
     * m.containsKey(k) would return true.))
     *
     * In the BoundedMap, an element put into the map may <strong>not</strong>
     * exist in the map in subsequent calls to get(). This is because more
     * entries may be fresher and the inserted value may have been removed.
     */
    public Object put(Object key, Object value) {
        // remove the previous value associated with this key
        Object previousValue = remove(key);
        
        // add the map entry to the linked list and tree
        BoundedMapEntry boundedMapEntry = new BoundedMapEntry((Comparable)key, value);
        addEntry(boundedMapEntry);
        
        // if the list has grown too long, remove the least recently accessed
        while(size > maxSize) {
            removeEntry(tailEntry);
        }
        
        return previousValue;
    }
    
    /**
     * Copies all of the mappings from the specified map to this map
     * (optional operation). The effect of this call is equivalent to
     * that of calling put(k, v) on this map once for each mapping from
     * key k to value v in the specified map. The behavior of this
     * operation is unspecified if the specified map is modified while
     * the operation is in progress.
     */
    public void putAll(Map source) {
        throw new UnsupportedOperationException("The putAll() method is not supported by BoundedMap!");
    }
    
    /**
     * Removes the mapping for this key from this map if it is present 
     * (optional operation). More formally, if this map contains a 
     * mapping from key k to value v such that (key==null ? k==null : key.equals(k)), 
     * that mapping is removed. (The map can contain at most one such mapping.)
     * 
     * Returns the value to which the map previously associated the key,
     * or null if the map contained no mapping for this key. (A null return
     * can also indicate that the map previously associated null with the
     * specified key if the implementation supports null values.) The map
     * will not contain a mapping for the specified key once the call returns.
     */
    public Object remove(Object key) {
        // lookup the node of the key
        BoundedMapEntry tempBoundedMapEntry = new BoundedMapEntry((Comparable)key, null);
        IndexedTreeNode node = tree.getNode(tempBoundedMapEntry);
        
        // if the key exists, remove its node and return its value
        if(node != null) {
            BoundedMapEntry boundedMapEntry = (BoundedMapEntry)node.getValue();
            removeEntry(boundedMapEntry);
            return boundedMapEntry.getValue();

        // if the key doesn't exist, return null
        } else {
            return null;
        }
    }
    
    /**
     * Returns the number of key-value mappings in this map. If the map
     * contains more thanInteger.MAX_VALUE elements, returns Integer.MAX_VALUE
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns a collection view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa. If the map is modified
     * while an iteration over the collection is in progress, the results
     * of the iteration are undefined. The collection supports element
     * removal, which removes the corresponding mapping from the map, via
     * the Iterator.remove, Collection.remove, removeAll,retainAll and clear
     * operations. It does not support the add or addAll operations.
     */
    public Collection values() {
        throw new UnsupportedOperationException("The values() method is not supported by BoundedMap!");
    }
    
    /**
     * Remove the specified map entry from the map. This removes
     * it from both the linked list and from the tree.
     */
    private void removeEntry(BoundedMapEntry boundedMapEntry) {
        if(headEntry == boundedMapEntry) headEntry = headEntry.getNext();
        if(tailEntry == boundedMapEntry) tailEntry = tailEntry.getPrevious();
        boundedMapEntry.removeFromLinkedList();
        boundedMapEntry.removeFromTree();
        size--;
        
        // both head entry and tail entry are null, or neither are null
        assert((headEntry == null) == (tailEntry == null));
    }
    
    /**
     * Add the specified map entry to the map. This adds it to
     * both the linked list and to the tree.
     */
    private void addEntry(BoundedMapEntry boundedMapEntry) {
        // insert this map entry in the tree
        IndexedTreeNode treeNode = tree.addByNode(boundedMapEntry);
        boundedMapEntry.setTreeNode(treeNode);
        
        // insert the entry into the list: list is empty
        if(size == 0) {
            assert(headEntry == null && tailEntry == null);
            headEntry = boundedMapEntry;
            tailEntry = boundedMapEntry;
        // insert the entry into the list: list is not empty
        } else {
            headEntry = headEntry.addFirst(boundedMapEntry);
        }
        size++;
    }
    
    /**
     * When a map entry is touched, it is set as the most recently visited
     * and will not be removed until <i>max size</i> other map entries have
     * been visited.
     */
    private void touchEntry(BoundedMapEntry boundedMapEntry) {
        // no action is necessary if this is the only entry or this is the head entry
        if(size == 1) return;
        if(headEntry == boundedMapEntry) return;
        
        // remove the entry from the linked list
        if(tailEntry == boundedMapEntry) tailEntry = tailEntry.getPrevious();
        boundedMapEntry.removeFromLinkedList();
        
        // add the entry to the linked list
        headEntry = headEntry.addFirst(boundedMapEntry);
    }
    
    /**
     * Test the bounded map.
     */
    public static void main(String[] args) {
        
        BoundedMap boundedMap = new BoundedMap(2);

        System.out.println("ADD 1, 2, 3");
        boundedMap.put(new Integer(1), "One");
        boundedMap.put(new Integer(2), "Two");
        boundedMap.put(new Integer(3), "Three");
        System.out.println(boundedMap.get(new Integer(1)));
        System.out.println(boundedMap.get(new Integer(2)));
        System.out.println(boundedMap.get(new Integer(3)));
        
        System.out.println("REMOVE 2");
        boundedMap.remove(new Integer(2));
        System.out.println(boundedMap.get(new Integer(1)));
        System.out.println(boundedMap.get(new Integer(2)));
        System.out.println(boundedMap.get(new Integer(3)));
        
        System.out.println("CLEAR");
        boundedMap.clear();
        System.out.println(boundedMap.get(new Integer(1)));
        System.out.println(boundedMap.get(new Integer(2)));
        System.out.println(boundedMap.get(new Integer(3)));
        
        System.out.println("ADD 1, 2, 3");
        boundedMap.put(new Integer(1), "One");
        boundedMap.put(new Integer(2), "Two");
        boundedMap.put(new Integer(3), "Three");
        System.out.println(boundedMap.get(new Integer(1)));
        System.out.println(boundedMap.get(new Integer(2)));
        System.out.println(boundedMap.get(new Integer(3)));
    }
}
