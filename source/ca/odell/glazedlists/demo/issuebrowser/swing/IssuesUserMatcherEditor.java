package ca.odell.glazedlists.demo.issuebrowser.swing;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.demo.issuebrowser.IssueUserator;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.swing.*;

/**
 * An IssuesUserMatcherEditor is a filter list that filters based on the selected
 * users.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class IssuesUserMatcherEditor extends AbstractMatcherEditor<Issue> implements ListSelectionListener {

    /** a list of users */
    private EventList<String> allUsers;
    private EventList<String> selectedUsers;

    /** a widget for selecting users */
    private JList userSelect;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public IssuesUserMatcherEditor(EventList<Issue> source) {
        // create a unique users list from the source issues list
        allUsers = new UniqueList<String>(new CollectionList<String,Issue>(source, new IssueUserator()));

        // create a JList that contains users
        EventListModel<String> usersListModel = new EventListModel<String>(allUsers);
        userSelect = new JList(usersListModel);

        // create an EventList containing the JList's selection
        EventSelectionModel<String> userSelectionModel = new EventSelectionModel<String>(allUsers);
        userSelect.setSelectionModel(userSelectionModel);
        selectedUsers = userSelectionModel.getSelected();
        userSelect.addListSelectionListener(this);
    }

    /**
     * Get the widget for selecting users.
     */
    public JList getUserSelect() {
        return userSelect;
    }

    /**
     * When the JList selection changes, refilter.
     */
    public void valueChanged(ListSelectionEvent e) {
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
        else if(relaxed) fireRelaxed(newUserMatcher);
        else if(constrained) fireConstrained(newUserMatcher);
        else fireChanged(newUserMatcher);
    }

    /**
     * If the set of users for this matcher contains any of an
     * issue's users, that issue matches.
     */
    private static class UserMatcher implements Matcher<Issue> {
        private Set<String> users;

        /**
         * Create a new {@link UserMatcher}, creating a private copy
         * of the specified {@link Collection} to match against. A private
         * copy is made because {@link Matcher}s must be immutable.
         */
        public UserMatcher(Collection<String> users) {
            this.users = new HashSet<String>(users);
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
        public boolean matches(Issue issue) {
            return !Collections.disjoint(this.users, issue.getAllUsers());
        }
    }
}