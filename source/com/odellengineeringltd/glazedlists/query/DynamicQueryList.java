/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.query;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// for running on a schedule
import java.util.TimerTask;

/**
 * A query list that receives new queries when the user directs
 * that, and then displays the specified new queries. Each dynamic
 * query list requires its own thread, which is either sleeping
 * or updating the list with the freshest query update.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class DynamicQueryList extends QueryList implements Runnable {
    
    /** the thread that executes the queries */
    public Thread queryThread;
    
    /** the interval between refreshes */
    public int queryUpdateInterval;
    
    /** whether this query will execute */
    public boolean active = true;

    /**
     * Create a new DynamicQueryList with no query. This starts the
     * thread sleeping. 
     *
     * @param queryUpdateInterval the number of milliseconds between
     *      refreshing the query.
     */
    public DynamicQueryList(int queryUpdateInterval) {
        this.queryUpdateInterval = queryUpdateInterval;
        this.query = new EmptyQuery();
        queryThread = new Thread(this);
        queryThread.setName("DynQuery");
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
     * Executes the query. This sleeps for the query refresh interval
     * after the query has been run, and then repeats. If a different
     * query is set while this query is executing, that query is interrupted
     * and the new query is started.
     */
    public void run() {
        // keep waiting for queries
        while(true) {
            try {
                if(active) setQueryResults(query.doQuery());
                Thread.sleep(queryUpdateInterval);
            } catch (InterruptedException e) {
                // when interrupted, cancel this query and start the new one
            }
        }
    }
}
