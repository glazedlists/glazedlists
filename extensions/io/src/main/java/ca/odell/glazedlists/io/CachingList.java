/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

// the Glazed Lists' change objects
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.impl.adt.AgedNode;
import ca.odell.glazedlists.impl.adt.AgedNodeComparator;
import ca.odell.glazedlists.impl.adt.SparseList;
import ca.odell.glazedlists.impl.adt.SparseListNode;
import ca.odell.glazedlists.impl.adt.barcode2.Element;
import ca.odell.glazedlists.impl.adt.barcode2.SimpleTree;
import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;
// For the execution of the performance test

/**
 * An {@link ca.odell.glazedlists.EventList} that caches elements from its source {@link ca.odell.glazedlists.EventList}.
 *
 * It is useful in cases when the {@link #get(int)} method of an
 * {@link ca.odell.glazedlists.EventList} is expensive. It can also be used when there are too many
 * elements to keep in memory simultaneously. For caching to be effective, object
 * access must be clustered.
 *
 * <p>This {@link ca.odell.glazedlists.EventList} caches the most recently requested <i>n</i> elements.
 *
 * <p>By overriding the {@link #preFetch(int)} method, you can modify this
 * CachingList to do predictive lookups for higher performance.
 *
 * <p><table border="1" width="100%" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><td colspan=2><font size="+2"><b>EventList Overview</b></font></td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Writable:</b></td><td>yes</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Concurrency:</b></td><td>thread ready, not thread safe</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Performance:</b></td><td>reads: O(log N), writes O(log N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Memory:</b></td><td>O(N)</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Unit Tests:</b></td><td>N/A</td></tr>
 * <tr><td class="TableSubHeadingColor"><b>Issues:</b></td><td>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=22">22</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=32">32</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=43">43</a>
 *   <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=262">262</a>
 * </td></tr>
 * </table>
 *
 * NOTE: The io extension and its types are deprecated.
 *       This extension becomes unsupported and will be removed
 *       from the official distribution with the next major release.
 *       However, {@link CachingList} will then be moved to the core.
 *
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class CachingList extends TransformedList {

    /** The cache is implemented using a tree-based cache */
    private SimpleTree<AgedNode> cache;

    /** The model of the source list for scalability with minimal memory footprint */
    private SparseList indexTree;

    /** The count of cache hits and cache misses for testing cache success */
    private int cacheHits;
    private int cacheMisses;

    /** The number of elements in the cache */
    private int currentSize = 0;
    private int maxSize = 0;

    /** The last inspected size of the source list */
    private int lastKnownSize = 0;

    /**
     * Creates a {@link CachingList} that caches elements from the specified source
     * {@link ca.odell.glazedlists.EventList}.
     *
     * @param source The source list to use to get values from
     * @param maxSize The maximum size of the cache
     */
    public CachingList(EventList source, int maxSize) {
        super(source);
        readWriteLock = new CacheLock(readWriteLock);
        this.maxSize = maxSize;

        cache = new SimpleTree<AgedNode>(new AgedNodeComparator());
        indexTree = new SparseList();
        indexTree.addNulls(0, source.size());
        source.addListEventListener(this);
        lastKnownSize = source.size();
    }

    /** {@inheritDoc} */
    @Override
    public final int size() {
        return source.size();
    }

    /** {@inheritDoc} */
    @Override
    public final Object get(int index) {
        if(index >= size()) throw new IndexOutOfBoundsException("cannot get from tree of size " + size() + " at " + index);
        preFetch(index);
        return fetch(index, true);
    }

    /**
     * Fetches a particular element.
     *
     * <p>This might seem redundant with the existence of {@link #get(int)}.
     * However, the goals of the methods are different.  This method exists
     * to be called by {@link #get(int)} or {@link #preFetch(int)}.
     * This distinction allows users overriding this class a means of entry
     * retrieval which does not implicitly execute a pre-fetch.  This is
     * particularly key for users overriding {@link #preFetch(int)}
     *
     * @param index The index of the value to retrieve
     * @param recordHitsOrMisses Whether to increment the hit/miss counters
     *        (this should always be <code>false</code> when called from
     *        {@link #preFetch(int)}).
     *
     * @return The value associated with the given index,
     *         or null if the index is not found.
     */
    protected final Object fetch(int index, boolean recordHitsOrMisses) {

        // attempt to get the element from the cache
        Object value = null;
        Element cacheNode = (Element)indexTree.get(index);

        // The value is cached, return cached value
        if(cacheNode != null) {
            if(recordHitsOrMisses) cacheHits ++;;
            AgedNode agedNode = (AgedNode)cacheNode.get();
            value = agedNode.getValue();
            cache.remove(cacheNode);
            SparseListNode indexNode = agedNode.getIndexNode();
            indexNode.setValue(cache.addInSortedOrder((byte)1, agedNode, 1));

        // The value is not cached, lookup from source and cache
        } else {
            if(recordHitsOrMisses) cacheMisses++;
            // Make room in the cache if it is full
            if(currentSize >= maxSize) {
                Element oldestInCache = cache.get(0);
                cache.remove(oldestInCache);
                AgedNode oldAgedNode = (AgedNode)oldestInCache.get();
                SparseListNode oldIndexNode = oldAgedNode.getIndexNode();
                indexTree.set(oldIndexNode.getIndex(), null);
                currentSize--;
            }

            // Cache the value
            value = source.get(index);
            indexTree.set(index, Boolean.TRUE);
            SparseListNode indexNode = indexTree.getNode(index);
            AgedNode agedNode = new AgedNode(indexNode, value);
            indexNode.setValue(cache.addInSortedOrder((byte)1, agedNode, 1));
            currentSize++;
        }
        return value;
    }

    /**
     * Pre-fetches a set of data given the index that was directly requested.
     *
     * <p>Each application that wishes to take advantage of pre-fetching should
     * implement this method in a way which best fits their particular use
     * cases.  As such, no default pre-fetch behaviour could really be defined,
     * and thus this method is empty by default.
     *
     * <p>Because pre-fetching can modify the cache, child classes of CachingList
     * should use careful consideration of locking when implementing this method.
     *
     * @param index The index that was requested from the cache
     */
    protected void preFetch(int index) {
    }

    /** {@inheritDoc} */
    @Override
    protected boolean isWritable() {
        return true;
    }


    /**
     * Gets the total number of times that this list has fetched its result
     * from the cache rather than from the source list.
     *
     * @return The number of times that this cache provided the result
     */
    public final int getCacheHits() {
        return cacheHits;
    }

    /**
     * Gets the total number of times that this list has fetched its result
     * from the source list rather than the cache.
     *
     * @return The number of times that this cache couldn't provide the result
     */
    public final int getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Gets the ratio of cache hits to cache misses. This is a number between
     * 0 and 1, where 0 means the cache is unused and 1 means the cache was
     * used exclusively.
     */
    public final float getCacheHitRatio() {
        if(cacheHits + cacheMisses == 0) return 0.0F;
        return (float)cacheHits / (float)(cacheHits + cacheMisses);
    }

    /** {@inheritDoc} */
    @Override
    public final void listChanged(ListEvent listChanges) {

        updates.beginEvent();
        while(listChanges.next()) {
            // get the current change info
            int index = listChanges.getIndex();
            int changeType = listChanges.getType();

            // Lookup the cache entry for this index if possible
            Element cacheNode = null;
            if(index < lastKnownSize) {
                cacheNode = (Element)indexTree.get(index);
            }

            // An INSERT causes the indexes of cached values to be offset.
            if(changeType == ListEvent.INSERT) {
                indexTree.add(index, null);

            // A DELETE causes an entry to be removed and/or the index values to be offset.
            } else if(changeType == ListEvent.DELETE) {
                if(cacheNode != null) {
                    cache.remove(cacheNode);
                    currentSize--;
                }
                indexTree.remove(index);

            // An UPDATE causes an existing entry to be removed
            } else if(changeType == ListEvent.UPDATE) {
                if(cacheNode != null) {
                    cache.remove(cacheNode);
                    currentSize--;
                }
            }
            updates.addChange(changeType, index, listChanges.getChange());
        }
        lastKnownSize = source.size();
        updates.commitEvent();
    }

   /**
    * A special lock to prevent deadlock in CachingList.
    */
   private static class CacheLock implements ReadWriteLock {

        /** The lock this CacheLock decorates */
        private ReadWriteLock sourceLock;

        /**
         * Creates a new lock for CachingList.
         */
        public CacheLock(ReadWriteLock sourceLock) {
            this.sourceLock = sourceLock;
        }

        /**
         * Since reads are write ops on caches, return the lock used for writing.
         */
        @Override
        public Lock readLock() {
            return writeLock();
        }

        /**
         * Return the lock used for writing.
         */
        @Override
        public Lock writeLock() {
            return sourceLock.writeLock();
        }
    }
}