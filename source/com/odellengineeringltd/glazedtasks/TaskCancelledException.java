/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedtasks;

/**
 * When a task is cancelled, a TaskInterruptedException can be thrown
 * instead of another type of exception to tell the task manager that
 * the cancel was cleanly performed.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class TaskCancelledException extends InterruptedException {

    /**
     * Create a new TaskCancelledException with the specified cause.
     */
    public TaskCancelledException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Create a new TaskCancelledException with the specified cause.
     */
    public TaskCancelledException(Throwable cause) {
        super("Task was cancelled");
        initCause(cause);
    }
    
    /**
     * Create a new TaskCancelledException.
     */
    public TaskCancelledException(String message) {
        super(message);
    }
    
    /**
     * Create a new TaskCancelledException.
     */
    public TaskCancelledException() {
        super("Task was cancelled");
    }
}
