/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo.issuebrowser.swt;

// glazed lists

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// the public demo
import ca.odell.glazedlists.demo.issuebrowser.Issue;
import ca.odell.glazedlists.demo.issuebrowser.IssuesToUserList;

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
        usersEventList = new UniqueList(new IssuesToUserList(source));
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

        Issue issue = (Issue) o;
        String user = issue.getAssignedTo();
        return usersSelectedList.contains(user);
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
            // maintain the state of the filters as they are affected by the changes
            int stateMask = 0;
            final int FILTER_RELAXED = 1;
            final int FILTER_CONSTRAINED = 2;
            final int FILTER_CHANGED = 3;

            /**
             * Loop through all the changes to see how the filters change
             */
            while(listChanges.next()) {
                int type = listChanges.getType();

                // Reordering events have no effect on filters
                if(listChanges.isReordering()) {
                    // no-op

                // Updates requires that the filter has changed
                } else if(type == ListEvent.UPDATE) {
                    stateMask = FILTER_CHANGED;

                // Only deletes result in a filter relaxation
                } else if(type == ListEvent.DELETE) {
                    stateMask = stateMask | FILTER_RELAXED;

                // Only inserts result in a filter constrainment
                } else if(type == ListEvent.INSERT) {
                    stateMask = stateMask | FILTER_CONSTRAINED;
                }
            }

            // The filter now contains a different set of users
            if((stateMask & FILTER_CHANGED) != 0) {
                handleFilterChanged();

            // The filter now contains fewer users
            } else if((stateMask & FILTER_RELAXED) != 0) {
                handleFilterRelaxed();

            // The filter now contains more users
            } else if((stateMask & FILTER_CONSTRAINED) != 0) {
                handleFilterConstrained();
            }
        }
    }
}
