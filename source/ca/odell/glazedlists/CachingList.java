/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists;

// the Glazed Lists' change objects
import ca.odell.glazedlists.event.*;
// volatile implementation support
import ca.odell.glazedlists.util.impl.*;
// For the execution of the performance test
import java.util.Random;

/**
 * An {@link EventList} that caches elements from its source {@link EventList}.
 *
 * It is useful in cases when the {@link #get(int)} method of an
 * {@link EventList} is expensive. It can also be used when there are too many
 * elements to keep in memory simultaneously. For caching to be effective, object
 * access must be clustered.
 *
 * <p>This {@link EventList} caches the most recently requested <i>n</i> elements.
 *
 * <p>By overriding the {@link #preFetch(int)} method, you can modify this
 * CachingList to do predictive lookups for higher performance.
 *
 * <p>This {@link EventList} supports all write operations.
 *
 * <p><strong><font color="#FF0000">Warning:</font></strong> This class is
 * thread ready but not thread safe. See {@link EventList} for an example
 * of thread safe code.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class CachingList extends TransformedList implements ListEventListener {

    /** The cache is implemented using a tree-based cache */
    private IndexedTree cache;

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
     * {@link EventList}.
     *
     * @param source The source list to use to get values from
     * @param maxSize The maximum size of the cache
     */
    public CachingList(EventList source, int maxSize) {
        super(source);
        this.maxSize = maxSize;

        cache = new IndexedTree(new AgedNodeComparator());
        indexTree = new SparseList();
        indexTree.addNulls(0, source.size());
        source.addListEventListener(this);
        lastKnownSize = source.size();
    }

    /** {@inheritDoc} */
    public final int size() {
        return source.size();
    }

    /** {@inheritDoc} */
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
        IndexedTreeNode cacheNode = (IndexedTreeNode)indexTree.get(index);

        // The value is cached, return cached value
        if(cacheNode != null) {
            if(recordHitsOrMisses) cacheHits ++;;
            AgedNode agedNode = (AgedNode)cacheNode.getValue();
            value = agedNode.getValue();
            cacheNode.removeFromTree();
            SparseListNode indexNode = agedNode.getIndexNode();
            indexNode.setValue(cache.addByNode(agedNode));

        // The value is not cached, lookup from source and cache
        } else {
            if(recordHitsOrMisses) cacheMisses++;
            // Make room in the cache if it is full
            if(currentSize >= maxSize) {
                IndexedTreeNode oldestInCache = cache.getNode(0);
                oldestInCache.removeFromTree();
                AgedNode oldAgedNode = (AgedNode)oldestInCache.getValue();
                SparseListNode oldIndexNode = oldAgedNode.getIndexNode();
                indexTree.set(oldIndexNode.getIndex(), null);
                currentSize--;
            }

            // Cache the value
            value = source.get(index);
            indexTree.set(index, Boolean.TRUE);
            SparseListNode indexNode = indexTree.getNode(index);
            AgedNode agedNode = new AgedNode(indexNode, value);
            indexNode.setValue(cache.addByNode(agedNode));
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
    public final void listChanged(ListEvent listChanges) {

        updates.beginEvent();
        while(listChanges.next()) {
            // get the current change info
            int index = listChanges.getIndex();
            int changeType = listChanges.getType();

            // Lookup the cache entry for this index if possible
            IndexedTreeNode cacheNode = null;
            if(index < lastKnownSize) {
                cacheNode = (IndexedTreeNode)indexTree.get(index);
            }

            // An INSERT causes the indexes of cached values to be offset.
            if(changeType == ListEvent.INSERT) {
                indexTree.add(index, null);

            // A DELETE causes an entry to be removed and/or the index values to be offset.
            } else if(changeType == ListEvent.DELETE) {
                if(cacheNode != null) {
                    cacheNode.removeFromTree();
                    currentSize--;
                }
                indexTree.remove(index);

            // An UPDATE causes an existing entry to be removed
            } else if(changeType == ListEvent.UPDATE) {
                if(cacheNode != null) {
                    cacheNode.removeFromTree();
                    currentSize--;
                }
            }
            updates.addChange(changeType, index);
        }
        lastKnownSize = source.size();
        updates.commitEvent();
    }

    /**
     * This main provides performance testing capabilities for CachingList
     */
    public static void main(String[] args) {
        int argsLength = args.length;
        int listSize = -1;
        int cacheSize = -1;
        int waitDuration = -1;
        CacheTestHelper testHelper = null;

        // Parse the arguments to set up the performance tests.
        if(argsLength == 2) {
            try {
                listSize = Integer.parseInt(args[0]);
                waitDuration = Integer.parseInt(args[1]);
                testHelper = new CacheTestHelper(listSize, waitDuration);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("CachingList expects integer arguments.");
            }
        } else if(argsLength == 3) {
            try {
                listSize = Integer.parseInt(args[0]);
                cacheSize = Integer.parseInt(args[1]);
                waitDuration = Integer.parseInt(args[2]);
                testHelper = new CacheTestHelper(listSize, cacheSize, waitDuration);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("CachingList expects integer arguments.");
            }
        } else {
            System.out.println("'CachingList' Usage:");
            System.out.println("CachingList <sourceListSize> <waitDuration>");
            System.out.println("OR");
            System.out.println("CachingList <sourceListSize> <cacheSize> <waitDuration>");
            System.exit(1);
        }

        // Now run the tests
        testHelper.runTests();
    }
}

