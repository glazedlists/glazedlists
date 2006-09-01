/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.io;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.event.ListEvent;
import junit.framework.TestCase;

import java.util.Random;

/**
 * This test verifies that the {@link ca.odell.glazedlists.io.CachingList} behaves as expected.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class CachingListTest extends TestCase {

    private CachingList cache = null;
    private BasicEventList source = null;

    /**
     * Prepare for the test.
     */
    public void setUp() {
        source  = new BasicEventList();
        cache = new CachingList(source, 15);
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
        cache.dispose();
        cache = null;
        source.clear();
        source = null;
    }

    /**
     * Validate that the cache correctly retrieves values from the source
     * and that there are zero cache hits.
     */
    public void testNonRepetitiveLookups() {
        // Load the source with data
        for(int i = 0;i < 25;i++) {
            source.add(new Integer(i));
        }

        // Lookup each value in the source
        for(int i = 0;i < 25;i++) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        // Verify that there have been no cache hits
        assertEquals(0, cache.getCacheHits());
        // Verify that there have been the correct number of cache misses
        assertEquals(25, cache.getCacheMisses());
    }

    /**
     * Validate that the cache correctly retrieves values from the source
     * when fetched repeatedly.  There should be one cache miss, and
     * 24 cached hits.
     */
    public void testRepetitiveLookup() {
        // Load the source with some data
        for(int i = 0;i < 3;i++) {
            source.add(new Integer(i));
        }

        // Lookup the middle value in the source
        Integer ONE = new Integer(1);
        for(int i = 0;i < 25;i++) {
            Object result = cache.get(1);
            assertEquals(ONE, (Integer)result);
        }

        // Verify that there have been 24 cache hits
        assertEquals(24, cache.getCacheHits());
        // Verify that there have been only 1 cache miss
        assertEquals(1, cache.getCacheMisses());
    }

    /**
     * Validate that the cache behaves correctly when it has reached the defined
     * maximum size and Objects which are uncached are requested.  This tests
     * cache overflow behaviour without repetitive lookups that can change which
     * cache entry is considered the 'oldest' as that test is covered by
     * {@link #testCacheEntryAccessReorderingForOverflow()}.
     */
    public void testCacheOverflowBehaviour() {
        // Load the source with data
        for(int i = 0;i < 25;i++) {
            source.add(new Integer(i));
        }

        // Lookup enough values to fill the cache
        for(int i = 0;i < 15;i++) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        int cacheHitBaseline = cache.getCacheHits();
        int cacheMissBaseline = cache.getCacheMisses();

        // Lookup one uncached object
        Object result = cache.get(20);
        assertEquals(new Integer(20), (Integer)result);

        assertEquals(cacheHitBaseline, cache.getCacheHits());
        assertEquals(cacheMissBaseline + 1, cache.getCacheMisses());

        // Look for the first request value which should not be in the cache
        result = cache.get(0);
        assertEquals(new Integer(0), (Integer)result);

        assertEquals(cacheHitBaseline, cache.getCacheHits());
        assertEquals(cacheMissBaseline + 2, cache.getCacheMisses());

    }

    /**
     * Validate that the cache reorders the cached entries correctly
     * based on last access so that the oldest entry is removed when
     * the cache overflows.
     */
    public void testCacheEntryAccessReorderingForOverflow() {
        // Load the source with data
        for(int i = 0;i < 25;i++) {
            source.add(new Integer(i));
        }

        // Lookup enough values to fill the cache
        for(int i = 0;i < 15;i++) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        // Randomly lookup cached data to randomize cache entry ordering
        Random random = new Random(11);
        for(int i = 0;i < 100;i++) {
            cache.get(random.nextInt(15));
        }

        int cacheHitBaseline = cache.getCacheHits();
        int cacheMissBaseline = cache.getCacheMisses();


        // Lookup the cached values in reverse order to re-order entries in reverse
        for(int i = 14;i >= 0;i--) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        assertEquals(cacheHitBaseline += 15, cache.getCacheHits());
        assertEquals(cacheMissBaseline, cache.getCacheMisses());

        // Lookup one uncached object
        Object result = cache.get(20);
        assertEquals(new Integer(20), (Integer)result);

        assertEquals(cacheHitBaseline, cache.getCacheHits());
        assertEquals(cacheMissBaseline + 1, cache.getCacheMisses());

        // Look for the previously cached values in reverse order
        for(int i = 14;i >= 0;i--) {
            result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
            assertEquals(cacheHitBaseline, cache.getCacheHits());
            assertEquals(cacheMissBaseline + 1 + (15 - i), cache.getCacheMisses());
        }
    }

    /**
     * Validates that when a remove is called on an index
     * that is cached, the value is successfully removed
     * from the cache.
     */
    public void testRemovingCachedValueWithCachedFollower() {
        // Load the source with some data
        for(int i = 0;i < 10;i++) {
            source.add(new Integer(i));
        }

        // Load some of the source data into the cache
        for(int i = 0;i < 5;i++) {
            cache.get(i);
        }

        // Remove data that is cached that has at least one
        // element cached that follows it
        cache.remove(2);

        assertEquals(0, cache.getCacheHits());
        assertEquals(5, cache.getCacheMisses());

        Object result = cache.get(2);
        assertEquals(3, ((Integer)result).intValue());

        assertEquals(1, cache.getCacheHits());
        assertEquals(5, cache.getCacheMisses());
    }

    /**
     * Validates that when a remove is called on an index
     * that is cached, the value is successfully removed
     * from the cache.
     */
    public void testRemovingCachedValueAtCacheEdge() {
        // Load the source with some data
        for(int i = 0;i < 10;i++) {
            source.add(new Integer(i));
        }

        // Load some of the source data into the cache
        for(int i = 0;i < 5;i++) {
            cache.get(i);
        }

        // Remove at the edge of the cached data
        cache.remove(4);

        assertEquals(0, cache.getCacheHits());
        assertEquals(5, cache.getCacheMisses());

        Object result = cache.get(4);
        assertEquals(5, ((Integer)result).intValue());

        assertEquals(0, cache.getCacheHits());
        assertEquals(6, cache.getCacheMisses());
    }

    /**
     * Validates that when a remove is called on an index
     * that has been cached, that the internal cache size
     * decreases.
     */
    public void testRemoveCorrectsCacheSize() {
        // Load the source with data
        for(int i = 0;i < 25;i++) {
            source.add(new Integer(i));
        }

        // Lookup enough values to fill the cache
        for(int i = 0;i < 15;i++) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        // Remove 10 values leaving the first value
        for(int i = 10; i > 0;i--) {
            cache.remove(i);
        }

        // Refill cache
        for(int i = 6; i < 15;i++) {
            cache.get(i);
        }

        int cacheHitBaseline = cache.getCacheHits();
        int cacheMissBaseline = cache.getCacheMisses();

        // Lookup the first value which should still be the oldest
        // in the cache if it resized correctly
        Object result = cache.get(0);
        assertEquals(0, ((Integer)result).intValue());
        assertEquals(cacheHitBaseline + 1, cache.getCacheHits());
        assertEquals(cacheMissBaseline, cache.getCacheMisses());
    }

    /**
     * Validate the proper response to requested an index beyond
     * <code>source.size() - 1</code>.  This test is included as
     * a result of CachingList overriding get() from TransformedList.
     */
    public void testBoundsErrorBehaviour() {
        // request beyond bounds with an empty source
        boolean exceptionThrown = false;
        try {
            cache.get(26);
        } catch(IndexOutOfBoundsException e) {
            exceptionThrown = true;
        }

        if(exceptionThrown == false) {
            fail("No exception was thrown when an invalid index was requested on an empty source.");
        }

        // Load the source with data
        for(int i = 0;i < 25;i++) {
            source.add(new Integer(i));
        }

        // request beyond bounds with non-empty source and an empty cache
        exceptionThrown = false;
        try {
            cache.get(26);
        } catch(IndexOutOfBoundsException e) {
            exceptionThrown = true;
        }

        if(exceptionThrown == false) {
            fail("No exception was thrown when an invalid index was requested on an empty cache.");
        }

        // Lookup some values to paritally fill the cache
        for(int i = 9;i < 15;i++) {
             Object result = cache.get(i);
             assertEquals(new Integer(i), (Integer)result);
        }

        // request beyond bounds with non-empty source and a partially filled cache
        exceptionThrown = false;
        try {
            cache.get(26);
        } catch(IndexOutOfBoundsException e) {
            exceptionThrown = true;
        }

        if(exceptionThrown == false) {
            fail("No exception was thrown when an invalid index was requested on a partially full cache.");
        }

        // Lookup enough values to fill the cache
        for(int i = 9;i < 15;i++) {
            Object result = cache.get(i);
            assertEquals(new Integer(i), (Integer)result);
        }

        // request beyond bounds with non-empty source and a full cache
        exceptionThrown = false;
        try {
            cache.get(26);
        } catch(IndexOutOfBoundsException e) {
            exceptionThrown = true;
        }

        if(!exceptionThrown) {
            fail("No exception was thrown when an invalid index was requested on a full cache.");
        }
    }



    /**
     * This main provides performance testing capabilities for CachingList.
     *
     * <p>It was originally the main method of CachingList itself, and it's since
     * been moved here. It should be linked into the proper test system.
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