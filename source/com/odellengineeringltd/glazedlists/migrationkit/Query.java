/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.migrationkit;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.SortedSet;

/**
 * Knows how to retrieve a subset of the objects on a database. A
 * query gets those objects in the background. When an object is
 * updated in the system, a query list may prompt the query as to
 * whether the updated object matches the query. This may cause
 * an element in a list to be removed upon update if it no longer
 * matches a query's specifications, and it may cause an element
 * to be added to a query if it matches the query's specification. 
 *     
 * Queries are cancellable, this means that the thread they are
 * running on is interrupted and the results are discarded. Potentially
 * in the future a new Query class will be created that allows for
 * partial results of queries to be accumulated; however here we
 * are only interested in a complete result or nothing at all.
 *
 * Some implementations may be scheduled to run frequently, such as a query
 * that gets a set of remote data for a local cache.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Query {

    /**
     * Gets the name of this query for display in a logging message.
     */
    public String getName();
    
    /**
     * Starts this query on the action that may take a while. This is usually
     * run in a thread that is not the Swing event dispacher thread, so be
     * careful not to use logging methods etc. that touch Swing objects.
     *
     * This thread executing this method may be interupted, so it is necessary
     * to call Thread.interrupted() and throw an InterruptedException if that
     * method returns true.
     */
    public SortedSet doQuery() throws InterruptedException;

    /**
     * Tests whether this query would contain the specified object. When an
     * object is changed by a user it is necessary to see if it should be
     * added or removed from any lists.
     */
    public boolean matchesObject(Comparable object);
}
