/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicboject.com,*/ 
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.demo.issuebrowser.swt;

import java.util.*;
// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// the public demo
import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.demo.issuebrowser.IssueUserator;

/**
 * An IssuesUserFilter is a filter list that filters based on the selected
 * users.  This is a gutted and retrofitted version of the IssuesUserFilter
 * found in the Swing package for the shiny new SWT demo.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public class IssuesUserFilter extends AbstractFilterList {

    /** a list of users */
    EventList usersEventList = null;

    /** a list that maintains selection */
    EventList usersSelectedList = null;

    /**
     * Create a filter list that filters the specified source list, which
     * must contain only Issue objects.
     */
    public IssuesUserFilter(EventList source) {
        super(source);

        // create a unique users list from the source issues list
        usersEventList = new UniqueList(new CollectionList(source, new IssueUserator()));
    }

    /**
     * Sets the selection driven EventList which triggers filter changes.
     */
    public void setSelectionList(EventList usersSelectedList) {
        this.usersSelectedList = usersSelectedList;
        usersSelectedList.addListEventListener(new SelectionChangeEventList());
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

    /**
     * Allow access to the unique list of users
     */
    EventList getUsersList() {
        return usersEventList;
    }

    /**
     * An EventList to respond to changes in selection from the ListEventViewer.
     */
    private final class SelectionChangeEventList implements ListEventListener {

        /** {@inheritDoc} */
        public void listChanged(ListEvent listChanges) {
            // skip reorderings
            if(listChanges.isReordering()) {
                listChanges.getReorderMap();
                return;
            }

            // Loop through all the changes to see how the filters change
            boolean constrained = false;
            boolean relaxed = false;
            while(listChanges.next()) {
                // do nothing
            }

            // The filter now contains a different set of users
            getReadWriteLock().writeLock().lock();
            try {
                handleFilterChanged();
            } finally {
                getReadWriteLock().writeLock().unlock();
            }
        }
    }
}
