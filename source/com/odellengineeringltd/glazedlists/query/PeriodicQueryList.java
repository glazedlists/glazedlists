/**
 * Glazed Lists
 * http://opensource.odellengineeringltd.com/glazedlists/
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
 * A query list that performs its query on an interval to keep it fresh.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class PeriodicQueryList extends QueryList {
    
    /** the task that updates the list */
    public TimerTask queryRefreshTask;

    /**
     * Create a new PeriodicQueryList which executes the specified query on
     * an interval. It is necessary to call getTimerTask() too add this timer
     * to a thread which can run it.
     */
    public PeriodicQueryList(Query query) {
        this.query = query;
        queryRefreshTask = new QueryRefreshTask();
    }
    
    /**
     * Gets the TimerTask. The caller should call the following to start the task:
     *     <code>long CACHE_UPDATE_INTERVAL = 1000*60*5; // five minutes</code>
     *     <code>Timer timer = new Timer(true);</code>
     *     <code>timer.scheduleAtFixedRate(queryList.getTimerTask(), 0, CACHE_UPDATE_INTERVAL);</code>
     */
    public TimerTask getTimerTask() {
        return queryRefreshTask;
    }
    
    /**
     * Class that updates the list by running the query.
     */
    class QueryRefreshTask extends TimerTask {
        public void run() {
            try {
                setQueryResults(query.doQuery());
            } catch(InterruptedException e) {
                System.err.println("Cache freshen interrupted " + query.getName());
                e.printStackTrace();
            }
        }
    }
}
