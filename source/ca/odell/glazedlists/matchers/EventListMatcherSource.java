/*
 * Copyright(c) 2002-2004, NEXVU Technologies
 * All rights reserved.
 *
 * Created: Feb 18, 2005 - 12:49:37 PM
 */
package ca.odell.glazedlists.matchers;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.Matcher;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;


/**
 * A {@link ca.odell.glazedlists.Matcher} that matches items based on whether they're in
 * an EventList.
 * <p/>
 * Note that use of this class with standard implementations of {@link EventList} is not
 * recommended for large data sets. The reason is that it has to use the {@link
 * EventList#contains(Object)} method, which is a linear search. With large data sets this
 * can be quite slow.
 *
 * @author <a href="mailto:rob@starlight-systems.com">Rob Eden</a>
 */
public class EventListMatcherSource extends AbstractMatcherSource implements ListEventListener {
	protected EventListMatcherSource(Matcher initial_matcher) {
		super(initial_matcher);	// TODO: implement
	}

	public void listChanged(ListEvent listChanges) {
		// TODO: implement
	}

	//    private volatile EventList match_set;
//    private volatile boolean match_in_list;
//
//
//    /**
//     * Create an instance that matches when items are found in the given set.
//     */
//    public EventListMatcherSource(EventList match_set) {
//        this(match_set, true);
//    }
//
//    /**
//     * Create an instance.
//     *
//     * @param match_set     Set used to determine if items shound match or not.
//     * @param match_in_list If true items will match if they are found in
//     *                      <tt>match_set</tt>, otherwise they will match if they are not
//     *                      found in <tt>match_set</tt>.
//     */
//    public EventListMatcherSource(EventList match_set, boolean match_in_list) {
//        if (match_set == null) throw new IllegalArgumentException("List cannot be null");
//
//        match_set.addListEventListener(this);
//
//        this.match_set = match_set;
//        this.match_in_list = match_in_list;
//    }
//
//
//    /**
//     * Determine whether items match if they are in or not in the match set.
//     *
//     * @param match_in_list If true items will match if they are found in
//     *                      <tt>match_set</tt>, otherwise they will match if they are not
//     *                      found in <tt>match_set</tt>.
//     */
//    public synchronized void setMatchInList(boolean match_in_list) {
//        this.match_in_list = match_in_list;
//
//        fireChanged();
//    }
//
//
//    /**
//     * Set the list used to determine what items match.
//     */
//    public synchronized void setMatchSet(EventList match_set) {
//        if (match_set == null) throw new IllegalArgumentException("List cannot be null");
//
//        EventList old_match_set = this.match_set;
//        if (old_match_set != null) old_match_set.removeListEventListener(this);
//
//        match_set.addListEventListener(this);
//        this.match_set = match_set;
//
//        fireChanged();
//    }
//
//
//    public boolean matches(Object item) {
//        boolean in_list = match_set.contains(item);
//
//        return match_in_list ? in_list : !in_list;
//    }
//
//
//    public void listChanged(ListEvent listChanges) {
//        // If we have all deletes or all inserts we can fire an optimized event.
//        // Otherwise, we'll have to update everything
//        boolean has_insert = false;
//        boolean has_delete = false;
//
//        while (listChanges.next()) {
//            int type = listChanges.getType();
//
//// Insert means we'll need to insert a new node in the array
//            if (type == ListEvent.INSERT) {
//                has_insert = true;
//            } else if (type == ListEvent.DELETE) {
//                has_delete = true;
//            } else if (type == ListEvent.UPDATE) {
//// Don't know what it is, count as both
//                has_insert = true;
//                has_delete = true;
//            }
//        }
//
//        if (has_delete && has_insert)
//            fireChanged();
//        else if (has_delete) {
//            if (match_in_list)
//                fireConstrained();
//            else
//                fireRelaxed();
//        } else if (has_insert) {
//            if (match_in_list)
//                fireRelaxed();
//            else
//                fireConstrained();
//        }
//    }
}
