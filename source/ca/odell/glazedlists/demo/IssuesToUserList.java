/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.demo;

// glazed lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.swing.*;

/**
 * An IssuesToUserList is a list of users that is obtained by getting
 * the users from an issues list.
 * 
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IssuesToUserList extends TransformedList {
    
    /**
     * Construct an IssuesToUserList from an EventList that contains only 
     * Issue objects.
     */
    public IssuesToUserList(EventList source) {
        super(source);
        source.addListEventListener(this);
    }
    
    /**
     * Gets the user at the specified index.
     */
    public Object get(int index) {
        Issue issue = (Issue)source.get(index);
        return issue.getAssignedTo();
    }
    
    /**
     * When the source issues list changes, propogate the exact same changes
     * for the users list.
     */
    public void listChanged(ListEvent listChanges) {
        // just pass on the changes
        updates.forwardEvent(listChanges);
    }
}
