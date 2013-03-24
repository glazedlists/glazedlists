package ca.odell.glazedlists.impl.matchers;

import org.junit.Test;

import static org.junit.Assert.*;

public class RangeMatcherTest {

    /**
     * At one point RangeMatcher was comparing the results of a Comparator
     * to the exact values -1 and 1. This is incorrect as a Comparator may
     * return *any* negative or positive integer. In fact, java.lang.String
     * does return negative and positive integers that are not -1 and 1.
     */
    @Test
    public void testRangeMatcher() {
        final RangeMatcher<String, String> rm = new RangeMatcher<String, String>("bad", "dog");
        
        assertFalse(rm.matches("a"));
        assertFalse(rm.matches("b"));
        assertFalse(rm.matches("babe"));
        assertTrue(rm.matches("badge"));
        assertTrue(rm.matches("c"));
        assertTrue(rm.matches("cat"));
        assertTrue(rm.matches("d"));
        assertTrue(rm.matches("dodge"));
        assertFalse(rm.matches("dogwood"));
        assertFalse(rm.matches("dolphin"));
        assertFalse(rm.matches("e"));
    }
}
