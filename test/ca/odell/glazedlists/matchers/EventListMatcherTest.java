/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 1:07:00 PM
 */
package ca.odell.glazedlists.matchers;

import junit.framework.TestCase;


/**
 * Test {@link EventListMatcherSource}.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EventListMatcherTest extends TestCase {
//    private static final List INITIAL_LIST = Arrays.asList(new Integer[]{
//        new Integer(0),
//        new Integer(1),
//        new Integer(2),
//        new Integer(3),
//        new Integer(4),
//        new Integer(5),
//        new Integer(6),
//        new Integer(7),
//        new Integer(8),
//        new Integer(9),
//        new Integer(10)
//    });
//
//    EventList parent_list;
//    FilterList filter_list;
//
//    EventList match_set;
//    EventListMatcherSource matchersource;
//
//
//    protected void setUp() throws Exception {
//        parent_list = new BasicEventList(new LinkedList(INITIAL_LIST));
//
//        match_set = new BasicEventList();
//
//        matchersource = new EventListMatcherSource(match_set);
//        filter_list = new FilterList(parent_list, matchersource);
//    }
//
//    protected void tearDown() throws Exception {
//        filter_list.dispose();
//        filter_list = null;
//
//        parent_list = null;
//    }
//
//
//    public void testMatching() {
//        // Match when in list
//        matchersource.setMatchInList(true);
//        assertTrue(filter_list.isEmpty());
//
//        match_set.add(new Integer(0));
//        assertEquals(1, filter_list.size());
//        assertEquals(new Integer(0), filter_list.get(0));
//
//        match_set.clear();
//        assertTrue(filter_list.isEmpty());
//
//        // Match when not in list
//        matchersource.setMatchInList(false);
//        assertEquals(parent_list.size(), filter_list.size());
//
//        match_set.add(new Integer(0));
//        assertEquals(parent_list.size() - 1, filter_list.size());
//        assertEquals(new Integer(1), filter_list.get(0));
//
//        match_set.clear();
//        matchersource.setMatchInList(true);
//    }
//
//
//    public void testWrites() {
//        // Add when not initially shown via filter
//        match_set.add(new Integer(0));
//        matchersource.setMatchInList(true);
//
//        filter_list.add(new Integer(11));
//
//        assertEquals(INITIAL_LIST.size() + 1, parent_list.size());
//        assertEquals(1, filter_list.size());
//        assertEquals(new Integer(0), filter_list.get(0));
//
//        match_set.clear();
//        match_set.add(new Integer(11));
//
//        assertEquals(1, filter_list.size());
//        assertEquals(new Integer(11), filter_list.get(0));
//
//        // Add when shown by filter
//        match_set.add(new Integer(12));
//
//        assertEquals(1, filter_list.size());
//        assertEquals(new Integer(11), filter_list.get(0));
//
//        filter_list.add(new Integer(12));
//
//        assertEquals(2, filter_list.size());
//        assertEquals(INITIAL_LIST.size() + 2, parent_list.size());
//        assertEquals(new Integer(11), filter_list.get(0));
//        assertEquals(new Integer(12), filter_list.get(1));
//    }
}
