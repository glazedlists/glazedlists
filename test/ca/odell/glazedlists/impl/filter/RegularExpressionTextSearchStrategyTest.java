package ca.odell.glazedlists.impl.filter;

import org.junit.Test;

import static org.junit.Assert.*;

public class RegularExpressionTextSearchStrategyTest {
    @Test
    public void testIndexOf() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z]");
        assertEquals(0, strategy.indexOf("a"));
    }

    @Test
    public void testMultipleMatches() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z] [a-z] [a-z] [a-z]");
        assertEquals(0, strategy.indexOf("a b c d"));
    }

    @Test
    public void testPartialMatch() {
        RegularExpressionTextSearchStrategy strategy = new RegularExpressionTextSearchStrategy();
        strategy.setSubtext("[a-z] ");
        assertEquals(-1, strategy.indexOf("a b c d"));
    }
}
