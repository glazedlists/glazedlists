/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.migrationkit;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
// Swing toolkit stuff for displaying widgets
import javax.swing.*;
import java.awt.GridBagLayout;
// for responding to user actions
import java.awt.event.*;
// for displaying lists in combo boxes
import javax.swing.ListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
// for keeping track of a set of listeners
import java.util.ArrayList;


/**
 * A string completer that gets completions from a dynamic list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ListStringCompleter implements StringCompleter {

    /** the complete list of messages before filters */
    protected EventList source;
        
    /**
     * Creates a new string completer that reads the toString() values of the
     * specified list as its completion input.
     */
    public ListStringCompleter(EventList source) {
        this.source = source;
    }

    /**
     * Takes a String and return a longer string which has the
     * supplied string as a prefix.
     */
    public String getCompleted(String prefix) {
        if(prefix.length() == 0) return prefix;
        // get all strings which this is a prefix of (and prefixes of this)
        for(int i = 0; i < source.size(); i++) {
            Object current = source.get(i);
            String completion = current.toString();
            if(completion.indexOf(prefix) == 0) {
                return completion;
            }
        }
        return prefix;
    }
}

