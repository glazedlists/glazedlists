/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * An UsersMatcherEditor is a matcher editor that filters based on the selected
 * users.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class UsersMatcherEditor extends AbstractMatcherEditor<Issue> {

    /** a list of users */
    private EventList<String> allUsers = null;

    /** a list that maintains selection */
    private EventList<String> selectedUsers = null;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public UsersMatcherEditor(EventList<Issue> source) {
        // create a unique users list from the source issues list
        allUsers = new UniqueList<String>(new CollectionList<Issue, String>(source, new IssueUserator()));
    }

    /**
     * Sets the selection driven EventList which triggers filter changes.
     */
    public void setSelectionList(EventList<String> usersSelectedList) {
        this.selectedUsers = usersSelectedList;
        usersSelectedList.addListEventListener(new SelectionChangeEventList());
    }

    /**
     * Allow access to the unique list of users
     */
    public EventList<String> getUsersList() {
        return allUsers;
    }

    /**
     * An EventList to respond to changes in selection from the ListEventViewer.
     */
    private final class SelectionChangeEventList implements ListEventListener<String> {

        /** {@inheritDoc} */
        public void listChanged(ListEvent<String> listChanges) {
            // if we have all or no users selected, match all users
            if(selectedUsers.isEmpty() || selectedUsers.size() == allUsers.size()) {
                fireMatchAll();
                return;
            }

            // match the selected subset of users
            final UserMatcher newUserMatcher = new UserMatcher(selectedUsers);

            // get the previous matcher. If it wasn't a user matcher, it must
            // have been an 'everything' matcher, so the new matcher must be
            // a constrainment of that
            final Matcher<Issue> previousMatcher = getMatcher();
            if(!(previousMatcher instanceof UserMatcher)) {
                fireConstrained(newUserMatcher);
                return;
            }
            final UserMatcher previousUserMatcher = (UserMatcher)previousMatcher;

            // Figure out what type of change to fire. This is an optimization over
            // always calling fireChanged() because it allows the FilterList to skip
            // extra elements by knowing how the new matcher relates to its predecessor
            boolean relaxed = newUserMatcher.isRelaxationOf(previousMatcher);
            boolean constrained = previousUserMatcher.isRelaxationOf(newUserMatcher);
            if(relaxed && constrained) return;

            if(relaxed) fireRelaxed(newUserMatcher);
            else if(constrained) fireConstrained(newUserMatcher);
            else fireChanged(newUserMatcher);
        }
    }

    /**
     * If the set of users for this matcher contains any of an
     * issue's users, that issue matches.
     */
    private static class UserMatcher implements Matcher<Issue> {
        private final Set<String> users;

        /**
         * Create a new {@link UserMatcher}, creating a private copy
         * of the specified {@link Collection} to match against. A private
         * copy is made because {@link Matcher}s must be immutable.
         */
        public UserMatcher(Collection<String> users) {
            this.users = new HashSet<String>(users);
        }

        /**
         * @return true if this matches every {@link Issue} the other matches.
         */
        public boolean isRelaxationOf(Matcher other) {
            if(!(other instanceof UserMatcher)) return false;
            UserMatcher otherUserMatcher = (UserMatcher)other;
            return users.containsAll(otherUserMatcher.users);
        }

        /**
         * Test whether to include or not include the specified issue based
         * on whether or not their user is selected.
         */
        public boolean matches(Issue issue) {
            for (Iterator<String> i = issue.getAllUsers().iterator(); i.hasNext(); ) {
                if(this.users.contains(i.next())) return true;
            }
            return false;
        }
    }
}