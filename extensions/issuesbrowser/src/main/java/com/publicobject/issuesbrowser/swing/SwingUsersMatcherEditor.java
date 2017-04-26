/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package com.publicobject.issuesbrowser.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import ca.odell.glazedlists.swing.DefaultEventListModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;

import com.publicobject.issuesbrowser.Issue;
import com.publicobject.issuesbrowser.UsersMatcherEditor;
import com.publicobject.misc.swing.NoFocusRenderer;

/**
 * A UsersMatcherEditor with Swing support.
 */
class SwingUsersMatcherEditor extends UsersMatcherEditor implements FilterComponent<Issue> {

    /** a widget for selecting users */
    private JList userSelect;

    /** scroll through users */
    private JScrollPane scrollPane;

    /** ThreadProxyList for user list */
    private TransformedList<String, String> allUserProxyList;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public SwingUsersMatcherEditor(EventList<Issue> source) {
        super(source);

        // create a JList that contains users
        final EventList<String> allUsers = getUsersList();
        allUserProxyList = GlazedListsSwing.swingThreadProxyList(allUsers);
        final DefaultEventListModel<String> usersListModel = new DefaultEventListModel<String>(allUserProxyList);
        userSelect = new JList(usersListModel);
        userSelect.setPrototypeCellValue("jessewilson");
        userSelect.setVisibleRowCount(10);
        // turn off cell focus painting
        userSelect.setCellRenderer(new NoFocusRenderer(userSelect.getCellRenderer()));

        // create an EventList containing the JList's selection
        final AdvancedListSelectionModel<String> userSelectionModel = GlazedListsSwing.eventSelectionModel(allUserProxyList);
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

    @Override
    public String toString() {
        return "Users";
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public MatcherEditor<Issue> getMatcherEditor() {
        return this;
    }

    @Override
    public void dispose() {
        allUserProxyList.dispose();
        super.dispose();
    }
}