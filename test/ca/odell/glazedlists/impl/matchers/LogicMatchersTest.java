/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 10:27:25 AM
 */
package ca.odell.glazedlists.impl.matchers;

import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.MatcherListener;
import junit.framework.TestCase;


/**
 * Tests {@link AndMatcher}, {@link OrMatcher}, {@link XorMatcher} and {@link
 * NotMatcher}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class LogicMatchersTest extends TestCase {
    private final Matcher FALSE_MATCHER = new Matcher() {
        public boolean matches(Object item) {
            return false;
        }

        // don't care
        public void addMatcherListener(MatcherListener listener) {
        }

        public void removeMatcherListener(MatcherListener listener) {
        }
    };

    private final Matcher TRUE_MATCHER = new Matcher() {
        public boolean matches(Object item) {
            return true;
        }

        // don't care
        public void addMatcherListener(MatcherListener listener) {
        }

        public void removeMatcherListener(MatcherListener listener) {
        }
    };


    public void testAndMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertTrue(new AndMatcher(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertFalse(new AndMatcher(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertFalse(new AndMatcher(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new AndMatcher(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testOrMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertTrue(new OrMatcher(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertTrue(new OrMatcher(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertTrue(new OrMatcher(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new OrMatcher(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testXorMatcher() {
        Object test_object = "Matchers rule";

        // true, true
        assertFalse(new XorMatcher(TRUE_MATCHER, TRUE_MATCHER).matches(test_object));

        // false, true
        assertTrue(new XorMatcher(FALSE_MATCHER, TRUE_MATCHER).matches(test_object));
        // true, false
        assertTrue(new XorMatcher(TRUE_MATCHER, FALSE_MATCHER).matches(test_object));

        // false, false
        assertFalse(new XorMatcher(FALSE_MATCHER, FALSE_MATCHER).matches(test_object));
    }

    public void testNotMatcher() {
        Object test_object = "Matchers rule";

        assertTrue(new NotMatcher(FALSE_MATCHER).matches(test_object));

        assertFalse(new NotMatcher(TRUE_MATCHER).matches(test_object));
    }
}
