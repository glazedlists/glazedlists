/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 10:27:25 AM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherSourceListener;
import junit.framework.TestCase;


/**
 * Tests {@link AndMatcherSource}, {@link OrMatcherSource}, {@link XorMatcherSource} and {@link
 * NotMatcherSource}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class LogicMatchersTest extends TestCase {
    private final Matcher FALSE_MATCHER = new Matcher() {
        public boolean matches(Object item) {
            return false;
        }

        // don't care
        public void addMatcherListener(MatcherSourceListener listener) {
        }

        public void removeMatcherListener(MatcherSourceListener listener) {
        }
    };

    private final Matcher TRUE_MATCHER = new Matcher() {
        public boolean matches(Object item) {
            return true;
        }

        // don't care
        public void addMatcherListener(MatcherSourceListener listener) {
        }

        public void removeMatcherListener(MatcherSourceListener listener) {
        }
    };


    public void testAndMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertTrue(new AndMatcherSource(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertFalse(new AndMatcherSource(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertFalse(new AndMatcherSource(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new AndMatcherSource(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testOrMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertTrue(new OrMatcherSource(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertTrue(new OrMatcherSource(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertTrue(new OrMatcherSource(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new OrMatcherSource(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testXorMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertFalse(new XorMatcherSource(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertTrue(new XorMatcherSource(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertTrue(new XorMatcherSource(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new XorMatcherSource(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testNotMatcher() {
        Object test_object = "Matchers rule";

        assertTrue(new NotMatcherSource(FALSE_MATCHER).matches(test_object));

        assertFalse(new NotMatcherSource(TRUE_MATCHER).matches(test_object));
    }
}