/**
 * A simple extension of the BasicEventList to
 * wait for an allotted time period before serving
 * a request, emulating a more expensive call such
 * as a database lookup.
 */
class WaitEventList extends TransformedList {

    private int waitDuration = 0;

    public WaitEventList(int waitDuration) {
        super(new BasicEventList());
        this.waitDuration = waitDuration;
    }

    public Object get(int index) {
        try {
            Thread.sleep(waitDuration);
            return source.get(index);
        } catch (Exception e) {
            throw new RuntimeException("Thread.sleep() failure.", e);
        }
    }

    /**
     * For implementing the ListEventListener interface. When the underlying list
     * changes, this sends notification to listening lists.
     */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.beginEvent();
        while(listChanges.next()) {
            updates.addChange(listChanges.getType(), listChanges.getIndex());
        }
        updates.commitEvent();
    }

    protected boolean isWritable() {
        return true;
    }
}

/**
 * A helper class provided to simplify the included
 * tests contained in main.
 */
class CacheTestHelper {

    /** The variables to allow the test to be changed easily */
    private int requestWaitTime = 0;
    private int sourceListSize = 0;
    private int cacheSize = 0;
    private int interval = 0;

    /** The CachingList to performance test */
    private CachingList testCache = null;

    /** The underlying EventList */
    private EventList sourceList = null;

    /**
     * Creates a new CacheTestHelper to performance test the
     * CachingList.  This Constructor implicitly sizes the
     * CachingList to be one-half the size of the source list.
     *
     * @param sourceListSize The size of the underlying list
     * @param cacheSize The explicitly set size for the CachingList
     * @param requestWaitTime The amount of time to delay when requesting
     *        information from the underlying list
     */
    protected CacheTestHelper(int sourceListSize, int requestWaitTime) {
        this.sourceListSize = sourceListSize;
        this.requestWaitTime = requestWaitTime;
        cacheSize = (int)Math.round(sourceListSize / 2.0);
        interval = (int)Math.round(cacheSize / 10.0);
    }

    /**
     * Creates a new CacheTestHelper to performance test the
     * CachingList.  This Constructor explicitly sizes the
     * CachingList
     *
     * @param sourceListSize The size of the underlying list
     * @param cacheSize The explicitly set size for the CachingList
     * @param requestWaitTime The amount of time to delay when requesting
     *        information from the underlying list
     */
    protected CacheTestHelper(int sourceListSize, int cacheSize, int requestWaitTime) {
        this.sourceListSize = sourceListSize;
        this.requestWaitTime = requestWaitTime;
        this.cacheSize = cacheSize;
        interval = (int)Math.round(cacheSize / 10.0);
    }

