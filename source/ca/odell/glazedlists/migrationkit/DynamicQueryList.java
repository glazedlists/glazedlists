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
// Java collections are used for underlying data storage
import java.util.*;

/**
 * A query list that receives new queries when the user directs
 * that, and then displays the specified new queries. Each dynamic
 * query list requires its own thread, which is either sleeping
 * or updating the list with the freshest query update.
 *
 * @see <a href="https://glazedlists.dev.java.net/tutorial/part6/index.html">Glazed
 * Lists Tutorial Part 6 - Query Lists</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DynamicQueryList extends TransformedList implements Runnable {
    
    /** the thread that executes the queries */
    public Thread queryThread;
    
    /** the interval between refreshes */
    public int queryUpdateInterval;
    
    /** whether this query will execute */
    public boolean active = true;
    
    /** the unique list is the source */
    public UniqueList uniqueSource;

    /** the query containing this list's elements */
    protected Query query = null;

    /**
     * Create a new DynamicQueryList with no query. This starts the
     * thread sleeping. 
     *
     * @param queryUpdateInterval the number of milliseconds between
     *      refreshing the query.
     */
    public DynamicQueryList(int queryUpdateInterval) {
        super(new UniqueList(new BasicEventList()));
        this.queryUpdateInterval = queryUpdateInterval;
        
        query = new EmptyQuery();
        uniqueSource = (UniqueList)source;
        
        // start an update thread
        queryThread = new Thread(this);
        queryThread.setName("Query");
        queryThread.start();
    }
    
    /**
     * Sets the query list to display the specified query. The list
     * will not change immediately, instead it will only change when
     * the specified query has loaded. As a woraround, consider
     * setting the query to an empty query to clear the list and then
     * to the desired query.
     */
    public synchronized void setQuery(Query query) {
        if(query == null) query = new EmptyQuery();
        this.query = query;
        if(active) queryThread.interrupt();
    }
    

    /**
     * Sets this query list to be active or not. A query that is active
     * is normal, it re-executes its query on the specified interval. A
     * query that is not active does not re-execute the query until it
     * is made active again. This can be used to gain performance where
     * multiple queries may be available simultaneously but not displayed.
     */
    public synchronized void setActive(boolean active) {
        this.active = active;
        if(active) queryThread.interrupt();
    }

    /**
     * When a list is changed, the changes are simply propagated to the listeners.
     */
    public void listChanged(ListEvent listChanges) {
        updates.beginEvent();
        while(listChanges.next()) {
            updates.addChange(listChanges.getType(), listChanges.getIndex());
        }
        updates.commitEvent();
    }

    /**
     * Executes the query. This sleeps for the query refresh interval
     * after the query has been run, and then repeats. If a different
     * query is set while this query is executing, that query is interrupted
     * and the new query is started.
     */
    public void run() {
        // keep waiting for queries
        while(true) {
            try {
                if(active) uniqueSource.replaceAll(query.doQuery());
                Thread.sleep(queryUpdateInterval);
            } catch (InterruptedException e) {
                // when interrupted, cancel this query and start the new one
            }
        }
    }

    /**
     * Whenever a user or external force updates an object that may be in this list
     * the list should be notified.
     *
     * <li>If the updated object is already in the list and it still belongs in the list
     * after the update, it will be updated.
     * <li>If the updated object is already in the list and it no longer belongs in the
     * list after the update, it will be removed.
     * <li>If the updated object is not alreayd in the list and it belongs in the list,
     * it will be added.
     */
    public void notifyObjectUpdated(Comparable updated) {
        getReadWriteLock().writeLock().lock();
        try {
            boolean queryMatches = false;
            if(query != null) queryMatches = query.matchesObject(updated);
            int listIndex = uniqueSource.indexOf(updated);

            // when it matches the query and its not in the list, add it!
            if(queryMatches && listIndex == -1) {
                uniqueSource.add(updated);
            // when it matches and it is in the list, update it!
            } else if(queryMatches && listIndex != -1) {
                uniqueSource.set(listIndex, updated);
            // when it doesn't match and it is in the list, remove it!
            } else if(!queryMatches && listIndex != -1) {
                uniqueSource.remove(listIndex);
            }
        } finally {
            getReadWriteLock().writeLock().unlock();
        }
    }
}

/**
 * A query of zero elements.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
class EmptyQuery implements Query {

    /**
     * Gets the name of this query for display in a logging message.
     */
    public String getName() {
        return "Empty Query";
    }
    
    /**
     * Returns an empty set.
     */
    public SortedSet doQuery() throws InterruptedException {
        return new TreeSet();
    }

    /**
     * The empty query matches no objects.
     */
    public boolean matchesObject(Comparable object) {
        return false;
    }
}
