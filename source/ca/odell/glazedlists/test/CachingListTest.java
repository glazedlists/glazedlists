/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import ca.odell.glazedlists.*;
// standard collections
import java.util.*;

/**
 * This test verifies that the {@link CachingList} behaves as expected.
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
}