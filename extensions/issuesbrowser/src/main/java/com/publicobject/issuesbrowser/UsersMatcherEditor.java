/* Glazed Lists                                                 (c) 2003-2006 */
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

/**
 * An UsersMatcherEditor is a matcher editor that filters based on the selected
 * users.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 * @author <a href="mailto:jesse@swank.ca">Jesse Wilson</a>
 */
public class UsersMatcherEditor extends AbstractMatcherEditor<Issue> {

    /** a list of users */
    private CollectionList<Issue, String> usersForIssues;
    private UniqueList<String> allUsers;

    /** a list that maintains selection */
    private EventList<String> selectedUsers;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public UsersMatcherEditor(EventList<Issue> source) {
        // create a unique users list from the source issues list
        usersForIssues = new CollectionList<>(source, Issue::getAllUsers);
        allUsers = UniqueList.create(usersForIssues);
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

    public void dispose() {
        allUsers.dispose();
        usersForIssues.dispose();
    }

    /**
     * An EventList to respond to changes in selection from the ListEventViewer.
     */
    private final class SelectionChangeEventList implements ListEventListener<String> {

        /** {@inheritDoc} */
        @Override
        public void listChanged(ListEvent<String> listChanges) {
            // if we have all or no users selected, match all users
            if(selectedUsers.isEmpty() || selectedUsers.size() == allUsers.size()) {
                fireMatchAll();
                return;
            }

            // match the selected subset of users
            final StringValueMatcher newUserMatcher = new StringValueMatcher(selectedUsers, Issue::getAllUsers);

            // get the previous matcher. If it wasn't a user matcher, it must
            // have been an 'everything' matcher, so the new matcher must be
            // a constrainment of that
            final Matcher<Issue> previousMatcher = getMatcher();
            if(!(previousMatcher instanceof StringValueMatcher)) {
                fireConstrained(newUserMatcher);
                return;
            }
            final StringValueMatcher previousUserMatcher = (StringValueMatcher) previousMatcher;

            // Figure out what type of change to fire. This is an optimization over
            // always calling fireChanged() because it allows the FilterList to skip
            // extra elements by knowing how the new matcher relates to its predecessor
            boolean relaxed = newUserMatcher.isRelaxationOf(previousMatcher);
            boolean constrained = previousUserMatcher.isRelaxationOf(newUserMatcher);
            if(relaxed && constrained) {
                return;
            }

            if(relaxed) {
                fireRelaxed(newUserMatcher);
            } else if(constrained) {
                fireConstrained(newUserMatcher);
            } else {
                fireChanged(newUserMatcher);
            }
        }
    }

}