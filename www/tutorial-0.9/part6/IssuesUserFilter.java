/**
 * Glazed Lists Tutorial
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2004 O'DELL ENGINEERING LTD.
 */

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.*;

import ca.odell.glazedlists.test.*;
/**
 * An IssuesUserFilter is a filter list that filters based on the selected
 * users.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesUserFilter extends AbstractFilterList implements ListSelectionListener {
    
    /** a list of users */
    EventList usersEventList;
    EventList usersSelectedList;
    /** a widget for selecting users */
    JList userSelect;
    
    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public IssuesUserFilter(EventList source) {
        super(source);
        
        // create a unique users list from the source issues list
        source.addListEventListener(new ConsistencyTestList(source, "source"));
        SortedList sortedSource = new SortedList(source);
        sortedSource.addListEventListener(new ConsistencyTestList(sortedSource, "sortedSource"));
        usersEventList = new UniqueList(new IssuesToUserList(source));
        ((UniqueList)usersEventList).debug = true;
        usersEventList.addListEventListener(new ConsistencyTestList(usersEventList, "users-unique"));

        // create a JList that contains users
        EventListModel usersListModel = new EventListModel(usersEventList);
        userSelect = new JList(usersListModel);

        // create an EventList containing the JList's selection
        EventSelectionModel userSelectionModel = new EventSelectionModel(usersEventList);
        userSelect.setSelectionModel(userSelectionModel.getListSelectionModel());
        usersSelectedList = userSelectionModel.getEventList();
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
        usersSelectedList.getReadWriteLock().readLock().lock();
        try {
            if(o == null) return false;
            if(usersSelectedList.isEmpty()) return true;
            
            Issue issue = (Issue)o;
            String user = issue.getAssignedTo();
            return usersSelectedList.contains(user);
            
        } finally {
            usersSelectedList.getReadWriteLock().readLock().unlock();
        }
    }
}
