/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists;

// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;
// volatile implementation support
import com.odellengineeringltd.glazedlists.util.impl.*;
// For the execution of the performance test
import java.util.Random;

/**
 * A caching list is a mutation list that caches elements from the source list.
 * It should be used when a get() on the source list is deemed to be expensive.
 * It is used when there are too many objects to keep in memory simultaneously
 * and object access happens in clusters.
 *
 * <p>This list simply remembers the last <i>n</i> elements which were
 * requested.  Accesses to such elements does not perform a recursive operation
 * on the source list, instead the element is fetched from the cache.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class CachingList extends TransformedList implements ListEventListener {

    /** The value signifying that an index node is currently unset  */
    public static final Integer EMPTY_INDEX_NODE = new Integer(-1);

    /** The cache is implemented using a tree-based cache and an index tree */
    private IndexedTree cache;
    private IndexedTree indexTree;

    /** The count of cache hits and cache misses for testing cache success */
    private int cacheHits;
    private int cacheMisses;

    /** The number of elements in the cache */
    private int currentSize = 0;
    private int maxSize = 0;

    /**
     * Creates a new Caching list that uses the specified source list and
     * has the specified maximum cache size.
     *
     * @param source The source list to use to get values from
     * @param maxSize The maxiumum size of the cache
     */
    public CachingList(EventList source, int maxSize) {
        super(source);
        this.maxSize = maxSize;
        cache = new IndexedTree(new AgedNodeComparator());
        indexTree = new IndexedTree();
        for(int i = 0; i < source.size();i++) {
            indexTree.addByNode(i, EMPTY_INDEX_NODE);
        }
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return The number of elements in this list.
     */
    public final int size() {
        return source.size();
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * If <code>preFetch(int)</code> is overridden, this method
     * also performs that pre-fetch operation to optimize what data
     * is cached for a particular application.  That pre-fetch is
     * conditional to the requested value being found.
     *
     * @param index The index of the value to retrieve
     *
     * @return The value associated with the given index,
     *         or null if the index is not found.
     */
    public final Object get(int index) {
        preFetch(index);
        return fetch(index, true);
    }

    /**
     * Fetches a particular element.
     *
     * <p>This might seem redundant with the existence of <code>get(int)</code>.
     * However, the goals of the methods are different.  This method exists
     * to be called by <code>get(int)</code> or <code>preFetch(int)</code>.
     * This distinction allows users overriding this class a means of entry
     * retrieval which does not implicitly execute a pre-fetch.  This is
     * particularly key for users overriding <code>preFecth(int)</code>.
     *
     * @param index The index of the value to retrieve
     * @param recordHitsOrMisses Whether to increment the hit/miss counters
     *        (this should always be <code>false</code> when called from
     *        <code>preFetch(int)</code>).
     *
     * @return The value associated with the given index,
     *         or null if the index is not found.
     */
    protected final Object fetch(int index, boolean recordHitsOrMisses) {
        getReadWriteLock().writeLock().lock();
        try {
            Object value = null;
    
            // attempt to get the element from the cache
            IndexedTreeNode treeNode = indexTree.getNode(index);
            Object nodeValue = treeNode.getValue();
            if(EMPTY_INDEX_NODE != nodeValue && nodeValue != null) {
                cacheHits ++;
                Object agedTreeNodeObject = treeNode.getValue();
                IndexedTreeNode agedTreeNode = (IndexedTreeNode)agedTreeNodeObject;
                Object agedNodeObject = agedTreeNode.getValue();
                AgedNode agedNode = (AgedNode)agedNodeObject;
                value = agedNode.getValue();
                agedTreeNode.removeFromTree();
                treeNode.setValue(cache.addByNode(agedNode));
            } else {
                cacheMisses++;
                value = source.get(index);
                if(currentSize < maxSize) {
                    currentSize++;
                    IndexedTreeNode cachedValue = cache.addByNode(new AgedNode(treeNode, value));
                    treeNode.setValue(cachedValue);
                } else {
                    IndexedTreeNode oldValue = cache.getNode(currentSize - 1);
                    oldValue.removeFromTree();
                    AgedNode oldNode = (AgedNode)oldValue.getValue();
                    IndexedTreeNode oldTreeNode = oldNode.getIndexNode();
                    oldTreeNode.setValue(EMPTY_INDEX_NODE);
                    IndexedTreeNode cachedValue = cache.addByNode(new AgedNode(treeNode, value));
                    treeNode.setValue(cachedValue);
                }
            }
            return value;
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
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

    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     *
     * @param mutationIndex The index of a value in the mutation list
     *
     * @return The index of the value in the source list
     *         (THIS IS THE SAME AS THE PASSED VALUE).
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }

    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     *
     * @return True as a CachingList is always writable
     */
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

    /**
     * When the list is changed the cache is updated appropriately.
     * An INSERT causes the indexes of cached values to be offset.
     * A DELETE causes an entry to be removed and/or the index values to be offset
     * An UPDATE causes an existing entry to be removed
     *
     * @param listChanges The list of changes to apply to this list
     */
    public final void listChanged(ListEvent listChanges) {
        updates.beginEvent();
        while(listChanges.next()) {
            // get the current change info
            final int index = listChanges.getIndex();
            final int changeType = listChanges.getType();

            // Lookup the cache entry for this index
            IndexedTreeNode treeNode = indexTree.getNode(index);
            Object value = treeNode.getValue();
            IndexedTreeNode cachedObject = null;
            if(EMPTY_INDEX_NODE != value && value != null) {
                cachedObject = (IndexedTreeNode)value;
            }

            // Perform specific actions for each type of change
            if(changeType == ListEvent.INSERT) {
                indexTree.addByNode(index, EMPTY_INDEX_NODE);
            } else if(changeType == ListEvent.DELETE) {
                if(cachedObject != null) {
                    cachedObject.removeFromTree();
                    currentSize--;
                }
                treeNode.removeFromTree();
            } else if(changeType == ListEvent.UPDATE) {
                if(cachedObject != null) {
                    cachedObject.removeFromTree();
                    currentSize--;
                }
            }
            updates.addChange(changeType, index);
        }
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
     * Convienience method to allow the running of all of the
     * tests defined in this helper class via a single method call
     */
    protected void runTests() {
        System.out.println("Beginning Performance Tests...");
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
        // Set up the source list
        sourceList = new WaitEventList(requestWaitTime);
        for(int i = 0; i < sourceListSize; i++) {
            sourceList.add(new Integer(i));
        }
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
        sourceList = null;
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
