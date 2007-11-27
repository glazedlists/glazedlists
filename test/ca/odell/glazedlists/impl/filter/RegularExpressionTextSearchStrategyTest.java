package ca.odell.glazedlists.impl.filter;

import junit.framework.TestCase;

public class RegularExpressionTextSearchStrategyTest extends TestCase {
    public void testIndexOf() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z]");
        assertEquals(0, strategy.indexOf("a"));
    }

    public void testMultipleMatches() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z] [a-z] [a-z] [a-z]");
        assertEquals(0, strategy.indexOf("a b c d"));
    }

    public void testPartialMatch() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z] ");
        assertEquals(-1, strategy.indexOf("a b c d"));
    }
}