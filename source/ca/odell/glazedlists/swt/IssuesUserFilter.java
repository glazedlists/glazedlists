/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// the public demo
import ca.odell.glazedlists.demo.Issue;
import ca.odell.glazedlists.demo.IssuesToUserList;
// for ArrayList
import java.util.ArrayList;
// swt
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.SelectionEvent;

/**
 * An IssuesUserFilter is a filter list that filters based on the selected
 * users.  This is a gutted and retrofitted version of the IssuesUserFilter
 * found in the Swing package for the shiny new SWT demo.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IssuesUserFilter extends AbstractFilterList implements SelectionListener {

    /** a list of users */
    EventList usersEventList;
    ArrayList usersSelectedList;

    /** a widget for selecting users */
    List userList;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public IssuesUserFilter(EventList source) {
        super(source);

        // create a unique users list from the source issues list
        usersEventList = new UniqueList(new IssuesToUserList(source));
        usersSelectedList = new ArrayList();
    }

    public void setList(List userList) {
        this.userList = userList;
        userList.addSelectionListener(this);
        handleFilterChanged();
    }

    /**
     * When the List selection changes, refilter.
     */
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }
    public void widgetSelected(SelectionEvent e) {
        usersSelectedList.clear();
        String[] selection = userList.getSelection();
        for(int i = 0; i < selection.length; i++) {
            usersSelectedList.add(selection[i]);
        }
        handleFilterChanged();
    }

    /**
     * Test whether to include or not include the specified issue based
     * on whether or not their user is selected.
     */
    public boolean filterMatches(Object o) {
        if(o == null) return false;
        if(usersSelectedList.isEmpty()) return true;

        Issue issue = (Issue)o;
        String user = issue.getAssignedTo();
        return usersSelectedList.contains(user);
    }

    public EventList getUsersList() {
        return usersEventList;
    }

    /**
     * Adds a listener to the unique users list.
     */
    public void addUserListListener(ListEventListener userListListener) {
        usersEventList.addListEventListener(userListListener);
    }
}
