/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.demo.issuebrowser.swt.UsersMatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventSelectionModel;

import javax.swing.*;

/**
 * A UsersMatcherEditor with Swing support.
 *
 */
class SwingUsersMatcherEditor extends UsersMatcherEditor {

    /** a widget for selecting users */
    private JList userSelect;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public SwingUsersMatcherEditor(EventList source) {
        super(source);

        // create a JList that contains users
        EventList allUsers = getUsersList();
        EventListModel usersListModel = new EventListModel(allUsers);
        userSelect = new JList(usersListModel);

        // create an EventList containing the JList's selection
        EventSelectionModel userSelectionModel = new EventSelectionModel(allUsers);
        userSelect.setSelectionModel(userSelectionModel);
        setSelectionList(userSelectionModel.getSelected());
    }

    /**
     * Get the widget for selecting users.
     */
    public JList getUserSelect() {
        return userSelect;
    }
}