    /**
     * Convenience method to allow the running of all of the
     * tests defined in this helper class via a single method call
     */
    protected void runTests() {
        System.out.println("Beginning Performance Tests...");

        // Set up the source list
        sourceList = new WaitEventList(requestWaitTime);
        for(int i = 0; i < sourceListSize; i++) {
            sourceList.add(new Integer(i));
        }
        long startTime = System.currentTimeMillis();
        testRetrievalFromCache();

        setUp();
        testRemoveOfCachedData();
        tearDown();

        setUp();
        testRemoveOfUncachedData();
        tearDown();

        setUp();
        testRandomInsertion();
        tearDown();

        setUp();
        testClearingFullCache();
        tearDown();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("End of Tests");
        System.out.println("Total Test Time: " + totalTime + "ms.");
    }

    /** The Particular Test Cases */

    private void testRetrievalFromCache() {
        for(int i = 0; i <= 10; i++) {
            setUp();
            performNGets(i * interval);
            tearDown();
        }
    }

    private void testRemoveOfCachedData() {
        System.out.println("Testing the Expense of Removing " + cacheSize
            + " Cached Entries.");
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < cacheSize; i++) {
            sourceList.remove(0);
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Test completed in: " + timeTaken + "ms\n");
    }

    private void testRemoveOfUncachedData() {
        System.out.println("Testing the Expense of Removing " + cacheSize
            + " Uncached Entries.");
        long startTime = System.currentTimeMillis();
        for(int i = cacheSize; i < sourceListSize; i++) {
            sourceList.remove(cacheSize);
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Test completed in: " + timeTaken + "ms\n");
    }

    private void testRandomInsertion() {
        Random random = new Random();
        System.out.println("Testing the Expense of " + cacheSize + " Random Insertions.");
        long startTime = System.currentTimeMillis();
        for(int i = 0; i < cacheSize; i++) {
            int index = random.nextInt(cacheSize);
            sourceList.add(index, new Integer(index));
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Test completed in: " + timeTaken + "ms\n");
    }

    private void testClearingFullCache() {
        System.out.println("Testing the Expense of Calling clear() on a Full Cache");
        long startTime = System.currentTimeMillis();
        testCache.clear();
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        System.out.println("Test completed in: " + timeTaken + "ms\n");
    }

    /**
     * Lookup from the cache starting at <code>start</code> and going
     * to the size of the cache
     *
     * @param start The index to start getting values from
     */
     private void performNGets(int start) {
        System.out.println("Running Test For A Cache Containing "
            + (100.0 * ((cacheSize - (float)start)/cacheSize))
            +"% of the searched for values: ");
        long startTime = System.currentTimeMillis();
        for(int j = start; j < start + cacheSize; j++) {
            testCache.get(j);
        }
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;
        int uncachedExpense = (testCache.getCacheMisses() - cacheSize) * requestWaitTime;
        System.out.println("Percentage of cache used: "
            + (getCacheHitRatio() * 100.0) + "%");
        System.out.println("Test completed in: " + timeTaken + "ms");
        System.out.println("Cost of Uncached Information Retrieval: "
            + uncachedExpense + "ms");
        System.out.println("Net Expense of using CachingList: "
            + (int)Math.round(timeTaken - uncachedExpense) + "ms\n");
    }

    /**
     * Prepare for a test.
     */
    private void setUp() {
        // Set up and preload the cache with the first items, up to cacheSize
        testCache = new CachingList(sourceList, cacheSize);
        for(int i = 0; i < cacheSize; i++) {
            testCache.get(i);
        }
    }

    /**
     * Clean up after a test.
     */
    private void tearDown() {
        testCache = null;
    }

    /**
     * Gets the ratio of cache hits to cache misses. This is a number between
     * 0 and 1, where 0 means the cache is unused and 1 means the cache was
     * used exclusively.
     *
     * @return The ratio of cache hits ot cache misses
     */
    private float getCacheHitRatio() {
        int cacheHits = testCache.getCacheHits();
        int cacheMisses = testCache.getCacheMisses() - cacheSize;
        if(cacheHits + cacheMisses == 0) return 0.0F;
        return (float)cacheHits / (float)(cacheHits + cacheMisses);
    }
}
