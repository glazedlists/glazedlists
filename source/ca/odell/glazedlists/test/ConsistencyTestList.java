/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.test;

// the Glazed Lists
import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;

/**
 * A very basic list that ensures that lists are kept consistent and that
 * the change events are consistent.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ConsistencyTestList implements ListEventListener {

    /** the internally tracked size, to compare with for consistency */
    private int size;
    
    /** a name for reporting problems with the list */
    private String name;
    
    /** the source list to compare against */
    private EventList source;
    
    /** keep a count of the number of changes, for reporting */
    private int changeCount = 0;
    
    /**
     * Creates a new ConsistencyTestList that ensures events from the source
     * list are consistent.
     */
    public ConsistencyTestList(EventList source, String name) {
        this.source = source;
        this.name = name;
        size = source.size();
    }
    
    /**
     * For implementing the ListEventListener interface.     
     */
    public void listChanged(ListEvent listChanges) {
        // keep track of the highest change index so far
        int highestChangeIndex = 0;
        
        // for all changes, one index at a time
        while(listChanges.next()) {
            
            // get the current change info
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();
            
            // make sure the change indicies are not descreasing
            if(changeIndex < highestChangeIndex) {
                new Exception(name + "/" + changeCount + " change indicies not in order, " + changeIndex + " after " + highestChangeIndex + ", event: " + listChanges).printStackTrace();
            }
            highestChangeIndex = changeIndex;
                
            // verify the index is big enough
            if(changeIndex < 0) new Exception(name + "/" + changeCount + " cannot insert at " + changeIndex + ", event: " + listChanges).printStackTrace();

            // verify the index is small enough, and adjust the size
            if(changeType == ListEvent.INSERT) {
                if(changeIndex > size) new Exception(name + "/" + changeCount + " cannot insert at " + changeIndex + ", size is: " + size + ", event: " + listChanges).printStackTrace();
                size++;
            } else if(changeType == ListEvent.DELETE) {
                if(changeIndex >= size) new Exception(name + "/" + changeCount + " cannot delete at " + changeIndex + ", size is: " + size + ", event: " + listChanges).printStackTrace();
                size--;
            } else if(changeType == ListEvent.UPDATE) {
                if(changeIndex >= size) new Exception(name + "/" + changeCount + " cannot update at " + changeIndex + ", size is: " + size + ", event: " + listChanges).printStackTrace();
            }
        }
        
        // verify the size is consistent with the source
        if(size != source.size()) {
            new Exception(name + "/" + changeCount + " size consistency problem! Expected " + size + ", got " + source.size() + ", event: " + listChanges).printStackTrace();
        }
        changeCount++;
    }
}
