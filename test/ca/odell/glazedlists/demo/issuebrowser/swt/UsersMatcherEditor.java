/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swt;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.event.*;
// the public demo
import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.demo.issuebrowser.IssueUserator;

/**
 * An UsersMatcherEditor is a matcher editor that filters based on the selected
 * users.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class UsersMatcherEditor extends AbstractMatcherEditor {

    /** a list of users */
    private EventList allUsers = null;

    /** a list that maintains selection */
    private EventList selectedUsers = null;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public UsersMatcherEditor(EventList source) {
        // create a unique users list from the source issues list
        allUsers = new UniqueList(new CollectionList(source, new IssueUserator()));
    }

    /**
     * Sets the selection driven EventList which triggers filter changes.
     */
    public void setSelectionList(EventList usersSelectedList) {
        this.selectedUsers = usersSelectedList;
        usersSelectedList.addListEventListener(new SelectionChangeEventList());
    }

    /**
     * Allow access to the unique list of users
     */
    public EventList getUsersList() {
        return allUsers;
    }

    /**
     * An EventList to respond to changes in selection from the ListEventViewer.
     */
    private final class SelectionChangeEventList implements ListEventListener {

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
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
            final Matcher previousMatcher = getMatcher();
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
            else if(relaxed) fireRelaxed(newUserMatcher);
            else if(constrained) fireConstrained(newUserMatcher);
            else fireChanged(newUserMatcher);
        }
    }



    /**
     * If the set of users for this matcher contains any of an
     * issue's users, that issue matches.
     */
    private static class UserMatcher implements Matcher {
        private Set users;

        /**
         * Create a new {@link UserMatcher}, creating a private copy
         * of the specified {@link Collection} to match against. A private
         * copy is made because {@link Matcher}s must be immutable.
         */
        public UserMatcher(Collection users) {
            this.users = new HashSet(users);
        }

        /**
         * @return true if this matches every {@link Issue} that other matches.
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
        public boolean matches(Object element) {
            Issue issue = (Issue)element;
            for(Iterator i = issue.getAllUsers().iterator(); i.hasNext(); ) {
                if(this.users.contains(i.next())) return true;
            }
            return false;
        }
    }
}
