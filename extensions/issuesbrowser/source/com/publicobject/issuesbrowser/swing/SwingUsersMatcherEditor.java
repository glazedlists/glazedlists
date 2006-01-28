/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.EventSelectionModel;
import com.publicobject.issuesbrowser.UsersMatcherEditor;
import com.publicobject.issuesbrowser.Issue;

import javax.swing.*;

/**
 * A UsersMatcherEditor with Swing support.
 *
 */
class SwingUsersMatcherEditor extends UsersMatcherEditor implements FilterComponent<Issue> {

    /** a widget for selecting users */
    private JList userSelect;

    /** scroll through users */
    private JScrollPane scrollPane;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public SwingUsersMatcherEditor(EventList<Issue> source) {
        super(source);

        // create a JList that contains users
        EventList allUsers = getUsersList();
        EventListModel<String> usersListModel = new EventListModel<String>(allUsers);
        userSelect = new JList(usersListModel);
        userSelect.setPrototypeCellValue("jessewilson");
        userSelect.setVisibleRowCount(10);
        // turn off cell focus painting
        userSelect.setCellRenderer(new NoFocusRenderer(userSelect.getCellRenderer()));

        // create an EventList containing the JList's selection
        EventSelectionModel<String> userSelectionModel = new EventSelectionModel<String>(allUsers);
        userSelect.setSelectionModel(userSelectionModel);
        setSelectionList(userSelectionModel.getSelected());

        // scroll through selected users
        scrollPane = new JScrollPane(userSelect, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * Get the widget for selecting users.
     */
    public JList getUserSelect() {
        return userSelect;
    }


    public String getName() {
        return "Users";
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }
}