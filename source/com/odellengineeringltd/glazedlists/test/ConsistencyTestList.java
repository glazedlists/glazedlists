/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.test;

// the Glazed Lists
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;

/**
 * A very basic list that ensures that lists are kept consistent and that
 * the change events are consistent.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class ConsistencyTestList implements ListChangeListener {

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
        source.addListChangeListener(new ListChangeListenerEventThreadProxy(this));
    }
    
    /**
     * For implementing the ListChangeListener interface.     
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        // for all changes, one index at a time
        while(listChanges.next()) {
            
            // get the current change info
            int changeIndex = listChanges.getIndex();
            int changeType = listChanges.getType();
            
            if(changeType == ListChangeBlock.INSERT) size++;
            else if(changeType == ListChangeBlock.DELETE) size--;
            
        }
        if(size != source.size()) {
            new Exception(name + "/" + changeCount + " size consistency problem! Expected " + size + ", got " + source.size()).printStackTrace();
        } else {
            System.out.println(name + "/" + changeCount + " size " + size);
        }
        changeCount++;
    }
}
