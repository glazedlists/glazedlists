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
// for keeping lists of queries
import java.util.ArrayList;

/**
 * A static class that propogates user changes to lists. List change
 * listeners can register themselves with this class, and receive
 * list events whenever a user updates an object. Note that some
 * list change events should be <strong>discarded</strong> because
 * they do not apply to a given list.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class QueryListManager {
    
    private static ArrayList queryLists = new ArrayList();

    /**
     * Tells the Query List Manager to notify the specified listener
     * to all list changes throughout the JVM. This listener must
     * discard list events that it is not interested in.
     */
    public static void addQueryList(QueryList queryList) {
        queryLists.add(queryList);
    }
    
    /**
     * Called by GUI classes to force lists thoughout the system to
     * update their view of the specified object.
     */
    public static void notifyObjectUpdated(Comparable updated) {
        for(int i = 0; i < queryLists.size(); i++) {
            QueryList queryList = (QueryList)queryLists.get(i);
            queryList.notifyObjectUpdated(updated);
        }
    }
}
