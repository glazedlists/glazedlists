/**
 * O'Dell Business System 2
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

/**
 * A class that listens to task updates.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface TaskListener {
    
    /**
     * When there is a change in one of the tasks, this is called
     * to notify the listener of the change. Task listeners will always
     * be notified on the event dispatch thread so it is always safe to
     * execute Swing code in this method.
     */
    public void taskUpdated(TaskContext task);
    
}
