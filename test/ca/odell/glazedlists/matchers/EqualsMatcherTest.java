/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 22, 2005 - 5:23:40 PM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Test {@link EqualsMatcherSource}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EqualsMatcherTest extends TestCase {
    private static final List INITIAL_LIST = Arrays.asList(new Integer[]{
        new Integer(0),
        new Integer(1),
        new Integer(1)
    });

    EventList parent_list;
    FilterList filter_list;

    EqualsMatcherSource matcher;


    protected void setUp() throws Exception {
        parent_list = new BasicEventList(new LinkedList(INITIAL_LIST));

        matcher = new EqualsMatcherSource();
        filter_list = new FilterList(parent_list, matcher);
    }

    protected void tearDown() throws Exception {
        filter_list.dispose();
        filter_list = null;

        parent_list = null;
    }


    public void testMatchOnNull() {
        matcher.setMatchOnNoValue(true);
        assertEquals(parent_list.size(), filter_list.size());
        for (int i = 0; i < parent_list.size(); i++) {
            assertEquals(parent_list.get(i), filter_list.get(i));
        }

        matcher.setMatchOnNoValue(false);
        assertTrue(filter_list.isEmpty());
    }


    public void testIdentityAndEquals() {
        // Use equals()
        matcher.setUseEquals(true);

        // Can match using equals()
        matcher.setMatchValue(new Integer(1));
        assertEquals(2, filter_list.size());

        // Cat match using ==
        matcher.setMatchValue(INITIAL_LIST.get(1));
        assertEquals(2, filter_list.size());


        // Use ==
        matcher.setUseEquals(false);

        // Can match using equals()
        matcher.setMatchValue(new Integer(1));
        assertTrue(filter_list.isEmpty());

        // Cat match using ==
        matcher.setMatchValue(INITIAL_LIST.get(1));
        assertEquals(1, filter_list.size());
        assertEquals(new Integer(1), filter_list.get(0));


        // Make sure it goes back
        matcher.setUseEquals(true);
        assertEquals(2, filter_list.size());
    }


    public void testChangingMatchValue() {
        matcher.setMatchValue(new Integer(1));
        assertEquals(2, filter_list.size());

        matcher.setMatchValue(new Integer(0));
        assertEquals(1, filter_list.size());
    }


    public void testLogicFlipping() {
        // Using equals()
        matcher.setMatchValue(new Integer(1));
        matcher.setUseEquals(true);
        matcher.setMatchOnEqual(true);
        assertEquals(2, filter_list.size());
        assertEquals(new Integer(1), filter_list.get(0));
        assertEquals(new Integer(1), filter_list.get(1));

        matcher.setMatchOnEqual(false);
        assertEquals(1, filter_list.size());
        assertEquals(new Integer(0), filter_list.get(0));

        // Using identity matching
        matcher.setMatchValue(INITIAL_LIST.get(1));
        matcher.setMatchOnEqual(true);
        matcher.setUseEquals(false);
        assertEquals(1, filter_list.size());
        assertEquals(new Integer(1), filter_list.get(0));

        matcher.setMatchOnEqual(false);
        assertEquals(2, filter_list.size());
        assertEquals(new Integer(0), filter_list.get(0));
        assertEquals(new Integer(1), filter_list.get(1));
        assertNotSame(INITIAL_LIST.get(1), filter_list.get(1));
    }
}
