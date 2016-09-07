/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.impl.filter;

// for being a JUnit test case
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This test verifies that the TextSearchStrategy implementations work.
 *
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class TextSearchStrategyTest {

    /** the strategies to test */
    private TextSearchStrategy[] strategies;

    /**
     * Prepare for the test.
     */
    @Before
    public void setUp() {
        strategies = new TextSearchStrategy[] {
            new BoyerMooreCaseInsensitiveTextSearchStrategy()
        };
    }

    /**
     * Clean up after the test.
     */
    @After
    public void tearDown() {
        strategies = null;
    }

    /**
     * Test for a String that is a prefix.
     */
    @Test
    public void testPrefix() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("Sask");
            assertEquals("Test class " + strategy.getClass(), 0, strategy.indexOf("Saskatchewan Roughriders"));
        }
    }

    /**
     * Test for a String that is a suffix.
     */
    @Test
    public void testSuffix() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("Riders");
            assertEquals("Test class " + strategy.getClass(), 18, strategy.indexOf("Saskatchewan Roughriders"));
        }
    }

    /**
     * Test for a String that is in the middle.
     */
    @Test
    public void testMiddle() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("CHEW");
            assertEquals("Test class " + strategy.getClass(), 6, strategy.indexOf("Saskatchewan Roughriders"));
        }
    }

    /**
     * Test for a String that is not a substring.
     */
    @Test
    public void testMissing() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("Tough");
            assertEquals("Test class " + strategy.getClass(), -1, strategy.indexOf("Saskatchewan Roughriders"));
        }
    }

    /**
     * Test for a String that uses characters beyond US-ASCII.
     */
    @Test
    public void testUnicodeMiddle() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("\u044Fiders"); // backwards R
            assertEquals("Test class " + strategy.getClass(), 18, strategy.indexOf("Saskatchewan Rough\u044Fiders"));
        }
    }

    /**
     * Test for a String that uses characters beyond US-ASCII.
     */
    @Test
    public void testUnicodeMissing() {
        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("\u044Fiders"); // backwards R
            assertEquals("Test class " + strategy.getClass(), -1, strategy.indexOf("Saskatchewan Roughriders"));
        }
    }

    /**
     * Test for a String that uses characters beyond US-ASCII that equal a tested
     * value when modded by 256. This is significant as the Boyer Moore implementation
     * uses mod 256.
     *
     * <p>This tests a randomly selected character, Unicode 042F, which is rendered as
     * a backwards capital R. This character mod 256 equals a forward slash character '/'.
     *
     * @see <a href="http://www.unicode.org/charts/">Unicode.org</a>
     */
    @Test
    public void testUnicodeCollision() {
        assertEquals(('\u042F'), Character.toUpperCase('\u042F'));
        assertEquals(('/'), Character.toUpperCase('/'));
        assertEquals(('\u042F') % 256, ('/') % 256);

        for(int s = 0; s < strategies.length; s++) {
            TextSearchStrategy strategy = strategies[s];
            strategy.setSubtext("\u042F");
            assertEquals("Test class " + strategy.getClass(), -1, strategy.indexOf("Saskatchewan Roughriders 50/50 Draw"));
        }
    }
}
