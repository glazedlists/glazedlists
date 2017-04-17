/* Glazed Lists                                                 (c) 2003-2014 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.javafx;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.javafx.EventObservableList;
import ca.odell.glazedlists.javafx.GlazedListsFx;
import ca.odell.glazedlists.matchers.Matcher;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.UsersMatcherEditor;

import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;

import java.util.List;

/**
 * A UsersMatcherEditor with JavaFx support.
 *
 * @author Holger Brands
 */
public final class JavaFxUsersMatcherEditor extends UsersMatcherEditor {

    /** a widget for selecting users */
    private ListView<String> userSelect;

    /** scroll through users */
    private ScrollPane scrollPane;

    /** JavaFx-Thread proxy list. */
    private EventList<String> threadProxyAllUsers;

    /** JAvaFX observable list adapted from source evetn list. */
    private ObservableList<String> observableAllUsers;

    /**
     * Builds a {@link ListView} of all users related to issues. Based on the user selection, the
     * issue list will be filtered according to the user association.
     * @param source the issue list
     */
    public JavaFxUsersMatcherEditor(EventList<Issue> source) {
        super(source);
        final EventList<String> allUsers = getUsersList();
        threadProxyAllUsers = GlazedListsFx.threadProxyList(allUsers);
        observableAllUsers = new EventObservableList<String>(threadProxyAllUsers);
        userSelect = new ListView<String>();
        userSelect.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        userSelect.setItems(observableAllUsers);
        scrollPane = new ScrollPane();
        scrollPane.setContent(userSelect);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        getSelectedUsers().addListener(new UserSelectionChangeListener());
    }

    /**
     * @return the currently selected users
     */
    private ObservableList<String> getSelectedUsers() {
        return userSelect.getSelectionModel().getSelectedItems();
    }

    /**
     * Get the widget for selecting users.
     */
    public ListView<String> getUserSelect() {
        return userSelect;
    }

    /**
     * @return the {@link ListView} inside {@link ScrollPane} for display
     */
    public Control getControl() {
        return scrollPane;
    }

    /**
     * A listener to respond to changes in user selection from the ListView
     */
    private final class UserSelectionChangeListener implements javafx.collections.ListChangeListener<String> {

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends String> c) {
            final List<String> selectedUsers = getSelectedUsers();
            // if we have all or no users selected, match all users
            if(selectedUsers.isEmpty() || selectedUsers.size() == observableAllUsers.size()) {
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
