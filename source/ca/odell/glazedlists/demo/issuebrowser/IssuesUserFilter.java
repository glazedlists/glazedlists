/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;


/**
 * An IssuesUserFilter is a filter list that filters based on the selected
 * users.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesUserFilter extends AbstractFilterList implements ListSelectionListener {

    /** a list of users */
    private EventList usersEventList;
    private EventList usersSelectedList;

    /** a widget for selecting users */
    private JList userSelect;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public IssuesUserFilter(EventList source) {
        super(source);

        // create a unique users list from the source issues list
        usersEventList = new UniqueList(new CollectionList(source, new IssueUserator()));

        // create a JList that contains users
        EventListModel usersListModel = new EventListModel(usersEventList);
        userSelect = new JList(usersListModel);

        // create an EventList containing the JList's selection
        EventSelectionModel userSelectionModel = new EventSelectionModel(usersEventList);
        userSelect.setSelectionModel(userSelectionModel);
        usersSelectedList = userSelectionModel.getSelected();
        userSelect.addListSelectionListener(this);

        handleFilterChanged();
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
        handleFilterChanged();
    }

    /**
     * Test whether to include or not include the specified issue based
     * on whether or not their user is selected.
     */
    public boolean filterMatches(Object o) {
        if (o == null) return false;
        if (usersSelectedList.isEmpty()) return true;

        Issue issue = (Issue)o;

        // see if the two lists have just one intersection
        List users = issue.getAllUsers();
        for(Iterator u = users.iterator(); u.hasNext(); ) {
            String user = (String)u.next();
            if(usersSelectedList.contains(user)) return true;
        }

        // no intersection
        return false;
    }
}
